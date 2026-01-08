package es.ull;

import es.ull.simulation.functions.TimeFunctionFactory;
import es.ull.simulation.experiment.BaseExperiment;
import es.ull.simulation.experiment.CommonArguments;
import es.ull.simulation.factory.SimulationFactory;
import es.ull.simulation.model.Resource;
import es.ull.simulation.model.ResourceType;
import es.ull.simulation.model.Simulation;
import es.ull.simulation.model.ISimulationCycle;
import es.ull.simulation.model.SimulationPeriodicCycle;
import es.ull.simulation.model.SimulationTimeFunction;
import es.ull.simulation.model.TimeStamp;
import es.ull.simulation.model.TimeUnit;
import es.ull.simulation.model.WorkGroup;
import es.ull.simulation.model.flow.ActivityFlow;
import es.ull.simulation.model.flow.IInitializerFlow;

class ExpConflict extends BaseExperiment {
    static final int NDAYS = 1;
	static final TimeUnit unit = Simulation.DEF_TIME_UNIT;
    
    ExpConflict(CommonArguments arguments) {
    	super("CHECKING CONFLICTS", arguments);
    }
    
    /**
     * Defines a model:
     * - A0 {RT0:1, RT1:1}; A1 {RT3:1, RT2:1}
     * - R0 {RT0, RT2}; R1 {RT3, RT1}
     * - E0 {A0}; E1 {A1} 
     */
    private void createSimulation1(SimulationFactory factory) {
    	final int NRT = 4;
    	final int NACTS = 2;
    	final int NELEM = 1;
    	
    	ResourceType [] rts = new ResourceType[NRT];
		for (int i = 0; i < NRT; i++)
			rts[i] = factory.getResourceTypeInstance("RT" + i);
		
		WorkGroup wgs[] = new WorkGroup[NACTS];
		wgs[0] = factory.getWorkGroupInstance(new ResourceType[] {rts[0], rts[1]}, new int[] {1, 1});
		wgs[1] = factory.getWorkGroupInstance(new ResourceType[] {rts[3], rts[2]}, new int[] {1, 1});
		
		ActivityFlow acts[] = new ActivityFlow[NACTS];
		for (int i = 0; i < NACTS; i++) {
			acts[i] = (ActivityFlow)factory.getFlowInstance("ActivityFlow", "ACT" + i);
	    	acts[i].newWorkGroupAdder(wgs[i]).withDelay(40).add();
		}
		
		ISimulationCycle c = SimulationPeriodicCycle.newDailyCycle(unit);
		
		Resource r0 = factory.getResourceInstance("Res0");
		Resource r1 = factory.getResourceInstance("Res1");
		r0.newTimeTableOrCancelEntriesAdder(rts[0]).withDuration(c, 480).addTimeTableEntry();
		r0.newTimeTableOrCancelEntriesAdder(rts[2]).withDuration(c, 480).addTimeTableEntry();
		r1.newTimeTableOrCancelEntriesAdder(rts[3]).withDuration(c, 480).addTimeTableEntry();
		r1.newTimeTableOrCancelEntriesAdder(rts[1]).withDuration(c, 480).addTimeTableEntry();


		ISimulationCycle c1 = new SimulationPeriodicCycle(unit, new TimeStamp(TimeUnit.MINUTE, 1),
				new SimulationTimeFunction(unit, "ConstantVariate", 1440),
				new TimeStamp(TimeUnit.MINUTE, 480));
		factory.getTimeDrivenElementGeneratorInstance(TimeFunctionFactory.getInstance(
				"ConstantVariate", NELEM),
						factory.getElementTypeInstance("ET0"), (IInitializerFlow)factory.getFlowInstance(
								"SingleFlow", acts[0]), c1);
		factory.getTimeDrivenElementGeneratorInstance(TimeFunctionFactory.getInstance("ConstantVariate",
						NELEM),
						factory.getElementTypeInstance("ET1"), (IInitializerFlow)factory.getFlowInstance(
								"SingleFlow", acts[1]), c1);
    }
    
    /**
     * Defines a model:
     * - A0 {RT0:1, RT1:1, RT4:1}; A1 {RT3:1, RT2:1}; A2 {RT5:1}
     * - R0 {RT0, RT2}; R1 {RT3, RT1}; R2 {RT5, RT4}
     * - E0 {A0}; E1 {A1}; E2 {A2} 
     */
    @SuppressWarnings("unused")
	private void createSimulation2(SimulationFactory factory) {
    	final int NRT = 6;
    	final int NACTS = 3;
    	final int NELEM = 1;
    	
    	ResourceType [] rts = new ResourceType[NRT];
		for (int i = 0; i < NRT; i++)
			rts[i] = factory.getResourceTypeInstance("RT" + i);
		WorkGroup wgs[] = new WorkGroup[NACTS];
		wgs[0] = factory.getWorkGroupInstance(new ResourceType[] {rts[0], rts[1], rts[4]}, new int[] {1, 1, 1});
		wgs[1] = factory.getWorkGroupInstance(new ResourceType[] {rts[3], rts[2]}, new int[] {1, 1});
		wgs[2] = factory.getWorkGroupInstance(new ResourceType[] {rts[5]}, new int[] {1});

		ActivityFlow acts[] = new ActivityFlow[NACTS];
		for (int i = 0; i < NACTS; i++) {
			acts[i] = (ActivityFlow)factory.getFlowInstance("ActivityFlow", "ACT" + i);
	    	acts[i].newWorkGroupAdder(wgs[i]).withDelay(40).add();
		}

		ISimulationCycle c = SimulationPeriodicCycle.newDailyCycle(unit);
		
		Resource r0 = factory.getResourceInstance("Res0");
		Resource r1 = factory.getResourceInstance("Res1");
		Resource r2 = factory.getResourceInstance("Res1");
		r0.newTimeTableOrCancelEntriesAdder(rts[0]).withDuration(c, 480).addTimeTableEntry();
		r0.newTimeTableOrCancelEntriesAdder(rts[2]).withDuration(c, 480).addTimeTableEntry();
		r1.newTimeTableOrCancelEntriesAdder(rts[3]).withDuration(c, 480).addTimeTableEntry();
		r1.newTimeTableOrCancelEntriesAdder(rts[1]).withDuration(c, 480).addTimeTableEntry();
		r2.newTimeTableOrCancelEntriesAdder(rts[4]).withDuration(c, 480).addTimeTableEntry();
		r2.newTimeTableOrCancelEntriesAdder(rts[5]).withDuration(c, 480).addTimeTableEntry();

		ISimulationCycle c1 = new SimulationPeriodicCycle(unit, new TimeStamp(TimeUnit.MINUTE, 1),
				new SimulationTimeFunction(unit, "ConstantVariate", 1440),
				new TimeStamp(TimeUnit.MINUTE, 480));
		factory.getTimeDrivenElementGeneratorInstance(TimeFunctionFactory.getInstance(
				"ConstantVariate", NELEM),
						factory.getElementTypeInstance("ET0"), (IInitializerFlow)factory.getFlowInstance(
								"SingleFlow", acts[0]), c1);
		factory.getTimeDrivenElementGeneratorInstance(TimeFunctionFactory.getInstance("ConstantVariate", NELEM), 
						factory.getElementTypeInstance("ET1"), (IInitializerFlow)factory.getFlowInstance(
								"SingleFlow", acts[1]), c1);
		factory.getTimeDrivenElementGeneratorInstance(TimeFunctionFactory.getInstance("ConstantVariate", NELEM), 
						factory.getElementTypeInstance("ET2"), (IInitializerFlow)factory.getFlowInstance(
								"SingleFlow", acts[2]), c1);
    }
    
	@Override
	public void runExperiment(int ind) {
		SimulationFactory factory = new SimulationFactory(ind, "TestConflicts");
		Simulation sim = factory.getSimulation();
		createSimulation1(factory);
		sim.run(TimeStamp.getZero(), new TimeStamp(TimeUnit.DAY, NDAYS));
	}	
}
/**
 * @author Iván Castilla Rodríguez
 *
 */
public class TestConflict {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
        final CommonArguments arguments = new CommonArguments();
		new ExpConflict(arguments).run();
	}

}
