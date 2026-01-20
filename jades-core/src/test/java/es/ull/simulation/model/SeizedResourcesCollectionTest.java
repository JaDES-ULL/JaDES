package es.ull.simulation.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayDeque;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for SeizedResourcesCollection class.
 * Tests collection operations including add, remove, and resource management.
 */
class SeizedResourcesCollectionTest {
    private Simulation simulation;
    private Element element;
    private ElementType elementType;
    private SeizedResourcesCollection collection;
    private ResourceType resourceType1;
    private ResourceType resourceType2;
    private Resource resource1;
    private Resource resource2;
    private Resource resource3;

    @BeforeEach
    void setUp() {
        simulation = new Simulation(1, "Test Simulation");
        elementType = new ElementType(simulation, "Test Element Type");
        element = new Element(simulation, elementType, null);
        collection = (SeizedResourcesCollection) element.resourceManager;
        
        // Create resource types and resources for testing
        resourceType1 = new ResourceType(simulation, "RT1");
        resourceType2 = new ResourceType(simulation, "RT2");
        
        // Create resources and assign their types directly (for unit testing)
        resource1 = new Resource(simulation, "R1");
        resource1.setCurrentResourceType(resourceType1);
        
        resource2 = new Resource(simulation, "R2");
        resource2.setCurrentResourceType(resourceType1);
        
        resource3 = new Resource(simulation, "R3");
        resource3.setCurrentResourceType(resourceType2);
        
        // Initialize simulation to set up the engine (required for some operations)
        simulation.init();
    }

    @Test
    void shouldBeEmptyInitially() {
        var allResources = collection.getAll();
        
        assertNotNull(allResources);
        assertEquals(0, allResources.size());
        assertFalse(collection.containsResourceType(resourceType1));
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
        
        assertNotNull(elem1.resourceManager);
        assertNotNull(elem2.resourceManager);
        assertNotSame(elem1.resourceManager, elem2.resourceManager);
    }

    @Test
    void shouldAddResources_toDefaultGroup() {
        ArrayDeque<Resource> resourcesToAdd = new ArrayDeque<>();
        resourcesToAdd.add(resource1);
        resourcesToAdd.add(resource2);
        
        collection.addResources(0, resourcesToAdd);
        
        assertEquals(2, collection.getAll().size());
        assertTrue(collection.containsResourceType(resourceType1));
        assertFalse(collection.containsResourceType(resourceType2));
    }

    @Test
    void shouldAddResources_toNewGroup() {
        ArrayDeque<Resource> resourcesToAdd = new ArrayDeque<>();
        resourcesToAdd.add(resource1);
        
        collection.addResources(5, resourcesToAdd);
        
        assertEquals(1, collection.getAll().size());
        assertEquals(1, collection.get(5).size());
        assertTrue(collection.containsResourceType(resourceType1));
    }

    @Test
    void shouldAddResources_ofMultipleTypes() {
        ArrayDeque<Resource> resourcesToAdd = new ArrayDeque<>();
        resourcesToAdd.add(resource1);
        resourcesToAdd.add(resource3);
        
        collection.addResources(0, resourcesToAdd);
        
        assertEquals(2, collection.getAll().size());
        assertTrue(collection.containsResourceType(resourceType1));
        assertTrue(collection.containsResourceType(resourceType2));
    }

    @Test
    void shouldGetResources_byGroupId() {
        ArrayDeque<Resource> group1 = new ArrayDeque<>();
        group1.add(resource1);
        ArrayDeque<Resource> group2 = new ArrayDeque<>();
        group2.add(resource2);
        
        collection.addResources(1, group1);
        collection.addResources(2, group2);
        
        assertEquals(1, collection.get(1).size());
        assertEquals(1, collection.get(2).size());
        assertEquals(2, collection.getAll().size());
    }

    @Test
    void shouldGetResources_byWorkGroup() {
        ArrayDeque<Resource> resourcesToAdd = new ArrayDeque<>();
        resourcesToAdd.add(resource1);
        resourcesToAdd.add(resource3);
        collection.addResources(0, resourcesToAdd);
        
        WorkGroup wg = new WorkGroup(simulation, new ResourceType[]{resourceType1}, new int[]{1});
        ArrayDeque<Resource> result = collection.get(0, wg);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(resource1, result.peek());
    }

    @Test
    void shouldRemoveResources_withWorkGroup() {
        // Add resources first
        ArrayDeque<Resource> resourcesToAdd = new ArrayDeque<>();
        resourcesToAdd.add(resource1);
        resourcesToAdd.add(resource2);
        collection.addResources(0, resourcesToAdd);
        
        // Remove one resource
        WorkGroup wg = new WorkGroup(simulation, new ResourceType[]{resourceType1}, new int[]{1});
        ArrayDeque<Resource> removed = collection.removeResources(0, wg);
        
        assertNotNull(removed);
        assertEquals(1, removed.size());
        assertEquals(1, collection.getAll().size());
    }

    @Test
    void shouldRemoveAllResources_whenWorkGroupIsNull() {
        ArrayDeque<Resource> resourcesToAdd = new ArrayDeque<>();
        resourcesToAdd.add(resource1);
        resourcesToAdd.add(resource2);
        collection.addResources(0, resourcesToAdd);
        
        ArrayDeque<Resource> removed = collection.removeResources(0, null);
        
        assertNotNull(removed);
        assertEquals(2, removed.size());
        assertEquals(0, collection.get(0).size());
    }

    @Test
    void shouldRemoveAllResourcesOfType_whenCountMatchesAvailable() {
        ArrayDeque<Resource> resourcesToAdd = new ArrayDeque<>();
        resourcesToAdd.add(resource1);
        resourcesToAdd.add(resource2);
        collection.addResources(0, resourcesToAdd);
        
        WorkGroup wg = new WorkGroup(simulation, new ResourceType[]{resourceType1}, new int[]{2});
        ArrayDeque<Resource> removed = collection.removeResources(0, wg);
        
        assertNotNull(removed);
        assertEquals(2, removed.size());
        assertFalse(collection.containsResourceType(resourceType1));
    }

    @Test
    void shouldReleaseAll_resources() {
        ArrayDeque<Resource> group1 = new ArrayDeque<>();
        group1.add(resource1);
        ArrayDeque<Resource> group2 = new ArrayDeque<>();
        group2.add(resource2);
        
        collection.addResources(1, group1);
        collection.addResources(2, group2);
        
        collection.releaseAll();
        
        assertEquals(0, collection.getAll().size());
        assertFalse(collection.containsResourceType(resourceType1));
    }

    @Test
    void shouldHandleSetElement() {
        Element newElement = new Element(simulation, elementType, null);
        collection.setElement(newElement);
        
        // Should not throw exception
        assertNotNull(collection);
    }

    @Test
    void shouldHandleIResourceManager_interface() {
        IResourceManager manager = collection;
        
        ArrayDeque<Resource> resourcesToAdd = new ArrayDeque<>();
        resourcesToAdd.add(resource1);
        
        manager.addResources(0, resourcesToAdd);
        
        assertTrue(manager.hasResourceType(resourceType1));
        assertEquals(1, manager.getAllResources().size());
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
    void shouldMaintainCollectionPerElement() {
        Element elem1 = new Element(simulation, elementType, null);
        Element elem2 = new Element(simulation, elementType, null);
        Element elem3 = new Element(simulation, elementType, null);
        
        assertNotNull(elem1.resourceManager);
        assertNotNull(elem2.resourceManager);
        assertNotNull(elem3.resourceManager);
        
        // All should be initially empty
        assertEquals(0, ((SeizedResourcesCollection)elem1.resourceManager).getAll().size());
        assertEquals(0, ((SeizedResourcesCollection)elem2.resourceManager).getAll().size());
        assertEquals(0, ((SeizedResourcesCollection)elem3.resourceManager).getAll().size());
    }
}
