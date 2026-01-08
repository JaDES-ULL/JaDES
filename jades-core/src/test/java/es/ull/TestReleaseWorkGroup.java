/**
 * 
 */
package es.ull;

import es.ull.simulation.experiment.BaseExperiment;
import es.ull.simulation.experiment.CommonArguments;
import es.ull.simulation.inforeceiver.StdInfoListener;
import es.ull.simulation.model.ElementType;
import es.ull.simulation.model.ResourceType;
import es.ull.simulation.model.Simulation;
import es.ull.simulation.model.SimulationPeriodicCycle;
import es.ull.simulation.model.TimeDrivenElementGenerator;
import es.ull.simulation.model.TimeUnit;
import es.ull.simulation.model.WorkGroup;
import es.ull.simulation.model.flow.ReleaseResourcesFlow;
import es.ull.simulation.model.flow.RequestResourcesFlow;

/**
 * @author Iván Castilla Rodríguez
 *
 */
public class TestReleaseWorkGroup extends BaseExperiment {
	final static TimeUnit UNIT = Simulation.DEF_TIME_UNIT;
	final static long END_TIME = 100;

	class ModelReleaseManagement extends Simulation {
		public ModelReleaseManagement (int ind) {
			super(ind, "Testing resource management " + ind);
			
			// The only element type
			final ElementType et = new ElementType(this, "Package");
			
			// The three resource types involved in the simulation
			final ResourceType rtOperatorA = new ResourceType(this, "OperatorA");
			final ResourceType rtMachine = new ResourceType(this, "Machine");
			final ResourceType rtLocationA = new ResourceType(this, "LocationA");

			// Create the specific resources
			rtOperatorA.addGenericResources(2);
			rtMachine.addGenericResources(2);
			rtLocationA.addGenericResources(1);
			
			// Define the workgroups
			final WorkGroup wgLocationA = new WorkGroup(this, new ResourceType[] {rtLocationA,
					rtOperatorA, rtMachine}, new int[] {1,2,2});
			final WorkGroup wgRelLocationA1 = new WorkGroup(this, new ResourceType[] {rtLocationA,
					rtMachine}, new int[] {1,1});
			final WorkGroup wgRelLocationA2 = new WorkGroup(this, new ResourceType[] {rtMachine,
					rtOperatorA}, new int[] {1,2});

			// Create basic steps of the IFlow
			final RequestResourcesFlow reqLocationA = new RequestResourcesFlow(this,
					"Request location A", 0);
			final ReleaseResourcesFlow relLocationA1 = new ReleaseResourcesFlow(this,
					"Release location A 1", 0, wgRelLocationA1);
			final ReleaseResourcesFlow relLocationA2 = new ReleaseResourcesFlow(this,
					"Release location A 2", 0, wgRelLocationA2);
			
			// Assign duration and workgroups to activities
			reqLocationA.newWorkGroupAdder(wgLocationA).add();

			// Create IFlow
			reqLocationA.link(relLocationA1).link(relLocationA2);
			SimulationPeriodicCycle cycle = SimulationPeriodicCycle.newDailyCycle(UNIT, 0);
			new TimeDrivenElementGenerator(this, 1, et, reqLocationA, cycle);
		}
	}
	
	/**
	 * 
	 */
	public TestReleaseWorkGroup(CommonArguments arguments) {
		super("Testing resource management", arguments);
	}

	@Override
	public void runExperiment(int ind) {
		final Simulation simul = new ModelReleaseManagement(ind);
		simul.registerListener(new StdInfoListener());
		simul.run(0, END_TIME);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
        final CommonArguments arguments = new CommonArguments();
		new TestReleaseWorkGroup(arguments).run();

	}

}
