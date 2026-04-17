package es.ull.simulation.hta.osdi.des;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import es.ull.simulation.hta.interventions.Intervention;
import es.ull.simulation.hta.osdi.OSDiLogger;
import es.ull.simulation.hta.osdi.des.factories.CostParametersHandler;
import es.ull.simulation.hta.osdi.des.factories.EffectFactory;
import es.ull.simulation.hta.osdi.des.factories.ParameterFactory;
import es.ull.simulation.hta.osdi.des.factories.UtilityParametersHandler;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.EffectWrapper;
import es.ull.simulation.hta.osdi.ontology.InterventionWrapper;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper.SpecificInformationForUtility;
import es.ull.simulation.hta.params.ParameterTemplate;
import es.ull.simulation.hta.params.ParameterGroup;
import es.ull.simulation.hta.params.StandardParameter;

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
		this.effects = interventionWrapper.getEffects();
		this.paramMapping = new TreeMap<>();

		this.costParametersHandler = new CostParametersHandler(interventionWrapper.getCosts());
		this.utilityHandler = new UtilityParametersHandler(interventionWrapper.getUtilities());

		final Set<ParameterWrapper> oneTimeCostParams = costParametersHandler.getCostParametersByApplication(true);
		final Set<ParameterWrapper> annualCostParams = costParametersHandler.getCostParametersByApplication(false);
		if (!oneTimeCostParams.isEmpty()) {
			if (oneTimeCostParams.size() > 1) {
				log.warn("More than one one-time cost parameters defined for intervention "+ name() +". The following parameters were found: " + oneTimeCostParams + ". Only the first will be used.");
			}
			paramMapping.put(StandardParameter.ONSET_COST, oneTimeCostParams.iterator().next());
		}
		if (!annualCostParams.isEmpty()) {
			if (annualCostParams.size() > 1) {
				log.warn("More than one annual cost parameters defined for intervention "+ name() +". The following parameters were found: " + annualCostParams + ". Only the first will be used.");
			}
			paramMapping.put(StandardParameter.ANNUAL_COST, annualCostParams.iterator().next());
		}
		final Set<ParameterWrapper> oneTimeUtilityParams = utilityHandler.getUtilityParametersByApplication(true);
		final Set<ParameterWrapper> annualUtilityParams = utilityHandler.getUtilityParametersByApplication(false);
		if (!oneTimeUtilityParams.isEmpty()) {
			if (oneTimeUtilityParams.size() > 1) {
				log.warn("More than one one-time utility parameters defined for intervention "+ name() +". The following parameters were found: " + oneTimeUtilityParams + ". Only the first will be used.");
			}
			ParameterWrapper utilityParam = oneTimeUtilityParams.iterator().next();
			boolean isDisutility = ((SpecificInformationForUtility) utilityParam.getSpecificInformation()).isDisutility();
			paramMapping.put(isDisutility ? StandardParameter.ONSET_DISUTILITY : StandardParameter.ONSET_UTILITY, utilityParam);
		}
		if (!annualUtilityParams.isEmpty()) {
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