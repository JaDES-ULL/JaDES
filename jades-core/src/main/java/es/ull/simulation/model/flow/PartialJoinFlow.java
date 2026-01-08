package es.ull.simulation.model.flow;

import es.ull.simulation.model.Simulation;

/**
 * An AND join IFlow which allows only the n-st true incoming branch to pass. It is
 * reset when all the incoming branches are activated exactly once.
 * Meets the Blocking Partial Join pattern (WFP31). 
 * @author ycallero
 */
public class PartialJoinFlow extends ANDJoinFlow {
	
	/**
	 * Creates a new PartialJoinFlow.
	 * @param acceptValue Number of incoming branches which activate the IFlow
	 */
	public PartialJoinFlow(Simulation model, int acceptValue) {
		super(model, acceptValue);
	}

	/**
	 * Create a new Partial Join IFlow which can be used in a safe context or a general one.
	 * @param safe True for safe context; false in other case
	 * @param acceptValue Number of incoming branches which activate the IFlow
	 */
	public PartialJoinFlow(Simulation model, boolean safe, int acceptValue) {
		super(model, safe, acceptValue);
	}
	
}
