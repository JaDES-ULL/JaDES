package es.ull.simulation.hta.outcomes;

/**
 * Common behaviour of the methods used to combine the payoffs (costs, disutilities, life expectancy reductions,
 * increased mortality rates...) of all the model items that are simultaneously active for an individual.
 * <p>
 * A combination method is applied, at any moment, to the set of active items: in a decision tree, the items along
 * the path to a leaf; in a discrete-event simulation, the state of the patient between two consecutive events.
 * Methods that {@link #countsSharedItemsOnce() count shared items once} only consider once each parameter that is
 * shared by several active items (e.g., the same follow-up visit required by two manifestations).
 * </p>
 * @author Iván Castilla Rodríguez
 */
public interface CombinationMethod {
	/**
	 * Combines two values into a single one
	 * @param value1 First value
	 * @param value2 Second value
	 * @return The result of combining the two values
	 */
	double combine(double value1, double value2);

	/**
	 * Indicates whether a parameter shared by several simultaneously active items must be considered only once
	 * @return true if a parameter shared by several simultaneously active items must be considered only once; false if it must be considered once per item
	 */
	default boolean countsSharedItemsOnce() {
		return false;
	}
}
