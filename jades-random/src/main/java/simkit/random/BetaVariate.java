package simkit.random;

/**
 * Generates random variates from a Beta distribution.
 * Uses the ratio of two independent Gamma samples:
 * if X ~ Gamma(alpha, 1) and Y ~ Gamma(beta, 1), then X/(X+Y) ~ Beta(alpha, beta).
 *
 * <p>Parameters: {@code alpha} (shape1, double, &gt; 0), {@code beta} (shape2, double, &gt; 0).
 * Mean = alpha / (alpha + beta).</p>
 *
 * @author JaDES Team (based on simkit BetaVariate)
 */
public class BetaVariate extends RandomVariateBase {

    private double alpha = 1.0;
    private double beta  = 1.0;

    private GammaVariate gammaAlpha;
    private GammaVariate gammaBeta;

    public BetaVariate() {
        gammaAlpha = new GammaVariate();
        gammaBeta  = new GammaVariate();
    }

    @Override
    public void setRandomNumber(RandomNumber rng) {
        super.setRandomNumber(rng);
        gammaAlpha.setRandomNumber(rng);
        gammaBeta.setRandomNumber(rng);
    }

    @Override
    public double generate() {
        double x = gammaAlpha.generate();
        double y = gammaBeta.generate();
        return x / (x + y);
    }

    @Override
    public void setParameters(Object... params) {
        if (params == null || params.length < 2) {
            throw new IllegalArgumentException(
                    "BetaVariate requires 2 parameters (alpha, beta), received "
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
        gammaAlpha.setParameters(alpha, 1.0);
    }

    public void setBeta(double beta) {
        if (beta <= 0.0) throw new IllegalArgumentException("beta must be > 0, got " + beta);
        this.beta = beta;
        gammaBeta.setParameters(beta, 1.0);
    }

    public double getAlpha() { return alpha; }
    public double getBeta()  { return beta; }

    @Override
    public String toString() {
        return "Beta(" + alpha + ", " + beta + ")";
    }
}
