package es.ull.simulation.condition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the AndCondition class
 */
class AndConditionTest {

    @Test
    void shouldReturnTrue_whenBothConditionsAreTrue() {
        // Given: two true conditions
        AbstractCondition<Object> cond1 = new TrueCondition<>();
        AbstractCondition<Object> cond2 = new TrueCondition<>();
        AndCondition<Object> condition = new AndCondition<>(cond1, cond2);

        // When: checking the AND condition
        boolean result = condition.check(new Object());

        // Then: it should return true
        assertTrue(result);
    }

    @Test
    void shouldReturnFalse_whenFirstConditionIsFalse() {
        // Given: first condition is false, second is true
        AbstractCondition<Object> cond1 = new FalseCondition<>();
        AbstractCondition<Object> cond2 = new TrueCondition<>();
        AndCondition<Object> condition = new AndCondition<>(cond1, cond2);

        // When: checking the AND condition
        boolean result = condition.check(new Object());

        // Then: it should return false
        assertFalse(result);
    }

    @Test
    void shouldReturnFalse_whenSecondConditionIsFalse() {
        // Given: first condition is true, second is false
        AbstractCondition<Object> cond1 = new TrueCondition<>();
        AbstractCondition<Object> cond2 = new FalseCondition<>();
        AndCondition<Object> condition = new AndCondition<>(cond1, cond2);

        // When: checking the AND condition
        boolean result = condition.check(new Object());

        // Then: it should return false
        assertFalse(result);
    }

    @Test
    void shouldReturnFalse_whenBothConditionsAreFalse() {
        // Given: both conditions are false
        AbstractCondition<Object> cond1 = new FalseCondition<>();
        AbstractCondition<Object> cond2 = new FalseCondition<>();
        AndCondition<Object> condition = new AndCondition<>(cond1, cond2);

        // When: checking the AND condition
        boolean result = condition.check(new Object());

        // Then: it should return false
        assertFalse(result);
    }

    @Test
    void shouldCreateFromCollection() {
        // Given: a collection of conditions
        Collection<AbstractCondition<Object>> conditions = Arrays.asList(
            new TrueCondition<>(),
            new TrueCondition<>(),
            new TrueCondition<>()
        );

        // When: creating AND condition from collection
        AndCondition<Object> condition = new AndCondition<>(conditions);

        // Then: it should check all conditions
        assertTrue(condition.check(new Object()));
    }

    @Test
    void shouldReturnFalse_whenOneConditionInCollectionIsFalse() {
        // Given: a collection with one false condition
        Collection<AbstractCondition<Object>> conditions = Arrays.asList(
            new TrueCondition<>(),
            new FalseCondition<>(),
            new TrueCondition<>()
        );

        // When: creating AND condition from collection
        AndCondition<Object> condition = new AndCondition<>(conditions);

        // Then: it should return false
        assertFalse(condition.check(new Object()));
    }

    @Test
    void shouldReturnConditionList() {
        // Given: an AND condition with two conditions
        AbstractCondition<Object> cond1 = new TrueCondition<>();
        AbstractCondition<Object> cond2 = new FalseCondition<>();
        AndCondition<Object> condition = new AndCondition<>(cond1, cond2);

        // When: getting the condition list
        Collection<AbstractCondition<Object>> result = condition.getConditionList();

        // Then: it should return a collection with 2 conditions
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void shouldHandleEmptyCollection() {
        // Given: an empty collection of conditions
        Collection<AbstractCondition<Object>> conditions = Arrays.asList();
        AndCondition<Object> condition = new AndCondition<>(conditions);

        // When: checking the AND condition
        boolean result = condition.check(new Object());

        // Then: it should return true (vacuous truth)
        assertTrue(result);
    }
}
