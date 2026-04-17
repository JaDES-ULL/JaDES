package es.ull.simulation.hta.inforeceiver;

import es.ull.simulation.hta.DiseaseProgressionSimulation;
import es.ull.simulation.hta.HTAExperiment;
import es.ull.simulation.hta.Patient;
import es.ull.simulation.hta.info.PatientInfo;
import es.ull.simulation.hta.interventions.Intervention;
import es.ull.simulation.hta.params.Discount;
import es.ull.simulation.info.IPieceOfInformation;
import es.ull.simulation.info.SimulationStartStopInfo;
import es.ull.simulation.model.TimeUnit;

public class BudgetImpactListener extends BasicHTAListener {
	private final int timeHorizon;
	private final double[][] cost;
	private final double[][] lastYear;
	private boolean finish;

	/**
	 * Creates a new listener for the budget impact
	 * @param exp The experiment to be analyzed
	 * @param budgetImpactCollector The collector of the budget impact
	 */
	public BudgetImpactListener(HTAExperiment exp, BudgetImpactCollector budgetImpactCollector) {
		super("Budget impact", exp, budgetImpactCollector);
		final int nInterventions = exp.getModel().getRegisteredInterventions().length;
		this.lastYear = new double[nInterventions][exp.getNPatients()];
		this.cost = new double[nInterventions][budgetImpactCollector.getTimeHorizon()+1];
		this.timeHorizon = budgetImpactCollector.getTimeHorizon();
		finish = false;
		addTargetInformation(PatientInfo.class);
		addTargetInformation(SimulationStartStopInfo.class);
	}

	public double[][] getCost() {
		return cost;
	}

	@Override
	public void infoEmited(IPieceOfInformation info) {
		if (!finish) {
			if (info instanceof SimulationStartStopInfo) {
				final SimulationStartStopInfo tInfo = (SimulationStartStopInfo) info;
				if (SimulationStartStopInfo.Type.END.equals(tInfo.getType())) {
					final long ts = tInfo.getTs();
					final DiseaseProgressionSimulation simul = (DiseaseProgressionSimulation)tInfo.getSimul();
					final TimeUnit simUnit = simul.getTimeUnit();
					final double endYear = simul.getModel().simulationTimeToYears(TimeUnit.DAY.convert(ts, simUnit));
					for (int i = 0; i < lastYear.length; i++) {
						final Patient pat = (Patient)simul.getGeneratedPatient(i);
						if (!pat.isDead()) {
							final double initYear = lastYear[pat.getnIntervention()][pat.getIdentifier()]; 
							if (endYear > initYear) {
								update(pat.getnIntervention(), pat.getDisease().getAnnualizedCostWithinPeriod(pat, initYear, endYear, Discount.ZERO_DISCOUNT), initYear);
								update(pat.getnIntervention(), pat.getIntervention().getAnnualizedCostWithinPeriod(pat, initYear, endYear, Discount.ZERO_DISCOUNT), initYear);
							}						
						}
					}
				}
			}
			else if (info instanceof PatientInfo) {
				final PatientInfo pInfo = (PatientInfo) info;
				final DiseaseProgressionSimulation simul = (DiseaseProgressionSimulation)pInfo.getSimul();
				final double endYear = simul.getModel().simulationTimeToYears(TimeUnit.DAY.convert(pInfo.getTs(), simul.getTimeUnit()));
				if (endYear > timeHorizon) {
					for (int i = 0; i < getnPatients(); i++) {
						final Patient pat = simul.getGeneratedPatient(i);
						if (!pat.isDead()) {
							final double initYear = lastYear[pat.getnIntervention()][pat.getIdentifier()]; 
							if (timeHorizon > initYear) {
								update(pat.getnIntervention(), pat.getDisease().getAnnualizedCostWithinPeriod(pat, initYear, timeHorizon, Discount.ZERO_DISCOUNT), initYear);
								update(pat.getnIntervention(), pat.getIntervention().getAnnualizedCostWithinPeriod(pat, initYear, timeHorizon, Discount.ZERO_DISCOUNT), initYear);
							}
						}
					}
					finish = true;
				}
				else {
					final Patient pat = pInfo.getPatient();
					final double initYear = lastYear[pat.getnIntervention()][pat.getIdentifier()]; 
					switch(pInfo.getType()) {
					case DIAGNOSIS:
						update(pat.getnIntervention(), pat.getDisease().getStartingCost(pat, endYear, Discount.ZERO_DISCOUNT), endYear);
						break;
					case SCREEN:
						update(pat.getnIntervention(), pat.getIntervention().getStartingCost(pat, endYear, Discount.ZERO_DISCOUNT), endYear);
						break;
					case START_MANIF:
						update(pat.getnIntervention(), pInfo.getDiseaseProgression().getStartingCost(pat, endYear, Discount.ZERO_DISCOUNT), endYear);
					case DEATH:
					case START:
						break;
					default:
						break;
					
					}
					if (!PatientInfo.Type.START.equals(pInfo.getType())) {
						// Update outcomes
						if (endYear > initYear) {
							update(pat.getnIntervention(), pat.getDisease().getAnnualizedCostWithinPeriod(pat, initYear, timeHorizon, Discount.ZERO_DISCOUNT), initYear);
							update(pat.getnIntervention(), pat.getIntervention().getAnnualizedCostWithinPeriod(pat, initYear, timeHorizon, Discount.ZERO_DISCOUNT), initYear);
						}
					}
					// Update lastYears
					lastYear[pat.getnIntervention()][pat.getIdentifier()] = endYear;
				}
			}
		}
	}

	/**
	 * Updates the value of this outcome for a specified period
	 * @param values An array with the values to be applied each time interval during the period
	 * @param index The first interval when it will be applied
	 */
	private void update(int nIntervention, double[] values, double initYear) {
		final int index = (int) initYear;
		for (int i = 0; i < values.length; i++) {
			cost[nIntervention][i + index] += values[i];
		}
	}
	
	/**
	 * Updates the value of this outcome at a specified age
	 * @param value The value to update
	 * @param age The age at which the value is applied
	 */
	private void update(int nIntervention, double value, double age) {
		cost[nIntervention][(int)age] += value;
	}
	
	@Override
	public void aggregateResultsAfterIntervention(int simulationId, Intervention intervention) {
		// Do nothing
	}
}