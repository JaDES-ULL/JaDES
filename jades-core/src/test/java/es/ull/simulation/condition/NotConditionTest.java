package es.ull.simulation.condition;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the NotCondition class
 */
class NotConditionTest {

    @Test
    void shouldNegateTrueCondition() {
        // Given: a TrueCondition wrapped in NotCondition
        AbstractCondition<Object> trueCondition = new TrueCondition<>();
        NotCondition<Object> condition = new NotCondition<>(trueCondition);

        // When: checking the condition
        boolean result = condition.check(new Object());

        // Then: it should return false (negation of true)
        assertFalse(result);
    }

    @Test
    void shouldNegateFalseCondition() {
        // Given: a FalseCondition wrapped in NotCondition
        AbstractCondition<Object> falseCondition = new FalseCondition<>();
        NotCondition<Object> condition = new NotCondition<>(falseCondition);

        // When: checking the condition
        boolean result = condition.check(new Object());

        // Then: it should return true (negation of false)
        assertTrue(result);
    }

    @Test
    void shouldReturnAssociatedCondition() {
        // Given: a condition wrapped in NotCondition
        AbstractCondition<Object> innerCondition = new TrueCondition<>();
        NotCondition<Object> condition = new NotCondition<>(innerCondition);

        // When: getting the associated condition
        AbstractCondition<Object> result = condition.getCond();

        // Then: it should return the same condition
        assertNotNull(result);
        assertSame(innerCondition, result);
    }

    @Test
    void shouldNegateMultipleTimes() {
        // Given: a TrueCondition wrapped in NotCondition
        NotCondition<String> condition = new NotCondition<>(new TrueCondition<>());

        // When: checking multiple times
        // Then: all checks should return false
        assertFalse(condition.check("test1"));
        assertFalse(condition.check("test2"));
        assertFalse(condition.check("test3"));
    }

    @Test
    void shouldHandleDoubleNegation() {
        // Given: a TrueCondition wrapped in double NotCondition
        AbstractCondition<Object> trueCondition = new TrueCondition<>();
        NotCondition<Object> notCondition = new NotCondition<>(trueCondition);
        NotCondition<Object> doubleNot = new NotCondition<>(notCondition);

        // When: checking the double negation
        boolean result = doubleNot.check(new Object());

        // Then: it should return true (negation of negation)
        assertTrue(result);
    }
}
