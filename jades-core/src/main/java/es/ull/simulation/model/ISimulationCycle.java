/**
 * 
 */
package es.ull.simulation.model;

import es.ull.simulation.utils.cycle.Cycle;

/**
 * A wrapper class for {@link es.ull.simulation.utils.cycle.Cycle Cycle} to be used inside a simulation.
 * Thus {@link TimeStamp} can be used to define the cycle parameters.
 * @author Iván Castilla Rodríguez
 *
 */
public interface ISimulationCycle {
	/**
	 * Returns the inner {@link es.ull.simulation.utils.cycle.Cycle Cycle}.
	 * @return the inner {@link es.ull.simulation.utils.cycle.Cycle Cycle}
	 */
	Cycle getCycle();
}
