/**
 * 
 */
package es.ull.simulation.model.flow;

import es.ull.simulation.model.Simulation;

/**
 * A structured IFlow whose initial step is a parallel IFlow and whose final step
 * is a synchronization IFlow. Meets the Interleaved Routing pattern (WFP40) if all the
 * activities are presential.
 * @author Iván Castilla Rodríguez
 */
public class InterleavedRoutingFlow extends AbstractPredefinedStructuredFlow {

	/**
	 * Creates a new InterleavedRoutingFlow 
	 */
	public InterleavedRoutingFlow(Simulation model) {
		super(model);
		initialFlow = new ParallelFlow(model);
		initialFlow.setParent(this);
		finalFlow = new SynchronizationFlow(model);
		finalFlow.setParent(this);
	}
}
