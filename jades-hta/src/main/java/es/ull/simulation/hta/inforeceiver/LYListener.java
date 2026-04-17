/**
 * 
 */
package es.ull.simulation.hta.inforeceiver;

import es.ull.simulation.hta.HTAExperiment;
import es.ull.simulation.hta.Patient;
import es.ull.simulation.hta.info.PatientInfo;
import es.ull.simulation.hta.params.Discount;

/**
 * @author Iván Castilla Rodríguez
 *
 */
public class LYListener extends OutcomeListener {
	
	/**
	 * @param description
	 */
	public LYListener(HTAExperiment exp, LYCollector collector, Discount discountRate) {
		this(exp, collector, "Life expectancy listener", discountRate);
	}

	/**
	 * @param description
	 */
	public LYListener(HTAExperiment exp, LYCollector collector, String description, Discount discountRate) {
		super(exp, collector, description, discountRate);
	}

	@Override
	public double getPeriodValue(Patient pat, double initAge, double endAge) {
		return discountRate.applyDiscount(1.0, initAge, endAge);
	}

	@Override
	public double getOneTimeValue(Patient pat, PatientInfo pInfo, double ts) {
		return 0.0;
	}
}
