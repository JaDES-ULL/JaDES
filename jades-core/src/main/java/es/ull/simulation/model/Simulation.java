/**
 * 
 */
package es.ull.simulation.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import es.ull.simulation.info.IPieceOfInformation;
import es.ull.simulation.info.SimulationStartStopInfo;
import es.ull.simulation.inforeceiver.BasicListener;
import es.ull.simulation.inforeceiver.IHandlesInformation;
import es.ull.simulation.inforeceiver.InfoHandler;
import es.ull.simulation.model.engine.SimulationEngine;
import es.ull.simulation.model.flow.BasicFlow;
import es.ull.simulation.model.flow.RequestResourcesFlow;
import es.ull.simulation.variable.BooleanVariable;
import es.ull.simulation.variable.ByteVariable;
import es.ull.simulation.variable.CharacterVariable;
import es.ull.simulation.variable.DoubleVariable;
import es.ull.simulation.variable.FloatVariable;
import es.ull.simulation.variable.IntVariable;
import es.ull.simulation.variable.LongVariable;
import es.ull.simulation.variable.ShortVariable;
import es.ull.simulation.variable.IUserVariable;
import es.ull.simulation.variable.IVariable;

/**
 * The main simulation class. Defines all the components of the model and the logical structures required to simulate them.
 * 
 * @author Ivan Castilla Rodriguez
 *
 */
public class Simulation implements IIdentifiable, IDescribable, IVariableStore, ILoggable, IHandlesInformation {
	/** The default time unit used by the simulation */
	public final static TimeUnit DEF_TIME_UNIT = TimeUnit.MINUTE; 	
	/** A short text describing this simulation. */
	protected final String description;
	/** Time unit of the simulation */
	protected final TimeUnit unit;
	/** A unique simulation identifier */
	protected final int id;
	/** The identifier to be assigned to the next element */ 
	private int elemCounter = 0;
	
	/** List of element types present in the simulation. */
	private final ArrayList<ElementType> elementTypeList = new ArrayList<ElementType>();
	/** List of resources present in the simulation. */
	private final ArrayList<Resource> AbstractResourceList = new ArrayList<Resource>();
	/** List of resource types present in the simulation. */
	private final ArrayList<ResourceType> resourceTypeList = new ArrayList<ResourceType>();
	/** List of workgroups present in the simulation */
	private final ArrayList<WorkGroup> workGroupList = new ArrayList<WorkGroup>();
	/** List of flows present in the simulation */
	private final ArrayList<BasicFlow> flowList = new ArrayList<BasicFlow>();
	/** List of request flows present in the simulation. */
	private final ArrayList<RequestResourcesFlow> reqFlowList = new ArrayList<RequestResourcesFlow>();
	/** List of time-driven element generators of the simulation. */
	private final ArrayList<TimeDrivenGenerator<?>> tGenList = new ArrayList<TimeDrivenGenerator<?>>();
	/** List of condition-driven element generators of the simulation. */
	private final ArrayList<ConditionDrivenGenerator<?>> cGenList = new ArrayList<ConditionDrivenGenerator<?>>();
	/** List of activity managers that partition the simulation. */
	private final ArrayList<ActivityManager> amList = new ArrayList<ActivityManager>();

	/** Variable store */
	protected final Map<String, IVariable> varCollection = new TreeMap<String, IVariable>();
	
	/** A handler for the information produced by the execution of this simulation */
	protected final InfoHandler infoHandler = new InfoHandler();
	
	/** The simulation engine that executes this model */
	protected SimulationEngine simulationEngine = null;
	
	/** The way the activity managers are created */
	protected ActivityManagerCreator amCreator = null;
	
	/** A value representing the simulation's start timestamp without unit */
	protected long startTs;

	/** A value representing the simulation's end timestamp without unit */
	protected long endTs;
	
	/**
	 * Creates a new instance of a simulation
	 *
	 * @param id Simulation identifier
	 * @param description A short text describing this simulation.
	 */
	public Simulation(final int id, final String description) {
		this.id = id;
		this.description = description;
		unit = DEF_TIME_UNIT;
	}
	
	/**
	 * Creates a new instance of a simulation
	 *
	 * @param id Simulation identifier
	 * @param description A short text describing this simulation.
	 */
	public Simulation(final int id, final String description, final TimeUnit unit) {
		this.id = id;
		this.description = description;
		this.unit = unit;
	}
	
	@Override
	public int getIdentifier() {
		return id;
	}

	/**
	 * Returns this simulation's time unit
	 * @return the unit
	 */
	public TimeUnit getTimeUnit() {
		return unit;
	}
	
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * Returns a long value representing the simulation's end timestamp without unit.
	 * @return A long value representing the simulation's end timestamp without unit
	 */
	public long getEndTs() {
		return endTs;
	}

	/**
	 * Returns a long value representing the simulation's start timestamp without unit.
	 * @return A long value representing the simulation's start timestamp without unit
	 */
	public long getStartTs() {
		return startTs;
	}

    /**
     * Returns the current simulation time
     * @return The current simulation time
     */
	public long getTs() {
		return simulationEngine.getTs();
	}
	
	/**
	 * Returns the simulation engine that executes this model
	 * @return The simulation engine that executes this model
	 */
	public SimulationEngine getSimulationEngine() {
		return simulationEngine;
	}

	/**
	 * Sets the simulation engine that executes this model
	 * @param simulationEngine the simulation engine to set
	 */
	public void setSimulationEngine(final SimulationEngine simulationEngine) {
		this.simulationEngine = simulationEngine;
		for (ElementType et : elementTypeList)
			et.assignSimulation(simulationEngine);
		for (ResourceType rt : resourceTypeList)
			rt.assignSimulation(simulationEngine);
		for (Resource res : AbstractResourceList)
			res.assignSimulation(simulationEngine);
		for (WorkGroup wg : workGroupList)	
			wg.assignSimulation(simulationEngine);
		for (BasicFlow f : flowList)
			f.assignSimulation(simulationEngine);
		for (Generator<?> gen : tGenList)
			gen.assignSimulation(simulationEngine);
		for (ActivityManager am : amList)
			am.assignSimulation(simulationEngine);
	}

	public void debug(final String description) {
		logger.debug(this.toString() + "\t" + getTs() + "\t" + description);
	}

	public void trace(final String description) {
		logger.trace(this.toString() + "\t" + getTs() + "\t" + description);
	}

	public void error(final String description) {
		logger.error(this.toString() + "\t" + getTs() + "\t" + description);
	}

	/**
	 * Returns a unique identifier for a newly created element. 
	 * @return a unique identifier for a newly created element.
	 */
	public int getNewElementId() {
		return elemCounter++;
	}
	
	/**
	 * Adds a new event to the simulation
	 * @param ev New event
	 */
	public void addEvent(final DiscreteEvent ev) {
		simulationEngine.addEvent(ev);
	}

	/**
	 * Starts the execution of the simulation at timestamp 0 using the default time unit.
	 * 
	 * @param endTs Simulation end timestamp
	 */
	public void run(final TimeStamp endTs) {
		run(0L, unit.convert(endTs));
	}

	/**
	 * Starts the execution of the simulation at timestamp 0 using the default time unit.
	 * @param unit This simulation's time unit
	 * @param endTs Simulation end expressed in simulation default time units
	 */
	public void run(final long endTs) {
		run(0L, endTs);
	}

	/**
	 * Starts the execution of the simulation using the default time unit.
	 * 
	 * @param startTs Simulation start timestamp
	 * @param endTs Simulation end timestamp
	 */
	public void run(final TimeStamp startTs, final TimeStamp endTs) {
		run(unit.convert(startTs), unit.convert(endTs));
	}

	/**
	 * Starts the execution of the simulation. It creates and initializes all the necessary 
	 * structures.<p> The following checks and initializations are performed within this method:
	 * <ol>
	 * <li>If no customized {@link ActivityManagerCreator AM creator} has been defined, the 
	 * {@link StandardActivityManagerCreator default one} is used.</li>
	 * <li>If no customized {@link SimulationEngine simulation engine} has been defined, a
	 * {@link SequentialSimulationEngine sequential engine} is used.</li>
	 * <li>The user defined method {@link #init()} is invoked.</li>
	 * <li>{@link Resource Resources} and {@link Generator generators} are started.</li>
	 * <li>The main simulation loop is run</li>
	 * <li>The user defined method {@link #end()} is invoked.</li>
	 * </ol>
	 * @param startTs Simulation start expressed in simulation default time units
	 * @param endTs Simulation end expressed in simulation default  time units
     */ 
	public void run(long startTs, long endTs) {
		this.startTs = startTs;
		this.endTs = endTs;
		// Sets default simulation engine
		if (simulationEngine == null) {
			setSimulationEngine(new SimulationEngine(id, this));
		}
		// Sets default AM creator
		if (amCreator == null)
			amCreator = new StandardActivityManagerCreator(this);
		amCreator.createActivityManagers();
		debugPrintActManager();					
		simulationEngine.initializeEngine();
		trace("SIMULATION MODEL CREATED\t" + getTs());
		init();

		infoHandler.notifyInfo(new SimulationStartStopInfo(this, SimulationStartStopInfo.Type.START, startTs));
		
		// Starts all the time driven generators
		for (TimeDrivenGenerator<?> evSource : tGenList)
			simulationEngine.addWait(evSource.onCreate(startTs));
		// Starts all the resources
		for (Resource res : AbstractResourceList)
			simulationEngine.addWait(res.onCreate(startTs));

		// Adds the event to control end of simulation
		simulationEngine.addWait(new SimulationEndEvent());
		
		simulationEngine.simulationLoop();

		trace("SIMULATION FINISHES\t" + getTs() + "\t[EXPECTED " + endTs + "]");
    	simulationEngine.printState();
		
		infoHandler.notifyInfo(new SimulationStartStopInfo(this, SimulationStartStopInfo.Type.END, endTs));    	
        // The user defined method for finalization is invoked
		end();
	}

	/**
	 * Checks the conditions stated in the condition-driven generators. If the condition meets,
	 * creates the corresponding event sources.
	 */
	public void checkConditions() {
		for (ConditionDrivenGenerator<?> gen : cGenList) {
			if (gen.getCondition().check(null))
				gen.create();
		}
	}
	
	/**
	 * Resets variables or contents of the model. It should be invoked by the user when the same model
	 * is used for multiple replicas
	 * and contains variables that must be initialized among replicas.
	 */
	public void reset() {		
	}
	
	/**
	 * Adds an {@link ElementType} to the model. This method is invoked from the object's constructor.
	 * @param et Element Type that's added to the model.
	 */
	public void add(final ElementType et) { 
		elementTypeList.add(et);
	}
	
	/**
	 * Adds a {@link Resource} to the simulation. This method is invoked from the object's constructor.
	 * @param res Resource that's added to the model.
	 */
	public void add(final Resource res) { 
		AbstractResourceList.add(res);
	}
	
	/**
	 * Adds an {@link ResourceType} to the model. This method is invoked from the object's constructor.
	 * @param rt Resource Type that's added to the model.
	 */
	public void add(final ResourceType rt) { 
		resourceTypeList.add(rt);
	}

	/**
	 * Adds an {@link WorkGroup} to the model. This method is invoked from the object's constructor.
	 * @param wg Workgroup that's added to the model.
	 */
	public void add(final WorkGroup wg) { 
		workGroupList.add(wg);
	}
	/**
	 * Adds an {@link BasicFlow} to the model. This method is invoked from the object's constructor.
	 * @param f IFlow that's added to the model.
	 */
	public void add(final BasicFlow f) { 
		flowList.add(f);
		if (f instanceof RequestResourcesFlow)
			reqFlowList.add((RequestResourcesFlow)f);
	}
	
	/**
	 * Adds an {@link TimeDrivenGenerator} to the model. This method is invoked from the object's constructor.
	 * @param gen Time-driven generator that's added to the model.
	 */
	public void add(final TimeDrivenGenerator<?> gen) {
		tGenList.add(gen);
	}
	
	/**
	 * Adds an {@link ConditionDrivenGenerator} to the model. This method is invoked from the object's constructor.
	 * @param gen Condition-driven generator that's added to the model.
	 */
	public void add(final ConditionDrivenGenerator<?> gen) {
		cGenList.add(gen);
	}
	
	/**
	 * Adds an {@link ActivityManager} to the simulation. The activity managers are  automatically added from
	 * their constructor.
	 * @param am Activity manager.
	 */
	public void add(final ActivityManager am) {
		amList.add(am);
	}

	/**
	 * Returns the list of {@link ElementType element types} defined within this simulation
	 * @return the list of {@link ElementType element types} defined within this simulation
	 */
	public List<ElementType> getElementTypeList() { 
		return elementTypeList;
	}
	
	/**
	 * Returns the list of {@link Resource resources} defined within this simulation
	 * @return the list of {@link Resource resources} defined within this simulation
	 */
	public List<Resource> getResourceList() { 
		return AbstractResourceList;
	}
	
	/**
	 * Returns the list of {@link ResourceType resource types} defined within this simulation
	 * @return the list of {@link ResourceType resource types} defined within this simulation
	 */
	public List<ResourceType> getResourceTypeList() { 
		return resourceTypeList;
	}
	
	/**
	 * Returns the list of {@link WorkGroup workgroups} defined within this simulation
	 * @return the list of {@link WorkGroup workgroups} defined within this simulation
	 */
	public List<WorkGroup> getWorkGroupList() { 
		return workGroupList;
	}
	
	/**
	 * Returns the list of {@link BasicFlow flows} defined within this simulation
	 * @return the list of {@link BasicFlow flows} defined within this simulation
	 */
	public List<BasicFlow> getFlowList() { 
		return flowList;
	}
	
	/**
	 * Returns the list of {@link RequestResourcesFlow flows requesting resources} defined within this simulation
	 * @return the list of {@link RequestResourcesFlow flows requesting resources} defined within this simulation
	 */
	public List<RequestResourcesFlow> getRequestFlowList() { 
		return reqFlowList;
	}

	/**
	 * Returns the list of {@link TimeDrivenGenerator time-driven generators} defined within this simulation
	 * @return the list of {@link TimeDrivenGenerator time-driven generators} defined within this simulation
	 */
	public List<TimeDrivenGenerator<?>> getTimeDrivenGeneratorList() {
		return tGenList;
	}

	/**
	 * Returns the list of {@link ConditionDrivenGenerator condition-driven generators} defined within this simulation
	 * @return the list of {@link ConditionDrivenGenerator condition-driven generators} defined within this simulation
	 */
	public List<ConditionDrivenGenerator<?>> getConditionDrivenGeneratorList() {
		return cGenList;
	}

	/**
	 * Returns the list of {@link ActivityManager activity managers} defined within this simulation
	 * @return the list of {@link ActivityManager activity managers} defined within this simulation
	 */
	public List<ActivityManager> getActivityManagerList() {
		return amList;
	}
	
	/**
	 * A convenience method for converting a timestamp to a long value expressed in the
	 * simulation's time unit.
	 * @param source A timestamp
	 * @return A long value representing the received timestamp in the simulation's time unit 
	 */
	public long simulationTime2Long(final TimeStamp source) {
		return unit.convert(source);
	}
	
	/**
	 * A convenience method for converting a long value expressed in the simulation's time unit
	 * to a timestamp.
	 * @param sourceValue A long value expressed in the simulation's time unit
	 * @return A timestamp representing the received long value in the simulation's time unit 
	 */
	public TimeStamp long2SimulationTime(final long sourceValue) {
		return new TimeStamp(unit, sourceValue);
	}

	@Override
	public String toString() {
		return "[SIM" + id + "]";
	}

	@Override
	public IVariable getVar(final String varName) {
		return varCollection.get(varName);
	}
	
	@Override
	public void putVar(final String varName, final IVariable value) {
		varCollection.put(varName, value);
	}
	
	@Override
	public void putVar(final String varName, final double value) {
		IUserVariable v = (IUserVariable) varCollection.get(varName);
		if (v != null) {
			v.setValue(value);
			varCollection.put(varName, v);
		} else
			varCollection.put(varName, new DoubleVariable(value));
	}
	
	@Override
	public void putVar(final String varName, final int value) {
		IUserVariable v = (IUserVariable) varCollection.get(varName);
		if (v != null) {
			v.setValue(value);
			varCollection.put(varName, v);
		} else
			varCollection.put(varName, new IntVariable(value));
	}

	@Override
	public void putVar(final String varName, final boolean value) {
		IUserVariable v = (IUserVariable) varCollection.get(varName);
		if (v != null) {
			v.setValue(value);
			varCollection.put(varName, v);
		} else
			varCollection.put(varName, new BooleanVariable(value));
	}

	@Override
	public void putVar(final String varName, final char value) {
		IUserVariable v = (IUserVariable) varCollection.get(varName);
		if (v != null) {
			v.setValue(value);
			varCollection.put(varName, v);
		} else
			varCollection.put(varName, new CharacterVariable(value));
	}
	
	@Override
	public void putVar(final String varName, final byte value) {
		IUserVariable v = (IUserVariable) varCollection.get(varName);
		if (v != null) {
			v.setValue(value);
			varCollection.put(varName, v);
		} else
			varCollection.put(varName, new ByteVariable(value));
	}

	@Override
	public void putVar(final String varName, final float value) {
		IUserVariable v = (IUserVariable) varCollection.get(varName);
		if (v != null) {
			v.setValue(value);
			varCollection.put(varName, v);
		} else
			varCollection.put(varName, new FloatVariable(value));
	}
	
	@Override
	public void putVar(final String varName, final long value) {
		IUserVariable v = (IUserVariable) varCollection.get(varName);
		if (v != null) {
			v.setValue(value);
			varCollection.put(varName, v);
		} else
			varCollection.put(varName, new LongVariable(value));
	}
	
	@Override
	public void putVar(final String varName, final short value) {
		IUserVariable v = (IUserVariable) varCollection.get(varName);
		if (v != null) {
			v.setValue(value);
			varCollection.put(varName, v);
		} else
			varCollection.put(varName, new ShortVariable(value));
	}
	
	public double getVarViewValue(final Object...params) {
		String varName = (String) params[0];
		params[0] = this;
		Number value = getVar(varName).getValue(params);
		if (value != null)
			return value.doubleValue();
		else
			return -1;
	}
	
	/**
	 * Prints the contents of the activity managers created.
	 */
	protected void debugPrintActManager() {
		StringBuffer str1 = new StringBuffer("Activity Managers:\r\n");
		for (ActivityManager am : amList)
			str1.append(am.getDescription() + "\r\n");
		debug(str1.toString());
	}

	@Override
	public void registerListener(final BasicListener receiver) {
		infoHandler.registerListener(receiver);
	}

	@Override
	public void notifyInfo(final IPieceOfInformation info) {
		infoHandler.notifyInfo(info);
	}
	
	/**
	 * Returns the listeners attached to this simulation that are interested in receiving information of a certain type.
	 * @param infoTypeClass The type of information.
	 * @return The listeners that are interested in receiving information of the given type.
	 */
	public ArrayList<BasicListener> getListeners(final Class<? extends IPieceOfInformation> infoTypeClass) {
		return infoHandler.getListeners(infoTypeClass);
	}

	/**
	 * Returns all the listeners attached to this simulation that are interested in receiving information.
	 * @return All the listeners that are interested in receiving information.
	 */
	public ArrayList<BasicListener> getListeners() {
		return infoHandler.getListeners();
	}

	// User methods
	
	/**
	 * Allows a user for adding customized code before the simulation starts.
	 */
	public void init() {
	};
	
	/**
	 * Allows a user for adding customized code after the simulation finishes.
	 */
	public void end() {
	};
	
	/**
	 * Allows a user for adding customized code before the simulation clock advances.
	 */
	public void beforeClockTick() {
	};
	
	/**
	 * Allows a user for adding customized code just after the simulation clock advances.
	 */
	public void afterClockTick() {
	}

    /**
     * Allows a user for setting a termination condition for the simulation. The default condition will be that
     * the simulation time is equal or higher than the expected simulation end time.
     * @return True if the simulation must finish; false otherwise.
     */
    public boolean isSimulationEnd(final long currentTs) {
    	return (currentTs >= endTs);
    }
    
	// End of user methods
	
	/**
	 * A basic event which facilitates the control of the end of the simulation. Scheduling this event
	 * ensures that there's always at least one event in the simulation. 
	 * @author Iván Castilla Rodríguez
	 */
    class SimulationEndEvent extends DiscreteEvent {
    	/**
    	 * Creates a very simple element to control the simulation end.
    	 */
		public SimulationEndEvent() {
			super(Long.MAX_VALUE);
		}

		@Override
		public void event() {
		}

    }

}
