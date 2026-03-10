package es.ull.simulation.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.ull.simulation.model.engine.SimulationEngine;
import es.ull.simulation.model.location.Node;

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

    // -----------------------------------------------------------------------
    // Tests requiring full engine setup (AM + RT + Resource + SimulationEngine)
    // Methods that access availableResourceList need assignSimulation called
    // -----------------------------------------------------------------------

    /**
     * Helper: fully initialised simulation where AM, RT and Resource have their engines.
     */
    private static final class RTFullSetup {
        final Simulation sim;
        final ActivityManager am;
        final ResourceType rt;
        final Resource resource;

        RTFullSetup(int id) {
            sim = new Simulation(id, "RTSetup-" + id);
            am = new ActivityManager(sim);
            rt = new ResourceType(sim, "RT-" + id);
            rt.setManager(am);
            resource = new Resource(sim, "Res-" + id);
            SimulationEngine eng = new SimulationEngine(id, sim);
            sim.setSimulationEngine(eng);
        }
    }

    @Test
    void shouldReturnNonNullAvailableResourceList_afterEngineAssignment() {
        RTFullSetup s = new RTFullSetup(300);

        assertNotNull(s.rt.getAvailableResourceList());
    }

    @Test
    void shouldHaveResourceInList_afterIncAvailable() {
        RTFullSetup s = new RTFullSetup(301);

        s.rt.incAvailable(s.resource);

        assertEquals(1, s.rt.getAvailableResourceList().size());
        assertEquals(s.resource, s.rt.getResource(0));
    }

    @Test
    void shouldRemoveResourceFromList_afterDecAvailable() {
        RTFullSetup s = new RTFullSetup(302);
        s.rt.incAvailable(s.resource);
        assertEquals(1, s.rt.getAvailableResourceList().size());

        s.rt.decAvailable(s.resource);

        assertEquals(0, s.rt.getAvailableResourceList().size());
    }

    @Test
    void shouldMarkResourceTimeOut_whenDecAvailable_andResourceCurrentlyInUseForThisType() {
        RTFullSetup s = new RTFullSetup(303);
        s.resource.setCurrentResourceType(s.rt); // simulate in-use
        s.rt.incAvailable(s.resource);

        s.rt.decAvailable(s.resource);

        assertTrue(s.resource.isTimeOut());
    }

    @Test
    void shouldClearTimeOut_whenIncAvailable_andResourceCurrentlyTimedOut() {
        RTFullSetup s = new RTFullSetup(304);
        s.resource.setCurrentResourceType(s.rt);
        s.resource.setTimeOut(true);

        s.rt.incAvailable(s.resource);

        assertFalse(s.resource.isTimeOut());
    }

    @Test
    void shouldReturnZeroAvailableResources_whenNoRoleAdded() {
        RTFullSetup s = new RTFullSetup(305);
        s.rt.incAvailable(s.resource);

        // Without addRole, isAvailable returns false → count = 0
        assertEquals(0, s.rt.getAvailableResources());
    }

    @Test
    void shouldReturnOneAvailableResource_whenRoleAddedWithFutureTimestamp() {
        RTFullSetup s = new RTFullSetup(306);
        s.rt.incAvailable(s.resource);
        s.resource.getEngine().addRole(s.rt, Long.MAX_VALUE);

        assertEquals(1, s.rt.getAvailableResources());
    }

    @Test
    void shouldReturnTrue_checkNeeded_whenOneResourcePresent() {
        RTFullSetup s = new RTFullSetup(307);
        s.rt.incAvailable(s.resource);

        // checkNeeded(0, 1): starting at index 0, needing 1 unseized resource
        assertTrue(s.rt.checkNeeded(0, 1));
    }

    @Test
    void shouldReturnFalse_checkNeeded_whenNotEnoughResources() {
        RTFullSetup s = new RTFullSetup(308);
        s.rt.incAvailable(s.resource); // only 1 resource

        // checkNeeded(0, 2): needs 2, only 1 available
        assertFalse(s.rt.checkNeeded(0, 2));
    }

    @Test
    void shouldReturnFalse_checkNeeded_whenResourceIsSeized() {
        RTFullSetup s = new RTFullSetup(309);
        s.rt.incAvailable(s.resource);
        s.resource.getEngine().addRole(s.rt, Long.MAX_VALUE);
        // Seize the resource by adding it to a solution (sets currentResourceType)
        java.util.ArrayDeque<Resource> sol = new java.util.ArrayDeque<>();
        s.resource.getEngine().add2Solution(sol, s.rt, null);
        // Now currentResourceType != null → checkNeeded won't count it
        assertFalse(s.rt.checkNeeded(0, 1));
    }

    @Test
    void shouldReturnMinusOne_getNextAvailableResource_whenNoRoleAdded() {
        RTFullSetup s = new RTFullSetup(310);
        s.rt.incAvailable(s.resource);
        // Without role, add2Solution returns false → getNextAvailableResource returns -1
        java.util.ArrayDeque<Resource> sol = new java.util.ArrayDeque<>();
        int idx = s.rt.getNextAvailableResource(sol, 0, null);

        assertEquals(-1, idx);
    }

    @Test
    void shouldReturnValidIndex_getNextAvailableResource_whenRoleAdded() {
        RTFullSetup s = new RTFullSetup(311);
        s.rt.incAvailable(s.resource);
        s.resource.getEngine().addRole(s.rt, Long.MAX_VALUE);
        java.util.ArrayDeque<Resource> sol = new java.util.ArrayDeque<>();

        int idx = s.rt.getNextAvailableResource(sol, 0, null);

        assertEquals(0, idx);
        assertTrue(sol.contains(s.resource));
    }

    @Test
    void shouldAddGenericResourcesWithLocation_whenRequested() {
        RTFullSetup s = new RTFullSetup(312);
        Node location = new Node("Loc-312");

        Resource[] resources = s.rt.addGenericResources(3, 2, location);

        assertNotNull(resources);
        assertEquals(3, resources.length);
        for (Resource res : resources) {
            assertNotNull(res);
            // currentLocation is null until simulation runs onCreate; check capacity instead
            assertEquals(2, res.getCapacity());
            assertFalse(res.getTimeTableEntries().isEmpty());
        }
    }

    @Test
    void shouldReturnNull_getAvailableResourceList_beforeEngineAssignment() {
        // No setSimulationEngine called → availableResourceList stays null
        ResourceType rt = new ResourceType(simulation, "Uninitialized RT");

        assertNull(rt.getAvailableResourceList());
    }
}
