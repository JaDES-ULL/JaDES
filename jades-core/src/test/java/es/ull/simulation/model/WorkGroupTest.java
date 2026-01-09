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

    @Test
    void shouldReturnCorrectSize_whenWorkGroupHasMultipleResourceTypes() {
        // Given: WorkGroup with 3 resource types
        ResourceType rt1 = new ResourceType(simulation, "RT1");
        ResourceType rt2 = new ResourceType(simulation, "RT2");
        ResourceType rt3 = new ResourceType(simulation, "RT3");
        WorkGroup wg = new WorkGroup(simulation, new ResourceType[]{rt1, rt2, rt3}, new int[]{1, 2, 3});

        // When: getting size
        int size = wg.size();

        // Then: should return 3
        assertEquals(3, size);
    }

    @Test
    void shouldReturnZeroSize_whenWorkGroupIsEmpty() {
        // Given: empty WorkGroup
        WorkGroup wg = new WorkGroup(simulation);

        // When: getting size
        int size = wg.size();

        // Then: should return 0
        assertEquals(0, size);
    }

    @Test
    void shouldReturnNeededValue_whenCalledWithIndex() {
        // Given: WorkGroup with single resource type needing 5 units
        ResourceType rt = new ResourceType(simulation, "TestRT");
        WorkGroup wg = new WorkGroup(simulation, rt, 5);

        // When: calling getNeeded with index 0
        int needed = wg.getNeeded(0);

        // Then: should return 5
        assertEquals(5, needed);
    }

    @Test
    void shouldReturnResourceTypeAtIndex_whenQueried() {
        // Given: WorkGroup with multiple resource types
        ResourceType rt1 = new ResourceType(simulation, "RT1");
        ResourceType rt2 = new ResourceType(simulation, "RT2");
        ResourceType rt3 = new ResourceType(simulation, "RT3");
        WorkGroup wg = new WorkGroup(simulation, new ResourceType[]{rt1, rt2, rt3}, new int[]{1, 2, 3});

        // When: getting resource types by index
        ResourceType retrievedRt1 = wg.getResourceType(0);
        ResourceType retrievedRt2 = wg.getResourceType(1);
        ResourceType retrievedRt3 = wg.getResourceType(2);

        // Then: should return correct resource types
        assertEquals(rt1, retrievedRt1);
        assertEquals(rt2, retrievedRt2);
        assertEquals(rt3, retrievedRt3);
    }

    @Test
    void shouldReturnNeededArray_whenQueried() {
        // Given: WorkGroup with specific needs
        ResourceType rt1 = new ResourceType(simulation, "RT1");
        ResourceType rt2 = new ResourceType(simulation, "RT2");
        int[] expectedNeeded = {5, 10};
        WorkGroup wg = new WorkGroup(simulation, new ResourceType[]{rt1, rt2}, expectedNeeded);

        // When: getting needed array
        int[] actualNeeded = wg.getNeeded();

        // Then: should return correct needs
        assertEquals(expectedNeeded.length, actualNeeded.length);
        assertEquals(5, actualNeeded[0]);
        assertEquals(10, actualNeeded[1]);
    }

    @Test
    void shouldReturnResourceTypesArray_whenQueried() {
        // Given: WorkGroup with specific resource types
        ResourceType rt1 = new ResourceType(simulation, "RT1");
        ResourceType rt2 = new ResourceType(simulation, "RT2");
        ResourceType[] expectedRts = {rt1, rt2};
        WorkGroup wg = new WorkGroup(simulation, expectedRts, new int[]{1, 2});

        // When: getting resource types array
        ResourceType[] actualRts = wg.getResourceTypes();

        // Then: should return correct resource types
        assertEquals(2, actualRts.length);
        assertEquals(rt1, actualRts[0]);
        assertEquals(rt2, actualRts[1]);
    }

    @Test
    void shouldGenerateCorrectDescription_whenQueried() {
        // Given: WorkGroup with specific configuration
        ResourceType rt1 = new ResourceType(simulation, "Doctors");
        ResourceType rt2 = new ResourceType(simulation, "Nurses");
        WorkGroup wg = new WorkGroup(simulation, new ResourceType[]{rt1, rt2}, new int[]{2, 5});

        // When: getting description
        String description = wg.getDescription();

        // Then: should contain workgroup ID and resource type info
        assertNotNull(description);
        assertTrue(description.startsWith("WG0"));
        assertTrue(description.contains("RT"));
    }

    @Test
    void shouldGenerateEmptyDescription_whenWorkGroupIsEmpty() {
        // Given: empty WorkGroup
        WorkGroup wg = new WorkGroup(simulation);

        // When: getting description
        String description = wg.getDescription();

        // Then: should just contain WG ID
        assertNotNull(description);
        assertTrue(description.startsWith("WG0"));
    }

    @Test
    void shouldHandleSingleResourceTypeSingleNeed_whenCreated() {
        // Given: single resource type with need of 1
        ResourceType rt = new ResourceType(simulation, "Single RT");
        WorkGroup wg = new WorkGroup(simulation, rt, 1);

        // When: querying size and elements
        int size = wg.size();
        ResourceType retrievedRt = wg.getResourceType(0);
        int[] needed = wg.getNeeded();

        // Then: should have size 1 with correct values
        assertEquals(1, size);
        assertEquals(rt, retrievedRt);
        assertEquals(1, needed[0]);
    }
}
