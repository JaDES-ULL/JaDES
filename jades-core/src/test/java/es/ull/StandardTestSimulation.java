/**
 * 
 */
package es.ull;

import java.util.ArrayList;
import java.util.TreeMap;

import com.beust.jcommander.Parameter;

import es.ull.CheckResourcesListener.ResourceUsageTimeStamps;
import es.ull.simulation.experiment.CommonArguments;
import es.ull.simulation.inforeceiver.StdInfoListener;
import es.ull.simulation.model.ElementType;
import es.ull.simulation.model.Resource;
import es.ull.simulation.model.ResourceType;
import es.ull.simulation.model.Simulation;
import es.ull.simulation.model.SimulationPeriodicCycle;
import es.ull.simulation.model.SimulationTimeFunction;
import es.ull.simulation.model.TimeDrivenElementGenerator;
import es.ull.simulation.model.TimeUnit;
import es.ull.simulation.model.WorkGroup;
import es.ull.simulation.model.flow.ActivityFlow;
import es.ull.simulation.model.flow.IInitializerFlow;
import es.ull.simulation.model.flow.ITaskFlow;

/**
 * The base class to create tests.
 * Since the checking needs to be automated, the models defined in the tests must adhere to the following restrictions:
 * - The simulation must be restricted to 1 day
 * - Simulation Time Unit must be MINUTE 
 * - No random number generators should be used. 
 * - Resources must use a simple periodic cycle
 * - The preferred cycle is the TableCycle   
 * - All the elements must finish their tasks within the simulated time
 * There are several methods to create "standard" simulation components. They should be used whenever possible. 
 * @author Iván Castilla Rodríguez
 *
 */
public abstract class StandardTestSimulation extends Simulation {
	public final static TimeUnit SIMUNIT = TimeUnit.MINUTE; 
	public final static long SIMSTART = 0L;
	private final ArrayList<Integer> nElems;
	private final ArrayList<Long> actDuration;
	private final TreeMap<ITaskFlow, Integer> actIndex; 
	private final ArrayList<ResourceUsageTimeStamps> roleOns;
	private final ArrayList<ResourceUsageTimeStamps> roleOffs;
	private final TestArguments args;
	
	public StandardTestSimulation(int id, String description, TestArguments args) {
		super(id, description);
		nElems = new ArrayList<Integer>();
		actDuration = new ArrayList<Long>();
		actIndex = new TreeMap<ITaskFlow, Integer>();
		roleOffs = new ArrayList<>();
		roleOns = new ArrayList<>();
		this.args = args;
		createModel();
		addCheckers();
	}
	
	/**
	 * Adds all the model components (resources, resource types...)
	 */
	protected abstract void createModel();

	/**
	 * Adds the required listeners to check that the simulation behaves as expected.
	 */
	protected void addCheckers() {
		if (!args.quiet)
			registerListener(new StdInfoListener());
		registerListener(new CheckResourcesListener(roleOns, roleOffs));
		registerListener(new CheckElementsListener(nElems));
		int n = 0;
		for (int count : nElems) 
			n += count;
		registerListener(new CheckActivitiesListener(n, actIndex, actDuration));
	}
	
	/**
	 * Returns the arguments used to create the simulation
	 * @return The arguments used to create the simulation
	 */
	public TestArguments getArguments() {
		return args;
	}
	/**
	 * Creates a new resource with the given description and resource type. The resource will follow default availability times.
	 * @param description The description of the resource
	 * @param rt The type of the resource 
	 * @return The new resource
	 */
	public Resource getDefResource(String description, ResourceType rt) {
		final Resource res = new Resource(this, description);
		final SimulationPeriodicCycle cycle = new SimulationPeriodicCycle(SIMUNIT, args.resStart, new SimulationTimeFunction(SIMUNIT, "ConstantVariate", args.resPeriod), 0);
		res.newTimeTableOrCancelEntriesAdder(rt).withDuration(cycle, args.resAvailability).addTimeTableEntry();
		
		long start = args.resStart;
		int activations = (int) ((args.simEnd - args.resStart) / args.resPeriod) + 1;
		long [] roleOnTimestamps = new long[activations];
		activations = (int) ((args.simEnd - args.resStart - args.resAvailability) / args.resPeriod) + 1; 
		long [] roleOffTimestamps = new long[activations];
		for (int i = 0; i < (int) ((args.simEnd - args.resStart) / args.resPeriod) + 1; i++) {
			roleOnTimestamps[i] = start;
			if (start + args.resAvailability < args.simEnd)
				roleOffTimestamps[i] = start + args.resAvailability;
			start += args.resPeriod;
		}

		roleOns.add(new ResourceUsageTimeStamps(res.getIdentifier(), rt.getIdentifier(), roleOnTimestamps));
		roleOffs.add(new ResourceUsageTimeStamps(res.getIdentifier(), rt.getIdentifier(), roleOffTimestamps));
		return res;
	}
	
	/**
	 * Creates a new resource type with the given description
	 * @param description The description of the resource type
	 * @return The new resource type
	 */
	public ResourceType getDefResourceType(String description) {
		return new ResourceType(this, description);
	}
	
	/**
	 * Creates a new exclusive activity with the given description and work group. The activity will have a default duration.
	 * @param description The description of the activity
	 * @param wg The work group required to perform the activity
	 * @return The new activity
	 */
	public ActivityFlow getDefActivity(String description, WorkGroup wg) {
		return getDefActivity(description, args.actDuration, wg, true);
	}
	
	/**
	 * Creates a new activity with the given description, work group and exclusivity. The activity will have a default duration.
	 * @param description The description of the activity
	 * @param wg The work group required to perform the activity
	 * @param exclusive If true, the activity cannot be performed concurrently with any other exclusive activity
	 * @return The new activity
	 */
	public ActivityFlow getDefActivity(String description, WorkGroup wg, boolean exclusive) {
		return getDefActivity(description, args.actDuration, wg, exclusive);
	}
	
	/**
	 * Creates a new exclusive activity with the given description, duration, and work group.
	 * @param description The description of the activity
	 * @param duration The duration of the activity
	 * @param wg The work group required to perform the activity
	 * @return The new activity
	 */
	public ActivityFlow getDefActivity(String description, long duration, WorkGroup wg) {
		return getDefActivity(description, duration, wg, true);
	}
	
	/**
	 * Creates a new activity with the given description, duration, work group, and exclusivity.
	 * @param description The description of the activity
	 * @param duration The duration of the activity
	 * @param wg The work group required to perform the activity
	 * @param exclusive If true, the activity cannot be performed concurrently with any other exclusive activity
	 * @return The new activity
	 */
	public ActivityFlow getDefActivity(String description, long duration, WorkGroup wg, boolean exclusive) {
		return new TestActivityFlow(description, duration, wg, exclusive);
	}
	
	/**
	 * Creates a new element type with the given description
	 * @param description The description of the element type
	 * @return The new element type
	 */
	public ElementType getDefElementType(String description) {
		// Adds a new element type but, until not used within a generator, it will create 0 elements
		nElems.add(0);
		return new ElementType(this, description);
	}

	/**
	 * Creates a new element type with the given description and priority
	 * @param description The description of the element type
	 * @param priority The priority of the element type
	 * @return The new element type
	 */
	public ElementType getDefElementType(String description, int priority) {
		// Adds a new element type but, until not used within a generator, it will create 0 elements
		nElems.add(0);
		return new ElementType(this, description, priority);
	}
	
	/**
	 * Creates a new generator cycle with the default values
	 * @return The new generator cycle
	 */
	public SimulationPeriodicCycle getGeneratorCycle() {
		return new SimulationPeriodicCycle(SIMUNIT, args.genStart, new SimulationTimeFunction(SIMUNIT, "ConstantVariate", args.genPeriod), 0);
	}
	
	/**
	 * Creates a new generator for the given element type, which will follow the specified flow. The generator will create the default number of elements
	 * @param et The element type to generate
	 * @param flow The flow to follow
	 * @return The new generator
	 */
	public TimeDrivenElementGenerator getDefGenerator(ElementType et, IInitializerFlow flow) {
		return getDefGenerator(args.nElements, et, flow);
	}
	
	/**
	 * Creates a new generator for the given element type, which will follow the specified flow. The generator will create the given number of elements
	 * @param elems The number of elements to generate
	 * @param et The element type to generate
	 * @param flow The flow to follow
	 * @return The new generator
	 */
	public TimeDrivenElementGenerator getDefGenerator(int elems, ElementType et, IInitializerFlow flow) {
		nElems.set(et.getIdentifier(), nElems.get(et.getIdentifier()) + elems);
        return new TimeDrivenElementGenerator(this, elems, et, flow, getGeneratorCycle());
	}
	
	public void registerActivity(ITaskFlow act, long duration) {
		actIndex.put(act, actDuration.size());
		actDuration.add(duration);
	}

	public void registerRoleOn(int resId, int rtId, long[] roleOnTimestamps) {
		roleOns.add(new ResourceUsageTimeStamps(resId, rtId, roleOnTimestamps));
	}

	public void registerRoleOff(int resId, int rtId, long[] roleOffTimestamps) {
		roleOffs.add(new ResourceUsageTimeStamps(resId, rtId, roleOffTimestamps));
	}

	public void run() {
		run(SIMSTART, args.simEnd);
	}

	/**
	 * A test activity flow that will be used to check the simulation. It simply defines a unique work group and duration.
	 */
	public class TestActivityFlow extends ActivityFlow {

		public TestActivityFlow(String description, long duration, WorkGroup wg, boolean exclusive) {
			super(StandardTestSimulation.this, description, exclusive, false);
	    	newWorkGroupAdder(wg).withDelay(duration).add();
			registerActivity(this, duration);
		}		
	}

	public static class TestArguments extends CommonArguments {
		@Parameter(names = {"-Trs", "--TresStart"}, description = "The start time for resources (minutes)", order = 1)
		public long resStart = 8 * 60;
		@Parameter(names = {"-Trp", "--TresPeriod"}, description = "The period for resources (minutes)", order = 2)
		public long resPeriod = 24 * 60;
		@Parameter(names = {"-Tra", "--TresAvailability"}, description = "The availability for resources (minutes)", order = 3)
		public long resAvailability = 7 * 60;
		@Parameter(names = {"-Tad", "--TactDuration"}, description = "The duration for activities (minutes)", order = 4)
		public long actDuration = 5;
		@Parameter(names = {"-Tse", "--TsimEnd"}, description = "The end time for the simulation (minutes)", order = 5)
		public long simEnd = 1440;
		@Parameter(names = {"-Tgs", "--TgenStart"}, description = "The start time for the generator of elements (minutes)", order = 6)
		public long genStart = 0;
		@Parameter(names = {"-Tgp", "--TgenPeriod"}, description = "The period for the generator of elements (minutes)", order = 7)
		public long genPeriod = 1440;
		@Parameter(names = {"-Te", "--TnElements"}, description = "The number of elements to generate per element type", order = 8)
		public int nElements = 3;
	}
}
