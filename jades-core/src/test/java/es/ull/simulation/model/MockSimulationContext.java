package es.ull.simulation.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple mock implementation of ISimulationContext for testing purposes.
 * Allows tests to control timestamp and capture scheduled events.
 *
 * Usage:
 * <pre>
 * MockSimulationContext ctx = new MockSimulationContext(42);
 * SimulationObject obj = new SimulationObject(ctx, 1, "TEST");
 * assertEquals(42, obj.getTs());
 * </pre>
 *
 * @author Refactoring 2026
 */
public class MockSimulationContext implements ISimulationContext {

    private final long fixedTimestamp;
    private final TimeUnit timeUnit;
    private final List<DiscreteEvent> scheduledEvents = new ArrayList<>();
    private final AtomicInteger idCounter = new AtomicInteger(0);

    /**
     * Creates a mock context with a fixed timestamp and default time unit (MINUTE)
     * @param fixedTimestamp The timestamp to return from getCurrentTimestamp()
     */
    public MockSimulationContext(long fixedTimestamp) {
        this(fixedTimestamp, TimeUnit.MINUTE);
    }

    /**
     * Creates a mock context with a fixed timestamp and specific time unit
     * @param fixedTimestamp The timestamp to return from getCurrentTimestamp()
     * @param timeUnit The time unit to return
     */
    public MockSimulationContext(long fixedTimestamp, TimeUnit timeUnit) {
        this.fixedTimestamp = fixedTimestamp;
        this.timeUnit = timeUnit;
    }

    @Override
    public long getCurrentTimestamp() {
        return fixedTimestamp;
    }

    @Override
    public void scheduleEvent(DiscreteEvent event) {
        scheduledEvents.add(event);
    }

    @Override
    public int generateId() {
        return idCounter.getAndIncrement();
    }

    @Override
    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    /**
     * Returns all events scheduled through this context
     * @return List of scheduled events
     */
    public List<DiscreteEvent> getScheduledEvents() {
        return new ArrayList<>(scheduledEvents);
    }

    /**
     * Clears the list of scheduled events
     */
    public void clearScheduledEvents() {
        scheduledEvents.clear();
    }
}
