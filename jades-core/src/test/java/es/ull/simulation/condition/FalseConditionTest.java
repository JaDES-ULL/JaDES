package es.ull.simulation.condition;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the FalseCondition class
 */
class FalseConditionTest {

    @Test
    void shouldAlwaysReturnFalse() {
        // Given: a FalseCondition
        FalseCondition<Object> condition = new FalseCondition<>();

        // When: checking with any object
        boolean result = condition.check(new Object());

        // Then: it should return false
        assertFalse(result);
    }

    @Test
    void shouldReturnFalse_whenCheckingWithNull() {
        // Given: a FalseCondition
        FalseCondition<Object> condition = new FalseCondition<>();

        // When: checking with null
        boolean result = condition.check(null);

        // Then: it should return false
        assertFalse(result);
    }

    @Test
    void shouldReturnFalse_whenCheckingMultipleTimes() {
        // Given: a FalseCondition
        FalseCondition<String> condition = new FalseCondition<>();

        // When: checking multiple times
        // Then: all checks should return false
        assertFalse(condition.check("test1"));
        assertFalse(condition.check("test2"));
        assertFalse(condition.check("test3"));
    }
}
