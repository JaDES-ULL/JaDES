package es.ull.simulation.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DiscreteEvent and its inner classes
 */
class DiscreteEventTest {

    /**
     * Simple IEventSource mock for testing
     */
    private static class MockEventSource implements IEventSource {
        @Override
        public DiscreteEvent onCreate(long ts) {
            return new DiscreteEvent.DefaultStartEvent(this, ts);
        }

        @Override
        public DiscreteEvent onDestroy(long ts) {
            return new DiscreteEvent.DefaultFinalizeEvent(this, ts);
        }

        @Override
        public void notifyEnd() {
            // Mock implementation
        }
    }

    // Tests for DiscreteEvent.DefaultStartEvent

    @Test
    void shouldCreateDefaultStartEvent_withValidParameters() {
        IEventSource source = new MockEventSource();
        long timestamp = 100;

        DiscreteEvent.DefaultStartEvent event = new DiscreteEvent.DefaultStartEvent(source, timestamp);

        assertNotNull(event);
        assertEquals(timestamp, event.getTs());
    }

    @Test
    void shouldCreateDefaultStartEvent_withZeroTimestamp() {
        IEventSource source = new MockEventSource();
        long timestamp = 0;

        DiscreteEvent.DefaultStartEvent event = new DiscreteEvent.DefaultStartEvent(source, timestamp);

        assertEquals(timestamp, event.getTs());
    }

    @Test
    void shouldCreateDefaultStartEvent_withLargeTimestamp() {
        IEventSource source = new MockEventSource();
        long timestamp = Long.MAX_VALUE;

        DiscreteEvent.DefaultStartEvent event = new DiscreteEvent.DefaultStartEvent(source, timestamp);

        assertEquals(timestamp, event.getTs());
    }

    @Test
    void shouldExecuteEvent_withoutException() {
        IEventSource source = new MockEventSource();
        DiscreteEvent.DefaultStartEvent event = new DiscreteEvent.DefaultStartEvent(source, 100);

        // Should not throw exception
        assertDoesNotThrow(() -> event.event());
    }

    @Test
    void shouldRunEvent_whenNotCancelled() {
        IEventSource source = new MockEventSource();
        DiscreteEvent.DefaultStartEvent event = new DiscreteEvent.DefaultStartEvent(source, 100);

        assertFalse(event.isCancelled());
        assertDoesNotThrow(() -> event.run());
    }

    @Test
    void shouldNotRunEvent_whenCancelled() {
        IEventSource source = new MockEventSource();
        DiscreteEvent.DefaultStartEvent event = new DiscreteEvent.DefaultStartEvent(source, 100);

        event.cancel();
        assertTrue(event.isCancelled());
        assertDoesNotThrow(() -> event.run());
    }

    @Test
    void shouldGenerateToString_withCorrectFormat() {
        IEventSource source = new MockEventSource();
        long timestamp = 500;
        DiscreteEvent.DefaultStartEvent event = new DiscreteEvent.DefaultStartEvent(source, timestamp);

        String result = event.toString();

        assertTrue(result.contains("Ev("));
        assertTrue(result.contains("DefaultStartEvent"));
        assertTrue(result.contains("[" + timestamp + "]"));
    }

    @Test
    void shouldCompare_withEqualTimestamp() {
        IEventSource source = new MockEventSource();
        DiscreteEvent.DefaultStartEvent event1 = new DiscreteEvent.DefaultStartEvent(source, 100);
        DiscreteEvent.DefaultStartEvent event2 = new DiscreteEvent.DefaultStartEvent(source, 100);

        assertEquals(0, event1.compareTo(event2));
    }

    @Test
    void shouldCompare_withGreaterTimestamp() {
        IEventSource source = new MockEventSource();
        DiscreteEvent.DefaultStartEvent event1 = new DiscreteEvent.DefaultStartEvent(source, 200);
        DiscreteEvent.DefaultStartEvent event2 = new DiscreteEvent.DefaultStartEvent(source, 100);

        assertTrue(event1.compareTo(event2) > 0);
    }

    @Test
    void shouldCompare_withLesserTimestamp() {
        IEventSource source = new MockEventSource();
        DiscreteEvent.DefaultStartEvent event1 = new DiscreteEvent.DefaultStartEvent(source, 50);
        DiscreteEvent.DefaultStartEvent event2 = new DiscreteEvent.DefaultStartEvent(source, 100);

        assertTrue(event1.compareTo(event2) < 0);
    }

    @Test
    void shouldCancel_andReturnTrue() {
        IEventSource source = new MockEventSource();
        DiscreteEvent.DefaultStartEvent event = new DiscreteEvent.DefaultStartEvent(source, 100);

        boolean result = event.cancel();

        assertTrue(result);
        assertTrue(event.isCancelled());
    }
}
