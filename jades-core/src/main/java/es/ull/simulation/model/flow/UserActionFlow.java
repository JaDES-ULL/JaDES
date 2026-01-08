/**
 * 
 */
package es.ull.simulation.model.flow;

import es.ull.simulation.model.ElementInstance;
import es.ull.simulation.model.Simulation;

/**
 * A simple IFlow to execute user actions at some point of the workflow. The user specifies the actions in the {@link #userAction(ElementInstance)} method.
 * @author Iván Castilla Rodríguez
 *
 */
public class UserActionFlow extends AbstractSingleSuccessorFlow implements IActionFlow {
	/** A brief description of the IFlow */
	final private String description;
	/**
	 * Creates a user action IFlow
	 * @param model The simulation model this IFlow belongs to
	 * @param description A brief description of the IFlow
	 */
	public UserActionFlow(final Simulation model, String description) {
		super(model);
		this.description = description;
	}

	@Override
	public void addPredecessor(IFlow predecessor) {
	}

	/**
	 * The code that this IFlow executes
	 * @param ei An element instance invoking this IFlow
	 */
	public void userAction(ElementInstance ei) {
		
	}
	@Override
	public void request(ElementInstance ei) {
		if (!ei.wasVisited(this)) {
			if (ei.isExecutable()) {
				if (beforeRequest(ei)) {
					userAction(ei);
					next(ei);
				}
				else {
					ei.cancel(this);
					next(ei);
				}
			}
			else {
				ei.updatePath(this);
				next(ei);
			}
		} else
			ei.notifyEnd();
	}

	@Override
	public String getDescription() {
		return description;
	}

}
