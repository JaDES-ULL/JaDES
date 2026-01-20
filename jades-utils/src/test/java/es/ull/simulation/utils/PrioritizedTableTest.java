package es.ull.simulation.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.jupiter.api.Test;

class PrioritizedTableTest {

    private static final class Task implements Prioritizable {
        private final int priority;
        private final String name;

        private Task(int priority, String name) {
            this.priority = priority;
            this.name = name;
        }

        @Override
        public int getPriority() {
            return priority;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @Test
    void shouldIterateInPriorityOrder() {
        PrioritizedTable<Task> table = new PrioritizedTable<>();
        Task a = new Task(0, "A");
        Task b = new Task(0, "B");
        Task c = new Task(1, "C");
        table.add(c);
        table.add(a);
        table.add(b);

        ArrayList<Task> ordered = new ArrayList<>();
        for (Task t : table) {
            ordered.add(t);
        }

        assertEquals(3, ordered.size());
        assertTrue(ordered.contains(a));
        assertTrue(ordered.contains(b));
        assertTrue(ordered.contains(c));
    }

    @Test
    void balancedIteratorShouldReturnAllElements() {
        PrioritizedTable<Task> table = new PrioritizedTable<>();
        table.add(new Task(0, "A"));
        table.add(new Task(0, "B"));
        table.add(new Task(1, "C"));

        Iterator<Task> it = table.balancedIterator();
        Set<Task> seen = new HashSet<>();
        seen.add(it.next());
        seen.add(it.next());
        seen.add(it.next());
        assertEquals(3, seen.size());
    }

    @Test
    void randomIteratorShouldReturnAllElements() {
        PrioritizedTable<Task> table = new PrioritizedTable<>();
        table.add(new Task(0, "A"));
        table.add(new Task(0, "B"));
        table.add(new Task(1, "C"));

        Iterator<Task> it = table.randomIterator();
        Set<Task> seen = new HashSet<>();
        while (it.hasNext()) {
            seen.add(it.next());
        }
        assertEquals(3, seen.size());
    }
}
