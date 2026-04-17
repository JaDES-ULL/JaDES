/**
 * 
 */
package es.ull.simulation.hta.params.modifiers;

import es.ull.simulation.hta.Patient;

/**
 * @author Iván Castilla
 *
 */
public class DiffParameterModifier implements ParameterModifier {
	final private String modifierParamName;

	/**
	 * 
	 */
	public DiffParameterModifier(String modifierParamName) {
		this.modifierParamName = modifierParamName;
	}

	@Override
	public double getModifiedValue(Patient pat, double originalValue) {
		return originalValue - pat.getSimulation().getModel().getParameterValue(modifierParamName, pat);
	}

}
