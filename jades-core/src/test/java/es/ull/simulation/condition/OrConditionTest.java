package es.ull.simulation.condition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the OrCondition class
 */
class OrConditionTest {

    @Test
    void shouldReturnTrue_whenBothConditionsAreTrue() {
        // Given: two true conditions
        AbstractCondition<Object> cond1 = new TrueCondition<>();
        AbstractCondition<Object> cond2 = new TrueCondition<>();
        OrCondition<Object> condition = new OrCondition<>(cond1, cond2);

        // When: checking the OR condition
        boolean result = condition.check(new Object());

        // Then: it should return true
        assertTrue(result);
    }

    @Test
    void shouldReturnTrue_whenFirstConditionIsTrue() {
        // Given: first condition is true, second is false
        AbstractCondition<Object> cond1 = new TrueCondition<>();
        AbstractCondition<Object> cond2 = new FalseCondition<>();
        OrCondition<Object> condition = new OrCondition<>(cond1, cond2);

        // When: checking the OR condition
        boolean result = condition.check(new Object());

        // Then: it should return true
        assertTrue(result);
    }

    @Test
    void shouldReturnTrue_whenSecondConditionIsTrue() {
        // Given: first condition is false, second is true
        AbstractCondition<Object> cond1 = new FalseCondition<>();
        AbstractCondition<Object> cond2 = new TrueCondition<>();
        OrCondition<Object> condition = new OrCondition<>(cond1, cond2);

        // When: checking the OR condition
        boolean result = condition.check(new Object());

        // Then: it should return true
        assertTrue(result);
    }

    @Test
    void shouldReturnFalse_whenBothConditionsAreFalse() {
        // Given: both conditions are false
        AbstractCondition<Object> cond1 = new FalseCondition<>();
        AbstractCondition<Object> cond2 = new FalseCondition<>();
        OrCondition<Object> condition = new OrCondition<>(cond1, cond2);

        // When: checking the OR condition
        boolean result = condition.check(new Object());

        // Then: it should return false
        assertFalse(result);
    }

    @Test
    void shouldCreateFromCollection() {
        // Given: a collection of conditions
        Collection<AbstractCondition<Object>> conditions = Arrays.asList(
            new FalseCondition<>(),
            new TrueCondition<>(),
            new FalseCondition<>()
        );

        // When: creating OR condition from collection
        OrCondition<Object> condition = new OrCondition<>(conditions);

        // Then: it should return true (at least one is true)
        assertTrue(condition.check(new Object()));
    }

    @Test
    void shouldReturnFalse_whenAllConditionsInCollectionAreFalse() {
        // Given: a collection with all false conditions
        Collection<AbstractCondition<Object>> conditions = Arrays.asList(
            new FalseCondition<>(),
            new FalseCondition<>(),
            new FalseCondition<>()
        );

        // When: creating OR condition from collection
        OrCondition<Object> condition = new OrCondition<>(conditions);

        // Then: it should return false
        assertFalse(condition.check(new Object()));
    }

    @Test
    void shouldReturnConditionList() {
        // Given: an OR condition with two conditions
        AbstractCondition<Object> cond1 = new TrueCondition<>();
        AbstractCondition<Object> cond2 = new FalseCondition<>();
        OrCondition<Object> condition = new OrCondition<>(cond1, cond2);

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
        OrCondition<Object> condition = new OrCondition<>(conditions);

        // When: checking the OR condition
        boolean result = condition.check(new Object());

        // Then: it should return false (no conditions to satisfy)
        assertFalse(result);
    }
}
