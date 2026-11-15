package es.ull.simulation.hta.osdi.decisiontree.nbsdecisiontree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;

import es.ull.simulation.hta.osdi.decisiontree.BranchDestinationNode;
import es.ull.simulation.hta.osdi.decisiontree.StrategyCosts;
import es.ull.simulation.hta.osdi.decisiontree.StrategyCosts.GuidelineCost;
import es.ull.simulation.hta.osdi.decisiontree.factories.DiseaseSubTreeGenerator;
import es.ull.simulation.hta.osdi.decisiontree.factories.SimpleInterventionGenerator;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.DetectionInterventionWrapper;
import es.ull.simulation.hta.osdi.ontology.DetectionStrategyWrapper;
import es.ull.simulation.hta.osdi.ontology.DiseaseWrapper;
import es.ull.simulation.hta.osdi.ontology.InterventionType;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper;
import es.ull.simulation.hta.osdi.ontology.ResourceUsageSKOSCategory;
import es.ull.simulation.hta.osdi.ontology.StrategyType;
import es.ull.simulation.hta.osdi.ontology.StrategyWrapper;

/**
 * The detection strategies of an arm of a newborn screening model, and the costs they produce (decision D2 of docs/osdi-1.3-diseno.md).
 * <p>
 * The detection strategies of an arm are those of the disease after applying the strategy replacements of the intervention of the arm, by type of
 * strategy (see {@link DiseaseSubTreeGenerator#replaceByType(Set, Set)}). A screening intervention usually replaces the detection strategies of the
 * disease with a group of a screening strategy and a diagnosis strategy. An arm may have at most one screening strategy and one diagnosis strategy,
 * and the arm without screening cannot have a screening strategy. A detection strategy that declares the disease it detects (detects) must detect the
 * disease of the model.
 * </p>
 * <ul>
 * <li>Screening costs (paid by the whole population of the screening arm): the own costs of the intervention with the screening category, plus
 * the costs of the screening strategy (see {@link StrategyCosts}).</li>
 * <li>Diagnosis costs (paid by the patients diagnosed): the own costs of the disease with the diagnosis category, plus the costs of the diagnosis
 * strategy of the arm.</li>
 * </ul>
 * The sensitivity and specificity of the screening are those of the screening strategy. The sensitivity and specificity of the diagnosis strategy
 * are not used: the diagnosis is assumed to be perfect.
 */
public final class NBSDetectionArm {
    /** The strategies that replace those of each health condition in this arm, indexed by the IRI of the health condition */
    private final Map<IRI, Set<StrategyWrapper>> replacements;
    /** The screening strategy of this arm, if any */
    private final Optional<DetectionStrategyWrapper> screeningStrategy;
    /** The costs paid by the whole population of the arm because of the screening */
    private final List<ParameterWrapper> screeningCosts = new ArrayList<>();
    /** The costs paid by the whole population of the arm because of the screening, which apply according to a guideline */
    private final List<GuidelineCost> screeningGuidelineCosts = new ArrayList<>();
    /** The costs paid by each patient diagnosed */
    private final List<ParameterWrapper> diagnosisCosts = new ArrayList<>();
    /** The costs paid by each patient diagnosed, which apply according to a guideline */
    private final List<GuidelineCost> diagnosisGuidelineCosts = new ArrayList<>();

    /**
     * Creates the detection strategies of an arm.
     * @param model The newborn screening model
     * @param intervention The intervention of the arm
     * @param ownDiagnosisCosts The own costs of the disease with the diagnosis category
     * @throws MalformedOSDiModelException If the arm is malformed
     * @throws UnsupportedOSDiFeatureException If the arm uses features not supported by newborn screening models
     */
    NBSDetectionArm(NBSModel model, DetectionInterventionWrapper intervention, Set<ParameterWrapper> ownDiagnosisCosts) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        final DiseaseWrapper disease = model.getDiseaseWrapper();
        this.replacements = SimpleInterventionGenerator.getStrategyReplacements(intervention);
        final Set<StrategyWrapper> strategies = DiseaseSubTreeGenerator.replaceByType(disease.getStrategies(), replacements.get(disease.getIndividualIRI()));
        this.screeningStrategy = getDetectionStrategy(intervention, disease, strategies, StrategyType.SCREENING_STRATEGY);
        final Optional<DetectionStrategyWrapper> diagnosisStrategy = getDetectionStrategy(intervention, disease, strategies, StrategyType.DIAGNOSIS_STRATEGY);
        if (screeningStrategy.isPresent() && !InterventionType.SCREENING.equals(intervention.getInterventionType())) {
            throw new MalformedOSDiModelException("The arm of the intervention " + intervention.getShortName() + " uses the screening strategy " + screeningStrategy.get().getShortName()
                + ", but it is not a screening intervention.");
        }
        if (InterventionType.SCREENING.equals(intervention.getInterventionType())) {
            screeningCosts.addAll(model.getOSDiWrapper().filterModelItemsWithSKOSCategory(intervention.getCosts(), ResourceUsageSKOSCategory.SCREENING));
            if (screeningStrategy.isPresent()) {
                StrategyCosts.collect(screeningStrategy.get(), screeningCosts, screeningGuidelineCosts);
            }
        }
        diagnosisCosts.addAll(ownDiagnosisCosts);
        if (diagnosisStrategy.isPresent()) {
            StrategyCosts.collect(diagnosisStrategy.get(), diagnosisCosts, diagnosisGuidelineCosts);
        }
    }

    /**
     * Returns the only detection strategy of a type among the strategies of an arm, if any.
     * @param intervention The intervention of the arm
     * @param disease The disease of the model
     * @param strategies The strategies of the disease in the arm
     * @param type The type of detection strategy
     * @return The detection strategy of the given type, if any
     * @throws UnsupportedOSDiFeatureException If the arm has more than one detection strategy of the type
     * @throws MalformedOSDiModelException If the strategy detects a disease that is not the disease of the model
     */
    private static Optional<DetectionStrategyWrapper> getDetectionStrategy(DetectionInterventionWrapper intervention, DiseaseWrapper disease, Set<StrategyWrapper> strategies, StrategyType type) throws UnsupportedOSDiFeatureException, MalformedOSDiModelException {
        final List<StrategyWrapper> found = strategies.stream().filter(strategy -> type.equals(strategy.getStrategyType())).toList();
        if (found.isEmpty()) {
            return Optional.empty();
        }
        if (found.size() > 1) {
            throw new UnsupportedOSDiFeatureException("The arm of the intervention " + intervention.getShortName() + " has more than one strategy of type " + type + ": " + found
                + ". Newborn screening models support at most one screening strategy and one diagnosis strategy per arm.");
        }
        final DetectionStrategyWrapper strategy = (DetectionStrategyWrapper) found.get(0);
        if (strategy.getDetectedDisease().isPresent() && !strategy.getDetectedDisease().get().getIndividualIRI().equals(disease.getIndividualIRI())) {
            throw new MalformedOSDiModelException("The detection strategy " + strategy.getShortName() + " of the arm of the intervention " + intervention.getShortName()
                + " detects " + strategy.getDetectedDisease().get().getShortName() + ", which is not the disease of the model (" + disease.getShortName() + ").");
        }
        return Optional.of(strategy);
    }

    /**
     * Returns the strategies that replace those of each health condition in this arm (see {@link DiseaseSubTreeGenerator#generate(String, Map)}).
     * @return The strategies that replace those of each health condition in this arm, indexed by the IRI of the health condition
     */
    public Map<IRI, Set<StrategyWrapper>> getReplacements() {
        return replacements;
    }

    /**
     * Returns the screening strategy of this arm, if any.
     * @return The screening strategy of this arm, if any
     */
    public Optional<DetectionStrategyWrapper> getScreeningStrategy() {
        return screeningStrategy;
    }

    /**
     * Adds the screening costs of this arm to a node.
     * @param node The node
     */
    public void addScreeningCosts(BranchDestinationNode node) {
        node.addCostParameters(screeningCosts);
        node.addGuidelineCosts(screeningGuidelineCosts);
    }

    /**
     * Adds the diagnosis costs of this arm to a node.
     * @param node The node
     */
    public void addDiagnosisCosts(BranchDestinationNode node) {
        node.addCostParameters(diagnosisCosts);
        node.addGuidelineCosts(diagnosisGuidelineCosts);
    }
}
