package es.ull.simulation.hta.osdi.ontology;

import java.util.Optional;
import java.util.Set;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.params.ParameterGroup;

public interface IModelItemWithParameterWrapper extends IModelItemWrapper {

    /**
     * Returns a single parameter associated with the given property for the specified individual.
     * @param property The property whose parameter is to be retrieved
     * @return The parameter associated with the given property for the specified individual, or null if not found
     * @throws MalformedOSDiModelException If the OSDi model for the parameter is malformed
     * @throws UnsupportedOSDiFeatureException 
     */
    public default ParameterWrapper getParameterForProperty(OSDiObjectProperty property, ParameterGroup type) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        final Optional<ParameterWrapper> param = getModelWrapper().getWrapperForPropertyAs(getIndividualIRI(), property, ParameterWrapper.class);
        if (param.isEmpty()) {
            return null;
        }
        return param.get();
        //return getModelWrapper().getParameterWrapper(paramIRI.get(), property.getShortName() + " parameter for " + getIndividualIRI().toString(), type);
    }

    /**
     * Returns all parameters associated with the given property for the specified individual.
     * @param property The property whose parameters are to be retrieved
     * @return A list of parameters associated with the given property for the specified individual
     * @throws MalformedOSDiModelException If the OSDi model for any of the parameters is malformed
     * @throws UnsupportedOSDiFeatureException 
     */
    public default Set<ParameterWrapper> getParametersForProperty(OSDiObjectProperty property, ParameterGroup type) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
//        final Set<ParameterWrapper> parameters = new TreeSet<>();
//       for (IRI paramIRI : getModelWrapper().getValues(getIndividualIRI(), property)) {
//            parameters.add(getModelWrapper().getParameterWrapper(paramIRI, property.getShortName() + " parameter for " + getIndividualIRI().toString(), type));
//        }
        return getModelWrapper().getWrappersForPropertyAs(getIndividualIRI(), property, ParameterWrapper.class);
    }

}
