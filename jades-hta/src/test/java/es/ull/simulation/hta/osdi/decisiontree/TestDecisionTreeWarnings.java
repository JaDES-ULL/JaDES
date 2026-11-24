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

    @Test
    @DisplayName("The frequency of an acute manifestation that is the target of the interventions (P64) is ignored: the tree evaluates a single episode (D1)")
    void warnsThatAnAcuteTargetIsASingleEpisode() throws Exception {
        try (LogCapture log = LogCapture.start()) {
            DecisionTreeTestSupport.loadModel(DecisionTreeCatalog.PAYOFFS, "TT_P64_Experiment").getTree();
            assertTrue(log.containsWarning("The acute manifestation TT_P64_ManA, the target of the interventions, defines a frequency (TT_P64_ManA_Frequency), which is ignored:"
                + " the decision tree evaluates a single episode, the one that every patient suffers at time 0 (a discrete event simulation also simulates the next episodes)."),
                "Warning not found: " + log.getMessages());
        }
    }

    @Test
    @DisplayName("The instant death of a chronic target (P67) is ignored: the patients survived its onset, as that of the disease")
    void warnsThatTheDeathOfAChronicTargetIsIgnored() throws Exception {
        try (LogCapture log = LogCapture.start()) {
            DecisionTreeTestSupport.loadModel(DecisionTreeCatalog.PAYOFFS, "TT_P67_Experiment").getTree();
            assertTrue(log.containsWarning("The instant death TT_P67_ManX_Death of TT_P67_ManX, the target of the interventions, is ignored:"
                + " every patient presents the target at time 0, so they survived its onset."),
                "Warning not found: " + log.getMessages());
        }
    }
}
