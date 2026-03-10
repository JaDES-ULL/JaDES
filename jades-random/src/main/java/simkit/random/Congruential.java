package simkit.random;

import java.util.Random;

/**
 * Linear Congruential Generator (LCG) backed by {@link java.util.Random}.
 * Java's {@code Random} uses the same LCG algorithm as the original simkit
 * {@code Congruential} class: multiplier {@code 0x5DEECE66DL}, addend {@code 0xBL},
 * modulus {@code 2^48}. This makes it a transparent drop-in replacement.
 *
 * <p>This is the default {@link RandomNumber} implementation returned by
 * {@link RandomNumberFactory#getInstance()}.</p>
 *
 * @author JaDES Team (based on the simkit Congruential by Kirk Stork / Arnold Buss)
 */
public class Congruential implements RandomNumber {

    /** Same multiplier as Java's Random and the original simkit Congruential */
    private static final long MULTIPLIER = 0x5DEECE66DL;

    /** The seed provided at construction or last {@link #setSeed(long)} call */
    private long initialSeed;

    /** The underlying Java RNG */
    private final Random rng;

    /**
     * Creates a generator seeded with the current system time.
     */
    public Congruential() {
        this(System.currentTimeMillis());
    }

    /**
     * Creates a generator with the given seed.
     * @param seed initial seed
     */
    public Congruential(long seed) {
        this.initialSeed = seed;
        this.rng = new Random(seed);
    }

    @Override
    public void setSeed(long seed) {
        this.initialSeed = seed;
        rng.setSeed(seed);
    }

    @Override
    public long getSeed() {
        return initialSeed;
    }

    @Override
    public void resetSeed() {
        rng.setSeed(initialSeed);
    }

    @Override
    public void setSeeds(long[] seeds) {
        if (seeds != null && seeds.length > 0) {
            setSeed(seeds[0]);
        }
    }

    @Override
    public long[] getSeeds() {
        return new long[]{initialSeed};
    }

    @Override
    public double draw() {
        return rng.nextDouble();
    }

    @Override
    public long drawLong() {
        return rng.nextLong();
    }

    @Override
    public double getMultiplier() {
        return MULTIPLIER;
    }
}
