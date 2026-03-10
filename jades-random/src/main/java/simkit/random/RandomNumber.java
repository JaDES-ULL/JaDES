package simkit.random;

/**
 * Interface for random number generators used in simulation experiments.
 * Drop-in replacement for {@code simkit.random.RandomNumber} from the original
 * simkit library, backed by {@code java.util.Random} (linear congruential generator).
 *
 * @author JaDES Team (based on the simkit API by Kirk Stork / Arnold Buss)
 */
public interface RandomNumber {

    /**
     * Sets the seed of this generator.
     * @param seed the new seed
     */
    void setSeed(long seed);

    /**
     * Returns the current seed.
     * @return current seed
     */
    long getSeed();

    /**
     * Resets the generator to its initial seed.
     */
    void resetSeed();

    /**
     * Sets multiple seeds (for generators that support substreams). Only the
     * first element is used by the default {@link Congruential} implementation.
     * @param seeds array of seeds
     */
    void setSeeds(long[] seeds);

    /**
     * Returns the seeds of this generator.
     * @return array of seeds
     */
    long[] getSeeds();

    /**
     * Draws a uniform random number in {@code [0.0, 1.0)}.
     * @return a double in [0, 1)
     */
    double draw();

    /**
     * Draws a random long value.
     * @return a random long
     */
    long drawLong();

    /**
     * Returns the multiplier constant used by the generator (implementation-defined).
     * @return multiplier constant
     */
    double getMultiplier();
}
