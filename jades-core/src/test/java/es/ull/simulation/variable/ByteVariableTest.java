package es.ull.simulation.variable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the ByteVariable class
 */
class ByteVariableTest {

    @Test
    void shouldCreateVariableFromByte() {
        // Given: a byte value
        // When: creating a ByteVariable
        ByteVariable variable = new ByteVariable((byte) 42);

        // Then: it should return the correct value
        assertEquals(Byte.valueOf((byte) 42), variable.getValue());
    }

    @Test
    void shouldCreateVariableFromDouble() {
        // Given: a double value
        // When: creating a ByteVariable
        ByteVariable variable = new ByteVariable(99.7);

        // Then: it should truncate to byte
        assertEquals(Byte.valueOf((byte) 99), variable.getValue());
    }

    @Test
    void shouldCreateVariableFromZero() {
        // Given: zero value
        // When: creating a ByteVariable
        ByteVariable variable = new ByteVariable((byte) 0);

        // Then: it should store zero
        assertEquals(Byte.valueOf((byte) 0), variable.getValue());
    }

    @Test
    void shouldCreateVariableFromNegative() {
        // Given: a negative byte value
        // When: creating a ByteVariable
        ByteVariable variable = new ByteVariable((byte) -100);

        // Then: it should store the negative value
        assertEquals(Byte.valueOf((byte) -100), variable.getValue());
    }

    @Test
    void shouldSetValueFromInt() {
        // Given: a ByteVariable
        ByteVariable variable = new ByteVariable((byte) 10);

        // When: setting value from int
        variable.setValue(50);

        // Then: it should convert to byte
        assertEquals(Byte.valueOf((byte) 50), variable.getValue());
    }

    @Test
    void shouldSetValueFromTrue() {
        // Given: a ByteVariable
        ByteVariable variable = new ByteVariable((byte) 10);

        // When: setting value from boolean true
        variable.setValue(true);

        // Then: it should be 0
        assertEquals(Byte.valueOf((byte) 0), variable.getValue());
    }

    @Test
    void shouldSetValueFromFalse() {
        // Given: a ByteVariable
        ByteVariable variable = new ByteVariable((byte) 10);

        // When: setting value from boolean false
        variable.setValue(false);

        // Then: it should be 1
        assertEquals(Byte.valueOf((byte) 1), variable.getValue());
    }

    @Test
    void shouldSetValueFromChar() {
        // Given: a ByteVariable
        ByteVariable variable = new ByteVariable((byte) 10);

        // When: setting value from char 'A' (ASCII 65)
        variable.setValue('A');

        // Then: it should convert to byte (truncated)
        assertEquals(Byte.valueOf((byte) 65), variable.getValue());
    }

    @Test
    void shouldSetValueFromByte() {
        // Given: a ByteVariable
        ByteVariable variable = new ByteVariable((byte) 10);

        // When: setting value from byte
        variable.setValue((byte) 127);

        // Then: it should update the value
        assertEquals(Byte.valueOf((byte) 127), variable.getValue());
    }

    @Test
    void shouldSetValueFromDouble() {
        // Given: a ByteVariable
        ByteVariable variable = new ByteVariable((byte) 10);

        // When: setting value from double
        variable.setValue(99.99);

        // Then: it should truncate to byte
        assertEquals(Byte.valueOf((byte) 99), variable.getValue());
    }

    @Test
    void shouldSetValueFromFloat() {
        // Given: a ByteVariable
        ByteVariable variable = new ByteVariable((byte) 10);

        // When: setting value from float
        variable.setValue(15.5f);

        // Then: it should truncate to byte
        assertEquals(Byte.valueOf((byte) 15), variable.getValue());
    }

    @Test
    void shouldSetValueFromLong() {
        // Given: a ByteVariable
        ByteVariable variable = new ByteVariable((byte) 10);

        // When: setting value from long
        variable.setValue(100L);

        // Then: it should convert to byte
        assertEquals(Byte.valueOf((byte) 100), variable.getValue());
    }

    @Test
    void shouldSetValueFromShort() {
        // Given: a ByteVariable
        ByteVariable variable = new ByteVariable((byte) 10);

        // When: setting value from short
        variable.setValue((short) 50);

        // Then: it should convert to byte
        assertEquals(Byte.valueOf((byte) 50), variable.getValue());
    }

    @Test
    void shouldSetValueFromNumber() {
        // Given: a ByteVariable
        ByteVariable variable = new ByteVariable((byte) 10);

        // When: setting value from Number object
        variable.setValue(Byte.valueOf((byte) 33));

        // Then: it should update the value
        assertEquals(Byte.valueOf((byte) 33), variable.getValue());
    }

    @Test
    void shouldSetValueFromObject() {
        // Given: a ByteVariable
        ByteVariable variable = new ByteVariable((byte) 10);

        // When: setting value from Object (Number)
        variable.setValue((Object) Byte.valueOf((byte) 77));

        // Then: it should update the value
        assertEquals(Byte.valueOf((byte) 77), variable.getValue());
    }

    @Test
    void shouldReturnCorrectToString() {
        // Given: a ByteVariable with value 42
        ByteVariable variable = new ByteVariable((byte) 42);

        // When: calling toString
        String result = variable.toString();

        // Then: it should return "42"
        assertEquals("42", result);
    }

    @Test
    void shouldCompareEqualVariables() {
        // Given: two ByteVariables with same value
        ByteVariable var1 = new ByteVariable((byte) 42);
        ByteVariable var2 = new ByteVariable((byte) 42);

        // When: comparing them
        boolean result = var1.equals(var2);

        // Then: they should be equal
        assertTrue(result);
    }

    @Test
    void shouldCompareDifferentVariables() {
        // Given: two ByteVariables with different values
        ByteVariable var1 = new ByteVariable((byte) 42);
        ByteVariable var2 = new ByteVariable((byte) 100);

        // When: comparing them
        boolean result = var1.equals(var2);

        // Then: they should not be equal
        assertFalse(result);
    }

    @Test
    void shouldHandleMaxValue() {
        // Given: maximum byte value (127)
        // When: creating a ByteVariable
        ByteVariable variable = new ByteVariable(Byte.MAX_VALUE);

        // Then: it should store the max value
        assertEquals(Byte.valueOf(Byte.MAX_VALUE), variable.getValue());
    }

    @Test
    void shouldHandleMinValue() {
        // Given: minimum byte value (-128)
        // When: creating a ByteVariable
        ByteVariable variable = new ByteVariable(Byte.MIN_VALUE);

        // Then: it should store the min value
        assertEquals(Byte.valueOf(Byte.MIN_VALUE), variable.getValue());
    }

    @Test
    void shouldHandleOverflow() {
        // Given: a ByteVariable
        // When: setting value that exceeds byte range (256 wraps to 0)
        ByteVariable variable = new ByteVariable((byte) 10);
        variable.setValue(256);

        // Then: it should wrap around
        assertEquals(Byte.valueOf((byte) 0), variable.getValue());
    }

    @Test
    void shouldUpdateValue_whenSetMultipleTimes() {
        // Given: a ByteVariable
        ByteVariable variable = new ByteVariable((byte) 1);

        // When: setting value multiple times
        variable.setValue((byte) 2);
        assertEquals(Byte.valueOf((byte) 2), variable.getValue());
        variable.setValue((byte) 3);
        assertEquals(Byte.valueOf((byte) 3), variable.getValue());
        variable.setValue((byte) 4);

        // Then: it should have the final value
        assertEquals(Byte.valueOf((byte) 4), variable.getValue());
    }
}
