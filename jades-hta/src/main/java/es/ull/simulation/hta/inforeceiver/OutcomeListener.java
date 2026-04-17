/**
 * 
 */
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
import es.ull.simulation.utils.Statistics;

/**
 * A listener that collects information about one of the outcomes of the model per patient and intervention.
 * @author Iván Castilla Rodríguez
 *
 */
public abstract class OutcomeListener extends BasicHTAListener {
	/** A discount rate to be applied to future values of the outcomes */
	protected final Discount discountRate;
	/** The aggregated value of the outcome per intervention */
	protected double[] aggregated;
	/** The values of the outcome per intervention and patient */
	protected final double[][]values;
	/** The last time a patient was updated per intervention and patient*/
	protected final long[][]lastTs;
	/** The final results put together */
	private final AggregatedResult[] results;

	/**
	 * Creates a new listener for outcomes
	 * @param description A description of the listener
	 * @param discountRate A discount rate to be applied to future values of the outcomes
	 * @param model The model that defines the simulations
	 */
	public OutcomeListener(HTAExperiment exp, OutcomeCollector collector, String description, Discount discountRate) {
		super(description, exp, collector);
		this.discountRate = discountRate;
		this.values = new double[interventions.length][nPatients];
		this.lastTs = new long[interventions.length][nPatients];
		this.results = new AggregatedResult[interventions.length];
		this.aggregated = new double[interventions.length];
		addTargetInformation(PatientInfo.class);
		addTargetInformation(SimulationStartStopInfo.class);
	}

	/**
	 * Returns the discount rate applied to future values of the outcomes
	 * @return The discount rate applied to future values of the outcomes
	 */
	public Discount getDiscountRate() {
		return discountRate;
	}

	@Override
	public void infoEmited(IPieceOfInformation info) {
		if (info instanceof SimulationStartStopInfo) {
			final SimulationStartStopInfo tInfo = (SimulationStartStopInfo) info;
			if (SimulationStartStopInfo.Type.END.equals(tInfo.getType())) {
				final long ts = tInfo.getTs();
				final DiseaseProgressionSimulation simul = (DiseaseProgressionSimulation)tInfo.getSimul();
				final TimeUnit simUnit = simul.getTimeUnit();
				for (int i = 0; i < nPatients; i++) {
					final Patient pat = (Patient)simul.getGeneratedPatient(i);
					if (!pat.isDead()) {
						final double initT = pat.getSimulation().getModel().simulationTimeToYears(TimeUnit.DAY.convert(lastTs[pat.getnIntervention()][pat.getIdentifier()], simUnit)); 
						final double endT = pat.getSimulation().getModel().simulationTimeToYears(TimeUnit.DAY.convert(ts, simUnit));
						if (endT > initT) {
							update(pat, getPeriodValue(pat, initT, endT));							
						}						
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
			final double initT = pat.getSimulation().getModel().simulationTimeToYears(TimeUnit.DAY.convert(lastTs[pat.getnIntervention()][pat.getIdentifier()], simUnit)); 
			final double endT = pat.getSimulation().getModel().simulationTimeToYears(TimeUnit.DAY.convert(ts, simUnit));
			// Update lastTs
			lastTs[pat.getnIntervention()][pat.getIdentifier()] = ts;

			// Update outcomes with one time values
			update(pat, getOneTimeValue(pat, pInfo, endT));
			
			if (!PatientInfo.Type.START.equals(pInfo.getType())) {
				// Update outcomes with period values
				if (endT > initT) {
					update(pat, getPeriodValue(pat, initT, endT));
				}
			}
		}
	}

	@Override
	public void aggregateResultsAfterIntervention(int simulationId, Intervention intervention) {
		final int nIntervention = intervention.ordinal();
		final double average = aggregated[nIntervention] / nPatients;
		final double sd = Statistics.stdDev(values[nIntervention], average);
		final double[] ci = Statistics.normal95CI(average, sd, nPatients);
		final double[] cip = Statistics.getPercentile95CI(values[nIntervention]);
		results[nIntervention] = new AggregatedResult(simulationId, intervention, average, sd, ci, cip);
	}

	/**
	 * Updates the value of this outcome
	 * @param pat A patient
	 * @param value The value to update
	 * @param age The age at which the value is applied
	 */
	private void update(Patient pat, double value) {
		values[pat.getnIntervention()][pat.getIdentifier()] += value;
		aggregated[pat.getnIntervention()] += value;
	}
	
	/**
	 * Returns average, standard deviation, lower 95%CI, upper 95%CI, percentile 2.5%, percentile 97.5% for each intervention
	 * @return An array with n t-uples {average, standard deviation, lower 95%CI, upper 95%CI, percentile 2.5%, percentile 97.5%}, 
	 * with n the number of interventions.  
	 */
	public AggregatedResult[] getResults() {
		return results;
	}

	/**
	 * Returns the results per intervention and patient
	 * @return the results per intervention and patient
	 */
	public double[][] getValues() {
		return values;
	}
	
	/**
	 * Returns the value of the outcome within a period of time
	 * @param pat A patient
	 * @param initT The initial time of the period
	 * @param endT The final time of the period
	 * @return The value of the outcome within a period of time
	 */
	public abstract double getPeriodValue(Patient pat, double initT, double endT);

	/**
	 * Returns the value of the outcome at a given time for a specific event
	 * @param pat A patient
	 * @param pInfo The information that triggers the event
	 * @param ts The time of the event
	 * @return The value of the outcome at a given time for a specific event
	 */
	public abstract double getOneTimeValue(Patient pat, PatientInfo pInfo, double ts);

    /**
     * A class that represents the aggregated results of an outcome
     */
    public static class AggregatedResult {
        /** The simulation identifier */
        private final int simulationId;
        /** The intervention assessed */
        private final Intervention intervention;
        /** The average value of the outcome */
        private final double average;
        /** The standard deviation of the outcome */
        private final double standardDeviation;
        /** The 95% confidence intervals of the outcome */
        private final double[] ci;
        /** The 95% percentiles of the outcome */
        private final double[] cip;
    
        /**
         * Creates a new aggregated result
         * @param simulationId The simulation identifier
         * @param intervention The intervention assessed
         * @param average The average value of the outcome
         * @param standardDeviation The standard deviation of the outcome
         * @param ci The 95% confidence intervals of the outcome
         * @param cip The 95% percentiles of the outcome
         */
        public AggregatedResult(int simulationId, Intervention intervention, double average, double standardDeviation, double[] ci, double[] cip) {
            this.simulationId = simulationId;
            this.intervention = intervention;
            this.average = average;
            this.standardDeviation = standardDeviation;
            this.ci = ci;
            this.cip = cip;
        }
    
        /**
         * Returns the simulation identifier
         * @return The simulation identifier
         */
        public int getSimulationId() {
            return simulationId;
        }
    
        /**
         * Returns the intervention assessed
         * @return The intervention assessed
         */
        public Intervention getIntervention() {
            return intervention;
        }
        
        /**
         * Returns the average value of the outcome
         * @return The average value of the outcome
         */         
        public double getAverage() {
            return average;
        }
    
        /**
         * Returns the standard deviation of the outcome
         * @return The standard deviation of the outcome
         */
        public double getStandardDeviation() {
            return standardDeviation;
        }
    
        /**
         * Returns the 95% confidence intervals of the outcome
         * @return The 95% confidence intervals of the outcome
         */
        public double[] get95ConfidenceIntervals() {
            return ci;
        }
    
        /**
         * Returns the 95% percentiles of the outcome
         * @return The 95% percentiles of the outcome
         */
        public double[] get95Percentiles() {
            return cip;
        }
    
    }    
}
