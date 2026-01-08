package es.ull.simulation.model.flow;

import es.ull.simulation.model.Simulation;

/**
 * An AND join IFlow which allows only the first true incoming branch to pass. It is
 * reset when all the incoming branches are activated exactly once.
 * Meets the Blocking Discriminator pattern (WFP28). 
 * @author ycallero
 */
public class DiscriminatorFlow extends ANDJoinFlow {
	
	/**
	 * Create a new DiscriminatorFlow.
	 * @param simul Simulation this IFlow belongs to.
	 */
	public DiscriminatorFlow(Simulation model) {
		super(model,1 );
	}

	/**
	 * Create a new discriminator IFlow which can be used in a safe context or a general one.
	 * @param simul Simulation this IFlow belongs to
	 * @param safe True for safe context; false in other case
	 */
	public DiscriminatorFlow(Simulation model, boolean safe) {
		super(model, safe, 1);
	}
	
}
