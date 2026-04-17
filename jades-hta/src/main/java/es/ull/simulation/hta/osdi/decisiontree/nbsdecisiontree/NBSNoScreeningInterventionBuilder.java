package es.ull.simulation.hta.osdi.decisiontree.nbsdecisiontree;

import java.util.Set;

import es.ull.simulation.hta.MalformedSimulationModelException;
import es.ull.simulation.hta.osdi.decisiontree.BranchDestinationNode;
import es.ull.simulation.hta.osdi.decisiontree.factories.InterventionGenerator;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.DetectionInterventionWrapper;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper;
import es.ull.simulation.hta.osdi.ontology.ResourceUsageSKOSCategory;

public class NBSNoScreeningInterventionBuilder extends InterventionGenerator {
    /**
     * The parent tree model associated with this factory.
     */
    private final NBSModel parentTreeModel;

    /**
     * Constructs a no-screening part for the given NBS tree model and part name.
     * It initializes the no-screening intervention parameters and builds the decision tree structure.
     *
     * @param parentTreeModel The parent tree model associated with this factory.
     * @param intervention  The intervention wrapper for the no-screening intervention.
     * @throws MalformedSimulationModelException If there is an issue with the simulation model.
     * @throws UnsupportedOSDiFeatureException   If an unsupported OSDi feature is encountered.
     * @throws MalformedOSDiModelException       If the OSDi model is malformed.
     */
    public NBSNoScreeningInterventionBuilder(NBSModel parentTreeModel, DetectionInterventionWrapper intervention) throws MalformedSimulationModelException, UnsupportedOSDiFeatureException, MalformedOSDiModelException {
        super(parentTreeModel, intervention);
        this.parentTreeModel = parentTreeModel;
    }

    @Override
    public BranchDestinationNode generate(String name) throws MalformedOSDiModelException, MalformedSimulationModelException, UnsupportedOSDiFeatureException {
        final ParameterWrapper birthPrevalenceParam = parentTreeModel.getBirthPrevalence();
        final BranchDestinationNode noScreeningPopulationNode = new BranchDestinationNode(parentTreeModel, name);
        noScreeningPopulationNode.updateCosts(getInterventionWrapper(), Set.of(ResourceUsageSKOSCategory.FOLLOW_UP, ResourceUsageSKOSCategory.TREATMENT), true);
        noScreeningPopulationNode.updateUtility(getInterventionWrapper(), Set.of(ResourceUsageSKOSCategory.FOLLOW_UP, ResourceUsageSKOSCategory.TREATMENT));
        noScreeningPopulationNode.updateEffects(getInterventionWrapper());
        final BranchDestinationNode diseaseNode = parentTreeModel.getDiseaseGenerator().generate("Has the disease");
        diseaseNode.addCostParameters(parentTreeModel.getDiagnosisCosts());
        
        noScreeningPopulationNode.link(diseaseNode, birthPrevalenceParam);
        // Adds a default branch for those not affected by the disease
        noScreeningPopulationNode.link(new BranchDestinationNode(parentTreeModel, "Does not have disease"));
        return noScreeningPopulationNode;
    }
}
