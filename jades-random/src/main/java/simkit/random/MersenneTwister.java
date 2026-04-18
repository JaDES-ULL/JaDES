package simkit.random;

import java.util.logging.Logger;

/**
 * Mersenne Twister pseudo-random number generator (MT19937).
 * <p>
 * Faithful port of the {@code simkit.random.MersenneTwister} from simkit 1.4.1
 * (Arnold Buss / Kirk Stork, Naval Postgraduate School), reverse-engineered from
 * the published binary so that all existing test expected values remain valid.
 * </p>
 * <p>
 * Key properties:
 * <ul>
 *   <li>State: 624 × 32-bit words ({@code int[624]}, index {@code mti})</li>
 *   <li>Initialisation: linear multiplier 69069</li>
 *   <li>Matrix constant: {@code 0x9908B0DF} (-1727483681 signed)</li>
 *   <li>Tempering masks: {@code 0x9D2C5680}, {@code 0xEFC60000}</li>
 *   <li>{@link #draw()} = {@link #drawLong()} × 2<sup>-32</sup></li>
 * </ul>
 * </p>
 * <p>
 * Default seed: 4357.  Seed 0 is rejected ({@link IllegalArgumentException}).
 * </p>
 */
public class MersenneTwister implements RandomNumber {

    private static final Logger logger = Logger.getLogger("simkit.random");

    // MT19937 parameters
    private static final int N                 = 624;
    private static final int M                 = 397;
    private static final int MATRIX_A          = 0x9908B0DF;   // -1727483681 signed
    private static final int UPPER_MASK        = 0x80000000;   // most-significant bit
    private static final int LOWER_MASK        = 0x7FFFFFFF;   // 31 least-significant bits
    private static final int TEMPERING_MASK_B  = 0x9D2C5680;   // -1658038656 signed
    private static final int TEMPERING_MASK_C  = 0xEFC60000;   // -272236544 signed

    /** 1 / 2^32  — normalises a 32-bit unsigned long to [0, 1) */
    private static final double MODULUS_MULT   = 2.3283064365386963e-10;

    // State
    private int[]   mt;
    private int     mti;
    private int[]   mag01;
    private int     originalSeed;

    // -----------------------------------------------------------------------
    // Constructors
    // -----------------------------------------------------------------------

    /** Creates a generator with the default seed (4357). */
    public MersenneTwister() {
        setSeed(4357L);
    }

    // -----------------------------------------------------------------------
    // RandomNumber interface
    // -----------------------------------------------------------------------

    /**
     * Seeds the generator.  The seed is truncated to 32 bits via {@code (int) seed}.
     * Seed 0 is illegal.
     */
    @Override
    public void setSeed(long seed) {
        if (seed == 0L) {
            logger.severe("Seed cannot be 0");
            throw new IllegalArgumentException("Seed cannot be 0");
        }
        sgenrand((int) seed);
    }

    /**
     * Returns the current MT state word at index {@code mti} (advancing the
     * generator if necessary), as a long — matches simkit's {@code getSeed()} behaviour.
     */
    @Override
    public long getSeed() {
        if (mti >= N) {
            fill();
        }
        return (long) mt[mti] & 0xFFFFFFFFL;
    }

    /** Reinitialises the generator with the original seed. */
    @Override
    public void resetSeed() {
        sgenrand(originalSeed);
    }

    /**
     * Sets the seed from a seed array.  Accepts arrays of length 1, 624, or 625,
     * matching simkit's behaviour exactly.
     */
    @Override
    public void setSeeds(long[] seeds) {
        if (seeds.length == 1) {
            sgenrand((int) seeds[0]);
        } else if (seeds.length == N) {
            for (int i = 0; i < N; i++) {
                mt[i] = (int) seeds[i];
            }
            mti = N;
        } else if (seeds.length == N + 1) {
            for (int i = 0; i < N; i++) {
                mt[i] = (int) seeds[i];
            }
            mti = (int) seeds[N];
        } else {
            String msg = "Seed array must be of length 1, 624, or 625; was " + seeds.length;
            logger.severe(msg);
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Returns the current state as a long array of length 625 (624 state words
     * + current index), matching simkit's {@code getSeeds()} behaviour.
     */
    @Override
    public long[] getSeeds() {
        long[] result = new long[N + 1];
        for (int i = 0; i < N; i++) {
            result[i] = (long) mt[i] & 0xFFFFFFFFL;
        }
        result[N] = mti;
        return result;
    }

    /**
     * Returns the next 32-bit pseudo-random value as a non-negative long,
     * applying the MT tempering transform.
     */
    @Override
    public long drawLong() {
        if (mti >= N) {
            fill();
        }
        int y = mt[mti++];

        // Tempering
        y ^= (y >>> 11);
        y ^= (y << 7)  & TEMPERING_MASK_B;
        y ^= (y << 15) & TEMPERING_MASK_C;
        y ^= (y >>> 18);

        return (long) y & 0xFFFFFFFFL;
    }

    /**
     * Returns a pseudo-random double in [0, 1) by computing
     * {@code drawLong() × 2^-32}.
     */
    @Override
    public double draw() {
        return drawLong() * MODULUS_MULT;
    }

    /** Returns the multiplier constant ({@code 2^-32}). */
    @Override
    public double getMultiplier() {
        return MODULUS_MULT;
    }

    @Override
    public String toString() {
        return "Mersenne Twister";
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    /**
     * Initialises the MT state from a single 32-bit seed using the
     * linear recurrence {@code mt[i] = 69069 × mt[i-1]} (mod 2<sup>32</sup>).
     */
    private void sgenrand(int seed) {
        mt = new int[N];
        mt[0] = seed & 0xFFFFFFFF;
        mti = 1;
        while (mti < N) {
            mt[mti] = (69069 * mt[mti - 1]) & 0xFFFFFFFF;
            mti++;
        }
        originalSeed = seed;
        mag01 = new int[]{0, MATRIX_A};
    }

    /** Generates the next batch of N words into {@code mt[]}. */
    private void fill() {
        int y;
        int kk;

        for (kk = 0; kk < N - M; kk++) {
            y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
            mt[kk] = mt[kk + M] ^ (y >>> 1) ^ mag01[y & 1];
        }
        for (; kk < N - 1; kk++) {
            y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
            mt[kk] = mt[kk + (M - N)] ^ (y >>> 1) ^ mag01[y & 1];
        }
        y = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
        mt[N - 1] = mt[M - 1] ^ (y >>> 1) ^ mag01[y & 1];

        mti = 0;
    }
}
