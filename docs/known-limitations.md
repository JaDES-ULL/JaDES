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

The discrete-event simulation should interpret progressions without risk in the same way, but it currently does not: `DiseaseProgressionRiskFactory.getPathwayInstances` creates no pathway for a progression without risk, so it never happens, unless the population assigns it an initial proportion (`hasInitialProportion`).

## jades-hta: Acute manifestations in decision trees

TODO: Acute manifestations are not supported yet in decision trees. Until they are, `DiseaseSubTreeGenerator` rejects any model that includes an acute manifestation with an `UnsupportedOSDiFeatureException`. Before, they were treated as chronic manifestations, so their annual payoffs were applied from their onset until death.

## jades-hta: Onset and end ages in decision trees

The onset and end ages of a progression (`hasOnsetAge`, `hasEndAge`) are ages, not times since the start of the model. The decision tree generator (`PayoffWrapper.applyDiscountToFormula`) translates them into times by subtracting the initial age of the population (`hasAge`; 0 if the population does not define it, with a warning). Only what happens between the initial age of the population and death (the undiscounted life years of each leaf) is accounted:

- Annual costs and disutilities are accounted from `MAX(0, onset age - initial age)` to `MIN(life years, end age - initial age)`. If the window starts after death, nothing is accounted.
- A one-time cost or disutility with an onset age is accounted at that age only if it happens between the initial age and death. A one-time payoff whose onset age is lower than the initial age is assumed to have happened before the start of the model, and it is not accounted.
