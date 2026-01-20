package es.ull.simulation.model;

import java.util.List;

import es.ull.simulation.model.flow.BasicFlow;
import es.ull.simulation.model.flow.RequestResourcesFlow;

/**
 * Registry interface for managing simulation components.
 * Follows Dependency Inversion Principle - high-level Simulation class depends on this abstraction.
 * 
 * @author Ivan Castilla Rodriguez
 */
public interface ISimulationRegistry {
    
    /**
     * Registers a new element type in the simulation
     * @param et Element type to register
     */
    void registerElementType(ElementType et);
    
    /**
     * Registers a new resource in the simulation
     * @param res Resource to register
     */
    void registerResource(Resource res);
    
    /**
     * Registers a new resource type in the simulation
     * @param rt Resource type to register
     */
    void registerResourceType(ResourceType rt);
    
    /**
     * Registers a new work group in the simulation
     * @param wg Work group to register
     */
    void registerWorkGroup(WorkGroup wg);
    
    /**
     * Registers a new flow in the simulation
     * @param flow Flow to register
     */
    void registerFlow(BasicFlow flow);
    
    /**
     * Registers a new time-driven generator in the simulation
     * @param gen Generator to register
     */
    void registerTimeDrivenGenerator(TimeDrivenGenerator<?> gen);
    
    /**
     * Registers a new condition-driven generator in the simulation
     * @param gen Generator to register
     */
    void registerConditionDrivenGenerator(ConditionDrivenGenerator<?> gen);
    
    /**
     * Registers a new activity manager in the simulation
     * @param am Activity manager to register
     */
    void registerActivityManager(ActivityManager am);
    
    /**
     * Returns the list of registered element types
     * @return List of element types
     */
    List<ElementType> getElementTypes();
    
    /**
     * Returns the list of registered resources
     * @return List of resources
     */
    List<Resource> getResources();
    
    /**
     * Returns the list of registered resource types
     * @return List of resource types
     */
    List<ResourceType> getResourceTypes();
    
    /**
     * Returns the list of registered work groups
     * @return List of work groups
     */
    List<WorkGroup> getWorkGroups();
    
    /**
     * Returns the list of registered flows
     * @return List of flows
     */
    List<BasicFlow> getFlows();
    
    /**
     * Returns the list of registered request flows
     * @return List of request flows
     */
    List<RequestResourcesFlow> getRequestFlows();
    
    /**
     * Returns the list of registered time-driven generators
     * @return List of time-driven generators
     */
    List<TimeDrivenGenerator<?>> getTimeDrivenGenerators();
    
    /**
     * Returns the list of registered condition-driven generators
     * @return List of condition-driven generators
     */
    List<ConditionDrivenGenerator<?>> getConditionDrivenGenerators();
    
    /**
     * Returns the list of registered activity managers
     * @return List of activity managers
     */
    List<ActivityManager> getActivityManagers();
    
    /**
     * Resets all registered components
     */
    void reset();
}
