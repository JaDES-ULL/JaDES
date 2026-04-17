/**
 * 
 */
package es.ull.simulation.hta.params.modifiers;

import es.ull.simulation.hta.Patient;

/**
 * @author Iván Castilla
 *
 */
public class SetConstantParameterModifier implements ParameterModifier {
	final private double value;
	/**
	 * 
	 */
	public SetConstantParameterModifier(double value) {
		this.value = value;
	}

	@Override
	public double getModifiedValue(Patient pat, double originalValue) {
		return value;
	}

}
