package es.ull.simulation.hta.osdi.ontology;

import java.util.Optional;

import org.semanticweb.owlapi.model.IRI;

import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;

/**
 * This class represents a detection strategy in the OSDi ontology. It extends the StrategyWrapper class and includes specific properties related to 
 * detection strategies, such as sensitivity, specificity, and the disease being detected.
 */
public class DetectionStrategyWrapper extends StrategyWrapper {
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
}
