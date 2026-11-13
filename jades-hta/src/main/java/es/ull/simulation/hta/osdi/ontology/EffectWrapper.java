package es.ull.simulation.hta.osdi.ontology;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.semanticweb.owlapi.model.IRI;

import es.ull.simulation.hta.osdi.OSDiLogger;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.params.modifiers.ModifierOperation;

/**
 * Wrapper for the effects defined in the ontology. An effect applies its magnitude (see {@link #getEffectMagnitude()}) to each of the
 * items it affects. The operation depends on the class of the effect and, for a generic effect, on the data item types of the magnitude
 * and of the affected item (see {@link #resolveOperation(OSDiDataItemType, OSDiDataItemType, String, String)}):
 * <ul>
 * <li>An instant death effect replaces, a life expectancy reduction effect subtracts and an increased mortality rate effect multiplies.</li>
 * <li>A generic effect follows decision D3 of the evolution towards OSDi 1.3 (docs/osdi-1.3-diseno.md).</li>
 * </ul>
 * When several effects modify the same parameter, they are applied in the order: replace, multiply, subtract (see
 * {@link #sortForParameter(IModelItemWrapper, Collection)}).
 */
public class EffectWrapper extends BaseModelItemWrapper {
	/**
	 * A logger for the EffectWrapper class
	 */
	private final static OSDiLogger log = OSDiLogger.getLogger(EffectWrapper.class);

	/**
	 * The set of items modified by this parameter modifier.
	 */
	private final Set<IModelItemWrapper> modifiedItems = new TreeSet<>();
	/**
	 * The operation applied to each modified item.
	 */
	private final TreeMap<IModelItemWrapper, ModifierOperation> operations = new TreeMap<>();
	/**
	 * The name of the intervention or condition that produced this effect.
	 */
	private final Set<IModelItemWrapper> causes = new TreeSet<>();
	/**
	 * The parameter wrapper representing the effect magnitude.
	 */
	private ParameterWrapper effectMagnitude = null;
	/**
	 * The effect type of this effect.
	 */
	private EffectType effectType = null;
	/**
	 * The cost parameter associated with this effect, if any.
	 */
    private Optional<ParameterWrapper> cost = null;

	/**
	 *
	 * @param wrap
	 * @param effectIRI
	 * @param causeIRI
	 * @throws MalformedOSDiModelException
	 */
	protected EffectWrapper(ModelWrapper modelWrapper, IRI individualIRI) {
		super(modelWrapper, individualIRI);
	}

	@Override
	public void doInitialize() throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
		final IRI individualIRI = getIndividualIRI();
		final ModelWrapper modelWrapper = getModelWrapper();
		OSDiWrapper wrap = modelWrapper.getOSDiWrapper();
		this.causes.addAll(modelWrapper.getWrappersForProperty(individualIRI, OSDiObjectProperty.IS_EFFECT_OF));
		this.modifiedItems.addAll(modelWrapper.getWrappersForProperty(individualIRI, OSDiObjectProperty.AFFECTS));
		this.effectType = EffectType.fromIRI(individualIRI, wrap);
		final Optional<ParameterWrapper> optionalEffectMagnitude = modelWrapper.getAndInitializeWrapperForPropertyAs(individualIRI, OSDiObjectProperty.HAS_EFFECT_MAGNITUDE, ParameterWrapper.class);
		if (!optionalEffectMagnitude.isPresent()) {
			throw new MalformedOSDiModelException(OSDiClass.EFFECT, individualIRI, OSDiObjectProperty.HAS_EFFECT_MAGNITUDE, "Effect magnitude is required for effect definitions");
		}
		this.effectMagnitude = optionalEffectMagnitude.get();
		for (IModelItemWrapper modifiedItem : modifiedItems) {
			operations.put(modifiedItem, computeOperation(wrap, modifiedItem));
		}
        this.cost = modelWrapper.getWrapperForPropertyAs(individualIRI, OSDiObjectProperty.HAS_COST, ParameterWrapper.class);
	}

	/**
	 * Computes the operation that this effect applies to a modified item, and checks that it is valid.
	 * @param wrap The OSDi wrapper
	 * @param modifiedItem The item modified by this effect
	 * @return The operation that this effect applies to the item
	 * @throws MalformedOSDiModelException If the data item types of the magnitude and the item cannot be combined
	 * @throws UnsupportedOSDiFeatureException If the combination is meaningful but not supported yet
	 */
	private ModifierOperation computeOperation(OSDiWrapper wrap, IModelItemWrapper modifiedItem) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
		switch (effectType) {
			case INSTANT_DEATH_EFFECT:
				return ModifierOperation.REPLACE;
			case LIFE_EXPECTANCY_REDUCTION_EFFECT:
				return ModifierOperation.SUBTRACT;
			case MORTALITY_RATE_EFFECT:
				return ModifierOperation.MULTIPLY;
			case GENERAL_EFFECT:
			default:
				break;
		}
		final IRI itemIRI = modifiedItem.getIndividualIRI();
		// The data item type is read from the ontology, since the affected item may not be initialized yet
		final OSDiDataItemType targetType = OSDiDataItemType.fromIndividualIRI(wrap.getValue(itemIRI, OSDiObjectProperty.HAS_DATA_ITEM_TYPE).orElse(null));
		final ModifierOperation operation = resolveOperation(effectMagnitude.getDataItemType(), targetType, getShortName(), modifiedItem.getShortName());
		if (ModifierOperation.TIME_HAZARD_RATIO.equals(operation)) {
			if (wrap.isInstanceOf(itemIRI, OSDiClass.FIRST_ORDER_UNCERTAINTY_PARAMETER)) {
				throw new UnsupportedOSDiFeatureException("The effect " + getShortName() + " applies a hazard ratio to the time to event " + modifiedItem.getShortName()
					+ ", which is characterized by a distribution. Hazard ratios are only supported on times to event without a distribution (assumed exponential).");
			}
			log.warn(getIndividualIRI(), OSDiObjectProperty.AFFECTS, "The hazard ratio of the effect " + getShortName() + " is applied to the time to event "
				+ modifiedItem.getShortName() + " assuming an exponential distribution (the mean time is divided by the hazard ratio).");
		}
		else if (ModifierOperation.MULTIPLY.equals(operation) || ModifierOperation.SUBTRACT.equals(operation)) {
			if (targetType.isProbability()) {
				warnIfOutOfRange(wrap, itemIRI, operation, modifiedItem.getShortName());
			}
		}
		return operation;
	}

	/**
	 * Warns if a linear operation on a probability produces a value out of [0, 1]. It can only be checked when the probability and the
	 * magnitude are deterministic.
	 */
	private void warnIfOutOfRange(OSDiWrapper wrap, IRI itemIRI, ModifierOperation operation, String itemName) {
		final IRI magnitudeIRI = effectMagnitude.getIndividualIRI();
		if (wrap.isInstanceOf(itemIRI, OSDiClass.DETERMINISTIC_PARAMETER) && wrap.isInstanceOf(magnitudeIRI, OSDiClass.DETERMINISTIC_PARAMETER)) {
			final OptionalDouble value = wrap.getDoubleValue(itemIRI, OSDiDataProperty.HAS_EXPECTED_VALUE);
			final OptionalDouble magnitude = wrap.getDoubleValue(magnitudeIRI, OSDiDataProperty.HAS_EXPECTED_VALUE);
			if (value.isPresent() && magnitude.isPresent()) {
				final double result = operation.apply(value.getAsDouble(), magnitude.getAsDouble());
				if (result < 0.0 || result > 1.0) {
					log.warn(getIndividualIRI(), OSDiObjectProperty.HAS_EFFECT_MAGNITUDE, "The effect " + getShortName() + " turns the probability " + itemName
						+ " into " + result + ", which is out of [0, 1]. The value is not truncated.");
				}
			}
		}
	}

	/**
	 * Returns the operation that the magnitude of a generic effect applies to an affected item, according to their data item types
	 * (decision D3 of the evolution towards OSDi 1.3):
	 * <ul>
	 * <li>Statistics of the uncertainty (standard deviation, confidence limits) cannot be magnitudes: the effect is rejected.</li>
	 * <li>A factor or a ratio multiplies.</li>
	 * <li>A relative risk or a hazard ratio on a probability (probability, proportion, incidence, prevalence...) is applied to the equivalent
	 * rate: 1 - (1 - p)^m. A relative risk on a time to event is not supported (a hazard ratio must be used); on anything else, it multiplies,
	 * with a warning.</li>
	 * <li>A hazard ratio on a time to event divides the mean time (exponential time to event). On anything else, the effect is rejected.</li>
	 * <li>An odds ratio on a probability multiplies the odds: p * m / (1 - p + p * m). On anything else, the effect is rejected.</li>
	 * <li>A mean difference subtracts.</li>
	 * <li>Any other type is an absolute value that replaces the affected item. A warning is issued if it does not match the type of the
	 * item (unless it is a continuous variable), or if it is undefined.</li>
	 * </ul>
	 * A relative magnitude always modifies the affected item, even if both have the same type (e.g., a relative risk that modifies an
	 * increased mortality rate expressed as a relative risk).
	 * @param magnitudeType The data item type of the magnitude of the effect
	 * @param targetType The data item type of the affected item ({@link OSDiDataItemType#DI_UNDEFINED} if it does not define one, e.g., an attribute)
	 * @param effectName The name of the effect, for the messages
	 * @param targetName The name of the affected item, for the messages
	 * @return The operation that the magnitude applies to the affected item
	 * @throws MalformedOSDiModelException If the types cannot be combined
	 * @throws UnsupportedOSDiFeatureException If the combination is meaningful but not supported yet
	 */
	public static ModifierOperation resolveOperation(OSDiDataItemType magnitudeType, OSDiDataItemType targetType, String effectName, String targetName)
			throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
		final String context = "The effect " + effectName + " (magnitude of type " + magnitudeType.getShortName() + ") on " + targetName + " (of type " + targetType.getShortName() + ")";
		if (magnitudeType.isUncertaintyStatistic()) {
			throw new MalformedOSDiModelException(context + ": a statistic of the uncertainty cannot be the magnitude of an effect.");
		}
		switch (magnitudeType) {
			case DI_FACTOR:
			case DI_RATIO:
				return ModifierOperation.MULTIPLY;
			case DI_RELATIVE_RISK:
				if (targetType.isProbability()) {
					return ModifierOperation.RATE_RATIO;
				}
				if (OSDiDataItemType.DI_TIME_TO_EVENT.equals(targetType)) {
					throw new UnsupportedOSDiFeatureException(context + ": a relative risk cannot modify a time to event. Use a hazard ratio instead.");
				}
				log.warn(context + ": the relative risk does not modify a probability, so it is applied as a factor. Consider using "
					+ OSDiDataItemType.DI_FACTOR.getShortName() + " instead.");
				return ModifierOperation.MULTIPLY;
			case DI_HAZARD_RATIO:
				if (targetType.isProbability()) {
					return ModifierOperation.RATE_RATIO;
				}
				if (OSDiDataItemType.DI_TIME_TO_EVENT.equals(targetType)) {
					return ModifierOperation.TIME_HAZARD_RATIO;
				}
				throw new MalformedOSDiModelException(context + ": a hazard ratio can only modify a probability or a time to event.");
			case DI_ODDS_RATIO:
				if (targetType.isProbability()) {
					return ModifierOperation.ODDS_RATIO;
				}
				throw new MalformedOSDiModelException(context + ": an odds ratio can only modify a probability.");
			case DI_MEAN_DIFFERENCE:
				return ModifierOperation.SUBTRACT;
			case DI_CONTINUOUS_VARIABLE:
				return ModifierOperation.REPLACE;
			case DI_UNDEFINED:
			case DI_OTHER:
				log.warn(context + ": the type of the magnitude does not define how to apply it. The magnitude replaces the value.");
				return ModifierOperation.REPLACE;
			default:
				if (!magnitudeType.equals(targetType)) {
					log.warn(context + ": the magnitude is an absolute value of a different type. The magnitude replaces the value.");
				}
				return ModifierOperation.REPLACE;
		}
	}

	/**
	 * Returns the effects of a collection that modify a parameter (or attribute), in the order in which they must be applied: replacement,
	 * multiplications (linear factors, rate ratios, odds ratios) and differences. Effects with operations of the same order are sorted by name.
	 * @param item The modified parameter or attribute
	 * @param effects A collection of effects
	 * @return The effects that modify the item, in the order in which they must be applied (empty if none modifies it)
	 * @throws MalformedOSDiModelException If more than one effect replaces the item
	 */
	public static List<EffectWrapper> sortForParameter(IModelItemWrapper item, Collection<EffectWrapper> effects) throws MalformedOSDiModelException {
		final List<EffectWrapper> selected = new ArrayList<>();
		for (EffectWrapper effect : effects) {
			if (effect.getOperation(item) != null) {
				selected.add(effect);
			}
		}
		selected.sort(Comparator.<EffectWrapper>comparingInt(effect -> effect.getOperation(item).getOrder()).thenComparing(effect -> effect.getShortName()));
		final long replacements = selected.stream().filter(effect -> ModifierOperation.REPLACE.equals(effect.getOperation(item))).count();
		if (replacements > 1) {
			throw new MalformedOSDiModelException("The effects " + selected + " modify " + item.getShortName() + ", and " + replacements
				+ " of them replace its value. At most one effect may replace a parameter.");
		}
		final Set<ModifierOperation> multiplications = new TreeSet<>();
		for (EffectWrapper effect : selected) {
			final ModifierOperation operation = effect.getOperation(item);
			if (operation.getOrder() > ModifierOperation.REPLACE.getOrder() && operation.getOrder() < ModifierOperation.SUBTRACT.getOrder()) {
				multiplications.add(operation);
			}
		}
		if (multiplications.size() > 1) {
			log.warn("The effects " + selected + " modify " + item.getShortName() + " on different scales (" + multiplications
				+ "). They are applied in the order: linear factors, rate ratios, odds ratios.");
		}
		return selected;
	}

    @Override
    public void doPersist() throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        // TODO
    }

	/**
	 * Returns the parameter wrapper representing the effect magnitude.
	 * @return The parameter wrapper representing the effect magnitude.
	 */
    public ParameterWrapper getEffectMagnitude() {
		return effectMagnitude;
	}

	/**
	 * Returns the operation that this effect applies to one of the items it modifies.
	 * @param modifiedItem An item modified by this effect
	 * @return The operation that this effect applies to the item, or null if the effect does not modify the item
	 */
	public ModifierOperation getOperation(IModelItemWrapper modifiedItem) {
		return operations.get(modifiedItem);
	}

	/**
	 * Returns the set of items modified by this parameter modifier.
	 * @return The set of items modified by this parameter modifier.
	 */
	public Set<IModelItemWrapper> getModifiedItems() {
		return modifiedItems;
	}

	/**
	 * Returns the wrappers of the items that cause this effect.
	 * @return The wrappers of the items that cause this effect.
	 */
	public Set<IModelItemWrapper> getCauses() {
		return causes;
	}

	/**
	 * Returns the effect type of this effect.
	 * @return The effect type of this effect.
	 */
	public EffectType getEffectType() {
		return effectType;
	}

	/**
	 * Returns the cost parameter associated with this effect, if any.
	 * @return The cost parameter associated with this effect, if any.
	 */
    public Optional<ParameterWrapper> getCost() {
        return cost;
    }
}
