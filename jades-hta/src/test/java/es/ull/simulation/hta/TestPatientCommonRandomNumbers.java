package es.ull.simulation.hta;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import simkit.random.RandomNumberFactory;

public class TestPatientCommonRandomNumbers {
    private static final double[] expectedResults1 = {0.395003795158118, 0.4317646191921085, 0.41451547713950276, 0.6460858534555882, 0.48979148897342384};
    private static final double[] expectedResults2 = {0.2792195361107588, 0.23806875152513385, 0.7213246969040483, 0.03858571010641754, 0.43779360968619585};
    private static final double EPSILON = 1e-6;

    @BeforeEach
    public void setUp() {
        PatientCommonRandomNumbers.setRNG(RandomNumberFactory.getInstance(35));
    }

    @Test
    @DisplayName("Test draw")
    public void testDraw() {
        PatientCommonRandomNumbers patientCommonRandomNumbers = new PatientCommonRandomNumbers();
        int n = 5;
        // test if the first n values of key and key2 are the expected ones
        List<Double> list = patientCommonRandomNumbers.draw("key", n);
        for (int i = 0; i < n; i++) {
            assertTrue(Math.abs(list.get(i) - expectedResults1[i]) < EPSILON);
        }
        list = patientCommonRandomNumbers.draw("key2", n);
        for (int i = 0; i < n; i++) {
            assertTrue(Math.abs(list.get(i) - expectedResults2[i]) < EPSILON);
        }
        // now test if asking for the first two values of key returns the expected values after having generated new numbers
        n = 2;
        list = patientCommonRandomNumbers.draw("key", n);
        for (int i = 0; i < n; i++) {
            assertTrue(Math.abs(list.get(i) - expectedResults1[i]) < EPSILON);
        }
        list = patientCommonRandomNumbers.draw("key2", n);
        for (int i = 0; i < n; i++) {
            assertTrue(Math.abs(list.get(i) - expectedResults2[i]) < EPSILON);
        }
    }
}
