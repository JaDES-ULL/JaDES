/**
 * 
 */
package es.ull;

import es.ull.simulation.model.ElementType;

import es.ull.simulation.model.ElementInstance;
import es.ull.simulation.model.Resource;
import es.ull.simulation.model.ResourceType;
import es.ull.simulation.model.WorkGroup;
import es.ull.simulation.model.flow.ActivityFlow;
import es.ull.simulation.model.flow.ParallelFlow;

/**
 * Checks the conditional generation of resources. A special type of ActivityFlow is used to generate a resource after such activity finishes.
 * @author Iván Castilla Rodríguez
 *
 */
public class TestConditionalResourceGeneratorSimulation extends StandardTestSimulation {
	private final static long DURATION = 5L;

	class SpecialActivityFlow extends TestActivityFlow {
		final private ResourceType rt1;
		
		public SpecialActivityFlow(String description, WorkGroup wg, ResourceType rt1) {
			super(description, DURATION, wg, true);
			this.rt1 = rt1;
		}
		
		@Override
		public void afterFinalize(ElementInstance fe) {
			// Here it is created the resource
			final Resource res = new Resource(simul, "Container later 1");
			res.newTimeTableOrCancelEntriesAdder(rt1).addTimeTableEntry();
			simul.addEvent(res.onCreate(simul.getTs()));
		}
	}

	/**
	 * @param description
	 * @param nExperiments
	 */
	public TestConditionalResourceGeneratorSimulation(TestArguments arguments) {
		super(0, "Testing conditional generation of resources", arguments);
	}

	@Override
	protected void createModel() {
		final ElementType et = getDefElementType("Crane");

		final ParallelFlow pf = new ParallelFlow(this);

		final ResourceType rt0 = getDefResourceType("Container existing");
		final ResourceType rt1 = getDefResourceType("Container arriving later");
		final WorkGroup wg0 = new WorkGroup(this, rt0, 1);
		final ActivityFlow req0 = new SpecialActivityFlow("Req 0", wg0, rt1);
		final WorkGroup wg1 = new WorkGroup(this, rt1, 1);
		final ActivityFlow req1 = getDefActivity("Req 1", DURATION, wg1);

		pf.link(req0);
		pf.link(req1);

		// Only the first resource is available from the beginning
		getDefResource("Container 0", rt0);
		
		getDefGenerator(et, pf);
	}

	@Override
	protected void addCheckers() {
		registerRoleOn(1, 1, new long[] {DURATION});
		super.addCheckers();
	}
/*	private void setListeners(ActivityFlow req0, ActivityFlow req1) {
		// Prepare structures to check behavior of resources
		final ArrayList<ResourceUsageTimeStamps> roleOns = new ArrayList<>();
		roleOns.add(new ResourceUsageTimeStamps(0, 0, new long[] {0L}));
		roleOns.add(new ResourceUsageTimeStamps(1, 1, new long[] {DURATION}));
		final ArrayList<ResourceUsageTimeStamps> roleOffs = new ArrayList<>();
		addInfoReceiver(new CheckResourcesListener(2, roleOns, roleOffs));
	}
*/
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestArguments arguments = new TestArguments();
        arguments.simEnd = 60;
        arguments.nElements = 1;
        arguments.resStart = 0;
		new TestConditionalResourceGeneratorSimulation(arguments).run();
	}

}
