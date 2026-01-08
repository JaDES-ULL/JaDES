package es.ull.simulation.info;

import es.ull.simulation.model.Simulation;

/**
 * A piece of information that is related to a simulation
 */
public abstract class SimulationInfo extends TimeStampedInfo {
	/** The simulation that generated the information */
	final protected Simulation simul;
	
	/**
	 * Creates a new piece of information related to a simulation
	 * @param simul Simulation
	 * @param ts Timestamp
	 */
	public SimulationInfo(final Simulation simul, long ts) {
		super(ts);
		this.simul = simul;
	}

	/**
	 * Returns the simulation that generated the information
	 * @return Simulation
	 */
	public Simulation getSimul() {
		return simul;
	}
}
