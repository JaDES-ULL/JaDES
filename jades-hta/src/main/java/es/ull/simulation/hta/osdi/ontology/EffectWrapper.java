package es.ull.simulation.hta.osdi.ontology;

import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import org.semanticweb.owlapi.model.IRI;

import es.ull.simulation.hta.osdi.OSDiLogger;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;

public class EffectWrapper extends BaseModelItemWrapper {
	/**
	 * A logger for the EffectWrapper class
	 */
	private final static OSDiLogger log = OSDiLogger.getLogger(EffectWrapper.class);

	/**
	 * Enum representing the type of magnitud that this wrapper represents.
	*/
	public enum MagnitudeType {
		SET,
		DIFF,
		FACTOR
	}
	/**
	 * The set of items modified by this parameter modifier.
	 */
	private final Set<IModelItemWrapper> modifiedItems = new TreeSet<>();
	/**
	 * The type of magnitud of this effect.
	 */
	private MagnitudeType magnitudeType = null;
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
		switch (effectType) {
			case INSTANT_DEATH_EFFECT:
				magnitudeType = MagnitudeType.SET;
				break;
			case LIFE_EXPECTANCY_REDUCTION_EFFECT:
				magnitudeType = MagnitudeType.DIFF;
				break;
			case MORTALITY_RATE_EFFECT:
				magnitudeType = MagnitudeType.FACTOR;
				break;
			case GENERAL_EFFECT:
			default:
				final OSDiDataItemType dataItemType = effectMagnitude.getDataItemType();
				if (OSDiDataItemType.DI_CONTINUOUS_VARIABLE.equals(dataItemType))
					magnitudeType = MagnitudeType.SET;
				else if (OSDiDataItemType.DI_FACTOR.equals(dataItemType) || OSDiDataItemType.DI_RELATIVE_RISK.equals(dataItemType))
					magnitudeType = MagnitudeType.FACTOR;
				else if (OSDiDataItemType.DI_MEAN_DIFFERENCE.equals(dataItemType))
					magnitudeType = MagnitudeType.DIFF;
				else {
					log.warn(effectMagnitude.getIndividualIRI(), OSDiObjectProperty.HAS_DATA_ITEM_TYPE, "None of the data item types defined for the effect magnitude are currently supported. Assuming a 'set' effect.");
					magnitudeType = MagnitudeType.SET;
				}
				break;
		}
        this.cost = modelWrapper.getWrapperForPropertyAs(individualIRI, OSDiObjectProperty.HAS_COST, ParameterWrapper.class);
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
	 * Returns the type of modification this wrapper represents.
	 * @return The type of modification
	 */
	public MagnitudeType getMagnitudeType() {
		return magnitudeType;
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
