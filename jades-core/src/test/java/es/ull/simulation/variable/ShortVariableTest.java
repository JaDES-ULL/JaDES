package es.ull.simulation.variable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the ShortVariable class
 */
class ShortVariableTest {

    @Test
    void shouldCreateVariableFromShort() {
        // Given: a short value
        // When: creating a ShortVariable
        ShortVariable variable = new ShortVariable((short) 1000);
        
        // Then: it should return the correct value
        assertEquals(Short.valueOf((short) 1000), variable.getValue());
    }
    
    @Test
    void shouldCreateVariableFromDouble() {
        // Given: a double value
        // When: creating a ShortVariable
        ShortVariable variable = new ShortVariable(999.7);
        
        // Then: it should truncate to short
        assertEquals(Short.valueOf((short) 999), variable.getValue());
    }
    
    @Test
    void shouldCreateVariableFromZero() {
        // Given: zero value
        // When: creating a ShortVariable
        ShortVariable variable = new ShortVariable((short) 0);
        
        // Then: it should store zero
        assertEquals(Short.valueOf((short) 0), variable.getValue());
    }
    
    @Test
    void shouldCreateVariableFromNegative() {
        // Given: a negative short value
        // When: creating a ShortVariable
        ShortVariable variable = new ShortVariable((short) -5000);
        
        // Then: it should store the negative value
        assertEquals(Short.valueOf((short) -5000), variable.getValue());
    }
    
    @Test
    void shouldSetValueFromInt() {
        // Given: a ShortVariable
        ShortVariable variable = new ShortVariable((short) 100);
        
        // When: setting value from int
        variable.setValue(5000);
        
        // Then: it should convert to short
        assertEquals(Short.valueOf((short) 5000), variable.getValue());
    }
    
    @Test
    void shouldSetValueFromTrue() {
        // Given: a ShortVariable
        ShortVariable variable = new ShortVariable((short) 100);
        
        // When: setting value from boolean true
        variable.setValue(true);
        
        // Then: it should be 0
        assertEquals(Short.valueOf((short) 0), variable.getValue());
    }
    
    @Test
    void shouldSetValueFromFalse() {
        // Given: a ShortVariable
        ShortVariable variable = new ShortVariable((short) 100);
        
        // When: setting value from boolean false
        variable.setValue(false);
        
        // Then: it should be 1
        assertEquals(Short.valueOf((short) 1), variable.getValue());
    }
    
    @Test
    void shouldSetValueFromChar() {
        // Given: a ShortVariable
        ShortVariable variable = new ShortVariable((short) 100);
        
        // When: setting value from char 'A' (ASCII 65)
        variable.setValue('A');
        
        // Then: it should convert to short
        assertEquals(Short.valueOf((short) 65), variable.getValue());
    }
    
    @Test
    void shouldSetValueFromByte() {
        // Given: a ShortVariable
        ShortVariable variable = new ShortVariable((short) 100);
        
        // When: setting value from byte
        variable.setValue((byte) 127);
        
        // Then: it should convert to short
        assertEquals(Short.valueOf((short) 127), variable.getValue());
    }
    
    @Test
    void shouldSetValueFromDouble() {
        // Given: a ShortVariable
        ShortVariable variable = new ShortVariable((short) 100);
        
        // When: setting value from double
        variable.setValue(9999.99);
        
        // Then: it should truncate to short
        assertEquals(Short.valueOf((short) 9999), variable.getValue());
    }
    
    @Test
    void shouldSetValueFromFloat() {
        // Given: a ShortVariable
        ShortVariable variable = new ShortVariable((short) 100);
        
        // When: setting value from float
        variable.setValue(1500.5f);
        
        // Then: it should truncate to short
        assertEquals(Short.valueOf((short) 1500), variable.getValue());
    }
    
    @Test
    void shouldSetValueFromLong() {
        // Given: a ShortVariable
        ShortVariable variable = new ShortVariable((short) 100);
        
        // When: setting value from long
        variable.setValue(10000L);
        
        // Then: it should convert to short
        assertEquals(Short.valueOf((short) 10000), variable.getValue());
    }
    
    @Test
    void shouldSetValueFromShort() {
        // Given: a ShortVariable
        ShortVariable variable = new ShortVariable((short) 100);
        
        // When: setting value from short
        variable.setValue((short) 500);
        
        // Then: it should update the value
        assertEquals(Short.valueOf((short) 500), variable.getValue());
    }
    
    @Test
    void shouldSetValueFromNumber() {
        // Given: a ShortVariable
        ShortVariable variable = new ShortVariable((short) 100);
        
        // When: setting value from Number object
        variable.setValue(Short.valueOf((short) 3333));
        
        // Then: it should update the value
        assertEquals(Short.valueOf((short) 3333), variable.getValue());
    }
    
    @Test
    void shouldSetValueFromObject() {
        // Given: a ShortVariable
        ShortVariable variable = new ShortVariable((short) 100);
        
        // When: setting value from Object (Number)
        variable.setValue((Object) Short.valueOf((short) 7777));
        
        // Then: it should update the value
        assertEquals(Short.valueOf((short) 7777), variable.getValue());
    }
    
    @Test
    void shouldReturnCorrectToString() {
        // Given: a ShortVariable with value 1234
        ShortVariable variable = new ShortVariable((short) 1234);
        
        // When: calling toString
        String result = variable.toString();
        
        // Then: it should return "1234"
        assertEquals("1234", result);
    }
    
    @Test
    void shouldCompareEqualVariables() {
        // Given: two ShortVariables with same value
        ShortVariable var1 = new ShortVariable((short) 1234);
        ShortVariable var2 = new ShortVariable((short) 1234);
        
        // When: comparing them
        boolean result = var1.equals(var2);
        
        // Then: they should be equal
        assertTrue(result);
    }
    
    @Test
    void shouldCompareDifferentVariables() {
        // Given: two ShortVariables with different values
        ShortVariable var1 = new ShortVariable((short) 1234);
        ShortVariable var2 = new ShortVariable((short) 5678);
        
        // When: comparing them
        boolean result = var1.equals(var2);
        
        // Then: they should not be equal
        assertFalse(result);
    }
    
    @Test
    void shouldHandleMaxValue() {
        // Given: maximum short value (32767)
        // When: creating a ShortVariable
        ShortVariable variable = new ShortVariable(Short.MAX_VALUE);
        
        // Then: it should store the max value
        assertEquals(Short.valueOf(Short.MAX_VALUE), variable.getValue());
    }
    
    @Test
    void shouldHandleMinValue() {
        // Given: minimum short value (-32768)
        // When: creating a ShortVariable
        ShortVariable variable = new ShortVariable(Short.MIN_VALUE);
        
        // Then: it should store the min value
        assertEquals(Short.valueOf(Short.MIN_VALUE), variable.getValue());
    }
    
    @Test
    void shouldUpdateValue_whenSetMultipleTimes() {
        // Given: a ShortVariable
        ShortVariable variable = new ShortVariable((short) 100);
        
        // When: setting value multiple times
        variable.setValue((short) 200);
        assertEquals(Short.valueOf((short) 200), variable.getValue());
        variable.setValue((short) 300);
        assertEquals(Short.valueOf((short) 300), variable.getValue());
        variable.setValue((short) 400);
        
        // Then: it should have the final value
        assertEquals(Short.valueOf((short) 400), variable.getValue());
    }
}
