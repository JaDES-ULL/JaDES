package es.ull.simulation.hta.outcomes;

import java.util.HashSet;
import java.util.Set;

/**
 * Accumulates the values of a payoff (cost, disutility, life expectancy reduction...) for the set of model items that are
 * simultaneously active for an individual, according to a {@link CombinationMethod}. Each value is identified by a key
 * (usually, the name of the parameter that defines it), so that methods that {@link CombinationMethod#countsSharedItemsOnce() count
 * shared items once} can discard repeated keys.
 * <p>
 * This class is shared by the different model generators (decision trees, discrete-event simulation) so that all of them
 * apply exactly the same rule.
 * </p>
 * @param <K> The type of the keys that identify each value
 * @author Iván Castilla Rodríguez
 */
public final class ActiveItemsCombiner<K> {
	/** The combination method */
	private final CombinationMethod method;
	/** The keys already combined */
	private final Set<K> seen;
	/** The current combined value */
	private double value;

	/**
	 * Creates a combiner for a specific method, starting from an initial value (e.g. 0 for costs and life expectancy reductions;
	 * 1 for mortality rate ratios). The initial value is not associated to any key.
	 * @param method The combination method
	 * @param initialValue The initial value
	 */
	public ActiveItemsCombiner(CombinationMethod method, double initialValue) {
		this.method = method;
		this.seen = new HashSet<>();
		this.value = initialValue;
	}

	/**
	 * Creates a combiner for a specific method, starting from the value contributed by a first item. Unlike {@link #ActiveItemsCombiner(CombinationMethod, double)},
	 * the initial value is associated to a key, so that later values with the same key may be discarded.
	 * @param method The combination method
	 * @param initialKey The key that identifies the initial value (may be null)
	 * @param initialValue The initial value
	 */
	public ActiveItemsCombiner(CombinationMethod method, K initialKey, double initialValue) {
		this(method, initialValue);
		if (initialKey != null) {
			seen.add(initialKey);
		}
	}

	/**
	 * Adds a new value to the combination. Values that are NaN are ignored. If the method counts shared items once,
	 * values whose key has been already added are also ignored. A null key is never considered as repeated.
	 * @param key The key that identifies the value (generally, the name of the parameter)
	 * @param newValue The value to combine
	 * @return true if the value was combined; false if it was ignored
	 */
	public boolean add(K key, double newValue) {
		if (Double.isNaN(newValue)) {
			return false;
		}
		if (key != null && !seen.add(key) && method.countsSharedItemsOnce()) {
			return false;
		}
		value = method.combine(value, newValue);
		return true;
	}

	/**
	 * Returns true if a value with the specified key would be ignored because it has been already added and the method counts
	 * shared items once
	 * @param key The key that identifies the value
	 * @return true if a value with the specified key would be ignored
	 */
	public boolean isRepeated(K key) {
		return key != null && method.countsSharedItemsOnce() && seen.contains(key);
	}

	/**
	 * Returns the combined value
	 * @return the combined value
	 */
	public double getValue() {
		return value;
	}
}
