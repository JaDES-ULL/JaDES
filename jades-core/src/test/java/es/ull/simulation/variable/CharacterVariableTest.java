package es.ull.simulation.variable;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the CharacterVariable class
 */
class CharacterVariableTest {

    @Test
    void shouldCreateVariableFromCharacter() {
        // Given: a Character value
        // When: creating a CharacterVariable
        CharacterVariable variable = new CharacterVariable('A');

        // Then: it should return character count (always 1)
        assertEquals(Integer.valueOf(1), variable.getValue());
    }

    @Test
    void shouldCreateVariableFromLowercaseLetter() {
        // Given: a lowercase letter
        // When: creating a CharacterVariable
        CharacterVariable variable = new CharacterVariable('z');

        // Then: it should store the character
        assertEquals(Integer.valueOf(1), variable.getValue());
        assertEquals("z", variable.toString());
    }

    @Test
    void shouldCreateVariableFromDigit() {
        // Given: a digit character
        // When: creating a CharacterVariable
        CharacterVariable variable = new CharacterVariable('5');

        // Then: it should store the digit
        assertEquals("5", variable.toString());
    }

    @Test
    void shouldCreateVariableFromSpecialChar() {
        // Given: a special character
        // When: creating a CharacterVariable
        CharacterVariable variable = new CharacterVariable('$');

        // Then: it should store the special char
        assertEquals("$", variable.toString());
    }

    @Test
    void shouldSetValueFromObject() {
        // Given: a CharacterVariable
        CharacterVariable variable = new CharacterVariable('A');

        // When: setting value from Character object
        variable.setValue(Character.valueOf('Z'));

        // Then: it should update the value
        assertEquals("Z", variable.toString());
    }

    @Test
    void shouldSetValueFromInt() {
        // Given: a CharacterVariable
        // When: setting value from int (ASCII 65 = 'A')
        CharacterVariable variable = new CharacterVariable('x');
        variable.setValue(65);

        // Then: it should convert to character
        assertEquals("A", variable.toString());
    }

    @Test
    void shouldSetValueFromTrue() {
        // Given: a CharacterVariable
        CharacterVariable variable = new CharacterVariable('A');

        // When: setting value from boolean true
        variable.setValue(true);

        // Then: it should be '0'
        assertEquals("0", variable.toString());
    }

    @Test
    void shouldSetValueFromFalse() {
        // Given: a CharacterVariable
        CharacterVariable variable = new CharacterVariable('A');

        // When: setting value from boolean false
        variable.setValue(false);

        // Then: it should be '1'
        assertEquals("1", variable.toString());
    }

    @Test
    void shouldSetValueFromChar() {
        // Given: a CharacterVariable
        CharacterVariable variable = new CharacterVariable('A');

        // When: setting value from char
        variable.setValue('B');

        // Then: it should update the character
        assertEquals("B", variable.toString());
    }

    @Test
    void shouldSetValueFromByte() {
        // Given: a CharacterVariable
        // When: setting value from byte (66 = 'B')
        CharacterVariable variable = new CharacterVariable('A');
        variable.setValue((byte) 66);

        // Then: it should convert to character
        assertEquals("B", variable.toString());
    }

    @Test
    void shouldSetValueFromDouble() {
        // Given: a CharacterVariable
        // When: setting value from double (67.9 truncates to 67 = 'C')
        CharacterVariable variable = new CharacterVariable('A');
        variable.setValue(67.9);

        // Then: it should truncate and convert to character
        assertEquals("C", variable.toString());
    }

    @Test
    void shouldSetValueFromFloat() {
        // Given: a CharacterVariable
        // When: setting value from float (68.5 truncates to 68 = 'D')
        CharacterVariable variable = new CharacterVariable('A');
        variable.setValue(68.5f);

        // Then: it should truncate and convert to character
        assertEquals("D", variable.toString());
    }

    @Test
    void shouldSetValueFromLong() {
        // Given: a CharacterVariable
        // When: setting value from long (69 = 'E')
        CharacterVariable variable = new CharacterVariable('A');
        variable.setValue(69L);

        // Then: it should convert to character
        assertEquals("E", variable.toString());
    }

    @Test
    void shouldSetValueFromShort() {
        // Given: a CharacterVariable
        // When: setting value from short (70 = 'F')
        CharacterVariable variable = new CharacterVariable('A');
        variable.setValue((short) 70);

        // Then: it should convert to character
        assertEquals("F", variable.toString());
    }

    @Test
    void shouldReturnCorrectToString() {
        // Given: a CharacterVariable with '@'
        CharacterVariable variable = new CharacterVariable('@');

        // When: calling toString
        String result = variable.toString();

        // Then: it should return "@"
        assertEquals("@", result);
    }

    @Test
    void shouldHandleSpace() {
        // Given: a space character
        // When: creating a CharacterVariable
        CharacterVariable variable = new CharacterVariable(' ');

        // Then: it should store the space
        assertEquals(" ", variable.toString());
    }

    @Test
    void shouldHandleNewline() {
        // Given: a newline character
        // When: creating a CharacterVariable
        CharacterVariable variable = new CharacterVariable('\n');

        // Then: it should store the newline
        assertEquals("\n", variable.toString());
    }

    @Test
    void shouldHandleTab() {
        // Given: a tab character
        // When: creating a CharacterVariable
        CharacterVariable variable = new CharacterVariable('\t');

        // Then: it should store the tab
        assertEquals("\t", variable.toString());
    }

    @Test
    void shouldHandleZeroChar() {
        // Given: null character (ASCII 0)
        // When: creating a CharacterVariable
        CharacterVariable variable = new CharacterVariable('\0');

        // Then: it should store the null char
        assertEquals("\0", variable.toString());
    }

    @Test
    void shouldUpdateValue_whenSetMultipleTimes() {
        // Given: a CharacterVariable
        CharacterVariable variable = new CharacterVariable('A');

        // When: setting value multiple times
        variable.setValue('B');
        assertEquals("B", variable.toString());
        variable.setValue('C');
        assertEquals("C", variable.toString());
        variable.setValue('D');

        // Then: it should have the final value
        assertEquals("D", variable.toString());
    }

    @Test
    void shouldHandleUnicodeCharacter() {
        // Given: a Unicode character
        // When: creating a CharacterVariable
        CharacterVariable variable = new CharacterVariable('€');

        // Then: it should store the Unicode character
        assertEquals("€", variable.toString());
    }
}
