package es.ull.simulation.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PrioritizedMapTest {

    private static final class Item implements Prioritizable {
        private final int priority;

        Item(int priority) {
            this.priority = priority;
        }

        @Override
        public int getPriority() {
            return priority;
        }
    }

    @Test
    void shouldAddRemoveAndClearItems() {
        PrioritizedTable<Item> table = new PrioritizedTable<>();
        Item a = new Item(0);
        Item b = new Item(1);

        table.add(a);
        table.add(b);
        assertEquals(2, table.size());

        table.remove(a);
        assertEquals(1, table.size());

        table.clear();
        assertEquals(0, table.size());
    }
}
