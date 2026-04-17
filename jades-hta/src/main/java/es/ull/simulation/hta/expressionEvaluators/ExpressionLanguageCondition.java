/**
 * 
 */
package es.ull.simulation.hta.expressionEvaluators;

import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlException;
import org.apache.commons.jexl3.JexlExpression;
import org.slf4j.Logger;

import es.ull.simulation.condition.AbstractCondition;
import es.ull.simulation.hta.progression.DiseaseProgressionPathway;

/**
 * @author Iván Castilla
 *
 */
public class ExpressionLanguageCondition extends AbstractCondition<DiseaseProgressionPathway.ConditionInformation> implements ExpressionLanguageInterface {
	private static final Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionLanguageCondition.class);
	private final JexlExpression exprToEvaluate;
	/**
	 * 
	 */
	public ExpressionLanguageCondition(String expression) {
		exprToEvaluate = JEXL.createExpression(expression);
	}
	
	@Override
	public boolean check(DiseaseProgressionPathway.ConditionInformation info) {
		final JexlContext jc = new ExpressionLanguagePatient(info.getPatient());
		boolean result = false;
		try {
			result = (boolean) exprToEvaluate.evaluate(jc);
		} catch (JexlException ex) {
			log.error("Error evaluating expression: " + ex.getMessage(), ex);
		}
		return result;
	}

}
