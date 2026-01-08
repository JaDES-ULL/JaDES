package es.ull.simulation.model.flow;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.ull.simulation.model.Simulation;
import es.ull.simulation.model.TimeUnit;

/**
 * Test class for {@link ANDJoinFlow}.
 * Tests AND join flow pattern functionality.
 */
public class ANDJoinFlowTest {

    private Simulation simulation;

    @BeforeEach
    public void setUp() {
        simulation = new Simulation(0, "Test Simulation", TimeUnit.MINUTE);
    }

    @Test
    public void shouldCreateANDJoinFlowWithDefaultConstructor() {
        // Given: a simulation
        // When: creating an AND join flow with default constructor
        ANDJoinFlow flow = new ANDJoinFlow(simulation) {};

        // Then: it should be created successfully with default values
        assertNotNull(flow);
        assertEquals(0, flow.getAcceptValue());
    }

    @Test
    public void shouldCreateANDJoinFlowWithAcceptValue() {
        // Given: a simulation and an accept value
        int acceptValue = 3;

        // When: creating an AND join flow with accept value
        ANDJoinFlow flow = new ANDJoinFlow(simulation, acceptValue) {};

        // Then: it should store the accept value
        assertNotNull(flow);
        assertEquals(acceptValue, flow.getAcceptValue());
    }

    @Test
    public void shouldCreateANDJoinFlowWithSafeFlag() {
        // Given: a simulation and a safe flag
        boolean safe = false;

        // When: creating an AND join flow with safe flag
        ANDJoinFlow flow = new ANDJoinFlow(simulation, safe) {};

        // Then: it should be created successfully
        assertNotNull(flow);
        assertEquals(0, flow.getAcceptValue());
    }

    @Test
    public void shouldCreateANDJoinFlowWithSafeFlagAndAcceptValue() {
        // Given: a simulation, safe flag, and accept value
        boolean safe = false;
        int acceptValue = 2;

        // When: creating an AND join flow with both parameters
        ANDJoinFlow flow = new ANDJoinFlow(simulation, safe, acceptValue) {};

        // Then: it should store both values
        assertNotNull(flow);
        assertEquals(acceptValue, flow.getAcceptValue());
    }

    @Test
    public void shouldBelongToSimulation() {
        // Given: an AND join flow
        ANDJoinFlow flow = new ANDJoinFlow(simulation) {};

        // Then: it should be registered in simulation
        assertTrue(simulation.getFlowList().contains(flow));
    }

    @Test
    public void shouldHaveObjectTypeIdentifier() {
        // Given: an AND join flow
        ANDJoinFlow flow = new ANDJoinFlow(simulation) {};

        // When: getting its identifier
        String id = flow.toString();

        // Then: it should have "F" prefix
        assertTrue(id.startsWith("[F"));
        assertTrue(id.endsWith("]"));
    }

    @Test
    public void shouldHandleZeroAcceptValue() {
        // Given: an AND join flow with zero accept value
        ANDJoinFlow flow = new ANDJoinFlow(simulation, 0) {};

        // Then: accept value should be 0
        assertEquals(0, flow.getAcceptValue());
    }

    @Test
    public void shouldHandleLargeAcceptValue() {
        // Given: an AND join flow with large accept value
        int largeValue = 100;
        ANDJoinFlow flow = new ANDJoinFlow(simulation, largeValue) {};

        // Then: accept value should be stored correctly
        assertEquals(largeValue, flow.getAcceptValue());
    }

    @Test
    public void shouldCreateWithSafeTrueFlag() {
        // Given: safe context
        boolean safe = true;
        int acceptValue = 4;

        // When: creating with safe=true
        ANDJoinFlow flow = new ANDJoinFlow(simulation, safe, acceptValue) {};

        // Then: it should be created successfully
        assertNotNull(flow);
        assertEquals(acceptValue, flow.getAcceptValue());
    }

    @Test
    public void shouldCreateWithSafeFalseFlag() {
        // Given: general (non-safe) context
        boolean safe = false;
        int acceptValue = 5;

        // When: creating with safe=false
        ANDJoinFlow flow = new ANDJoinFlow(simulation, safe, acceptValue) {};

        // Then: it should be created successfully
        assertNotNull(flow);
        assertEquals(acceptValue, flow.getAcceptValue());
    }
}
