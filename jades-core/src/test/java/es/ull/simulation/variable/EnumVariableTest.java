package es.ull.simulation.variable;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EnumVariableTest {

    private EnumType colorType;
    private EnumVariable enumVariable;

    @BeforeEach
    void setUp() {
        colorType = new EnumType("RED", "GREEN", "BLUE");
        enumVariable = new EnumVariable(colorType, 0);
    }

    @Test
    void shouldCreateEnumVariable_withIntegerValue() {
        EnumVariable variable = new EnumVariable(colorType, Integer.valueOf(1));

        assertNotNull(variable);
        assertEquals(1, variable.getValue());
    }

    @Test
    void shouldCreateEnumVariable_withDoubleValue() {
        EnumVariable variable = new EnumVariable(colorType, Double.valueOf(2.7));

        assertNotNull(variable);
        assertEquals(2, variable.getValue());
    }

    @Test
    void shouldGetValue_returnsCorrectValue() {
        Number value = enumVariable.getValue();

        assertEquals(0, value.intValue());
    }

    @Test
    void shouldGetValue_returnsMinus1_whenValueExceedsMaxValue() {
        enumVariable.setValue(10); // Exceeds maxValue of 3

        Number value = enumVariable.getValue();

        assertEquals(-1, value.intValue());
    }

    @Test
    void shouldSetValue_withStringValue() {
        enumVariable.setValue("GREEN");

        assertEquals(1, enumVariable.getValue().intValue());
        assertEquals("GREEN", enumVariable.toString());
    }

    @Test
    void shouldSetValue_withNonExistentString() {
        enumVariable.setValue("YELLOW");

        assertEquals(-1, enumVariable.getValue().intValue());
    }

    @Test
    void shouldSetValue_withIntegerObject() {
        enumVariable.setValue((Object) Integer.valueOf(2));

        assertEquals(2, enumVariable.getValue().intValue());
    }

    @Test
    void shouldSetValue_withIntPrimitive() {
        enumVariable.setValue(2);

        assertEquals(2, enumVariable.getValue().intValue());
    }

    @Test
    void shouldSetValue_withBooleanTrue() {
        enumVariable.setValue(true);

        assertEquals(0, enumVariable.getValue().intValue());
    }

    @Test
    void shouldSetValue_withBooleanFalse() {
        enumVariable.setValue(false);

        assertEquals(1, enumVariable.getValue().intValue());
    }

    @Test
    void shouldSetValue_withChar() {
        enumVariable.setValue((char) 2);

        assertEquals(2, enumVariable.getValue().intValue());
    }

    @Test
    void shouldSetValue_withByte() {
        enumVariable.setValue((byte) 2);

        assertEquals(2, enumVariable.getValue().intValue());
    }

    @Test
    void shouldSetValue_withDouble() {
        enumVariable.setValue(2.9);

        assertEquals(2, enumVariable.getValue().intValue());
    }

    @Test
    void shouldSetValue_withFloat() {
        enumVariable.setValue(1.8f);

        assertEquals(1, enumVariable.getValue().intValue());
    }

    @Test
    void shouldSetValue_withLong() {
        enumVariable.setValue(2L);

        assertEquals(2, enumVariable.getValue().intValue());
    }

    @Test
    void shouldSetValue_withShort() {
        enumVariable.setValue((short) 1);

        assertEquals(1, enumVariable.getValue().intValue());
    }

    @Test
    void shouldEquals_returnTrue_forEqualValues() {
        EnumVariable other = new EnumVariable(colorType, 0);

        assertTrue(enumVariable.equals(other));
    }

    @Test
    void shouldEquals_returnFalse_forDifferentValues() {
        EnumVariable other = new EnumVariable(colorType, 1);

        assertFalse(enumVariable.equals(other));
    }

    @Test
    void shouldToString_returnStringRepresentation() {
        enumVariable.setValue(1);

        String result = enumVariable.toString();

        assertEquals("GREEN", result);
    }

    @Test
    void shouldToString_returnCorrectValue_afterSettingWithString() {
        enumVariable.setValue("BLUE");

        assertEquals("BLUE", enumVariable.toString());
    }

    @Test
    void shouldHandleMultipleSetValueCalls() {
        enumVariable.setValue(1);
        assertEquals(1, enumVariable.getValue().intValue());

        enumVariable.setValue("RED");
        assertEquals(0, enumVariable.getValue().intValue());

        enumVariable.setValue(2.5);
        assertEquals(2, enumVariable.getValue().intValue());
    }

    @Test
    void shouldCreateEnumVariable_withZeroValue() {
        EnumVariable variable = new EnumVariable(colorType, 0);

        assertEquals(0, variable.getValue().intValue());
        assertEquals("RED", variable.toString());
    }

    @Test
    void shouldSetValue_withStringFromType() {
        enumVariable.setValue("RED");
        assertEquals("RED", enumVariable.toString());

        enumVariable.setValue("GREEN");
        assertEquals("GREEN", enumVariable.toString());

        enumVariable.setValue("BLUE");
        assertEquals("BLUE", enumVariable.toString());
    }

    @Test
    void shouldGetValue_withParameters() {
        Number value = enumVariable.getValue("param1", "param2");

        assertEquals(0, value.intValue());
    }

    @Test
    void shouldHandleLargeEnumType() {
        EnumType largeType = new EnumType("V1", "V2", "V3", "V4", "V5", "V6", "V7", "V8", "V9", "V10");
        EnumVariable largeVariable = new EnumVariable(largeType, 5);

        assertEquals(5, largeVariable.getValue().intValue());
    }

    @Test
    void shouldSetValue_withNegativeInt() {
        enumVariable.setValue(-1);

        assertEquals(-1, enumVariable.getValue().intValue());
    }
}
