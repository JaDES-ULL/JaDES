package es.ull.simulation.model.flow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
}
