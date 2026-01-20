package es.ull.simulation.model.flow;

import es.ull.simulation.model.ElementInstance;
import es.ull.simulation.model.Simulation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for UserActionFlow
 */
class UserActionFlowTest {
    private Simulation simulation;
    private UserActionFlow userActionFlow;

    @BeforeEach
    void setUp() {
        simulation = new Simulation(1, "Test Simulation");
        userActionFlow = new UserActionFlow(simulation, "Test User Action");
    }

    @Test
    void shouldCreateUserActionFlow_whenValidParametersProvided() {
        assertNotNull(userActionFlow);
        assertEquals("Test User Action", userActionFlow.getDescription());
    }

    @Test
    void shouldReturnCorrectDescription_whenQueried() {
        UserActionFlow flow = new UserActionFlow(simulation, "Custom Description");
        assertEquals("Custom Description", flow.getDescription());
    }

    @Test
    void shouldCreateUserActionFlow_withEmptyDescription() {
        UserActionFlow flow = new UserActionFlow(simulation, "");
        assertEquals("", flow.getDescription());
    }

    @Test
    void shouldCreateUserActionFlow_withNullDescription() {
        UserActionFlow flow = new UserActionFlow(simulation, null);
        assertNull(flow.getDescription());
    }

    @Test
    void shouldCreateUserActionFlow_withLongDescription() {
        String longDescription = "This is a very long description for a user action flow that tests the ability to handle longer text descriptions in the flow system";
        UserActionFlow flow = new UserActionFlow(simulation, longDescription);
        assertEquals(longDescription, flow.getDescription());
    }

    @Test
    void shouldImplementIActionFlow() {
        assertTrue(userActionFlow instanceof IActionFlow);
    }

    @Test
    void shouldExtendAbstractSingleSuccessorFlow() {
        assertTrue(userActionFlow instanceof AbstractSingleSuccessorFlow);
    }

    @Test
    void shouldHaveGetDescriptionMethod() {
        assertDoesNotThrow(() -> userActionFlow.getDescription());
    }

    @Test
    void shouldHaveUserActionMethod() {
        // Verify the method exists and can be called with a custom subclass
        TestUserActionFlow testFlow = new TestUserActionFlow(simulation, "Test");
        assertDoesNotThrow(() -> testFlow.userAction(null));
    }

    @Test
    void shouldCreateMultipleUserActionFlows_withDifferentDescriptions() {
        UserActionFlow flow1 = new UserActionFlow(simulation, "Action 1");
        UserActionFlow flow2 = new UserActionFlow(simulation, "Action 2");
        UserActionFlow flow3 = new UserActionFlow(simulation, "Action 3");

        assertEquals("Action 1", flow1.getDescription());
        assertEquals("Action 2", flow2.getDescription());
        assertEquals("Action 3", flow3.getDescription());
    }

    @Test
    void shouldBeExtendable_asAbstractClass() {
        TestUserActionFlow testFlow = new TestUserActionFlow(simulation, "Test Flow");
        assertEquals("Test Flow", testFlow.getDescription());
        assertTrue(testFlow.actionExecuted == false);
    }

    /**
     * Test subclass to verify extensibility
     */
    private static class TestUserActionFlow extends UserActionFlow {
        boolean actionExecuted = false;

        public TestUserActionFlow(Simulation model, String description) {
            super(model, description);
        }

        @Override
        public void userAction(ElementInstance ei) {
            actionExecuted = true;
        }
    }
}
