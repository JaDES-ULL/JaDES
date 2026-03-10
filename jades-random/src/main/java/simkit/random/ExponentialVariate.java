package simkit.random;

/**
 * Generates random variates from an Exponential distribution.
 * Uses the inverse transform: X = -mean * ln(U), where U ~ Uniform(0,1).
 *
 * <p>Parameters: {@code mean} (double, &gt; 0)</p>
 *
 * @author JaDES Team (based on simkit ExponentialVariate by Kirk Stork / Arnold Buss)
 */
public class ExponentialVariate extends RandomVariateBase {

    private double mean = 1.0;

    public ExponentialVariate() {}

    @Override
    public double generate() {
        return -mean * Math.log(rng.draw());
    }

    @Override
    public void setParameters(Object... params) {
        if (params == null || params.length < 1) {
            throw new IllegalArgumentException(
                    "ExponentialVariate requires 1 parameter (mean), received 0");
        }
        setMean(((Number) params[0]).doubleValue());
    }

    @Override
    public Object[] getParameters() {
        return new Object[]{mean};
    }

    public void setMean(double mean) {
        if (mean <= 0.0) throw new IllegalArgumentException("mean must be > 0, got " + mean);
        this.mean = mean;
    }

    public double getMean() { return mean; }

    @Override
    public String toString() {
        return "Exponential(" + mean + ")";
    }
}
