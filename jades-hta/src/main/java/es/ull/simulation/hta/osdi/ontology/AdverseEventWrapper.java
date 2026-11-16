package es.ull.simulation.hta.osdi.ontology;

import java.util.Optional;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;

import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;

/**
 * Wrapper for an adverse event (AdverseEvent, decision D8 of docs/osdi-1.3-diseno.md): a health condition caused by a strategy (hasAdverseEvent).
 * Each time the strategy is applied, the adverse event happens with its risk (hasRiskCharacterization, mandatory: a probability per application,
 * or a time to event in a simulation), independently of the outcome of the strategy, unless the strategy fails because of it (failsIf). Its costs
 * and utilities apply once, when it happens; its effects (including an InstantDeathEffect) and its own strategies apply to the patients who suffer
 * it. isOccurrenceOf identifies the health condition it represents, but does not activate the structure of that condition.
 */
public class AdverseEventWrapper extends BaseModelItemWrapper implements HealthConditionWrapper {
    private ParameterWrapper risk = null;
    private Optional<IModelItemWrapper> occurrenceOf = Optional.empty();

    public AdverseEventWrapper(ModelWrapper modelWrapper, IRI individualIRI) {
        super(modelWrapper, individualIRI);
    }

    /**
     * Reads the risk of the adverse event and the health condition it represents.
     * @throws MalformedOSDiModelException If the adverse event does not define exactly one risk, or the risk is not a parameter
     */
    @Override
    public void doInitialize() throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        final ModelWrapper modelWrapper = getModelWrapper();
        final IRI iri = getIndividualIRI();
        final Set<IModelItemWrapper> risks = modelWrapper.getWrappersForProperty(iri, OSDiObjectProperty.HAS_RISK_CHARACTERIZATION);
        if (risks.isEmpty()) {
            throw new MalformedOSDiModelException("The adverse event " + getShortName() + " does not define a risk (hasRiskCharacterization). The risk of an adverse event is mandatory: "
                + "a probability per application of the strategy that causes it.");
        }
        if (risks.size() > 1) {
            throw new MalformedOSDiModelException("The adverse event " + getShortName() + " defines several risks (hasRiskCharacterization): " + risks + ". Only one is allowed.");
        }
        final IModelItemWrapper item = risks.iterator().next();
        if (!(item instanceof ParameterWrapper param)) {
            throw new MalformedOSDiModelException("The risk (hasRiskCharacterization) " + item.getShortName() + " of the adverse event " + getShortName() + " is not a parameter.");
        }
        risk = param;
        final Set<IModelItemWrapper> occurrences = modelWrapper.getWrappersForProperty(iri, OSDiObjectProperty.IS_OCCURRENCE_OF);
        if (occurrences.size() > 1) {
            throw new MalformedOSDiModelException("The adverse event " + getShortName() + " is an occurrence of several health conditions (isOccurrenceOf): " + occurrences + ". Only one is allowed.");
        }
        occurrenceOf = occurrences.stream().findFirst();
    }

    @Override
    public void doPersist() throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
    }

    /**
     * Returns the risk of the adverse event: a probability per application of the strategy that causes it, or a time to event.
     * @return The risk of the adverse event
     */
    public ParameterWrapper getRisk() {
        return risk;
    }

    /**
     * Returns the health condition of which this adverse event is an occurrence (isOccurrenceOf), if declared. It only identifies the condition.
     * @return The health condition of which this adverse event is an occurrence, if declared
     */
    public Optional<IModelItemWrapper> getOccurrenceOf() {
        return occurrenceOf;
    }
}
