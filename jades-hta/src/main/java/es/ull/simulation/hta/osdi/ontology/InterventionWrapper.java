package es.ull.simulation.hta.osdi.ontology;

import java.util.Optional;

import org.semanticweb.owlapi.model.IRI;

import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;

public class InterventionWrapper extends BaseModelItemWrapper implements IModelItemWithEffectWrapper, IModelItemWithStrategyWrapper {
    private Optional<Boolean> isAssessedIntervention = Optional.empty();
    private InterventionType interventionType;;

    public InterventionWrapper(ModelWrapper modelWrapper, IRI individualIRI) {
        super(modelWrapper, individualIRI);
    }

    @Override
    public void doInitialize() throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        isAssessedIntervention = getOSDiWrapper().getBooleanValue(getIndividualIRI(), OSDiDataProperty.IS_ASSESSED_INTERVENTION);
        interventionType = InterventionType.fromIRI(getIndividualIRI(), getOSDiWrapper());
        if (interventionType == null) {
            throw new MalformedOSDiModelException("The intervention " + getIndividualIRI() + " does not have a valid type in the OSDi model.");
        }
    }

    @Override
    public void doPersist() throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        // TODO
    }

    /**
     * Returns whether this intervention is the assessed intervention in the model.
     * @return True if this intervention is the assessed intervention, false if it is the comparator, and empty if it is not defined in the model.
     */
    public Optional<Boolean> isAssessedIntervention() {
        return isAssessedIntervention;
    }

    /**
     * Returns the type of this intervention.
     * @return The type of this intervention.
     */
    public InterventionType getInterventionType() {
        return interventionType;
    }

    /**
     * Creates a new intervention in the OSDi model.
     * @param wrap The OSDi wrapper that contains the ontology
     * @param parentModelIRI The IRI of the parent model
     * @param individualIRI The IRI of the intervention individual
     * @param type The type of intervention
     * @param description The description of the intervention
     */
	public static void create(OSDiWrapper wrap, IRI parentModelIRI, IRI individualIRI, InterventionType type, String description) {
		wrap.createIndividual(type.getClazz(), individualIRI);
        wrap.assertDataProperty(individualIRI, OSDiDataProperty.HAS_DESCRIPTION, description);
		
		wrap.includeInModel(parentModelIRI, individualIRI);
	}

}
