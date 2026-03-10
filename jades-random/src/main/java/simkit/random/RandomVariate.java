package simkit.random;

/**
 * Interface for random variate generators (probability distributions).
 * Drop-in replacement for {@code simkit.random.RandomVariate} from the original
 * simkit library.
 *
 * @author JaDES Team (based on the simkit API by Kirk Stork / Arnold Buss)
 */
public interface RandomVariate {

    /**
     * Generates a random value according to this distribution.
     * @return a sample from the distribution
     */
    double generate();

    /**
     * Sets the parameters of this distribution.
     * @param params distribution parameters
     */
    void setParameters(Object... params);

    /**
     * Returns the current parameters of this distribution.
     * @return array of parameters
     */
    Object[] getParameters();

    /**
     * Sets the underlying random number generator.
     * @param rng the random number generator to use
     */
    void setRandomNumber(RandomNumber rng);

    /**
     * Returns the underlying random number generator.
     * @return the random number generator
     */
    RandomNumber getRandomNumber();
}
