/**
 * 
 */
package es.ull.simulation.hta.pbdmodel;

import es.ull.simulation.hta.HTAExperiment;
import es.ull.simulation.hta.HTAModel;
import es.ull.simulation.hta.MalformedSimulationModelException;
import es.ull.simulation.hta.interventions.DoNothingIntervention;
import es.ull.simulation.hta.progression.Disease;

/**
 * @author Iván Castilla Rodríguez
 *
 */
public class PBDModel extends HTAModel {

	/**
	 * @param nRuns
	 * @param nPatients
	 */
	public PBDModel(HTAExperiment experiment, boolean allAffected) throws MalformedSimulationModelException {
		super(experiment);
		Disease dis = new PBDDisease(this);
		new PBDPopulation(this, dis, allAffected);
		new DoNothingIntervention(this);
		new PBDNewbornScreening(this);
	}

}
