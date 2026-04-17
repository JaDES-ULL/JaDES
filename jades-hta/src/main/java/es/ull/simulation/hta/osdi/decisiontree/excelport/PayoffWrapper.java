package es.ull.simulation.hta.osdi.decisiontree.excelport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeMap;
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
import es.ull.simulation.hta.outcomes.DisutilityCombinationMethod;
import es.ull.simulation.utils.ExcelFormulaLibrary;

/**
 * Wrapper class for HTA payoffs in Excel.
 * This class is responsible for managing the various parameters and their associated Excel representations
 * for the payoffs in a Health Technology Assessment (HTA) context.
 */
public class PayoffWrapper {
    /**
     * Logger for the PayoffWrapper class.
     */
    private final static OSDiLogger log = OSDiLogger.getLogger(PayoffWrapper.class);
    /**
     * The list of probabilities associated with this node, which is used to calculate the expected value of the decision represented by this node.
     */
    private final ArrayList<CellReference> probabilities;
    /**
     * The list of cost parameters associated with this node, which is used to calculate the cost of the decision represented by this node.
     */
    private final ArrayList<ParameterWrapper> costParameters;
    /**
     * The list of disutility parameters associated with this node, which is used to calculate the disutility of the decision represented by this node.
     */
    private final Set<ParameterWrapper> disutilityParameters;
    /**
     * The list of effects associated with this node.
     */
    private final Set<EffectWrapper> effects;
    /**
     * Temporal constraints for the parameters in this wrapper.
     */
    private final TreeMap<ParameterWrapper, TemporalConstraint> temporalConstraints;
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
        this.costParameters = new ArrayList<>();
        this.disutilityParameters = new TreeSet<>();
        this.effects = new TreeSet<>();
        this.temporalConstraints = new TreeMap<>();
    }

    /**
     * Copy constructor for the HTAExcelPayoffWrapper class.
     * @param original The original HTAExcelPayoffWrapper to copy.
     */
    public PayoffWrapper(PayoffWrapper original) {
        this.wrap = original.wrap;
        this.probabilities = new ArrayList<>(original.probabilities);
        this.costParameters = new ArrayList<>(original.costParameters);
        this.disutilityParameters = new TreeSet<>(original.disutilityParameters);
        this.effects = new TreeSet<>(original.effects);
        this.temporalConstraints = new TreeMap<>(original.temporalConstraints);
    }

    /**
     * Gets the list of probabilities associated with this node, used to compute the probability of the decision represented by this node.
     * @return The list of probabilities.
     */
    public ArrayList<CellReference> getProbabilities() {
        return probabilities;
    }

    /**
     * Gets the list of cost parameters associated with this node, used to compute the cost of the decision represented by this node.
     * @return The list of cost parameters.
     */
    public ArrayList<ParameterWrapper> getCostParameters() {
        return costParameters;
    }

    /**
     * Gets the list of disutility parameters associated with this node, which is used to calculate the disutility of the decision represented by this node.
     * @return The list of disutility parameters.
     */
    public Set<ParameterWrapper> getDisutilityParameters() {
        return disutilityParameters;
    }

    /**
     * Gets the list of effects associated with this node, which is used to calculate the lifetime reduction and other modifications of the decision represented by this node.
     * @return The list of effects.
     */
    public Set<EffectWrapper> getEffects() {
        return effects;
    }

    /**
     * Gets the temporal constraints associated with this node.
     * @return The temporal constraints.
     */
    public TreeMap<ParameterWrapper, TemporalConstraint> getTemporalConstraints() {
        return temporalConstraints;
    }

    /**
     * Adds a probability cell reference to this wrapper.
     * @param probability The probability cell reference to add.
     */
    public void addProbability(CellReference probability) {
        this.probabilities.add(probability);
    }

    /**
     * Adds a cost parameter to this wrapper.
     * @param parameter The cost parameter to add.
     */
    public void addCostParameter(ParameterWrapper parameter) {
        this.costParameters.add(parameter);
    }

    /**
     * Adds a disutility parameter to this wrapper.
     * @param parameter The disutility parameter to add.
     */
    public void addDisutilityParameter(ParameterWrapper parameter) {
        this.disutilityParameters.add(parameter);
    }

    /**
     * Adds an effect to this wrapper.
     * @param effect The effect to add.
     */
    public void addEffect(EffectWrapper effect) {
        this.effects.add(effect);
    }

    /**
     * Adds a temporal constraint to this wrapper.
     * @param parameterIRI The IRI of the parameter to which the constraint applies.
     * @param constraint The temporal constraint to add.
     */
    public void addTemporalConstraint(ParameterWrapper parameter, TemporalConstraint constraint) {
        this.temporalConstraints.put(parameter, constraint);
    }

    /**
     * Adds a temporal constraint to this wrapper.
     * @param parameters The list of parameters to which the constraint applies.
     * @param constraint The temporal constraint to add.
     */
    public void addTemporalConstraint(Collection<ParameterWrapper> parameters, TemporalConstraint constraint) {
        for (ParameterWrapper param : parameters) {
            this.temporalConstraints.put(param, constraint);
        }
    }

    /**
     * Adds a list of probabilities to this wrapper.
     * @param probabilities The list of probability cell references to add.
     */
    public void addProbabilities(Collection<CellReference> probabilities) {
        this.probabilities.addAll(probabilities);
    }

    /**
     * Adds a list of cost parameters to this wrapper.
     * @param parameters The list of cost parameters to add.
     */
    public void addCostParameters(Collection<ParameterWrapper> parameters) {
        this.costParameters.addAll(parameters);
    }

    /**
     * Adds a list of disutility parameters to this wrapper.
     * @param parameters The list of disutility parameters to add.
     */
    public void addDisutilityParameters(Collection<ParameterWrapper> parameters) {
        this.disutilityParameters.addAll(parameters);
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
     * Adds a list of temporal constraints to this wrapper.
     * @param constraints The map of parameter IRIs to their temporal constraints.
     */
    public void addTemporalConstraints(TreeMap<ParameterWrapper, TemporalConstraint> constraints) {
        this.temporalConstraints.putAll(constraints);
    }

    /**
     * Updates this wrapper with the parameters and constraints from a decision tree node.
     * @param node The decision tree node to update from.
     */
    public void updateWithTreeNode(BranchDestinationNode node) {
        // Add cost parameters from the node
        this.addCostParameters(node.getCostParameters());
        // Add disutility parameters from the node
        this.addDisutilityParameters(node.getDisutilityParameters());
        // Add lifetime reduction parameters from the node
        this.addEffects(node.getEffects());
        // Add temporal constraints from the node
        this.addTemporalConstraint(node.getCostParameters(), node.getTemporalConstraint());
        this.addTemporalConstraint(node.getDisutilityParameters(), node.getTemporalConstraint());
    }

    /**
     * Returns the cost formula for the parameters in this wrapper. Processes each cost parameter by taking into account whether they are modified or not, 
     * whether they applied once or annually, and when they should start and/or end to account. With all this information, the appropriate discounting formula can be applied.
     * @param cellUndiscountedLY The cell reference for the undiscounted life years.
     * @return The cost formula as a string.
     * @throws UnsupportedOSDiFeatureException If the cost parameters cannot be processed.
     */
    public String getCostFormula(CellReference cellUndiscountedLY) throws UnsupportedOSDiFeatureException {
        final ArrayList<String> costTexts = new ArrayList<>();
        // If there are no cost parameters, return an empty string
        if (costParameters.isEmpty()) {
            return "";
        }

        for (ParameterWrapper param : costParameters) {
            TemporalConstraint constraint = temporalConstraints.get(param);
            // If no constraints are defined, we assume the cost applies for the entire lifetime
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
            // Define the base text formula for the parameter, including any modifications
            String text = getParameterTextFormula(param);
            text = applyDiscountToFormula(text, HTAExcelModelFactory.CommonNamedRanges.DISCOUNT_COSTS.getName(), ((SpecificInformationForCost) param.getSpecificInformation()).appliesOneTime(), start, end);
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
     * @param method The method to combine disutilities (ADD or MAX).
     * @return The QALE formula as a string.
     * @throws UnsupportedOSDiFeatureException If the disutility parameters cannot be processed or if the combination method is unsupported.
     */
    public String getQALEFormula(CellReference cellUndiscountedLY, ParameterWrapper baseUtilityParam, DisutilityCombinationMethod method) throws UnsupportedOSDiFeatureException {
        final ArrayList<String> disutilityTexts = new ArrayList<>();
        final String baseUtilityText = FormulaLibrary.CONT_DISCOUNT.getFormula(HTAExcelModelFactory.CommonNamedRanges.DISCOUNT_QALE.getName(), cellUndiscountedLY.formatAsString(), baseUtilityParam.getShortName());
        // If there are no disutility parameters, return an empty string
        if (disutilityParameters.isEmpty()) {
            return baseUtilityText; // No disutilities, return the base utility
        }

        for (ParameterWrapper param : disutilityParameters) {
            TemporalConstraint constraint = temporalConstraints.get(param);
            // If no constraints are defined, we assume the disutility applies for the entire lifetime
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
            // Define the base text formula for the parameter, including any modifications
            String text = getParameterTextFormula(param);
            final SpecificInformationForUtility specificInfo = (SpecificInformationForUtility)param.getSpecificInformation(); 
            if (!specificInfo.isDisutility()) {
                // If the parameter is a utility, we need to invert the sign
                text = "1 - " + text;
            }
            text = applyDiscountToFormula(text, HTAExcelModelFactory.CommonNamedRanges.DISCOUNT_QALE.getName(), specificInfo.appliesOneTime(), start, end);
            disutilityTexts.add(text);
        }
        switch (method) {
            case ADD:
                return baseUtilityText
                    + " - " + (disutilityTexts.size() == 1 ? disutilityTexts.get(0) : ExcelFormulaLibrary.SUM.getFormula(String.join(", ", disutilityTexts)));
            case MAX:
                return baseUtilityText
                    + " - " + (disutilityTexts.size() == 1 ? disutilityTexts.get(0) : ExcelFormulaLibrary.MAX.getFormula(String.join(", ", disutilityTexts)));
            default:
                log.warn("The disutility combination method " + method + " is not supported in the Excel model builder. We will use the addition method instead.");
                return baseUtilityText
                    + " - " + (disutilityTexts.size() == 1 ? disutilityTexts.get(0) : ExcelFormulaLibrary.SUM.getFormula(String.join(", ", disutilityTexts)));
        }
    }

    /**
     * Returns the undiscounted life expectancy formula for the parameters in this wrapper. It processes each lifetime reduction parameter by taking into account whether they are modified or not.     * 
     * @param baseLifeExpectancyParam The base (population's) life expectancy parameter for which the undiscounted life expectancy formula is calculated.
     * @return The undiscounted life expectancy formula as a string.
     * @throws UnsupportedOSDiFeatureException If the lifetime reduction parameters cannot be processed.
     */
    public String getUndiscountedLEFormula(ParameterWrapper baseLifeExpectancyParam) throws UnsupportedOSDiFeatureException {
        final ArrayList<String> lifetimeReductionTexts = new ArrayList<>();
        final ArrayList<String> mortalityRateTexts = new ArrayList<>();

        boolean hasInstantDeathEffect = false;
        for (EffectWrapper effect : effects) {
            switch (effect.getEffectType()) {
                case INSTANT_DEATH_EFFECT:
                    hasInstantDeathEffect = true;
                    break;
                case LIFE_EXPECTANCY_REDUCTION_EFFECT:
                    lifetimeReductionTexts.add(getParameterTextFormula(effect.getEffectMagnitude()));
                    break;
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
        if (lifetimeReductionTexts.size() == 1) {
            // If there is only one lifetime reduction parameter, we can substract it directly
            txtFormula += " - " + lifetimeReductionTexts.get(0);
        }
        else if (lifetimeReductionTexts.size() > 1) {
            txtFormula += " - " + ExcelFormulaLibrary.SUM.getFormula(String.join(", ", lifetimeReductionTexts));
        }
        if (mortalityRateTexts.size() == 1) {
            // If there is only one mortality rate parameter, we can substract it directly
            txtFormula = "(" + txtFormula + ") * " + mortalityRateTexts.get(0);
        }
        else if (mortalityRateTexts.size() > 1) {
            txtFormula = "(" + txtFormula + ") * " + ExcelFormulaLibrary.MIN.getFormula(String.join(", ", mortalityRateTexts));
        }
        return txtFormula;
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
        return ExcelFormulaLibrary.PRODUCT.getFormula(String.join(", ", probabilities.stream().map(CellReference::formatAsString).toList()));
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
