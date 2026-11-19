package es.ull.simulation.hta.osdi.ontology;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;

import es.ull.simulation.hta.osdi.OSDiLogger;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;

/**
 * Wrapper interface for OSDi Strategy elements.
 * TODO: Extend with methods to access strategy-specific properties as needed. Currently only cost and utility retrieval is supported.
 * Conditional strategies (hasConditionExpression) are not supported yet, and chained strategies (followedByStrategy) are only used by the outcomes of a strategy (see {@link #getOutcomes()}). The costs of guidelines are computed by the decision trees (see StrategyCosts).
 */
public class StrategyWrapper extends BaseModelItemWrapper implements IModelItemWithCostWrapper, IModelItemWithUtilityWrapper {
    private final static OSDiLogger log = OSDiLogger.getLogger(StrategyWrapper.class);
    private StrategyType strategyType;

    public StrategyWrapper(ModelWrapper modelWrapper, IRI individualIRI) {
        super(modelWrapper, individualIRI);
    }

    @Override
    public void doInitialize() throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        final ModelWrapper modelWrapper = getModelWrapper();
        final IRI individualIRI = getIndividualIRI();
        strategyType = StrategyType.fromIRI(individualIRI, getOSDiWrapper());
        if (strategyType == null) {
            throw new MalformedOSDiModelException("The strategy " + individualIRI + " does not have a valid type in the OSDi model.");
        }
        log.warn(!modelWrapper.getWrappersForProperty(individualIRI, OSDiObjectProperty.HAS_CONDITION_EXPRESSION).isEmpty(), individualIRI, OSDiObjectProperty.HAS_CONDITION_EXPRESSION, "Conditional strategies are not supported yet. They will be ignored");
        // The next strategy is only used by the outcomes of the strategy (see getOutcomes), or to confirm the positive results of a detection strategy
        log.warn(!modelWrapper.getWrappersForProperty(individualIRI, OSDiObjectProperty.FOLLOWED_BY_STRATEGY).isEmpty() && !hasOutcomes() && !(this instanceof DetectionStrategyWrapper),individualIRI, OSDiObjectProperty.FOLLOWED_BY_STRATEGY, "Chained strategies without outcomes are not supported yet. They will be ignored");
        // Guidelines are supported in decision trees since phase 5 (see StrategyCosts); the unsupported cases are rejected there
    }

    @Override
    public void doPersist() throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        // TODO
    }

    /**
     * Returns the type of this strategy.
     * @return The type of this strategy.
     */
    /**
     * An outcome of a strategy (decision D4 of docs/osdi-1.3-diseno.md), either explicit (hasStrategyOutcome) or derived from the short form
     * (failsIf, followedOnFailureBy, followedOnSuccessBy, resolvesOnSuccess).
     * @param name The name of the outcome
     * @param risk The probability or time to event of the outcome; empty for the default outcome, or if it depends on a health condition
     * @param failureCondition The health condition whose appearance produces this outcome (failsIf a health condition), if any
     * @param evaluationTime The time when the outcome is evaluated, since the start of the strategy, if any
     * @param next The strategy, or the group of alternative strategies, that follows the outcome: its own, or that of the strategy (followedByStrategy), if any
     * @param resolves Whether the outcome resolves the health condition
     * @param wrapper The wrapper of the explicit outcome, which defines its costs, utilities and effects; empty for the short form
     */
    public record Outcome(String name, Optional<ParameterWrapper> risk, Optional<HealthConditionWrapper> failureCondition, Optional<ParameterWrapper> evaluationTime,
            NextStrategy next, boolean resolves, Optional<StrategyOutcomeWrapper> wrapper) {}

    /**
     * Returns true if this strategy has outcomes, either explicit (hasStrategyOutcome) or through the short form (failsIf).
     * @return true if this strategy has outcomes.
     * @throws MalformedOSDiModelException If the ontology is malformed
     * @throws UnsupportedOSDiFeatureException If an unsupported OSDi feature is encountered
     */
    public boolean hasOutcomes() throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        return !getOSDiWrapper().getValues(getIndividualIRI(), OSDiObjectProperty.HAS_STRATEGY_OUTCOME).isEmpty()
            || !getOSDiWrapper().getValues(getIndividualIRI(), OSDiObjectProperty.FAILS_IF).isEmpty();
    }

    /**
     * Returns the mutually exclusive outcomes of this strategy (decision D4). The short form (failsIf) is expanded into a failure, with the risk of
     * the parameter or produced by the appearance of the health condition, followed by followedOnFailureBy; and a default success, followed by
     * followedOnSuccessBy, which resolves the health condition if resolvesOnSuccess. An explicit outcome without its own next strategy is followed
     * by the followedByStrategy of this strategy, if any.
     * @return The outcomes of this strategy (empty if it has none).
     * @throws MalformedOSDiModelException If the short form is combined with explicit outcomes, an outcome is not a treatment outcome, more than one
     * outcome has no risk, or failsIf does not point to a parameter or a health condition.
     * @throws UnsupportedOSDiFeatureException If the strategy is a detection strategy with outcomes, which are only supported in the detection
     * arms of newborn screening models (see {@link DetectionStrategyWrapper#getDetectionOutcomes()}).
     */
    public List<Outcome> getOutcomes() throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        final ModelWrapper modelWrapper = getModelWrapper();
        final IRI iri = getIndividualIRI();
        final List<Outcome> outcomes = new ArrayList<>();
        if (!hasOutcomes()) {
            return outcomes;
        }
        // The outcomes of detection strategies are only supported in newborn screening models (see DetectionStrategyWrapper.getDetectionOutcomes)
        if (this instanceof DetectionStrategyWrapper) {
            throw new UnsupportedOSDiFeatureException("The detection strategy " + getShortName() + " defines outcomes. The outcomes of detection strategies are only supported in the detection arms of newborn screening models.");
        }
        final NextStrategy strategyNext = NextStrategy.read(modelWrapper, iri, OSDiObjectProperty.FOLLOWED_BY_STRATEGY);
        final Set<IModelItemWrapper> failsIf = modelWrapper.getWrappersForProperty(iri, OSDiObjectProperty.FAILS_IF);
        final Set<IModelItemWrapper> explicit = modelWrapper.getWrappersForProperty(iri, OSDiObjectProperty.HAS_STRATEGY_OUTCOME);
        if (!failsIf.isEmpty()) {
            if (!explicit.isEmpty()) {
                throw new MalformedOSDiModelException("The strategy " + getShortName() + " combines the short form (failsIf) with explicit outcomes (hasStrategyOutcome).");
            }
            if (failsIf.size() > 1) {
                throw new MalformedOSDiModelException("The strategy " + getShortName() + " defines several failure conditions (failsIf): " + failsIf + ".");
            }
            final IModelItemWrapper condition = failsIf.iterator().next();
            final Optional<ParameterWrapper> risk = (condition instanceof ParameterWrapper param) ? Optional.of(param) : Optional.empty();
            final Optional<HealthConditionWrapper> healthCondition = (condition instanceof HealthConditionWrapper hc) ? Optional.of(hc) : Optional.empty();
            if (risk.isEmpty() && healthCondition.isEmpty()) {
                throw new MalformedOSDiModelException("The failure condition (failsIf) " + condition.getShortName() + " of the strategy " + getShortName() + " is neither a parameter nor a health condition.");
            }
            final NextStrategy onFailure = NextStrategy.read(modelWrapper, iri, OSDiObjectProperty.FOLLOWED_ON_FAILURE_BY);
            final NextStrategy onSuccess = NextStrategy.read(modelWrapper, iri, OSDiObjectProperty.FOLLOWED_ON_SUCCESS_BY);
            final boolean resolvesOnSuccess = getOSDiWrapper().getBooleanValue(iri, OSDiDataProperty.RESOLVES_ON_SUCCESS).orElse(false);
            outcomes.add(new Outcome(getShortName() + "_Failure", risk, healthCondition, Optional.empty(), onFailure.or(strategyNext), false, Optional.empty()));
            outcomes.add(new Outcome(getShortName() + "_Success", Optional.empty(), Optional.empty(), Optional.empty(), onSuccess.or(strategyNext), resolvesOnSuccess, Optional.empty()));
            return outcomes;
        }
        int withoutRisk = 0;
        for (IModelItemWrapper item : explicit) {
            if (!(item instanceof StrategyOutcomeWrapper outcome) || !outcome.isTreatmentOutcome()) {
                throw new MalformedOSDiModelException("The outcome " + item.getShortName() + " of the strategy " + getShortName() + " must be a treatment outcome (TreatmentOutcome or one of its subclasses).");
            }
            outcome.initialize();
            if (outcome.getRisk().isEmpty()) {
                withoutRisk++;
            }
            outcomes.add(new Outcome(outcome.getShortName(), outcome.getRisk(), Optional.empty(), outcome.getEvaluationTime(), outcome.getNext().or(strategyNext),
                outcome.resolvesHealthCondition(), Optional.of(outcome)));
        }
        if (withoutRisk > 1) {
            throw new MalformedOSDiModelException("The strategy " + getShortName() + " has " + withoutRisk + " outcomes without risk. Only one outcome (the default one) may lack a risk.");
        }
        return outcomes;
    }

    /**
     * Returns true if this strategy declares adverse events (hasAdverseEvent).
     * @return true if this strategy declares adverse events
     */
    public boolean hasAdverseEvents() {
        return !getOSDiWrapper().getValues(getIndividualIRI(), OSDiObjectProperty.HAS_ADVERSE_EVENT).isEmpty();
    }

    /**
     * Returns the adverse events of this strategy (decision D8 of docs/osdi-1.3-diseno.md), sorted by name. Each time the strategy is applied, each
     * adverse event happens with its risk, independently of the rest and of the outcome of the strategy (unless the strategy fails because of it).
     * @return The adverse events of this strategy (empty if it has none)
     * @throws MalformedOSDiModelException If an adverse event is not an AdverseEvent, or its risk is missing or is not a parameter
     * @throws UnsupportedOSDiFeatureException If an unsupported OSDi feature is encountered
     */
    public List<AdverseEventWrapper> getAdverseEvents() throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        final List<AdverseEventWrapper> adverseEvents = new ArrayList<>();
        for (IModelItemWrapper item : getModelWrapper().getWrappersForProperty(getIndividualIRI(), OSDiObjectProperty.HAS_ADVERSE_EVENT)) {
            if (!(item instanceof AdverseEventWrapper adverseEvent)) {
                throw new MalformedOSDiModelException("The adverse event (hasAdverseEvent) " + item.getShortName() + " of the strategy " + getShortName() + " is not an AdverseEvent.");
            }
            adverseEvent.initialize();
            adverseEvents.add(adverseEvent);
        }
        adverseEvents.sort((a, b) -> a.getShortName().compareTo(b.getShortName()));
        return adverseEvents;
    }

    public StrategyType getStrategyType() {
        return strategyType;
    }
}
