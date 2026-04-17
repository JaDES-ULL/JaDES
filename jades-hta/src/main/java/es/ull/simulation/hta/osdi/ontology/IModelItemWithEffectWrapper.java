package es.ull.simulation.hta.osdi.ontology;

import java.util.Set;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;

public interface IModelItemWithEffectWrapper extends IModelItemWithParameterWrapper {
    public default Set<EffectWrapper> getEffects() throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        return getModelWrapper().getWrappersForPropertyAs(getIndividualIRI(), OSDiObjectProperty.HAS_EFFECT, EffectWrapper.class);
    }

}
