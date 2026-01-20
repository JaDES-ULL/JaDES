package es.ull.simulation.model.flow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.ull.simulation.condition.AbstractCondition;
import es.ull.simulation.condition.TrueCondition;
import es.ull.simulation.model.ElementInstance;
import es.ull.simulation.model.Simulation;

class StructuredSynchroMergeFlowTest {
    private Simulation simulation;

    @BeforeEach
    void setUp() {
        simulation = new Simulation(1, "Test Simulation");
    }

    @Test
    void shouldInitializeWithMultiChoiceAndSynchronization() {
        StructuredSynchroMergeFlow flow = new StructuredSynchroMergeFlow(simulation);

        assertTrue(flow.getInitialFlow() instanceof MultiChoiceFlow);
        assertTrue(flow.getFinalFlow() instanceof SynchronizationFlow);
        assertSame(flow, ((BasicFlow) flow.getInitialFlow()).getParent());
        assertSame(flow, ((BasicFlow) flow.getFinalFlow()).getParent());
    }

    @Test
    void shouldAddBranchWithCondition() {
        StructuredSynchroMergeFlow flow = new StructuredSynchroMergeFlow(simulation);
        DummyTaskFlow branch = new DummyTaskFlow(simulation);
        AbstractCondition<ElementInstance> condition = new AbstractCondition<>() {
            @Override
            public boolean check(ElementInstance ei) {
                return false;
            }
        };

        flow.addBranch(branch, condition);

        MultiChoiceFlow initial = (MultiChoiceFlow) flow.getInitialFlow();
        assertEquals(1, initial.getSuccessorList().size());
        assertSame(branch, initial.getSuccessorList().get(0));
        assertSame(condition, initial.getConditionList().get(0));
        assertSame(flow.getFinalFlow(), branch.getSuccessor());
    }

    @Test
    void shouldAddBranchWithDefaultTrueCondition() {
        StructuredSynchroMergeFlow flow = new StructuredSynchroMergeFlow(simulation);
        DummyTaskFlow branch = new DummyTaskFlow(simulation);

        flow.addBranch(branch);

        MultiChoiceFlow initial = (MultiChoiceFlow) flow.getInitialFlow();
        assertEquals(1, initial.getSuccessorList().size());
        assertTrue(initial.getConditionList().get(0) instanceof TrueCondition);
        assertSame(flow.getFinalFlow(), branch.getSuccessor());
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
