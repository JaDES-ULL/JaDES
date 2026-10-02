package es.ull.simulation.hta.osdi.decisiontree.excelport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import org.apache.poi.ss.util.CellReference;
import es.ull.simulation.hta.osdi.OSDiLogger;
import es.ull.simulation.hta.osdi.decisiontree.BranchDestinationNode;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.TemporalConstraint;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper.SpecificInformationForCost;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper.SpecificInformationForUtility;
import es.ull.simulation.hta.osdi.ontology.EffectWrapper;
import es.ull.simulation.hta.osdi.ontology.IModelItemWrapper;
import es.ull.simulation.hta.osdi.ontology.OSDiWrapper;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper;
import es.ull.simulation.hta.outcomes.CombinationMethod;
import es.ull.simulation.hta.outcomes.CostCombinationMethod;
import es.ull.simulation.hta.outcomes.DisutilityCombinationMethod;
import es.ull.simulation.hta.outcomes.LifeExpectancyReductionCombinationMethod;
import es.ull.simulation.hta.outcomes.MortalityRateCombinationMethod;
import es.ull.simulation.utils.ExcelFormulaLibrary;

/**
 * Wrapper class for HTA payoffs in Excel.
 * This class is responsible for managing the various parameters and their associated Excel representations
 * for the payoffs in a Health Technology Assessment (HTA) context.
 * <p>
 * The wrapper accumulates the payoffs of every node along a path of the tree. A parameter that is shared by several nodes
 * of the same path is kept once per node, together with the temporal constraint of that node. How these payoffs are combined 
 * is decided when the formulas are built, according to the combination methods defined in the experiment 
 * ({@link CostCombinationMethod}, {@link DisutilityCombinationMethod}, {@link LifeExpectancyReductionCombinationMethod} and 
 * {@link MortalityRateCombinationMethod}). One-time costs and disutilities are always added, once per node.
 * </p>
 */
public class PayoffWrapper {
    /**
     * Logger for the PayoffWrapper class.
     */
    private final static OSDiLogger log = OSDiLogger.getLogger(PayoffWrapper.class);
    /**
     * A cost or utility parameter contributed by a node of the tree, together with the temporal constraint of such node.
     * @param parameter The parameter
     * @param constraint The temporal constraint of the node that contributed the parameter (may be null)
     */
    private record ParameterEntry(ParameterWrapper parameter, TemporalConstraint constraint) {}
    /**
     * The list of probabilities associated with this node, which is used to calculate the expected value of the decision represented by this node.
     */
    private final ArrayList<CellReference> probabilities;
    /**
     * The cost parameters contributed by the nodes along the path, in order of appearance.
     */
    private final ArrayList<ParameterEntry> costEntries;
    /**
     * The disutility parameters contributed by the nodes along the path, in order of appearance.
     */
    private final ArrayList<ParameterEntry> disutilityEntries;
    /**
     * The distinct effects associated with the nodes along the path. Used to modify parameters and probabilities.
     */
    private final Set<EffectWrapper> effects;
    /**
     * The effects contributed by the nodes along the path, in order of appearance and including repetitions. Used to compute the 
     * outcomes (life expectancy) according to the combination methods.
     */
    private final ArrayList<EffectWrapper> effectEntries;
    /**
     * The OSDi wrapper associated with this payoff wrapper.
     */
    private final OSDiWrapper wrap;

    /**
     * Constructor for the wrapper of the payoff for an HTA model in Excel.
     * This constructor initializes the context and the lists of costs, disutilities and lifetime reductions, if the intervention defines them.
     */
    public PayoffWrapper(OSDiWrapper wrap) {
        this.wrap = wrap;
        this.probabilities = new ArrayList<>();
        this.costEntries = new ArrayList<>();
        this.disutilityEntries = new ArrayList<>();
        this.effects = new TreeSet<>();
        this.effectEntries = new ArrayList<>();
    }

    /**
     * Copy constructor for the HTAExcelPayoffWrapper class.
     * @param original The original HTAExcelPayoffWrapper to copy.
     */
    public PayoffWrapper(PayoffWrapper original) {
        this.wrap = original.wrap;
        this.probabilities = new ArrayList<>(original.probabilities);
        this.costEntries = new ArrayList<>(original.costEntries);
        this.disutilityEntries = new ArrayList<>(original.disutilityEntries);
        this.effects = new TreeSet<>(original.effects);
        this.effectEntries = new ArrayList<>(original.effectEntries);
    }

    /**
     * Gets the list of probabilities associated with this node, used to compute the probability of the decision represented by this node.
     * @return The list of probabilities.
     */
    public ArrayList<CellReference> getProbabilities() {
        return probabilities;
    }

    /**
     * Gets the cost parameters contributed by the nodes along the path, including repetitions.
     * @return The list of cost parameters.
     */
    public List<ParameterWrapper> getCostParameters() {
        return costEntries.stream().map(entry -> entry.parameter()).toList();
    }

    /**
     * Gets the distinct disutility parameters contributed by the nodes along the path.
     * @return The set of disutility parameters.
     */
    public Set<ParameterWrapper> getDisutilityParameters() {
        final Set<ParameterWrapper> result = new TreeSet<>();
        for (ParameterEntry entry : disutilityEntries) {
            result.add(entry.parameter());
        }
        return result;
    }

    /**
     * Gets the distinct effects associated with the nodes along the path, which are used to modify parameters and probabilities.
     * @return The set of effects.
     */
    public Set<EffectWrapper> getEffects() {
        return effects;
    }

    /**
     * Adds a probability cell reference to this wrapper.
     * @param probability The probability cell reference to add.
     */
    public void addProbability(CellReference probability) {
        this.probabilities.add(probability);
    }

    /**
     * Adds a cost parameter with no temporal constraint to this wrapper.
     * @param parameter The cost parameter to add.
     */
    public void addCostParameter(ParameterWrapper parameter) {
        this.costEntries.add(new ParameterEntry(parameter, null));
    }

    /**
     * Adds a disutility parameter with no temporal constraint to this wrapper.
     * @param parameter The disutility parameter to add.
     */
    public void addDisutilityParameter(ParameterWrapper parameter) {
        this.disutilityEntries.add(new ParameterEntry(parameter, null));
    }

    /**
     * Adds an effect to this wrapper.
     * @param effect The effect to add.
     */
    public void addEffect(EffectWrapper effect) {
        this.effects.add(effect);
        this.effectEntries.add(effect);
    }

    /**
     * Adds a list of probabilities to this wrapper.
     * @param probabilities The list of probability cell references to add.
     */
    public void addProbabilities(Collection<CellReference> probabilities) {
        this.probabilities.addAll(probabilities);
    }

    /**
     * Adds a list of effects to this wrapper.
     * @param effects The list of effects to add.
     */
    public void addEffects(Collection<EffectWrapper> effects) {
        for (EffectWrapper effect : effects) {
            addEffect(effect);
        }
    }

    /**
     * Updates this wrapper with the parameters and constraints from a decision tree node.
     * @param node The decision tree node to update from.
     */
    public void updateWithTreeNode(BranchDestinationNode node) {
        final TemporalConstraint constraint = node.getTemporalConstraint();
        // Add cost parameters from the node
        for (ParameterWrapper param : node.getCostParameters()) {
            this.costEntries.add(new ParameterEntry(param, constraint));
        }
        // Add disutility parameters from the node
        for (ParameterWrapper param : node.getDisutilityParameters()) {
            this.disutilityEntries.add(new ParameterEntry(param, constraint));
        }
        // Add effects (e.g. lifetime reductions) from the node
        this.addEffects(node.getEffects());
    }

    /**
     * Selects the entries that must be included in a formula. One-time entries are always included. Annual entries are included once 
     * per node, unless the combination method counts shared items once; in such case, only the first entry for each parameter is included.
     * @param entries The entries to select from.
     * @param method The combination method.
     * @param payoffName A name for the payoff (used in error messages).
     * @return The selected entries.
     * @throws UnsupportedOSDiFeatureException If a parameter that must be counted once is shared by nodes with different temporal constraints.
     */
    private List<ParameterEntry> selectEntries(List<ParameterEntry> entries, CombinationMethod method, String payoffName) throws UnsupportedOSDiFeatureException {
        final ArrayList<ParameterEntry> selected = new ArrayList<>();
        final Map<String, ParameterEntry> firstEntries = new HashMap<>();
        for (ParameterEntry entry : entries) {
            if (method.countsSharedItemsOnce() && !appliesOneTime(entry.parameter())) {
                final String key = entry.parameter().getIndividualIRI().toString();
                final ParameterEntry first = firstEntries.get(key);
                if (first != null) {
                    if (!sameConstraint(first.constraint(), entry.constraint())) {
                        throw new UnsupportedOSDiFeatureException("The " + payoffName + " parameter " + entry.parameter().getShortName() 
                            + " is shared by several items with different temporal constraints, and the experiment combines it with " + method 
                            + ". Combining shared " + payoffName + " parameters with different onset or end times is not supported. Use a different parameter for each item or a non-distinct combination method.");
                    }
                    log.debug("The " + payoffName + " parameter " + entry.parameter().getShortName() + " is shared by several items in the same path. It is considered only once (" + method + ").");
                    continue;
                }
                firstEntries.put(key, entry);
            }
            selected.add(entry);
        }
        return selected;
    }

    /**
     * Returns true if the parameter is a cost or utility that applies only once.
     * @param param The parameter.
     * @return true if the parameter is a cost or utility that applies only once.
     */
    private static boolean appliesOneTime(ParameterWrapper param) {
        if (param.getSpecificInformation() instanceof SpecificInformationForCost info) {
            return info.appliesOneTime();
        }
        if (param.getSpecificInformation() instanceof SpecificInformationForUtility info) {
            return info.appliesOneTime();
        }
        return false;
    }

    /**
     * Checks whether two temporal constraints are equivalent, i.e., they use the same parameters to define start and end times.
     * @param c1 First constraint (may be null).
     * @param c2 Second constraint (may be null).
     * @return true if both constraints are equivalent.
     */
    private static boolean sameConstraint(TemporalConstraint c1, TemporalConstraint c2) {
        if (c1 == null || c2 == null) {
            return c1 == c2;
        }
        return sameParameter(c1.startTime(), c2.startTime()) && sameParameter(c1.endTime(), c2.endTime());
    }

    /**
     * Checks whether two optional parameters refer to the same individual.
     * @param p1 First parameter.
     * @param p2 Second parameter.
     * @return true if both are empty or refer to the same individual.
     */
    private static boolean sameParameter(Optional<ParameterWrapper> p1, Optional<ParameterWrapper> p2) {
        if (p1.isEmpty() || p2.isEmpty()) {
            return p1.isEmpty() && p2.isEmpty();
        }
        return p1.get().getIndividualIRI().equals(p2.get().getIndividualIRI());
    }

    /**
     * Returns the start and end times (as text formulas) for a parameter, according to its temporal constraint.
     * @param constraint The temporal constraint (may be null).
     * @param cellUndiscountedLY The cell reference for the undiscounted life years.
     * @return An array with the start time (null if the parameter applies from the beginning) and the end time.
     */
    private static String[] getStartAndEnd(TemporalConstraint constraint, CellReference cellUndiscountedLY) {
        // If no constraints are defined, we assume the parameter applies for the entire lifetime
        String start = null;
        String end = cellUndiscountedLY.formatAsString();
        // The start and end times are modified by the temporal constraints
        if (constraint != null) {
            if (constraint.startTime().isPresent()) {
                start = constraint.startTime().get().getShortName();
            } else {
                start = "0.0";
            }
            if (constraint.endTime().isPresent()) {
                end = constraint.endTime().get().getShortName();
            }
        }
        return new String[] {start, end};
    }

    /**
     * Returns the cost formula for the parameters in this wrapper. Processes each cost parameter by taking into account whether they are modified or not, 
     * whether they applied once or annually, and when they should start and/or end to account. With all this information, the appropriate discounting formula can be applied.
     * @param cellUndiscountedLY The cell reference for the undiscounted life years.
     * @param method The method to combine the annual costs of the nodes along the path.
     * @return The cost formula as a string.
     * @throws UnsupportedOSDiFeatureException If the cost parameters cannot be processed.
     */
    public String getCostFormula(CellReference cellUndiscountedLY, CostCombinationMethod method) throws UnsupportedOSDiFeatureException {
        final ArrayList<String> costTexts = new ArrayList<>();
        // If there are no cost parameters, return an empty string
        if (costEntries.isEmpty()) {
            return "";
        }

        for (ParameterEntry entry : selectEntries(costEntries, method, "cost")) {
            final ParameterWrapper param = entry.parameter();
            final String[] startEnd = getStartAndEnd(entry.constraint(), cellUndiscountedLY);
            // Define the base text formula for the parameter, including any modifications
            String text = getParameterTextFormula(param);
            text = applyDiscountToFormula(text, HTAExcelModelFactory.CommonNamedRanges.DISCOUNT_COSTS.getName(), ((SpecificInformationForCost) param.getSpecificInformation()).appliesOneTime(), startEnd[0], startEnd[1]);
            costTexts.add(text);
        }
        if (costTexts.size() == 1) {
            // If there is only one cost parameter, we can return it directly
            return costTexts.get(0);
        }
        return ExcelFormulaLibrary.SUM.getFormula(String.join(", ", costTexts));
    }

    /**
     * Returns the QALE formula for the parameters in this wrapper. Processes each disutility parameter by taking into account whether they are modified or not,
     * whether they applied once or annually, and when they should start and/or end to account. With all this information, the appropriate discounting formula can be applied.
     * @param cellUndiscountedLY The cell reference for the undiscounted life years.
     * @param baseUtilityParam The base utility parameter to which the disutilities will be applied.
     * @param method The method to combine disutilities (ADD, ADD_DISTINCT or MAX).
     * @return The QALE formula as a string.
     * @throws UnsupportedOSDiFeatureException If the disutility parameters cannot be processed or if the combination method is unsupported.
     */
    public String getQALEFormula(CellReference cellUndiscountedLY, ParameterWrapper baseUtilityParam, DisutilityCombinationMethod method) throws UnsupportedOSDiFeatureException {
        final ArrayList<String> disutilityTexts = new ArrayList<>();
        final String baseUtilityText = FormulaLibrary.CONT_DISCOUNT.getFormula(HTAExcelModelFactory.CommonNamedRanges.DISCOUNT_QALE.getName(), cellUndiscountedLY.formatAsString(), baseUtilityParam.getShortName());
        // If there are no disutility parameters, return the base utility
        if (disutilityEntries.isEmpty()) {
            return baseUtilityText;
        }

        for (ParameterEntry entry : selectEntries(disutilityEntries, method, "utility")) {
            final ParameterWrapper param = entry.parameter();
            final String[] startEnd = getStartAndEnd(entry.constraint(), cellUndiscountedLY);
            // Define the base text formula for the parameter, including any modifications
            String text = getParameterTextFormula(param);
            final SpecificInformationForUtility specificInfo = (SpecificInformationForUtility)param.getSpecificInformation(); 
            if (!specificInfo.isDisutility()) {
                // If the parameter is a utility, we need to invert the sign
                text = "1 - " + text;
            }
            text = applyDiscountToFormula(text, HTAExcelModelFactory.CommonNamedRanges.DISCOUNT_QALE.getName(), specificInfo.appliesOneTime(), startEnd[0], startEnd[1]);
            disutilityTexts.add(text);
        }
        final String combined;
        if (disutilityTexts.size() == 1) {
            combined = disutilityTexts.get(0);
        }
        else {
            switch (method) {
                case MAX:
                    combined = ExcelFormulaLibrary.MAX.getFormula(String.join(", ", disutilityTexts));
                    break;
                case ADD:
                case ADD_DISTINCT:
                    combined = ExcelFormulaLibrary.SUM.getFormula(String.join(", ", disutilityTexts));
                    break;
                default:
                    log.warn("The disutility combination method " + method + " is not supported in the Excel model builder. We will use the addition method instead.");
                    combined = ExcelFormulaLibrary.SUM.getFormula(String.join(", ", disutilityTexts));
                    break;
            }
        }
        return baseUtilityText + " - " + combined;
    }

    /**
     * Returns the undiscounted life expectancy formula for the parameters in this wrapper. The base life expectancy is first divided by the combined 
     * increased mortality rate (if any), and then the combined life expectancy reduction (if any) is subtracted. The result is never negative.
     * This is the same order used by the discrete-event simulation death submodels.
     * @param baseLifeExpectancyParam The base (population's) life expectancy parameter for which the undiscounted life expectancy formula is calculated.
     * @param lerMethod The method to combine the life expectancy reductions of the nodes along the path.
     * @param imrMethod The method to combine the increased mortality rates of the nodes along the path.
     * @return The undiscounted life expectancy formula as a string.
     * @throws UnsupportedOSDiFeatureException If the lifetime reduction parameters cannot be processed.
     */
    public String getUndiscountedLEFormula(ParameterWrapper baseLifeExpectancyParam, LifeExpectancyReductionCombinationMethod lerMethod, MortalityRateCombinationMethod imrMethod) throws UnsupportedOSDiFeatureException {
        final ArrayList<String> lifetimeReductionTexts = new ArrayList<>();
        final ArrayList<String> mortalityRateTexts = new ArrayList<>();
        final Set<String> lerKeys = new TreeSet<>();

        boolean hasInstantDeathEffect = false;
        for (EffectWrapper effect : effectEntries) {
            switch (effect.getEffectType()) {
                case INSTANT_DEATH_EFFECT:
                    hasInstantDeathEffect = true;
                    break;
                case LIFE_EXPECTANCY_REDUCTION_EFFECT: {
                    // The magnitude parameter identifies the reduction
                    final String key = effect.getEffectMagnitude().getIndividualIRI().toString();
                    if (!lerKeys.add(key) && lerMethod.countsSharedItemsOnce()) {
                        log.debug("The life expectancy reduction " + effect.getEffectMagnitude().getShortName() + " is shared by several items in the same path. It is considered only once (" + lerMethod + ").");
                    }
                    else {
                        lifetimeReductionTexts.add(getParameterTextFormula(effect.getEffectMagnitude()));
                    }
                    break;
                }
                case MORTALITY_RATE_EFFECT:
                    mortalityRateTexts.add(getParameterTextFormula(effect.getEffectMagnitude()));
                    break;
                default:
                    break;
            }
        }
        if (hasInstantDeathEffect) {
            // TODO: Consider that death may occur later
            return "0"; // If there is an instant death effect, the life expectancy is 0
        }
        String txtFormula = baseLifeExpectancyParam.getShortName();
        // Increased mortality rates are applied first, by dividing the life expectancy by the combined rate ratio
        if (mortalityRateTexts.size() == 1) {
            txtFormula = txtFormula + " / " + mortalityRateTexts.get(0);
        }
        else if (mortalityRateTexts.size() > 1) {
            switch (imrMethod) {
                case MULT:
                    txtFormula = txtFormula + " / " + ExcelFormulaLibrary.PRODUCT.getFormula(String.join(", ", mortalityRateTexts));
                    break;
                case MAX:
                default:
                    txtFormula = txtFormula + " / " + ExcelFormulaLibrary.MAX.getFormula(String.join(", ", mortalityRateTexts));
                    break;
            }
        }
        // Then, the combined life expectancy reduction is subtracted
        if (lifetimeReductionTexts.isEmpty()) {
            return txtFormula;
        }
        if (lifetimeReductionTexts.size() == 1) {
            txtFormula += " - " + lifetimeReductionTexts.get(0);
        }
        else {
            switch (lerMethod) {
                case MAX:
                    txtFormula += " - " + ExcelFormulaLibrary.MAX.getFormula(String.join(", ", lifetimeReductionTexts));
                    break;
                case ADD:
                case ADD_DISTINCT:
                default:
                    txtFormula += " - " + ExcelFormulaLibrary.SUM.getFormula(String.join(", ", lifetimeReductionTexts));
                    break;
            }
        }
        // The life expectancy can never be negative
        return ExcelFormulaLibrary.MAX.getFormula("0, " + txtFormula);
    }

    /**
     * Returns the probability formula for the probabilities in this wrapper. If there are no probabilities, it returns an empty string.
     * @return The probability formula as a string.
     */
    public String getProbabilityFormula() {
        if (probabilities.isEmpty()) {
            return "";
        }
        if (probabilities.size() == 1) {
            return probabilities.get(0).formatAsString();
        }
        return ExcelFormulaLibrary.PRODUCT.getFormula(String.join(", ", probabilities.stream().map(celRef -> celRef.formatAsString()).toList()));
    }

    /**
     * Applies the discount formula to the given text formula based on whether it applies once or continuously, and the start and end times.
     * @param text The text formula to which the discount will be applied.
     * @param discountName The name of the discount parameter to use.
     * @param appliesOneTime Whether the discount applies once at a specific time in the future, or it is annual.
     * @param start The start time for the discount.
     * @param end The end time for the discount.
     * @return The text formula with the discount applied.
     * @throws UnsupportedOSDiFeatureException If the discount formula is not supported.
     */
    private String applyDiscountToFormula(String text, String discountName, boolean appliesOneTime, String start, String end) throws UnsupportedOSDiFeatureException {
        if (start != null) {
            if (appliesOneTime) {
                // Parameters that apply once at a specific time in the future
                return FormulaLibrary.PUNCTUAL_DISCOUNT.getFormula(text, discountName, start);
            }
            // Parameters that apply annually, starting at a specific time in the future
            return FormulaLibrary.FUTURE_CONT_DISCOUNT.getFormula(discountName, end + " - " + start, text, start);
        } else if (!appliesOneTime) {
            // Parameters that apply annually, starting from the beginning of the model
            return FormulaLibrary.CONT_DISCOUNT.getFormula(discountName, end, text);
        }
        return text;
    }

    /**
     * Returns the text formula for a parameter in the decision tree. It takes into account the modifications made to the parameter in the decision tree.
     * @param param The IRI of the parameter
     * @return The string formula for the parameter.
     * @throws UnsupportedOSDiFeatureException If the parameter has a modification whose type is unsupported.
     */
    private String getParameterTextFormula(ParameterWrapper param) throws UnsupportedOSDiFeatureException {
        EffectWrapper modifParam = null;
        for (EffectWrapper effect : effects) {
            for (IModelItemWrapper modifiedParamIRI : effect.getModifiedItems()) {
                if (modifiedParamIRI.compareTo(param) == 0) {
                    modifParam = effect;
                    break;
                }
            }
        }
        // If the successor is a parameterized component, we need to get the modified probability
        if (modifParam == null) {
            return param.getShortName(); // No modification, return the original IRI
        }
        switch (modifParam.getMagnitudeType()) {
            case DIFF:
                return param.getShortName() + " - " + modifParam.getEffectMagnitude().getShortName();
            case FACTOR:
                return param.getShortName() + " * " + modifParam.getEffectMagnitude().getShortName();
            case SET:
                return modifParam.getEffectMagnitude().getShortName();
            default:
                throw new UnsupportedOSDiFeatureException("Unsupported parameter modification type: " + modifParam.getMagnitudeType());
        }
    }
}
