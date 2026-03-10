package es.ull.simulation.examples.icu;

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
import es.ull.simulation.model.flow.DoWhileFlow;
import es.ull.simulation.model.flow.ParallelFlow;
import es.ull.simulation.model.flow.SynchronizationFlow;

/**
 * Intensive Care Unit model simulating 7 days of operation.
 *
 * <h2>Patient flow</h2>
 * <pre>
 *   Patient
 *     └─[ICU Admission]         (intensivist + nurse, 60–120 min)
 *          └─ ParallelFlow (WFP-2)
 *              ├─[Lab Analysis]            (lab technician, 30 min)
 *              └─[Specialist Consultation] (intensivist,   20–40 min)
 *          └─ SynchronizationFlow (WFP-3) — waits for both branches
 *          └─[Treatment Plan]    (intensivist, 30 min)
 *          └─ DoWhileFlow — repeats with {@value #PCT_COMPLICATION} % probability
 *              └─[Treatment Round]  (nurse, 90–150 min)
 *          └─[Recovery Monitoring] (nurse, 180–360 min)
 *          └─ discharge
 * </pre>
 *
 * <h2>Resources — rotating shifts</h2>
 * <ul>
 *   <li>{@value #N_INTENSIVISTS} Intensivists: two 12-hour shifts (00:00 and 12:00)</li>
 *   <li>{@value #N_NURSES} ICU Nurses: three 8-hour shifts (00:00, 08:00, 16:00),
 *       {@value #N_NURSES_PER_SHIFT} nurses per shift</li>
 *   <li>1 Lab Technician: available 24 h / day</li>
 * </ul>
 *
 * <h2>JaDES concepts illustrated</h2>
 * <ul>
 *   <li>{@link ParallelFlow} + {@link SynchronizationFlow} (WFP-2 / WFP-3)</li>
 *   <li>{@link DoWhileFlow} + {@link PercentageCondition} for treatment loops</li>
 *   <li>Multi-shift resource availability via {@link SimulationPeriodicCycle#newDailyCycle}</li>
 * </ul>
 *
 * @author JaDES Team
 */
public class ICUModel extends Simulation {

    // ── Activity names (referenced by ICUStatsListener) ──────────────────────
    /** Description of the ICU admission activity. */
    public static final String ACT_ADMISSION       = "ICU Admission";
    /** Description of the lab analysis activity. */
    public static final String ACT_LAB             = "Lab Analysis";
    /** Description of the specialist consultation activity. */
    public static final String ACT_SPECIALIST      = "Specialist Consultation";
    /** Description of the treatment-plan activity. */
    public static final String ACT_TREATMENT_PLAN  = "Treatment Plan";
    /** Description of the treatment-round activity (loop body). */
    public static final String ACT_TREATMENT_ROUND = "Treatment Round";
    /** Description of the recovery monitoring activity. */
    public static final String ACT_RECOVERY        = "Recovery Monitoring";

    // ── Resource-type names (referenced by ICUStatsListener) ─────────────────
    /** Description of the intensivist resource type. */
    public static final String RT_INTENSIVIST = "Intensivist";
    /** Description of the ICU nurse resource type. */
    public static final String RT_NURSE       = "ICU Nurse";
    /** Description of the lab technician resource type. */
    public static final String RT_LAB_TECH    = "Lab Technician";

    // ── Scenario parameters ──────────────────────────────────────────────────
    /** Simulation horizon in minutes (7 days). */
    public static final long   HORIZON_MIN           = 7L * 24 * 60;
    /** One patient is admitted every {@value} minutes (1 every 2 hours). */
    public static final double ARRIVAL_INTERVAL_MIN  = 120.0;
    /** Number of intensivists (2 shifts of 12 h, 1 per shift). */
    public static final int    N_INTENSIVISTS         = 2;
    /** Total number of ICU nurses (3 shifts × 2 nurses). */
    public static final int    N_NURSES               = 6;
    /** ICU nurses on duty per shift. */
    public static final int    N_NURSES_PER_SHIFT     = 2;
    /** Shift length for nurses in minutes (8 hours). */
    public static final long   NURSE_SHIFT_MIN        = 8 * 60;
    /** Shift length for intensivists in minutes (12 hours). */
    public static final long   INTENSIVIST_SHIFT_MIN  = 12 * 60;

    // ── Activity durations ───────────────────────────────────────────────────
    /** Minimum admission duration (Uniform), in minutes. */
    public static final double ADMISSION_MIN_MIN      = 60.0;
    /** Maximum admission duration (Uniform), in minutes. */
    public static final double ADMISSION_MAX_MIN      = 120.0;
    /** Lab analysis duration (constant), in minutes. */
    public static final long   LAB_DURATION_MIN       = 30;
    /** Minimum specialist consultation duration (Uniform), in minutes. */
    public static final double SPECIALIST_MIN_MIN     = 20.0;
    /** Maximum specialist consultation duration (Uniform), in minutes. */
    public static final double SPECIALIST_MAX_MIN     = 40.0;
    /** Treatment-plan duration (constant), in minutes. */
    public static final long   PLAN_DURATION_MIN      = 30;
    /** Minimum treatment-round duration (Uniform), in minutes. */
    public static final double ROUND_MIN_MIN          = 90.0;
    /** Maximum treatment-round duration (Uniform), in minutes. */
    public static final double ROUND_MAX_MIN          = 150.0;
    /** Minimum recovery monitoring duration (Uniform), in minutes. */
    public static final double RECOVERY_MIN_MIN       = 180.0;
    /** Maximum recovery monitoring duration (Uniform), in minutes. */
    public static final double RECOVERY_MAX_MIN       = 360.0;

    // ── DoWhile probability ──────────────────────────────────────────────────
    /**
     * Probability (%) of a complication requiring a further treatment round.
     * The expected number of rounds per patient is 1 / (1 - PCT_COMPLICATION/100).
     */
    public static final double PCT_COMPLICATION = 30.0;

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Builds and fully initialises the ICU model.
     *
     * @param id Replication index.
     */
    public ICUModel(int id) {
        super(id, "Intensive Care Unit – 7-day simulation");

        // ── 1. Resource types ────────────────────────────────────────────────
        final ResourceType rtIntensivisit = new ResourceType(this, RT_INTENSIVIST);
        final ResourceType rtNurse        = new ResourceType(this, RT_NURSE);
        final ResourceType rtLabTech      = new ResourceType(this, RT_LAB_TECH);

        // ── 2. Shift cycles ──────────────────────────────────────────────────
        // Intensivists: two 12-hour shifts per day
        final SimulationPeriodicCycle intensivistMorning =
                SimulationPeriodicCycle.newDailyCycle(unit, 0);        // 00:00
        final SimulationPeriodicCycle intensivistEvening =
                SimulationPeriodicCycle.newDailyCycle(unit, 12 * 60);  // 12:00

        // ICU nurses: three 8-hour shifts per day
        final SimulationPeriodicCycle nurseShift1 =
                SimulationPeriodicCycle.newDailyCycle(unit, 0);        // 00:00
        final SimulationPeriodicCycle nurseShift2 =
                SimulationPeriodicCycle.newDailyCycle(unit, 8 * 60);   // 08:00
        final SimulationPeriodicCycle nurseShift3 =
                SimulationPeriodicCycle.newDailyCycle(unit, 16 * 60);  // 16:00

        // Lab technician: one 24-hour cycle (always available)
        final SimulationPeriodicCycle always =
                SimulationPeriodicCycle.newDailyCycle(unit, 0);

        // ── 3. Resources ─────────────────────────────────────────────────────
        // Two intensivists on alternating 12-hour shifts
        new Resource(this, RT_INTENSIVIST + " 1")
                .newTimeTableOrCancelEntriesAdder(rtIntensivisit)
                .withDuration(intensivistMorning, INTENSIVIST_SHIFT_MIN)
                .addTimeTableEntry();
        new Resource(this, RT_INTENSIVIST + " 2")
                .newTimeTableOrCancelEntriesAdder(rtIntensivisit)
                .withDuration(intensivistEvening, INTENSIVIST_SHIFT_MIN)
                .addTimeTableEntry();

        // Six nurses: 2 per 8-hour shift
        final SimulationPeriodicCycle[] nurseShifts =
                { nurseShift1, nurseShift1, nurseShift2, nurseShift2, nurseShift3, nurseShift3 };
        for (int i = 1; i <= N_NURSES; i++) {
            new Resource(this, RT_NURSE + " " + i)
                    .newTimeTableOrCancelEntriesAdder(rtNurse)
                    .withDuration(nurseShifts[i - 1], NURSE_SHIFT_MIN)
                    .addTimeTableEntry();
        }

        // One lab technician, available 24 h/day
        new Resource(this, RT_LAB_TECH + " 1")
                .newTimeTableOrCancelEntriesAdder(rtLabTech)
                .withDuration(always, 24 * 60L)
                .addTimeTableEntry();

        // ── 4. Work groups ───────────────────────────────────────────────────
        final WorkGroup wgAdmission   = new WorkGroup(this,
                new ResourceType[]{ rtIntensivisit, rtNurse }, new int[]{ 1, 1 });
        final WorkGroup wgLab         = new WorkGroup(this, rtLabTech, 1);
        final WorkGroup wgSpecialist  = new WorkGroup(this, rtIntensivisit, 1);
        final WorkGroup wgPlan        = new WorkGroup(this, rtIntensivisit, 1);
        final WorkGroup wgRound       = new WorkGroup(this, rtNurse, 1);
        final WorkGroup wgRecovery    = new WorkGroup(this, rtNurse, 1);

        // ── 5. Activities ────────────────────────────────────────────────────
        final ActivityFlow actAdmission  = new ActivityFlow(this, ACT_ADMISSION);
        final ActivityFlow actLab        = new ActivityFlow(this, ACT_LAB);
        final ActivityFlow actSpecialist = new ActivityFlow(this, ACT_SPECIALIST);
        final ActivityFlow actPlan       = new ActivityFlow(this, ACT_TREATMENT_PLAN);
        final ActivityFlow actRound      = new ActivityFlow(this, ACT_TREATMENT_ROUND);
        final ActivityFlow actRecovery   = new ActivityFlow(this, ACT_RECOVERY);

        actAdmission .newWorkGroupAdder(wgAdmission)
                     .withDelay(TimeFunctionFactory.getInstance(
                             "UniformVariate", ADMISSION_MIN_MIN, ADMISSION_MAX_MIN))
                     .add();
        actLab       .newWorkGroupAdder(wgLab)
                     .withDelay(LAB_DURATION_MIN)
                     .add();
        actSpecialist.newWorkGroupAdder(wgSpecialist)
                     .withDelay(TimeFunctionFactory.getInstance(
                             "UniformVariate", SPECIALIST_MIN_MIN, SPECIALIST_MAX_MIN))
                     .add();
        actPlan      .newWorkGroupAdder(wgPlan)
                     .withDelay(PLAN_DURATION_MIN)
                     .add();
        actRound     .newWorkGroupAdder(wgRound)
                     .withDelay(TimeFunctionFactory.getInstance(
                             "UniformVariate", ROUND_MIN_MIN, ROUND_MAX_MIN))
                     .add();
        actRecovery  .newWorkGroupAdder(wgRecovery)
                     .withDelay(TimeFunctionFactory.getInstance(
                             "UniformVariate", RECOVERY_MIN_MIN, RECOVERY_MAX_MIN))
                     .add();

        // ── 6. Flow ──────────────────────────────────────────────────────────
        // Parallel assessment: lab analysis and specialist consultation run
        // simultaneously, then the SynchronizationFlow waits for both branches.
        final ParallelFlow       parallelAssessment = new ParallelFlow(this);
        final SynchronizationFlow syncAssessment    = new SynchronizationFlow(this);

        // DoWhileFlow: execute actRound, then loop back with PCT_COMPLICATION %
        // probability (the condition returns true = "there is a complication").
        final DoWhileFlow loopTreatment = new DoWhileFlow(this, actRound,
                new PercentageCondition<ElementInstance>(PCT_COMPLICATION));

        // Link all flows together
        actAdmission    .link(parallelAssessment);
        parallelAssessment.link(actLab);
        parallelAssessment.link(actSpecialist);
        actLab          .link(syncAssessment);
        actSpecialist   .link(syncAssessment);
        syncAssessment  .link(actPlan);
        actPlan         .link(loopTreatment);
        loopTreatment   .link(actRecovery);

        // ── 7. Patient generator ─────────────────────────────────────────────
        // One patient arrives every ARRIVAL_INTERVAL_MIN minutes for 7 days.
        final SimulationPeriodicCycle arrivalCycle = new SimulationPeriodicCycle(
                unit,
                new TimeStamp(unit, 0),
                TimeFunctionFactory.getInstance("ConstantVariate", ARRIVAL_INTERVAL_MIN),
                new TimeStamp(unit, HORIZON_MIN));

        new TimeDrivenElementGenerator(this, 1,
                new ElementType(this, "ICU Patient"),
                actAdmission,
                arrivalCycle);
    }
}
