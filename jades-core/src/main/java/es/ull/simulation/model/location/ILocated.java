/**
 * 
 */
package es.ull.simulation.model.location;

/**
 * An object that can be located, i.e., has a spatial {@link Location} and a capacity
 * @author Iván Castilla Rodríguez
 *
 */
public interface ILocated {
	/**
	 * Returns the physical capacity of the object. Units are simulation-dependent
	 * @return the physical capacity of the object
	 */
	int getCapacity();
	/**
	 * Returns the current location of the object
	 * @return the current location of the object
	 */
	Location getLocation();
}
