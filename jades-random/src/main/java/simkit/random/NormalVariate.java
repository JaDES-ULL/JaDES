package simkit.random;

/**
 * Generates random variates from a Normal (Gaussian) distribution.
 * Uses the Box-Muller transform on pairs of uniform samples from
 * the underlying {@link RandomNumber} generator.
 *
 * <p>Parameters: {@code mean} (double), {@code standardDeviation} (double, &gt;= 0)</p>
 *
 * @author JaDES Team (based on simkit NormalVariate by Kirk Stork / Arnold Buss)
 */
public class NormalVariate extends RandomVariateBase {

    private double mean = 0.0;
    private double standardDeviation = 1.0;

    /** Box-Muller spare value (generated in pairs) */
    private double spare;
    private boolean hasSpare = false;

    public NormalVariate() {}

    @Override
    public double generate() {
        if (hasSpare) {
            hasSpare = false;
            return spare * standardDeviation + mean;
        }
        double u, v, s;
        do {
            u = rng.draw() * 2.0 - 1.0;
            v = rng.draw() * 2.0 - 1.0;
            s = u * u + v * v;
        } while (s >= 1.0 || s == 0.0);
        double factor = Math.sqrt(-2.0 * Math.log(s) / s);
        spare = v * factor;
        hasSpare = true;
        return (u * factor) * standardDeviation + mean;
    }

    @Override
    public void setParameters(Object... params) {
        if (params == null || params.length < 2) {
            throw new IllegalArgumentException(
                    "NormalVariate requires 2 parameters (mean, stdDev), received "
                            + (params == null ? 0 : params.length));
        }
        setMean(((Number) params[0]).doubleValue());
        setStandardDeviation(((Number) params[1]).doubleValue());
    }

    @Override
    public Object[] getParameters() {
        return new Object[]{mean, standardDeviation};
    }

    public void setMean(double mean) { this.mean = mean; }
    public double getMean() { return mean; }

    public void setStandardDeviation(double sd) {
        if (sd < 0.0) throw new IllegalArgumentException("standardDeviation must be >= 0, got " + sd);
        this.standardDeviation = sd;
        this.hasSpare = false;
    }
    public double getStandardDeviation() { return standardDeviation; }
    public double getVariance() { return standardDeviation * standardDeviation; }

    @Override
    public String toString() {
        return "Normal(" + mean + ", " + standardDeviation + ")";
    }
}
