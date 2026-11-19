package es.ull.simulation.hta.osdi.des;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
import es.ull.simulation.model.TimeUnit;

/**
 * Checks the values assumed by the discrete event simulation when the population declares neither age nor sex (decision D10 of docs/osdi-1.3-diseno.md):
 * an initial age of 18 years and a proportion of women of 0.5. Ontology OSDi_test_des.ttl, case D02 (a copy of D01 without age and sex).
 */
@OSDiTest
@DisplayName("Default age and sex of the population in the discrete event simulation (D10)")
@TestInstance(Lifecycle.PER_CLASS)
public class TestDESPopulationDefaults {
    private static final String OWL_PATH = "/OSDi_test_des.ttl";

    private Patient[] patients;

    @BeforeAll
    public void setUp() throws Exception {
        final OSDiWrapper wrapper = new OSDiWrapper.Builder().build(new StreamDocumentSource(Objects.requireNonNull(
                getClass().getResourceAsStream(OWL_PATH), "Ontology file not found in resources: " + OWL_PATH)));
        final RunDESOsdiArgs args = new RunDESOsdiArgs();
        args.osdiExperiment = "TD_D02_Experiment";
        final OSDiExperiment experiment = new OSDiExperiment(new OSDiExperimentConfigurationProvider(args, wrapper), new BaseHTAOutputConfigProvider());
        experiment.initializeModel();
        final OSDiDESModel model = (OSDiDESModel) experiment.getModel();
        final Intervention comparator = model.getIntervention("TD_D02_InterventionNone");
        assertNotNull(comparator, "The comparator intervention must be registered");
        final DiseaseProgressionSimulation simul = new DiseaseProgressionSimulation(0, comparator, model);
        simul.run(model.getSimulationTimeUnit().convert(experiment.getYearsHorizon(), TimeUnit.YEAR));
        patients = simul.getGeneratedPatients();
    }

    @Test
    @DisplayName("D02. Without age, the patients start at 18 years old; without sex, half of them are women")
    public void assumesDefaultAgeAndSex() {
        int women = 0;
        double minAge = Double.MAX_VALUE;
        double maxAge = -Double.MAX_VALUE;
        for (Patient pat : patients) {
            women += pat.getSex();
            minAge = Math.min(minAge, pat.getInitAge());
            maxAge = Math.max(maxAge, pat.getInitAge());
        }
        final double proportionOfWomen = (double) women / patients.length;
        final double youngest = minAge;
        final double oldest = maxAge;
        // Sex: 0 for men, 1 for women. Tolerance: about three standard errors for 5000 patients (0.007)
        assertAll("Default age and sex",
            () -> assertEquals(18.0, youngest, 1e-9, "Initial age of the youngest patient"),
            () -> assertEquals(18.0, oldest, 1e-9, "Initial age of the oldest patient"),
            () -> assertEquals(0.5, proportionOfWomen, 0.02, "Proportion of women")
        );
    }
}
