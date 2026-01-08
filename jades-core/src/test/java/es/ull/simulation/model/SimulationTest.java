package es.ull.simulation.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SimulationTest {
    private Simulation simulation;
    private static final int SIMULATION_ID = 1;
    private static final String DESCRIPTION = "Test Simulation";

    @BeforeEach
    void setUp() {
        simulation = new Simulation(SIMULATION_ID, DESCRIPTION);
    }

    @Test
    void shouldInitializeWithCorrectId_whenCreated() {
        assertEquals(SIMULATION_ID, simulation.getIdentifier());
    }

    @Test
    void shouldInitializeWithCorrectDescription_whenCreated() {
        assertEquals(DESCRIPTION, simulation.getDescription());
    }

    @Test
    void shouldUseDefaultTimeUnit_whenNoUnitProvided() {
        assertEquals(TimeUnit.MINUTE, simulation.getTimeUnit());
    }

    @Test
    void shouldUseCustomTimeUnit_whenProvided() {
        Simulation simWithCustomUnit = new Simulation(2, "Custom Unit Sim", TimeUnit.HOUR);
        assertEquals(TimeUnit.HOUR, simWithCustomUnit.getTimeUnit());
    }

    @Test
    void shouldGenerateUniqueElementIds_whenRequested() {
        int firstId = simulation.getNewElementId();
        int secondId = simulation.getNewElementId();
        int thirdId = simulation.getNewElementId();

        assertEquals(0, firstId);
        assertEquals(1, secondId);
        assertEquals(2, thirdId);
    }

    @Test
    void shouldReturnEmptyElementTypeList_whenNoTypesAdded() {
        assertNotNull(simulation.getElementTypeList());
        assertTrue(simulation.getElementTypeList().isEmpty());
    }

    @Test
    void shouldReturnEmptyResourceList_whenNoResourcesAdded() {
        assertNotNull(simulation.getResourceList());
        assertTrue(simulation.getResourceList().isEmpty());
    }

    @Test
    void shouldReturnEmptyResourceTypeList_whenNoResourceTypesAdded() {
        assertNotNull(simulation.getResourceTypeList());
        assertTrue(simulation.getResourceTypeList().isEmpty());
    }

    @Test
    void shouldReturnEmptyWorkGroupList_whenNoWorkGroupsAdded() {
        assertNotNull(simulation.getWorkGroupList());
        assertTrue(simulation.getWorkGroupList().isEmpty());
    }

    @Test
    void shouldReturnEmptyFlowList_whenNoFlowsAdded() {
        assertNotNull(simulation.getFlowList());
        assertTrue(simulation.getFlowList().isEmpty());
    }

    @Test
    void shouldReturnEmptyRequestFlowList_whenNoRequestFlowsAdded() {
        assertNotNull(simulation.getRequestFlowList());
        assertTrue(simulation.getRequestFlowList().isEmpty());
    }

    @Test
    void shouldReturnEmptyTimeDrivenGeneratorList_whenNoGeneratorsAdded() {
        assertNotNull(simulation.getTimeDrivenGeneratorList());
        assertTrue(simulation.getTimeDrivenGeneratorList().isEmpty());
    }

    @Test
    void shouldReturnEmptyConditionDrivenGeneratorList_whenNoGeneratorsAdded() {
        assertNotNull(simulation.getConditionDrivenGeneratorList());
        assertTrue(simulation.getConditionDrivenGeneratorList().isEmpty());
    }

    @Test
    void shouldReturnEmptyActivityManagerList_whenNoManagersAdded() {
        assertNotNull(simulation.getActivityManagerList());
        assertTrue(simulation.getActivityManagerList().isEmpty());
    }

    @Test
    void shouldReturnSimulationEnd_whenCurrentTimestampEqualsEnd() {
        simulation = new Simulation(3, "End Test");
        // Simulamos que ya se ejecutó y el timestamp actual es >= endTs
        assertTrue(simulation.isSimulationEnd(100L));
    }

    @Test
    void shouldNotReturnSimulationEnd_whenCurrentTimestampLessThanEnd() {
        simulation = new Simulation(4, "Not End Test");
        // Por defecto, endTs es 0, así que cualquier valor negativo no debería acabar
        assertFalse(simulation.isSimulationEnd(-10L));
    }

    @Test
    void shouldHaveNullSimulationEngine_whenNotSet() {
        assertEquals(null, simulation.getSimulationEngine());
    }
}
