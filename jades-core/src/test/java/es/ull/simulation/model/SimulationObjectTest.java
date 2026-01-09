package es.ull.simulation.model;

import es.ull.simulation.model.engine.SimulationEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SimulationObject abstract class
 */
class SimulationObjectTest {

    private ISimulationContext context;

    /**
     * Concrete test implementation of SimulationObject
     */
    private static class TestSimulationObject extends SimulationObject {
        public TestSimulationObject(ISimulationContext context, int id, String objectTypeId) {
            super(context, id, objectTypeId);
        }

        @Override
        protected void assignSimulation(SimulationEngine engine) {
            // Mock implementation
        }
    }

    @BeforeEach
    void setUp() {
        context = new MockSimulationContext(42);
    }

    @Test
    void shouldCallDebug_withMessage() {
        TestSimulationObject obj = new TestSimulationObject(context, 1, "TST");

        // Should not throw exception
        assertDoesNotThrow(() -> obj.debug("Test debug message"));
    }

    @Test
    void shouldCallError_withDescription() {
        TestSimulationObject obj = new TestSimulationObject(context, 2, "ERR");

        // Should not throw exception
        assertDoesNotThrow(() -> obj.error("Test error description"));
    }

    @Test
    void shouldGetTime_returnsSimulationTimestamp() {
        TestSimulationObject obj = new TestSimulationObject(context, 3, "OBJ");

        double time = obj.getTime();

        assertEquals(42.0, time, 0.001);
    }

    @Test
    void shouldGetTs_returnsSimulationTimestamp() {
        TestSimulationObject obj = new TestSimulationObject(context, 4, "OBJ");

        long ts = obj.getTs();

        assertEquals(42, ts);
    }

    @Test
    void shouldDebug_withMultipleMessages() {
        TestSimulationObject obj = new TestSimulationObject(context, 5, "MSG");

        assertDoesNotThrow(() -> {
            obj.debug("First message");
            obj.debug("Second message");
            obj.debug("Third message");
        });
    }

    @Test
    void shouldError_withMultipleDescriptions() {
        TestSimulationObject obj = new TestSimulationObject(context, 6, "ERR");

        assertDoesNotThrow(() -> {
            obj.error("First error");
            obj.error("Second error");
        });
    }

    @Test
    void shouldGetTime_matchesGetTs() {
        TestSimulationObject obj = new TestSimulationObject(context, 7, "OBJ");

        double time = obj.getTime();
        long ts = obj.getTs();

        assertEquals((double) ts, time, 0.001);
    }

    @Test
    void shouldDebug_withEmptyMessage() {
        TestSimulationObject obj = new TestSimulationObject(context, 8, "TST");

        assertDoesNotThrow(() -> obj.debug(""));
    }

    @Test
    void shouldError_withEmptyDescription() {
        TestSimulationObject obj = new TestSimulationObject(context, 9, "ERR");

        assertDoesNotThrow(() -> obj.error(""));
    }
}
