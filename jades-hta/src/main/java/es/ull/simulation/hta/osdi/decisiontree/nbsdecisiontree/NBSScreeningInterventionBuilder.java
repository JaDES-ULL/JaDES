package es.ull.simulation.hta.osdi.decisiontree.nbsdecisiontree;

import java.util.Set;

import es.ull.simulation.hta.MalformedSimulationModelException;
import es.ull.simulation.hta.osdi.decisiontree.BranchDestinationNode;
import es.ull.simulation.hta.osdi.decisiontree.factories.InterventionGenerator;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.DetectionInterventionWrapper;
import es.ull.simulation.hta.osdi.ontology.DiseaseWrapper;
import es.ull.simulation.hta.osdi.ontology.ModelWrapper;
import es.ull.simulation.hta.osdi.ontology.ParameterNatureType;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper.DeterministicParameterData;
import es.ull.simulation.hta.osdi.ontology.ResourceUsageSKOSCategory;
import es.ull.simulation.hta.params.StandardParameter;

public class NBSScreeningInterventionBuilder extends InterventionGenerator {
    /**
     * The disease wrapper associated with this part of the decision tree.
     */
    private final DiseaseWrapper disease;

    /**
     * The screening sensitivity parameter for the screening intervention.
     */
    private final ParameterWrapper sensitivityParam;
    /**
     * The screening specificity parameter for the screening intervention.
     */
    private final ParameterWrapper specificityParam;
    /**
     * The cost parameters associated with the screening intervention.
     */
    private final Set<ParameterWrapper> screeningCostParams;

    /**
     * Constructs a screening part for the given NBS tree model and part name.
     * It initializes the screening intervention parameters and builds the decision tree structure.
     *
     * @param parentTreeModel The parent tree model associated with this factory.
     * @param interventionIRI  The IRI of the screening intervention.
     * @throws MalformedSimulationModelException If there is an issue with the simulation model.
     * @throws UnsupportedOSDiFeatureException   If an unsupported OSDi feature is encountered.
     * @throws MalformedOSDiModelException       If the OSDi model is malformed.
     */
    public NBSScreeningInterventionBuilder(NBSModel parentTreeModel, DetectionInterventionWrapper intervention) throws MalformedSimulationModelException, UnsupportedOSDiFeatureException, MalformedOSDiModelException {
        super(parentTreeModel, intervention);
        final ModelWrapper modelWrapper = parentTreeModel.getModelWrapper();
        this.disease = parentTreeModel.getDiseaseWrapper();
        this.screeningCostParams = parentTreeModel.getOSDiWrapper().filterModelItemsWithSKOSCategory(intervention.getCosts(), ResourceUsageSKOSCategory.SCREENING);
        // TODO: Explore strategies in the intervention to find screening strategy and costs as well. 
        if (intervention.getSensitivityParameter().isEmpty()) {
            // If no sensitivity parameter is defined, create a default one
            DeterministicParameterData defaultSensitivityData = new DeterministicParameterData(1.0);
            this.sensitivityParam = new ParameterWrapper.SyntheticBuilder(modelWrapper, StandardParameter.SENSITIVITY.createName(intervention.getShortName()), defaultSensitivityData)
            .withSource("Assumption")
            .withDescription("No parameter defined for sensitivity of " + intervention.getShortName() + " . Assuming perfect sensitivity = 1.0.")
            .build();
        }
        else {
            this.sensitivityParam = intervention.getSensitivityParameter().get();
        }
        if (intervention.getSpecificityParameter().isEmpty()) {
            DeterministicParameterData defaultSpecificityData = new DeterministicParameterData(1.0);
            this.specificityParam = new ParameterWrapper.SyntheticBuilder(modelWrapper, StandardParameter.SPECIFICITY.createName(intervention.getShortName()), defaultSpecificityData)
            .withSource("Assumption")
            .withDescription("No parameter defined for specificity of " + intervention.getShortName() + " . Assuming perfect specificity = 1.0.")
            .build();
        }
        else {
            this.specificityParam = intervention.getSpecificityParameter().get();
        }
    }

    @Override
    public BranchDestinationNode generate(String name) throws MalformedOSDiModelException, MalformedSimulationModelException, UnsupportedOSDiFeatureException {
        final NBSModel parentTreeModel = (NBSModel) getParentTreeModel();
        final ParameterWrapper birthPrevalenceParam = parentTreeModel.getBirthPrevalence();
        final BranchDestinationNode screeningPopulationNode = new BranchDestinationNode(parentTreeModel, name);
        screeningPopulationNode.updateCosts(getInterventionWrapper(), Set.of(ResourceUsageSKOSCategory.FOLLOW_UP, ResourceUsageSKOSCategory.TREATMENT), true);
        screeningPopulationNode.addCostParameters(screeningCostParams);
        screeningPopulationNode.updateUtility(getInterventionWrapper(), Set.of(ResourceUsageSKOSCategory.FOLLOW_UP, ResourceUsageSKOSCategory.TREATMENT));
        // TODO: Until I find a better method, I will update effects only in subbranches selectively
        // screeningPopulationNode.updateEffects(this);

        if (requiresNode(sensitivityParam)) {
            final BranchDestinationNode affectedNode = new BranchDestinationNode(parentTreeModel, "Has the disease");
            screeningPopulationNode.link(affectedNode, birthPrevalenceParam);
            final BranchDestinationNode truePositiveDiseaseNode = parentTreeModel.getDiseaseGenerator().generate("True positive " + disease.getShortName());

            // TODO: Until I find a better method, I will update effects only in subbranches selectively
            truePositiveDiseaseNode.updateEffects(getInterventionWrapper());

            truePositiveDiseaseNode.addCostParameters(parentTreeModel.getDiagnosisCosts());
            affectedNode.link(truePositiveDiseaseNode, sensitivityParam);
            final BranchDestinationNode falseNegativeDiseaseNode = parentTreeModel.getDiseaseGenerator().generate("False negative " + disease.getShortName());
            affectedNode.link(falseNegativeDiseaseNode);
        }
        else {
            final BranchDestinationNode truePositiveDiseaseNode = parentTreeModel.getDiseaseGenerator().generate("True positive " + disease.getShortName());
            truePositiveDiseaseNode.updateEffects(getInterventionWrapper());
            truePositiveDiseaseNode.addCostParameters(parentTreeModel.getDiagnosisCosts());
            screeningPopulationNode.link(truePositiveDiseaseNode, birthPrevalenceParam);
        }
        if (requiresNode(specificityParam)) {
            final BranchDestinationNode notAffectedNode = new BranchDestinationNode(parentTreeModel, "Does not have disease");
            screeningPopulationNode.link(notAffectedNode);
            notAffectedNode.link(new BranchDestinationNode(parentTreeModel, "True negative"), specificityParam);
            final BranchDestinationNode falsePositivePayoff = new BranchDestinationNode(parentTreeModel, "False positive");
            falsePositivePayoff.addCostParameters(parentTreeModel.getDiagnosisCosts());
            notAffectedNode.link(falsePositivePayoff);
        } else {
            screeningPopulationNode.link(new BranchDestinationNode(parentTreeModel, "True negative"));
        }
        return screeningPopulationNode;

    }

    /**
     * Checks if the given screening parameter requires a node in the decision tree.
     * In general, deterministic parameters with a value of 1.0 do not require a node,
     * while second order and calculated parameters always require a node.
     * @param screeningParam The screening parameter to check.
     * @return true if the screening parameter requires a node, false otherwise.
     * @throws UnsupportedOSDiFeatureException If the parameter nature is unsupported.
     */
    private boolean requiresNode(ParameterWrapper screeningParam) throws UnsupportedOSDiFeatureException {
        if (screeningParam == null) {
            return false;
        }
        switch(screeningParam.getNature()) {
            case ParameterNatureType.DETERMINISTIC:
                if (((DeterministicParameterData)screeningParam.getParameterNatureData()).value() == 1.0) {
                    return false;
                }
                return true;
            case ParameterNatureType.SECOND_ORDER:
            case ParameterNatureType.CALCULATED:
                return true;
            default:
                throw new UnsupportedOSDiFeatureException("Unsupported parameter nature: " + screeningParam.getNature());
        }
    }
}
