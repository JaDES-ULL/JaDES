package es.ull.simulation.model;

import java.util.ArrayDeque;
import java.util.ArrayList;

/**
 * Interface for resource engine operations following Dependency Inversion Principle.
 * Decouples Resource from concrete ResourceEngine implementation, enabling better testability
 * and flexibility.
 * 
 * This interface defines the contract for resource state management, availability checking,
 * and interaction with element instances during simulation execution.
 * 
 * @author Dependency Inversion Refactoring
 */
public interface IResourceEngine {
    
    /**
     * Notifies that the resource execution has ended.
     */
    void notifyEnd();
    
    /**
     * Returns the element instance that currently owns this resource.
     * 
     * @return The current element, or null if the resource is not seized
     */
    Element getCurrentElement();
    
    /**
     * Checks if the resource is available for a specific resource type.
     * 
     * @param rt The resource type to check availability for
     * @return True if the resource is available for the specified type
     */
    boolean isAvailable(ResourceType rt);
    
    /**
     * Sets the cancellation state of the resource.
     * 
     * @param available True if the resource should be marked as not canceled
     */
    void setNotCanceled(boolean available);
    
    /**
     * Attempts to add this resource to a solution for resource allocation.
     * 
     * @param solution The collection of resources forming the solution
     * @param rt The resource type being requested
     * @param ei The element instance requesting the resource
     * @return True if the resource was successfully added to the solution
     */
    boolean add2Solution(ArrayDeque<Resource> solution, ResourceType rt, ElementInstance ei);
    
    /**
     * Removes this resource from a solution.
     * 
     * @param solution The collection of resources to remove from
     * @param ei The element instance releasing the resource
     */
    void removeFromSolution(ArrayDeque<Resource> solution, ElementInstance ei);
    
    /**
     * Marks the resource as caught/seized by an element instance.
     * 
     * @param ei The element instance catching the resource
     * @return The availability timestamp for this resource type
     */
    long catchResource(ElementInstance ei);
    
    /**
     * Releases the resource from the current element instance.
     * 
     * @param ei The element instance releasing the resource
     * @return True if successfully released, false if timeout occurred
     */
    boolean releaseResource(ElementInstance ei);
    
    /**
     * Returns the list of activity managers associated with current resource roles.
     * 
     * @return List of current activity managers
     */
    ArrayList<ActivityManager> getCurrentManagers();
    
    /**
     * Increments the counter of valid timetable entries.
     * 
     * @return The updated count of valid entries
     */
    int incValidTimeTableEntries();
    
    /**
     * Decrements the counter of valid timetable entries.
     * 
     * @return The updated count of valid entries
     */
    int decValidTimeTableEntries();
    
    /**
     * Returns the number of valid timetable entries.
     * 
     * @return The count of valid timetable entries
     */
    int getValidTimeTableEntries();
    
    /**
     * Adds a role to this resource with an availability end timestamp.
     * 
     * @param role The resource type/role to add
     * @param ts The timestamp when availability ends
     */
    void addRole(ResourceType role, long ts);
    
    /**
     * Removes a role from this resource if its availability has expired.
     * 
     * @param role The resource type/role to remove
     */
    void removeRole(ResourceType role);
    
    /**
     * Notifies all current activity managers that the resource state has changed.
     */
    void notifyCurrentManagers();
}
