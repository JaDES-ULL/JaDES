package es.ull.simulation.hta.osdi.des;

import es.ull.simulation.condition.AbstractCondition;
import es.ull.simulation.hta.Patient;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper;
import es.ull.simulation.hta.progression.DiseaseProgression;
import es.ull.simulation.hta.progression.DiseaseProgressionPathway;
import es.ull.simulation.hta.progression.calculator.ConstantTimeToEventCalculator;
import es.ull.simulation.hta.progression.calculator.TimeToEventCalculator;
import es.ull.simulation.model.TimeUnit;
import es.ull.simulation.utils.Statistics;

/**
 * The pathways that generate the episodes of an acute manifestation that is the target of the interventions (decision D1 of docs/osdi-1.3-diseno.md):
 * every patient suffers an episode at time 0, and the next ones come from the frequency of the manifestation (hasFrequency) or, if it has none, from
 * its risk. A disease progression takes the lowest time to event among its suitable pathways, so the first episode happens at time 0, and the next
 * ones are scheduled after each episode from the rest of pathways.
 */
public final class TargetEpisodePathways {

	private TargetEpisodePathways() {
	}

	/**
	 * Creates the pathway of the episode at time 0: a time to event of 0 for the patients that have not suffered any episode yet.
	 * @param model The model
	 * @param target The acute manifestation that the interventions target
	 * @return The pathway, which is already added to the manifestation
	 */
	public static DiseaseProgressionPathway createFirstEpisodePathway(OSDiDESModel model, DiseaseProgression target) {
		final AbstractCondition<DiseaseProgressionPathway.ConditionInformation> noEpisodeYet = new AbstractCondition<>() {
			@Override
			public boolean check(DiseaseProgressionPathway.ConditionInformation info) {
				return info.getPatient().getNDiseaseProgressions(target) == 0;
			}
		};
		return new DiseaseProgressionPathway(model, "FIRST_EPISODE_" + target.name(), "Episode of " + target.name() + " at time 0, since it is the target of the interventions",
			target, new ConstantTimeToEventCalculator(0.0), noEpisodeYet);
	}

	/**
	 * Creates the pathway of the next episodes from the frequency of the manifestation: the time between episodes is exponential, with a rate equal
	 * to the frequency (episodes per year), modified by the effects of the intervention.
	 * @param model The model
	 * @param target The acute manifestation that the interventions target
	 * @param frequency The frequency of the episodes (hasFrequency), which is a parameter of the model
	 * @return The pathway, which is already added to the manifestation
	 */
	public static DiseaseProgressionPathway createFrequencyPathway(OSDiDESModel model, DiseaseProgression target, ParameterWrapper frequency) {
		final String frequencyName = frequency.getShortName();
		final TimeToEventCalculator calculator = new TimeToEventCalculator() {
			@Override
			public double getTimeToEvent(Patient pat) {
				final double rate = model.getParameterValue(frequencyName, pat);
				return Statistics.getAnnualBasedTimeToEventFromRate(rate, Math.log(pat.getRandomNumberForIncidence(target)), 1.0);
			}

			@Override
			public TimeUnit getTimeUnit() {
				return TimeUnit.YEAR;
			}
		};
		return new DiseaseProgressionPathway(model, "FREQUENCY_" + target.name(), "Episodes of " + target.name() + " with the frequency " + frequencyName, target, calculator,
			new AbstractCondition<>() {
				@Override
				public boolean check(DiseaseProgressionPathway.ConditionInformation info) {
					return true;
				}
			});
	}
}
