package es.ull.simulation.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for SimulationTableCycle.
 */
class SimulationTableCycleTest {
    private TimeUnit timeUnit;
    private TimeStamp[] timestamps;

    @BeforeEach
    void setUp() {
        timeUnit = TimeUnit.MINUTE;
        timestamps = new TimeStamp[] {
            new TimeStamp(timeUnit, 0),
            new TimeStamp(timeUnit, 10),
            new TimeStamp(timeUnit, 20)
        };
    }

    @Test
    void shouldCreateSimulationTableCycle_withTimestamps() {
        SimulationTableCycle cycle = new SimulationTableCycle(timeUnit, timestamps);
        
        assertNotNull(cycle);
        assertNotNull(cycle.getCycle());
    }

    @Test
    void shouldCreateSimulationTableCycle_withEmptyTimestamps() {
        TimeStamp[] emptyTimestamps = new TimeStamp[0];
        SimulationTableCycle cycle = new SimulationTableCycle(timeUnit, emptyTimestamps);
        
        assertNotNull(cycle);
        assertNotNull(cycle.getCycle());
    }

    @Test
    void shouldCreateSimulationTableCycle_withSingleTimestamp() {
        TimeStamp[] singleTimestamp = new TimeStamp[] { new TimeStamp(timeUnit, 5) };
        SimulationTableCycle cycle = new SimulationTableCycle(timeUnit, singleTimestamp);
        
        assertNotNull(cycle);
        assertNotNull(cycle.getCycle());
    }

    @Test
    void shouldCreateSimulationTableCycle_withManyTimestamps() {
        TimeStamp[] manyTimestamps = new TimeStamp[10];
        for (int i = 0; i < 10; i++) {
            manyTimestamps[i] = new TimeStamp(timeUnit, i * 5);
        }
        SimulationTableCycle cycle = new SimulationTableCycle(timeUnit, manyTimestamps);
        
        assertNotNull(cycle);
        assertNotNull(cycle.getCycle());
    }

    @Test
    void shouldCreateSimulationTableCycle_withSubCycle() {
        SimulationTableCycle subCycle = new SimulationTableCycle(timeUnit, timestamps);
        SimulationTableCycle cycle = new SimulationTableCycle(timeUnit, timestamps, subCycle);
        
        assertNotNull(cycle);
        assertNotNull(cycle.getCycle());
    }

    @Test
    void shouldCreateSimulationTableCycle_withDifferentTimeUnits() {
        TimeUnit hourUnit = TimeUnit.HOUR;
        TimeStamp[] hourTimestamps = new TimeStamp[] {
            new TimeStamp(hourUnit, 0),
            new TimeStamp(hourUnit, 1),
            new TimeStamp(hourUnit, 2)
        };
        SimulationTableCycle cycle = new SimulationTableCycle(hourUnit, hourTimestamps);
        
        assertNotNull(cycle);
        assertNotNull(cycle.getCycle());
    }

    @Test
    void shouldImplementISimulationCycle() {
        SimulationTableCycle cycle = new SimulationTableCycle(timeUnit, timestamps);
        
        assertTrue(cycle instanceof ISimulationCycle);
    }

    @Test
    void shouldReturnNonNullCycle() {
        SimulationTableCycle cycle = new SimulationTableCycle(timeUnit, timestamps);
        
        assertNotNull(cycle.getCycle());
    }

    @Test
    void shouldCreateSimulationTableCycle_withZeroTimestamps() {
        TimeStamp[] zeroTimestamps = new TimeStamp[] { new TimeStamp(timeUnit, 0) };
        SimulationTableCycle cycle = new SimulationTableCycle(timeUnit, zeroTimestamps);
        
        assertNotNull(cycle);
        assertNotNull(cycle.getCycle());
    }

    @Test
    void shouldCreateSimulationTableCycle_withLargeTimestamps() {
        TimeStamp[] largeTimestamps = new TimeStamp[] {
            new TimeStamp(timeUnit, 1000),
            new TimeStamp(timeUnit, 2000),
            new TimeStamp(timeUnit, 3000)
        };
        SimulationTableCycle cycle = new SimulationTableCycle(timeUnit, largeTimestamps);
        
        assertNotNull(cycle);
        assertNotNull(cycle.getCycle());
    }

    @Test
    void shouldCreateMultipleSimulationTableCycles() {
        SimulationTableCycle cycle1 = new SimulationTableCycle(timeUnit, timestamps);
        SimulationTableCycle cycle2 = new SimulationTableCycle(TimeUnit.HOUR, timestamps);
        SimulationTableCycle cycle3 = new SimulationTableCycle(TimeUnit.DAY, timestamps);
        
        assertNotNull(cycle1);
        assertNotNull(cycle2);
        assertNotNull(cycle3);
        assertNotNull(cycle1.getCycle());
        assertNotNull(cycle2.getCycle());
        assertNotNull(cycle3.getCycle());
    }
}
