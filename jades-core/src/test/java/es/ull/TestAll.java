package es.ull;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import es.ull.StandardTestSimulation.TestArguments;
import es.ull.simulation.experiment.BaseExperiment;

public class TestAll {
    public enum TestType {
        WAIT_FOR_SIGNAL,
        CONDITIONAL_RESOURCE_GENERATOR,
        INTERRUPTIBLE_ACTIVITIES,
        PRIORITY_ELEMENTS
    }
    public static class TestMoreFlowsExperiment extends BaseExperiment {
        final private TestType expId;
        final private TestArguments args;
        public TestMoreFlowsExperiment(TestType expId, TestArguments args) {
            super("Testing More Flows", args);
            this.expId = expId;
            this.args = args;
        }

        @Override
        public void runExperiment(int ind) {
            StandardTestSimulation simul = null;
            switch (expId) {
                case WAIT_FOR_SIGNAL:
                    args.simEnd = 35;
                    args.nElements = 10;
                    simul = new TestWaitForSignalFlowSimulation(args);
                    break;            
                case CONDITIONAL_RESOURCE_GENERATOR:
                    args.simEnd = 60;
                    args.nElements = 1;
                    args.resStart = 0;
                    simul = new TestConditionalResourceGeneratorSimulation(args);
                    break;
                case INTERRUPTIBLE_ACTIVITIES:
                    args.simEnd = 400L;
                    args.nElements = 1;
                    args.resStart = 20L;
                    args.resPeriod	= 100L;
                    args.resAvailability = 40L;
                    simul = new TestInterruptibleActivitiesSimulation(args);
                    break;
                case PRIORITY_ELEMENTS:
                default:
                    args.simEnd = 400L;
                    args.nElements = 10;
                    args.resStart = 20L;
                    args.resPeriod	= 100L;
                    args.resAvailability = 40L;
                    simul = new TestPriorityElementSimulation(args);
                    break;
            }
            simul.run();
        }
    }

    @ParameterizedTest
    @EnumSource(TestType.class)
    public void testAll(TestType type) {
        final TestArguments arguments = new TestArguments();
        arguments.quiet = true;
        final TestMoreFlowsExperiment experiment = new TestMoreFlowsExperiment(type, arguments);
        experiment.run();
    }
}
