package es.ull.simulation.model.flow;

import es.ull.simulation.model.Simulation;

/**
 * A structured IFlow whose initial step is a parallel IFlow and whose final step
 * is a discriminator IFlow. Meets the Structured Discriminator pattern (WFP9). 
 * @author ycallero
 */
public class StructuredDiscriminatorFlow extends AbstractPredefinedStructuredFlow {
	/**
	 * Create a new StructureDiscriminatorMetaFlow.
	 */
	public StructuredDiscriminatorFlow(Simulation model) {
		super(model);
		initialFlow = new ParallelFlow(model);
		initialFlow.setParent(this);
		finalFlow = new DiscriminatorFlow(model);
		finalFlow.setParent(this);
	}

}
