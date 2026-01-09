package es.ull.simulation.model;

import es.ull.simulation.model.flow.IInitializerFlow;

/**
 * Manages workflow execution for an Element.
 * Handles the initial flow and main instance lifecycle.
 * 
 * <p>This class encapsulates the workflow-related state and operations
 * that were previously part of the Element class, improving separation
 * of concerns and testability.</p>
 * 
 * <p>Extracted from Element class during Phase 3 refactoring to improve
 * code organization and test coverage.</p>
 * 
 * @author Iván Castilla Rodríguez
 */
public class ElementFlow {
	/** Reference to the owning element */
	private final Element element;
	
	/** First step of the workflow for this element */
	private final IInitializerFlow initialFlow;
	
	/** Main element instance managing workflow execution */
	private ElementInstance mainInstance = null;
	
	/**
	 * Creates a new ElementFlow manager.
	 * 
	 * @param element The element that owns this flow manager
	 * @param initialFlow The first step of the workflow, or null if no workflow
	 */
	public ElementFlow(final Element element, final IInitializerFlow initialFlow) {
		this.element = element;
		this.initialFlow = initialFlow;
		this.mainInstance = null;
	}
	
	/**
	 * Returns the initial flow for this element.
	 * 
	 * @return The initial flow, or null if the element has no workflow
	 */
	public IInitializerFlow getInitialFlow() {
		return initialFlow;
	}
	
	/**
	 * Returns the main element instance.
	 * 
	 * @return The main element instance, or null if not initialized
	 */
	public ElementInstance getMainInstance() {
		return mainInstance;
	}
	
	/**
	 * Sets the main element instance.
	 * 
	 * @param mainInstance The main element instance
	 */
	public void setMainInstance(final ElementInstance mainInstance) {
		this.mainInstance = mainInstance;
	}
	
	/**
	 * Checks if this element has an initial flow.
	 * 
	 * @return true if initialFlow is not null, false otherwise
	 */
	public boolean hasInitialFlow() {
		return initialFlow != null;
	}
	
	/**
	 * Initializes the main instance for this element.
	 * This method should be called during element creation (onCreate).
	 * 
	 * @return The created main instance, or null if there's no initial flow
	 */
	public ElementInstance initializeMainInstance() {
		if (initialFlow != null) {
			mainInstance = ElementInstance.getMainElementInstance(element);
			return mainInstance;
		}
		return null;
	}
}
