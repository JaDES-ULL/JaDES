package es.ull.simulation.hta.osdi.ontology;

import org.semanticweb.owlapi.model.IRI;

/**
 * Abstract base class for all model item wrappers, providing common fields and methods for handling the model wrapper, individual IRI, and status.
 * This class implements the IModelItemWrapper interface and provides a default implementation of the getStatus(), setStatus(), getModelWrapper(), 
 * and getIndividualIRI() methods.
 */
public abstract class BaseModelItemWrapper implements IModelItemWrapper {
    private final ModelWrapper modelWrapper;
    private final IRI individualIRI;
    private Status status = Status.NOT_INITIALIZED;

    public BaseModelItemWrapper(ModelWrapper modelWrapper, IRI individualIRI) {
        this.modelWrapper = modelWrapper;
        this.individualIRI = individualIRI;
    }

    @Override
    public IRI getIndividualIRI() {
        return individualIRI;
    }

    @Override
    public ModelWrapper getModelWrapper() {
        return modelWrapper;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public void setStatus(Status status) {
        this.status = status;
    }

}
