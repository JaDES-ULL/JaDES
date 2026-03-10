package es.ull.simulation.model.flow.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.ull.simulation.model.IElementInstance;
import es.ull.simulation.model.Simulation;
import es.ull.simulation.model.flow.AbstractStructuredFlow;
import es.ull.simulation.model.flow.ActivityFlow;
import es.ull.simulation.model.flow.IFlow;
import es.ull.simulation.model.flow.IFlowContract;

class FlowContractToCoreAdapterTest {

    private Simulation simulation;

    @BeforeEach
    void setUp() {
        simulation = new Simulation(1, "Test");
    }

    // ── getContract / constructor ─────────────────────────────────────────────

    @Test
    void shouldReturnContract_whenCreated() {
        DummyContract contract = new DummyContract();
        FlowContractToCoreAdapter adapter = new FlowContractToCoreAdapter(contract);

        assertSame(contract, adapter.getContract());
    }

    // ── getIdentifier / getDescription ───────────────────────────────────────

    @Test
    void shouldDelegateIdentifierToContract() {
        FlowContractToCoreAdapter adapter = new FlowContractToCoreAdapter(new DummyContract());
        assertEquals(42, adapter.getIdentifier());
    }

    @Test
    void shouldDelegateDescriptionToContract() {
        FlowContractToCoreAdapter adapter = new FlowContractToCoreAdapter(new DummyContract());
        assertEquals("dummy-desc", adapter.getDescription());
    }

    // ── link ─────────────────────────────────────────────────────────────────

    @Test
    void shouldLinkTwoFlows_whenCalled() {
        FlowContractToCoreAdapter adapter = new FlowContractToCoreAdapter(new DummyContract());
        FlowContractToCoreAdapter successor = new FlowContractToCoreAdapter(new DummyContract());

        IFlow result = adapter.link(successor);
        // DummyContract.link returns the successor contract;
        // FlowAdapters.toFlow on a non-FlowContractAdapter wraps it in a new FlowContractToCoreAdapter
        assertNotNull(result);
    }

    // ── addPredecessor ────────────────────────────────────────────────────────

    @Test
    void shouldAddPredecessor_whenCalled() {
        FlowContractToCoreAdapter adapter = new FlowContractToCoreAdapter(new DummyContract());
        FlowContractToCoreAdapter predecessor = new FlowContractToCoreAdapter(new DummyContract());

        // No debe lanzar excepción
        adapter.addPredecessor(predecessor);
    }

    // ── getParent ─────────────────────────────────────────────────────────────

    @Test
    void shouldReturnNull_whenContractParentIsNull() {
        FlowContractToCoreAdapter adapter = new FlowContractToCoreAdapter(new DummyContract());
        assertNull(adapter.getParent());
    }

    @Test
    void shouldReturnNull_whenContractParentIsNotFlowContractAdapter() {
        // DummyContractWithParent returns a DummyContract (not FlowContractAdapter) as parent
        DummyContract inner = new DummyContract();
        DummyContractWithParent contract = new DummyContractWithParent(inner);
        FlowContractToCoreAdapter adapter = new FlowContractToCoreAdapter(contract);

        assertNull(adapter.getParent());
    }

    @Test
    void shouldReturnNull_whenContractParentIsFlowContractAdapterWrappingNonStructuredFlow() {
        // FlowContractToCoreAdapter itself is an IFlow but not AbstractStructuredFlow
        FlowContractAdapter nonStructuredParentAdapter =
                new FlowContractAdapter(new FlowContractToCoreAdapter(new DummyContract()));
        DummyContractWithParent contract = new DummyContractWithParent(nonStructuredParentAdapter);
        FlowContractToCoreAdapter adapter = new FlowContractToCoreAdapter(contract);

        assertNull(adapter.getParent());
    }

    @Test
    void shouldReturnStructuredFlow_whenContractParentIsFlowContractAdapterWrappingStructuredFlow() {
        ActivityFlow activityFlow = new ActivityFlow(simulation, "Structured");
        FlowContractAdapter parentAdapter = new FlowContractAdapter(activityFlow);
        DummyContractWithParent contract = new DummyContractWithParent(parentAdapter);
        FlowContractToCoreAdapter adapter = new FlowContractToCoreAdapter(contract);

        AbstractStructuredFlow result = adapter.getParent();
        assertEquals(activityFlow, result);
    }

    // ── setParent ─────────────────────────────────────────────────────────────

    @Test
    void shouldSetParentNull_whenCalledWithNull() {
        FlowContractToCoreAdapter adapter = new FlowContractToCoreAdapter(new DummyContract());
        // No debe lanzar excepción
        adapter.setParent(null);
    }

    @Test
    void shouldSetParent_whenCalledWithStructuredFlow() {
        ActivityFlow activityFlow = new ActivityFlow(simulation, "Parent");
        FlowContractToCoreAdapter adapter = new FlowContractToCoreAdapter(new DummyContract());

        // No debe lanzar excepción
        adapter.setParent(activityFlow);
    }

    // ── setRecursiveStructureLink ─────────────────────────────────────────────

    @Test
    void shouldSetRecursiveStructureLink_withNullParentAndNullVisited() {
        FlowContractToCoreAdapter adapter = new FlowContractToCoreAdapter(new DummyContract());
        // No debe lanzar excepción
        adapter.setRecursiveStructureLink(null, null);
    }

    @Test
    void shouldSetRecursiveStructureLink_withStructuredParentAndEmptyVisited() {
        ActivityFlow parent = new ActivityFlow(simulation, "Parent");
        FlowContractToCoreAdapter adapter = new FlowContractToCoreAdapter(new DummyContract());

        // No debe lanzar excepción
        adapter.setRecursiveStructureLink(parent, new HashSet<>());
    }

    @Test
    void shouldSetRecursiveStructureLink_withNonEmptyVisitedSet() {
        FlowContractToCoreAdapter adapter = new FlowContractToCoreAdapter(new DummyContract());
        FlowContractToCoreAdapter visited1 = new FlowContractToCoreAdapter(new DummyContract());
        Set<IFlow> visitedSet = new HashSet<>();
        visitedSet.add(visited1);

        // No debe lanzar excepción; visited son convertidos a contratos
        adapter.setRecursiveStructureLink(null, visitedSet);
    }

    // ── beforeRequest / request / next ────────────────────────────────────────

    @Test
    void shouldDelegateBeforeRequest_toContract() {
        FlowContractToCoreAdapter adapter = new FlowContractToCoreAdapter(new DummyContract());
        // DummyContract.beforeRequest siempre devuelve true
        assertTrue(adapter.beforeRequest(null));
    }

    @Test
    void shouldDelegateRequest_toContract() {
        FlowContractToCoreAdapter adapter = new FlowContractToCoreAdapter(new DummyContract());
        // No debe lanzar excepción
        adapter.request(null);
    }

    @Test
    void shouldDelegateNext_toContract() {
        FlowContractToCoreAdapter adapter = new FlowContractToCoreAdapter(new DummyContract());
        // No debe lanzar excepción
        adapter.next(null);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Contrato dummy que no hace nada y devuelve valores fijos. */
    private static final class DummyContract implements IFlowContract {
        @Override public IFlowContract link(IFlowContract successor) { return successor; }
        @Override public void addPredecessor(IFlowContract predecessor) {}
        @Override public IFlowContract getParent() { return null; }
        @Override public void setParent(IFlowContract parent) {}
        @Override public void setRecursiveStructureLink(IFlowContract parent, Set<IFlowContract> v) {}
        @Override public boolean beforeRequest(IElementInstance ei) { return true; }
        @Override public void request(IElementInstance ei) {}
        @Override public void next(IElementInstance ei) {}
        @Override public int getIdentifier() { return 42; }
        @Override public String getDescription() { return "dummy-desc"; }
    }

    /** Contrato dummy que devuelve un parent configurable. */
    private static final class DummyContractWithParent implements IFlowContract {
        private final IFlowContract parent;
        DummyContractWithParent(IFlowContract parent) { this.parent = parent; }
        @Override public IFlowContract link(IFlowContract successor) { return successor; }
        @Override public void addPredecessor(IFlowContract predecessor) {}
        @Override public IFlowContract getParent() { return parent; }
        @Override public void setParent(IFlowContract parent) {}
        @Override public void setRecursiveStructureLink(IFlowContract parent, Set<IFlowContract> v) {}
        @Override public boolean beforeRequest(IElementInstance ei) { return true; }
        @Override public void request(IElementInstance ei) {}
        @Override public void next(IElementInstance ei) {}
        @Override public int getIdentifier() { return 99; }
        @Override public String getDescription() { return "with-parent"; }
    }
}
