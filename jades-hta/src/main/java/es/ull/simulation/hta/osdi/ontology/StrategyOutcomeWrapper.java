package es.ull.simulation.hta.osdi.ontology;

import java.util.Optional;

import org.semanticweb.owlapi.model.IRI;

import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;

/**
 * Wrapper for an outcome of a strategy (StrategyOutcome, decision D4 of docs/osdi-1.3-diseno.md). It gives access to its risk, its evaluation
 * time, the strategy that follows it, whether it resolves the health condition, and its own costs, utilities and effects.
 */
public class StrategyOutcomeWrapper extends BaseModelItemWrapper implements IModelItemWithEffectWrapper, IModelItemWithCostWrapper, IModelItemWithUtilityWrapper {
    private Optional<ParameterWrapper> risk = Optional.empty();
    private Optional<ParameterWrapper> evaluationTime = Optional.empty();
    private Optional<StrategyWrapper> nextStrategy = Optional.empty();
    private boolean resolves = false;

    public StrategyOutcomeWrapper(ModelWrapper modelWrapper, IRI individualIRI) {
        super(modelWrapper, individualIRI);
    }

    @Override
    public void doInitialize() throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        final ModelWrapper modelWrapper = getModelWrapper();
        final IRI iri = getIndividualIRI();
        risk = modelWrapper.getWrapperForPropertyAs(iri, OSDiObjectProperty.HAS_OUTCOME_RISK, ParameterWrapper.class);
        evaluationTime = modelWrapper.getWrapperForPropertyAs(iri, OSDiObjectProperty.HAS_EVALUATION_TIME, ParameterWrapper.class);
        nextStrategy = modelWrapper.getAndInitializeWrapperForPropertyAs(iri, OSDiObjectProperty.FOLLOWED_BY_STRATEGY, StrategyWrapper.class);
        resolves = getOSDiWrapper().getBooleanValue(iri, OSDiDataProperty.RESOLVES_HEALTH_CONDITION).orElse(false);
    }

    @Override
    public void doPersist() throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
    }

    /**
     * Returns true if this is an outcome of a treatment (TreatmentOutcome), i.e., not of a detection strategy.
     * @return true if this is an outcome of a treatment.
     */
    public boolean isTreatmentOutcome() {
        return getOSDiWrapper().isInstanceOf(getIndividualIRI(), OSDiClass.TREATMENT_OUTCOME);
    }

    public Optional<ParameterWrapper> getRisk() {
        return risk;
    }

    public Optional<ParameterWrapper> getEvaluationTime() {
        return evaluationTime;
    }

    public Optional<StrategyWrapper> getNextStrategy() {
        return nextStrategy;
    }

    public boolean resolvesHealthCondition() {
        return resolves;
    }
}
