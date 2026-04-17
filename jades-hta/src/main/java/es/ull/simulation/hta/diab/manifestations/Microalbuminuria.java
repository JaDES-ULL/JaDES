/**
 * 
 */
package es.ull.simulation.hta.diab.manifestations;

import es.ull.simulation.hta.HTAModel;
import es.ull.simulation.hta.params.StandardParameter;
import es.ull.simulation.hta.progression.Disease;
import es.ull.simulation.hta.progression.DiseaseProgression;
import simkit.random.RandomVariateFactory;

/**
 * @author Iván Castilla
 *
 */
public class Microalbuminuria extends DiseaseProgression {
	public static final String NAME = "ALB1";

	/**
	 * @param model
	 * @param disease
	 */
	public Microalbuminuria(HTAModel model, Disease disease) {
		super(model, NAME, "Microalbuminuria", disease, Type.CHRONIC_MANIFESTATION);
	}

	@Override
	public void createParameters() {
		addUsedParameter(StandardParameter.ANNUAL_COST, "", "Assumption", 2021, 0.0, RandomVariateFactory.getInstance("ConstantVariate", 0.0));
		addUsedParameter(StandardParameter.ANNUAL_DISUTILITY, "Disutility of " + getDescription(), "Assumption", 0.0, RandomVariateFactory.getInstance("ConstantVariate", 0.0));
	}

}
