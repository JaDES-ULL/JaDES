package es.ull.simulation.model;

/**
 * Sequential implementation of ID generator.
 * Generates sequential integer IDs starting from 0.
 * Thread-safe for concurrent simulations.
 * 
 * @author Ivan Castilla Rodriguez
 */
public class SequentialIdGenerator implements IIdGenerator {
    
    /** Counter for generating sequential IDs */
    private int counter = 0;
    
    @Override
    public synchronized int generateId() {
        return counter++;
    }
    
    @Override
    public synchronized void reset() {
        counter = 0;
    }
}
