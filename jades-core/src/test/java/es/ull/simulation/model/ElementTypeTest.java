package es.ull.simulation.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ElementTypeTest {
    private Simulation simulation;
    private static final int SIMULATION_ID = 1;
    private static final String SIMULATION_DESC = "Test Simulation";
    private static final String ET_DESC = "Test Element Type";

    @BeforeEach
    void setUp() {
        simulation = new Simulation(SIMULATION_ID, SIMULATION_DESC);
    }

    @Test
    void shouldCreateElementType_whenInstantiated() {
        ElementType et = new ElementType(simulation, ET_DESC);

        assertNotNull(et);
        assertEquals(ET_DESC, et.getDescription());
    }

    @Test
    void shouldAutoRegisterInSimulation_whenCreated() {
        ElementType et1 = new ElementType(simulation, "ET1");
        ElementType et2 = new ElementType(simulation, "ET2");

        assertEquals(2, simulation.getElementTypeList().size());
        assertTrue(simulation.getElementTypeList().contains(et1));
        assertTrue(simulation.getElementTypeList().contains(et2));
    }

    @Test
    void shouldHaveSequentialIds_whenMultipleCreated() {
        ElementType et1 = new ElementType(simulation, "ET1");
        ElementType et2 = new ElementType(simulation, "ET2");
        ElementType et3 = new ElementType(simulation, "ET3");

        assertEquals(0, et1.getIdentifier());
        assertEquals(1, et2.getIdentifier());
        assertEquals(2, et3.getIdentifier());
    }

    @Test
    void shouldHaveObjectTypeIdentifier_whenCreated() {
        ElementType et = new ElementType(simulation, ET_DESC);

        assertEquals("ET", et.getObjectTypeIdentifier());
    }

    @Test
    void shouldBelongToSimulation_whenCreated() {
        ElementType et = new ElementType(simulation, ET_DESC);

        assertNotNull(et.getSimulation());
        assertEquals(simulation, et.getSimulation());
    }

    @Test
    void shouldReturnDescription_whenQueried() {
        String customDesc = "Custom Element Type Description";
        ElementType et = new ElementType(simulation, customDesc);

        assertEquals(customDesc, et.getDescription());
    }
}
