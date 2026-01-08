package es.ull.simulation.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
}
