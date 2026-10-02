package es.ull.simulation.hta.progression.calculator;

import es.ull.simulation.hta.HTAModel;
import es.ull.simulation.hta.Patient;
import es.ull.simulation.hta.outcomes.ActiveItemsCombiner;
import es.ull.simulation.hta.outcomes.PayoffKeys;
import es.ull.simulation.hta.params.StandardParameter;
import es.ull.simulation.hta.progression.DiseaseProgression;

/**
 * Combines the increased mortality rates and life expectancy reductions of the disease progressions currently active for a patient,
 * according to the combination methods defined in the experiment. Shared by the death submodels so that all of them apply the same rule.
 * The death submodels must apply the combined increased mortality rate first and then subtract the combined life expectancy reduction.
 * @param increasedMortalityRate The combined increased mortality rate (rate ratio; 1.0 if no progression increases mortality)
 * @param lifeExpectancyReduction The combined life expectancy reduction, in years (0.0 if no progression reduces life expectancy)
 * @author Iván Castilla Rodríguez
 */
public record MortalityModifiers(double increasedMortalityRate, double lifeExpectancyReduction) {

	/**
	 * Combines the increased mortality rates and life expectancy reductions of the disease progressions currently active for a patient
	 * @param pat A patient
	 * @return The combined increased mortality rate and life expectancy reduction
	 */
	public static MortalityModifiers of(Patient pat) {
		final HTAModel htaModel = pat.getSimulation().getModel();
		final ActiveItemsCombiner<String> imr = new ActiveItemsCombiner<>(htaModel.getMortalityRateCombinationMethod(), 1.0);
		final ActiveItemsCombiner<String> ler = new ActiveItemsCombiner<>(htaModel.getLifeExpectancyReductionCombinationMethod(), 0.0);
		for (final DiseaseProgression state : pat.getState()) {
			imr.add(PayoffKeys.getKey(htaModel, state, StandardParameter.INCREASED_MORTALITY_RATE), state.getUsedParameterValue(StandardParameter.INCREASED_MORTALITY_RATE, pat));
			ler.add(PayoffKeys.getKey(htaModel, state, StandardParameter.LIFE_EXPECTANCY_REDUCTION), state.getUsedParameterValue(StandardParameter.LIFE_EXPECTANCY_REDUCTION, pat));
		}
		return new MortalityModifiers(imr.getValue(), ler.getValue());
	}
}
