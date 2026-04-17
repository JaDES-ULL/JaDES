package es.ull.simulation.hta.osdi.ontology;

/**
 * A list of the different data properties that can be defined in OSDi
 * @author Iván Castilla Rodríguez
 */
public enum OSDiDataProperty implements IOSDiComponentWrapper {
	APPLIES_ONE_TIME("appliesOneTime"),
	HAS_ALFA_PARAMETER("hasAlfaParameter"),
	HAS_AUTHOR("hasAuthor"),
	HAS_AVERAGE_PARAMETER("hasAverageParameter"),
	HAS_BETA_PARAMETER("hasBetaParameter"),
	HAS_CALCULATION_METHOD("hasCalculationMethod"),
	HAS_DESCRIPTION("hasDescription"),
	HAS_DISCOUNT_FOR_COSTS("hasDiscountForCosts"),
	HAS_DISCOUNT_FOR_EFFECTS("hasDiscountForEffects"),
	HAS_DISUTILITY_COMBINATION_METHOD("hasDisutilityCombinationMethod"),
	HAS_EXPECTED_VALUE("hasExpectedValue"),
	HAS_EXPRESSION_VALUE("hasExpressionValue"),
	HAS_GEOGRAPHICAL_CONTEXT("hasGeographicalContext"),
	HAS_HOURS_INTERVAL("hasHoursInterval"),
	HAS_LAMBDA_PARAMETER("hasLambdaParameter"),
	HAS_LOWER_LIMIT_PARAMETER("hasLowerLimitParameter"),
	HAS_MAX_AGE("hasMaxAge"),
	HAS_MIN_AGE("hasMinAge"),
	HAS_NULL_PROGRESSION("hasNullProgression"),
	HAS_NUMBER_OF_PSA_RUNS("hasNumberOfPSARuns"),
	HAS_NUMBER_OF_SIMULATED_INDIVIDUALS("hasNumberOfSimulatedIndividuals"),
	HAS_OFFSET_PARAMETER("hasOffsetParameter"),
	HAS_PROBABILITY_DISTRIBUTION_PARAMETER("hasProbabilityDistributionParameter"),
	HAS_PROBABILITY_PARAMETER("hasProbabilityParameter"),
	HAS_RANGE("hasRange"),
	HAS_REF_TO("hasRefTo"),
	HAS_REF_TO_DO("hasRefToDO"),
	HAS_REF_TO_GARD("hasRefToGARD"),
	HAS_REF_TO_ICD("hasRefToICD"),
	HAS_REF_TO_OMIM("hasRefToOMIM"),
	HAS_REF_TO_ORDO("hasRefToORDO"),
	HAS_REF_TO_SNOMED("hasRefToSNOMED"),
	HAS_REF_TO_STATO("hasRefToSTATO"),
	HAS_REF_TO_WIKIDATA("hasRefToWikidata"),
	HAS_SCALE_PARAMETER("hasScaleParameter"),
	HAS_SIZE("hasSize"),
	HAS_SOURCE("hasSource"),
	HAS_STANDARD_DEVIATION_PARAMETER("hasStandardDeviationParameter"),
	HAS_TIME_HORIZON("hasTimeHorizon"),
	HAS_UNIT("hasUnit"),
	HAS_UPPER_LIMIT_PARAMETER("hasUpperLimitParameter"),
	HAS_YEAR("hasYear"),
	IS_ASSESSED_INTERVENTION("isAssessedIntervention"),
	IS_DISUTILITY("isDisutility"),
	IS_TRUE_EPIDEMIOLOGICAL_PARAMETER_ESTIMATE("isTrueEpidemiologicalParameterEstimate");
	
	private final String shortName;
	private OSDiDataProperty(String shortName) {
		this.shortName = shortName;
	}
	
	@Override
	public String getShortName() {
		return shortName;
	}
	
	@Override
	public boolean isCore() {
		return true;
	}
}