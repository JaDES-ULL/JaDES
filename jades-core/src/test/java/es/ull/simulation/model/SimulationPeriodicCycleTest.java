package es.ull.simulation.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.ull.simulation.functions.ConstantFunction;

/**
 * Test class for {@link SimulationPeriodicCycle}.
 * Tests periodic cycle functionality with timestamps and iterations.
 */
public class SimulationPeriodicCycleTest {

    private TimeUnit unit;

    @BeforeEach
    public void setUp() {
        unit = TimeUnit.MINUTE;
    }

    @Test
    public void shouldCreateCycleWithEndTimestamp() {
        // Given: start time, period, and end time
        TimeStamp startTs = new TimeStamp(unit, 0);
        ConstantFunction period = new ConstantFunction(10);
        TimeStamp endTs = new TimeStamp(unit, 100);

        // When: creating a periodic cycle with end timestamp
        SimulationPeriodicCycle cycle = new SimulationPeriodicCycle(unit, startTs, period, endTs);

        // Then: it should be created successfully
        assertNotNull(cycle);
        assertNotNull(cycle.getCycle());
    }

    @Test
    public void shouldCreateCycleWithIterations() {
        // Given: start time, period, and iteration count
        TimeStamp startTs = new TimeStamp(unit, 0);
        ConstantFunction period = new ConstantFunction(10);
        int iterations = 5;

        // When: creating a periodic cycle with iterations
        SimulationPeriodicCycle cycle = new SimulationPeriodicCycle(unit, startTs, period, iterations);

        // Then: it should be created successfully
        assertNotNull(cycle);
        assertNotNull(cycle.getCycle());
    }

    @Test
    public void shouldCreateCycleWithSubCycleAndEndTimestamp() {
        // Given: a subcycle
        TimeStamp subStartTs = new TimeStamp(unit, 0);
        ConstantFunction subPeriod = new ConstantFunction(5);
        TimeStamp subEndTs = new TimeStamp(unit, 50);
        SimulationPeriodicCycle subCycle = new SimulationPeriodicCycle(unit, subStartTs, subPeriod, subEndTs);

        // And: main cycle parameters
        TimeStamp startTs = new TimeStamp(unit, 0);
        ConstantFunction period = new ConstantFunction(20);
        TimeStamp endTs = new TimeStamp(unit, 100);

        // When: creating a cycle with subcycle and end timestamp
        SimulationPeriodicCycle cycle = new SimulationPeriodicCycle(unit, startTs, period, endTs, subCycle);

        // Then: it should be created successfully
        assertNotNull(cycle);
        assertNotNull(cycle.getCycle());
    }

    @Test
    public void shouldCreateCycleWithSubCycleAndIterations() {
        // Given: a subcycle
        TimeStamp subStartTs = new TimeStamp(unit, 0);
        ConstantFunction subPeriod = new ConstantFunction(5);
        int subIterations = 3;
        SimulationPeriodicCycle subCycle = new SimulationPeriodicCycle(unit, subStartTs, subPeriod, subIterations);

        // And: main cycle parameters
        TimeStamp startTs = new TimeStamp(unit, 0);
        ConstantFunction period = new ConstantFunction(20);
        int iterations = 4;

        // When: creating a cycle with subcycle and iterations
        SimulationPeriodicCycle cycle = new SimulationPeriodicCycle(unit, startTs, period, iterations, subCycle);

        // Then: it should be created successfully
        assertNotNull(cycle);
        assertNotNull(cycle.getCycle());
    }

    @Test
    public void shouldHandleZeroIterations() {
        // Given: cycle with zero iterations (infinite)
        TimeStamp startTs = new TimeStamp(unit, 0);
        ConstantFunction period = new ConstantFunction(10);
        int iterations = 0;

        // When: creating a cycle with zero iterations
        SimulationPeriodicCycle cycle = new SimulationPeriodicCycle(unit, startTs, period, iterations);

        // Then: it should be created successfully (0 means infinite)
        assertNotNull(cycle);
        assertNotNull(cycle.getCycle());
    }

    @Test
    public void shouldHandleDifferentTimeUnits() {
        // Given: cycles with different time units
        TimeStamp startTsMinutes = new TimeStamp(TimeUnit.MINUTE, 0);
        ConstantFunction periodMinutes = new ConstantFunction(5);
        TimeStamp endTsMinutes = new TimeStamp(TimeUnit.MINUTE, 50);

        TimeStamp startTsHours = new TimeStamp(TimeUnit.HOUR, 0);
        ConstantFunction periodHours = new ConstantFunction(1);
        TimeStamp endTsHours = new TimeStamp(TimeUnit.HOUR, 10);

        // When: creating cycles with different units
        SimulationPeriodicCycle cycleMinutes = new SimulationPeriodicCycle(TimeUnit.MINUTE, startTsMinutes, periodMinutes, endTsMinutes);
        SimulationPeriodicCycle cycleHours = new SimulationPeriodicCycle(TimeUnit.HOUR, startTsHours, periodHours, endTsHours);

        // Then: both should be created successfully
        assertNotNull(cycleMinutes);
        assertNotNull(cycleMinutes.getCycle());
        assertNotNull(cycleHours);
        assertNotNull(cycleHours.getCycle());
    }

    @Test
    public void shouldReturnInnerCycle() {
        // Given: a periodic cycle
        TimeStamp startTs = new TimeStamp(unit, 0);
        ConstantFunction period = new ConstantFunction(10);
        TimeStamp endTs = new TimeStamp(unit, 100);
        SimulationPeriodicCycle cycle = new SimulationPeriodicCycle(unit, startTs, period, endTs);

        // When: getting the inner cycle
        var innerCycle = cycle.getCycle();

        // Then: it should return a non-null Cycle
        assertNotNull(innerCycle);
    }

    @Test
    public void shouldHandleNonZeroStartTime() {
        // Given: cycle with non-zero start time
        TimeStamp startTs = new TimeStamp(unit, 50);
        ConstantFunction period = new ConstantFunction(10);
        TimeStamp endTs = new TimeStamp(unit, 150);

        // When: creating the cycle
        SimulationPeriodicCycle cycle = new SimulationPeriodicCycle(unit, startTs, period, endTs);

        // Then: it should be created successfully
        assertNotNull(cycle);
        assertNotNull(cycle.getCycle());
    }

    @Test
    public void shouldHandleLargePeriod() {
        // Given: cycle with large period
        TimeStamp startTs = new TimeStamp(unit, 0);
        ConstantFunction period = new ConstantFunction(1000);
        TimeStamp endTs = new TimeStamp(unit, 10000);

        // When: creating the cycle
        SimulationPeriodicCycle cycle = new SimulationPeriodicCycle(unit, startTs, period, endTs);

        // Then: it should be created successfully
        assertNotNull(cycle);
        assertNotNull(cycle.getCycle());
    }

    @Test
    public void shouldHandleOneIteration() {
        // Given: cycle with single iteration
        TimeStamp startTs = new TimeStamp(unit, 0);
        ConstantFunction period = new ConstantFunction(10);
        int iterations = 1;

        // When: creating the cycle
        SimulationPeriodicCycle cycle = new SimulationPeriodicCycle(unit, startTs, period, iterations);

        // Then: it should be created successfully
        assertNotNull(cycle);
        assertNotNull(cycle.getCycle());
    }
}
