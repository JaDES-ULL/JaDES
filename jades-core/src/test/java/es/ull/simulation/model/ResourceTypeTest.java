package es.ull.simulation.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ResourceTypeTest {
    private Simulation simulation;
    private static final int SIMULATION_ID = 1;
    private static final String SIMULATION_DESC = "Test Simulation";
    private static final String RT_DESC = "Test Resource Type";

    @BeforeEach
    void setUp() {
        simulation = new Simulation(SIMULATION_ID, SIMULATION_DESC);
    }

    @Test
    void shouldCreateResourceType_whenInstantiated() {
        ResourceType rt = new ResourceType(simulation, RT_DESC);
        
        assertNotNull(rt);
        assertEquals(RT_DESC, rt.getDescription());
    }

    @Test
    void shouldAutoRegisterInSimulation_whenCreated() {
        ResourceType rt1 = new ResourceType(simulation, "RT1");
        ResourceType rt2 = new ResourceType(simulation, "RT2");
        
        assertEquals(2, simulation.getResourceTypeList().size());
        assertTrue(simulation.getResourceTypeList().contains(rt1));
        assertTrue(simulation.getResourceTypeList().contains(rt2));
    }

    @Test
    void shouldHaveSequentialIds_whenMultipleCreated() {
        ResourceType rt1 = new ResourceType(simulation, "RT1");
        ResourceType rt2 = new ResourceType(simulation, "RT2");
        ResourceType rt3 = new ResourceType(simulation, "RT3");
        
        assertEquals(0, rt1.getIdentifier());
        assertEquals(1, rt2.getIdentifier());
        assertEquals(2, rt3.getIdentifier());
    }

    @Test
    void shouldHaveObjectTypeIdentifier_whenCreated() {
        ResourceType rt = new ResourceType(simulation, RT_DESC);
        
        assertEquals("RT", rt.getObjectTypeIdentifier());
    }

    @Test
    void shouldBelongToSimulation_whenCreated() {
        ResourceType rt = new ResourceType(simulation, RT_DESC);
        
        assertNotNull(rt.getSimulation());
        assertEquals(simulation, rt.getSimulation());
    }

    @Test
    void shouldReturnDescription_whenQueried() {
        String customDesc = "Custom Resource Type Description";
        ResourceType rt = new ResourceType(simulation, customDesc);
        
        assertEquals(customDesc, rt.getDescription());
    }
}
