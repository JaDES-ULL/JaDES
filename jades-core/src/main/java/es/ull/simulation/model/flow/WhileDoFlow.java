package es.ull.simulation.model.flow;

import es.ull.simulation.condition.AbstractCondition;
import es.ull.simulation.model.ElementInstance;
import es.ull.simulation.model.Simulation;


/**
 * A structured loop IFlow which resembles a while-do loop. A precondition is
 * checked before executing the internal IFlow. If the postcondition is false,
 * this IFlow finishes.
 * @author ycallero
 */
public class WhileDoFlow extends StructuredLoopFlow {
	/** Condition which controls the loop operation. */
	protected final AbstractCondition<ElementInstance> cond;
	
	/**
	 * Create a new WhileDoFlow.
	 * @param initialSubFlow First step of the internal subflow
	 * @param finalSubFlow Last step of the internal subflow
	 * @param postCondition Break loop condition.
 	 */
	public WhileDoFlow(Simulation model, IInitializerFlow initialSubFlow, IFinalizerFlow finalSubFlow,
					   AbstractCondition<ElementInstance> postCondition) {
		super(model, initialSubFlow, finalSubFlow);
		cond = postCondition;
	}

	/**
	 * Create a new WhileDoFlow.
	 * @param subFlow A unique IFlow defining an internal subflow
	 * @param postCondition Break loop condition.
 	 */
	public WhileDoFlow(Simulation model, ITaskFlow subFlow, AbstractCondition<ElementInstance> postCondition) {
		this(model, subFlow, subFlow, postCondition);
	}

	/** 
	 * Returns the condition which controls the loop operation.
	 * @return The condition which controls the loop operation
	 */
	public AbstractCondition<ElementInstance> getCondition() {
		return cond;
	}

	/*
	 * (non-Javadoc)
	 * @see com.ull.simulation.IFlow#request(com.ull.simulation.FlowExecutor)
	 */
	public void request(ElementInstance wThread) {
		if (!wThread.wasVisited(this)) {
			if (wThread.isExecutable()) {
				if (beforeRequest(wThread)) {
					finish(wThread);
				}
				else {
					wThread.cancel(this);
					next(wThread);				
				}
			}
			else {
				wThread.updatePath(this);
				next(wThread);
			}
		} else
			wThread.notifyEnd();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.ull.simulation.ITaskFlow#finish(com.ull.simulation.FlowExecutor)
	 */
	public void finish(ElementInstance wThread) {
		// The loop condition is checked
		if (cond.check(wThread)) {
			wThread.getElement().addRequestEvent(initialFlow, wThread.getDescendantElementInstance(initialFlow));
		} else {
			super.finish(wThread);
		}
	}

}

