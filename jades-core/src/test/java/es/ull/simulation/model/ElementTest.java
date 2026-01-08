package es.ull.simulation.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
}
