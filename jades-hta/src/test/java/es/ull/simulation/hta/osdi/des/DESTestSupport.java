package es.ull.simulation.hta.osdi.des;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Objects;

import org.semanticweb.owlapi.io.StreamDocumentSource;

import es.ull.simulation.hta.DiseaseProgressionSimulation;
import es.ull.simulation.hta.MalformedSimulationModelException;
import es.ull.simulation.hta.Patient;
import es.ull.simulation.hta.config.BaseHTAOutputConfigProvider;
import es.ull.simulation.hta.inforeceiver.CostCollector;
import es.ull.simulation.hta.inforeceiver.CostListener;
import es.ull.simulation.hta.inforeceiver.QALYCollector;
import es.ull.simulation.hta.inforeceiver.QALYListener;
import es.ull.simulation.hta.interventions.Intervention;
import es.ull.simulation.hta.osdi.LogCapture;
import es.ull.simulation.hta.osdi.OSDiExperimentConfigurationProvider;
import es.ull.simulation.hta.osdi.cliargs.RunDESOsdiArgs;
import es.ull.simulation.hta.osdi.ontology.OSDiWrapper;
import es.ull.simulation.hta.params.Discount;
import es.ull.simulation.hta.progression.DiseaseProgression;
import es.ull.simulation.model.TimeUnit;

/**
 * Utilities for the tests of the discrete event simulation models of the ontology OSDi_test_des.ttl. Every case of that ontology has a
 * comparator (TD_Dxx_InterventionNone) and an assessed intervention (TD_Dxx_InterventionTreat), no discount and a time horizon of 5 years.
 */
final class DESTestSupport {
    private static final String OWL_PATH = "/OSDi_test_des.ttl";

    /**
     * The result of simulating both arms of an experiment
     * @param model The model
     * @param comparator The comparator intervention
     * @param assessed The assessed intervention
     * @param comparatorPatients The patients of the comparator arm
     * @param assessedPatients The patients of the assessed arm (clones of the comparator ones)
     * @param costs The cost of each patient, indexed by the order of the intervention and the identifier of the patient (no discount)
     * @param qalys The QALYs of each patient, indexed by the order of the intervention and the identifier of the patient (no discount)
     * @param messages The messages logged while building the model
     */
    record Run(OSDiDESModel model, Intervention comparator, Intervention assessed, Patient[] comparatorPatients, Patient[] assessedPatients,
            double[][] costs, double[][] qalys, List<String> messages) {

        /**
         * Returns true if a warning containing a text was logged while building the model
         */
        boolean containsWarning(String text) {
            return messages.stream().anyMatch(message -> message.startsWith("WARN ") && message.contains(text));
        }
    }

    private static OSDiWrapper wrapper = null;

    private DESTestSupport() {
    }

    private static synchronized OSDiWrapper getOntology() throws Exception {
        if (wrapper == null) {
            wrapper = new OSDiWrapper.Builder().build(new StreamDocumentSource(Objects.requireNonNull(
                    DESTestSupport.class.getResourceAsStream(OWL_PATH), "Ontology file not found in resources: " + OWL_PATH)));
        }
        return wrapper;
    }

    /**
     * Creates the experiment of a case, without building its model
     * @param code The code of the case (e.g., "D04")
     */
    static OSDiExperiment createExperiment(String code) throws Exception {
        final RunDESOsdiArgs args = new RunDESOsdiArgs();
        args.osdiExperiment = "TD_" + code + "_Experiment";
        return new OSDiExperiment(new OSDiExperimentConfigurationProvider(args, getOntology()), new BaseHTAOutputConfigProvider());
    }

    /**
     * Builds the model of a case and simulates both arms with the same procedure as HTAExperiment (common random numbers)
     * @param code The code of the case (e.g., "D04")
     */
    static Run run(String code) throws Exception {
        final OSDiExperiment experiment = createExperiment(code);
        final List<String> messages;
        try (LogCapture log = LogCapture.start()) {
            experiment.initializeModel();
            messages = List.copyOf(log.getMessages());
        }
        final OSDiDESModel model = (OSDiDESModel) experiment.getModel();
        final Intervention comparator = model.getIntervention("TD_" + code + "_InterventionNone");
        final Intervention assessed = model.getIntervention("TD_" + code + "_InterventionTreat");
        assertNotNull(comparator, "The comparator intervention must be registered");
        assertNotNull(assessed, "The assessed intervention must be registered");
        final CostListener costListener = new CostListener(experiment, new CostCollector(experiment, true, "Costs", Discount.ZERO_DISCOUNT), Discount.ZERO_DISCOUNT);
        final QALYListener qalyListener = new QALYListener(experiment, new QALYCollector(experiment, true, "QALYs", Discount.ZERO_DISCOUNT), Discount.ZERO_DISCOUNT,
            experiment.getDisutilityCombinationMethod());
        final long timeHorizon = model.getSimulationTimeUnit().convert(experiment.getYearsHorizon(), TimeUnit.YEAR);
        DiseaseProgressionSimulation simul = new DiseaseProgressionSimulation(0, comparator, model);
        simul.registerListener(costListener);
        simul.registerListener(qalyListener);
        simul.run(timeHorizon);
        final Patient[] comparatorPatients = simul.getGeneratedPatients();
        simul = new DiseaseProgressionSimulation(simul, assessed);
        simul.registerListener(costListener);
        simul.registerListener(qalyListener);
        simul.run(timeHorizon);
        return new Run(model, comparator, assessed, comparatorPatients, simul.getGeneratedPatients(), costListener.getValues(), qalyListener.getValues(), messages);
    }

    /**
     * Checks that the model of a case is rejected, and returns the cause of the rejection
     * @param code The code of the case (e.g., "D05")
     * @param expectedCause The type of the expected cause (MalformedOSDiModelException or UnsupportedOSDiFeatureException)
     * @param expectedMessage A fragment of the message of the cause
     */
    static void assertRejected(String code, Class<? extends Exception> expectedCause, String expectedMessage) throws Exception {
        final OSDiExperiment experiment = createExperiment(code);
        final MalformedSimulationModelException exception = assertThrows(MalformedSimulationModelException.class, experiment::initializeModel);
        assertInstanceOf(expectedCause, exception.getCause(), "Unexpected cause: " + exception.getCause());
        assertTrue(exception.getCause().getMessage().contains(expectedMessage), "Unexpected message: " + exception.getCause().getMessage());
    }

    /**
     * Converts a timestamp of the simulation into years
     */
    static double toYears(OSDiDESModel model, Patient pat, long ts) {
        return model.simulationTimeToYears(TimeUnit.DAY.convert(ts, pat.getSimulation().getTimeUnit()));
    }

    /**
     * Returns the years that a patient lived within the time horizon of 5 years
     */
    static double yearsLived(OSDiDESModel model, Patient pat) {
        final long death = pat.getTimeToDeath();
        if (death == Long.MAX_VALUE) {
            return 5.0;
        }
        return Math.min(5.0, toYears(model, pat, death));
    }

    /**
     * Returns the mean number of onsets (episodes) of a progression per patient
     */
    static double meanEpisodes(Patient[] patients, DiseaseProgression progression) {
        double total = 0.0;
        for (Patient pat : patients) {
            total += pat.getNDiseaseProgressions(progression);
        }
        return total / patients.length;
    }
}
