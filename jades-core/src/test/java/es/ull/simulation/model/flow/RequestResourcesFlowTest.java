package es.ull.simulation.model.flow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.ull.simulation.model.ResourceType;
import es.ull.simulation.model.Simulation;
import es.ull.simulation.model.WorkGroup;

class RequestResourcesFlowTest {
    private Simulation simulation;
    private static final int SIMULATION_ID = 1;
    private static final String SIMULATION_DESC = "Test Simulation";
    private static final String FLOW_DESC = "Test Request Flow";

    @BeforeEach
    void setUp() {
        simulation = new Simulation(SIMULATION_ID, SIMULATION_DESC);
    }

    @Test
    void shouldCreateFlowWithDescription_whenConstructedWithDefaults() {
        RequestResourcesFlow flow = new RequestResourcesFlow(simulation, FLOW_DESC);

        assertEquals(FLOW_DESC, flow.getDescription());
    }

    @Test
    void shouldHaveDefaultPriority_whenConstructedWithoutPriority() {
        RequestResourcesFlow flow = new RequestResourcesFlow(simulation, FLOW_DESC);

        assertEquals(0, flow.getPriority());
    }

    @Test
    void shouldHaveCustomPriority_whenConstructedWithPriority() {
        int priority = 5;
        RequestResourcesFlow flow = new RequestResourcesFlow(simulation, FLOW_DESC, priority);

        assertEquals(priority, flow.getPriority());
    }

    @Test
    void shouldHaveCustomPriorityAndResourceId_whenConstructedWithBothParameters() {
        int resourcesId = 10;
        int priority = 5;
        RequestResourcesFlow flow = new RequestResourcesFlow(simulation, FLOW_DESC, resourcesId, priority);

        assertEquals(priority, flow.getPriority());
        assertEquals(FLOW_DESC, flow.getDescription());
    }

    @Test
    void shouldBelongToSimulation_whenCreated() {
        RequestResourcesFlow flow = new RequestResourcesFlow(simulation, FLOW_DESC);

        assertNotNull(flow.getSimulation());
        assertEquals(simulation, flow.getSimulation());
    }

    @Test
    void shouldNotBeInExclusiveActivity_byDefault() {
        RequestResourcesFlow flow = new RequestResourcesFlow(simulation, FLOW_DESC);

        assertFalse(flow.isInExclusiveActivity());
    }

    @Test
    void shouldHaveZeroWorkGroups_whenCreated() {
        RequestResourcesFlow flow = new RequestResourcesFlow(simulation, FLOW_DESC);

        assertEquals(0, flow.getWorkGroupSize());
    }

    @Test
    void shouldAddWorkGroup_whenUsingWorkGroupAdder() {
        RequestResourcesFlow flow = new RequestResourcesFlow(simulation, FLOW_DESC);
        ResourceType rt = new ResourceType(simulation, "RT");
        WorkGroup wg = new WorkGroup(simulation, rt, 1);

        flow.newWorkGroupAdder(wg).add();

        assertEquals(1, flow.getWorkGroupSize());
    }

    @Test
    void shouldAddMultipleWorkGroups_whenUsingWorkGroupAdder() {
        RequestResourcesFlow flow = new RequestResourcesFlow(simulation, FLOW_DESC);
        ResourceType rt1 = new ResourceType(simulation, "RT1");
        ResourceType rt2 = new ResourceType(simulation, "RT2");
        WorkGroup wg1 = new WorkGroup(simulation, rt1, 1);
        WorkGroup wg2 = new WorkGroup(simulation, rt2, 2);

        flow.newWorkGroupAdder(wg1).add();
        flow.newWorkGroupAdder(wg2).add();

        assertEquals(2, flow.getWorkGroupSize());
    }

    @Test
    void shouldReturnSequentialWorkGroupIds_whenAddingWorkGroups() {
        RequestResourcesFlow flow = new RequestResourcesFlow(simulation, FLOW_DESC);
        ResourceType rt = new ResourceType(simulation, "RT");
        WorkGroup wg1 = new WorkGroup(simulation, rt, 1);
        WorkGroup wg2 = new WorkGroup(simulation, rt, 1);

        int id1 = flow.newWorkGroupAdder(wg1).add();
        int id2 = flow.newWorkGroupAdder(wg2).add();

        assertEquals(0, id1);
        assertEquals(1, id2);
    }

    @Test
    void shouldAllowWorkGroupWithPriority_whenSpecified() {
        RequestResourcesFlow flow = new RequestResourcesFlow(simulation, FLOW_DESC);
        ResourceType rt = new ResourceType(simulation, "RT");
        WorkGroup wg = new WorkGroup(simulation, rt, 1);

        int id = flow.newWorkGroupAdder(wg).withPriority(10).add();

        assertEquals(0, id);
        assertEquals(1, flow.getWorkGroupSize());
    }

    @Test
    void shouldAllowWorkGroupWithDelay_whenSpecified() {
        RequestResourcesFlow flow = new RequestResourcesFlow(simulation, FLOW_DESC);
        ResourceType rt = new ResourceType(simulation, "RT");
        WorkGroup wg = new WorkGroup(simulation, rt, 1);

        int id = flow.newWorkGroupAdder(wg).withDelay(100L).add();

        assertEquals(0, id);
        assertEquals(1, flow.getWorkGroupSize());
    }
}
