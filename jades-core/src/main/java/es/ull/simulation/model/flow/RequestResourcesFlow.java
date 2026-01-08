/**
 * 
 */
package es.ull.simulation.model.flow;

import java.util.ArrayDeque;
import java.util.Iterator;

import es.ull.simulation.functions.AbstractTimeFunction;
import es.ull.simulation.functions.TimeFunctionFactory;
import es.ull.simulation.condition.AbstractCondition;
import es.ull.simulation.condition.TrueCondition;
import es.ull.simulation.info.ElementActionInfo;
import es.ull.simulation.model.ActivityManager;
import es.ull.simulation.model.Element;
import es.ull.simulation.model.ElementInstance;
import es.ull.simulation.model.Resource;
import es.ull.simulation.model.Simulation;
import es.ull.simulation.model.WorkGroup;
import es.ull.simulation.model.engine.RequestResourcesEngine;
import es.ull.simulation.model.engine.SimulationEngine;
import es.ull.simulation.utils.Prioritizable;
import es.ull.simulation.utils.PrioritizedTable;

/**
 * A IFlow to request a set of resources, defined as {@link WorkGroup workgroups}. If all the resources from a workgroup are available, the element seizes 
 * them until a {@link ReleaseResourcesFlow} is used. The IFlow can use an identifier, so all the resources grouped under such identifier can be
 * released together later on. By default, all resources are grouped with a 0 identifier.<p> 
 * 
 * After seizing the resources, the element can suffer a delay.<p>
 * 
 * Each request IFlow is associated to an {@link ActivityManager}, which handles the way the resources are accessed.<p>
 * The IFlow is potentially feasible if there is no proof that none of the workgroups are available. The IFlow is feasible if it's potentially feasible 
 * and there is at least one workgroup with enough available resources.<p>
 * An element requesting a request IFlow which is not feasible is added to a queue until new resources are available.
 * @author Iván Castilla Rodríguez
 *
 */
public class RequestResourcesFlow extends AbstractSingleSuccessorFlow implements ITaskFlow, IResourceHandlerFlow, Prioritizable {
    /** Priority. The lowest the value, the highest the priority */
    private final int priority;
    /** A brief description of the activity */
    private final String description;
    /** Work Groups available to perform this basic step */
    private final PrioritizedTable<ActivityWorkGroup> workGroupTable;
    /** A unique identifier that serves to tell a ReleaseResourcesFlow which resources to release */
	private final int resourcesId;
    /** Activity manager this request IFlow belongs to. */
    protected ActivityManager manager;
    /** Indicates that the basic step is potentially feasible. */
    protected boolean stillFeasible = true;
    /** Indicates whether the IFlow is the first step of an exclusive activity */
    private boolean inExclusiveActivity = false; 
    /** An engine to perform the simulation tasks associated to this IFlow */
    private RequestResourcesEngine engine;

	/**
	 * Creates a IFlow to seize a group of resources with the highest priority, and default identifier 
	 * @param model The simulation model this IFlow belongs to
	 * @param description A brief description of the IFlow
	 */
	public RequestResourcesFlow(final Simulation model, final String description) {

		this(model, description, 0, 0);
	}

	/**
	 * Creates a IFlow to seize a group of resources, with the specified priority, default identifier 
	 * @param model The simulation model this IFlow belongs to
	 * @param description A brief description of the IFlow
	 * @param priority Priority. The lowest the value, the highest the priority
	 */
	public RequestResourcesFlow(final Simulation model, final String description, final int priority) {
		this(model, description, 0, priority);
	}

	/**
	 * Creates a IFlow to seize a group of resources non-exclusively, with the specified priority and identifier 
	 * @param model The simulation model this IFlow belongs to
	 * @param description A brief description of the IFlow
	 * @param resourcesId Identifier of the group of resources
	 * @param priority Priority. The lowest the value, the highest the priority
	 */
	public RequestResourcesFlow(final Simulation model, final String description,
								final int resourcesId, final int priority) {
		super(model);
        this.description = description;
        this.priority = priority;
		this.resourcesId = resourcesId;
        workGroupTable = new PrioritizedTable<ActivityWorkGroup>();
	}

	@Override
	public void setParent(final AbstractStructuredFlow parent) {
		super.setParent(parent);
		if (parent instanceof ActivityFlow)
			inExclusiveActivity = ((ActivityFlow)parent).isExclusive();
	}
	
	/**
	 * Returns true if the IFlow is descendant of an exclusive activity; false otherwise 
	 * @return True if the IFlow is descendant of an exclusive activity; false otherwise
	 */
	public boolean isInExclusiveActivity() {
		return inExclusiveActivity;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
    public int getPriority() {
        return priority;
    }
	
    /**
     * Returns the activity manager this IFlow belongs to.
     * @return Value of property manager.
     */
    public ActivityManager getManager() {
        return manager;
    }
    
    /**
     * Sets the activity manager this IFlow belongs to. It also
     * adds this IFlow to the manager.
     * @param manager New value of manager.
     */
    public void setManager(final ActivityManager manager) {
        this.manager = manager;
        manager.add(this);
    }

	/**
	 * Returns an identifier for the group of resources seized within this IFlow 
	 * @return an identifier for the group of resources seized within this IFlow
	 */
	public int getResourcesId() {
		return resourcesId;
	}
	
    /**
     * Searches and returns a workgroup with the specified id.
     * @param wgId The id of the workgroup searched
     * @return A workgroup contained in this IFlow with the specified id
     */
    public ActivityWorkGroup getWorkGroup(final int wgId) {
        Iterator<ActivityWorkGroup> iter = workGroupTable.iterator();
        while (iter.hasNext()) {
        	ActivityWorkGroup opc = iter.next();
        	if (opc.getIdentifier() == wgId)
        		return opc;        	
        }
        return null;
    }
	
	/**
	 * Returns the amount of WGs associated to this IFlow
	 * @return the amount of WGs associated to this IFlow
	 */
	public int getWorkGroupSize() {
		return workGroupTable.size();
	}

    /**
     * Returns an iterator over the WGs of this activity.
     * @return An iterator over the WGs that can perform this activity.
     */
    public Iterator<ActivityWorkGroup> iterator() {
    	return workGroupTable.iterator();
    }

	@Override
	public void addPredecessor(final IFlow newFlow) {}
    
	/**
	 * Returns true if this delay IFlow is being used as part of an interruptible activity
	 * @return True if this delay IFlow is being used as part of an interruptible activity
	 */
	public boolean partOfInterruptible() {
		if (parent != null)
			if (parent instanceof ActivityFlow)
				return ((ActivityFlow)parent).isInterruptible();
		return false;
	}
	
	@Override
	public String getObjectTypeIdentifier() {
		return "ACQ";
	}
	
	// User methods
	/**
	 * Allows a user for adding a customized code when the {@link ElementInstance} actually starts the
	 * execution of the {@link RequestResourcesFlow}.
	 * @param ei {@link ElementInstance} requesting this {@link RequestResourcesFlow}
	 */
	public void afterAcquire(final ElementInstance ei) {}

	/**
	 * Allows a user for adding a customized code when a {@link com.ull.simulation.model.ElementInstance} from an {@link com.ull.simulation.model.Element}
	 * is enqueued, waiting for available {@link com.ull.simulation.model.Resource}. 
	 * @param ei {@link com.ull.simulation.model.ElementInstance} requesting resources
	 */
	public void inqueue(final ElementInstance ei) {}
	
	@Override
	public void afterFinalize(final ElementInstance ei) {}

	// End of user methods

	/**
     * Checks if this basic step can be performed with any of its workgroups. Firstly 
     * checks if the basic step is not potentially feasible, then goes through the 
     * workgroups looking for an appropriate one. If the basic step cannot be performed with 
     * any of the workgroups it's marked as not potentially feasible. 
     * @param ei Element instance wanting to perform the basic step 
     * @return A set of resources that makes a valid solution for this request IFlow; null otherwise. 
     */
	public ArrayDeque<Resource> isFeasible(final ElementInstance ei) {
    	if (!stillFeasible)
    		return null;
        Iterator<ActivityWorkGroup> iter = workGroupTable.balancedIterator();
        while (iter.hasNext()) {
        	ActivityWorkGroup wg = iter.next();
        	final ArrayDeque<Resource> solution = new ArrayDeque<Resource>(); 
            if (engine.checkWorkGroup(solution, wg, ei)) {
                ei.setExecutionWG(wg);
        		ei.getElement().trace("Can carry out \t" + this + "\t" + wg);
                return solution;
            }            
        }
        // No valid WG was found
        stillFeasible = false;
        return null;
	}

    /**
     * Sets this activity as potentially feasible.
     */
    public void resetFeasible() {
    	stillFeasible = true;
    }
    
    /**
     * Returns how many elements are waiting to seize resources with this IFlow
     * @return how many elements are waiting to seize resources with this IFlow
     */
    public int getQueueSize() {
    	return engine.getQueueSize();
    }
    
    /**
     * Removes an element instance from the queue
     * @param ei Element instance
     */
    public void queueRemove(final ElementInstance ei) {
    	engine.queueRemove(ei);
    }

	/*
	 * (non-Javadoc)
	 * @see com.ull.simulation.IFlow#request(com.ull.simulation.FlowExecutor)
	 */
	public void request(final ElementInstance ei) {
		if (!ei.wasVisited(this)) {
			if (ei.isExecutable()) {
				if (beforeRequest(ei)) {
					simul.notifyInfo(new ElementActionInfo(simul, ei, ei.getElement(),
							this, ei.getExecutionWG(), null, ElementActionInfo.Type.REQ, simul.getTs()));
					ei.getElement().trace("Requests\t" + this + "\t" + getDescription());
					engine.queueAdd(ei); // The element is introduced in the queue
					manager.notifyAvailableElement(ei);
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

	/*
	 * (non-Javadoc)
	 * @see com.ull.simulation.ITaskFlow#finish(com.ull.simulation.FlowExecutor)
	 */
	public void finish(final ElementInstance wThread) {
		wThread.endDelay(this);
		next(wThread);
	}
	
	@Override
	public void assignSimulation(final SimulationEngine simul) {

		engine = simul.getRequestResourcesEngineInstance(this);
	}

	/**
	 * Creates a builder object for adding workgroups to this IFlow. 
	 * @param wg The set of pairs <ResurceType, amount> which will be seized
	 * @return The builder object for adding workgroups to this IFlow
	 */
	public WorkGroupAdder newWorkGroupAdder(final WorkGroup wg) {
		return new WorkGroupAdder(wg);
	}
	
	/**
	 * A builder for adding workgroups. By default, workgroups have the highest priority, unconditionally available and have not delay.
	 * The priority, condition and delay can be modified by using the "with..." methods.
	 * @author Iván Castilla Rodríguez
	 *
	 */
	public final class WorkGroupAdder {
		/** The set of pairs <ResurceType, amount> which will be seized */
		final private WorkGroup wg;
		/** Priority of the workgroup */
		private int priority = 0;
		/** Availability condition */
		private AbstractCondition<ElementInstance> cond = null;
		/** Delay applied after seizing the resources */
		private AbstractTimeFunction delay = null;
		
		private WorkGroupAdder(final WorkGroup wg) {
			this.wg = wg;
		}
		
		public WorkGroupAdder withPriority(final int priority) {
			this.priority = priority;
			return this;
		}
		
		public WorkGroupAdder withCondition(final AbstractCondition<ElementInstance> cond) {
			this.cond = cond;
			return this;
		}

		public WorkGroupAdder withDelay(final AbstractTimeFunction delay) {
			this.delay = delay;
			return this;
		}

		public WorkGroupAdder withDelay(final long delay) {
			this.delay = TimeFunctionFactory.getInstance("ConstantVariate", delay);
			return this;
		}
		
	    /**
	     * Creates a new workgroup for this IFlow. 
	     * @return The new workgroup's identifier.
	     */
		public int add() {
			if (cond == null)
				cond = new TrueCondition<ElementInstance>();
			if (delay == null)
				delay = TimeFunctionFactory.getInstance("ConstantVariate", 0L);			
	    	final int wgId = workGroupTable.size();
	        workGroupTable.add(new ActivityWorkGroup(simul, wgId, priority, wg, cond, delay));
	        return wgId;
		}
	}

	/**
	 * A set of resources needed for carrying out an activity. A workgroup (WG) consists on a 
	 * set of (resource type, #needed resources) pairs, a condition which determines if the 
	 * workgroup can be used or not, and the priority of the workgroup inside the basicStep.
	 * @author Iván Castilla Rodríguez
	 */
	public class ActivityWorkGroup extends WorkGroup implements Prioritizable {
		/** Priority of the workgroup */
	    final private int priority;
	    /** Availability condition */
	    final private AbstractCondition<ElementInstance> cond;
	    /** A function to characterize the duration of the delay */
	    final private AbstractTimeFunction duration;
	    /** Precomputed string which identifies this WG */
	    final private String idString; 
		
	    /**
	     * Creates a new instance of WorkGroup which contains the same resource types
	     * than an already existing one.
	     * @param id Identifier of this workgroup.
	     * @param priority Priority of the workgroup.
	     * @param wg The original workgroup
	     * @param cond  Availability condition
	     */    
	    public ActivityWorkGroup(final Simulation model, final int id, final int priority, final WorkGroup wg,
								 final AbstractCondition<ElementInstance> cond, final AbstractTimeFunction duration) {
	    	super(model, wg.getResourceTypes(), wg.getNeeded());
	        this.priority = priority;
	        this.cond = cond;
	        this.duration = duration;
	        this.idString = new String("(" + RequestResourcesFlow.this + ")" + wg.getDescription());
	    }
	    
	    /**
	     * Getter for property priority.
	     * @return Value of property priority.
	     */
	    public int getPriority() {
	        return priority;
	    }

	    /**
	     * Returns a function to characterize the duration of the delay
		 * @return A function to characterize the duration of the delay
		 */
		public AbstractTimeFunction getDuration() {
			return duration;
		}

	    /**
	     * Returns the duration of the activity where this workgroup is used. 
	     * The value returned by the random number function could be negative. 
	     * In this case, it returns 0.
	     * @param elem The element performing the activity
	     * @return The activity duration.
	     */
	    public long getDurationSample(final Element elem) {
	    	return Math.round(duration.getValue(elem));
	    }
	    
	    @Override
	    public String toString() {
	    	return idString;
	    }

	    /**
	     * Returns a condition to set the availability of the workgroup
	     * @return a condition to set the availability of the workgroup
	     */
		public AbstractCondition<ElementInstance> getCondition() {
			return cond;
		}
	}
}
