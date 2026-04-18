package simkit.random;

/**
 * Generates random variates from a Bernoulli distribution.
 * Returns 1 with probability {@code p} and 0 with probability {@code 1 - p}.
 *
 * <p>Parameters: {@code p} (double, in [0, 1])</p>
 *
 * @author JaDES Team (based on simkit BernoulliVariate by Kirk Stork / Arnold Buss)
 */
public class BernoulliVariate extends RandomVariateBase implements DiscreteRandomVariate {

    private double p = 0.5;

    public BernoulliVariate() {}

    @Override
    public int generateInt() {
        return rng.draw() < p ? 1 : 0;
    }

    @Override
    public double generate() {
        return generateInt();
    }

    @Override
    public void setParameters(Object... params) {
        if (params == null || params.length < 1) {
            throw new IllegalArgumentException(
                    "BernoulliVariate requires 1 parameter (p), received 0");
        }
        setP(((Number) params[0]).doubleValue());
    }

    @Override
    public Object[] getParameters() {
        return new Object[]{p};
    }

    public void setP(double p) {
        if (p < 0.0 || p > 1.0) {
            throw new IllegalArgumentException("p must be in [0, 1], got " + p);
        }
        this.p = p;
    }

    public double getP() { return p; }

    @Override
    public String toString() {
        return "Bernoulli(" + p + ")";
    }
}
