package es.ull.simulation.model.flow.adapter;

import java.util.HashSet;
import java.util.Set;

import es.ull.simulation.model.ElementInstance;
import es.ull.simulation.model.flow.AbstractStructuredFlow;
import es.ull.simulation.model.flow.IFlow;
import es.ull.simulation.model.flow.IFlowContract;

/**
 * Adapter to expose API {@link IFlowContract} as core {@link IFlow}.
 */
public final class FlowContractToCoreAdapter implements IFlow {
	private final IFlowContract contract;

	public FlowContractToCoreAdapter(final IFlowContract contract) {
		this.contract = contract;
	}

	public IFlowContract getContract() {
		return contract;
	}

	@Override
	public IFlow link(final IFlow successor) {
		final IFlowContract successorContract = FlowAdapters.toContract(successor);
		return FlowAdapters.toFlow(contract.link(successorContract));
	}

	@Override
	public void addPredecessor(final IFlow predecessor) {
		contract.addPredecessor(FlowAdapters.toContract(predecessor));
	}

	@Override
	public AbstractStructuredFlow getParent() {
		final IFlowContract parent = contract.getParent();
		if (parent instanceof FlowContractAdapter) {
			final IFlow coreParent = ((FlowContractAdapter) parent).getDelegate();
			return (coreParent instanceof AbstractStructuredFlow) ? (AbstractStructuredFlow) coreParent : null;
		}
		return null;
	}

	@Override
	public void setParent(final AbstractStructuredFlow parent) {
		contract.setParent(parent == null ? null : new FlowContractAdapter(parent));
	}

	@Override
	public void setRecursiveStructureLink(final AbstractStructuredFlow parent, final Set<IFlow> visited) {
		final Set<IFlowContract> contractVisited = new HashSet<>();
		if (visited != null) {
			for (IFlow flow : visited) {
				contractVisited.add(FlowAdapters.toContract(flow));
			}
		}
		contract.setRecursiveStructureLink(parent == null ? null : new FlowContractAdapter(parent), contractVisited);
	}

	@Override
	public boolean beforeRequest(final ElementInstance ei) {
		return contract.beforeRequest(ei);
	}

	@Override
	public void request(final ElementInstance ei) {
		contract.request(ei);
	}

	@Override
	public void next(final ElementInstance ei) {
		contract.next(ei);
	}

	@Override
	public int getIdentifier() {
		return contract.getIdentifier();
	}

	@Override
	public String getDescription() {
		return contract.getDescription();
	}
}
