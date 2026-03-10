package es.ull.simulation.model.flow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.ull.simulation.model.Simulation;

class ThreadMergeFlowTest {
    private Simulation simulation;

    @BeforeEach
    void setUp() {
        simulation = new Simulation(1, "Test Simulation");
    }

    // ── Constructores ─────────────────────────────────────────────────────────

    @Test
    void shouldCreateThreadMergeFlow_withNInstances_whenTwoParamConstructorUsed() {
        ThreadMergeFlow flow = new ThreadMergeFlow(simulation, 3);

        assertNotNull(flow);
        assertEquals(3, flow.getIncomingBranches());
        assertEquals(3, flow.getAcceptValue());
    }

    @Test
    void shouldCreateThreadMergeFlow_withNInstancesAndAcceptValue_whenThreeParamConstructorUsed() {
        ThreadMergeFlow flow = new ThreadMergeFlow(simulation, 5, 2);

        assertNotNull(flow);
        assertEquals(5, flow.getIncomingBranches());
        assertEquals(2, flow.getAcceptValue());
    }

    @Test
    void shouldBeRegisteredInSimulation_whenCreated() {
        ThreadMergeFlow flow = new ThreadMergeFlow(simulation, 2);

        assertNotNull(flow.getSimulation());
        assertEquals(simulation, flow.getSimulation());
    }

    // ── addPredecessor ────────────────────────────────────────────────────────

    @Test
    void shouldNotIncrementIncomingBranches_whenAddPredecessorCalled() {
        // ThreadMergeFlow.addPredecessor es un no-op (a diferencia del padre)
        ThreadMergeFlow flow = new ThreadMergeFlow(simulation, 4);
        int branchesBefore = flow.getIncomingBranches();

        // Crear un flujo predecesor y añadirlo
        ThreadMergeFlow predecessor = new ThreadMergeFlow(simulation, 1);
        flow.addPredecessor(predecessor);

        // Las ramas entrantes no deben cambiar (addPredecessor sobreescrito)
        assertEquals(branchesBefore, flow.getIncomingBranches());
    }

    @Test
    void shouldSupportDiscriminatorMode_whenAcceptValueIsOne() {
        // Thread Discriminator: acceptValue = 1 -> only first branch passes
        ThreadMergeFlow discriminator = new ThreadMergeFlow(simulation, 4, 1);

        assertEquals(4, discriminator.getIncomingBranches());
        assertEquals(1, discriminator.getAcceptValue());
    }

    @Test
    void shouldSupportPartialJoinMode_whenAcceptValueIsIntermediate() {
        // Partial join: acceptValue between 1 and nInstances
        ThreadMergeFlow partialJoin = new ThreadMergeFlow(simulation, 6, 3);

        assertEquals(6, partialJoin.getIncomingBranches());
        assertEquals(3, partialJoin.getAcceptValue());
    }
}
