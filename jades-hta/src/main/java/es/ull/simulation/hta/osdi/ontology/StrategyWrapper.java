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
 * In the case of costs, conditional strategies, chained strategies, and guideline-based strategies are not supported yet and will return an empty cost list.
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
        // The next strategy is only used by the outcomes of the strategy (see getOutcomes)
        log.warn(!modelWrapper.getWrappersForProperty(individualIRI, OSDiObjectProperty.FOLLOWED_BY_STRATEGY).isEmpty() && !hasOutcomes(), individualIRI, OSDiObjectProperty.FOLLOWED_BY_STRATEGY, "Chained strategies without outcomes are not supported yet. They will be ignored");
        log.warn(!modelWrapper.getWrappersForProperty(individualIRI, OSDiObjectProperty.HAS_GUIDELINE).isEmpty(), individualIRI, OSDiObjectProperty.HAS_GUIDELINE, "Guideline-based strategies are not supported yet. They will be ignored");    
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
     * @param nextStrategy The strategy that follows the outcome: its own, or that of the strategy (followedByStrategy), if any
     * @param resolves Whether the outcome resolves the health condition
     * @param wrapper The wrapper of the explicit outcome, which defines its costs, utilities and effects; empty for the short form
     */
    public record Outcome(String name, Optional<ParameterWrapper> risk, Optional<HealthConditionWrapper> failureCondition, Optional<ParameterWrapper> evaluationTime,
            Optional<StrategyWrapper> nextStrategy, boolean resolves, Optional<StrategyOutcomeWrapper> wrapper) {}

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
     * @throws UnsupportedOSDiFeatureException If the strategy is a detection strategy with outcomes (not supported yet).
     */
    public List<Outcome> getOutcomes() throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        final ModelWrapper modelWrapper = getModelWrapper();
        final IRI iri = getIndividualIRI();
        final List<Outcome> outcomes = new ArrayList<>();
        if (!hasOutcomes()) {
            return outcomes;
        }
        // TODO: Support the outcomes of detection strategies (phase 7b, see docs/osdi-1.3-diseno.md)
        if (this instanceof DetectionStrategyWrapper) {
            throw new UnsupportedOSDiFeatureException("The detection strategy " + getShortName() + " defines outcomes, which are not supported yet.");
        }
        final Optional<StrategyWrapper> strategyNext = modelWrapper.getAndInitializeWrapperForPropertyAs(iri, OSDiObjectProperty.FOLLOWED_BY_STRATEGY, StrategyWrapper.class);
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
            final Optional<StrategyWrapper> onFailure = modelWrapper.getAndInitializeWrapperForPropertyAs(iri, OSDiObjectProperty.FOLLOWED_ON_FAILURE_BY, StrategyWrapper.class);
            final Optional<StrategyWrapper> onSuccess = modelWrapper.getAndInitializeWrapperForPropertyAs(iri, OSDiObjectProperty.FOLLOWED_ON_SUCCESS_BY, StrategyWrapper.class);
            final boolean resolvesOnSuccess = getOSDiWrapper().getBooleanValue(iri, OSDiDataProperty.RESOLVES_ON_SUCCESS).orElse(false);
            outcomes.add(new Outcome(getShortName() + "_Failure", risk, healthCondition, Optional.empty(), onFailure.or(() -> strategyNext), false, Optional.empty()));
            outcomes.add(new Outcome(getShortName() + "_Success", Optional.empty(), Optional.empty(), Optional.empty(), onSuccess.or(() -> strategyNext), resolvesOnSuccess, Optional.empty()));
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
            outcomes.add(new Outcome(outcome.getShortName(), outcome.getRisk(), Optional.empty(), outcome.getEvaluationTime(), outcome.getNextStrategy().or(() -> strategyNext),
                outcome.resolvesHealthCondition(), Optional.of(outcome)));
        }
        if (withoutRisk > 1) {
            throw new MalformedOSDiModelException("The strategy " + getShortName() + " has " + withoutRisk + " outcomes without risk. Only one outcome (the default one) may lack a risk.");
        }
        return outcomes;
    }

    public StrategyType getStrategyType() {
        return strategyType;
    }
}
