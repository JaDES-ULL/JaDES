package es.ull;

import java.util.ArrayList;

import es.ull.simulation.functions.TimeFunctionFactory;
import es.ull.simulation.experiment.BaseExperiment;
import es.ull.simulation.experiment.CommonArguments;
import es.ull.simulation.factory.SimulationFactory;
import es.ull.simulation.inforeceiver.StdInfoListener;
import es.ull.simulation.model.Resource;
import es.ull.simulation.model.ResourceType;
import es.ull.simulation.model.Simulation;
import es.ull.simulation.model.SimulationPeriodicCycle;
import es.ull.simulation.model.SimulationTimeFunction;
import es.ull.simulation.model.TimeStamp;
import es.ull.simulation.model.TimeUnit;
import es.ull.simulation.model.WorkGroup;
import es.ull.simulation.model.flow.ActivityFlow;
import es.ull.simulation.model.flow.ParallelFlow;

/**
 * 
 */
class ExpOverlapped extends BaseExperiment {
	final static TimeUnit unit = Simulation.DEF_TIME_UNIT;
    static final int NDAYS = 1;
	final static int NELEM = 3;
	final static int NRESOURCES = 2;
	final static int NEEDED = 1;

	public ExpOverlapped(CommonArguments arguments) {
		super("Overlapped", arguments);
	}

	public void runExperiment(int ind) {
		SimulationFactory factory = new SimulationFactory(ind, "Sistema de análisis");
		Simulation sim = factory.getSimulation();

        // PASO 1: Inicializo las Activityes de las que se compone
//    	Activity actDummy = factory.getActivityInstance("Dummy");
    	ActivityFlow actSangre = (ActivityFlow)factory.getFlowInstance("ActivityFlow",
				"Análisis de sangre");
    	ActivityFlow actOrina = (ActivityFlow)factory.getFlowInstance("ActivityFlow",
				"Análisis de orina");
 
        // PASO 2: Inicializo las clases de recursos
        ResourceType crSangre = factory.getResourceTypeInstance("Máquina Análisis Sangre");
        ResourceType crOrina = factory.getResourceTypeInstance("Máquina Análisis Orina");
//        ResourceType crDummy = factory.getResourceTypeInstance("Dummy");

        // PASO 3: Creo las tablas de clases de recursos
        WorkGroup wg1 = factory.getWorkGroupInstance(new ResourceType[] {crSangre}, new int[] {NEEDED});
    	actSangre.newWorkGroupAdder(wg1).withDelay(new SimulationTimeFunction(unit, "NormalVariate",
				20, 5)).add();
//        wg1.add(crOrina, 1);
        WorkGroup wg2 = factory.getWorkGroupInstance(new ResourceType[] {crOrina}, new int[] {1});
    	actOrina.newWorkGroupAdder(wg2).withDelay(new SimulationTimeFunction(unit, "NormalVariate",
				20, 5)).add();
//        WorkGroup wg3 = factory.getWorkGroupInstance(new ResourceType[] {crDummy}, new int[] {1});
//        actDummy.addWorkGroup(new ModelTimeFunction(unit, "NormalVariate", 10, 2), wg3);

//		ArrayList<ResourceType> al1 = new ArrayList<ResourceType>();
//		al1.add(getResourceType(0));
//		al1.add(getResourceType(2));
//        for (int i = 0; i < NRESOURCES; i++) {
//        	Resource res = factory.getResourceInstance(i, "Máquina Análisis Sangre " + i);
//        	res.addTimeTableEntry(new ModelPeriodicCycle(unit, 480, RandomVariateFactory.getInstance("ConstantVariate",
//        	1440), 0), 480, al1);
//        }
//		ArrayList<ResourceType> al2 = new ArrayList<ResourceType>();
//		al2.add(crOrina);
//		al2.add(crDummy);
//		Resource orina1 = factory.getResourceInstance("Máquina Análisis Orina 1");
//		orina1.addTimeTableEntry(new ModelPeriodicCycle(unit, 480, RandomVariateFactory.getInstance("ConstantVariate",
//		1440), 0), 480, al2);

		ArrayList<ResourceType> al2 = new ArrayList<ResourceType>();
		al2.add(crOrina);
		al2.add(crSangre);
        for (int i = 0; i < NRESOURCES; i++) {
			Resource poli1 = factory.getResourceInstance("Máquina Polivalente 1");
			poli1.newTimeTableOrCancelEntriesAdder(al2).withDuration(new SimulationPeriodicCycle(unit, 480,
					new SimulationTimeFunction(
							unit, "ConstantVariate", 1440), 0), 480)
					.addTimeTableEntry();
        }
        
		ParallelFlow metaFlow = (ParallelFlow)factory.getFlowInstance("ParallelFlow");
		metaFlow.link(actOrina);
		metaFlow.link(actSangre);
		
//		SingleFlow metaFlow = (SingleFlow)factory.getFlowInstance(RandomVariateFactory.getInstance(
//		"ConstantVariate", 1), actDummy);
		SimulationPeriodicCycle c = new SimulationPeriodicCycle(unit, 0, new SimulationTimeFunction(
				unit, "ConstantVariate", 1440), NDAYS);
		factory.getTimeDrivenElementGeneratorInstance(TimeFunctionFactory.getInstance(
				"ConstantVariate", NELEM),
						factory.getElementTypeInstance("ET0"), metaFlow, c);
		
		sim.registerListener(new StdInfoListener());
		sim.run(TimeStamp.getZero(), new TimeStamp(TimeUnit.DAY, NDAYS));
	}
}

public class TestOverlapped {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
        final CommonArguments arguments = new CommonArguments();
		new ExpOverlapped(arguments).run();
	}

}
