/**
 * 
 */
package es.ull.simulation.hta.osdi.ontology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.io.IRIDocumentSource;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import es.ull.simulation.ontology.OntologyLoader;
import es.ull.simulation.ontology.LoadedOntology;
import es.ull.simulation.ontology.OWLOntologyWrapper;
import es.ull.simulation.ontology.OntologyLoadOptions;
import es.ull.simulation.hta.osdi.OSDiLogger;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;

/**
 * A wrapper for the OSDi ontology. It provides some useful methods to access the ontology.
 * @author Iván Castilla Rodríguez
 *
 */
public class OSDiWrapper extends OWLOntologyWrapper {
	/** 
	 * The logger for this class 
	 */
	private final static OSDiLogger log = OSDiLogger.getLogger(OSDiWrapper.class);

	/**
	 * The local resource that contains the OSDi ontology (expressed as String)
	 */
	private final static String LOCAL_ONTOLOGY = "/OSDi.ttl";
	/**
	 * The IRI for the OSDi ontology (expressed as String)
	 */
	public final static IRI OSDI_IRI = IRI.create("https://w3id.org/ontologies-ULL/OSDi");
	/**
	 * The OSDi core ontology
	 */
	private final OWLOntology osdiCoreOntology;
	/**
	 * Additional sources of individuals to be loaded in the wrapper. These ontologies must import the OSDi core ontology and will be loaded together 
	 * with the main individuals ontology, allowing to split the model in several files if desired.
	 */
	private final List<OWLOntology> additionalIndividualsSources = new ArrayList<>();

	/**
	 * A builder for the OSDiWrapper class, allowing to specify the ontology load options and the source of the OSDi core ontology.
	 */
	public static class Builder {
		/**
		 * The ontology load options to use when loading the OSDi core and individuals ontologies. If not specified, default options will be used.
		 */
		private OntologyLoadOptions options;
		/**
		 * The source of the OSDi core ontology. If not specified, the local resource will be used.
		 */
		private final OWLOntologyDocumentSource osdiCoreSource;
		/**
		 * Additional sources of individuals to be loaded in the wrapper. These ontologies must import the OSDi core ontology and will be loaded together 
		 * with the main individuals ontology, allowing to split the model in several files if desired.
		 */
		private List<OWLOntologyDocumentSource> additionalIndividualsSources;

		/**
		 * Creates a new builder for the OSDiWrapper class, with default values for all the fields (local resource for the OSDi core ontology and default 
		 * ontology load options).
		 */
		public Builder() {
			this(new IRIDocumentSource(
				Objects.requireNonNull(
					IRI.create(
						Objects.requireNonNull(OSDiWrapper.class.getResource(LOCAL_ONTOLOGY), "OSDi core ontology not found in resources: " + LOCAL_ONTOLOGY)))));
		}

		public Builder(OWLOntologyDocumentSource osdiCoreSource) {
			this.osdiCoreSource = Objects.requireNonNull(osdiCoreSource, "osdiCoreSource must not be null");
			this.options = null;
			this.additionalIndividualsSources = new ArrayList<>();
		}
		/**
		 * Sets the ontology load options to use when loading the OSDi core and individuals ontologies. If not specified, default options will be used.
		 * @param options The ontology load options to use when loading the OSDi core and individuals ontologies. If not specified, default options will be used.
		 * @return This builder, to allow method chaining
		 */
		public Builder withOntologyLoadOptions(OntologyLoadOptions options) {
			this.options = Objects.requireNonNull(options, "options must not be null");
			return this;
		}

		/**
		 * Adds an additional source of individuals to be loaded in the wrapper. These ontologies must import the OSDi core ontology and will be loaded together 
		 * with the main individuals ontology, allowing to split the model in several files if desired.
		 * @param additionalIndividualsSource An additional source of individuals to be loaded in the wrapper. 
		 * @return This builder, to allow method chaining
		 */
		public Builder withAdditionalIndividuals(OWLOntologyDocumentSource additionalIndividualsSource) {
			this.additionalIndividualsSources.add(Objects.requireNonNull(additionalIndividualsSource, "additionalIndividualsSource must not be null"));
			return this;
		}

		/**
		 * Loads the OSDi core ontology from the specified source (local version if no other source was specified). 
		 * Then loads the individuals ontology from the specified source, validates imports and constructs the operational wrapper over the individuals ontology.
		 * @param individualsSource The source of the individuals ontology
		 * @return The OSDi wrapper over the individuals ontology
		 * @throws OWLOntologyCreationException 
		 * @throws MalformedOSDiModelException
		 */
		public OSDiWrapper build(OWLOntologyDocumentSource individualsSource) throws OWLOntologyCreationException, MalformedOSDiModelException {
			final OntologyLoader ontologyLoader = new OntologyLoader();
			final LoadedOntology coreLoaded = (options != null) ? ontologyLoader.load(osdiCoreSource, options) : ontologyLoader.load(osdiCoreSource);
			final OWLOntology core = coreLoaded.ontology();
			final OWLOntologyManager manager = core.getOWLOntologyManager();

			// Obtains the versionIRI declared in the ontology
			final IRI osdiVersionIRI = core.getOntologyID().getVersionIRI().orElseThrow(() -> new MalformedOSDiModelException("The provided OSDi ontology has no ontology IRI declared."));
			final IRI osdiOntologyIRI = core.getOntologyID().getOntologyIRI().orElseThrow(() -> new MalformedOSDiModelException("The provided OSDi ontology has no ontology IRI declared."));
			
			if (!OSDI_IRI.equals(osdiOntologyIRI))
				throw new MalformedOSDiModelException("The provided OSDi ontology IRI does not match the expected one. Found: " + osdiOntologyIRI + ", expected: " + OSDI_IRI);

			// Registers a mapper to solve every import to that IRI to the local resource
			SimpleIRIMapper mapper = new SimpleIRIMapper(Objects.requireNonNull(osdiVersionIRI), Objects.requireNonNull(osdiOntologyIRI));
			manager.getIRIMappers().add(mapper);		

			final List<OWLOntology> additionalOntologies = new ArrayList<>();
			// First load the additional individuals ontologies, to ensure that all the imports are registered before loading the main individuals ontology
			for (OWLOntologyDocumentSource additionalSource : additionalIndividualsSources) {
				final LoadedOntology loaded = ontologyLoader.load(additionalSource, manager);
				assertImportsCore(loaded.ontology(), core);
				additionalOntologies.add(loaded.ontology());
			}
			// Load individuals ontology using the same manager
			final LoadedOntology individualsLoaded = ontologyLoader.load(individualsSource, manager, options);
			// Validate that the individuals ontology imports OSDi (at least by ontology IRI)
			assertImportsCore(individualsLoaded.ontology(), core);
			final OSDiWrapper osdiWrapper = new OSDiWrapper(individualsLoaded, core, additionalOntologies);
			if (!osdiWrapper.getReasoner().isConsistent()) {
				osdiWrapper.getReasoner().dispose();
				throw new MalformedOSDiModelException("The provided ontology is not consistent when loaded with the OSDi ontology.");
			}
			return osdiWrapper;
		}
	}

   /**
     * Minimal check: the working ontology must import the core ontology IRI.
	 * @param individuals The individuals ontology
	 * @param core The core ontology
	 * @throws IllegalArgumentException if the individuals ontology does not import the core ontology
     */
    private static void assertImportsCore(final OWLOntology individuals, final OWLOntology core) {
        final IRI coreOntologyIri = core.getOntologyID()
                .getVersionIRI()
                .orElseThrow(() -> new IllegalStateException("OSDi core has no ontology IRI"));

        final boolean importsCore = individuals.getImportsDeclarations().stream()
                .anyMatch(d -> d.getIRI().equals(coreOntologyIri));

        if (!importsCore) {
            throw new IllegalArgumentException(
                    "Individuals ontology must import OSDi core ontology IRI: " + coreOntologyIri);
        }	
	}
   
	/**
	 * Creates a new OSDiWrapper with the specified individuals ontology, OSDi core ontology and additional individuals sources.
	 * @param individualsOntology The individuals ontology, which must import the OSDi core ontology
	 * @param osdiCoreOntology The OSDi core ontology
	 * @param additionalIndividualsSources Additional sources of individuals to be loaded in the wrapper. 
	 * @throws MalformedOSDiModelException In case the provided ontologies do not conform to the expected structure for OSDi models
	 * @throws OWLOntologyCreationException In case any of the provided ontologies cannot be created from the provided sources
	 */
	private OSDiWrapper(LoadedOntology individualsOntology, OWLOntology osdiCoreOntology, List<OWLOntology> additionalIndividualsSources) throws MalformedOSDiModelException, OWLOntologyCreationException {
		super(Objects.requireNonNull(individualsOntology, "individualsOntology must not be null"));
		this.osdiCoreOntology = Objects.requireNonNull(osdiCoreOntology, "osdiCoreOntology should not be null");
		this.additionalIndividualsSources.addAll(Objects.requireNonNull(additionalIndividualsSources, "additionalIndividualsSources should not be null"));
		improveConsistency();
	}

	/**
	 * Returns the OSDi core ontology.
	 * @return the OSDi core ontology
	 */
	public OWLOntology getOsdiCoreOntology() {
		return osdiCoreOntology;
	}

	/**
	 * Returns the additional individuals ontologies.
	 * @return the additional individuals ontologies
	 */
	public List<OWLOntology> getAdditionalIndividualsSources() {
		return additionalIndividualsSources;
	}

	/**
	 * Returns the IRI for the specified OSDi component, by concatenating the OSDi ontology IRI with the short name of the component.
	 * @param osdiComponentWrapper An OSDi component wrapper, which provides the short name of the component
	 * @return the IRI for the specified OSDi component, by concatenating the OSDi ontology IRI with the short name of the component.
	 */
	public IRI toIRI(IOSDiComponentWrapper osdiComponentWrapper) {
		return toIRI(osdiComponentWrapper.getShortName());
	}

	/**
	 * Sets the experiment and working model for this wrapper. These elements are defined by an ontology that imports the OSDi ontology
	 * @param experimentIRI The name of the experiment that defines the working model and other experiment characteristics
	 * @throws MalformedOSDiModelException In case the provided ontology is not a valid OSDi model
	 * @throws UnsupportedOSDiFeatureException 
	 */
	public ExperimentWrapper buildExperiment(IRI experimentIRI) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
		return new ExperimentWrapper(this, experimentIRI);
	}

	/**
	 * Performs a consistency revision of the ontology, checking that all the instances and properties are correctly defined.
	 * Fixes any inconsistencies found by removing or modifying the problematic instances or properties. Hence, subsequent accesses to
	 * the ontology will be more robust and consistent. This method should be called after loading the ontology and before accessing any of its elements.
	 * Currently this method just reviews the includedByModel and includesModelItem properties to ensure that all instances are included in the model.	 * 
	 */
	public void improveConsistency() {
		improveModelItemsConsistency();
		improveEffectToCauseConsistency();
		improveClinicalProgressionsConsistency();
	}

	/**
	 * Improves the mapping between model items and models in the ontology, ensuring that all model items that are included in a model are correctly linked to that model
	 *  with the includedByModel property, and that all model items that define the includedByModel property are correctly linked to the corresponding model with the 
	 * includesModelItem property. 
	 * This method should be called after loading the ontology and before accessing any of its elements, to ensure a consistent mapping between model items and models.
	 */
	private void improveModelItemsConsistency() {
		final Set<IRI> modelIRIs = getIndividualsOfClass(OSDiClass.MODEL);
		for (IRI modelIRI : modelIRIs) {
			// The set of model items that are included in the working model
			final Set<IRI> includedModelItems = getValues(modelIRI, OSDiObjectProperty.INCLUDES_MODEL_ITEM);
			// The set of model items that are defined in the ontology, independent of the working model
			final Set<IRI> modelItemsToCheck = getIndividualsOfClass(OSDiClass.MODEL_ITEM);
			// Fixes all those model items that are included in the model but donot define the includedByModel property
			for (IRI item : includedModelItems) {
				if (!getValues(item, OSDiObjectProperty.INCLUDED_BY_MODEL).contains(modelIRI)) {
					assertObjectProperty(item, OSDiObjectProperty.INCLUDED_BY_MODEL, modelIRI);
				}
				// Remove the item from the set of model items to check, since it is already included in the model
				modelItemsToCheck.remove(item);
			}
			// Fixes all those model items that are not included in the model but define the includedByModel property
			for (IRI item : modelItemsToCheck) {
				if (getValues(item, OSDiObjectProperty.INCLUDED_BY_MODEL).contains(modelIRI)) {
					assertObjectProperty(modelIRI, OSDiObjectProperty.INCLUDES_MODEL_ITEM, item);
				}
			}
		}
	}

	/**
	 * Improves the mapping between effects and causes in the ontology, ensuring that all effects that have a cause defined are included in the map with the correct set of causes,
	 * and that all causes are correctly linked to their effects. Also ensures that all effects that have no cause defined are included in the map with an empty set of causes, and 
	 * that all causes are correctly linked to their effects. 
	 * This method should be called after loading the ontology and before accessing any of its elements, to ensure a consistent mapping between effects and causes.	
	 */
	private void improveEffectToCauseConsistency() {
		final Map<IRI, Set<IRI>> effectToCauses = new HashMap<>();
		final Set<IRI> causeIRIs = getSubjectsForObjectProperty(OSDiObjectProperty.HAS_EFFECT);
		final Set<IRI> effectIRIs = getIndividualsOfClass(OSDiClass.EFFECT);
		// Ensures that all effects that have a cause defined are included in the map with the correct set of causes,
		//  and that all causes are correctly linked to their effects
		for (IRI causeIRI : causeIRIs) {
			final Set<IRI> effects = getValues(causeIRI, OSDiObjectProperty.HAS_EFFECT);
			for (IRI effectIRI : effects) {
				effectToCauses.computeIfAbsent(effectIRI, k -> new HashSet<>()).add(causeIRI);
				Set<IRI> currentCauses = getValues(effectIRI, OSDiObjectProperty.IS_EFFECT_OF);
				if (!currentCauses.contains(causeIRI)) {
					assertObjectProperty(effectIRI, OSDiObjectProperty.IS_EFFECT_OF, causeIRI);
				}
			}
		}
		// Ensures that all effects that have no cause defined are included in the map with an empty set of causes, 
		// and that all causes are correctly linked to their effects
		for (IRI effectIRI : effectIRIs) {
			if (!effectToCauses.containsKey(effectIRI)) {
				effectToCauses.put(effectIRI, new HashSet<>());
				Set<IRI> currentCauses = getValues(effectIRI, OSDiObjectProperty.IS_EFFECT_OF);
				for (IRI causeIRI : currentCauses) {
					effectToCauses.get(effectIRI).add(causeIRI);
					assertObjectProperty(causeIRI, OSDiObjectProperty.HAS_EFFECT, effectIRI);
				}
			}
		}
	}

	/**
	 * Improves the consistency of the clinical progressions defined in the ontology, ensuring that all sub-progressions of a progression are included in the disease by means of the 
	 * hasProgressionElement property.
	 */
	private void improveClinicalProgressionsConsistency() {
		Set<IRI> allProgressionIRIs = getIndividualsOfClass(OSDiClass.DISEASE_PROGRESSION_ELEMENT);
		Set<IRI> diseaseIRIs = getIndividualsOfClass(OSDiClass.DISEASE);
		for (IRI diseaseIRI : diseaseIRIs) {
			Set<IRI> progressionIRIs = getValues(diseaseIRI, OSDiObjectProperty.HAS_PROGRESSION_ELEMENT);
			for (IRI progressionIRI : progressionIRIs) {
				// In case it is a clinical progression element
				Set<IRI> subProgressionIRIs = getValues(progressionIRI, OSDiObjectProperty.HAS_SUB_PROGRESSION);
				// ... and in case it is a progression combination rule
				subProgressionIRIs.addAll(getValues(progressionIRI, OSDiObjectProperty.AFFECTS_PROGRESSION_ELEMENT));
				for (IRI subProgressionIRI : subProgressionIRIs) {
					// Add subprogressions that are not included directly in the disease
					if (!progressionIRIs.contains(subProgressionIRI)) {
						log.warn("The development " + progressionIRI + " defines a sub-progression " + subProgressionIRI + " that is not included directly in the disease " + diseaseIRI + ". Adding it to the disease progression.");
						assertObjectProperty(diseaseIRI, OSDiObjectProperty.HAS_PROGRESSION_ELEMENT, subProgressionIRI);
					}
				}
			}
			// Removes all the connected progressions from the set of all progression IRIs, to obtain the set of disconnected progressions
			allProgressionIRIs.removeAll(getValues(diseaseIRI, OSDiObjectProperty.HAS_PROGRESSION_ELEMENT));
		}
		for (IRI progressionIRI : allProgressionIRIs) {
			log.warn("The progression " + progressionIRI + " is not included in any disease. It will be ignored.");
		}
	}

	/**
	 * Returns the set of individuals that are subjects of the specified object property. All the imported ontologies will be considered when retrieving the individuals.
	 * @param property The object property for which the subjects will be retrieved
	 * @return the set of individuals that are subjects of the specified object property. All the imported ontologies will be considered when retrieving the individuals.
	 */
	public Set<IRI> getSubjectsForObjectProperty(OSDiObjectProperty property) {
		Set<IRI> subjects = new HashSet<>();
		IRI targetIRI = toIRI(property);

		getOntology().getImportsClosure().forEach(ont -> {
			ont.getAxioms(Objects.requireNonNull(AxiomType.OBJECT_PROPERTY_ASSERTION)).forEach(ax -> {
				if (ax.getProperty().asOWLObjectProperty().getIRI().equals(targetIRI)) {
					OWLIndividual subject = ax.getSubject();
					if (subject.isNamed()) {
						subjects.add(subject.asOWLNamedIndividual().getIRI());
					}
				}
			});
		});
		return subjects;
	}

	/**
	 * Includes a model item in the working model by adding the appropriate object properties in both directions.
	 * @param individualIRI The IRI of the model item to include
	 */
	public void includeInModel(IRI modelIRI, IRI individualIRI) {
		assertObjectProperty(individualIRI, OSDiObjectProperty.INCLUDED_BY_MODEL, modelIRI);
		assertObjectProperty(modelIRI, OSDiObjectProperty.INCLUDES_MODEL_ITEM, individualIRI);
	}

	/**
	 * Returns a set of OSDiClass representing the types of a specified individual.
	 * All the imported ontologies will be considered when retrieving the classes.
	 * @param individualIRI An individual in the ontology
	 * @param mode The mode to use when retrieving the types of the individual. 
	 * @return a set of OSDiClass representing the types of a specified individual.
	 */
	public Set<OSDiClass> getTypes(IRI individualIRI, InstanceCheckMode mode) {
		final Set<OSDiClass> result = new HashSet<>();
		final Set<IRI> typeIRIs = getTypes(individualIRI, Imports.INCLUDED, mode);
		for (IRI typeIRI : typeIRIs) {
			Optional<OSDiClass> cls = OSDiClass.fromIRI(typeIRI);
			if (cls.isPresent()) {
				result.add(cls.get());
			} else {
				log.warn("The individual " + individualIRI + " has a type that is not defined in the OSDi ontology: " + typeIRI);
			}
		}
		return result;
	}
	/**
	 * Returns the individuals defined for the specified class.
	 * All the imported ontologies will be considered when retrieving the individuals. 
	 * @param clazz The class for which the individuals will be retrieved
	 * @return the individuals defined for the specified class.
	 */
	public Set<IRI> getIndividualsOfClass(OSDiClass clazz) {
		return getIndividualsOfClass(toIRI(clazz), Imports.INCLUDED, InstanceCheckMode.ASSERTED_ALL);
	}

	/**
	 * Returns true if the specified individual is an instance of the specified class (or any of its subclasses)
	 * @param individualIRI The IRI of an individual in the ontology
	 * @param clazz The class to check
	 * @return true if the specified individual is an instance of the specified class (or any of its subclasses)
	 */
	public boolean isInstanceOf(IRI individualIRI, OSDiClass clazz) {
		return isInstanceOf(individualIRI, toIRI(clazz), Imports.INCLUDED, InstanceCheckMode.ASSERTED_ALL);
	}

	/**
	 * Creates an individual of the specified class with the specified IRI.
	 * @param clazz The class of the individual to create
	 * @param individualIRI The IRI of the individual to create
	 */
	public void createIndividual(OSDiClass clazz, IRI individualIRI) {
		createIndividual(toIRI(clazz), individualIRI);
	}

	/**
	 * Adds a value for the object property of the specified individual.
	 * @param srcIndividualIRI The individual to which the object property belongs
	 * @param property The object property to add
	 * @param destIndividualIRI The individual to be added as a value for the object property
	 * @return true if the object property was added successfully, false otherwise
	 */
	public boolean assertObjectProperty(IRI srcIndividualIRI, OSDiObjectProperty property, IRI destIndividualIRI) {
		return assertObjectProperty(srcIndividualIRI, toIRI(property), destIndividualIRI);
	}

	/**
	 * Returns only the first value for the object property of the specified individual. If more than one are defined, prints a warning.
	 * @param individualIRI A specific individual in the ontology
	 * @param property The object property to retrieve
	 * @return only the first value for the object property of the specified individual; null if non defined.
	 */
	public Optional<IRI> getValue(IRI individualIRI, OSDiObjectProperty property) {
		Set<IRI> values = getValues(individualIRI, property);
		if (values.size() > 1)
			log.warn(individualIRI, property, "Found more than one value for the object property. Using only " + values.toArray()[0]);
		if (values.size() == 0)
			return Optional.empty();
		return Optional.of((IRI)values.toArray()[0]);
	}
	
	/**
	 * Returns all values for the object property of the specified individual. 
	 * @param wrap The wrapper for the OSDi ontology
	 * @param individualIRI A specific individual in the ontology
	 * @return all values for the object property of the specified individual; empty set if non defined.
	 */
	public Set<IRI> getValues(IRI individualIRI, OSDiObjectProperty property) {
		return getObjectPropertyValues(individualIRI, toIRI(property), Imports.INCLUDED);
	}

	/**
	 * Adds a value for the data property of the specified individual.
	 * @param individualIRI The individual to which the data property belongs
	 * @param property The data property to add
	 * @param value The value to be added for the data property
	 * @return true if the data property was added successfully, false otherwise
	 */
	public boolean assertDataProperty(IRI individualIRI, OSDiDataProperty property, String value) {
		if(!("".equals(value))) {
			return assertDataProperty(individualIRI, toIRI(property), value);
		}
		return false;
	}
	
	/**
	 * Adds a value for the data property of the specified individual, with the specified data type.
	 * @param individualIRI The individual to which the data property belongs
	 * @param property The data property to add
	 * @param value The value to be added for the data property
	 * @param dataType The data type of the value to be added for the data property
	 * @return true if the data property was added successfully, false otherwise
	 */
	public boolean assertDataProperty(IRI individualIRI, OSDiDataProperty property, String value, OWL2Datatype dataType) {
		if(!("".equals(value))) {
			return assertDataProperty(individualIRI, toIRI(property), value, dataType);
		}
		return false;
	}
	
	/**
	 * Returns the values of the specified data property for the specified individual.
	 * @param individualIRI The IRI of an individual in the ontology
	 * @param property The data property to retrieve
	 * @return the values of the specified data property for the specified individual, or an empty set if there is no value for this property.
	 */
	public Set<OWLLiteral> getValues(IRI individualIRI, OSDiDataProperty property) {
		return getDataPropertyValues(individualIRI, toIRI(property), Imports.INCLUDED);
	}

	/**
	 * Returns the value of the specified data property for the specified individual, if it is defined and there is only one value for this property. 
	 * If there are more than one value for this property, a warning is logged and one of the values is returned. If there is no value for this property, an empty optional is returned.
	 * @param individualIRI The IRI of an individual in the ontology
	 * @param property The data property to retrieve
	 * @return the value of the specified data property for the specified individual, if it is defined and there is only one value for this property, or an empty optional if there is no value for this property. 
	 */
	public Optional<OWLLiteral> getValue(IRI individualIRI, OSDiDataProperty property) {
		Set<OWLLiteral> values = getValues(individualIRI, property);
		if (values.size() > 1)
			log.warn(individualIRI, property, "Found more than one value for the data property. Using only " + values.toArray()[0]);
		return  (values.size() == 0) ? Optional.empty() : Optional.of(values.iterator().next());
	}

	/**
	 * Returns the value of the specified data property for the specified individual, formatted as a string. 
	 * If there is no value for this property, an empty optional is returned and a warning is logged in the latter case.
	 * @param individualIRI The IRI of an individual in the ontology
	 * @param property The data property to retrieve
	 * @return the value of the specified data property for the specified individual, formatted as a string, or an empty optional if there is no value for this property. 
	 */
	public Optional<String> getStringValue(IRI individualIRI, OSDiDataProperty property) {
		Optional<OWLLiteral> value = getValue(individualIRI, property);
		return value.isPresent() ? Optional.of(value.get().getLiteral()) : Optional.empty();
	}

	/**
	 * Returns the value of the specified data property for the specified individual, if it is a valid boolean.
	 * If the value is not defined or is not a valid boolean, an empty optional is returned and a warning is logged.
	 * @param individualIRI The IRI of an individual in the ontology
	 * @param property The data property to retrieve
	 * @return the value of the specified data property for the specified individual, if it is a valid boolean, or an empty optional if the value is not defined or is not a valid boolean.
	 */
	public Optional<Boolean> getBooleanValue(IRI individualIRI, OSDiDataProperty property) {
		Optional<OWLLiteral> value = getValue(individualIRI, property);
		if (value.isPresent()) {
			String literal = value.get().getLiteral();
			if ("true".equalsIgnoreCase(literal) || "false".equalsIgnoreCase(literal)) {
				return Optional.of(Boolean.parseBoolean(literal));
			} else {
				log.warn(individualIRI, property, "The value of the data property is not a valid boolean: " + literal);
			}
		}
		return Optional.empty();
	}

	/**
	 * Returns the value of the specified data property for the specified individual, if it is a valid double.
	 * If the value is not defined or is not a valid double, an empty optional is returned and a warning is logged.
	 * @param individualIRI The IRI of an individual in the ontology
	 * @param property The data property to retrieve
	 * @return the value of the specified data property for the specified individual, if it is a valid double, or an empty optional if the value is not defined or is not a valid double.
	 */
	public OptionalDouble getDoubleValue(IRI individualIRI, OSDiDataProperty property) {
		Optional<OWLLiteral> value = getValue(individualIRI, property);
		if (value.isPresent()) {
			try {
				return OptionalDouble.of(Double.parseDouble(value.get().getLiteral()));
			} catch(NumberFormatException ex) {
				log.warn(individualIRI, property, "The value of the data property is not a valid double: " + value.get().getLiteral());
			}
		}
		return OptionalDouble.empty();
	}

	/**
	 * Returns the value of the specified data property for the specified individual, if it is a valid integer.
	 * If the value is not defined or is not a valid integer, an empty optional is returned and a warning is logged.
	 * @param individualIRI The IRI of an individual in the ontology
	 * @param property The data property to retrieve
	 * @return the value of the specified data property for the specified individual, if it is a valid integer, or an empty optional if the value is not defined or is not a valid integer.
	 */
	public OptionalInt getIntegerValue(IRI individualIRI, OSDiDataProperty property) {
		Optional<OWLLiteral> value = getValue(individualIRI, property);
		if (value.isPresent()) {
			try {
				return OptionalInt.of(Integer.parseInt(value.get().getLiteral()));
			} catch(NumberFormatException ex) {
				log.warn(individualIRI, property, "The value of the data property is not a valid integer: " + value.get().getLiteral());
			}
		}
		return OptionalInt.empty();
	}

    /**
     * Returns the set of SKOS categories that are inferred for the specified individual, by retrieving the values of the hasCategory property for that individual 
	 * and all its superclasses.
	 * @param individualIRI The IRI of an individual in the ontology
	 * @return the set of SKOS categories that are inferred for the specified individual.
	 */
    public Set<ResourceUsageSKOSCategory> getInferredSKOSCategories(IRI individualIri) {
        final OWLDataFactory df = getDataFactory();
        final OWLNamedIndividual individual = df.getOWLNamedIndividual(Objects.requireNonNull(individualIri));
		final IRI hasCategoryIRI = Objects.requireNonNull(toIRI(OSDiObjectProperty.HAS_CATEGORY));
        final OWLObjectProperty hasCategory = df.getOWLObjectProperty(hasCategoryIRI);
		return getReasoner().getObjectPropertyValues(Objects.requireNonNull(individual), Objects.requireNonNull(hasCategory))
            .getFlattened()
            .stream()
            .map(OWLNamedIndividual::getIRI)
            .map(ResourceUsageSKOSCategory::fromIRI) 
            .filter(Objects::nonNull) 
            .collect(Collectors.toSet());
    }

	/**
	 * Filters the model items in the provided set, returning only those that are inferred to belong to the specified SKOS category. 
	 * The inference is performed by retrieving the values of the hasCategory property for each parameter and all its superclasses, 
	 * and checking if the specified category is among them.
	 * @param params The set of model items to filter
	 * @param category The SKOS category to filter by
	 * @return the set of model items in the provided set that are inferred to belong to the specified SKOS category.
	 */
	public <T extends IModelItemWrapper> Set<T> filterModelItemsWithSKOSCategory(Set<T> params, ResourceUsageSKOSCategory category) {
		return params.stream()
			.filter(param -> getInferredSKOSCategories(param.getIndividualIRI()).contains(category))
			.collect(Collectors.toSet());
	}

	/**
	 * Filters the model items in the provided set, returning only those that are inferred to not belong to any SKOS category.
	 * @param params The set of model items to filter
	 * @return the set of model items in the provided set that are inferred to not belong to any SKOS category.
	 */
	public <T extends IModelItemWrapper> Set<T> filterModelItemsWithoutAnySKOSCategory(Set<T> params) {
		return params.stream()
			.filter(param -> getInferredSKOSCategories(param.getIndividualIRI()).isEmpty())
			.collect(Collectors.toSet());
	}
}
