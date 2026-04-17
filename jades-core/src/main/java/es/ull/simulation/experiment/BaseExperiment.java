package es.ull.simulation.experiment;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;

/**
 * A class to execute several simulation experiments sequentially. It uses a single thread to execute the simuation experiments.
 */
public abstract class BaseExperiment implements IExperiment {
	/**
	 * The logger for this class
	 */
	private final static Logger log = org.slf4j.LoggerFactory.getLogger(BaseExperiment.class); 
	
	/** A short text describing this experiment */
	private final String description;
	/** The arguments for the experiment */
	private final IExperimentConfigurationProvider configProvider;
	/** A structure to print the progress of simulations */
	private final PrintProgress progress;
	/** The number of experiments to be carried out */
	private final int nExperiments;

	/**
	 * Creates a new experiment.
	 * @param description A short text describing this experiment
	 */
    public BaseExperiment(String description, IExperimentConfigurationProvider config) {
        super();
        this.description = description;
		this.configProvider = config;
		this.nExperiments = (config.getNRuns().isPresent()) ? config.getNRuns().getAsInt() : IExperiment.DEFAULT_RUNS;
		this.progress = new PrintProgress(nExperiments + 1);
		IExperiment.setSeed(config.getSeed().isPresent() ? config.getSeed().getAsLong() : IExperiment.getSeed());
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
		return nExperiments;
	}

	/**
	 * Returns the arguments for the experiment.
	 * @return The arguments for the experiment
	 */
	public IExperimentConfigurationProvider getConfigProvider() {
		return configProvider;
	}

	@Override
    public void run() {
		final long time = System.currentTimeMillis();
        beforeStart();
		progress.print();
		if (nExperiments > 0) {
			if (configProvider.isParallel().orElse(false)) {
				final int nThreads = configProvider.getNThreads().isPresent() ? configProvider.getNThreads().getAsInt()
						: IExperiment.DEFAULT_N_THREADS;
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
		log.info("Execution time: {} sec", (System.currentTimeMillis() - time) / 1000);
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
			this.gap = (nExperiments > N_PROGRESS) ? nExperiments / N_PROGRESS : 1;
			this.counter = new AtomicInteger();
		}

		public void print() {
			if (counter.incrementAndGet() % gap == 0)
				log.info("{}% finished", (counter.get() * 100 / totalSim));
		}

	}
	
    
}
