/**
 * 
 */
package es.ull.simulation.model.flow;

import es.ull.simulation.model.ElementInstance;
import es.ull.simulation.model.Simulation;


/**
 * Creates an OR IFlow which allows all the true incoming branches to pass.
 * Meets the Multi-Merge pattern (WFP8).
 * @author Iván Castilla Rodríguez
 */
public class MultiMergeFlow extends ORJoinFlow {

	/**
	 * Creates a new MultiMergeFlow.
	 */
	public MultiMergeFlow(Simulation model) {
		super(model);
	}
	
	/* (non-Javadoc)
	 * @see com.ull.simulation.AbstractMergeFlow#canPass(com.ull.simulation.FlowExecutor)
	 */
	@Override
	protected boolean canPass(ElementInstance wThread) {
		return wThread.isExecutable();
	}
}
