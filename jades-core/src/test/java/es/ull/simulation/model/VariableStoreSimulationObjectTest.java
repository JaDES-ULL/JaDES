package es.ull.simulation.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.ull.simulation.variable.*;

/**
 * Unit tests for {@link VariableStoreSimulationObject}.
 * Tests variable storage and retrieval capabilities with all primitive types.
 */
class VariableStoreSimulationObjectTest {

    private Simulation simulation;
    private TestVariableStoreObject testObject;

    /**
     * Concrete implementation of VariableStoreSimulationObject for testing purposes
     */
    private static class TestVariableStoreObject extends VariableStoreSimulationObject {
        public TestVariableStoreObject(Simulation simul, int id, String objectTypeId) {
            super(simul, id, objectTypeId);
        }

        @Override
        public void assignSimulation(es.ull.simulation.model.engine.SimulationEngine engine) {
            // Empty implementation for testing purposes
        }
    }

    @BeforeEach
    void setUp() {
        simulation = new Simulation(0, "Test Simulation");
        testObject = new TestVariableStoreObject(simulation, 1, "TEST");
    }

    // ==================== getVar Tests ====================

    @Test
    void shouldReturnNull_whenVariableDoesNotExist() {
        // Given: an object with no variables
        // When: getting a non-existent variable
        IVariable result = testObject.getVar("nonExistent");

        // Then: should return null
        assertNull(result);
    }

    @Test
    void shouldReturnVariable_whenVariableExists() {
        // Given: an object with a variable
        testObject.putVar("testVar", 42.5);

        // When: getting the variable
        IVariable result = testObject.getVar("testVar");

        // Then: should return the variable
        assertNotNull(result);
        assertTrue(result instanceof DoubleVariable);
        assertEquals(42.5, ((DoubleVariable) result).getValue());
    }

    // ==================== putVar(String, IVariable) Tests ====================

    @Test
    void shouldStoreIVariable_whenPutVarWithIVariable() {
        // Given: a custom variable
        IVariable customVar = new IntVariable(100);

        // When: storing the variable
        testObject.putVar("custom", customVar);

        // Then: should be retrievable
        IVariable retrieved = testObject.getVar("custom");
        assertNotNull(retrieved);
        assertSame(customVar, retrieved);
    }

    @Test
    void shouldReplaceExistingIVariable_whenPutVarWithSameName() {
        // Given: an existing variable
        testObject.putVar("var", new IntVariable(10));
        IVariable newVar = new IntVariable(20);

        // When: replacing with new variable
        testObject.putVar("var", newVar);

        // Then: should retrieve the new variable
        IVariable retrieved = testObject.getVar("var");
        assertSame(newVar, retrieved);
        assertEquals(20, ((IntVariable) retrieved).getValue());
    }

    // ==================== putVar(String, double) Tests ====================

    @Test
    void shouldCreateDoubleVariable_whenPutVarWithDoubleAndNoExisting() {
        // Given: no existing variable
        // When: storing a double value
        testObject.putVar("doubleVar", 3.14);

        // Then: should create a DoubleVariable
        IVariable result = testObject.getVar("doubleVar");
        assertNotNull(result);
        assertTrue(result instanceof DoubleVariable);
        assertEquals(3.14, ((DoubleVariable) result).getValue());
    }

    @Test
    void shouldUpdateExistingVariable_whenPutVarWithDoubleAndExisting() {
        // Given: an existing double variable
        testObject.putVar("doubleVar", 1.0);

        // When: updating with new double value
        testObject.putVar("doubleVar", 2.0);

        // Then: should update the variable
        IVariable result = testObject.getVar("doubleVar");
        assertEquals(2.0, ((DoubleVariable) result).getValue());
    }

    @Test
    void shouldHandleNegativeDouble_whenPutVarWithDouble() {
        // Given: a negative double value
        // When: storing negative double
        testObject.putVar("negative", -99.99);

        // Then: should store correctly
        DoubleVariable result = (DoubleVariable) testObject.getVar("negative");
        assertEquals(-99.99, result.getValue());
    }

    @Test
    void shouldHandleZeroDouble_whenPutVarWithDouble() {
        // Given: zero value
        // When: storing zero
        testObject.putVar("zero", 0.0);

        // Then: should store correctly
        DoubleVariable result = (DoubleVariable) testObject.getVar("zero");
        assertEquals(0.0, result.getValue());
    }

    // ==================== putVar(String, int) Tests ====================

    @Test
    void shouldCreateIntVariable_whenPutVarWithIntAndNoExisting() {
        // Given: no existing variable
        // When: storing an int value
        testObject.putVar("intVar", 42);

        // Then: should create an IntVariable
        IVariable result = testObject.getVar("intVar");
        assertNotNull(result);
        assertTrue(result instanceof IntVariable);
        assertEquals(42, ((IntVariable) result).getValue());
    }

    @Test
    void shouldUpdateExistingVariable_whenPutVarWithIntAndExisting() {
        // Given: an existing int variable
        testObject.putVar("intVar", 10);

        // When: updating with new int value
        testObject.putVar("intVar", 20);

        // Then: should update the variable
        IVariable result = testObject.getVar("intVar");
        assertEquals(20, ((IntVariable) result).getValue());
    }

    @Test
    void shouldHandleMaxInt_whenPutVarWithInt() {
        // Given: maximum int value
        // When: storing max int
        testObject.putVar("maxInt", Integer.MAX_VALUE);

        // Then: should store correctly
        IntVariable result = (IntVariable) testObject.getVar("maxInt");
        assertEquals(Integer.MAX_VALUE, result.getValue());
    }

    @Test
    void shouldHandleMinInt_whenPutVarWithInt() {
        // Given: minimum int value
        // When: storing min int
        testObject.putVar("minInt", Integer.MIN_VALUE);

        // Then: should store correctly
        IntVariable result = (IntVariable) testObject.getVar("minInt");
        assertEquals(Integer.MIN_VALUE, result.getValue());
    }

    // ==================== putVar(String, boolean) Tests ====================

    @Test
    void shouldCreateBooleanVariable_whenPutVarWithBooleanAndNoExisting() {
        // Given: no existing variable
        // When: storing a boolean value
        testObject.putVar("boolVar", true);

        // Then: should create a BooleanVariable
        IVariable result = testObject.getVar("boolVar");
        assertNotNull(result);
        assertTrue(result instanceof BooleanVariable);
        assertEquals(1, ((BooleanVariable) result).getValue());
    }

    @Test
    void shouldUpdateExistingVariable_whenPutVarWithBooleanAndExisting() {
        // Given: an existing boolean variable
        testObject.putVar("boolVar", false);

        // When: updating with new boolean value
        testObject.putVar("boolVar", true);

        // Then: should update the variable
        IVariable result = testObject.getVar("boolVar");
        assertEquals(1, ((BooleanVariable) result).getValue());
    }

    @Test
    void shouldStoreFalse_whenPutVarWithBoolean() {
        // Given: false value
        // When: storing false
        testObject.putVar("falseVar", false);

        // Then: should store correctly
        BooleanVariable result = (BooleanVariable) testObject.getVar("falseVar");
        assertEquals(0, result.getValue());
    }

    // ==================== putVar(String, char) Tests ====================

    @Test
    void shouldCreateCharacterVariable_whenPutVarWithCharAndNoExisting() {
        // Given: no existing variable
        // When: storing a char value
        testObject.putVar("charVar", 'A');

        // Then: should create a CharacterVariable
        // Note: CharacterVariable.getValue() returns Character.charCount() which is always 1 for normal chars
        IVariable result = testObject.getVar("charVar");
        assertNotNull(result);
        assertTrue(result instanceof CharacterVariable);
        assertEquals(1, ((CharacterVariable) result).getValue());
    }

    @Test
    void shouldUpdateExistingVariable_whenPutVarWithCharAndExisting() {
        // Given: an existing char variable
        testObject.putVar("charVar", 'X');

        // When: updating with new char value
        testObject.putVar("charVar", 'Y');

        // Then: should update the variable
        // Note: CharacterVariable.getValue() returns Character.charCount() which is always 1
        IVariable result = testObject.getVar("charVar");
        assertEquals(1, ((CharacterVariable) result).getValue());
        // Verify the actual character through toString()
        assertEquals("Y", result.toString());
    }

    @Test
    void shouldHandleSpecialChars_whenPutVarWithChar() {
        // Given: special characters
        // When: storing special chars
        testObject.putVar("newline", '\n');
        testObject.putVar("tab", '\t');
        testObject.putVar("space", ' ');

        // Then: should store correctly
        // Note: CharacterVariable.getValue() returns Character.charCount() which is always 1
        assertEquals(1, ((CharacterVariable) testObject.getVar("newline")).getValue());
        assertEquals(1, ((CharacterVariable) testObject.getVar("tab")).getValue());
        assertEquals(1, ((CharacterVariable) testObject.getVar("space")).getValue());
        // Verify actual characters through toString()
        assertEquals("\n", testObject.getVar("newline").toString());
        assertEquals("\t", testObject.getVar("tab").toString());
        assertEquals(" ", testObject.getVar("space").toString());
    }

    // ==================== putVar(String, byte) Tests ====================

    @Test
    void shouldCreateByteVariable_whenPutVarWithByteAndNoExisting() {
        // Given: no existing variable
        // When: storing a byte value
        testObject.putVar("byteVar", (byte) 100);

        // Then: should create a ByteVariable
        IVariable result = testObject.getVar("byteVar");
        assertNotNull(result);
        assertTrue(result instanceof ByteVariable);
        assertEquals((byte) 100, ((ByteVariable) result).getValue());
    }

    @Test
    void shouldUpdateExistingVariable_whenPutVarWithByteAndExisting() {
        // Given: an existing byte variable
        testObject.putVar("byteVar", (byte) 10);

        // When: updating with new byte value
        testObject.putVar("byteVar", (byte) 20);

        // Then: should update the variable
        IVariable result = testObject.getVar("byteVar");
        assertEquals((byte) 20, ((ByteVariable) result).getValue());
    }

    @Test
    void shouldHandleMaxByte_whenPutVarWithByte() {
        // Given: maximum byte value
        // When: storing max byte
        testObject.putVar("maxByte", Byte.MAX_VALUE);

        // Then: should store correctly
        ByteVariable result = (ByteVariable) testObject.getVar("maxByte");
        assertEquals(Byte.MAX_VALUE, result.getValue());
    }

    @Test
    void shouldHandleMinByte_whenPutVarWithByte() {
        // Given: minimum byte value
        // When: storing min byte
        testObject.putVar("minByte", Byte.MIN_VALUE);

        // Then: should store correctly
        ByteVariable result = (ByteVariable) testObject.getVar("minByte");
        assertEquals(Byte.MIN_VALUE, result.getValue());
    }

    // ==================== putVar(String, float) Tests ====================

    @Test
    void shouldCreateFloatVariable_whenPutVarWithFloatAndNoExisting() {
        // Given: no existing variable
        // When: storing a float value
        testObject.putVar("floatVar", 2.5f);

        // Then: should create a FloatVariable
        IVariable result = testObject.getVar("floatVar");
        assertNotNull(result);
        assertTrue(result instanceof FloatVariable);
        assertEquals(2.5f, ((FloatVariable) result).getValue());
    }

    @Test
    void shouldUpdateExistingVariable_whenPutVarWithFloatAndExisting() {
        // Given: an existing float variable
        testObject.putVar("floatVar", 1.0f);

        // When: updating with new float value
        testObject.putVar("floatVar", 2.0f);

        // Then: should update the variable
        IVariable result = testObject.getVar("floatVar");
        assertEquals(2.0f, ((FloatVariable) result).getValue());
    }

    @Test
    void shouldHandleNegativeFloat_whenPutVarWithFloat() {
        // Given: negative float value
        // When: storing negative float
        testObject.putVar("negative", -3.14f);

        // Then: should store correctly
        FloatVariable result = (FloatVariable) testObject.getVar("negative");
        assertEquals(-3.14f, result.getValue());
    }

    // ==================== putVar(String, long) Tests ====================

    @Test
    void shouldCreateLongVariable_whenPutVarWithLongAndNoExisting() {
        // Given: no existing variable
        // When: storing a long value
        testObject.putVar("longVar", 1000000L);

        // Then: should create a LongVariable
        IVariable result = testObject.getVar("longVar");
        assertNotNull(result);
        assertTrue(result instanceof LongVariable);
        assertEquals(1000000L, ((LongVariable) result).getValue());
    }

    @Test
    void shouldUpdateExistingVariable_whenPutVarWithLongAndExisting() {
        // Given: an existing long variable
        testObject.putVar("longVar", 100L);

        // When: updating with new long value
        testObject.putVar("longVar", 200L);

        // Then: should update the variable
        IVariable result = testObject.getVar("longVar");
        assertEquals(200L, ((LongVariable) result).getValue());
    }

    @Test
    void shouldHandleMaxLong_whenPutVarWithLong() {
        // Given: maximum long value
        // When: storing max long
        testObject.putVar("maxLong", Long.MAX_VALUE);

        // Then: should store correctly
        LongVariable result = (LongVariable) testObject.getVar("maxLong");
        assertTrue(result.getValue().equals(Long.MAX_VALUE));
    }

    @Test
    void shouldHandleMinLong_whenPutVarWithLong() {
        // Given: minimum long value
        // When: storing min long
        testObject.putVar("minLong", Long.MIN_VALUE);

        // Then: should store correctly
        LongVariable result = (LongVariable) testObject.getVar("minLong");
        assertTrue(result.getValue().equals(Long.MIN_VALUE));
    }

    // ==================== putVar(String, short) Tests ====================

    @Test
    void shouldCreateShortVariable_whenPutVarWithShortAndNoExisting() {
        // Given: no existing variable
        // When: storing a short value
        testObject.putVar("shortVar", (short) 500);

        // Then: should create a ShortVariable
        IVariable result = testObject.getVar("shortVar");
        assertNotNull(result);
        assertTrue(result instanceof ShortVariable);
        assertEquals((short) 500, ((ShortVariable) result).getValue());
    }

    @Test
    void shouldUpdateExistingVariable_whenPutVarWithShortAndExisting() {
        // Given: an existing short variable
        testObject.putVar("shortVar", (short) 10);

        // When: updating with new short value
        testObject.putVar("shortVar", (short) 20);

        // Then: should update the variable
        IVariable result = testObject.getVar("shortVar");
        assertEquals((short) 20, ((ShortVariable) result).getValue());
    }

    @Test
    void shouldHandleMaxShort_whenPutVarWithShort() {
        // Given: maximum short value
        // When: storing max short
        testObject.putVar("maxShort", Short.MAX_VALUE);

        // Then: should store correctly
        ShortVariable result = (ShortVariable) testObject.getVar("maxShort");
        assertEquals(Short.MAX_VALUE, result.getValue());
    }

    @Test
    void shouldHandleMinShort_whenPutVarWithShort() {
        // Given: minimum short value
        // When: storing min short
        testObject.putVar("minShort", Short.MIN_VALUE);

        // Then: should store correctly
        ShortVariable result = (ShortVariable) testObject.getVar("minShort");
        assertEquals(Short.MIN_VALUE, result.getValue());
    }

    // ==================== Integration Tests ====================

    @Test
    void shouldHandleMultipleVariablesOfDifferentTypes_whenStored() {
        // Given: multiple variables of different types
        testObject.putVar("intVar", 42);
        testObject.putVar("doubleVar", 3.14);
        testObject.putVar("boolVar", true);
        testObject.putVar("charVar", 'X');
        testObject.putVar("byteVar", (byte) 10);
        testObject.putVar("floatVar", 2.5f);
        testObject.putVar("longVar", 1000L);
        testObject.putVar("shortVar", (short) 100);

        // When: retrieving all variables
        // Then: all should be stored correctly with correct types
        assertEquals(42, ((IntVariable) testObject.getVar("intVar")).getValue());
        assertEquals(3.14, ((DoubleVariable) testObject.getVar("doubleVar")).getValue());
        assertEquals(1, ((BooleanVariable) testObject.getVar("boolVar")).getValue());
        // CharacterVariable.getValue() returns Character.charCount() which is always 1
        assertEquals(1, ((CharacterVariable) testObject.getVar("charVar")).getValue());
        assertEquals("X", testObject.getVar("charVar").toString());
        assertEquals((byte) 10, ((ByteVariable) testObject.getVar("byteVar")).getValue());
        assertEquals(2.5f, ((FloatVariable) testObject.getVar("floatVar")).getValue());
        assertEquals(1000L, ((LongVariable) testObject.getVar("longVar")).getValue());
        assertEquals((short) 100, ((ShortVariable) testObject.getVar("shortVar")).getValue());
    }

    @Test
    void shouldOverwriteVariableOfDifferentType_whenStoringSameKey() {
        // Given: an int variable
        testObject.putVar("var", 42);

        // When: overwriting with an IVariable (not using update path)
        testObject.putVar("var", new DoubleVariable(3.14));

        // Then: should have the double variable
        IVariable result = testObject.getVar("var");
        assertTrue(result instanceof DoubleVariable);
        assertEquals(3.14, ((DoubleVariable) result).getValue());
    }

    @Test
    void shouldMaintainSeparateVariablesInDifferentObjects() {
        // Given: two different test objects
        TestVariableStoreObject obj1 = new TestVariableStoreObject(simulation, 1, "OBJ1");
        TestVariableStoreObject obj2 = new TestVariableStoreObject(simulation, 2, "OBJ2");

        // When: storing different values with same key
        obj1.putVar("value", 100);
        obj2.putVar("value", 200);

        // Then: each should maintain its own value
        assertEquals(100, ((IntVariable) obj1.getVar("value")).getValue());
        assertEquals(200, ((IntVariable) obj2.getVar("value")).getValue());
    }
}
