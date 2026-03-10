package simkit.random;

/**
 * Interface for discrete random variate generators.
 * Extends {@link RandomVariate} with an integer-valued generation method.
 *
 * @author JaDES Team (based on simkit DiscreteRandomVariate by Kirk Stork / Arnold Buss)
 */
public interface DiscreteRandomVariate extends RandomVariate {

    /**
     * Generates an integer-valued sample from this distribution.
     * @return a random integer
     */
    int generateInt();
}
