/**
 * 
 */
package es.ull.simulation.model;


/**
 * A class to create sets of activity managers
 * @author Iván Castilla Rodríguez
 *
 */
public abstract class ActivityManagerCreator {
	/** The simulation model the activity managers belong to */ 
	protected final Simulation simul;

	/**
	 * Creates an activity manager creator
	 * @param model The simulation model the activity managers belong to
	 */
	public ActivityManagerCreator(final Simulation model) {
		this.simul = model;
	}

	/**
	 * Specifies the way the structure of the activity managers is built.
	 */	
	protected abstract void createActivityManagers();
}
