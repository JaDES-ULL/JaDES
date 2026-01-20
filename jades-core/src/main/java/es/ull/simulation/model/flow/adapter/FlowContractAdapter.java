package es.ull.simulation.model.flow.adapter;

import java.util.HashSet;
import java.util.Set;

import es.ull.simulation.model.IElementInstance;
import es.ull.simulation.model.flow.IFlow;
import es.ull.simulation.model.flow.IFlowContract;
import es.ull.simulation.model.flow.IFlowDescriptor;

/**
 * Adapter to expose core {@link IFlow} as API {@link IFlowContract}.
 */
public final class FlowContractAdapter implements IFlowContract {
	private final IFlow flow;

	public FlowContractAdapter(final IFlow flow) {
		this.flow = flow;
	}

	public IFlow getDelegate() {
		return flow;
	}

	@Override
	public IFlowContract link(final IFlowContract successor) {
		if (!(successor instanceof FlowContractAdapter)) {
			throw new IllegalArgumentException("Unsupported flow contract implementation: " + successor);
		}
		final IFlow successorFlow = ((FlowContractAdapter) successor).getDelegate();
		return new FlowContractAdapter(flow.link(successorFlow));
	}

	@Override
	public void addPredecessor(final IFlowContract predecessor) {
		if (!(predecessor instanceof FlowContractAdapter)) {
			throw new IllegalArgumentException("Unsupported flow contract implementation: " + predecessor);
		}
		flow.addPredecessor(((FlowContractAdapter) predecessor).getDelegate());
	}

	@Override
	public IFlowContract getParent() {
		final IFlow parent = flow.getParent();
		return parent == null ? null : new FlowContractAdapter(parent);
	}

	@Override
	public void setParent(final IFlowContract parent) {
		if (parent == null) {
			flow.setParent(null);
			return;
		}
		if (!(parent instanceof FlowContractAdapter)) {
			throw new IllegalArgumentException("Unsupported flow contract implementation: " + parent);
		}
		final IFlow parentFlow = ((FlowContractAdapter) parent).getDelegate();
		if (parentFlow instanceof es.ull.simulation.model.flow.AbstractStructuredFlow) {
			flow.setParent((es.ull.simulation.model.flow.AbstractStructuredFlow) parentFlow);
		} else {
			flow.setParent(null);
		}
	}

	@Override
	public void setRecursiveStructureLink(final IFlowContract parent, final Set<IFlowContract> visited) {
		final IFlow parentFlow = parent == null ? null : unwrap(parent);
		final es.ull.simulation.model.flow.AbstractStructuredFlow structuredParent =
				(parentFlow instanceof es.ull.simulation.model.flow.AbstractStructuredFlow)
						? (es.ull.simulation.model.flow.AbstractStructuredFlow) parentFlow
						: null;
		final Set<IFlow> coreVisited = new HashSet<>();
		if (visited != null) {
			for (IFlowContract contract : visited) {
				coreVisited.add(unwrap(contract));
			}
		}
		flow.setRecursiveStructureLink(structuredParent, coreVisited);
	}

	@Override
	public boolean beforeRequest(final IElementInstance ei) {
		return flow.beforeRequest(FlowElementInstanceAdapter.unwrap(ei));
	}

	@Override
	public void request(final IElementInstance ei) {
		flow.request(FlowElementInstanceAdapter.unwrap(ei));
	}

	@Override
	public void next(final IElementInstance ei) {
		flow.next(FlowElementInstanceAdapter.unwrap(ei));
	}

	@Override
	public int getIdentifier() {
		return ((IFlowDescriptor) flow).getIdentifier();
	}

	@Override
	public String getDescription() {
		return ((IFlowDescriptor) flow).getDescription();
	}

	private IFlow unwrap(final IFlowContract contract) {
		if (!(contract instanceof FlowContractAdapter)) {
			throw new IllegalArgumentException("Unsupported flow contract implementation: " + contract);
		}
		return ((FlowContractAdapter) contract).getDelegate();
	}
}

final class FlowElementInstanceAdapter {
	private FlowElementInstanceAdapter() {
	}

	static es.ull.simulation.model.ElementInstance unwrap(final IElementInstance ei) {
		if (ei instanceof es.ull.simulation.model.ElementInstance) {
			return (es.ull.simulation.model.ElementInstance) ei;
		}
		throw new IllegalArgumentException("Unsupported element instance implementation: " + ei);
	}
}
