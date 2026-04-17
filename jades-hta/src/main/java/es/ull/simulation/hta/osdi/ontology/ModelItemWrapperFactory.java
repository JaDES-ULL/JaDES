package es.ull.simulation.hta.osdi.ontology;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.ontology.OWLOntologyWrapper.InstanceCheckMode;
import es.ull.simulation.ontology.ReasonedQuery;

/**
 * Factory responsible of deciding which wrapper to instantiate for an OWL individual.
 * The factory maintains a mapping of OWL class IRIs to wrapper constructors, and uses the individual's asserted types to select the most appropriate constructor.
 * If no specific constructor is found for any of the individual's asserted types, a default generic wrapper is created, which provides basic access to the 
 * individual's IRI and the OSDiWrapper, but does not provide any specific functionality.
 * The factory also provides a method to register new constructors for specific OWL classes, allowing for extensibility and customization of the wrapper creation process.
 */
public final class ModelItemWrapperFactory {

    /** 
     * Fallback when no specific mapping is found. 
     **/
    public static final class GenericWrapper extends BaseModelItemWrapper {
        public GenericWrapper(ModelWrapper modelWrapper, IRI individualIRI) { 
            super(modelWrapper, individualIRI);
        }
        @Override
        public void doInitialize() throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        }
        @Override
        public void doPersist() throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        }
    }

    /**
     * Reference to the OSDiWrapper, needed to access the ontology and the reasoner when creating wrappers.
     */
    private final OSDiWrapper wrap;
    /**
     * Map: OWL class IRI -> wrapper constructor (ModelWrapper, IRI individual -> wrapper).
     */
    private final Map<IRI, BiFunction<ModelWrapper,IRI, ? extends IModelItemWrapper>> constructorsByClassIri = new HashMap<>();
    /**
     * Default constructor used when no specific constructor is registered for any of the individual's asserted types. 
     * It creates a generic wrapper that provides access to the individual's IRI and the OSDiWrapper, but does not provide any specific functionality.
     */
    private final BiFunction<ModelWrapper,IRI, ? extends IModelItemWrapper> defaultConstructor = GenericWrapper::new;

    /**
     * Constructor that initializes the factory with the given OSDiWrapper and registers the constructors defined in the OSDiClass enum.
     * @param wrap The OSDiWrapper used to access the ontology and reasoner when creating wrappers. Must not be null.
     */
    public ModelItemWrapperFactory(OSDiWrapper wrap) {
        this.wrap = Objects.requireNonNull(wrap);
        // Registers all the constructors defined in the OSDiClass enum.
        for (OSDiClass cls : OSDiClass.values()) {
            cls.getConstructor().ifPresent(ctor -> register(wrap.toIRI(cls), ctor));
        }
    }

    /**
     * Registers a wrapper constructor for a given OWL class IRI. The constructor will be used to create wrappers for individuals that are asserted to be of the given class
     * (or its subclasses).
     * @param classIri The IRI of the OWL class for which the constructor is to be registered
     * @param ctor A function that takes a ModelWrapper and an individual IRI, and returns an instance of a wrapper corresponding to the given class. The function must not return null.
     */
    public void register(IRI classIri, BiFunction<ModelWrapper, IRI, ? extends IModelItemWrapper> ctor) {
        constructorsByClassIri.put(Objects.requireNonNull(classIri), Objects.requireNonNull(ctor));
    }

    /**
     * Creates a wrapper for the given individual IRI, based on the registered constructors and the individual's asserted types.
     * The selection of the constructor is based on the following heuristic:
     * 1. Retrieve all the asserted types of the individual
     * 2. Select the "best" registered type among the asserted types (the most specific one, i.e. the one that is not a superclass of any other asserted type)
     * 3. If a registered type is found, use the corresponding constructor to create the wrapper. Otherwise, use the default constructor.
     * 
     * @param modelWrapper The model wrapper to which the created wrapper will belong
     * @param individualIri The IRI of the individual for which the wrapper is to be created
     * @return A wrapper instance corresponding to the given individual IRI.
     */
    public IModelItemWrapper create(ModelWrapper modelWrapper, IRI individualIri) {
        Objects.requireNonNull(modelWrapper);
        Objects.requireNonNull(individualIri);

        Set<IRI> candidateTypes = wrap.getTypes(individualIri, Imports.INCLUDED, InstanceCheckMode.ASSERTED_ALL);
        Optional<IRI> selected = selectBestRegisteredType(candidateTypes);

        if (selected.isPresent()) {
            BiFunction<ModelWrapper,IRI, ? extends IModelItemWrapper> ctor = constructorsByClassIri.get(selected.get());
            if (ctor != null) {
                return ctor.apply(modelWrapper, individualIri);
            }
        }
        return defaultConstructor.apply(modelWrapper, individualIri);
    }

    /**
     * Selects the "best" registered type from a set of candidate types. The heuristic is:
     * 1. If only one candidate type is registered, select it.
     * 2. If multiple candidate types are registered, select the one that is not a superclass of any other candidate type (i.e. the most specific one).
     * 3. If there are multiple candidate types that are not superclasses of each other, select the one with the lexicographically smallest IRI (to ensure determinism).
     * 
     * @param candidateTypes The set of candidate types (IRIs) to select from.
     * @return An Optional containing the selected type IRI, or empty if no candidate type is registered. 
     */
    private Optional<IRI> selectBestRegisteredType(Set<IRI> candidateTypes) {
        final List<IRI> matching = candidateTypes.stream()
                .filter(constructorsByClassIri::containsKey)
                .sorted(Comparator.comparing(IRI::toString))
                .collect(Collectors.toList()); 

        if (matching.isEmpty()) return Optional.empty();
        if (matching.size() == 1) return Optional.of(matching.get(0));

        // Remove any candidate type that is a superclass of another candidate type.
        final ReasonedQuery reasonedQuery = wrap.getReasonedQuery();
        final Set<IRI> toRemove = new HashSet<>();
        for (IRI candidate: matching) {
            final Set<IRI> subClasses = reasonedQuery.getSubClassesInferred(candidate, true);
            if (subClasses.stream().anyMatch(matching::contains)) {
                toRemove.add(candidate);
            }
        }
        matching.removeAll(toRemove);
        return matching.stream().sorted(Comparator.comparing(IRI::toString)).findFirst()
                .or(() -> Optional.of(matching.get(0)));
    }
}
