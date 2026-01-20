package es.ull.simulation.model.flow;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.ull.simulation.model.ElementInstance;
import es.ull.simulation.model.Simulation;

class StructuredLoopFlowTest {
    private Simulation simulation;

    @BeforeEach
    void setUp() {
        simulation = new Simulation(1, "Test Simulation");
    }

    @Test
    void shouldUseSingleFlowAsInitialAndFinal() {
        DummyTaskFlow subflow = new DummyTaskFlow(simulation);
        DummyLoopFlow loop = new DummyLoopFlow(simulation, subflow);

        assertSame(subflow, loop.getInitialFlow());
        assertSame(subflow, loop.getFinalFlow());
        assertSame(loop, ((BasicFlow) subflow).getParent());
    }

    @Test
    void shouldAssignParentForInitialAndFinalFlows() {
        DummyTaskFlow initial = new DummyTaskFlow(simulation);
        DummyTaskFlow fin = new DummyTaskFlow(simulation);
        initial.link(fin);

        DummyLoopFlow loop = new DummyLoopFlow(simulation, initial, fin);

        assertSame(initial, loop.getInitialFlow());
        assertSame(fin, loop.getFinalFlow());
        assertSame(loop, ((BasicFlow) initial).getParent());
        assertSame(loop, ((BasicFlow) fin).getParent());
        assertTrue(initial.getSuccessor() == fin);
    }

    private static final class DummyLoopFlow extends StructuredLoopFlow {
        private DummyLoopFlow(Simulation model, IInitializerFlow initial, IFinalizerFlow fin) {
            super(model, initial, fin);
        }

        private DummyLoopFlow(Simulation model, ITaskFlow subflow) {
            super(model, subflow);
        }
    }

    private static final class DummyTaskFlow extends AbstractSingleSuccessorFlow implements ITaskFlow {
        private DummyTaskFlow(Simulation model) {
            super(model);
        }

        @Override
        public void request(ElementInstance ei) {
            next(ei);
        }

        @Override
        public void addPredecessor(IFlow predecessor) {
        }

        @Override
        public void finish(ElementInstance ei) {
            next(ei);
        }

        @Override
        public void afterFinalize(ElementInstance ei) {
        }
    }
}
