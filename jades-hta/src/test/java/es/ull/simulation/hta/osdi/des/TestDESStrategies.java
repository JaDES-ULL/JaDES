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
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.OSDiTest;
import es.ull.simulation.hta.progression.DiseaseProgression;

/**
 * Checks the strategies of the health conditions and their replacements by the interventions in the discrete event simulation (decision D2 of
 * docs/osdi-1.3-diseno.md; ontology OSDi_test_des.ttl). As in decision trees, the strategies of each arm are those of each health condition after
 * the strategy replacements of the intervention of the arm, by type; the payoffs of the treatment and follow-up strategies are added to those of
 * their health condition, with its schedule. The DES does not simulate the outcomes of the strategies, so strategies with outcomes or adverse
 * events, and strategies with guidelines, are not supported. The expected values were computed by hand. Every case has a population of 20 years
 * old (base utility 0.85), no discount and a time horizon of 5 years.
 * <ul>
 * <li>D09: the disease has a follow-up strategy (50 per year) and a treatment strategy (300 per year, 100 once). The assessed intervention replaces
 * the treatment by another one (800 per year, 400 once, annual disutility 0.1); the follow-up is kept.</li>
 * <li>D10: the target is the acute manifestation ManC (2000 per episode, annual probability 0.5), whose treatment (100 per episode) is replaced in
 * the assessed arm by another one (400 per episode, and an annual cost of 30 that is ignored with a warning).</li>
 * <li>D11: the chronic manifestation ManX (annual probability 0.2) has a treatment strategy (100 once, 20 per year).</li>
 * <li>D14: a treatment strategy with outcomes (failsIf). D15: a treatment strategy with a guideline.</li>
 * </ul>
 * The detection strategies (D12, D13) are checked in {@link TestDESDetection}.
 */
@OSDiTest
@DisplayName("Strategies and strategy replacements in the discrete event simulation (D2)")
@TestInstance(Lifecycle.PER_CLASS)
public class TestDESStrategies {
    private static final double TOLERANCE = 1e-6;

    private Run d09;
    private Run d10;
    private Run d11;

    @BeforeAll
    public void setUp() throws Exception {
        d09 = DESTestSupport.run("D09");
        d10 = DESTestSupport.run("D10");
        d11 = DESTestSupport.run("D11");
    }

    @Test
    @DisplayName("D09. The strategies of the disease add their payoffs from time 0, and the intervention replaces them by type")
    public void addsPayoffsOfStrategiesOfDiseaseReplacedByType() {
        // Comparator: 100 + (50 + 300) * years lived; assessed: 400 + (50 + 800) * years lived (the follow-up is kept)
        assertAll("Cost of each patient",
            () -> {
                for (Patient pat : d09.comparatorPatients()) {
                    final double expected = 100.0 + 350.0 * DESTestSupport.yearsLived(d09.model(), pat);
                    assertEquals(expected, d09.costs()[d09.comparator().ordinal()][pat.getIdentifier()], TOLERANCE, "Cost of patient " + pat.getIdentifier() + " without intervention");
                }
            },
            () -> {
                for (Patient pat : d09.assessedPatients()) {
                    final double expected = 400.0 + 850.0 * DESTestSupport.yearsLived(d09.model(), pat);
                    assertEquals(expected, d09.costs()[d09.assessed().ordinal()][pat.getIdentifier()], TOLERANCE, "Cost of patient " + pat.getIdentifier() + " with the intervention");
                }
            });
    }

    @Test
    @DisplayName("D09. The annual disutility of a strategy is added to that of its health condition")
    public void addsDisutilityOfStrategy() {
        // The intervention has no effects, so each patient lives the same in both arms: the difference of QALYs is -0.1 * years lived
        assertAll("Difference of QALYs of each patient",
            () -> {
                for (Patient pat : d09.assessedPatients()) {
                    final int id = pat.getIdentifier();
                    final double expected = -0.1 * DESTestSupport.yearsLived(d09.model(), pat);
                    assertEquals(expected, d09.qalys()[d09.assessed().ordinal()][id] - d09.qalys()[d09.comparator().ordinal()][id], TOLERANCE, "Difference of QALYs of patient " + id);
                }
            });
    }

    @Test
    @DisplayName("D10. The replaced strategy of an acute target applies with each episode, and its annual payoffs are ignored with a warning")
    public void appliesReplacedStrategyWithEachEpisode() {
        // Comparator: (2000 + 100) per episode; assessed: (2000 + 400) per episode (the annual cost of 30 is ignored)
        final DiseaseProgression manC = d10.model().getDiseaseProgression("TD_D10_ManC");
        assertNotNull(manC);
        assertTrue(d10.containsWarning("The strategy TD_D10_Treatment2 of the acute manifestation TD_D10_ManC defines annual costs or utilities"), "Warning not found: " + d10.messages());
        assertAll("Cost of each patient",
            () -> {
                for (Patient pat : d10.comparatorPatients()) {
                    assertEquals(2100.0 * pat.getNDiseaseProgressions(manC), d10.costs()[d10.comparator().ordinal()][pat.getIdentifier()], TOLERANCE, "Cost of patient " + pat.getIdentifier() + " without intervention");
                }
            },
            () -> {
                for (Patient pat : d10.assessedPatients()) {
                    assertEquals(2400.0 * pat.getNDiseaseProgressions(manC), d10.costs()[d10.assessed().ordinal()][pat.getIdentifier()], TOLERANCE, "Cost of patient " + pat.getIdentifier() + " with the intervention");
                }
            });
    }

    @Test
    @DisplayName("D11. The strategy of a chronic manifestation applies its one-time payoffs at the onset and its annual payoffs while the patient has it")
    public void appliesStrategyOfChronicManifestationWhileActive() {
        // With ManX at time t (years): 100 + 20 * (years lived - t); without ManX: 0
        final DiseaseProgression manX = d11.model().getDiseaseProgression("TD_D11_ManX");
        assertNotNull(manX);
        int withManX = 0;
        for (Patient pat : d11.comparatorPatients()) {
            double expected = 0.0;
            if (pat.getNDiseaseProgressions(manX) > 0) {
                withManX++;
                final double onset = DESTestSupport.toYears(d11.model(), pat, pat.getTimeToDiseaseProgression(manX));
                expected = 100.0 + 20.0 * (DESTestSupport.yearsLived(d11.model(), pat) - onset);
            }
            assertEquals(expected, d11.costs()[d11.comparator().ordinal()][pat.getIdentifier()], TOLERANCE, "Cost of patient " + pat.getIdentifier());
        }
        assertTrue(withManX > 0, "No patient suffered ManX");
    }

    @Test
    @DisplayName("D14. A strategy with outcomes is not supported in discrete event simulation models")
    public void rejectsStrategyWithOutcomes() throws Exception {
        DESTestSupport.assertRejected("D14", UnsupportedOSDiFeatureException.class, "The strategy TD_D14_Treatment defines outcomes");
    }

    @Test
    @DisplayName("D15. A strategy with a guideline is not supported in discrete event simulation models")
    public void rejectsStrategyWithGuideline() throws Exception {
        DESTestSupport.assertRejected("D15", UnsupportedOSDiFeatureException.class, "The strategy TD_D15_Treatment uses a guideline");
    }
}
