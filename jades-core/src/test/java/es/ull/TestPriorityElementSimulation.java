/**
 * 
 */
package es.ull;

import es.ull.simulation.model.ElementType;
import es.ull.simulation.model.ResourceType;
import es.ull.simulation.model.WorkGroup;
import es.ull.simulation.model.flow.ActivityFlow;
import es.ull.simulation.model.flow.ParallelFlow;

/**
 * A test for elements with different priorities.
 * @author Iván Castilla Rodríguez
 *
 */
public class TestPriorityElementSimulation extends StandardTestSimulation {
	static private final int NACT = 4;
	static private final int NELEMT = 2;
	static private final int NRES = 10;

	public TestPriorityElementSimulation(TestArguments arguments) {
		super(0, "Testing Elements with priority", arguments);
	}

	@Override
	protected void createModel() {
		ResourceType rt = getDefResourceType("RT0");
		WorkGroup wg = new WorkGroup(this, new ResourceType[] {rt}, new int[] {2});
		ActivityFlow acts[] = new ActivityFlow[NACT];
		for (int i = 0; i < NACT; i++) {
			acts[i] = new ActivityFlow(this, "ACT" + i, i / 2, false, false);
			acts[i].newWorkGroupAdder(wg).withDelay(10).add();
			registerActivity(acts[i], 10);
		}
		ParallelFlow meta = new ParallelFlow(this);
		for (int i = 0; i < NACT; i++) {
			meta.link(acts[i]);
		}
		
		for (int i = 0; i < NRES; i++) {
			getDefResource("RES" + i, rt);
		}

		for (int i = 0; i < NELEMT; i++) {
			ElementType et = getDefElementType("ET" + i, i);
			getDefGenerator(et, meta);
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
        final TestArguments arguments = new TestArguments();
		arguments.simEnd = 400;
		arguments.nElements = 10;
		arguments.resStart = 20;
		arguments.resPeriod = 100;
		arguments.resAvailability = 40;
		TestPriorityElementSimulation sim = new TestPriorityElementSimulation(arguments);
		sim.run();
	}

}
