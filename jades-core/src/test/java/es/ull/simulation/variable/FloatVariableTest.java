package es.ull.simulation.variable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the FloatVariable class
 */
class FloatVariableTest {

    @Test
    void shouldCreateVariableFromFloat() {
        // Given: a float value
        // When: creating a FloatVariable
        FloatVariable variable = new FloatVariable(3.14f);
        
        // Then: it should return the correct value
        assertEquals(Float.valueOf(3.14f), variable.getValue());
    }
    
    @Test
    void shouldCreateVariableFromDouble() {
        // Given: a double value
        // When: creating a FloatVariable
        FloatVariable variable = new FloatVariable(2.71828);
        
        // Then: it should convert to float
        assertEquals(Float.valueOf(2.71828f), variable.getValue());
    }
    
    @Test
    void shouldCreateVariableFromZero() {
        // Given: zero value
        // When: creating a FloatVariable
        FloatVariable variable = new FloatVariable(0.0f);
        
        // Then: it should store zero
        assertEquals(Float.valueOf(0.0f), variable.getValue());
    }
    
    @Test
    void shouldCreateVariableFromNegative() {
        // Given: a negative float value
        // When: creating a FloatVariable
        FloatVariable variable = new FloatVariable(-99.5f);
        
        // Then: it should store the negative value
        assertEquals(Float.valueOf(-99.5f), variable.getValue());
    }
    
    @Test
    void shouldSetValueFromInt() {
        // Given: a FloatVariable
        FloatVariable variable = new FloatVariable(10.5f);
        
        // When: setting value from int
        variable.setValue(42);
        
        // Then: it should convert to float
        assertEquals(Float.valueOf(42.0f), variable.getValue());
    }
    
    @Test
    void shouldSetValueFromTrue() {
        // Given: a FloatVariable
        FloatVariable variable = new FloatVariable(10.0f);
        
        // When: setting value from boolean true
        variable.setValue(true);
        
        // Then: it should be 0
        assertEquals(Float.valueOf(0.0f), variable.getValue());
    }
    
    @Test
    void shouldSetValueFromFalse() {
        // Given: a FloatVariable
        FloatVariable variable = new FloatVariable(10.0f);
        
        // When: setting value from boolean false
        variable.setValue(false);
        
        // Then: it should be 1
        assertEquals(Float.valueOf(1.0f), variable.getValue());
    }
    
    @Test
    void shouldSetValueFromChar() {
        // Given: a FloatVariable
        FloatVariable variable = new FloatVariable(10.0f);
        
        // When: setting value from char 'A' (ASCII 65)
        variable.setValue('A');
        
        // Then: it should convert to float of ASCII value
        assertEquals(Float.valueOf(65.0f), variable.getValue());
    }
    
    @Test
    void shouldSetValueFromByte() {
        // Given: a FloatVariable
        FloatVariable variable = new FloatVariable(10.0f);
        
        // When: setting value from byte
        variable.setValue((byte) 127);
        
        // Then: it should convert to float
        assertEquals(Float.valueOf(127.0f), variable.getValue());
    }
    
    @Test
    void shouldSetValueFromDouble() {
        // Given: a FloatVariable
        FloatVariable variable = new FloatVariable(10.0f);
        
        // When: setting value from double
        variable.setValue(99.99);
        
        // Then: it should convert to float
        assertEquals(Float.valueOf(99.99f), variable.getValue());
    }
    
    @Test
    void shouldSetValueFromFloat() {
        // Given: a FloatVariable
        FloatVariable variable = new FloatVariable(10.0f);
        
        // When: setting value from float
        variable.setValue(15.5f);
        
        // Then: it should update the value
        assertEquals(Float.valueOf(15.5f), variable.getValue());
    }
    
    @Test
    void shouldSetValueFromLong() {
        // Given: a FloatVariable
        FloatVariable variable = new FloatVariable(10.0f);
        
        // When: setting value from long
        variable.setValue(1000000L);
        
        // Then: it should convert to float
        assertEquals(Float.valueOf(1000000.0f), variable.getValue());
    }
    
    @Test
    void shouldSetValueFromShort() {
        // Given: a FloatVariable
        FloatVariable variable = new FloatVariable(10.0f);
        
        // When: setting value from short
        variable.setValue((short) 500);
        
        // Then: it should convert to float
        assertEquals(Float.valueOf(500.0f), variable.getValue());
    }
    
    @Test
    void shouldSetValueFromNumber() {
        // Given: a FloatVariable
        FloatVariable variable = new FloatVariable(10.0f);
        
        // When: setting value from Number object
        variable.setValue(Float.valueOf(3.14f));
        
        // Then: it should update the value
        assertEquals(Float.valueOf(3.14f), variable.getValue());
    }
    
    @Test
    void shouldSetValueFromObject() {
        // Given: a FloatVariable
        FloatVariable variable = new FloatVariable(10.0f);
        
        // When: setting value from Object (Number)
        variable.setValue((Object) Float.valueOf(2.71f));
        
        // Then: it should update the value
        assertEquals(Float.valueOf(2.71f), variable.getValue());
    }
    
    @Test
    void shouldReturnCorrectToString() {
        // Given: a FloatVariable with value 3.14
        FloatVariable variable = new FloatVariable(3.14f);
        
        // When: calling toString
        String result = variable.toString();
        
        // Then: it should return "3.14"
        assertEquals("3.14", result);
    }
    
    @Test
    void shouldCompareEqualVariables() {
        // Given: two FloatVariables with same value
        FloatVariable var1 = new FloatVariable(3.14f);
        FloatVariable var2 = new FloatVariable(3.14f);
        
        // When: comparing them
        boolean result = var1.equals(var2);
        
        // Then: they should be equal
        assertTrue(result);
    }
    
    @Test
    void shouldCompareDifferentVariables() {
        // Given: two FloatVariables with different values
        FloatVariable var1 = new FloatVariable(3.14f);
        FloatVariable var2 = new FloatVariable(2.71f);
        
        // When: comparing them
        boolean result = var1.equals(var2);
        
        // Then: they should not be equal
        assertFalse(result);
    }
    
    @Test
    void shouldHandleMaxValue() {
        // Given: maximum float value
        // When: creating a FloatVariable
        FloatVariable variable = new FloatVariable(Float.MAX_VALUE);
        
        // Then: it should store the max value
        assertEquals(Float.valueOf(Float.MAX_VALUE), variable.getValue());
    }
    
    @Test
    void shouldHandleMinValue() {
        // Given: minimum float value
        // When: creating a FloatVariable
        FloatVariable variable = new FloatVariable(Float.MIN_VALUE);
        
        // Then: it should store the min value
        assertEquals(Float.valueOf(Float.MIN_VALUE), variable.getValue());
    }
    
    @Test
    void shouldHandlePositiveInfinity() {
        // Given: positive infinity
        // When: creating a FloatVariable
        FloatVariable variable = new FloatVariable(Float.POSITIVE_INFINITY);
        
        // Then: it should store positive infinity
        assertEquals(Float.valueOf(Float.POSITIVE_INFINITY), variable.getValue());
    }
    
    @Test
    void shouldHandleNegativeInfinity() {
        // Given: negative infinity
        // When: creating a FloatVariable
        FloatVariable variable = new FloatVariable(Float.NEGATIVE_INFINITY);
        
        // Then: it should store negative infinity
        assertEquals(Float.valueOf(Float.NEGATIVE_INFINITY), variable.getValue());
    }
    
    @Test
    void shouldUpdateValue_whenSetMultipleTimes() {
        // Given: a FloatVariable
        FloatVariable variable = new FloatVariable(1.0f);
        
        // When: setting value multiple times
        variable.setValue(2.0f);
        assertEquals(Float.valueOf(2.0f), variable.getValue());
        variable.setValue(3.0f);
        assertEquals(Float.valueOf(3.0f), variable.getValue());
        variable.setValue(4.0f);
        
        // Then: it should have the final value
        assertEquals(Float.valueOf(4.0f), variable.getValue());
    }
}
