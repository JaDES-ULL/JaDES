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
import es.ull.simulation.hta.inforeceiver.CostCollector;
import es.ull.simulation.hta.inforeceiver.CostListener;
import es.ull.simulation.hta.inforeceiver.QALYCollector;
import es.ull.simulation.hta.inforeceiver.QALYListener;
import es.ull.simulation.hta.interventions.Intervention;
import es.ull.simulation.hta.osdi.OSDiExperimentConfigurationProvider;
import es.ull.simulation.hta.osdi.cliargs.RunDESOsdiArgs;
import es.ull.simulation.hta.osdi.ontology.OSDiTest;
import es.ull.simulation.hta.osdi.ontology.OSDiWrapper;
import es.ull.simulation.hta.params.Discount;
import es.ull.simulation.model.TimeUnit;

/**
 * Checks how the discrete event simulation applies the costs and utilities of the interventions (ontology OSDi_test_des.ttl). The expected values
 * were computed by hand.
 * D03: a disease without progressions, costs or utilities. The assessed intervention, which targets the disease, has a one-time cost of 500 euros
 * (of the year of the study, so it is not updated) and a one-time disutility of 0.05; the comparator has none. Discount rate of 3% for costs and
 * 0% for effects; time horizon of 5 years.
 */
@OSDiTest
@DisplayName("Costs and utilities of the interventions in the discrete event simulation")
@TestInstance(Lifecycle.PER_CLASS)
public class TestDESInterventionPayoffs {
    private static final String OWL_PATH = "/OSDi_test_des.ttl";
    private static final double TOLERANCE = 1e-9;

    private Intervention comparator;
    private Intervention assessed;
    private Patient[] comparatorPatients;
    private Patient[] assessedPatients;
    private CostListener costListener;
    private QALYListener qalyListener;

    @BeforeAll
    public void setUp() throws Exception {
        final OSDiWrapper wrapper = new OSDiWrapper.Builder().build(new StreamDocumentSource(Objects.requireNonNull(
                getClass().getResourceAsStream(OWL_PATH), "Ontology file not found in resources: " + OWL_PATH)));
        final RunDESOsdiArgs args = new RunDESOsdiArgs();
        args.osdiExperiment = "TD_D03_Experiment";
        final OSDiExperiment experiment = new OSDiExperiment(new OSDiExperimentConfigurationProvider(args, wrapper), new BaseHTAOutputConfigProvider());
        experiment.initializeModel();
        final OSDiDESModel model = (OSDiDESModel) experiment.getModel();
        comparator = model.getIntervention("TD_D03_InterventionNone");
        assessed = model.getIntervention("TD_D03_InterventionTreat");
        assertNotNull(comparator, "The comparator intervention must be registered");
        assertNotNull(assessed, "The assessed intervention must be registered");

        // The same discount rate for costs as the experiment (3%)
        final Discount discount = new Discount(0.03);
        costListener = new CostListener(experiment, new CostCollector(experiment, true, "Costs", discount), discount);
        qalyListener = new QALYListener(experiment, new QALYCollector(experiment, true, "QALYs", Discount.ZERO_DISCOUNT), Discount.ZERO_DISCOUNT, experiment.getDisutilityCombinationMethod());
        // Same procedure as HTAExperiment: the second simulation clones the patients of the first one (common random numbers)
        final long timeHorizon = model.getSimulationTimeUnit().convert(experiment.getYearsHorizon(), TimeUnit.YEAR);
        DiseaseProgressionSimulation simul = new DiseaseProgressionSimulation(0, comparator, model);
        simul.registerListener(costListener);
        simul.registerListener(qalyListener);
        simul.run(timeHorizon);
        comparatorPatients = simul.getGeneratedPatients();
        simul = new DiseaseProgressionSimulation(simul, assessed);
        simul.registerListener(costListener);
        simul.registerListener(qalyListener);
        simul.run(timeHorizon);
        assessedPatients = simul.getGeneratedPatients();
    }

    @Test
    @DisplayName("D03. The one-time cost of an intervention that targets the disease is charged once, when the patient starts the simulation")
    public void chargesOneTimeCostAtStart() {
        // Each patient of the assessed arm pays 500 at time 0, without discount; the comparator pays nothing (the disease has no costs)
        final double[][] costs = costListener.getValues();
        assertAll("Cost of each patient",
            () -> {
                for (Patient pat : comparatorPatients) {
                    assertEquals(0.0, costs[comparator.ordinal()][pat.getIdentifier()], TOLERANCE, "Cost of patient " + pat.getIdentifier() + " without intervention");
                }
            },
            () -> {
                for (Patient pat : assessedPatients) {
                    assertEquals(500.0, costs[assessed.ordinal()][pat.getIdentifier()], TOLERANCE, "Cost of patient " + pat.getIdentifier() + " with the intervention");
                }
            }
        );
    }

    @Test
    @DisplayName("D03. The one-time disutility of an intervention that targets the disease is a loss of QALYs applied once, when the patient starts the simulation")
    public void appliesOneTimeDisutilityAtStart() {
        // The intervention has no other effect, so each patient lives the same in both arms (common random numbers): the difference of QALYs of
        // each patient is the one-time disutility, -0.05, without discount
        final double[][] qalys = qalyListener.getValues();
        assertAll("Difference of QALYs of each patient",
            () -> {
                for (int i = 0; i < assessedPatients.length; i++) {
                    final int id = assessedPatients[i].getIdentifier();
                    assertEquals(-0.05, qalys[assessed.ordinal()][id] - qalys[comparator.ordinal()][id], TOLERANCE, "Difference of QALYs of patient " + id);
                }
            }
        );
    }
}
