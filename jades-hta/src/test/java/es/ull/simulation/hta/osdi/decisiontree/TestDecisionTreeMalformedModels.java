package es.ull.simulation.hta.osdi.decisiontree;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import es.ull.simulation.hta.osdi.ontology.OSDiTest;
import es.ull.simulation.hta.osdi.decisiontree.DecisionTreeCatalog.MalformedCase;

/**
 * Checks that the models that cannot be converted into a decision tree are rejected with the right exception, instead of
 * silently generating a wrong tree.
 */
@OSDiTest
@DisplayName("Malformed or unsupported models are rejected")
public class TestDecisionTreeMalformedModels {
    @TempDir
    Path tempDir;

    static Stream<MalformedCase> cases() {
        return DecisionTreeCatalog.malformedCases();
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("cases")
    void rejectsModel(MalformedCase tc) {
        // The exception may be thrown when loading the experiment, when generating the tree or when generating the Excel file
        final Exception exception = assertThrows(tc.expectedException(), () -> {
            final SingleDiseaseAndPopulationModel model = DecisionTreeTestSupport.loadModel(tc.ontology(), tc.experiment());
            model.getTree();
            DecisionTreeTestSupport.generateExcel(model, tempDir.resolve(tc.experiment() + ".xlsm")).close();
        }, tc.toString());
        // The message checks that the model is rejected for the right reason
        assertTrue(exception.getMessage() != null && exception.getMessage().contains(tc.expectedMessage()),
            tc + ": unexpected message. Expected it to contain \"" + tc.expectedMessage() + "\" but found \"" + exception.getMessage() + "\"");
    }
}
