package es.ull.simulation.model;

import es.ull.simulation.model.location.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the ResourceLocation class - extracted from Resource refactoring
 */
class ResourceLocationTest {
    private Simulation simulation;
    private Resource resource;
    private static final int SIMULATION_ID = 1;
    private static final String SIMULATION_DESC = "Test Simulation";

    @BeforeEach
    void setUp() {
        simulation = new Simulation(SIMULATION_ID, SIMULATION_DESC);
    }

    @Test
    void shouldCreateResourceLocation_withNullInitLocation() {
        // Given: resource without init location
        resource = new Resource(simulation, "Test Resource", 0, null);

        // When: getting location
        Location location = resource.getLocation();

        // Then: location should be null
        assertNull(location);
    }

    @Test
    void shouldReturnCapacity_whenResourceHasSize() {
        // Given: resource with size 5
        resource = new Resource(simulation, "Test Resource", 5, null);

        // When: getting capacity
        int capacity = resource.getCapacity();

        // Then: should return 5
        assertEquals(5, capacity);
    }

    @Test
    void shouldMaintainSize_throughoutLifecycle() {
        // Given: resource with specific size
        final int expectedSize = 7;
        resource = new Resource(simulation, "Test Resource", expectedSize, null);

        // When: getting capacity
        int capacity = resource.getCapacity();

        // Then: capacity should remain constant
        assertEquals(expectedSize, capacity);
    }

    @Test
    void shouldReturnZeroCapacity_whenResourceHasNoSize() {
        // Given: resource with size 0
        resource = new Resource(simulation, "Test Resource");

        // When: getting capacity
        int capacity = resource.getCapacity();

        // Then: capacity should be 0
        assertEquals(0, capacity);
    }

    @Test
    void shouldCreateResourceWithValidCapacity() {
        // Given: various resource sizes
        Resource r1 = new Resource(simulation, "Resource 1", 1, null);
        Resource r2 = new Resource(simulation, "Resource 2", 10, null);
        Resource r3 = new Resource(simulation, "Resource 3", 100, null);

        // Then: capacities should match constructor arguments
        assertEquals(1, r1.getCapacity());
        assertEquals(10, r2.getCapacity());
        assertEquals(100, r3.getCapacity());
    }
}
