package es.ull.simulation.hta.outcomes;

/**
 * Defines different methods to combine the annual costs of the model items that are simultaneously active for a patient.
 * One-time costs are not affected by this method: they are always added, once per triggering event.
 * @author Iván Castilla Rodríguez
 */
public enum CostCombinationMethod implements CombinationMethod {
	/** Adds every cost */
	ADD {
		@Override
		public double combine(double cost1, double cost2) {
			return cost1 + cost2;
		}
	},
	/** Adds the costs, but a cost parameter shared by several simultaneously active items is considered only once */
	ADD_DISTINCT {
		@Override
		public double combine(double cost1, double cost2) {
			return cost1 + cost2;
		}

		@Override
		public boolean countsSharedItemsOnce() {
			return true;
		}
	};

	@Override
	public abstract double combine(double cost1, double cost2);
}
