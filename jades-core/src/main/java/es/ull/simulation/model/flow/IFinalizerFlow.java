package es.ull.simulation.model.flow;

import es.ull.simulation.model.ElementInstance;

/**
 * A {@link IFlow} which finishes an execution branch. Only finalizer flows can be used as the last
 * step in a IFlow structure.
 * A {@link IFinalizerFlow} includes a user-defined method {@link #afterFinalize(ElementInstance)}, which is invoked 
 * just after the task performed by the IFlow has been performed.<p>
 * @author Iván Castilla Rodríguez
 */
public interface IFinalizerFlow extends IFlow {
	/**
	 * Allows a user for adding customized code carried out after the IFlow has finished.
	 * @param ei {@link ElementInstance} requesting this IFlow
	 */
	void afterFinalize(ElementInstance ei);

}
