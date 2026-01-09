package es.ull.simulation.model;

import es.ull.simulation.model.flow.RequestResourcesFlow.ActivityWorkGroup;

/**
 * Manages resource-related tracking and state for an element instance.
 * This class encapsulates the workgroup execution, arrival tracking, and
 * remaining task information needed during activity execution.
 * 
 * <p>This includes:
 * <ul>
 * <li>The workgroup used to carry out the current activity</li>
 * <li>Arrival order among other element instances</li>
 * <li>Arrival timestamp for the current activity</li>
 * <li>Remaining task proportion for interruptible activities</li>
 * </ul>
 * 
 * @author Ivan Castilla Rodriguez
 */
public class ElementInstanceResources {
	/** Reference to the element instance that owns this resource manager */
	private final ElementInstance elementInstance;
	
	/** The workgroup which is used to carry out this activity. 
	 * If null, the activity has not been carried out. */
	private ActivityWorkGroup executionWG = null;
	
	/** The arrival order of this element instance relatively to the rest of 
	 * element instances in the same activity manager. */
	private int arrivalOrder;
	
	/** The simulation timestamp when this element instance was requested. */
	private long arrivalTs = -1;
	
	/** The proportion of time left to finish the activity. 
	 * Used in interruptible activities. Range: [0.0, 1.0] */
	private double remainingTask = 0.0;
	
	/**
	 * Creates a new resource manager for an element instance.
	 * 
	 * @param elementInstance The element instance that owns this resource manager
	 */
	public ElementInstanceResources(ElementInstance elementInstance) {
		this.elementInstance = elementInstance;
	}
	
	/**
	 * Gets the workgroup that the element instance is using to execute a resource handler flow.
	 * 
	 * @return the workgroup, or null if no activity is being executed
	 */
	public ActivityWorkGroup getExecutionWG() {
		return executionWG;
	}
	
	/**
	 * Sets the workgroup used to carry out the activity.
	 * 
	 * @param executionWG the workgroup which is used to carry out this activity
	 */
	public void setExecutionWG(ActivityWorkGroup executionWG) {
		this.executionWG = executionWG;
	}
	
	/**
	 * Gets the order this instance occupies among the rest of element instances.
	 * 
	 * @return the order of arrival of this element instance to request the activity
	 */
	public int getArrivalOrder() {
		return arrivalOrder;
	}
	
	/**
	 * Sets the order this instance occupies among the rest of element instances.
	 * 
	 * @param arrivalOrder the order of arrival of this element instance to request the activity
	 */
	public void setArrivalOrder(int arrivalOrder) {
		this.arrivalOrder = arrivalOrder;
	}
	
	/**
	 * Gets the timestamp when this element instance arrives to request the current activity.
	 * 
	 * @return the timestamp when this element instance arrives to request the current activity
	 */
	public long getArrivalTs() {
		return arrivalTs;
	}
	
	/**
	 * Sets the timestamp when this element instance arrives to request the current activity.
	 * 
	 * @param arrivalTs the timestamp when this element instance arrives to request the current activity
	 */
	public void setArrivalTs(long arrivalTs) {
		this.arrivalTs = arrivalTs;
	}
	
	/**
	 * Gets the proportion of time left to finish the activity.
	 * 
	 * @return the remaining task proportion (0.0 to 1.0)
	 */
	public double getRemainingTask() {
		return remainingTask;
	}
	
	/**
	 * Sets the proportion of time left to finish the activity.
	 * 
	 * @param remainingTask the remaining task proportion (0.0 to 1.0)
	 */
	public void setRemainingTask(double remainingTask) {
		this.remainingTask = remainingTask;
	}
	
	/**
	 * Resets resource tracking state for a new activity.
	 * Clears the execution workgroup and arrival timestamp.
	 */
	public void resetForNewActivity() {
		this.executionWG = null;
		this.arrivalTs = -1;
	}
	
	/**
	 * Checks if the instance has an active workgroup assigned.
	 * 
	 * @return true if a workgroup is assigned, false otherwise
	 */
	public boolean hasActiveWorkGroup() {
		return executionWG != null;
	}
	
	/**
	 * Checks if the instance has arrived (has a valid arrival timestamp).
	 * 
	 * @return true if arrived (arrivalTs >= 0), false otherwise
	 */
	public boolean hasArrived() {
		return arrivalTs >= 0;
	}
}
