package es.ull.simulation.model;

import es.ull.simulation.functions.AbstractTimeFunction;
import es.ull.simulation.functions.ConstantFunction;
import es.ull.simulation.utils.cycle.RoundedPeriodicCycle.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for SimulationRoundedPeriodicCycle.
 */
class SimulationRoundedPeriodicCycleTest {
    private TimeUnit timeUnit;
    private AbstractTimeFunction period;
    private Type type;

    @BeforeEach
    void setUp() {
        timeUnit = TimeUnit.MINUTE;
        period = new ConstantFunction(10.0);
        type = Type.FLOOR;
    }

    @Test
    void shouldCreateSimulationRoundedPeriodicCycle_withLongParameters() {
        long startTs = 0;
        long endTs = 100;
        long scale = 5;
        long shift = 0;

        SimulationRoundedPeriodicCycle cycle = new SimulationRoundedPeriodicCycle(
            timeUnit, startTs, period, endTs, type, scale, shift);

        assertNotNull(cycle);
        assertNotNull(cycle.getCycle());
    }

    @Test
    void shouldCreateSimulationRoundedPeriodicCycle_withTimeStampParameters() {
        TimeStamp startTs = new TimeStamp(timeUnit, 0);
        TimeStamp endTs = new TimeStamp(timeUnit, 100);
        TimeStamp scale = new TimeStamp(timeUnit, 5);
        TimeStamp shift = new TimeStamp(timeUnit, 0);

        SimulationRoundedPeriodicCycle cycle = new SimulationRoundedPeriodicCycle(
            timeUnit, startTs, period, endTs, type, scale, shift);

        assertNotNull(cycle);
        assertNotNull(cycle.getCycle());
    }

    @Test
    void shouldCreateSimulationRoundedPeriodicCycle_withIterationsLong() {
        long startTs = 0;
        int iterations = 10;
        long scale = 5;
        long shift = 0;

        SimulationRoundedPeriodicCycle cycle = new SimulationRoundedPeriodicCycle(
            timeUnit, startTs, period, iterations, type, scale, shift);

        assertNotNull(cycle);
        assertNotNull(cycle.getCycle());
    }

    @Test
    void shouldCreateSimulationRoundedPeriodicCycle_withIterationsTimeStamp() {
        TimeStamp startTs = new TimeStamp(timeUnit, 0);
        int iterations = 10;
        TimeStamp scale = new TimeStamp(timeUnit, 5);
        TimeStamp shift = new TimeStamp(timeUnit, 0);

        SimulationRoundedPeriodicCycle cycle = new SimulationRoundedPeriodicCycle(
            timeUnit, startTs, period, iterations, type, scale, shift);

        assertNotNull(cycle);
        assertNotNull(cycle.getCycle());
    }

    @Test
    void shouldCreateSimulationRoundedPeriodicCycle_withFloorType() {
        SimulationRoundedPeriodicCycle cycle = new SimulationRoundedPeriodicCycle(
            timeUnit, 0, period, 100, Type.FLOOR, 5, 0);

        assertNotNull(cycle);
        assertNotNull(cycle.getCycle());
    }

    @Test
    void shouldCreateSimulationRoundedPeriodicCycle_withCeilType() {
        SimulationRoundedPeriodicCycle cycle = new SimulationRoundedPeriodicCycle(
            timeUnit, 0, period, 100, Type.CEIL, 5, 0);

        assertNotNull(cycle);
        assertNotNull(cycle.getCycle());
    }

    @Test
    void shouldCreateSimulationRoundedPeriodicCycle_withRoundType() {
        SimulationRoundedPeriodicCycle cycle = new SimulationRoundedPeriodicCycle(
            timeUnit, 0, period, 100, Type.ROUND, 5, 0);

        assertNotNull(cycle);
        assertNotNull(cycle.getCycle());
    }

    @Test
    void shouldCreateSimulationRoundedPeriodicCycle_withZeroIterations() {
        SimulationRoundedPeriodicCycle cycle = new SimulationRoundedPeriodicCycle(
            timeUnit, 0, period, 0, type, 5, 0);

        assertNotNull(cycle);
        assertNotNull(cycle.getCycle());
    }

    @Test
    void shouldCreateSimulationRoundedPeriodicCycle_withLargeScale() {
        SimulationRoundedPeriodicCycle cycle = new SimulationRoundedPeriodicCycle(
            timeUnit, 0, period, 100, type, 100, 0);

        assertNotNull(cycle);
        assertNotNull(cycle.getCycle());
    }

    @Test
    void shouldCreateSimulationRoundedPeriodicCycle_withShift() {
        SimulationRoundedPeriodicCycle cycle = new SimulationRoundedPeriodicCycle(
            timeUnit, 0, period, 100, type, 5, 2);

        assertNotNull(cycle);
        assertNotNull(cycle.getCycle());
    }

    @Test
    void shouldCreateSimulationRoundedPeriodicCycle_withDifferentTimeUnits() {
        SimulationRoundedPeriodicCycle cycle1 = new SimulationRoundedPeriodicCycle(
            TimeUnit.MINUTE, 0, period, 100, type, 5, 0);
        SimulationRoundedPeriodicCycle cycle2 = new SimulationRoundedPeriodicCycle(
            TimeUnit.HOUR, 0, period, 100, type, 5, 0);
        SimulationRoundedPeriodicCycle cycle3 = new SimulationRoundedPeriodicCycle(
            TimeUnit.DAY, 0, period, 100, type, 5, 0);

        assertNotNull(cycle1);
        assertNotNull(cycle2);
        assertNotNull(cycle3);
    }

    @Test
    void shouldImplementISimulationCycle() {
        SimulationRoundedPeriodicCycle cycle = new SimulationRoundedPeriodicCycle(
            timeUnit, 0, period, 100, type, 5, 0);

        assertTrue(cycle instanceof ISimulationCycle);
    }

    @Test
    void shouldReturnNonNullCycle() {
        SimulationRoundedPeriodicCycle cycle = new SimulationRoundedPeriodicCycle(
            timeUnit, 0, period, 100, type, 5, 0);

        assertNotNull(cycle.getCycle());
    }

    @Test
    void shouldCreateSimulationRoundedPeriodicCycle_withDifferentPeriods() {
        AbstractTimeFunction period1 = new ConstantFunction(5.0);
        AbstractTimeFunction period2 = new ConstantFunction(10.0);
        AbstractTimeFunction period3 = new ConstantFunction(20.0);

        SimulationRoundedPeriodicCycle cycle1 = new SimulationRoundedPeriodicCycle(
            timeUnit, 0, period1, 100, type, 5, 0);
        SimulationRoundedPeriodicCycle cycle2 = new SimulationRoundedPeriodicCycle(
            timeUnit, 0, period2, 100, type, 5, 0);
        SimulationRoundedPeriodicCycle cycle3 = new SimulationRoundedPeriodicCycle(
            timeUnit, 0, period3, 100, type, 5, 0);

        assertNotNull(cycle1);
        assertNotNull(cycle2);
        assertNotNull(cycle3);
    }
}
