/**
 * 
 */
package es.ull.simulation.model.engine;

import es.ull.simulation.model.ElementInstance;
/**
 * @author Iván Castilla Rodríguez
 *
 */
public class ElementInstanceEngine extends AbstractEngineObject {
	/** Element instance's counter. Useful for identifying each instance */
	// Must start in 1 to avoid problems with internal control of request flows
	private static int counter = 1;
	/** Associated {@link ElementInstance} */
	private final ElementInstance modelInstance;

	/**
	 * Constructs a new ElementInstanceEngine object.
	 * ElementInstanceEngine represents individual instances of elements within a simulation, each with its own
	 * counter for identification.It is associated with a specific ElementInstance and is used in a
	 * SequentialSimulationEngine.
	 *
	 * @param simul         The SimulationEngine to which this element instance engine belongs.
	 * @param modelInstance The associated ElementInstance object representing this element instance.
	 */
	public ElementInstanceEngine(SimulationEngine simul, ElementInstance modelInstance) {
		super(counter++, simul, "EI");
		this.modelInstance = modelInstance;
	}

	/**
	 * @return the modelInstance
	 */
	public ElementInstance getModelInstance() {
		return modelInstance;
	}

	public void notifyResourcesAcquired() {
		// Nothing to do
	}

}
