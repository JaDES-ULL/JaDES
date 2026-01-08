/**
 * 
 */
package es.ull.simulation.model.location;

import es.ull.simulation.info.EntityLocationInfo;
import es.ull.simulation.model.Element;
import es.ull.simulation.model.ElementInstance;
import es.ull.simulation.model.Simulation;
import es.ull.simulation.model.flow.IActionFlow;
import es.ull.simulation.model.flow.IFlow;
import es.ull.simulation.model.flow.AbstractSingleSuccessorFlow;
import es.ull.simulation.model.flow.ITaskFlow;

/**
 * A workflow step that allows {@link Element elements} to move from one {@link Location} to another.
 * The route IFlow uses a {@link IRouter} to define the path of the element, ensures that the destination is reachable, and moves the 
 * element from one location to another until reaching the destination.
 * @author Iván Castilla Rodríguez
 *
 */
public class MoveFlow extends AbstractSingleSuccessorFlow implements ITaskFlow, IActionFlow {
    /** A brief description of the route */
    private final String description;
    /** Final destination of the element */ 
    private final Location destination;
    /** Instance that returns the path for the element */
    private final IRouter router;

    /**
     * Creates a IFlow to move an element
     * @param model Model this IFlow belongs to
     * @param description A brief description of the route
     * @param destination Final destination of the element
     * @param IRouter Instance that returns the path for the element
     */
	public MoveFlow(Simulation model, String description, Location destination, IRouter IRouter) {
		super(model);
		this.description = description;
		this.destination = destination;
		this.router = IRouter;
	}


	@Override
	public String getDescription() {
		return description;
	}
    
	@Override
	public void addPredecessor(final IFlow predecessor) {
	}

	@Override
	public void afterFinalize(final ElementInstance fe) {
	}

	@Override
	public void request(final ElementInstance ei) {
		if (!ei.wasVisited(this)) {
			if (ei.isExecutable()) {
				if (beforeRequest(ei)) {
					// If already at destination, just finish the IFlow
					if (destination.equals(ei.getElement().getLocation())) {
						afterFinalize(ei);
						next(ei);
					}
					else {
						ei.getElement().keepMoving(this, ei);
					}
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

	/**
	 * Performs a partial step to the final destination
	 * @param ei Element instance moving
	 */
	public void move(final ElementInstance ei) {
		final Element elem = ei.getElement();
		final Location nextLocation = router.getNextLocationTo(elem, destination);
		if (IRouter.isUnreachableLocation(nextLocation)) {
			ei.cancel(this);
			next(ei);
    		error("Destination unreachable. Current: " + elem.getLocation() + "; destination: " + destination);
		}
		else if (IRouter.isConditionalWaitLocation(nextLocation)) {
			simul.notifyInfo(new EntityLocationInfo(simul, elem, elem.getLocation(), EntityLocationInfo.Type.COND_WAIT, getTs()));			
		}
		else if (!nextLocation.fitsIn(elem)) {
			nextLocation.waitFor(elem);
			simul.notifyInfo(new EntityLocationInfo(simul, elem, elem.getLocation(), EntityLocationInfo.Type.WAIT_FOR, getTs()));
		}
		else {
			nextLocation.enter(elem);
			if (nextLocation.equals(destination)) {
				finish(ei);
			}
			else {
				elem.keepMoving(this, ei);
			}
		}
				
	}
	
	@Override
	public void finish(final ElementInstance ei) {
		afterFinalize(ei);
		next(ei);
	}


	/**
	 * Returns the final destination 
	 * @return the final destination
	 */
	public Location getDestination() {
		return destination;
	}


	/**
	 * @return the IRouter
	 */
	public IRouter getRouter() {
		return router;
	}

}
