package es.ull.simulation.model.flow;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.ull.simulation.functions.AbstractTimeFunction;
import es.ull.simulation.functions.ConstantFunction;
import es.ull.simulation.model.Simulation;
import es.ull.simulation.model.TimeUnit;

/**
 * Test class for {@link ForLoopFlow}.
 * Tests for-loop flow pattern.
 */
public class ForLoopFlowTest {

    private Simulation simulation;

    @BeforeEach
    public void setUp() {
        simulation = new Simulation(0, "Test Simulation", TimeUnit.MINUTE);
    }

    @Test
    public void shouldCreateForLoopFlowWithConstantIterations() {
        // Given: a task flow and iterations
        DelayFlow taskFlow = new DelayFlow(simulation, "Task") {
            @Override
            public long getDurationSample(es.ull.simulation.model.Element elem) {
                return 10;
            }
        };
        int iterations = 5;

        // When: creating a for-loop flow with constant iterations
        ForLoopFlow flow = new ForLoopFlow(simulation, taskFlow, iterations);

        // Then: it should be created successfully
        assertNotNull(flow);
        assertTrue(flow.getIterations() instanceof ConstantFunction);
    }

    @Test
    public void shouldCreateForLoopFlowWithFunctionIterations() {
        // Given: a task flow and an iteration function
        DelayFlow taskFlow = new DelayFlow(simulation, "Task") {
            @Override
            public long getDurationSample(es.ull.simulation.model.Element elem) {
                return 10;
            }
        };
        AbstractTimeFunction iterationFunction = new ConstantFunction(10);

        // When: creating a for-loop flow with function
        ForLoopFlow flow = new ForLoopFlow(simulation, taskFlow, iterationFunction);

        // Then: it should be created successfully
        assertNotNull(flow);
        assertEquals(iterationFunction, flow.getIterations());
    }

    @Test
    public void shouldCreateForLoopFlowWithSeparateFlows() {
        // Given: initial and final flows with iterations
        DelayFlow initialFlow = new DelayFlow(simulation, "Initial") {
            @Override
            public long getDurationSample(es.ull.simulation.model.Element elem) {
                return 5;
            }
        };
        DelayFlow finalFlow = new DelayFlow(simulation, "Final") {
            @Override
            public long getDurationSample(es.ull.simulation.model.Element elem) {
                return 10;
            }
        };
        int iterations = 3;

        // When: creating a for-loop flow with separate initial/final flows
        ForLoopFlow flow = new ForLoopFlow(simulation, initialFlow, finalFlow, iterations);

        // Then: it should be created successfully
        assertNotNull(flow);
        assertTrue(flow.getIterations() instanceof ConstantFunction);
    }

    @Test
    public void shouldCreateForLoopFlowWithSeparateFlowsAndFunction() {
        // Given: initial and final flows with iteration function
        DelayFlow initialFlow = new DelayFlow(simulation, "Initial") {
            @Override
            public long getDurationSample(es.ull.simulation.model.Element elem) {
                return 5;
            }
        };
        DelayFlow finalFlow = new DelayFlow(simulation, "Final") {
            @Override
            public long getDurationSample(es.ull.simulation.model.Element elem) {
                return 10;
            }
        };
        AbstractTimeFunction iterationFunction = new ConstantFunction(7);

        // When: creating a for-loop flow
        ForLoopFlow flow = new ForLoopFlow(simulation, initialFlow, finalFlow, iterationFunction);

        // Then: it should be created successfully
        assertNotNull(flow);
        assertEquals(iterationFunction, flow.getIterations());
    }

    @Test
    public void shouldBelongToSimulation() {
        // Given: a for-loop flow
        DelayFlow taskFlow = new DelayFlow(simulation, "Task") {
            @Override
            public long getDurationSample(es.ull.simulation.model.Element elem) {
                return 10;
            }
        };
        ForLoopFlow flow = new ForLoopFlow(simulation, taskFlow, 5);

        // Then: it should be registered in simulation
        assertTrue(simulation.getFlowList().contains(flow));
    }

    @Test
    public void shouldHaveObjectTypeIdentifier() {
        // Given: a for-loop flow
        DelayFlow taskFlow = new DelayFlow(simulation, "Task") {
            @Override
            public long getDurationSample(es.ull.simulation.model.Element elem) {
                return 10;
            }
        };
        ForLoopFlow flow = new ForLoopFlow(simulation, taskFlow, 5);

        // When: getting its identifier
        String id = flow.toString();

        // Then: it should have "F" prefix
        assertTrue(id.startsWith("[F"));
        assertTrue(id.endsWith("]"));
    }

    @Test
    public void shouldStoreIterationFunction() {
        // Given: a specific iteration function
        AbstractTimeFunction iterationFunction = new ConstantFunction(15);
        DelayFlow taskFlow = new DelayFlow(simulation, "Task") {
            @Override
            public long getDurationSample(es.ull.simulation.model.Element elem) {
                return 10;
            }
        };

        // When: creating a for-loop flow with this function
        ForLoopFlow flow = new ForLoopFlow(simulation, taskFlow, iterationFunction);

        // Then: the function should be accessible
        assertSame(iterationFunction, flow.getIterations());
    }

    @Test
    public void shouldHandleZeroIterations() {
        // Given: a for-loop flow with zero iterations
        DelayFlow taskFlow = new DelayFlow(simulation, "Task") {
            @Override
            public long getDurationSample(es.ull.simulation.model.Element elem) {
                return 10;
            }
        };
        ForLoopFlow flow = new ForLoopFlow(simulation, taskFlow, 0);

        // Then: it should be created successfully
        assertNotNull(flow);
        assertTrue(flow.getIterations() instanceof ConstantFunction);
    }

    @Test
    public void shouldStoreInitialFlow() {
        // Given: initial and final flows
        DelayFlow initialFlow = new DelayFlow(simulation, "Initial") {
            @Override
            public long getDurationSample(es.ull.simulation.model.Element elem) {
                return 5;
            }
        };
        DelayFlow finalFlow = new DelayFlow(simulation, "Final") {
            @Override
            public long getDurationSample(es.ull.simulation.model.Element elem) {
                return 10;
            }
        };

        // When: creating a for-loop flow
        ForLoopFlow flow = new ForLoopFlow(simulation, initialFlow, finalFlow, 3);

        // Then: initial flow should be accessible
        assertEquals(initialFlow, flow.getInitialFlow());
    }

    @Test
    public void shouldStoreFinalFlow() {
        // Given: initial and final flows
        DelayFlow initialFlow = new DelayFlow(simulation, "Initial") {
            @Override
            public long getDurationSample(es.ull.simulation.model.Element elem) {
                return 5;
            }
        };
        DelayFlow finalFlow = new DelayFlow(simulation, "Final") {
            @Override
            public long getDurationSample(es.ull.simulation.model.Element elem) {
                return 10;
            }
        };

        // When: creating a for-loop flow
        ForLoopFlow flow = new ForLoopFlow(simulation, initialFlow, finalFlow, 3);

        // Then: final flow should be accessible
        assertEquals(finalFlow, flow.getFinalFlow());
    }

    @Test
    public void shouldHandleLargeNumberOfIterations() {
        // Given: a for-loop flow with large number of iterations
        DelayFlow taskFlow = new DelayFlow(simulation, "Task") {
            @Override
            public long getDurationSample(es.ull.simulation.model.Element elem) {
                return 10;
            }
        };
        int largeIterations = 10000;
        ForLoopFlow flow = new ForLoopFlow(simulation, taskFlow, largeIterations);

        // Then: it should be created and store the iterations
        assertNotNull(flow);
        assertTrue(flow.getIterations() instanceof ConstantFunction);
    }
}
