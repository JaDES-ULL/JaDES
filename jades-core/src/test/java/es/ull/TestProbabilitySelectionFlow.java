package es.ull;
import es.ull.simulation.functions.TimeFunctionFactory;
import es.ull.simulation.experiment.BaseExperiment;
import es.ull.simulation.experiment.CommonArguments;
import es.ull.simulation.factory.SimulationFactory;
import es.ull.simulation.inforeceiver.StdInfoListener;
import es.ull.simulation.model.ElementType;
import es.ull.simulation.model.ResourceType;
import es.ull.simulation.model.Simulation;
import es.ull.simulation.model.SimulationPeriodicCycle;
import es.ull.simulation.model.SimulationTimeFunction;
import es.ull.simulation.model.TimeStamp;
import es.ull.simulation.model.TimeUnit;
import es.ull.simulation.model.WorkGroup;
import es.ull.simulation.model.flow.ActivityFlow;
import es.ull.simulation.model.flow.ProbabilitySelectionFlow;

/**
 * 
 */
class ExperimentProbSel extends BaseExperiment {
    static final int NDAYS = 1;
	
	public ExperimentProbSel(CommonArguments arguments) {
		super("Banco", arguments);
	}

	public void runExperiment(int ind) {
		Simulation sim = null;
		TimeUnit unit = TimeUnit.MINUTE;
		SimulationFactory factory = new SimulationFactory(ind, "EjProbabilidades");
		sim = factory.getSimulation();

    	ActivityFlow act0 = (ActivityFlow)factory.getFlowInstance("ActivityFlow", "10 %", 0,
				false, false);
    	ActivityFlow act1 = (ActivityFlow)factory.getFlowInstance("ActivityFlow", "30 %", 0,
				false, false);
    	ActivityFlow act2 = (ActivityFlow)factory.getFlowInstance("ActivityFlow", "60 %", 0,
				false, false);
        ResourceType rt = factory.getResourceTypeInstance("Empleado");
        
        WorkGroup wg = factory.getWorkGroupInstance(new ResourceType[] {rt}, new int[] {1});

        act0.newWorkGroupAdder(wg).withDelay(new SimulationTimeFunction(unit, "NormalVariate",
				15, 2)).add();
        act1.newWorkGroupAdder(wg).withDelay(new SimulationTimeFunction(unit, "NormalVariate",
				15, 2)).add();
        act2.newWorkGroupAdder(wg).withDelay(new SimulationTimeFunction(unit, "NormalVariate",
				15, 2)).add();
   
        SimulationPeriodicCycle subc2 = new SimulationPeriodicCycle(unit, 480, new SimulationTimeFunction(
				unit, "ConstantVariate", 1040), 5);
        SimulationPeriodicCycle c2 = new SimulationPeriodicCycle(unit, 0, new SimulationTimeFunction(
				unit, "ConstantVariate", 1040 * 7), 0, subc2);

        factory.getResourceInstance("Empleado1").newTimeTableOrCancelEntriesAdder(rt).withDuration(
				c2, 420).addTimeTableEntry();
        factory.getResourceInstance("Empleado2").newTimeTableOrCancelEntriesAdder(rt).withDuration(
				c2, 420).addTimeTableEntry();
        factory.getResourceInstance("Empleado3").newTimeTableOrCancelEntriesAdder(rt).withDuration(
				c2, 420).addTimeTableEntry();
        

        ProbabilitySelectionFlow root = (ProbabilitySelectionFlow)factory.getFlowInstance(
				"ProbabilitySelectionFlow");
        
        root.link(act0, 0.1);
        root.link(act1, 0.3);
        root.link(act2, 0.6);
        
        ElementType et = factory.getElementTypeInstance("Cliente");
        SimulationPeriodicCycle cGen = new SimulationPeriodicCycle(unit, 0, new SimulationTimeFunction(
				unit, "ConstantVariate", 1040), NDAYS);
        factory.getTimeDrivenElementGeneratorInstance(TimeFunctionFactory.getInstance(
				"ConstantVariate", 100), et, root, cGen);
		
		StdInfoListener debugView = new StdInfoListener();
		sim.registerListener(debugView);
		sim.run(TimeStamp.getZero(), new TimeStamp(TimeUnit.DAY, NDAYS));
	}
	

}


public class TestProbabilitySelectionFlow {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
        final CommonArguments arguments = new CommonArguments();
		new ExperimentProbSel(arguments).run();
	}

}
