package es.ull.simulation.hta.outcomes;

/**
 * Defines different methods to combine the increased mortality rates (expressed as rate ratios) of the model items that are
 * simultaneously active for a patient. The combined rate ratio is applied before subtracting the life expectancy reductions.
 * @author Iván Castilla Rodríguez
 */
public enum MortalityRateCombinationMethod implements CombinationMethod {
	/** Applies only the largest rate ratio */
	MAX {
		@Override
		public double combine(double imr1, double imr2) {
			return Math.max(imr1, imr2);
		}
	},
	/** Multiplies all the rate ratios */
	MULT {
		@Override
		public double combine(double imr1, double imr2) {
			return imr1 * imr2;
		}
	};

	@Override
	public abstract double combine(double imr1, double imr2);
}
