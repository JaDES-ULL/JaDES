package es.ull.simulation.model.flow;

import es.ull.simulation.model.ElementInstance;
import es.ull.simulation.model.Simulation;

/**
 * A IFlow which can contain other flows. This kind of flows have a single entry point
 * <code>initialFlow</code> and a single exit point <code>finalFlow</code>, and can contain 
 * one or several internal branches.
 * @author ycallero
 *
 */
public abstract class AbstractStructuredFlow extends AbstractSingleSuccessorFlow implements ITaskFlow {
	/**	The entry point of the internal structure */
	protected IInitializerFlow initialFlow = null;
	/**	The exit point of the internal structure */
	protected IFinalizerFlow finalFlow = null;
	
	/**
	 * Creates a new structured IFlow with no initial nor final step.
	 */
	public AbstractStructuredFlow(Simulation model) {
		super(model);
	}

	@Override
	public void addPredecessor(IFlow newFlow) {
	}

	@Override
	public void afterFinalize(ElementInstance fe) {
	}

	public IFinalizerFlow getFinalFlow() {
		return finalFlow;
	}

	public IInitializerFlow getInitialFlow() {
		return initialFlow;
	}	

	/*
	 * (non-Javadoc)
	 * @see com.ull.simulation.IFlow#request(com.ull.simulation.FlowExecutor)
	 */
	public void request(ElementInstance wThread) {
		if (!wThread.wasVisited(this)) {
			if (wThread.isExecutable()) {
				if (beforeRequest(wThread))
					wThread.getElement().addRequestEvent(initialFlow, wThread.getDescendantElementInstance(initialFlow));
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
		afterFinalize(wThread);
		next(wThread);
	}
}
