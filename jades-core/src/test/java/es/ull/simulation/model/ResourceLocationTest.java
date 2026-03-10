package es.ull.simulation.model;

import es.ull.simulation.model.location.ILocation;
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
        ILocation location = resource.getLocation();

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

    @Test
    void shouldReturnNullLocation_initiallyWithoutInitLocation() {
        // Given: resource created without init location
        resource = new Resource(simulation, "Test Resource", 5, null);

        // When: checking location
        ILocation loc = resource.getLocation();

        // Then: location should be null
        assertNull(loc);
    }

    @Test
    void shouldMaintainCapacityConsistency() {
        // Given: resource with specific capacity
        final int capacity = 42;
        resource = new Resource(simulation, "Test Resource", capacity, null);

        // When: querying capacity multiple times
        int cap1 = resource.getCapacity();
        int cap2 = resource.getCapacity();
        int cap3 = resource.getCapacity();

        // Then: all should return same value
        assertEquals(capacity, cap1);
        assertEquals(capacity, cap2);
        assertEquals(capacity, cap3);
    }

    @Test
    void shouldSupportLargeCapacity() {
        // Given: resource with large capacity
        resource = new Resource(simulation, "Large Resource", 10000, null);

        // When: getting capacity
        int capacity = resource.getCapacity();

        // Then: should support large values
        assertEquals(10000, capacity);
    }

    @Test
    void shouldAllowZeroCapacity() {
        // Given: resource with zero capacity
        resource = new Resource(simulation, "Zero Resource", 0, null);

        // When: getting capacity
        int capacity = resource.getCapacity();

        // Then: should be zero
        assertEquals(0, capacity);
    }

    @Test
    void shouldMaintainIndependentLocations_acrossResources() {
        // Given: multiple resources
        Resource r1 = new Resource(simulation, "R1", 1, null);
        Resource r2 = new Resource(simulation, "R2", 2, null);

        // When: checking locations
        ILocation loc1 = r1.getLocation();
        ILocation loc2 = r2.getLocation();

        // Then: both should be independent (both null in this case)
        assertNull(loc1);
        assertNull(loc2);
    }

    // ── Tests adicionales de ResourceLocation ─────────────────────────────────

    @Test
    void shouldReturnNullInitLocation_whenCreatedWithNull() {
        ResourceLocation rl = new ResourceLocation(
                new Resource(simulation, "R"), null, 5);

        assertNull(rl.getInitLocation());
    }

    @Test
    void shouldReturnNullMovingInstance_initially() {
        ResourceLocation rl = new ResourceLocation(
                new Resource(simulation, "R"), null, 3);

        assertNull(rl.getMovingInstance());
    }

    @Test
    void shouldSetMovingInstance_whenCalled() {
        ResourceLocation rl = new ResourceLocation(
                new Resource(simulation, "R"), null, 1);

        // Establecer null de nuevo no lanza excepción
        rl.setMovingInstance(null);

        assertNull(rl.getMovingInstance());
    }

    @Test
    void shouldReturnTrue_whenInitializeWithNullInitLocation() {
        ResourceLocation rl = new ResourceLocation(
                new Resource(simulation, "R"), null, 2);

        // null initLocation → initialize() devuelve true inmediatamente
        assertTrue(rl.initialize());
    }
}
