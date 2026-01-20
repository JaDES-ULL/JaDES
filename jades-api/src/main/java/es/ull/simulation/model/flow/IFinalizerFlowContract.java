package es.ull.simulation.model.flow;

import es.ull.simulation.model.IElementInstance;

/**
 * Marker for flows that finish an execution branch.
 */
public interface IFinalizerFlowContract extends IFlowContract {
	void afterFinalize(IElementInstance ei);
}
