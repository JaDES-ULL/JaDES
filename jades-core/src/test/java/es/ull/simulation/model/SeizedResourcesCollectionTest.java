package es.ull.simulation.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for SeizedResourcesCollection class.
 * Tests basic collection operations.
 */
class SeizedResourcesCollectionTest {
    private Simulation simulation;
    private Element element;
    private ElementType elementType;
    private SeizedResourcesCollection collection;

    @BeforeEach
    void setUp() {
        simulation = new Simulation(1, "Test Simulation");
        elementType = new ElementType(simulation, "Test Element Type");
        element = new Element(simulation, elementType, null);
        collection = element.seizedResources;
    }

    @Test
    void shouldBeEmptyInitially() {
        var allResources = collection.getAll();
        
        assertNotNull(allResources);
        assertEquals(0, allResources.size());
    }

    @Test
    void shouldReturnEmptyDeque_whenGettingDefaultGroup() {
        var resources = collection.get(0);
        
        assertNotNull(resources);
        assertEquals(0, resources.size());
    }

    @Test
    void shouldBeIndependentPerElement() {
        Element elem1 = new Element(simulation, elementType, null);
        Element elem2 = new Element(simulation, elementType, null);
        
        assertNotNull(elem1.seizedResources);
        assertNotNull(elem2.seizedResources);
        assertNotSame(elem1.seizedResources, elem2.seizedResources);
    }

    @Test
    void shouldReturnConsistentResults_forSameGroupId() {
        var resources1 = collection.get(0);
        var resources2 = collection.get(0);
        
        assertNotNull(resources1);
        assertNotNull(resources2);
        assertEquals(resources1.size(), resources2.size());
    }

    @Test
    void shouldHandleGetAll_withEmptyCollection() {
        var all = collection.getAll();
        
        assertNotNull(all);
        assertTrue(all.isEmpty());
    }

    @Test
    void shouldMaintainCollectionPerElement() {
        Element elem1 = new Element(simulation, elementType, null);
        Element elem2 = new Element(simulation, elementType, null);
        Element elem3 = new Element(simulation, elementType, null);
        
        assertNotNull(elem1.seizedResources);
        assertNotNull(elem2.seizedResources);
        assertNotNull(elem3.seizedResources);
        
        // All should be initially empty
        assertEquals(0, elem1.seizedResources.getAll().size());
        assertEquals(0, elem2.seizedResources.getAll().size());
        assertEquals(0, elem3.seizedResources.getAll().size());
    }
}
