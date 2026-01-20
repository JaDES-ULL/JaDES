/**
 * 
 */
package es.ull.simulation.experiment;

import es.ull.simulation.model.ILoggable;
import simkit.random.RandomNumber;
import simkit.random.RandomNumberFactory;
import es.ull.simulation.model.IDescribable;

/**
 * Controls a set of simulation experiments. 
 * @author Iván Castilla Rodríguez
 */
public interface IExperiment extends IDescribable, ILoggable {
	RandomNumber randomNumber = RandomNumberFactory.getInstance();

	/** */
	public static long getSeed() {
		return randomNumber.getSeed();
	}

	public static void setSeed(long seed) {
		randomNumber.setSeed(seed);
	}

	/** 
	 * Executes the experiment with the given index.
	 * @param index Number of the experiment to be executed
	 */
	public void runExperiment(int index);

	/**
	 * Performs actions before the experiments start.
	 * Implementations of this interface should call this method before starting any experiments.
	 */
	public default void beforeStart() {
	}

	/**
	 * Implementations of this method must call {@link #runExperiment(int)} to carry out all the 
	 * simulations planned in this experiment.
	 */
	public void run();
	
	/**
	 * Performs actions after the experiments have finished. The user should place here actions such as closing files, DB access...
	 * Implementations of this interface should call this method after finishing all the experiments.
	 */
	public default void afterFinalize() {
	}

}
