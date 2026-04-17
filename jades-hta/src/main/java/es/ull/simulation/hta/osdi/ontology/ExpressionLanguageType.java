package es.ull.simulation.hta.osdi.ontology;

import java.util.TreeMap;

import org.semanticweb.owlapi.model.IRI;

public enum ExpressionLanguageType implements IOSDiComponentWrapper {
	JAVALUATOR("Exp_Javaluator"),
	JEXL("Exp_JEXL"),
	JAVA("Exp_Java"),
	EXCEL("Exp_Excel");

	private static final TreeMap<String, ExpressionLanguageType> reverseExpressionLanguage = new TreeMap<>();
	static {
		for (ExpressionLanguageType lang : ExpressionLanguageType.values()) {
			reverseExpressionLanguage.put(lang.getShortName(), lang);
		}
	}

	/**
	 * The instance name of the expression language.
	 */
	private final String shortName;
	private ExpressionLanguageType(String individualIRI) {
		this.shortName = individualIRI;
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
	 * Returns the expression language associated to the instance name
	 * @param individualIRI The individual IRI of the expression language
	 * @return The expression language associated to the individual IRI
	 */
	public static ExpressionLanguageType fromIndividualIRI(IRI individualIRI) {
		return reverseExpressionLanguage.get(individualIRI.getShortForm());
	}
}