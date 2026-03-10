package simkit.random;

/**
 * Generates random variates from a Uniform distribution on [minValue, maxValue].
 *
 * <p>The probability density function is:
 * <pre>f(x) = 1 / (max - min)  for min &lt;= x &lt;= max</pre>
 *
 * <p>Uses the inverse transform method: X = min + (max - min) * U, where U ~ Uniform(0,1).
 */
public class UniformVariate extends RandomVariateBase {

    /** Lower bound of the distribution. */
    private double minValue;

    /** Upper bound of the distribution. */
    private double maxValue;

    /**
     * Creates a UniformVariate with default parameters min=0.0, max=1.0.
     */
    public UniformVariate() {
        this.minValue = 0.0;
        this.maxValue = 1.0;
    }

    /**
     * Generates a value from Uniform(minValue, maxValue).
     *
     * @return a random value in [minValue, maxValue]
     */
    @Override
    public double generate() {
        return minValue + (maxValue - minValue) * rng.draw();
    }

    /**
     * Sets the parameters for this distribution.
     *
     * @param params two Number values: params[0] = min, params[1] = max
     * @throws IllegalArgumentException if params is null, has wrong length, or min >= max
     */
    @Override
    public void setParameters(Object... params) {
        if (params == null || params.length < 2) {
            throw new IllegalArgumentException(
                "UniformVariate requires 2 parameters: min and max");
        }
        double min = ((Number) params[0]).doubleValue();
        double max = ((Number) params[1]).doubleValue();
        if (min >= max) {
            throw new IllegalArgumentException(
                "min (" + min + ") must be less than max (" + max + ")");
        }
        this.minValue = min;
        this.maxValue = max;
    }

    /**
     * Returns the parameters of this distribution.
     *
     * @return array [minValue, maxValue]
     */
    @Override
    public Object[] getParameters() {
        return new Object[] { minValue, maxValue };
    }

    /**
     * Returns the lower bound of the distribution.
     *
     * @return min value
     */
    public double getMinValue() {
        return minValue;
    }

    /**
     * Returns the upper bound of the distribution.
     *
     * @return max value
     */
    public double getMaxValue() {
        return maxValue;
    }

    @Override
    public String toString() {
        return "Uniform [" + minValue + ", " + maxValue + "]";
    }
}
