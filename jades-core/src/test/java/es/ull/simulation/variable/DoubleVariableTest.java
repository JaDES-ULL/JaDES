package es.ull.simulation.variable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the DoubleVariable class
 */
class DoubleVariableTest {

    @Test
    void shouldCreateVariableFromDouble() {
        // Given: a double value
        // When: creating a DoubleVariable
        DoubleVariable variable = new DoubleVariable(3.14159);

        // Then: it should return the correct value
        assertEquals(Double.valueOf(3.14159), variable.getValue());
    }

    @Test
    void shouldCreateVariableFromZero() {
        // Given: zero value
        // When: creating a DoubleVariable
        DoubleVariable variable = new DoubleVariable(0.0);

        // Then: it should store zero
        assertEquals(Double.valueOf(0.0), variable.getValue());
    }

    @Test
    void shouldCreateVariableFromNegative() {
        // Given: a negative double value
        // When: creating a DoubleVariable
        DoubleVariable variable = new DoubleVariable(-99.99);

        // Then: it should store the negative value
        assertEquals(Double.valueOf(-99.99), variable.getValue());
    }

    @Test
    void shouldSetValueFromInt() {
        // Given: a DoubleVariable
        DoubleVariable variable = new DoubleVariable(10.5);

        // When: setting value from int
        variable.setValue(42);

        // Then: it should convert to double
        assertEquals(Double.valueOf(42.0), variable.getValue());
    }

    @Test
    void shouldSetValueFromTrue() {
        // Given: a DoubleVariable
        DoubleVariable variable = new DoubleVariable(10.0);

        // When: setting value from boolean true
        variable.setValue(true);

        // Then: it should be 0
        assertEquals(Double.valueOf(0.0), variable.getValue());
    }

    @Test
    void shouldSetValueFromFalse() {
        // Given: a DoubleVariable
        DoubleVariable variable = new DoubleVariable(10.0);

        // When: setting value from boolean false
        variable.setValue(false);

        // Then: it should be 1
        assertEquals(Double.valueOf(1.0), variable.getValue());
    }

    @Test
    void shouldSetValueFromChar() {
        // Given: a DoubleVariable
        DoubleVariable variable = new DoubleVariable(10.0);

        // When: setting value from char 'A' (ASCII 65)
        variable.setValue('A');

        // Then: it should convert to double of ASCII value
        assertEquals(Double.valueOf(65.0), variable.getValue());
    }

    @Test
    void shouldSetValueFromByte() {
        // Given: a DoubleVariable
        DoubleVariable variable = new DoubleVariable(10.0);

        // When: setting value from byte
        variable.setValue((byte) 127);

        // Then: it should convert to double
        assertEquals(Double.valueOf(127.0), variable.getValue());
    }

    @Test
    void shouldSetValueFromDouble() {
        // Given: a DoubleVariable
        DoubleVariable variable = new DoubleVariable(10.0);

        // When: setting value from double
        variable.setValue(99.99);

        // Then: it should update the value
        assertEquals(Double.valueOf(99.99), variable.getValue());
    }

    @Test
    void shouldSetValueFromFloat() {
        // Given: a DoubleVariable
        DoubleVariable variable = new DoubleVariable(10.0);

        // When: setting value from float
        variable.setValue(15.5f);

        // Then: it should convert to double
        assertEquals(Double.valueOf(15.5), variable.getValue());
    }

    @Test
    void shouldSetValueFromLong() {
        // Given: a DoubleVariable
        DoubleVariable variable = new DoubleVariable(10.0);

        // When: setting value from long
        variable.setValue(1000000L);

        // Then: it should convert to double
        assertEquals(Double.valueOf(1000000.0), variable.getValue());
    }

    @Test
    void shouldSetValueFromShort() {
        // Given: a DoubleVariable
        DoubleVariable variable = new DoubleVariable(10.0);

        // When: setting value from short
        variable.setValue((short) 500);

        // Then: it should convert to double
        assertEquals(Double.valueOf(500.0), variable.getValue());
    }

    @Test
    void shouldSetValueFromNumber() {
        // Given: a DoubleVariable
        DoubleVariable variable = new DoubleVariable(10.0);

        // When: setting value from Number object
        variable.setValue(Double.valueOf(3.14159));

        // Then: it should update the value
        assertEquals(Double.valueOf(3.14159), variable.getValue());
    }

    @Test
    void shouldSetValueFromObject() {
        // Given: a DoubleVariable
        DoubleVariable variable = new DoubleVariable(10.0);

        // When: setting value from Object (Number)
        variable.setValue((Object) Double.valueOf(2.71828));

        // Then: it should update the value
        assertEquals(Double.valueOf(2.71828), variable.getValue());
    }

    @Test
    void shouldReturnCorrectToString() {
        // Given: a DoubleVariable with value 3.14
        DoubleVariable variable = new DoubleVariable(3.14);

        // When: calling toString
        String result = variable.toString();

        // Then: it should return "3.14"
        assertEquals("3.14", result);
    }

    @Test
    void shouldCompareEqualVariables() {
        // Given: two DoubleVariables with same value
        DoubleVariable var1 = new DoubleVariable(3.14159);
        DoubleVariable var2 = new DoubleVariable(3.14159);

        // When: comparing them
        boolean result = var1.equals(var2);

        // Then: they should be equal
        assertTrue(result);
    }

    @Test
    void shouldCompareDifferentVariables() {
        // Given: two DoubleVariables with different values
        DoubleVariable var1 = new DoubleVariable(3.14);
        DoubleVariable var2 = new DoubleVariable(2.71);

        // When: comparing them
        boolean result = var1.equals(var2);

        // Then: they should not be equal
        assertFalse(result);
    }

    @Test
    void shouldHandleMaxValue() {
        // Given: maximum double value
        // When: creating a DoubleVariable
        DoubleVariable variable = new DoubleVariable(Double.MAX_VALUE);

        // Then: it should store the max value
        assertEquals(Double.valueOf(Double.MAX_VALUE), variable.getValue());
    }

    @Test
    void shouldHandleMinValue() {
        // Given: minimum double value
        // When: creating a DoubleVariable
        DoubleVariable variable = new DoubleVariable(Double.MIN_VALUE);

        // Then: it should store the min value
        assertEquals(Double.valueOf(Double.MIN_VALUE), variable.getValue());
    }

    @Test
    void shouldHandlePositiveInfinity() {
        // Given: positive infinity
        // When: creating a DoubleVariable
        DoubleVariable variable = new DoubleVariable(Double.POSITIVE_INFINITY);

        // Then: it should store positive infinity
        assertEquals(Double.valueOf(Double.POSITIVE_INFINITY), variable.getValue());
    }

    @Test
    void shouldHandleNegativeInfinity() {
        // Given: negative infinity
        // When: creating a DoubleVariable
        DoubleVariable variable = new DoubleVariable(Double.NEGATIVE_INFINITY);

        // Then: it should store negative infinity
        assertEquals(Double.valueOf(Double.NEGATIVE_INFINITY), variable.getValue());
    }

    @Test
    void shouldHandleVerySmallNumber() {
        // Given: a very small number
        // When: creating a DoubleVariable
        DoubleVariable variable = new DoubleVariable(0.000000001);

        // Then: it should store the small value
        assertEquals(Double.valueOf(0.000000001), variable.getValue());
    }

    @Test
    void shouldHandleVeryLargeNumber() {
        // Given: a very large number
        // When: creating a DoubleVariable
        DoubleVariable variable = new DoubleVariable(1.23456789e100);

        // Then: it should store the large value
        assertEquals(Double.valueOf(1.23456789e100), variable.getValue());
    }

    @Test
    void shouldUpdateValue_whenSetMultipleTimes() {
        // Given: a DoubleVariable
        DoubleVariable variable = new DoubleVariable(1.0);

        // When: setting value multiple times
        variable.setValue(2.0);
        assertEquals(Double.valueOf(2.0), variable.getValue());
        variable.setValue(3.0);
        assertEquals(Double.valueOf(3.0), variable.getValue());
        variable.setValue(4.0);

        // Then: it should have the final value
        assertEquals(Double.valueOf(4.0), variable.getValue());
    }

    @Test
    void shouldHandleNegativeZero() {
        // Given: negative zero
        // When: creating a DoubleVariable
        DoubleVariable variable = new DoubleVariable(-0.0);

        // Then: it should store negative zero
        assertEquals(Double.valueOf(-0.0), variable.getValue());
    }
}
