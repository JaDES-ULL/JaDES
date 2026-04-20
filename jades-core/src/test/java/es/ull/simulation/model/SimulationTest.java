package es.ull.simulation.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.ull.simulation.info.IPieceOfInformation;
import es.ull.simulation.info.SimulationStartStopInfo;
import es.ull.simulation.inforeceiver.BasicListener;

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
        int firstId = simulation.generateId();
        int secondId = simulation.generateId();
        int thirdId = simulation.generateId();

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

    // -----------------------------------------------------------------------
    // toString, simulationTime2Long, long2SimulationTime
    // -----------------------------------------------------------------------

    @Test
    void shouldReturnFormattedString_inToString() {
        assertEquals("[SIM" + SIMULATION_ID + "]", simulation.toString());
    }

    @Test
    void shouldConvertTimeStampToLong_inDefaultTimeUnit() {
        long result = simulation.simulationTime2Long(new TimeStamp(TimeUnit.MINUTE, 5));

        assertEquals(5L, result);
    }

    @Test
    void shouldConvertTimeStampToLong_fromHoursToMinutes() {
        // Default unit is MINUTE; 1 HOUR = 60 MINUTES
        long result = simulation.simulationTime2Long(new TimeStamp(TimeUnit.HOUR, 1));

        assertEquals(60L, result);
    }

    @Test
    void shouldConvertLongToTimeStamp_inDefaultTimeUnit() {
        TimeStamp ts = simulation.long2SimulationTime(10L);

        assertNotNull(ts);
        assertEquals(10L, ts.getValue());
        assertEquals(TimeUnit.MINUTE, ts.getUnit());
    }

    // -----------------------------------------------------------------------
    // reset() — resets id generator
    // -----------------------------------------------------------------------

    @Test
    void shouldResetIdGenerator_whenResetCalled() {
        simulation.generateId(); // 0
        simulation.generateId(); // 1
        simulation.reset();

        assertEquals(0, simulation.generateId()); // back to 0
    }

    // -----------------------------------------------------------------------
    // putVar / getVar — covers all 8 primitive overloads and both branches
    // -----------------------------------------------------------------------

    @Test
    void shouldStoreAndRetrieveDoubleVar() {
        simulation.putVar("d", 3.14);

        assertNotNull(simulation.getVar("d"));
        assertEquals(3.14, simulation.getVar("d").getValue(null).doubleValue(), 1e-9);
    }

    @Test
    void shouldUpdateExistingDoubleVar() {
        simulation.putVar("d", 1.0);
        simulation.putVar("d", 2.0); // hits if-branch: v != null

        assertEquals(2.0, simulation.getVar("d").getValue(null).doubleValue(), 1e-9);
    }

    @Test
    void shouldStoreAndRetrieveIntVar() {
        simulation.putVar("i", 42);

        assertEquals(42, simulation.getVar("i").getValue(null).intValue());
    }

    @Test
    void shouldUpdateExistingIntVar() {
        simulation.putVar("i", 10);
        simulation.putVar("i", 20);

        assertEquals(20, simulation.getVar("i").getValue(null).intValue());
    }

    @Test
    void shouldStoreAndRetrieveBooleanVar() {
        simulation.putVar("b", true);

        assertEquals(true, simulation.getVar("b").getValue(null).intValue() != 0);
    }

    @Test
    void shouldUpdateExistingBooleanVar() {
        simulation.putVar("b", true);
        simulation.putVar("b", false);

        assertEquals(0, simulation.getVar("b").getValue(null).intValue());
    }

    @Test
    void shouldStoreAndRetrieveCharVar() {
        simulation.putVar("c", 'X');

        assertNotNull(simulation.getVar("c"));
    }

    @Test
    void shouldUpdateExistingCharVar() {
        simulation.putVar("c", 'A');
        simulation.putVar("c", 'Z');

        assertNotNull(simulation.getVar("c"));
    }

    @Test
    void shouldStoreAndRetrieveByteVar() {
        simulation.putVar("by", (byte) 7);

        assertEquals(7, simulation.getVar("by").getValue(null).byteValue());
    }

    @Test
    void shouldUpdateExistingByteVar() {
        simulation.putVar("by", (byte) 1);
        simulation.putVar("by", (byte) 9);

        assertEquals(9, simulation.getVar("by").getValue(null).byteValue());
    }

    @Test
    void shouldStoreAndRetrieveFloatVar() {
        simulation.putVar("f", 1.5f);

        assertEquals(1.5f, simulation.getVar("f").getValue(null).floatValue(), 1e-6f);
    }

    @Test
    void shouldUpdateExistingFloatVar() {
        simulation.putVar("f", 1.0f);
        simulation.putVar("f", 2.0f);

        assertEquals(2.0f, simulation.getVar("f").getValue(null).floatValue(), 1e-6f);
    }

    @Test
    void shouldStoreAndRetrieveLongVar() {
        simulation.putVar("l", 999L);

        assertEquals(999L, simulation.getVar("l").getValue(null).longValue());
    }

    @Test
    void shouldUpdateExistingLongVar() {
        simulation.putVar("l", 100L);
        simulation.putVar("l", 200L);

        assertEquals(200L, simulation.getVar("l").getValue(null).longValue());
    }

    @Test
    void shouldStoreAndRetrieveShortVar() {
        simulation.putVar("s", (short) 5);

        assertEquals((short) 5, simulation.getVar("s").getValue(null).shortValue());
    }

    @Test
    void shouldUpdateExistingShortVar() {
        simulation.putVar("s", (short) 1);
        simulation.putVar("s", (short) 3);

        assertEquals((short) 3, simulation.getVar("s").getValue(null).shortValue());
    }

    @Test
    void shouldReturnNull_getVar_whenVariableNotDefined() {
        assertNull(simulation.getVar("nonexistent"));
    }

    // -----------------------------------------------------------------------
    // registerListener / notifyInfo / getListeners
    // -----------------------------------------------------------------------

    @Test
    void shouldReturnEmptyListeners_whenNoneRegistered() {
        assertTrue(simulation.getListeners().isEmpty());
    }

    @Test
    void shouldReturnListenersList_afterRegisterListener() {
        BasicListener listener = new BasicListener("test-listener") {
            @Override
            public void infoEmited(IPieceOfInformation info) {}
        };
        listener.addTargetInformation(SimulationStartStopInfo.class);

        simulation.registerListener(listener);

        assertFalse(simulation.getListeners().isEmpty());
        assertEquals(1, simulation.getListeners().size());
    }

    @Test
    void shouldNotifyListeners_whenNotifyInfoCalled() {
        final boolean[] called = {false};
        BasicListener listener = new BasicListener("notify-listener") {
            @Override
            public void infoEmited(IPieceOfInformation info) {
                called[0] = true;
            }
        };
        listener.addTargetInformation(SimulationStartStopInfo.class);
        simulation.registerListener(listener);

        simulation.notifyInfo(new SimulationStartStopInfo(simulation,
                SimulationStartStopInfo.Type.START, 0L));

        assertTrue(called[0]);
    }

    @Test
    void shouldReturnTypedListeners_viaGetListeners_withClass() {
        BasicListener listener = new BasicListener("typed-listener") {
            @Override
            public void infoEmited(IPieceOfInformation info) {}
        };
        listener.addTargetInformation(SimulationStartStopInfo.class);
        simulation.registerListener(listener);

        var list = simulation.getListeners(SimulationStartStopInfo.class);

        assertNotNull(list);
        assertEquals(1, list.size());
    }

    // -----------------------------------------------------------------------
    // beforeClockTick / afterClockTick — empty user methods (coverage only)
    // -----------------------------------------------------------------------

    @Test
    void shouldNotThrow_beforeClockTick() {
        simulation.beforeClockTick();
    }

    @Test
    void shouldNotThrow_afterClockTick() {
        simulation.afterClockTick();
    }

    // -----------------------------------------------------------------------
    // getStartTs / getEndTs — covered indirectly but add explicit test
    // -----------------------------------------------------------------------

    @Test
    void shouldReturnZeroStartTs_whenNotRunYet() {
        assertEquals(0L, simulation.getStartTs());
    }

    @Test
    void shouldReturnZeroEndTs_whenNotRunYet() {
        assertEquals(0L, simulation.getEndTs());
    }
}
