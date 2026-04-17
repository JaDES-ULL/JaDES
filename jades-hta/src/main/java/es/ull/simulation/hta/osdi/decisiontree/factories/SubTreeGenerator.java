package es.ull.simulation.hta.osdi.decisiontree.factories;

import es.ull.simulation.hta.MalformedSimulationModelException;
import es.ull.simulation.hta.osdi.decisiontree.BranchDestinationNode;
import es.ull.simulation.hta.osdi.decisiontree.Model;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;

public interface SubTreeGenerator {

    /**
     * Generates a part of the tree
     * @param name The name of the node to be generated.
     * @return The generated part of the decision tree node.
     * @throws MalformedOSDiModelException If the OSDi model is malformed.
     * @throws MalformedSimulationModelException If the simulation model is malformed.
     * @throws UnsupportedOSDiFeatureException If the OSDi feature is unsupported.
     */
    public BranchDestinationNode generate(String name) throws MalformedOSDiModelException, MalformedSimulationModelException, UnsupportedOSDiFeatureException;

    /**
     * Returns the parent tree model associated with this factory.
     * @return The parent tree model associated with this factory.
     */
    public Model getParentTreeModel();

}
