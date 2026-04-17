/**
 * 
 */
package es.ull.simulation.hta.inforeceiver;

import java.util.ArrayList;
import java.util.TreeMap;

import es.ull.simulation.hta.DiseaseProgressionSimulation;
import es.ull.simulation.hta.HTAExperiment;
import es.ull.simulation.hta.HTAModel;
import es.ull.simulation.hta.Patient;
import es.ull.simulation.hta.interventions.Intervention;
import es.ull.simulation.hta.progression.DiseaseProgression;
import es.ull.simulation.info.IPieceOfInformation;
import es.ull.simulation.info.SimulationStartStopInfo;
import es.ull.simulation.utils.Statistics;

/**
 * A listener to capture the time until a chronic manifestation onsets. Time can be 0 (if the patient starts with the manifestation),
 * Long.MAX_VALUE if the simulation finishes and the patient never developed the manifestation, or any positive value among both extremes
 * (it is expected to be always inferior to the maximum age of the patient)
 * @author Iván Castilla
 *
 */
public class TimeToDiseaseProgressionListener extends BasicHTAListener {
	/** Inner structure to store time to chronic manifestations. For each mapped manifestation contains an array with t-uples <intervention, patient> */
	private final TreeMap<DiseaseProgression, long[][]> timeToEvents;
	/** Inner structure to store number of acute events per patient. For each mapped manifestation contains an array with t-uples <intervention, patient> */
	private final TreeMap<DiseaseProgression, int[][]> nEvents;
	/** For each intervention, number of patients that develop each chronic manifestation, including
	 * those who started the simulated with such manifestation */  
	private final int [][] prevalence;
	/** For each intervention, number of patients that develop each manifestation during their 
	 * simulated lifetime */  
	private final int [][] incidence;
	/** Available manifestations in the simulation */
	private final DiseaseProgression[] availableManifestations;
    /** The model this component belongs to */
    private final HTAModel model;
	/** The aggregated results by intervention */
	private final AggregatedResult[] results;

	/**
	 * 
	 * @param exp The experiment to be analyzed
	 */
	public TimeToDiseaseProgressionListener(HTAExperiment exp, TimeToDiseaseProgressionCollector collector) {
		super("Standard patient viewer", exp, collector);
		this.model = exp.getModel();
		this.availableManifestations = model.getRegisteredDiseaseProgressions();
		prevalence = new int[interventions.length][availableManifestations.length];
		incidence = new int[interventions.length][availableManifestations.length];
		timeToEvents = new TreeMap<>();
		nEvents = new TreeMap<>();
		for (DiseaseProgression manif : availableManifestations) {
			if (DiseaseProgression.Type.CHRONIC_MANIFESTATION.equals(manif.getType()))
				timeToEvents.put(manif, new long[interventions.length][nPatients]);
			else
				nEvents.put(manif, new int[interventions.length][nPatients]);
		}
		results = new AggregatedResult[interventions.length];
		addTargetInformation(SimulationStartStopInfo.class);
	}
	
	@Override
	public void infoEmited(IPieceOfInformation info) {
		if (info instanceof SimulationStartStopInfo) {
			final SimulationStartStopInfo tInfo = (SimulationStartStopInfo) info;
			if (SimulationStartStopInfo.Type.END.equals(tInfo.getType())) {
				final DiseaseProgressionSimulation simul = (DiseaseProgressionSimulation)tInfo.getSimul();
				for (Patient pat : simul.getGeneratedPatients()) {
					updatePatient(pat);
				}
				incrementInterventionsSimulated(simul);
			}
		}
	}
	
	/**
	 * Updates the global results with the information of a patient
	 * @param pat A patient
	 */
	private void updatePatient(Patient pat) {
		final int nIntervention = pat.getnIntervention(); 
		// Check all the complications
		for (int i = 0; i < availableManifestations.length; i++) {
			final DiseaseProgression manif = availableManifestations[i];
			final long time = pat.getTimeToDiseaseProgression(manif);
			if (DiseaseProgression.Type.CHRONIC_MANIFESTATION.equals(manif.getType())) {
				timeToEvents.get(manif)[nIntervention][pat.getIdentifier()] = time;
				if (time == 0) {
					prevalence[nIntervention][i]++;
				}
				else if (Long.MAX_VALUE != time){
					prevalence[nIntervention][i]++;
					incidence[nIntervention][i]++;
				}
			}
			else {
				nEvents.get(manif)[nIntervention][pat.getIdentifier()] = pat.getNDiseaseProgressions(manif);
				incidence[nIntervention][i] += nEvents.get(manif)[nIntervention][pat.getIdentifier()];
				
			}
		}
	}
	
	/**
	 * Returns a list with those values corresponding to valid times to an event, i.e., different to 0 and Long.MAX_VALUE 
	 * @param values Original times to event 
	 * @return  a list containing only those values corresponding to valid times to an event
	 */
	private ArrayList<Long> getValidValues(long[] values) {
		final ArrayList<Long> validValues = new ArrayList<>();
		for (long val : values) {
			if (Long.MAX_VALUE != val && val != 0) {
				validValues.add(val);
			}
		}
		return validValues;
	}

	/**
	 * @return the results
	 */
	public AggregatedResult[] getResults() {
		return results;
	}

	@Override
	public void aggregateResultsAfterIntervention(int simulationId, Intervention intervention) {
		final int nIntervention = intervention.ordinal();
		final TreeMap<DiseaseProgression, ResultsPerDiseaseProgression> resultsPerDiseaseProgression = new TreeMap<>();
		for (int i = 0; i < availableManifestations.length; i++) {
			if (DiseaseProgression.Type.CHRONIC_MANIFESTATION.equals(availableManifestations[i].getType())) {
				final ArrayList<Long> validValues = getValidValues(timeToEvents.get(availableManifestations[i])[nIntervention]);
				if (validValues.size() > 0) {
					final double rawAvg = Statistics.average(validValues);
					final double avgTimeToEvent = (validValues.size() == 0) ? Double.NaN : model.simulationTimeToYears(rawAvg);
					final double[] ci = Statistics.normal95CI(rawAvg, Statistics.stdDev(validValues, rawAvg), validValues.size());
					ci[0] = model.simulationTimeToYears(ci[0]);
					ci[1] = model.simulationTimeToYears(ci[1]);
					resultsPerDiseaseProgression.put(availableManifestations[i], new ResultsPerChronicManifestation(availableManifestations[i], incidence[nIntervention][i], prevalence[nIntervention][i], avgTimeToEvent, ci));
				}
				else {
					resultsPerDiseaseProgression.put(availableManifestations[i], new ResultsPerChronicManifestation(availableManifestations[i], incidence[nIntervention][i], prevalence[nIntervention][i], Double.NaN, new double[] {Double.NaN, Double.NaN}));
				}
			}
			else if (DiseaseProgression.Type.ACUTE_MANIFESTATION.equals(availableManifestations[i].getType())) {
				final double avgIncidence = (double)incidence[nIntervention][i] / getnPatients();
				final int[] cip = Statistics.getPercentile95CI(nEvents.get(availableManifestations[i])[nIntervention]);
				resultsPerDiseaseProgression.put(availableManifestations[i], new ResultsPerAcuteEvent(availableManifestations[i], avgIncidence, cip));
			}
		}

		results[nIntervention] = new AggregatedResult(simulationId, intervention, resultsPerDiseaseProgression);
	}

	public static class AggregatedResult {
        private final int simulationId;
        private final Intervention intervention;
		private final TreeMap<DiseaseProgression, ResultsPerDiseaseProgression> resultsPerDiseaseProgression;

		public AggregatedResult(int simulationId, Intervention intervention, TreeMap<DiseaseProgression, ResultsPerDiseaseProgression> resultsPerDiseaseProgression) {
			this.simulationId = simulationId;
			this.intervention = intervention;
			this.resultsPerDiseaseProgression = resultsPerDiseaseProgression;
		}

		public int getSimulationId() {
			return simulationId;
		}

		public Intervention getIntervention() {
			return intervention;
		}

		public TreeMap<DiseaseProgression, ResultsPerDiseaseProgression> getResultsPerDiseaseProgression() {
			return resultsPerDiseaseProgression;
		}
	}

	public static class ResultsPerDiseaseProgression {
		private final DiseaseProgression diseaseProgression;

		public ResultsPerDiseaseProgression(DiseaseProgression diseaseProgression) {
			this.diseaseProgression = diseaseProgression;
		}

		public DiseaseProgression getDiseaseProgression() {
			return diseaseProgression;
		}
	}

	public static class ResultsPerChronicManifestation extends ResultsPerDiseaseProgression {
		private final int incidence;
		private final int prevalence;
		private final double avgTimeToEvent;
		private final double[] ciTimeToEvent;

		public ResultsPerChronicManifestation(DiseaseProgression diseaseProgression, int incidence, int prevalence, double avgTimeToEvent, double[] ciTimeToEvent) {
			super(diseaseProgression);
			this.incidence = incidence;
			this.prevalence = prevalence;
			this.avgTimeToEvent = avgTimeToEvent;
			this.ciTimeToEvent = ciTimeToEvent;
		}

		public int getIncidence() {
			return incidence;
		}
		public int getPrevalence() {
			return prevalence;
		}

		public double getAvgTimeToEvent() {
			return avgTimeToEvent;
		}

		public double[] getCiTimeToEvent() {
			return ciTimeToEvent;
		}
	}

	public static class ResultsPerAcuteEvent extends ResultsPerDiseaseProgression {
		private final double avgNEvents;
		private final int[] ciEvents;

		public ResultsPerAcuteEvent(DiseaseProgression diseaseProgression, double avgNEvents, int[] ciEvents) {
			super(diseaseProgression);
			this.avgNEvents = avgNEvents;
			this.ciEvents = ciEvents;
		}

		public double getAvgNEvents() {
			return avgNEvents;
		}

		public int[] getCiEvents() {
			return ciEvents;
		}
	}
}
