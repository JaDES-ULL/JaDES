package es.ull.simulation.variable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the LongVariable class
 */
class LongVariableTest {

    @Test
    void shouldCreateVariableFromLong() {
        // Given: a long value
        // When: creating a LongVariable
        LongVariable variable = new LongVariable(1000000L);

        // Then: it should return the correct value
        assertEquals(Long.valueOf(1000000L), variable.getValue());
    }

    @Test
    void shouldCreateVariableFromDouble() {
        // Given: a double value
        // When: creating a LongVariable
        LongVariable variable = new LongVariable(999999.7);

        // Then: it should truncate to long
        assertEquals(Long.valueOf(999999L), variable.getValue());
    }

    @Test
    void shouldCreateVariableFromZero() {
        // Given: zero value
        // When: creating a LongVariable
        LongVariable variable = new LongVariable(0L);

        // Then: it should store zero
        assertEquals(Long.valueOf(0L), variable.getValue());
    }

    @Test
    void shouldCreateVariableFromNegative() {
        // Given: a negative long value
        // When: creating a LongVariable
        LongVariable variable = new LongVariable(-5000000L);

        // Then: it should store the negative value
        assertEquals(Long.valueOf(-5000000L), variable.getValue());
    }

    @Test
    void shouldSetValueFromInt() {
        // Given: a LongVariable
        LongVariable variable = new LongVariable(100L);

        // When: setting value from int
        variable.setValue(50000);

        // Then: it should convert to long
        assertEquals(Long.valueOf(50000L), variable.getValue());
    }

    @Test
    void shouldSetValueFromTrue() {
        // Given: a LongVariable
        LongVariable variable = new LongVariable(100L);

        // When: setting value from boolean true
        variable.setValue(true);

        // Then: it should be 0
        assertEquals(Long.valueOf(0L), variable.getValue());
    }

    @Test
    void shouldSetValueFromFalse() {
        // Given: a LongVariable
        LongVariable variable = new LongVariable(100L);

        // When: setting value from boolean false
        variable.setValue(false);

        // Then: it should be 1
        assertEquals(Long.valueOf(1L), variable.getValue());
    }

    @Test
    void shouldSetValueFromChar() {
        // Given: a LongVariable
        LongVariable variable = new LongVariable(100L);

        // When: setting value from char 'A' (ASCII 65)
        variable.setValue('A');

        // Then: it should convert to long
        assertEquals(Long.valueOf(65L), variable.getValue());
    }

    @Test
    void shouldSetValueFromByte() {
        // Given: a LongVariable
        LongVariable variable = new LongVariable(100L);

        // When: setting value from byte
        variable.setValue((byte) 127);

        // Then: it should convert to long
        assertEquals(Long.valueOf(127L), variable.getValue());
    }

    @Test
    void shouldSetValueFromDouble() {
        // Given: a LongVariable
        LongVariable variable = new LongVariable(100L);

        // When: setting value from double
        variable.setValue(9999999.99);

        // Then: it should truncate to long
        assertEquals(Long.valueOf(9999999L), variable.getValue());
    }

    @Test
    void shouldSetValueFromFloat() {
        // Given: a LongVariable
        LongVariable variable = new LongVariable(100L);

        // When: setting value from float
        variable.setValue(150000.5f);

        // Then: it should truncate to long
        assertEquals(Long.valueOf(150000L), variable.getValue());
    }

    @Test
    void shouldSetValueFromLong() {
        // Given: a LongVariable
        LongVariable variable = new LongVariable(100L);

        // When: setting value from long
        variable.setValue(10000000L);

        // Then: it should update the value
        assertEquals(Long.valueOf(10000000L), variable.getValue());
    }

    @Test
    void shouldSetValueFromShort() {
        // Given: a LongVariable
        LongVariable variable = new LongVariable(100L);

        // When: setting value from short
        variable.setValue((short) 5000);

        // Then: it should convert to long
        assertEquals(Long.valueOf(5000L), variable.getValue());
    }

    @Test
    void shouldSetValueFromNumber() {
        // Given: a LongVariable
        LongVariable variable = new LongVariable(100L);

        // When: setting value from Number object
        variable.setValue(Long.valueOf(333333L));

        // Then: it should update the value
        assertEquals(Long.valueOf(333333L), variable.getValue());
    }

    @Test
    void shouldSetValueFromObject() {
        // Given: a LongVariable
        LongVariable variable = new LongVariable(100L);

        // When: setting value from Object (Number)
        variable.setValue((Object) Long.valueOf(777777L));

        // Then: it should update the value
        assertEquals(Long.valueOf(777777L), variable.getValue());
    }

    @Test
    void shouldReturnCorrectToString() {
        // Given: a LongVariable with value 123456
        LongVariable variable = new LongVariable(123456L);

        // When: calling toString
        String result = variable.toString();

        // Then: it should return "123456"
        assertEquals("123456", result);
    }

    @Test
    void shouldCompareEqualVariables() {
        // Given: two LongVariables with same value
        LongVariable var1 = new LongVariable(123456L);
        LongVariable var2 = new LongVariable(123456L);

        // When: comparing them
        // Note: LongVariable has a typo - method is "equas" not "equals"
        assertTrue(var1.equas(var2));
    }

    @Test
    void shouldCompareDifferentVariables() {
        // Given: two LongVariables with different values
        LongVariable var1 = new LongVariable(123456L);
        LongVariable var2 = new LongVariable(654321L);

        // When: checking their values
        // Then: they should not be equal
        assertFalse(var1.equas(var2));
    }

    @Test
    void shouldHandleMaxValue() {
        // Given: maximum long value
        // When: creating a LongVariable
        LongVariable variable = new LongVariable(Long.MAX_VALUE);

        // Then: it should store the max value
        assertEquals(Long.valueOf(Long.MAX_VALUE), variable.getValue());
    }

    @Test
    void shouldHandleMinValue() {
        // Given: minimum long value
        // When: creating a LongVariable
        LongVariable variable = new LongVariable(Long.MIN_VALUE);

        // Then: it should store the min value
        assertEquals(Long.valueOf(Long.MIN_VALUE), variable.getValue());
    }

    @Test
    void shouldHandleVeryLargeNumber() {
        // Given: a very large long value
        // When: creating a LongVariable
        LongVariable variable = new LongVariable(999999999999L);

        // Then: it should store the large value
        assertEquals(Long.valueOf(999999999999L), variable.getValue());
    }

    @Test
    void shouldUpdateValue_whenSetMultipleTimes() {
        // Given: a LongVariable
        LongVariable variable = new LongVariable(100L);

        // When: setting value multiple times
        variable.setValue(200L);
        assertEquals(Long.valueOf(200L), variable.getValue());
        variable.setValue(300L);
        assertEquals(Long.valueOf(300L), variable.getValue());
        variable.setValue(400L);

        // Then: it should have the final value
        assertEquals(Long.valueOf(400L), variable.getValue());
    }
}
