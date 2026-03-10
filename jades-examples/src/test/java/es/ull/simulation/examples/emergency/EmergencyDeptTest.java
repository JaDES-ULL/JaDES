package es.ull.simulation.examples.emergency;

import org.junit.jupiter.api.Test;

import es.ull.simulation.experiment.CommonArguments;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Smoke test for the Emergency Department example.
 *
 * <p>Verifies that a single replication of the 8-hour emergency-department
 * model starts, runs to completion, and produces statistics without throwing
 * any exception.  The test is intentionally shallow — it checks integration,
 * not numeric correctness — so it stays fast and stable in CI.
 */
class EmergencyDeptTest {

    /**
     * Runs one replication of the Emergency Department model (8 h = 480 min).
     * No output is printed (quiet mode).
     */
    @Test
    void shouldRunWithoutErrors() {
        final CommonArguments args = new CommonArguments();
        args.nRuns  = 1;
        args.quiet  = true;

        assertDoesNotThrow(() -> new EmergencyDeptMain(args).run(),
                "EmergencyDeptMain.run() must not throw any exception");
    }

    /**
     * Runs three replications to verify that multi-run mode also works.
     */
    @Test
    void shouldRunMultipleReplications() {
        final CommonArguments args = new CommonArguments();
        args.nRuns  = 3;
        args.quiet  = true;

        assertDoesNotThrow(() -> new EmergencyDeptMain(args).run(),
                "EmergencyDeptMain.run() must not throw any exception with 3 replications");
    }
}
