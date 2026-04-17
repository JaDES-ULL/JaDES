package es.ull.simulation.hta.osdi.ontology;

/**
 * Enumeration of the object properties defined in the OSDi ontology.
 * Each object property corresponds to a relationship between individuals in the ontology.
 * The short name of the object property is used to refer to it in the ontology.
 * @author Iván Castilla Rodríguez
 */
public enum OSDiObjectProperty implements IOSDiComponentWrapper {
	AFFECTS("affects"),
	AFFECTS_PROGRESSION_ELEMENT("affectsProgressionElement"),
	BELONGS_TO_GROUP("belongsToGroup"),
	BROADER("broader", false),
	CAUSES_FAIL_OF("causesFailOf"),
	DEPENDS_ON("dependsOn"),
	DETECTS("detects"),
	FAILS_IF("failsIf"),
	FOLLOWED_BY_STRATEGY("followedByStrategy"),
	FOLLOWS_STRATEGY("followsStrategy"),
	HAS_AGE("hasAge"),
	HAS_ATTRIBUTE_VALUE("hasAttributeValue"),
	HAS_CATEGORY("hasCategory"),
	HAS_CONDITION_EXPRESSION("hasConditionExpression"),
	HAS_COST("hasCost"),
	HAS_DATA_ITEM_TYPE("hasDataItemType"),
	HAS_DEPENDANT_CALCULATED_PARAMETER("hasDependantCalculatedParameter"),
	HAS_DOSE("hasDose"),
	HAS_DURATION("hasDuration"),
	HAS_EFFECT("hasEffect"),
	HAS_EFFECT_MAGNITUDE("hasEffectMagnitude"),
	HAS_END_AGE("hasEndAge"),
	HAS_EPIDEMIOLOGICAL_PARAMETER("hasEpidemiologicalParameter"),
	HAS_EXPRESSION_LANGUAGE("hasExpressionLanguage"),
	HAS_FIRST("hasFirst"),
	HAS_FREQUENCY("hasFrequency"),
	HAS_GROUP_ITEM("hasGroupItem"),
	HAS_GUIDELINE("hasGuideline"),
	HAS_INITIAL_PROPORTION("hasInitialProportion"),
	HAS_INTERVENTION("hasIntervention"),
	HAS_LIFE_EXPECTANCY("hasLifeExpectancy"),
	HAS_NEXT("hasNext"),
	HAS_ONSET_AGE("hasOnsetAge"),
	HAS_PARAMETER("hasParameter"),
	HAS_PREVIOUS("hasPrevious"),
	HAS_PROGRESSION_ELEMENT("hasProgressionElement"),
	HAS_PROPORTION_WITHIN_GROUP("hasProportionWithinGroup"),
	HAS_RISK_CHARACTERIZATION("hasRiskCharacterization"),
	HAS_SENSITIVITY("hasSensitivity"),
	HAS_SEX("hasSex"),
	HAS_SPECIFICITY("hasSpecificity"),
	HAS_STRATEGY("hasStrategy"),
	HAS_SUB_PROGRESSION("hasSubProgression"),
	HAS_SUBPOPULATION("hasSubpopulation"),
	HAS_TEMPORARY_THRESHOLD("hasTemporaryThreshold"),
	HAS_UNCERTAINTY_CHARACTERIZATION("hasUncertaintyCharacterization"),
	HAS_UTILITY("hasUtility"),
	IN_SCHEME("inScheme", false),
	INCLUDED_BY_MODEL("includedByModel"),
	INCLUDES_MODEL_ITEM("includesModelItem"),
	IS_AFFECTED_BY("isAffectedBy"),
	IS_CONDITION_EXPRESSION_OF("isConditionExpressionOf"),
	IS_DATA_ITEM_TYPE_OF("isDataItemTypeOf"),
	IS_DETECTED_BY("isDetectedBy"),
	IS_EFFECT_OF("isEffectOf"),
	IS_EXPRESSION_LANGUAGE_OF("isExpressionLanguageOf"),
	IS_GUIDELINE_OF("isGuidelineOf"),
	IS_INTERVENTION_OF("isInterventionOf"),
	IS_PARAMETER_OF("isParameterOf"),
	IS_PROGRESSION_ELEMENT_OF("isProgressionElementOf"),
	IS_REQUIRED_BY("isRequiredBy"),
	IS_RISK_CHARACTERIZATION_OF("isRiskCharacterizationOf"),
	IS_STRATEGY_OF("isStrategyOf"),
	IS_SUBPOPULATION_OF("isSubpopulationOf"),
	IS_SUPERSEDED_BY_CLINICAL_PROGRESSION("isSupersededByClinicalProgression"),
	IS_UNCERTAINTY_CHARACTERIZATION_OF("isUncertaintyCharacterizationOf"),
	IS_USED_AS_ATTRIBUTE_VALUE_BY("isUsedAsAttributeValueBy"),
	IS_USED_BY_STRATEGY("isUsedByStrategy"),
	IS_VALUE_OF_ATTRIBUTE("isValueOfAttribute"),
	REQUIRES("requires"),
	SUPERSEDES_CLINICAL_PROGRESSION("supersedesClinicalProgression"),
	TOP_CONCEPT_OF("topConceptOf", false),
	USES_ATTRIBUTE_VALUE("usesAttributeValue"),
	USES_HEALTH_TECHNOLOGY("usesHealthTechnology"),
	USES_MODEL("usesModel"),
	USES_SAME_MODEL_ITEMS_AS("usesSameModelItemsAs");

	/** The short name of the object property. */
	private final String shortName;
	/** Indicates whether this object property is part of the OSDi core ontology. */
	private final boolean isCore;
	private OSDiObjectProperty(String shortName) {
		this(shortName, true);
	}
	private OSDiObjectProperty(String shortName, boolean isCore) {
		this.shortName = shortName;
		this.isCore = isCore;
	}

	@Override
	public String getShortName() {
		return shortName;
	}

	@Override
	public boolean isCore() {
		return isCore;
	}
}