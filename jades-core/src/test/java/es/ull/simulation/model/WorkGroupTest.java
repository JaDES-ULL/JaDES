package es.ull.simulation.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WorkGroupTest {
    private Simulation simulation;
    private static final int SIMULATION_ID = 1;
    private static final String SIMULATION_DESC = "Test Simulation";

    @BeforeEach
    void setUp() {
        simulation = new Simulation(SIMULATION_ID, SIMULATION_DESC);
    }

    @Test
    void shouldCreateEmptyWorkGroup_whenNoParametersProvided() {
        WorkGroup wg = new WorkGroup(simulation);

        assertNotNull(wg);
    }

    @Test
    void shouldCreateWorkGroupWithSingleResourceType_whenProvided() {
        ResourceType rt = new ResourceType(simulation, "Test RT");
        WorkGroup wg = new WorkGroup(simulation, rt, 2);

        assertNotNull(wg);
    }

    @Test
    void shouldCreateWorkGroupWithMultipleResourceTypes_whenProvided() {
        ResourceType rt1 = new ResourceType(simulation, "RT1");
        ResourceType rt2 = new ResourceType(simulation, "RT2");
        ResourceType[] rts = {rt1, rt2};
        int[] needed = {1, 2};

        WorkGroup wg = new WorkGroup(simulation, rts, needed);

        assertNotNull(wg);
    }

    @Test
    void shouldAutoRegisterInSimulation_whenCreated() {
        WorkGroup wg1 = new WorkGroup(simulation);
        WorkGroup wg2 = new WorkGroup(simulation);

        assertEquals(2, simulation.getWorkGroupList().size());
        assertTrue(simulation.getWorkGroupList().contains(wg1));
        assertTrue(simulation.getWorkGroupList().contains(wg2));
    }

    @Test
    void shouldHaveSequentialIds_whenMultipleCreated() {
        WorkGroup wg1 = new WorkGroup(simulation);
        WorkGroup wg2 = new WorkGroup(simulation);
        WorkGroup wg3 = new WorkGroup(simulation);

        assertEquals(0, wg1.getIdentifier());
        assertEquals(1, wg2.getIdentifier());
        assertEquals(2, wg3.getIdentifier());
    }

    @Test
    void shouldHaveObjectTypeIdentifier_whenCreated() {
        WorkGroup wg = new WorkGroup(simulation);

        assertEquals("WG", wg.getObjectTypeIdentifier());
    }

    @Test
    void shouldBelongToSimulation_whenCreated() {
        WorkGroup wg = new WorkGroup(simulation);

        assertNotNull(wg.getSimulation());
        assertEquals(simulation, wg.getSimulation());
    }

    @Test
    void shouldHandleMultipleResourceTypesWithDifferentNeeds_whenCreated() {
        ResourceType rt1 = new ResourceType(simulation, "RT1");
        ResourceType rt2 = new ResourceType(simulation, "RT2");
        ResourceType rt3 = new ResourceType(simulation, "RT3");
        ResourceType[] rts = {rt1, rt2, rt3};
        int[] needed = {1, 5, 3};

        WorkGroup wg = new WorkGroup(simulation, rts, needed);

        assertNotNull(wg);
        assertEquals(simulation, wg.getSimulation());
    }
}
