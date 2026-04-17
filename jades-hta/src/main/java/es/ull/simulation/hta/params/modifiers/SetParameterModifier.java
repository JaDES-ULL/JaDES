/**
 * 
 */
package es.ull.simulation.hta.params.modifiers;

import es.ull.simulation.hta.Patient;

/**
 * @author Iván Castilla
 *
 */
public class SetParameterModifier implements ParameterModifier {
	final private String modifierParamName;
	/**
	 * 
	 */
	public SetParameterModifier(String modifierParamName) {
		this.modifierParamName = modifierParamName;
	}

	@Override
	public double getModifiedValue(Patient pat, double originalValue) {
		return pat.getSimulation().getModel().getParameterValue(modifierParamName, pat);
	}

}
