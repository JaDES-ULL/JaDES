package es.ull.simulation.model;

import es.ull.simulation.info.EntityLocationInfo;
import es.ull.simulation.model.location.Location;
import es.ull.simulation.model.location.MoveFlow;

/**
 * Manages the movement functionality of an Element, implementing the IMovable interface logic.
 * Handles location tracking, movement events, and location notifications.
 * 
 * Extracted from Element class to improve separation of concerns and maintainability.
 * 
 * @author Iván Castilla Rodríguez
 */
public class ElementMovement {
	/** Reference to the owning element */
	private final Element element;
	/** The current location of the element */
	private Location currentLocation;
	/** The initial location of the element */
	private final Location initLocation;
	/** The current element instance that drives the movement of the element */
	private ElementInstance movingInstance = null;
	/** The size of the element (capacity) */
	private final int size;
	
	/**
	 * Creates a movement manager for an element
	 * @param element The element that owns this movement manager
	 * @param size The size of the element
	 * @param initLocation The initial location of the element (can be null)
	 */
	public ElementMovement(final Element element, final int size, final Location initLocation) {
		this.element = element;
		this.size = size;
		this.initLocation = initLocation;
		this.currentLocation = null;
	}
	
	/**
	 * Returns the element's capacity (size)
	 * @return the element's capacity
	 */
	public int getCapacity() {
		return size;
	}
	
	/**
	 * Returns the current location of the element
	 * @return the current location
	 */
	public Location getLocation() {
		return currentLocation;
	}
	
	/**
	 * Returns the initial location of the element
	 * @return the initial location (may be null)
	 */
	public Location getInitLocation() {
		return initLocation;
	}
	
	/**
	 * Returns the element instance currently driving the movement
	 * @return the moving instance
	 */
	public ElementInstance getMovingInstance() {
		return movingInstance;
	}
	
	/**
	 * Sets the element instance that drives the movement
	 * @param movingInstance The element instance moving
	 */
	public void setMovingInstance(final ElementInstance movingInstance) {
		this.movingInstance = movingInstance;
	}
	
	/**
	 * Sets the location of the element and notifies the simulation about location changes.
	 * If this is the first location assignment, notifies START.
	 * Otherwise, notifies LEAVE from current location and ARRIVE at new location.
	 * 
	 * @param location The new location
	 */
	public void setLocation(final Location location) {
		if (currentLocation == null) {
			element.getSimulation().notifyInfo(new EntityLocationInfo(element.getSimulation(), element, location,
					EntityLocationInfo.Type.START, element.getTs()));
			currentLocation = location;
		}
		else {
			element.getSimulation().notifyInfo(new EntityLocationInfo(element.getSimulation(), element, currentLocation,
					EntityLocationInfo.Type.LEAVE, element.getTs()));
			currentLocation = location;
			element.getSimulation().notifyInfo(new EntityLocationInfo(element.getSimulation(), element, currentLocation,
					EntityLocationInfo.Type.ARRIVE, element.getTs()));
		}
	}
	
	/**
	 * Initializes the element at its initial location.
	 * Returns true if successfully placed, false otherwise.
	 * 
	 * @return true if element fits and entered the location; false otherwise
	 */
	public boolean initializeLocation() {
		if (initLocation != null) {
			if (initLocation.fitsIn(element)) {
				initLocation.enter(element);
				return true;
			}
			else {
				element.error("Unable to initialize element. Not enough space in location "
						+ initLocation + " (available: " + initLocation.getAvailableCapacity() +
						" - required: " + size + ")");
				return false;
			}
		}
		return true; // No init location means no issue
	}
	
	/**
	 * Handles notification that a location has become available for the element.
	 * The element enters the location and continues or finishes its movement flow.
	 * 
	 * @param location The location that became available
	 */
	public void notifyLocationAvailable(final Location location) {
		location.enter(element);

    	final MoveFlow flow = (MoveFlow)movingInstance.getCurrentFlow();
		
		if (currentLocation.equals(flow.getDestination())) {
			flow.finish(movingInstance);
		}
		else {
			keepMoving(flow, movingInstance);
		}
	}
	
	/**
	 * Makes the element issue a move event to continue the movement to its destination.
	 * Creates a MoveEvent scheduled at current time plus the delay at exit from current location.
	 * 
	 * @param flow The flow indicating the destination
	 * @param ei Element instance moving
	 */
	public void keepMoving(final MoveFlow flow, final ElementInstance ei) {
    	movingInstance = ei;
		final Element.MoveEvent mEvent = element.new MoveEvent(
			element.getTs() + currentLocation.getDelayAtExit(element), flow, ei);
    	element.getSimulation().scheduleEvent(mEvent);		
	}
}
