/**
 * 
 */
package es.ull.simulation.model.flow;

import es.ull.simulation.model.Simulation;

/**
 * Meets the Multiple Instances with a Priori Design-Time Knowledge pattern (WFP13)
 * @author Iván Castilla Rodríguez
 *
 */
public class SynchronizedMultipleInstanceFlow extends StaticPartialJoinMultipleInstancesFlow {

	/**
	 * Creates a Synchronized Multiple Instances IFlow
	 * @param nInstances The number of thread instances this IFlow creates and which must 
	 * finish to pass the control
	 */
	public SynchronizedMultipleInstanceFlow(Simulation model, int nInstances) {
		super(model, nInstances, nInstances);
	}


}
