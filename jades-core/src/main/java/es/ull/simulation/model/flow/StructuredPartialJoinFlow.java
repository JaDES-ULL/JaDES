package es.ull.simulation.model.flow;

import es.ull.simulation.model.Simulation;

/**
 * A structured IFlow whose initial step is a parallel IFlow and whose final step
 * is a partial join IFlow. Meets the Structured Partial Join pattern (WFP30).
 * @author ycallero
 */
public class StructuredPartialJoinFlow extends AbstractPredefinedStructuredFlow {
	
	/**
	 * Creates a new StructuredPartialJoinFlow.
	 */
	public StructuredPartialJoinFlow(Simulation model, int partialValue) {
		super(model);
		initialFlow = new ParallelFlow(model);
		initialFlow.setParent(this);
		finalFlow = new PartialJoinFlow(model, partialValue);
		finalFlow.setParent(this);
	}

}
