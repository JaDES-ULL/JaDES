package es.ull.simulation.hta.inforeceiver;

import es.ull.simulation.hta.interventions.Intervention;

/**
 * A class that represents the aggregated results of an outcome
 */
public class CommonAggregatedResult {
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
    public CommonAggregatedResult(int simulationId, Intervention intervention, double average, double standardDeviation, double[] ci, double[] cip) {
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