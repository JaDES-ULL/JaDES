package es.ull.simulation.hta.osdi.decisiontree.nbsdecisiontree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;

import es.ull.simulation.hta.MalformedSimulationModelException;
import es.ull.simulation.hta.osdi.OSDiLogger;
import es.ull.simulation.hta.osdi.decisiontree.BranchDestinationNode;
import es.ull.simulation.hta.osdi.decisiontree.StrategyCosts;
import es.ull.simulation.hta.osdi.decisiontree.StrategyCosts.GuidelineCost;
import es.ull.simulation.hta.osdi.decisiontree.factories.InterventionGenerator;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.DetectionInterventionWrapper;
import es.ull.simulation.hta.osdi.ontology.DetectionStrategyWrapper;
import es.ull.simulation.hta.osdi.ontology.DetectionStrategyWrapper.DetectionOutcome;
import es.ull.simulation.hta.osdi.ontology.DiseaseWrapper;
import es.ull.simulation.hta.osdi.ontology.ModelWrapper;
import es.ull.simulation.hta.osdi.ontology.NextStrategy;
import es.ull.simulation.hta.osdi.ontology.OSDiObjectProperty;
import es.ull.simulation.hta.osdi.ontology.ParameterNatureType;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper.DeterministicParameterData;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper.SpecificInformationForCost;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper.SpecificInformationForUtility;
import es.ull.simulation.hta.osdi.ontology.ResourceUsageSKOSCategory;
import es.ull.simulation.hta.osdi.ontology.StrategyOutcomeWrapper;
import es.ull.simulation.hta.osdi.ontology.StrategyOutcomeWrapper.DetectionResult;
import es.ull.simulation.hta.osdi.ontology.StrategyType;
import es.ull.simulation.hta.osdi.ontology.StrategyWrapper;
import es.ull.simulation.hta.params.StandardParameter;

/**
 * Builds the screening arm of a newborn screening model (decisions D2 and D6 of docs/osdi-1.3-diseno.md).
 * <p>
 * The individuals with and without the disease branch over the results of the screening strategy of the arm (see {@link NBSDetectionArm}): its
 * explicit detection outcomes, with the risks for each status (hasRiskIfDiseased and hasRiskIfNotDiseased; the outcome without risk for a status
 * takes the remaining probability), or the short form, which is expanded into a positive outcome (sensitivity if diseased; remaining probability
 * if not) and a negative outcome (specificity if not diseased; remaining probability if diseased). Missing sensitivity or specificity are assumed
 * to be perfect (1), with a warning.
 * </p>
 * <ul>
 * <li>A result followed by a diagnosis strategy (or a positive result without next strategy, which is confirmed by the diagnosis strategy of the
 * arm) is a perfect confirmation: the individuals pay the confirmation costs (see
 * {@link NBSDetectionArm#addConfirmationCosts(BranchDestinationNode, Optional)}); those with the disease are detected (true positives: sub-tree of
 * the disease with the effects of the intervention), and those without the disease are discarded (false positives).</li>
 * <li>A result followed by another detection strategy (a repeated or second-tier test) pays the costs of such strategy and branches again over its
 * results, for the same status. Cycles in the chain of tests are not supported.</li>
 * <li>A negative result ends the detection: those with the disease are not diagnosed by the screening (false negatives: sub-tree of the disease
 * without the effects of the intervention), and those without it are true negatives. What happens with the false negatives is pending (see
 * docs/osdi-1.3-diseno.md).</li>
 * <li>An inconclusive result must be followed by a detection strategy.</li>
 * <li>The costs and utilities of an explicit outcome apply once (the screening happens at the start of the simulation), and its effects apply to the
 * branch of the outcome. Annual costs and utilities, and the evaluation time, are ignored with a warning.</li>
 * </ul>
 */
public class NBSScreeningInterventionBuilder extends InterventionGenerator {
    private final static OSDiLogger log = OSDiLogger.getLogger(NBSScreeningInterventionBuilder.class);
    /** A result of a detection strategy for a status (with or without the disease): its risk (empty for the default result) and next strategy */
    private record Result(String name, DetectionResult result, Optional<ParameterWrapper> risk, Optional<StrategyWrapper> nextStrategy, Optional<StrategyOutcomeWrapper> wrapper) {}
    /** A branch of the tree: its destination node and its probability (empty for the default branch) */
    private record Branch(BranchDestinationNode node, Optional<ParameterWrapper> probability) {}
    /** The sensitivity and specificity of a detection strategy that uses the short form */
    private record ShortForm(ParameterWrapper sensitivity, ParameterWrapper specificity, Optional<StrategyWrapper> nextStrategy) {}

    /**
     * The disease wrapper associated with this part of the decision tree.
     */
    private final DiseaseWrapper disease;
    /**
     * The detection strategies of the screening arm, and the costs they produce.
     */
    private final NBSDetectionArm arm;
    /**
     * The short form of the screening strategy of the arm (or a perfect screening, if there is no screening strategy), if it does not declare
     * explicit outcomes.
     */
    private final Optional<ShortForm> screeningShortForm;
    /**
     * The short form of the other detection strategies used in the chains of tests, indexed by their IRI.
     */
    private final Map<IRI, ShortForm> shortForms = new HashMap<>();

    /**
     * Constructs a screening part for the given NBS tree model and part name.
     * The results of the screening are those of the screening strategy of the arm (see {@link NBSDetectionArm}). If the arm has no
     * screening strategy, or the strategy defines neither explicit outcomes nor its sensitivity and specificity, they are assumed to be perfect (1),
     * with a warning.
     *
     * @param parentTreeModel The parent tree model associated with this factory.
     * @param intervention  The screening intervention.
     * @throws MalformedSimulationModelException If there is an issue with the simulation model.
     * @throws UnsupportedOSDiFeatureException   If an unsupported OSDi feature is encountered.
     * @throws MalformedOSDiModelException       If the OSDi model is malformed.
     */
    public NBSScreeningInterventionBuilder(NBSModel parentTreeModel, DetectionInterventionWrapper intervention) throws MalformedSimulationModelException, UnsupportedOSDiFeatureException, MalformedOSDiModelException {
        super(parentTreeModel, intervention);
        this.disease = parentTreeModel.getDiseaseWrapper();
        this.arm = parentTreeModel.getDetectionArm(intervention);
        final Optional<DetectionStrategyWrapper> screeningStrategy = arm.getScreeningStrategy();
        if (screeningStrategy.isEmpty()) {
            log.warn("The arm of the screening intervention " + intervention.getShortName() + " has no screening strategy. The screening is assumed to be perfect and to have no cost, apart from the own screening costs of the intervention.");
            this.screeningShortForm = Optional.of(createShortForm(parentTreeModel.getModelWrapper(), intervention.getShortName(), Optional.empty(), Optional.empty(), Optional.empty()));
        }
        else if (screeningStrategy.get().hasExplicitOutcomes()) {
            this.screeningShortForm = Optional.empty();
        }
        else {
            this.screeningShortForm = Optional.of(getShortForm(screeningStrategy.get(), intervention.getShortName()));
        }
    }

    /**
     * Returns the short form of a detection strategy, creating it the first time.
     * @param strategy The detection strategy
     * @param name The name used for the assumed sensitivity and specificity, if the strategy does not define them
     * @return The short form of the detection strategy
     */
    private ShortForm getShortForm(DetectionStrategyWrapper strategy, String name) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        ShortForm shortForm = shortForms.get(strategy.getIndividualIRI());
        if (shortForm == null) {
            final ModelWrapper modelWrapper = strategy.getModelWrapper();
            final Optional<StrategyWrapper> next = DetectionStrategyWrapper.requireSingle(NextStrategy.read(modelWrapper, strategy.getIndividualIRI(), OSDiObjectProperty.FOLLOWED_BY_STRATEGY), strategy.getShortName());
            shortForm = createShortForm(modelWrapper, name, strategy.getSensitivityParameter(), strategy.getSpecificityParameter(), next);
            shortForms.put(strategy.getIndividualIRI(), shortForm);
        }
        return shortForm;
    }

    /**
     * Creates the short form of a detection strategy, assuming a perfect sensitivity or specificity (1), with a warning, if not defined.
     */
    private static ShortForm createShortForm(ModelWrapper modelWrapper, String name, Optional<ParameterWrapper> sensitivity, Optional<ParameterWrapper> specificity, Optional<StrategyWrapper> next) {
        final ParameterWrapper sensitivityParam;
        final ParameterWrapper specificityParam;
        if (sensitivity.isEmpty()) {
            // If no sensitivity parameter is defined, create a default one
            log.warn("No sensitivity defined for the screening of " + name + ". Assuming perfect sensitivity = 1.0.");
            DeterministicParameterData defaultSensitivityData = new DeterministicParameterData(1.0);
            sensitivityParam = new ParameterWrapper.SyntheticBuilder(modelWrapper, StandardParameter.SENSITIVITY.createName(name), defaultSensitivityData)
            .withSource("Assumption")
            .withDescription("No parameter defined for sensitivity of " + name + " . Assuming perfect sensitivity = 1.0.")
            .build();
        }
        else {
            sensitivityParam = sensitivity.get();
        }
        if (specificity.isEmpty()) {
            log.warn("No specificity defined for the screening of " + name + ". Assuming perfect specificity = 1.0.");
            DeterministicParameterData defaultSpecificityData = new DeterministicParameterData(1.0);
            specificityParam = new ParameterWrapper.SyntheticBuilder(modelWrapper, StandardParameter.SPECIFICITY.createName(name), defaultSpecificityData)
            .withSource("Assumption")
            .withDescription("No parameter defined for specificity of " + name + " . Assuming perfect specificity = 1.0.")
            .build();
        }
        else {
            specificityParam = specificity.get();
        }
        return new ShortForm(sensitivityParam, specificityParam, next);
    }

    /**
     * Generates the sub-tree of the screening arm. The whole population pays the screening costs of the arm; the individuals with and without the
     * disease branch over the results of the screening (see the description of this class). The strategy replacements of the arm also apply to the
     * sub-trees of the disease.
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

        final Set<IRI> chain = new HashSet<>();
        arm.getScreeningStrategy().ifPresent(strategy -> chain.add(strategy.getIndividualIRI()));
        final List<Branch> diseased = getBranches(true, getScreeningResults(true), chain);
        linkStatus(screeningPopulationNode, "Has the disease", Optional.of(birthPrevalenceParam), diseased);
        final List<Branch> notDiseased = getBranches(false, getScreeningResults(false), chain);
        linkStatus(screeningPopulationNode, "Does not have disease", Optional.empty(), notDiseased);
        return screeningPopulationNode;
    }

    /**
     * Links the branches of a status (with or without the disease) to the root of the arm, through a node of the status if there is more than one.
     */
    private void linkStatus(BranchDestinationNode root, String name, Optional<ParameterWrapper> probability, List<Branch> branches) throws MalformedSimulationModelException {
        final BranchDestinationNode statusNode;
        if (branches.size() == 1) {
            statusNode = branches.get(0).node();
        }
        else {
            statusNode = new BranchDestinationNode(getParentTreeModel(), name);
            linkBranches(statusNode, branches);
        }
        if (probability.isPresent()) {
            root.link(statusNode, probability.get());
        }
        else {
            root.link(statusNode);
        }
    }

    private static void linkBranches(BranchDestinationNode parent, List<Branch> branches) throws MalformedSimulationModelException {
        for (Branch branch : branches) {
            if (branch.probability().isPresent()) {
                parent.link(branch.node(), branch.probability().get());
            }
            else {
                parent.link(branch.node());
            }
        }
    }

    /**
     * Returns the results of the screening strategy of the arm for a status.
     */
    private List<Result> getScreeningResults(boolean diseased) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        if (screeningShortForm.isPresent()) {
            return getShortFormResults(screeningShortForm.get(), diseased);
        }
        return getExplicitResults(arm.getScreeningStrategy().get(), diseased);
    }

    /**
     * Returns the results of a detection strategy for a status (with or without the disease).
     */
    private List<Result> getResults(DetectionStrategyWrapper strategy, boolean diseased) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        if (strategy.hasExplicitOutcomes()) {
            return getExplicitResults(strategy, diseased);
        }
        return getShortFormResults(getShortForm(strategy, strategy.getShortName()), diseased);
    }

    /**
     * Returns the results of the short form for a status: a positive result, with the sensitivity if diseased, and a negative result, with the
     * specificity if not diseased. A perfect sensitivity or specificity leaves a single result.
     */
    private static List<Result> getShortFormResults(ShortForm shortForm, boolean diseased) throws UnsupportedOSDiFeatureException {
        final List<Result> results = new ArrayList<>();
        if (diseased) {
            results.add(new Result(null, DetectionResult.POSITIVE, Optional.of(shortForm.sensitivity()), shortForm.nextStrategy(), Optional.empty()));
            results.add(new Result(null, DetectionResult.NEGATIVE, Optional.empty(), Optional.empty(), Optional.empty()));
        }
        else {
            results.add(new Result(null, DetectionResult.NEGATIVE, Optional.of(shortForm.specificity()), Optional.empty(), Optional.empty()));
            results.add(new Result(null, DetectionResult.POSITIVE, Optional.empty(), shortForm.nextStrategy(), Optional.empty()));
        }
        return selectCertainResult(results);
    }

    /**
     * Returns the results of the explicit outcomes of a detection strategy for a status (with or without the disease).
     */
    private static List<Result> getExplicitResults(DetectionStrategyWrapper strategy, boolean diseased) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        final List<Result> results = new ArrayList<>();
        for (DetectionOutcome outcome : strategy.getDetectionOutcomes()) {
            results.add(new Result(outcome.name(), outcome.result(), diseased ? outcome.riskIfDiseased() : outcome.riskIfNotDiseased(), outcome.nextStrategy(), Optional.of(outcome.wrapper())));
        }
        return selectCertainResult(results);
    }

    /**
     * If a result is certain (its risk is a deterministic 1), returns only that result, as the default one; otherwise, returns all the results.
     */
    private static List<Result> selectCertainResult(List<Result> results) throws UnsupportedOSDiFeatureException {
        for (Result result : results) {
            if (result.risk().isPresent() && !requiresNode(result.risk().get())) {
                return List.of(new Result(result.name(), result.result(), Optional.empty(), result.nextStrategy(), result.wrapper()));
            }
        }
        return results;
    }

    /**
     * Creates the branches of the results of a detection strategy for a status (with or without the disease).
     * @param diseased True for the individuals with the disease
     * @param results The results of the detection strategy for the status
     * @param chain The detection strategies already applied in this path
     * @return The branches of the results
     */
    private List<Branch> getBranches(boolean diseased, List<Result> results, Set<IRI> chain) throws MalformedOSDiModelException, MalformedSimulationModelException, UnsupportedOSDiFeatureException {
        final List<Branch> branches = new ArrayList<>();
        for (Result result : results) {
            final BranchDestinationNode node = createResultNode(diseased, result, chain);
            if (result.wrapper().isPresent()) {
                addOutcomePayoffs(node, result.wrapper().get());
            }
            branches.add(new Branch(node, result.risk()));
        }
        return branches;
    }

    /**
     * Creates the node (and the sub-tree below it) of a result of a detection strategy for a status.
     */
    private BranchDestinationNode createResultNode(boolean diseased, Result result, Set<IRI> chain) throws MalformedOSDiModelException, MalformedSimulationModelException, UnsupportedOSDiFeatureException {
        final String suffix = (result.name() == null) ? "" : " (" + result.name() + ")";
        if (DetectionResult.NEGATIVE.equals(result.result())) {
            if (result.nextStrategy().isPresent()) {
                log.warn("The negative outcome " + result.name() + " is followed by the strategy " + result.nextStrategy().get().getShortName()
                    + ", which is ignored: a negative result ends the detection in newborn screening models.");
            }
            if (diseased) {
                // TODO: Decide what happens with the false negatives (see docs/osdi-1.3-diseno.md)
                return ((NBSModel) getParentTreeModel()).getDiseaseGenerator().generate("False negative " + disease.getShortName() + suffix, arm.getReplacements());
            }
            return new BranchDestinationNode(getParentTreeModel(), "True negative" + suffix);
        }
        if (result.nextStrategy().isEmpty()) {
            if (DetectionResult.INCONCLUSIVE.equals(result.result())) {
                throw new MalformedOSDiModelException("The inconclusive outcome " + result.name() + " is not followed by a strategy. An inconclusive result must be followed by a detection strategy (a test or a diagnosis).");
            }
            return createConfirmationNode(diseased, Optional.empty(), suffix);
        }
        final StrategyWrapper next = result.nextStrategy().get();
        if (!(next instanceof DetectionStrategyWrapper nextDetection)) {
            throw new MalformedOSDiModelException("The " + result.result().toString().toLowerCase() + " outcome " + ((result.name() == null) ? "" : result.name() + " ") + "is followed by the strategy "
                + next.getShortName() + ", which is not a detection strategy. Positive and inconclusive results must be followed by a detection strategy (a test or a diagnosis).");
        }
        if (StrategyType.DIAGNOSIS_STRATEGY.equals(nextDetection.getStrategyType())) {
            return createConfirmationNode(diseased, Optional.of(nextDetection), suffix);
        }
        if (chain.contains(nextDetection.getIndividualIRI())) {
            throw new UnsupportedOSDiFeatureException("The chain of detection strategies that follows the outcome " + result.name() + " goes back to the strategy " + nextDetection.getShortName()
                + ". Cycles are not supported in decision trees: a repetition of a test must be modeled as another strategy.");
        }
        final Set<IRI> newChain = new HashSet<>(chain);
        newChain.add(nextDetection.getIndividualIRI());
        final BranchDestinationNode node = new BranchDestinationNode(getParentTreeModel(), nextDetection.getShortName() + suffix);
        final List<ParameterWrapper> costs = new ArrayList<>();
        final List<GuidelineCost> guidelineCosts = new ArrayList<>();
        StrategyCosts.collect(nextDetection, costs, guidelineCosts);
        node.addCostParameters(costs);
        node.addGuidelineCosts(guidelineCosts);
        linkBranches(node, getBranches(diseased, getResults(nextDetection, diseased), newChain));
        return node;
    }

    /**
     * Creates the node of a result confirmed by a (perfect) diagnosis: a true positive, with the sub-tree of the disease and the effects of the
     * intervention, for the individuals with the disease; a false positive for those without it. Both pay the confirmation costs.
     */
    private BranchDestinationNode createConfirmationNode(boolean diseased, Optional<StrategyWrapper> confirmation, String suffix) throws MalformedOSDiModelException, MalformedSimulationModelException, UnsupportedOSDiFeatureException {
        final BranchDestinationNode node;
        if (diseased) {
            node = ((NBSModel) getParentTreeModel()).getDiseaseGenerator().generate("True positive " + disease.getShortName() + suffix, arm.getReplacements());
            // TODO: Until I find a better method, I will update effects only in subbranches selectively
            node.updateEffects(getInterventionWrapper());
        }
        else {
            node = new BranchDestinationNode(getParentTreeModel(), "False positive" + suffix);
        }
        arm.addConfirmationCosts(node, confirmation);
        return node;
    }

    /**
     * Adds the payoffs and effects of an explicit outcome to its node. They apply once, since the screening happens at the start of the simulation:
     * annual costs and utilities, and the evaluation time, are ignored with a warning.
     */
    private static void addOutcomePayoffs(BranchDestinationNode node, StrategyOutcomeWrapper outcome) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        if (outcome.getEvaluationTime().isPresent()) {
            log.warn("The evaluation time of the detection outcome " + outcome.getShortName() + " is ignored: the screening happens at the start of the simulation in newborn screening models.");
        }
        final List<ParameterWrapper> costs = new ArrayList<>(outcome.getCosts());
        final List<ParameterWrapper> utilities = new ArrayList<>(outcome.getUtilities());
        removeAnnualPayoffs(outcome, costs);
        removeAnnualPayoffs(outcome, utilities);
        node.addCostParameters(costs);
        node.addDisutilityParameters(utilities);
        node.updateEffects(outcome);
    }

    /**
     * Removes, with a warning, the annual costs or utilities of a detection outcome.
     */
    private static void removeAnnualPayoffs(StrategyOutcomeWrapper outcome, List<ParameterWrapper> parameters) {
        final List<ParameterWrapper> annual = parameters.stream().filter(param -> !appliesOneTime(param)).toList();
        if (!annual.isEmpty()) {
            log.warn("The detection outcome " + outcome.getShortName() + " defines annual costs or utilities (" + annual
                + "), which are ignored: the payoffs of a detection outcome apply once in newborn screening models.");
            parameters.removeAll(annual);
        }
    }

    private static boolean appliesOneTime(ParameterWrapper param) {
        if (param.getSpecificInformation() instanceof SpecificInformationForCost info) {
            return info.appliesOneTime();
        }
        if (param.getSpecificInformation() instanceof SpecificInformationForUtility info) {
            return info.appliesOneTime();
        }
        return false;
    }

    /**
     * Checks if the given screening parameter requires a node in the decision tree.
     * In general, deterministic parameters with a value of 1.0 do not require a node,
     * while second order and calculated parameters always require a node.
     * @param screeningParam The screening parameter to check.
     * @return true if the screening parameter requires a node, false otherwise.
     * @throws UnsupportedOSDiFeatureException If the parameter nature is unsupported.
     */
    private static boolean requiresNode(ParameterWrapper screeningParam) throws UnsupportedOSDiFeatureException {
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
