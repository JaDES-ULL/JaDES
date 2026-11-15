# Known Limitations

## jades-hta: Utilities and disutilities

Both the discrete-event simulation (`UsesParameters.forceUtilityParameterValue`) and the decision tree generator (`PayoffWrapper.getQALEFormula`) apply the same rule: an absolute utility `u` (a `Utility` parameter with `isDisutility = false`) of a progression, the disease or an intervention is equivalent to a disutility of `(base utility of the population - u)`. If it is the only one active, the patient gets exactly `u`. The converted value is combined with the rest of disutilities by using the combination method of the experiment (`hasDisutilityCombinationMethod`), both for annual and one-time utilities.

This approach has the following limitations:

- **Base utility of the source.** Absolute utilities are converted into disutilities by subtracting them from the base utility of the model. If the source measured the utility against a different base utility, the decrement attributed to the condition will be wrong.
- **Single combination method.** Absolute utilities and disutilities are combined with a single method:
  - With `ADD`, an absolute utility measured in patients that already includes the effect of another condition is counted twice.
  - With `MAX`, independent disutilities are lost.
- **`MAX` in decision trees.** The combination method only applies to annual disutilities (one-time disutilities are always added). In the decision trees, `MAX` is applied to the discounted lifetime totals of each annual disutility, not year by year. If the disutilities have different onset or end ages, the result is not the maximum in each period (it should be computed by intervals).

### Possible improvements

- Treat each type differently: absolute utilities replace the current utility (taking the worst one), whereas disutilities are added.
- Add a multiplicative combination method.
- Indicate in OSDi the reference base utility of each source.

## jades-hta: Progressions without risk

A development or chronic manifestation without risk characterization (`hasRiskCharacterization`) that does not belong to a group of two or more alternative progressions (i.e., not affected by any combination rule, affected by a `CoexistentCombinationRule`, or by an `AlternativeCombinationRule` with a single member) is always active in its context. The decision tree generator (`DiseaseSubTreeGenerator`) does not create a chance node for it: its node is the only branch of its parent, with probability 1, so its payoffs apply to every leaf of the sub-tree that hangs from it. A manifestation without risk that depends on a development (`hasSubProgression`) is only active in the branches where such development is active. Within a group of two or more alternative progressions, the progression without risk takes the remaining probability (1 minus the sum of the rest); at most one progression of the group may lack a risk, and only if the group has no null progression.

The discrete-event simulation should interpret progressions without risk in the same way, but it currently does not: `DiseaseProgressionRiskFactory.getPathwayInstances` creates no pathway for a progression without risk, so it never happens, unless the population assigns it an initial proportion (`hasInitialProportion`). `DiseaseFactory` logs a warning for each manifestation that will never occur (see the conformance table below).

## jades-hta: Acute manifestations in decision trees

In a decision tree, an acute manifestation represents a **single episode** between the initial age of the population and death (the undiscounted life years of the leaf). `DiseaseSubTreeGenerator` builds its node as for a chronic manifestation, so it can belong to coexistent or alternative combination rules, depend on a development, and carry effects (instant death, life expectancy reduction, increased mortality rate) that apply to the leaf:

- **Risk:** its risk characterization is the probability that the episode happens. Intervention effects on it follow the general rules (see "Effect magnitudes"). An acute manifestation without risk follows the rule of progressions without risk: the episode always happens in its context (e.g., a tree that starts in a patient suffering a migraine attack).
- **Frequency (`hasFrequency`):** ignored with a warning if the acute manifestation also defines a risk. An acute manifestation that only defines a frequency is rejected with an `UnsupportedOSDiFeatureException` (N06); otherwise, it would silently become an episode that always happens. The frequency is not converted into the probability of at least one episode, 1 − e^(−f·H): the tree has no time horizon H (each leaf accounts for its own life years, and the time horizon of the experiment is not transferred to the Excel file), and counting a single episode would discard the expected number of episodes (f·H).
- **One-time costs and utilities:** they apply once, when the episode happens: at the start of the model, or at the onset age if defined (see "Onset and end ages in decision trees"). A one-time utility is a QALY loss. Several one-time payoffs of the same acute manifestation are added.
- **Annual costs and utilities:** ignored with a warning (`DiseaseSubTreeGenerator.generateNode`), as in the discrete-event simulation. Before acute manifestations were rejected, they were treated as chronic ones, so their annual payoffs were applied from their onset until death.
- `hasDuration` is ignored, as for any other progression.

Tests: P28 (risk and one-time payoffs), P29 (annual payoffs ignored), P30 (frequency ignored), P31 (without risk), P32 (intervention effect on the risk) and N06 (only a frequency).

Divergences with the discrete-event simulation: the simulation counts every episode (an acute manifestation can happen again), whereas the tree counts one; and, if an acute manifestation defines several one-time costs or utilities, the simulation only uses the first one (with a warning), whereas the tree adds all of them. Repeated episodes will be modeled through recurrence (D9 in `docs/osdi-1.3-diseno.md`).

## jades-hta: Onset and end ages in decision trees

The onset and end ages of a progression (`hasOnsetAge`, `hasEndAge`) are ages, not times since the start of the model. The decision tree generator (`PayoffWrapper.applyDiscountToFormula`) translates them into times by subtracting the initial age of the population (`hasAge`; 0 if the population does not define it, with a warning). Only what happens between the initial age of the population and death (the undiscounted life years of each leaf) is accounted:

- Annual costs and disutilities are accounted from `MAX(0, onset age - initial age)` to `MIN(life years, end age - initial age)`. If the window starts after death, nothing is accounted.
- A one-time cost or disutility with an onset age is accounted at that age only if it happens between the initial age and death. A one-time payoff whose onset age is lower than the initial age is assumed to have happened before the start of the model, and it is not accounted.

## jades-hta: Effect magnitudes

The magnitude of a generic `Effect` is applied to each affected parameter or attribute according to the data item types of the magnitude and of the affected item (`EffectWrapper.resolveOperation`, decision D3 of `docs/osdi-1.3-diseno.md`):

| Magnitude | Operation | Rejected or warned |
|---|---|---|
| `DI_Factor`, `DI_Ratio` | `x * m` | — |
| `DI_RelativeRisk` | On a probability (probability, proportion, incidence, prevalence, birth prevalence, sensitivity, specificity): `1 - (1 - x)^m` (the rate equivalent to the probability is multiplied). On anything else: `x * m` | Warning if the item is not a probability (`DI_Factor` should be used). `UnsupportedOSDiFeatureException` on a time to event (a hazard ratio must be used) |
| `DI_HazardRatio` | On a probability: `1 - (1 - x)^m`. On a time to event: `x / m` (exponential time) | Warning on a time to event; `UnsupportedOSDiFeatureException` if the time has a distribution (first-order parameter); `MalformedOSDiModelException` on anything else |
| `DI_OddsRatio` | On a probability: `x * m / (1 - x + x * m)` | `MalformedOSDiModelException` on anything else |
| `DI_MeanDifference` | `x - m` | — |
| `DI_Continuous_Variable` and any other absolute type | `m` (replaces) | Warning if the type does not match that of the item, or if it is undefined (`DI_Undefined`, `DI_Other`) |
| `DI_StandardDeviation`, `DI_Lower95ConfidenceLimit`, `DI_Upper95ConfidenceLimit` | — | `MalformedOSDiModelException` |

A relative magnitude always modifies the item, even if both have the same type (e.g., a relative risk that modifies an increased mortality rate). The subclasses of `Effect` keep their own operation: instant death replaces, life expectancy reduction subtracts and increased mortality rate multiplies.

When several effects modify the same parameter (in a path of the decision tree, or in an intervention of the DES), they are applied in the order: replace, multiply (linear factors, then rate ratios, then odds ratios) and subtract (`EffectWrapper.sortForParameter`). More than one replacement of the same parameter is rejected with a `MalformedOSDiModelException`. A warning is logged if the multiplications use different scales.

Limitations:

- The rate conversion assumes a constant rate within the period of the probability. A relative risk is treated as a ratio of rates; to multiply a probability linearly (e.g., when the relative risk and the probability come from the same source and period), the magnitude must be a `DI_Factor`.
- Linear operations (`DI_Factor`, `DI_Ratio`, `DI_MeanDifference`) on a probability may produce values out of [0, 1]. They are not truncated; a warning is logged when both the probability and the magnitude are deterministic.
- TODO: Effects on a time to event are rejected in decision trees (`UnsupportedOSDiFeatureException`), because the generator does not support times to event yet: the probability derived from the time (`P_<time>`) is not written in the Excel file.
- The DES only applies the effects whose cause is an intervention, and it does not support times to event as risks yet.

## jades-hta: Conformance between the decision tree and the DES

OSDi defines the semantics of a model; the decision tree generator (`osdi.decisiontree`) and the discrete-event simulation (`osdi.des`) implement it to a different extent. The table describes what each one does today. When a feature is not supported, the generator either rejects the model (`UnsupportedOSDiFeatureException`), logs a warning, or ignores it silently.

| OSDi feature | Decision tree | DES |
|---|---|---|
| Developments and their payoffs | Each development is a node of the tree (a chance node, the default branch of a group of alternatives, or an always active branch). Its costs (including those of its follow-up and treatment strategies), utilities and effects apply to every leaf of the sub-tree that hangs from it. | Built as a `Development` with only a name and a description: it never happens, and its costs, utilities and effects are not applied. `DiseaseFactory` logs a warning ("ignored by the DES") for each development with costs, strategies, utilities or effects. |
| Coexistent rules (`CoexistentCombinationRule`) | Processed as progressions without rule: an independent chance node per progression. | Ignored; `DiseaseFactory` logs a warning ("ignored by the DES") for each rule. Since every manifestation has its own independent pathway, the result for manifestations is the same as without rule. |
| Alternative rules (`AlternativeCombinationRule`, two or more members) | A single chance node with a branch per member. The member without risk takes the remaining probability (1 minus the sum of the rest); `hasNullProgression` adds a branch with no progression. Two members without risk, or a member without risk plus a null progression, are rejected (`MalformedOSDiModelException`). | Ignored; `DiseaseFactory` logs a warning ("ignored by the DES") for each rule. Each member with risk has its own pathway, so the members are competing but not mutually exclusive (unless `supersedesClinicalProgression` says so), and the member without risk never happens. |
| Progression without risk (outside a group of two or more alternatives) | Always active in its context: no chance node, its node is the only branch of its parent (probability 1). A manifestation without risk that depends on a development is active whenever such development is active. | No pathway is created, so it never happens unless the population assigns it an initial proportion. `DiseaseFactory` logs a warning ("will never occur in the DES") for each manifestation with neither risk nor initial proportion. |
| `hasInitialProportion` | Not supported: `DiseaseSubTreeGenerator` throws an `UnsupportedOSDiFeatureException` (trees assume that every patient starts without progressions). | Only for non-acute manifestations. At the start, each patient starts with the manifestation with such probability (`Disease.getInitialStage`), removing those superseded by others. A patient that starts with it does not go through its risk; the rest can still develop it through its risk. |
| `supersedesClinicalProgression` | Ignored silently: never read by the decision tree generator. | When a chronic progression happens, the progressions it supersedes are removed from the state of the patient; events of progressions superseded by the current state are cancelled; and the initial state does not include superseded progressions. |
| `hasDuration` | Ignored silently (the property is never read). | Ignored silently (the property is never read). |
| `hasFrequency` | Only read for acute manifestations: ignored with a warning if they define a risk; rejected (`UnsupportedOSDiFeatureException`) if they do not. | Ignored silently (the property is never read). |
| Acute manifestations | A single episode within the lifetime, whose probability is the risk (see "Acute manifestations in decision trees"). One-time payoffs apply once; annual payoffs are ignored with a warning. | Simulated as events: they are not added to the state of the patient (so they have no annual payoffs), their one-time payoffs apply at each occurrence, and they can happen again, since their next occurrence is scheduled after each event. They never get an initial proportion, and their probability of death is not loaded from OSDi. |
| Time horizon of the experiment (`hasTimeHorizon`) | Not used: the undiscounted life years of each leaf are limited by the `timeHorizon` named range of the Excel template (150), which the generator does not set. | Used to limit the simulation (100 years if not defined), and it can be overridden from the command line. |
| Combination methods (`hasCostCombinationMethod`, `hasDisutilityCombinationMethod`, `hasLifeExpectancyReductionCombinationMethod`, `hasMortalityRateCombinationMethod`) | Costs: `ADD`, `ADD_DISTINCT`. Disutilities: `ADD`, `ADD_DISTINCT`, `MAX`, only among annual disutilities (one-time disutilities are always added), and `MAX` is applied to the discounted lifetime totals. Life expectancy reductions: `ADD`, `ADD_DISTINCT`, `MAX`. Mortality rates: `MAX`, `MULT`. The life expectancy of a leaf is the base life expectancy divided by the combined increased mortality rate, minus the combined reduction. | The same four methods and values, applied to the progressions active at each moment of the simulation (`Disease.getCostWithinPeriod`, `Patient.getUtilityValue`, death submodels), so `MAX` is applied period by period. |
| Effect magnitudes (`hasEffectMagnitude`) | Applied in the Excel formula of each parameter, with the operation given by the data item types (see "Effect magnitudes"); all the effects of the path, in the order replace, multiply, subtract. Effects on a time to event are rejected. | Applied by a `CompositeParameterModifier` per parameter and intervention, with the same operations and order. Only effects caused by an intervention. |

## jades-hta: Future work: developments in the DES

The DES does not simulate developments or combination rules yet. A possible correspondence with the semantics already implemented in the decision trees:

- A group of alternatives is a categorical draw at the start of the simulation.
- A coexistent development with risk `p` is an independent draw with probability `p`.
- A development without risk is always present.
- The payoffs of a development are annual payoffs while the patient has it.
- The risk of a manifestation of a development only applies to patients with such development.
- A manifestation without risk has an initial proportion of 1 in its context.
