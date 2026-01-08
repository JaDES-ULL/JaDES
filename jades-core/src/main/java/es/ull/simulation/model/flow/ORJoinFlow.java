/**
 * 
 */
package es.ull.simulation.model.flow;

import es.ull.simulation.model.Simulation;

/**
 * A merge IFlow which allows all the true incoming branches to pass.
 * @author Iván Castilla Rodríguez
 */
public abstract class ORJoinFlow extends AbstractMergeFlow {

	/**
	 * Creates a new OR Join IFlow.
	 */
	public ORJoinFlow(Simulation model) {
		super(model);
	}
	
}
