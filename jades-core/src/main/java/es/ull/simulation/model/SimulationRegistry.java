package es.ull.simulation.model;

import java.util.ArrayList;
import java.util.List;

import es.ull.simulation.model.flow.BasicFlow;
import es.ull.simulation.model.flow.RequestResourcesFlow;

/**
 * Default implementation of simulation component registry.
 * Manages all simulation components in collections.
 * 
 * @author Ivan Castilla Rodriguez
 */
public class SimulationRegistry implements ISimulationRegistry {
    
    /** List of element types present in the simulation. */
    private final ArrayList<ElementType> elementTypeList = new ArrayList<>();
    /** List of resources present in the simulation. */
    private final ArrayList<Resource> resourceList = new ArrayList<>();
    /** List of resource types present in the simulation. */
    private final ArrayList<ResourceType> resourceTypeList = new ArrayList<>();
    /** List of workgroups present in the simulation */
    private final ArrayList<WorkGroup> workGroupList = new ArrayList<>();
    /** List of flows present in the simulation */
    private final ArrayList<BasicFlow> flowList = new ArrayList<>();
    /** List of request flows present in the simulation. */
    private final ArrayList<RequestResourcesFlow> reqFlowList = new ArrayList<>();
    /** List of time-driven element generators of the simulation. */
    private final ArrayList<TimeDrivenGenerator<?>> tGenList = new ArrayList<>();
    /** List of condition-driven element generators of the simulation. */
    private final ArrayList<ConditionDrivenGenerator<?>> cGenList = new ArrayList<>();
    /** List of activity managers that partition the simulation. */
    private final ArrayList<ActivityManager> amList = new ArrayList<>();
    
    @Override
    public void registerElementType(ElementType et) {
        elementTypeList.add(et);
    }
    
    @Override
    public void registerResource(Resource res) {
        resourceList.add(res);
    }
    
    @Override
    public void registerResourceType(ResourceType rt) {
        resourceTypeList.add(rt);
    }
    
    @Override
    public void registerWorkGroup(WorkGroup wg) {
        workGroupList.add(wg);
    }
    
    @Override
    public void registerFlow(BasicFlow flow) {
        flowList.add(flow);
        if (flow instanceof RequestResourcesFlow)
            reqFlowList.add((RequestResourcesFlow) flow);
    }
    
    @Override
    public void registerTimeDrivenGenerator(TimeDrivenGenerator<?> gen) {
        tGenList.add(gen);
    }
    
    @Override
    public void registerConditionDrivenGenerator(ConditionDrivenGenerator<?> gen) {
        cGenList.add(gen);
    }
    
    @Override
    public void registerActivityManager(ActivityManager am) {
        amList.add(am);
    }
    
    @Override
    public List<ElementType> getElementTypes() {
        return elementTypeList;
    }
    
    @Override
    public List<Resource> getResources() {
        return resourceList;
    }
    
    @Override
    public List<ResourceType> getResourceTypes() {
        return resourceTypeList;
    }
    
    @Override
    public List<WorkGroup> getWorkGroups() {
        return workGroupList;
    }
    
    @Override
    public List<BasicFlow> getFlows() {
        return flowList;
    }
    
    @Override
    public List<RequestResourcesFlow> getRequestFlows() {
        return reqFlowList;
    }
    
    @Override
    public List<TimeDrivenGenerator<?>> getTimeDrivenGenerators() {
        return tGenList;
    }
    
    @Override
    public List<ConditionDrivenGenerator<?>> getConditionDrivenGenerators() {
        return cGenList;
    }
    
    @Override
    public List<ActivityManager> getActivityManagers() {
        return amList;
    }
    
    @Override
    public void reset() {
        // Reset all lists but keep instances for reuse
        elementTypeList.clear();
        resourceList.clear();
        resourceTypeList.clear();
        workGroupList.clear();
        flowList.clear();
        reqFlowList.clear();
        tGenList.clear();
        cGenList.clear();
        amList.clear();
    }
}
