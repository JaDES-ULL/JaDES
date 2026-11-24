package es.ull.simulation.hta.osdi;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.ull.simulation.hta.osdi.ontology.OSDiTest;

/**
 * Checks the genExcel command of {@link OSDiMain} (phase 18 of docs/osdi-1.3-diseno.md): it must register the factories of decision trees and
 * accept file paths without the file:/// prefix, so that the Excel of a model can be generated outside the tests.
 */
@OSDiTest
@DisplayName("genExcel command of OSDiMain")
public class TestOSDiMainGenExcel {

    private static Path copyTestOntology(Path dir) throws Exception {
        final Path ontology = dir.resolve("OSDi_test.ttl");
        try (InputStream stream = Objects.requireNonNull(TestOSDiMainGenExcel.class.getResourceAsStream("/OSDi_test.ttl"))) {
            Files.copy(stream, ontology);
        }
        return ontology;
    }

    private static void genExcel(String osdiPath, Path excel) {
        OSDiMain.main(new String[] { "genExcel", "--osdiPath", osdiPath, "--osdiExperiment", "TEST_Experiment1", "--excelPath", excel.toString() });
    }

    @Test
    @DisplayName("genExcel accepts an absolute file path without file:///")
    void acceptsAbsolutePath(@TempDir Path dir) throws Exception {
        final Path ontology = copyTestOntology(dir);
        final Path excel = dir.resolve("absolute.xlsm");
        // Forward slashes, as in a Unix path or a path written by hand on Windows
        genExcel(ontology.toAbsolutePath().toString().replace('\\', '/'), excel);
        assertTrue(Files.isRegularFile(excel), "The Excel file was not generated from " + ontology);
    }

    @Test
    @DisplayName("genExcel accepts a relative file path")
    void acceptsRelativePath(@TempDir Path dir) throws Exception {
        final Path ontology = copyTestOntology(dir);
        final Path excel = dir.resolve("relative.xlsm");
        final Path relative = Paths.get("").toAbsolutePath().relativize(ontology.toAbsolutePath());
        genExcel(relative.toString(), excel);
        assertTrue(Files.isRegularFile(excel), "The Excel file was not generated from " + relative);
    }
}
