package es.ull.simulation.hta.osdi.decisiontree;

import java.util.ArrayList;

import es.ull.simulation.hta.MalformedSimulationModelException;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper;

/**
 * Represents a component in a decision tree.
 */
public interface DecisionTreeNode {

    /**
     * Adds a successor to this node.
     * @param successor The successor to be added, which can be a probability node or a payoff.
     * @throws MalformedSimulationModelException If the successor is null or if it is a default branch and a default successor already exists.
     */
    public void addSuccessor(BranchDestinationNode successor)  throws MalformedSimulationModelException;

    default public void link(BranchDestinationNode destination) throws MalformedSimulationModelException {
        link(destination, null);
    }

    default public void link(BranchDestinationNode destination, ParameterWrapper probability) throws MalformedSimulationModelException {
        if (destination == null) {
            throw new IllegalArgumentException("Destination node must not be null");
        }
        destination.setPredecessor(this);
        if (probability != null) {
            destination.setProbability(probability);
        }
        addSuccessor(destination);
    }

    
    /**
     * Returns the list of successors of this node.
     * This list contains all the components that can be reached from this node, such as probability nodes or payoffs.
     * @return An ArrayList of DecisionTreeBranchDestinationComponent representing the successors of this node.
     */
    public ArrayList<BranchDestinationNode> getSuccessors();

}
