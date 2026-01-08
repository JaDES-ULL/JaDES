package es.ull.simulation.model.flow;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.ull.simulation.model.Element;
import es.ull.simulation.model.Simulation;
import es.ull.simulation.model.TimeUnit;

/**
 * Test class for {@link DelayFlow}.
 * Tests delay flow behavior with fixed and variable delays.
 */
public class DelayFlowTest {

    private Simulation simulation;

    @BeforeEach
    public void setUp() {
        simulation = new Simulation(0, "Test Simulation", TimeUnit.MINUTE);
    }

    @Test
    public void shouldCreateDelayFlowWithDescription() {
        // Given: a description
        String description = "Test Delay";

        // When: creating a delay flow with fixed duration
        DelayFlow flow = new DelayFlow(simulation, description) {
            @Override
            public long getDurationSample(Element elem) {
                return 10;
            }
        };

        // Then: it should have the correct description
        assertEquals(description, flow.getDescription());
    }

    @Test
    public void shouldBelongToSimulation() {
        // Given: a simulation
        // When: creating a delay flow
        DelayFlow flow = new DelayFlow(simulation, "Delay") {
            @Override
            public long getDurationSample(Element elem) {
                return 5;
            }
        };

        // Then: it should belong to the simulation
        assertTrue(simulation.getFlowList().contains(flow));
    }

    @Test
    public void shouldReturnFixedDuration() {
        // Given: a delay flow with fixed duration of 20
        long expectedDuration = 20;
        DelayFlow flow = new DelayFlow(simulation, "Fixed Delay") {
            @Override
            public long getDurationSample(Element elem) {
                return expectedDuration;
            }
        };

        // When: getting duration sample
        long duration = flow.getDurationSample(null);

        // Then: it should return the fixed duration
        assertEquals(expectedDuration, duration);
    }

    @Test
    public void shouldAllowVariableDuration() {
        // Given: a delay flow with variable duration based on element
        DelayFlow flow = new DelayFlow(simulation, "Variable Delay") {
            @Override
            public long getDurationSample(Element elem) {
                // Different delays: 10 for null, 20 otherwise
                return elem == null ? 10 : 20;
            }
        };

        // When: getting duration without element
        long duration1 = flow.getDurationSample(null);
        
        // Then: durations should reflect the logic
        assertEquals(10, duration1);
        // Note: Creating actual Element requires complex setup, 
        // so we test the method signature and null case
    }

    @Test
    public void shouldHaveObjectTypeIdentifier() {
        // Given: a delay flow
        DelayFlow flow = new DelayFlow(simulation, "Delay") {
            @Override
            public long getDurationSample(Element elem) {
                return 1;
            }
        };

        // When: getting its identifier
        String id = flow.toString();

        // Then: it should start with "F" (Flow)
        assertTrue(id.startsWith("[F"));
    }

    @Test
    public void shouldAutoRegisterInSimulation() {
        // Given: current flow count
        int initialCount = simulation.getFlowList().size();

        // When: creating a new delay flow
        DelayFlow flow = new DelayFlow(simulation, "Delay") {
            @Override
            public long getDurationSample(Element elem) {
                return 1;
            }
        };

        // Then: it should be registered in simulation
        assertEquals(initialCount + 1, simulation.getFlowList().size());
        assertTrue(simulation.getFlowList().contains(flow));
    }

    @Test
    public void shouldHaveSequentialIds() {
        // Given: first delay flow
        DelayFlow flow1 = new DelayFlow(simulation, "Delay 1") {
            @Override
            public long getDurationSample(Element elem) {
                return 1;
            }
        };

        // When: creating second delay flow
        DelayFlow flow2 = new DelayFlow(simulation, "Delay 2") {
            @Override
            public long getDurationSample(Element elem) {
                return 2;
            }
        };

        // Then: they should have sequential identifiers
        int id1 = Integer.parseInt(flow1.toString().replaceAll("[^0-9]", ""));
        int id2 = Integer.parseInt(flow2.toString().replaceAll("[^0-9]", ""));
        assertTrue(id2 > id1);
    }

    @Test
    public void shouldSupportZeroDuration() {
        // Given: a delay flow with zero duration
        DelayFlow flow = new DelayFlow(simulation, "Zero Delay") {
            @Override
            public long getDurationSample(Element elem) {
                return 0;
            }
        };

        // When: getting duration
        long duration = flow.getDurationSample(null);

        // Then: it should be 0
        assertEquals(0, duration);
    }

    @Test
    public void shouldSupportLargeDuration() {
        // Given: a delay flow with very large duration
        long largeDuration = 1000000L;
        DelayFlow flow = new DelayFlow(simulation, "Large Delay") {
            @Override
            public long getDurationSample(Element elem) {
                return largeDuration;
            }
        };

        // When: getting duration
        long duration = flow.getDurationSample(null);

        // Then: it should handle large values
        assertEquals(largeDuration, duration);
    }
}
