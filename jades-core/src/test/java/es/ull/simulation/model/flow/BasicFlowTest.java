package es.ull.simulation.model.flow;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.ull.simulation.model.Simulation;
import es.ull.simulation.model.TimeUnit;

/**
 * Test class for {@link BasicFlow}.
 * Tests basic flow functionality and default behaviors.
 */
public class BasicFlowTest {

    private Simulation simulation;
    private DelayFlow testFlow;

    @BeforeEach
    public void setUp() {
        simulation = new Simulation(0, "Test Simulation", TimeUnit.MINUTE);
        // Use DelayFlow which is a concrete implementation of BasicFlow
        testFlow = new DelayFlow(simulation, "Test Delay") {
            @Override
            public long getDurationSample(es.ull.simulation.model.Element elem) {
                return 10;
            }
        };
    }

    @Test
    public void shouldBelongToSimulation() {
        // Given: a simulation and a flow created
        // When: checking if flow is in simulation
        // Then: it should be registered
        assertTrue(simulation.getFlowList().contains(testFlow));
    }

    @Test
    public void shouldAutoRegisterInSimulation() {
        // Given: initial flow count
        int initialCount = simulation.getFlowList().size();

        // When: creating a new flow
        DelayFlow newFlow = new DelayFlow(simulation, "Another Delay") {
            @Override
            public long getDurationSample(es.ull.simulation.model.Element elem) {
                return 5;
            }
        };

        // Then: it should be registered
        assertEquals(initialCount + 1, simulation.getFlowList().size());
        assertTrue(simulation.getFlowList().contains(newFlow));
    }

    @Test
    public void shouldHaveObjectTypeIdentifier() {
        // When: getting its identifier
        String id = testFlow.toString();

        // Then: it should have "F" prefix
        assertTrue(id.startsWith("[F"));
        assertTrue(id.endsWith("]"));
    }

    @Test
    public void shouldHaveSequentialIds() {
        // Given: first flow
        // When: creating second flow
        DelayFlow flow2 = new DelayFlow(simulation, "Delay 2") {
            @Override
            public long getDurationSample(es.ull.simulation.model.Element elem) {
                return 20;
            }
        };

        // Then: they should have sequential IDs
        int id1 = Integer.parseInt(testFlow.toString().replaceAll("[^0-9]", ""));
        int id2 = Integer.parseInt(flow2.toString().replaceAll("[^0-9]", ""));
        assertTrue(id2 > id1);
    }

    @Test
    public void shouldInitiallyHaveNoParent() {
        // When: getting parent
        AbstractStructuredFlow parent = testFlow.getParent();

        // Then: it should be null
        assertNull(parent);
    }

    @Test
    public void shouldAllowSettingParent() {
        // Given: a structured flow (ActivityFlow as parent)
        ActivityFlow parentFlow = new ActivityFlow(simulation, "Parent Activity");

        // When: setting parent
        testFlow.setParent(parentFlow);

        // Then: parent should be set
        assertEquals(parentFlow, testFlow.getParent());
    }

    @Test
    public void shouldReturnTrueForBeforeRequestByDefault() {
        // When: calling beforeRequest
        boolean result = testFlow.beforeRequest(null);

        // Then: it should return true by default
        assertTrue(result);
    }
}
