package simkit.random;

/**
 * Generates random variates by resampling (with replacement) from a finite dataset.
 * Equivalent to bootstrapping: each call to {@link #generate()} picks a uniformly
 * random element from the data array.
 *
 * <p>Parameters: {@code data} (double[])</p>
 *
 * @author JaDES Team (based on simkit ResampleVariate by Kirk Stork / Arnold Buss)
 */
public class ResampleVariate extends RandomVariateBase {

    private double[] data;

    public ResampleVariate() {}

    @Override
    public double generate() {
        if (data == null || data.length == 0) {
            throw new IllegalStateException("ResampleVariate: data has not been set");
        }
        int index = (int) (rng.draw() * data.length);
        return data[index];
    }

    @Override
    public void setParameters(Object... params) {
        if (params == null || params.length < 1) {
            throw new IllegalArgumentException(
                    "ResampleVariate requires 1 parameter (double[]), received 0");
        }
        if (!(params[0] instanceof double[])) {
            throw new IllegalArgumentException(
                    "ResampleVariate parameter must be double[], got: " + params[0].getClass().getName());
        }
        setData((double[]) params[0]);
    }

    @Override
    public Object[] getParameters() {
        return new Object[]{data};
    }

    public void setData(double[] data) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("ResampleVariate: data must be non-null and non-empty");
        }
        this.data = data.clone();
    }

    public double[] getData() {
        return data == null ? null : data.clone();
    }

    @Override
    public String toString() {
        return "Resample(n=" + (data == null ? 0 : data.length) + ")";
    }
}
