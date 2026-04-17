package es.ull.simulation.hta.inforeceiver;

import es.ull.simulation.hta.DiseaseProgressionSimulation;
import es.ull.simulation.hta.HTAExperiment;
import es.ull.simulation.hta.HTAModel;
import es.ull.simulation.hta.Patient;
import es.ull.simulation.hta.info.PatientInfo;
import es.ull.simulation.hta.interventions.Intervention;
import es.ull.simulation.hta.params.Discount;
import es.ull.simulation.info.IPieceOfInformation;
import es.ull.simulation.info.SimulationStartStopInfo;
import es.ull.simulation.model.TimeUnit;

/**
 * A viewer to show the breakdown of annual costs 
 * 
 * @author Iván Castilla Rodríguez
 *
 */
public class BreakdownCostListener extends BasicHTAListener {
	/** The model to be simulated */
	private final HTAModel model;
	/** The discount to be applied to the costs */
	private final Discount discount;
	/** The minimum age of the patients in the simulation */
	private final int minAge;
	/** The maximum age of the patients in the simulation */
	private final int maxAge;
	/** The breakdown of costs by intervention, disease and age */
	private final double[][][] diseaseCost;
	/** The breakdown of costs directly related to the intervention, by intervention and age */
	private final double[][] interventionCost;
	/** The breakdown of costs directly related to the management of the disease, by intervention and age */
	private final double[][] managementCost;
	/** The year when the patient was last updated, by intervention and patient */
	private final double[][]lastYear;

	/**
	 * Creates a listener for the breakdown of annual costs
	 * @param exp The experiment to be analyzed
	 * @param collector The collector of the breakdown of costs for a set of simulations
	 */
	public BreakdownCostListener(HTAExperiment exp, BreakdownCostCollector collector, Discount discount) {
		super("Breakdown of costs", exp, collector);
		this.model = exp.getModel();
		final int nInterventions = model.getRegisteredInterventions().length;
		this.discount = discount;
		this.lastYear = new double[nInterventions][exp.getNPatients()];
		this.minAge = (int)model.getPopulation().getMinAge();
		this.maxAge = (int)Math.ceil(model.getPopulation().getMaxAge());
		diseaseCost = new double[nInterventions][model.getRegisteredDiseases().length][maxAge-minAge+1];
		interventionCost = new double[nInterventions][maxAge-minAge+1];
		managementCost = new double[nInterventions][maxAge-minAge+1];
		addTargetInformation(PatientInfo.class);
		addTargetInformation(SimulationStartStopInfo.class);
	}
	
	@Override
	public void infoEmited(IPieceOfInformation info) {
		if (info instanceof SimulationStartStopInfo) {
			final SimulationStartStopInfo tInfo = (SimulationStartStopInfo) info;
			if (SimulationStartStopInfo.Type.END.equals(tInfo.getType())) {
				final long ts = tInfo.getTs();
				final DiseaseProgressionSimulation simul = (DiseaseProgressionSimulation)tInfo.getSimul();
				final TimeUnit simUnit = simul.getTimeUnit();
				for (int i = 0; i < lastYear.length; i++) {
					final Patient pat = (Patient)simul.getGeneratedPatient(i);
					if (!pat.isDead()) {
						final double initYear = lastYear[pat.getnIntervention()][pat.getIdentifier()]; 
						final double endYear = pat.getSimulation().getModel().simulationTimeToYears(TimeUnit.DAY.convert(ts, simUnit));
						updateAll(pat, initYear, endYear);
					}
				}
				incrementInterventionsSimulated(simul);
			}
		}
		else if (info instanceof PatientInfo) {
			final PatientInfo pInfo = (PatientInfo) info;
			final Patient pat = pInfo.getPatient();
			final long ts = pInfo.getTs();
			final TimeUnit simUnit = pat.getSimulation().getTimeUnit();
			final double initYear = lastYear[pat.getnIntervention()][pat.getIdentifier()]; 
			final double endYear = pat.getSimulation().getModel().simulationTimeToYears(TimeUnit.DAY.convert(ts, simUnit));
			switch(pInfo.getType()) {
			case DIAGNOSIS:
				diseaseCost[pat.getnIntervention()][pat.getDisease().ordinal()][(int) endYear] += pat.getDisease().getStartingCost(pat, endYear, discount);
				break;
			case SCREEN:
				interventionCost[pat.getnIntervention()][(int) endYear] += pat.getIntervention().getStartingCost(pat, endYear, discount);
				break;
			case START_MANIF:
				diseaseCost[pat.getnIntervention()][pat.getDisease().ordinal()][(int) endYear] += pInfo.getDiseaseProgression().getStartingCost(pat, endYear, discount);
			case DEATH:
			case START:
				break;
			default:
				break;
			
			}
			if (!PatientInfo.Type.START.equals(pInfo.getType())) {
				// Update outcomes
				updateAll(pat, initYear, endYear);
			}
			// Update lastTs
			lastYear[pat.getnIntervention()][pat.getIdentifier()] = endYear;
		}
	}

	/**
	 * Updates all the pending patients when the simulation stops
	 * @param pat The patient to be updated
	 * @param initYear The year when the patient was last updated
	 * @param endYear The year when the patient is updated
	 */
	private void updateAll(Patient pat, double initYear, double endYear) {
		if (endYear > initYear) {
			update(interventionCost[pat.getnIntervention()], pat.getIntervention().getAnnualizedCostWithinPeriod(pat, initYear, endYear, discount), initYear);
			update(managementCost[pat.getnIntervention()], pat.getDisease().getAnnualizedTreatmentAndFollowUpCosts(pat, initYear, endYear, discount), initYear);
			// Assuming each patient may have at most one disease
			if (!pat.isHealthy())
				update(diseaseCost[pat.getnIntervention()][pat.getDisease().ordinal()], pat.getDisease().getAnnualizedCostWithinPeriod(pat, initYear, endYear, discount), initYear);
		}
	}

	
	/**
	 * Updates the value of this outcome for a specified period
	 * @param values An array with the values to be applied each time interval during the period
	 * @param index The first interval when it will be applied
	 */
	private void update(double[]outcome, double[] values, double initYear) {
		final int index = (int) initYear;
		for (int i = 0; i < values.length; i++) {
			outcome[i + index] += values[i];
		}
	}

	public double[][][] getDiseaseCost() {
		return diseaseCost;
	}

	public double[][] getInterventionCost() {
		return interventionCost;
	}

	public double[][] getManagementCost() {
		return managementCost;
	}
	
	@Override
	public void aggregateResultsAfterIntervention(int simulationId, Intervention intervention) {
		// Do nothing		
	}
}