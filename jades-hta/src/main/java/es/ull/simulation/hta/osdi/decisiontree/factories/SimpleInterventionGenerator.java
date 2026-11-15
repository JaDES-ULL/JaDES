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

    /**
     * Generates the sub-tree of the intervention: the sub-tree of the disease (see {@link DiseaseSubTreeGenerator}), whose root node also gets the
     * payoffs and effects of the intervention. The intervention applies from the start of the tree, i.e., from the appearance of its target (D1).
     * If the target is an acute manifestation, the annual costs and utilities of the intervention are ignored with a warning, as those of the
     * acute manifestation itself: the tree represents a single episode, so an annual payoff would be charged until death because of one episode.
     */
    @Override
    public BranchDestinationNode generate(String interventionName) throws MalformedOSDiModelException, MalformedSimulationModelException, UnsupportedOSDiFeatureException {
        final BranchDestinationNode interventionNode;
        boolean hasAcuteTarget = false;
        if (getParentTreeModel() instanceof SingleDiseaseModel) {
            final DiseaseSubTreeGenerator diseaseGenerator = ((SingleDiseaseModel)getParentTreeModel()).getDiseaseGenerator();
            interventionNode = diseaseGenerator.generate(interventionName);
            hasAcuteTarget = diseaseGenerator.hasAcuteTarget();
        }
        else {
            interventionNode = new BranchDestinationNode(getParentTreeModel(), interventionName);
        }
        // The payoffs of the intervention are collected apart, so that they can be filtered without affecting those of the disease
        final BranchDestinationNode interventionPayoffs = new BranchDestinationNode(getParentTreeModel(), interventionName);
        interventionPayoffs.updateCosts(this.getInterventionWrapper(), Set.of(ResourceUsageSKOSCategory.FOLLOW_UP, ResourceUsageSKOSCategory.TREATMENT), true);
        interventionPayoffs.updateUtility(this.getInterventionWrapper(), Set.of(ResourceUsageSKOSCategory.FOLLOW_UP, ResourceUsageSKOSCategory.TREATMENT));
        interventionPayoffs.updateEffects(this.getInterventionWrapper());
        if (hasAcuteTarget) {
            final String owner = "The intervention " + getInterventionWrapper().getShortName() + ", whose target is an acute manifestation,";
            DiseaseSubTreeGenerator.removeAnnualPayoffs(owner, interventionPayoffs.getCostParameters());
            DiseaseSubTreeGenerator.removeAnnualPayoffs(owner, interventionPayoffs.getDisutilityParameters());
        }
        interventionNode.addCostParameters(interventionPayoffs.getCostParameters());
        interventionNode.addDisutilityParameters(interventionPayoffs.getDisutilityParameters());
        interventionNode.addEffects(interventionPayoffs.getEffects());
        return interventionNode;
    }
}
