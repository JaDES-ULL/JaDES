package es.ull.simulation.info;

import es.ull.simulation.model.Simulation;

/**
 * Information related to the start, end and time advance during simulation. Collects the simulation timestamp, and the CPU time for the start and
 * end of the simulation.
 * @author Iván Castilla Rodríguez
 *
 */
public class SimulationStartStopInfo extends SimulationInfo {
	/** The types of information related to simulation time */
	public enum Type implements IPieceOfInformation.IInfoType {
		START	("SIMULATION STARTS"), 
		END		("SIMULATION ENDS");
		
		private final String description;
		
		Type (String description) {
			this.description = description;
		}

		public String getDescription() {
			return description;
		}		
		
	};
	/** The CPU time for START and END events; 0L for TICK events */
	final private long cpuTime;
	/** Type of information */
	final private Type type;

	/**
	 * Notifies the start or end of the simulation, or clock advance
	 * @param model Simulation model
	 * @param type Type of information
	 * @param ts Current simulation timestamp
	 */
	public SimulationStartStopInfo(final Simulation model, final Type type, final long ts) {
		super(model, ts);
		this.type = type;
		this.cpuTime = System.nanoTime();
	}

	/**
	 * Returns the CPU time for the start or end of the simulation
	 * @return CPU time
	 */
	public long getCpuTime() {
		return cpuTime;
	}
	
	/**
	 * Returns the type of information
	 * @return Type of information
	 */
	public Type getType() {
		return type;
	}
	
	@Override
	public String toString() {
		return  simul.long2SimulationTime(getTs()) + "\t[SIM]\t" + type.getDescription();
	}
}
