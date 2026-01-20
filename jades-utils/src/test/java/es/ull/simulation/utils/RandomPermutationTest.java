package es.ull.simulation.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class RandomPermutationTest {

    @Test
    void shouldGeneratePermutationWithAllElements() {
        int n = 10;
        int[] perm = RandomPermutation.nextPermutation(n);
        assertEquals(n, perm.length);
        Set<Integer> seen = new HashSet<>();
        for (int value : perm) {
            assertTrue(value >= 0 && value < n);
            seen.add(value);
        }
        assertEquals(n, seen.size());
    }
}
