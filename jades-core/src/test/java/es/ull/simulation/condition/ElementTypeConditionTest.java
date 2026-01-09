package es.ull.simulation.condition;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.ull.simulation.model.Element;
import es.ull.simulation.model.ElementInstance;
import es.ull.simulation.model.ElementType;
import es.ull.simulation.model.Simulation;
import es.ull.simulation.model.TimeUnit;

/**
 * Unit tests for the ElementTypeCondition class.
 * Note: Full integration testing of check() would require running the simulation,
 * so these tests focus on construction and getters.
 */
class ElementTypeConditionTest {

    private Simulation simulation;

    @BeforeEach
    void setUp() {
        simulation = new Simulation(0, "Test Simulation", TimeUnit.MINUTE);
    }

    @Test
    void shouldStoreType() {
        // Given: an element type
        ElementType type = new ElementType(simulation, "TestType");

        // When: creating a condition
        ElementTypeCondition condition = new ElementTypeCondition(type);

        // Then: it should store the type
        assertSame(type, condition.getType());
    }

    @Test
    void shouldCreateWithDifferentTypes() {
        // Given: two different element types
        ElementType type1 = new ElementType(simulation, "Type1");
        ElementType type2 = new ElementType(simulation, "Type2");

        // When: creating conditions
        ElementTypeCondition condition1 = new ElementTypeCondition(type1);
        ElementTypeCondition condition2 = new ElementTypeCondition(type2);

        // Then: each should store its type
        assertSame(type1, condition1.getType());
        assertSame(type2, condition2.getType());
    }

    @Test
    void shouldReturnSameTypeReference() {
        // Given: a condition with a type
        ElementType type = new ElementType(simulation, "TestType");
        ElementTypeCondition condition = new ElementTypeCondition(type);

        // When: getting type multiple times
        ElementType result1 = condition.getType();
        ElementType result2 = condition.getType();

        // Then: it should return the same reference
        assertNotNull(result1);
        assertSame(type, result1);
        assertSame(result1, result2);
    }
}

