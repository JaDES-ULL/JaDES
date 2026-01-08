package es.ull.simulation.model.flow;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.ull.simulation.model.Simulation;
import es.ull.simulation.model.TimeUnit;

/**
 * Test class for {@link DiscriminatorFlow}.
 * Tests Blocking Discriminator pattern (WFP28).
 */
public class DiscriminatorFlowTest {

    private Simulation simulation;

    @BeforeEach
    public void setUp() {
        simulation = new Simulation(0, "Test Simulation", TimeUnit.MINUTE);
    }

    @Test
    public void shouldCreateDiscriminatorFlowWithDefaultConstructor() {
        // Given: a simulation
        // When: creating a discriminator flow
        DiscriminatorFlow flow = new DiscriminatorFlow(simulation);

        // Then: it should be created with accept value of 1
        assertNotNull(flow);
        assertEquals(1, flow.getAcceptValue());
    }

    @Test
    public void shouldCreateDiscriminatorFlowWithSafeFlag() {
        // Given: a simulation and safe flag
        boolean safe = false;

        // When: creating a discriminator flow with safe flag
        DiscriminatorFlow flow = new DiscriminatorFlow(simulation, safe);

        // Then: it should be created with accept value of 1
        assertNotNull(flow);
        assertEquals(1, flow.getAcceptValue());
    }

    @Test
    public void shouldBelongToSimulation() {
        // Given: a discriminator flow
        DiscriminatorFlow flow = new DiscriminatorFlow(simulation);

        // Then: it should be registered in simulation
        assertTrue(simulation.getFlowList().contains(flow));
    }

    @Test
    public void shouldHaveObjectTypeIdentifier() {
        // Given: a discriminator flow
        DiscriminatorFlow flow = new DiscriminatorFlow(simulation);

        // When: getting its identifier
        String id = flow.toString();

        // Then: it should have "F" prefix
        assertTrue(id.startsWith("[F"));
        assertTrue(id.endsWith("]"));
    }

    @Test
    public void shouldAlwaysHaveAcceptValueOfOne() {
        // Given: discriminator flows with different constructors
        DiscriminatorFlow flow1 = new DiscriminatorFlow(simulation);
        DiscriminatorFlow flow2 = new DiscriminatorFlow(simulation, true);
        DiscriminatorFlow flow3 = new DiscriminatorFlow(simulation, false);

        // Then: all should have accept value of 1
        assertEquals(1, flow1.getAcceptValue());
        assertEquals(1, flow2.getAcceptValue());
        assertEquals(1, flow3.getAcceptValue());
    }

    @Test
    public void shouldExtendANDJoinFlow() {
        // Given: a discriminator flow
        DiscriminatorFlow flow = new DiscriminatorFlow(simulation);

        // Then: it should be an instance of ANDJoinFlow
        assertTrue(flow instanceof ANDJoinFlow);
    }

    @Test
    public void shouldCreateWithSafeTrueFlag() {
        // Given: safe context
        boolean safe = true;

        // When: creating with safe=true
        DiscriminatorFlow flow = new DiscriminatorFlow(simulation, safe);

        // Then: it should be created successfully
        assertNotNull(flow);
        assertEquals(1, flow.getAcceptValue());
    }

    @Test
    public void shouldCreateWithSafeFalseFlag() {
        // Given: general (non-safe) context
        boolean safe = false;

        // When: creating with safe=false
        DiscriminatorFlow flow = new DiscriminatorFlow(simulation, safe);

        // Then: it should be created successfully
        assertNotNull(flow);
        assertEquals(1, flow.getAcceptValue());
    }

    @Test
    public void shouldHaveSequentialIds() {
        // Given: two discriminator flows
        DiscriminatorFlow flow1 = new DiscriminatorFlow(simulation);
        DiscriminatorFlow flow2 = new DiscriminatorFlow(simulation);

        // Then: they should have sequential IDs
        int id1 = Integer.parseInt(flow1.toString().replaceAll("[^0-9]", ""));
        int id2 = Integer.parseInt(flow2.toString().replaceAll("[^0-9]", ""));
        assertTrue(id2 > id1);
    }
}
