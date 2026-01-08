/**
 * 
 */
package es.ull.simulation.factory;

import es.ull.simulation.functions.AbstractTimeFunction;
import es.ull.simulation.condition.AbstractCondition;
import es.ull.simulation.model.ElementInstance;
import es.ull.simulation.model.ElementType;
import es.ull.simulation.model.Resource;
import es.ull.simulation.model.ResourceType;
import es.ull.simulation.model.Simulation;
import es.ull.simulation.model.ISimulationCycle;
import es.ull.simulation.model.TimeDrivenElementGenerator;
import es.ull.simulation.model.WorkGroup;
import es.ull.simulation.model.flow.IFlow;
import es.ull.simulation.model.flow.IInitializerFlow;


/**
 * @author Iván Castilla Rodríguez
 *
 */
public class SimulationFactory {
	private final static String workingPkg = Simulation.class.getPackage().getName();
	private final Simulation simul;
	private int rtId;
	private int flowId;
	private int creId;
	private int condId;

	/**
	 * @param id
	 * @param description
	 * @param startTs
	 * @param endTs
	 */
	public SimulationFactory(int id, String description) {
		simul = new Simulation(id, description);
	}

	public Simulation getSimulation() {
		return simul;
	}

	public TimeDrivenElementGenerator getTimeDrivenElementGeneratorInstance(AbstractTimeFunction elem, ISimulationCycle cycle) throws ClassCastException {
		return new TimeDrivenElementGenerator(simul, elem, cycle);
	}

	public TimeDrivenElementGenerator getTimeDrivenElementGeneratorInstance(AbstractTimeFunction elem, ElementType et, IInitializerFlow IFlow, ISimulationCycle cycle) throws ClassCastException {
		return new TimeDrivenElementGenerator(simul, elem,  et, IFlow, cycle);
	}

	public TimeDrivenElementGenerator getTimeDrivenElementGeneratorInstance(AbstractTimeFunction elem, ISimulationCycle cycle, SimulationUserCode userMethods) throws ClassCastException {
		// Prepare the constructor call
		final String constructorStr = "(Simulation sim, TimeFunction nElem, ISimulationCycle cycle) {super(sim, nElem, cycle);}";
		// Prepare the new params.
		final Object obj = StandardCompilator.getInstance(workingPkg, TimeDrivenElementGenerator.class.getName(), creId++, constructorStr, userMethods, simul, elem, cycle);
		if (obj != null)
			return (TimeDrivenElementGenerator)obj;
		return null;
	}

	public TimeDrivenElementGenerator getTimeDrivenElementGeneratorInstance(AbstractTimeFunction elem, ElementType et, IInitializerFlow IFlow, ISimulationCycle cycle, SimulationUserCode userMethods) throws ClassCastException {
		// Prepare the constructor call
		final String constructorStr = "(Simulation sim, TimeFunction nElem, ElementType et, IInitializerFlow IFlow, ISimulationCycle cycle) {super(sim, nElem, et, IFlow, cycle);}";
		// Prepare the new params.
		final Object obj = StandardCompilator.getInstance(workingPkg, TimeDrivenElementGenerator.class.getName(), creId++, constructorStr, userMethods, simul, elem, et, IFlow, cycle);
		if (obj != null)
			return (TimeDrivenElementGenerator)obj;
		return null;
	}
	public ElementType getElementTypeInstance(String description) throws ClassCastException {
		return new ElementType(simul, description);
	}

	public ElementType getElementTypeInstance(String description, int priority) throws ClassCastException {
		return new ElementType(simul, description, priority);
	}

	public Resource getResourceInstance(String description) throws ClassCastException {
		return new Resource(simul, description);
	}

	public ResourceType getResourceTypeInstance(String description) throws ClassCastException {
		return new ResourceType(simul, description);
	}

	public ResourceType getResourceTypeInstance(String description, SimulationUserCode userMethods) throws ClassCastException {
		// Prepare the constructor call
		String constructorStr = "(int id, Simulation simul, String description) {super(id, simul, description);}";
		// Prepare the new params.
		Object obj = StandardCompilator.getInstance(workingPkg, "ResourceType", rtId, constructorStr, userMethods, rtId++, simul, description);
		if (obj != null)
			return (ResourceType)obj;
		return null;
	}

	public WorkGroup getWorkGroupInstance(ResourceType[] rts, int[] needed) throws ClassCastException {
		ResourceType[] temp = new ResourceType[rts.length];
		for (int i = 0; i < rts.length; i++)
			temp[i] = (ResourceType)rts[i];
		return new WorkGroup(simul, temp, needed);
	}

	public IFlow getFlowInstance(String flowType, Object... params) throws ClassCastException {
		return FlowFactory.getInstance(flowId++, flowType, simul, params);
	}

	public IFlow getFlowInstance(String flowType, SimulationUserCode userMethods, Object... params) throws ClassCastException {
		return FlowFactory.getInstance(flowId++, flowType, userMethods, simul, params);
	}

	public AbstractCondition<ElementInstance> getCustomizedConditionInstance(String imports, String condition) {
		return ConditionFactory.getInstance(condId++, imports, condition);
	}
}	
