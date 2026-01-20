package es.ull.simulation.utils.cycle;

import static org.junit.jupiter.api.Assertions.*;

import java.util.EnumSet;

import org.junit.jupiter.api.Test;

class WeeklyPeriodicCycleTest {

    @Test
    void shouldIterateSelectedWeekDays() {
        WeeklyPeriodicCycle cycle = new WeeklyPeriodicCycle(
                EnumSet.of(WeeklyPeriodicCycle.WeekDays.MONDAY, WeeklyPeriodicCycle.WeekDays.WEDNESDAY),
                10.0,
                0.0,
                100.0);

        CycleIterator iter = cycle.iterator(0.0, 100.0);
        double first = iter.next();
        double second = iter.next();

        assertEquals(0.0, first, 1e-9);
        assertEquals(20.0, second, 1e-9);
    }

    @Test
    void shouldExposeDaySetAndDiscreteIteration() {
        WeeklyPeriodicCycle cycle = new WeeklyPeriodicCycle(
                EnumSet.of(WeeklyPeriodicCycle.WeekDays.FRIDAY),
                24.0,
                0.0,
                2);

        assertEquals(EnumSet.of(WeeklyPeriodicCycle.WeekDays.FRIDAY), cycle.getDaySet());

        DiscreteCycleIterator iter = cycle.iterator(0L, 400L);
        assertEquals(96L, iter.next());
        assertEquals(96L + 168L, iter.next());
        assertEquals(-1L, iter.next());
    }
}
