package es.ull.simulation.hta.osdi.decisiontree;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.ull.simulation.hta.osdi.LogCapture;
import es.ull.simulation.hta.osdi.ontology.OSDiTest;

/**
 * Checks the warnings of the generator of decision trees, which signal the assumptions made when building a model.
 */
@OSDiTest
@DisplayName("Warnings of the generator of decision trees")
public class TestDecisionTreeWarnings {

    @Test
    @DisplayName("A strategy with a guideline (P40) does not produce the outdated warning that guidelines are not supported (they are, since phase 5)")
    void noOutdatedGuidelineWarning() throws Exception {
        try (LogCapture log = LogCapture.start()) {
            DecisionTreeTestSupport.loadModel(DecisionTreeCatalog.PAYOFFS, "TT_P40_Experiment").getTree();
            assertFalse(log.containsWarning("Guideline-based strategies are not supported yet"), "Outdated warning found: " + log.getMessages());
        }
    }

    @Test
    @DisplayName("An intervention without target (P62) is assumed to target the disease, with a warning (D1)")
    void warnsAboutInterventionWithoutTarget() throws Exception {
        try (LogCapture log = LogCapture.start()) {
            DecisionTreeTestSupport.loadModel(DecisionTreeCatalog.PAYOFFS, "TT_P62_Experiment").getTree();
            assertTrue(log.containsWarning("The intervention TT_P62_InterventionTreat does not define its target (isInterventionOf or hasIntervention) in the model. It is assumed to target the disease TT_P62_Disease."),
                "Warning not found: " + log.getMessages());
        }
    }
}
