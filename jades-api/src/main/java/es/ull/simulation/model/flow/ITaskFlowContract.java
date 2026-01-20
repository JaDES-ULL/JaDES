package es.ull.simulation.model.flow;

import es.ull.simulation.model.IElementInstance;

/**
 * Contract for task flows.
 */
public interface ITaskFlowContract extends IInitializerFlowContract, IFinalizerFlowContract {
	void finish(final IElementInstance ei);
}
