package es.ull.simulation.hta.inforeceiver;

import java.util.TreeMap;
import java.util.TreeSet;

import es.ull.simulation.hta.DiseaseProgressionSimulation;
import es.ull.simulation.hta.HTAExperiment;
import es.ull.simulation.hta.HTAModel;
import es.ull.simulation.hta.Named;
import es.ull.simulation.hta.Patient;
import es.ull.simulation.hta.info.PatientInfo;
import es.ull.simulation.hta.interventions.Intervention;
import es.ull.simulation.hta.progression.Disease;
import es.ull.simulation.hta.progression.DiseaseProgression;
import es.ull.simulation.info.IPieceOfInformation;
import es.ull.simulation.info.SimulationStartStopInfo;

/**
 * A listener that collects incidence or prevalence from a single simulation 
 * @author Iván Castilla Rodríguez
 *
 */
public class EpidemiologicListener extends BasicHTAListener {
	/** The model to be simulated */
	private final HTAModel model;
	/** Results on the proportion of births (or spawns) of patients by intervention and time interval */
	private final TreeMap<Intervention, int []> nBirths;
	/** Results on the proportion of deaths by intervention and time interval */
	private final TreeMap<Intervention, int []> nDeaths;
	/** Results on the proportion of deaths by specific cause, intevention and time interval */
	private final TreeMap<Intervention, TreeMap<Named, int[]>> nDeathsByCause;
	/** The set of causes of death, independently of the intervention */
	private final TreeSet<Named> causesOfDeath;
	/** Results on the proportion of patients with a specific manifestation by intervention and time interval */
	private final TreeMap<Intervention, TreeMap<DiseaseProgression, int []>> nDiseaseProgression;
	/** Results on the proportion of patients who finish suffering a specific manifestation by intervention and time interval */
	private final TreeMap<Intervention, TreeMap<DiseaseProgression, int []>> nEndDiseaseProgression;
	/** Results on the proportion of patients with a specific disease by intervention and time interval */
	private final TreeMap<Intervention, TreeMap<Disease, int []>> nDisease;
	/** Results on the proportion of patients who finish suffering a specific disease by intervention and time interval */
	private final TreeMap<Intervention, TreeMap<Disease, int []>> nEndDisease;
	/** For each intervention, disease and patient, true if a patient already has the disease */ 
	private final TreeMap<Intervention, TreeMap<Disease, boolean []>> patientDisease;
	/** The final aggregated results */
	private final AggregatedResult[] results;

	/**
	 * Creates a listener for epidemiologic information
	 * @param exp The experiment to be analyzed
	 * @param epidemiologicCollector The collector that will store the results
	 */
	public EpidemiologicListener(HTAExperiment exp, EpidemiologicCollector epidemiologicCollector) {
		super("Viewer for incidence or prevalence", exp, epidemiologicCollector);
		this.model = exp.getModel();
		this.results = new AggregatedResult[interventions.length];
		this.causesOfDeath = new TreeSet<>();
		this.nDeaths = new TreeMap<>();
		this.nBirths = new TreeMap<>();
		this.nDeathsByCause = new TreeMap<>();			
		this.nDiseaseProgression = new TreeMap<>();
		this.nEndDiseaseProgression = new TreeMap<>();
		this.nDisease = new TreeMap<>();
		this.nEndDisease = new TreeMap<>();
		this.patientDisease = new TreeMap<>();

		for (Intervention intervention : interventions) {
			this.nDeaths.put(intervention, new int[epidemiologicCollector.getnIntervals()]);
			this.nBirths.put(intervention, new int[epidemiologicCollector.getnIntervals()]);
			this.nDeathsByCause.put(intervention, new TreeMap<>());
			this.nDiseaseProgression.put(intervention, new TreeMap<>());
			this.nEndDiseaseProgression.put(intervention, new TreeMap<>());
			this.nDisease.put(intervention, new TreeMap<>());
			this.nEndDisease.put(intervention, new TreeMap<>());
			this.patientDisease.put(intervention, new TreeMap<>());
			for (DiseaseProgression progression : model.getRegisteredDiseaseProgressions()) {
				this.nDiseaseProgression.get(intervention).put(progression, new int[epidemiologicCollector.getnIntervals()]);
				this.nEndDiseaseProgression.get(intervention).put(progression, new int[epidemiologicCollector.getnIntervals()]);
			}
			for (Disease disease : model.getRegisteredDiseases()) {
				this.nDisease.get(intervention).put(disease, new int[epidemiologicCollector.getnIntervals()]);
				this.nEndDisease.get(intervention).put(disease, new int[epidemiologicCollector.getnIntervals()]);
				this.patientDisease.get(intervention).put(disease, new boolean[epidemiologicCollector.getNPatients()]);
			}
		}
		addTargetInformation(PatientInfo.class);
		addTargetInformation(SimulationStartStopInfo.class);
	}

	@Override
	public void infoEmited(IPieceOfInformation info) {
		if (info instanceof SimulationStartStopInfo) {
			final SimulationStartStopInfo tInfo = (SimulationStartStopInfo) info;
			if (SimulationStartStopInfo.Type.END.equals(tInfo.getType())) {
				final DiseaseProgressionSimulation simul = (DiseaseProgressionSimulation)tInfo.getSimul();
				incrementInterventionsSimulated(simul);
			}
		} else if (info instanceof PatientInfo) {
			EpidemiologicCollector epidemiologicCollector = (EpidemiologicCollector) getCollector();
			final PatientInfo pInfo = (PatientInfo) info;
			final Patient pat = (Patient)pInfo.getPatient();
			final Intervention intervention = pat.getIntervention();
			final int interval = epidemiologicCollector.findIntervalForPatient(pat, pInfo.getTs());
			switch(pInfo.getType()) {
				case START:
					nBirths.get(intervention)[interval]++;
					if (!patientDisease.get(intervention).get(pat.getDisease())[pInfo.getPatient().getIdentifier()]) {
						patientDisease.get(intervention).get(pat.getDisease())[pInfo.getPatient().getIdentifier()] = true;
						nDisease.get(intervention).get(pat.getDisease())[interval]++;
					}
					break;
				case START_MANIF:
					nDiseaseProgression.get(intervention).get(pInfo.getDiseaseProgression())[interval]++;
					if (!patientDisease.get(intervention).get(pInfo.getDiseaseProgression().getDisease())[pInfo.getPatient().getIdentifier()]) {
						patientDisease.get(intervention).get(pInfo.getDiseaseProgression().getDisease())[pInfo.getPatient().getIdentifier()] = true;
						nDisease.get(intervention).get(pInfo.getDiseaseProgression().getDisease())[interval]++;
					}
					break;
				case END_MANIF:
					nEndDiseaseProgression.get(intervention).get(pInfo.getDiseaseProgression())[interval]++;
					break;
				case DEATH:
					nDeaths.get(intervention)[interval]++;
					final Named cause = pInfo.getCause();
					if (cause != null) {
						if (!nDeathsByCause.get(intervention).containsKey(cause)) {
							nDeathsByCause.get(intervention).put(cause, new int[epidemiologicCollector.getnIntervals()]);
							causesOfDeath.add(cause);
						}
						nDeathsByCause.get(intervention).get(cause)[interval]++;
					}
					// Removes the disease and manifestations of the patient from the total account 
					for (DiseaseProgression progression : pat.getState())
						nEndDiseaseProgression.get(intervention).get(progression)[interval]++;
					nEndDisease.get(intervention).get(pat.getDisease())[interval]++;
					break;
				default:
					break;
			}
		}
	}

	/**
	 * Returns the aggregated final results
	 * @return The aggregated final results
	 */
	public AggregatedResult[] getResults() {
		return results;
	}

	/**
	 * Returns the set of causes of death
	 * @return The set of causes of death
	 */
	public TreeSet<Named> getCausesOfDeath() {
		return causesOfDeath;
	}

	@Override
	public void aggregateResultsAfterIntervention(int simulationId, Intervention intervention) {
		results[intervention.ordinal()] = new AggregatedResult(simulationId, intervention, nDeaths.get(intervention), nBirths.get(intervention), nDeathsByCause.get(intervention), 
			nDiseaseProgression.get(intervention), nDisease.get(intervention), nEndDiseaseProgression.get(intervention), nEndDisease.get(intervention));
	}


	public static class AggregatedResult {
		private final int simulationId;
		private final Intervention intervention;
		/** Results on the proportion of deaths by interval */
		private final int[] nDeaths;
		/** Results on the proportion of births (or patient spawns) by interval */
		protected final int[] nBirths;	
		/** Results on the proportion of deaths by specific cause and interval */
		protected final TreeMap<Named, int[]> nDeathsByCause;
		/** Results on the proportion of patients with a specific manifestation by interval */
		protected final TreeMap<DiseaseProgression, int[]> nDiseaseProgression;
		/** Results on the proportion of patients with a specific disease by intervention and interval */
		protected final TreeMap<Disease, int[]> nDisease;
		protected final TreeMap<DiseaseProgression, int[]> nEndDiseaseProgression;
		protected final TreeMap<Disease, int[]> nEndDisease;



		public AggregatedResult(int simulationId, Intervention intervention, int[] nDeaths, int[] nBirths, TreeMap<Named, int[]> nDeathsByCause, TreeMap<DiseaseProgression, int[]> nDiseaseProgression, TreeMap<Disease, int[]> nDisease,
				TreeMap<DiseaseProgression, int[]> nEndDiseaseProgression, TreeMap<Disease, int[]> nEndDisease) {
			this.simulationId = simulationId;
			this.intervention = intervention;
			this.nDeaths = nDeaths;
			this.nBirths = nBirths;
			this.nDeathsByCause = nDeathsByCause;
			this.nDiseaseProgression = nDiseaseProgression;
			this.nDisease = nDisease;
			this.nEndDiseaseProgression = nEndDiseaseProgression;
			this.nEndDisease = nEndDisease;
		}

		public int getSimulationId() {
			return simulationId;
		}

		public Intervention getIntervention() {
			return intervention;
		}

		public int[] getNDeaths() {
			return nDeaths;
		}

		public int[] getNBirths() {
			return nBirths;
		}

		public TreeMap<Named, int[]> getNDeathsByCause() {
			return nDeathsByCause;
		}

		public TreeMap<DiseaseProgression, int[]> getNDiseaseProgressions() {
			return nDiseaseProgression;
		}

		public TreeMap<Disease, int[]> getNDiseases() {
			return nDisease;
		}

		public TreeMap<DiseaseProgression, int[]> getNEndDiseaseProgressions() {
			return nEndDiseaseProgression;
		}

		public TreeMap<Disease, int[]> getNEndDiseases() {
			return nEndDisease;
		}
	}
}