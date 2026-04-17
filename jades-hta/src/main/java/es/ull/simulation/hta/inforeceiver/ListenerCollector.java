/**
 * 
 */
package es.ull.simulation.hta.inforeceiver;

/**
 * Classes that implement this interface can collect information from a set of listeners attached to multiple simulations.
 * 
 * @author Iván Castilla Rodríguez
 */
public interface ListenerCollector extends MultipleInterventionListenerBuilder {
	void collectFromListener(int simulationId, AttachableToCollector listener);
}
