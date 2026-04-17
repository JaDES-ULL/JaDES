package es.ull.simulation.hta.osdi.ontology;

import java.util.TreeMap;

import org.semanticweb.owlapi.model.IRI;

/**
 * Individuals defined in the ontology as DataItemTypes. The enum name must be the upper case version of the individual IRI.
 * @author Iván Castilla Rodríguez
 *
 */
public enum OSDiDataItemType implements IOSDiComponentWrapper {
	CURRENCY_DOLLAR("Currency_Dollar", 0.0),
	CURRENCY_EURO("Currency_Euro", 0.0),
	CURRENCY_POUND("Currency_Pound", 0.0),
	DI_BIRTH_PREVALENCE("DI_BirthPrevalence", 1.0),
	DI_CONTINUOUS_VARIABLE("DI_Continuous_Variable", 0.0),
	DI_COUNT("DI_Count", 0.0),
	DI_FACTOR("DI_Factor", 1.0),
	DI_GENERIC_UTILITY("DI_Generic_Utility", 1.0),
	DI_INCIDENCE("DI_Incidence", 1.0),
	DI_LOWER_95_CONFIDENCE_LIMIT("DI_Lower95ConfidenceLimit", 0.0),
	DI_MEAN_DIFFERENCE("DI_MeanDifference", 0.0),
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