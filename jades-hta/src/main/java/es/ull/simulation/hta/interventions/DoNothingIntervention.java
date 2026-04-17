/**
 * 
 */
package es.ull.simulation.hta.interventions;

import es.ull.simulation.hta.HTAModel;

/**
 * @author Iván Castilla	
 *
 */
public class DoNothingIntervention extends Intervention {

	/**
	 * @param secParams
	 */
	public DoNothingIntervention(HTAModel model) {
		super(model, "NONE", "Do nothing");
	}

}
