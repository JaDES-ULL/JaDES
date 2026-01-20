package es.ull.simulation.utils.cycle;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import es.ull.simulation.functions.AbstractTimeFunction;
import es.ull.simulation.functions.TimeFunctionParams;

class RoundedPeriodicCycleTest {

    private static final class ConstantTimeFunction extends AbstractTimeFunction {
        private final double value;

        ConstantTimeFunction(double value) {
            this.value = value;
        }

        @Override
        public double getValue(TimeFunctionParams params) {
            return value;
        }

        @Override
        public void setParameters(Object... params) {
        }
    }

    @Test
    void shouldRoundToScaleAndShift() {
        RoundedPeriodicCycle cycle = new RoundedPeriodicCycle(0.0, new ConstantTimeFunction(4.0), 15.0,
                RoundedPeriodicCycle.Type.ROUND, 5.0, 1.0);
        CycleIterator iter = cycle.iterator(0.0, 15.0);

        assertEquals(1.0, iter.next(), 1e-9);
        assertEquals(6.0, iter.next(), 1e-9);
        assertEquals(11.0, iter.next(), 1e-9);
    }

    @Test
    void shouldRoundDiscreteIterator() {
        RoundedPeriodicCycle cycle = new RoundedPeriodicCycle(0.0, new ConstantTimeFunction(3.0), 3,
                RoundedPeriodicCycle.Type.CEIL, 5.0, 1.0);
        DiscreteCycleIterator iter = cycle.iterator(0L, 20L);

        assertEquals(1L, iter.next());
        assertEquals(6L, iter.next());
        assertEquals(11L, iter.next());
        assertEquals(-1L, iter.next());
    }

    @Test
    void shouldExposeConfigurationAndFloorValues() {
        RoundedPeriodicCycle cycle = new RoundedPeriodicCycle(0.0, new ConstantTimeFunction(6.9), 1,
                RoundedPeriodicCycle.Type.FLOOR, 5.0, 0.0);
        assertEquals(RoundedPeriodicCycle.Type.FLOOR, cycle.getType());
        assertEquals(5.0, cycle.getScale(), 1e-9);
        assertEquals(0.0, cycle.getShift(), 1e-9);

        CycleIterator iter = cycle.iterator(0.0, 10.0);
        assertEquals(0.0, iter.next(), 1e-9);
    }
}
