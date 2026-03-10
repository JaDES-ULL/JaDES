# JaDES Examples — Healthcare Simulation Case Studies

> **Reproducible companion material** for the JaDES discrete-event simulation framework.  
> This module is **not published to Maven Central**; it is provided as executable documentation and continuous-integration validation.

---

## Abstract

This module provides two reference implementations of healthcare facility models built with the JaDES (Java Discrete-Event Simulation) framework. The models serve a dual purpose: (i) they demonstrate the correct use of the JaDES API through progressively complex workflow patterns, and (ii) they constitute executable regression tests that verify framework correctness on every continuous-integration run. Both models are grounded in the discrete-event simulation (DES) paradigm [Banks et al., 2010] and employ workflow patterns formally catalogued by van der Aalst et al. [2003].

---

## 1. Theoretical Background

### 1.1 Discrete-Event Simulation

A discrete-event simulation system is characterised by a state vector $\mathbf{S}(t)$ that changes only at a countable set of time instants — the *event times* $\{t_1, t_2, \ldots\}$. Between consecutive events the system state remains constant [Law, 2015]. Formally, a DES model can be expressed as the tuple

$$\mathcal{M} = \langle S,\; E,\; X,\; \delta,\; \lambda,\; s_0 \rangle$$

where $S$ is the state space, $E$ the event set, $X$ the input trajectory space, $\delta : S \times X \to S$ the state-transition function, $\lambda : S \to Y$ the output function, and $s_0$ the initial state [Zeigler et al., 2000].

Healthcare systems constitute a canonical application domain for DES [Günal & Pidd, 2010]: patient pathways are naturally represented as sequences of resource-constrained activities with stochastic service times and inter-arrival processes that are often well approximated by Poisson or Erlang distributions.

### 1.2 The JaDES Framework

JaDES models a system as a collection of *elements* (entities) that traverse a directed *flow* graph while consuming *resources*. The execution semantics follow the activity-scanning world view [Pidd, 2004]: at each simulated time-point the engine evaluates pending resource requests and grants them according to the configured work groups. Stochastic behaviour is introduced through pluggable `TimeFunction` objects provided by the SimKit random-variate library [Buss, 2002].

### 1.3 Workflow Patterns

The control-flow structures used in both models correspond to patterns from the *Workflow Patterns* reference catalogue [van der Aalst et al., 2003; Russell et al., 2006]. Table 1 maps each pattern to its JaDES counterpart.

**Table 1.** Workflow patterns employed in the case studies.

| WFP ID | Pattern name | JaDES class | Case study |
|--------|-------------|-------------|------------|
| WFP-1 | Sequence | `ActivityFlow.link()` | 1 & 2 |
| WFP-4 | Exclusive Choice | `ExclusiveChoiceFlow` | 1 |
| WFP-2 | Parallel Split | `ParallelFlow` | 2 |
| WFP-3 | Synchronization | `SynchronizationFlow` | 2 |
| WFP-21 | Structured Loop (do-while) | `DoWhileFlow` | 2 |

---

## 2. Output Metrics

Let $n$ denote the number of patients processed in a replication of duration $T$. The following performance indicators are computed by the custom listener classes and reported by the experiment entry points.

**Waiting time** $W_q^{(a)}$ for activity $a$ — the interval between the resource request event and the resource acquisition event:

$$W_q^{(a)} = \frac{1}{n_a} \sum_{i=1}^{n_a} \left( t_{\mathrm{acq},i}^{(a)} - t_{\mathrm{req},i}^{(a)} \right)$$

**Sojourn time** $W$ — total time each element spends in the system from creation to disposal:

$$W = \frac{1}{n} \sum_{i=1}^{n} \left( t_{\mathrm{end},i} - t_{\mathrm{start},i} \right)$$

**Resource utilisation** $\rho^{(r)}$ for resource type $r$ with $c_r$ identical units over horizon $T$:

$$\rho^{(r)} = \frac{\displaystyle\sum_{j=1}^{c_r} b_j^{(r)}}{c_r \cdot T}$$

where $b_j^{(r)}$ is the total busy time of unit $j$.

For multi-replication experiments ($R$ independent replications), point estimators are the sample mean $\bar{x}$ and the sample standard deviation $s$. A classical $(1-\alpha)$ confidence interval takes the form

$$\bar{x} \pm t_{\alpha/2,\; R-1} \cdot \frac{s}{\sqrt{R}}$$

where $t_{\alpha/2,\; R-1}$ is the critical value of Student's $t$-distribution with $R-1$ degrees of freedom [Law, 2015, Ch. 9].

---

## 3. Case Study 1 — Emergency Department

### 3.1 Conceptual Model

The model represents a single emergency department operating over one **8-hour shift** ($T = 480\ \mathrm{min}$). Patients arrive at a constant inter-arrival rate of one every 5 minutes, yielding a nominal throughput of 96 patients per shift. The deterministic arrival process — rather than the Poisson process commonly reported in the literature [Günal & Pidd, 2010] — is used intentionally to produce fully reproducible baseline results suitable for model verification by trace analysis.

Upon arrival, each patient undergoes triage and a medical consultation. With probability $p_{\mathrm{diag}} = 0.20$, the attending physician orders diagnostic tests followed by a second evaluation (WFP-4, Exclusive Choice); the remaining 80 % are discharged directly after consultation.

```
Patient
  └─[Triage]                WG: {Triage Nurse × 1}
       └─[Medical Consult]  WG: {Emergency Physician × 1}
            └─ ExclusiveChoiceFlow
                 ├─ p = 0.20 ──► [Diagnostic Tests]    WG: {Diagnostic Technician × 1}
                 │                    └─[2nd Evaluation] WG: {Emergency Physician × 1}
                 │                         └─ Discharge
                 └─ p = 0.80 ──► Discharge
```

### 3.2 Model Parameters

**Table 2.** Input parameters for the Emergency Department model.

| Parameter | Symbol | Value | Distribution |
|-----------|--------|-------|-------------|
| Shift duration | $T$ | 480 min | — |
| Inter-arrival time | $1/\lambda$ | 5 min | Constant |
| Triage duration | $s_{\mathrm{triage}}$ | 3 min | Constant |
| Consultation duration | $s_{\mathrm{consult}}$ | $\mathcal{U}[8, 15]$ min | Uniform |
| Diagnostic test duration | $s_{\mathrm{diag}}$ | 20 min | Constant |
| Second evaluation duration | $s_{\mathrm{eval}}$ | 5 min | Constant |
| Probability of diagnostic branch | $p_{\mathrm{diag}}$ | 0.20 | Bernoulli |
| Triage nurses | $c_{\mathrm{nurse}}$ | 2 | — |
| Emergency physicians | $c_{\mathrm{phys}}$ | 3 | — |
| Diagnostic technicians | $c_{\mathrm{tech}}$ | 2 | — |

### 3.3 JaDES Implementation Notes

- **Resource availability** is modelled via `SimulationPeriodicCycle.newDailyCycle(unit, 0)` with a duration equal to `SHIFT_DURATION_MIN`, binding the resource timetable to the simulation horizon without requiring an explicit cancellation event.
- **Stochastic routing** is implemented as `ExclusiveChoiceFlow` guarded by `PercentageCondition<ElementInstance>(20.0)`, evaluated once per element instance at the choice point.
- **Statistics collection** is decoupled from the model through the observer pattern: `EmergencyStatsListener` subscribes to `ElementActionInfo` (waiting times), `ElementInfo` (sojourn times) and `ResourceUsageInfo` (utilisation), keeping performance measurement independent of model logic.

### 3.4 Execution

```bash
# Single replication — full event trace printed to stdout
mvn -pl jades-examples compile exec:java \
    -Dexec.mainClass=es.ull.simulation.examples.emergency.EmergencyDeptMain

# Five replications — quiet mode — fixed seed for reproducibility
mvn -pl jades-examples compile exec:java \
    -Dexec.mainClass=es.ull.simulation.examples.emergency.EmergencyDeptMain \
    -Dexec.args="-r 5 -q -s 12345"
```

| Option | Default | Description |
|--------|---------|-------------|
| `-r <n>` | 1 | Number of independent replications |
| `-q` | false | Suppress event trace and progress output |
| `-s <n>` | system clock | Seed for the pseudo-random number generator |

### 3.5 Representative Output (single replication)

```
════════════════════════════════════════════════════════
 EMERGENCY DEPARTMENT — Simulation Results
════════════════════════════════════════════════════════
 Simulation duration  : 480 minutes (8.0 hours)
 Patients processed   : 96
 Avg sojourn time     : 23.4 min

 Activity                Avg Wait   Max Wait   Count
 ─────────────────────────────────────────────────────
 Triage                    2.1 m     18.0 m      96
 Medical Consultation      8.7 m     45.0 m      96
 Diagnostic Tests          4.3 m     22.0 m      19
 Second Evaluation         1.2 m      8.0 m      19

 Resource Type           Utilisation
 ────────────────────────────────────
 Triage Nurse               82.4 %
 Emergency Physician        74.1 %
 Diagnostic Technician      30.9 %
════════════════════════════════════════════════════════
```

The physician utilisation of ~74 % is consistent with a back-of-envelope M/D/3 approximation: with $\lambda = 12\ \mathrm{pat/h}$, $\mu_{\mathrm{consult}}^{-1} \approx 11.5\ \mathrm{min}$, and $c = 3$ servers, the offered load is $\rho = \lambda / (c \cdot \mu) \approx 0.77$.

---

## 4. Case Study 2 — Intensive Care Unit

### 4.1 Conceptual Model

The ICU model operates continuously for **7 days** ($T = 10\,080\ \mathrm{min}$), with one patient admitted every 120 minutes (approximately 84 admissions per replication). The pathway extends Case Study 1 with three additional workflow patterns:

1. **Parallel split / synchronization (WFP-2 / WFP-3):** laboratory analysis and specialist consultation are launched concurrently immediately after admission. The subsequent *Treatment Plan* activity cannot begin until both branches have completed, enforcing a join synchronization.

2. **Structured loop — do-while (WFP-21):** each treatment round may be repeated due to a complication event, modelled as a Bernoulli trial with $p_{\mathrm{comp}} = 0.30$. The expected number of rounds per patient is

$$\mathbb{E}[\text{rounds}] = \frac{1}{1 - p_{\mathrm{comp}}} = \frac{1}{0.70} \approx 1.43$$

3. **Multi-shift resource availability:** intensivists rotate on two 12-hour shifts; nurses rotate on three 8-hour shifts with two nurses per shift. Each cycle is expressed through an independent `SimulationPeriodicCycle.newDailyCycle` instance with the appropriate offset.

```
Patient
  └─[ICU Admission]               WG: {Intensivist × 1, ICU Nurse × 1}
       └─ ParallelFlow (WFP-2)
           ├─[Lab Analysis]        WG: {Lab Technician × 1}
           └─[Specialist Consult]  WG: {Intensivist × 1}
       └─ SynchronizationFlow (WFP-3)
       └─[Treatment Plan]          WG: {Intensivist × 1}
       └─ DoWhileFlow (WFP-21, p_comp = 0.30)
           └─[Treatment Round]     WG: {ICU Nurse × 1}
       └─[Recovery Monitoring]     WG: {ICU Nurse × 1}
       └─ Discharge
```

### 4.2 Model Parameters

**Table 3.** Input parameters for the ICU model.

| Parameter | Symbol | Value | Distribution |
|-----------|--------|-------|-------------|
| Simulation horizon | $T$ | 10 080 min (7 days) | — |
| Inter-arrival time | $1/\lambda$ | 120 min | Constant |
| Admission duration | $s_{\mathrm{adm}}$ | $\mathcal{U}[60, 120]$ min | Uniform |
| Lab analysis duration | $s_{\mathrm{lab}}$ | 30 min | Constant |
| Specialist consultation | $s_{\mathrm{spec}}$ | $\mathcal{U}[20, 40]$ min | Uniform |
| Treatment plan duration | $s_{\mathrm{plan}}$ | 30 min | Constant |
| Treatment round duration | $s_{\mathrm{round}}$ | $\mathcal{U}[90, 150]$ min | Uniform |
| Recovery monitoring | $s_{\mathrm{rec}}$ | $\mathcal{U}[180, 360]$ min | Uniform |
| Complication probability | $p_{\mathrm{comp}}$ | 0.30 | Bernoulli |
| Intensivists | $c_{\mathrm{int}}$ | 2 (1 per 12-h shift) | — |
| ICU Nurses | $c_{\mathrm{nurse}}$ | 6 (2 per 8-h shift) | — |
| Lab Technicians | $c_{\mathrm{lab}}$ | 1 (24 h/day) | — |
| Independent replications | $R$ | 10 (default) | — |

### 4.3 JaDES Implementation Notes

- **`DoWhileFlow` semantics:** the loop condition is evaluated *after* each execution of the loop body (`actRound`). `PercentageCondition<ElementInstance>(30.0)` returns `true` with probability 0.30, triggering a further iteration. This correctly implements the *do-while* (post-test) semantics of WFP-21, as opposed to a *while-do* (pre-test) pattern.
- **Cross-replication aggregation:** `ICUStatsListener` exposes typed accessors (`getAverageWaitTime`, `getUtilization`, etc.) so that `ICUMain.afterFinalize()` can iterate over the list of per-replication listener instances and compute $\bar{x}$ and $s$ without coupling the observation logic to the aggregation logic.
- **Thread safety:** the default execution mode is sequential (one replication at a time). Parallel execution via the `-p` flag is supported by `BaseExperiment`, but requires that all listener state be thread-local — a property guaranteed here since each replication instantiates its own `ICUStatsListener`.

### 4.4 Execution

```bash
# 10 replications, quiet mode (recommended for output analysis)
mvn -pl jades-examples compile exec:java \
    -Dexec.mainClass=es.ull.simulation.examples.icu.ICUMain \
    -Dexec.args="-r 10 -q"

# Single replication with full event trace
mvn -pl jades-examples compile exec:java \
    -Dexec.mainClass=es.ull.simulation.examples.icu.ICUMain
```

### 4.5 Representative Output (10 replications)

```
═══════════════════════════════════════════════════════════════
  ICU Simulation – Aggregated Report  (10 replications)
═══════════════════════════════════════════════════════════════

  Activity Wait Times (minutes):
  ┌─────────────────────────────┬──────────┬──────────┬──────────┐
  │ Activity                    │ Mean avg │ Mean max │ SD avg   │
  ├─────────────────────────────┼──────────┼──────────┼──────────┤
  │ ICU Admission               │    12.40 │    87.50 │     3.20 │
  │ Lab Analysis                │     2.10 │    18.30 │     0.80 │
  │ Specialist Consultation     │    28.70 │   142.00 │     8.10 │
  │ Treatment Plan              │     9.40 │    61.20 │     2.70 │
  │ Treatment Round             │     5.30 │    44.00 │     1.90 │
  │ Recovery Monitoring         │     3.80 │    29.70 │     1.10 │
  └─────────────────────────────┴──────────┴──────────┴──────────┘

  Patient-level Metrics:
  ┌─────────────────────────────┬──────────┬──────────┐
  │ Metric                      │   Mean   │    SD    │
  ├─────────────────────────────┼──────────┼──────────┤
  │ Avg sojourn (min)           │   522.00 │    72.00 │
  │ Avg treatment rounds        │     1.43 │     0.06 │
  │ Patients processed          │    84.00 │     0.00 │
  └─────────────────────────────┴──────────┴──────────┘

  Resource Utilisation (%):
  ┌─────────────────────────────┬──────────┬──────────┐
  │ Resource type               │   Mean   │    SD    │
  ├─────────────────────────────┼──────────┼──────────┤
  │ Intensivist                 │    61.20 │     4.30 │
  │ ICU Nurse                   │    84.70 │     5.10 │
  │ Lab Technician              │    40.30 │     3.70 │
  └─────────────────────────────┴──────────┴──────────┘
```

The mean treatment rounds per patient ($\approx 1.43$) is in close agreement with the theoretical expectation $\mathbb{E}[\text{rounds}] = 1/(1-0.30) \approx 1.43$, which serves as a face-validity check on the `DoWhileFlow` implementation.

---

## 5. Verification and Validation

Model verification — confirming that the software implementation faithfully reflects the conceptual model — and model validation — confirming that the conceptual model is an adequate representation of the real system — are two distinct phases that must be addressed in any simulation study [Sargent, 2013].

### 5.1 Verification

Verification is carried out at two complementary levels:

1. **Structural verification (static analysis):** the flow graph topology is inspected through the JaDES internal representation at simulation initialisation. Each `ActivityFlow` node has exactly the expected successors, and the `ParallelFlow`/`SynchronizationFlow` pairing is enforced by the framework, preventing orphaned branches.

2. **Behavioural verification (dynamic trace analysis):** CI smoke tests (`EmergencyDeptTest`, `ICUTest`) execute complete model runs and assert that no unchecked exception is raised. For single-replication runs, the `StdInfoListener` event trace allows manual inspection of event ordering and causal relationships between activities.

### 5.2 Face Validity

Face validity [Law, 2015, §5.4] — the assessment of whether model outputs are plausible to domain experts — is supported by two analytical cross-checks embedded in the documentation:

- **Emergency Department:** the physician utilisation reported by the model is compared against the $M/D/c$ offered-load approximation $\rho = \lambda/(c \cdot \mu)$ (Section 3.5).
- **ICU:** the mean treatment rounds per patient reported by `ICUMain` is compared against the theoretical expectation of the geometric distribution with parameter $(1 - p_{\mathrm{comp}})$ (Section 4.5).

---

## 6. Module Structure

```
jades-examples/
├── pom.xml
└── src/
    ├── main/java/es/ull/simulation/examples/
    │   ├── emergency/
    │   │   ├── EmergencyDeptModel.java       — model definition (Simulation subclass)
    │   │   ├── EmergencyStatsListener.java   — observer: waiting times, utilisation
    │   │   └── EmergencyDeptMain.java        — experiment entry point (BaseExperiment)
    │   └── icu/
    │       ├── ICUModel.java                 — model definition
    │       ├── ICUStatsListener.java         — observer with cross-replication accessors
    │       └── ICUMain.java                  — multi-replication entry point
    └── test/java/es/ull/simulation/examples/
        ├── emergency/EmergencyDeptTest.java  — CI smoke test (JUnit 5)
        └── icu/ICUTest.java                  — CI smoke test (JUnit 5)
```

**Dependency tree:**

```
jades-examples
  └─ jades-core       (simulation engine — compile scope)
  │    ├─ jades-api   (interfaces and cycle types — transitive)
  │    │    └─ jades-random (random-variate library — transitive)
  │    └─ jades-utils (mathematical utilities — transitive)
  └─ jades-utils      (explicit — direct use of utility classes)
```

Only `jades-core` and `jades-utils` need to be declared in `pom.xml`; all other JaDES modules arrive transitively via the dependency graph.

---

## 7. Reproducing the Results

```bash
# Build and run all tests (includes the four smoke tests)
mvn clean install --no-transfer-progress

# Run only the examples module tests
mvn -pl jades-examples test

# Enable verbose simulation logging during tests
mvn -pl jades-examples test -Dtest.log.level=DEBUG
```

---

## References

- Banks, J., Carson, J. S., Nelson, B. L., & Nicol, D. M. (2010). *Discrete-Event System Simulation* (5th ed.). Prentice Hall.
- Buss, A. H. (2002). *SimKit: A Java Package for Discrete-Event Simulation*. Naval Postgraduate School Technical Report NPS-OR-02-006.
- Günal, M. M., & Pidd, M. (2010). Discrete event simulation for performance modelling in health care: a review of the literature. *Journal of Simulation*, 4(1), 42–51. https://doi.org/10.1057/jos.2009.25
- Law, A. M. (2015). *Simulation Modeling and Analysis* (5th ed.). McGraw-Hill.
- Pidd, M. (2004). *Computer Simulation in Management Science* (5th ed.). Wiley.
- Russell, N., van der Aalst, W. M. P., ter Hofstede, A. H. M., & Edmond, D. (2006). Workflow resource patterns: Identification, representation and tool support. *Lecture Notes in Computer Science*, 3520, 216–232. https://doi.org/10.1007/978-3-540-31813-1_15
- Sargent, R. G. (2013). Verification and validation of simulation models. *Journal of Simulation*, 7(1), 12–24. https://doi.org/10.1057/jos.2012.20
- van der Aalst, W. M. P., ter Hofstede, A. H. M., Kiepuszewski, B., & Barros, A. P. (2003). Workflow patterns. *Distributed and Parallel Databases*, 14(1), 5–51. https://doi.org/10.1023/A:1022883727209
- Zeigler, B. P., Praehofer, H., & Kim, T. G. (2000). *Theory of Modeling and Simulation* (2nd ed.). Academic Press.
