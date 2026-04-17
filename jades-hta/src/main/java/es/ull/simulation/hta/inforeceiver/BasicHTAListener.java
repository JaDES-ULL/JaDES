package es.ull.simulation.hta.inforeceiver;

import es.ull.simulation.hta.DiseaseProgressionSimulation;
import es.ull.simulation.hta.HTAExperiment;
import es.ull.simulation.hta.HTAModel;
import es.ull.simulation.hta.interventions.Intervention;
import es.ull.simulation.inforeceiver.BasicListener;

/**
 * Listeners that collect the information for a bunch of simulations, each one with a different intervention. 
 * The output is supposed to be printed only once, when all the simulations have been run.
 */
public abstract class BasicHTAListener extends BasicListener implements AttachableToCollector {
	/** The collector of the results produced by this listener */
	private final ListenerCollector collector;
	/** Interventions assessed */
	protected final Intervention[] interventions;
	/** The number of simulated patients  */
	protected final int nPatients;
	/** The number of interventions already simulated */
	private int interventionsSimulated;

    public BasicHTAListener(String description, HTAExperiment exp, ListenerCollector collector) {
        super(description);
		this.collector = collector;
		final HTAModel model = exp.getModel();
		this.interventions = model.getRegisteredInterventions();
		this.nPatients = exp.getNPatients();
		interventionsSimulated = 0;
    }

	@Override
	public ListenerCollector getCollector() {
		return collector;
	}

    /**
	 * Returns the interventions assessed
	 * @return The interventions assessed
	 */
	public Intervention[] getInterventions() {
		return interventions;
	}

	/**
	 * Returns the number of simulated patients
	 * @return The number of simulated patients
	 */
	public int getnPatients() {
		return nPatients;
	}

	/**
	 * Returns the number of interventions simulated
	 * @return The number of interventions simulated
	 */
	public int getInterventionsSimulated() {
		return interventionsSimulated;
	}

	/**
	 * Increments the number of interventions simulated
	 * @param simul The simulation that has been finished
	 */
	public void incrementInterventionsSimulated(DiseaseProgressionSimulation simul) {
		interventionsSimulated++;
		aggregateResultsAfterIntervention(simul.getIdentifier(), simul.getIntervention());
		if (getInterventionsSimulated() == getInterventions().length) {
			collector.collectFromListener(simul.getIdentifier(), this);
		}
	}

	/**
	 * Calculates and add the aggregated value of the outcome when the simulation of an intervention is finished
	 * @param simulationId The identifier of the simulation
	 * @param intervention The intervention that has been simulated
	 */
	public abstract void aggregateResultsAfterIntervention(int simulationId, Intervention intervention);
}
