package es.ull.simulation.utils.cycle;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TableCycleTest {

    @Test
    void shouldIterateOverTimestamps() {
        TableCycle cycle = new TableCycle(new double[] {1.0, 3.0, 5.0});
        CycleIterator iter = cycle.iterator(0.0, 10.0);

        assertEquals(1.0, iter.next(), 1e-9);
        assertEquals(3.0, iter.next(), 1e-9);
        assertEquals(5.0, iter.next(), 1e-9);
        assertTrue(Double.isNaN(iter.next()));
    }

    @Test
    void shouldIterateOverDiscreteTimestamps() {
        TableCycle cycle = new TableCycle(new double[] {1.0, 3.0, 5.0});
        DiscreteCycleIterator iter = cycle.iterator(0L, 10L);

        assertEquals(1L, iter.next());
        assertEquals(3L, iter.next());
        assertEquals(5L, iter.next());
        assertEquals(-1L, iter.next());
    }

    @Test
    void shouldHandleDiscreteEndMinusOne() {
        TableCycle cycle = new TableCycle(new double[] {1.0});
        DiscreteCycleIterator iter = cycle.iterator(0L, -1L);

        assertEquals(-1L, iter.next());
    }

    @Test
    void shouldIterateWithSubcycle() {
        TableCycle sub = new TableCycle(new double[] {0.0, 2.0});
        TableCycle cycle = new TableCycle(new double[] {1.0, 4.0}, sub);
        DiscreteCycleIterator iter = cycle.iterator(0L, 10L);

        assertEquals(1L, iter.next());
        assertEquals(3L, iter.next());
        assertEquals(-1L, iter.next());
    }
}
