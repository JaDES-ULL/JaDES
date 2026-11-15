package es.ull.simulation.hta.params.modifiers;

/**
 * Operations that apply the magnitude of an effect (m) to the value of a parameter (x). When several operations modify the same parameter,
 * they are applied in ascending {@link #getOrder() order}: first the replacement, then the multiplications (linear factors, then ratios
 * of rates, then odds ratios) and finally the differences. Operations of the same order commute.
 * <ul>
 * <li>{@link #REPLACE}: m.</li>
 * <li>{@link #MULTIPLY}: x * m.</li>
 * <li>{@link #RATE_RATIO}: x is a probability, which is converted into the equivalent constant rate r = -ln(1 - x); the rate is multiplied
 * by m and converted back into a probability: 1 - exp(-r * m) = 1 - (1 - x)^m. The result does not depend on the length of the period
 * of the probability, and it always lies in [0, 1] for m &ge; 0.</li>
 * <li>{@link #TIME_HAZARD_RATIO}: x is the mean of an exponential time to event, whose rate is multiplied by m: x / m.</li>
 * <li>{@link #ODDS_RATIO}: x is a probability whose odds are multiplied by m: x * m / (1 - x + x * m).</li>
 * <li>{@link #SUBTRACT}: x - m.</li>
 * </ul>
 */
public enum ModifierOperation {
    /** The magnitude replaces the value of the parameter */
    REPLACE(0) {
        @Override
        public double apply(double value, double magnitude) {
            return magnitude;
        }
    },
    /** The value of the parameter is multiplied by the magnitude */
    MULTIPLY(1) {
        @Override
        public double apply(double value, double magnitude) {
            return value * magnitude;
        }
    },
    /** The magnitude multiplies the rate equivalent to a probability: 1 - (1 - x)^m */
    RATE_RATIO(2) {
        @Override
        public double apply(double value, double magnitude) {
            return 1.0 - Math.pow(1.0 - value, magnitude);
        }
    },
    /** The magnitude multiplies the rate of an exponential time to event: x / m */
    TIME_HAZARD_RATIO(2) {
        @Override
        public double apply(double value, double magnitude) {
            return value / magnitude;
        }
    },
    /** The magnitude multiplies the odds of a probability: x * m / (1 - x + x * m) */
    ODDS_RATIO(3) {
        @Override
        public double apply(double value, double magnitude) {
            return value * magnitude / (1.0 - value + value * magnitude);
        }
    },
    /** The magnitude is subtracted from the value of the parameter */
    SUBTRACT(4) {
        @Override
        public double apply(double value, double magnitude) {
            return value - magnitude;
        }
    };

    private final int order;

    private ModifierOperation(int order) {
        this.order = order;
    }

    /**
     * Returns the position of this operation when several operations modify the same parameter (lower values are applied first)
     * @return the position of this operation when several operations modify the same parameter
     */
    public int getOrder() {
        return order;
    }

    /**
     * Applies the operation
     * @param value The current value of the parameter
     * @param magnitude The magnitude of the effect
     * @return The modified value of the parameter
     */
    public abstract double apply(double value, double magnitude);
}
