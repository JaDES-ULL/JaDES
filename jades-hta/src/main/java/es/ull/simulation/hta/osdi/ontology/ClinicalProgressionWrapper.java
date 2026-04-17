package es.ull.simulation.hta.osdi.ontology;

import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import org.semanticweb.owlapi.model.IRI;

import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;

/**
 * A wrapper for the clinical progression elements defined in OSDi. This wrapper provides access to the risk characterizations, temporal constraints, 
 * and superseded progressions defined for a clinical progression element.
 * It also provides a method to determine the type of clinical progression (acute manifestation, chronic manifestation, development, or stage) based on the OSDi class 
 * of the progression individual.
 * @author Iván Castilla Rodríguez
 */
public class ClinicalProgressionWrapper extends BaseModelItemWrapper implements HealthConditionWrapper {
    /**
     * The IRIs of the risk characterization parameters for this clinical progression, or an empty set if this is the default progression.
     */
    private final Set<IModelItemWrapper> progressionRisks = new TreeSet<>();
    /**
     * The set of clinical progression wrappers that are superseded by this clinical progression.
     */
    private final Set<ClinicalProgressionWrapper> supersededProgressions = new TreeSet<>();
    /**
     * The temporal constraint for this clinical progression, or null if not defined.
     */
    private TemporalConstraint temporalConstraint = null;
    /**
     * The type of this clinical progression, which is determined from the OSDi class of the progression individual. 
     */
    private ClinicalProgressionType type = null;

    /**
     * Creates a clinical progression wrapper for the given individual IRI in the provided model wrapper. 
     * @param modelWrapper The model wrapper containing the clinical progression individual.
     * @param individualIRI The IRI of the clinical progression individual represented by this wrapper.
     */
    public ClinicalProgressionWrapper(ModelWrapper modelWrapper, IRI individualIRI) {
        super(modelWrapper, individualIRI);
    }

    @Override
    public void doInitialize() throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        final IRI individualIRI = getIndividualIRI();
        final ModelWrapper modelWrapper = getModelWrapper();
        this.type = ClinicalProgressionType.fromIRI(individualIRI, getOSDiWrapper());
        this.progressionRisks.clear();
        this.progressionRisks.addAll(modelWrapper.getWrappersForProperty(individualIRI, OSDiObjectProperty.HAS_RISK_CHARACTERIZATION));
        this.supersededProgressions.clear();
        this.supersededProgressions.addAll(modelWrapper.getWrappersForPropertyAs(individualIRI, OSDiObjectProperty.SUPERSEDES_CLINICAL_PROGRESSION, ClinicalProgressionWrapper.class));
        Optional<ParameterWrapper> onsetAgeParam = modelWrapper.getWrapperForPropertyAs(individualIRI, OSDiObjectProperty.HAS_ONSET_AGE, ParameterWrapper.class);
        Optional<ParameterWrapper> endAgeParam = modelWrapper.getWrapperForPropertyAs(individualIRI, OSDiObjectProperty.HAS_END_AGE, ParameterWrapper.class);
        if (onsetAgeParam.isPresent() || endAgeParam.isPresent()) {
            this.temporalConstraint = new TemporalConstraint(onsetAgeParam, endAgeParam);
        }
    }

    @Override
    public void doPersist() throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        // TODO
    }

    /**
     * Indicates whether this clinical progression is the default progression (i.e., has no risk characterization).
     * @return true if this is the default progression; false otherwise.
     */
    public boolean isDefaultProgression() {
        return progressionRisks.isEmpty();
    }

    /**
     * Returns the temporal constraint for this clinical progression.
     * @return The temporal constraint for the disease progression, or null if not defined
     */
    public Optional<TemporalConstraint> getTemporalConstraintForProgression() {
        return Optional.ofNullable(temporalConstraint);
    }

    /**
     * Returns the type of this clinical progression.
     * @return The type of this clinical progression.
     */
    public ClinicalProgressionType getClinicalProgressionType() {
        return type;
    }

    /**
     * Returns the set of risk characterizations defined for this clinical progression. If this is the default progression, this set will be empty.
     * @return the set of risk characterizations defined for this clinical progression. If this is the default progression, this set will be empty.
     */
    public Set<IModelItemWrapper> getProgressionRisks() {
        return progressionRisks;
    }

    /**
     * Returns the set of clinical progressions that are superseded by this clinical progression. If this is the default progression, this set will be empty.
     * @return the set of clinical progressions that are superseded by this clinical progression. If this is the default progression, this set will be empty.
     */
    public Set<ClinicalProgressionWrapper> getSupersededProgressions() {
        return supersededProgressions;
    }    

	/**
	 * Creates a clinical progression element
	 * @param individualIRI The name of the disease progression instance
	 * @param type The type of clinical progression
	 * @param description The description of the disease progression
	 * @param exclusions The set of clinical progressions that are excluded by this progression
	 * @param diseaseIRI The name of the disease instance to which this progression belongs
	 */
	public static void create(OSDiWrapper wrap, IRI parentModelIRI, IRI individualIRI, ClinicalProgressionType type, String description, Set<IRI> exclusions, IRI diseaseIRI) {
		wrap.createIndividual(type.getClazz(), individualIRI);		
        wrap.assertDataProperty(individualIRI, OSDiDataProperty.HAS_DESCRIPTION, description);
		wrap.assertObjectProperty(diseaseIRI, OSDiObjectProperty.HAS_PROGRESSION_ELEMENT, individualIRI);
		wrap.includeInModel(parentModelIRI, individualIRI);
		for (IRI excludedProgIRI : exclusions) {
            wrap.assertObjectProperty(individualIRI, OSDiObjectProperty.IS_SUPERSEDED_BY_CLINICAL_PROGRESSION, excludedProgIRI);
		}
	}
	
}
