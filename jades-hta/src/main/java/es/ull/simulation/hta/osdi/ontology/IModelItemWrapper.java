package es.ull.simulation.hta.osdi.ontology;

import org.semanticweb.owlapi.model.IRI;

import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;

/**
 * Wrapper for any individual defined in the OSDi ontology and belonging to a model.
 * This interface extends IIndividualWrapper to provide access to the model wrapper containing the individual, as well as an initialization method to load all necessary information from the OSDi model. 
 * It also provides a default implementation of the getOSDiWrapper() method that retrieves the OSDiWrapper from the model wrapper.
 * Classes implementing this interface should have a lazy construction, where the constructor only sets the model wrapper and individual IRI, 
 * and the initialize() method loads all the necessary information from the OSDi model. This allows to avoid loading unnecessary information when creating the wrapper, 
 * and to handle potential exceptions that may arise during the loading process in a more flexible way.
 * 
 * @author Iván Castilla Rodríguez
 */
public interface IModelItemWrapper extends IIndividualWrapper {
    public static final String SYNTHETIC_IRI_BASE = OSDiWrapper.OSDI_IRI + "/synthetic#";
    public enum Status {
        NOT_INITIALIZED, INITIALIZING, READY, DIRTY, ERROR
    }
    /**
     * Returns the status of the wrapper, which can be NOT_INITIALIZED, INITIALIZING, READY, DIRTY, or ERROR. This method can be used to check the status of the wrapper before using it, 
     * and to handle potential errors that may arise during the initialization process.
     * @return The status of the wrapper.
     */
    public Status getStatus();

    /**
     * Sets the status of the wrapper. This method can be used to update the status of the wrapper during the initialization process, 
     * or to set it to ERROR if an error occurs during initialization.
     * @param status The new status of the wrapper.
     */
    public void setStatus(Status status);

    /**
     * Returns the model wrapper containing this individual.
     * @return The model wrapper containing this individual.
     */
    public ModelWrapper getModelWrapper();

    /**
     * Controls the status of the wrapper and initializes it by invoking the doInitialize() method. This method should be called after creating the wrapper and before using it.
     * @throws MalformedOSDiModelException
     * @throws UnsupportedOSDiFeatureException
     */
    public default void initialize() throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        if (Status.READY.equals(getStatus()) || Status.DIRTY.equals(getStatus())) {
            return;
        }
        if (Status.INITIALIZING.equals(getStatus())) {
            throw new MalformedOSDiModelException("Circular dependency detected during initialization of wrapper for individual IRI: " + getIndividualIRI());
        }
        setStatus(Status.INITIALIZING);
        try {
            doInitialize();
            setStatus(Status.READY);
        } catch (MalformedOSDiModelException | UnsupportedOSDiFeatureException e) {
            setStatus(Status.ERROR);
            throw e;
        }
    }

    public default void persist() throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        if (Status.DIRTY.equals(getStatus())) {
            try {
                doPersist();
                setStatus(Status.READY);
            } catch (MalformedOSDiModelException | UnsupportedOSDiFeatureException e) {
                setStatus(Status.ERROR);
                throw e;
            }
        }
    }

    /**
     * Performs the initialization of the wrapper by loading all the necessary information from the OSDi model. This method should be implemented by classes implementing this 
     * interface to load the specific information for each type of wrapper, and should not be called directly by users of the wrapper.
     * @throws MalformedOSDiModelException If the OSDi model is malformed, such as if required properties are missing or have invalid values.
     * @throws UnsupportedOSDiFeatureException If the OSDi model uses features that are not supported by this wrapper, such as if the individual belongs to a class that is not supported by this wrapper.
     */
    public void doInitialize() throws MalformedOSDiModelException, UnsupportedOSDiFeatureException;

    /**
     * Generates the axioms for this wrapper.
     * @throws MalformedOSDiModelException If the OSDi model is malformed.
     * @throws UnsupportedOSDiFeatureException If the OSDi model uses features that are not supported by this wrapper.
     */
    public void doPersist() throws MalformedOSDiModelException, UnsupportedOSDiFeatureException;

    @Override
    public default OSDiWrapper getOSDiWrapper() {
        return getModelWrapper().getOSDiWrapper();
    }

    /**
     * Indicates whether this wrapper represents a synthetic item that does not have a direct representation in the OSDi model.
     * @return true if this wrapper represents a synthetic item; false otherwise.
     */
    public default boolean isSynthetic() {
        return false;
    }

    /**
     * Generates a synthetic IRI for an item with the given name, using the SYNTHETIC_IRI_BASE as namespace.
     * @param name The name to use for generating the synthetic IRI. 
     * @return The generated synthetic IRI for the item with the given name.
     */
    public static IRI getSyntheticIRI(String name) {
        return IRI.create(SYNTHETIC_IRI_BASE, name);
    }

    /**
     * Generates a synthetic IRI for an item with the given name, using the SYNTHETIC_IRI_BASE as namespace and the fragment of the sourceIRI as prefix.
     * @param sourceIRI The source IRI to use as prefix for generating the synthetic IRI. 
     * @param name The name to use for generating the synthetic IRI.
     * return The generated synthetic IRI for the item with the given name.
     */
    public static IRI getSyntheticIRIFrom(IRI sourceIRI, String name) {
        String sourceFragment = sourceIRI.getShortForm();
        return IRI.create(SYNTHETIC_IRI_BASE, sourceFragment + "_" + name);
    }


}
