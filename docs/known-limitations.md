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
