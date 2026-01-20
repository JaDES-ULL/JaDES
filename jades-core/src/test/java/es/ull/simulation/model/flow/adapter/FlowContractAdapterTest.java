package es.ull.simulation.model.flow.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import es.ull.simulation.model.IElementInstance;
import es.ull.simulation.model.Simulation;
import es.ull.simulation.model.flow.AbstractSingleSuccessorFlow;
import es.ull.simulation.model.flow.IFlow;
import es.ull.simulation.model.flow.IFlowContract;
import es.ull.simulation.model.flow.ITaskFlow;

class FlowContractAdapterTest {

    @Test
    void shouldThrowWhenLinkingUnsupportedContract() {
        Simulation simulation = new Simulation(1, "Test Simulation");
        DummyTaskFlow coreFlow = new DummyTaskFlow(simulation);
        FlowContractAdapter adapter = new FlowContractAdapter(coreFlow);

        IFlowContract unsupported = new DummyContract();
        assertThrows(IllegalArgumentException.class, () -> adapter.link(unsupported));
    }

    @Test
    void shouldThrowWhenAddingUnsupportedPredecessor() {
        Simulation simulation = new Simulation(1, "Test Simulation");
        DummyTaskFlow coreFlow = new DummyTaskFlow(simulation);
        FlowContractAdapter adapter = new FlowContractAdapter(coreFlow);

        IFlowContract unsupported = new DummyContract();
        assertThrows(IllegalArgumentException.class, () -> adapter.addPredecessor(unsupported));
    }

    @Test
    void shouldReturnNullParentWhenUnset() {
        Simulation simulation = new Simulation(1, "Test Simulation");
        DummyTaskFlow coreFlow = new DummyTaskFlow(simulation);
        FlowContractAdapter adapter = new FlowContractAdapter(coreFlow);

        assertNull(adapter.getParent());
        adapter.setParent(null);
        assertNull(adapter.getParent());
    }

    @Test
    void shouldThrowWhenUsingUnsupportedElementInstance() {
        Simulation simulation = new Simulation(1, "Test Simulation");
        DummyTaskFlow coreFlow = new DummyTaskFlow(simulation);
        FlowContractAdapter adapter = new FlowContractAdapter(coreFlow);

        assertThrows(IllegalArgumentException.class, () -> adapter.beforeRequest(null));
        assertThrows(IllegalArgumentException.class, () -> adapter.request(null));
        assertThrows(IllegalArgumentException.class, () -> adapter.next(null));
    }

    @Test
    void shouldExposeIdentifierAndDescription() {
        Simulation simulation = new Simulation(1, "Test Simulation");
        DummyTaskFlow coreFlow = new DummyTaskFlow(simulation);
        FlowContractAdapter adapter = new FlowContractAdapter(coreFlow);

        assertEquals(coreFlow.getIdentifier(), adapter.getIdentifier());
        assertEquals(coreFlow.getDescription(), adapter.getDescription());
    }

    private static final class DummyTaskFlow extends AbstractSingleSuccessorFlow implements ITaskFlow {
        private DummyTaskFlow(Simulation model) {
            super(model);
        }

        @Override
        public void request(es.ull.simulation.model.ElementInstance ei) {
            next(ei);
        }

        @Override
        public void addPredecessor(IFlow predecessor) {
        }

        @Override
        public void finish(es.ull.simulation.model.ElementInstance ei) {
            next(ei);
        }

        @Override
        public void afterFinalize(es.ull.simulation.model.ElementInstance ei) {
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
        public void setRecursiveStructureLink(IFlowContract parent, java.util.Set<IFlowContract> visited) {
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
            return 7;
        }

        @Override
        public String getDescription() {
            return "Dummy";
        }
    }
}
