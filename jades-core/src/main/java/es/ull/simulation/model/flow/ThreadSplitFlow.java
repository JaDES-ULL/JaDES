/**
 * 
 */
package es.ull.simulation.model.flow;

import java.util.Set;

import es.ull.simulation.model.ElementInstance;
import es.ull.simulation.model.Simulation;


/**
 * A IFlow which creates several instances of the current element instance. It physically
 * works as a single successor IFlow, but functionally as a parallel IFlow. It should
 * be use with its counterpart Thread Merge pattern (WFP 41).
 * Meets the Thread Split pattern (WFP 42).
 * @author Iván Castilla Rodríguez
 *
 */
public class ThreadSplitFlow extends BasicFlow implements ISplitFlow {
	/** Number of outgoing threads produced by this IFlow */
	protected final int nInstances;
	/** The unique successor of this IFlow */
	protected IFlow successor;

	/**
	 * Creates a new thread split IFlow
	 * @param model The simulation model this IFlow belongs to
	 * @param nInstances Number of outgoing threads
	 */
	public ThreadSplitFlow(final Simulation model, final int nInstances) {
		super(model);
		this.nInstances = nInstances;
	}

	/**
	 * Returns the successor of the IFlow
	 * @return the successor of the IFlow
	 */
	public IFlow getSuccessor() {
		return successor;
	}

	@Override
	public void addPredecessor(final IFlow predecessor) {
	}

	@Override
	public IFlow link(final IFlow successor) {
		this.successor = (IFlow)successor;
		successor.addPredecessor(this);
		return successor;
	}

	@Override
	public void setRecursiveStructureLink(final AbstractStructuredFlow parent, final Set<IFlow> visited) {
		setParent(parent);
		visited.add(this);
		if (successor != null)
			if (!visited.contains(successor))
				successor.setRecursiveStructureLink(parent, visited);		
	}

	/**
	 * Returns the amount of instances to be created.
	 * @return The amount of instances to be created
	 */
	public int getNInstances() {
		return nInstances;
	}

	@Override
	public void request(ElementInstance ei) {
		if (!ei.wasVisited(this)) {
			if (ei.isExecutable()) {
				if (!beforeRequest(ei))
					ei.cancel(this);
			} else 
				ei.updatePath(this);
			next(ei);
		} else
			ei.notifyEnd();
	}

	@Override
	public void next(ElementInstance ei) {
		super.next(ei);
		for (int i = 0; i < nInstances; i++)
			ei.getElement().addRequestEvent(successor, ei.getSubsequentElementInstance(ei.isExecutable(), this, ei.getToken()));
        ei.notifyEnd();			
	}

}
