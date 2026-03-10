# JaDES Examples

Executable, pedagogical simulation examples for the [JaDES](https://github.com/JaDES-ULL/JaDES) discrete-event simulation framework.

> **Note:** This module is **not published to Maven Central**. It exists purely to illustrate how to build models with JaDES and to serve as live smoke-tests in CI.

---

## Module structure

```
jades-examples/
├── pom.xml
└── src/
    ├── main/java/es/ull/simulation/examples/
    │   ├── emergency/
    │   │   ├── EmergencyDeptModel.java      ← model definition
    │   │   ├── EmergencyStatsListener.java  ← wait-time & utilisation stats
    │   │   └── EmergencyDeptMain.java       ← runnable entry point
    │   └── icu/
    │       ├── ICUModel.java                ← model definition
    │       ├── ICUStatsListener.java        ← per-replication statistics
    │       └── ICUMain.java                 ← runnable entry point (multi-run)
    └── test/java/es/ull/simulation/examples/
        ├── emergency/EmergencyDeptTest.java ← CI smoke test
        └── icu/ICUTest.java                 ← CI smoke test
```

---

## Example 1 — Emergency Department (`emergency`)

### Scenario

A single emergency department runs one **8-hour shift** (480 min). Patients arrive every **5 minutes** on average. Each patient follows one of two paths:

```
Patient ──[Triage]──[Medical Consultation]──(80%)──► Discharge
                                           └─(20%)──[Diagnostic Tests]──[Second Evaluation]──► Discharge
```

### Resources

| Type                  | Units | Availability      |
|-----------------------|-------|-------------------|
| Triage Nurse          | 2     | Full 8-hour shift |
| Emergency Physician   | 3     | Full 8-hour shift |
| Diagnostic Technician | 2     | Full 8-hour shift |

### JaDES concepts covered

| Concept | Where used |
|---------|-----------|
| `ResourceType` / `Resource` | All three staff types |
| `WorkGroup` | One resource per activity |
| `ActivityFlow` | Triage, Consultation, Tests, Evaluation |
| `SimulationPeriodicCycle.newDailyCycle` | 8-hour shift availability |
| `TimeDrivenElementGenerator` | Patient arrival every 5 min |
| `ExclusiveChoiceFlow` + `PercentageCondition` | 20 % routing to diagnostic tests |
| `StdInfoListener` | Full event trace |
| Custom `EmergencyStatsListener` | Wait times, sojourn time, resource utilisation |

### How to run

```bash
# From repository root
mvn -pl jades-examples compile exec:java \
    -Dexec.mainClass=es.ull.simulation.examples.emergency.EmergencyDeptMain

# With options
mvn -pl jades-examples compile exec:java \
    -Dexec.mainClass=es.ull.simulation.examples.emergency.EmergencyDeptMain \
    -Dexec.args="-r 5 -q -s 12345"
```

| Option | Default | Description |
|--------|---------|-------------|
| `-r <n>` | 1 | Number of replications |
| `-q` | false | Quiet mode (no event trace) |
| `-s <n>` | random | Random seed |

### Sample output (single run)

```
════════════════════════════════════════════════════════
 EMERGENCY DEPARTMENT — Simulation Results
════════════════════════════════════════════════════════
 Simulation duration  : 480 minutes (8.0 hours)
 Patients processed   : 96
 Avg sojourn time     : 23.4 min

 Activity                       Avg Wait   Max Wait   Count
 ────────────────────────────────────────────────────────
 Triage                           2.1m      18.0m       96
 Medical Consultation             8.7m      45.0m       96
 Diagnostic Tests                 4.3m      22.0m       19
 Second Evaluation                1.2m       8.0m       19

 Resource Type                  Utilisation
 ──────────────────────────────────────────
 Triage Nurse                      82.4 %
 Emergency Physician               74.1 %
 Diagnostic Technician             30.9 %
════════════════════════════════════════════════════════
```

---

## Example 2 — Intensive Care Unit (`icu`)

### Scenario

An ICU runs for **7 days** (10 080 min). A patient arrives every **2 hours** (84 patients total). Each patient follows a complex flow:

```
Patient
  └─[ICU Admission] (intensivist + nurse)
       └─ ParallelFlow
           ├─[Lab Analysis]           (lab technician)
           └─[Specialist Consultation](intensivist)
       └─ SynchronizationFlow
       └─[Treatment Plan]             (intensivist)
       └─ DoWhileFlow (repeat 30 % of the time)
           └─[Treatment Round]        (nurse)
       └─[Recovery Monitoring]        (nurse)
       └─ Discharge
```

### Resources — rotating shifts

| Type            | Units | Shift pattern                        |
|-----------------|-------|--------------------------------------|
| Intensivist     | 2     | Two 12-hour shifts (00:00 / 12:00)   |
| ICU Nurse       | 6     | Three 8-hour shifts (00:00/08:00/16:00), 2 per shift |
| Lab Technician  | 1     | 24 h / day (single resource)         |

### JaDES concepts covered — beyond Example 1

| Concept | Where used |
|---------|-----------|
| `ParallelFlow` + `SynchronizationFlow` | Parallel assessment (WFP-2 / WFP-3) |
| `DoWhileFlow` + `PercentageCondition` | Treatment repetition on complication |
| Multi-shift `SimulationPeriodicCycle.newDailyCycle` | 12-h and 8-h rotations |
| `BaseExperiment` + `CommonArguments` | Multiple replications with `-r` |
| `ICUStatsListener` + aggregation | Mean ± std dev across replications |

### How to run

```bash
# Default: 10 replications, quiet mode
mvn -pl jades-examples compile exec:java \
    -Dexec.mainClass=es.ull.simulation.examples.icu.ICUMain \
    -Dexec.args="-r 10 -q"

# Single run with full trace
mvn -pl jades-examples compile exec:java \
    -Dexec.mainClass=es.ull.simulation.examples.icu.ICUMain
```

### Sample output (10 replications)

```
════════════════════════════════════════════════════════════════
 INTENSIVE CARE UNIT — Aggregated Results  (10 replications)
════════════════════════════════════════════════════════════════
 Patients admitted  :  84 per run

 Wait times (minutes)          Mean ± SD       Max (mean)
 ──────────────────────────────────────────────────────────
 ICU Admission                12.4 ±  3.2        87.5
 Lab Analysis                  2.1 ±  0.8        18.3
 Specialist Consultation      28.7 ±  8.1       142.0
 Treatment Plan                9.4 ±  2.7        61.2
 Treatment Round               5.3 ±  1.9        44.0
 Recovery Monitoring           3.8 ±  1.1        29.7

 Avg sojourn time (h)         :  8.7 ± 1.2
 Avg treatment rounds/patient :  1.4 ± 0.1

 Resource utilisation          Mean ± SD
 ──────────────────────────────────────────
 Intensivist                  61.2 ±  4.3 %
 ICU Nurse                    84.7 ±  5.1 %
 Lab Technician               40.3 ±  3.7 %
════════════════════════════════════════════════════════════════
```

---

## Dependencies

```
jades-examples
  └─ jades-core       (simulation engine — compile)
  │    ├─ jades-api   (interfaces & cycles — transitive)
  │    │    └─ jades-random (random variates — transitive)
  │    └─ jades-utils (functions, math helpers — transitive)
  └─ jades-utils      (explicit — used directly for utility functions)
```

Only `jades-core` and `jades-utils` need to be declared in `pom.xml`; all other JaDES modules arrive transitively.

---

## Running the tests

The test classes are **smoke tests** that run each model for a short duration and assert the simulation terminates without errors. They are executed automatically by `mvn test` or as part of the full `mvn install` build.

```bash
mvn -pl jades-examples test
```
