package simkit.random;

/**
 * Abstract base class for {@link RandomVariate} implementations.
 * Holds a reference to the underlying {@link RandomNumber} generator and
 * provides default implementations of {@link #setRandomNumber} and
 * {@link #getRandomNumber}.
 *
 * <p>All custom distribution classes (e.g. {@code ExponentialVariate},
 * {@code GompertzVariate}, etc.) extend this class.</p>
 *
 * @author JaDES Team (based on the simkit RandomVariateBase by Kirk Stork / Arnold Buss)
 */
public abstract class RandomVariateBase implements RandomVariate {

    /** The random number generator used by this variate. */
    protected RandomNumber rng;

    /**
     * Creates a new instance with a fresh {@link Congruential} generator.
     */
    public RandomVariateBase() {
        this.rng = RandomNumberFactory.getInstance();
    }

    @Override
    public void setRandomNumber(RandomNumber rng) {
        this.rng = rng;
    }

    @Override
    public RandomNumber getRandomNumber() {
        return rng;
    }
}
