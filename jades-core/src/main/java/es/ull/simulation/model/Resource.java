/**
 * 
 */
package es.ull.simulation.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;

import es.ull.simulation.info.EntityLocationInfo;
import es.ull.simulation.info.ResourceInfo;
import es.ull.simulation.model.engine.ResourceEngine;
import es.ull.simulation.model.engine.SimulationEngine;
import es.ull.simulation.model.location.Location;
import es.ull.simulation.model.location.IMovable;
import es.ull.simulation.model.location.MoveResourcesFlow;
import es.ull.simulation.model.location.IRouter;
import es.ull.simulation.model.location.TransportFlow;
import es.ull.simulation.utils.cycle.DiscreteCycleIterator;

/**
 * A simulation resource whose availability is controlled by means of {@link TimeTableEntry timetable entries}.
 * Timetable entries can overlap in time, thus allowing the resource for being potentially available for
 * different resource types simultaneously.
 * A resource finishes its execution when it has no longer valid timetable entries.
 * @author Iván Castilla Rodríguez
 *
 */
public class Resource extends VariableStoreSimulationObject implements IDescribable, IEventSource, IMovable {
    /** A brief description of the resource */
    protected final String description;
	/** Manages location and movement for this resource */
	private final ResourceLocation resourceLocation;
	/** The current location of the resource - DEPRECATED: use resourceLocation */
	@Deprecated
	private Location currentLocation;
	/** The initial location of the resource - DEPRECATED: use resourceLocation */
	@Deprecated
	private Location initLocation;
	/** The size of the resource - DEPRECATED: use resourceLocation */
	@Deprecated
	private final int size;
	/** The current element instance that drives the movement of the resource - DEPRECATED: use resourceLocation */
	@Deprecated
	private ElementInstance movingInstance = null;
	/** Manages availability, timetables, and resource types for this resource */
	private final ResourceAvailability resourceAvailability;
	/** Timetable which defines the availability estructure of the resource - DEPRECATED: use resourceAvailability */
	@Deprecated
    protected final ArrayList<TimeTableEntry> timeTable;
    /** Availability time table - DEPRECATED: use resourceAvailability */
	@Deprecated
    protected final ArrayList<TimeTableEntry> cancelPeriodTable;
    /** If true, indicates that this resource is being used after its availability time has expired - DEPRECATED: use resourceAvailability */
	@Deprecated
    private boolean timeOut = false;
    /** The resource type which this resource is being booked for - DEPRECATED: use resourceAvailability */
	@Deprecated
    protected ResourceType currentResourceType = null;
    /** The engine in charge of executing specific actions */
    private ResourceEngine engine;

    /**
     * Creates a resource with size 0
     * @param model The simulation model this resource belongs to 
     * @param description A brief description of the resource
     */
	public Resource(final Simulation model, final String description) {
		this(model, description, 0, null);
	}

    /**
     * Creates a resource
     * @param model The simulation model this resource belongs to 
     * @param description A brief description of the resource
     * @param size The size of the resource
     * @param initLocation The initial location of the resource
     */
	public Resource(final Simulation model, final String description, final int size, final Location initLocation) {
		super(model, model.getResourceList().size(), "RES");
		this.description = description;
		// Initialize new ResourceLocation
		this.resourceLocation = new ResourceLocation(this, initLocation, size);
		// Initialize new ResourceAvailability
		this.resourceAvailability = new ResourceAvailability(this);
		// Keep deprecated fields for backward compatibility
		this.size = size;
		this.initLocation = initLocation;
		this.timeTable = (ArrayList<TimeTableEntry>) resourceAvailability.getTimeTableEntries();
		this.cancelPeriodTable = (ArrayList<TimeTableEntry>) resourceAvailability.getCancellationPeriodEntries();
		model.add(this);
	}

	@Override
	protected void assignSimulation(SimulationEngine simul) {
		engine = simul.getResourceEngineInstance(this);
	}      
	
	/**
	 * Returns the associated {@link IResourceEngine}
	 * @return the associated {@link IResourceEngine}
	 */
	public ResourceEngine getEngine() {
		return engine;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public int getCapacity() {
		return resourceLocation.getCapacity();
	}

	@Override
	public Location getLocation() {
		// Delegate to ResourceLocation and sync deprecated field
		currentLocation = resourceLocation.getLocation();
		return currentLocation;
	}

	@Override
	public void setLocation(final Location location) {
		// Delegate to ResourceLocation
		resourceLocation.setLocation(location);
		// Sync deprecated field for backward compatibility
		currentLocation = location;
	}

	/**
	 * Returns a collection with the timetable defined for this resource.
	 * @return a collection with the timetable defined for this resource
	 */
	public Collection<TimeTableEntry> getTimeTableEntries() {
		return resourceAvailability.getTimeTableEntries();
	}

	/**
	 * Returns a collection with the cancellation timetable defined for this resource.
	 * @return a collection with the cancellation timetable defined for this resource
	 */
	public Collection<TimeTableEntry> getCancellationPeriodEntries() {
		return resourceAvailability.getCancellationPeriodEntries();
	}

	@Override
	public DiscreteEvent onCreate(final long ts) {
		// Delegate initialization to ResourceLocation
		if (!resourceLocation.initialize()) {
			return onDestroy(ts);
		}
		// Sync deprecated field for backward compatibility
		currentLocation = resourceLocation.getLocation();
		return new CreateResourceEvent(ts);
	}

	@Override
	public DiscreteEvent onDestroy(final long ts) {
		return new DiscreteEvent.DefaultFinalizeEvent(this, ts);
	}
    
    /**
     * Informs the resource that it must finish its execution. 
     */
    public void notifyEnd() {
        engine.notifyEnd();
    }
    
    /**
     * Returns the {@link ResourceType resource type} currently assigned to this resource, in case it is in use.
	 * Returns null otherwise.
     * @return Value of property currentResourceType.
     */
    public ResourceType getCurrentResourceType() {
        ResourceType rt = resourceAvailability.getCurrentResourceType();
        currentResourceType = rt; // Sync deprecated field
        return rt;
    }

    /**
     * Assigns a {@link ResourceType resource type} for this resource,
	 * whenever it is going to be used by an {@link Element}.
     * @param rt Value of property currentResourceType.
     */
    public void setCurrentResourceType(final ResourceType rt) {
    	resourceAvailability.setCurrentResourceType(rt);
    	currentResourceType = rt; // Sync deprecated field
    }
    
    /**
     * Returns true if the resource is currently seized by an {@link Element element}; false otherwise 
     * @return true if the resource is currently seized by an {@link Element element}; false otherwise
     */
    public boolean isSeized() {
    	return (engine.getCurrentElement() != null);
    }
    
    /**
     * Returns <code>true</code> if this resource is being used in spite of having finished its availability.
     * @return <code>True</code> if this resource is being used in spite of having finished its availability;
     * <code>false</code> otherwise.
     */
    public boolean isTimeOut() {
        boolean result = resourceAvailability.isTimeOut();
        this.timeOut = result; // Sync deprecated field
        return result;
    }
    
    /**
     * Sets the state of this resource as being used in spite of having finished its availability. 
     * @param timeOut <code>True</code> if this resource is being used beyond its availability; 
     * <code>false</code> otherwise.
     */
    public void setTimeOut(final boolean timeOut) {
        resourceAvailability.setTimeOut(timeOut);
        this.timeOut = timeOut; // Sync deprecated field
    }
    
	/**
	 * Checks if a resource is available for a specific {@link ResourceType resource type}.
	 * The resource type is used to prevent
	 * using a resource when it's becoming unavailable right at this timestamp. 
	 * @param rt Resource type
	 * @return True if the resource is available.
	 */
	public boolean isAvailable(final ResourceType rt) {
		return engine.isAvailable(rt);
	}

	/**
	 * Sets the available flag of a resource.
	 * @param available The availability state of the resource.
	 */
	protected void setNotCanceled(final boolean available) {
		engine.setNotCanceled(available);
	}
	
	/**
	 * Adds a resource to the set of resources requested by an {@link Element}. 
	 * @param solution Tentative solution with booked resources
	 * @param rt Resource type
	 * @param ei Element instance corresponding to an Element
	 * @return <code>true</code> if the resource can be used within the proposed solution; <code>false</code> otherwise.
	 */
	public boolean add2Solution(final ArrayDeque<Resource> solution, final ResourceType rt, final ElementInstance ei) {
		return engine.add2Solution(solution, rt, ei);
	}

	/**
	 * Removes a resource from the set of resourcs requested by an {@link Element}
	 * @param solution Tentative solution with booked resources
	 * @param ei Element instance corresponding to an Element
	 */
	public void removeFromSolution(final ArrayDeque<Resource> solution, final ElementInstance ei) {
		engine.removeFromSolution(solution, ei);
	}

	/**
	 * Marks this resource as taken by an {@link Element}
	 * @param ei The element instance in charge of executing the current IFlow
	 * @return The availability timestamp of this resource for this resource type 
	 */
	protected long catchResource(final ElementInstance ei) {
		return engine.catchResource(ei);
	}
	
    /**
     * Releases this resource
	 * @param ei The element instance in charge of executing the current IFlow
     * @return True if the resource could be correctly released. False if the availability time of the resource
	 * had already expired.
     */
    protected boolean releaseResource(final ElementInstance ei) {
    	return engine.releaseResource(ei);
    }
    
	/**
	 * Builds a list of activity managers referenced by the roles of the resource. 
	 * @return Returns the list of activity managers referenced by the roles of the resource.
	 */
    public ArrayList<ActivityManager> getCurrentManagers() {
    	return engine.getCurrentManagers();
    }
    
    /**
     * Generates an event which finalizes a period of unavailability.
     * @param ts Current simulation time.
     * @param duration Duration of the unavailability period.
     */
    public void generateCancelPeriodOffEvent(final long ts, final long duration) {
    	final CancelPeriodOffEvent aEvent = new CancelPeriodOffEvent(ts + duration, null, 0);
        simul.addEvent(aEvent);
    }

	/**
	 * Initiates the movement of the specified element instance.
	 * This method begins the process of moving an element instance to its destination. It determines the next location
	 * on the route using the provided router, creates a MoveEvent accordingly, and schedules it in the simulation.
	 *
	 * @param ei The ElementInstance to be moved.
	 */
    public void startMove(final ElementInstance ei) {
    	final MoveResourcesFlow flow = (MoveResourcesFlow)ei.getCurrentFlow();
    	final Location destination = flow.getDestination();
    	final IRouter router = flow.getRouter();
		trace("Start route\t" + this + "\t" + destination);
    	// Delegate to ResourceLocation
    	resourceLocation.setMovingInstance(ei);
    	movingInstance = ei; // Sync deprecated field
    	// No need to move
    	if (resourceLocation.getLocation().equals(destination)) {
    		endMove(flow, true);
    	}
    	else {
			final Location nextLoc = router.getNextLocationTo(this, destination);
			if (IRouter.isUnreachableLocation(nextLoc)) {
	    		endMove(flow, false);
			}
			else {
		    	simul.addEvent(new MoveEvent(getTs() + resourceLocation.getLocation().getDelayAtExit(this), nextLoc, destination, router));
			}
    	}
    }

	/**
	 * Initiates a transport event to move to a destination using the specified router.
	 *
	 * This method begins the process of transporting an element instance to its destination. It determines the next location
	 * on the route using the provided router, creates a TransportEvent accordingly, and schedules it in the simulation.
	 *
	 * @param ei           The ElementInstance initiating the move.
	 */
    public void startTransport(final ElementInstance ei) {
    	final TransportFlow flow = (TransportFlow)ei.getCurrentFlow();
    	final Location destination = flow.getDestination();
    	final IRouter router = flow.getRouter();
		trace("Start transport\t" + this + "\t" + destination);
    	// Delegate to ResourceLocation
    	resourceLocation.setMovingInstance(ei);
    	movingInstance = ei; // Sync deprecated field
    	// No need to move
    	if (resourceLocation.getLocation().equals(destination)) {
    		endTransport(flow, true);
    	}
    	else {
			final Location nextLoc = router.getNextLocationTo(this, destination);
			if (IRouter.isUnreachableLocation(nextLoc)) {
	    		endTransport(flow, false);
			}
			else {
		    	simul.addEvent(new TransportEvent(getTs() + resourceLocation.getLocation().getDelayAtExit(this),
						nextLoc, destination, router));
			}
    	}
    }

	/**
	 * Notifies the MoveResourcesFlow that the movement has finished.
	 * This method notifies the specified MoveResourcesFlow about the completion of the movement, indicating whether
	 * the resource successfully arrived at its destination or if the destination was unreachable.
	 *
	 * @param flow     The MoveResourcesFlow driving the movement.
	 * @param success  A boolean value indicating whether the resource arrived at its destination (true) or if the
	 *                 destination was unreachable (false).
	 */
    private void endMove(final MoveResourcesFlow flow, final boolean success) {
		flow.notifyArrival(resourceLocation.getMovingInstance(), success);
    	// Clear moving instance in both locations
    	resourceLocation.setMovingInstance(null);
    	movingInstance = null; // Sync deprecated field
    	if (success)
			trace("Finishes route\t" + this + "\t" + flow.getDestination());
    	else
			error("Destination unreachable. Current: " + resourceLocation.getLocation() + "; destination: " +
					flow.getDestination());
    }

	/**
	 * Notifies the TransportFlow that the transport has finished.
	 * This method notifies the specified TransportFlow about the completion of the transport, indicating whether
	 * the resource successfully arrived at its destination or if the destination was unreachable.
	 *
	 * @param flow     The TransportFlow driving the transport.
	 * @param success  A boolean value indicating whether the resource arrived at its destination (true) or if the destination was unreachable (false).
	 */
    private void endTransport(final TransportFlow flow, final boolean success) {
    	if (success) {
			flow.finish(resourceLocation.getMovingInstance());
    		resourceLocation.setMovingInstance(null);
    		movingInstance = null; // Sync deprecated field
    		trace("Finishes transport\t" + this + "\t" + flow.getDestination());
    	}
    	else {
    		final ElementInstance ei = resourceLocation.getMovingInstance();
			ei.cancel(flow);
			flow.next(ei);
	    	resourceLocation.setMovingInstance(null);
	    	movingInstance = null; // Sync deprecated field
    		error("Destination unreachable. Current: " + resourceLocation.getLocation() + "; destination: " +
					flow.getDestination());
    	}
    }

	@Override
	public void notifyLocationAvailable(final Location location) {
		// Delegate to ResourceLocation
		resourceLocation.notifyLocationAvailable(location);
		// Sync deprecated field
		currentLocation = resourceLocation.getLocation();
	}
	
	/**
	 * Handles the logic when a location becomes available.
	 * This method is called from ResourceLocation.notifyLocationAvailable().
	 * Package-private to allow ResourceLocation to call it.
	 * 
	 * @param location The location that became available
	 * @param movingInstance The element instance that is moving
	 */
	void handleLocationAvailable(final Location location, final ElementInstance movingInstance) {
		if (movingInstance.getCurrentFlow() instanceof MoveResourcesFlow) {
	    	final MoveResourcesFlow flow = (MoveResourcesFlow)movingInstance.getCurrentFlow();
	    	final Location destination = flow.getDestination();
	    	final IRouter router = flow.getRouter();
			
			if (resourceLocation.getLocation().equals(destination)) {
				endMove(flow, true);
			}
			else {
				final Location nextLoc = router.getNextLocationTo(this, destination);
				if (IRouter.isUnreachableLocation(nextLoc)) {
					endMove(flow, false);
				}
				else {
			    	simul.addEvent(new MoveEvent(getTs() + resourceLocation.getLocation().getDelayAtExit(this),
							nextLoc, destination, router));
				}
			}			
		}
		else if (movingInstance.getCurrentFlow() instanceof TransportFlow) {
	    	final TransportFlow flow = (TransportFlow)movingInstance.getCurrentFlow();
			// Move the element without checking anything else
			movingInstance.getElement().setLocation(location);
	    	final Location destination = flow.getDestination();
	    	final IRouter router = flow.getRouter();
			
			if (resourceLocation.getLocation().equals(destination)) {
				endTransport(flow, true);
			}
			else {
				final Location nextLoc = router.getNextLocationTo(this, destination);
				if (IRouter.isUnreachableLocation(nextLoc)) {
					endTransport(flow, false);
				}
				else {
			    	simul.addEvent(new TransportEvent(getTs() + resourceLocation.getLocation().getDelayAtExit(this),
							nextLoc, destination, router));
				}
			}			
		}
	}
	
	/**
	 * Returns a builder class for adding time table or cancellation entries
	 * @param roleList The types of this resource during every activation /to be cancelled
	 * @return a builder class for adding time table or cancellation entries
	 */
	public ResourceAvailability.TimeTableOrCancelEntriesAdder newTimeTableOrCancelEntriesAdder(final ArrayList<ResourceType> roleList) {
		return resourceAvailability.newTimeTableOrCancelEntriesAdder(roleList);
	}
	
	/**
	 * Returns a builder class for adding time table or cancellation entries
	 * @param role The type of this resource during every activation/to be cancelled 
	 * @return a builder class for adding time table or cancellation entries
	 */
	public ResourceAvailability.TimeTableOrCancelEntriesAdder newTimeTableOrCancelEntriesAdder(final ResourceType role) {
		return resourceAvailability.newTimeTableOrCancelEntriesAdder(role);
	}
	    
    /**
     * The event in charge of initializing the resource
     * @author Iván Castilla Rodríguez
     *
     */
    protected class CreateResourceEvent extends DiscreteEvent {

    	public CreateResourceEvent(long ts) {
    		super(ts);
		}
    	
		@Override
		public void event() {
			simul.notifyInfo(new ResourceInfo(simul, Resource.this, null, ResourceInfo.Type.START, getTs()));
			for (int i = 0 ; i < timeTable.size(); i++) {
				TimeTableEntry tte = timeTable.get(i);
				if (tte.isPermanent()) {
		            final RoleOnEvent rEvent = new RoleOnEvent(getTs(), tte.getRole(), null,
							Long.MAX_VALUE - getTs());
		            simul.addEvent(rEvent);
		            engine.incValidTimeTableEntries();
				}
				else {
					/* FIXME: Check whether it works when using a condition to end simulation:
					should I use simul.getEndTs() instead of Long.MAX_VALUE?*/
			        DiscreteCycleIterator iter = tte.getCycle().getCycle().iterator(getTs(), Long.MAX_VALUE);
			        final long nextTs = iter.next();
			        if (nextTs != -1) {
			            RoleOnEvent rEvent = new RoleOnEvent(nextTs, tte.getRole(), iter,
								simul.simulationTime2Long(tte.getDuration()));
			            simul.addEvent(rEvent);
			            engine.incValidTimeTableEntries();
			        }
				}
			}
			for (int i = 0 ; i < cancelPeriodTable.size(); i++) {
				final TimeTableEntry tte = cancelPeriodTable.get(i);
				/* FIXME: Check whether it works when using a condition to end simulation:
				 should I use simul.getEndTs() instead of Long.MAX_VALUE?*/
		        final DiscreteCycleIterator iter = tte.getCycle().getCycle().iterator(getTs(), Long.MAX_VALUE);
		        long nextTs = iter.next();
		        if (nextTs != -1) {
		            final CancelPeriodOnEvent aEvent = new CancelPeriodOnEvent(nextTs, iter,
							simul.simulationTime2Long(tte.getDuration()));
		            simul.addEvent(aEvent);
		            engine.incValidTimeTableEntries();
		        }
			}
			if (engine.getValidTimeTableEntries() == 0)// at least one tte should be valid
				notifyEnd();
		}
    	
    }

    /**
     * Makes available a resource with a specific role. 
     */
    protected class RoleOnEvent extends DiscreteEvent {
        /** Available role */
        private final ResourceType role;
        /** Cycle iterator */
        private final DiscreteCycleIterator iter;
        /** Availability duration */
        private final long duration;
        
        /**
         * Creates a new event
         * @param ts Timestamp when the resource will  be available.
         * @param role Role played by the resource.
         * @param iter The cycle iterator that handles the availability of this resource
         * @param duration The duration of the availability.
         */        
        public RoleOnEvent(final long ts, final ResourceType role, final DiscreteCycleIterator iter,
						   final long duration) {
            super(ts);
            this.iter = iter;
            this.role = role;
            this.duration = duration;
        }
        
        @Override
        public void event() {
        	final long waitTime = role.beforeRoleOn();
        	if (waitTime == 0) {
        		simul.notifyInfo(new ResourceInfo(simul, Resource.this, role, ResourceInfo.Type.ROLON, ts));
        		trace("Resource available\t" + role);
        		role.incAvailable(Resource.this);
        		engine.addRole(role, ts + duration);
        		role.afterRoleOn();
        		RoleOffEvent rEvent = new RoleOffEvent(ts + duration, role, iter, duration);
        		simul.addEvent(rEvent);
        	} else {
        		RoleOnEvent rEvent = new RoleOnEvent(ts + waitTime, role, iter, duration);
        		simul.addEvent(rEvent);
        	}
        }

		/**
		 * Returns the resource type
		 * @return Returns the role.
		 */
		public ResourceType getRole() {
			return role;
		}
    }
    
    /**
     * Makes unavailable a resource with a specific role. 
     */
    protected class RoleOffEvent extends DiscreteEvent {
        /** Unavailable role */
        private final ResourceType role;
        /** Cycle iterator */
        private final DiscreteCycleIterator iter;
        /** Availability duration */
        private final long duration;
        
        /**
         * Creates a new event
         * @param ts Timestamp when the resource will be unavailable.
         * @param role Role played by the resource.
         * @param iter The cycle iterator that handles the availability of this resource
         * @param duration The duration of the availability.
         */        
        public RoleOffEvent(final long ts, final ResourceType role, final DiscreteCycleIterator iter,
							final long duration) {
            super(ts);
            this.role = role;
            this.iter = iter;
            this.duration = duration;
        }
        
        @Override
        public void event() {
        	final long waitTime = role.beforeRoleOff();
        	if (waitTime == 0) {
        		simul.notifyInfo(new ResourceInfo(simul, Resource.this, role, ResourceInfo.Type.ROLOFF, ts));
        		role.decAvailable(Resource.this);
        		engine.removeRole(role);
        		trace("Resource unavailable\t" + role);
        		final long nextTs = (iter == null) ? -1 : iter.next();
        		if (nextTs != -1) {
        			RoleOnEvent rEvent = new RoleOnEvent(nextTs, role, iter, duration);
        			simul.addEvent(rEvent);            	
        		}
        		else if (engine.decValidTimeTableEntries() == 0) {
        			role.afterRoleOff();
        			notifyEnd();
        		}
        	} else {
        		RoleOffEvent rEvent = new RoleOffEvent(ts + waitTime, role, iter, duration);
        		simul.addEvent(rEvent);
        	}
        }

		/**
		 * Returns the resource type
		 * @return Returns the role.
		 */
		public ResourceType getRole() {
			return role;
		}        
    }
    
	/**
	 * Event which starts a cancellation period for this resource
	 * @author ycallero
	 *
	 */
	protected class CancelPeriodOnEvent extends DiscreteEvent {
		/** Cycle iterator */
		private final DiscreteCycleIterator iter;
		/** Duration of the availability */
		private final long duration;

		/**
		 * Creates a new CancelPeriodOnEvent.
		 * @param ts Actual simulation time.
		 * @param iter Cycle iterator.
		 * @param duration Event duration.
		 */
		public CancelPeriodOnEvent(final long ts, final DiscreteCycleIterator iter, final long duration) {
			super(ts);
			this.iter = iter;
			this.duration = duration;
		}

		@Override
		public void event() {
			simul.notifyInfo(new ResourceInfo(simul, Resource.this, getCurrentResourceType(),
					ResourceInfo.Type.CANCELON, ts));
			engine.setNotCanceled(false);
			CancelPeriodOffEvent aEvent = new CancelPeriodOffEvent(ts + duration, iter, duration);
			simul.addEvent(aEvent);
		}

	}

	/**
	 * Event which ends a cancellation period for this resource
	 * @author ycallero
	 *
	 */
	protected class CancelPeriodOffEvent extends DiscreteEvent {
		/** Cycle iterator */
		private final DiscreteCycleIterator iter;
		/** Duration of the availability */
		private final long duration;

		/**
		 * Creates a new CancelPeriodOffEvent.
		 * @param ts Actual simulation time.
		 * @param iter Cycle iterator.
		 * @param duration The event duration.
		 */   
		public CancelPeriodOffEvent(final long ts, final DiscreteCycleIterator iter, final long duration) {
			super(ts);
			this.iter = iter;
			this.duration = duration;
		}

		@Override
		public void event() {
			simul.notifyInfo(new ResourceInfo(simul, Resource.this, getCurrentResourceType(),
					ResourceInfo.Type.CANCELOFF, ts));
			engine.setNotCanceled(true);
			engine.notifyCurrentManagers();
			long nextTs = -1;
			if (iter != null)
				nextTs = iter.next();
			if (nextTs != -1) {
				CancelPeriodOnEvent aEvent = new CancelPeriodOnEvent(nextTs, iter, duration);
				simul.addEvent(aEvent);            	
			}
		}
	}

	/**
	 * Event to move the resource to a different location
	 * @author Iván Castilla Rodríguez
	 *
	 */
	protected class MoveEvent extends DiscreteEvent {
		/** Final destination of the move */
		final private Location destination;
		/** Next location in the way to the final destination */
		final private Location nextLocation;
		/** The instance that computes the path to the final destination */
		final private IRouter router;
		
		/**
		 * Creates a move event that starts a move from the resource's current location
		 * @param ts Current timestamp
		 * @param destination Destination location
		 * @param IRouter Instance that returns the path for the resource
		 */
		public MoveEvent(final long ts, final Location destination, final IRouter IRouter) {
			this(ts, resourceLocation.getLocation(), destination, IRouter);
		}

		/**
		 * Constructs a MoveEvent representing an intermediate step towards a destination.
		 * This constructor creates a MoveEvent object that signifies an intermediate step in the journey towards
		 * a final destination. It specifies the timestamp when the resource will arrive at the intermediate location,
		 * the intermediate location itself, the final destination, and the router responsible for determining the
		 * path for the resource.
		 *
		 * @param ts            The timestamp when the resource will arrive at the intermediate location.
		 * @param nextLocation  The intermediate location.
		 * @param destination   The final destination.
		 * @param router        The IRouter instance responsible for determining the path for the resource.
		 */
		public MoveEvent(final long ts, final Location nextLocation, final Location destination, final IRouter router) {
			super(ts);
			this.destination = destination;
			this.nextLocation = nextLocation;
			this.router = router;
		}

		@Override
		public void event() {
			if (nextLocation.fitsIn(Resource.this)) {
				final MoveResourcesFlow flow = ((MoveResourcesFlow)resourceLocation.getMovingInstance().getCurrentFlow());
				nextLocation.enter(Resource.this);
				if (nextLocation.equals(destination)) {
					endMove(flow, true);
				}
				else {
					final Location nextLoc = router.getNextLocationTo(Resource.this, destination);
					if (IRouter.isUnreachableLocation(nextLoc)) {
						endMove(flow, false);
					}
					else {
						final MoveEvent mEvent = new MoveEvent(getTs() +
								resourceLocation.getLocation().getDelayAtExit(Resource.this), nextLoc, destination, router);
				    	simul.addEvent(mEvent);						
					}
				}
			}
			else {
				nextLocation.waitFor(Resource.this);
				simul.notifyInfo(new EntityLocationInfo(simul, Resource.this, nextLocation,
						EntityLocationInfo.Type.WAIT_FOR, getTs()));
			}
		}
	}

	/**
	 * Event to move the resource to a different location
	 * @author Iván Castilla Rodríguez
	 *
	 */
	protected class TransportEvent extends DiscreteEvent {
		/** Final destination of the move */
		final private Location destination;
		/** Next location in the way to the final destination */
		final private Location nextLocation;
		/** The instance that computes the path to the final destination */
		final private IRouter router;
		
		/**
		 * Creates a transport event that starts a move from the resource's current location
		 * @param ts Current timestamp
		 * @param destination Destination location
		 * @param IRouter Instance that returns the path for the resource
		 */
		public TransportEvent(final long ts, final Location destination, final IRouter IRouter) {
			this(ts, resourceLocation.getLocation(), destination, IRouter);
		}

		/**
		 * Constructs a TransportEvent representing an intermediate step towards a destination.
		 *
		 * This constructor creates a TransportEvent object that signifies an intermediate step in the journey towards
		 * a final destination. It specifies the timestamp when the resource will arrive at the intermediate location,
		 * the intermediate location itself, the final destination, and the router responsible for determining
		 * the path for the resource.
		 *
		 * @param ts            The timestamp when the resource will arrive at the intermediate location.
		 * @param nextLocation  The intermediate location.
		 * @param destination   The final destination.
		 * @param router        The IRouter instance responsible for determining the path for the resource.
		 */
		public TransportEvent(final long ts, final Location nextLocation, final Location destination,
							  final IRouter router) {
			super(ts);
			this.destination = destination;
			this.nextLocation = nextLocation;
			this.router = router;
		}

		@Override
		public void event() {
			if (nextLocation.fitsIn(Resource.this)) {
				final TransportFlow flow = ((TransportFlow)resourceLocation.getMovingInstance().getCurrentFlow());
				nextLocation.enter(Resource.this);
				// Move the element without checking anything else
				resourceLocation.getMovingInstance().getElement().setLocation(nextLocation);
				if (nextLocation.equals(destination)) {
					endTransport(flow, true);
				}
				else {
					final Location nextLoc = router.getNextLocationTo(Resource.this, destination);
					if (IRouter.isUnreachableLocation(nextLoc)) {
						endTransport(flow, false);
					}
					else {
						final TransportEvent mEvent = new TransportEvent(getTs() +
								resourceLocation.getLocation().getDelayAtExit(Resource.this), nextLoc, destination, router);
				    	simul.addEvent(mEvent);						
					}
				}
			}
			else {
				nextLocation.waitFor(Resource.this);
				simul.notifyInfo(new EntityLocationInfo(simul, Resource.this,
						nextLocation, EntityLocationInfo.Type.WAIT_FOR, getTs()));
			}
		}
	}
}
