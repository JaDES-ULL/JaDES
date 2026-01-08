package es.ull.simulation.condition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the PercentageCondition class
 */
class PercentageConditionTest {

    @Test
    void shouldAlwaysReturnTrue_when100Percent() {
        // Given: a condition with 100% success
        PercentageCondition<Object> condition = new PercentageCondition<>(100.0);
        
        // When: checking multiple times
        // Then: all checks should return true
        for (int i = 0; i < 100; i++) {
            assertTrue(condition.check(new Object()));
        }
    }
    
    @Test
    void shouldAlwaysReturnFalse_when0Percent() {
        // Given: a condition with 0% success
        PercentageCondition<Object> condition = new PercentageCondition<>(0.0);
        
        // When: checking multiple times
        // Then: all checks should return false
        for (int i = 0; i < 100; i++) {
            assertFalse(condition.check(new Object()));
        }
    }
    
    @Test
    void shouldReturnMixedResults_when50Percent() {
        // Given: a condition with 50% success
        PercentageCondition<Object> condition = new PercentageCondition<>(50.0);
        
        // When: checking many times
        int trueCount = 0;
        int iterations = 10000;
        for (int i = 0; i < iterations; i++) {
            if (condition.check(new Object())) {
                trueCount++;
            }
        }
        
        // Then: approximately 50% should be true (with some tolerance)
        double actualPercentage = (trueCount * 100.0) / iterations;
        assertTrue(actualPercentage > 45.0 && actualPercentage < 55.0, 
            "Expected ~50%, got " + actualPercentage + "%");
    }
    
    @Test
    void shouldAcceptAnyObject() {
        // Given: a condition with 100% success
        PercentageCondition<Object> condition = new PercentageCondition<>(100.0);
        
        // When: checking with different objects
        // Then: all should return true
        assertTrue(condition.check("string"));
        assertTrue(condition.check(123));
        assertTrue(condition.check(new Object()));
        assertTrue(condition.check(null));
    }
    
    @Test
    void shouldReturnConsistentBehavior_when100Percent() {
        // Given: multiple conditions with 100% success
        PercentageCondition<String> cond1 = new PercentageCondition<>(100.0);
        PercentageCondition<String> cond2 = new PercentageCondition<>(100.0);
        
        // When: checking both conditions
        // Then: both should always return true
        assertTrue(cond1.check("test"));
        assertTrue(cond2.check("test"));
    }
    
    @Test
    void shouldReturnConsistentBehavior_when0Percent() {
        // Given: multiple conditions with 0% success
        PercentageCondition<String> cond1 = new PercentageCondition<>(0.0);
        PercentageCondition<String> cond2 = new PercentageCondition<>(0.0);
        
        // When: checking both conditions
        // Then: both should always return false
        assertFalse(cond1.check("test"));
        assertFalse(cond2.check("test"));
    }
    
    @Test
    void shouldHandleHighPercentage() {
        // Given: a condition with 95% success
        PercentageCondition<Object> condition = new PercentageCondition<>(95.0);
        
        // When: checking many times
        int trueCount = 0;
        int iterations = 10000;
        for (int i = 0; i < iterations; i++) {
            if (condition.check(new Object())) {
                trueCount++;
            }
        }
        
        // Then: approximately 95% should be true
        double actualPercentage = (trueCount * 100.0) / iterations;
        assertTrue(actualPercentage > 93.0 && actualPercentage < 97.0,
            "Expected ~95%, got " + actualPercentage + "%");
    }
    
    @Test
    void shouldHandleLowPercentage() {
        // Given: a condition with 5% success
        PercentageCondition<Object> condition = new PercentageCondition<>(5.0);
        
        // When: checking many times
        int trueCount = 0;
        int iterations = 10000;
        for (int i = 0; i < iterations; i++) {
            if (condition.check(new Object())) {
                trueCount++;
            }
        }
        
        // Then: approximately 5% should be true
        double actualPercentage = (trueCount * 100.0) / iterations;
        assertTrue(actualPercentage > 3.0 && actualPercentage < 7.0,
            "Expected ~5%, got " + actualPercentage + "%");
    }
}
