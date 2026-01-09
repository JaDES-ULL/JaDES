package es.ull.simulation.experiment;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.ull.simulation.model.Simulation;

class BaseExperimentTest {

    private TestExperiment experiment;
    private CommonArguments arguments;

    @BeforeEach
    void setUp() {
        arguments = new CommonArguments();
        arguments.nRuns = 5;
        arguments.seed = 42L;
        arguments.parallel = false;
        arguments.nThreads = 1;
        arguments.quiet = false;
        experiment = new TestExperiment("Test Experiment", arguments);
    }

    @Test
    void shouldCreateBaseExperiment_withValidArguments() {
        assertNotNull(experiment);
        assertEquals("Test Experiment", experiment.getDescription());
        assertEquals(5, experiment.getNExperiments());
        assertEquals(arguments, experiment.getArguments());
    }

    @Test
    void shouldGetDescription() {
        String description = experiment.getDescription();

        assertEquals("Test Experiment", description);
        assertNotNull(description);
    }

    @Test
    void shouldGetNExperiments() {
        int nExperiments = experiment.getNExperiments();

        assertEquals(5, nExperiments);
        assertTrue(nExperiments > 0);
    }

    @Test
    void shouldGetArguments() {
        CommonArguments args = experiment.getArguments();

        assertNotNull(args);
        assertEquals(5, args.nRuns);
        assertEquals(42L, args.seed);
        assertFalse(args.parallel);
        assertEquals(1, args.nThreads);
        assertFalse(args.quiet);
    }

    @Test
    void shouldCreateBaseExperiment_withZeroExperiments() {
        CommonArguments zeroArgs = new CommonArguments();
        zeroArgs.nRuns = 0;
        TestExperiment zeroExperiment = new TestExperiment("Zero Experiment", zeroArgs);

        assertEquals(0, zeroExperiment.getNExperiments());
    }

    @Test
    void shouldCreateBaseExperiment_withParallelFlag() {
        CommonArguments parallelArgs = new CommonArguments();
        parallelArgs.nRuns = 10;
        parallelArgs.parallel = true;
        parallelArgs.nThreads = 4;
        TestExperiment parallelExperiment = new TestExperiment("Parallel Experiment", parallelArgs);

        assertTrue(parallelExperiment.getArguments().parallel);
        assertEquals(4, parallelExperiment.getArguments().nThreads);
    }

    @Test
    void shouldCreateBaseExperiment_withQuietMode() {
        CommonArguments quietArgs = new CommonArguments();
        quietArgs.nRuns = 3;
        quietArgs.quiet = true;
        TestExperiment quietExperiment = new TestExperiment("Quiet Experiment", quietArgs);

        assertTrue(quietExperiment.getArguments().quiet);
    }

    @Test
    void shouldCreateBaseExperiment_withDifferentSeeds() {
        CommonArguments args1 = new CommonArguments();
        args1.nRuns = 2;
        args1.seed = 100L;
        CommonArguments args2 = new CommonArguments();
        args2.nRuns = 2;
        args2.seed = 200L;

        TestExperiment exp1 = new TestExperiment("Exp1", args1);
        TestExperiment exp2 = new TestExperiment("Exp2", args2);

        assertEquals(100L, exp1.getArguments().seed);
        assertEquals(200L, exp2.getArguments().seed);
    }

    @Test
    void shouldCreateBaseExperiment_withMultipleThreads() {
        CommonArguments multiThreadArgs = new CommonArguments();
        multiThreadArgs.nRuns = 20;
        multiThreadArgs.parallel = true;
        multiThreadArgs.nThreads = 8;
        TestExperiment multiThreadExperiment = new TestExperiment("Multi-thread Experiment", multiThreadArgs);

        assertEquals(8, multiThreadExperiment.getArguments().nThreads);
        assertTrue(multiThreadExperiment.getArguments().parallel);
    }

    @Test
    void shouldCreateMultipleBaseExperiments() {
        CommonArguments args1 = new CommonArguments();
        args1.nRuns = 1;
        CommonArguments args2 = new CommonArguments();
        args2.nRuns = 2;
        CommonArguments args3 = new CommonArguments();
        args3.nRuns = 3;

        TestExperiment exp1 = new TestExperiment("Experiment 1", args1);
        TestExperiment exp2 = new TestExperiment("Experiment 2", args2);
        TestExperiment exp3 = new TestExperiment("Experiment 3", args3);

        assertEquals(1, exp1.getNExperiments());
        assertEquals(2, exp2.getNExperiments());
        assertEquals(3, exp3.getNExperiments());
    }

    // Concrete implementation for testing
    private static class TestExperiment extends BaseExperiment {

        public TestExperiment(String description, CommonArguments arguments) {
            super(description, arguments);
        }

        @Override
        public void beforeStart() {
            // Test implementation
        }

        @Override
        public void afterFinalize() {
            // Test implementation
        }

        @Override
        public void runExperiment(int ind) {
            // Test implementation
        }
    }
}
