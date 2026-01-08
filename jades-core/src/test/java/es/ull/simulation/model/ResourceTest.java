package es.ull.simulation.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
}
