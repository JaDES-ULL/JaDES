/**
 * 
 */
package es.ull.simulation.hta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import simkit.random.RandomNumber;
import simkit.random.RandomNumberFactory;

/**
 * Common random numbers shared among the instances of a patient when evaluated for different interventions.
 * They ensure that the differences among interventions depend on what the intervention modifies and not on random numbers.
 * The same patient can have different series of random numbers identified by a key.
 * @author Iván Castilla Rodríguez
 */
public class PatientCommonRandomNumbers {
	/** The internal structure to store the random numbers. There is a list of double values for each key */
	private final HashMap<String, ArrayList<Double>> rndValues;
	/** A random number generator for first order parameter values */
	private static RandomNumber RNG = RandomNumberFactory.getInstance();

	/**
	 * Creates a new instance of the class
	 */
	public PatientCommonRandomNumbers() {
		
		rndValues = new HashMap<>();
	}

	/**
	 * Returns the first n random numbers for a given key
	 * @param key Key that identifies a specific set of the random numbers
	 * @param n Number of random numbers to return
	 * @return A list with the first n random numbers for the given key
	 */
	public List<Double> draw(String key, int n) {
		ArrayList<Double> values = rndValues.get(key);
		if (values == null) {
			values = new ArrayList<>();
			rndValues.put(key, values);
		}
		if (n > values.size()) {
			for (int i = values.size(); i < n; i++) {
				final double rnd = RNG.draw();
				values.add(rnd);
			}
		}
		return values.subList(0, n);
	}
	
	/**
	 * Returns a single  random number for a given key
	 * @param key Key that identifies a specific set of the random numbers
	 * @return A single random number for the given key
	 */
	public double draw(String key) {
		ArrayList<Double> values = rndValues.get(key);
		if (values == null) {
			values = new ArrayList<>();
			rndValues.put(key, values);
			final double rnd = RNG.draw();
			values.add(rnd);
			return rnd;
		}
		return values.get(0);
	}

    /**
     * Changes the default random number generator for first order uncertainty
     * @param rng New random number generator
     */
    public static void setRNG(RandomNumber rng) {
    	RNG = rng;
    }

    /**
     * Returns the random number generator for first order uncertainty
     * @return the random number generator for first order uncertainty
     */
    public static RandomNumber getRNG() {
    	return RNG;
    }
}
