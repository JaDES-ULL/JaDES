package es.ull.simulation.hta.osdi.ontology;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import es.ull.simulation.hta.osdi.OSDiLogger;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;

public class PopulationWrapper extends BaseModelItemWrapper implements IModelItemWithUtilityWrapper {
    /**
     * The logger for this class
     */
    private final static OSDiLogger log = OSDiLogger.getLogger(PopulationWrapper.class);
    /**
     * The epidemiological parameters related to the population. These parameters are used to characterize the disease in the population, 
     * and they are expected to be related to both the population and the disease. 
     */
    private Set<ParameterWrapper> epidemiologicalParameters = new TreeSet<>();
    /**
     * The attribute values of the population.
     */
    private Set<ParameterWrapper> attributeValues = new TreeSet<>();
    /**
     * The initial proportions of each clinical progression within the population. This is expected to be related to both the population and the clinical progression, and it will be used to initialize the population in the model.
     */
    private Map<ClinicalProgressionWrapper, ParameterWrapper> initialProportions = new TreeMap<>();
    /**
     * The life expectancy parameter of the population.
     */
    private Optional<ParameterWrapper> lifeExpectancy = Optional.empty();
    /**
     * The age parameter of the population. 
     */
    private Optional<ParameterWrapper> ageParameter = Optional.empty();
    /**
     * The sex parameter of the population. 
     */
    private Optional<ParameterWrapper> sexParameter = Optional.empty();
    /**
     * The minimum age of the population.
     */
    private OptionalDouble minAge = OptionalDouble.empty();
    /**
     * The maximum age of the population.
     */
    private OptionalDouble maxAge = OptionalDouble.empty();

    public PopulationWrapper(ModelWrapper modelWrapper, IRI individualIRI) {
        super(modelWrapper, individualIRI);
    }

    @Override
    public void doInitialize() throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        final ModelWrapper modelWrapper = getModelWrapper();
        final IRI individualIRI = getIndividualIRI();
        epidemiologicalParameters = modelWrapper.getWrappersForPropertyAs(individualIRI, OSDiObjectProperty.HAS_EPIDEMIOLOGICAL_PARAMETER, ParameterWrapper.class);
        lifeExpectancy = modelWrapper.getWrapperForPropertyAs(individualIRI, OSDiObjectProperty.HAS_LIFE_EXPECTANCY, ParameterWrapper.class);
        ageParameter = modelWrapper.getWrapperForPropertyAs(individualIRI, OSDiObjectProperty.HAS_AGE, ParameterWrapper.class);
        sexParameter = modelWrapper.getWrapperForPropertyAs(individualIRI, OSDiObjectProperty.HAS_SEX, ParameterWrapper.class);
        minAge = getOSDiWrapper().getDoubleValue(individualIRI, OSDiDataProperty.HAS_MIN_AGE);
        maxAge = getOSDiWrapper().getDoubleValue(individualIRI, OSDiDataProperty.HAS_MAX_AGE);
        attributeValues = modelWrapper.getWrappersForPropertyAs(individualIRI, OSDiObjectProperty.HAS_ATTRIBUTE_VALUE, ParameterWrapper.class);
        // Just in case... Remove those attribute values that are already identified as age, sex or life expectancy parameters
        if (lifeExpectancy.isPresent()) {
            attributeValues.remove(lifeExpectancy.get());
        }
        if (ageParameter.isPresent()) {
            attributeValues.remove(ageParameter.get());
        }
        if (sexParameter.isPresent()) {
            attributeValues.remove(sexParameter.get());
        }
        Set<ParameterWrapper> initialProportionParams = modelWrapper.getWrappersForPropertyAs(individualIRI, OSDiObjectProperty.HAS_INITIAL_PROPORTION, ParameterWrapper.class);
        for (ParameterWrapper param : initialProportionParams) {        
            Set<IModelItemWrapper> relatedProgressions = modelWrapper.getWrappersForProperty(param.getIndividualIRI(), OSDiObjectProperty.IS_PARAMETER_OF);
            relatedProgressions.remove(this); // Remove the population itself from the related model items
            if (relatedProgressions.isEmpty()) {
                log.warn(param.getIndividualIRI(), OSDiObjectProperty.HAS_INITIAL_PROPORTION, "Initial proportion parameters should be related to a clinical progression. No clinical progression found for this parameter.");
            }
            else {
                if (relatedProgressions.size() > 1) {
                    log.warn(param.getIndividualIRI(), OSDiObjectProperty.HAS_INITIAL_PROPORTION, "Initial proportion parameters should be related to a single clinical progression. Found " + relatedProgressions.size() + ": " + relatedProgressions);
                }
                final ClinicalProgressionWrapper progression = (ClinicalProgressionWrapper) relatedProgressions.iterator().next();
                initialProportions.put(progression, param);
            }
        }
    }

    @Override
    public void doPersist() throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        // TODO
    }
    
    /** 
     * Returns the minimum age of the population, if defined.
     * @return The minimum age of the population, if defined.
     */
    public OptionalDouble getMinAge() {
        return minAge;
    }

    /**
     * Returns the maximum age of the population, if defined.
     * @return The maximum age of the population, if defined.
     */
    public OptionalDouble getMaxAge() {
        return maxAge;
    }

    /**
     * Returns the life expectancy parameter defined in the population of this model. 
     * @return The life expectancy parameter.
     */
    public Optional<ParameterWrapper> getLifeExpectancy() {
        return lifeExpectancy;
    } 

    /**
     * Returns the age parameter defined in the population of this model. 
     * @return The age parameter.
     */
    public Optional<ParameterWrapper> getAgeParameter() {
        return ageParameter;
    }

    /**
     * Returns the sex parameter defined in the population of this model.
     * @return The sex parameter.
     */    
    public Optional<ParameterWrapper> getSexParameter() {
        return sexParameter;
    }

    /** 
     * Returns the attribute values of the population.
     * @return The attribute values of the population.
     */
    public Set<ParameterWrapper> getAttributeValues() {
        return attributeValues;
    }

    /**
     * Returns the initial proportions of clinical progressions within this population. The key of the map is the clinical progression, and the value is the parameter that defines the initial proportion.
     * @return The initial proportions of clinical progressions within this population.
     */
    public Map<ClinicalProgressionWrapper, ParameterWrapper> getInitialProportions() {
        return initialProportions;
    }

    /**
     * Returns an epidemiological parameter defined in the population of this model. 
     * @return The epidemiological parameter. Null if not found.
     * @throws MalformedOSDiModelException 
     * @throws UnsupportedOSDiFeatureException 
     */
    public ParameterWrapper getEpidemiologicalCharacterizationOfDisease(EpidemiologicCharacterizationType characterization, DiseaseWrapper diseaseWrapper) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        ParameterWrapper epidem = null;
        for (ParameterWrapper param : epidemiologicalParameters) {
            if (param.getDataItemType() != null && param.getDataItemType().equals(characterization.getType())) {
                if (epidem != null) {
                    log.warn("A population can define just one " + characterization.getLabel() + ". At least two identified: " + param.getIndividualIRI() + " and " + epidem.getIndividualIRI());
                }
                else{
                    epidem = param;
                    final Set<IModelItemWrapper> relatedModelItems = getModelWrapper().getWrappersForProperty(param.getIndividualIRI(), OSDiObjectProperty.IS_PARAMETER_OF);
                    if (!relatedModelItems.contains(diseaseWrapper)) {
                        log.warn(param.getIndividualIRI(), OSDiObjectProperty.IS_PARAMETER_OF, "The parameter is related to the population but not to the disease. We will assume that it is as far as only one disease is supported by OSDi");
                    // TODO: Check whether this is now being replaced by the previous initialization of parameters
                    // epidem = modelWrapper.getParameterWrapper(paramName, characterization.getLabel(), ParameterType.RISK);
                    }
                }
            }
        }
        return epidem;
    }

    /**
     * Returns all epidemiological parameters defined in the population of this model for a specific disease.
     * @return The set of epidemiological parameters. Empty if none found.
     * @throws MalformedOSDiModelException 
     * @throws UnsupportedOSDiFeatureException 
     */
    public Set<ParameterWrapper> getEpidemiologicalCharacterizationOfDisease(DiseaseWrapper diseaseWrapper) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        final Set<ParameterWrapper> epidemSet = new TreeSet<>();
        for (ParameterWrapper param : epidemiologicalParameters) {
            final Set<IModelItemWrapper> relatedModelItems = getModelWrapper().getWrappersForProperty(param.getIndividualIRI(), OSDiObjectProperty.IS_PARAMETER_OF);
            if (relatedModelItems.contains(diseaseWrapper)) {
                epidemSet.add(param);
            }
        }
        return epidemSet;
    }
	
    /**
     * Creates a population instance in the OSDi model.
     * @param wrap The OSDi wrapper
     * @param parentModelIRI The IRI of the parent model
     * @param individualIRI The IRI of the population individual
     * @param description The description of the population
     * @param minAge The minimum age of the population
     * @param maxAge The maximum age of the population
     * @param size The size of the population
     * @param year The year of the population
     */
	public static void create(OSDiWrapper wrap, IRI parentModelIRI, IRI individualIRI, String description, int minAge, int maxAge, int size, int year) {
		wrap.createIndividual(OSDiClass.POPULATION, individualIRI);
        wrap.assertDataProperty(individualIRI, OSDiDataProperty.HAS_DESCRIPTION, description, OWL2Datatype.XSD_STRING);
        wrap.assertDataProperty(individualIRI, OSDiDataProperty.HAS_MIN_AGE, "" + minAge, OWL2Datatype.XSD_INTEGER);
        wrap.assertDataProperty(individualIRI, OSDiDataProperty.HAS_MAX_AGE, "" + maxAge, OWL2Datatype.XSD_INTEGER);
        wrap.assertDataProperty(individualIRI, OSDiDataProperty.HAS_SIZE, "" + size, OWL2Datatype.XSD_INTEGER);
        wrap.assertDataProperty(individualIRI, OSDiDataProperty.HAS_YEAR, "" + year, OWL2Datatype.XSD_INTEGER);
		
		wrap.includeInModel(parentModelIRI, individualIRI);
	}

}
