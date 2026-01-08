package es.ull.simulation.condition;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the TrueCondition class
 */
class TrueConditionTest {

    @Test
    void shouldAlwaysReturnTrue() {
        // Given: a TrueCondition
        TrueCondition<Object> condition = new TrueCondition<>();
        
        // When: checking with any object
        boolean result = condition.check(new Object());
        
        // Then: it should return true
        assertTrue(result);
    }
    
    @Test
    void shouldReturnTrue_whenCheckingWithNull() {
        // Given: a TrueCondition
        TrueCondition<Object> condition = new TrueCondition<>();
        
        // When: checking with null
        boolean result = condition.check(null);
        
        // Then: it should return true
        assertTrue(result);
    }
    
    @Test
    void shouldReturnTrue_whenCheckingMultipleTimes() {
        // Given: a TrueCondition
        TrueCondition<String> condition = new TrueCondition<>();
        
        // When: checking multiple times
        // Then: all checks should return true
        assertTrue(condition.check("test1"));
        assertTrue(condition.check("test2"));
        assertTrue(condition.check("test3"));
    }
}
