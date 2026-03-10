package es.ull.simulation.model.flow.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import es.ull.simulation.model.IElementInstance;
import es.ull.simulation.model.Simulation;
import es.ull.simulation.model.flow.AbstractSingleSuccessorFlow;
import es.ull.simulation.model.flow.ActivityFlow;
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

    // ── Happy-path tests ──────────────────────────────────────────────────────

    @Test
    void shouldReturnDelegate_whenCreated() {
        Simulation simulation = new Simulation(2, "Test Simulation");
        DummyTaskFlow coreFlow = new DummyTaskFlow(simulation);
        FlowContractAdapter adapter = new FlowContractAdapter(coreFlow);

        assertEquals(coreFlow, adapter.getDelegate());
    }

    @Test
    void shouldLinkTwoAdapters_whenBothAreValidFlowContractAdapters() {
        Simulation simulation = new Simulation(3, "Test Simulation");
        DummyTaskFlow flow1 = new DummyTaskFlow(simulation);
        DummyTaskFlow flow2 = new DummyTaskFlow(simulation);
        FlowContractAdapter adapter1 = new FlowContractAdapter(flow1);
        FlowContractAdapter adapter2 = new FlowContractAdapter(flow2);

        IFlowContract linked = adapter1.link(adapter2);

        assertNotNull(linked);
        assertInstanceOf(FlowContractAdapter.class, linked);
    }

    @Test
    void shouldAddPredecessor_whenBothAreValidFlowContractAdapters() {
        Simulation simulation = new Simulation(4, "Test Simulation");
        DummyTaskFlow flow1 = new DummyTaskFlow(simulation);
        DummyTaskFlow flow2 = new DummyTaskFlow(simulation);
        FlowContractAdapter predecessor = new FlowContractAdapter(flow1);
        FlowContractAdapter adapter = new FlowContractAdapter(flow2);

        // No debe lanzar excepción
        adapter.addPredecessor(predecessor);
    }

    @Test
    void shouldSetParentToNull_whenParentHasNonStructuredFlow() {
        Simulation simulation = new Simulation(5, "Test Simulation");
        DummyTaskFlow childFlow = new DummyTaskFlow(simulation);
        DummyTaskFlow parentFlow = new DummyTaskFlow(simulation); // no es AbstractStructuredFlow
        FlowContractAdapter childAdapter = new FlowContractAdapter(childFlow);
        FlowContractAdapter parentAdapter = new FlowContractAdapter(parentFlow);

        // El parent no es AbstractStructuredFlow → internamente llama setParent(null)
        childAdapter.setParent(parentAdapter);
        assertNull(childAdapter.getParent());
    }

    @Test
    void shouldSetParent_whenParentIsAbstractStructuredFlow() {
        Simulation simulation = new Simulation(6, "Test Simulation");
        DummyTaskFlow childFlow = new DummyTaskFlow(simulation);
        ActivityFlow structuredParent = new ActivityFlow(simulation, "Parent");
        FlowContractAdapter childAdapter = new FlowContractAdapter(childFlow);
        FlowContractAdapter parentAdapter = new FlowContractAdapter(structuredParent);

        childAdapter.setParent(parentAdapter);

        assertNotNull(childAdapter.getParent());
    }

    @Test
    void shouldSetRecursiveStructureLink_withNullParentAndNullVisited() {
        Simulation simulation = new Simulation(7, "Test Simulation");
        DummyTaskFlow flow = new DummyTaskFlow(simulation);
        FlowContractAdapter adapter = new FlowContractAdapter(flow);

        // No debe lanzar excepción
        adapter.setRecursiveStructureLink(null, null);
    }

    @Test
    void shouldSetRecursiveStructureLink_withStructuredParentAndEmptyVisited() {
        Simulation simulation = new Simulation(8, "Test Simulation");
        DummyTaskFlow flow = new DummyTaskFlow(simulation);
        ActivityFlow structuredParent = new ActivityFlow(simulation, "Parent");
        FlowContractAdapter adapter = new FlowContractAdapter(flow);
        FlowContractAdapter parentAdapter = new FlowContractAdapter(structuredParent);

        Set<IFlowContract> visited = new HashSet<>();

        // No debe lanzar excepción
        adapter.setRecursiveStructureLink(parentAdapter, visited);
    }

    @Test
    void shouldThrowWhenSetRecursiveStructureLinkWithUnsupportedParent() {
        Simulation simulation = new Simulation(9, "Test Simulation");
        DummyTaskFlow flow = new DummyTaskFlow(simulation);
        FlowContractAdapter adapter = new FlowContractAdapter(flow);
        IFlowContract unsupported = new DummyContract();

        assertThrows(IllegalArgumentException.class,
                () -> adapter.setRecursiveStructureLink(unsupported, null));
    }

    @Test
    void shouldSetRecursiveStructureLink_withValidParentAndNonEmptyVisited() {
        Simulation simulation = new Simulation(10, "Test Simulation");
        DummyTaskFlow flow = new DummyTaskFlow(simulation);
        ActivityFlow structuredParent = new ActivityFlow(simulation, "Parent");
        FlowContractAdapter adapter = new FlowContractAdapter(flow);
        FlowContractAdapter parentAdapter = new FlowContractAdapter(structuredParent);

        // Visited set with the adapter itself (should be unwrapped and forwarded)
        Set<IFlowContract> visited = new HashSet<>();
        visited.add(adapter);

        // No debe lanzar excepción
        adapter.setRecursiveStructureLink(parentAdapter, visited);
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
