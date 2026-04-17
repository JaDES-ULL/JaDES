/**
 * 
 */
package es.ull.simulation.hta.pbdmodel;

import es.ull.simulation.hta.HTAModel;
import es.ull.simulation.hta.interventions.ScreeningIntervention;
import es.ull.simulation.hta.params.StandardParameter;
import es.ull.simulation.hta.params.modifiers.ParameterModifier;
import es.ull.simulation.hta.params.modifiers.SetConstantParameterModifier;
import es.ull.simulation.hta.progression.DiseaseProgression;
import simkit.random.RandomVariateFactory;

/**
 * @author Iván Castilla Rodríguez
 *
 */
public class PBDNewbornScreening extends ScreeningIntervention {
	private final static double C_TEST = 0.89;

	/**
	 * @param secParams
	 */
	public PBDNewbornScreening(HTAModel model) {
		super(model, "#PBD_InterventionScreening", "Basic screening");
	}

	@Override
	public void createParameters() {
		addUsedParameter(StandardParameter.ONSET_COST, "", "", 2013, C_TEST, RandomVariateFactory.getInstance("UniformVariate", 0.5, 2.5));
		addUsedParameter(StandardParameter.SENSITIVITY, "", "", 1.0);
		addUsedParameter(StandardParameter.SPECIFICITY, "", "", 0.999935);
		final ParameterModifier modifier = new SetConstantParameterModifier(0.0); 
		for (DiseaseProgression manif : model.getRegisteredDiseaseProgressions())
			model.addParameterModifier(StandardParameter.PROPORTION.createName(manif), this, modifier);
	}

}
