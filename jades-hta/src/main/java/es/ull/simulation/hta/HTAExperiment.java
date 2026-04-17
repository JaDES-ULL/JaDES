/**
 * 
 */
package es.ull.simulation.hta;

import java.io.IOException;
import java.time.Year;
import java.util.ArrayList;
import es.ull.simulation.experiment.BaseExperiment;
import es.ull.simulation.hta.config.IHTAExperimentConfigProvider;
import es.ull.simulation.hta.config.IHTAOutputConfigProvider;
import es.ull.simulation.hta.inforeceiver.BasicHTAListener;
import es.ull.simulation.hta.interventions.Intervention;
import es.ull.simulation.hta.outcomes.DisutilityCombinationMethod;
import es.ull.simulation.hta.output.JsonHasDiscount;
import es.ull.simulation.hta.output.JsonOutputSpec;
import es.ull.simulation.hta.output.OutputHub;
import es.ull.simulation.inforeceiver.BasicListener;
import es.ull.simulation.model.TimeUnit;

/**
 * Base class for all the experiments in the HTA domain
 * 
 * @author Iván Castilla Rodríguez
 *
 */
public abstract class HTAExperiment extends BaseExperiment {
    private static final int DEFAULT_PATIENTS = 1;
    private static final int DEFAULT_TIME_HORIZON = 100;
    private static final int DEFAULT_STUDY_YEAR = Year.now().getValue();
    private static final boolean DEFAULT_BASE_CASE = true;
    private static final double DEFAULT_DISCOUNT_RATE = 0.00;
	private static final DisutilityCombinationMethod DEFAULT_DISUTILITY_COMBINATION_METHOD = DisutilityCombinationMethod.ADD;

	/** Number of patients to be generated during each simulation */
	private final int nPatients;
	/** Time horizon for the simulation (in years) */
	private final int yearsHorizon;
	/** The model to be simulated */
	private HTAModel model = null;
	/** The method to combine different disutilities. */
	private final DisutilityCombinationMethod method;
	/** A hub to handle the listeners and the outputs of the simulations */
	private final OutputHub outputHub;
	/** Year of the study for cost updating */
	private final int studyYear;
	/** Default discount rate for costs */
	private final double defaultDiscountRateForCosts;
	/** Default discount rate for effects */
	private final double defaultDiscountRateForEffects;
	/** Indicates whether the base case is to be simulated */
	private final boolean baseCase;

	/**
	 * Creates a new HTA experiment
	 * 
	 * @param arguments Arguments for the experiment. 
	 * @throws MalformedSimulationModelException If the model created is not valid
	 * @throws IOException                       If there is an error reading files
	 */
	public HTAExperiment(IHTAExperimentConfigProvider configProvider, IHTAOutputConfigProvider outputConfigProvider) throws MalformedSimulationModelException, IOException {
		super("HTA experiment", configProvider);
		this.outputHub = new OutputHub(this, outputConfigProvider);
		this.nPatients = configProvider.getNPatients().orElse(DEFAULT_PATIENTS);
		this.studyYear = configProvider.getStudyYear().orElse(DEFAULT_STUDY_YEAR);
		this.yearsHorizon = configProvider.getTimeHorizon().orElse(DEFAULT_TIME_HORIZON);
		this.defaultDiscountRateForCosts = configProvider.getDefaultDiscountRateForCosts().orElse(DEFAULT_DISCOUNT_RATE);
		this.defaultDiscountRateForEffects = configProvider.getDefaultDiscountRateForEffects().orElse(DEFAULT_DISCOUNT_RATE);
		this.baseCase = configProvider.isBaseCaseEnabled().orElse(DEFAULT_BASE_CASE);
		this.method = configProvider.getDisutilityCombinationMethod().orElse(DEFAULT_DISUTILITY_COMBINATION_METHOD);
		// Add patient debug listeners
		for (int patientId : configProvider.getDebugPatients()) {
			this.outputHub.addSinglePatientListener(patientId);
		}
	}

    public double resolveDiscountForCosts(JsonOutputSpec spec) {
        if (spec instanceof JsonHasDiscount hd) {
            Double explicit = hd.getDiscount();
            if (explicit != null) {
                return explicit;
            }
        }
		return defaultDiscountRateForCosts;
	}

	public double resolveDiscountForEffects(JsonOutputSpec spec) {
		if (spec instanceof JsonHasDiscount hd) {
			Double explicit = hd.getDiscount();
			if (explicit != null) {
				return explicit;
			}
		}
		return defaultDiscountRateForEffects;
	}
	
	/**
	 * Creates additional listeners to be attached to the simulation identified by id
	 * @param simulationId Simulation identifier
	 * @return Additional listeners to be attached to the simulation identified by id
	 */
	public ArrayList<BasicListener> createAdditionalListeners(int simulationId) {
		return new ArrayList<>();
	}

	/**
	 * Creates the HTA model to be simulated. This mehod is invoked from the initializeModel method and
	 * must be implemented by subclasses to provide the specific model for the experiment.
	 * @return The HTA model to be simulated
	 * @throws MalformedSimulationModelException If the model created is not valid
	 */
	public abstract HTAModel createModel() throws MalformedSimulationModelException;

	/**
	 * Initializes the model by invoking the createModel method and checking its validity. 
	 * This method should be called before running any simulations to ensure that the model is properly set up.
	 * @throws MalformedSimulationModelException
	 */
	public void initializeModel() throws MalformedSimulationModelException {
		if (model == null) {
			this.model = createModel();
			model.checkValidity();
			model.createParameters();			
		}
	}

	/**
	 * Returns the model to be simulated
	 * @return the model to be simulated
	 */
	public HTAModel getModel() {
		return model;
	}
	
	/**
	 * Returns the year of the study (for cost updating)
	 * @return the year of the study (for cost updating)
	 */
	public int getStudyYear() {
		return studyYear;
	}

	/**
	 * Returns the combination method used to combine different disutilities
	 * @return the combination method used to combine different disutilities
	 */
	public DisutilityCombinationMethod getDisutilityCombinationMethod() {
		return method;
	}
	
	@Override
	public void beforeStart() {
		super.beforeStart();
		// The base case is launched first
		if (baseCase)
			simulateInterventions(0);
	}

	@Override
	public void afterFinalize() {
		super.afterFinalize();
		if (getNExperiments() > 0) {
			outputHub.processListenersAfterPSA();
		}
		outputHub.close();
	}

	@Override
	public void runExperiment(int index) {
		simulateInterventions(index + 1);
	}
	/**
	 * Runs the simulations for each intervention
	 * 
	 * @param id Simulation identifier (0 is base case)
	 */
	private void simulateInterventions(int id) {
		final Intervention[] interventions = model.getRegisteredInterventions();
		final int nInterventions = interventions.length;
		long timeHorizon = model.getSimulationTimeUnit().convert(yearsHorizon, TimeUnit.YEAR);


		// Base intervention
		DiseaseProgressionSimulation simul = new DiseaseProgressionSimulation(id, interventions[0], model);
		final ArrayList<BasicHTAListener> listeners = outputHub.getListeners(id == 0);
		for (BasicHTAListener listener : listeners) {
			simul.registerListener(listener);
		}
		simul.run(timeHorizon);

		// The rest of interventions to be compared
		for (int i = 1; i < nInterventions; i++) {
			simul = new DiseaseProgressionSimulation(simul, interventions[i]);
			for (BasicHTAListener listener : listeners) {
				simul.registerListener(listener);
			}
			simul.run(timeHorizon);
		}

		// Notify the output hub that all interventions have been simulated
		outputHub.processAfterSingleExperiment(id);
	}

	/**
	 * Returns the number of patients to be generated during each simulation
	 * @return the number of patients to be generated during each simulation
	 */
	public int getNPatients() {
		return nPatients;
	}

	/**
	 * Returns the time horizon for the simulation (in years)
	 * @return the time horizon for the simulation (in years)
	 */
	public int getYearsHorizon() {
		return yearsHorizon;
	}	
}
