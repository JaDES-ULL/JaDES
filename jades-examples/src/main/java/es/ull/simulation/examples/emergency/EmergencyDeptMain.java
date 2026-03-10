package es.ull.simulation.examples.emergency;

import com.beust.jcommander.JCommander;
import es.ull.simulation.experiment.BaseExperiment;
import es.ull.simulation.experiment.CommonArguments;
import es.ull.simulation.inforeceiver.StdInfoListener;
import es.ull.simulation.model.Simulation;

/**
 * Entry point for the Emergency Department example.
 *
 * <h2>Usage</h2>
 * <pre>
 *   java -cp jades-examples.jar es.ull.simulation.examples.emergency.EmergencyDeptMain [options]
 *
 *   Options
 *     -r &lt;n&gt;   Number of replications  (default: 1)
 *     -q        Quiet — suppress progress output
 *     -s &lt;n&gt;   Random seed
 * </pre>
 *
 * <p>For a single replication the full event trace is printed via
 * {@link StdInfoListener}. For multiple replications the trace is suppressed
 * to keep output readable; only the statistics summary is shown.
 *
 * @see EmergencyDeptModel
 * @see EmergencyStatsListener
 */
public class EmergencyDeptMain extends BaseExperiment {

    /** Activity names forwarded to the stats listener. */
    private static final String[] TRACKED_ACTIVITIES = {
        EmergencyDeptModel.ACT_TRIAGE,
        EmergencyDeptModel.ACT_CONSULT,
        EmergencyDeptModel.ACT_DIAG,
        EmergencyDeptModel.ACT_EVAL
    };

    /** Resource-type names forwarded to the stats listener. */
    private static final String[] TRACKED_RESOURCES = {
        EmergencyDeptModel.RT_NURSE,
        EmergencyDeptModel.RT_PHYSICIAN,
        EmergencyDeptModel.RT_DIAG_TECH
    };

    /** Print the full event trace only for single-run mode. */
    private final boolean printTrace;

    /** Statistics collected in the last (or only) replication. */
    private EmergencyStatsListener lastStats;

    /**
     * Creates a new experiment.
     * @param arguments Command-line arguments parsed by JCommander.
     */
    public EmergencyDeptMain(CommonArguments arguments) {
        super("Emergency Department Experiment", arguments);
        this.printTrace = arguments.nRuns == 1;
    }

    // ── BaseExperiment hooks ─────────────────────────────────────────────────

    @Override
    public void runExperiment(int ind) {
        final Simulation simul = new EmergencyDeptModel(ind);

        lastStats = new EmergencyStatsListener(TRACKED_ACTIVITIES, TRACKED_RESOURCES);
        simul.registerListener(lastStats);

        // Full trace only when running a single replication
        if (printTrace) {
            simul.registerListener(new StdInfoListener());
        }

        simul.run(0, EmergencyDeptModel.SHIFT_DURATION_MIN);
    }

    /**
     * Called by {@link BaseExperiment#run()} after all replications finish.
     * Prints the statistics summary from the last replication.
     */
    @Override
    public void afterFinalize() {
        if (lastStats != null) {
            lastStats.printSummary();
        }
    }

    // ── main ─────────────────────────────────────────────────────────────────

    /**
     * Application entry point.
     * @param args Command-line arguments (parsed by JCommander).
     */
    public static void main(String[] args) {
        final CommonArguments arguments = new CommonArguments();
        JCommander.newBuilder().addObject(arguments).build().parse(args);
        new EmergencyDeptMain(arguments).run();
    }
}
