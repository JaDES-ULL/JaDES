/**
 *
 */
package es.ull.simulation.model;

/**
 * A wrapper class for a cycle definition to be used inside a simulation.
 * Thus {@link TimeStamp} can be used to define the cycle parameters.
 * @author Iván Castilla Rodríguez
 *
 */
public interface ISimulationCycle {
	/**
	 * Returns the inner cycle definition.
	 * @return the inner cycle definition
	 */
	ICycle getCycle();
}
