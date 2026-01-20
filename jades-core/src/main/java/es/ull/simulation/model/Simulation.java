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
public class Simulation implements IIdentifiable, IDescribable, IVariableStore, ILoggable, IHandlesInformation, ISimulationContext {
	/** The default time unit used by the simulation */
	public final static TimeUnit DEF_TIME_UNIT = TimeUnit.MINUTE;
	/** A short text describing this simulation. */
	protected final String description;
	/** Time unit of the simulation */
	protected final TimeUnit unit;
	/** A unique simulation identifier */
	protected final int id;
	
	/** Component registry - DIP: depends on abstraction */
	private final ISimulationRegistry registry;
	
	/** ID generator - DIP: depends on abstraction */
	private final IIdGenerator idGenerator;

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
		this(id, description, DEF_TIME_UNIT, new SimulationRegistry(), new SequentialIdGenerator());
	}

	/**
	 * Creates a new instance of a simulation
	 *
	 * @param id Simulation identifier
	 * @param description A short text describing this simulation.
	 */
	public Simulation(final int id, final String description, final TimeUnit unit) {
		this(id, description, unit, new SimulationRegistry(), new SequentialIdGenerator());
	}
	
	/**
	 * Creates a new instance of a simulation with custom registry and ID generator (DIP constructor)
	 *
	 * @param id Simulation identifier
	 * @param description A short text describing this simulation.
	 * @param unit Time unit
	 * @param registry Component registry implementation
	 * @param idGenerator ID generator implementation
	 */
	public Simulation(final int id, final String description, final TimeUnit unit, 
			final ISimulationRegistry registry, final IIdGenerator idGenerator) {
		this.id = id;
		this.description = description;
		this.unit = unit;
		this.registry = registry;
		this.idGenerator = idGenerator;
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
	@Override
	public long getCurrentTimestamp() {
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
		for (ElementType et : registry.getElementTypes())
			et.assignSimulation(simulationEngine);
		for (ResourceType rt : registry.getResourceTypes())
			rt.assignSimulation(simulationEngine);
		for (Resource res : registry.getResources())
			res.assignSimulation(simulationEngine);
		for (WorkGroup wg : registry.getWorkGroups())
			wg.assignSimulation(simulationEngine);
		for (BasicFlow f : registry.getFlows())
			f.assignSimulation(simulationEngine);
		for (Generator<?> gen : registry.getTimeDrivenGenerators())
			gen.assignSimulation(simulationEngine);
		for (ActivityManager am : registry.getActivityManagers())
			am.assignSimulation(simulationEngine);
	}

	public void debug(final String description) {
		logger.debug(this.toString() + "\t" + getCurrentTimestamp() + "\t" + description);
	}

	public void trace(final String description) {
		logger.trace(this.toString() + "\t" + getCurrentTimestamp() + "\t" + description);
	}

	public void error(final String description) {
		logger.error(this.toString() + "\t" + getCurrentTimestamp() + "\t" + description);
	}

	/**
	 * Returns a unique identifier for a newly created element.
	 * @return a unique identifier for a newly created element.
	 */
	@Override
	public int generateId() {
		return idGenerator.generateId();
	}

	/**
	 * Adds a new event to the simulation
	 * @param ev New event
	 */
	@Override
	public void scheduleEvent(final DiscreteEvent ev) {
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
		trace("SIMULATION MODEL CREATED\t" + getCurrentTimestamp());
		init();

		infoHandler.notifyInfo(new SimulationStartStopInfo(this, SimulationStartStopInfo.Type.START, startTs));

		// Starts all the time driven generators
		for (TimeDrivenGenerator<?> evSource : registry.getTimeDrivenGenerators())
			simulationEngine.addWait(evSource.onCreate(startTs));
		// Starts all the resources
		for (Resource res : registry.getResources())
			simulationEngine.addWait(res.onCreate(startTs));

		// Adds the event to control end of simulation
		simulationEngine.addWait(new SimulationEndEvent());

		simulationEngine.simulationLoop();

		trace("SIMULATION FINISHES\t" + getCurrentTimestamp() + "\t[EXPECTED " + endTs + "]");
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
		for (ConditionDrivenGenerator<?> gen : registry.getConditionDrivenGenerators()) {
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
		registry.reset();
		idGenerator.reset();
	}

	/**
	 * Adds an {@link ElementType} to the model. This method is invoked from the object's constructor.
	 * @param et Element Type that's added to the model.
	 */
	public void add(final ElementType et) {
		registry.registerElementType(et);
	}

	/**
	 * Adds a {@link Resource} to the simulation. This method is invoked from the object's constructor.
	 * @param res Resource that's added to the model.
	 */
	public void add(final Resource res) {
		registry.registerResource(res);
	}

	/**
	 * Adds an {@link ResourceType} to the model. This method is invoked from the object's constructor.
	 * @param rt Resource Type that's added to the model.
	 */
	public void add(final ResourceType rt) {
		registry.registerResourceType(rt);
	}

	/**
	 * Adds an {@link WorkGroup} to the model. This method is invoked from the object's constructor.
	 * @param wg Workgroup that's added to the model.
	 */
	public void add(final WorkGroup wg) {
		registry.registerWorkGroup(wg);
	}
	/**
	 * Adds an {@link BasicFlow} to the model. This method is invoked from the object's constructor.
	 * @param f IFlow that's added to the model.
	 */
	public void add(final BasicFlow f) {
		registry.registerFlow(f);
	}

	/**
	 * Adds an {@link TimeDrivenGenerator} to the model. This method is invoked from the object's constructor.
	 * @param gen Time-driven generator that's added to the model.
	 */
	public void add(final TimeDrivenGenerator<?> gen) {
		registry.registerTimeDrivenGenerator(gen);
	}

	/**
	 * Adds an {@link ConditionDrivenGenerator} to the model. This method is invoked from the object's constructor.
	 * @param gen Condition-driven generator that's added to the model.
	 */
	public void add(final ConditionDrivenGenerator<?> gen) {
		registry.registerConditionDrivenGenerator(gen);
	}

	/**
	 * Adds an {@link ActivityManager} to the simulation. The activity managers are  automatically added from
	 * their constructor.
	 * @param am Activity manager.
	 */
	public void add(final ActivityManager am) {
		registry.registerActivityManager(am);
	}

	/**
	 * Returns the list of {@link ElementType element types} defined within this simulation
	 * @return the list of {@link ElementType element types} defined within this simulation
	 */
	public List<ElementType> getElementTypeList() {
		return registry.getElementTypes();
	}

	/**
	 * Returns the list of {@link Resource resources} defined within this simulation
	 * @return the list of {@link Resource resources} defined within this simulation
	 */
	public List<Resource> getResourceList() {
		return registry.getResources();
	}

	/**
	 * Returns the list of {@link ResourceType resource types} defined within this simulation
	 * @return the list of {@link ResourceType resource types} defined within this simulation
	 */
	public List<ResourceType> getResourceTypeList() {
		return registry.getResourceTypes();
	}

	/**
	 * Returns the list of {@link WorkGroup workgroups} defined within this simulation
	 * @return the list of {@link WorkGroup workgroups} defined within this simulation
	 */
	public List<WorkGroup> getWorkGroupList() {
		return registry.getWorkGroups();
	}

	/**
	 * Returns the list of {@link BasicFlow flows} defined within this simulation
	 * @return the list of {@link BasicFlow flows} defined within this simulation
	 */
	public List<BasicFlow> getFlowList() {
		return registry.getFlows();
	}

	/**
	 * Returns the list of {@link RequestResourcesFlow flows requesting resources} defined within this simulation
	 * @return the list of {@link RequestResourcesFlow flows requesting resources} defined within this simulation
	 */
	public List<RequestResourcesFlow> getRequestFlowList() {
		return registry.getRequestFlows();
	}

	/**
	 * Returns the list of {@link TimeDrivenGenerator time-driven generators} defined within this simulation
	 * @return the list of {@link TimeDrivenGenerator time-driven generators} defined within this simulation
	 */
	public List<TimeDrivenGenerator<?>> getTimeDrivenGeneratorList() {
		return registry.getTimeDrivenGenerators();
	}

	/**
	 * Returns the list of {@link ConditionDrivenGenerator condition-driven generators} defined within this simulation
	 * @return the list of {@link ConditionDrivenGenerator condition-driven generators} defined within this simulation
	 */
	public List<ConditionDrivenGenerator<?>> getConditionDrivenGeneratorList() {
		return registry.getConditionDrivenGenerators();
	}

	/**
	 * Returns the list of {@link ActivityManager activity managers} defined within this simulation
	 * @return the list of {@link ActivityManager activity managers} defined within this simulation
	 */
	public List<ActivityManager> getActivityManagerList() {
		return registry.getActivityManagers();
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
		for (ActivityManager am : registry.getActivityManagers())
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
