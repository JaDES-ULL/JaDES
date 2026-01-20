package es.ull.simulation.model;

/**
 * Interface for generating unique identifiers.
 * Follows Single Responsibility Principle - only responsible for ID generation.
 * 
 * @author Ivan Castilla Rodriguez
 */
public interface IIdGenerator {
    
    /**
     * Generates a new unique identifier
     * @return A unique identifier
     */
    int generateId();
    
    /**
     * Resets the ID counter
     */
    void reset();
}
