package es.ull.simulation.hta.expressionEvaluators;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;

public interface ExpressionLanguageInterface {
	public static final JexlEngine JEXL = new JexlBuilder().create();

}
