package es.ull.simulation.hta.osdi.decisiontree;

import java.util.ArrayList;

/**
 * Represents a choice node in a decision tree, which allows the user to choose between different interventions.
 * Each branch of the choice node is associated with a specific intervention and leads to a probability or payoff node.
 */
public class ChoiceNode implements DecisionTreeNode {
    /**
     * The list of successor nodes associated with this choice node.
     * Each link points to a DecisionTreeBranchDestinationComponent that represents a possible intervention.
     */
    private final ArrayList<BranchDestinationNode> successors;
    /**
     * The parent tree model that this part belongs to.
     */
    protected final Model parentTreeModel;

    /**
     * A choice node represents a decision point in a decision tree where the user can choose between different interventions.
     */
    public ChoiceNode(Model parentTreeModel) {
        this.successors = new ArrayList<>();
        this.parentTreeModel = parentTreeModel;
    }

    @Override
    public void addSuccessor(BranchDestinationNode successor) {
        if (successor == null) {
            throw new IllegalArgumentException("Successor cannot be null");
        }
        successors.add(successor);
    }
    
    @Override
    public ArrayList<BranchDestinationNode> getSuccessors() {
        return successors;
    }
}
