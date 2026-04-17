/**
 * 
 */
package es.ull.simulation.hta.inforeceiver;

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import es.ull.simulation.hta.HTAExperiment;
import es.ull.simulation.hta.HTAModel;
import es.ull.simulation.hta.Named;
import es.ull.simulation.hta.Patient;
import es.ull.simulation.hta.interventions.Intervention;
import es.ull.simulation.hta.progression.Disease;
import es.ull.simulation.hta.progression.DiseaseProgression;

/**
 * A collector for epidemiologic evolution of the simulated patients. Shows information on prevalence, incidence or cumulative incidence; either relative or absolute (number of patients);
 * either by age or according to the time from the simulation start.
 * It can show a single result or aggregated results from various simulation experiments (by using the nExperiments parameter).
 * It shows separate results for each intervention.
 * It shows results for every manifestation. Is also shows general  and specific mortality.  
 * FIXME: Must review computation of relative measures, since they should depend on the number of persons at risk 
 * @author Iván Castilla
 *
 */
public abstract class EpidemiologicCollector extends BasicListenerCollector implements JSONProducer, TxtProducer {
	/** A brief text describing the listener */
	protected final String description;
	/** A collection of the interventions being analyzed */
	protected final Intervention[] interventions;
	/** Number of time intervals the viewer uses to split the results */
	private final int nIntervals;
	/** Results on the proportion of deaths by intervention and interval */
	protected final TreeMap<Intervention, double []> nDeaths;
	/** Results on the proportion of births (or patient spawns) by intervention and interval */
	protected final TreeMap<Intervention, double []> nBirths;	
	/** Results on the proportion of deaths by specific cause, intervention and interval */
	protected final TreeMap<Intervention, TreeMap<Named, double[]>> nDeathsByCause;
	/** Results on the proportion of patients with a specific manifestation by intervention and interval */
	protected final TreeMap<Intervention, TreeMap<DiseaseProgression, double []>> nDiseaseProgression;
	/** Results on the proportion of patients with a specific disease by intervention and interval */
	protected final TreeMap<Intervention, TreeMap<Disease, double []>> nDisease;
	/** The set of causes of death, independently of the intervention */
	private final TreeSet<Named> causesOfDeath;
	/** If true, shows number of patients; otherwise, shows ratios */
	protected final boolean absolute;
	/** If true, creates intervals depending on the current age of the patients; otherwise, creates intervals depending on the time from simulation start */
	protected final boolean byAge;
	/** The format for printing results */
	protected final String format;
	/** Minimum possible age for a patient */
	protected final int minAge;
	/** Maximum possible age for a patient */
	protected final int maxAge;
	/** Length of the intervals (in years) */
	protected final int length;

	/**
	 * Creates a epidemiologic viewer
	 * @param description A brief text describing the listener
	 * @param exp The experiment to be analyzed
	 * @param baseCase If true, collects data from a single experiment; otherwise, collects data from multiple experiments
	 * @param length Length of the intervals (in years)
	 * @param absolute If true, shows number of patients; otherwise, shows ratios
	 * @param byAge If true, creates intervals depending on the current age of the patients; otherwise, creates intervals depending on the time from simulation start
	 */
	public EpidemiologicCollector(String description, HTAExperiment exp, boolean baseCase, int length, boolean absolute, boolean byAge) {
		super(exp, baseCase);
		this.description = description;
		this.absolute = absolute;
		this.byAge = byAge;
		final int nExperiments = baseCase ? 1: exp.getNExperiments();
		this.format = (absolute && nExperiments == 1) ? "%.0f" : "%.3f";
		final HTAModel model = exp.getModel();
		this.minAge = (int)model.getPopulation().getMinAge();
		this.maxAge = (int)Math.ceil(model.getPopulation().getMaxAge());
		this.length = length;
		this.nIntervals = ((maxAge - minAge) / length) + 1;
		this.interventions = model.getRegisteredInterventions();
		nDeaths = new TreeMap<>();
		nBirths = new TreeMap<>();
		nDeathsByCause = new TreeMap<>();
		nDiseaseProgression = new TreeMap<>();
		nDisease = new TreeMap<>();
		causesOfDeath = new TreeSet<>();

		for (Intervention intervention : interventions) {
			nDeaths.put(intervention, new double[nIntervals]);
			nBirths.put(intervention, new double[nIntervals]);
			nDeathsByCause.put(intervention, new TreeMap<>());
			nDiseaseProgression.put(intervention, new TreeMap<>());
			nDisease.put(intervention, new TreeMap<>());
			for (DiseaseProgression progression : model.getRegisteredDiseaseProgressions()) {
				nDiseaseProgression.get(intervention).put(progression, new double[nIntervals]);
			}
			for (Disease disease : model.getRegisteredDiseases()) {
				nDisease.get(intervention).put(disease, new double[nIntervals]);
			}
		}
	}
	
	public int getnIntervals() {
		return nIntervals;
	}

	public int findIntervalForPatient(Patient patient, long ts) {
		return this.byAge ? (int)((patient.getAge() - this.minAge) / this.length) : (int)Math.ceil(patient.getSimulation().getModel().simulationTimeToYears(ts));
	}
	
	/**
	 * Returns the name to be used in the "name" property of the JSON object
	 * @return The name to be used in the "name" property of the JSON object
	 */
	public abstract String getJSONNameString();

	@Override
	public synchronized void collectFromListener(int simulationId, AttachableToCollector listener) {
		causesOfDeath.addAll(((EpidemiologicListener) listener).getCausesOfDeath());
	}

	@Override
	public ObjectNode produceJSON() {
		final ObjectMapper mapper = new ObjectMapper();
		final ObjectNode json = mapper.createObjectNode();
		json.put("name", getJSONNameString());
		json.put("absolute", absolute);
		json.put("by Age", byAge);
		final HTAModel model = getModel();
		final ArrayNode interventionsArray = mapper.createArrayNode();
 		for (Intervention intervention : interventions) {
			final ObjectNode jsonIntervention = mapper.createObjectNode();
			jsonIntervention.put("name", intervention.name());
			final ArrayNode jsonIntervals = mapper.createArrayNode();
			jsonIntervention.set(byAge ? "ages" : "years", jsonIntervals);
			final ArrayNode jsonBirths = mapper.createArrayNode();
			jsonIntervention.set("births", jsonBirths);
			final ArrayNode jsonDeaths = mapper.createArrayNode();
			jsonIntervention.set("deaths by cause", jsonDeaths);
			final ArrayNode jsonDeathsByAllCauses = mapper.createArrayNode();
			ObjectNode jsonDeathObject = mapper.createObjectNode();
			jsonDeathObject.set("all", jsonDeathsByAllCauses);
			jsonDeaths.add(jsonDeathObject);
			final Map<Named, ArrayNode> jsonDeathsByCauses = new TreeMap<>();
			for (final Named cause : causesOfDeath) {
				jsonDeathsByCauses.put(cause, mapper.createArrayNode());
				jsonDeathObject = mapper.createObjectNode();
				jsonDeathObject.set(cause.name(), jsonDeathsByAllCauses);
				jsonDeaths.add(jsonDeathObject);			
			}
			final ArrayNode[] jsonDiseaseResults = new ArrayNode[model.getRegisteredDiseases().length];
			for (Disease disease : model.getRegisteredDiseases()) {
				jsonDiseaseResults[disease.ordinal()] = mapper.createArrayNode();
				jsonIntervention.set(disease.name(), jsonDiseaseResults[disease.ordinal()]);
			}
			final ArrayNode[] jsonDiseaseProgressionResults = new ArrayNode[model.getRegisteredDiseaseProgressions().length];
			for (DiseaseProgression progression : model.getRegisteredDiseaseProgressions()) {
				jsonDiseaseProgressionResults[progression.ordinal()] = mapper.createArrayNode();
				jsonIntervention.set(progression.name(), jsonDiseaseProgressionResults[progression.ordinal()]);
			}

			for (int year = 0; year < nIntervals; year++) {
				jsonIntervals.add(byAge ? (length * year) + minAge : (length * year));
				jsonBirths.add(nBirths.get(intervention)[year]);
				jsonDeathsByAllCauses.add(nDeaths.get(intervention)[year]);
				for (final Named cause : causesOfDeath) {
					if (!nDeathsByCause.get(intervention).containsKey(cause))
						jsonDeathsByCauses.get(cause).add(0);
					else 
						jsonDeathsByCauses.get(cause).add(nDeathsByCause.get(intervention).get(cause)[year]);
				}
				for (Disease disease : model.getRegisteredDiseases()) {
					jsonDiseaseResults[disease.ordinal()].add(nDisease.get(intervention).get(disease)[year]);
				}
				for (DiseaseProgression progression : model.getRegisteredDiseaseProgressions()) {
					jsonDiseaseProgressionResults[progression.ordinal()].add(nDiseaseProgression.get(intervention).get(progression)[year]);
				}
			}

			interventionsArray.add(jsonIntervention);
		}
		json.set("results", interventionsArray);
		return json;
	}

	@Override
	public String getStrHeader() {
		final HTAModel model = getModel();
		final StringBuilder str = new StringBuilder(description).append(absolute ? " ABS" : " REL").append(byAge ? " AGE" : "");
		str.append(System.lineSeparator()).append(byAge ? "AGE" : "YEAR");
		for (Intervention intervention : interventions) {
			final String name = intervention.name();
			if (byAge)
				str.append("\t" + name + "_N");
			str.append("\t" + name + "_DEATH");
			for (final Named cause : causesOfDeath) {
				str.append("\t" + name + "_DEATH_" + cause);				
			}
			for (Disease dis : model.getRegisteredDiseases()) {
				str.append("\t" + name + "_").append(dis.name());
			}
			for (DiseaseProgression comp : model.getRegisteredDiseaseProgressions()) {
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
		for (int year = 0; year < nIntervals; year++) {
			str.append(byAge ? (length * year) + minAge : (length *year));
			for (Intervention intervention : interventions) {
				if (byAge) {
					str.append("\t").append(String.format(Locale.US, format, nBirths.get(intervention)[year]));					
				}
				str.append("\t").append(String.format(Locale.US, format, nDeaths.get(intervention)[year]));
				for (final Named cause : causesOfDeath) {
					if (!nDeathsByCause.get(intervention).containsKey(cause))
						str.append("\t0");
					else
						str.append("\t").append(String.format(Locale.US, format, nDeathsByCause.get(intervention).get(cause)[year]));
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
	
	@Override
	public String toString() {
		return getStrHeader() + getFormattedResult();			
	}

	@Override
	public BasicHTAListener createListener(HTAExperiment exp) {
		return new EpidemiologicListener(exp, this);
	}
	
	/**
	 * Included just for checking whether it would be useful for new listeners...
	 * @param minAge
	 * @param maxAge
	 * @param gap
	 * @param fillToLifetime
	 * @return
	 */
	@Deprecated 
	public static double[][] buildAgesInterval(int minAge, int maxAge, int gap, boolean fillToLifetime) {
		int nGroups = (maxAge - minAge) / gap;
		if (fillToLifetime)
			nGroups++;
		final double[][] ageIntervals = new double[nGroups][2];
		for (int i = 0; i < nGroups; i++) {
			ageIntervals[i][0] = minAge + gap * i;
			ageIntervals[i][1] = minAge + gap * (i + 1);
		}
		if (fillToLifetime)
			ageIntervals[nGroups - 1][1] = maxAge;
		return ageIntervals;
	}
}

