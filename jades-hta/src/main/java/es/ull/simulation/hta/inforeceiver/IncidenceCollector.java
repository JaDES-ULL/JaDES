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
 * A viewer for the incidence of mortality and manifestation of the simulated patients, which can be shown either relative or absolute (number of patients);
 * either by age or according to the time from the simulation start.
 * Incidence is he number of new cases divided by the persons at risk during an interval.
 * TODO Relative incidence is not completely accurate since the cohort at risk does not exclude exclusive manifestations
 * It can show a single result or aggregated results from various simulation experiments (by using the nExperiments parameter).
 * It shows separate results for each intervention.
 * It shows results for every manifestation. Is also shows general and manifestation-specific mortality.  
 * @author Iván Castilla Rodríguez
 *
 */
public class IncidenceCollector extends EpidemiologicCollector {
	/** A coefficient used to compute relative incidences. Computed only once. */
	private final double coefPatients;
	/** A coefficient used to compute average incidences (total / number of experiments). Computed only once. */
	private final double coefExperiments;

	/**
	 * Creates an incidence viewer
	 * @param exp The experiment to be analyzed
	 * @param baseCase If true, collects data from a single experiment; otherwise, collects data from multiple experiments
	 * @param length Length of the intervals (in years)
	 * @param absolute If true, shows number of patients; otherwise, shows ratios
	 * @param byAge If true, creates intervals depending on the current age of the patients; otherwise, creates intervals depending on the time from simulation start
	 */
	public IncidenceCollector(HTAExperiment exp, boolean baseCase, int length, boolean absolute, boolean byAge) {
		super("Incidence", exp, baseCase, length, absolute, byAge);
		this.coefPatients = 1.0 / (absolute ? 1.0 : (double)exp.getNPatients());
		this.coefExperiments = 1.0 / (double)(baseCase ? 1: exp.getNExperiments());
	}

	@Override
	public String getJSONNameString() {
		return "incidence";
	}
	
	@Override
	public synchronized void collectFromListener(int simulationId, AttachableToCollector listener) {
		super.collectFromListener(simulationId, listener);
		final HTAModel model = getModel();
		final AggregatedResult[] aggregatedResults = ((EpidemiologicListener) listener).getResults();
			
		for (Intervention intervention : interventions) {
			final AggregatedResult interventionResults = aggregatedResults[intervention.ordinal()];
			double alive = interventionResults.getNBirths()[0];
			nBirths.get(intervention)[0] += alive * coefPatients;
			double coef = 1.0 / (absolute ? 1.0 : alive);		
			// Should always be 0
			nDeaths.get(intervention)[0] += interventionResults.getNDeaths()[0] * coef;
			for (final Named cause : interventionResults.getNDeathsByCause().keySet()) {
				if (!nDeathsByCause.get(intervention).containsKey(cause)) {
					nDeathsByCause.get(intervention).put(cause, new double[getnIntervals()]);
				}
				nDeathsByCause.get(intervention).get(cause)[0] += interventionResults.getNDeathsByCause().get(cause)[0] * coef;
			}
			final double [] nAtRiskDisease = new double[model.getRegisteredDiseases().length]; 
			for (Disease disease : model.getRegisteredDiseases()) {
				nDisease.get(intervention).get(disease)[0] += interventionResults.getNDiseases().get(disease)[0] * coef;
				// Patients at risk of developing the disease in the next time interval are those who are alive and has not the disease yet
				nAtRiskDisease[disease.ordinal()] = alive - interventionResults.getNDiseases().get(disease)[0]; 
			}
			final double [] nAtRiskManifestation = new double[model.getRegisteredDiseaseProgressions().length]; 
			for (DiseaseProgression progression : model.getRegisteredDiseaseProgressions()) {
				nDiseaseProgression.get(intervention).get(progression)[0] += interventionResults.getNDiseaseProgressions().get(progression)[0] * coef;
				// Patients at risk of developing the manifestation in the next time interval are those who are alive and has not the manifestation yet, in case the manifestation is chronic
				nAtRiskManifestation[progression.ordinal()] = alive - (DiseaseProgression.Type.CHRONIC_MANIFESTATION.equals(progression.getType()) ? interventionResults.getNDiseaseProgressions().get(progression)[0] : 0); 
			}
			// Rest of intervals
			for (int year = 1; year < getnIntervals(); year++) {
				alive += interventionResults.getNBirths()[year] - interventionResults.getNDeaths()[year - 1];
				coef = 1.0 / (absolute ? 1.0 : alive);
				if (interventionResults.getNBirths()[year] != 0)
					nBirths.get(intervention)[year] += interventionResults.getNBirths()[year] * coefPatients;
				if (interventionResults.getNDeaths()[year] != 0)
					nDeaths.get(intervention)[year] +=  interventionResults.getNDeaths()[year] * coef;
					for (final Named cause : interventionResults.getNDeathsByCause().keySet()) {
						if (interventionResults.getNDeathsByCause().get(cause)[year] != 0)
							nDeathsByCause.get(intervention).get(cause)[year] += interventionResults.getNDeathsByCause().get(cause)[year] * coef;
				}
				for (Disease disease : model.getRegisteredDiseases()) {
					if (interventionResults.getNDiseases().get(disease)[year] != 0) {
						nDisease.get(intervention).get(disease)[year] += interventionResults.getNDiseases().get(disease)[year] / (absolute ? 1.0 : nAtRiskDisease[disease.ordinal()]);
						// People who start suffering a disease must be excluded from the people at risk of suffering so
						nAtRiskDisease[disease.ordinal()] -= interventionResults.getNDiseases().get(disease)[year];
					}
					// Disease is supposed to be "chronic"; hence, the "end disease" group includes deceased individuals.
					nAtRiskDisease[disease.ordinal()] -= interventionResults.getNEndDiseases().get(disease)[year];
				}
				for (DiseaseProgression progression : model.getRegisteredDiseaseProgressions()) {
					if (interventionResults.getNDiseaseProgressions().get(progression)[year] != 0) {
						nDiseaseProgression.get(intervention).get(progression)[year] += interventionResults.getNDiseaseProgressions().get(progression)[year] / (absolute ? 1.0 : nAtRiskManifestation[progression.ordinal()]);
						// People who start suffering a chronic manifestation must be excluded from the people at risk of suffering so
						if (DiseaseProgression.Type.CHRONIC_MANIFESTATION.equals(progression.getType()))
							nAtRiskManifestation[progression.ordinal()] -= interventionResults.getNDiseaseProgressions().get(progression)[year];					
					}
					// The account of "end manifestation" includes the decease individuals with the manifestation. Should be always 0 for acute ones
					nAtRiskManifestation[progression.ordinal()] -= interventionResults.getNEndDiseaseProgressions().get(progression)[year];
				}
			}
		}
	}

	@Override
	public void incrementExperimentsPerformed() {
		super.incrementExperimentsPerformed();
		final HTAModel model = getModel();
		if (getExperimentsPerformed() == getNExperiments()) {
			for (int year = 0; year < getnIntervals(); year++) {
				for (Intervention intervention : interventions) {
					nBirths.get(intervention)[year] *= coefExperiments;					
					nDeaths.get(intervention)[year] *= coefExperiments;
					for (final Named cause : nDeathsByCause.keySet()) {
						nDeathsByCause.get(cause).get(intervention)[year] *= coefExperiments;
					}
					for (Disease disease : model.getRegisteredDiseases()) {
						nDisease.get(intervention).get(disease)[year] *= coefExperiments;
					}
					for (DiseaseProgression progression : model.getRegisteredDiseaseProgressions()) {
						nDiseaseProgression.get(intervention).get(progression)[year] *= coefExperiments;
					}
				}
			}
		}
	}
}

