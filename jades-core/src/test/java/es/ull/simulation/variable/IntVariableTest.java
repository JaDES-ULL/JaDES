package es.ull.simulation.variable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the IntVariable class
 */
class IntVariableTest {

    @Test
    void shouldCreateVariableFromInt() {
        // Given: an integer value
        // When: creating an IntVariable
        IntVariable var = new IntVariable(42);
        
        // Then: it should return the correct value
        assertEquals(Integer.valueOf(42), var.getValue());
    }
    
    @Test
    void shouldCreateVariableFromDouble() {
        // Given: a double value
        // When: creating an IntVariable
        IntVariable var = new IntVariable(3.14);
        
        // Then: it should truncate to integer
        assertEquals(Integer.valueOf(3), var.getValue());
    }
    
    @Test
    void shouldCreateVariableFromNegativeInt() {
        // Given: a negative integer
        // When: creating an IntVariable
        IntVariable var = new IntVariable(-100);
        
        // Then: it should store the negative value
        assertEquals(Integer.valueOf(-100), var.getValue());
    }
    
    @Test
    void shouldCreateVariableFromZero() {
        // Given: zero value
        // When: creating an IntVariable
        IntVariable var = new IntVariable(0);
        
        // Then: it should store zero
        assertEquals(Integer.valueOf(0), var.getValue());
    }
    
    @Test
    void shouldSetValueFromInt() {
        // Given: an IntVariable
        IntVariable var = new IntVariable(10);
        
        // When: setting value from int
        var.setValue(25);
        
        // Then: it should update the value
        assertEquals(Integer.valueOf(25), var.getValue());
    }
    
    @Test
    void shouldSetValueFromTrue() {
        // Given: an IntVariable
        IntVariable var = new IntVariable(10);
        
        // When: setting value from boolean true
        var.setValue(true);
        
        // Then: it should be 0
        assertEquals(Integer.valueOf(0), var.getValue());
    }
    
    @Test
    void shouldSetValueFromFalse() {
        // Given: an IntVariable
        IntVariable var = new IntVariable(10);
        
        // When: setting value from boolean false
        var.setValue(false);
        
        // Then: it should be 1
        assertEquals(Integer.valueOf(1), var.getValue());
    }
    
    @Test
    void shouldSetValueFromChar() {
        // Given: an IntVariable
        IntVariable var = new IntVariable(10);
        
        // When: setting value from char 'A' (ASCII 65)
        var.setValue('A');
        
        // Then: it should convert to ASCII value
        assertEquals(Integer.valueOf(65), var.getValue());
    }
    
    @Test
    void shouldSetValueFromByte() {
        // Given: an IntVariable
        IntVariable var = new IntVariable(10);
        
        // When: setting value from byte
        var.setValue((byte) 127);
        
        // Then: it should convert to int
        assertEquals(Integer.valueOf(127), var.getValue());
    }
    
    @Test
    void shouldSetValueFromDouble() {
        // Given: an IntVariable
        IntVariable var = new IntVariable(10);
        
        // When: setting value from double
        var.setValue(99.99);
        
        // Then: it should truncate to int
        assertEquals(Integer.valueOf(99), var.getValue());
    }
    
    @Test
    void shouldSetValueFromNegativeDouble() {
        // Given: an IntVariable
        IntVariable var = new IntVariable(10);
        
        // When: setting value from negative double
        var.setValue(-5.7);
        
        // Then: it should truncate to negative int
        assertEquals(Integer.valueOf(-5), var.getValue());
    }
    
    @Test
    void shouldSetValueFromFloat() {
        // Given: an IntVariable
        IntVariable var = new IntVariable(10);
        
        // When: setting value from float
        var.setValue(15.5f);
        
        // Then: it should truncate to int
        assertEquals(Integer.valueOf(15), var.getValue());
    }
    
    @Test
    void shouldSetValueFromLong() {
        // Given: an IntVariable
        IntVariable var = new IntVariable(10);
        
        // When: setting value from long
        var.setValue(1000000L);
        
        // Then: it should convert to int (may overflow)
        assertEquals(Integer.valueOf(1000000), var.getValue());
    }
    
    @Test
    void shouldSetValueFromShort() {
        // Given: an IntVariable
        IntVariable var = new IntVariable(10);
        
        // When: setting value from short
        var.setValue((short) 500);
        
        // Then: it should convert to int
        assertEquals(Integer.valueOf(500), var.getValue());
    }
    
    @Test
    void shouldSetValueFromNumber() {
        // Given: an IntVariable
        IntVariable var = new IntVariable(10);
        
        // When: setting value from Number object
        var.setValue(Integer.valueOf(333));
        
        // Then: it should update the value
        assertEquals(Integer.valueOf(333), var.getValue());
    }
    
    @Test
    void shouldSetValueFromObject() {
        // Given: an IntVariable
        IntVariable var = new IntVariable(10);
        
        // When: setting value from Object (Number)
        var.setValue((Object) Integer.valueOf(777));
        
        // Then: it should update the value
        assertEquals(Integer.valueOf(777), var.getValue());
    }
    
    @Test
    void shouldReturnCorrectToString() {
        // Given: an IntVariable with value 42
        IntVariable var = new IntVariable(42);
        
        // When: calling toString
        String result = var.toString();
        
        // Then: it should return "42"
        assertEquals("42", result);
    }
    
    @Test
    void shouldReturnCorrectToStringForNegative() {
        // Given: an IntVariable with negative value
        IntVariable var = new IntVariable(-100);
        
        // When: calling toString
        String result = var.toString();
        
        // Then: it should return "-100"
        assertEquals("-100", result);
    }
    
    @Test
    void shouldCompareEqualVariables() {
        // Given: two IntVariables with same value
        IntVariable var1 = new IntVariable(42);
        IntVariable var2 = new IntVariable(42);
        
        // When: comparing them
        boolean result = var1.equals(var2);
        
        // Then: they should be equal
        assertTrue(result);
    }
    
    @Test
    void shouldCompareDifferentVariables() {
        // Given: two IntVariables with different values
        IntVariable var1 = new IntVariable(42);
        IntVariable var2 = new IntVariable(100);
        
        // When: comparing them
        boolean result = var1.equals(var2);
        
        // Then: they should not be equal
        assertFalse(result);
    }
    
    @Test
    void shouldHandleMaxValue() {
        // Given: maximum integer value
        // When: creating an IntVariable
        IntVariable var = new IntVariable(Integer.MAX_VALUE);
        
        // Then: it should store the max value
        assertEquals(Integer.valueOf(Integer.MAX_VALUE), var.getValue());
    }
    
    @Test
    void shouldHandleMinValue() {
        // Given: minimum integer value
        // When: creating an IntVariable
        IntVariable var = new IntVariable(Integer.MIN_VALUE);
        
        // Then: it should store the min value
        assertEquals(Integer.valueOf(Integer.MIN_VALUE), var.getValue());
    }
    
    @Test
    void shouldUpdateValue_whenSetMultipleTimes() {
        // Given: an IntVariable
        IntVariable var = new IntVariable(10);
        
        // When: setting value multiple times
        var.setValue(20);
        assertEquals(Integer.valueOf(20), var.getValue());
        var.setValue(30);
        assertEquals(Integer.valueOf(30), var.getValue());
        var.setValue(40);
        
        // Then: it should have the final value
        assertEquals(Integer.valueOf(40), var.getValue());
    }
}
