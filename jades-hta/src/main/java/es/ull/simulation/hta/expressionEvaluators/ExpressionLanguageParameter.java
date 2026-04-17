package es.ull.simulation.hta.expressionEvaluators;

import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlException;
import org.apache.commons.jexl3.JexlExpression;
import org.slf4j.Logger;

import es.ull.simulation.hta.HTAModel;
import es.ull.simulation.hta.Patient;
import es.ull.simulation.hta.params.Parameter;
import es.ull.simulation.hta.params.ParameterGroup;

public class ExpressionLanguageParameter extends Parameter implements ExpressionLanguageInterface {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionLanguageParameter.class);
	private final JexlExpression exprToEvaluate;

    public ExpressionLanguageParameter(HTAModel model, String name, String description, String source, int year, ParameterGroup type, String expression) {
        super(model, name, description, source, year, type);
		this.exprToEvaluate = JEXL.createExpression(expression);
    }

    @Override
    public double getValue(Patient pat) {
		final JexlContext jc = new ExpressionLanguagePatient(pat);
        double value = 0.0;
		try {
			value = (double)exprToEvaluate.evaluate(jc);
		} catch (JexlException ex) {
			log.error("Error evaluating expression: " + ex.getMessage(), ex);
		}
        return value;
    }
}
