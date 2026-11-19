package es.ull.simulation.hta.osdi.des;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.semanticweb.owlapi.io.StreamDocumentSource;

import es.ull.simulation.hta.DiseaseProgressionSimulation;
import es.ull.simulation.hta.MalformedSimulationModelException;
import es.ull.simulation.hta.Patient;
import es.ull.simulation.hta.config.BaseHTAOutputConfigProvider;
import es.ull.simulation.hta.inforeceiver.CostCollector;
import es.ull.simulation.hta.inforeceiver.CostListener;
import es.ull.simulation.hta.interventions.Intervention;
import es.ull.simulation.hta.osdi.LogCapture;
import es.ull.simulation.hta.osdi.OSDiExperimentConfigurationProvider;
import es.ull.simulation.hta.osdi.cliargs.RunDESOsdiArgs;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.OSDiTest;
import es.ull.simulation.hta.osdi.ontology.OSDiWrapper;
import es.ull.simulation.hta.params.Discount;
import es.ull.simulation.hta.progression.DiseaseProgression;
import es.ull.simulation.model.TimeUnit;

/**
 * Checks the target of the interventions in the discrete event simulation (decision D1 of docs/osdi-1.3-diseno.md; ontology OSDi_test_des.ttl).
 * Time 0 is the moment when the intervention is applied, and the population describes the patients at that moment: if the target is a clinical
 * progression element T, every patient presents T at time 0. The expected values were computed by hand. Every case has a population of 20 years
 * old, no discount and a time horizon of 5 years; the mortality between 20 and 25 years old (about 0.1% per year) is neglected in the expected
 * proportions and numbers of episodes, but not in the costs of each patient, which use its own time of death.
 * <ul>
 * <li>D04: the target is the chronic manifestation ManX (annual cost 1000, one-time cost 200; its risk is ignored). ManY requires ManX (annual
 * probability 0.2). The assessed intervention has an annual cost of 100.</li>
 * <li>D05: the population declares an initial proportion of 0.5 for the target: malformed.</li>
 * <li>D06: the target is the acute manifestation ManC (cost of 2000 per episode, annual probability 0.5). The assessed intervention costs 300 per
 * episode, has an annual cost of 100 (ignored with a warning) and a hazard ratio of 0.5 on the risk of ManC.</li>
 * <li>D07: the target is the acute manifestation ManC (cost of 2000 per episode) with a frequency of 2 episodes per year and no risk.</li>
 * <li>D08: the target is a development: not supported.</li>
 * </ul>
 */
@OSDiTest
@DisplayName("Target of the interventions in the discrete event simulation (D1)")
@TestInstance(Lifecycle.PER_CLASS)
public class TestDESInterventionTarget {
    private static final String OWL_PATH = "/OSDi_test_des.ttl";
    private static final double TOLERANCE = 1e-6;

    /**
     * The result of simulating both arms of an experiment
     * @param model The model
     * @param comparator The comparator intervention
     * @param assessed The assessed intervention
     * @param comparatorPatients The patients of the comparator arm
     * @param assessedPatients The patients of the assessed arm (clones of the comparator ones)
     * @param costs The cost of each patient, indexed by the order of the intervention and the identifier of the patient
     * @param warnings The messages logged while building the model
     */
    private record Run(OSDiDESModel model, Intervention comparator, Intervention assessed, Patient[] comparatorPatients, Patient[] assessedPatients,
            double[][] costs, List<String> warnings) {
    }

    private OSDiWrapper wrapper;
    private Run d04;
    private Run d06;
    private Run d07;

    @BeforeAll
    public void setUp() throws Exception {
        wrapper = new OSDiWrapper.Builder().build(new StreamDocumentSource(Objects.requireNonNull(
                getClass().getResourceAsStream(OWL_PATH), "Ontology file not found in resources: " + OWL_PATH)));
        d04 = run("D04");
        d06 = run("D06");
        d07 = run("D07");
    }

    private OSDiExperiment createExperiment(String code) throws Exception {
        final RunDESOsdiArgs args = new RunDESOsdiArgs();
        args.osdiExperiment = "TD_" + code + "_Experiment";
        return new OSDiExperiment(new OSDiExperimentConfigurationProvider(args, wrapper), new BaseHTAOutputConfigProvider());
    }

    /**
     * Builds the model of a case and simulates both arms with the same procedure as HTAExperiment (common random numbers)
     */
    private Run run(String code) throws Exception {
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
        final long timeHorizon = model.getSimulationTimeUnit().convert(experiment.getYearsHorizon(), TimeUnit.YEAR);
        DiseaseProgressionSimulation simul = new DiseaseProgressionSimulation(0, comparator, model);
        simul.registerListener(costListener);
        simul.run(timeHorizon);
        final Patient[] comparatorPatients = simul.getGeneratedPatients();
        simul = new DiseaseProgressionSimulation(simul, assessed);
        simul.registerListener(costListener);
        simul.run(timeHorizon);
        return new Run(model, comparator, assessed, comparatorPatients, simul.getGeneratedPatients(), costListener.getValues(), messages);
    }

    /**
     * Returns the years that a patient lived within the time horizon of 5 years
     */
    private static double yearsLived(OSDiDESModel model, Patient pat) {
        final long death = pat.getTimeToDeath();
        if (death == Long.MAX_VALUE) {
            return 5.0;
        }
        return Math.min(5.0, model.simulationTimeToYears(TimeUnit.DAY.convert(death, pat.getSimulation().getTimeUnit())));
    }

    private static double meanEpisodes(Patient[] patients, DiseaseProgression progression) {
        double total = 0.0;
        for (Patient pat : patients) {
            total += pat.getNDiseaseProgressions(progression);
        }
        return total / patients.length;
    }

    private static boolean containsWarning(List<String> messages, String text) {
        return messages.stream().anyMatch(message -> message.startsWith("WARN ") && message.contains(text));
    }

    @Test
    @DisplayName("D04. If the target is a chronic manifestation, every patient presents it at time 0, and its absent initial proportion is assumed to be 1 with a warning")
    public void everyPatientStartsWithChronicTarget() {
        final DiseaseProgression manX = d04.model().getDiseaseProgression("TD_D04_ManX");
        assertNotNull(manX);
        assertTrue(containsWarning(d04.warnings(), "does not declare the initial proportion of TD_D04_ManX"), "Warning not found: " + d04.warnings());
        assertAll("Every patient presents ManX from time 0",
            () -> {
                for (Patient pat : d04.comparatorPatients()) {
                    assertEquals(1, pat.getNDiseaseProgressions(manX), "Onsets of ManX of patient " + pat.getIdentifier());
                    assertEquals(0L, pat.getTimeToDiseaseProgression(manX), "Onset of ManX of patient " + pat.getIdentifier());
                }
            });
    }

    @Test
    @DisplayName("D04. If the target is a chronic manifestation, its one-time cost is charged at time 0 and the annual costs from time 0")
    public void chargesCostsOfChronicTargetFromTimeZero() {
        // Comparator: 200 + 1000 * years lived; assessed: 200 + (1000 + 100) * years lived
        assertAll("Cost of each patient",
            () -> {
                for (Patient pat : d04.comparatorPatients()) {
                    final double expected = 200.0 + 1000.0 * yearsLived(d04.model(), pat);
                    assertEquals(expected, d04.costs()[d04.comparator().ordinal()][pat.getIdentifier()], TOLERANCE, "Cost of patient " + pat.getIdentifier() + " without intervention");
                }
            },
            () -> {
                for (Patient pat : d04.assessedPatients()) {
                    final double expected = 200.0 + 1100.0 * yearsLived(d04.model(), pat);
                    assertEquals(expected, d04.costs()[d04.assessed().ordinal()][pat.getIdentifier()], TOLERANCE, "Cost of patient " + pat.getIdentifier() + " with the intervention");
                }
            });
    }

    @Test
    @DisplayName("D04. The progressions from a chronic target are planned from time 0: the proportion of ManY after 5 years is 1 - (1 - 0.2)^5")
    public void plansProgressionsFromChronicTargetFromTimeZero() {
        // 1 - 0.8^5 = 0.67232. Tolerance: about three standard errors for 5000 patients (0.007)
        final DiseaseProgression manY = d04.model().getDiseaseProgression("TD_D04_ManY");
        assertNotNull(manY);
        int count = 0;
        for (Patient pat : d04.comparatorPatients()) {
            if (pat.getNDiseaseProgressions(manY) > 0) {
                count++;
            }
        }
        assertEquals(0.67232, (double) count / d04.comparatorPatients().length, 0.02, "Proportion of patients with ManY");
    }

    @Test
    @DisplayName("D05. An initial proportion of the target different from 1 contradicts the target of the interventions")
    public void rejectsInitialProportionOfTargetDifferentFromOne() throws Exception {
        final OSDiExperiment experiment = createExperiment("D05");
        final MalformedSimulationModelException exception = assertThrows(MalformedSimulationModelException.class, experiment::initializeModel);
        assertInstanceOf(MalformedOSDiModelException.class, exception.getCause(), "Unexpected cause: " + exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("declares an initial proportion of 0.5 for TD_D05_ManX"), "Unexpected message: " + exception.getCause().getMessage());
    }

    @Test
    @DisplayName("D06. If the target is an acute manifestation, every patient suffers an episode at time 0, and the next ones come from its risk, modified by the intervention")
    public void simulatesEpisodesOfAcuteTargetWithRisk() {
        // Rate of the next episodes: -ln(1 - p). Comparator: 1 + 5 * ln(2) = 4.465736; assessed (hazard ratio of 0.5 on p = 0.5): p' = 1 - 0.5^0.5,
        // so 1 + 5 * 0.5 * ln(2) = 2.732868. Tolerance: about four standard errors for 5000 patients (0.026)
        final DiseaseProgression manC = d06.model().getDiseaseProgression("TD_D06_ManC");
        assertNotNull(manC);
        assertAll("Episodes of ManC",
            () -> {
                for (Patient pat : d06.comparatorPatients()) {
                    assertTrue(pat.getNDiseaseProgressions(manC) >= 1, "Patient " + pat.getIdentifier() + " without the episode at time 0");
                }
            },
            () -> {
                for (Patient pat : d06.assessedPatients()) {
                    assertTrue(pat.getNDiseaseProgressions(manC) >= 1, "Patient " + pat.getIdentifier() + " without the episode at time 0 with the intervention");
                }
            },
            () -> assertEquals(4.465736, meanEpisodes(d06.comparatorPatients(), manC), 0.1, "Mean episodes without intervention"),
            () -> assertEquals(2.732868, meanEpisodes(d06.assessedPatients(), manC), 0.1, "Mean episodes with the intervention"));
    }

    @Test
    @DisplayName("D06. If the target is an acute manifestation, the one-time payoffs of the intervention apply with each episode, and its annual ones are ignored with a warning")
    public void chargesInterventionWithEachEpisodeOfAcuteTarget() {
        // Comparator: 2000 per episode; assessed: 2000 + 300 per episode (the annual cost of 100 is ignored)
        final DiseaseProgression manC = d06.model().getDiseaseProgression("TD_D06_ManC");
        assertTrue(containsWarning(d06.warnings(), "The intervention TD_D06_InterventionTreat, whose target is an acute manifestation, defines annual costs or utilities"),
            "Warning not found: " + d06.warnings());
        assertAll("Cost of each patient",
            () -> {
                for (Patient pat : d06.comparatorPatients()) {
                    assertEquals(2000.0 * pat.getNDiseaseProgressions(manC), d06.costs()[d06.comparator().ordinal()][pat.getIdentifier()], TOLERANCE, "Cost of patient " + pat.getIdentifier() + " without intervention");
                }
            },
            () -> {
                for (Patient pat : d06.assessedPatients()) {
                    assertEquals(2300.0 * pat.getNDiseaseProgressions(manC), d06.costs()[d06.assessed().ordinal()][pat.getIdentifier()], TOLERANCE, "Cost of patient " + pat.getIdentifier() + " with the intervention");
                }
            });
    }

    @Test
    @DisplayName("D07. If the target is an acute manifestation with a frequency, every patient suffers an episode at time 0, and the next ones come from the frequency")
    public void simulatesEpisodesOfAcuteTargetWithFrequency() {
        // 1 + 2 * 5 = 11 episodes. Tolerance: about three standard errors for 5000 patients (0.045)
        final DiseaseProgression manC = d07.model().getDiseaseProgression("TD_D07_ManC");
        assertNotNull(manC);
        assertAll("Episodes of ManC",
            () -> {
                for (Patient pat : d07.comparatorPatients()) {
                    assertTrue(pat.getNDiseaseProgressions(manC) >= 1, "Patient " + pat.getIdentifier() + " without the episode at time 0");
                    assertEquals(2000.0 * pat.getNDiseaseProgressions(manC), d07.costs()[d07.comparator().ordinal()][pat.getIdentifier()], TOLERANCE, "Cost of patient " + pat.getIdentifier());
                }
            },
            () -> assertEquals(11.0, meanEpisodes(d07.comparatorPatients(), manC), 0.15, "Mean episodes"));
    }

    @Test
    @DisplayName("D08. A development as the target of the interventions is not supported in discrete event simulation models")
    public void rejectsDevelopmentAsTarget() throws Exception {
        final OSDiExperiment experiment = createExperiment("D08");
        final MalformedSimulationModelException exception = assertThrows(MalformedSimulationModelException.class, experiment::initializeModel);
        assertInstanceOf(UnsupportedOSDiFeatureException.class, exception.getCause(), "Unexpected cause: " + exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("target the development TD_D08_DevA"), "Unexpected message: " + exception.getCause().getMessage());
    }
}
