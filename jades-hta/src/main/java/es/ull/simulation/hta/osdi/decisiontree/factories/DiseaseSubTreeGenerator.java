package es.ull.simulation.hta.osdi.decisiontree.factories;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import org.semanticweb.owlapi.model.IRI;

import es.ull.simulation.hta.MalformedSimulationModelException;
import es.ull.simulation.hta.osdi.OSDiLogger;
import es.ull.simulation.hta.osdi.decisiontree.BranchDestinationNode;
import es.ull.simulation.hta.osdi.decisiontree.Model;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.ClinicalProgressionType;
import es.ull.simulation.hta.osdi.ontology.ClinicalProgressionWrapper;
import es.ull.simulation.hta.osdi.ontology.DiseaseWrapper;
import es.ull.simulation.hta.osdi.ontology.EffectType;
import es.ull.simulation.hta.osdi.ontology.EffectWrapper;
import es.ull.simulation.hta.osdi.ontology.IModelItemWrapper;
import es.ull.simulation.hta.osdi.ontology.InterventionWrapper;
import es.ull.simulation.hta.osdi.ontology.ModelWrapper;
import es.ull.simulation.hta.osdi.ontology.OSDiClass;
import es.ull.simulation.hta.osdi.ontology.OSDiDataItemType;
import es.ull.simulation.hta.osdi.ontology.OSDiObjectProperty;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper.SpecificInformationForCost;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper.SpecificInformationForUtility;
import es.ull.simulation.hta.osdi.ontology.PathwayWrapper;
import es.ull.simulation.hta.osdi.ontology.PopulationWrapper;
import es.ull.simulation.hta.osdi.ontology.ProgressionCombinationRuleType;
import es.ull.simulation.hta.osdi.ontology.ProgressionCombinationRuleWrapper;
import es.ull.simulation.hta.osdi.ontology.ResourceUsageSKOSCategory;
import es.ull.simulation.hta.osdi.ontology.TemporalConstraint;

/**
 * A sub-tree builder for a specific disease within a decision tree model.
 * 
 * The decision tree structure starts from a disease node, which may lead to various clinical progression nodes based on the disease's defined progressions. First,
 * coexistent developments are processed, followed by sets of alternative developments. Each development may lead to manifestations, which are also processed in a similar manner.
 * 
 * Developments always produce nodes. If they are part of an alternative set (with two or more members), the node is a probability node together with the other
 * developments in the set; a development without risk in such a set becomes the default branch (1 - the sum of the rest). Otherwise, they produce two probability
 * nodes: one for the development occurring, and another for it not occurring.
 *
 * A development or chronic manifestation without risk that does not belong to an alternative set with two or more members (i.e., not affected by any rule, affected
 * by a coexistent rule, or by an alternative rule with a single member) is always active in its context: it produces no chance node. Its node is linked as the only
 * (default) branch of its parent, with probability 1, so its payoffs apply to every leaf of the sub-tree that hangs from it. A manifestation without risk that
 * depends on a development is only active in the branches where such development is active.
 *
 * An acute manifestation represents a single episode between the initial age of the population and death (see {@link #checkAcuteManifestation} and
 * {@link #generateNode}). It produces the same nodes as a chronic manifestation: its risk is the probability that the episode happens, and an acute
 * manifestation without risk always happens in its context. Initial proportions of progressions (hasInitialProportion) are not supported yet.
 *
 * Manifestations declared as subprogressions of one or more developments are assumed to appear only together with such developments. Conversely, "orphan" manifestations 
 * are supposed to appear independently of any development. 
 * 
 */
public class DiseaseSubTreeGenerator implements SubTreeGenerator {
    private final static OSDiLogger log = OSDiLogger.getLogger(DiseaseSubTreeGenerator.class);
    /**
     * The parent tree model that this part belongs to.
     */
    protected final Model parentTreeModel;
    /**
     * The wrapper of the disease individual in this model.
     */
    private final DiseaseWrapper diseaseWrapper;
    /**
     * The clinical progression element that the interventions of the model target, or an empty optional if they target the disease (see
     * {@link #resolveInterventionTarget()}).
     */
    private final Optional<ClinicalProgressionWrapper> targetProgression;

    /**
     * Constructs a disease part for the given NBS tree model and part name.
     *
     * @param parentTreeModel The parent NBS tree model that this part belongs to.
     * @param diseaseWrapper The wrapper of the disease individual in this model.
     * @throws UnsupportedOSDiFeatureException 
     * @throws MalformedSimulationModelException 
     * @throws MalformedOSDiModelException 
     */
    public DiseaseSubTreeGenerator(Model parentTreeModel, DiseaseWrapper diseaseWrapper) throws MalformedSimulationModelException, UnsupportedOSDiFeatureException, MalformedOSDiModelException {
        this.diseaseWrapper = diseaseWrapper;
        this.parentTreeModel = parentTreeModel;
        this.targetProgression = resolveInterventionTarget();
    }

    /**
     * Returns the clinical progression element that the interventions of the model target, or an empty optional if they target the disease.
     * @return The clinical progression element that the interventions of the model target, or an empty optional if they target the disease.
     */
    public Optional<ClinicalProgressionWrapper> getTargetProgression() {
        return targetProgression;
    }

    /**
     * Returns true if the interventions of the model target an acute manifestation.
     * @return true if the interventions of the model target an acute manifestation.
     */
    public boolean hasAcuteTarget() {
        return targetProgression.isPresent() && ClinicalProgressionType.ACUTE_MANIFESTATION.equals(targetProgression.get().getClinicalProgressionType());
    }

    /**
     * Resolves the common target of the interventions of the model (D1 in docs/osdi-1.3-diseno.md): the health condition to which they are applied.
     * <p>
     * The target of an intervention is read from isInterventionOf and from its inverse, hasIntervention, since the reasoner does not infer inverse
     * properties. Only the targets included in the model are considered: the ontology states that every intervention has exactly one target, but
     * the same intervention individual may be shared by several models. Hence:
     * <ul>
     * <li>An intervention without target in the model is assumed to target the disease of the model, with a warning.</li>
     * <li>An intervention with more than one target in the model is malformed.</li>
     * <li>The target must be the disease of the model or one of its progressions; otherwise, the model is malformed.</li>
     * <li>The compared interventions must have the same target; otherwise, the model is malformed.</li>
     * </ul>
     * Stages and manifestations that depend on a development are not supported as targets in decision trees: a patient that presents a manifestation
     * of a development also presents the development, but the manifestation may depend on several developments.
     * </p>
     * @return The clinical progression element that the interventions target, or an empty optional if they target the disease.
     * @throws MalformedOSDiModelException If an intervention has more than one target, its target does not belong to the disease of the model, or the
     * interventions have different targets.
     * @throws UnsupportedOSDiFeatureException If the target is a stage or a manifestation that depends on a development.
     */
    private Optional<ClinicalProgressionWrapper> resolveInterventionTarget() throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        final ModelWrapper modelWrapper = parentTreeModel.getExperimentWrapper().getModelWrapper();
        final IRI diseaseIRI = diseaseWrapper.getIndividualIRI();
        // The health conditions that may be targeted: the disease and its progressions
        final Map<IRI, ClinicalProgressionWrapper> progressions = new LinkedHashMap<>();
        for (ClinicalProgressionWrapper progression : diseaseWrapper.getDevelopments()) {
            progressions.put(progression.getIndividualIRI(), progression);
        }
        for (ClinicalProgressionWrapper progression : diseaseWrapper.getManifestations()) {
            progressions.put(progression.getIndividualIRI(), progression);
        }
        for (ClinicalProgressionWrapper progression : diseaseWrapper.getStages()) {
            progressions.put(progression.getIndividualIRI(), progression);
        }
        final List<IRI> healthConditions = new ArrayList<>();
        healthConditions.add(diseaseIRI);
        healthConditions.addAll(progressions.keySet());

        IRI commonTarget = null;
        InterventionWrapper firstIntervention = null;
        for (InterventionWrapper intervention : new TreeSet<>(modelWrapper.getInterventionIndividuals())) {
            final IRI interventionIRI = intervention.getIndividualIRI();
            final Set<IRI> targets = new LinkedHashSet<>();
            for (IModelItemWrapper target : modelWrapper.getWrappersForProperty(interventionIRI, OSDiObjectProperty.IS_INTERVENTION_OF)) {
                targets.add(target.getIndividualIRI());
            }
            for (IRI healthCondition : healthConditions) {
                for (IModelItemWrapper item : modelWrapper.getWrappersForProperty(healthCondition, OSDiObjectProperty.HAS_INTERVENTION)) {
                    if (item.getIndividualIRI().equals(interventionIRI)) {
                        targets.add(healthCondition);
                    }
                }
            }
            final IRI target;
            if (targets.isEmpty()) {
                log.warn("The intervention " + intervention.getShortName() + " does not define its target (isInterventionOf or hasIntervention) in the model. It is assumed to target the disease " + diseaseWrapper.getShortName() + ".");
                target = diseaseIRI;
            }
            else if (targets.size() > 1) {
                throw new MalformedOSDiModelException("The intervention " + intervention.getShortName() + " defines more than one target in the model: " + targets + ". Every intervention must have exactly one target.");
            }
            else {
                target = targets.iterator().next();
            }
            if (!target.equals(diseaseIRI) && !progressions.containsKey(target)) {
                throw new MalformedOSDiModelException("The target " + target + " of the intervention " + intervention.getShortName() + " is neither the disease " + diseaseWrapper.getShortName() + " nor one of its progressions.");
            }
            if (commonTarget == null) {
                commonTarget = target;
                firstIntervention = intervention;
            }
            else if (!commonTarget.equals(target)) {
                throw new MalformedOSDiModelException("The compared interventions must have the same target, but " + firstIntervention.getShortName() + " targets " + commonTarget
                    + " and " + intervention.getShortName() + " targets " + target + ".");
            }
        }
        if (commonTarget == null || commonTarget.equals(diseaseIRI)) {
            return Optional.empty();
        }
        final ClinicalProgressionWrapper target = progressions.get(commonTarget);
        if (ClinicalProgressionType.STAGE.equals(target.getClinicalProgressionType())) {
            throw new UnsupportedOSDiFeatureException("The interventions of the model target the stage " + target.getShortName() + ". Stages are not supported in decision trees.");
        }
        for (ClinicalProgressionWrapper development : diseaseWrapper.getDevelopments()) {
            if (diseaseWrapper.getManifestationsLinkedToDevelopment(development).contains(target)) {
                throw new UnsupportedOSDiFeatureException("The interventions of the model target the manifestation " + target.getShortName() + ", which depends on the development " + development.getShortName()
                    + ". A manifestation that depends on a development is not supported as the target of the interventions in decision trees.");
            }
        }
        return Optional.of(target);
    }

    @Override
    public Model getParentTreeModel() {
        return parentTreeModel;
    }

    /**
     * Returns the wrapper of the disease individual in this model.
     * @return The wrapper of the disease individual in this model.
     */
    public DiseaseWrapper getDiseaseWrapper() {
        return diseaseWrapper;
    }

    @Override
    public BranchDestinationNode generate(String name) throws MalformedOSDiModelException, MalformedSimulationModelException, UnsupportedOSDiFeatureException {
        for (ClinicalProgressionWrapper manifestation : diseaseWrapper.getManifestations()) {
            if (ClinicalProgressionType.ACUTE_MANIFESTATION.equals(manifestation.getClinicalProgressionType())) {
                checkAcuteManifestation(manifestation);
            }
        }
        for (PopulationWrapper population : parentTreeModel.getExperimentWrapper().getModelWrapper().getPopulationIndividuals()) {
            // TODO: Support initial proportions in decision trees (see docs/known-limitations.md)
            if (!population.getInitialProportions().isEmpty()) {
                final ClinicalProgressionWrapper progression = population.getInitialProportions().keySet().iterator().next();
                throw new UnsupportedOSDiFeatureException("The population " + population.getShortName() + " defines an initial proportion (hasInitialProportion) for the progression " + progression.getShortName()
                    + ". Initial proportions are not supported yet in decision trees, which assume that every patient starts without progressions.");
            }
        }
        final BranchDestinationNode rootNode = new BranchDestinationNode(parentTreeModel, name);
        rootNode.updateCosts(diseaseWrapper, Set.of(ResourceUsageSKOSCategory.FOLLOW_UP, ResourceUsageSKOSCategory.TREATMENT), true);
        rootNode.updateUtility(diseaseWrapper, Set.of(ResourceUsageSKOSCategory.FOLLOW_UP, ResourceUsageSKOSCategory.TREATMENT));
        rootNode.updateEffects(diseaseWrapper, EffectType.GENERAL_EFFECT);
        rootNode.updateEffects(diseaseWrapper, EffectType.MORTALITY_RATE_EFFECT);
        rootNode.updateEffects(diseaseWrapper, EffectType.LIFE_EXPECTANCY_REDUCTION_EFFECT);

        // If the disease has a probability of death, we need to create a death node. Such death happens at the onset of the disease, so it does not
        // apply when the interventions target a progression of the disease: the patients that present it have survived the onset
        boolean hasInstantDeathEffect = false;
        for (EffectWrapper effect : diseaseWrapper.getEffects()) {
            if (targetProgression.isEmpty() && EffectType.INSTANT_DEATH_EFFECT.equals(effect.getEffectType())) {
                final BranchDestinationNode deathNode = new BranchDestinationNode(parentTreeModel, "Death from " + diseaseWrapper.getShortName());
                deathNode.updateEffects(diseaseWrapper, EffectType.INSTANT_DEATH_EFFECT);
                rootNode.link(deathNode, effect.getEffectMagnitude());
                hasInstantDeathEffect = true;
            }
        }
        BranchDestinationNode diseaseNode = null;
        if (hasInstantDeathEffect) {
            diseaseNode = new BranchDestinationNode(parentTreeModel, "Alive with " + diseaseWrapper.getShortName());
            rootNode.link(diseaseNode);
        }
        else {
            diseaseNode = rootNode;
        }

        // Identify independent manifestations (not linked to any development)
        final Set<ClinicalProgressionWrapper> pendingManifestations = new TreeSet<>(diseaseWrapper.getManifestations());
        // ...and also create the default coexistent development set with those developments not affected by any combination rule
        final Set<ClinicalProgressionWrapper> defaultCoexistentDevelopments = new TreeSet<>();
        for (ClinicalProgressionWrapper development : diseaseWrapper.getDevelopments()) {
            pendingManifestations.removeAll(diseaseWrapper.getManifestationsLinkedToDevelopment(development));
            if (diseaseWrapper.getCombinationRuleForClinicalProgression(development).isEmpty()) {
                defaultCoexistentDevelopments.add(development);
                // TODO: This was done when these actions were processed in disease wrapper. Check if it is still necessary to do it here or if it can be removed.
                //this.reverseCombinationRuleComponents.put(development, defaultCoexistentDevelopments);
            }
        }
        // Do the same for manifestations
        final Set<ClinicalProgressionWrapper> defaultCoexistentManifestations = new TreeSet<>();
        for (ClinicalProgressionWrapper manifestation : diseaseWrapper.getManifestations()) {
            // Only manifestations not affected by any combination rule
            if (diseaseWrapper.getCombinationRuleForClinicalProgression(manifestation).isEmpty()) {
                defaultCoexistentManifestations.add(manifestation);
                // TODO: This was done when these actions were processed in disease wrapper. Check if it is still necessary to do it here or if it can be removed.
                //this.reverseCombinationRuleComponents.put(manifestation, defaultCoexistentManifestations);
            }
        }

        // Declare and simplify elements to work with
        final ArrayList<ProgressionCombinationRuleWrapper> altDevelopmentCombinationRules = new ArrayList<>();
        final ArrayList<ProgressionCombinationRuleWrapper> altManifestationCombinationRules = new ArrayList<>();
        for (ProgressionCombinationRuleWrapper combinationRule : diseaseWrapper.getCombinationRules()) {
            if (ClinicalProgressionType.DEVELOPMENT.equals(combinationRule.getCombinedElementsType())) {
                // Only alternative rules with more than one progression are relevant
                if (combinationRule.getProgressions().size() > 1 && ProgressionCombinationRuleType.ALTERNATIVE.equals(combinationRule.getCombinationType())) {
                    altDevelopmentCombinationRules.add(combinationRule);
                }
                // The rest are processed as coexistent developments
                else {
                    defaultCoexistentDevelopments.addAll(combinationRule.getProgressions());
                }
            } else if (ClinicalProgressionType.ACUTE_MANIFESTATION.equals(combinationRule.getCombinedElementsType()) ||
                       ClinicalProgressionType.CHRONIC_MANIFESTATION.equals(combinationRule.getCombinedElementsType())) {
                if (combinationRule.getProgressions().size() > 1 && ProgressionCombinationRuleType.ALTERNATIVE.equals(combinationRule.getCombinationType())) {
                    altManifestationCombinationRules.add(combinationRule);
                }
                else {
                    defaultCoexistentManifestations.addAll(combinationRule.getProgressions());
                }
            } else {
                throw new MalformedOSDiModelException("The combination rule " + combinationRule + " does not have a valid type for the combined elements.");
            }
        }
        if (targetProgression.isPresent()) {
            generateTargetSubTree(diseaseNode, defaultCoexistentManifestations);
        }
        else if (!defaultCoexistentDevelopments.isEmpty()) {
            generateCoexistentDevelopmentSubTree(diseaseNode, defaultCoexistentDevelopments, altDevelopmentCombinationRules, defaultCoexistentManifestations, pendingManifestations);
        }
        else if (!altDevelopmentCombinationRules.isEmpty()) {
            generateAlternativeDevelopmentSubTree(diseaseNode, altDevelopmentCombinationRules, defaultCoexistentManifestations, pendingManifestations);
        }
        else if (!pendingManifestations.isEmpty()) {
            generateManifestationsSubTree(diseaseNode, defaultCoexistentManifestations, new TreeSet<>(pendingManifestations));
        }
        return rootNode;
    }

    /**
     * Generates the sub-tree when the interventions of the model target a clinical progression element T instead of the disease (D1 in
     * docs/osdi-1.3-diseno.md). The tree starts with the patients that present T:
     * <ul>
     * <li>The node of T is the only (default) branch of the disease node, with probability 1, and it contributes its payoffs and effects. The risk of
     * T and the combination rules that affect it are ignored.</li>
     * <li>The payoffs and effects of the disease still apply, since the patients have the disease, except for its instant death, which happens at
     * the onset of the disease (see {@link #generate(String)}).</li>
     * <li>Only the progressions that depend on T are included: if T is a development, its own manifestations (hasSubProgression), with their
     * combination rules; if T is a manifestation, nothing else.</li>
     * </ul>
     * @param diseaseNode The node of the disease.
     * @param defaultCoexistentManifestations The manifestations that are not affected by any alternative combination rule.
     * @throws MalformedOSDiModelException If the OSDi model is malformed.
     * @throws UnsupportedOSDiFeatureException If an unsupported OSDi feature is encountered.
     * @throws MalformedSimulationModelException If the simulation model is malformed.
     */
    private void generateTargetSubTree(BranchDestinationNode diseaseNode, Set<ClinicalProgressionWrapper> defaultCoexistentManifestations) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException, MalformedSimulationModelException {
        final ClinicalProgressionWrapper target = targetProgression.get();
        if (!target.isDefaultProgression() || diseaseWrapper.getCombinationRuleForClinicalProgression(target).isPresent()) {
            log.info("The interventions of the model target " + target.getShortName() + ", so the tree starts with the patients that present it: its risk and the combination rules that affect it are ignored.");
        }
        final BranchDestinationNode targetNode = generateNode(target);
        diseaseNode.link(targetNode);
        if (ClinicalProgressionType.DEVELOPMENT.equals(target.getClinicalProgressionType())) {
            final Set<ClinicalProgressionWrapper> pendingManifestations = new TreeSet<>(diseaseWrapper.getManifestationsLinkedToDevelopment(target));
            if (!pendingManifestations.isEmpty()) {
                generateManifestationsSubTree(targetNode, defaultCoexistentManifestations, pendingManifestations);
            }
        }
    }

    /**
     * Checks how the frequency of an acute manifestation (hasFrequency) is used in a decision tree. A decision tree represents a single episode
     * of the acute manifestation, whose probability is its risk:
     * <ul>
     * <li>If the acute manifestation defines both a risk and a frequency, the frequency is ignored with a warning.</li>
     * <li>If it only defines a frequency, the tree cannot be built. The frequency cannot be converted into the probability of the episode:
     * such conversion (1 - e^(-frequency * horizon)) requires a time horizon, which the tree does not have (each leaf accounts for its own
     * life years), and counting a single episode would discard the expected number of episodes. Without this check, the acute manifestation
     * would be treated as a manifestation without risk, i.e., an episode that always happens.</li>
     * </ul>
     * @param manifestation An acute manifestation
     * @throws UnsupportedOSDiFeatureException If the acute manifestation defines a frequency but no risk
     * @throws MalformedOSDiModelException If the frequency of the acute manifestation is not a valid parameter
     */
    private void checkAcuteManifestation(ClinicalProgressionWrapper manifestation) throws UnsupportedOSDiFeatureException, MalformedOSDiModelException {
        final Optional<ParameterWrapper> frequency = manifestation.getModelWrapper().getWrapperForPropertyAs(manifestation.getIndividualIRI(), OSDiObjectProperty.HAS_FREQUENCY, ParameterWrapper.class);
        if (frequency.isEmpty()) {
            return;
        }
        if (manifestation.isDefaultProgression()) {
            throw new UnsupportedOSDiFeatureException("The acute manifestation " + manifestation.getShortName() + " defines a frequency (" + frequency.get().getShortName()
                + ") but no risk. Decision trees represent a single episode of an acute manifestation, whose probability must be defined as a risk (hasRiskCharacterization).");
        }
        log.warn("The acute manifestation " + manifestation.getShortName() + " defines a frequency (" + frequency.get().getShortName()
            + "), which is ignored in decision trees: they represent a single episode, whose probability is the risk of the acute manifestation.");
    }

    public ParameterWrapper getAdaptedRiskParameter(ClinicalProgressionWrapper progression) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        if (progression.isDefaultProgression()) {
            return null;
        }
        final Set<IModelItemWrapper> riskParameters = progression.getProgressionRisks();
        if (riskParameters.size() > 1) {
            log.warn("Clinical progression " + progression.getShortName() + " defines multiple risk characterizations: " + riskParameters + ". Currently, only one is supported. The first one found will be used, but this may lead to unexpected results.");
        }
        final IModelItemWrapper param = riskParameters.iterator().next();
        if (param instanceof PathwayWrapper) {
            throw new UnsupportedOSDiFeatureException(param.getShortName() + " is a pathway, which is not supported in the decision tree model.");
        }
        if (!(param instanceof ParameterWrapper)) {
            throw new MalformedOSDiModelException("The risk characterization " + param.getShortName() + " for the clinical progression " + progression.getShortName() + " is not a parameter.");
        }
        // TODO: Support effects on times to event in decision trees (see docs/known-limitations.md). The probability derived from the time is a
        // synthetic parameter, so an effect on the time would be silently lost
        if (OSDiDataItemType.DI_TIME_TO_EVENT.equals(((ParameterWrapper) param).getDataItemType())) {
            for (EffectWrapper effect : parentTreeModel.getExperimentWrapper().getModelWrapper().getAndInitializeModelItemsByClassAs(OSDiClass.EFFECT, EffectWrapper.class, false)) {
                if (effect.getOperation(param) != null) {
                    throw new UnsupportedOSDiFeatureException("The effect " + effect.getShortName() + " modifies the time to event " + param.getShortName()
                        + " of the clinical progression " + progression.getShortName() + ". Effects on times to event are not supported yet in decision trees.");
                }
            }
        }
        // In case the original parameter was not a valid probability parameter, we need to convert it
        return AbstractModelFactory.getProbabilityParameterFromGenericParameter((ParameterWrapper) param);
    }

    /**
     * Generates the tree parts for a set of coexistent clinical progressions. Coexistent clinical progressions are those that can occur simultaneously.
     * When generating the tree, each clinical progression becomes a binary decision node, where one branch represents the occurrence of the clinical progression 
     * and the other branch represents its absence.
     *
     * @param rootNode The root node of the set tree.
     * @param pendingDevelopments A set of ClinicalProgressionWrapper objects representing the clinical progressions that need to be processed.
     * @param pendingManifestations A set of ClinicalManifestationWrapper objects representing the clinical manifestations that need to be processed.
     * @throws MalformedSimulationModelException If the simulation model is malformed.
     * @throws UnsupportedOSDiFeatureException If an unsupported OSDi feature is encountered.
     * @throws MalformedOSDiModelException If the OSDi model is malformed.
     */
    public void generateCoexistentDevelopmentSubTree(BranchDestinationNode rootNode, Set<ClinicalProgressionWrapper> pendingDevelopments, ArrayList<ProgressionCombinationRuleWrapper> altDevelopmentCombinationRules, Set<ClinicalProgressionWrapper> defaultCoexistentManifestations, Set<ClinicalProgressionWrapper> pendingManifestations) throws MalformedSimulationModelException, UnsupportedOSDiFeatureException, MalformedOSDiModelException {
        // Pick the first development to process
        final ClinicalProgressionWrapper development = pendingDevelopments.iterator().next();
        pendingDevelopments.remove(development);
        final BranchDestinationNode progressionNode = generateNode(development);
        final ParameterWrapper riskParam = getAdaptedRiskParameter(development);
        // The progression branch also includes the manifestations linked to the development
        final Set<ClinicalProgressionWrapper> newPendingManifestations = updatePendingManifestations(pendingManifestations, development);
        if (riskParam == null) {
            // A development without risk that does not belong to a group of alternatives is always active: there is no chance node.
            // It is linked as the only (default) branch, and the rest of the sub-tree hangs from it
            log.info("Development " + development.getShortName() + " does not define a risk parameter and it does not belong to a group of alternatives. It is assumed to be always active.");
            rootNode.link(progressionNode);
            continueCoexistentDevelopmentSubTree(progressionNode, pendingDevelopments, altDevelopmentCombinationRules, defaultCoexistentManifestations, newPendingManifestations);
            return;
        }
        rootNode.link(progressionNode, riskParam);
        // Adds the no progression branch as default branch
        final BranchDestinationNode noProgressionNode = new BranchDestinationNode(parentTreeModel, "No " + development.getShortName());
        rootNode.link(noProgressionNode);
        continueCoexistentDevelopmentSubTree(progressionNode, new TreeSet<>(pendingDevelopments), altDevelopmentCombinationRules, defaultCoexistentManifestations, newPendingManifestations);
        continueCoexistentDevelopmentSubTree(noProgressionNode, pendingDevelopments, altDevelopmentCombinationRules, defaultCoexistentManifestations, new TreeSet<>(pendingManifestations));
    }

    /**
     * Continues the generation of the sub-tree that hangs from a coexistent development: first, the rest of coexistent developments; then,
     * the groups of alternative developments; and finally, the pending manifestations.
     * @param node The node from which the sub-tree hangs.
     * @param pendingDevelopments The coexistent developments that remain to be processed in this branch.
     * @param altDevelopmentCombinationRules The groups of alternative developments.
     * @param defaultCoexistentManifestations The manifestations that are not affected by any alternative combination rule.
     * @param pendingManifestations The manifestations that remain to be processed in this branch.
     * @throws MalformedSimulationModelException If the simulation model is malformed.
     * @throws UnsupportedOSDiFeatureException If an unsupported OSDi feature is encountered.
     * @throws MalformedOSDiModelException If the OSDi model is malformed.
     */
    private void continueCoexistentDevelopmentSubTree(BranchDestinationNode node, Set<ClinicalProgressionWrapper> pendingDevelopments, ArrayList<ProgressionCombinationRuleWrapper> altDevelopmentCombinationRules, Set<ClinicalProgressionWrapper> defaultCoexistentManifestations, Set<ClinicalProgressionWrapper> pendingManifestations) throws MalformedSimulationModelException, UnsupportedOSDiFeatureException, MalformedOSDiModelException {
        if (!pendingDevelopments.isEmpty()) {
            generateCoexistentDevelopmentSubTree(node, pendingDevelopments, altDevelopmentCombinationRules, defaultCoexistentManifestations, pendingManifestations);
        }
        else if (!altDevelopmentCombinationRules.isEmpty()) {
            generateAlternativeDevelopmentSubTree(node, new ArrayList<>(altDevelopmentCombinationRules), defaultCoexistentManifestations, pendingManifestations);
        }
        // generateManifestationsSubTree does not accept an empty set
        else if (!pendingManifestations.isEmpty()) {
            generateManifestationsSubTree(node, defaultCoexistentManifestations, pendingManifestations);
        }
    }
    
    public void generateAlternativeDevelopmentSubTree(BranchDestinationNode rootNode, ArrayList<ProgressionCombinationRuleWrapper> pendingAlternativeDevelopmentSets, Set<ClinicalProgressionWrapper> defaultCoexistentManifestations, Set<ClinicalProgressionWrapper> pendingManifestations) throws MalformedSimulationModelException, UnsupportedOSDiFeatureException, MalformedOSDiModelException {
        final ProgressionCombinationRuleWrapper alternativeDevelopments = pendingAlternativeDevelopmentSets.iterator().next();
        pendingAlternativeDevelopmentSets.remove(alternativeDevelopments);
        final boolean hasNullProgression = alternativeDevelopments.hasNullProgression().orElse(false);
        boolean hasDefaultDevelopment = false;
        // For each disease progression in the set, create a branch in the tree
        for (ClinicalProgressionWrapper development : alternativeDevelopments.getProgressions()) {
            final BranchDestinationNode destinationNode = generateNode(development);
            final ParameterWrapper riskParam = getAdaptedRiskParameter(development);
            if (riskParam != null) {
                rootNode.link(destinationNode, riskParam);
            }
            else {
                if (hasDefaultDevelopment) {
                    throw new MalformedOSDiModelException("Multiple developments without risk parameter are not allowed in an alternative combination rule. Development " + development.getShortName() + " is the second one found.");
                }
                if (hasNullProgression) {
                    throw new MalformedOSDiModelException("Development " + development.getShortName() + " does not define a risk parameter and it is affected by an alternative combination rule that is supposed to include a null progression. Both things are not compatible.");
                }
                hasDefaultDevelopment = true;
                log.warn("Development " + development.getShortName() + " does not define a risk parameter and it is affected by an alternative combination rule. It will be treated as a default development.");
                // Links the default development as the default branch
                rootNode.link(destinationNode);
            }
            final Set<ClinicalProgressionWrapper> newPendingManifestations = updatePendingManifestations(pendingManifestations, development);
            if (!pendingAlternativeDevelopmentSets.isEmpty()) {
                generateAlternativeDevelopmentSubTree(destinationNode, new ArrayList<>(pendingAlternativeDevelopmentSets), defaultCoexistentManifestations, newPendingManifestations);
            }
            else if (!newPendingManifestations.isEmpty()) {
                generateManifestationsSubTree(destinationNode, defaultCoexistentManifestations, newPendingManifestations);
            }
        }
        if (hasNullProgression) {
            // Adds the no progression branch as default branch
            final BranchDestinationNode noProgressionNode = new BranchDestinationNode(parentTreeModel, "None from " + alternativeDevelopments.getShortName());
            rootNode.link(noProgressionNode);
            if (!pendingAlternativeDevelopmentSets.isEmpty()) {
                generateAlternativeDevelopmentSubTree(noProgressionNode, new ArrayList<>(pendingAlternativeDevelopmentSets), defaultCoexistentManifestations, pendingManifestations);
            }
            else if (!pendingManifestations.isEmpty()) {
                generateManifestationsSubTree(noProgressionNode, defaultCoexistentManifestations, pendingManifestations);
            }
        }
    }

    private Set<ClinicalProgressionWrapper> updatePendingManifestations(Set<ClinicalProgressionWrapper> pendingManifestations, ClinicalProgressionWrapper development) {
        final Set<ClinicalProgressionWrapper> newPendingManifestations = new TreeSet<>(pendingManifestations);
        final Set<ClinicalProgressionWrapper> subManifestations = diseaseWrapper.getManifestationsLinkedToDevelopment(development);
        newPendingManifestations.addAll(subManifestations);
        return newPendingManifestations;
    }

    private void generateManifestationsSubTree(BranchDestinationNode rootNode, Set<ClinicalProgressionWrapper> defaultCoexistentManifestations, Set<ClinicalProgressionWrapper> pendingManifestations) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException, MalformedSimulationModelException {
        final Set<ClinicalProgressionWrapper> coexistent = new TreeSet<>(defaultCoexistentManifestations);
        coexistent.retainAll(pendingManifestations);
        if (!coexistent.isEmpty()) {
            pendingManifestations.removeAll(coexistent);
            generateCoexistentManifestationSubTree(rootNode, coexistent, pendingManifestations);
        }
        else {
            generateAlternativeManifestationSubTree(rootNode, pendingManifestations);
        }
    }

    private void generateCoexistentManifestationSubTree(BranchDestinationNode rootNode, Set<ClinicalProgressionWrapper> pendingCoexistentManifestations, Set<ClinicalProgressionWrapper> pendingManifestations) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException, MalformedSimulationModelException {
        // Pick the first manifestation to process
        final ClinicalProgressionWrapper manifestation = pendingCoexistentManifestations.iterator().next();
        pendingCoexistentManifestations.remove(manifestation);
        final BranchDestinationNode progressionNode = generateNode(manifestation);
        final ParameterWrapper riskParam = getAdaptedRiskParameter(manifestation);
        if (riskParam == null) {
            // A manifestation without risk that does not belong to a group of alternatives is always active in its context (e.g., in the branches
            // where the development it depends on is active): there is no chance node. It is linked as the only (default) branch, and the rest of
            // the sub-tree hangs from it
            log.info("Manifestation " + manifestation.getShortName() + " does not define a risk parameter and it does not belong to a group of alternatives. It is assumed to be always active.");
            rootNode.link(progressionNode);
            continueCoexistentManifestationSubTree(progressionNode, pendingCoexistentManifestations, pendingManifestations);
            return;
        }
        rootNode.link(progressionNode, riskParam);
        // Adds the no progression branch as default branch
        final BranchDestinationNode noProgressionNode = new BranchDestinationNode(parentTreeModel, "No " + manifestation.getShortName());
        rootNode.link(noProgressionNode);
        continueCoexistentManifestationSubTree(noProgressionNode, new TreeSet<>(pendingCoexistentManifestations), new TreeSet<>(pendingManifestations));
        continueCoexistentManifestationSubTree(progressionNode, pendingCoexistentManifestations, pendingManifestations);
    }

    /**
     * Continues the generation of the sub-tree that hangs from a coexistent manifestation: first, the rest of coexistent manifestations; then,
     * once all of them are processed, the alternative ones.
     * @param node The node from which the sub-tree hangs.
     * @param pendingCoexistentManifestations The coexistent manifestations that remain to be processed in this branch.
     * @param pendingManifestations The alternative manifestations that remain to be processed in this branch.
     * @throws MalformedOSDiModelException If the OSDi model is malformed.
     * @throws UnsupportedOSDiFeatureException If an unsupported OSDi feature is encountered.
     * @throws MalformedSimulationModelException If the simulation model is malformed.
     */
    private void continueCoexistentManifestationSubTree(BranchDestinationNode node, Set<ClinicalProgressionWrapper> pendingCoexistentManifestations, Set<ClinicalProgressionWrapper> pendingManifestations) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException, MalformedSimulationModelException {
        if (!pendingCoexistentManifestations.isEmpty()) {
            generateCoexistentManifestationSubTree(node, pendingCoexistentManifestations, pendingManifestations);
        }
        else if (!pendingManifestations.isEmpty()) {
            // generateAlternativeManifestationSubTree removes elements from the set it receives
            generateAlternativeManifestationSubTree(node, new TreeSet<>(pendingManifestations));
        }
    }

    private void generateAlternativeManifestationSubTree(BranchDestinationNode rootNode, Set<ClinicalProgressionWrapper> pendingManifestations) throws MalformedSimulationModelException, UnsupportedOSDiFeatureException, MalformedOSDiModelException {
        // First creates a subset of manifestations affected by the same alternative combination rule
        final ProgressionCombinationRuleWrapper alternativeRule = diseaseWrapper.getCombinationRuleForClinicalProgression(pendingManifestations.iterator().next()).get();
        final Set<ClinicalProgressionWrapper> alternativeManifestations = new TreeSet<>(pendingManifestations);
        alternativeManifestations.retainAll(alternativeRule.getProgressions());
        if (alternativeManifestations.isEmpty()) {
            throw new IllegalStateException("The application logic should prevent this from happening, since combination rules are previously checked. No manifestation from the pending set is affected by an alternative combination rule.");
        }
        pendingManifestations.removeAll(alternativeManifestations);
        final boolean hasNullProgression = alternativeRule.hasNullProgression().orElse(false);
        boolean hasDefaultManifestation = false;
        // Then creates the branches for each manifestation in the set
        for (ClinicalProgressionWrapper manifestation : alternativeManifestations) {
            final BranchDestinationNode destinationNode = generateNode(manifestation);
            final ParameterWrapper riskParam = getAdaptedRiskParameter(manifestation);
            if (riskParam != null) {
                rootNode.link(destinationNode, riskParam);
            }
            else {
                if (hasDefaultManifestation) {
                    throw new MalformedOSDiModelException("Multiple manifestations without risk parameter are not allowed in an alternative combination rule. Manifestation " + manifestation.getShortName() + " is the second one found.");
                }
                if (hasNullProgression) {
                    throw new MalformedOSDiModelException("Manifestation " + manifestation.getShortName() + " does not define a risk parameter and it is affected by an alternative combination rule that is supposed to include a null progression. Both things are not compatible.");
                }
                hasDefaultManifestation = true;
                log.warn("Manifestation " + manifestation.getShortName() + " does not define a risk parameter and it is affected by an alternative combination rule. It will be treated as a default manifestation.");
                // Links the default manifestation as the default branch
                rootNode.link(destinationNode);
            }
            if (!pendingManifestations.isEmpty()) {
                generateAlternativeManifestationSubTree(destinationNode, new TreeSet<>(pendingManifestations));
            }
        }
        if (hasNullProgression) {
            // Adds the no progression branch as default branch
            final BranchDestinationNode noProgressionNode = new BranchDestinationNode(parentTreeModel, "None from " + alternativeRule.getShortName());
            rootNode.link(noProgressionNode);
            if (!pendingManifestations.isEmpty()) {
                generateAlternativeManifestationSubTree(noProgressionNode, pendingManifestations);
            }
        }
    }

    /**
     * Generates a BranchDestinationNode for this clinical progression.
     * <p>
     * The node of an acute manifestation represents a single episode between the initial age of the population and death. Its one-time costs and
     * utilities apply once, when the episode happens: at the start of the model, or at the onset age, if defined (see
     * {@link es.ull.simulation.hta.osdi.decisiontree.excelport.PayoffWrapper}). A one-time utility is a QALY loss. Annual costs and utilities
     * of an acute manifestation are ignored with a warning, as in the discrete-event simulation: an episode has no annual payoffs, and charging
     * them until death would treat the acute manifestation as a chronic one.
     * </p>
     * @param progression The clinical progression.
     * @return The generated BranchDestinationNode representing this clinical progression.
     * @throws MalformedOSDiModelException If the OSDi model is malformed, such as if the progression IRI does not correspond to a valid instance.
     * @throws UnsupportedOSDiFeatureException If an unsupported OSDi feature is encountered, such as if the progression defines unsupported parameters.
     * @throws MalformedSimulationModelException If the simulation model is malformed, such as if the progression does not define valid cost or disutility parameters.
     */
    public BranchDestinationNode generateNode(ClinicalProgressionWrapper progression) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException, MalformedSimulationModelException {
        final Optional<TemporalConstraint> temporalConstraint = progression.getTemporalConstraintForProgression();
        final BranchDestinationNode node = new BranchDestinationNode(parentTreeModel, progression.getClinicalProgressionType() + ": " + progression.getShortName());
        if (temporalConstraint.isPresent()) {
            node.setTemporalConstraint(temporalConstraint.get());
        }
        node.updateCosts(progression, Set.of(ResourceUsageSKOSCategory.FOLLOW_UP, ResourceUsageSKOSCategory.TREATMENT), true);
        node.updateUtility(progression, Set.of(ResourceUsageSKOSCategory.FOLLOW_UP, ResourceUsageSKOSCategory.TREATMENT));
        node.updateEffects(progression);
        if (ClinicalProgressionType.ACUTE_MANIFESTATION.equals(progression.getClinicalProgressionType())) {
            final String owner = "The acute manifestation " + progression.getShortName();
            removeAnnualPayoffs(owner, node.getCostParameters());
            removeAnnualPayoffs(owner, node.getDisutilityParameters());
        }
        return node;
    }

    /**
     * Removes, with a warning, the annual costs or utilities that apply to a single episode of an acute manifestation: those of the acute
     * manifestation itself (see {@link #generateNode}) and those of an intervention whose target is an acute manifestation (see
     * {@link SimpleInterventionGenerator#generate(String)}).
     * @param owner A description of the item that defines the parameters, used in the warning (e.g., "The acute manifestation X").
     * @param parameters The cost or utility parameters, from which the annual ones are removed.
     */
    static void removeAnnualPayoffs(String owner, Collection<ParameterWrapper> parameters) {
        final List<ParameterWrapper> annual = parameters.stream().filter(param -> !appliesOneTime(param)).toList();
        if (!annual.isEmpty()) {
            log.warn(owner + " defines annual costs or utilities (" + annual
                + "), which are ignored in decision trees: an acute manifestation is a single episode, so only one-time costs and utilities apply.");
            parameters.removeAll(annual);
        }
    }

    /**
     * Returns true if the parameter is a cost or utility that applies only once.
     * @param param The parameter.
     * @return true if the parameter is a cost or utility that applies only once.
     */
    private static boolean appliesOneTime(ParameterWrapper param) {
        if (param.getSpecificInformation() instanceof SpecificInformationForCost info) {
            return info.appliesOneTime();
        }
        if (param.getSpecificInformation() instanceof SpecificInformationForUtility info) {
            return info.appliesOneTime();
        }
        return false;
    }

}
