package es.ull.simulation.hta.outcomes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Checks the combination methods used to combine the payoffs of simultaneously active items, and the {@link ActiveItemsCombiner}
 * shared by the decision tree and the discrete-event simulation generators.
 */
@DisplayName("Combination of payoffs of simultaneously active items")
public class TestCombinationMethods {
	private static final double ERROR = 1e-12;

	@Test
	@DisplayName("ADD counts shared parameters once per item; ADD_DISTINCT only once")
	public void testAddVsAddDistinct() {
		final ActiveItemsCombiner<String> add = new ActiveItemsCombiner<>(CostCombinationMethod.ADD, 0.0);
		final ActiveItemsCombiner<String> distinct = new ActiveItemsCombiner<>(CostCombinationMethod.ADD_DISTINCT, 0.0);
		for (ActiveItemsCombiner<String> combiner : java.util.List.of(add, distinct)) {
			combiner.add("SharedVisit", 50.0);
			combiner.add("SharedVisit", 50.0);
			combiner.add("Drug", 100.0);
		}
		assertEquals(200.0, add.getValue(), ERROR);
		assertEquals(150.0, distinct.getValue(), ERROR);
	}

	@Test
	@DisplayName("Values without key (default values) are never considered shared")
	public void testNullKeys() {
		final ActiveItemsCombiner<String> distinct = new ActiveItemsCombiner<>(DisutilityCombinationMethod.ADD_DISTINCT, 0.0);
		assertTrue(distinct.add(null, 0.1));
		assertTrue(distinct.add(null, 0.1));
		assertEquals(0.2, distinct.getValue(), ERROR);
	}

	@Test
	@DisplayName("The key of the initial value is taken into account")
	public void testInitialKey() {
		final ActiveItemsCombiner<String> distinct = new ActiveItemsCombiner<>(CostCombinationMethod.ADD_DISTINCT, "DiseaseCost", 1000.0);
		assertTrue(distinct.isRepeated("DiseaseCost"));
		assertFalse(distinct.add("DiseaseCost", 1000.0));
		assertTrue(distinct.add("ManifestationCost", 500.0));
		assertEquals(1500.0, distinct.getValue(), ERROR);
		final ActiveItemsCombiner<String> add = new ActiveItemsCombiner<>(CostCombinationMethod.ADD, "DiseaseCost", 1000.0);
		assertFalse(add.isRepeated("DiseaseCost"));
		assertTrue(add.add("DiseaseCost", 1000.0));
		assertEquals(2000.0, add.getValue(), ERROR);
	}

	@Test
	@DisplayName("NaN values are ignored")
	public void testNaN() {
		final ActiveItemsCombiner<String> max = new ActiveItemsCombiner<>(LifeExpectancyReductionCombinationMethod.MAX, 0.0);
		assertFalse(max.add("A", Double.NaN));
		assertTrue(max.add("B", 5.0));
		assertEquals(5.0, max.getValue(), ERROR);
	}

	@Test
	@DisplayName("Life expectancy reductions: ADD, ADD_DISTINCT and MAX")
	public void testLifeExpectancyReduction() {
		final double[] expected = {45.0, 25.0, 20.0};
		final LifeExpectancyReductionCombinationMethod[] methods = {LifeExpectancyReductionCombinationMethod.ADD, LifeExpectancyReductionCombinationMethod.ADD_DISTINCT, LifeExpectancyReductionCombinationMethod.MAX};
		for (int i = 0; i < methods.length; i++) {
			final ActiveItemsCombiner<String> ler = new ActiveItemsCombiner<>(methods[i], 0.0);
			ler.add("Severe", 20.0);
			ler.add("Severe", 20.0);
			ler.add("Mild", 5.0);
			assertEquals(expected[i], ler.getValue(), ERROR, "Unexpected value for " + methods[i]);
		}
	}

	@Test
	@DisplayName("Increased mortality rates: MAX and MULT, starting from 1")
	public void testMortalityRate() {
		final ActiveItemsCombiner<String> max = new ActiveItemsCombiner<>(MortalityRateCombinationMethod.MAX, 1.0);
		final ActiveItemsCombiner<String> mult = new ActiveItemsCombiner<>(MortalityRateCombinationMethod.MULT, 1.0);
		assertEquals(1.0, max.getValue(), ERROR, "No active item must not modify mortality");
		assertEquals(1.0, mult.getValue(), ERROR, "No active item must not modify mortality");
		for (ActiveItemsCombiner<String> combiner : java.util.List.of(max, mult)) {
			combiner.add("IMR1", 2.0);
			combiner.add("IMR2", 1.25);
		}
		assertEquals(2.0, max.getValue(), ERROR);
		assertEquals(2.5, mult.getValue(), ERROR);
	}

	@Test
	@DisplayName("Disutilities: MAX ignores whether parameters are shared")
	public void testDisutilityMax() {
		final ActiveItemsCombiner<String> max = new ActiveItemsCombiner<>(DisutilityCombinationMethod.MAX, 0.0);
		max.add("Severe", 0.4);
		max.add("Severe", 0.4);
		max.add("Mild", 0.2);
		assertEquals(0.4, max.getValue(), ERROR);
		assertFalse(DisutilityCombinationMethod.MAX.countsSharedItemsOnce());
		assertTrue(DisutilityCombinationMethod.ADD_DISTINCT.countsSharedItemsOnce());
	}
}
