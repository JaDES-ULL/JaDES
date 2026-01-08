/**
 * 
 */
package es.ull.performance;

import es.ull.simulation.experiment.BaseExperiment;
import es.ull.simulation.experiment.CommonArguments;
import es.ull.simulation.factory.SimulationFactory;
import es.ull.simulation.model.Simulation;

/**
 * @author Iván Castilla Rodríguez
 *
 */
public class TestPerformance {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final CommonArguments arguments = new CommonArguments();

		new BaseExperiment("EXP", arguments) {

			@Override
			public void runExperiment(int ind) {
				SimulationFactory factory = new SimulationFactory(ind, "SimTest");
				Simulation sim = factory.getSimulation();
				int i = 0;
				try {
					for (; ; i++)
						factory.getResourceTypeInstance("RT" + i);
				} catch(OutOfMemoryError e) {
					System.out.println("Not enough memory with " + i + " res. types");
				} 
//				try {
//					for (; ; i++)
//						factory.getTimeDrivenActivityInstance(i, "ACT" + i);
//				} catch(OutOfMemoryError e) {
//					System.out.println("Not enough memory with " + i + " activities");
//				}
				
				sim.run(0L);
			}			
		}.run();

	}

}
