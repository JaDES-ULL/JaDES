package es.ull.simulation.variable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the BooleanVariable class
 */
class BooleanVariableTest {

    @Test
    void shouldCreateVariableWithTrueValue() {
        // Given: a true boolean value
        // When: creating a BooleanVariable
        BooleanVariable var = new BooleanVariable(true);

        // Then: it should return 1 as integer value
        assertEquals(Integer.valueOf(1), var.getValue());
    }

    @Test
    void shouldCreateVariableWithFalseValue() {
        // Given: a false boolean value
        // When: creating a BooleanVariable
        BooleanVariable var = new BooleanVariable(false);

        // Then: it should return 0 as integer value
        assertEquals(Integer.valueOf(0), var.getValue());
    }

    @Test
    void shouldCreateVariableFromZeroDouble() {
        // Given: a double value of 0.0
        // When: creating a BooleanVariable
        BooleanVariable var = new BooleanVariable(0.0);

        // Then: it should be true and return 1
        assertEquals(Integer.valueOf(1), var.getValue());
    }

    @Test
    void shouldCreateVariableFromNonZeroDouble() {
        // Given: a non-zero double value
        // When: creating a BooleanVariable
        BooleanVariable var = new BooleanVariable(5.0);

        // Then: it should be false and return 0
        assertEquals(Integer.valueOf(0), var.getValue());
    }

    @Test
    void shouldSetValueFromObject() {
        // Given: a BooleanVariable
        BooleanVariable var = new BooleanVariable(false);

        // When: setting value from Boolean object
        var.setValue(Boolean.TRUE);

        // Then: it should return 1
        assertEquals(Integer.valueOf(1), var.getValue());
    }

    @Test
    void shouldSetValueFromInt() {
        // Given: a BooleanVariable
        BooleanVariable var = new BooleanVariable(true);

        // When: setting value from int 0
        var.setValue(0);

        // Then: it should be true and return 1
        assertEquals(Integer.valueOf(1), var.getValue());
    }

    @Test
    void shouldSetValueFromNonZeroInt() {
        // Given: a BooleanVariable
        BooleanVariable var = new BooleanVariable(true);

        // When: setting value from non-zero int
        var.setValue(5);

        // Then: it should be false and return 0
        assertEquals(Integer.valueOf(0), var.getValue());
    }

    @Test
    void shouldSetValueFromBoolean() {
        // Given: a BooleanVariable
        BooleanVariable var = new BooleanVariable(false);

        // When: setting value from boolean
        var.setValue(true);

        // Then: it should return 1
        assertEquals(Integer.valueOf(1), var.getValue());
    }

    @Test
    void shouldSetValueFromChar() {
        // Given: a BooleanVariable
        BooleanVariable var = new BooleanVariable(true);

        // When: setting value from char '0'
        var.setValue('0');

        // Then: it should be true and return 1
        assertEquals(Integer.valueOf(1), var.getValue());
    }

    @Test
    void shouldSetValueFromNonZeroChar() {
        // Given: a BooleanVariable
        BooleanVariable var = new BooleanVariable(true);

        // When: setting value from non-zero char
        var.setValue('1');

        // Then: it should be false and return 0
        assertEquals(Integer.valueOf(0), var.getValue());
    }

    @Test
    void shouldSetValueFromByte() {
        // Given: a BooleanVariable
        BooleanVariable var = new BooleanVariable(true);

        // When: setting value from byte 0
        var.setValue((byte) 0);

        // Then: it should be true and return 1
        assertEquals(Integer.valueOf(1), var.getValue());
    }

    @Test
    void shouldSetValueFromNonZeroByte() {
        // Given: a BooleanVariable
        BooleanVariable var = new BooleanVariable(true);

        // When: setting value from non-zero byte
        var.setValue((byte) 5);

        // Then: it should be false and return 0
        assertEquals(Integer.valueOf(0), var.getValue());
    }

    @Test
    void shouldSetValueFromDouble() {
        // Given: a BooleanVariable
        BooleanVariable var = new BooleanVariable(true);

        // When: setting value from double 0
        var.setValue(0.0);

        // Then: it should be true and return 1
        assertEquals(Integer.valueOf(1), var.getValue());
    }

    @Test
    void shouldSetValueFromNonZeroDouble() {
        // Given: a BooleanVariable
        BooleanVariable var = new BooleanVariable(true);

        // When: setting value from non-zero double
        var.setValue(3.14);

        // Then: it should be false and return 0
        assertEquals(Integer.valueOf(0), var.getValue());
    }

    @Test
    void shouldSetValueFromFloat() {
        // Given: a BooleanVariable
        BooleanVariable var = new BooleanVariable(true);

        // When: setting value from float 0
        var.setValue(0.0f);

        // Then: it should be true and return 1
        assertEquals(Integer.valueOf(1), var.getValue());
    }

    @Test
    void shouldSetValueFromNonZeroFloat() {
        // Given: a BooleanVariable
        BooleanVariable var = new BooleanVariable(true);

        // When: setting value from non-zero float
        var.setValue(2.5f);

        // Then: it should be false and return 0
        assertEquals(Integer.valueOf(0), var.getValue());
    }

    @Test
    void shouldSetValueFromLong() {
        // Given: a BooleanVariable
        BooleanVariable var = new BooleanVariable(true);

        // When: setting value from long 0
        var.setValue(0L);

        // Then: it should be true and return 1
        assertEquals(Integer.valueOf(1), var.getValue());
    }

    @Test
    void shouldSetValueFromNonZeroLong() {
        // Given: a BooleanVariable
        BooleanVariable var = new BooleanVariable(true);

        // When: setting value from non-zero long
        var.setValue(100L);

        // Then: it should be false and return 0
        assertEquals(Integer.valueOf(0), var.getValue());
    }

    @Test
    void shouldSetValueFromShort() {
        // Given: a BooleanVariable
        BooleanVariable var = new BooleanVariable(true);

        // When: setting value from short 0
        var.setValue((short) 0);

        // Then: it should be true and return 1
        assertEquals(Integer.valueOf(1), var.getValue());
    }

    @Test
    void shouldSetValueFromNonZeroShort() {
        // Given: a BooleanVariable
        BooleanVariable var = new BooleanVariable(true);

        // When: setting value from non-zero short
        var.setValue((short) 10);

        // Then: it should be false and return 0
        assertEquals(Integer.valueOf(0), var.getValue());
    }

    @Test
    void shouldReturnCorrectToString_whenTrue() {
        // Given: a true BooleanVariable
        BooleanVariable var = new BooleanVariable(true);

        // When: calling toString
        String result = var.toString();

        // Then: it should return "true"
        assertEquals("true", result);
    }

    @Test
    void shouldReturnCorrectToString_whenFalse() {
        // Given: a false BooleanVariable
        BooleanVariable var = new BooleanVariable(false);

        // When: calling toString
        String result = var.toString();

        // Then: it should return "false"
        assertEquals("false", result);
    }

    @Test
    void shouldCompareEqualVariables() {
        // Given: two BooleanVariables with same value
        BooleanVariable var1 = new BooleanVariable(true);
        BooleanVariable var2 = new BooleanVariable(true);

        // When: comparing them
        boolean result = var1.equals(var2);

        // Then: they should be equal
        assertTrue(result);
    }

    @Test
    void shouldCompareDifferentVariables() {
        // Given: two BooleanVariables with different values
        BooleanVariable var1 = new BooleanVariable(true);
        BooleanVariable var2 = new BooleanVariable(false);

        // When: comparing them
        boolean result = var1.equals(var2);

        // Then: they should not be equal
        assertFalse(result);
    }

    @Test
    void shouldToggleValue_whenSetMultipleTimes() {
        // Given: a BooleanVariable
        BooleanVariable var = new BooleanVariable(false);

        // When: toggling the value multiple times
        var.setValue(true);
        assertEquals(Integer.valueOf(1), var.getValue());
        var.setValue(false);
        assertEquals(Integer.valueOf(0), var.getValue());
        var.setValue(true);

        // Then: it should have the final value
        assertEquals(Integer.valueOf(1), var.getValue());
    }
}
