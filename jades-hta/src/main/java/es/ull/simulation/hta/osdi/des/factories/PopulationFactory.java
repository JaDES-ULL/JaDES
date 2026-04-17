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
	 * TODO: Currently, it only uses the deterministic values for the population parameters. It is remaining to create second order parameters to represent the uncertainty on age, sex...
	 * @param model
	 * @param populationWrapper
	 * @param disease
	 * @return
	 */
	public static OSDiPopulation getPopulationInstance(OSDiDESModel model, PopulationWrapper populationWrapper, OSDiDisease disease) throws MalformedOSDiModelException, MalformedSimulationModelException, UnsupportedOSDiFeatureException {
		return new OSDiPopulation(model, populationWrapper, disease);
	}
}
