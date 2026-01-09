package es.ull.simulation.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ElementFlow class focusing on workflow management.
 * Tests basic flow operations without requiring full simulation initialization.
 */
class ElementFlowTest {
    private Simulation simulation;
    private Element element;
    private ElementType elementType;

    @BeforeEach
    void setUp() {
        simulation = new Simulation(1, "Test Simulation");
        elementType = new ElementType(simulation, "Test Element Type");
    }

    @Test
    void shouldReturnNullFlow_whenCreatedWithoutFlow() {
        element = new Element(simulation, elementType, null);
        
        assertNull(element.getFlow());
    }

    @Test
    void shouldReturnCorrectFlow_whenCreatedWithFlow() {
        var testFlow = new es.ull.simulation.model.flow.ActivityFlow(simulation, "Test Activity");
        element = new Element(simulation, elementType, testFlow);
        
        assertEquals(testFlow, element.getFlow());
    }

    @Test
    void shouldMaintainFlowReference_acrossMultipleGets() {
        var testFlow = new es.ull.simulation.model.flow.ActivityFlow(simulation, "Test Activity");
        element = new Element(simulation, elementType, testFlow);
        
        var flow1 = element.getFlow();
        var flow2 = element.getFlow();
        
        assertSame(flow1, flow2);
    }

    @Test
    void shouldSupportMultipleElementsWithSameFlow() {
        var sharedFlow = new es.ull.simulation.model.flow.ActivityFlow(simulation, "Shared Activity");
        
        Element elem1 = new Element(simulation, elementType, sharedFlow);
        Element elem2 = new Element(simulation, elementType, sharedFlow);
        
        assertEquals(sharedFlow, elem1.getFlow());
        assertEquals(sharedFlow, elem2.getFlow());
    }

    @Test
    void shouldSupportMultipleElementsWithDifferentFlows() {
        var flow1 = new es.ull.simulation.model.flow.ActivityFlow(simulation, "Activity 1");
        var flow2 = new es.ull.simulation.model.flow.ActivityFlow(simulation, "Activity 2");
        
        Element elem1 = new Element(simulation, elementType, flow1);
        Element elem2 = new Element(simulation, elementType, flow2);
        
        assertEquals(flow1, elem1.getFlow());
        assertEquals(flow2, elem2.getFlow());
        assertNotEquals(elem1.getFlow(), elem2.getFlow());
    }

    @Test
    void shouldMaintainFlowIndependentOfSize() {
        var testFlow = new es.ull.simulation.model.flow.ActivityFlow(simulation, "Test Activity");
        
        Element smallElement = new Element(simulation, "E", elementType, testFlow, 1, null);
        Element largeElement = new Element(simulation, "E", elementType, testFlow, 100, null);
        
        assertEquals(testFlow, smallElement.getFlow());
        assertEquals(testFlow, largeElement.getFlow());
    }

    @Test
    void shouldAllowNullAndNonNullFlowsInSameSimulation() {
        var testFlow = new es.ull.simulation.model.flow.ActivityFlow(simulation, "Test Activity");
        
        Element elemWithFlow = new Element(simulation, elementType, testFlow);
        Element elemWithoutFlow = new Element(simulation, elementType, null);
        
        assertNotNull(elemWithFlow.getFlow());
        assertNull(elemWithoutFlow.getFlow());
    }
}
