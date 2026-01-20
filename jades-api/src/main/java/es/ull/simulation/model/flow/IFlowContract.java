package es.ull.simulation.model.flow;

import java.util.Set;

import es.ull.simulation.model.IElementInstance;

/**
 * Public flow contract decoupled from core implementations.
 */
public interface IFlowContract extends IFlowDescriptor {
	IFlowContract link(final IFlowContract successor);
	void addPredecessor(final IFlowContract predecessor);
	IFlowContract getParent();
	void setParent(final IFlowContract parent);
	void setRecursiveStructureLink(final IFlowContract parent, final Set<IFlowContract> visited);
	boolean beforeRequest(IElementInstance ei);
	void request(final IElementInstance ei);
	void next(final IElementInstance ei);
}
