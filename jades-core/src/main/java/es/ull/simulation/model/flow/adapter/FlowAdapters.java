package es.ull.simulation.model.flow.adapter;

import java.util.HashSet;
import java.util.Set;

import es.ull.simulation.model.flow.IFlow;
import es.ull.simulation.model.flow.IFlowContract;

/**
 * Utilities for adapting between core flows and API flow contracts.
 */
public final class FlowAdapters {
	private FlowAdapters() {
	}

	public static IFlowContract toContract(final IFlow flow) {
		if (flow == null) {
			return null;
		}
		if (flow instanceof FlowContractToCoreAdapter) {
			return ((FlowContractToCoreAdapter) flow).getContract();
		}
		return new FlowContractAdapter(flow);
	}

	public static IFlow toFlow(final IFlowContract contract) {
		if (contract == null) {
			return null;
		}
		if (contract instanceof FlowContractAdapter) {
			return ((FlowContractAdapter) contract).getDelegate();
		}
		return new FlowContractToCoreAdapter(contract);
	}

	public static Set<IFlowContract> toContractSet(final Set<IFlow> flows) {
		if (flows == null) {
			return null;
		}
		final Set<IFlowContract> result = new HashSet<>();
		for (IFlow flow : flows) {
			result.add(toContract(flow));
		}
		return result;
	}

	public static Set<IFlow> toFlowSet(final Set<IFlowContract> contracts) {
		if (contracts == null) {
			return null;
		}
		final Set<IFlow> result = new HashSet<>();
		for (IFlowContract contract : contracts) {
			result.add(toFlow(contract));
		}
		return result;
	}
}
