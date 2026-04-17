package es.ull.simulation.hta.expressionEvaluators;

import com.fathzer.soft.javaluator.DoubleEvaluator;

import es.ull.simulation.hta.HTAModel;
import es.ull.simulation.hta.Patient;
import es.ull.simulation.hta.params.Parameter;
import es.ull.simulation.hta.params.ParameterGroup;

public class JavaluatorParameter extends Parameter {
    final private static DoubleEvaluator evaluator = new DoubleEvaluator();
    private final String expression;

    public JavaluatorParameter(HTAModel model, String name, String description, String source, int year, ParameterGroup type, String expression) {
        super(model, name, description, source, year, type);
		this.expression = expression;
    }

    @Override
    public double getValue(Patient pat) {
        final JavaluatorPatient jc = new JavaluatorPatient(pat);
        return evaluator.evaluate(expression, jc);
    }
}
