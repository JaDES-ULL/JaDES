package es.ull.simulation.hta.inforeceiver;

import es.ull.simulation.hta.HTAExperiment;
import es.ull.simulation.hta.HTAModel;

public abstract class BasicListenerCollector implements ListenerCollector {
	/** The number of experiments to be analyzed */
	private final int nExperiments;
    /** A counter to know when all the experiments has been performed */
    private int experimentsPerformed;
	/** Whether we are collecting data from the base case or the PSA experiments */
	private final boolean baseCase;
	/** The model to be simulated */
	private final HTAModel model;
	/** The number of patients in the simulation */
	private final int nPatients;

    /** 
     * Creates a basic collector for a set of listeners attached to several simulations
     */
    public BasicListenerCollector(HTAExperiment exp, boolean baseCase) {
		this.baseCase = baseCase;
		this.nExperiments = baseCase ? 1: exp.getNExperiments();
		this.model = exp.getModel();
        this.nPatients = exp.getNPatients();
        this.experimentsPerformed = 0;
    }

    /**
     * Returns the number of experiments to be analyzed
     * @return the number of experiments to be analyzed
     */
    public int getNExperiments() {
        return nExperiments;
    }

    /**
     * Returns the model to be simulated
     * @return the model to be simulated
     */
    public HTAModel getModel() {
        return model;
    }

    /**
     * Returns the number of patients in the simulation
     * @return the number of patients in the simulation
     */
    public int getNPatients() {
        return nPatients;
    }

    /**
     * Returns whether we are collecting data from the base case or the PSA experiments
     * @return true if we are collecting data from the base case; false otherwise
     */
    public boolean isBaseCase() {
        return baseCase;
    }

    /**
     * Returns the number of experiments already performed
     * @return the number of experiments already performed
     */
    public synchronized int getExperimentsPerformed() {
        return experimentsPerformed;
    }

    /**
     * Increments the number of experiments already performed
     */
    public synchronized void incrementExperimentsPerformed() {
        experimentsPerformed++;
    }
}
