/**
 * 
 */
package es.ull.simulation.hta.inforeceiver;

import es.ull.simulation.hta.HTAExperiment;
import es.ull.simulation.hta.Patient;
import es.ull.simulation.hta.info.PatientInfo;
import es.ull.simulation.hta.params.Discount;

/**
 * A listener that collects the cost per patient and intervention.
 * @author Iván Castilla Rodríguez
 *
 */
public class CostListener extends OutcomeListener {

	/**
	 * @param description
	 */
	public CostListener(HTAExperiment exp, CostCollector collector, Discount discountRate) {
		this(exp, collector, "Cost listener", discountRate);
	}

	/**
	 * @param description
	 */
	public CostListener(HTAExperiment exp, CostCollector collector, String description, Discount discountRate) {
		super(exp, collector, description, discountRate);
	}

	@Override
	public double getPeriodValue(Patient pat, double initT, double endT) {
		return pat.getIntervention().getCostWithinPeriod(pat, initT, endT, discountRate) +
			pat.getDisease().getCostWithinPeriod(pat, initT, endT, discountRate);
	}

	@Override
	public double getOneTimeValue(Patient pat, PatientInfo pInfo, double ts) {
		double value = 0.0;
		switch(pInfo.getType()) {
			case DIAGNOSIS:
				value = pat.getDisease().getStartingCost(pat, ts, discountRate);
				break;
			case SCREEN:
			value = pat.getIntervention().getStartingCost(pat, ts, discountRate);
				break;
			case START_MANIF:
			value = pInfo.getDiseaseProgression().getStartingCost(pat, ts, discountRate);
			case DEATH:
			case START:
				break;
			default:
				break;			
		}
		return value;
	}

}
