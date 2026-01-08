/**
 * 
 */
package es.ull.simulation.model;

import es.ull.simulation.functions.TimeFunctionParams;
import es.ull.simulation.model.engine.SimulationEngine;

/**
 * @author Ivan Castilla Rodriguez
 *
 */
public abstract class SimulationObject implements Comparable<SimulationObject>, IIdentifiable, ILoggable,
		TimeFunctionParams {
	protected final Simulation simul;
	private final String objectTypeId;
	protected final int id;
    /** String which represents the object */
    private final String idString;
	
    /**
     * Creates a simulation object that belongs to a simulation.
     * @param simul Simulation this object belongs to
     * @param id Object identifier
     * @param objectTypeId a String that identifies the type of simulation object
     */
	public SimulationObject(Simulation simul, int id, String objectTypeId) {
		this.simul = simul;
		this.objectTypeId = objectTypeId;
		this.id = id;
		idString = new String("[" + objectTypeId + id + "]");
		// In case the object is created after the simulation has started
		if (simul.getSimulationEngine() != null)
			assignSimulation(simul.getSimulationEngine());
	}
	
	/**
	 * Returns the simulation this object belongs to
	 * @return the simulation this object belongs to
	 */
	public Simulation getSimulation() {
		return simul;
	}

	/**
	 * Returns a String that identifies the type of simulation object.
	 * This should be a 3-or-less character description.
	 * @return A short string describing the type of the simulation object.
	 */
	public String getObjectTypeIdentifier() {
		return objectTypeId;
	}

	@Override
	public int compareTo(SimulationObject o) {
		if (id < o.id)
			return -1;
		if (id > o.id)
			return 1;
		return 0;
	}

	@Override
	public int getIdentifier() {
		return id;
	}
	
	@Override
	public String toString() {
    	return idString;
    }
	
    /**
     * Returns the current simulation time
     * @return The current simulation time
     */
	public long getTs() {
		return simul.getTs();
	}

	@Override
	public double getTime() {
		return simul.getTs();
	}

    public void debug(String message) {
		logger.debug(this.toString() + "\t" + getTs() + "\t" + message);
	}

    public void trace(String message) {
		logger.trace(this.toString() + "\t" + getTs() + "\t" + message);
	}
	
	public void error(String description) {
		logger.error(this.toString() +
				"\t" + getTs() + "\t" + description);
	}

	/**
	 * Assigns a simulation engine to this object. Useful when different behavior is
	 * expected depending on the engine chosen  
	 * @param engine A simulation engine
	 */
	protected abstract void assignSimulation(SimulationEngine engine);
}
