package es.ull.simulation.examples.hospital;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BasicHospitalModelTest {

    @Test
    void shouldBuildHospitalModelWithExpectedComponents() {
        BasicHospitalModel model = new BasicHospitalModel(1);

        assertEquals(1, model.getElementTypeList().size());
        assertEquals(3, model.getResourceTypeList().size());
        assertEquals(9, model.getResourceList().size());
        assertTrue(model.getWorkGroupList().size() >= 3);
        assertEquals(1, model.getTimeDrivenGeneratorList().size());
        assertEquals(2, model.getRequestFlowList().size());
        assertTrue(model.getFlowList().size() >= 7);
    }
}
