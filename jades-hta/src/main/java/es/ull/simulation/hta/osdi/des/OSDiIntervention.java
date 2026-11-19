package es.ull.simulation.hta.osdi.des;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import es.ull.simulation.hta.interventions.Intervention;
import es.ull.simulation.hta.osdi.OSDiLogger;
import es.ull.simulation.hta.osdi.des.factories.CostParametersHandler;
import es.ull.simulation.hta.osdi.des.factories.EffectFactory;
import es.ull.simulation.hta.osdi.des.factories.ParameterFactory;
import es.ull.simulation.hta.osdi.des.factories.UtilityParametersHandler;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.EffectType;
import es.ull.simulation.hta.osdi.ontology.EffectWrapper;
import es.ull.simulation.hta.osdi.ontology.IModelItemWrapper;
import es.ull.simulation.hta.osdi.ontology.InterventionWrapper;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper.SpecificInformationForUtility;
import es.ull.simulation.hta.params.ParameterTemplate;
import es.ull.simulation.hta.params.ParameterGroup;
import es.ull.simulation.hta.params.StandardParameter;
import es.ull.simulation.hta.progression.DiseaseProgression;

/**
 * An intervention in the discrete event simulation. The intervention is applied at time 0 and is active during the whole simulation (decision D1 of
 * docs/osdi-1.3-diseno.md): its effects modify the parameters of the patients of its arm from the start. Its annual payoffs apply from time 0, and its
 * one-time payoffs once, at time 0. If the interventions target an acute manifestation, the one-time payoffs of the intervention apply with each
 * episode of the target instead (including the episode at time 0), since each episode is treated, and its annual payoffs are ignored with a warning,
 * as in decision trees. Only the first one-time and annual cost and utility are used.
 */
public class OSDiIntervention extends Intervention {
	private final static OSDiLogger log = OSDiLogger.getLogger(OSDiIntervention.class);

	private final Set<EffectWrapper> effects;
	private final Map<ParameterTemplate, ParameterWrapper> paramMapping;
	private final InterventionWrapper interventionWrapper;
	private final CostParametersHandler costParametersHandler;
	private final UtilityParametersHandler utilityHandler;

	public OSDiIntervention(OSDiDESModel model, InterventionWrapper interventionWrapper) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
		super(model, interventionWrapper.getShortName(), interventionWrapper.getDescription().orElse(interventionWrapper.getShortName()));
		this.interventionWrapper = interventionWrapper;
		// The DES does not simulate strategies, so strategy replacement effects (decision D2) are ignored
		this.effects = new TreeSet<>();
		for (EffectWrapper effect : interventionWrapper.getEffects()) {
			if (EffectType.STRATEGY_REPLACEMENT_EFFECT.equals(effect.getEffectType())) {
				log.warn("The strategy replacement effect " + effect.getShortName() + " of the intervention " + interventionWrapper.getShortName() + " is ignored by the DES, which does not simulate strategies.");
			}
			else {
				this.effects.add(effect);
			}
		}
		// Checks that the effects of the intervention can be combined (e.g., at most one of them replaces each parameter)
		for (EffectWrapper effect : effects) {
			for (IModelItemWrapper modifiedItem : effect.getModifiedItems()) {
				EffectWrapper.sortForParameter(modifiedItem, effects);
			}
		}
		this.paramMapping = new TreeMap<>();

		this.costParametersHandler = new CostParametersHandler(interventionWrapper.getCosts());
		this.utilityHandler = new UtilityParametersHandler(interventionWrapper.getUtilities());

		final Set<ParameterWrapper> oneTimeCostParams = costParametersHandler.getCostParametersByApplication(true);
		final Set<ParameterWrapper> annualCostParams = costParametersHandler.getCostParametersByApplication(false);
		final Set<ParameterWrapper> oneTimeUtilityParams = utilityHandler.getUtilityParametersByApplication(true);
		final Set<ParameterWrapper> annualUtilityParams = utilityHandler.getUtilityParametersByApplication(false);
		// An acute target is a sequence of episodes: the annual payoffs of the intervention are ignored (D1), as in decision trees
		final boolean ignoreAnnual = model.hasAcuteTarget();
		if (ignoreAnnual && (!annualCostParams.isEmpty() || !annualUtilityParams.isEmpty())) {
			final List<ParameterWrapper> annual = new ArrayList<>(annualCostParams);
			annual.addAll(annualUtilityParams);
			log.warn("The intervention " + interventionWrapper.getShortName() + ", whose target is an acute manifestation, defines annual costs or utilities (" + annual
				+ "), which are ignored: the intervention treats each episode, so only its one-time costs and utilities apply, with each episode.");
		}
		if (!oneTimeCostParams.isEmpty()) {
			if (oneTimeCostParams.size() > 1) {
				log.warn("More than one one-time cost parameters defined for intervention "+ name() +". The following parameters were found: " + oneTimeCostParams + ". Only the first will be used.");
			}
			paramMapping.put(StandardParameter.ONSET_COST, oneTimeCostParams.iterator().next());
		}
		if (!ignoreAnnual && !annualCostParams.isEmpty()) {
			if (annualCostParams.size() > 1) {
				log.warn("More than one annual cost parameters defined for intervention "+ name() +". The following parameters were found: " + annualCostParams + ". Only the first will be used.");
			}
			paramMapping.put(StandardParameter.ANNUAL_COST, annualCostParams.iterator().next());
		}
		if (!oneTimeUtilityParams.isEmpty()) {
			if (oneTimeUtilityParams.size() > 1) {
				log.warn("More than one one-time utility parameters defined for intervention "+ name() +". The following parameters were found: " + oneTimeUtilityParams + ". Only the first will be used.");
			}
			ParameterWrapper utilityParam = oneTimeUtilityParams.iterator().next();
			boolean isDisutility = ((SpecificInformationForUtility) utilityParam.getSpecificInformation()).isDisutility();
			paramMapping.put(isDisutility ? StandardParameter.ONSET_DISUTILITY : StandardParameter.ONSET_UTILITY, utilityParam);
		}
		if (!ignoreAnnual && !annualUtilityParams.isEmpty()) {
			if (annualUtilityParams.size() > 1) {
				log.warn("More than one annual utility parameters defined for intervention "+ name() +". The following parameters were found: " + annualUtilityParams + ". Only the first will be used.");
			}
			ParameterWrapper utilityParam = annualUtilityParams.iterator().next();
			boolean isDisutility = ((SpecificInformationForUtility) utilityParam.getSpecificInformation()).isDisutility();
			paramMapping.put(isDisutility ? StandardParameter.ANNUAL_DISUTILITY : StandardParameter.ANNUAL_UTILITY, utilityParam);
		}
	}

	public OSDiDESModel getModel() {
		return (OSDiDESModel) super.getModel();
	}
	
	public InterventionWrapper getInterventionWrapper() {
		return interventionWrapper;
	}

	/**
	 * The one-time payoffs apply once, at time 0, unless the interventions target an acute manifestation: then they apply with each episode
	 * (see {@link #appliesOneTimePayoffsOn(DiseaseProgression)})
	 */
	@Override
	public boolean appliesOneTimePayoffsAtStart() {
		return !getModel().hasAcuteTarget();
	}

	/**
	 * The one-time payoffs apply with each episode of the acute manifestation that the interventions target, including the episode at time 0 (D1)
	 */
	@Override
	public boolean appliesOneTimePayoffsOn(DiseaseProgression progression) {
		return getModel().hasAcuteTarget() && getModel().getTargetProgression().get().equals(progression);
	}
	
	@Override
	public void createParameters() {
		for (EffectWrapper mod : effects) {
			model.addParameter(EffectFactory.getParameterModifierInstance(getModel(), mod, ParameterGroup.MODIFIER));
		}
		for (ParameterTemplate paramDesc : paramMapping.keySet()) {
			final ParameterWrapper param = paramMapping.get(paramDesc);
			addUsedParameter(paramDesc, ParameterFactory.getParameterInstance(getModel(), param, paramDesc.getGroup()));
		}
	}		
}