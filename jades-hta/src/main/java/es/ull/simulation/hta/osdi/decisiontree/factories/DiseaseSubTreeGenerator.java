package es.ull.simulation.hta.osdi.decisiontree.factories;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

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
import es.ull.simulation.hta.osdi.ontology.ModelWrapper;
import es.ull.simulation.hta.osdi.ontology.OSDiDataItemType;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper;
import es.ull.simulation.hta.osdi.ontology.PathwayWrapper;
import es.ull.simulation.hta.osdi.ontology.ProgressionCombinationRuleType;
import es.ull.simulation.hta.osdi.ontology.ProgressionCombinationRuleWrapper;
import es.ull.simulation.hta.osdi.ontology.ResourceUsageSKOSCategory;
import es.ull.simulation.hta.osdi.ontology.TemporalConstraint;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper.ParameterNatureData;
import es.ull.simulation.hta.params.StandardParameter;

/**
 * A sub-tree builder for a specific disease within a decision tree model.
 * 
 * The decision tree structure starts from a disease node, which may lead to various clinical progression nodes based on the disease's defined progressions. First,
 * coexistent developments are processed, followed by sets of alternative developments. Each development may lead to manifestations, which are also processed in a similar manner.
 * 
 * Developments always produce nodes. If they are part of an alternative set, the node is a probability node together with the other developments in the set. Otherwise,
 * they produce two probability nodes: one for the development occurring, and another for it not occurring.
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
     * The wrapper of the population individual in this model. 
     */
    private final ModelWrapper modelWrapper;

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
        this.modelWrapper = parentTreeModel.getExperimentWrapper().getModelWrapper();
        this.diseaseWrapper = diseaseWrapper;
        this.parentTreeModel = parentTreeModel;
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
        final BranchDestinationNode rootNode = new BranchDestinationNode(parentTreeModel, name);
        rootNode.updateCosts(diseaseWrapper, Set.of(ResourceUsageSKOSCategory.FOLLOW_UP, ResourceUsageSKOSCategory.TREATMENT), true);
        rootNode.updateUtility(diseaseWrapper, Set.of(ResourceUsageSKOSCategory.FOLLOW_UP, ResourceUsageSKOSCategory.TREATMENT));
        rootNode.updateEffects(diseaseWrapper, EffectType.GENERAL_EFFECT);
        rootNode.updateEffects(diseaseWrapper, EffectType.MORTALITY_RATE_EFFECT);
        rootNode.updateEffects(diseaseWrapper, EffectType.LIFE_EXPECTANCY_REDUCTION_EFFECT);

        // If the disease has a probability of death, we need to create a death node
        boolean hasInstantDeathEffect = false;
        for (EffectWrapper effect : diseaseWrapper.getEffects()) {
            if (EffectType.INSTANT_DEATH_EFFECT.equals(effect.getEffectType())) {
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
        if (!defaultCoexistentDevelopments.isEmpty()) {
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
        // In case the original parameter was not a valid probability parameter, we need to convert it
        return AbstractModelFactory.getProbabilityParameterFromGenericParameter((ParameterWrapper) param);
    }
    
    public ParameterWrapper getDefaultRiskParameter(String syntheticName, String progressionName) {
        ParameterNatureData paramData = new ParameterWrapper.DeterministicParameterData(1.0);
        ParameterWrapper syntheticRiskParam = new ParameterWrapper.SyntheticBuilder(modelWrapper, 
            syntheticName, paramData)
            .withSource("Assumption")
            .withDescription("Default risk parameter for development " + progressionName)
            .withDataItemType(OSDiDataItemType.DI_PROBABILITY).build();
        return syntheticRiskParam;
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
        ParameterWrapper riskParam = getAdaptedRiskParameter(development);
        if (riskParam == null) {
            log.warn("Development " + development.getShortName() + " does not define a risk parameter and it is affected by a coexistence combination rule. It will be assumed to have a default risk of 1.");
            riskParam = getDefaultRiskParameter(StandardParameter.PROBABILITY.createName(development.getShortName()), 
                development.getShortName());
        }
        rootNode.link(progressionNode, riskParam);
        // Adds the no progression branch as default branch
        final BranchDestinationNode noProgressionNode = new BranchDestinationNode(parentTreeModel, "No " + development.getShortName());
        rootNode.link(noProgressionNode);
        final Set<ClinicalProgressionWrapper> newPendingManifestations = updatePendingManifestations(pendingManifestations, development);
        if (!pendingDevelopments.isEmpty()) {
            generateCoexistentDevelopmentSubTree(progressionNode, new TreeSet<>(pendingDevelopments), altDevelopmentCombinationRules, defaultCoexistentManifestations, newPendingManifestations);
            generateCoexistentDevelopmentSubTree(noProgressionNode, pendingDevelopments, altDevelopmentCombinationRules, defaultCoexistentManifestations, pendingManifestations);
        }
        else if (!altDevelopmentCombinationRules.isEmpty()) {
            generateAlternativeDevelopmentSubTree(progressionNode, new ArrayList<>(altDevelopmentCombinationRules), defaultCoexistentManifestations, newPendingManifestations);
            generateAlternativeDevelopmentSubTree(noProgressionNode, new ArrayList<>(altDevelopmentCombinationRules), defaultCoexistentManifestations, pendingManifestations);
        }
        else if (!pendingManifestations.isEmpty()) {
            generateManifestationsSubTree(progressionNode, defaultCoexistentManifestations, newPendingManifestations);
            generateManifestationsSubTree(noProgressionNode, defaultCoexistentManifestations, new TreeSet<>(pendingManifestations));
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
        ParameterWrapper riskParam = getAdaptedRiskParameter(manifestation);
        if (riskParam == null) {
            log.warn("Manifestation " + manifestation.getShortName() + " does not define a risk parameter and it is affected by a coexistence combination rule. It will be assumed to have a default risk of 1.");
            riskParam = getDefaultRiskParameter(StandardParameter.PROBABILITY.createName(manifestation.getShortName()), manifestation.getShortName());
        }
        rootNode.link(progressionNode, riskParam);
        // Adds the no progression branch as default branch
        final BranchDestinationNode noProgressionNode = new BranchDestinationNode(parentTreeModel, "No " + manifestation.getShortName());
        rootNode.link(noProgressionNode);
        if (!pendingCoexistentManifestations.isEmpty()) {
            generateCoexistentManifestationSubTree(noProgressionNode, new TreeSet<>(pendingCoexistentManifestations), new TreeSet<>(pendingManifestations));
            generateCoexistentManifestationSubTree(progressionNode, pendingCoexistentManifestations, pendingManifestations);
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
        return node;
    }

}
