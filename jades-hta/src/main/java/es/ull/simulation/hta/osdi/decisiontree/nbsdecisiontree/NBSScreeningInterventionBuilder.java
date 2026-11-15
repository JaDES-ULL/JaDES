package es.ull.simulation.hta.osdi.decisiontree.nbsdecisiontree;

import java.util.Optional;
import java.util.Set;

import es.ull.simulation.hta.MalformedSimulationModelException;
import es.ull.simulation.hta.osdi.OSDiLogger;
import es.ull.simulation.hta.osdi.decisiontree.BranchDestinationNode;
import es.ull.simulation.hta.osdi.decisiontree.factories.InterventionGenerator;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.DetectionInterventionWrapper;
import es.ull.simulation.hta.osdi.ontology.DetectionStrategyWrapper;
import es.ull.simulation.hta.osdi.ontology.DiseaseWrapper;
import es.ull.simulation.hta.osdi.ontology.ModelWrapper;
import es.ull.simulation.hta.osdi.ontology.ParameterNatureType;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper.DeterministicParameterData;
import es.ull.simulation.hta.osdi.ontology.ResourceUsageSKOSCategory;
import es.ull.simulation.hta.params.StandardParameter;

public class NBSScreeningInterventionBuilder extends InterventionGenerator {
    private final static OSDiLogger log = OSDiLogger.getLogger(NBSScreeningInterventionBuilder.class);
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
     * The detection strategies of the screening arm, and the costs they produce.
     */
    private final NBSDetectionArm arm;

    /**
     * Constructs a screening part for the given NBS tree model and part name.
     * The sensitivity and specificity of the screening are those of the screening strategy of the arm (see {@link NBSDetectionArm}). If the arm has no
     * screening strategy, or the strategy does not define them, they are assumed to be perfect (1), with a warning.
     *
     * @param parentTreeModel The parent tree model associated with this factory.
     * @param intervention  The screening intervention.
     * @throws MalformedSimulationModelException If there is an issue with the simulation model.
     * @throws UnsupportedOSDiFeatureException   If an unsupported OSDi feature is encountered.
     * @throws MalformedOSDiModelException       If the OSDi model is malformed.
     */
    public NBSScreeningInterventionBuilder(NBSModel parentTreeModel, DetectionInterventionWrapper intervention) throws MalformedSimulationModelException, UnsupportedOSDiFeatureException, MalformedOSDiModelException {
        super(parentTreeModel, intervention);
        final ModelWrapper modelWrapper = parentTreeModel.getModelWrapper();
        this.disease = parentTreeModel.getDiseaseWrapper();
        this.arm = parentTreeModel.getDetectionArm(intervention);
        final Optional<DetectionStrategyWrapper> screeningStrategy = arm.getScreeningStrategy();
        if (screeningStrategy.isEmpty()) {
            log.warn("The arm of the screening intervention " + intervention.getShortName() + " has no screening strategy. The screening is assumed to be perfect and to have no cost, apart from the own screening costs of the intervention.");
        }
        final Optional<ParameterWrapper> sensitivity = screeningStrategy.isPresent() ? screeningStrategy.get().getSensitivityParameter() : Optional.empty();
        final Optional<ParameterWrapper> specificity = screeningStrategy.isPresent() ? screeningStrategy.get().getSpecificityParameter() : Optional.empty();
        if (sensitivity.isEmpty()) {
            // If no sensitivity parameter is defined, create a default one
            log.warn("No sensitivity defined for the screening of " + intervention.getShortName() + ". Assuming perfect sensitivity = 1.0.");
            DeterministicParameterData defaultSensitivityData = new DeterministicParameterData(1.0);
            this.sensitivityParam = new ParameterWrapper.SyntheticBuilder(modelWrapper, StandardParameter.SENSITIVITY.createName(intervention.getShortName()), defaultSensitivityData)
            .withSource("Assumption")
            .withDescription("No parameter defined for sensitivity of " + intervention.getShortName() + " . Assuming perfect sensitivity = 1.0.")
            .build();
        }
        else {
            this.sensitivityParam = sensitivity.get();
        }
        if (specificity.isEmpty()) {
            log.warn("No specificity defined for the screening of " + intervention.getShortName() + ". Assuming perfect specificity = 1.0.");
            DeterministicParameterData defaultSpecificityData = new DeterministicParameterData(1.0);
            this.specificityParam = new ParameterWrapper.SyntheticBuilder(modelWrapper, StandardParameter.SPECIFICITY.createName(intervention.getShortName()), defaultSpecificityData)
            .withSource("Assumption")
            .withDescription("No parameter defined for specificity of " + intervention.getShortName() + " . Assuming perfect specificity = 1.0.")
            .build();
        }
        else {
            this.specificityParam = specificity.get();
        }
    }

    /**
     * Generates the sub-tree of the screening arm. The whole population pays the screening costs of the arm; the true and false positives pay its
     * diagnosis costs (see {@link NBSDetectionArm}). The false negatives do not pay a diagnosis: what happens with them depends on the disease
     * (later clinical diagnosis, or death without diagnosis), which is pending (see docs/osdi-1.3-diseno.md). The strategy replacements of the arm
     * also apply to the sub-tree of the disease.
     */
    @Override
    public BranchDestinationNode generate(String name) throws MalformedOSDiModelException, MalformedSimulationModelException, UnsupportedOSDiFeatureException {
        final NBSModel parentTreeModel = (NBSModel) getParentTreeModel();
        final ParameterWrapper birthPrevalenceParam = parentTreeModel.getBirthPrevalence();
        final BranchDestinationNode screeningPopulationNode = new BranchDestinationNode(parentTreeModel, name);
        screeningPopulationNode.updateCosts(getInterventionWrapper(), Set.of(ResourceUsageSKOSCategory.FOLLOW_UP, ResourceUsageSKOSCategory.TREATMENT), true);
        arm.addScreeningCosts(screeningPopulationNode);
        screeningPopulationNode.updateUtility(getInterventionWrapper(), Set.of(ResourceUsageSKOSCategory.FOLLOW_UP, ResourceUsageSKOSCategory.TREATMENT));
        // TODO: Until I find a better method, I will update effects only in subbranches selectively
        // screeningPopulationNode.updateEffects(this);

        if (requiresNode(sensitivityParam)) {
            final BranchDestinationNode affectedNode = new BranchDestinationNode(parentTreeModel, "Has the disease");
            screeningPopulationNode.link(affectedNode, birthPrevalenceParam);
            final BranchDestinationNode truePositiveDiseaseNode = parentTreeModel.getDiseaseGenerator().generate("True positive " + disease.getShortName(), arm.getReplacements());

            // TODO: Until I find a better method, I will update effects only in subbranches selectively
            truePositiveDiseaseNode.updateEffects(getInterventionWrapper());

            arm.addDiagnosisCosts(truePositiveDiseaseNode);
            affectedNode.link(truePositiveDiseaseNode, sensitivityParam);
            // TODO: Decide what happens with the false negatives (see docs/osdi-1.3-diseno.md)
            final BranchDestinationNode falseNegativeDiseaseNode = parentTreeModel.getDiseaseGenerator().generate("False negative " + disease.getShortName(), arm.getReplacements());
            affectedNode.link(falseNegativeDiseaseNode);
        }
        else {
            final BranchDestinationNode truePositiveDiseaseNode = parentTreeModel.getDiseaseGenerator().generate("True positive " + disease.getShortName(), arm.getReplacements());
            truePositiveDiseaseNode.updateEffects(getInterventionWrapper());
            arm.addDiagnosisCosts(truePositiveDiseaseNode);
            screeningPopulationNode.link(truePositiveDiseaseNode, birthPrevalenceParam);
        }
        if (requiresNode(specificityParam)) {
            final BranchDestinationNode notAffectedNode = new BranchDestinationNode(parentTreeModel, "Does not have disease");
            screeningPopulationNode.link(notAffectedNode);
            notAffectedNode.link(new BranchDestinationNode(parentTreeModel, "True negative"), specificityParam);
            final BranchDestinationNode falsePositivePayoff = new BranchDestinationNode(parentTreeModel, "False positive");
            arm.addDiagnosisCosts(falsePositivePayoff);
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
