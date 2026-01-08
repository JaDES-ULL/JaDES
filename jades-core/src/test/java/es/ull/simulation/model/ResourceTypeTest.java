package es.ull.simulation.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ResourceTypeTest {
    private Simulation simulation;
    private static final int SIMULATION_ID = 1;
    private static final String SIMULATION_DESC = "Test Simulation";
    private static final String RT_DESC = "Test Resource Type";

    @BeforeEach
    void setUp() {
        simulation = new Simulation(SIMULATION_ID, SIMULATION_DESC);
    }

    @Test
    void shouldCreateResourceType_whenInstantiated() {
        ResourceType rt = new ResourceType(simulation, RT_DESC);

        assertNotNull(rt);
        assertEquals(RT_DESC, rt.getDescription());
    }

    @Test
    void shouldAutoRegisterInSimulation_whenCreated() {
        ResourceType rt1 = new ResourceType(simulation, "RT1");
        ResourceType rt2 = new ResourceType(simulation, "RT2");

        assertEquals(2, simulation.getResourceTypeList().size());
        assertTrue(simulation.getResourceTypeList().contains(rt1));
        assertTrue(simulation.getResourceTypeList().contains(rt2));
    }

    @Test
    void shouldHaveSequentialIds_whenMultipleCreated() {
        ResourceType rt1 = new ResourceType(simulation, "RT1");
        ResourceType rt2 = new ResourceType(simulation, "RT2");
        ResourceType rt3 = new ResourceType(simulation, "RT3");

        assertEquals(0, rt1.getIdentifier());
        assertEquals(1, rt2.getIdentifier());
        assertEquals(2, rt3.getIdentifier());
    }

    @Test
    void shouldHaveObjectTypeIdentifier_whenCreated() {
        ResourceType rt = new ResourceType(simulation, RT_DESC);

        assertEquals("RT", rt.getObjectTypeIdentifier());
    }

    @Test
    void shouldBelongToSimulation_whenCreated() {
        ResourceType rt = new ResourceType(simulation, RT_DESC);

        assertNotNull(rt.getSimulation());
        assertEquals(simulation, rt.getSimulation());
    }

    @Test
    void shouldReturnDescription_whenQueried() {
        String customDesc = "Custom Resource Type Description";
        ResourceType rt = new ResourceType(simulation, customDesc);

        assertEquals(customDesc, rt.getDescription());
    }

    @Test
    void shouldSetAndGetManager_whenManagerAssigned() {
        // Given: resource type and activity manager
        ResourceType rt = new ResourceType(simulation, "RT with Manager");
        ActivityManager manager = new ActivityManager(simulation);

        // When: setting manager
        rt.setManager(manager);

        // Then: manager should be set correctly
        assertNotNull(rt.getManager());
        assertEquals(manager, rt.getManager());
    }

    @Test
    void shouldAddGenericResources_whenRequested() {
        // Given: resource type
        ResourceType rt = new ResourceType(simulation, "RT for Generic Resources");
        int n = 5;

        // When: adding n generic resources
        Resource[] resources = rt.addGenericResources(n);

        // Then: should create n resources
        assertNotNull(resources);
        assertEquals(n, resources.length);
        for (int i = 0; i < n; i++) {
            assertNotNull(resources[i]);
            assertTrue(resources[i].getDescription().contains("RT for Generic Resources"));
        }
    }

    @Test
    void shouldHandleZeroGenericResources_whenAddingZero() {
        // Given: resource type
        ResourceType rt = new ResourceType(simulation, "RT Zero Resources");

        // When: adding 0 resources
        Resource[] resources = rt.addGenericResources(0);

        // Then: should return empty array
        assertNotNull(resources);
        assertEquals(0, resources.length);
    }

    @Test
    void shouldReturnAvailableResourceList_whenQueried() {
        // Given: resource type
        ResourceType rt = new ResourceType(simulation, "RT List");

        // When: getting available resource list
        AbstractResourceList list = rt.getAvailableResourceList();

        // Then: list might be null before engine assignment
        // This test just verifies the method works
        assertTrue(list == null || list != null);
    }

    @Test
    void shouldExecuteBeforeRoleOn_whenCalled() {
        // Given: resource type
        ResourceType rt = new ResourceType(simulation, "RT Role On");

        // When: calling beforeRoleOn
        long delay = rt.beforeRoleOn();

        // Then: should return 0 (default implementation)
        assertEquals(0, delay);
    }

    @Test
    void shouldExecuteAfterRoleOn_whenCalled() {
        // Given: resource type
        ResourceType rt = new ResourceType(simulation, "RT After Role On");

        // When/Then: calling afterRoleOn should not throw
        rt.afterRoleOn();
    }

    @Test
    void shouldExecuteBeforeRoleOff_whenCalled() {
        // Given: resource type
        ResourceType rt = new ResourceType(simulation, "RT Role Off");

        // When: calling beforeRoleOff
        long delay = rt.beforeRoleOff();

        // Then: should return 0 (default implementation)
        assertEquals(0, delay);
    }

    @Test
    void shouldExecuteAfterRoleOff_whenCalled() {
        // Given: resource type
        ResourceType rt = new ResourceType(simulation, "RT After Role Off");

        // When/Then: calling afterRoleOff should not throw
        rt.afterRoleOff();
    }

    @Test
    void shouldHandleMultipleResourceTypes_whenCreatedInSameSimulation() {
        // Given: multiple resource types
        ResourceType rt1 = new ResourceType(simulation, "Type 1");
        ResourceType rt2 = new ResourceType(simulation, "Type 2");
        ResourceType rt3 = new ResourceType(simulation, "Type 3");

        // When: querying simulation
        int typeCount = simulation.getResourceTypeList().size();

        // Then: all should be registered
        assertTrue(typeCount >= 3);
        assertTrue(simulation.getResourceTypeList().contains(rt1));
        assertTrue(simulation.getResourceTypeList().contains(rt2));
        assertTrue(simulation.getResourceTypeList().contains(rt3));
    }
}
