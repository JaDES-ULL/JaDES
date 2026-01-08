package es.ull.simulation.model.flow;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.ull.simulation.condition.AbstractCondition;
import es.ull.simulation.model.ElementInstance;
import es.ull.simulation.model.Simulation;
import es.ull.simulation.model.TimeUnit;

/**
 * Test class for {@link DoWhileFlow}.
 * Tests do-while loop flow pattern.
 */
public class DoWhileFlowTest {

    private Simulation simulation;

    @BeforeEach
    public void setUp() {
        simulation = new Simulation(0, "Test Simulation", TimeUnit.MINUTE);
    }

    @Test
    public void shouldCreateDoWhileFlowWithSingleTaskFlow() {
        // Given: a task flow and a condition
        DelayFlow taskFlow = new DelayFlow(simulation, "Task") {
            @Override
            public long getDurationSample(es.ull.simulation.model.Element elem) {
                return 10;
            }
        };
        AbstractCondition<ElementInstance> condition = new AbstractCondition<ElementInstance>() {
            @Override
            public boolean check(ElementInstance fe) {
                return true;
            }
        };

        // When: creating a do-while flow with single task
        DoWhileFlow flow = new DoWhileFlow(simulation, taskFlow, condition);

        // Then: it should be created successfully
        assertNotNull(flow);
        assertEquals(condition, flow.getCondition());
    }

    @Test
    public void shouldCreateDoWhileFlowWithSeparateFlows() {
        // Given: initial and final flows, and a condition
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
        AbstractCondition<ElementInstance> condition = new AbstractCondition<ElementInstance>() {
            @Override
            public boolean check(ElementInstance fe) {
                return false;
            }
        };

        // When: creating a do-while flow with separate initial/final flows
        DoWhileFlow flow = new DoWhileFlow(simulation, initialFlow, finalFlow, condition);

        // Then: it should be created successfully
        assertNotNull(flow);
        assertEquals(condition, flow.getCondition());
    }

    @Test
    public void shouldBelongToSimulation() {
        // Given: a do-while flow
        DelayFlow taskFlow = new DelayFlow(simulation, "Task") {
            @Override
            public long getDurationSample(es.ull.simulation.model.Element elem) {
                return 10;
            }
        };
        AbstractCondition<ElementInstance> condition = new AbstractCondition<ElementInstance>() {
            @Override
            public boolean check(ElementInstance fe) {
                return true;
            }
        };
        DoWhileFlow flow = new DoWhileFlow(simulation, taskFlow, condition);

        // Then: it should be registered in simulation
        assertTrue(simulation.getFlowList().contains(flow));
    }

    @Test
    public void shouldHaveObjectTypeIdentifier() {
        // Given: a do-while flow
        DelayFlow taskFlow = new DelayFlow(simulation, "Task") {
            @Override
            public long getDurationSample(es.ull.simulation.model.Element elem) {
                return 10;
            }
        };
        AbstractCondition<ElementInstance> condition = new AbstractCondition<ElementInstance>() {
            @Override
            public boolean check(ElementInstance fe) {
                return true;
            }
        };
        DoWhileFlow flow = new DoWhileFlow(simulation, taskFlow, condition);

        // When: getting its identifier
        String id = flow.toString();

        // Then: it should have "F" prefix
        assertTrue(id.startsWith("[F"));
        assertTrue(id.endsWith("]"));
    }

    @Test
    public void shouldStoreCondition() {
        // Given: a specific condition
        AbstractCondition<ElementInstance> condition = new AbstractCondition<ElementInstance>() {
            private boolean checkValue = true;
            
            @Override
            public boolean check(ElementInstance fe) {
                return checkValue;
            }
        };
        DelayFlow taskFlow = new DelayFlow(simulation, "Task") {
            @Override
            public long getDurationSample(es.ull.simulation.model.Element elem) {
                return 10;
            }
        };

        // When: creating a do-while flow with this condition
        DoWhileFlow flow = new DoWhileFlow(simulation, taskFlow, condition);

        // Then: the condition should be accessible
        assertSame(condition, flow.getCondition());
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
        AbstractCondition<ElementInstance> condition = new AbstractCondition<ElementInstance>() {
            @Override
            public boolean check(ElementInstance fe) {
                return false;
            }
        };

        // When: creating a do-while flow
        DoWhileFlow flow = new DoWhileFlow(simulation, initialFlow, finalFlow, condition);

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
        AbstractCondition<ElementInstance> condition = new AbstractCondition<ElementInstance>() {
            @Override
            public boolean check(ElementInstance fe) {
                return false;
            }
        };

        // When: creating a do-while flow
        DoWhileFlow flow = new DoWhileFlow(simulation, initialFlow, finalFlow, condition);

        // Then: final flow should be accessible
        assertEquals(finalFlow, flow.getFinalFlow());
    }
}
