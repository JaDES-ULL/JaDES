package es.ull.simulation.utils.cycle;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import es.ull.simulation.functions.AbstractTimeFunction;
import es.ull.simulation.functions.TimeFunctionParams;

class PeriodicCycleTest {

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
    void shouldIterateUntilEndTimestamp() {
        PeriodicCycle cycle = new PeriodicCycle(0.0, new ConstantTimeFunction(2.0), 7.0);
        CycleIterator iter = cycle.iterator(0.0, 7.0);

        double first = iter.next();
        double second = iter.next();
        double third = iter.next();

        assertEquals(0.0, first, 1e-9);
        assertEquals(2.0, second, 1e-9);
        assertEquals(4.0, third, 1e-9);
    }

    @Test
    void shouldIterateFixedIterations() {
        PeriodicCycle cycle = new PeriodicCycle(0.0, new ConstantTimeFunction(2.0), 3);
        DiscreteCycleIterator iter = cycle.iterator(0L, Long.MAX_VALUE);

        assertEquals(0L, iter.next());
        assertEquals(2L, iter.next());
        assertEquals(4L, iter.next());
        assertEquals(-1L, iter.next());
    }

    @Test
    void shouldExposeMetadataAndToString() {
        PeriodicCycle byEnd = new PeriodicCycle(1.0, new ConstantTimeFunction(2.0), 10.0);
        assertEquals(1.0, byEnd.getStartTs(), 1e-9);
        assertEquals(10.0, byEnd.getEndTs(), 1e-9);
        assertEquals(0, byEnd.getIterations());
        assertTrue(byEnd.toString().contains("End"));

        PeriodicCycle byIterations = new PeriodicCycle(0.0, new ConstantTimeFunction(1.0), 2);
        assertTrue(Double.isNaN(byIterations.getEndTs()));
        assertEquals(2, byIterations.getIterations());
        assertTrue(byIterations.toString().contains("Iterations"));
    }

    @Test
    void shouldIterateWithSubcycle() {
        PeriodicCycle subCycle = new PeriodicCycle(0.0, new ConstantTimeFunction(1.0), 2);
        PeriodicCycle cycle = new PeriodicCycle(0.0, new ConstantTimeFunction(3.0), 2, subCycle);
        CycleIterator iter = cycle.iterator(0.0, 10.0);

        assertEquals(0.0, iter.next(), 1e-9);
        assertEquals(1.0, iter.next(), 1e-9);
        assertEquals(3.0, iter.next(), 1e-9);
        assertEquals(4.0, iter.next(), 1e-9);
    }
}
