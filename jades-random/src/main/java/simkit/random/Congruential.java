package simkit.random;

/**
 * Direct 48-bit Linear Congruential Generator (LCG).
 * Exact port of {@code simkit.random.Congruential} from the original simkit library
 * (Kirk Stork / Arnold Buss, Naval Postgraduate School).
 *
 * <p>Parameters: multiplier {@code 0x5DEECE66DL}, addend {@code 0xBL}, modulus {@code 2^48} —
 * the same constants as {@link java.util.Random}. The seed is scrambled at initialisation
 * with {@code (seed ^ MULTIPLIER) & MASK}, also matching Java.</p>
 *
 * <p>The key difference from {@link java.util.Random} is that {@link #draw()} performs
 * <em>one</em> LCG step and returns the full 48-bit state normalised to [0,1), whereas
 * {@code java.util.Random.nextDouble()} performs <em>two</em> steps and returns 53 bits.
 * This single-step behaviour produces the same random sequence as the original simkit
 * library, ensuring reproducibility of results computed with simkit.</p>
 *
 * <p>This is the default {@link RandomNumber} implementation returned by
 * {@link RandomNumberFactory#getInstance()}.</p>
 *
 * @author JaDES Team (based on the simkit Congruential by Kirk Stork / Arnold Buss)
 */
public class Congruential implements RandomNumber {

    private static final long MULTIPLIER = 0x5DEECE66DL;
    private static final long ADDEND     = 0xBL;
    private static final long MASK       = (1L << 48) - 1;
    private static final double NORM     = (double) (1L << 48);

    /** The seed provided at construction or last {@link #setSeed(long)} call */
    private long initialSeed;

    /** Current LCG state (48 bits) */
    private long state;

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
        this.state = (seed ^ MULTIPLIER) & MASK;
    }

    @Override
    public void setSeed(long seed) {
        this.initialSeed = seed;
        this.state = (seed ^ MULTIPLIER) & MASK;
    }

    @Override
    public long getSeed() {
        return initialSeed;
    }

    @Override
    public void resetSeed() {
        this.state = (initialSeed ^ MULTIPLIER) & MASK;
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

    /**
     * Advances the LCG by one step and returns the result normalised to [0, 1).
     * 48-bit precision, matching the original simkit {@code Congruential.draw()}.
     */
    @Override
    public double draw() {
        state = (MULTIPLIER * state + ADDEND) & MASK;
        return (double) state / NORM;
    }

    /**
     * Returns a pseudorandom long by combining two consecutive 48-bit LCG steps.
     */
    @Override
    public long drawLong() {
        state = (MULTIPLIER * state + ADDEND) & MASK;
        long high = state;
        state = (MULTIPLIER * state + ADDEND) & MASK;
        return (high << 16) | (state >>> 32);
    }

    @Override
    public double getMultiplier() {
        return MULTIPLIER;
    }
}
