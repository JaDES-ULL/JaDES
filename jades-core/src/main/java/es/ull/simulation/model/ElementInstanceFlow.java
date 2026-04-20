package es.ull.simulation.model;

import es.ull.simulation.model.flow.IFlow;

/**
 * Manages the flow execution state of an element instance.
 * This class encapsulates all flow-related state including the initial flow,
 * current flow, and last flow visited by the element instance.
 * 
 * <p>Flow state management is crucial for tracking the execution path
 * of an element instance through the simulation model.
 * 
 * @author Ivan Castilla Rodriguez
 */
public class ElementInstanceFlow {
	/** Reference to the element instance that owns this flow manager */
	private final ElementInstance elementInstance;
	
	/** Thread's initial flow - the first flow to be executed */
	private final IFlow initialFlow;
	
	/** The current flow the thread is executing */
	private IFlow currentFlow = null;
	
	/** The last flow the thread executed */
	private IFlow lastFlow = null;
	
	/**
	 * Creates a new flow manager for an element instance.
	 * 
	 * @param elementInstance The element instance that owns this flow manager
	 * @param initialFlow The first flow to be executed by this instance
	 */
	public ElementInstanceFlow(ElementInstance elementInstance, IFlow initialFlow) {
		this.elementInstance = elementInstance;
		this.initialFlow = initialFlow;
	}

	/**
	 * Gets the element instance that owns this flow manager.
	 * 
	 * @return The element instance
	 */
	public ElementInstance getElementInstance() {
		return elementInstance;
	}

	/**
	 * Gets the initial flow assigned to this element instance.
	 * 
	 * @return The initial flow
	 */
	public IFlow getInitialFlow() {
		return initialFlow;
	}
	
	/**
	 * Gets the flow currently being executed.
	 * 
	 * @return The current flow, or null if no flow is executing
	 */
	public IFlow getCurrentFlow() {
		return currentFlow;
	}
	
	/**
	 * Sets the flow currently being executed by this element instance.
	 * This also resets flow-related state in the element instance.
	 * 
	 * @param flow The flow to be performed
	 */
	public void setCurrentFlow(IFlow flow) {
		// Save previous current flow as last flow before updating
		// Only update lastFlow if there was actually a currentFlow
		if (this.currentFlow != null && flow != null) {
			this.lastFlow = this.currentFlow;
		}
		this.currentFlow = flow;
	}
	
	/**
	 * Gets the last flow visited by this element instance.
	 * 
	 * @return The last flow, or null if no flow has been visited yet
	 */
	public IFlow getLastFlow() {
		return lastFlow;
	}
	
	/**
	 * Sets the last flow visited by this element instance.
	 * 
	 * @param lastFlow The last flow visited
	 */
	public void setLastFlow(IFlow lastFlow) {
		this.lastFlow = lastFlow;
	}
	
	/**
	 * Checks if the instance is currently executing a flow.
	 * 
	 * @return true if a flow is executing, false otherwise
	 */
	public boolean isExecutingFlow() {
		return currentFlow != null;
	}
	
	/**
	 * Clears the current flow (sets it to null).
	 * Used when flow execution completes.
	 */
	public void clearCurrentFlow() {
		if (currentFlow != null) {
			this.lastFlow = currentFlow;
		}
		this.currentFlow = null;
	}
}
