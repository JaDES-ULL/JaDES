package es.ull.simulation.utils.cycle;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CycleIteratorTest {

    @Test
    void shouldReturnCycleFromIterator() {
        TableCycle cycle = new TableCycle(new double[] {1.0});
        CycleIterator iter = cycle.iterator(0.0, 10.0);
        assertSame(cycle, iter.getCycle());

        DiscreteCycleIterator dIter = cycle.iterator(0L, 10L);
        assertSame(cycle, dIter.getCycle());
    }
}
