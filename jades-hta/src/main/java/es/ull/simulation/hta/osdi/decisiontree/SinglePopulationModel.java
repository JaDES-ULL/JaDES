package es.ull.simulation.hta.osdi.decisiontree;

import es.ull.simulation.hta.MalformedSimulationModelException;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper;
import es.ull.simulation.hta.osdi.ontology.PopulationWrapper;

public interface SinglePopulationModel extends Model {
    PopulationWrapper getPopulationWrapper();
    ParameterWrapper getLifeExpectancy() throws MalformedSimulationModelException, MalformedOSDiModelException, UnsupportedOSDiFeatureException;
    ParameterWrapper getBaseUtility() throws MalformedSimulationModelException, MalformedOSDiModelException, UnsupportedOSDiFeatureException;
}
