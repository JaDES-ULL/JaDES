package es.ull.simulation.examples.icu;

import org.junit.jupiter.api.Test;

import es.ull.simulation.experiment.CommonArguments;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Smoke test for the Intensive Care Unit example.
 *
 * <p>Verifies that one (or several) replications of the 7-day ICU model run
 * to completion without exceptions.  Event-driven simulation is much faster
 * than wall-clock time, so even the 7-day horizon (10 080 simulation minutes)
 * completes in milliseconds.
 */
class ICUTest {

    /**
     * Runs one replication of the ICU model (7 days = 10 080 min).
     * No output is printed (quiet mode).
     */
    @Test
    void shouldRunWithoutErrors() {
        final CommonArguments args = new CommonArguments();
        args.nRuns  = 1;
        args.quiet  = true;

        assertDoesNotThrow(() -> new ICUMain(args).run(),
                "ICUMain.run() must not throw any exception");
    }

    /**
     * Runs three replications to verify that multi-replication aggregation works.
     */
    @Test
    void shouldRunMultipleReplications() {
        final CommonArguments args = new CommonArguments();
        args.nRuns  = 3;
        args.quiet  = true;

        assertDoesNotThrow(() -> new ICUMain(args).run(),
                "ICUMain.run() must not throw any exception with 3 replications");
    }
}
