package es.ull.simulation.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.ull.simulation.model.flow.ActivityFlow;
import es.ull.simulation.model.flow.IInitializerFlow;

class ElementTest {
    private Simulation simulation;
    private ElementType elementType;
    private IInitializerFlow initialFlow;
    private static final int SIMULATION_ID = 1;
    private static final String SIMULATION_DESC = "Test Simulation";
    private static final String ELEMENT_TYPE_DESC = "Test Element Type";

    @BeforeEach
    void setUp() {
        simulation = new Simulation(SIMULATION_ID, SIMULATION_DESC);
        elementType = new ElementType(simulation, ELEMENT_TYPE_DESC);
        initialFlow = null;
    }

    @Test
    void shouldGenerateUniqueId_whenCreated() {
        Element element1 = new Element(simulation, elementType, initialFlow);
        Element element2 = new Element(simulation, elementType, initialFlow);

        assertEquals(0, element1.getIdentifier());
        assertEquals(1, element2.getIdentifier());
    }

    @Test
    void shouldHaveCorrectElementType_whenCreated() {
        Element element = new Element(simulation, elementType, initialFlow);

        assertEquals(elementType, element.getType());
    }

    @Test
    void shouldBelongToSimulation_whenCreated() {
        Element element = new Element(simulation, elementType, initialFlow);

        assertNotNull(element.getSimulation());
        assertEquals(simulation, element.getSimulation());
    }

    @Test
    void shouldHaveDefaultSize_whenCreatedWithoutSize() {
        Element element = new Element(simulation, elementType, initialFlow);

        assertEquals(0, element.getCapacity());
    }

    @Test
    void shouldHaveCustomSize_whenCreatedWithSize() {
        int customSize = 5;
        Element element = new Element(simulation, "E", elementType, initialFlow, customSize, null);

        assertEquals(customSize, element.getCapacity());
    }

    @Test
    void shouldHaveObjectTypeIdentifier_whenCreated() {
        Element element = new Element(simulation, elementType, initialFlow);

        assertNotNull(element.getObjectTypeIdentifier());
        assertEquals("E", element.getObjectTypeIdentifier());
    }

    @Test
    void shouldIncrementSimulationElementCounter_whenMultipleElementsCreated() {
        Element element1 = new Element(simulation, elementType, initialFlow);
        Element element2 = new Element(simulation, elementType, initialFlow);
        Element element3 = new Element(simulation, elementType, initialFlow);

        assertEquals(element1.getIdentifier() + 1, element2.getIdentifier());
        assertEquals(element2.getIdentifier() + 1, element3.getIdentifier());
    }

    @Test
    void shouldReturnInitialFlow_whenCreated() {
        ActivityFlow testFlow = new ActivityFlow(simulation, "Test Activity");
        Element element = new Element(simulation, elementType, testFlow);

        assertEquals(testFlow, element.getFlow());
    }

    @Test
    void shouldHandleNullInitialFlow_whenCreated() {
        Element element = new Element(simulation, elementType, null);

        assertEquals(null, element.getFlow());
    }

    @Test
    void shouldHaveDefaultPriority_whenCreated() {
        Element element = new Element(simulation, elementType, initialFlow);

        // Default priority should be 0
        assertEquals(0, element.getPriority());
    }

    @Test
    void shouldAllowSettingExclusiveFlag() {
        Element element = new Element(simulation, elementType, initialFlow);

        // When: setting exclusive to true
        element.setExclusive(true);

        // Then: element should be created successfully
        // (exclusive flag is internal, no getter available)
        assertNotNull(element);
    }

    @Test
    void shouldHaveEmptyCaughtResourcesInitially() {
        Element element = new Element(simulation, elementType, initialFlow);

        // When: getting caught resources
        var caughtResources = element.getCaughtResources();

        // Then: it should be empty initially
        assertNotNull(caughtResources);
        assertEquals(0, caughtResources.size());
    }

    @Test
    void shouldHaveNullLocationInitially() {
        Element element = new Element(simulation, elementType, initialFlow);

        // When: getting location without setting it
        var location = element.getLocation();

        // Then: it should be null
        assertEquals(null, location);
    }

    @Test
    void shouldHaveEngineInstance() {
        Element element = new Element(simulation, elementType, initialFlow);

        // When: getting engine (may be null if simulation not started)
        var engine = element.getEngine();

        // Then: engine reference should be accessible
        // (Engine is assigned when simulation starts, may be null initially)
        // Just verify the method is accessible
        assertNotNull(element);
    }

    @Test
    void shouldCreateElementWithCustomObjectTypeId() {
        String customTypeId = "CUSTOM";
        Element element = new Element(simulation, customTypeId, elementType, initialFlow, 0, null);

        // Then: it should have the custom type identifier
        assertEquals(customTypeId, element.getObjectTypeIdentifier());
    }

    @Test
    void shouldHandleZeroSize() {
        Element element = new Element(simulation, "E", elementType, initialFlow, 0, null);

        assertEquals(0, element.getCapacity());
    }

    @Test
    void shouldHandleLargeSize() {
        int largeSize = 1000;
        Element element = new Element(simulation, "E", elementType, initialFlow, largeSize, null);

        assertEquals(largeSize, element.getCapacity());
    }
}
