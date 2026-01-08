package es.ull.simulation.model;

import es.ull.simulation.utils.cycle.WeeklyPeriodicCycle.WeekDays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for SimulationWeeklyPeriodicCycle.
 */
class SimulationWeeklyPeriodicCycleTest {
    private TimeUnit timeUnit;
    private EnumSet<WeekDays> daySet;

    @BeforeEach
    void setUp() {
        timeUnit = TimeUnit.MINUTE;
        daySet = EnumSet.of(WeekDays.MONDAY, WeekDays.WEDNESDAY, WeekDays.FRIDAY);
    }

    @Test
    void shouldCreateSimulationWeeklyPeriodicCycle_withTimeStampEndTs() {
        TimeStamp startTs = new TimeStamp(timeUnit, 0);
        TimeStamp endTs = new TimeStamp(timeUnit, 10080); // 1 week in minutes
        
        SimulationWeeklyPeriodicCycle cycle = new SimulationWeeklyPeriodicCycle(
            timeUnit, daySet, startTs, endTs);
        
        assertNotNull(cycle);
        assertNotNull(cycle.getCycle());
    }

    @Test
    void shouldCreateSimulationWeeklyPeriodicCycle_withTimeStampIterations() {
        TimeStamp startTs = new TimeStamp(timeUnit, 0);
        int iterations = 4;
        
        SimulationWeeklyPeriodicCycle cycle = new SimulationWeeklyPeriodicCycle(
            timeUnit, daySet, startTs, iterations);
        
        assertNotNull(cycle);
        assertNotNull(cycle.getCycle());
    }

    @Test
    void shouldCreateSimulationWeeklyPeriodicCycle_withLongEndTs() {
        long startTs = 0;
        long endTs = 10080; // 1 week in minutes
        
        SimulationWeeklyPeriodicCycle cycle = new SimulationWeeklyPeriodicCycle(
            timeUnit, daySet, startTs, endTs);
        
        assertNotNull(cycle);
        assertNotNull(cycle.getCycle());
    }

    @Test
    void shouldCreateSimulationWeeklyPeriodicCycle_withLongIterations() {
        long startTs = 0;
        int iterations = 4;
        
        SimulationWeeklyPeriodicCycle cycle = new SimulationWeeklyPeriodicCycle(
            timeUnit, daySet, startTs, iterations);
        
        assertNotNull(cycle);
        assertNotNull(cycle.getCycle());
    }

    @Test
    void shouldCreateSimulationWeeklyPeriodicCycle_withSingleDay() {
        EnumSet<WeekDays> singleDay = EnumSet.of(WeekDays.MONDAY);
        
        SimulationWeeklyPeriodicCycle cycle = new SimulationWeeklyPeriodicCycle(
            timeUnit, singleDay, 0, 10080);
        
        assertNotNull(cycle);
        assertNotNull(cycle.getCycle());
    }

    @Test
    void shouldCreateSimulationWeeklyPeriodicCycle_withAllWeekDays() {
        EnumSet<WeekDays> allDays = EnumSet.allOf(WeekDays.class);
        
        SimulationWeeklyPeriodicCycle cycle = new SimulationWeeklyPeriodicCycle(
            timeUnit, allDays, 0, 10080);
        
        assertNotNull(cycle);
        assertNotNull(cycle.getCycle());
    }

    @Test
    void shouldCreateSimulationWeeklyPeriodicCycle_withWeekendDays() {
        EnumSet<WeekDays> weekendDays = EnumSet.of(WeekDays.SATURDAY, WeekDays.SUNDAY);
        
        SimulationWeeklyPeriodicCycle cycle = new SimulationWeeklyPeriodicCycle(
            timeUnit, weekendDays, 0, 10080);
        
        assertNotNull(cycle);
        assertNotNull(cycle.getCycle());
    }

    @Test
    void shouldCreateSimulationWeeklyPeriodicCycle_withWeekDays() {
        EnumSet<WeekDays> weekDays = EnumSet.of(
            WeekDays.MONDAY, WeekDays.TUESDAY, WeekDays.WEDNESDAY, 
            WeekDays.THURSDAY, WeekDays.FRIDAY);
        
        SimulationWeeklyPeriodicCycle cycle = new SimulationWeeklyPeriodicCycle(
            timeUnit, weekDays, 0, 10080);
        
        assertNotNull(cycle);
        assertNotNull(cycle.getCycle());
    }

    @Test
    void shouldCreateSimulationWeeklyPeriodicCycle_withZeroIterations() {
        SimulationWeeklyPeriodicCycle cycle = new SimulationWeeklyPeriodicCycle(
            timeUnit, daySet, 0, 0);
        
        assertNotNull(cycle);
        assertNotNull(cycle.getCycle());
    }

    @Test
    void shouldCreateSimulationWeeklyPeriodicCycle_withLargeIterations() {
        SimulationWeeklyPeriodicCycle cycle = new SimulationWeeklyPeriodicCycle(
            timeUnit, daySet, 0, 100);
        
        assertNotNull(cycle);
        assertNotNull(cycle.getCycle());
    }

    @Test
    void shouldCreateSimulationWeeklyPeriodicCycle_withDifferentTimeUnits() {
        SimulationWeeklyPeriodicCycle cycle1 = new SimulationWeeklyPeriodicCycle(
            TimeUnit.MINUTE, daySet, 0, 10080);
        SimulationWeeklyPeriodicCycle cycle2 = new SimulationWeeklyPeriodicCycle(
            TimeUnit.HOUR, daySet, 0, 168);
        SimulationWeeklyPeriodicCycle cycle3 = new SimulationWeeklyPeriodicCycle(
            TimeUnit.DAY, daySet, 0, 7);
        
        assertNotNull(cycle1);
        assertNotNull(cycle2);
        assertNotNull(cycle3);
    }

    @Test
    void shouldImplementISimulationCycle() {
        SimulationWeeklyPeriodicCycle cycle = new SimulationWeeklyPeriodicCycle(
            timeUnit, daySet, 0, 10080);
        
        assertTrue(cycle instanceof ISimulationCycle);
    }

    @Test
    void shouldReturnNonNullCycle() {
        SimulationWeeklyPeriodicCycle cycle = new SimulationWeeklyPeriodicCycle(
            timeUnit, daySet, 0, 10080);
        
        assertNotNull(cycle.getCycle());
    }

    @Test
    void shouldCreateMultipleSimulationWeeklyPeriodicCycles() {
        SimulationWeeklyPeriodicCycle cycle1 = new SimulationWeeklyPeriodicCycle(
            timeUnit, daySet, 0, 10080);
        SimulationWeeklyPeriodicCycle cycle2 = new SimulationWeeklyPeriodicCycle(
            timeUnit, EnumSet.of(WeekDays.MONDAY), 100, 10180);
        SimulationWeeklyPeriodicCycle cycle3 = new SimulationWeeklyPeriodicCycle(
            timeUnit, EnumSet.allOf(WeekDays.class), 200, 10280);
        
        assertNotNull(cycle1);
        assertNotNull(cycle2);
        assertNotNull(cycle3);
    }

    @Test
    void shouldCreateSimulationWeeklyPeriodicCycle_withNonZeroStartTs() {
        long startTs = 1440; // 1 day in minutes
        long endTs = 11520; // 1 week + 1 day in minutes
        
        SimulationWeeklyPeriodicCycle cycle = new SimulationWeeklyPeriodicCycle(
            timeUnit, daySet, startTs, endTs);
        
        assertNotNull(cycle);
        assertNotNull(cycle.getCycle());
    }
}
