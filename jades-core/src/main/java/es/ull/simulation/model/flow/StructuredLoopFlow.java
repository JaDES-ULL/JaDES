package es.ull.simulation.model.flow;

import java.util.TreeSet;

import es.ull.simulation.model.Simulation;

/**
 * A structured IFlow which defines a repetitive subflow. Different subclasses
 * of this class represent different loop structures: while-do, do-while, for...
 * Meets the Structured Loop pattern (WFP21). 
 * @author ycallero
 */
// TODO: Consider merge StructuredLoopFlows into PredefinedStructuredFlows
public abstract class StructuredLoopFlow extends AbstractStructuredFlow {
	
	/**
	 * Create a new StructuredLoopFlow starting in <code>initialSubFlow</code> and 
	 * finishing in <code>finalSubFlow</code>.
	 * @param initialSubFlow First step of the internal subflow
	 * @param finalSubFlow Last step of the internal subflow
	 */
	public StructuredLoopFlow(Simulation model, IInitializerFlow initialSubFlow, IFinalizerFlow finalSubFlow) {
		super(model);
		initialFlow = initialSubFlow;
		finalFlow = finalSubFlow;
		final TreeSet<IFlow> visited = new TreeSet<IFlow>(); 
		initialFlow.setRecursiveStructureLink(this, visited);
	}

	/**
	 * Create a new StructuredLoopFlow consisting of a unique IFlow.
	 * @param subFlow A unique IFlow defining an internal subflow
	 */
	public StructuredLoopFlow(Simulation model, ITaskFlow subFlow) {
		this(model, subFlow, subFlow);
	}
}

