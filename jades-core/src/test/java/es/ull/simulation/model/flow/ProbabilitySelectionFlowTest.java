package es.ull.simulation.model.flow;

import es.ull.simulation.model.Simulation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ProbabilitySelectionFlow
 */
class ProbabilitySelectionFlowTest {
    private Simulation simulation;
    private ProbabilitySelectionFlow probabilitySelectionFlow;

    @BeforeEach
    void setUp() {
        simulation = new Simulation(1, "Test Simulation");
        probabilitySelectionFlow = new ProbabilitySelectionFlow(simulation);
    }

    @Test
    void shouldCreateProbabilitySelectionFlow_whenValidSimulationProvided() {
        assertNotNull(probabilitySelectionFlow);
        assertNotNull(probabilitySelectionFlow.getProbabilities());
        assertTrue(probabilitySelectionFlow.getProbabilities().isEmpty());
    }

    @Test
    void shouldExtendMultipleSuccessorFlow() {
        assertTrue(probabilitySelectionFlow instanceof MultipleSuccessorFlow);
    }

    @Test
    void shouldHaveEmptyProbabilitiesList_whenCreated() {
        ProbabilitySelectionFlow flow = new ProbabilitySelectionFlow(simulation);
        assertEquals(0, flow.getProbabilities().size());
    }

    @Test
    void shouldReturnProbabilitiesList() {
        ArrayList<Double> probs = probabilitySelectionFlow.getProbabilities();
        assertNotNull(probs);
        assertTrue(probs instanceof ArrayList);
    }

    @Test
    void shouldLinkSuccessor_withExplicitProbability() {
        UserActionFlow successor = new UserActionFlow(simulation, "Test");
        IFlow linkedFlow = probabilitySelectionFlow.link(successor, 0.5);

        assertEquals(successor, linkedFlow);
        assertEquals(1, probabilitySelectionFlow.getProbabilities().size());
        assertEquals(0.5, probabilitySelectionFlow.getProbabilities().get(0), 0.001);
    }

    @Test
    void shouldLinkSuccessor_withDefaultProbability() {
        UserActionFlow successor = new UserActionFlow(simulation, "Test");
        IFlow linkedFlow = probabilitySelectionFlow.link(successor);

        assertEquals(successor, linkedFlow);
        assertEquals(1, probabilitySelectionFlow.getProbabilities().size());
        assertEquals(1.0, probabilitySelectionFlow.getProbabilities().get(0), 0.001);
    }

    @Test
    void shouldLinkMultipleSuccessors_withIndividualProbabilities() {
        UserActionFlow successor1 = new UserActionFlow(simulation, "Test1");
        UserActionFlow successor2 = new UserActionFlow(simulation, "Test2");
        UserActionFlow successor3 = new UserActionFlow(simulation, "Test3");

        probabilitySelectionFlow.link(successor1, 0.2);
        probabilitySelectionFlow.link(successor2, 0.3);
        probabilitySelectionFlow.link(successor3, 0.5);

        assertEquals(3, probabilitySelectionFlow.getProbabilities().size());
        assertEquals(0.2, probabilitySelectionFlow.getProbabilities().get(0), 0.001);
        assertEquals(0.3, probabilitySelectionFlow.getProbabilities().get(1), 0.001);
        assertEquals(0.5, probabilitySelectionFlow.getProbabilities().get(2), 0.001);
    }

    @Test
    void shouldLinkCollection_withExplicitProbabilities() {
        Collection<IFlow> successors = Arrays.asList(
            new UserActionFlow(simulation, "Test1"),
            new UserActionFlow(simulation, "Test2"),
            new UserActionFlow(simulation, "Test3")
        );
        Collection<Double> probabilities = Arrays.asList(0.25, 0.35, 0.40);

        probabilitySelectionFlow.link(successors, probabilities);

        assertEquals(3, probabilitySelectionFlow.getProbabilities().size());
        assertEquals(0.25, probabilitySelectionFlow.getProbabilities().get(0), 0.001);
        assertEquals(0.35, probabilitySelectionFlow.getProbabilities().get(1), 0.001);
        assertEquals(0.40, probabilitySelectionFlow.getProbabilities().get(2), 0.001);
    }

    @Test
    void shouldLinkCollection_withEqualProbabilities() {
        Collection<IFlow> successors = Arrays.asList(
            new UserActionFlow(simulation, "Test1"),
            new UserActionFlow(simulation, "Test2"),
            new UserActionFlow(simulation, "Test3")
        );

        probabilitySelectionFlow.link(successors);

        assertEquals(3, probabilitySelectionFlow.getProbabilities().size());
        // Each should have probability of 1/3 ≈ 0.333
        for (Double prob : probabilitySelectionFlow.getProbabilities()) {
            assertEquals(1.0 / 3.0, prob, 0.001);
        }
    }

    @Test
    void shouldDistributeEqualProbabilities_forTwoSuccessors() {
        Collection<IFlow> successors = Arrays.asList(
            new UserActionFlow(simulation, "Test1"),
            new UserActionFlow(simulation, "Test2")
        );

        probabilitySelectionFlow.link(successors);

        assertEquals(2, probabilitySelectionFlow.getProbabilities().size());
        assertEquals(0.5, probabilitySelectionFlow.getProbabilities().get(0), 0.001);
        assertEquals(0.5, probabilitySelectionFlow.getProbabilities().get(1), 0.001);
    }

    @Test
    void shouldDistributeEqualProbabilities_forFourSuccessors() {
        Collection<IFlow> successors = Arrays.asList(
            new UserActionFlow(simulation, "Test1"),
            new UserActionFlow(simulation, "Test2"),
            new UserActionFlow(simulation, "Test3"),
            new UserActionFlow(simulation, "Test4")
        );

        probabilitySelectionFlow.link(successors);

        assertEquals(4, probabilitySelectionFlow.getProbabilities().size());
        for (Double prob : probabilitySelectionFlow.getProbabilities()) {
            assertEquals(0.25, prob, 0.001);
        }
    }

    @Test
    void shouldHandleZeroProbability() {
        UserActionFlow successor = new UserActionFlow(simulation, "Test");
        probabilitySelectionFlow.link(successor, 0.0);

        assertEquals(1, probabilitySelectionFlow.getProbabilities().size());
        assertEquals(0.0, probabilitySelectionFlow.getProbabilities().get(0), 0.001);
    }

    @Test
    void shouldHandleMaximumProbability() {
        UserActionFlow successor = new UserActionFlow(simulation, "Test");
        probabilitySelectionFlow.link(successor, 1.0);

        assertEquals(1, probabilitySelectionFlow.getProbabilities().size());
        assertEquals(1.0, probabilitySelectionFlow.getProbabilities().get(0), 0.001);
    }

    @Test
    void shouldAllowProbabilitiesGreaterThanOne() {
        // Note: The implementation doesn't enforce 0-1 range, just stores values
        UserActionFlow successor = new UserActionFlow(simulation, "Test");
        probabilitySelectionFlow.link(successor, 1.5);

        assertEquals(1, probabilitySelectionFlow.getProbabilities().size());
        assertEquals(1.5, probabilitySelectionFlow.getProbabilities().get(0), 0.001);
    }

    @Test
    void shouldAccumulateMultipleLinkCalls() {
        probabilitySelectionFlow.link(new UserActionFlow(simulation, "Test1"), 0.1);
        probabilitySelectionFlow.link(new UserActionFlow(simulation, "Test2"), 0.2);

        Collection<IFlow> moreSuccessors = Arrays.asList(
            new UserActionFlow(simulation, "Test3"),
            new UserActionFlow(simulation, "Test4")
        );
        probabilitySelectionFlow.link(moreSuccessors);

        assertEquals(4, probabilitySelectionFlow.getProbabilities().size());
        assertEquals(0.1, probabilitySelectionFlow.getProbabilities().get(0), 0.001);
        assertEquals(0.2, probabilitySelectionFlow.getProbabilities().get(1), 0.001);
        assertEquals(0.5, probabilitySelectionFlow.getProbabilities().get(2), 0.001);
        assertEquals(0.5, probabilitySelectionFlow.getProbabilities().get(3), 0.001);
    }
}
