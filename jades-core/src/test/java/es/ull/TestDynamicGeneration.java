/**
 * 
 */
package es.ull;

import es.ull.simulation.condition.AbstractCondition;
import es.ull.simulation.condition.NotCondition;
import es.ull.simulation.experiment.BaseExperiment;
import es.ull.simulation.experiment.CommonArguments;
import es.ull.simulation.factory.SimulationFactory;
import es.ull.simulation.model.ElementInstance;
import es.ull.simulation.model.Resource;
import es.ull.simulation.model.ResourceType;
import es.ull.simulation.model.Simulation;
import es.ull.simulation.model.SimulationPeriodicCycle;
import es.ull.simulation.model.TimeUnit;
import es.ull.simulation.model.WorkGroup;
import es.ull.simulation.model.flow.ActivityFlow;

class TestDynamicGenerationExperiment extends BaseExperiment {
	
	public TestDynamicGenerationExperiment(CommonArguments arguments) {
		super("Test Dynamic Generation", arguments);
	}

	@Override
	public void runExperiment(int ind) {
		TimeUnit unit = Simulation.DEF_TIME_UNIT;
		SimulationFactory factory = new SimulationFactory(ind, "Test Dynamic");
		
		ResourceType rt0 = factory.getResourceTypeInstance("RT0");
		ResourceType rt1 = factory.getResourceTypeInstance("RT1");
		
		Resource r0 =  factory.getResourceInstance("Res0");
		r0.newTimeTableOrCancelEntriesAdder(rt0).withDuration(SimulationPeriodicCycle.newDailyCycle(unit),
				1).addTimeTableEntry();
		Resource r1 = factory.getResourceInstance("Res1");
		r1.newTimeTableOrCancelEntriesAdder(rt1).withDuration(SimulationPeriodicCycle.newDailyCycle(unit),
				1).addTimeTableEntry();
		
		WorkGroup wg0 = factory.getWorkGroupInstance(new ResourceType [] {rt0, rt1}, new int[] {1,1});
		
		AbstractCondition<ElementInstance> cond = factory.getCustomizedConditionInstance(null, "false");
		ActivityFlow act0 = (ActivityFlow)factory.getFlowInstance("ActivityFlow", "ACT0");
    	act0.newWorkGroupAdder(wg0).withDelay(10).withCondition(new NotCondition<ElementInstance>(cond)).add();
		
		factory.getElementTypeInstance("ET0");
		factory.getFlowInstance("SingleFlow", act0);
		factory.getSimulation().run(0, 1);
	}
}

/**
 * @author Iván Castilla Rodríguez
 *
 */
public class TestDynamicGeneration {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
        final CommonArguments arguments = new CommonArguments();

		new TestDynamicGenerationExperiment(arguments).run();

	}

}
