package es.ull.simulation.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ElementMovement class focusing on capacity management.
 * Tests basic movement operations without requiring location changes that need initialized simulation.
 */
class ElementMovementTest {
    private Simulation simulation;
    private Element element;
    private ElementType elementType;

    @BeforeEach
    void setUp() {
        simulation = new Simulation(1, "Test Simulation");
        elementType = new ElementType(simulation, "Test Element Type");
    }

    @Test
    void shouldHaveZeroCapacity_whenCreatedWithDefaultConstructor() {
        element = new Element(simulation, elementType, null);
        
        assertEquals(0, element.getCapacity());
    }

    @Test
    void shouldHaveCorrectCapacity_whenCreatedWithSize() {
        element = new Element(simulation, "E", elementType, null, 5, null);
        
        assertEquals(5, element.getCapacity());
    }

    @Test
    void shouldSupportSmallCapacity() {
        element = new Element(simulation, "E", elementType, null, 1, null);
        
        assertEquals(1, element.getCapacity());
    }

    @Test
    void shouldSupportLargeCapacity() {
        element = new Element(simulation, "E", elementType, null, 1000, null);
        
        assertEquals(1000, element.getCapacity());
    }

    @Test
    void shouldMaintainCapacityIndependentlyPerElement() {
        Element elem1 = new Element(simulation, "E", elementType, null, 1, null);
        Element elem2 = new Element(simulation, "E", elementType, null, 5, null);
        Element elem3 = new Element(simulation, "E", elementType, null, 10, null);
        
        assertEquals(1, elem1.getCapacity());
        assertEquals(5, elem2.getCapacity());
        assertEquals(10, elem3.getCapacity());
    }

    @Test
    void shouldHaveNullLocation_whenCreatedWithoutInitialLocation() {
        element = new Element(simulation, elementType, null);
        
        assertNull(element.getLocation());
    }

    @Test
    void shouldPreserveCapacityValue() {
        int expectedCapacity = 42;
        element = new Element(simulation, "E", elementType, null, expectedCapacity, null);
        
        // Get capacity multiple times
        assertEquals(expectedCapacity, element.getCapacity());
        assertEquals(expectedCapacity, element.getCapacity());
        assertEquals(expectedCapacity, element.getCapacity());
    }

    @Test
    void shouldHandleZeroCapacity() {
        element = new Element(simulation, "E", elementType, null, 0, null);
        
        assertEquals(0, element.getCapacity());
    }

    @Test
    void shouldSupportMultipleElementTypes() {
        ElementType type1 = new ElementType(simulation, "Type 1");
        ElementType type2 = new ElementType(simulation, "Type 2");
        
        Element elem1 = new Element(simulation, "E", type1, null, 5, null);
        Element elem2 = new Element(simulation, "E", type2, null, 10, null);
        
        assertEquals(5, elem1.getCapacity());
        assertEquals(10, elem2.getCapacity());
    }
}
