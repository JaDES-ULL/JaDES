/**
 * 
 */
package es.ull.simulation.model.flow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import es.ull.simulation.model.ElementInstance;
import es.ull.simulation.model.Simulation;


/**
 * A IFlow with multiple successors. Multiple successors are split nodes, that is,
 * new element instances are created from this IFlow on, when it's requested.
 * @author Iván Castilla Rodríguez
 */
public abstract class MultipleSuccessorFlow extends BasicFlow implements ISplitFlow {
	/** Successor list */
	protected final ArrayList<IFlow> successorList;

	/**
	 * Creates a IFlow with multiple successors.
	 * @param model The simulation this IFlow belongs to.
	 */
	public MultipleSuccessorFlow(final Simulation model) {
		super(model);
		successorList = new ArrayList<IFlow>();
	}

	@Override
	public void addPredecessor(final IFlow newFlow) {
	}

	@Override
	public IFlow link(final IFlow successor) {
		successorList.add(successor);
    	successor.addPredecessor(this);
    	return successor;
	}

	/**
	 * Adds a collection of IFlow's successors. This method must invoke 
	 * <code>successor.addPredecessor</code> to build the graph properly. 
	 * @param succList This IFlow's successors.
	 */
	public void link(final Collection<IFlow> succList) {
        for (IFlow succ : succList) {
        	successorList.add(succ);
        	succ.addPredecessor(this);
        }		
	}

	@Override
	public void setRecursiveStructureLink(final AbstractStructuredFlow parent, final Set<IFlow> visited) {
		 setParent(parent);
		 visited.add(this);
		 for (IFlow f : successorList)
			 if (!visited.contains(f))
				 f.setRecursiveStructureLink(parent, visited); 	
	}

	/**
	 * Returns the list of successor flows which follows this one.
	 * @return the list of successor flows which follows this one.
	 */
	public ArrayList<IFlow> getSuccessorList() {
		ArrayList<IFlow> newSuccList = new ArrayList<IFlow>();
		for (IFlow f : successorList)
			newSuccList.add(f);
		return newSuccList;
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

}
