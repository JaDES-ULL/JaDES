/**
 * 
 */
package es.ull.simulation.model.location;

import es.ull.simulation.model.IDescribable;
import es.ull.simulation.model.IIdentifiable;

/**
 * Minimal contract for a location in the simulation.
 *
 * @author Refactoring 2026
 */
public interface ILocation extends IIdentifiable, IDescribable {
	/**
	 * Returns the physical capacity of the location. Units are simulation-dependent
	 * @return the physical capacity of the location
	 */
	int getCapacity();
}
