package es.ull.simulation.utils.cycle;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import es.ull.simulation.functions.AbstractTimeFunction;
import es.ull.simulation.functions.TimeFunctionParams;

class PeriodicCycleDiscreteIteratorTest {

    private static final class ConstantTimeFunction extends AbstractTimeFunction {
        private double value;

        ConstantTimeFunction(double value) {
            this.value = value;
        }

        @Override
        public double getValue(TimeFunctionParams params) {
            return value;
        }

        @Override
        public void setParameters(Object... params) {
            this.value = (double) params[0];
        }
    }

    @Test
    void shouldIterateDiscreteWithStartOffset() {
        PeriodicCycle cycle = new PeriodicCycle(5.0, new ConstantTimeFunction(2.0), 3);
        DiscreteCycleIterator iter = cycle.iterator(0L, 20L);

        assertEquals(5L, iter.next());
        assertEquals(7L, iter.next());
        assertEquals(9L, iter.next());
        assertEquals(-1L, iter.next());
    }

    @Test
    void shouldSkipUntilAbsoluteStart() {
        PeriodicCycle cycle = new PeriodicCycle(5.0, new ConstantTimeFunction(2.0), 3);
        DiscreteCycleIterator iter = cycle.iterator(8L, 20L);

        assertEquals(9L, iter.next());
        assertEquals(-1L, iter.next());
    }

    @Test
    void shouldStopWhenEndReached() {
        PeriodicCycle cycle = new PeriodicCycle(5.0, new ConstantTimeFunction(2.0), 3);
        DiscreteCycleIterator iter = cycle.iterator(0L, 6L);

        assertEquals(5L, iter.next());
        assertEquals(-1L, iter.next());
    }

    @Test
    void shouldReturnMinusOneWhenAbsEndIsMinusOne() {
        PeriodicCycle cycle = new PeriodicCycle(0.0, new ConstantTimeFunction(1.0), 3);
        DiscreteCycleIterator iter = cycle.iterator(0L, -1L);

        assertEquals(-1L, iter.next());
    }

    @Test
    void shouldHandleInfiniteIterationsWithinEnd() {
        PeriodicCycle cycle = new PeriodicCycle(0.0, new ConstantTimeFunction(1.0), 0);
        DiscreteCycleIterator iter = cycle.iterator(0L, 3L);

        assertEquals(0L, iter.next());
        assertEquals(1L, iter.next());
        assertEquals(2L, iter.next());
        assertEquals(-1L, iter.next());
    }
}
