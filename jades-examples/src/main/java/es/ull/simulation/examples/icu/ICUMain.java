package es.ull.simulation.examples.icu;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.JCommander;
import es.ull.simulation.experiment.BaseExperiment;
import es.ull.simulation.experiment.CommonArguments;
import es.ull.simulation.inforeceiver.StdInfoListener;
import es.ull.simulation.model.Simulation;

/**
 * Entry point for the Intensive Care Unit example (Scenario 2).
 *
 * <h2>Usage</h2>
 * <pre>
 *   java -cp jades-examples.jar es.ull.simulation.examples.icu.ICUMain [options]
 *
 *   Options
 *     -r &lt;n&gt;   Number of replications  (default: 10)
 *     -q        Quiet — suppress progress output
 *     -s &lt;n&gt;   Random seed
 * </pre>
 *
 * <p>For a single replication the full event trace is also printed via
 * {@link StdInfoListener}. For multiple replications the trace is suppressed;
 * only the aggregated statistics summary (mean ± SD over replications) is shown.
 *
 * <p>After all replications the following aggregated metrics are printed:
 * <ul>
 *   <li>Mean and maximum wait time per activity</li>
 *   <li>Mean sojourn time (total time inside the ICU per patient)</li>
 *   <li>Mean treatment rounds per patient</li>
 *   <li>Resource utilisation (%) per resource type</li>
 *   <li>Patient throughput (total patients processed per replication)</li>
 * </ul>
 *
 * @see ICUModel
 * @see ICUStatsListener
 */
public class ICUMain extends BaseExperiment {

    /** Activity names tracked across all replications. */
    private static final String[] TRACKED_ACTIVITIES = {
        ICUModel.ACT_ADMISSION,
        ICUModel.ACT_LAB,
        ICUModel.ACT_SPECIALIST,
        ICUModel.ACT_TREATMENT_PLAN,
        ICUModel.ACT_TREATMENT_ROUND,
        ICUModel.ACT_RECOVERY
    };

    /** Resource-type names tracked across all replications. */
    private static final String[] TRACKED_RESOURCES = {
        ICUModel.RT_INTENSIVIST,
        ICUModel.RT_NURSE,
        ICUModel.RT_LAB_TECH
    };

    /**
     * Default number of replications when none is specified on the command line.
     * A larger value gives narrower confidence intervals for the output metrics.
     */
    public static final int DEFAULT_REPLICATIONS = 10;

    /** Stats listener from each completed replication. */
    private final List<ICUStatsListener> allStats = new ArrayList<>();

    /** Print the full event trace only for single-run mode. */
    private final boolean printTrace;

    /**
     * Creates a new ICU experiment.
     *
     * @param arguments Command-line arguments parsed by JCommander.
     */
    public ICUMain(CommonArguments arguments) {
        super("ICU 7-day Simulation Experiment", arguments);
        this.printTrace = arguments.nRuns == 1;
    }

    // ── BaseExperiment hooks ─────────────────────────────────────────────────

    /**
     * Runs one replication of the 7-day ICU model.
     * The stats listener is registered on the simulation and kept for the
     * final aggregated report produced by {@link #afterFinalize()}.
     *
     * @param ind Zero-based replication index.
     */
    @Override
    public void runExperiment(int ind) {
        final Simulation simul = new ICUModel(ind);

        final ICUStatsListener stats =
                new ICUStatsListener(TRACKED_ACTIVITIES, TRACKED_RESOURCES);
        simul.registerListener(stats);

        // Full trace only when running a single replication
        if (printTrace) {
            simul.registerListener(new StdInfoListener());
        }

        simul.run(0, ICUModel.HORIZON_MIN);
        allStats.add(stats);
    }

    /**
     * Called by {@link BaseExperiment#run()} after all replications finish.
     * Computes and prints mean ± SD for each metric over all replications.
     */
    @Override
    public void afterFinalize() {
        if (allStats.isEmpty()) {
            return;
        }

        final int n = allStats.size();
        System.out.println("\n═══════════════════════════════════════════════════════════════");
        System.out.printf("  ICU Simulation – Aggregated Report  (%d replication%s)%n",
                n, n == 1 ? "" : "s");
        System.out.println("═══════════════════════════════════════════════════════════════\n");

        // ── Activity wait times ──────────────────────────────────────────────
        System.out.println("  Activity Wait Times (minutes):");
        System.out.println("  ┌─────────────────────────────┬──────────┬──────────┬──────────┐");
        System.out.println("  │ Activity                    │ Mean avg │ Mean max │ SD avg   │");
        System.out.println("  ├─────────────────────────────┼──────────┼──────────┼──────────┤");
        for (final String act : TRACKED_ACTIVITIES) {
            final double meanAvg = mean(allStats, s -> s.getAverageWaitTime(act));
            final double meanMax = mean(allStats, s -> s.getMaxWaitTime(act));
            final double sdAvg   = sd(allStats, s -> s.getAverageWaitTime(act), meanAvg);
            System.out.printf("  │ %-27s │ %8.2f │ %8.2f │ %8.2f │%n",
                    act, meanAvg, meanMax, sdAvg);
        }
        System.out.println("  └─────────────────────────────┴──────────┴──────────┴──────────┘\n");

        // ── Sojourn times ────────────────────────────────────────────────────
        System.out.println("  Patient-level Metrics:");
        System.out.println("  ┌─────────────────────────────┬──────────┬──────────┐");
        System.out.println("  │ Metric                      │   Mean   │    SD    │");
        System.out.println("  ├─────────────────────────────┼──────────┼──────────┤");

        final double avgSojourn = mean(allStats, ICUStatsListener::getAverageSojournTime);
        final double sdSojourn  = sd(allStats, ICUStatsListener::getAverageSojournTime, avgSojourn);
        System.out.printf("  │ %-27s │ %8.2f │ %8.2f │%n",
                "Avg sojourn (min)", avgSojourn, sdSojourn);

        final double avgRounds = mean(allStats, ICUStatsListener::getAverageTreatmentRoundsPerPatient);
        final double sdRounds  = sd(allStats, ICUStatsListener::getAverageTreatmentRoundsPerPatient, avgRounds);
        System.out.printf("  │ %-27s │ %8.2f │ %8.2f │%n",
                "Avg treatment rounds", avgRounds, sdRounds);

        final double avgPatients = mean(allStats, s -> (double) s.getElementCount());
        final double sdPatients  = sd(allStats, s -> (double) s.getElementCount(), avgPatients);
        System.out.printf("  │ %-27s │ %8.2f │ %8.2f │%n",
                "Patients processed", avgPatients, sdPatients);

        System.out.println("  └─────────────────────────────┴──────────┴──────────┘\n");

        // ── Resource utilisation ─────────────────────────────────────────────
        System.out.println("  Resource Utilisation (%):");
        System.out.println("  ┌─────────────────────────────┬──────────┬──────────┐");
        System.out.println("  │ Resource type               │   Mean   │    SD    │");
        System.out.println("  ├─────────────────────────────┼──────────┼──────────┤");
        for (final String rt : TRACKED_RESOURCES) {
            final double meanUtil = mean(allStats, s -> s.getUtilization(rt));
            final double sdUtil   = sd(allStats, s -> s.getUtilization(rt), meanUtil);
            System.out.printf("  │ %-27s │ %8.2f │ %8.2f │%n", rt, meanUtil, sdUtil);
        }
        System.out.println("  └─────────────────────────────┴──────────┴──────────┘\n");
    }

    // ── Statistical helpers ──────────────────────────────────────────────────

    /** Arithmetic mean of a metric extracted from all replications. */
    private static double mean(List<ICUStatsListener> stats,
                               java.util.function.ToDoubleFunction<ICUStatsListener> extractor) {
        double sum = 0.0;
        for (final ICUStatsListener s : stats) {
            sum += extractor.applyAsDouble(s);
        }
        return stats.isEmpty() ? 0.0 : sum / stats.size();
    }

    /**
     * Sample standard deviation of a metric over replications.
     *
     * @param precomputedMean Mean already computed by {@link #mean} to avoid a second pass.
     */
    private static double sd(List<ICUStatsListener> stats,
                              java.util.function.ToDoubleFunction<ICUStatsListener> extractor,
                              double precomputedMean) {
        if (stats.size() < 2) {
            return 0.0;
        }
        double sumSq = 0.0;
        for (final ICUStatsListener s : stats) {
            final double diff = extractor.applyAsDouble(s) - precomputedMean;
            sumSq += diff * diff;
        }
        return Math.sqrt(sumSq / (stats.size() - 1));
    }

    // ── main ─────────────────────────────────────────────────────────────────

    /**
     * Application entry point.
     *
     * <p>If the user does not pass {@code -r}, the number of replications
     * defaults to {@value #DEFAULT_REPLICATIONS}.
     *
     * @param args Command-line arguments (parsed by JCommander).
     */
    public static void main(String[] args) {
        final CommonArguments arguments = new CommonArguments();
        // Default to 10 replications if the user doesn't specify -r
        arguments.nRuns = DEFAULT_REPLICATIONS;
        JCommander.newBuilder().addObject(arguments).build().parse(args);
        new ICUMain(arguments).run();
    }
}
