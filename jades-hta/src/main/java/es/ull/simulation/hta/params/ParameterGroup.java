package es.ull.simulation.hta.params;

/** 
 * The different groups of parameters 
 */
public enum ParameterGroup {
	/** The collection of attributes, i.e., parameters that define specific characteristics for each patient */
	ATTRIBUTE,
	/** The collection of risk parameters */
	RISK,
	/** The collection of cost parameters */
	COST,
	/** The collection of utility parameters */
	UTILITY,
	/** The collection of disutility parameters */
	DISUTILITY,
	/** The collection of life expectancy parameters */
	LIFE_EXPECTANCY,
	/** The collection of parameter modifiers */
	MODIFIER,
	/** The collection of miscellaneous parameters */
	OTHER,
	/** Unknown parameter type */
	UNKNOWN;
}