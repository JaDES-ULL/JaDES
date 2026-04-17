package es.ull.simulation.hta.osdi.des;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import es.ull.simulation.hta.osdi.OSDiLogger;
import es.ull.simulation.hta.osdi.des.factories.CostParametersHandler;
import es.ull.simulation.hta.osdi.des.factories.ParameterFactory;
import es.ull.simulation.hta.osdi.des.factories.UtilityParametersHandler;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.ClinicalProgressionType;
import es.ull.simulation.hta.osdi.ontology.ClinicalProgressionWrapper;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper;
import es.ull.simulation.hta.osdi.ontology.PopulationWrapper;
import es.ull.simulation.hta.osdi.ontology.TemporalConstraint;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper.SpecificInformationForUtility;
import es.ull.simulation.hta.params.ParameterTemplate;
import es.ull.simulation.hta.params.StandardParameter;
import es.ull.simulation.hta.progression.Disease;
import es.ull.simulation.hta.progression.DiseaseProgression;

/**
 * A disease progression in the OSDi DES model. This class extends the generic DiseaseProgression class to represent the specific characteristics of a disease progression 
 * as defined in the OSDi ontology. 
 */
public class OSDiDiseaseProgression extends DiseaseProgression {
	private final static OSDiLogger log = OSDiLogger.getLogger(OSDiDiseaseProgression.class);

	private final Map<ParameterTemplate, ParameterWrapper> paramMapping;
	private final CostParametersHandler	costParametersHandler;
	private final UtilityParametersHandler utilityHandler;
	private final ClinicalProgressionWrapper progressionWrapper;
	
	public OSDiDiseaseProgression(OSDiDESModel model, ClinicalProgressionWrapper progressionWrapper, Disease disease, PopulationWrapper populationWrapper) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
		super(model, progressionWrapper.getShortName(), progressionWrapper.getDescription().orElse(""), disease, getTypeFromWrapper(progressionWrapper));
		paramMapping = new TreeMap<>();
		this.progressionWrapper = progressionWrapper;
		this.costParametersHandler = new CostParametersHandler(progressionWrapper.getCosts());
		this.utilityHandler = new UtilityParametersHandler(progressionWrapper.getUtilities());
		// Gets the one-time and annual cost parameters defined in the ontology for that disease progression
		final Set<ParameterWrapper> oneTimeCostParams = costParametersHandler.getCostParametersByApplication(true);
		final Set<ParameterWrapper> annualCostParams = costParametersHandler.getCostParametersByApplication(false);
		final Set<ParameterWrapper> oneTimeUtilityParams = utilityHandler.getUtilityParametersByApplication(true);
		final Set<ParameterWrapper> annualUtilityParams = utilityHandler.getUtilityParametersByApplication(false);
		// Checking coherence of number of costs and the type of manifestation		
		if (DiseaseProgression.Type.ACUTE_MANIFESTATION.equals(getType())) {
			if (!oneTimeCostParams.isEmpty()) {
				if (oneTimeCostParams.size() > 1) {
					log.warn("More than one one-time cost parameters defined for acute manifestation "+ name() +". The following parameters were found: " + oneTimeCostParams + ". Only the first will be used.");
				}
				paramMapping.put(StandardParameter.ONSET_COST, oneTimeCostParams.iterator().next());
			}
			if (!annualCostParams.isEmpty()) {
				log.warn("Annual cost parameters defined for acute manifestation "+ name() +". The following parameters were found: " + annualCostParams + ". These parameters will be ignored.");
			}
			if (!oneTimeUtilityParams.isEmpty()) {
				if (oneTimeUtilityParams.size() > 1) {
					log.warn("More than one one-time utility parameters defined for acute manifestation "+ name() +". The following parameters were found: " + oneTimeUtilityParams + ". Only the first will be used.");
				}
				ParameterWrapper utilityParam = oneTimeUtilityParams.iterator().next();
				boolean isDisutility = ((SpecificInformationForUtility) utilityParam.getSpecificInformation()).isDisutility();
				paramMapping.put(isDisutility ? StandardParameter.ONSET_DISUTILITY : StandardParameter.ONSET_UTILITY, utilityParam);
			}
			if (!annualUtilityParams.isEmpty()) {
				log.warn("Annual utility parameters defined for acute manifestation "+ name() +". The following parameters were found: " + annualUtilityParams + ". These parameters will be ignored.");
			}
		}
		else {
			if (!oneTimeCostParams.isEmpty()) {
				if (oneTimeCostParams.size() > 1) {
					log.warn("More than one one-time cost parameters defined for disease progression "+ name() +". The following parameters were found: " + oneTimeCostParams + ". Only the first will be used.");
				}
				paramMapping.put(StandardParameter.ONSET_COST, oneTimeCostParams.iterator().next());
			}
			if (!annualCostParams.isEmpty()) {
				if (annualCostParams.size() > 1) {
					log.warn("More than one annual cost parameters defined for disease progression "+ name() +". The following parameters were found: " + annualCostParams + ". Only the first will be used.");
				}
				paramMapping.put(StandardParameter.ANNUAL_COST, annualCostParams.iterator().next());
			}
			if (!oneTimeUtilityParams.isEmpty()) {
				if (oneTimeUtilityParams.size() > 1) {
					log.warn("More than one one-time utility parameters defined for disease progression "+ name() +". The following parameters were found: " + oneTimeUtilityParams + ". Only the first will be used.");
				}
				ParameterWrapper utilityParam = oneTimeUtilityParams.iterator().next();
				boolean isDisutility = ((SpecificInformationForUtility) utilityParam.getSpecificInformation()).isDisutility();
				paramMapping.put(isDisutility ? StandardParameter.ONSET_DISUTILITY : StandardParameter.ONSET_UTILITY, utilityParam);
			}
			if (!annualUtilityParams.isEmpty()) {
				if (annualUtilityParams.size() > 1) {
					log.warn("More than one annual utility parameters defined for disease progression "+ name() +". The following parameters were found: " + annualUtilityParams + ". Only the first will be used.");
				}
				ParameterWrapper utilityParam = annualUtilityParams.iterator().next();
				boolean isDisutility = ((SpecificInformationForUtility) utilityParam.getSpecificInformation()).isDisutility();
				paramMapping.put(isDisutility ? StandardParameter.ANNUAL_DISUTILITY : StandardParameter.ANNUAL_UTILITY, utilityParam);
			}
		}
		// TODO: Process effects
		//createUsedParameter(OSDiObjectProperty.HAS_PROBABILITY_OF_DIAGNOSIS, StandardParameter.DISEASE_PROGRESSION_PROBABILITY_OF_DIAGNOSIS);
		// Chronic manifestations may have increased mortality rates, reductions of life expectancy
		// FIXME: Currently, we are accepting even a probability of death. Conceptually this could only happen when the chronic manifestation is preceded by an acute manifestation 
		// (actually, the acute manifestation would be the one with such probability). We are doing so to simplify modeling, but it is inaccurate
		//createUsedParameter(OSDiObjectProperty.HAS_PROBABILITY_OF_DEATH, StandardParameter.DISEASE_PROGRESSION_RISK_OF_DEATH);
		if (progressionWrapper.getTemporalConstraintForProgression().isPresent()) {
			TemporalConstraint temporalConstraint = progressionWrapper.getTemporalConstraintForProgression().get();
			if (temporalConstraint.startTime().isPresent()) {
				paramMapping.put(StandardParameter.DISEASE_PROGRESSION_ONSET_AGE, temporalConstraint.startTime().get());
			}
			if (temporalConstraint.endTime().isPresent()) {
				paramMapping.put(StandardParameter.DISEASE_PROGRESSION_END_AGE, temporalConstraint.endTime().get());
			}
		}

		// Acute manifestations are assumed not to involve further mortality parameters
		if (!DiseaseProgression.Type.ACUTE_MANIFESTATION.equals(getType())) {
		//	createUsedParameter(OSDiObjectProperty.HAS_INCREASED_MORTALITY_RATE, StandardParameter.INCREASED_MORTALITY_RATE);
	//		createUsedParameter(OSDiObjectProperty.HAS_LIFE_EXPECTANCY_REDUCTION, StandardParameter.LIFE_EXPECTANCY_REDUCTION);
			createInitialProportionParam(populationWrapper);
		}
	}

	private static final DiseaseProgression.Type getTypeFromWrapper(ClinicalProgressionWrapper progressionWrapper) {
		ClinicalProgressionType progressionType = progressionWrapper.getClinicalProgressionType();
		switch (progressionType) {
			case DEVELOPMENT:
				return null;
				// TODO: merge development into diseaseprogression.type
				//return DiseaseProgression.Type.DEVELOPMENT;
			case STAGE:
				return DiseaseProgression.Type.STAGE;
			case ACUTE_MANIFESTATION:
				return DiseaseProgression.Type.ACUTE_MANIFESTATION;
			case CHRONIC_MANIFESTATION:
				return DiseaseProgression.Type.CHRONIC_MANIFESTATION;
			default:
				break;
		}
		return null;
	}

	public OSDiDESModel getModel() {
		return (OSDiDESModel) super.getModel();
	}
	
	public ClinicalProgressionWrapper getProgressionWrapper() {
		return progressionWrapper;
	}

	private void createInitialProportionParam(PopulationWrapper populationWrapper) throws MalformedOSDiModelException {
		final Map<ClinicalProgressionWrapper, ParameterWrapper> initialProportions = populationWrapper.getInitialProportions();
		final ParameterWrapper initialProportionParam = initialProportions.get(this.progressionWrapper);
		if (initialProportionParam != null) {
			paramMapping.put(StandardParameter.DISEASE_PROGRESSION_INITIAL_PROPORTION, initialProportionParam);
		}
	}
	
	@Override
	public void createParameters() {
		for (ParameterTemplate paramDesc : paramMapping.keySet()) {
			final ParameterWrapper param = paramMapping.get(paramDesc);
			addUsedParameter(paramDesc, ParameterFactory.getParameterInstance(getModel(), param, paramDesc.getGroup()));
		}
	}
	
}