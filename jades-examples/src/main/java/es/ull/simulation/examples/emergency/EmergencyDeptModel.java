package es.ull.simulation.examples.emergency;

import es.ull.simulation.condition.PercentageCondition;
import es.ull.simulation.functions.TimeFunctionFactory;
import es.ull.simulation.model.ElementInstance;
import es.ull.simulation.model.ElementType;
import es.ull.simulation.model.Resource;
import es.ull.simulation.model.ResourceType;
import es.ull.simulation.model.Simulation;
import es.ull.simulation.model.SimulationPeriodicCycle;
import es.ull.simulation.model.TimeDrivenElementGenerator;
import es.ull.simulation.model.TimeStamp;
import es.ull.simulation.model.WorkGroup;
import es.ull.simulation.model.flow.ActivityFlow;
import es.ull.simulation.model.flow.ExclusiveChoiceFlow;

/**
 * Emergency department model simulating a single 8-hour shift.
 *
 * <h2>Patient flow</h2>
 * <pre>
 *   Patient ──[Triage]──[Medical Consultation]──(80%)──► discharge
 *                                               └─(20%)──[Diagnostic Tests]──[Second Evaluation]──► discharge
 * </pre>
 *
 * <h2>Resources (one 8-hour shift, starting at minute 0)</h2>
 * <ul>
 *   <li>{@value #N_TRIAGE_NURSES} Triage Nurses</li>
 *   <li>{@value #N_PHYSICIANS} Emergency Physicians</li>
 *   <li>{@value #N_DIAG_TECHS} Diagnostic Technicians</li>
 * </ul>
 *
 * <h2>JaDES concepts illustrated</h2>
 * <ul>
 *   <li>{@link ResourceType} / {@link Resource} / {@link WorkGroup}</li>
 *   <li>{@link ActivityFlow} with constant and uniform durations</li>
 *   <li>{@link SimulationPeriodicCycle#newDailyCycle} for shift availability</li>
 *   <li>{@link TimeDrivenElementGenerator} for periodic patient arrival</li>
 *   <li>{@link ExclusiveChoiceFlow} + {@link PercentageCondition} for routing</li>
 * </ul>
 *
 * @author JaDES Team
 */
public class EmergencyDeptModel extends Simulation {

    // ── Activity names (shared with EmergencyStatsListener) ─────────────────
    /** Description of the triage activity. */
    public static final String ACT_TRIAGE  = "Triage";
    /** Description of the medical consultation activity. */
    public static final String ACT_CONSULT = "Medical Consultation";
    /** Description of the diagnostic tests activity. */
    public static final String ACT_DIAG    = "Diagnostic Tests";
    /** Description of the second evaluation activity. */
    public static final String ACT_EVAL    = "Second Evaluation";

    // ── Resource-type names (shared with EmergencyStatsListener) ────────────
    /** Description of the triage nurse resource type. */
    public static final String RT_NURSE      = "Triage Nurse";
    /** Description of the emergency physician resource type. */
    public static final String RT_PHYSICIAN  = "Emergency Physician";
    /** Description of the diagnostic technician resource type. */
    public static final String RT_DIAG_TECH  = "Diagnostic Technician";

    // ── Scenario parameters ──────────────────────────────────────────────────
    /** Duration of one shift in minutes (8 hours). */
    public static final long   SHIFT_DURATION_MIN    = 8 * 60;
    /** One patient arrives every {@value} minutes. */
    public static final double ARRIVAL_INTERVAL_MIN  = 5.0;
    /** Number of triage nurses on duty. */
    public static final int    N_TRIAGE_NURSES        = 2;
    /** Number of emergency physicians on duty. */
    public static final int    N_PHYSICIANS           = 3;
    /** Number of diagnostic technicians on duty. */
    public static final int    N_DIAG_TECHS           = 2;

    // ── Activity durations ───────────────────────────────────────────────────
    /** Triage duration (constant), in minutes. */
    public static final long   TRIAGE_DURATION_MIN   = 3;
    /** Minimum consultation duration (Uniform distribution), in minutes. */
    public static final double CONSULT_MIN_MIN        = 8.0;
    /** Maximum consultation duration (Uniform distribution), in minutes. */
    public static final double CONSULT_MAX_MIN        = 15.0;
    /** Diagnostic tests duration (constant), in minutes. */
    public static final long   DIAG_DURATION_MIN      = 20;
    /** Second evaluation duration (constant), in minutes. */
    public static final long   EVAL_DURATION_MIN      = 5;

    // ── Routing ──────────────────────────────────────────────────────────────
    /** Percentage of patients routed to diagnostic tests after consultation. */
    public static final double PCT_NEEDS_TESTS        = 20.0;

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Builds and fully initialises the Emergency Department model.
     *
     * @param id Replication index (used to seed randomness per-replication).
     */
    public EmergencyDeptModel(int id) {
        super(id, "Emergency Department – 8-hour shift");

        // ── 1. Resource types ────────────────────────────────────────────────
        final ResourceType rtNurse  = new ResourceType(this, RT_NURSE);
        final ResourceType rtDoc    = new ResourceType(this, RT_PHYSICIAN);
        final ResourceType rtDiag   = new ResourceType(this, RT_DIAG_TECH);

        // ── 2. Shift cycle: repeats daily, staff starts at minute 0 ──────────
        //    withDuration(cycle, SHIFT_DURATION_MIN) makes each resource
        //    available for exactly one 8-hour block per simulated day.
        final SimulationPeriodicCycle shiftCycle =
                SimulationPeriodicCycle.newDailyCycle(unit, 0);

        // ── 3. Resources ─────────────────────────────────────────────────────
        for (int i = 1; i <= N_TRIAGE_NURSES; i++) {
            new Resource(this, RT_NURSE + " " + i)
                    .newTimeTableOrCancelEntriesAdder(rtNurse)
                    .withDuration(shiftCycle, SHIFT_DURATION_MIN)
                    .addTimeTableEntry();
        }
        for (int i = 1; i <= N_PHYSICIANS; i++) {
            new Resource(this, RT_PHYSICIAN + " " + i)
                    .newTimeTableOrCancelEntriesAdder(rtDoc)
                    .withDuration(shiftCycle, SHIFT_DURATION_MIN)
                    .addTimeTableEntry();
        }
        for (int i = 1; i <= N_DIAG_TECHS; i++) {
            new Resource(this, RT_DIAG_TECH + " " + i)
                    .newTimeTableOrCancelEntriesAdder(rtDiag)
                    .withDuration(shiftCycle, SHIFT_DURATION_MIN)
                    .addTimeTableEntry();
        }

        // ── 4. Work groups (resources needed per activity) ───────────────────
        final WorkGroup wgTriage  = new WorkGroup(this, rtNurse, 1);
        final WorkGroup wgConsult = new WorkGroup(this, rtDoc,   1);
        final WorkGroup wgDiag    = new WorkGroup(this, rtDiag,  1);
        final WorkGroup wgEval    = new WorkGroup(this, rtDoc,   1);

        // ── 5. Activities ────────────────────────────────────────────────────
        final ActivityFlow actTriage  = new ActivityFlow(this, ACT_TRIAGE);
        final ActivityFlow actConsult = new ActivityFlow(this, ACT_CONSULT);
        final ActivityFlow actDiag    = new ActivityFlow(this, ACT_DIAG);
        final ActivityFlow actEval    = new ActivityFlow(this, ACT_EVAL);

        actTriage .newWorkGroupAdder(wgTriage)
                  .withDelay(TRIAGE_DURATION_MIN)
                  .add();
        actConsult.newWorkGroupAdder(wgConsult)
                  .withDelay(TimeFunctionFactory.getInstance(
                          "UniformVariate", CONSULT_MIN_MIN, CONSULT_MAX_MIN))
                  .add();
        actDiag   .newWorkGroupAdder(wgDiag)
                  .withDelay(DIAG_DURATION_MIN)
                  .add();
        actEval   .newWorkGroupAdder(wgEval)
                  .withDelay(EVAL_DURATION_MIN)
                  .add();

        // ── 6. Flow: Triage → Consult → [20% Tests → Eval] ──────────────────
        //    ExclusiveChoiceFlow routes patients to the linked branch whose
        //    condition is satisfied first.  Patients not routed to actDiag
        //    exit the system automatically (no explicit "Discharge" flow needed).
        final ExclusiveChoiceFlow fNeedsTests = new ExclusiveChoiceFlow(this);
        actTriage .link(actConsult);
        actConsult.link(fNeedsTests);
        fNeedsTests.link(actDiag, new PercentageCondition<ElementInstance>(PCT_NEEDS_TESTS));
        actDiag   .link(actEval);

        // ── 7. Patient generator: 1 patient every ARRIVAL_INTERVAL_MIN min ───
        //    The cycle ends at SHIFT_DURATION_MIN so no new patients arrive
        //    once the shift is over.  The simulation itself ends when all
        //    in-flight patients have finished.
        final SimulationPeriodicCycle arrivalCycle = new SimulationPeriodicCycle(
                unit,
                new TimeStamp(unit, 0),
                TimeFunctionFactory.getInstance("ConstantVariate", ARRIVAL_INTERVAL_MIN),
                new TimeStamp(unit, SHIFT_DURATION_MIN));

        new TimeDrivenElementGenerator(this, 1,
                new ElementType(this, "Patient"),
                actTriage,
                arrivalCycle);
    }
}
