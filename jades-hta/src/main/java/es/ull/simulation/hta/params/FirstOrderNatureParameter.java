package es.ull.simulation.hta.params;

import java.util.Arrays;

import es.ull.simulation.hta.HTAModel;
import es.ull.simulation.hta.Patient;
import simkit.random.RandomVariate;
import simkit.random.RandomVariateFactory;

/**
 * A parameter that returns always the same value for each patient, independently on the simulation  
 * @author Iván Castilla Rodríguez
 *
 */
public class FirstOrderNatureParameter extends Parameter {
    /** The expression or probability distribution that characterizes the uncertainty on the parameter */
	private final ParameterExpression expression;
	/** All the generated values for this parameter. Index 0 is the deterministic/expected value */
	private final double[] generatedValues;

	/**
	 * 
	 */
	public FirstOrderNatureParameter(HTAModel model, String name, String description, String source, int year, ParameterGroup group, ParameterExpression expression) {
        super(model, name, description, source, year, group);
		this.expression = expression;
		if (expression == null)
			throw new IllegalArgumentException("expression cannot be null");
		generatedValues = new double[model.getNPatients()];
		Arrays.fill(generatedValues, Double.NaN);
	}
	/**
	 * 
	 */
	public FirstOrderNatureParameter(HTAModel model, String name, String description, String source, int year, ParameterGroup group, RandomVariate rnd) {
		this(model, name, description, source, year, group, new RandomParameterExpression(rnd));
	}

	public FirstOrderNatureParameter(HTAModel model, String name, String description, String source, int year, ParameterGroup group, String rndFunction, Object... params) {
		this(model, name, description, source, year, group, new RandomParameterExpression(RandomVariateFactory.getInstance(rndFunction, params)));		
	}

	@Override
	public double getValue(Patient pat) {
		final int id = pat.getIdentifier();
		if (Double.isNaN(generatedValues[id]))
			generatedValues[id] = expression.getValue(pat);
		return generatedValues[id];
	}

}
