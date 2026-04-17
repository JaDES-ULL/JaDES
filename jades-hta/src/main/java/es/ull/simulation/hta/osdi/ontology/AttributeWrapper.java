package es.ull.simulation.hta.osdi.ontology;

import org.semanticweb.owlapi.model.IRI;

import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;

public class AttributeWrapper extends BaseModelItemWrapper {

    public AttributeWrapper(ModelWrapper modelWrapper, IRI attributeIRI) {
        super(modelWrapper, attributeIRI);
    }

    @Override
    public void doInitialize() throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        // TODO
    }

    @Override
    public void doPersist() throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        // TODO
    }

}
