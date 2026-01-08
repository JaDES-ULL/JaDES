/**
 * 
 */
package es.ull;

import es.ull.simulation.model.ElementType;
import es.ull.simulation.model.ResourceType;
import es.ull.simulation.model.WorkGroup;
import es.ull.simulation.model.flow.ActivityFlow;

/**
 * Checks interruptible activities. Activities are interrupted when a resource stops being available before the activity has finalized.
 * The test involves a single activity that lasts more than the available time of a resource. The resource is available cyclically, so
 * the activity resumes every time the resource is available again. 
 * @author Iván Castilla Rodríguez
 *
 */
public class TestInterruptibleActivitiesSimulation extends StandardTestSimulation {
	static private final long ACT_DURATION = 101;


	public TestInterruptibleActivitiesSimulation(TestArguments args) {
		super(0, "Testing interruptible activities", args);
	}

	@Override
	protected void createModel() {
		final ResourceType rt = getDefResourceType("RT0");
		final WorkGroup wg = new WorkGroup(this, new ResourceType[] {rt}, new int[] {1});

		final ActivityFlow act = new ActivityFlow(this, "ACT0", 0, false, true);
		act.newWorkGroupAdder(wg).withDelay(ACT_DURATION).add();
		registerActivity(act, ACT_DURATION);
		getDefResource("RES0", rt);
		final ElementType et = getDefElementType("ET0");
		getDefGenerator(et, act);
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestArguments targs = new TestArguments();
		targs.simEnd = 400L;
		targs.nElements = 1;
		targs.resStart = 20L;
		targs.resPeriod	= 100L;
		targs.resAvailability = 40L;
		StandardTestSimulation sim = new TestInterruptibleActivitiesSimulation(targs);
		sim.run();
	}

}
