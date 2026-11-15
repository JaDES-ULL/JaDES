package es.ull.simulation.hta.osdi.ontology;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;

import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.StrategyOutcomeWrapper.DetectionResult;

/**
 * This class represents a detection strategy in the OSDi ontology. It extends the StrategyWrapper class and includes specific properties related to
 * detection strategies, such as sensitivity, specificity, and the disease being detected.
 * <p>
 * The results of a detection strategy may be declared in two ways (decision D6 of docs/osdi-1.3-diseno.md): the short form (hasSensitivity and
 * hasSpecificity), or explicit detection outcomes (hasStrategyOutcome), which also allow inconclusive results (see {@link #getDetectionOutcomes()}).
 * </p>
 */
public class DetectionStrategyWrapper extends StrategyWrapper {
    /**
     * An explicit outcome of a detection strategy: its type of result, its risks for the individuals with and without the disease (the outcome
     * without risk for a status takes the remaining probability), the strategy that follows it, and the wrapper of the outcome.
     */
    public record DetectionOutcome(String name, DetectionResult result, Optional<ParameterWrapper> riskIfDiseased, Optional<ParameterWrapper> riskIfNotDiseased,
            Optional<StrategyWrapper> nextStrategy, StrategyOutcomeWrapper wrapper) {}

    /**
     * The sensitivity parameter for the detection strategy.
     */
    private Optional<ParameterWrapper> sensitivityParameter = Optional.empty();
    /**
     * The specificity parameter for the detection strategy.
     */
    private Optional<ParameterWrapper> specificityParameter = Optional.empty();
    /**
     * The disease being detected by the strategy.
     */
    private Optional<DiseaseWrapper> detectedDisease = Optional.empty();

    /**
     * Constructs a DetectionStrategyWrapper with the given model wrapper and strategy IRI.
     * @param modelWrapper the model wrapper containing the strategy
     * @param interventionIRI the IRI of the detection strategy individual in the ontology
     */
    public DetectionStrategyWrapper(ModelWrapper modelWrapper, IRI interventionIRI) {
        super(modelWrapper, interventionIRI);
    }

    @Override
    public void doInitialize() throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        super.doInitialize();
        sensitivityParameter = getModelWrapper().getWrapperForPropertyAs(getIndividualIRI(), OSDiObjectProperty.HAS_SENSITIVITY, ParameterWrapper.class);
        specificityParameter = getModelWrapper().getWrapperForPropertyAs(getIndividualIRI(), OSDiObjectProperty.HAS_SPECIFICITY, ParameterWrapper.class);
        detectedDisease = getModelWrapper().getWrapperForPropertyAs(getIndividualIRI(), OSDiObjectProperty.DETECTS, DiseaseWrapper.class);
    }

    /**
     * Returns the sensitivity parameter of the detection strategy, if defined.
     * @return an Optional containing the sensitivity parameter, or empty if not defined
     */
    public Optional<ParameterWrapper> getSensitivityParameter() {
        return sensitivityParameter;
    }

    /**
     * Returns the specificity parameter of the detection strategy, if defined.
     * @return an Optional containing the specificity parameter, or empty if not defined
     */
    public Optional<ParameterWrapper> getSpecificityParameter() {
        return specificityParameter;
    }

    /**
     * Returns the disease being detected by the strategy, if defined.
     * @return an Optional containing the detected disease, or empty if not defined
     */
    public Optional<DiseaseWrapper> getDetectedDisease() {
        return detectedDisease;
    }

    /**
     * Returns true if this strategy declares explicit outcomes (hasStrategyOutcome) instead of the short form (sensitivity and specificity).
     * @return true if this strategy declares explicit outcomes.
     */
    public boolean hasExplicitOutcomes() {
        return !getOSDiWrapper().getValues(getIndividualIRI(), OSDiObjectProperty.HAS_STRATEGY_OUTCOME).isEmpty();
    }

    /**
     * Returns the explicit outcomes of this detection strategy (decision D6), sorted as positive, inconclusive and negative results. Each outcome
     * has a risk for the individuals with the disease (hasRiskIfDiseased) and another for those without it (hasRiskIfNotDiseased). For each
     * status, at most one outcome may lack a risk: it takes the remaining probability. A positive outcome without its own next strategy is followed
     * by the next strategy of this strategy (followedByStrategy), if any.
     * @return The explicit outcomes of this strategy (empty if it uses the short form).
     * @throws MalformedOSDiModelException If the outcomes are combined with the short form, an outcome is not a positive, negative or inconclusive
     * detection outcome, or more than one outcome lacks a risk for the same status.
     * @throws UnsupportedOSDiFeatureException If an unsupported OSDi feature is encountered
     */
    public List<DetectionOutcome> getDetectionOutcomes() throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        final List<DetectionOutcome> outcomes = new ArrayList<>();
        if (!hasExplicitOutcomes()) {
            return outcomes;
        }
        if (sensitivityParameter.isPresent() || specificityParameter.isPresent()) {
            throw new MalformedOSDiModelException("The detection strategy " + getShortName() + " combines the short form (sensitivity and specificity) with explicit outcomes (hasStrategyOutcome).");
        }
        final ModelWrapper modelWrapper = getModelWrapper();
        final Optional<StrategyWrapper> strategyNext = modelWrapper.getAndInitializeWrapperForPropertyAs(getIndividualIRI(), OSDiObjectProperty.FOLLOWED_BY_STRATEGY, StrategyWrapper.class);
        final Set<IModelItemWrapper> explicit = modelWrapper.getWrappersForProperty(getIndividualIRI(), OSDiObjectProperty.HAS_STRATEGY_OUTCOME);
        int withoutRiskIfDiseased = 0;
        int withoutRiskIfNotDiseased = 0;
        for (IModelItemWrapper item : explicit) {
            final Optional<DetectionResult> result = (item instanceof StrategyOutcomeWrapper candidate) ? candidate.getDetectionResult() : Optional.empty();
            if (result.isEmpty()) {
                throw new MalformedOSDiModelException("The outcome " + item.getShortName() + " of the detection strategy " + getShortName()
                    + " must be a positive, negative or inconclusive detection outcome (PositiveDetectionOutcome, NegativeDetectionOutcome or InconclusiveDetectionOutcome).");
            }
            final StrategyOutcomeWrapper outcome = (StrategyOutcomeWrapper) item;
            outcome.initialize();
            if (outcome.getRiskIfDiseased().isEmpty()) {
                withoutRiskIfDiseased++;
            }
            if (outcome.getRiskIfNotDiseased().isEmpty()) {
                withoutRiskIfNotDiseased++;
            }
            final Optional<StrategyWrapper> next = DetectionResult.POSITIVE.equals(result.get()) ? outcome.getNextStrategy().or(() -> strategyNext) : outcome.getNextStrategy();
            outcomes.add(new DetectionOutcome(outcome.getShortName(), result.get(), outcome.getRiskIfDiseased(), outcome.getRiskIfNotDiseased(), next, outcome));
        }
        if (withoutRiskIfDiseased > 1) {
            throw new MalformedOSDiModelException("The detection strategy " + getShortName() + " has " + withoutRiskIfDiseased
                + " outcomes without risk for the individuals with the disease (hasRiskIfDiseased). Only one outcome (the default one) may lack it.");
        }
        if (withoutRiskIfNotDiseased > 1) {
            throw new MalformedOSDiModelException("The detection strategy " + getShortName() + " has " + withoutRiskIfNotDiseased
                + " outcomes without risk for the individuals without the disease (hasRiskIfNotDiseased). Only one outcome (the default one) may lack it.");
        }
        // Sorts the outcomes to build the same tree every time: positive, inconclusive and negative results
        outcomes.sort((o1, o2) -> {
            final int cmp = Integer.compare(order(o1.result()), order(o2.result()));
            return (cmp != 0) ? cmp : o1.name().compareTo(o2.name());
        });
        return outcomes;
    }

    private static int order(DetectionResult result) {
        return switch (result) {
            case POSITIVE -> 0;
            case INCONCLUSIVE -> 1;
            case NEGATIVE -> 2;
        };
    }
}
