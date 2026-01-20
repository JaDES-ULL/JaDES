package es.ull.simulation.model;

/**
 * Abstraction for simulation context - decouples SimulationObject from Simulation.
 * Provides only the essential services needed by simulation objects.
 *
 * This interface enables testability by allowing mock implementations
 * without requiring a full Simulation setup.
 *
 * @author Refactoring 2026
 */
public interface ISimulationContext {

    /**
     * Returns the current simulation timestamp
     * @return Current simulation time in internal units
     */
    long getCurrentTimestamp();

    /**
     * Adds an event to the simulation event queue
     * @param event The event to schedule
     */
    void scheduleEvent(DiscreteEvent event);

    /**
     * Returns a unique identifier for new entities
     * @return Unique identifier
     */
    int generateId();

    /**
     * Gets the time unit used in this simulation
     * @return The time unit
     */
    TimeUnit getTimeUnit();
}
