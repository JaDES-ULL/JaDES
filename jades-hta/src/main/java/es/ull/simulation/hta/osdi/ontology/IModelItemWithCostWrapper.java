package es.ull.simulation.hta.osdi.ontology;

import java.util.Set;

import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.params.ParameterGroup;

public interface IModelItemWithCostWrapper extends IModelItemWithParameterWrapper {

    public default Set<ParameterWrapper> getCosts() throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        return getParametersForProperty(OSDiObjectProperty.HAS_COST, ParameterGroup.COST);
    }

}
