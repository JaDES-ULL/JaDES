package es.ull.simulation.condition;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.ull.simulation.model.ResourceType;
import es.ull.simulation.model.Simulation;
import es.ull.simulation.model.TimeUnit;

/**
 * Unit tests for the ResourceTypeAcquiredCondition class.
 * Note: Full integration testing of check() would require running the simulation,
 * so these tests focus on construction and getters.
 */
class ResourceTypeAcquiredConditionTest {

    private Simulation simulation;

    @BeforeEach
    void setUp() {
        simulation = new Simulation(0, "Test Simulation", TimeUnit.MINUTE);
    }

    @Test
    void shouldStoreResourceType() {
        // Given: a resource type
        ResourceType resourceType = new ResourceType(simulation, "TestResource");

        // When: creating a condition
        ResourceTypeAcquiredCondition condition = new ResourceTypeAcquiredCondition(resourceType);

        // Then: it should store the resource type
        assertNotNull(condition.getResourceType());
        assertSame(resourceType, condition.getResourceType());
    }

    @Test
    void shouldCreateWithDifferentResourceTypes() {
        // Given: multiple resource types
        ResourceType resourceType1 = new ResourceType(simulation, "Resource1");
        ResourceType resourceType2 = new ResourceType(simulation, "Resource2");

        // When: creating conditions
        ResourceTypeAcquiredCondition condition1 = new ResourceTypeAcquiredCondition(resourceType1);
        ResourceTypeAcquiredCondition condition2 = new ResourceTypeAcquiredCondition(resourceType2);

        // Then: each should store its resource type
        assertSame(resourceType1, condition1.getResourceType());
        assertSame(resourceType2, condition2.getResourceType());
    }

    @Test
    void shouldReturnSameResourceTypeReference() {
        // Given: a condition with a resource type
        ResourceType resourceType = new ResourceType(simulation, "TestResource");
        ResourceTypeAcquiredCondition condition = new ResourceTypeAcquiredCondition(resourceType);

        // When: getting resource type multiple times
        ResourceType result1 = condition.getResourceType();
        ResourceType result2 = condition.getResourceType();

        // Then: it should return the same reference
        assertNotNull(result1);
        assertSame(resourceType, result1);
        assertSame(result1, result2);
    }
}
