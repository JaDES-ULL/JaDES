/**
 * 
 */
package es.ull.simulation.info;

import es.ull.simulation.model.Simulation;

/**
 * A piece of information that is related to a change in the clock of a simulation
 */
public class TimeChangeInfo extends SimulationInfo {

	/**
	 * Creates a new piece of information related to a change in the clock of a simulation
	 * @param simul Simulation
	 * @param ts Timestamp
	 */
	public TimeChangeInfo(Simulation model, long ts) {
		super(model, ts);
	}
	
	@Override
	public String toString() {
		return simul.long2SimulationTime(getTs()) + "\t[SIM]\tCLOCK AVANCED";
	}
}