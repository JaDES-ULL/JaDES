/**
 * 
 */
package es.ull.simulation.hta.inforeceiver;

import java.util.HashMap;
import java.util.Locale;

import es.ull.simulation.hta.HTAExperiment;
import es.ull.simulation.hta.HTAModel;
import es.ull.simulation.hta.Named;
import es.ull.simulation.hta.inforeceiver.EpidemiologicListener.AggregatedResult;
import es.ull.simulation.hta.interventions.Intervention;
import es.ull.simulation.hta.progression.Disease;
import es.ull.simulation.hta.progression.DiseaseProgression;

/**
 * A viewer for the prevalence of manifestation of the simulated patients, which can be shown either relative or absolute (number of patients);
 * either by age or according to the time from the simulation start.
 * It can show a single result or aggregated results from various simulation experiments (by using the nExperiments parameter).
 * It shows separate results for each intervention.
 * It shows results for every manifestation. 
 * No results are shown for mortality
 * @author Iván Castilla Rodríguez
 *
 */
public class PrevalenceCollector extends EpidemiologicCollector {
	/** Number of experiments to be collected together */
	protected final int nExperiments;

	/**
	 * Creates a prevalence viewer
	 * @param exp The experiment to be analyzed
	 * @param baseCase If true, collects data from a single experiment; otherwise, collects data from multiple experiments
	 * @param length Length of the intervals (in years)
	 * @param absolute If true, shows number of patients; otherwise, shows ratios
	 * @param byAge If true, creates intervals depending on the current age of the patients; otherwise, creates intervals depending on the time from simulation start
	 */
	public PrevalenceCollector(HTAExperiment exp, boolean baseCase, int length, boolean absolute, boolean byAge) {
		super("Prevalence", exp, baseCase, length, absolute, byAge);
		this.nExperiments = baseCase ? 1: exp.getNExperiments();
	}

	@Override
	public String getJSONNameString() {
		return "prevalence";
	}

	@Override
	public synchronized void collectFromListener(int simulationId, AttachableToCollector listener) {
		super.collectFromListener(simulationId, listener);
		final AggregatedResult[] aggregatedResults = ((EpidemiologicListener) listener).getResults();
		final HTAModel model = getModel();

		for (Intervention intervention : interventions) {
			final AggregatedResult interventionResults = aggregatedResults[intervention.ordinal()];
			// First process base time interval
			double accDeaths = interventionResults.getNDeaths()[0];
			double accPatients = interventionResults.getNBirths()[0];
			final double []accManifestation = new double[model.getRegisteredDiseaseProgressions().length];
			final double []accDisease = new double[model.getRegisteredDiseases().length];
			final HashMap<Named, Double> accDeathsByCause = new HashMap<>();
			for (final Named cause : interventionResults.getNDeathsByCause().keySet()) {
				accDeathsByCause.put(cause, (double)interventionResults.getNDeathsByCause().get(cause)[0]);
				if (!nDeathsByCause.get(intervention).containsKey(cause)) {
					nDeathsByCause.get(intervention).put(cause, new double[getnIntervals()]);
				}
				nDeathsByCause.get(intervention).get(cause)[0] += accDeathsByCause.get(cause);
			}
			nDeaths.get(intervention)[0] += accDeaths;
			nBirths.get(intervention)[0] += accPatients;
			for (Disease disease : model.getRegisteredDiseases()) {
				accDisease[disease.ordinal()] = interventionResults.getNDiseases().get(disease)[0];
				nDisease.get(intervention).get(disease)[0] += accDisease[disease.ordinal()];
			}
			for (DiseaseProgression progression : model.getRegisteredDiseaseProgressions()) {
				accManifestation[progression.ordinal()] = interventionResults.getNDiseaseProgressions().get(progression)[0];
				nDiseaseProgression.get(intervention).get(progression)[0] += accManifestation[progression.ordinal()];
			}
			// Now process the rest of time intervals
			for (int i = 1; i < getnIntervals(); i++) {
				accPatients += interventionResults.getNBirths()[i];
				accDeaths += interventionResults.getNDeaths()[i];
				nDeaths.get(intervention)[i] += accDeaths;
				nBirths.get(intervention)[i] += accPatients;
				for (final Named cause : interventionResults.getNDeathsByCause().keySet()) {
					accDeathsByCause.put(cause, accDeathsByCause.get(cause) + interventionResults.getNDeathsByCause().get(cause)[i]);
					nDeathsByCause.get(intervention).get(cause)[i] += accDeathsByCause.get(cause);
				}
				for (Disease disease : model.getRegisteredDiseases()) {
					accDisease[disease.ordinal()] += interventionResults.getNDiseases().get(disease)[i] - interventionResults.getNEndDiseases().get(disease)[i-1];
					nDisease.get(intervention).get(disease)[i] += accDisease[disease.ordinal()];
				}
				for (DiseaseProgression progression : model.getRegisteredDiseaseProgressions()) {
					accManifestation[progression.ordinal()] += interventionResults.getNDiseaseProgressions().get(progression)[i] - interventionResults.getNEndDiseaseProgressions().get(progression)[i-1];
					nDiseaseProgression.get(intervention).get(progression)[i] += accManifestation[progression.ordinal()];
				}
			}
		}
	}

	@Override
	public void incrementExperimentsPerformed() {
		super.incrementExperimentsPerformed();
		final HTAModel model = getModel();
		if (getExperimentsPerformed() == nExperiments) {
			for (Intervention intervention : interventions) {
				double coef = (absolute) ? nExperiments : nBirths.get(intervention)[0];
	
				nBirths.get(intervention)[0] /= coef;					
				for (Disease disease : model.getRegisteredDiseases()) {
					nDisease.get(intervention).get(disease)[0] /= coef;
				}
				for (DiseaseProgression progression : model.getRegisteredDiseaseProgressions()) {
					nDiseaseProgression.get(intervention).get(progression)[0] /= coef;
				}
				
				for (int year = 1; year < getnIntervals(); year++) {
					// Computes people at risk as number of accumulated births minus the number of accumulated deaths in the previous interval 
					coef = (absolute) ? nExperiments : (nBirths.get(intervention)[year] - nDeaths.get(intervention)[year - 1]);
					nBirths.get(intervention)[year] /= coef;					
					for (Disease disease : model.getRegisteredDiseases()) {
						nDisease.get(intervention).get(disease)[year] /= coef;
					}
					for (DiseaseProgression progression : model.getRegisteredDiseaseProgressions()) {
						nDiseaseProgression.get(intervention).get(progression)[year] /= coef;
					}
				}
			}				
		}
	}

	@Override
	public String getStrHeader() {
		final StringBuilder str = new StringBuilder(description).append(absolute ? " ABS" : " REL").append(byAge ? " AGE" : "");
		str.append(System.lineSeparator()).append(byAge ? "AGE" : "YEAR");
		for (int i = 0; i < interventions.length; i++) {
			final String name = interventions[i].name();
			if (byAge)
				str.append("\t" + name + "_N");
			for (Disease dis : getModel().getRegisteredDiseases()) {
				str.append("\t" + name + "_").append(dis.name());
			}
			for (DiseaseProgression comp : getModel().getRegisteredDiseaseProgressions()) {
				str.append("\t" + name + "_").append(comp.name());
			}
		}
		str.append(System.lineSeparator());
		return str.toString();
	}
	
	@Override
	public String getFormattedResult() {
		final StringBuilder str = new StringBuilder();
		final HTAModel model = getModel();
		for (int year = 0; year < getnIntervals(); year++) {
			str.append(byAge ? (length * year) + minAge : (length *year));
			for (Intervention intervention : interventions) {
				if (byAge) {
					str.append("\t").append(String.format(Locale.US, format, nBirths.get(intervention)[year]));					
				}
					for (Disease disease : model.getRegisteredDiseases()) {
					str.append("\t").append(String.format(Locale.US, format, nDisease.get(intervention).get(disease)[year]));
				}
				for (DiseaseProgression progression : model.getRegisteredDiseaseProgressions()) {
					str.append("\t").append(String.format(Locale.US, format, nDiseaseProgression.get(intervention).get(progression)[year]));
				}
			}
			str.append(System.lineSeparator());
		}
		return str.toString();
	}
	
}
