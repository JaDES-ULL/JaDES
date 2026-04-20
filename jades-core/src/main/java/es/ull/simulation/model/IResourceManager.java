package es.ull.simulation.model;

import java.util.ArrayDeque;

import es.ull.simulation.model.flow.RequestResourcesFlow;

/**
 * Interface for managing seized resources of an element.
 * Follows Dependency Inversion Principle.
 * 
 * @author Ivan Castilla Rodriguez
 */
public interface IResourceManager {
    
    /**
     * Adds seized resources to the collection
     * @param resourcesId Identifier for this resource group
     * @param resources Resources to add
     */
    void addResources(int resourcesId, ArrayDeque<Resource> resources);
    
    /**
     * Removes and returns resources matching the work group
     * @param resourcesId Identifier for this resource group
     * @param wg Work group specifying which resources to release
     * @return Released resources
     */
    ArrayDeque<Resource> removeResources(int resourcesId, WorkGroup wg);
    
    /**
     * Returns all seized resources
     * @return All seized resources
     */
    ArrayDeque<Resource> getAllResources();
    
    /**
     * Returns resources seized by specific request flow and instance
     * @param reqFlow Request flow that seized the resources
     * @param ei Element instance
     * @return Seized resources for this flow and instance
     */
    ArrayDeque<Resource> getResourcesByFlow(RequestResourcesFlow reqFlow, ElementInstance ei);
    
    /**
     * Returns resources matching the work group
     * @param resourcesId Identifier for this resource group
     * @param wg Work group to match
     * @return Resources matching the work group
     */
    ArrayDeque<Resource> getResourcesByWorkGroup(int resourcesId, WorkGroup wg);
    
    /**
     * Checks if a specific resource type is acquired
     * @param rt Resource type to check
     * @return true if the resource type is acquired
     */
    boolean hasResourceType(ResourceType rt);
    
    /**
     * Releases all resources
     */
    void releaseAll();
}
