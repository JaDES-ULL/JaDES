package es.ull.simulation.model.flow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.ull.simulation.condition.AbstractCondition;
import es.ull.simulation.model.ElementInstance;
import es.ull.simulation.model.ResourceType;
import es.ull.simulation.model.Simulation;
import es.ull.simulation.model.WorkGroup;

class ReleaseResourcesFlowTest {
    private Simulation simulation;
    private static final int SIMULATION_ID = 1;
    private static final String SIMULATION_DESC = "Test Simulation";
    private static final String FLOW_DESC = "Test Release Flow";

    @BeforeEach
    void setUp() {
        simulation = new Simulation(SIMULATION_ID, SIMULATION_DESC);
    }

    @Test
    void shouldCreateFlowWithDescription_whenConstructedWithDefaults() {
        ReleaseResourcesFlow flow = new ReleaseResourcesFlow(simulation, FLOW_DESC);

        assertEquals(FLOW_DESC, flow.getDescription());
    }

    @Test
    void shouldBelongToSimulation_whenCreated() {
        ReleaseResourcesFlow flow = new ReleaseResourcesFlow(simulation, FLOW_DESC);

        assertNotNull(flow.getSimulation());
        assertEquals(simulation, flow.getSimulation());
    }

    @Test
    void shouldCreateFlowWithWorkGroup_whenSpecified() {
        ResourceType rt = new ResourceType(simulation, "RT");
        WorkGroup wg = new WorkGroup(simulation, rt, 1);
        ReleaseResourcesFlow flow = new ReleaseResourcesFlow(simulation, FLOW_DESC, wg);

        assertEquals(FLOW_DESC, flow.getDescription());
        assertEquals(simulation, flow.getSimulation());
    }

    @Test
    void shouldCreateFlowWithResourcesId_whenSpecified() {
        int resourcesId = 10;
        ReleaseResourcesFlow flow = new ReleaseResourcesFlow(simulation, FLOW_DESC, resourcesId);

        assertEquals(FLOW_DESC, flow.getDescription());
        assertEquals(simulation, flow.getSimulation());
    }

    @Test
    void shouldCreateFlowWithResourcesIdAndWorkGroup_whenBothSpecified() {
        int resourcesId = 10;
        ResourceType rt = new ResourceType(simulation, "RT");
        WorkGroup wg = new WorkGroup(simulation, rt, 1);
        ReleaseResourcesFlow flow = new ReleaseResourcesFlow(simulation, FLOW_DESC, resourcesId, wg);

        assertEquals(FLOW_DESC, flow.getDescription());
        assertEquals(simulation, flow.getSimulation());
    }

    // ── Nuevos tests ──────────────────────────────────────────────────────────

    @Test
    void shouldReturnResourcesId_whenCreatedWithExplicitId() {
        ReleaseResourcesFlow flow = new ReleaseResourcesFlow(simulation, FLOW_DESC, -5);

        assertEquals(-5, flow.getResourcesId());
    }

    @Test
    void shouldReturnZeroResourcesId_whenCreatedWithDefaultConstructor() {
        ReleaseResourcesFlow flow = new ReleaseResourcesFlow(simulation, FLOW_DESC);

        assertEquals(0, flow.getResourcesId());
    }

    @Test
    void shouldReturnWorkGroup_whenCreatedWithWorkGroup() {
        ResourceType rt = new ResourceType(simulation, "RT");
        WorkGroup wg = new WorkGroup(simulation, rt, 1);
        ReleaseResourcesFlow flow = new ReleaseResourcesFlow(simulation, FLOW_DESC, wg);

        assertEquals(wg, flow.getWorkGroup());
    }

    @Test
    void shouldReturnNullWorkGroup_whenCreatedWithoutWorkGroup() {
        ReleaseResourcesFlow flow = new ReleaseResourcesFlow(simulation, FLOW_DESC);

        assertNull(flow.getWorkGroup());
    }

    @Test
    void shouldReturnREL_forObjectTypeIdentifier() {
        ReleaseResourcesFlow flow = new ReleaseResourcesFlow(simulation, FLOW_DESC);

        assertEquals("REL", flow.getObjectTypeIdentifier());
    }

    @Test
    void shouldReturnZero_whenGetResourceCancellationForUnknownType() {
        ReleaseResourcesFlow flow = new ReleaseResourcesFlow(simulation, FLOW_DESC);
        ResourceType rt = new ResourceType(simulation, "RT");

        // rt no está en la lista → debe devolver 0
        assertEquals(0L, flow.getResourceCancellation(rt, null));
    }

    @Test
    void shouldReturnDuration_whenNoConditionSet() {
        ReleaseResourcesFlow flow = new ReleaseResourcesFlow(simulation, FLOW_DESC);
        ResourceType rt = new ResourceType(simulation, "RT");
        flow.addResourceCancellation(rt, 150L);

        // sin condición → siempre devuelve la duración
        assertEquals(150L, flow.getResourceCancellation(rt, null));
    }

    @Test
    void shouldReturnDuration_whenConditionIsMet() {
        ReleaseResourcesFlow flow = new ReleaseResourcesFlow(simulation, FLOW_DESC);
        ResourceType rt = new ResourceType(simulation, "RT");
        AbstractCondition<ElementInstance> trueCondition = new AbstractCondition<>() {
            @Override public boolean check(ElementInstance fe) { return true; }
        };
        flow.addResourceCancellation(rt, 200L, trueCondition);

        assertEquals(200L, flow.getResourceCancellation(rt, null));
    }

    @Test
    void shouldReturnZero_whenConditionIsNotMet() {
        ReleaseResourcesFlow flow = new ReleaseResourcesFlow(simulation, FLOW_DESC);
        ResourceType rt = new ResourceType(simulation, "RT");
        AbstractCondition<ElementInstance> falseCondition = new AbstractCondition<>() {
            @Override public boolean check(ElementInstance fe) { return false; }
        };
        flow.addResourceCancellation(rt, 300L, falseCondition);

        assertEquals(0L, flow.getResourceCancellation(rt, null));
    }

    @Test
    void shouldNotThrow_whenAddPredecessorCalled() {
        ReleaseResourcesFlow flow = new ReleaseResourcesFlow(simulation, FLOW_DESC);
        ReleaseResourcesFlow other = new ReleaseResourcesFlow(simulation, "other");

        // addPredecessor es un no-op
        flow.addPredecessor(other);
    }

    @Test
    void shouldNotThrow_whenAfterFinalizeCalled() {
        ReleaseResourcesFlow flow = new ReleaseResourcesFlow(simulation, FLOW_DESC);

        // afterFinalize es un no-op
        flow.afterFinalize(null);
    }
}
