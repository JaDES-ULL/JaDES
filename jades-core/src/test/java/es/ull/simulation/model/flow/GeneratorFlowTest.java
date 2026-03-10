package es.ull.simulation.model.flow;

import es.ull.simulation.model.ElementInstance;
import es.ull.simulation.model.Simulation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for GeneratorFlow
 */
class GeneratorFlowTest {
    private Simulation simulation;
    private GeneratorFlow generatorFlow;

    @BeforeEach
    void setUp() {
        simulation = new Simulation(1, "Test Simulation");
        generatorFlow = new GeneratorFlow(simulation, "Test Generator");
    }

    @Test
    void shouldCreateGeneratorFlow_whenValidParametersProvided() {
        assertNotNull(generatorFlow);
        assertEquals("Test Generator", generatorFlow.getDescription());
    }

    @Test
    void shouldReturnCorrectDescription_whenQueried() {
        GeneratorFlow flow = new GeneratorFlow(simulation, "Custom Generator");
        assertEquals("Custom Generator", flow.getDescription());
    }

    @Test
    void shouldCreateGeneratorFlow_withEmptyDescription() {
        GeneratorFlow flow = new GeneratorFlow(simulation, "");
        assertEquals("", flow.getDescription());
    }

    @Test
    void shouldCreateGeneratorFlow_withNullDescription() {
        GeneratorFlow flow = new GeneratorFlow(simulation, null);
        assertNull(flow.getDescription());
    }

    @Test
    void shouldCreateGeneratorFlow_withLongDescription() {
        String longDescription = "This is a very long description for a generator flow that tests the ability to handle longer text descriptions in the flow system";
        GeneratorFlow flow = new GeneratorFlow(simulation, longDescription);
        assertEquals(longDescription, flow.getDescription());
    }

    @Test
    void shouldImplementIActionFlow() {
        assertTrue(generatorFlow instanceof IActionFlow);
    }

    @Test
    void shouldImplementITaskFlow() {
        assertTrue(generatorFlow instanceof ITaskFlow);
    }

    @Test
    void shouldExtendAbstractSingleSuccessorFlow() {
        assertTrue(generatorFlow instanceof AbstractSingleSuccessorFlow);
    }

    @Test
    void shouldHaveGetDescriptionMethod() {
        assertDoesNotThrow(() -> generatorFlow.getDescription());
    }

    @Test
    void shouldHaveCreateMethod() {
        // Verify the method exists and can be called with a custom subclass
        TestGeneratorFlow testFlow = new TestGeneratorFlow(simulation, "Test");
        assertDoesNotThrow(() -> testFlow.create(null));
    }

    @Test
    void shouldHaveAfterFinalizeMethod() {
        // Verify the afterFinalize method exists
        TestGeneratorFlow testFlow = new TestGeneratorFlow(simulation, "Test");
        assertDoesNotThrow(() -> testFlow.afterFinalize(null));
    }

    @Test
    void shouldCreateMultipleGeneratorFlows_withDifferentDescriptions() {
        GeneratorFlow flow1 = new GeneratorFlow(simulation, "Generator 1");
        GeneratorFlow flow2 = new GeneratorFlow(simulation, "Generator 2");
        GeneratorFlow flow3 = new GeneratorFlow(simulation, "Generator 3");

        assertEquals("Generator 1", flow1.getDescription());
        assertEquals("Generator 2", flow2.getDescription());
        assertEquals("Generator 3", flow3.getDescription());
    }

    @Test
    void shouldBeExtendable_asAbstractClass() {
        TestGeneratorFlow testFlow = new TestGeneratorFlow(simulation, "Test Flow");
        assertEquals("Test Flow", testFlow.getDescription());
        assertFalse(testFlow.createExecuted);
        assertFalse(testFlow.afterFinalizeExecuted);
    }

    // ── Cobertura de métodos del cuerpo de GeneratorFlow ──────────────────────

    @Test
    void shouldNotThrow_whenAddPredecessorCalledOnBaseClass() {
        // addPredecessor es un no-op en GeneratorFlow
        generatorFlow.addPredecessor(null);
    }

    @Test
    void shouldNotThrow_whenBaseCreateCalledWithNull() {
        // Crear instancia directa (no subclase) → llama al create() vacío de GeneratorFlow
        GeneratorFlow baseFlow = new GeneratorFlow(simulation, "base");
        baseFlow.create(null);
    }

    @Test
    void shouldNotThrow_whenBaseAfterFinalizeCalledWithNull() {
        // afterFinalize es un no-op en GeneratorFlow
        GeneratorFlow baseFlow = new GeneratorFlow(simulation, "base");
        baseFlow.afterFinalize(null);
    }

    /**
     * Test subclass to verify extensibility
     */
    private static class TestGeneratorFlow extends GeneratorFlow {
        boolean createExecuted = false;
        boolean afterFinalizeExecuted = false;

        public TestGeneratorFlow(Simulation model, String description) {
            super(model, description);
        }

        @Override
        public void create(ElementInstance ei) {
            createExecuted = true;
        }

        @Override
        public void afterFinalize(ElementInstance ei) {
            afterFinalizeExecuted = true;
        }
    }
}
