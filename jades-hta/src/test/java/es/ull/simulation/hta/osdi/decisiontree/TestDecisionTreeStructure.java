package es.ull.simulation.hta.osdi.decisiontree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import es.ull.simulation.hta.osdi.ontology.OSDiTest;
import es.ull.simulation.hta.osdi.decisiontree.DecisionTreeCatalog.TreeCase;

/**
 * Checks the structure of the decision trees generated from OSDi, without generating the Excel file. The test does not depend on
 * the order in which the tree branches: each arm must contain exactly one leaf for every feasible combination of developments and
 * manifestations, with the right probability.
 */
@OSDiTest
@DisplayName("Structure of the decision trees generated from OSDi")
public class TestDecisionTreeStructure {

    static Stream<TreeCase> cases() {
        return DecisionTreeCatalog.structureCases();
    }

    static Stream<TreeCase> knownIssues() {
        return DecisionTreeCatalog.structureKnownIssues();
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("cases")
    @DisplayName("Each arm has one leaf for every combination of progressions")
    void generatesExpectedLeaves(TreeCase tc) throws Exception {
        checkLeaves(tc);
    }

    /**
     * Cases that are not expected to pass until the corresponding issue of the generator is fixed
     */
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("knownIssues")
    @Tag("known-issue")
    @DisplayName("Each arm has one leaf for every combination of progressions (known issues of the generator)")
    void generatesExpectedLeavesKnownIssues(TreeCase tc) throws Exception {
        checkLeaves(tc);
    }

    private void checkLeaves(TreeCase tc) throws Exception {
        final SingleDiseaseAndPopulationModel model = DecisionTreeTestSupport.loadModel(tc.ontology(), tc.experiment());
        final ArrayList<BranchDestinationNode> interventions = model.getTree().getSuccessors();
        assertEquals(2, interventions.size(), "The tree must have one branch per intervention");
        final String hint = (tc.structureIssue() == null) ? "" : " [Known issue: " + tc.structureIssue().getDescription() + "]";

        for (int i = 0; i < interventions.size(); i++) {
            final String arm = interventions.get(i).name();
            final Map<String, Double> leaves = DecisionTreeTestSupport.readLeaves(interventions.get(i));
            // Same combinations of progressions in both arms
            assertEquals(tc.leaves().keySet(), leaves.keySet(), "Unexpected leaves in the arm " + arm + hint);
            final double sum = leaves.values().stream().mapToDouble(Double::doubleValue).sum();
            assertTrue(DecisionTreeTestSupport.close(1.0, sum), "The probabilities of the leaves of the arm " + arm + " sum " + sum + hint);
        }
        // The probabilities are checked only for the comparator, since the effects of the intervention are not applied when reading the tree
        final Map<String, Double> comparatorLeaves = DecisionTreeTestSupport.readLeaves(interventions.get(DecisionTreeTestSupport.COMPARATOR));
        for (Map.Entry<String, Double> expected : tc.leaves().entrySet()) {
            final double actual = comparatorLeaves.get(expected.getKey());
            assertTrue(DecisionTreeTestSupport.close(expected.getValue(), actual), "Probability of the leaf '" + expected.getKey()
                + "' in the comparator arm. Expected " + expected.getValue() + " but found " + actual + hint);
        }
    }
}
