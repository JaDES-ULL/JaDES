package es.ull.simulation.hta.osdi.ontology;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;

import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.ontology.OWLOntologyWrapper.InstanceCheckMode;

/**
 * A registry for model item wrappers.
 * This class is intended to be used as a central place to register and retrieve model item wrappers for different classes of individuals in the OSDi ontology. 
 * 
 */
public class ModelItemWrapperRegistry {
    /**
     * A map to store the registered model item wrappers, where the key is the IRI of the individual and the value is a wrapper class
     */
    private final Map<IRI, IModelItemWrapper> registry = new HashMap<>();
	/**
	 * A factory to create the wrappers for the items of the model
	 */
	private final ModelItemWrapperFactory modelItemWrapperFactory;
    /**
     * A map to store the registered model item wrappers by class, where the key is the OSDiClass and the value is a set of wrappers for individuals that are instance of that class. 
     * This map is used to efficiently retrieve all the wrappers for a given class, without having to iterate over all the registered wrappers and check their types.
     */
    private final Map<OSDiClass, Set<IModelItemWrapper>> modelItemsByClass = new HashMap<>();
    /**
     * The model wrapper that contains the ontology information. 
     */
    private final ModelWrapper modelWrapper;

    /**
     * Constructor that initializes the registry by creating a wrapper for each individual of the model and storing it in the registry. 
     * The wrappers are created using the ModelItemWrapperFactory,
     * @param wrap The OSDiWrapper containing the ontology information.
     * @param modelWrapper The model wrapper that contains the ontology information.
     */ 
    public ModelItemWrapperRegistry(OSDiWrapper wrap, ModelWrapper modelWrapper) {
        this.modelWrapper = modelWrapper;
        final IRI modelIRI = modelWrapper.getIndividualIRI();
        this.modelItemWrapperFactory = new ModelItemWrapperFactory(wrap);
		for (IRI modelItemIri : wrap.getValues(modelIRI, OSDiObjectProperty.INCLUDES_MODEL_ITEM)) {
			registerModelItem(modelItemIri);
		}
    }
    
    /**
     * Retrieves the set of IRIs of the individuals for which a wrapper is registered in the registry.
     * @return The set of IRIs of the individuals for which a wrapper is registered in the registry.
     */
    public Set<IRI> getRegisteredModelItemIRIs() {
        return registry.keySet();
    }
    
    /**
     * Retrieves the wrapper for a given individual IRI. If the wrapper has not been initialized yet, it initializes it before returning it.
     * @param individualIri The IRI of the individual for which to retrieve the wrapper.
     * @return The wrapper for the given individual IRI.
     * @throws UnsupportedOSDiFeatureException 
     * @throws MalformedOSDiModelException 
     */
    public IModelItemWrapper getModelItem(IRI individualIri) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        IModelItemWrapper wrapper = registry.get(individualIri);
        if (wrapper == null) {
            throw new IllegalArgumentException("No wrapper registered for individual IRI: " + individualIri);
        }
        return wrapper;
    }
    
    /**
     * Retrieves the model item wrappers for all the individuals that are registered in the registry.
     * Initializes the wrappers that have not been initialized yet before returning them.
     * @return The collection of model item wrappers for all the individuals that are registered in the registry.
     * @throws UnsupportedOSDiFeatureException 
     * @throws MalformedOSDiModelException 
     */
    public Collection<IModelItemWrapper> getModelItems() throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        return registry.values();
    }

    /**
     * Retrieves the set of wrappers for individuals that are instance of a given class. 
     * Initializes the wrappers that have not been initialized yet before returning them.
     * @param clazz The OSDiClass for which to retrieve the wrappers of individuals that are instance of that class.
     * @return The set of wrappers for individuals that are instance of the given class. If no wrapper is registered for any individual of that class, an empty set is returned.
     * @throws UnsupportedOSDiFeatureException 
     * @throws MalformedOSDiModelException 
     */
    public Set<IModelItemWrapper> getModelItemsByClass(OSDiClass clazz) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        return modelItemsByClass.getOrDefault(clazz, Set.of());
    }

    /**
     * Checks if a wrapper is registered for a given individual IRI.
     * @param individualIri The IRI of the individual to check.
     * @return true if a wrapper is registered for the given individual IRI, false otherwise.
     */
    public boolean containsModelItem(IRI individualIri) {
        return registry.containsKey(individualIri);
    }

    /**
     * Registers a wrapper for a given individual IRI. This method is used to register wrappers for individuals that are explicitly included in the ontology, 
     * and therefore have a corresponding individual in the OSDi model.
     * @param individualIri The IRI of the individual for which to register the wrapper.
     */
    private void registerModelItem(IRI individualIri) {
        IModelItemWrapper wrapper = modelItemWrapperFactory.create(modelWrapper, individualIri);
        registry.put(individualIri, wrapper);
        Set<OSDiClass> types = wrapper.getTypes(InstanceCheckMode.ASSERTED_DIRECT);
        for (OSDiClass type : types) {
            modelItemsByClass.computeIfAbsent(type, k -> new HashSet<>()).add(wrapper);
        }
    }

    /**
     * Registers a synthetic wrapper. This method can be used to register wrappers for individuals that are not explicitly included in the ontology,
     * but are needed for some reason (e.g., to represent inferred individuals or to provide additional functionality for certain individuals).
     * Synthetic wrappers are initialized at the time of registration, as they are created with all necessary information and do not depend on any other ontology item.
     * @param wrapper The synthetic wrapper to register for the given individual IRI.
     * @throws UnsupportedOSDiFeatureException 
     * @throws MalformedOSDiModelException 
     */
    public void registerSyntheticModelItem(IModelItemWrapper wrapper) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        IRI individualIri = wrapper.getIndividualIRI();
        if (registry.containsKey(individualIri)) {
            throw new IllegalArgumentException("A wrapper is already registered for individual IRI: " + individualIri);
        }
        // TODO: Review whether I should allow initialization here, or should I prevent any parameter to be registered after the initialization of the modelWrapper
        // Indeed, synthetic parameters do not require initialization in general... 
        wrapper.initialize();
        registry.put(individualIri, wrapper);
        Set<OSDiClass> types = wrapper.getTypes(InstanceCheckMode.ASSERTED_DIRECT);
        for (OSDiClass type : types) {
            modelItemsByClass.computeIfAbsent(type, k -> new HashSet<>()).add(wrapper);
        }
    }
}
