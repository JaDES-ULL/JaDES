package es.ull.simulation.experiment;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A class to execute several simulation experiments sequentially. It uses a single thread to execute the simuation experiments.
 */
public abstract class BaseExperiment implements IExperiment {
	/** A short text describing this experiment */
	private final String description;
	/** The arguments for the experiment */
	private final CommonArguments arguments;
	/** A structure to print the progress of simulations */
	private final PrintProgress progress;

	/**
	 * Creates a new experiment.
	 * @param description A short text describing this experiment
	 */
    public BaseExperiment(String description, CommonArguments arguments) {
        super();
        this.description = description;
		this.arguments = arguments;
		this.progress = new PrintProgress(arguments.nRuns + 1);
		if (arguments.seed != IExperiment.getSeed()) {
			IExperiment.setSeed(arguments.seed);
		}
    }

	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the number of experiments to be carried out.
	 * @return The number of experiments to be carried out
	 */
	public int getNExperiments() {
		return arguments.nRuns;
	}

	/**
	 * Returns the arguments for the experiment.
	 * @return The arguments for the experiment
	 */
	public CommonArguments getArguments() {
		return arguments;
	}

	@Override
    public void run() {
		final long time = System.currentTimeMillis();
        beforeStart();
		final int nExperiments = getNExperiments();
		progress.print();
		if (nExperiments > 0) {
			if (arguments.parallel) {
				final int nThreads = arguments.nThreads;
				try {
					final Thread[] workers = new Thread[nThreads];
					int nExperimentsPerThread = nExperiments / nThreads;
					for (int nTh = 0; nTh < nThreads; nTh++) {
						workers[nTh] = new Thread(new ParallelExperimentsLauncher(nExperimentsPerThread * nTh,
								Math.min(nExperiments, nExperimentsPerThread * (nTh + 1)) - 1));
						workers[nTh].start();
					}
					for (int nTh = 0; nTh < nThreads; nTh++) {
						workers[nTh].join();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				}
			else {
				for (int i = 0; i < nExperiments; i++) {
					runExperiment(i);
				}
			}
		}
        afterFinalize();
		if (!arguments.quiet)
			System.out.println("Execution time: " + ((System.currentTimeMillis() - time) / 1000) + " sec");
    }
	
	protected class ParallelExperimentsLauncher implements Runnable {
        /** The index of the first experiment executed in this launcher */
		private final int firstIndex;
        /** The index of the last experiment executed in this launcher */
		private final int lastIndex;

		
		public ParallelExperimentsLauncher(int firstIndex, int lastIndex) {
			this.firstIndex = firstIndex;
			this.lastIndex = lastIndex;
		}

		@Override
		public void run() {
			for (int sim = firstIndex; sim <= lastIndex; sim++) {
				runExperiment(sim);
				progress.print();
			}
		}
		
	}

	/**
	 * A class to print the progression of the simulations
	 * 
	 * @author Iván Castilla Rodríguez
	 *
	 */
	protected class PrintProgress {
		/** How many replications have to be run to show a new progression percentage message */
		private static final int N_PROGRESS = 20;
		final private int totalSim;
		final private int gap;
		private final AtomicInteger counter;

		public PrintProgress(int totalSim) {
			this.totalSim = totalSim;
			this.gap = (arguments.nRuns > N_PROGRESS) ? arguments.nRuns / N_PROGRESS : 1;
			this.counter = new AtomicInteger();
		}

		public void print() {
			if (!arguments.quiet) {
				if (counter.incrementAndGet() % gap == 0)
					System.out.println("" + (counter.get() * 100 / totalSim) + "% finished");
			}
		}

	}
	
    
}
