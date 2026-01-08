package es.ull.simulation.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.TreeSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.ull.simulation.model.flow.ActivityFlow;
import es.ull.simulation.model.flow.IFlow;

class WorkTokenTest {
    private Simulation simulation;
    private IFlow flow1;
    private IFlow flow2;
    private IFlow flow3;

    @BeforeEach
    void setUp() {
        simulation = new Simulation(1, "Test Simulation");
        flow1 = new ActivityFlow(simulation, "Flow1");
        flow2 = new ActivityFlow(simulation, "Flow2");
        flow3 = new ActivityFlow(simulation, "Flow3");
    }

    @Test
    void shouldCreateTokenWithState() {
        WorkToken token = new WorkToken(true);

        assertNotNull(token);
        assertTrue(token.isExecutable());
        assertNotNull(token.getPath());
        assertTrue(token.getPath().isEmpty());
    }

    @Test
    void shouldCreateTokenWithFalseState() {
        WorkToken token = new WorkToken(false);

        assertFalse(token.isExecutable());
        assertTrue(token.getPath().isEmpty());
    }

    @Test
    void shouldCreateTokenWithStateAndStartPoint() {
        WorkToken token = new WorkToken(true, flow1);

        assertTrue(token.isExecutable());
        assertFalse(token.getPath().isEmpty());
        assertEquals(1, token.getPath().size());
        assertTrue(token.wasVisited(flow1));
    }

    @Test
    void shouldCreateCopyOfToken() {
        WorkToken originalToken = new WorkToken(true, flow1);
        originalToken.addFlow(flow2);

        WorkToken copiedToken = new WorkToken(originalToken);

        assertTrue(copiedToken.isExecutable());
        assertEquals(2, copiedToken.getPath().size());
        assertTrue(copiedToken.wasVisited(flow1));
        assertTrue(copiedToken.wasVisited(flow2));
    }

    @Test
    void shouldResetToken() {
        WorkToken token = new WorkToken(true, flow1);
        token.addFlow(flow2);

        token.reset();

        assertFalse(token.isExecutable());
        assertTrue(token.getPath().isEmpty());
    }

    @Test
    void shouldAddFlow() {
        WorkToken token = new WorkToken(true);

        token.addFlow(flow1);

        assertEquals(1, token.getPath().size());
        assertTrue(token.wasVisited(flow1));
    }

    @Test
    void shouldAddMultipleFlows() {
        WorkToken token = new WorkToken(true);

        token.addFlow(flow1);
        token.addFlow(flow2);
        token.addFlow(flow3);

        assertEquals(3, token.getPath().size());
        assertTrue(token.wasVisited(flow1));
        assertTrue(token.wasVisited(flow2));
        assertTrue(token.wasVisited(flow3));
    }

    @Test
    void shouldAddCollectionOfFlows() {
        WorkToken token = new WorkToken(true);
        TreeSet<IFlow> flows = new TreeSet<>();
        flows.add(flow1);
        flows.add(flow2);

        token.addFlow(flows);

        assertEquals(2, token.getPath().size());
        assertTrue(token.wasVisited(flow1));
        assertTrue(token.wasVisited(flow2));
    }

    @Test
    void shouldNotReportVisitedForNewFlow() {
        WorkToken token = new WorkToken(true, flow1);

        assertFalse(token.wasVisited(flow2));
    }

    @Test
    void shouldSetState() {
        WorkToken token = new WorkToken(true);

        token.setState(false);

        assertFalse(token.isExecutable());
    }

    @Test
    void shouldToggleState() {
        WorkToken token = new WorkToken(true);

        token.setState(false);
        assertFalse(token.isExecutable());

        token.setState(true);
        assertTrue(token.isExecutable());
    }

    @Test
    void shouldNotDuplicateFlowsInPath() {
        WorkToken token = new WorkToken(true);

        token.addFlow(flow1);
        token.addFlow(flow1);

        assertEquals(1, token.getPath().size());
    }

    @Test
    void shouldMaintainFlowOrderInTreeSet() {
        WorkToken token = new WorkToken(true);
        
        token.addFlow(flow3);
        token.addFlow(flow1);
        token.addFlow(flow2);

        assertEquals(3, token.getPath().size());
        // TreeSet mantiene orden natural
        assertTrue(token.wasVisited(flow1));
        assertTrue(token.wasVisited(flow2));
        assertTrue(token.wasVisited(flow3));
    }

    @Test
    void shouldCopyTokenWithFalseState() {
        WorkToken originalToken = new WorkToken(false, flow1);

        WorkToken copiedToken = new WorkToken(originalToken);

        assertFalse(copiedToken.isExecutable());
        assertEquals(1, copiedToken.getPath().size());
        assertTrue(copiedToken.wasVisited(flow1));
    }

    @Test
    void shouldResetMultipleTimes() {
        WorkToken token = new WorkToken(true, flow1);

        token.reset();
        assertFalse(token.isExecutable());
        assertTrue(token.getPath().isEmpty());

        token.setState(true);
        token.addFlow(flow2);

        token.reset();
        assertFalse(token.isExecutable());
        assertTrue(token.getPath().isEmpty());
    }

    @Test
    void shouldHandleEmptyCollectionOfFlows() {
        WorkToken token = new WorkToken(true);
        TreeSet<IFlow> emptyFlows = new TreeSet<>();

        token.addFlow(emptyFlows);

        assertTrue(token.getPath().isEmpty());
    }

    @Test
    void shouldCopyEmptyToken() {
        WorkToken originalToken = new WorkToken(true);

        WorkToken copiedToken = new WorkToken(originalToken);

        assertTrue(copiedToken.isExecutable());
        assertTrue(copiedToken.getPath().isEmpty());
    }
}
