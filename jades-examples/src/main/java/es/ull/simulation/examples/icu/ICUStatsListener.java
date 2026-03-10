package es.ull.simulation.examples.icu;

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
 * Collects statistics for <strong>one</strong> ICU replication.
 *
 * <p>After the replication ends the values can be read via the accessor methods
 * and aggregated across replications in {@link ICUMain}.
 *
 * <p>Tracked metrics per replication:
 * <ul>
 *   <li>Wait time per activity (average, max)</li>
 *   <li>Average patient sojourn time (total ICU stay)</li>
 *   <li>Average number of treatment rounds per patient</li>
 *   <li>Resource utilisation per resource type (%)</li>
 * </ul>
 */
public class ICUStatsListener extends BasicListener {

    // ── Wait times ───────────────────────────────────────────────────────────
    /** Per-activity accumulators: [totalWait, count, maxWait]. */
    private final Map<String, long[]> waitStats = new LinkedHashMap<>();
    private final Map<ElementInstance, Long> requestTimes = new HashMap<>();

    // ── Sojourn times ────────────────────────────────────────────────────────
    private long totalSojournTime = 0;
    private final Map<Integer, Long> elementStartTimes = new HashMap<>();
    private int elementCount = 0;

    // ── Treatment rounds ─────────────────────────────────────────────────────
    /** Total REQ events on the Treatment Round activity across all patients. */
    private int totalTreatmentRounds = 0;

    // ── Resource utilisation ─────────────────────────────────────────────────
    private final Map<String, long[]> resourceBusyTime = new LinkedHashMap<>();
    private final Map<Integer, Long> resourceCaughtAt = new HashMap<>();
    private final Map<String, Set<Integer>> resourceIdsByType = new LinkedHashMap<>();

    private long simulationEndTs = 1;

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a new statistics listener for one replication.
     *
     * @param activityNames     Activity descriptions to track.
     * @param resourceTypeNames Resource-type descriptions to track utilisation.
     */
    public ICUStatsListener(String[] activityNames, String[] resourceTypeNames) {
        super("ICU Statistics");
        addTargetInformation(ElementActionInfo.class);
        addTargetInformation(ElementInfo.class);
        addTargetInformation(ResourceUsageInfo.class);
        addTargetInformation(SimulationStartStopInfo.class);
        for (String name : activityNames) {
            waitStats.put(name, new long[3]);
        }
        for (String name : resourceTypeNames) {
            resourceBusyTime.put(name, new long[1]);
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
            case REQ -> {
                requestTimes.put(eai.getElementInstance(), eai.getTs());
                // Count each treatment round request (= one loop iteration)
                if (ICUModel.ACT_TREATMENT_ROUND.equals(actDesc)) {
                    totalTreatmentRounds++;
                }
            }
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
            default -> { /* ignore */ }
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

    // ── Accessors (used by ICUMain for cross-replication aggregation) ────────

    /**
     * Returns the average wait time for the specified activity (minutes).
     * @param activityName Activity description.
     * @return Average wait time in minutes, or {@code 0.0} if not tracked.
     */
    public double getAverageWaitTime(String activityName) {
        final long[] s = waitStats.get(activityName);
        return (s != null && s[1] > 0) ? (double) s[0] / s[1] : 0.0;
    }

    /**
     * Returns the maximum observed wait time for the specified activity (minutes).
     * @param activityName Activity description.
     */
    public double getMaxWaitTime(String activityName) {
        final long[] s = waitStats.get(activityName);
        return s != null ? (double) s[2] : 0.0;
    }

    /**
     * Returns the average patient sojourn time (total ICU stay) in minutes.
     */
    public double getAverageSojournTime() {
        return elementCount > 0 ? (double) totalSojournTime / elementCount : 0.0;
    }

    /** Returns the number of patients who completed the ICU stay in this run. */
    public int getElementCount() {
        return elementCount;
    }

    /**
     * Returns the average number of treatment rounds per patient.
     * A value of 1.0 means no complications; values above 1.0 reflect loops.
     */
    public double getAverageTreatmentRoundsPerPatient() {
        return elementCount > 0 ? (double) totalTreatmentRounds / elementCount : 0.0;
    }

    /**
     * Returns the resource utilisation for the given resource type (0–100 %).
     * @param resourceTypeName Resource-type description.
     */
    public double getUtilization(String resourceTypeName) {
        final long[] busy = resourceBusyTime.get(resourceTypeName);
        final Set<Integer> ids = resourceIdsByType.get(resourceTypeName);
        if (busy == null || ids == null || ids.isEmpty()) return 0.0;
        return 100.0 * busy[0] / ((long) ids.size() * simulationEndTs);
    }
}
