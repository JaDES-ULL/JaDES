/**
 * 
 */
package es.ull.simulation.model.flow;

import es.ull.simulation.model.ElementInstance;
import es.ull.simulation.model.Simulation;
import es.ull.simulation.model.SimulationObject;
import es.ull.simulation.model.engine.SimulationEngine;


/**
 * Basic implementation of a IFlow. Defines the default behavior of most methods. 
 * @author Iván Castilla Rodríguez
 */
public abstract class BasicFlow extends SimulationObject implements IFlow {
	/** The structured IFlow containing this IFlow. */
	protected AbstractStructuredFlow parent = null;
	
	/**
	 * Create a new basic IFlow.
	 * @param model The simulation this IFlow belongs to.
	 */
	public BasicFlow(final Simulation model) {
		super(model, model.getFlowList().size(), "F");
		model.add(this);
	}
	
	@Override
	public AbstractStructuredFlow getParent() {
		return parent;
	}
	
	@Override
	public void setParent(final AbstractStructuredFlow parent) {
		this.parent = (AbstractStructuredFlow)parent;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * By default, returns true.
	 * @return True by default.
	 */
	public boolean beforeRequest(final ElementInstance ei) {
		return true;
	}

	/**
	 * Assigns this IFlow as the last IFlow visited by the element instance.
	 * @param ei Element instance which requested this IFlow.
	 */
	public void next(final ElementInstance ei) {
		ei.setLastFlow(this);
	}

	@Override
	public void assignSimulation(final SimulationEngine simul) {
	}
}
