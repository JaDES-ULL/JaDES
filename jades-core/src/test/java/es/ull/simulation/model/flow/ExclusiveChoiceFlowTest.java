package es.ull.simulation.model.flow;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.ull.simulation.condition.AbstractCondition;
import es.ull.simulation.condition.TrueCondition;
import es.ull.simulation.model.ElementInstance;
import es.ull.simulation.model.Simulation;
import es.ull.simulation.model.TimeUnit;

import java.util.Arrays;

/**
 * Test class for {@link ExclusiveChoiceFlow}.
 * Tests exclusive choice flow pattern (WFP4).
 */
public class ExclusiveChoiceFlowTest {

    private Simulation simulation;

    @BeforeEach
    public void setUp() {
        simulation = new Simulation(0, "Test Simulation", TimeUnit.MINUTE);
    }

    @Test
    public void shouldCreateExclusiveChoiceFlow() {
        // Given: a simulation
        // When: creating an exclusive choice flow
        ExclusiveChoiceFlow flow = new ExclusiveChoiceFlow(simulation);

        // Then: it should be created successfully
        assertNotNull(flow);
    }

    @Test
    public void shouldBelongToSimulation() {
        // Given: a simulation
        // When: creating an exclusive choice flow
        ExclusiveChoiceFlow flow = new ExclusiveChoiceFlow(simulation);

        // Then: it should be registered in simulation
        assertTrue(simulation.getFlowList().contains(flow));
    }

    @Test
    public void shouldHaveObjectTypeIdentifier() {
        // Given: an exclusive choice flow
        ExclusiveChoiceFlow flow = new ExclusiveChoiceFlow(simulation);

        // When: getting its identifier
        String id = flow.toString();

        // Then: it should have "F" prefix
        assertTrue(id.startsWith("[F"));
        assertTrue(id.endsWith("]"));
    }

    @Test
    public void shouldInitiallyHaveNoSuccessors() {
        // Given: a newly created exclusive choice flow
        ExclusiveChoiceFlow flow = new ExclusiveChoiceFlow(simulation);

        // When: checking successors
        int successorCount = flow.getSuccessorList().size();

        // Then: it should have no successors
        assertEquals(0, successorCount);
    }

    @Test
    public void shouldAllowLinkingSuccessorWithDefaultCondition() {
        // Given: an exclusive choice flow and a successor
        ExclusiveChoiceFlow flow = new ExclusiveChoiceFlow(simulation);
        DelayFlow successor = new DelayFlow(simulation, "Successor") {
            @Override
            public long getDurationSample(es.ull.simulation.model.Element elem) {
                return 10;
            }
        };

        // When: linking the successor without explicit condition
        flow.link(successor);

        // Then: successor should be added with TrueCondition
        assertEquals(1, flow.getSuccessorList().size());
        assertEquals(successor, flow.getSuccessorList().get(0));
        assertEquals(1, flow.getConditionList().size());
        assertTrue(flow.getConditionList().get(0) instanceof TrueCondition);
    }

    @Test
    public void shouldAllowLinkingSuccessorWithCustomCondition() {
        // Given: an exclusive choice flow, a successor, and a condition
        ExclusiveChoiceFlow flow = new ExclusiveChoiceFlow(simulation);
        DelayFlow successor = new DelayFlow(simulation, "Successor") {
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

        // When: linking the successor with a custom condition
        flow.link(successor, condition);

        // Then: successor and condition should be added
        assertEquals(1, flow.getSuccessorList().size());
        assertEquals(successor, flow.getSuccessorList().get(0));
        assertEquals(1, flow.getConditionList().size());
        assertEquals(condition, flow.getConditionList().get(0));
    }

    @Test
    public void shouldAllowLinkingMultipleSuccessors() {
        // Given: an exclusive choice flow and multiple successors
        ExclusiveChoiceFlow flow = new ExclusiveChoiceFlow(simulation);
        DelayFlow successor1 = new DelayFlow(simulation, "Successor1") {
            @Override
            public long getDurationSample(es.ull.simulation.model.Element elem) {
                return 10;
            }
        };
        DelayFlow successor2 = new DelayFlow(simulation, "Successor2") {
            @Override
            public long getDurationSample(es.ull.simulation.model.Element elem) {
                return 20;
            }
        };

        // When: linking multiple successors
        flow.link(Arrays.asList(successor1, successor2));

        // Then: all successors should be added
        assertEquals(2, flow.getSuccessorList().size());
        assertTrue(flow.getSuccessorList().contains(successor1));
        assertTrue(flow.getSuccessorList().contains(successor2));
        assertEquals(2, flow.getConditionList().size());
    }

    @Test
    public void shouldAllowLinkingMultipleSuccessorsWithConditions() {
        // Given: an exclusive choice flow, successors, and conditions
        ExclusiveChoiceFlow flow = new ExclusiveChoiceFlow(simulation);
        DelayFlow successor1 = new DelayFlow(simulation, "Successor1") {
            @Override
            public long getDurationSample(es.ull.simulation.model.Element elem) {
                return 10;
            }
        };
        DelayFlow successor2 = new DelayFlow(simulation, "Successor2") {
            @Override
            public long getDurationSample(es.ull.simulation.model.Element elem) {
                return 20;
            }
        };
        AbstractCondition<ElementInstance> cond1 = new TrueCondition<ElementInstance>();
        AbstractCondition<ElementInstance> cond2 = new AbstractCondition<ElementInstance>() {
            @Override
            public boolean check(ElementInstance fe) {
                return false;
            }
        };

        // When: linking successors with conditions
        flow.link(Arrays.asList(successor1, successor2), Arrays.asList(cond1, cond2));

        // Then: all successors and conditions should be added
        assertEquals(2, flow.getSuccessorList().size());
        assertEquals(2, flow.getConditionList().size());
        assertEquals(cond1, flow.getConditionList().get(0));
        assertEquals(cond2, flow.getConditionList().get(1));
    }

    @Test
    public void shouldReturnLinkedSuccessor() {
        // Given: an exclusive choice flow and a successor
        ExclusiveChoiceFlow flow = new ExclusiveChoiceFlow(simulation);
        DelayFlow successor = new DelayFlow(simulation, "Successor") {
            @Override
            public long getDurationSample(es.ull.simulation.model.Element elem) {
                return 10;
            }
        };

        // When: linking the successor
        IFlow returned = flow.link(successor);

        // Then: the linked successor should be returned
        assertSame(successor, returned);
    }

    @Test
    public void shouldReturnLinkedSuccessorWithCondition() {
        // Given: an exclusive choice flow, a successor, and a condition
        ExclusiveChoiceFlow flow = new ExclusiveChoiceFlow(simulation);
        DelayFlow successor = new DelayFlow(simulation, "Successor") {
            @Override
            public long getDurationSample(es.ull.simulation.model.Element elem) {
                return 10;
            }
        };
        AbstractCondition<ElementInstance> condition = new TrueCondition<ElementInstance>();

        // When: linking the successor with a condition
        IFlow returned = flow.link(successor, condition);

        // Then: the linked successor should be returned
        assertSame(successor, returned);
    }
}
