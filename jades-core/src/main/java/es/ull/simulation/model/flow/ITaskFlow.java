/**
 * 
 */
package es.ull.simulation.model.flow;

import es.ull.simulation.model.ElementInstance;

/**
 * A {@link IFlow} which executes some kind of work. A task IFlow is both an {@link IInitializerFlow}
 * and a {@link IFinalizerFlow}. After being requested, it must perform some kind of work, and when 
 * this work is finished, it must notify its end.<p>
 * @author Iván Castilla Rodríguez
 *
 */
public interface ITaskFlow extends IInitializerFlow, IFinalizerFlow {
	/**
	 * Finishes the associated task.
	 * @param ei The element instance which requested this IFlow.
	 */
	void finish(final ElementInstance ei);

}
