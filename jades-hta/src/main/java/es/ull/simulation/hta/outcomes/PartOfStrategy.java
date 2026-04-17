/**
 * 
 */
package es.ull.simulation.hta.outcomes;

import es.ull.simulation.hta.Patient;
import es.ull.simulation.hta.params.Discount;

/**
 * @author Iván Castilla
 *
 */
public interface PartOfStrategy {
	double getCostForPeriod(Patient pat, double startT, double endT, Discount discountRate);
	double[] getAnnualizedCostForPeriod(Patient pat, double startT, double endT, Discount discountRate);
}
