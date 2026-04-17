package es.ull.simulation.hta.osdi.ontology;

import java.util.Collection;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.TreeSet;

import org.apache.poi.ss.formula.functions.T;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import es.ull.simulation.hta.osdi.OSDiLogger;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.ontology.OWLOntologyWrapper.InstanceCheckMode;

public class ModelWrapper implements IIndividualWrapper {
	/**
	 * A logger for the ModelWrapper class
	 */
	private final static OSDiLogger log = OSDiLogger.getLogger(ModelWrapper.class);
    /**
     * The OSDi wrapper that contains the ontology
     */
    private final OSDiWrapper wrap;
    /**
     * The experiment wrapper that defines the working model and other experiment characteristics
     */
    private final ExperimentWrapper experimentWrapper;
    /**
     * The IRI of the model individual in the ontology
     */
    private final IRI individualIRI;
	/**
	 * The parameter individuals defined in the model.
	 */
	private final Set<ParameterWrapper> parameters;

	/** 
	 * The disease individuals defined in the model 
	 */
    private final Set<DiseaseWrapper> diseaseIndividuals;
	/** 
	 * The population individuals defined in the model 
	 */
    private final Set<PopulationWrapper> populationIndividuals;
	/**
	 *  The intervention individuals defined in the model 
	 */
    private final Set<InterventionWrapper> interventionIndividuals;
	/**
	 * The registry of model item wrappers for the individuals included in the model
	 */
	private final ModelItemWrapperRegistry registry;

    public ModelWrapper(ExperimentWrapper experimentWrapper, IRI individualIRI) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        this.wrap = experimentWrapper.getOSDiWrapper();
        this.experimentWrapper = experimentWrapper;
        this.individualIRI = individualIRI;

		this.registry = new ModelItemWrapperRegistry(wrap, this);
		// ...plus all data item types and expression languages (these are needed for parameters)
		// FIXME: This should not be required. Data items and expression languages should be seeked in all the included individuals, not only model items
		//modelItems.addAll(OSDiClass.DATA_ITEM_TYPE.getIndividuals(wrap));
		//modelItems.addAll(OSDiClass.EXPRESSION_LANGUAGE.getIndividuals(wrap));
		diseaseIndividuals = getModelItemsByClassAs(OSDiClass.DISEASE, DiseaseWrapper.class, false);
		populationIndividuals = getModelItemsByClassAs(OSDiClass.POPULATION, PopulationWrapper.class, false);
		interventionIndividuals = getModelItemsByClassAs(OSDiClass.INTERVENTION, InterventionWrapper.class, false);
		parameters = new TreeSet<>(getModelItemsByClassAs(OSDiClass.PARAMETER, ParameterWrapper.class, false));
		preliminaryValidation();
		for (IModelItemWrapper item : registry.getModelItems()) {
			item.initialize();
		}
    }

	/**
	 * Performs a preliminary validation of the model, checking that it includes at least one disease, one population, and one intervention.
	 * @throws MalformedOSDiModelException In case the model does not include at least one disease, one population, and one intervention.
	 * @throws UnsupportedOSDiFeatureException In case the OSDi model uses features that are not supported by this wrapper.
	 */
	private void preliminaryValidation() throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
		if (diseaseIndividuals.isEmpty()) {
			throw new MalformedOSDiModelException(OSDiClass.MODEL, individualIRI, 
				OSDiObjectProperty.INCLUDES_MODEL_ITEM, 
				"The model does not include any disease.");
		}
		if (populationIndividuals.isEmpty()) {
			throw new MalformedOSDiModelException(OSDiClass.MODEL, individualIRI, 
				OSDiObjectProperty.INCLUDES_MODEL_ITEM, 
				"The model does not include any population.");
		}
		if (interventionIndividuals.isEmpty()) {
			throw new MalformedOSDiModelException(OSDiClass.MODEL, individualIRI, 
				OSDiObjectProperty.INCLUDES_MODEL_ITEM, 
				"The model does not include any intervention.");
		}
	}

    @Override
    public OSDiWrapper getOSDiWrapper() {
        return wrap;
    }

	public ExperimentWrapper getExperimentWrapper() {
		return experimentWrapper;
	}

	@Override
    public IRI getIndividualIRI() {
        return individualIRI;
    }

	/**
	 * Returns the IRI of the disease instances used in the model
	 * @return The IRI of the disease instances
	 */
	public Collection<DiseaseWrapper> getDiseaseIndividuals() {
		return diseaseIndividuals;
	}

	/**
	 * Returns the IRI of the population instances used in the model
	 * @return The IRI of the population instances
	 */
	public Collection<PopulationWrapper> getPopulationIndividuals() {
		return populationIndividuals;
	}

	/**
	 * Returns the set of IRIs of the interventions defined in the model
	 * @return The set of IRIs of the interventions
	 */
	public Collection<InterventionWrapper> getInterventionIndividuals() {
		return interventionIndividuals;
	}

	/**
	 * Returns the IRIs of the items belonging to the working model
	 * @return the IRIs of the items belonging to the working model
	 */
	public Set<IRI> getModelItemIRIs() {
		return registry.getRegisteredModelItemIRIs();
	}

	/**
	 * Returns the individuals of the working model
	 * @return the individuals of the working model
	 * @throws UnsupportedOSDiFeatureException 
	 * @throws MalformedOSDiModelException 
	 */
	public Collection<IModelItemWrapper> getModelItems() throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
		return registry.getModelItems();
	}

	public Collection<IModelItemWrapper> getAndInitializeModelItems() throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
		Collection<IModelItemWrapper> wrappers = getModelItems();
		for (IModelItemWrapper wrapper : wrappers) {
			wrapper.initialize();
		}
		return wrappers;
	}

	/**
	 * Returns the model item wrapper for a given item IRI, if it belongs to the working model.
	 * @param itemIRI The IRI of the item to get the wrapper for
	 * @return an Optional containing the model item wrapper for the given item IRI if it belongs to the working model, or an empty optional otherwise
	 * @throws UnsupportedOSDiFeatureException 
	 * @throws MalformedOSDiModelException 
	 */
	public Optional<IModelItemWrapper> getModelItem(IRI itemIRI) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
		return Optional.ofNullable(registry.getModelItem(itemIRI));
	}

	public Optional<IModelItemWrapper> getAndInitializeModelItem(IRI itemIRI) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
		Optional<IModelItemWrapper> wrapper = getModelItem(itemIRI);
		if (wrapper.isPresent()) {
			wrapper.get().initialize();
		}
		return wrapper;
	}

	/**
	 * Returns the model item wrapper for a given item IRI and wrapper class, if it belongs to the working model and has a wrapper of the expected type.
	 * @param <T> The type of the wrapper expected for the item
	 * @param itemIRI The IRI of the item to get the wrapper for
	 * @param wrapperClass The expected wrapper class for the item
	 * @return an Optional containing the model item wrapper for the given item IRI if it belongs to the working model and has a wrapper of the expected type,
	 *  or an empty optional otherwise
	 */
	public <W extends IModelItemWrapper> Optional<W> getModelItemAs(IRI itemIRI, Class<W> wrapperClass) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
		IModelItemWrapper item = registry.getModelItem(itemIRI);
		if (item != null && wrapperClass.isInstance(item)) {
			return Optional.of(wrapperClass.cast(item));
		}
		return Optional.empty();
	}
	
	public <W extends IModelItemWrapper> Optional<W> getAndInitializeModelItemsAs(IRI itemIRI, Class<W> wrapperClass) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
		Optional<W> wrapper = getModelItemAs(itemIRI, wrapperClass);
		if (wrapper.isPresent()) {
			wrapper.get().initialize();
		}
		return wrapper;
	}

	/**
	 * Returns the individuals of a class that are model items of this model.
	 * @param clazz The class of the individuals to return
	 * @return a set of individuals for the class
	 * @throws UnsupportedOSDiFeatureException 
	 * @throws MalformedOSDiModelException 
	 */
	public Set<IModelItemWrapper> getModelItemsByClass(OSDiClass clazz) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
		return registry.getModelItemsByClass(clazz);
	}    

	public Set<IModelItemWrapper> getAndInitializeModelItemsByClass(OSDiClass clazz) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
		Set<IModelItemWrapper> wrappers = registry.getModelItemsByClass(clazz);
		for (IModelItemWrapper wrapper : wrappers) {
			wrapper.initialize();
		}
		return wrappers;
	}
	
	/**
	 * Returns the model item wrappers for a given OSDi class, if it belongs to the working model and has a wrapper of the expected type.
	 * @param <T> The type of the wrapper expected for the item
	 * @param clazz The OSDi class of the items to get the wrappers for
	 * @param wrapperClass The expected wrapper class for the item
	 * @return a set of model item wrappers for the given OSDi class if they belong to the working model and have a wrapper of the expected type,
	 *  or an empty set otherwise
	 * @throws UnsupportedOSDiFeatureException 
	 */
	public <W extends IModelItemWrapper> Set<W> getModelItemsByClassAs(OSDiClass clazz, Class<W> wrapperClass, boolean onlyDirect) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
		Set<IModelItemWrapper> items = new TreeSet<>();
		items.addAll(registry.getModelItemsByClass(clazz));
		if (!onlyDirect) {
			for (IRI subClassIRI : wrap.getSubClasses(wrap.toIRI(clazz), Imports.INCLUDED, InstanceCheckMode.INFERRED_ALL)) {
				Optional<OSDiClass> subClass = OSDiClass.fromIRI(subClassIRI);
				if (subClass.isPresent()) {
					Set<IModelItemWrapper> subClassItems = registry.getModelItemsByClass(subClass.get());
					items.addAll(subClassItems);
				}
			}
		}
		Set<W> result = new TreeSet<>();
		for (IModelItemWrapper item : items) {
			if (wrapperClass.isInstance(item)) {
				result.add(wrapperClass.cast(item));
			}
			else {
				throw new MalformedOSDiModelException("The individual " + item.getShortName() + 
					" should be a " + clazz.getShortName() + 
					" but a wrapper of type " + item.getClass().getSimpleName() + " was created instead.");
			}
		}
		return result;
	}

	public <W extends IModelItemWrapper> Set<W> getAndInitializeModelItemsByClassAs(OSDiClass clazz, Class<W> wrapperClass, boolean onlyDirect) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
		Set<W> wrappers = getModelItemsByClassAs(clazz, wrapperClass, onlyDirect);
		for (W wrapper : wrappers) {
			wrapper.initialize();
		}
		return wrappers;
	}

	/**
	 * Adds a model item wrapper to the model. If an item with the same IRI already exists in the model, it throws an exception.
	 * @param item The model item wrapper to add
	 * @throws MalformedOSDiModelException If an item with the same IRI already exists in the model
	 * @throws UnsupportedOSDiFeatureException If the OSDi model uses features that are not supported by this wrapper
	 */
	public void registerSyntheticModelItem(IModelItemWrapper item) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
		this.registry.registerSyntheticModelItem(item);
	}

    /**
     * Returns a unique required parameter wrapper for a given individual IRI and property.
     * @param individualIRI The IRI of the individual.
     * @param property The OSDi object property that defines the parameter.
     * @return The unique required parameter wrapper.
     * @throws MalformedOSDiModelException If the OSDi model is malformed or if the required parameter is not found.
     * @throws UnsupportedOSDiFeatureException 
     */
    public ParameterWrapper getUniqueRequiredParameterWrapper(IRI individualIRI, OSDiObjectProperty property) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        final Optional<ParameterWrapper> param = getWrapperForPropertyAs(individualIRI, property, ParameterWrapper.class);
        if (!param.isPresent())
            throw new MalformedOSDiModelException("The individual " + individualIRI + " does not define a required parameter for property " + property);
        return param.get();
    }
    
	/**
	 * Returns a collection of all the parameter wrappers defined in the working model.
	 * @return a collection of all the parameter wrappers defined in the working model
	 */
	public Collection<ParameterWrapper> getParameters() {
		return parameters;
	}

	/**
	 * Processes the hasYear data property of an individual and returns an integer representation of its value. If the property is not defined
	 * or its value has a wrong format, returns the study year defined for the working model. 
	 * @param individualIRI The IRI of a valid individual in the ontology
	 * @return an integer representation of the hasYear data property for an individual
	 */
	public int parseHasYearProperty(IRI individualIRI) {
		OptionalInt year = wrap.getIntegerValue(individualIRI, OSDiDataProperty.HAS_YEAR);
		if (year.isPresent())
			return year.getAsInt();
		return experimentWrapper.getStudyYear();		
	}

	/**
	 * Creates a model by using the working model instance specified and adds it to the ontology.
	 * @param wrap The modifiable OSDi wrapper that contains the ontology
	 * @param individualIRI The IRI of the working model instance
	 * @param type The type of the model
	 * @param author The author of the model
	 * @param description The description of the model
	 * @param geoContext The geographical context of the model
	 * @param year The year of the model
	 * @param reference The reference for the model
	 */
	public static void create(OSDiWrapper wrap, IRI individualIRI, ModelType type, String author, String description, String geoContext, Collection<String> reference) {
		wrap.createIndividual(type.getClazz(), individualIRI);
		wrap.assertDataProperty(individualIRI, OSDiDataProperty.HAS_AUTHOR, author, OWL2Datatype.XSD_STRING);
		wrap.assertDataProperty(individualIRI, OSDiDataProperty.HAS_DESCRIPTION, description, OWL2Datatype.XSD_STRING);
		wrap.assertDataProperty(individualIRI, OSDiDataProperty.HAS_GEOGRAPHICAL_CONTEXT, geoContext, OWL2Datatype.XSD_STRING);
		for (String ref : reference)			
			wrap.assertDataProperty(individualIRI, OSDiDataProperty.HAS_REF_TO, ref, OWL2Datatype.XSD_STRING);
	}
	
	/**
	 * Returns only the first value for the object property of the specified individual that are defined in the model. 
	 * If more than one are defined, prints a warning
	 * @param individualIRI A specific individual in the ontology
	 * @param property The object property to retrieve
	 * @return only the first value for the object property of the specified individual; null if non defined.
	 * @throws UnsupportedOSDiFeatureException 
	 * @throws MalformedOSDiModelException 
	 * @throws IllegalStateException 
	 */
	public Optional<IModelItemWrapper> getWrapperForProperty(IRI individualIRI, OSDiObjectProperty property) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
		Set<IModelItemWrapper> values = getWrappersForProperty(individualIRI, property);
		if (values.size() > 1)
			log.warn(individualIRI, property, "Found more than one value for the object property. Using only " + values.toArray()[0]);
		if (values.size() == 0)
			return Optional.empty();
		return Optional.of(values.toArray(new IModelItemWrapper[0])[0]);
	}
	
	public Optional<IModelItemWrapper> getAndInitializeWrapperForProperty(IRI individualIRI, OSDiObjectProperty property) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
		Optional<IModelItemWrapper> wrapper = getWrapperForProperty(individualIRI, property);
		if (wrapper.isPresent()) {
			wrapper.get().initialize();
		}
		return wrapper;
	}

	/**
	 * Returns all values for the object property of the specified individual that are defined in the model.
	 * @param individualIRI A specific individual in the ontology
	 * @param property The object property to retrieve
	 * @return all values for the object property of the specified individual; empty set if non defined.
	 * @throws UnsupportedOSDiFeatureException 
	 * @throws MalformedOSDiModelException 
	 * @throws IllegalStateException 
	 */
	public Set<IModelItemWrapper> getWrappersForProperty(IRI individualIRI, OSDiObjectProperty property) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
		final Set<IRI> results = wrap.getValues(individualIRI, property);
		results.retainAll(getModelItemIRIs());
		Set<IModelItemWrapper> wrappedResults = new TreeSet<>();
		for (IRI iri : results) {
			wrappedResults.add(getModelItem(iri).orElseThrow(() -> new IllegalStateException("The individual " + iri + " should belong to the working model, but it was not found among the registered model items.")));
		}
		return wrappedResults;
	}	
	
	public Set<IModelItemWrapper> getAndInitializeWrappersForProperty(IRI individualIRI, OSDiObjectProperty property) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
		Set<IModelItemWrapper> wrappers = getWrappersForProperty(individualIRI, property);
		for (IModelItemWrapper wrapper : wrappers) {
			wrapper.initialize();
		}
		return wrappers;
	}

	public <W extends IModelItemWrapper> Optional<W> getWrapperForPropertyAs(IRI individualIRI, OSDiObjectProperty property, Class<W> wrapperClass) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
		final Optional<IModelItemWrapper> value = getWrapperForProperty(individualIRI, property);
		if (value.isPresent()) {
			if (wrapperClass.isInstance(value.get())) {
				return Optional.of(wrapperClass.cast(value.get()));
			}
			else {
				throw new IllegalStateException("The individual " + value.get().getIndividualIRI() + " should belong to the working model and have a wrapper of type " 
					+ wrapperClass.getSimpleName() + ", but its wrapper is of type " + value.get().getClass().getSimpleName() + ".");
			}
		}
		return Optional.empty();
	}

	public <W extends IModelItemWrapper> Optional<W> getAndInitializeWrapperForPropertyAs(IRI individualIRI, OSDiObjectProperty property, Class<W> wrapperClass) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
		Optional<W> wrapper = getWrapperForPropertyAs(individualIRI, property, wrapperClass);
		if (wrapper.isPresent()) {
			wrapper.get().initialize();
		}
		return wrapper;
	}

	public <W extends IModelItemWrapper> Set<W> getWrappersForPropertyAs(IRI individualIRI, OSDiObjectProperty property, Class<W> wrapperClass) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
		final Set<IModelItemWrapper> values = getWrappersForProperty(individualIRI, property);
		Set<W> wrappedResults = new TreeSet<>();
		for (IModelItemWrapper value : values) {
			if (wrapperClass.isInstance(value)) {
				wrappedResults.add(wrapperClass.cast(value));
			}
			else {
				throw new IllegalStateException("The individual " + value.getIndividualIRI() + " should belong to the working model and have a wrapper of type " + wrapperClass.getSimpleName() + ", but its wrapper is of type " + value.getClass().getSimpleName() + ".");
			}
		}
		return wrappedResults;
	}

	public <W extends IModelItemWrapper> Set<W> getAndInitializeWrappersForPropertyAs(IRI individualIRI, OSDiObjectProperty property, Class<W> wrapperClass) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
		Set<W> wrappers = getWrappersForPropertyAs(individualIRI, property, wrapperClass);
		for (W wrapper : wrappers) {
			wrapper.initialize();
		}
		return wrappers;
	}
}
