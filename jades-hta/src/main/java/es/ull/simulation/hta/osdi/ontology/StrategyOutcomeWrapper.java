package es.ull.simulation.hta.osdi.ontology;

import java.util.Optional;

import org.semanticweb.owlapi.model.IRI;

import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;

/**
 * Wrapper for an outcome of a strategy (StrategyOutcome, decision D4 of docs/osdi-1.3-diseno.md). It gives access to its risk, its evaluation
 * time, the strategy that follows it, whether it resolves the health condition, and its own costs, utilities and effects. The outcomes of a
 * detection strategy (DetectionOutcome) have a risk for the individuals with and without the disease (hasRiskIfDiseased, hasRiskIfNotDiseased)
 * and a type of result (positive, negative or inconclusive).
 */
public class StrategyOutcomeWrapper extends BaseModelItemWrapper implements IModelItemWithEffectWrapper, IModelItemWithCostWrapper, IModelItemWithUtilityWrapper {
    /** The type of result of a detection outcome */
    public enum DetectionResult {
        POSITIVE,
        NEGATIVE,
        INCONCLUSIVE
    }

    private Optional<ParameterWrapper> risk = Optional.empty();
    private Optional<ParameterWrapper> riskIfDiseased = Optional.empty();
    private Optional<ParameterWrapper> riskIfNotDiseased = Optional.empty();
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
        riskIfDiseased = modelWrapper.getWrapperForPropertyAs(iri, OSDiObjectProperty.HAS_RISK_IF_DISEASED, ParameterWrapper.class);
        riskIfNotDiseased = modelWrapper.getWrapperForPropertyAs(iri, OSDiObjectProperty.HAS_RISK_IF_NOT_DISEASED, ParameterWrapper.class);
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

    /**
     * Returns the type of result of this outcome, if it is a positive, negative or inconclusive outcome of a detection strategy.
     * @return The type of result of this outcome, or empty if it is not a positive, negative or inconclusive detection outcome.
     */
    public Optional<DetectionResult> getDetectionResult() {
        final IRI iri = getIndividualIRI();
        if (getOSDiWrapper().isInstanceOf(iri, OSDiClass.POSITIVE_DETECTION_OUTCOME)) {
            return Optional.of(DetectionResult.POSITIVE);
        }
        if (getOSDiWrapper().isInstanceOf(iri, OSDiClass.NEGATIVE_DETECTION_OUTCOME)) {
            return Optional.of(DetectionResult.NEGATIVE);
        }
        if (getOSDiWrapper().isInstanceOf(iri, OSDiClass.INCONCLUSIVE_DETECTION_OUTCOME)) {
            return Optional.of(DetectionResult.INCONCLUSIVE);
        }
        return Optional.empty();
    }

    public Optional<ParameterWrapper> getRisk() {
        return risk;
    }

    /**
     * Returns the risk of this outcome for the individuals with the disease (detection outcomes).
     * @return The risk of this outcome for the individuals with the disease, if defined.
     */
    public Optional<ParameterWrapper> getRiskIfDiseased() {
        return riskIfDiseased;
    }

    /**
     * Returns the risk of this outcome for the individuals without the disease (detection outcomes).
     * @return The risk of this outcome for the individuals without the disease, if defined.
     */
    public Optional<ParameterWrapper> getRiskIfNotDiseased() {
        return riskIfNotDiseased;
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
