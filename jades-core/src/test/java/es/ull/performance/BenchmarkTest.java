/**
 * 
 */
package es.ull.performance;

import java.io.PrintStream;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import es.ull.performance.BenchmarkModel.ModelType;
import es.ull.simulation.experiment.BaseExperiment;
import es.ull.simulation.experiment.CommonArguments;
import es.ull.simulation.model.Simulation;

/**
 * @author Iván Castilla Rodríguez
 *
 */
public class BenchmarkTest {
	static PrintStream out = System.out;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
        final BenchmarkArguments arguments = new BenchmarkArguments();
		final JCommander jc = JCommander.newBuilder().addObject(arguments).build();
		jc.parse(args);
		if (arguments.modType < 0 || arguments.modType >= BenchmarkModel.ModelType.values().length) {
			throw new ParameterException("Invalid model type. Must be a number >= 0 and < " + BenchmarkModel.ModelType.values().length);
		}
		if (arguments.ovType < 0 || arguments.ovType >= BenchmarkModel.OverlappingType.values().length) {
			throw new ParameterException("Invalid overlapping type. Must be a number >= 0 and < " + BenchmarkModel.OverlappingType.values().length);
		}
		// Force time horizon for certain models
		switch(ModelType.values()[arguments.modType]) {
			case PARALLEL: arguments.timeHorizon = arguments.nElem * arguments.nAct * (arguments.nIter + 1) + 1; break;
			case NORESOURCES:
			case RESOURCES: 
			case MIXCONFLICT: 
			case TOTALCONFLICT:
			case CONFLICT:
			default: arguments.timeHorizon = arguments.nElem * (arguments.nIter + 1) + 1; break;
		}				

		BaseExperiment exp = new BaseExperiment("Same Time", arguments) {
			long t1;

			@Override
			public void beforeStart() {
				t1 = System.nanoTime();
				super.beforeStart();
			}
			
			@Override
			public void afterFinalize() {
				super.afterFinalize();
				if (!arguments.quiet)
					System.out.println("TOTAL EXPERIMENT: " + ((System.nanoTime() - t1) / 1000000) + " miliseconds");
			}
			
			@Override
			public void runExperiment(int ind) {
				BenchmarkModel config = new BenchmarkModel(ind, arguments);
				System.out.println(config);
				Simulation sim = config.getTestModel(); 
				
				sim.registerListener(new BenchmarkListener(System.out));
				sim.run(arguments.timeHorizon);
			}
			
		};
		
		exp.run();		
	}
	
	public static class BenchmarkArguments extends CommonArguments {
		@Parameter(names = { "--Bmodel", "-Bm" }, description = "Model type", order = 1)
		public int modType = 0;
		@Parameter(names = { "--Boverlap", "-Bo" }, description = "Overlapping type", order = 2)
		public int ovType = 0;
		@Parameter(names = { "--Bnthreads", "-Bt" }, description = "Number of threads", order = 3)
		public int nThreads = 1;
		@Parameter(names = { "--Bniter", "-Bn" }, description = "Number of iterations", order = 4)
		public int nIter = 100;
		@Parameter(names = { "--Bnelem", "-Be" }, description = "Number of elements", order = 5)
		public int nElem = 512;
		@Parameter(names = { "--Bnact", "-Ba" }, description = "Number of activities", order = 6)
		public int nAct = 512;
		@Parameter(names = { "--Bmix", "-Bx" }, description = "Mix factor", order = 7)
		public int mixFactor = 2;
		@Parameter(names = { "--Bworkload", "-Bw" }, description = "Workload", order = 8)
		public long workLoad = 0;
		@Parameter(names = { "--BrtXAct", "-Bra" }, description = "Resource types per activity", order = 9)
		public int rtXAct = 4;
		@Parameter(names = { "--BrtXRes", "-Brr" }, description = "Resource types per resource", order = 10)
		public int rtXRes = 1;
		@Parameter(names = { "--BrAvailFactor", "-Bf" }, description = "Resource availability factor", order = 11)
		public double resAvailabilityFactor = 1.0;
	}
}
