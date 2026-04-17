package es.ull.simulation.experiment;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * An interface to provide experiment configuration
 */
public interface IExperimentConfigurationProvider {
    /**
     * Gets the number of runs for the experiment
     * @return The number of runs for the experiment
     */
    public OptionalInt getNRuns();
    /**
     * Gets the seed for the random number generator
     * @return The seed for the random number generator
     */
    public OptionalLong getSeed();
    /**
     * Gets the time horizon for the simulation (years)
     * @return The time horizon for the simulation (years)
     */
    public OptionalInt getTimeHorizon();
    /**
     * Gets the number of threads to run the experiments in parallel
     * @return The number of threads to run the experiments in parallel
     */
    public OptionalInt getNThreads();
    /**
     * Indicates if the experiments should be run in parallel
     * @return true if the experiments should be run in parallel, false otherwise
     */
    public Optional<Boolean> isParallel();

}
