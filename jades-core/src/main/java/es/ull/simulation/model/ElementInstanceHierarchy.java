package es.ull.simulation.model;

import java.util.ArrayList;

/**
 * Manages the hierarchical structure of element instances following the Composite pattern.
 * This class encapsulates the parent-child relationships and descendant management,
 * separating hierarchy concerns from the main ElementInstance business logic.
 * 
 * <p>This supports three types of element instances:
 * <ul>
 * <li>Main instance: Root instance with no parent</li>
 * <li>Descendant thread: Child instance for structured flow execution</li>
 * <li>Subsequent thread: Sibling instance after a split</li>
 * </ul>
 * 
 * @author Ivan Castilla Rodriguez
 */
public class ElementInstanceHierarchy {
	/** Reference to the element instance that owns this hierarchy */
	private final ElementInstance elementInstance;
	
	/** The parent element instance */
	private final ElementInstance parent;
	
	/** The descendant element instances (children) */
	private final ArrayList<ElementInstance> descendants;
	
	/**
	 * Creates a new hierarchy manager for an element instance.
	 * 
	 * @param elementInstance The element instance that owns this hierarchy
	 * @param parent The parent element instance, or null if this is a root instance
	 */
	public ElementInstanceHierarchy(ElementInstance elementInstance, ElementInstance parent) {
		this.elementInstance = elementInstance;
		this.parent = parent;
		this.descendants = new ArrayList<ElementInstance>();
		
		// Auto-register with parent if exists
		if (parent != null) {
			parent.addDescendant(elementInstance);
		}
	}
	
	/**
	 * Gets the parent element instance.
	 * 
	 * @return The parent element instance, or null if this is a root instance
	 */
	public ElementInstance getParent() {
		return parent;
	}
	
	/**
	 * Gets the list of descendant element instances (children).
	 * 
	 * @return The list of descendants
	 */
	public ArrayList<ElementInstance> getDescendants() {
		return descendants;
	}
	
	/**
	 * Checks if this instance is a root instance (has no parent).
	 * 
	 * @return true if this is a root instance, false otherwise
	 */
	public boolean isRoot() {
		return parent == null;
	}
	
	/**
	 * Checks if this instance has any descendants.
	 * 
	 * @return true if there are descendants, false otherwise
	 */
	public boolean hasDescendants() {
		return !descendants.isEmpty();
	}
	
	/**
	 * Gets the number of direct descendants.
	 * 
	 * @return The number of descendants
	 */
	public int getDescendantCount() {
		return descendants.size();
	}
	
	/**
	 * Adds a descendant element instance to this instance's children.
	 * This is called internally by the hierarchy during instance creation.
	 * 
	 * @param descendant The descendant to add
	 */
	void addDescendant(ElementInstance descendant) {
		descendants.add(descendant);
	}
	
	/**
	 * Removes a descendant element instance from this instance's children.
	 * This is typically called when a child finishes execution.
	 * 
	 * @param descendant The descendant to remove
	 */
	void removeDescendant(ElementInstance descendant) {
		descendants.remove(descendant);
	}
	
	/**
	 * Notifies the parent that this instance has finished execution.
	 * If this is the last descendant, notifies the parent to continue execution.
	 * If this is a root instance with no more descendants, notifies the element to end.
	 */
	public void notifyEndToParent() {
		if (parent != null) {
			parent.getHierarchy().removeDescendant(elementInstance);
			
			// If parent has no more descendants and has a current flow, finish it
			if (!parent.getHierarchy().hasDescendants() && parent.getCurrentFlow() != null) {
				((es.ull.simulation.model.flow.ITaskFlow) parent.getCurrentFlow()).finish(parent);
			}
		} else if (!hasDescendants()) {
			// Root instance with no descendants: notify element to end
			elementInstance.getElement().notifyEnd();
		}
	}
	
	/**
	 * Gets the root instance of this hierarchy tree.
	 * Traverses up the parent chain until finding the root.
	 * 
	 * @return The root element instance
	 */
	public ElementInstance getRootInstance() {
		ElementInstance current = elementInstance;
		while (current.getHierarchy().getParent() != null) {
			current = current.getHierarchy().getParent();
		}
		return current;
	}
}
