package es.ull.simulation.hta.outcomes;

/**
 * Defines different methods to combine the annual disutilities of the model items that are simultaneously active for a patient.
 * One-time disutilities are not affected by this method: they are always added, once per triggering event.
 * @author Iván Castilla Rodríguez
 *
 */
public enum DisutilityCombinationMethod implements CombinationMethod {
	/** Additive method */
	ADD {
		@Override
		public double combine(double du1, double du2) {
			return du1 + du2;
		}
	},
	/** Additive method, but a utility parameter shared by several simultaneously active items is considered only once */
	ADD_DISTINCT {
		@Override
		public double combine(double du1, double du2) {
			return du1 + du2;
		}

		@Override
		public boolean countsSharedItemsOnce() {
			return true;
		}
	},
	/** Takes the maximum among the disutilities */
	MAX {
		@Override
		public double combine(double du1, double du2) {
			if (du1 > du2)
				return du1;
			return du2;
		}
	};
	/**
	 * Combines two disutilities into a single value
	 * @param du1 First disutility
	 * @param du2 Second disutility
	 * @return The result of combining the two disutilities
	 */
	@Override
	public abstract double combine(double du1, double du2);

}
