/**
 * 
 */
package es.ull.simulation.model;

import java.util.ArrayDeque;
import java.util.Map.Entry;
import java.util.TreeMap;

import es.ull.simulation.info.ElementInfo;
import es.ull.simulation.info.EntityLocationInfo;
import es.ull.simulation.model.engine.ElementEngine;
import es.ull.simulation.model.engine.SimulationEngine;
import es.ull.simulation.model.flow.IFlow;
import es.ull.simulation.model.flow.IInitializerFlow;
import es.ull.simulation.model.flow.ReleaseResourcesFlow;
import es.ull.simulation.model.flow.RequestResourcesFlow;
import es.ull.simulation.model.flow.ITaskFlow;
import es.ull.simulation.model.location.Location;
import es.ull.simulation.model.location.IMovable;
import es.ull.simulation.model.location.MoveFlow;
import es.ull.simulation.variable.EnumVariable;
import es.ull.simulation.utils.Prioritizable;

/**
 * An entity capable of following a {@link IFlow workflow}. Elements have a {@link ElementType type} and
 * interact with {@link Resource resources}by means of
 * {@link es.ull.simulation.model.flow.IResourceHandlerFlow resource handler flows}
 * Elements can also move from a {@link Location} to another.   
 * @author Iván Castilla Rodríguez
 *
 */
public class Element extends VariableStoreSimulationObject implements Prioritizable, IEventSource, IMovable {
	/** Element type */
	protected ElementType elementType;
	/** Workflow manager for initial flow and main instance */
	private final ElementFlow flowManager;
	/** If true, the element is in exclusive mode, and cannot perform other exclusive tasks concurrently */ 
	protected boolean exclusive = false;
	/** Movement manager for location and transport */
	private final ElementMovement movementManager;
    /** Collection manager for seized resources - DIP: depends on abstraction */
    final protected IResourceManager resourceManager;
	/** The engine that executes specific behavior of the element */
	private ElementEngine engine;
	
	/**
	 * Creates an element with a type and initial IFlow; and 0 size.
	 * @param simul Simulation model this element belongs to
	 * @param elementType Element type
	 * @param initialFlow First step of the IFlow of the element
	 */
	public Element(final Simulation simul, final ElementType elementType, final IInitializerFlow initialFlow) {
		this(simul, "E", elementType, initialFlow, 0, null, new SeizedResourcesCollection(null));
	}
	
	/**
	 * Creates an element with a type and initial IFlow
	 * @param simul Simulation model this element belongs to
	 * @param elementType Element type
	 * @param initialFlow First step of the IFlow of the element
	 * @param size The size of the element
     * @param initLocation The initial location of the element
	 */
	public Element(final Simulation simul, String objectTypeId, final ElementType elementType,
				   final IInitializerFlow initialFlow, final int size, final Location initLocation) {
		this(simul, objectTypeId, elementType, initialFlow, size, initLocation, new SeizedResourcesCollection(null));
	}
	
	/**
	 * Creates an element with dependency injection (DIP constructor)
	 * @param simul Simulation model this element belongs to
	 * @param objectTypeId Object type identifier
	 * @param elementType Element type
	 * @param initialFlow First step of the IFlow of the element
	 * @param size The size of the element
     * @param initLocation The initial location of the element
     * @param resourceManager Resource manager implementation
	 */
	public Element(final Simulation simul, String objectTypeId, final ElementType elementType,
				   final IInitializerFlow initialFlow, final int size, final Location initLocation,
				   final IResourceManager resourceManager) {
		super(simul, simul.generateId(), objectTypeId);
		this.elementType = elementType;
		this.flowManager = new ElementFlow(this, initialFlow);
        this.resourceManager = resourceManager;
        // Fix element reference after construction for default case
        if (resourceManager instanceof SeizedResourcesCollection) {
        	((SeizedResourcesCollection)resourceManager).setElement(this);
        }
        this.movementManager = new ElementMovement(this, size, initLocation);
		initializeElementVars(this.elementType.getElementValues());
	}
	
	/**
	 * Creates an element from the information of a Generator
	 * @param simul Simulation model this element belongs to
	 * @param info Information required to create the element  
	 */
	public Element(final Simulation simul, String objectTypeId, final StandardElementGenerationInfo info) {
		super(simul, simul.generateId(), objectTypeId);
		this.elementType = info.getElementType();
		this.flowManager = new ElementFlow(this, info.getFlow());
        this.resourceManager = new SeizedResourcesCollection(this);
        final int elemSize = info.getSize(this);
        final Location elemInitLocation = info.getInitLocation();
        this.movementManager = new ElementMovement(this, elemSize, elemInitLocation);
		initializeElementVars(this.elementType.getElementValues());
	}
	
	/**
	 * Creates an element from the information of a Generator
	 * @param simul Simulation model this element belongs to
	 * @param info Information required to create the element  
	 */
	public Element(final Simulation simul, final StandardElementGenerationInfo info) {
		this(simul, "E", info);
	}
	
	/**
	 * Returns the corresponding type of the element.
	 * @return the corresponding type of the element
	 */
	public ElementType getType() {
		return elementType;
	}
	
	/**
	 * Returns the associated {@link es.ull.simulation.model.flow.IInitializerFlow IFlow}.
	 * @return the associated {@link es.ull.simulation.model.flow.IInitializerFlow IFlow}
	 */
	public IInitializerFlow getFlow() {
		return flowManager.getInitialFlow();
	}

	/**
	 * Returns the element's priority, which is the element type's priority.
	 * @return Returns the priority.
	 */
	public int getPriority() {
		return elementType.getPriority();
	}

	/**
	 * Notifies an instance of the element is waiting in an activity queue.
	 * @param ei Element instance waiting in queue.
	 */
	public void incInQueue(final ElementInstance ei) {
		engine.incInQueue(ei);
	}
	
	/**
	 * Notifies an instance of the element has finished waiting in an activity queue.
	 * @param ei Element instance that was waiting in a queue.
	 */
	public void decInQueue(final ElementInstance ei) {
		engine.decInQueue(ei);
	}
	
	/**
	 * Returns true if the element is currently performing an exclusive activity; returns false otherwise 
	 * @return true if the element is currently performing an exclusive activity; returns false otherwise
	 */
	public boolean isExclusive() {
		return exclusive;
	}

	/**
	 * Sets the element instance performing the current exclusive activity. If such instance is null, it means that
	 * the element is available to perform other exclusive activity. Hence, creates the events to notify the activities
	 * that this element is now available.
	 * @param exclusive The element instance performing the current exclusive activity;
	 *                     null if the element has finished performing the activity
	 */
	public void setExclusive(final boolean exclusive) {
		this.exclusive = exclusive;
		if (!exclusive) {
			engine.notifyAvailableElement();
		}
	}

	/**
	 * Adds the specified resources to the list of seized resources
	 * @param reqFlow The IFlow that seized the resources
	 * @param ei Element instance performing the seizing
	 * @param newResources New resources seized by the element
	 */
    protected void seizeResources(final RequestResourcesFlow reqFlow,
								  final ElementInstance ei, final ArrayDeque<Resource> newResources) {
    	final int resId = (reqFlow.getResourcesId() < 0) ? -ei.getIdentifier() : reqFlow.getResourcesId();
    	resourceManager.addResources(resId, newResources);
    }
    
	/**
     * Removes the resources specified in the workgroup from the list of seized resources
     * @param relFlow The IFlow that released the resources
     * @param ei Element instance performing the releasing
     * @return The released resources
     */
    protected ArrayDeque<Resource> releaseResources(final ReleaseResourcesFlow relFlow, final ElementInstance ei) {
    	final int resId = (relFlow.getResourcesId() < 0) ? -ei.getIdentifier() : relFlow.getResourcesId();
    	final WorkGroup wg = relFlow.getWorkGroup();
    	return resourceManager.removeResources(resId, wg);
    }
    
    /**
     * Returns the list of resources currently seized by the element
     * @return the list of resources currently seized by the element
     */
	public ArrayDeque<Resource> getCaughtResources() {
		return resourceManager.getAllResources();
	}
	
	/**
	 * Returns the list of resources caught by the specified element instance when performing the specified IFlow
	 * @param reqFlow The IFlow that seized the resources
	 * @param ei Element instance that performed the seizing
	 * @return the list of resources caught by the specified element instance when performing the specified IFlow
	 */
	public ArrayDeque<Resource> getCaughtResources(final RequestResourcesFlow reqFlow, final ElementInstance ei) {
    	return resourceManager.getResourcesByFlow(reqFlow, ei);
    }
    
	/**
	 * Returns the list of resources caught by the specified element instance when performing the specified IFlow
	 * @param resourcesId Identifier of the group of resources
	 * @param wg Resource types
	 * @return the list of resources caught by the specified element instance when performing the specified IFlow
	 */
	public ArrayDeque<Resource> getCaughtResources(final int resourcesId, final WorkGroup wg) {
		return resourceManager.getResourcesByWorkGroup(resourcesId, wg);
	}

	/**
	 * Returns <code>true</code> if the element has currently acquired any resource of type
	 * <code>rt</code>; <code>false</code> otherwise.
	 * @param rt ResourceType been searched
	 * @return <code>true</code> if the element has currently acquired any resource of type <code>rt</code>
	 */
	public boolean isAcquiredResourceType(final ResourceType rt) {
		return resourceManager.hasResourceType(rt);
	}
	
	/**
	 * Initializes the variables of the element as indicated by a generator
	 * @param varList List of variables and values
	 */
	protected void initializeElementVars(final TreeMap<String, Object> varList) {
		for (Entry<String, Object> entry : varList.entrySet()) {
			final String name = entry.getKey();
			final Object value = entry.getValue();
			if (value instanceof Number) {
				this.putVar(name, ((Number)value).doubleValue());
			}
			else {
				if (value instanceof Boolean) {
					this.putVar(name, ((Boolean)value).booleanValue());
				}
				else if (value instanceof EnumVariable) {
						this.putVar(name, ((EnumVariable)value));
				}
				else if (value instanceof Character) {
					this.putVar(name, ((Character)value).charValue());
				}
			}
		}
	}

	/**
	 * Initializes the element by requesting the <code>initialFlow</code>, and placing the element in its
	 * initial location. If there's no initial Flow or the element does not fit into the initial location,
	 * the element finishes immediately.
	 */
	@Override
	public DiscreteEvent onCreate(final long ts) {
		simul.notifyInfo(new ElementInfo(simul, this, elementType, ElementInfo.Type.START, getTs()));
		if (!movementManager.initializeLocation()) {
			return onDestroy(ts);
		}
		if (flowManager.hasInitialFlow()) {
			ElementInstance instance = flowManager.initializeMainInstance();
			return (new RequestFlowEvent(ts, flowManager.getInitialFlow(), instance.getDescendantElementInstance(flowManager.getInitialFlow())));
		}
		else
			return onDestroy(ts);
	}

	@Override
	public DiscreteEvent onDestroy(long ts) {
		simul.notifyInfo(new ElementInfo(simul, this, elementType, ElementInfo.Type.FINISH, getTs()));
		return new DiscreteEvent.DefaultFinalizeEvent(this, ts);
	}

    /**
     * Informs the element that it must finish its execution. Thus, a FinalizeEvent is
     * created.
     */
    public void notifyEnd() {
    	engine.notifyEnd();
    }
    
    /**
     * Creates and adds an event to request a IFlow at the current simulation time
     * @param f IFlow to be requested
     * @param ei Element instance that will perform the IFlow
     */
	public void addRequestEvent(final IFlow f, final ElementInstance ei) {
		simul.scheduleEvent(new RequestFlowEvent(getTs(), f, ei));
	}
	
	/**
     * Creates and adds an event to finish a IFlow at the specified simulation time
	 * @param ts Timestamp when the finalization of the IFlow is scheduled to happen
	 * @param f IFlow to be finished
	 * @param ei Element instance that will finish the IFlow
	 */
	public void addFinishEvent(final long ts, final ITaskFlow f, final ElementInstance ei) {
		simul.scheduleEvent(new FinishFlowEvent(ts, f, ei));
	}

	@Override
	protected void assignSimulation(SimulationEngine simul) {
		engine = simul.getElementEngineInstance(this);
	}

	/**
	 * Returns the engine that helps the simulation processing the element actions
	 * @return the engine
	 */
	public ElementEngine getEngine() {
		return engine;
	}
	

	@Override
	public int getCapacity() {
		return movementManager.getCapacity();
	}

	@Override
	public Location getLocation() {
		return movementManager.getLocation();
	}

	@Override
	public void setLocation(final Location location) {
		movementManager.setLocation(location);
	}

	@Override
	public void notifyLocationAvailable(final Location location) {
		movementManager.notifyLocationAvailable(location);
	}
	
	/**
	 * Makes the element issue a {@link MoveEvent move event} to continue the movement to its destination
	 * @param IFlow The IFlow indicating the destination
	 * @param ei Element instance moving
	 */
	public void keepMoving(final MoveFlow IFlow, final ElementInstance ei) {
		movementManager.keepMoving(IFlow, ei);
	}
	
	/**
	 * An event to request a IFlow.
	 * @author Iván Castilla Rodríguez
	 */
	protected class RequestFlowEvent extends DiscreteEvent {
		/** The element instance that executes the request */
		private final ElementInstance ei;
		/** The IFlow to be requested */
		private final IFlow f;

		public RequestFlowEvent(final long ts, final IFlow f, final ElementInstance ei) {
			super(ts);
			this.ei = ei;
			this.f = f;
		}		

		@Override
		public void event() {
			ei.setCurrentFlow(f);
			f.request(ei);
		}
	}
	
	/**
	 * An event to finish a IFlow. 
	 * @author Iván Castilla Rodríguez
	 */
	protected class FinishFlowEvent extends DiscreteEvent {
		/** The element instance that executes the finish */
		private final ElementInstance ei;
		/** The IFlow to be finished */
		private final ITaskFlow f;

		public FinishFlowEvent(final long ts, final ITaskFlow f, final ElementInstance ei) {
			super(ts);
			this.ei = ei;
			this.f = f;
		}		

		@Override
		public void event() {
			f.finish(ei);
		}
	}

    /**
     * An event to perform a move. The element try to move to the next location in its path to its final destination.
	 * The element only leaves its current location if there is enough free space for the element in the new location;
	 * otherwise, it waits.
     * @author Iván Castilla Rodríguez
     *
     */
	protected class MoveEvent extends DiscreteEvent {
		/** The element instance that executes the event */
		private final ElementInstance ei;
		/** The instance that computes the path to the final destination */
		private final MoveFlow IFlow;

		/**
		 * Constructs a MoveEvent object representing an intermediate step in a resource's journey.
		 * This event signifies the movement of a resource to an intermediate location on its way to a final destination.
		 *
		 * @param ts              The timestamp indicating when the resource will arrive at the intermediate location.
		 * @param IFlow           The MoveFlow instance responsible for determining the path for the resource.
		 * @param ei              The ElementInstance representing the resource.
		 */
		public MoveEvent(final long ts, final MoveFlow IFlow, final ElementInstance ei) {
			super(ts);
			this.IFlow = IFlow;
			this.ei = ei;
		}

		@Override
		public void event() {
			IFlow.move(ei);
		}
	}
}
