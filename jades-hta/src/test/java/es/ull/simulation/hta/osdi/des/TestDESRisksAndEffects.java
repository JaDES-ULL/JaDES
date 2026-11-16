package es.ull.simulation.hta.osdi.des;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Objects;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.semanticweb.owlapi.io.StreamDocumentSource;

import es.ull.simulation.hta.DiseaseProgressionSimulation;
import es.ull.simulation.hta.Patient;
import es.ull.simulation.hta.config.BaseHTAOutputConfigProvider;
import es.ull.simulation.hta.interventions.Intervention;
import es.ull.simulation.hta.osdi.OSDiExperimentConfigurationProvider;
import es.ull.simulation.hta.osdi.cliargs.RunDESOsdiArgs;
import es.ull.simulation.hta.osdi.ontology.OSDiTest;
import es.ull.simulation.hta.osdi.ontology.OSDiWrapper;
import es.ull.simulation.hta.progression.DiseaseProgression;
import es.ull.simulation.model.TimeUnit;

/**
 * Checks how the discrete event simulation uses the risks of the manifestations and the effects of the interventions on them
 * (ontology OSDi_test_des.ttl). The expected values were computed by hand from the semantics of OSDi (decision D3).
 * D01: chronic manifestations ManX (annual probability 0.2) and ManY (annual probability 0.3). The assessed intervention applies
 * a hazard ratio of 0.5 on the risk of ManX and a factor of 0.5 on the risk of ManY. Population of 20 years old; time horizon of 5 years.
 */
@OSDiTest
@DisplayName("Risks of the manifestations and effects of the interventions in the discrete event simulation")
@TestInstance(Lifecycle.PER_CLASS)
public class TestDESRisksAndEffects {
    private static final String OWL_PATH = "/OSDi_test_des.ttl";
    private static final double TOLERANCE = 1e-9;

    private OSDiDESModel model;
    private Intervention comparator;
    private Intervention assessed;
    private Patient[] comparatorPatients;
    private Patient[] assessedPatients;

    @BeforeAll
    public void setUp() throws Exception {
        final OSDiWrapper wrapper = new OSDiWrapper.Builder().build(new StreamDocumentSource(Objects.requireNonNull(
                getClass().getResourceAsStream(OWL_PATH), "Ontology file not found in resources: " + OWL_PATH)));
        final RunDESOsdiArgs args = new RunDESOsdiArgs();
        args.osdiExperiment = "TD_D01_Experiment";
        final OSDiExperiment experiment = new OSDiExperiment(new OSDiExperimentConfigurationProvider(args, wrapper), new BaseHTAOutputConfigProvider());
        experiment.initializeModel();
        model = (OSDiDESModel) experiment.getModel();
        comparator = model.getIntervention("TD_D01_InterventionNone");
        assessed = model.getIntervention("TD_D01_InterventionTreat");
        assertNotNull(comparator, "The comparator intervention must be registered");
        assertNotNull(assessed, "The assessed intervention must be registered");

        // Same procedure as HTAExperiment: the second simulation clones the patients of the first one (common random numbers)
        final long timeHorizon = model.getSimulationTimeUnit().convert(experiment.getYearsHorizon(), TimeUnit.YEAR);
        DiseaseProgressionSimulation simul = new DiseaseProgressionSimulation(0, comparator, model);
        simul.run(timeHorizon);
        comparatorPatients = simul.getGeneratedPatients();
        simul = new DiseaseProgressionSimulation(simul, assessed);
        simul.run(timeHorizon);
        assessedPatients = simul.getGeneratedPatients();
    }

    /**
     * Returns the proportion of patients that suffered a progression during the simulation
     */
    private static double proportionWith(Patient[] patients, DiseaseProgression progression) {
        int count = 0;
        for (Patient pat : patients) {
            if (pat.getNDiseaseProgressions(progression) > 0) {
                count++;
            }
        }
        return (double) count / patients.length;
    }

    @Test
    @DisplayName("D01. The effects of the intervention modify the risks with the operation given by their data item type")
    public void appliesEffectsOnRisks() {
        // Hazard ratio on a probability, applied to the equivalent rate: 1 - (1 - 0.2)^0.5 = 0.105572809000084
        // Factor on a probability: 0.3 * 0.5 = 0.15
        assertAll("Risks of each arm",
            () -> assertEquals(0.2, model.getParameterValue("TD_D01_ManX_Risk", comparatorPatients[0]), TOLERANCE, "Risk of ManX without intervention"),
            () -> assertEquals(0.105572809000084, model.getParameterValue("TD_D01_ManX_Risk", assessedPatients[0]), TOLERANCE, "Risk of ManX with a hazard ratio of 0.5"),
            () -> assertEquals(0.3, model.getParameterValue("TD_D01_ManY_Risk", comparatorPatients[0]), TOLERANCE, "Risk of ManY without intervention"),
            () -> assertEquals(0.15, model.getParameterValue("TD_D01_ManY_Risk", assessedPatients[0]), TOLERANCE, "Risk of ManY with a factor of 0.5")
        );
    }

    @Test
    @DisplayName("D01. Manifestations with a risk occur during the simulation, less often with the intervention")
    public void manifestationsWithRiskOccur() {
        final DiseaseProgression manX = model.getDiseaseProgression("TD_D01_ManX");
        final DiseaseProgression manY = model.getDiseaseProgression("TD_D01_ManY");
        assertNotNull(manX);
        assertNotNull(manY);
        final double manXComparator = proportionWith(comparatorPatients, manX);
        final double manXAssessed = proportionWith(assessedPatients, manX);
        final double manYComparator = proportionWith(comparatorPatients, manY);
        final double manYAssessed = proportionWith(assessedPatients, manY);
        assertAll("Proportion of patients with each manifestation",
            () -> assertTrue(manXComparator > 0.0, "No patient suffered ManX without intervention"),
            () -> assertTrue(manXAssessed > 0.0, "No patient suffered ManX with the intervention"),
            () -> assertTrue(manYComparator > 0.0, "No patient suffered ManY without intervention"),
            () -> assertTrue(manYAssessed > 0.0, "No patient suffered ManY with the intervention"),
            () -> assertTrue(manXAssessed < manXComparator, "ManX should be less frequent with the intervention: " + manXAssessed + " vs " + manXComparator),
            () -> assertTrue(manYAssessed < manYComparator, "ManY should be less frequent with the intervention: " + manYAssessed + " vs " + manYComparator)
        );
    }
}
