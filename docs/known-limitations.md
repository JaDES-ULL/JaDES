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

TODO: Acute manifestations are not supported yet in decision trees. Until they are, `DiseaseSubTreeGenerator` rejects any model that includes an acute manifestation with an `UnsupportedOSDiFeatureException`. Before, they were treated as chronic manifestations, so their annual payoffs were applied from their onset until death.

## jades-hta: Onset and end ages in decision trees

The onset and end ages of a progression (`hasOnsetAge`, `hasEndAge`) are ages, not times since the start of the model. The decision tree generator (`PayoffWrapper.applyDiscountToFormula`) translates them into times by subtracting the initial age of the population (`hasAge`; 0 if the population does not define it, with a warning). Only what happens between the initial age of the population and death (the undiscounted life years of each leaf) is accounted:

- Annual costs and disutilities are accounted from `MAX(0, onset age - initial age)` to `MIN(life years, end age - initial age)`. If the window starts after death, nothing is accounted.
- A one-time cost or disutility with an onset age is accounted at that age only if it happens between the initial age and death. A one-time payoff whose onset age is lower than the initial age is assumed to have happened before the start of the model, and it is not accounted.

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
| `hasFrequency` | Ignored silently (the property is never read). | Ignored silently (the property is never read). |
| Acute manifestations | Not supported: `DiseaseSubTreeGenerator` throws an `UnsupportedOSDiFeatureException`. | Simulated as events: they are not added to the state of the patient (so they have no annual payoffs), their one-time payoffs apply at each occurrence, and they can happen again, since their next occurrence is scheduled after each event. They never get an initial proportion, and their probability of death is not loaded from OSDi. |
| Time horizon of the experiment (`hasTimeHorizon`) | Not used: the undiscounted life years of each leaf are limited by the `timeHorizon` named range of the Excel template (150), which the generator does not set. | Used to limit the simulation (100 years if not defined), and it can be overridden from the command line. |
| Combination methods (`hasCostCombinationMethod`, `hasDisutilityCombinationMethod`, `hasLifeExpectancyReductionCombinationMethod`, `hasMortalityRateCombinationMethod`) | Costs: `ADD`, `ADD_DISTINCT`. Disutilities: `ADD`, `ADD_DISTINCT`, `MAX`, only among annual disutilities (one-time disutilities are always added), and `MAX` is applied to the discounted lifetime totals. Life expectancy reductions: `ADD`, `ADD_DISTINCT`, `MAX`. Mortality rates: `MAX`, `MULT`. The life expectancy of a leaf is the base life expectancy divided by the combined increased mortality rate, minus the combined reduction. | The same four methods and values, applied to the progressions active at each moment of the simulation (`Disease.getCostWithinPeriod`, `Patient.getUtilityValue`, death submodels), so `MAX` is applied period by period. |

## jades-hta: Future work: developments in the DES

The DES does not simulate developments or combination rules yet. A possible correspondence with the semantics already implemented in the decision trees:

- A group of alternatives is a categorical draw at the start of the simulation.
- A coexistent development with risk `p` is an independent draw with probability `p`.
- A development without risk is always present.
- The payoffs of a development are annual payoffs while the patient has it.
- The risk of a manifestation of a development only applies to patients with such development.
- A manifestation without risk has an initial proportion of 1 in its context.
