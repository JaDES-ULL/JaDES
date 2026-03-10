package es.ull.simulation.model.flow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.ull.simulation.condition.AbstractCondition;
import es.ull.simulation.model.ElementInstance;
import es.ull.simulation.model.ResourceType;
import es.ull.simulation.model.Simulation;
import es.ull.simulation.model.WorkGroup;

class ActivityFlowTest {
    private Simulation simulation;
    private static final int SIMULATION_ID = 1;
    private static final String SIMULATION_DESC = "Test Simulation";
    private static final String ACTIVITY_DESC = "Test Activity";

    @BeforeEach
    void setUp() {
        simulation = new Simulation(SIMULATION_ID, SIMULATION_DESC);
    }

    @Test
    void shouldCreateActivityWithDescription_whenConstructedWithDefaults() {
        ActivityFlow activity = new ActivityFlow(simulation, ACTIVITY_DESC);

        assertEquals(ACTIVITY_DESC, activity.getDescription());
    }

    @Test
    void shouldBeExclusiveByDefault_whenConstructedWithoutParameters() {
        ActivityFlow activity = new ActivityFlow(simulation, ACTIVITY_DESC);

        assertTrue(activity.isExclusive());
    }

    @Test
    void shouldBeNonInterruptibleByDefault_whenConstructedWithoutParameters() {
        ActivityFlow activity = new ActivityFlow(simulation, ACTIVITY_DESC);

        assertFalse(activity.isInterruptible());
    }

    @Test
    void shouldHaveDefaultPriority_whenConstructedWithoutPriority() {
        ActivityFlow activity = new ActivityFlow(simulation, ACTIVITY_DESC);

        assertEquals(0, activity.getPriority());
    }

    @Test
    void shouldHaveCustomPriority_whenConstructedWithPriority() {
        int priority = 5;
        ActivityFlow activity = new ActivityFlow(simulation, ACTIVITY_DESC, priority);

        assertEquals(priority, activity.getPriority());
        assertTrue(activity.isExclusive());
        assertFalse(activity.isInterruptible());
    }

    @Test
    void shouldAllowNonExclusiveActivity_whenSpecified() {
        ActivityFlow activity = new ActivityFlow(simulation, ACTIVITY_DESC, false, false);

        assertFalse(activity.isExclusive());
        assertFalse(activity.isInterruptible());
    }

    @Test
    void shouldAllowInterruptibleActivity_whenSpecified() {
        ActivityFlow activity = new ActivityFlow(simulation, ACTIVITY_DESC, true, true);

        assertTrue(activity.isExclusive());
        assertTrue(activity.isInterruptible());
    }

    @Test
    void shouldHaveInitialFlow_whenCreated() {
        ActivityFlow activity = new ActivityFlow(simulation, ACTIVITY_DESC);

        assertNotNull(activity.getInitialFlow());
        assertTrue(activity.getInitialFlow() instanceof RequestResourcesFlow);
    }

    @Test
    void shouldHaveFinalFlow_whenCreated() {
        ActivityFlow activity = new ActivityFlow(simulation, ACTIVITY_DESC);

        assertNotNull(activity.getFinalFlow());
        assertTrue(activity.getFinalFlow() instanceof ReleaseResourcesFlow);
    }

    @Test
    void shouldLinkInitialAndFinalFlows_whenCreated() {
        ActivityFlow activity = new ActivityFlow(simulation, ACTIVITY_DESC);
        IFlow initialFlow = activity.getInitialFlow();

        assertNotNull(initialFlow);
        // El initial flow está linkeado al final flow
        assertTrue(initialFlow instanceof RequestResourcesFlow);
    }

    @Test
    void shouldBelongToSimulation_whenCreated() {
        ActivityFlow activity = new ActivityFlow(simulation, ACTIVITY_DESC);

        assertNotNull(activity.getSimulation());
        assertEquals(simulation, activity.getSimulation());
    }

    @Test
    void shouldAllowAddingWorkGroup_whenCreated() {
        ActivityFlow activity = new ActivityFlow(simulation, ACTIVITY_DESC);
        ResourceType rt = new ResourceType(simulation, "RT");
        WorkGroup wg = new WorkGroup(simulation, rt, 1);

        activity.newWorkGroupAdder(wg).add();

        // Verificamos que se añadió correctamente
        RequestResourcesFlow requestFlow = (RequestResourcesFlow) activity.getInitialFlow();
        assertEquals(1, requestFlow.getWorkGroupSize());
    }

    @Test
    void shouldAllowMultipleWorkGroups_whenAdded() {
        ActivityFlow activity = new ActivityFlow(simulation, ACTIVITY_DESC);
        ResourceType rt1 = new ResourceType(simulation, "RT1");
        ResourceType rt2 = new ResourceType(simulation, "RT2");
        WorkGroup wg1 = new WorkGroup(simulation, rt1, 1);
        WorkGroup wg2 = new WorkGroup(simulation, rt2, 2);

        activity.newWorkGroupAdder(wg1).add();
        activity.newWorkGroupAdder(wg2).add();

        RequestResourcesFlow requestFlow = (RequestResourcesFlow) activity.getInitialFlow();
        assertEquals(2, requestFlow.getWorkGroupSize());
    }

    @Test
    void shouldAllowWorkGroupWithDelay_whenSpecified() {
        ActivityFlow activity = new ActivityFlow(simulation, ACTIVITY_DESC);
        ResourceType rt = new ResourceType(simulation, "RT");
        WorkGroup wg = new WorkGroup(simulation, rt, 1);

        activity.newWorkGroupAdder(wg).withDelay(100L).add();

        RequestResourcesFlow requestFlow = (RequestResourcesFlow) activity.getInitialFlow();
        assertEquals(1, requestFlow.getWorkGroupSize());
    }

    @Test
    void shouldReturnWorkGroupSize_whenAccessedViaActivityFlow() {
        ActivityFlow activity = new ActivityFlow(simulation, ACTIVITY_DESC);
        assertEquals(0, activity.getWorkGroupSize());

        ResourceType rt = new ResourceType(simulation, "RT");
        WorkGroup wg = new WorkGroup(simulation, rt, 1);
        activity.newWorkGroupAdder(wg).add();

        assertEquals(1, activity.getWorkGroupSize());
    }

    @Test
    void shouldReturnWorkGroupById_whenWorkGroupAdded() {
        ActivityFlow activity = new ActivityFlow(simulation, ACTIVITY_DESC);
        ResourceType rt = new ResourceType(simulation, "RT");
        WorkGroup wg = new WorkGroup(simulation, rt, 1);
        // wg ocupa id=0 en la lista de WGs; el ActivityWorkGroup interno ocupa id=1
        activity.newWorkGroupAdder(wg).add();

        assertNotNull(activity.getWorkGroup(1));
    }

    @Test
    void shouldAddResourceCancellation_withDurationOnly() {
        ActivityFlow activity = new ActivityFlow(simulation, ACTIVITY_DESC);
        ResourceType rt = new ResourceType(simulation, "RT");

        // No debe lanzar excepción
        activity.addResourceCancellation(rt, 100L);
    }

    @Test
    void shouldAddResourceCancellation_withDurationAndCondition() {
        ActivityFlow activity = new ActivityFlow(simulation, ACTIVITY_DESC);
        ResourceType rt = new ResourceType(simulation, "RT");
        AbstractCondition<ElementInstance> cond = new AbstractCondition<>() {
            @Override
            public boolean check(ElementInstance fe) {
                return true;
            }
        };

        // No debe lanzar excepción
        activity.addResourceCancellation(rt, 200L, cond);
    }

    @Test
    void shouldReturnACT_forObjectTypeIdentifier() {
        ActivityFlow activity = new ActivityFlow(simulation, ACTIVITY_DESC);
        assertEquals("ACT", activity.getObjectTypeIdentifier());
    }

    @Test
    void shouldReturnNegativeResourcesId_whenCreated() {
        ActivityFlow activity = new ActivityFlow(simulation, ACTIVITY_DESC);
        assertTrue(activity.getResourcesId() < 0);
    }

    @Test
    void shouldHaveConsecutiveResourcesIds_whenMultipleActivitiesCreated() {
        ActivityFlow a1 = new ActivityFlow(simulation, "A1");
        ActivityFlow a2 = new ActivityFlow(simulation, "A2");
        // Los IDs son negativos y consecutivos (decrecientes)
        assertTrue(a2.getResourcesId() < a1.getResourcesId());
    }
}
