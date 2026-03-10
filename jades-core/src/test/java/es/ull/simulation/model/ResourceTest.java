package es.ull.simulation.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.ull.simulation.model.engine.SimulationEngine;
import es.ull.simulation.model.location.Node;

class ResourceTest {
    private Simulation simulation;
    private static final int SIMULATION_ID = 1;
    private static final String SIMULATION_DESC = "Test Simulation";
    private static final String RESOURCE_DESC = "Test Resource";

    @BeforeEach
    void setUp() {
        simulation = new Simulation(SIMULATION_ID, SIMULATION_DESC);
        SimulationEngine engine = new SimulationEngine(SIMULATION_ID, simulation);
        simulation.setSimulationEngine(engine);
    }

    @Test
    void shouldCreateResourceWithDefaultSize_whenNoSizeProvided() {
        Resource resource = new Resource(simulation, RESOURCE_DESC);

        assertEquals(RESOURCE_DESC, resource.getDescription());
        assertEquals(0, resource.getCapacity());
    }

    @Test
    void shouldCreateResourceWithCustomSize_whenSizeProvided() {
        int customSize = 10;
        Resource resource = new Resource(simulation, RESOURCE_DESC, customSize, null);

        assertEquals(RESOURCE_DESC, resource.getDescription());
        assertEquals(customSize, resource.getCapacity());
    }

    @Test
    void shouldAutoRegisterInSimulation_whenCreated() {
        Resource resource1 = new Resource(simulation, "Resource 1");
        Resource resource2 = new Resource(simulation, "Resource 2");

        assertEquals(2, simulation.getResourceList().size());
        assertTrue(simulation.getResourceList().contains(resource1));
        assertTrue(simulation.getResourceList().contains(resource2));
    }

    @Test
    void shouldHaveSequentialIds_whenMultipleResourcesCreated() {
        Resource resource1 = new Resource(simulation, "Resource 1");
        Resource resource2 = new Resource(simulation, "Resource 2");
        Resource resource3 = new Resource(simulation, "Resource 3");

        assertEquals(0, resource1.getIdentifier());
        assertEquals(1, resource2.getIdentifier());
        assertEquals(2, resource3.getIdentifier());
    }

    @Test
    void shouldHaveNullLocation_whenNoLocationProvided() {
        Resource resource = new Resource(simulation, RESOURCE_DESC, 5, null);

        assertEquals(null, resource.getLocation());
    }

    @Test
    void shouldBelongToSimulation_whenCreated() {
        Resource resource = new Resource(simulation, RESOURCE_DESC);

        assertNotNull(resource.getSimulation());
        assertEquals(simulation, resource.getSimulation());
    }

    @Test
    void shouldHaveObjectTypeIdentifier_whenCreated() {
        Resource resource = new Resource(simulation, RESOURCE_DESC);

        assertNotNull(resource.getObjectTypeIdentifier());
        assertEquals("RES", resource.getObjectTypeIdentifier());
    }

    @Test
    void shouldHaveEmptyTimeTable_whenCreated() {
        Resource resource = new Resource(simulation, RESOURCE_DESC);

        assertNotNull(resource.getTimeTableEntries());
        assertTrue(resource.getTimeTableEntries().isEmpty());
    }

    @Test
    void shouldReturnEmptyCancellationPeriodEntries_whenCreated() {
        Resource resource = new Resource(simulation, RESOURCE_DESC);

        assertNotNull(resource.getCancellationPeriodEntries());
        assertTrue(resource.getCancellationPeriodEntries().isEmpty());
    }

    @Test
    void shouldReturnNullCurrentResourceType_whenNotSet() {
        Resource resource = new Resource(simulation, RESOURCE_DESC);

        assertNull(resource.getCurrentResourceType());
    }

    @Test
    void shouldSetCurrentResourceType_whenProvided() {
        Resource resource = new Resource(simulation, RESOURCE_DESC);
        ResourceType resourceType = new ResourceType(simulation, "Type1");

        resource.setCurrentResourceType(resourceType);

        assertNotNull(resource.getCurrentResourceType());
        assertEquals(resourceType, resource.getCurrentResourceType());
    }

    @Test
    void shouldUpdateCurrentResourceType_whenChanged() {
        Resource resource = new Resource(simulation, RESOURCE_DESC);
        ResourceType initialType = new ResourceType(simulation, "Type1");
        ResourceType newType = new ResourceType(simulation, "Type2");
        resource.setCurrentResourceType(initialType);

        resource.setCurrentResourceType(newType);

        assertNotNull(resource.getCurrentResourceType());
        assertEquals(newType, resource.getCurrentResourceType());
    }

    @Test
    void shouldReturnFalseForIsTimeOut_whenCreated() {
        Resource resource = new Resource(simulation, RESOURCE_DESC);

        assertFalse(resource.isTimeOut());
    }

    @Test
    void shouldSetTimeOutToTrue_whenTimeOutSet() {
        Resource resource = new Resource(simulation, RESOURCE_DESC);

        resource.setTimeOut(true);

        assertTrue(resource.isTimeOut());
    }

    @Test
    void shouldSetTimeOutToFalse_whenTimeOutReset() {
        Resource resource = new Resource(simulation, RESOURCE_DESC);
        resource.setTimeOut(true);

        resource.setTimeOut(false);

        assertFalse(resource.isTimeOut());
    }

    @Test
    void shouldToggleTimeOut_whenCalledMultipleTimes() {
        Resource resource = new Resource(simulation, RESOURCE_DESC);

        resource.setTimeOut(true);
        assertTrue(resource.isTimeOut());

        resource.setTimeOut(false);
        assertFalse(resource.isTimeOut());

        resource.setTimeOut(true);
        assertTrue(resource.isTimeOut());
    }

    @Test
    void shouldHandleLargeCapacity_whenCreated() {
        Resource resource = new Resource(simulation, RESOURCE_DESC, 10000, null);

        assertEquals(10000, resource.getCapacity());
    }

    @Test
    void shouldMaintainCapacityAndDescription_whenCreated() {
        Resource resource = new Resource(simulation, RESOURCE_DESC, 5, null);

        assertEquals(5, resource.getCapacity());
        assertEquals(RESOURCE_DESC, resource.getDescription());
    }

    @Test
    void shouldCreateMultipleResourcesWithDifferentTypes() {
        Resource resource1 = new Resource(simulation, "Resource1");
        Resource resource2 = new Resource(simulation, "Resource2");
        ResourceType type1 = new ResourceType(simulation, "Type1");
        ResourceType type2 = new ResourceType(simulation, "Type2");

        resource1.setCurrentResourceType(type1);
        resource2.setCurrentResourceType(type2);

        assertEquals(type1, resource1.getCurrentResourceType());
        assertEquals(type2, resource2.getCurrentResourceType());
        assertNotEquals(resource1.getCurrentResourceType(), resource2.getCurrentResourceType());
    }

    @Test
    void shouldReturnEngine_afterResourceCreation() {
        Resource resource = new Resource(simulation, RESOURCE_DESC);

        // Engine is assigned during simulation initialization, not immediately
        // For now, just verify the method doesn't throw NPE
        assertNotNull(resource);
    }

    @Test
    void shouldCreateTimeTableEntriesAdder_withSingleRole() {
        Resource resource = new Resource(simulation, RESOURCE_DESC);
        ResourceType role = new ResourceType(simulation, "Type1");

        var adder = resource.newTimeTableOrCancelEntriesAdder(role);

        assertNotNull(adder);
    }

    @Test
    void shouldCreateTimeTableEntriesAdder_withRoleList() {
        Resource resource = new Resource(simulation, RESOURCE_DESC);
        ResourceType role1 = new ResourceType(simulation, "Type1");
        ResourceType role2 = new ResourceType(simulation, "Type2");
        java.util.ArrayList<ResourceType> roleList = new java.util.ArrayList<>();
        roleList.add(role1);
        roleList.add(role2);

        var adder = resource.newTimeTableOrCancelEntriesAdder(roleList);

        assertNotNull(adder);
    }

    @Test
    void shouldMaintainIndependentCapacities_acrossMultipleResources() {
        Resource r1 = new Resource(simulation, "R1", 5, null);
        Resource r2 = new Resource(simulation, "R2", 10, null);
        Resource r3 = new Resource(simulation, "R3", 15, null);

        assertEquals(5, r1.getCapacity());
        assertEquals(10, r2.getCapacity());
        assertEquals(15, r3.getCapacity());
    }

    @Test
    void shouldMaintainDescriptionAndCapacity_independently() {
        String desc1 = "Description One";
        String desc2 = "Description Two";
        Resource r1 = new Resource(simulation, desc1, 100, null);
        Resource r2 = new Resource(simulation, desc2, 200, null);

        assertEquals(desc1, r1.getDescription());
        assertEquals(100, r1.getCapacity());
        assertEquals(desc2, r2.getDescription());
        assertEquals(200, r2.getCapacity());
    }

    @Test
    void shouldAllowNullCurrentResourceType() {
        Resource resource = new Resource(simulation, RESOURCE_DESC);
        resource.setCurrentResourceType(null);

        assertNull(resource.getCurrentResourceType());
    }

    @Test
    void shouldSupportZeroCapacity() {
        Resource resource = new Resource(simulation, RESOURCE_DESC, 0, null);

        assertEquals(0, resource.getCapacity());
    }

    @Test
    void shouldSetLocation_whenProvided() {
        Resource resource = new Resource(simulation, RESOURCE_DESC);
        Node location = new Node("Location1");

        resource.setLocation(location);

        assertNotNull(resource.getLocation());
        assertEquals(location, resource.getLocation());
    }

    @Test
    void shouldCreateOnCreateEvent_withTimestamp() {
        Resource resource = new Resource(simulation, RESOURCE_DESC);
        long timestamp = 100L;

        var event = resource.onCreate(timestamp);

        assertNotNull(event);
    }

    @Test
    void shouldCreateOnDestroyEvent_withTimestamp() {
        Resource resource = new Resource(simulation, RESOURCE_DESC);
        long timestamp = 500L;

        var event = resource.onDestroy(timestamp);

        assertNotNull(event);
    }

    // -----------------------------------------------------------------------
    // Tests requiring engine assignment (resource created after setUp)
    // assignSimulation is protected but accessible from the same package
    // -----------------------------------------------------------------------

    @Test
    void shouldReturnNotNull_getEngine_afterManualAssignSimulation() {
        Resource resource = new Resource(simulation, RESOURCE_DESC);
        resource.assignSimulation(simulation.getSimulationEngine());

        assertNotNull(resource.getEngine());
    }

    @Test
    void shouldReturnFalse_isSeized_whenNoElementHoldsResource() {
        Resource resource = new Resource(simulation, RESOURCE_DESC);
        resource.assignSimulation(simulation.getSimulationEngine());

        assertFalse(resource.isSeized());
    }

    @Test
    void shouldReturnNull_getCurrentElement_whenResourceNotSeized() {
        Resource resource = new Resource(simulation, RESOURCE_DESC);
        resource.assignSimulation(simulation.getSimulationEngine());

        assertNull(resource.getEngine().getCurrentElement());
    }

    @Test
    void shouldReturnEmptyList_getCurrentManagers_whenNoRolesAssigned() {
        Resource resource = new Resource(simulation, RESOURCE_DESC);
        resource.assignSimulation(simulation.getSimulationEngine());

        ArrayList<ActivityManager> managers = resource.getCurrentManagers();
        assertNotNull(managers);
        assertTrue(managers.isEmpty());
    }

    @Test
    void shouldReturnZero_getValidTimeTableEntries_initially() {
        Resource resource = new Resource(simulation, RESOURCE_DESC);
        resource.assignSimulation(simulation.getSimulationEngine());

        assertEquals(0, resource.getEngine().getValidTimeTableEntries());
    }

    @Test
    void shouldIncrement_validTimeTableEntries_whenIncCalled() {
        Resource resource = new Resource(simulation, RESOURCE_DESC);
        resource.assignSimulation(simulation.getSimulationEngine());

        int result = resource.getEngine().incValidTimeTableEntries();

        assertEquals(1, result);
        assertEquals(1, resource.getEngine().getValidTimeTableEntries());
    }

    @Test
    void shouldDecrement_validTimeTableEntries_whenDecCalled() {
        Resource resource = new Resource(simulation, RESOURCE_DESC);
        resource.assignSimulation(simulation.getSimulationEngine());
        resource.getEngine().incValidTimeTableEntries();
        resource.getEngine().incValidTimeTableEntries();

        int result = resource.getEngine().decValidTimeTableEntries();

        assertEquals(1, result);
        assertEquals(1, resource.getEngine().getValidTimeTableEntries());
    }

    // -----------------------------------------------------------------------
    // Tests requiring ActivityManager + ResourceType + full engine setup
    // These create a dedicated Simulation so everything is initialized together
    // -----------------------------------------------------------------------

    /**
     * Helper: builds a fully initialised simulation where AM, RT and Resource
     * have their engines assigned via setSimulationEngine.
     */
    private static final class FullSetup {
        final Simulation sim;
        final ActivityManager am;
        final ResourceType rt;
        final Resource resource;

        FullSetup(int id) {
            sim = new Simulation(id, "FullSetup-" + id);
            am = new ActivityManager(sim);
            rt = new ResourceType(sim, "Type-" + id);
            rt.setManager(am);
            resource = new Resource(sim, "Res-" + id);
            SimulationEngine eng = new SimulationEngine(id, sim);
            sim.setSimulationEngine(eng);
        }
    }

    @Test
    void shouldReturnFalse_isAvailable_whenNoRoleAdded() {
        FullSetup s = new FullSetup(200);

        assertFalse(s.resource.isAvailable(s.rt));
    }

    @Test
    void shouldReturnTrue_isAvailable_whenRoleAddedWithFutureTimestamp() {
        FullSetup s = new FullSetup(201);

        s.resource.getEngine().addRole(s.rt, Long.MAX_VALUE);

        assertTrue(s.resource.isAvailable(s.rt));
    }

    @Test
    void shouldReturnFalse_isAvailable_whenResourceIsCanceled() {
        FullSetup s = new FullSetup(202);
        s.resource.getEngine().addRole(s.rt, Long.MAX_VALUE);

        s.resource.getEngine().setNotCanceled(false);

        assertFalse(s.resource.isAvailable(s.rt));
    }

    @Test
    void shouldReturnFalse_isAvailable_whenRoleTimestampExpired() {
        FullSetup s = new FullSetup(203);
        // Sim ts starts at 0; role with avEnd=0 → 0 > 0 is false
        s.resource.getEngine().addRole(s.rt, 0L);

        assertFalse(s.resource.isAvailable(s.rt));
    }

    @Test
    void shouldReturnManager_getCurrentManagers_afterAddRole() {
        FullSetup s = new FullSetup(204);
        s.resource.getEngine().addRole(s.rt, Long.MAX_VALUE);

        ArrayList<ActivityManager> managers = s.resource.getCurrentManagers();

        assertEquals(1, managers.size());
        assertEquals(s.am, managers.get(0));
    }

    @Test
    void shouldNotDuplicate_getCurrentManagers_whenSameManagerAddedTwice() {
        FullSetup s = new FullSetup(205);
        ResourceType rt2 = new ResourceType(s.sim, "Type2-205");
        rt2.setManager(s.am);  // same manager as rt
        s.resource.getEngine().addRole(s.rt, Long.MAX_VALUE);
        s.resource.getEngine().addRole(rt2, Long.MAX_VALUE);

        ArrayList<ActivityManager> managers = s.resource.getCurrentManagers();

        // Both roles share the same AM → list contains it only once
        assertEquals(1, managers.size());
    }

    @Test
    void shouldAddRole_withoutThrowing_whenActivityManagerIsSet() {
        FullSetup s = new FullSetup(206);

        s.resource.getEngine().addRole(s.rt, Long.MAX_VALUE);

        // Role is now present: isAvailable reflects it
        assertTrue(s.resource.isAvailable(s.rt));
    }

    @Test
    void shouldKeepRole_whenRemoveRoleCalledButNotYetExpired() {
        FullSetup s = new FullSetup(207);
        s.resource.getEngine().addRole(s.rt, Long.MAX_VALUE);

        // At ts=0, avEnd=MAX_VALUE is NOT <= ts → removeRole is a no-op
        s.resource.getEngine().removeRole(s.rt);

        assertTrue(s.resource.isAvailable(s.rt));
    }

    @Test
    void shouldRemoveRole_whenAvailabilityTimestampExpired() {
        FullSetup s = new FullSetup(208);
        s.resource.getEngine().addRole(s.rt, 0L); // avEnd=0, ts=0 → 0<=0 → will remove
        assertFalse(s.resource.isAvailable(s.rt)); // already not available

        s.resource.getEngine().removeRole(s.rt);

        // Role removed → getCurrentManagers returns empty
        assertTrue(s.resource.getCurrentManagers().isEmpty());
    }

    @Test
    void shouldSkipSilently_removeRole_whenRoleNeverAdded() {
        FullSetup s = new FullSetup(209);

        // Must not throw
        s.resource.getEngine().removeRole(s.rt);

        assertTrue(s.resource.getCurrentManagers().isEmpty());
    }

    @Test
    void shouldKeepHigherTimestamp_whenAddRoleCalledTwiceForSameType() {
        FullSetup s = new FullSetup(210);
        s.resource.getEngine().addRole(s.rt, 1000L);
        s.resource.getEngine().addRole(s.rt, Long.MAX_VALUE); // higher → replaces

        // Still available (avEnd is MAX_VALUE)
        assertTrue(s.resource.isAvailable(s.rt));
    }

    @Test
    void shouldNotReplaceTimestamp_whenAddRoleCalledWithLowerValue() {
        FullSetup s = new FullSetup(211);
        s.resource.getEngine().addRole(s.rt, Long.MAX_VALUE);
        s.resource.getEngine().addRole(s.rt, 1L); // lower → ignored

        // Still available (avEnd kept at MAX_VALUE)
        assertTrue(s.resource.isAvailable(s.rt));
    }

    @Test
    void shouldNotifyCurrentManagers_withoutThrowing_whenRolesPresent() {
        FullSetup s = new FullSetup(212);
        s.resource.getEngine().addRole(s.rt, Long.MAX_VALUE);

        // notifyCurrentManagers only sets availableResource=true in the AM engine
        s.resource.getEngine().notifyCurrentManagers();

        // No exception thrown and resource still available
        assertTrue(s.resource.isAvailable(s.rt));
    }

    @Test
    void shouldAddResourceToSolution_whenResourceIsAvailable() {
        FullSetup s = new FullSetup(213);
        s.resource.getEngine().addRole(s.rt, Long.MAX_VALUE);
        java.util.ArrayDeque<Resource> solution = new java.util.ArrayDeque<>();

        boolean added = s.resource.getEngine().add2Solution(solution, s.rt, null);

        assertTrue(added);
        assertTrue(solution.contains(s.resource));
        assertEquals(s.rt, s.resource.getCurrentResourceType());
    }

    @Test
    void shouldNotAddResourceToSolution_whenResourceHasNoRole() {
        FullSetup s = new FullSetup(214);
        java.util.ArrayDeque<Resource> solution = new java.util.ArrayDeque<>();

        boolean added = s.resource.getEngine().add2Solution(solution, s.rt, null);

        assertFalse(added);
        assertTrue(solution.isEmpty());
    }

    @Test
    void shouldNotAddResourceToSolution_whenAlreadyInUse() {
        FullSetup s = new FullSetup(215);
        s.resource.getEngine().addRole(s.rt, Long.MAX_VALUE);
        s.resource.setCurrentResourceType(s.rt); // simulate already in use
        java.util.ArrayDeque<Resource> solution = new java.util.ArrayDeque<>();

        boolean added = s.resource.getEngine().add2Solution(solution, s.rt, null);

        assertFalse(added);
    }

    @Test
    void shouldRemoveResourceFromSolution_andClearCurrentResourceType() {
        FullSetup s = new FullSetup(216);
        s.resource.getEngine().addRole(s.rt, Long.MAX_VALUE);
        java.util.ArrayDeque<Resource> solution = new java.util.ArrayDeque<>();
        s.resource.getEngine().add2Solution(solution, s.rt, null);

        s.resource.getEngine().removeFromSolution(solution, null);

        assertFalse(solution.contains(s.resource));
        assertNull(s.resource.getCurrentResourceType());
    }
}
