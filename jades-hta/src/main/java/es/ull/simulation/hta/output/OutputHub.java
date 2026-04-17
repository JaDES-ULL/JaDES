package es.ull.simulation.hta.output;

import java.io.IOException;
import java.util.ArrayList;
import es.ull.simulation.hta.HTAExperiment;
import es.ull.simulation.hta.config.IHTAOutputConfigProvider;
import es.ull.simulation.hta.inforeceiver.BasicHTAListener;
import es.ull.simulation.hta.inforeceiver.PatientInfoListener;

/**
 * A hub to collect and handle the output of the simulation experiment
 */
public class OutputHub {
	/** A listener to print a detailed information on the events that affect a patient */
	private final ArrayList<PatientInfoListener> patientListener = new ArrayList<>();
	/** The experiment to be analyzed */
	private final HTAExperiment exp;
	/** The list of output items that will handle different information to be printed */
	private final ArrayList<OutputItem<?>> baseCaseOutputItems = new ArrayList<>();
	/** The list of output items that will handle different information to be printed */
	private final ArrayList<OutputItem<?>> psaOutputItems = new ArrayList<>();

	/**
	 * Creates an OutputHub for the specified experiment and configuration file
	 * @param exp The experiment to create the OutputHub for
	 * @param config The JSON configuration
	 * @return An OutputHub for the specified experiment
	 * @throws IOException If there is an error reading the configuration file
	 */
	public OutputHub(HTAExperiment exp, IHTAOutputConfigProvider config) throws IOException {
		this.exp = exp;
		this.baseCaseOutputItems.addAll(config.getBaseCaseOutputItems(exp));
		this.psaOutputItems.addAll(config.getPSAOutputItems(exp));
	}

	/**
	 * Adds a listener to print detailed information for a single patient
	 * @param patientId The identifier of the patient to be followed
	 * @return An OutputHub that includes a listener to print detailed information for a single patient
	 */
	public OutputHub addSinglePatientListener(int patientId) {
		this.patientListener.add(new PatientInfoListener(patientId));
		return this;
	}

	public ArrayList<OutputItem<?>> getBaseCaseOutputItems() {
		return baseCaseOutputItems;
	}

	public ArrayList<OutputItem<?>> getPsaOutputItems() {
		return psaOutputItems;
	}

	/**
	 * Creates and gets the list of listeners to be attached to a set of simulation (i.e. the simulations for the different interventions)
	 * @param baseCase True if the simulation is the base case
	 * @return The list of listeners to be attached to a set of simulation (i.e. the simulations for the different interventions)
	 */
	public ArrayList<BasicHTAListener> getListeners(boolean baseCase) {
		final ArrayList<BasicHTAListener> listeners = new ArrayList<>();
		if (baseCase) {
			for (OutputItem<?> outputItem : baseCaseOutputItems) {
				listeners.addAll(outputItem.getListeners(exp));
			}
		} else {
			for (OutputItem<?> outputItem : psaOutputItems) {
				listeners.addAll(outputItem.getListeners(exp));
			}
		}
		return listeners;
	}
	
	public void processAfterSingleExperiment(int simulationId) {
		if (simulationId == 0) {
			for (OutputItem<?> outputItem : baseCaseOutputItems) {
				outputItem.processAfterSingleExperiment(simulationId);;
				outputItem.processAfterAllExperiments();
				outputItem.flush();
			}	
		}
		else {
			for (OutputItem<?> outputItem : psaOutputItems) {
				outputItem.processAfterSingleExperiment(simulationId);;
				outputItem.flush();
			}
		}
	}

	public void processListenersAfterPSA() {
		for (OutputItem<?> outputItem : psaOutputItems) {
			outputItem.processAfterAllExperiments();
			outputItem.flush();
		}
	}

	public void close() {
		for (OutputItem<?> outputItem : baseCaseOutputItems) {
			outputItem.close();
		}
		for (OutputItem<?> outputItem : psaOutputItems) {
			outputItem.close();
		}
	}
}
