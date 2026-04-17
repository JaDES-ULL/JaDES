package es.ull.simulation.hta.osdi.ontology;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.semanticweb.owlapi.model.IRI;

import es.ull.simulation.hta.MalformedSimulationModelException;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;

public class DiseaseWrapper extends BaseModelItemWrapper implements HealthConditionWrapper {
    /**
     * A set of manifestation objects associated with the disease.
     */
    private final Set<ClinicalProgressionWrapper> manifestations = new TreeSet<>();
    /**
     * A set of development objects associated with the disease.
     */
    private final Set<ClinicalProgressionWrapper> developments = new TreeSet<>();
    /**
     * A set of stages associated with the disease.
     */
    private final Set<ClinicalProgressionWrapper> stages = new TreeSet<>();
    /**
     * A map of the manifestations included by a development.
     */
    private final Map<ClinicalProgressionWrapper, Set<ClinicalProgressionWrapper>> reverseManifestationsToDevelopment = new TreeMap<>();
    /**
     * A map that indicates which combination rule affects which clinical progressions.
     */
    private final Set<ProgressionCombinationRuleWrapper> combinationRules = new TreeSet<>();
    /**
     * A map that indicates which clinical progressions are affected by a combination rule.
     */
    private final Map<ClinicalProgressionWrapper, ProgressionCombinationRuleWrapper> reverseCombinationRuleComponents = new TreeMap<>();

    /**
     * Constructs a DiseaseWrapper for the given individual IRI.
     * @param wrap The OSDi wrapper associated with this disease wrapper.
     * @param individualIRI The IRI of the individual represented by this wrapper.
     * @throws MalformedSimulationModelException
     * @throws UnsupportedOSDiFeatureException
     * @throws MalformedOSDiModelException
     */
    public DiseaseWrapper(ModelWrapper modelWrapper, IRI individualIRI) {
        super(modelWrapper, individualIRI);
    }

    @Override
    public void doInitialize() throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        final ModelWrapper modelWrapper = getModelWrapper();
        // First separate the progression IRIs by type to be able to process them in order
        this.developments.addAll(modelWrapper.getModelItemsByClassAs(OSDiClass.DEVELOPMENT, ClinicalProgressionWrapper.class, false));
        this.manifestations.addAll(modelWrapper.getModelItemsByClassAs(OSDiClass.MANIFESTATION, ClinicalProgressionWrapper.class, false));
        this.stages.addAll(modelWrapper.getModelItemsByClassAs(OSDiClass.STAGE, ClinicalProgressionWrapper.class, false));
        this.combinationRules.addAll(modelWrapper.getAndInitializeModelItemsByClassAs(OSDiClass.PROGRESSION_COMBINATION_RULE, ProgressionCombinationRuleWrapper.class, false));
        populateReverseManifestationToDevelopmentMap(developments);
        // Populate the reverse clinical progression to combination rule map based on the combination rules defined in the model
        for (ProgressionCombinationRuleWrapper combinationRule : combinationRules) {
            final Set<ClinicalProgressionWrapper> affectedProgressions = combinationRule.getProgressions();
            for (ClinicalProgressionWrapper progressionWrapper : affectedProgressions) {
                reverseCombinationRuleComponents.put(progressionWrapper, combinationRule);
            }
        }
    }

    @Override
    public void doPersist() throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        // TODO
    }
    
    /**
     * Populates the reverse manifestation to development map based on the given set of development IRIs
     * @param developments The set of development to consider.
     * @return A set of strings representing manifestation IRIs.
     * @throws UnsupportedOSDiFeatureException
     * @throws MalformedOSDiModelException
     */
    private void populateReverseManifestationToDevelopmentMap(Set<ClinicalProgressionWrapper> developments) throws UnsupportedOSDiFeatureException, MalformedOSDiModelException {
        for (ClinicalProgressionWrapper development : developments) {
            final Set<ClinicalProgressionWrapper> subProgressions = getModelWrapper().getWrappersForPropertyAs(development.getIndividualIRI(), OSDiObjectProperty.HAS_SUB_PROGRESSION, ClinicalProgressionWrapper.class);
            for (ClinicalProgressionWrapper subProgression : subProgressions) {
                if (!ClinicalProgressionType.ACUTE_MANIFESTATION.equals(subProgression.getClinicalProgressionType()) 
                    && !ClinicalProgressionType.CHRONIC_MANIFESTATION.equals(subProgression.getClinicalProgressionType())) {
                    throw new MalformedOSDiModelException("The development " + development + " defines a sub-progression " + subProgression + " that is not a valid manifestation.");
                }
                this.reverseManifestationsToDevelopment.putIfAbsent(development, new TreeSet<>());
                this.reverseManifestationsToDevelopment.get(development).add(subProgression);
            }
        }
    }

    /**
     * Retrieves the combination rules associated with the disease.
     * @return A set of ProgressionCombinationRuleWrapper objects representing the combination rules associated with the disease.
     */
    public Set<ProgressionCombinationRuleWrapper> getCombinationRules() {
        return combinationRules;
    }

    /**
     * Retrieves the manifestations associated with the disease.
     * @return A set of ClinicalProgressionWrapper objects representing the manifestations associated with the disease.
     */
    public Set<ClinicalProgressionWrapper> getManifestations() {
        return manifestations;
    } 

    /**
     * Retrieves the stages associated with the disease.
     * @return A set of ClinicalProgressionWrapper objects representing the stages associated with the disease.
     */
    public Set<ClinicalProgressionWrapper> getStages() {
        return stages;
    }

    /**
    * Retrieves the developments associated with the disease.
    * @return A set of ClinicalProgressionWrapper objects representing the developments associated with the disease.
    */
    public Set<ClinicalProgressionWrapper> getDevelopments() {
        return developments;
    }

    /**
     * Retrieves the combination rule associated with a given progression IRI.
     * @param progressionIRI The IRI of the progression for which to retrieve the associated combination rule.
     * @return An Optional containing the ProgressionCombinationRuleWrapper representing the combination rule associated with the given progression IRI, 
     * or an empty Optional if no combination rule is associated with it.
     */
    public Optional<ProgressionCombinationRuleWrapper> getCombinationRuleForClinicalProgression(ClinicalProgressionWrapper progression) {
        final ProgressionCombinationRuleWrapper combinationRuleIRI = this.reverseCombinationRuleComponents.get(progression);
        return Optional.ofNullable(combinationRuleIRI);
    }

    /**
     * Retrieves the manifestations linked to a given development.
     * @param development The development for which to retrieve the linked manifestations.
     * @return A set of manifestation linked to the given development, or an empty set if no manifestations are linked to it.
     */
    public Set<ClinicalProgressionWrapper> getManifestationsLinkedToDevelopment(ClinicalProgressionWrapper development) {
        return this.reverseManifestationsToDevelopment.getOrDefault(development, new TreeSet<>());
    }
    
	/**
	 * Creates a disease
     * @param parentModel The model wrapper that will contain the disease
	 * @param individualIRI The name of the disease instance
	 * @param description The description of the disease
	 * @param refToDO The reference to the Disease Ontology
	 * @param refToICD The reference to the International Classification of Diseases
	 * @param refToOMIM The reference to the Online Mendelian Inheritance in Man
	 * @param refToSNOMED The reference to the Systematized Nomenclature of Medicine
	 */
	public static void create(OSDiWrapper wrap, IRI parentModelIRI, IRI individualIRI, String description, String refToDO, String refToICD, String refToOMIM, String refToSNOMED) {
		wrap.createIndividual(OSDiClass.DISEASE, individualIRI);	
        wrap.assertDataProperty(individualIRI, OSDiDataProperty.HAS_DESCRIPTION, description);	
        wrap.assertDataProperty(individualIRI, OSDiDataProperty.HAS_REF_TO_DO, refToDO);
        wrap.assertDataProperty(individualIRI, OSDiDataProperty.HAS_REF_TO_ICD, refToICD);
        wrap.assertDataProperty(individualIRI, OSDiDataProperty.HAS_REF_TO_OMIM, refToOMIM);
        wrap.assertDataProperty(individualIRI, OSDiDataProperty.HAS_REF_TO_SNOMED, refToSNOMED);
		wrap.includeInModel(parentModelIRI, individualIRI);
	}
}