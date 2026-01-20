package es.ull.simulation.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

class StatisticsTest {

    @Test
    void shouldComputeAverageAndStdDevForDoubles() {
        double[] values = new double[] {1.0, 2.0, 3.0};
        assertEquals(2.0, Statistics.average(values), 1e-9);
        assertEquals(1.0, Statistics.stdDev(values), 1e-9);
        assertEquals(1.0, Statistics.stdDev(values, 2.0), 1e-9);
    }

    @Test
    void shouldComputeAverageAndStdDevForInts() {
        int[] values = new int[] {2, 4, 6};
        assertEquals(4.0, Statistics.average(values), 1e-9);
        assertEquals(2.0, Statistics.stdDev(values), 1e-9);
    }

    @Test
    void shouldComputeAverageAndStdDevForBoxed() {
        Double[] values = new Double[] {2.0, 4.0, 6.0};
        assertEquals(4.0, Statistics.average(values), 1e-9);
        assertEquals(2.0, Statistics.stdDev(values), 1e-9);
    }

    @Test
    void shouldComputeAverageAndStdDevForList() {
        ArrayList<Number> values = new ArrayList<>(Arrays.asList(1, 2, 3, 4));
        assertEquals(2.5, Statistics.average(values), 1e-9);
        assertEquals(1.2909944487, Statistics.stdDev(values), 1e-6);
    }

    @Test
    void shouldComputeRelativeErrors() {
        assertEquals(0.1, Statistics.relError(10.0, 9.0), 1e-9);
        assertEquals(10.0, Statistics.relError100(10.0, 9.0), 1e-9);
    }

    @Test
    void shouldComputeNormal95CI() {
        double[] ci = Statistics.normal95CI(10.0, 2.0, 4);
        assertEquals(10.0 - 1.96, ci[0], 1e-6);
        assertEquals(10.0 + 1.96, ci[1], 1e-6);
    }

    @Test
    void shouldComputePercentile() {
        double[] values = new double[] {1, 2, 3, 4, 5};
        assertEquals(3.5, Statistics.percentile(values, 0.5), 1e-9);
        assertTrue(Double.isNaN(Statistics.percentile(values, 0.0)));
        assertTrue(Double.isNaN(Statistics.percentile(values, 1.5)));
        assertEquals(1.0, Statistics.percentile(values, 0.1), 1e-9);
        assertEquals(5.0, Statistics.percentile(values, 0.999), 1e-9);

        double[] single = new double[] {7.0};
        assertEquals(7.0, Statistics.percentile(single, 0.5), 1e-9);
    }

    @Test
    void shouldComputePercentile95CI() {
        double[] values = new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        double[] ci = Statistics.getPercentile95CI(values);
        assertEquals(1.0, ci[0], 1e-9);
        assertEquals(10.0, ci[1], 1e-9);

        int[] intValues = new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        int[] intCi = Statistics.getPercentile95CI(intValues);
        assertEquals(1, intCi[0]);
        assertEquals(10, intCi[1]);
    }

    @Test
    void shouldComputeAnnualBasedTimeToEvent() {
        assertEquals(Double.MAX_VALUE, Statistics.getAnnualBasedTimeToEvent(0.0, -0.5, 1.0));
        double value = Statistics.getAnnualBasedTimeToEvent(0.1, -0.5, 1.0);
        assertTrue(value > 0.0);
    }

    @Test
    void shouldComputeAnnualBasedTimeToEventFromRate() {
        assertEquals(Double.MAX_VALUE, Statistics.getAnnualBasedTimeToEventFromRate(0.0, -0.5, 1.0));
        double value = Statistics.getAnnualBasedTimeToEventFromRate(0.2, -0.5, 2.0);
        assertTrue(value > 0.0);
    }

    @Test
    void shouldComputeAnnualRiskFromTimeToEvent() {
        assertTrue(Double.isNaN(Statistics.getAnnualRiskFromTimeToEvent(0.0)));
        double risk = Statistics.getAnnualRiskFromTimeToEvent(2.0);
        assertTrue(risk > 0.0 && risk < 1.0);
    }

    @Test
    void shouldComputeSdFrom95CI() {
        double sd = Statistics.sdFrom95CI(new double[] {8.0, 12.0});
        assertEquals(1.020408163, sd, 1e-6);
    }

    @Test
    void shouldComputeBetaAndGammaParameters() {
        double[] beta = Statistics.betaParametersFromNormal(0.5, 0.1);
        assertEquals(0.5, beta[0] / (beta[0] + beta[1]), 1e-6);

        double[] gamma = Statistics.gammaParametersFromNormal(2.0, 1.0);
        assertEquals(4.0, gamma[0], 1e-9);
        assertEquals(0.5, gamma[1], 1e-9);
    }

    @Test
    void shouldComputeBetaParametersFromEmpiricData() {
        double[] beta = Statistics.betaParametersFromEmpiricData(0.5, 0.6, 0.0, 1.0);
        assertEquals(2, beta.length);
        assertTrue(beta[0] > 0.0);
        assertTrue(beta[1] > 0.0);
    }

    @Test
    void shouldComputeBetaModeFromMeanSd() {
        double mode = Statistics.betaModeFromMeanSD(0.6, 0.1);
        assertTrue(mode > 0.0 && mode < 1.0);
    }

    @Test
    void shouldComputeWeightedAverage() {
        assertEquals(3.0, Statistics.weightedAverage(new double[] {1.0, 1.0}, new double[] {2.0, 4.0}), 1e-9);
        assertTrue(Double.isNaN(Statistics.weightedAverage(new double[] {1.0}, new double[] {})));
        assertTrue(Double.isNaN(Statistics.weightedAverage(new double[] {1.0}, new double[] {2.0, 3.0})));
    }

    @Test
    void shouldComputeWeightedPercentile() {
        double[] weights = new double[] {1.0, 1.0, 1.0};
        double[] values = new double[] {1.0, 2.0, 3.0};
        assertEquals(2.0, Statistics.weightedPercentile(weights, values, 0.5, true), 1e-9);
        assertEquals(2.0, Statistics.weightedPercentile(weights, values, 0.5, false), 1e-9);

        assertTrue(Double.isNaN(Statistics.weightedPercentile(weights, values, 0.0, true)));
        assertTrue(Double.isNaN(Statistics.weightedPercentile(new double[] {}, new double[] {}, 0.5, true)));
        assertTrue(Double.isNaN(Statistics.weightedPercentile(new double[] {1.0}, new double[] {2.0, 3.0}, 0.5, true)));
        assertTrue(Double.isNaN(Statistics.weightedPercentile(new double[] {0.0, 0.0}, new double[] {1.0, 2.0}, 0.5, true)));
        assertTrue(Double.isNaN(Statistics.weightedPercentile(weights, values, 1.1, true)));

        assertEquals(4.0, Statistics.weightedPercentile(new double[] {1.0}, new double[] {4.0}, 0.5, true), 1e-9);
    }
}
