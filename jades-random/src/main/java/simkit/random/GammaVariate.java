package simkit.random;

/**
 * Generates random variates from a Gamma distribution using the
 * Marsaglia-Tsang (2000) fast algorithm.
 *
 * <p>Parameters: {@code alpha} (shape, double, &gt; 0), {@code beta} (scale, double, &gt; 0).
 * Mean = alpha * beta, Variance = alpha * beta^2.</p>
 *
 * @author JaDES Team (based on simkit GammaVariate by Kirk Stork / Arnold Buss)
 */
public class GammaVariate extends RandomVariateBase {

    private double alpha = 1.0;
    private double beta  = 1.0;

    /** Convenience fields pre-computed on parameter change */
    private double d, c;

    public GammaVariate() {
        setConvenienceParameters();
    }

    @Override
    public double generate() {
        if (alpha < 1.0) {
            // Reduction: Gamma(alpha) = Gamma(alpha+1) * U^(1/alpha)
            GammaVariate g = new GammaVariate();
            g.setRandomNumber(rng);
            g.setParameters(alpha + 1.0, beta);
            return g.generate() * Math.pow(rng.draw(), 1.0 / alpha);
        }
        // Marsaglia-Tsang for alpha >= 1
        double x, v;
        while (true) {
            do {
                x = nextGaussian();
                v = 1.0 + c * x;
            } while (v <= 0.0);
            v = v * v * v;
            double u = rng.draw();
            if (u < 1.0 - 0.0331 * (x * x) * (x * x)) {
                return d * v * beta;
            }
            if (Math.log(u) < 0.5 * x * x + d * (1.0 - v + Math.log(v))) {
                return d * v * beta;
            }
        }
    }

    /** Marsaglia polar method for a standard normal sample. */
    private double nextGaussian() {
        double u, v, s;
        do {
            u = rng.draw() * 2.0 - 1.0;
            v = rng.draw() * 2.0 - 1.0;
            s = u * u + v * v;
        } while (s >= 1.0 || s == 0.0);
        return u * Math.sqrt(-2.0 * Math.log(s) / s);
    }

    protected void setConvenienceParameters() {
        double a = Math.max(alpha, 1.0);
        d = a - 1.0 / 3.0;
        c = 1.0 / Math.sqrt(9.0 * d);
    }

    @Override
    public void setParameters(Object... params) {
        if (params == null || params.length < 2) {
            throw new IllegalArgumentException(
                    "GammaVariate requires 2 parameters (alpha, beta), received "
                            + (params == null ? 0 : params.length));
        }
        setAlpha(((Number) params[0]).doubleValue());
        setBeta(((Number) params[1]).doubleValue());
    }

    @Override
    public Object[] getParameters() {
        return new Object[]{alpha, beta};
    }

    public void setAlpha(double alpha) {
        if (alpha <= 0.0) throw new IllegalArgumentException("alpha must be > 0, got " + alpha);
        this.alpha = alpha;
        setConvenienceParameters();
    }

    public void setBeta(double beta) {
        if (beta <= 0.0) throw new IllegalArgumentException("beta must be > 0, got " + beta);
        this.beta = beta;
    }

    public double getAlpha() { return alpha; }
    public double getBeta()  { return beta; }

    @Override
    public String toString() {
        return "Gamma(" + alpha + ", " + beta + ")";
    }
}
