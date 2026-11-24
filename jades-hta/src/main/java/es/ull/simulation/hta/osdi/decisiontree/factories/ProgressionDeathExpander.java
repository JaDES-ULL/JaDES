package es.ull.simulation.hta.osdi.decisiontree.factories;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import es.ull.simulation.hta.MalformedSimulationModelException;
import es.ull.simulation.hta.osdi.decisiontree.BranchDestinationNode;
import es.ull.simulation.hta.osdi.decisiontree.BranchDestinationNode.PayoffWindow;
import es.ull.simulation.hta.osdi.decisiontree.Model;
import es.ull.simulation.hta.osdi.ontology.EffectWrapper;

/**
 * Expands the instant deaths (InstantDeathEffect) of the clinical progressions of a decision tree (phase 16 of docs/osdi-1.3-diseno.md). The instant
 * death of a progression happens at its onset: MAX(0, onset age - initial age), or the start of the model if it has no onset age. Below each leaf where
 * the node of the progression is active, the branch splits into death, with the probability of the magnitude of the effect, and survival:
 * <ul>
 * <li>The life expectancy of the death leaf is MIN(onset of the progression, life expectancy without the death), so its life years and payoffs are cut
 * at the onset (see {@link es.ull.simulation.hta.osdi.decisiontree.excelport.PayoffWrapper}). If the patient dies earlier, the death does not happen.</li>
 * <li>Nothing else is expanded after the death, as with the death caused by an adverse event (see {@link StrategyOutcomeExpander}).</li>
 * <li>Several progressions with an instant death along the path are expanded in the order of the path.</li>
 * </ul>
 * The instant death of the disease is not expanded here: it happens at the onset of the disease (see {@link DiseaseSubTreeGenerator}).
 */
final class ProgressionDeathExpander {

    private ProgressionDeathExpander() {
    }

    /**
     * Expands the instant deaths of the clinical progressions of a sub-tree below its leaves.
     * @param model The tree model
     * @param root The root of the sub-tree
     * @throws MalformedSimulationModelException If the tree cannot be built
     */
    static void expand(Model model, BranchDestinationNode root) throws MalformedSimulationModelException {
        final List<BranchDestinationNode> leaves = new ArrayList<>();
        final List<List<BranchDestinationNode>> paths = new ArrayList<>();
        collectLeaves(root, new ArrayList<>(), leaves, paths);
        for (int i = 0; i < leaves.size(); i++) {
            BranchDestinationNode leaf = leaves.get(i);
            for (BranchDestinationNode owner : paths.get(i)) {
                for (EffectWrapper effect : owner.getOnsetDeathEffects()) {
                    final String progression = owner.getHealthCondition().map(hc -> hc.getShortName()).orElse(owner.name());
                    final BranchDestinationNode death = new BranchDestinationNode(model, "Death from " + progression);
                    death.addEffects(List.of(effect));
                    death.setDeathTime(new PayoffWindow(owner, List.of(), Optional.empty()));
                    leaf.link(death, effect.getEffectMagnitude());
                    final BranchDestinationNode alive = new BranchDestinationNode(model, "Alive with " + progression);
                    leaf.link(alive);
                    leaf = alive;
                }
            }
        }
    }

    /**
     * Collects the leaves of a sub-tree whose path includes a node with instant deaths, together with such nodes, in the order of the path.
     */
    private static void collectLeaves(BranchDestinationNode node, List<BranchDestinationNode> owners, List<BranchDestinationNode> leaves, List<List<BranchDestinationNode>> paths) {
        final List<BranchDestinationNode> newOwners = new ArrayList<>(owners);
        if (!node.getOnsetDeathEffects().isEmpty()) {
            newOwners.add(node);
        }
        final List<BranchDestinationNode> children = new ArrayList<>(node.getSuccessors());
        if (node.getDefaultSuccessor() != null) {
            children.add(node.getDefaultSuccessor());
        }
        if (children.isEmpty()) {
            if (!newOwners.isEmpty()) {
                leaves.add(node);
                paths.add(newOwners);
            }
            return;
        }
        for (BranchDestinationNode child : children) {
            collectLeaves(child, newOwners, leaves, paths);
        }
    }
}
