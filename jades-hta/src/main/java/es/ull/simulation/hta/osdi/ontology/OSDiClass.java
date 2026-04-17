package es.ull.simulation.hta.osdi.ontology;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.BiFunction;

import org.semanticweb.owlapi.model.IRI;

/**
 * A list of the different classes that can be defined in OSDi
 * @author Iván Castilla Rodríguez
 */
public enum OSDiClass implements IOSDiComponentWrapper {
	ACUTE_MANIFESTATION("AcuteManifestation", ClinicalProgressionWrapper::new),
	AGENT_BASED_MODEL("AgentBasedModel", null),
	ALTERNATIVE_COMBINATION_RULE("AlternativeCombinationRule", ProgressionCombinationRuleWrapper::new),
	ATTRIBUTE("Attribute", AttributeWrapper::new),
	ATTRIBUTE_VALUE_PARAMETER("AttributeValueParameter", ParameterWrapper::new),
	BERNOULLI_DISTRIBUTION_EXPRESSION("BernoulliDistributionExpression", ProbabilisticExpressionWrapper::new),
	BETA_DISTRIBUTION_EXPRESSION("BetaDistributionExpression", ProbabilisticExpressionWrapper::new),
	CALCULATED_PARAMETER("CalculatedParameter", ParameterWrapper::new),
	CHRONIC_MANIFESTATION("ChronicManifestation", ClinicalProgressionWrapper::new),
	CLINICAL_PROGRESSION_ELEMENT("ClinicalProgressionElement", ClinicalProgressionWrapper::new),
	CLINICAL_PROGRESSION_WITH_INITIAL_PROPORTION("ClinicalProgressionWithInitialProportion", ClinicalProgressionWrapper::new),
	COEXISTENT_COMBINATION_RULE("CoexistentCombinationRule", ProgressionCombinationRuleWrapper::new),
	CONCEPT("Concept", null, false),
	CONCEPT_SCHEME("ConceptScheme", null, false),
	COST("Cost", ParameterWrapper::new),
	CURRENCY("Currency", null),
	DATA_ITEM_TYPE("DataItemType", null),
	DECISION_TREE_MODEL("DecisionTreeModel", null),
	DESCRIBABLE("Describable", null),
	DETECTION_INTERVENTION("DetectionIntervention", DetectionInterventionWrapper::new),
	DETECTION_STRATEGY("DetectionStrategy", DetectionStrategyWrapper::new),
	DETERMINISTIC_PARAMETER("DeterministicParameter", ParameterWrapper::new),
	DEVELOPMENT("Development", ClinicalProgressionWrapper::new),
	DIAGNOSIS_INTERVENTION("DiagnosisIntervention", DetectionInterventionWrapper::new),
	DIAGNOSIS_STRATEGY("DiagnosisStrategy", DetectionStrategyWrapper::new),
	DISCRETE_EVENT_SIMULATION_MODEL("DiscreteEventSimulationModel", null),
	DISEASE("Disease", DiseaseWrapper::new),
	DISEASE_PROGRESSION_ELEMENT("DiseaseProgressionElement", null),
	EFFECT("Effect", EffectWrapper::new),
	EPIDEMIOLOGICAL_PARAMETER("EpidemiologicalParameter", ParameterWrapper::new),
	EXPERIMENT("Experiment", null),
	EXPONENTIAL_DISTRIBUTION_EXPRESSION("ExponentialDistributionExpression", ProbabilisticExpressionWrapper::new),
	EXPRESSION_LANGUAGE("ExpressionLanguage", null),
	FIRST_ORDER_UNCERTAINTY_PARAMETER("FirstOrderUncertaintyParameter", ParameterWrapper::new),
	FOLLOW_UP_STRATEGY("FollowUpStrategy", StrategyWrapper::new),
	GAMMA_DISTRIBUTION_EXPRESSION("GammaDistributionExpression", ProbabilisticExpressionWrapper::new),
	GROUP("Group", null),
	GROUPABLE_MODEL_ITEM("GroupableModelItem", null),
	GROUPABLE_MODEL_ITEM_WITH_PROPORTION("GroupableModelItemWithProportion", null),
	GUIDELINE("Guideline", null),
	HEALTH_CONDITION("HealthCondition", ClinicalProgressionWrapper::new),
	HEALTH_TECHNOLOGY("HealthTechnology", null),
	INCREASED_MORTALITY_RATE_EFFECT("IncreasedMortalityRateEffect", EffectWrapper::new),
	INFECTIOUS_DISEASE("InfectiousDisease",DiseaseWrapper::new),
	INHERITED_DISEASE("InheritedDisease", DiseaseWrapper::new),
	INSTANT_DEATH_EFFECT("InstantDeathEffect", EffectWrapper::new),
	INTERVENTION("Intervention", InterventionWrapper::new),
	LIFE_EXPECTANCY_REDUCTION_EFFECT("LifeExpectancyReductionEffect", EffectWrapper::new),
	LINE_OF_THERAPY("LineOfTherapy", StrategyWrapper::new),
	MANIFESTATION("Manifestation", ClinicalProgressionWrapper::new),
	MARKOV_MODEL("MarkovModel", null),
	MEASURED_DATA_ITEM_TYPE("MeasuredDataItemType", null),
	MODEL("Model", null),
	MODEL_ITEM("ModelItem", null),
	MODEL_ITEM_DEFINING_DISEASE_PROGRESSION("ModelItemDefiningDiseaseProgression", null),
	MODEL_ITEM_WITH_COST("ModelItemWithCost", null),
	MODEL_ITEM_WITH_DETECTION("ModelItemWithDetection", null),
	MODEL_ITEM_WITH_EFFECT("ModelItemWithEffect", null),
	MODEL_ITEM_WITH_FOLLOW_UP("ModelItemWithFollowUp", null),
	MODEL_ITEM_WITH_RISK_CHARACTERIZATION("ModelItemWithRiskCharacterization", null),
	MODEL_ITEM_WITH_TEMPORAL_CONSTRAINTS("ModelItemWithTemporalConstraints", null),
	MODEL_ITEM_WITH_TREATMENT("ModelItemWithTreatment", null),
	MODEL_ITEM_WITH_UTILITY("ModelItemWithUtility", null),
	NORMAL_DISTRIBUTION_EXPRESSION("NormalDistributionExpression", ProbabilisticExpressionWrapper::new),
	ORDERED_MODEL_ITEM("OrderedModelItem", null),
	PARAMETER("Parameter", ParameterWrapper::new),
	PARAMETER_NATURE("ParameterNature", null),
	PATHWAY("Pathway", PathwayWrapper::new),
	POISSON_DISTRIBUTION_EXPRESSION("PoissonDistributionExpression", ProbabilisticExpressionWrapper::new),
	POPULATION("Population", PopulationWrapper::new),
	PROBABILITY_DISTRIBUTION_EXPRESSION("ProbabilityDistributionExpression", ProbabilisticExpressionWrapper::new),
	PROGRESSION_COMBINATION_RULE("ProgressionCombinationRule", ProgressionCombinationRuleWrapper::new),
	QOL_DATA_ITEM_TYPE("QoLDataItemType", null),
	RARE_DISEASE("RareDisease", DiseaseWrapper::new),
	RESOURCE_USAGE_ITEM("ResourceUsageItem", null),
	SCREENING_INTERVENTION("ScreeningIntervention", DetectionInterventionWrapper::new),
	SCREENING_STRATEGY("ScreeningStrategy", DetectionStrategyWrapper::new),
	SECOND_ORDER_UNCERTAINTY_PARAMETER("SecondOrderUncertaintyParameter", ParameterWrapper::new),
	SEQUENTIAL_COMBINATION_RULE("SequentialCombinationRule", ProgressionCombinationRuleWrapper::new),
	STAGE("Stage", ClinicalProgressionWrapper::new),
	STRATEGY("Strategy", StrategyWrapper::new),
	THERAPEUTIC_INTERVENTION("TherapeuticIntervention", InterventionWrapper::new),
	UNIFORM_DISTRIBUTION_EXPRESSION("UniformDistributionExpression", ProbabilisticExpressionWrapper::new),
	UTILITY("Utility", ParameterWrapper::new),
	YEAR_DEPENDANT_PARAMETER("YearDependantParameter", ParameterWrapper::new);

	/** The short name that is used as IRI of this class in the ontology */
	private final String shortName;
	/** Indicates whether this class is part of the OSDi core ontology */
	private final boolean isCore;
	/** The correspondent constructor for the wrapper of the class */
	private final BiFunction<ModelWrapper, IRI, ? extends IModelItemWrapper> constructor;

	private static final Map<String, OSDiClass> reverseClass = new TreeMap<>();
	static {
		for (OSDiClass cls : OSDiClass.values()) {
			reverseClass.put(cls.getShortName(), cls);
		}
	}

	/**
	 * Creates a new OSDi class with the specified short name.
	 * @param shortName The short name that is used as IRI of this class in the ontology
	 */
	private OSDiClass(String shortName, BiFunction<ModelWrapper, IRI, ? extends IModelItemWrapper> constructor) {
		this(shortName, constructor, true);
	}

	/**
	 * Creates a new OSDi class with the specified short name and core flag.
	 * @param shortName The short name that is used as IRI of this class in the ontology
	 * @param isCore Indicates whether this class is part of the OSDi core ontology
	 */
	private OSDiClass(String shortName, BiFunction<ModelWrapper, IRI, ? extends IModelItemWrapper> constructor, boolean isCore) {
		this.shortName = shortName;
		this.isCore = isCore;
		this.constructor = constructor;
	}

	@Override
	public String getShortName() {
		return shortName;
	}

	/**
	 * Returns the correspondent constructor for the wrapper of this class, if any.
	 * @return the correspondent constructor for the wrapper of this class, or an empty optional if no wrapper is defined for this class.
	 */
	public Optional<BiFunction<ModelWrapper, IRI, ? extends IModelItemWrapper>> getConstructor() {
		return Optional.ofNullable(constructor);
	}

	@Override
	public boolean isCore() {
		return isCore;
	}

	/**
	 * Returns the OSDiClass associated to the IRI. It uses the short form of the IRI to find the corresponding class in the enum. 
	 * If the IRI does not match any of the defined classes, it returns null. 
	 * @param iri The IRI of the class
	 * @return The OSDiClass associated to the IRI. If the IRI does not match any of the defined classes, it returns null.
	 */	
	public static Optional<OSDiClass> fromIRI(IRI iri) {
		return Optional.ofNullable(reverseClass.get(iri.getShortForm()));
	}
}