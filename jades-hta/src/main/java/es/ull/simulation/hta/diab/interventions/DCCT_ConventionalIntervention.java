/**
 * 
 */
package es.ull.simulation.hta.diab.interventions;

import es.ull.simulation.hta.HTAModel;
import es.ull.simulation.hta.Patient;
import es.ull.simulation.hta.interventions.Intervention;
import es.ull.simulation.hta.params.Discount;

/**
 * @author Iván Castilla
 *
 */
public class DCCT_ConventionalIntervention extends Intervention {
	private static final String NAME = "CONV";
	
	/**
	 * @param model
	 */
	public DCCT_ConventionalIntervention(HTAModel model) {
		super(model, NAME, NAME);
	}

	@Override
	public double getCostWithinPeriod(Patient pat, double initT, double endT, Discount discountRate) {
		return 0.0;
	}

	@Override
	public double getStartingCost(Patient pat, double time, Discount discountRate) {
		return 0.0;
	}

	@Override
	public double[] getAnnualizedCostWithinPeriod(Patient pat, double initT, double endT, Discount discountRate) {
		return discountRate.applyAnnualDiscount(0.0, initT, endT);
	}

	@Override
	public double getTreatmentAndFollowUpCosts(Patient pat, double initT, double endT, Discount discountRate) {
		return 0;
	}

	@Override
	public double[] getAnnualizedTreatmentAndFollowUpCosts(Patient pat, double initT, double endT,
			Discount discountRate) {
		return discountRate.applyAnnualDiscount(0.0, initT, endT);
	}

}
