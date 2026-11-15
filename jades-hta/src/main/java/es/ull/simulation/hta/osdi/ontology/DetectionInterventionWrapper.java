package es.ull.simulation.hta.osdi.ontology;

import org.semanticweb.owlapi.model.IRI;

import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;

/**
 * This class represents a detection intervention (screening or diagnosis) in the OSDi ontology.
 * <p>
 * A detection intervention does not declare detection properties (decision D2 of docs/osdi-1.3-diseno.md): its sensitivity, specificity and
 * costs are those of the detection strategies of its arm, which a screening intervention declares through a strategy replacement effect on the
 * disease; and the disease it detects is its target (isInterventionOf, hasIntervention). Hence, an intervention that declares hasSensitivity,
 * hasSpecificity, detects or hasTemporaryThreshold is malformed.
 * </p>
 */
public class DetectionInterventionWrapper extends InterventionWrapper {
    /**
     * The properties that only detection strategies can declare.
     */
    private static final OSDiObjectProperty[] STRATEGY_PROPERTIES = {OSDiObjectProperty.HAS_SENSITIVITY, OSDiObjectProperty.HAS_SPECIFICITY,
        OSDiObjectProperty.DETECTS, OSDiObjectProperty.HAS_TEMPORARY_THRESHOLD};

    /**
     * Constructs a DetectionInterventionWrapper with the given model wrapper and intervention IRI.
     * @param modelWrapper the model wrapper containing the intervention
     * @param interventionIRI the IRI of the detection intervention individual in the ontology
     */
    public DetectionInterventionWrapper(ModelWrapper modelWrapper, IRI interventionIRI) {
        super(modelWrapper, interventionIRI);
    }

    @Override
    public void doInitialize() throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        super.doInitialize();
        for (OSDiObjectProperty property : STRATEGY_PROPERTIES) {
            if (!getOSDiWrapper().getValues(getIndividualIRI(), property).isEmpty()) {
                throw new MalformedOSDiModelException("The detection intervention " + getShortName() + " declares " + property.getShortName()
                    + ". Detection properties are declared by the detection strategies: a screening intervention replaces the detection strategies of the disease"
                    + " through a strategy replacement effect, and the disease detected by an intervention is its target (isInterventionOf).");
            }
        }
    }
}
