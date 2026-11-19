package es.ull.simulation.hta.osdi.ontology;

import java.util.TreeMap;

import org.semanticweb.owlapi.model.IRI;

/**
 * Individuals defined in the ontology as DataItemTypes. The enum name must be the upper case version of the individual IRI.
 * @author Iván Castilla Rodríguez
 *
 */
public enum OSDiDataItemType implements IOSDiComponentWrapper {
	CURRENCY_CANADIAN_DOLLAR("Currency_CanadianDollar", 0.0),
	CURRENCY_DOLLAR("Currency_Dollar", 0.0),
	CURRENCY_EURO("Currency_Euro", 0.0),
	CURRENCY_POUND("Currency_Pound", 0.0),
	DI_BIRTH_PREVALENCE("DI_BirthPrevalence", 1.0),
	DI_CONTINUOUS_VARIABLE("DI_Continuous_Variable", 0.0),
	DI_COUNT("DI_Count", 0.0),
	DI_EQ5D_UTILITY("DI_EQ5D_Utility", 1.0),
	DI_FACTOR("DI_Factor", 1.0),
	DI_GENERIC_UTILITY("DI_Generic_Utility", 1.0),
	DI_HAZARD_RATIO("DI_HazardRatio", 1.0),
	DI_INCIDENCE("DI_Incidence", 1.0),
	DI_LOWER_95_CONFIDENCE_LIMIT("DI_Lower95ConfidenceLimit", 0.0),
	DI_MEAN_DIFFERENCE("DI_MeanDifference", 0.0),
	DI_ODDS_RATIO("DI_OddsRatio", 1.0),
	DI_OTHER("DI_Other", 0.0),
	DI_PREVALENCE("DI_Prevalence", 1.0),
	DI_PROBABILITY("DI_Probability", 0.0),
	DI_PROPORTION("DI_Proportion", 0.0),
	DI_RATIO("DI_Ratio", 1.0),
	DI_RELATIVE_RISK("DI_RelativeRisk", 1.0),
	DI_SENSITIVITY("DI_Sensitivity", 1.0),
	DI_SPECIFICITY("DI_Specificity", 1.0),
	DI_STANDARD_DEVIATION("DI_StandardDeviation", 0.0),
	DI_TIME_TO_EVENT("DI_TimeToEvent", 0.0),
	DI_UPPER_95_CONFIDENCE_LIMIT("DI_Upper95ConfidenceLimit", 0.0),
	DI_UNDEFINED("DI_Undefined", Double.NaN);


	private static final TreeMap<String, OSDiDataItemType> reverseDataItemType = new TreeMap<>();
	static {
		for (OSDiDataItemType type : OSDiDataItemType.values()) {
			reverseDataItemType.put(type.getShortName(), type);
		}
	}

	private final String shortName;
	private final double defaultValue;
	private OSDiDataItemType(String individualIRI, double defaultValue) {
		this.shortName = individualIRI;
		this.defaultValue = defaultValue;
	}

	@Override
	public String getShortName() {
		return shortName;
	}

	@Override
	public boolean isCore() {
		return true;
	}

	/**
	 * @return the defaultValue
	 */
	public double getDefaultValue() {
		return defaultValue;
	}		
	/**
	 * Returns true if this type represents a probability, i.e., a value in [0, 1] that the decision trees use as the probability of a branch:
	 * probabilities, proportions, incidences, prevalences, birth prevalences, sensitivities and specificities.
	 * @return true if this type represents a probability
	 */
	public boolean isProbability() {
		switch (this) {
			case DI_PROBABILITY:
			case DI_PROPORTION:
			case DI_INCIDENCE:
			case DI_PREVALENCE:
			case DI_BIRTH_PREVALENCE:
			case DI_SENSITIVITY:
			case DI_SPECIFICITY:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Returns true if this type represents a relative magnitude, i.e., one that is defined with respect to another value: factors, ratios,
	 * relative risks, hazard ratios, odds ratios and mean differences. When used as the magnitude of an effect, a relative magnitude always
	 * modifies the affected parameter, instead of replacing it.
	 * @return true if this type represents a relative magnitude
	 */
	public boolean isRelative() {
		switch (this) {
			case DI_FACTOR:
			case DI_RATIO:
			case DI_RELATIVE_RISK:
			case DI_HAZARD_RATIO:
			case DI_ODDS_RATIO:
			case DI_MEAN_DIFFERENCE:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Returns true if this type represents a statistic of the uncertainty of another value (standard deviation and confidence limits),
	 * which cannot be used as the magnitude of an effect.
	 * @return true if this type represents a statistic of the uncertainty of another value
	 */
	public boolean isUncertaintyStatistic() {
		switch (this) {
			case DI_STANDARD_DEVIATION:
			case DI_LOWER_95_CONFIDENCE_LIMIT:
			case DI_UPPER_95_CONFIDENCE_LIMIT:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Returns the OSDiDataItemTypes associated to the individual IRI.
	 * @param individualIRI The individual IRI of the data item type
	 * @return The OSDiDataItemTypes associated to the individual IRI. If the individual IRI does not match any of the defined data item types, it returns DI_UNDEFINED.
	 * DI_UNDEFINED is also returned if the individual IRI does not correspond to a data item type individual.
	 */
	public static OSDiDataItemType fromIndividualIRI(IRI individualIRI) {
		if (individualIRI == null) {
			return OSDiDataItemType.DI_UNDEFINED;
		}
		final OSDiDataItemType individual = reverseDataItemType.get(individualIRI.getShortForm());
		if (individual == null) {
			return OSDiDataItemType.DI_UNDEFINED;
		}
		return individual;
	}
}