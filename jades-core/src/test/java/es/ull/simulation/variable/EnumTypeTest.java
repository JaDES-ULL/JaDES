package es.ull.simulation.variable;

import static org.junit.jupiter.api.Assertions.*;

import java.util.TreeMap;

import org.junit.jupiter.api.Test;

class EnumTypeTest {

    @Test
    void shouldCreateEnumType_withSingleField() {
        EnumType enumType = new EnumType("VALUE1");
        
        assertNotNull(enumType);
        assertEquals(1, enumType.getMaxValue());
        assertEquals("VALUE1", enumType.getValuesDescrip().get(0));
    }

    @Test
    void shouldCreateEnumType_withMultipleFields() {
        EnumType enumType = new EnumType("RED", "GREEN", "BLUE");
        
        assertEquals(3, enumType.getMaxValue());
        assertEquals("RED", enumType.getValuesDescrip().get(0));
        assertEquals("GREEN", enumType.getValuesDescrip().get(1));
        assertEquals("BLUE", enumType.getValuesDescrip().get(2));
    }

    @Test
    void shouldCreateEnumType_withEmptyParams() {
        EnumType enumType = new EnumType();
        
        assertNotNull(enumType);
        assertEquals(0, enumType.getMaxValue());
        assertTrue(enumType.getValuesDescrip().isEmpty());
    }

    @Test
    void shouldAddField_toExistingEnumType() {
        EnumType enumType = new EnumType("FIRST");
        
        enumType.addField("SECOND");
        
        assertEquals(2, enumType.getMaxValue());
        assertEquals("FIRST", enumType.getValuesDescrip().get(0));
        assertEquals("SECOND", enumType.getValuesDescrip().get(1));
    }

    @Test
    void shouldAddMultipleFields_sequentially() {
        EnumType enumType = new EnumType("START");
        
        enumType.addField("MIDDLE");
        enumType.addField("END");
        
        assertEquals(3, enumType.getMaxValue());
        assertEquals("START", enumType.getValuesDescrip().get(0));
        assertEquals("MIDDLE", enumType.getValuesDescrip().get(1));
        assertEquals("END", enumType.getValuesDescrip().get(2));
    }

    @Test
    void shouldGetMaxValue_afterConstruction() {
        EnumType enumType = new EnumType("A", "B", "C", "D", "E");
        
        int maxValue = enumType.getMaxValue();
        
        assertEquals(5, maxValue);
    }

    @Test
    void shouldGetValuesDescrip_returnsTreeMap() {
        EnumType enumType = new EnumType("ONE", "TWO");
        
        TreeMap<Integer, String> values = enumType.getValuesDescrip();
        
        assertNotNull(values);
        assertEquals(2, values.size());
        assertTrue(values instanceof TreeMap);
    }

    @Test
    void shouldSetValuesDescrip_withNewTreeMap() {
        EnumType enumType = new EnumType("ORIGINAL");
        TreeMap<Integer, String> newValues = new TreeMap<>();
        newValues.put(0, "NEW1");
        newValues.put(1, "NEW2");
        newValues.put(2, "NEW3");
        
        enumType.setValuesDescrip(newValues);
        
        assertEquals(newValues, enumType.getValuesDescrip());
        assertEquals("NEW1", enumType.getValuesDescrip().get(0));
        assertEquals("NEW2", enumType.getValuesDescrip().get(1));
        assertEquals("NEW3", enumType.getValuesDescrip().get(2));
    }

    @Test
    void shouldCreateEnumType_withManyFields() {
        EnumType enumType = new EnumType("F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "F10");
        
        assertEquals(10, enumType.getMaxValue());
        assertEquals("F1", enumType.getValuesDescrip().get(0));
        assertEquals("F10", enumType.getValuesDescrip().get(9));
    }

    @Test
    void shouldMaintainOrder_inTreeMap() {
        EnumType enumType = new EnumType();
        
        enumType.addField("FIRST");
        enumType.addField("SECOND");
        enumType.addField("THIRD");
        
        TreeMap<Integer, String> values = enumType.getValuesDescrip();
        assertEquals("FIRST", values.get(0));
        assertEquals("SECOND", values.get(1));
        assertEquals("THIRD", values.get(2));
    }

    @Test
    void shouldCreateMultipleEnumTypes_independently() {
        EnumType type1 = new EnumType("TYPE1_VAL1", "TYPE1_VAL2");
        EnumType type2 = new EnumType("TYPE2_VAL1", "TYPE2_VAL2", "TYPE2_VAL3");
        
        assertEquals(2, type1.getMaxValue());
        assertEquals(3, type2.getMaxValue());
        assertNotEquals(type1.getValuesDescrip(), type2.getValuesDescrip());
    }

    @Test
    void shouldAddField_toEmptyEnumType() {
        EnumType enumType = new EnumType();
        
        enumType.addField("FIRST_ADDED");
        
        assertEquals(1, enumType.getMaxValue());
        assertEquals("FIRST_ADDED", enumType.getValuesDescrip().get(0));
    }
}
