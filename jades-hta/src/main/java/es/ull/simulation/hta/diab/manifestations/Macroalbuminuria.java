/**
 * 
 */
package es.ull.simulation.hta.diab.manifestations;

import es.ull.simulation.hta.HTAModel;
import es.ull.simulation.hta.params.StandardParameter;
import es.ull.simulation.hta.progression.DiseaseProgression;
import es.ull.simulation.hta.progression.Disease;
import es.ull.simulation.utils.Statistics;
import simkit.random.RandomVariateFactory;

/**
 * @author Iván Castilla
 *
 */
public class Macroalbuminuria extends DiseaseProgression {
	/** Utility (avg, SD) from either Bagust and Beale; or Sullivan */
	private static final double[] DU = new double[] {0.048, (0.091 - 0.005) / 3.92};
	public static final String NAME = "ALB2";

	/**
	 * @param model
	 * @param disease
	 */
	public Macroalbuminuria(HTAModel model, Disease disease) {
		super(model, NAME, "Macroalbuminuria", disease, Type.CHRONIC_MANIFESTATION);
	}

	@Override
	public void createParameters() {
		addUsedParameter(StandardParameter.ANNUAL_COST, "", "Assumption", 2021, 0.0, RandomVariateFactory.getInstance("ConstantVariate", 0.0));
		final double[] paramsDu = Statistics.betaParametersFromNormal(DU[0], DU[1]);
		addUsedParameter(StandardParameter.ANNUAL_DISUTILITY, "Disutility of " + getDescription(), "Bagust and Beale", DU[0], RandomVariateFactory.getInstance("BetaVariate", paramsDu[0], paramsDu[1]));
		addUsedParameter(StandardParameter.INCREASED_MORTALITY_RATE, "severe proteinuria", 
				"https://doi.org/10.2337/diacare.28.3.617", 
				2.23, RandomVariateFactory.getInstance("RRFromLnCIVariate", 2.23, 1.11, 4.49, 1));
	}

}
