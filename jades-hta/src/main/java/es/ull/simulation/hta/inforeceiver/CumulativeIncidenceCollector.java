/**
 * 
 */
package es.ull.simulation.hta.inforeceiver;

import es.ull.simulation.hta.HTAExperiment;
import es.ull.simulation.hta.HTAModel;
import es.ull.simulation.hta.Named;
import es.ull.simulation.hta.inforeceiver.EpidemiologicListener.AggregatedResult;
import es.ull.simulation.hta.interventions.Intervention;
import es.ull.simulation.hta.progression.Disease;
import es.ull.simulation.hta.progression.DiseaseProgression;

/**
 * A collector for the cumulative incidence of mortality and manifestation of the simulated patients, which can be shown either relative or absolute (number of patients);
 * either by age or according to the time from the simulation start.
 * It can show a single result or aggregated results from various simulation experiments (by using the nExperiments parameter).
 * It shows separate results for each intervention.
 * It shows results for every manifestation. Is also shows general and manifestation-specific mortality.  
 * @author Iván Castilla Rodríguez
 *
 */
public class CumulativeIncidenceCollector extends EpidemiologicCollector {
	/** Number of experiments to be collected together */
	protected final int nExperiments;

	/**
	 * Creates a cumulative incidence viewer
	 * @param exp The experiment to be analyzed
	 * @param baseCase If true, collects data from a single experiment; otherwise, collects data from multiple experiments
	 * @param length Length of the intervals (in years)
	 * @param absolute If true, shows number of patients; otherwise, shows ratios
	 * @param byAge If true, creates intervals depending on the current age of the patients; otherwise, creates intervals depending on the time from simulation start
	 */
	public CumulativeIncidenceCollector(HTAExperiment exp, boolean baseCase, int length, boolean absolute, boolean byAge) {
		super("Cumulative incidence", exp, baseCase, length, absolute, byAge);
		this.nExperiments = baseCase ? 1: exp.getNExperiments();
	}

	@Override
	public String getJSONNameString() {
		return "cumulative incidence";
	}
	
	@Override
	public synchronized void collectFromListener(int simulationId, AttachableToCollector listener) {
		super.collectFromListener(simulationId, listener);
		final HTAModel model = getModel();
		final AggregatedResult[] aggregatedResults = ((EpidemiologicListener) listener).getResults();		
		for (Intervention intervention : interventions) {
			for (int year = 0; year < getnIntervals(); year++) {
				nBirths.get(intervention)[year] += aggregatedResults[intervention.ordinal()].getNBirths()[year];
				nDeaths.get(intervention)[year] += aggregatedResults[intervention.ordinal()].getNDeaths()[year];
				for (final Named cause : aggregatedResults[intervention.ordinal()].getNDeathsByCause().keySet()) {
					if (!nDeathsByCause.get(intervention).containsKey(cause)) {
						nDeathsByCause.get(intervention).put(cause, new double[getnIntervals()]);
					}
					nDeathsByCause.get(intervention).get(cause)[year] += aggregatedResults[intervention.ordinal()].getNDeathsByCause().get(cause)[year];
				}
				for (Disease disease : model.getRegisteredDiseases()) {
					nDisease.get(intervention).get(disease)[year] += aggregatedResults[intervention.ordinal()].getNDiseases().get(disease)[year];
				}
				for (DiseaseProgression progression : model.getRegisteredDiseaseProgressions()) {
					nDiseaseProgression.get(intervention).get(progression)[year] += aggregatedResults[intervention.ordinal()].getNDiseaseProgressions().get(progression)[year];
				}
			}
		}
	}

	@Override
	public void incrementExperimentsPerformed() {
		super.incrementExperimentsPerformed();
		final HTAModel model = getModel();
		if (getExperimentsPerformed() == nExperiments) {
			if (absolute) {
				for (Intervention intervention : interventions) {
					int year = 0;
					nBirths.get(intervention)[year] /= (double)nExperiments;					
					nDeaths.get(intervention)[year] /= (double)nExperiments;
					for (final Named cause : nDeathsByCause.keySet()) {
						nDeathsByCause.get(cause).get(intervention)[year] /= (double)nExperiments;
					}
					for (Disease disease : model.getRegisteredDiseases()) {
						nDisease.get(intervention).get(disease)[year] /= (double)nExperiments;
					}
					for (DiseaseProgression progression : model.getRegisteredDiseaseProgressions()) {
						nDiseaseProgression.get(intervention).get(progression)[year] /= (double)nExperiments;
					}
					year++;
					for (; year < getnIntervals(); year++) {
						nBirths.get(intervention)[year] = nBirths.get(intervention)[year - 1] + nBirths.get(intervention)[year] / (double)nExperiments;					
						nDeaths.get(intervention)[year] = nDeaths.get(intervention)[year - 1] + nDeaths.get(intervention)[year] / (double)nExperiments;
						for (final Named cause : nDeathsByCause.keySet()) {
							nDeathsByCause.get(cause).get(intervention)[year] = nDeathsByCause.get(cause).get(intervention)[year - 1] + nDeathsByCause.get(cause).get(intervention)[year] / (double)nExperiments;
						}
						for (Disease disease : model.getRegisteredDiseases()) {
							nDisease.get(intervention).get(disease)[year] = nDisease.get(intervention).get(disease)[year - 1] + nDisease.get(intervention).get(disease)[year] / (double)nExperiments;
						}
						for (DiseaseProgression progression : model.getRegisteredDiseaseProgressions()) {
							nDiseaseProgression.get(intervention).get(progression)[year] = nDiseaseProgression.get(intervention).get(progression)[year - 1] + nDiseaseProgression.get(intervention).get(progression)[year] / (double)nExperiments;
						}
					}
				}
			}
			else {
				for (Intervention intervention : interventions) {
					int year = 0;
					nBirths.get(intervention)[year] /= (double)nExperiments;
					double alive = nBirths.get(intervention)[year];
					nDeaths.get(intervention)[year] /= ((double)nExperiments * alive);
					for (final Named cause : nDeathsByCause.keySet()) {
						nDeathsByCause.get(cause).get(intervention)[year] /= ((double)nExperiments * alive);
					}
					for (Disease disease : model.getRegisteredDiseases()) {
						nDisease.get(intervention).get(disease)[year] /= ((double)nExperiments * alive);
					}
					for (DiseaseProgression progression : model.getRegisteredDiseaseProgressions()) {
						nDiseaseProgression.get(intervention).get(progression)[year] /= ((double)nExperiments * alive);
					}
					year++;
					for (; year < getnIntervals(); year++) {
						alive += nBirths.get(intervention)[year] / (double)nExperiments;
						nBirths.get(intervention)[year] = nBirths.get(intervention)[year - 1] + nBirths.get(intervention)[year] / ((double)nExperiments * alive);					
						nDeaths.get(intervention)[year] = nDeaths.get(intervention)[year - 1] + nDeaths.get(intervention)[year] / ((double)nExperiments * alive);
						for (final Named cause : nDeathsByCause.keySet()) {
							nDeathsByCause.get(cause).get(intervention)[year] = nDeathsByCause.get(cause).get(intervention)[year - 1] + nDeathsByCause.get(cause).get(intervention)[year] / ((double)nExperiments * alive);
						}
						for (Disease disease : model.getRegisteredDiseases()) {
							nDisease.get(intervention).get(disease)[year] = nDisease.get(intervention).get(disease)[year - 1] + nDisease.get(intervention).get(disease)[year] / ((double)nExperiments * alive);
						}
						for (DiseaseProgression progression : model.getRegisteredDiseaseProgressions()) {
							nDiseaseProgression.get(intervention).get(progression)[year] = nDiseaseProgression.get(intervention).get(progression)[year - 1] + nDiseaseProgression.get(intervention).get(progression)[year] / ((double)nExperiments * alive);
						}
					}
				}
			}
		}
	}
}

