package es.ull.simulation.hta.osdi.ontology;

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
        log.warn(!modelWrapper.getWrappersForProperty(individualIRI, OSDiObjectProperty.FOLLOWED_BY_STRATEGY).isEmpty(), individualIRI, OSDiObjectProperty.FOLLOWED_BY_STRATEGY, "Chained strategies are not supported yet. They will be ignored");
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
    public StrategyType getStrategyType() {
        return strategyType;
    }
}
