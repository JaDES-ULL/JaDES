package es.ull.simulation.hta.osdi.des;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import es.ull.simulation.hta.Patient;
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
import es.ull.simulation.hta.params.Discount;
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

	/**
	 * Returns true if every patient starts with this progression because it is the target of the interventions (decision D1) and the population
	 * does not declare its initial proportion, which is then assumed to be 1 (see {@link DESInterventionTarget}). If the population declares it,
	 * {@link DESInterventionTarget} has already checked that it is 1.
	 * @return true if the initial proportion of this progression is assumed to be 1 because it is the target of the interventions
	 */
	private boolean isTargetWithAssumedInitialProportion() {
		return getModel().isTarget(progressionWrapper) && !paramMapping.containsKey(StandardParameter.DISEASE_PROGRESSION_INITIAL_PROPORTION);
	}

	@Override
	public void createParameters() {
		for (ParameterTemplate paramDesc : paramMapping.keySet()) {
			final ParameterWrapper param = paramMapping.get(paramDesc);
			addUsedParameter(paramDesc, ParameterFactory.getParameterInstance(getModel(), param, paramDesc.getGroup()));
		}
		// A chronic target of the interventions is present in every patient at time 0 (D1)
		if (!DiseaseProgression.Type.ACUTE_MANIFESTATION.equals(getType()) && isTargetWithAssumedInitialProportion()) {
			addUsedParameter(StandardParameter.DISEASE_PROGRESSION_INITIAL_PROPORTION, "Initial proportion of the target of the interventions", "Assumption (D1)", 1.0);
		}
	}

	/**
	 * Returns the payoffs of the strategies of this progression in the arm of a patient (decision D2, see {@link DESStrategyPayoffs})
	 */
	private DESStrategyPayoffs.Payoffs getStrategyPayoffs(Patient pat) {
		return getModel().getStrategyPayoffs().get(pat.getIntervention(), progressionWrapper.getIndividualIRI());
	}

	/**
	 * Adds the annual costs of the strategies of the progression in the arm of the patient, while the patient has the progression (D2)
	 */
	@Override
	public double getCostWithinPeriod(Patient pat, double initYear, double endYear, Discount discountRate) {
		return super.getCostWithinPeriod(pat, initYear, endYear, discountRate) + getStrategyPayoffs(pat).getCostWithinPeriod(pat, initYear, endYear, discountRate);
	}

	@Override
	public double[] getAnnualizedCostWithinPeriod(Patient pat, double initYear, double endYear, Discount discountRate) {
		return getStrategyPayoffs(pat).getAnnualizedCostWithinPeriod(pat, initYear, endYear, discountRate, super.getAnnualizedCostWithinPeriod(pat, initYear, endYear, discountRate));
	}

	/**
	 * Adds the one-time costs of the strategies of the progression in the arm of the patient, at its onset or with each episode (D2)
	 */
	@Override
	public double getStartingCost(Patient pat, double time, Discount discountRate) {
		return super.getStartingCost(pat, time, discountRate) + getStrategyPayoffs(pat).getStartingCost(pat, time, discountRate);
	}

	/**
	 * Adds the annual disutilities of the strategies of the progression in the arm of the patient (D2)
	 */
	@Override
	public double getAnnualDisutility(Patient pat) {
		return super.getAnnualDisutility(pat) + getStrategyPayoffs(pat).getAnnualDisutility(pat);
	}

	/**
	 * Adds the one-time disutilities of the strategies of the progression in the arm of the patient, at its onset or with each episode (D2)
	 */
	@Override
	public double getStartingDisutility(Patient pat) {
		return super.getStartingDisutility(pat) + getStrategyPayoffs(pat).getStartingDisutility(pat);
	}
	
}