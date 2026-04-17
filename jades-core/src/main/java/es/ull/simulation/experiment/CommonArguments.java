package es.ull.simulation.experiment;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;

import com.beust.jcommander.Parameter;

/** 
 * Commonly used arguments for the simulation experiments.
 */
public class CommonArguments implements IExperimentConfigurationProvider {
	@Parameter(names = { "--runs", "-r" }, description = "Number of simulation experiments to launch", order = 1)
	public int nRuns = -1;
 	@Parameter(names = { "--seed", "-s" }, description = "Seed for the random number generator", order = 3)
	public long seed = -1;
	@Parameter(names = { "--horizon", "-h" }, description = "Time horizon for the simulation (years)", order = 2)
	public int timeHorizon = -1;
	@Parameter(names = { "--nthreads", "-th" }, description = "Sets a specific number of threads to run the experiments in parallel (by default, the number of available processors)", order = 5)
	public int nThreads = -1;
	@Parameter(names = { "--parallel", "-p" }, description = "Enables parallel execution", order = 4)
	public boolean parallel = false;
	
	@Override
	public OptionalInt getNRuns() {
		return (nRuns >= 0) ? OptionalInt.of(nRuns) : OptionalInt.empty();
	}
	@Override
	public OptionalLong getSeed() {
		return (seed >= 0) ? OptionalLong.of(seed) : OptionalLong.empty();
	}
	@Override
	public OptionalInt getTimeHorizon() {
		return (timeHorizon >= 0) ? OptionalInt.of(timeHorizon) : OptionalInt.empty();
	}
	@Override
	public OptionalInt getNThreads() {
		return (nThreads >= 0) ? OptionalInt.of(nThreads) : OptionalInt.empty();
	}
	@Override
	public Optional<Boolean> isParallel() {
		// Important: only return a value if parallel is true; otherwise, return empty
		return  parallel ? Optional.of(Boolean.TRUE) : Optional.empty();
	}
}
