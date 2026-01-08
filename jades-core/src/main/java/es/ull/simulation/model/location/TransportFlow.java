/**
 * 
 */
package es.ull.simulation.model.location;

import java.util.ArrayDeque;

import es.ull.simulation.model.Element;
import es.ull.simulation.model.ElementInstance;
import es.ull.simulation.model.Resource;
import es.ull.simulation.model.ResourceType;
import es.ull.simulation.model.Simulation;
import es.ull.simulation.model.flow.IActionFlow;
import es.ull.simulation.model.flow.IFlow;
import es.ull.simulation.model.flow.AbstractSingleSuccessorFlow;
import es.ull.simulation.model.flow.ITaskFlow;

/**
 * A workflow step that selects a resource type as a transport, and moves an {@link Element} from one {@link Location} to another on that transport.
 * The route IFlow uses a {@link IRouter} to define the path of the element, ensures that the destination is reachable, and moves the 
 * element and the transport from one location to another until reaching the destination. Only the size of the resource is used to check the capacity of 
 * the pathway. The element must have seized at least one resource belonging to the specified resource type.
 * @author Iván Castilla Rodríguez
 *
 */
public class TransportFlow extends AbstractSingleSuccessorFlow implements ITaskFlow, IActionFlow {
    /** A brief description of the route */
    private final String description;
    /** Final destination of the element */ 
    private final Location destination;
    /** Resource type that will act as a transport for the element */
    private final ResourceType rtTransport;
    /** Instance that returns the path for the element */
    private final IRouter IRouter;

    /**
     * Creates a IFlow to move an element
     * @param model Model this IFlow belongs to
     * @param description A brief description of the route
     * @param destination Final destination of the element
     * @param IRouter Instance that returns the path for the element
     */
	public TransportFlow(Simulation model, String description, Location destination, IRouter IRouter, ResourceType rtTransport) {
		super(model);
		this.description = description;
		this.destination = destination;
		this.rtTransport = rtTransport;
		this.IRouter = IRouter;
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
					final ArrayDeque<Resource> resList = ei.getElement().getCaughtResources();
					Resource resFound = null;
					for (final Resource res : resList) {
						if (rtTransport.equals(res.getCurrentResourceType())) {
							resFound = res;
							break;
						}
					}
					if (resFound == null) {
						error("Element " + ei.getElement() + " has not seized any resource of type " + rtTransport + " before moving");
						ei.cancel(this);
						next(ei);
					}
					else {
						resFound.startTransport(ei);
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
		return IRouter;
	}

}
