/**
 * 
 */
package es.ull.simulation.hta.inforeceiver;

import es.ull.simulation.hta.HTAExperiment;
import es.ull.simulation.hta.Patient;
import es.ull.simulation.hta.info.PatientInfo;
import es.ull.simulation.hta.outcomes.DisutilityCombinationMethod;
import es.ull.simulation.hta.params.Discount;

/**
 * @author Iván Castilla Rodríguez
 *
 */
public class QALYListener extends OutcomeListener {
	protected final DisutilityCombinationMethod method;

	/**
	 * @param description
	 */
	public QALYListener(HTAExperiment exp, QALYCollector collector, Discount discountRate, DisutilityCombinationMethod method) {
		this(exp, collector, "Quality adjusted life expectancy listener", discountRate, method);
	}

	/**
	 * @param description
	 */
	public QALYListener(HTAExperiment exp, QALYCollector collector, String description, Discount discountRate, DisutilityCombinationMethod method) {
		super(exp, collector, description, discountRate);
		this.method = method;
	}

	@Override
	public double getPeriodValue(Patient pat, double initYear, double endYear) {
		return discountRate.applyDiscount(pat.getUtilityValue(method), initYear, endYear);
	}

	@Override
	public double getOneTimeValue(Patient pat, PatientInfo pInfo, double ts) {
		double value = 0.0;
		switch(pInfo.getType()) {
			case DIAGNOSIS:
				value = -pat.getDisease().getStartingDisutility(pat);
				break;
			case SCREEN:
				break;
			case START_MANIF:
				value = -pInfo.getDiseaseProgression().getStartingDisutility(pat);
				break;
			case DEATH:
				break;
			case START:
				break;
			default:
				break;			
		}
		return discountRate.applyPunctualDiscount(value, ts);
	}
}
