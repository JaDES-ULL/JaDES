/**
 *
 */
package es.ull.simulation.model.location;

/**
 * Class that calculates the steps that an entity has to follow to reach a destination
 *
 * @author Iván Castilla Rodríguez
 *
 */
public interface IRouter {
	/** Abstract location to indicate that the entity has to wait for a condition to meet before moving */
	static final ILocation COND_WAIT_LOCATION = new SpecialLocation(-1, "Abstract location to indicate that the entity has to wait");
	/** Abstract location for an unreachable destination */
	static final ILocation UNREACHABLE_LOCATION = new SpecialLocation(-2, "Abstract location for an unreachable destination");
	/**
	 * Returns the next location in the way for an entity trying to reach a destination; null if the destination is not reachable from
	 * the current location
	 * @param entity Entity moving from its current location to destination
	 * @param destination Destination of the entity
	 * @return the next location in the way for an entity trying to reach a destination; {@link #UNREACHABLE_LOCATION} if the destination
	 * is not reachable from the current location; and {@link #COND_WAIT_LOCATION} if the entity has to wait before moving
	 */
	ILocation getNextLocationTo(final IMovable entity, final ILocation destination);

	/**
	 * Returns true if the location is an unreachable location
	 * @param loc Location
	 * @return true if the location is an unreachable location
	 */
	public static boolean isUnreachableLocation(ILocation loc) {
		return UNREACHABLE_LOCATION.equals(loc);
	}

	/**
	 * Returns true if the location represents a conditional waiting
	 * @param loc Location
	 * @return true if the location represents a conditional waiting
	 */
	public static boolean isConditionalWaitLocation(ILocation loc) {
		return COND_WAIT_LOCATION.equals(loc);
	}

	/**
	 * Minimal special location implementation for routing markers.
	 */
	final class SpecialLocation implements ILocation {
		private final int id;
		private final String description;

		private SpecialLocation(final int id, final String description) {
			this.id = id;
			this.description = description;
		}

		@Override
		public int getCapacity() {
			return 0;
		}

		@Override
		public int getIdentifier() {
			return id;
		}

		@Override
		public String getDescription() {
			return description;
		}

		@Override
		public String toString() {
			return description;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof SpecialLocation)) {
				return false;
			}
			return ((SpecialLocation) obj).id == id;
		}

		@Override
		public int hashCode() {
			return Integer.hashCode(id);
		}
	}
}
