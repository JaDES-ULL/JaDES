package es.ull.simulation.hta.params;

import es.ull.simulation.hta.populations.Population;

/**
 * The standard parameters used by a model component.
 */
public enum StandardParameter implements ParameterTemplate {
	ANNUAL_COST(0.0, "Annual cost of a model component", ParameterGroup.COST, "AC_"),
	ANNUAL_DISUTILITY(0.0, "Annual disutility of a model component", ParameterGroup.DISUTILITY, "DU_"),
	ANNUAL_UTILITY(1.0, "Annual utility of a model component", ParameterGroup.UTILITY, "U_"),
    BIRTH_PREVALENCE(1.0, "Birth prevalence of a disease", ParameterGroup.RISK, "BIRTH_PREV_"),
	DISEASE_DIAGNOSIS_COST(0.0, "Diagnosis cost of a model component", ParameterGroup.COST, "C_DIAG_"),
	DISEASE_PROGRESSION_DURATION(Population.DEF_MAX_AGE - Population.DEF_MIN_AGE, "Duration of the disease progression (in years)", ParameterGroup.OTHER, "DUR_"),
	DISEASE_PROGRESSION_END_AGE(Population.DEF_MAX_AGE, "Age at which the disease progression ends", ParameterGroup.OTHER, "END_AGE_"),
	DISEASE_PROGRESSION_INITIAL_PROPORTION(0.0, "Initial proportion of patients with this disease progression", ParameterGroup.RISK, "P_INIT_"),
	DISEASE_PROGRESSION_ONSET_AGE(0.0, "Age at which the disease progression starts", ParameterGroup.OTHER, "ONSET_AGE_"),
	DISEASE_PROGRESSION_PROBABILITY_OF_DIAGNOSIS(0.0, "Probability of diagnosis upon onset of this disease progression", ParameterGroup.RISK, "P_DIAG_"),
	DISEASE_PROGRESSION_RISK_OF_DEATH(0.0, "Risk of death due to this disease progression", ParameterGroup.RISK, "P_DEATH_"),
	FOLLOW_UP_COST(0.0, "Annual follow up cost of a model component", ParameterGroup.COST, "C_FOLLOW_"),
    INCIDENCE(1.0, "Incidence of a model component", ParameterGroup.RISK, "INC_"),
	INCIDENCE_RATE_RATIO(1.0, "Incidence rate ratio", ParameterGroup.RISK, "IRR_"),
	INCREASED_MORTALITY_RATE(1.0, "Increased mortality rate", ParameterGroup.OTHER, "IMR_"),
	LIFE_EXPECTANCY_REDUCTION(0.0, "Life expectancy reduction", ParameterGroup.OTHER, "LER_"),
	ONE_TIME_COST(0.0, "One-time cost of a model component", ParameterGroup.COST, "OC_"),
	ONSET_COST(0.0, "Cost applied to a model component when it appears", ParameterGroup.COST, "TC_"),
	ONSET_DISUTILITY(0.0, "Disutility to be applied on onset of a model component", ParameterGroup.DISUTILITY, "TDU_"),
	ONSET_UTILITY(1.0, "Utility to be applied on onset of a model component", ParameterGroup.DISUTILITY, "TU_"),
    POPULATION_BASE_UTILITY(1.0, "Base utility of a population", ParameterGroup.UTILITY, "U_"),
    PREVALENCE(1.0, "Prevalence of a model component", ParameterGroup.RISK, "PREV_"),
	PROBABILITY(0.0, "Probability", ParameterGroup.RISK, "P_"),
	PROPORTION(0.0, "Proportion", ParameterGroup.RISK, "P_"),
	RATE(0.0, "Rate", ParameterGroup.RISK, "RATE_"),
	RELATIVE_RISK(1.0, "Relative risk", ParameterGroup.RISK, "RR_"),
	RESOURCE_USAGE(1.0, "Resource usage", ParameterGroup.OTHER, "USAGE_"),
    SENSITIVITY(1.0, "Sensitivity", ParameterGroup.RISK, "SENS_"),
    SPECIFICITY(1.0, "Specificity", ParameterGroup.RISK, "SPEC_"),
	TIME_TO_EVENT(Double.NaN, "Time to event", ParameterGroup.RISK, "TTE_"),
	TREATMENT_COST(0.0, "Annual treatment cost of a model component", ParameterGroup.COST, "C_TREAT_"),
	UNIT_COST(0.0, "Unit cost of a model component", ParameterGroup.COST, "UC_");

	private final double defaultValue;
	private final String defaultDescription;
	private final ParameterGroup group;
	private final String prefix;

	private StandardParameter(double defaultValue, String defaultDescription, ParameterGroup group, String prefix) {
		this.defaultValue = defaultValue;
		this.defaultDescription = defaultDescription;
		this.group = group;
		this.prefix = prefix;
	}

	private StandardParameter(double defaultValue, String defaultDescription, ParameterGroup group) {
		this(defaultValue, defaultDescription, group, "");
	}

	@Override
	public String getDefaultDescription() {
		return defaultDescription;
	}

	@Override
	public double getDefaultValue() {
		return defaultValue;
	}

	@Override
	public ParameterGroup getGroup() {
		return group;
	}

	@Override
	public String getPrefix() {
		return prefix;
	}
}