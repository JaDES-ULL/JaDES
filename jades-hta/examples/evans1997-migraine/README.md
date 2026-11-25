# Evans et al. (1997): sumatriptan versus caffeine/ergotamine for migraine

Evans KW, Boan JA, Evans JL, Shuaib A. Economic evaluation of oral sumatriptan compared with oral caffeine/ergotamine for migraine. *PharmacoEconomics*. 1997;12(5):565-577.

The study compares two oral treatments of a migraine attack in Canada with a decision tree over 24 hours. This example represents it in OSDi ([evans1997-migraine.ttl](evans1997-migraine.ttl)) and checks that JaDES reproduces its results ([expected-results.json](expected-results.json)).

## The model

- The attack is treated with the drug of the arm: sumatriptan (CAD 16.10 per dose) or caffeine/ergotamine (CAD 1.32).
- If the drug does not relieve the attack (probability 0.442 with sumatriptan, 0.621 with caffeine/ergotamine), the patient loses 1.3/365 QALYs and is rescued at home (0.92) or in the emergency department (0.08; CAD 63.16).
- The relief in the emergency department has a QALY loss of −0.4/365, i.e., a gain. If it fails (0.002), the patient is hospitalized (CAD 1093).
- If the drug relieves the attack, the attack may come back (relapse: 0.406 with sumatriptan, 0.297 with caffeine/ergotamine; 0.1/365 QALYs). A relapse is treated with another dose of the drug, which always relieves it.
- Time horizon of 24 hours (1/365 years), no discount, base utility 1, costs in Canadian dollars of 1997, the year of the study.

## How it is represented in OSDi

- The migraine is a `Disease` and the attack, an `AcuteManifestation`. The attack is the target of both interventions (`isInterventionOf`): every patient starts with an attack, and the tree evaluates it (decision D1). The migraine does not declare `hasIntervention`, which would give each intervention a second target.
- Each intervention replaces the treatment of the attack by its drug (`StrategyReplacementEffect` with `hasReplacementStrategy`, D2).
- The drug is a `LineOfTherapy` with a `SuccessOutcome` and a `FailureOutcome` (D4). The relief resolves the attack (`resolvesHealthCondition`).
- The failure of the drug is followed by a `Group` of alternative rescues, each one with its `hasProportionWithinGroup`: at home and in the emergency department.
- The emergency department is a strategy with its own outcomes: relief (with a negative QALY loss) and failure, followed by the hospitalization.
- The relapse follows the pattern of D9: the relief is followed by a `FollowUpStrategy` without cost, whose failure is the relapse, followed by the second dose.
- QALY losses are one-time utilities (`appliesOneTime`, `isDisutility`), which apply once at the outcome (D7).
- Costs use `Currency_CanadianDollar` with the year of the study, so they are not updated with the CPI.

## Assumptions of the article made explicit

They are also written in the descriptions of the individuals.

- After the relief in the emergency department there is no follow-up, so there is no relapse.
- The second dose after a relapse always relieves the attack (a strategy without outcomes).
- Staying at home, the emergency department, the hospitalization and the second dose have no QALY loss of their own.
- The base utility is 1.

## Modeling decisions

- The second dose is another strategy (`EVANS_SumatriptanSecondDose`, `EVANS_CafergotSecondDose`) with the same cost parameter as the drug, since chains of strategies cannot go back to a strategy already applied: a repetition is modeled as another strategy.
- The outcomes do not declare an evaluation time, so they are evaluated at the start of the strategy, with a warning. With a time horizon of 24 hours this does not change the results.
- The population does not declare age, sex or life expectancy: none of them affects the results. JaDES assumes 18 years, 50 % women and a life expectancy of 100 years, with warnings.

## Results and known differences

| Measure | Caffeine/ergotamine | Sumatriptan |
| --- | --- | --- |
| Cost (CAD) | 4.71497244 | 22.05805736 |
| QALYs | 0.201275956/365 | 0.416860912/365 |

The incremental cost-effectiveness ratio is CAD 29 363 per QALY gained, against CAD 29 366 in the article. The difference (0.01 %) comes from the rounding of the published inputs. Costs and QALYs per arm were computed by hand from the model; only the ratio is compared with the article, with a relative tolerance of 0.001.

## Pending

- The table of the article where the ratio is reported has to be checked, and other published values (e.g., the expected costs per arm) may be added to `expected-results.json`.
- The individuals use the namespace of OSDi (`EVANS_…`), because JaDES finds the experiment by its short name in that namespace. The ontology imports OSDi 1.2 until OSDi 1.3 is published and JaDES uses it: then the import becomes `https://w3id.org/ontologies-ULL/OSDi/1.3` (see `docs/osdi-1.3-traslado.md` in JaDES).
