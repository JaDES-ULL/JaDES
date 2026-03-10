package simkit.random;

/**
 * A {@link RandomVariate} that always returns a fixed constant value.
 * Used by {@link es.ull.simulation.functions.PolynomialFunction} via
 * {@code RandomVariateFactory.getInstance("ConstantVariate", value)}.
 *
 * @author JaDES Team
 */
public class ConstantVariate extends RandomVariateBase {

    private double value;

    public ConstantVariate() {
        this.value = 0.0;
    }

    @Override
    public double generate() {
        return value;
    }

    @Override
    public void setParameters(Object... params) {
        if (params == null || params.length < 1) {
            throw new IllegalArgumentException(
                    "ConstantVariate requires 1 parameter (the constant value), received 0");
        }
        if (!(params[0] instanceof Number)) {
            throw new IllegalArgumentException(
                    "ConstantVariate parameter must be a Number, got: " + params[0].getClass().getName());
        }
        this.value = ((Number) params[0]).doubleValue();
    }

    @Override
    public Object[] getParameters() {
        return new Object[]{value};
    }

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
}
