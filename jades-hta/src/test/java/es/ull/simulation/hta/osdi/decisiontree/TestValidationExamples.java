package es.ull.simulation.hta.osdi.decisiontree;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import es.ull.simulation.hta.osdi.ontology.OSDiTest;

/**
 * Validation suite of OSDi and JaDES (phase 18 of docs/osdi-1.3-diseno.md). Each validation example represents a published study, in a folder of
 * examples/ with its ontology and its expected results (expected-results.json). The suite generates the model of each example, compares its results
 * with the expected ones, and writes a report in target/validation/report.md.
 * <p>
 * It is not part of the normal build (tag "validation", excluded by default): run it with {@code mvn -pl jades-hta -am test -Pvalidation}.
 * </p>
 * <p>
 * Expected values: "published" values are compared with their tolerance (relative or absolute); values computed by hand ("hand") with the tolerance
 * of the tests (relative 1e-8). Measures: cost, lifeYears and qalys of an arm (assessed or comparator); incrementalCost, incrementalQalys and icer.
 * </p>
 */
@OSDiTest
@Tag("validation")
@DisplayName("Validation examples of OSDi and JaDES")
public class TestValidationExamples {
    /** The folder with the validation examples (relative to the module), which can be changed with this system property */
    private static final String EXAMPLES_PROPERTY = "jades.examples.dir";
    /** The report of the validation */
    private static final Path REPORT = Paths.get("target", "validation", "report.md");
    /** Relative tolerance of the values computed by hand, as in the tests */
    private static final double HAND_TOLERANCE = 1e-8;
    /** The rows of the report */
    private static final List<String> rows = new ArrayList<>();

    /**
     * The folders of the validation examples: those of the examples folder with an expected-results.json file
     */
    static Stream<Path> examples() throws Exception {
        final Path dir = Paths.get(System.getProperty(EXAMPLES_PROPERTY, "examples"));
        assertTrue(Files.isDirectory(dir), "The folder of the validation examples does not exist: " + dir.toAbsolutePath());
        final List<Path> examples = new ArrayList<>();
        try (Stream<Path> children = Files.list(dir)) {
            children.filter(p -> Files.isRegularFile(p.resolve("expected-results.json"))).sorted().forEach(examples::add);
        }
        assertFalse(examples.isEmpty(), "No validation example found in " + dir.toAbsolutePath());
        return examples.stream();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("examples")
    @DisplayName("The results of the example reproduce the expected ones")
    void reproducesExpectedResults(Path example) throws Exception {
        final JsonNode expected = new ObjectMapper().readTree(example.resolve("expected-results.json").toFile());
        final String name = example.getFileName().toString();
        final Path ontology = example.resolve(expected.get("ontology").asText());
        final SingleDiseaseAndPopulationModel model = DecisionTreeTestSupport.loadModelFromFile(ontology, expected.get("experiment").asText());
        final double[] outcomes = DecisionTreeTestSupport.readExcelOutcomes(model, Paths.get("target", "validation", name + ".xlsm"));
        final List<String> failures = new ArrayList<>();
        for (JsonNode value : expected.get("values")) {
            final String measure = value.get("measure").asText();
            final String arm = value.has("arm") ? value.get("arm").asText() : "";
            final double expectedValue = value.get("value").asDouble();
            final double actual = getValue(outcomes, measure, arm);
            final String source = value.get("source").asText();
            final double tolerance;
            final boolean relative;
            if ("published".equals(source)) {
                final JsonNode tol = value.get("tolerance");
                relative = tol.has("relative");
                tolerance = relative ? tol.get("relative").asDouble() : tol.get("absolute").asDouble();
            }
            else {
                relative = true;
                tolerance = HAND_TOLERANCE;
            }
            final double difference = actual - expectedValue;
            final boolean passes = relative ? Math.abs(difference) <= tolerance * Math.max(1.0, Math.abs(expectedValue)) : Math.abs(difference) <= tolerance;
            final String label = arm.isEmpty() ? measure : measure + " (" + arm + ")";
            rows.add(String.format(Locale.ROOT, "| %s | %s | %s | %.10g | %.10g | %.4g | %s%s | %s |", name, label, source, expectedValue, actual, difference,
                relative ? "relative " : "absolute ", fmt(tolerance), passes ? "yes" : "**no**"));
            if (!passes) {
                failures.add(label + " (" + source + "): expected " + expectedValue + " but found " + actual);
            }
        }
        assertTrue(failures.isEmpty(), name + ": " + failures);
    }

    private static String fmt(double value) {
        return String.format(Locale.ROOT, "%.3g", value);
    }

    /**
     * Returns a measure of the results of a model (see {@link DecisionTreeTestSupport#readExcelOutcomes})
     */
    private static double getValue(double[] outcomes, String measure, String arm) {
        final int offset;
        if ("assessed".equals(arm)) {
            offset = 0;
        }
        else if ("comparator".equals(arm)) {
            offset = 3;
        }
        else {
            offset = -1;
        }
        switch (measure) {
            case "cost": return outcomes[offset];
            case "lifeYears": return outcomes[offset + 1];
            case "qalys": return outcomes[offset + 2];
            case "incrementalCost": return outcomes[0] - outcomes[3];
            case "incrementalQalys": return outcomes[2] - outcomes[5];
            case "icer": return (outcomes[0] - outcomes[3]) / (outcomes[2] - outcomes[5]);
            default: throw new IllegalArgumentException("Unknown measure: " + measure);
        }
    }

    @AfterAll
    static void writeReport() throws Exception {
        final List<String> lines = new ArrayList<>();
        lines.add("# Validation of OSDi and JaDES");
        lines.add("");
        lines.add("Generated by `TestValidationExamples` (`mvn -pl jades-hta -am test -Pvalidation`). Difference = JaDES - expected.");
        lines.add("");
        lines.add("| Example | Measure | Source | Expected | JaDES | Difference | Tolerance | Passes |");
        lines.add("| --- | --- | --- | --- | --- | --- | --- | --- |");
        lines.addAll(rows);
        Files.createDirectories(REPORT.getParent());
        Files.write(REPORT, lines, StandardCharsets.UTF_8);
    }
}
