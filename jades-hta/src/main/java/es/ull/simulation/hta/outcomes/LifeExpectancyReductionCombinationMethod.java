package es.ull.simulation.hta.outcomes;

/**
 * Defines different methods to combine the life expectancy reductions (in years) of the model items that are simultaneously
 * active for a patient.
 * @author Iván Castilla Rodríguez
 */
public enum LifeExpectancyReductionCombinationMethod implements CombinationMethod {
	/** Adds every reduction */
	ADD {
		@Override
		public double combine(double ler1, double ler2) {
			return ler1 + ler2;
		}
	},
	/** Adds the reductions, but a parameter shared by several simultaneously active items is considered only once */
	ADD_DISTINCT {
		@Override
		public double combine(double ler1, double ler2) {
			return ler1 + ler2;
		}

		@Override
		public boolean countsSharedItemsOnce() {
			return true;
		}
	},
	/** Applies only the largest reduction */
	MAX {
		@Override
		public double combine(double ler1, double ler2) {
			return Math.max(ler1, ler2);
		}
	};

	@Override
	public abstract double combine(double ler1, double ler2);
}
