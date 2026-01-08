package es.ull.simulation.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.ull.simulation.model.flow.RequestResourcesFlow;

class ActivityManagerTest {
    private Simulation simulation;
    private ActivityManager activityManager;
    private static final int SIMULATION_ID = 1;
    private static final String SIMULATION_DESC = "Test Simulation";

    @BeforeEach
    void setUp() {
        simulation = new Simulation(SIMULATION_ID, SIMULATION_DESC);
        activityManager = new ActivityManager(simulation);
    }

    @Test
    void shouldGenerateUniqueId_whenCreated() {
        ActivityManager am1 = new ActivityManager(simulation);
        ActivityManager am2 = new ActivityManager(simulation);

        assertTrue(am1.getIdentifier() != am2.getIdentifier());
    }

    @Test
    void shouldAutoRegisterInSimulation_whenCreated() {
        ActivityManager am = new ActivityManager(simulation);
        
        assertTrue(simulation.getActivityManagerList().contains(am));
    }

    @Test
    void shouldHaveObjectTypeIdentifier_whenCreated() {
        assertEquals("AM", activityManager.getObjectTypeIdentifier());
    }

    @Test
    void shouldBelongToSimulation_whenCreated() {
        assertNotNull(activityManager.getSimulation());
        assertEquals(simulation, activityManager.getSimulation());
    }

    @Test
    void shouldHaveDescriptionWithIdentifier_whenCreated() {
        String description = activityManager.getDescription();
        
        assertNotNull(description);
        assertTrue(description.contains("Activity Manager"));
        assertTrue(description.contains(String.valueOf(activityManager.getIdentifier())));
    }

    @Test
    void shouldAddResourceType_whenRequested() {
        ResourceType rt = new ResourceType(simulation, "Test Resource Type");
        
        activityManager.add(rt);
        
        String description = activityManager.getDescription();
        assertTrue(description.contains(rt.toString()));
    }

    @Test
    void shouldAddMultipleResourceTypes_whenRequested() {
        ResourceType rt1 = new ResourceType(simulation, "RT1");
        ResourceType rt2 = new ResourceType(simulation, "RT2");
        
        activityManager.add(rt1);
        activityManager.add(rt2);
        
        String description = activityManager.getDescription();
        assertTrue(description.contains(rt1.toString()));
        assertTrue(description.contains(rt2.toString()));
    }

    @Test
    void shouldIncludeActivitiesInDescription_whenAdded() {
        ResourceType rt = new ResourceType(simulation, "RT");
        WorkGroup wg = new WorkGroup(simulation, rt, 1);
        RequestResourcesFlow flow = new RequestResourcesFlow(simulation, "TestFlow");
        flow.newWorkGroupAdder(wg).add();
        
        activityManager.add(flow);
        
        String description = activityManager.getDescription();
        assertTrue(description.contains(flow.toString()));
    }

    @Test
    void shouldShowPriorityInDescription_whenActivityAdded() {
        ResourceType rt = new ResourceType(simulation, "RT");
        WorkGroup wg = new WorkGroup(simulation, rt, 1);
        RequestResourcesFlow flow = new RequestResourcesFlow(simulation, "TestFlow", 5);
        flow.newWorkGroupAdder(wg).add();
        
        activityManager.add(flow);
        
        String description = activityManager.getDescription();
        assertTrue(description.contains("[5]"));
    }

    @Test
    void shouldMaintainMultipleActivities_whenAdded() {
        ResourceType rt = new ResourceType(simulation, "RT");
        WorkGroup wg = new WorkGroup(simulation, rt, 1);
        RequestResourcesFlow flow1 = new RequestResourcesFlow(simulation, "Flow1", 1);
        flow1.newWorkGroupAdder(wg).add();
        RequestResourcesFlow flow2 = new RequestResourcesFlow(simulation, "Flow2", 2);
        flow2.newWorkGroupAdder(wg).add();
        
        activityManager.add(flow1);
        activityManager.add(flow2);
        
        String description = activityManager.getDescription();
        assertTrue(description.contains(flow1.toString()));
        assertTrue(description.contains(flow2.toString()));
    }
}
