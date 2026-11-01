package es.ull.simulation.hta.osdi.decisiontree;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import es.ull.simulation.hta.osdi.decisiontree.DecisionTreeCatalog.TreeCase;
import es.ull.simulation.hta.osdi.ontology.OSDiTest;

/**
 * Checks the results (cost, life years and QALYs of each intervention) of the Excel files generated for the decision trees.
 * The expected values were computed independently of the generator (see {@link DecisionTreeCatalog}).
 */
@OSDiTest
@DisplayName("Outcomes of the decision trees generated from OSDi")
public class TestDecisionTreeOutcomes {
    @TempDir
    Path tempDir;

    static Stream<TreeCase> cases() {
        return DecisionTreeCatalog.outcomeCases();
    }

    static Stream<TreeCase> knownIssues() {
        return DecisionTreeCatalog.outcomeKnownIssues();
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("cases")
    @DisplayName("The generated Excel produces the expected cost, life years and QALYs")
    void generatesExpectedOutcomes(TreeCase tc) throws Exception {
        checkOutcomes(tc);
    }

    /**
     * Cases that are not expected to pass until the corresponding issue of the generator is fixed
     */
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("knownIssues")
    @Tag("known-issue")
    @DisplayName("The generated Excel produces the expected cost, life years and QALYs (known issues of the generator)")
    void generatesExpectedOutcomesKnownIssues(TreeCase tc) throws Exception {
        checkOutcomes(tc);
    }

    private void checkOutcomes(TreeCase tc) throws Exception {
        final SingleDiseaseAndPopulationModel model = DecisionTreeTestSupport.loadModel(tc.ontology(), tc.experiment());
        final double[] actual = DecisionTreeTestSupport.readExcelOutcomes(model, tempDir.resolve(tc.experiment() + ".xlsm"));
        final double[] expected = new double[actual.length];
        System.arraycopy(tc.assessed(), 0, expected, 0, 3);
        System.arraycopy(tc.comparator(), 0, expected, 3, 3);
        final String hint = (tc.outcomesIssue() == null) ? "" : " [Known issue: " + tc.outcomesIssue().getDescription() + "]";

        final List<Executable> checks = new ArrayList<>();
        for (int i = 0; i < expected.length; i++) {
            final int index = i;
            checks.add(() -> assertTrue(DecisionTreeTestSupport.close(expected[index], actual[index]),
                "Unexpected " + DecisionTreeTestSupport.OUTCOME_NAMES[index] + ". Expected " + expected[index] + " but found " + actual[index] + hint));
        }
        assertAll(tc.toString(), checks);
    }
}
