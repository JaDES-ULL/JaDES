package es.ull.simulation.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for SimulationTimeFunction.
 *
 * Coverage target: setParameters() method and constructor edge cases
 */
class SimulationTimeFunctionTest {

    @Test
    void shouldCreateSimulationTimeFunction_withConstantValue() {
        // Test constructor with ConstantVariate
        SimulationTimeFunction function = new SimulationTimeFunction(
            TimeUnit.MINUTE,
            "ConstantVariate",
            5.0
        );

        assertNotNull(function);
    }

    @Test
    void shouldCreateSimulationTimeFunction_withTimeStampParameter() {
        // Test constructor with TimeStamp parameter
        TimeStamp timestamp = new TimeStamp(TimeUnit.MINUTE, 10);
        SimulationTimeFunction function = new SimulationTimeFunction(
            TimeUnit.MINUTE,
            "ConstantVariate",
            timestamp
        );

        assertNotNull(function);
    }

    @Test
    void shouldCreateSimulationTimeFunction_withNumberParameter() {
        // Test constructor with Number parameter
        SimulationTimeFunction function = new SimulationTimeFunction(
            TimeUnit.HOUR,
            "ConstantVariate",
            42
        );

        assertNotNull(function);
    }

    @Test
    void shouldCreateSimulationTimeFunction_withMultipleParameters() {
        // Test constructor with multiple parameters of different types
        TimeStamp timestamp = new TimeStamp(TimeUnit.SECOND, 30);
        SimulationTimeFunction function = new SimulationTimeFunction(
            TimeUnit.SECOND,
            "UniformVariate",
            1.0,
            timestamp
        );

        assertNotNull(function);
    }

    @Test
    void shouldCallSetParameters_withNoEffect() {
        // Test setParameters method (currently a no-op / TODO method)
        SimulationTimeFunction function = new SimulationTimeFunction(
            TimeUnit.DAY,
            "ConstantVariate",
            7.0
        );

        // Should not throw exception even though method is empty
        assertDoesNotThrow(() -> function.setParameters(1.0, 2.0, 3.0));
    }

    @Test
    void shouldCallSetParameters_withEmptyParameters() {
        // Test setParameters with no parameters
        SimulationTimeFunction function = new SimulationTimeFunction(
            TimeUnit.YEAR,
            "ConstantVariate",
            365.0
        );

        assertDoesNotThrow(() -> function.setParameters());
    }

    @Test
    void shouldCallGetValue_returnsInnerFunctionValue() {
        // Test getValue delegates to inner function
        SimulationTimeFunction function = new SimulationTimeFunction(
            TimeUnit.MINUTE,
            "ConstantVariate",
            100.0
        );

        // Create a mock TimeFunctionParams (can be null for ConstantVariate)
        double value = function.getValue(null);

        // ConstantVariate should return the constant value
        assertEquals(100.0, value, 0.001);
    }

    @Test
    void shouldCreateSimulationTimeFunction_withZeroValue() {
        // Edge case: zero value
        SimulationTimeFunction function = new SimulationTimeFunction(
            TimeUnit.MINUTE,
            "ConstantVariate",
            0.0
        );

        double value = function.getValue(null);
        assertEquals(0.0, value, 0.001);
    }

    @Test
    void shouldCreateSimulationTimeFunction_withNegativeValue() {
        // Edge case: negative value
        SimulationTimeFunction function = new SimulationTimeFunction(
            TimeUnit.HOUR,
            "ConstantVariate",
            -5.0
        );

        double value = function.getValue(null);
        assertEquals(-5.0, value, 0.001);
    }

    @Test
    void shouldCreateSimulationTimeFunction_withDifferentTimeUnits() {
        // Test with different time units
        SimulationTimeFunction minuteFunction = new SimulationTimeFunction(
            TimeUnit.MINUTE,
            "ConstantVariate",
            60.0
        );

        SimulationTimeFunction secondFunction = new SimulationTimeFunction(
            TimeUnit.SECOND,
            "ConstantVariate",
            3600.0
        );

        assertNotNull(minuteFunction);
        assertNotNull(secondFunction);
    }
}
