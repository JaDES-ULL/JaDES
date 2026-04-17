package es.ull.simulation.hta.osdi.decisiontree.factories;

import java.util.Set;

import es.ull.simulation.hta.MalformedSimulationModelException;
import es.ull.simulation.hta.osdi.decisiontree.BranchDestinationNode;
import es.ull.simulation.hta.osdi.decisiontree.Model;
import es.ull.simulation.hta.osdi.decisiontree.SingleDiseaseModel;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.InterventionWrapper;
import es.ull.simulation.hta.osdi.ontology.ResourceUsageSKOSCategory;

/**
 * A simple intervention for a single disease and population model.
 */
public class SimpleInterventionGenerator extends InterventionGenerator {

    /**
     * Constructs a new GenericInterventionTreePart with the specified context and part name.
     *
     * @param parentTreeModel The parent tree model associated with this factory.
     * @throws MalformedSimulationModelException If there is an issue with the simulation model.
     * @throws UnsupportedOSDiFeatureException   If an unsupported OSDi feature is encountered.
     * @throws MalformedOSDiModelException       If the OSDi model is malformed.
     */
    public SimpleInterventionGenerator(Model parentTreeModel, InterventionWrapper interventionWrapper) throws MalformedOSDiModelException, MalformedSimulationModelException, UnsupportedOSDiFeatureException {
        super(parentTreeModel, interventionWrapper);
    }

    @Override
    public BranchDestinationNode generate(String interventionName) throws MalformedOSDiModelException, MalformedSimulationModelException, UnsupportedOSDiFeatureException {
        final BranchDestinationNode interventionNode;
        if (getParentTreeModel() instanceof SingleDiseaseModel) {
            interventionNode = ((SingleDiseaseModel)getParentTreeModel()).getDiseaseGenerator().generate(interventionName);
        }
        else {
            interventionNode = new BranchDestinationNode(getParentTreeModel(), interventionName);
        }
        interventionNode.updateCosts(this.getInterventionWrapper(), Set.of(ResourceUsageSKOSCategory.FOLLOW_UP, ResourceUsageSKOSCategory.TREATMENT), true);
        interventionNode.updateUtility(this.getInterventionWrapper(), Set.of(ResourceUsageSKOSCategory.FOLLOW_UP, ResourceUsageSKOSCategory.TREATMENT));
        interventionNode.updateEffects(this.getInterventionWrapper());
        return interventionNode;
    }
}
