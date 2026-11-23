package es.ull.simulation.hta.osdi.des;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import es.ull.simulation.hta.Patient;
import es.ull.simulation.hta.osdi.des.DESTestSupport.Run;
import es.ull.simulation.hta.osdi.ontology.OSDiTest;
import es.ull.simulation.hta.params.StandardParameter;

/**
 * Checks the detection interventions in the discrete event simulation (ontology OSDi_test_des.ttl). The sensitivity and specificity of a detection
 * intervention come from the detection strategy of its arm (decision D2 of docs/osdi-1.3-diseno.md), and are assumed to be perfect (1) if the
 * strategy does not define them. The expected values were computed by hand.
 * <ul>
 * <li>D12: the disease has a diagnosis strategy (sensitivity 0.7, without specificity). The screening arm replaces the detection strategies by a
 * group with a screening test (sensitivity 0.9, specificity 0.95) and the diagnosis.</li>
 * </ul>
 */
@OSDiTest
@DisplayName("Detection interventions in the discrete event simulation")
@TestInstance(Lifecycle.PER_CLASS)
public class TestDESDetection {
    private static final double TOLERANCE = 1e-6;

    private Run d12;

    @BeforeAll
    public void setUp() throws Exception {
        d12 = DESTestSupport.run("D12");
    }

    @Test
    @DisplayName("D12. The sensitivity and specificity of a detection intervention come from the detection strategy of its arm after the replacement by type")
    public void readsDetectionStrategyOfArm() {
        // Screening arm: screening test (0.9; 0.95). Arm without screening: clinical diagnosis (0.7; without specificity, 1)
        final Patient comparatorPatient = d12.comparatorPatients()[0];
        final Patient assessedPatient = d12.assessedPatients()[0];
        assertAll("Sensitivity and specificity of each arm",
            () -> assertEquals(0.9, d12.assessed().getUsedParameterValue(StandardParameter.SENSITIVITY, assessedPatient), TOLERANCE, "Sensitivity of the screening arm"),
            () -> assertEquals(0.95, d12.assessed().getUsedParameterValue(StandardParameter.SPECIFICITY, assessedPatient), TOLERANCE, "Specificity of the screening arm"),
            () -> assertEquals(0.7, d12.comparator().getUsedParameterValue(StandardParameter.SENSITIVITY, comparatorPatient), TOLERANCE, "Sensitivity of the arm without screening"),
            () -> assertEquals(1.0, d12.comparator().getUsedParameterValue(StandardParameter.SPECIFICITY, comparatorPatient), TOLERANCE, "Specificity of the arm without screening"));
    }
}
