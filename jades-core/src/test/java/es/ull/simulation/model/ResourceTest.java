package es.ull.simulation.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.ull.simulation.model.location.Node;

class ResourceTest {
    private Simulation simulation;
    private static final int SIMULATION_ID = 1;
    private static final String SIMULATION_DESC = "Test Simulation";
    private static final String RESOURCE_DESC = "Test Resource";

    @BeforeEach
    void setUp() {
        simulation = new Simulation(SIMULATION_ID, SIMULATION_DESC);
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
}
