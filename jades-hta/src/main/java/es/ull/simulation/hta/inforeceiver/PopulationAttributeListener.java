/**
 * 
 */
package es.ull.simulation.hta.inforeceiver;

import java.util.Set;
import java.util.TreeMap;

import es.ull.simulation.hta.DiseaseProgressionSimulation;
import es.ull.simulation.hta.HTAExperiment;
import es.ull.simulation.hta.Patient;
import es.ull.simulation.hta.info.PatientInfo;
import es.ull.simulation.hta.interventions.Intervention;
import es.ull.simulation.hta.params.Parameter;
import es.ull.simulation.hta.params.ParameterGroup;
import es.ull.simulation.info.IPieceOfInformation;
import es.ull.simulation.info.SimulationStartStopInfo;
import es.ull.simulation.utils.Statistics;

/**
 * @author Iván Castilla Rodríguez
 *
 */
public class PopulationAttributeListener extends BasicHTAListener {
	public static final String STR_INIT_AGE = "INIT_AGE";
	/** The names of the attributes used in the simulation */
	private final String [] attributeNames;
	/** The aggregated value of each attribute per intervention */
	private final TreeMap<String, double[]> aggregated;
	/** The values of the attributes per intervention and patient */
	private final TreeMap<String, double[][]> values;
	/** The aggregated initial ages of the patients per intervention */
	private double[] aggregatedInitAge;
	/** The initial ages of the patients per intervention and patient */
	private final double[][] initAges;
	/** The final results put together */
	private final TreeMap<String, AttributeAggregatedResult> results;
	

	/**
	 * Creates a new listener for the attributes of the patients
	 * @param exp The experiment to be analyzed
	 */
	public PopulationAttributeListener(HTAExperiment exp, PopulationAttributeCollector collector) {
		super("Listener for individual parameters", exp, collector);
		this.values = new TreeMap<>();
		final Set<String> attributeList = Parameter.getParametersByType(ParameterGroup.ATTRIBUTE).keySet();
		attributeNames = attributeList.toArray(new String[attributeList.size()]);
		this.aggregated = new TreeMap<>();
		this.results = new TreeMap<>();
		
		for (int i = 0; i < attributeList.size(); i++) {
			values.put(attributeNames[i], new double[interventions.length][nPatients]);
			aggregated.put(attributeNames[i], new double[interventions.length]);
			results.put(attributeNames[i], new AttributeAggregatedResult(interventions.length));
		}
		results.put(STR_INIT_AGE, new AttributeAggregatedResult(interventions.length));
		initAges = new double[interventions.length][nPatients];
		aggregatedInitAge = new double[interventions.length];
		addTargetInformation(PatientInfo.class);
		addTargetInformation(SimulationStartStopInfo.class);
	}

	/**
	 * @return the results
	 */
	public TreeMap<String, AttributeAggregatedResult> getResults() {
		return results;
	}

	@Override
	public void aggregateResultsAfterIntervention(int simulationId, Intervention intervention) {
		final int nIntervention = intervention.ordinal();
		double avg = aggregatedInitAge[nIntervention] / nPatients;
		double stdDev = Statistics.stdDev(initAges[nIntervention], avg);
		double[] ci = Statistics.normal95CI(avg, stdDev, nPatients);
		double[] cip = Statistics.getPercentile95CI(initAges[nIntervention]);
		results.get(STR_INIT_AGE).updateIntervention(nIntervention, avg, stdDev, ci, cip);

		for (String name : attributeNames) {
			avg = aggregated.get(name)[nIntervention] / nPatients;
			stdDev = Statistics.stdDev(values.get(name)[nIntervention], avg);
			ci = Statistics.normal95CI(avg, stdDev, nPatients);
			cip = Statistics.getPercentile95CI(values.get(name)[nIntervention]);
			results.get(name).updateIntervention(nIntervention, avg, stdDev, ci, cip);
		}
	}

	/* (non-Javadoc)
	 * @see es.ull.simulation.inforeceiver.InfoReceiver#infoEmited(es.ull.simulation.info.SimulationInfo)
	 */
	@Override
	public void infoEmited(IPieceOfInformation info) {
		if (info instanceof SimulationStartStopInfo) {
			final SimulationStartStopInfo tInfo = (SimulationStartStopInfo) info;
			if (SimulationStartStopInfo.Type.END.equals(tInfo.getType())) {
				final DiseaseProgressionSimulation simul = (DiseaseProgressionSimulation)tInfo.getSimul();
				incrementInterventionsSimulated(simul);
			}
		}
		else if (info instanceof PatientInfo) {
			final PatientInfo pInfo = (PatientInfo) info;
			final Patient pat = pInfo.getPatient();
			if (PatientInfo.Type.START.equals(pInfo.getType())) {
				initAges[pat.getnIntervention()][pat.getIdentifier()] = pat.getInitAge();
				aggregatedInitAge[pat.getnIntervention()] += initAges[pat.getnIntervention()][pat.getIdentifier()]; 
				for (String name : attributeNames) {
					values.get(name)[pat.getnIntervention()][pat.getIdentifier()] = (double)pat.getAttributeValue(name);
					aggregated.get(name)[pat.getnIntervention()] += values.get(name)[pat.getnIntervention()][pat.getIdentifier()];
				}
			}
		}
	}
	
	/**
	 * Returns the results per attribute, intervention and patient
	 * @return the results per attribute, intervention and patient
	 */
	public TreeMap<String, double[][]> getValues() {
		return values;
	}

    /**
     * A class that represents the aggregated results of an outcome
     */
    public static class AttributeAggregatedResult {
        /** The average value of the outcome */
        private final double[] average;
        /** The standard deviation of the outcome */
        private final double[] standardDeviation;
        /** The 95% confidence intervals of the outcome */
        private final double[][] ci;
        /** The 95% percentiles of the outcome */
        private final double[][] cip;
    
        /**
         * Creates a new aggregated result
         * @param average The average value of the outcome
         * @param standardDeviation The standard deviation of the outcome
         * @param ci The 95% confidence intervals of the outcome
         * @param cip The 95% percentiles of the outcome
         */
        public AttributeAggregatedResult(int nInterventions) {
            this.average = new double[nInterventions];
            this.standardDeviation = new double[nInterventions];
            this.ci = new double[nInterventions][2];
            this.cip = new double[nInterventions][2];
        }
    
		public void updateIntervention(int interventionId, double average, double standardDeviation, double[] ci, double[] cip) {
			this.average[interventionId] = average;
			this.standardDeviation[interventionId] = standardDeviation;
			this.ci[interventionId] = ci;
			this.cip[interventionId] = cip;
		}

        /**
         * Returns the average value of the outcome
         * @return The average value of the outcome
         */         
        public double getAverage(int interventionId) {
            return average[interventionId];
        }
    
        /**
         * Returns the standard deviation of the outcome
         * @return The standard deviation of the outcome
         */
        public double getStandardDeviation(int interventionId) {
            return standardDeviation[interventionId];
        }
    
        /**
         * Returns the 95% confidence intervals of the outcome
         * @return The 95% confidence intervals of the outcome
         */
        public double[] get95ConfidenceIntervals(int interventionId) {
            return ci[interventionId];
        }
    
        /**
         * Returns the 95% percentiles of the outcome
         * @return The 95% percentiles of the outcome
         */
        public double[] get95Percentiles(int interventionId) {
            return cip[interventionId];
        }
    
    }    

}
