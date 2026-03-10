package es.ull.simulation.examples.emergency;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import es.ull.simulation.info.ElementActionInfo;
import es.ull.simulation.info.ElementInfo;
import es.ull.simulation.info.IPieceOfInformation;
import es.ull.simulation.info.ResourceUsageInfo;
import es.ull.simulation.info.SimulationStartStopInfo;
import es.ull.simulation.inforeceiver.BasicListener;
import es.ull.simulation.model.ElementInstance;

/**
 * Collects wait times, sojourn times and resource utilisation for the
 * Emergency Department simulation and prints a formatted summary at the end.
 *
 * <p>Register this listener before calling {@code simulation.run()}:
 * <pre>{@code
 *   EmergencyStatsListener stats = new EmergencyStatsListener(...);
 *   simulation.registerListener(stats);
 *   simulation.run(0, 480);
 *   stats.printSummary();
 * }</pre>
 *
 * <p>Each array in {@code waitStats} stores {@code [totalWait, count, maxWait]}
 * (all values in simulation time units, i.e. minutes).
 */
public class EmergencyStatsListener extends BasicListener {

    // ── Wait times per activity ──────────────────────────────────────────────
    /** Per-activity wait-time accumulators: [totalWait, count, maxWait]. */
    private final Map<String, long[]> waitStats = new LinkedHashMap<>();
    /** Request timestamp keyed by the element instance currently waiting. */
    private final Map<ElementInstance, Long> requestTimes = new HashMap<>();

    // ── Sojourn times ────────────────────────────────────────────────────────
    private long totalSojournTime = 0;
    private final Map<Integer, Long> elementStartTimes = new HashMap<>();
    private int elementCount = 0;

    // ── Resource utilisation ─────────────────────────────────────────────────
    /** Total busy time (sum of CAUGHT→RELEASED intervals) per resource type. */
    private final Map<String, long[]> resourceBusyTime = new LinkedHashMap<>();
    /** Timestamp when each resource was last seized, keyed by resource ID. */
    private final Map<Integer, Long> resourceCaughtAt = new HashMap<>();
    /** All resource IDs seen per type (used to derive the pool size). */
    private final Map<String, Set<Integer>> resourceIdsByType = new LinkedHashMap<>();

    private long simulationEndTs = 1;

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a new statistics listener.
     *
     * @param activityNames     Names of the {@link es.ull.simulation.model.flow.ActivityFlow}
     *                          instances to track (must match their {@code description}).
     * @param resourceTypeNames Names of the {@link es.ull.simulation.model.ResourceType}
     *                          instances to track utilisation for.
     */
    public EmergencyStatsListener(String[] activityNames, String[] resourceTypeNames) {
        super("Emergency Dept Statistics");
        addTargetInformation(ElementActionInfo.class);
        addTargetInformation(ElementInfo.class);
        addTargetInformation(ResourceUsageInfo.class);
        addTargetInformation(SimulationStartStopInfo.class);
        for (String name : activityNames) {
            waitStats.put(name, new long[3]);          // [totalWait, count, maxWait]
        }
        for (String name : resourceTypeNames) {
            resourceBusyTime.put(name, new long[1]);   // [totalBusyMinutes]
            resourceIdsByType.put(name, new HashSet<>());
        }
    }

    // ── IListener ────────────────────────────────────────────────────────────

    @Override
    public void infoEmited(IPieceOfInformation info) {
        if (info instanceof ElementActionInfo eai) {
            handleElementAction(eai);
        } else if (info instanceof ElementInfo ei) {
            handleElementInfo(ei);
        } else if (info instanceof ResourceUsageInfo rui) {
            handleResourceUsage(rui);
        } else if (info instanceof SimulationStartStopInfo ssi
                && ssi.getType() == SimulationStartStopInfo.Type.END) {
            simulationEndTs = Math.max(1, ssi.getTs());
        }
    }

    // ── Internal handlers ────────────────────────────────────────────────────

    private void handleElementAction(ElementActionInfo eai) {
        final String actDesc = eai.getActivity().getDescription();
        switch (eai.getType()) {
            case REQ -> requestTimes.put(eai.getElementInstance(), eai.getTs());
            case ACQ -> {
                final Long reqTs = requestTimes.remove(eai.getElementInstance());
                if (reqTs != null && waitStats.containsKey(actDesc)) {
                    final long wait = eai.getTs() - reqTs;
                    final long[] s = waitStats.get(actDesc);
                    s[0] += wait;
                    s[1]++;
                    s[2] = Math.max(s[2], wait);
                }
            }
            default -> { /* START / END / REL not needed here */ }
        }
    }

    private void handleElementInfo(ElementInfo ei) {
        final int elemId = ei.getElement().getIdentifier();
        if (ei.getType() == ElementInfo.Type.START) {
            elementStartTimes.put(elemId, ei.getTs());
        } else if (ei.getType() == ElementInfo.Type.FINISH) {
            final Long startTs = elementStartTimes.remove(elemId);
            if (startTs != null) {
                totalSojournTime += ei.getTs() - startTs;
                elementCount++;
            }
        }
    }

    private void handleResourceUsage(ResourceUsageInfo rui) {
        final String typeName = rui.getResourceType().getDescription();
        final int resId = rui.getResource().getIdentifier();
        if (rui.getType() == ResourceUsageInfo.Type.CAUGHT) {
            resourceCaughtAt.put(resId, rui.getTs());
            if (resourceIdsByType.containsKey(typeName)) {
                resourceIdsByType.get(typeName).add(resId);
            }
        } else if (rui.getType() == ResourceUsageInfo.Type.RELEASED) {
            final Long caughtTs = resourceCaughtAt.remove(resId);
            if (caughtTs != null && resourceBusyTime.containsKey(typeName)) {
                resourceBusyTime.get(typeName)[0] += rui.getTs() - caughtTs;
            }
        }
    }

    // ── Output ───────────────────────────────────────────────────────────────

    /**
     * Prints a formatted summary to {@link System#out}.
     * Call this after the simulation has finished.
     */
    public void printSummary() {
        System.out.println();
        System.out.println("════════════════════════════════════════════════════════");
        System.out.println(" EMERGENCY DEPARTMENT — Simulation Results");
        System.out.println("════════════════════════════════════════════════════════");
        System.out.printf(" Simulation duration  : %.0f minutes (%.1f hours)%n",
                (double) simulationEndTs, simulationEndTs / 60.0);
        System.out.printf(" Patients processed   : %d%n", elementCount);
        System.out.printf(" Avg sojourn time     : %.1f min%n",
                elementCount > 0 ? (double) totalSojournTime / elementCount : 0.0);
        System.out.println();
        System.out.printf(" %-28s  %8s  %8s  %8s%n", "Activity", "Avg Wait", "Max Wait", "Count");
        System.out.println(" " + "─".repeat(60));
        for (Map.Entry<String, long[]> e : waitStats.entrySet()) {
            final long[] s = e.getValue();
            System.out.printf(" %-28s  %7.1fm  %7.1fm  %8d%n",
                    e.getKey(),
                    s[1] > 0 ? (double) s[0] / s[1] : 0.0,
                    (double) s[2],
                    s[1]);
        }
        System.out.println();
        System.out.printf(" %-28s  %12s%n", "Resource Type", "Utilisation");
        System.out.println(" " + "─".repeat(42));
        for (Map.Entry<String, long[]> e : resourceBusyTime.entrySet()) {
            final int nRes = resourceIdsByType.getOrDefault(e.getKey(), Set.of()).size();
            final long maxPossible = (long) nRes * simulationEndTs;
            System.out.printf(" %-28s  %10.1f %%%n",
                    e.getKey(),
                    maxPossible > 0 ? 100.0 * e.getValue()[0] / maxPossible : 0.0);
        }
        System.out.println("════════════════════════════════════════════════════════");
        System.out.println();
    }
}
