/**
 * 
 */
package es.ull.simulation.model.flow;

import java.util.Set;

import es.ull.simulation.model.ElementInstance;
import es.ull.simulation.model.Simulation;


/**
 * A IFlow with a unique successor.
 * @author Iván Castilla Rodríguez
 */
public abstract class AbstractSingleSuccessorFlow extends BasicFlow {
	/** The unique successor of this IFlow */
	protected IFlow successor = null;

	/**
	 * Creates a new unique successor IFlow.
	 * @param model The simulation model this IFlow belongs to
	 */
	public AbstractSingleSuccessorFlow(final Simulation model) {
		super(model);
	}
	
	@Override
	public void setRecursiveStructureLink(final AbstractStructuredFlow parent, final Set<IFlow> visited) {
		setParent(parent);
		visited.add(this);
		if (successor != null)
			if (!visited.contains(successor))
				successor.setRecursiveStructureLink(parent, visited);			
	}	

	@Override
	public IFlow link(final IFlow succ) {
		if (successor != null) {
			simul.error("Trying to link already linked IFlow " + this.getClass() + " " + this);
		}
		else {
			successor = (IFlow) succ;
			succ.addPredecessor(this);
		}
		return succ;
	}

	/**
	 * Returns the successor of the IFlow
	 * @return the successor of the IFlow
	 */
	public IFlow getSuccessor() {
		return successor;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * If this IFlow has a valid successor, requests this successor passing
	 * the same element instance. If not, the element instance finishes here, and, 
	 * if this IFlow has a valid parent, it's notified that this IFlow finished.
	 */
	public void next(final ElementInstance ei) {
		super.next(ei);
		if (successor != null) {
			ei.getElement().addRequestEvent(successor, ei);
		}
		else {
			ei.notifyEnd();
		}
	}
	
}
