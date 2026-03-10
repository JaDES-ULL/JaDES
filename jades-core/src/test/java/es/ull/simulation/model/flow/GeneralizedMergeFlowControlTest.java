package es.ull.simulation.model.flow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.ull.simulation.model.Simulation;
import es.ull.simulation.model.WorkToken;

class GeneralizedMergeFlowControlTest {
    private Simulation simulation;

    @BeforeEach
    void setUp() {
        simulation = new Simulation(1, "Test Simulation");
    }

    private GeneralizedMergeFlowControl createControl(int nInstances,
                                                       Map<IFlow, LinkedList<WorkToken>> map) {
        ThreadMergeFlow flow = new ThreadMergeFlow(simulation, nInstances);
        return new GeneralizedMergeFlowControl(flow, map);
    }

    // ── canReset ──────────────────────────────────────────────────────────────

    @Test
    void shouldReturnTrue_whenCanResetWithEmptyMapAndZeroCheckValue() {
        GeneralizedMergeFlowControl ctrl = createControl(3, new HashMap<>());
        assertTrue(ctrl.canReset(0));
    }

    @Test
    void shouldReturnFalse_whenCanResetWithEmptyMapAndNonZeroCheckValue() {
        GeneralizedMergeFlowControl ctrl = createControl(3, new HashMap<>());
        assertFalse(ctrl.canReset(1));
        assertFalse(ctrl.canReset(3));
    }

    // ── reset ─────────────────────────────────────────────────────────────────

    @Test
    void shouldReturnTrue_whenResetWithEmptyIncBranches() {
        GeneralizedMergeFlowControl ctrl = createControl(2, new HashMap<>());
        assertTrue(ctrl.reset());
    }

    // ── isActivated / setActivated ────────────────────────────────────────────

    @Test
    void shouldBeFalse_whenActivatedInitially() {
        GeneralizedMergeFlowControl ctrl = createControl(3, new HashMap<>());
        assertFalse(ctrl.isActivated());
    }

    @Test
    void shouldBeTrue_whenActivatedAfterSetActivated() {
        GeneralizedMergeFlowControl ctrl = createControl(3, new HashMap<>());
        ctrl.setActivated();
        assertTrue(ctrl.isActivated());
    }

    // ── getTrueChecked ────────────────────────────────────────────────────────

    @Test
    void shouldReturnZero_whenGetTrueCheckedInitially() {
        GeneralizedMergeFlowControl ctrl = createControl(3, new HashMap<>());
        assertEquals(0, ctrl.getTrueChecked());
    }

    // ── getOutgoingFalseToken ─────────────────────────────────────────────────

    @Test
    void shouldReturnNonNull_whenGetOutgoingFalseToken() {
        GeneralizedMergeFlowControl ctrl = createControl(3, new HashMap<>());
        assertNotNull(ctrl.getOutgoingFalseToken());
    }
}
