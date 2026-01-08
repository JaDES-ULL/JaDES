/*
 * Simulation.java
 *
 * Created on 8 de noviembre de 2005, 18:47
 */

package es.ull.simulation.model.engine;

import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;

import es.ull.simulation.info.TimeChangeInfo;
import es.ull.simulation.model.ActivityManager;
import es.ull.simulation.model.DiscreteEvent;
import es.ull.simulation.model.Element;
import es.ull.simulation.model.ElementType;
import es.ull.simulation.model.IIdentifiable;
import es.ull.simulation.model.Resource;
import es.ull.simulation.model.Simulation;
import es.ull.simulation.model.TimeDrivenElementGenerator;
import es.ull.simulation.model.TimeUnit;
import es.ull.simulation.model.WorkGroup;
import es.ull.simulation.model.flow.ActivityFlow;
import es.ull.simulation.model.flow.IFlow;
import es.ull.simulation.model.flow.AbstractMergeFlow;
import es.ull.simulation.model.flow.RequestResourcesFlow;
import es.ull.simulation.utils.Output;

/**
 * Main simulation class, identified by means of an identifier and a description.
 * A simulation executes a model defined by means of different structures: 
 * <ul>
 * <li>{@link ResourceTypeEngine}</li>
 * <li>{@link IResourceEngine}</li>
 * <li>{@link WorkGroup}</li>
 * <li>{@link ActivityFlow}</li>
 * <li>{@link ElementType}</li>
 * <li>{@link IFlow}</li>
 * <li>{@link TimeDrivenElementGenerator}</li>
 * </ul>
 * A simulation has an associated clock which starts in <tt>startTs</tt> and advances according 
 * to the events produced by the {@link Element}s, {@link IResourceEngine}s and {@link TimeDrivenElementGenerator}s. 
 * A "next-event" technique is used to determine the next timestamp to advance. A minimum 
 * {@link TimeUnit} determines the accuracy of the simulation's clock. The simulation ends when the 
 * simulation clock reaches the <tt>endTs</tt> timestamp or no more events are available.<br>
 * Depending on the specific implementation, a simulation can use one or more "worker" threads to 
 * execute the event's actions.
 * <p>
 * A user can interact with this Simulation by filling in some user methods that are activated in different
 * instants:
 * <ul>
 * <li>Just before the simulation starts {@link #init()}</li>
 * <li>Just after the simulation ends {@link #end()}</li>
 * <li>Just before the simulation clock advances {@link #beforeClockTick()}</li> 
 * <li>Just After the simulation clock advances {@link #afterClockTick()}</li> 
 * </ul> 
 * <p>
 * For debugging purposes, an {@link Output} can be associated to this simulation, thus
 * defining the destination for error and debug messages.
 * @author Iván Castilla Rodríguez
 */
public class SimulationEngine implements IIdentifiable {
	/** Simulation's identifier */
	protected final int id;
	/** The associated {@link Simulation} */
	protected final Simulation simul;

	/** End-of-simulation control */
	private CountDownLatch endSignal;

	/** The identifier to be assigned to the next element */ 
	protected int nextElementId = 0;
	/** List of active elements */
	private final Map<Integer, ElementEngine> activeElementList = new TreeMap<Integer, ElementEngine>();

    /** Local virtual time. Represents the current simulation time */
	protected long lvt;

	/** A timestamp-ordered list of events whose timestamp is in the future. */
	protected final PriorityQueue<DiscreteEvent> waitQueue = new PriorityQueue<DiscreteEvent>();
	
	/**
	 * Creates a new instance of Simulation
	 *
	 * @param id This simulation's identifier
	 * @param description A short text describing this simulation.
	 * @param unit This simulation's time unit
	 * @param startTs Timestamp of simulation's start expressed in Simulation Time Units
	 * @param endTs Timestamp of simulation's end expressed in Simulation Time Units
	 */
	public SimulationEngine(int id, Simulation simul) {
		this.id = id;
		this.simul = simul;
	}

	/**
	 * @return the simul
	 */
	public Simulation getSimulation() {
		return simul;
	}

	@Override
	public int getIdentifier() {
		return id;
	}
	
    /**
     * Sends an event to the waiting queue. An event is added to the waiting queue if 
     * its timestamp is greater than the LP timestamp.
     * @param e Event to be added
     */
	public void addWait(DiscreteEvent e) {
		waitQueue.add(e);
	}
	
	public void addExecution(DiscreteEvent e) {
		e.run();
	}
	
    protected DiscreteEvent removeWait() {
        return waitQueue.poll();
    }
    
    /**
     * Removes a specific event from the waiting queue. This function can be used to cancel an event
     * @param e Event to be removed
     * @return True if the queue contained the event; false otherwise
     */
    protected boolean removeWait(DiscreteEvent e) {
        return waitQueue.remove(e);
    }
    

	/**
	 * @return the nextElementId
	 */
	protected int getNextElementId() {
		return nextElementId++;
	}

	/**
	 * Adds an element when it starts its execution.
	 * 
	 * @param elem
	 *            An element that starts its execution.
	 */
	public void addActiveElement(ElementEngine elem) {
		activeElementList.put(elem.getIdentifier(), elem);
	}

	/**
	 * Removes an element when it finishes its execution.
	 * 
	 * @param elem
	 *            An element that finishes its execution.
	 */
	public void removeActiveElement(ElementEngine elem) {
		activeElementList.remove(elem.getIdentifier());
	}

	/**
	 * Returns the element with the specified identifier.
	 * 
	 * @param id
	 *            The element's identifier.
	 * @return The element with the specified identifier.
	 */
	public ElementEngine getActiveElement(int id) {
		return activeElementList.get(id);
	}

	/**
	 * Notifies the end of a logical process.
	 */
	protected void notifyEnd() {
		endSignal.countDown();
	}

	/**
	 * Prints the current state of the simulation for debug purposes. Prints the current local 
	 * time, the contents of the future event list and the execution queue. 
	 */
	public void printState() {
		StringBuffer strLong = new StringBuffer("------    LP STATE    ------");
		strLong.append("LVT: " + lvt + "\r\n");
		strLong.append(waitQueue.size() + " waiting elements: ");
		for (DiscreteEvent e : waitQueue)
			strLong.append(e + " ");
		strLong.append("\r\n------ LP STATE FINISHED ------\r\n");
		simul.debug(strLong.toString());
	}

    /**
     * Returns the current simulation time
     * @return The current simulation time
     */
	public long getTs() {
		return lvt;
	}


	public Map<Integer, ElementEngine> getActiveElementList() {
		return activeElementList;
	}
	
	public void initializeEngine() {
	}

	public ResourceList getResourceListInstance() {
		return new ResourceList();
	}

	public ResourceEngine getResourceEngineInstance(Resource modelRes) {

		return new ResourceEngine(this, modelRes);
	}

	public ActivityManagerEngine getActivityManagerEngineInstance(ActivityManager modelAM) {
		return new ActivityManagerEngine(this, modelAM);
	}

	public ElementEngine getElementEngineInstance(Element modelElem) {

		return new ElementEngine(this, modelElem);
	}
    
	public RequestResourcesEngine getRequestResourcesEngineInstance(
			RequestResourcesFlow reqFlow) {
		return new RequestResourcesEngine(this, reqFlow);
	}

	public MergeFlowEngine getMergeFlowEngineInstance(AbstractMergeFlow modelFlow) {
		return new MergeFlowEngine(this, modelFlow);
	}

	public void simulationLoop() {
		while (!simul.isSimulationEnd(lvt)) {
            // Executes all the events with timestamps equal to lvt 
			while (waitQueue.peek().getTs() == lvt) {
            	addExecution(waitQueue.poll());
			}
			// Checks the condition-driven generators and executes the events
			// FIXME: Not sure whether it would work 
			simul.checkConditions();
			while (waitQueue.peek().getTs() == lvt) {
            	addExecution(waitQueue.poll());
			}
			for (ActivityManager am : simul.getActivityManagerList()) {
				am.executeWork();
			}
			// Executes user-specified actions after all the events with the same timestamp have been executed
            simul.afterClockTick();
			// Updates the clock to the next valid event
			while (waitQueue.peek().isCancelled()) {
				waitQueue.poll();
			}
			final long newLVT = waitQueue.peek().getTs();
			// Executes user-specified actions before the clock advances
            simul.beforeClockTick();
			if (simul.isSimulationEnd(newLVT)) {
	            // Updates the simulation clock but do not execute further events
				lvt = (lvt > simul.getEndTs()) ? lvt : simul.getEndTs();
			}
			else {
	            // Updates the simulation clock
	            lvt = newLVT;
	    		simul.notifyInfo(new TimeChangeInfo(simul, lvt));
	            simul.trace("SIMULATION TIME ADVANCING " + lvt);
			}
		}
	}

	public void addEvent(DiscreteEvent ev) {
		if (ev.getTs() < lvt) {
			simul.error("Causal restriction broken\t" + lvt + "\t" + ev);
		}
        else {
            addWait(ev);
        }		
	}

	
}
