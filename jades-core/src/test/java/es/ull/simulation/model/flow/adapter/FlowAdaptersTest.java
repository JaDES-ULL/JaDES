package es.ull.simulation.model.flow.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import es.ull.simulation.model.ElementInstance;
import es.ull.simulation.model.IElementInstance;
import es.ull.simulation.model.Simulation;
import es.ull.simulation.model.flow.AbstractSingleSuccessorFlow;
import es.ull.simulation.model.flow.IFlow;
import es.ull.simulation.model.flow.IFlowContract;
import es.ull.simulation.model.flow.ITaskFlow;

class FlowAdaptersTest {

    @Test
    void shouldHandleNulls() {
        assertNull(FlowAdapters.toContract(null));
        assertNull(FlowAdapters.toFlow(null));
        assertNull(FlowAdapters.toContractSet(null));
        assertNull(FlowAdapters.toFlowSet(null));
    }

    @Test
    void shouldAdaptCoreFlowToContractAndBack() {
        Simulation simulation = new Simulation(1, "Test Simulation");
        DummyTaskFlow coreFlow = new DummyTaskFlow(simulation);

        IFlowContract contract = FlowAdapters.toContract(coreFlow);
        assertNotNull(contract);
        assertTrue(contract instanceof FlowContractAdapter);
        assertSame(coreFlow, ((FlowContractAdapter) contract).getDelegate());

        IFlow backToFlow = FlowAdapters.toFlow(contract);
        assertSame(coreFlow, backToFlow);
    }

    @Test
    void shouldAdaptCustomContractToCoreFlow() {
        DummyContract contract = new DummyContract();

        IFlow flow = FlowAdapters.toFlow(contract);
        assertNotNull(flow);
        assertTrue(flow instanceof FlowContractToCoreAdapter);
        assertSame(contract, ((FlowContractToCoreAdapter) flow).getContract());

        IFlowContract backToContract = FlowAdapters.toContract(flow);
        assertSame(contract, backToContract);
    }

    @Test
    void shouldAdaptSets() {
        Simulation simulation = new Simulation(1, "Test Simulation");
        DummyTaskFlow coreFlow = new DummyTaskFlow(simulation);
        DummyContract contract = new DummyContract();

        Set<IFlowContract> contracts = FlowAdapters.toContractSet(Set.of(coreFlow));
        assertEquals(1, contracts.size());

        Set<IFlow> flows = FlowAdapters.toFlowSet(Set.of(contract));
        assertEquals(1, flows.size());
    }

    private static final class DummyTaskFlow extends AbstractSingleSuccessorFlow implements ITaskFlow {
        private DummyTaskFlow(Simulation model) {
            super(model);
        }

        @Override
        public void request(ElementInstance ei) {
            next(ei);
        }

        @Override
        public void addPredecessor(IFlow predecessor) {
        }

        @Override
        public void finish(ElementInstance ei) {
            next(ei);
        }

        @Override
        public void afterFinalize(ElementInstance ei) {
        }
    }

    private static final class DummyContract implements IFlowContract {
        @Override
        public IFlowContract link(IFlowContract successor) {
            return successor;
        }

        @Override
        public void addPredecessor(IFlowContract predecessor) {
        }

        @Override
        public IFlowContract getParent() {
            return null;
        }

        @Override
        public void setParent(IFlowContract parent) {
        }

        @Override
        public void setRecursiveStructureLink(IFlowContract parent, Set<IFlowContract> visited) {
        }

        @Override
        public boolean beforeRequest(IElementInstance ei) {
            return true;
        }

        @Override
        public void request(IElementInstance ei) {
        }

        @Override
        public void next(IElementInstance ei) {
        }

        @Override
        public int getIdentifier() {
            return 1;
        }

        @Override
        public String getDescription() {
            return "Dummy";
        }
    }
}
