package es.ull.simulation.hta.osdi.des;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import es.ull.simulation.hta.Patient;
import es.ull.simulation.hta.osdi.des.DESTestSupport.Run;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.OSDiTest;
import es.ull.simulation.hta.progression.DiseaseProgression;

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
    private static final double TOLERANCE = 1e-6;

    private Run d04;
    private Run d06;
    private Run d07;

    @BeforeAll
    public void setUp() throws Exception {
        d04 = DESTestSupport.run("D04");
        d06 = DESTestSupport.run("D06");
        d07 = DESTestSupport.run("D07");
    }

    @Test
    @DisplayName("D04. If the target is a chronic manifestation, every patient presents it at time 0, and its absent initial proportion is assumed to be 1 with a warning")
    public void everyPatientStartsWithChronicTarget() {
        final DiseaseProgression manX = d04.model().getDiseaseProgression("TD_D04_ManX");
        assertNotNull(manX);
        assertTrue(d04.containsWarning("does not declare the initial proportion of TD_D04_ManX"), "Warning not found: " + d04.messages());
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
                    final double expected = 200.0 + 1000.0 * DESTestSupport.yearsLived(d04.model(), pat);
                    assertEquals(expected, d04.costs()[d04.comparator().ordinal()][pat.getIdentifier()], TOLERANCE, "Cost of patient " + pat.getIdentifier() + " without intervention");
                }
            },
            () -> {
                for (Patient pat : d04.assessedPatients()) {
                    final double expected = 200.0 + 1100.0 * DESTestSupport.yearsLived(d04.model(), pat);
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
        DESTestSupport.assertRejected("D05", MalformedOSDiModelException.class, "declares an initial proportion of 0.5 for TD_D05_ManX");
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
            () -> assertEquals(4.465736, DESTestSupport.meanEpisodes(d06.comparatorPatients(), manC), 0.1, "Mean episodes without intervention"),
            () -> assertEquals(2.732868, DESTestSupport.meanEpisodes(d06.assessedPatients(), manC), 0.1, "Mean episodes with the intervention"));
    }

    @Test
    @DisplayName("D06. If the target is an acute manifestation, the one-time payoffs of the intervention apply with each episode, and its annual ones are ignored with a warning")
    public void chargesInterventionWithEachEpisodeOfAcuteTarget() {
        // Comparator: 2000 per episode; assessed: 2000 + 300 per episode (the annual cost of 100 is ignored)
        final DiseaseProgression manC = d06.model().getDiseaseProgression("TD_D06_ManC");
        assertTrue(d06.containsWarning("The intervention TD_D06_InterventionTreat, whose target is an acute manifestation, defines annual costs or utilities"),
            "Warning not found: " + d06.messages());
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
            () -> assertEquals(11.0, DESTestSupport.meanEpisodes(d07.comparatorPatients(), manC), 0.15, "Mean episodes"));
    }

    @Test
    @DisplayName("D08. A development as the target of the interventions is not supported in discrete event simulation models")
    public void rejectsDevelopmentAsTarget() throws Exception {
        DESTestSupport.assertRejected("D08", UnsupportedOSDiFeatureException.class, "target the development TD_D08_DevA");
    }
}
