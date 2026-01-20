package es.ull.simulation.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ExtendedMathTest {

    @Test
    void shouldRoundCeilAndFloorToFactor() {
        assertEquals(10.0, ExtendedMath.round(12.2, 5.0));
        assertEquals(15.0, ExtendedMath.round(12.8, 5.0));
        assertEquals(15.0, ExtendedMath.ceil(12.2, 5.0));
        assertEquals(10.0, ExtendedMath.floor(12.8, 5.0));
    }

    @Test
    void shouldComputeNextHigherPowerOfTwo() {
        assertEquals(1, ExtendedMath.nextHigherPowerOfTwo(1));
        assertEquals(2, ExtendedMath.nextHigherPowerOfTwo(2));
        assertEquals(4, ExtendedMath.nextHigherPowerOfTwo(3));
        assertEquals(8, ExtendedMath.nextHigherPowerOfTwo(5));
    }

    @Test
    void shouldComputeIntPowers() {
        assertEquals(1, ExtendedMath.powInt(5, 0));
        assertEquals(8, ExtendedMath.powInt(2, 3));
        assertEquals(81, ExtendedMath.powInt(3, 4));
    }

    @Test
    void shouldComputeDoublePower() {
        assertEquals(1.0, ExtendedMath.power(2.0, 0));
        assertEquals(8.0, ExtendedMath.power(2.0, 3));
        assertEquals(0.25, ExtendedMath.power(0.5, 2));
    }

    @Test
    void shouldComputeMinAndMax() {
        assertEquals(2L, ExtendedMath.min(5L, 9L, 2L, 7L));
        assertEquals(9L, ExtendedMath.max(5L, 9L, 2L, 7L));
    }

    @Test
    void shouldNormalizeFrequencies() {
        double[] input = new double[] {1.0, 1.0, 2.0};
        double[] normalized = ExtendedMath.normalize(input);
        assertEquals(0.25, normalized[0], 1e-9);
        assertEquals(0.25, normalized[1], 1e-9);
        assertEquals(0.5, normalized[2], 1e-9);
    }

    @Test
    void shouldRejectNegativeFrequencies() {
        double[] input = new double[] {1.0, -1.0};
        assertThrows(IllegalArgumentException.class, () -> ExtendedMath.normalize(input));
    }

    @Test
    void shouldRejectZeroSumFrequencies() {
        double[] input = new double[] {0.0, 0.0};
        assertThrows(IllegalArgumentException.class, () -> ExtendedMath.normalize(input));
    }
}
