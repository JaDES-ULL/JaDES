package es.ull.simulation.hta.osdi.des.factories;

import es.ull.simulation.hta.MalformedSimulationModelException;
import es.ull.simulation.hta.osdi.des.OSDiDESModel;
import es.ull.simulation.hta.osdi.des.OSDiDisease;
import es.ull.simulation.hta.osdi.des.OSDiPopulation;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.PopulationWrapper;

/**
 * @author Iván Castilla Rodríguez
 *
 */
public interface PopulationFactory extends DESModelComponentFactory {

	/**
	 * Returns a population according to the description in the ontology. 
	 * @param model DES model to which the population will belong
	 * @param populationWrapper Population description in the ontology
	 * @param disease Disease to which the population belongs
	 * @return Population instance according to the description in the ontology
	 * @throws MalformedOSDiModelException If the population description in the ontology is not valid
	 * @throws MalformedSimulationModelException If the population description in the ontology is valid but it cannot be implemented in the simulation model (e.g., because of missing data)
	 * @throws UnsupportedOSDiFeatureException If the population description in the ontology contains features that are not supported by the simulation model
	 */
	public static OSDiPopulation getPopulationInstance(OSDiDESModel model, PopulationWrapper populationWrapper, OSDiDisease disease) throws MalformedOSDiModelException, MalformedSimulationModelException, UnsupportedOSDiFeatureException {
		return new OSDiPopulation(model, populationWrapper, disease);
	}
}
