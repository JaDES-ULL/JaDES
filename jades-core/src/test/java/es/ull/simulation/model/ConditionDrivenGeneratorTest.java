package es.ull.simulation.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.ull.simulation.condition.AbstractCondition;
import es.ull.simulation.condition.TrueCondition;
import es.ull.simulation.functions.ConstantFunction;
import es.ull.simulation.model.flow.ActivityFlow;

/**
 * Test class for {@link ConditionDrivenGenerator}.
 * Tests condition-driven element generation functionality.
 */
public class ConditionDrivenGeneratorTest {

    private Simulation simulation;
    private AbstractCondition<ElementInstance> condition;
    private ElementType elementType;
    private ActivityFlow flow;

    @BeforeEach
    public void setUp() {
        simulation = new Simulation(0, "Test Simulation", TimeUnit.MINUTE);
        condition = new TrueCondition<>();
        elementType = new ElementType(simulation, "Test Element Type");
        flow = new ActivityFlow(simulation, "Test Activity");
    }

    @Test
    public void shouldCreateGeneratorWithFixedElements() {
        // Given: number of elements to generate
        int nElem = 5;

        // When: creating a condition-driven generator with fixed number
        ConditionDrivenGenerator<StandardElementGenerationInfo> generator =
            new ConditionDrivenGenerator<StandardElementGenerationInfo>(simulation, nElem, condition) {
                @Override
                public Element createEventSource(int ind, StandardElementGenerationInfo info) {
                    return new Element(simulation, info);
                }
            };

        // Then: it should be created successfully
        assertNotNull(generator);
        assertEquals(condition, generator.getCondition());
    }

    @Test
    public void shouldCreateGeneratorWithFunctionElements() {
        // Given: function for number of elements
        ConstantFunction nElemFunction = new ConstantFunction(10);

        // When: creating a condition-driven generator with function
        ConditionDrivenGenerator<StandardElementGenerationInfo> generator =
            new ConditionDrivenGenerator<StandardElementGenerationInfo>(simulation, nElemFunction, condition) {
                @Override
                public Element createEventSource(int ind, StandardElementGenerationInfo info) {
                    return new Element(simulation, info);
                }
            };

        // Then: it should be created successfully
        assertNotNull(generator);
        assertEquals(condition, generator.getCondition());
    }

    @Test
    public void shouldBelongToSimulation() {
        // Given: a generator
        int nElem = 3;
        ConditionDrivenGenerator<StandardElementGenerationInfo> generator =
            new ConditionDrivenGenerator<StandardElementGenerationInfo>(simulation, nElem, condition) {
                @Override
                public Element createEventSource(int ind, StandardElementGenerationInfo info) {
                    return new Element(simulation, info);
                }
            };

        // Then: it should be registered in simulation
        assertTrue(simulation.getConditionDrivenGeneratorList().contains(generator));
    }

    @Test
    public void shouldHaveObjectTypeIdentifier() {
        // Given: a generator
        int nElem = 2;
        ConditionDrivenGenerator<StandardElementGenerationInfo> generator =
            new ConditionDrivenGenerator<StandardElementGenerationInfo>(simulation, nElem, condition) {
                @Override
                public Element createEventSource(int ind, StandardElementGenerationInfo info) {
                    return new Element(simulation, info);
                }
            };

        // When: getting its identifier
        String id = generator.toString();

        // Then: it should have "GEN" identifier
        assertTrue(id.startsWith("[GEN"));
        assertTrue(id.endsWith("]"));
    }

    @Test
    public void shouldHaveSequentialIds() {
        // Given: multiple generators
        int nElem = 1;
        ConditionDrivenGenerator<StandardElementGenerationInfo> gen1 =
            new ConditionDrivenGenerator<StandardElementGenerationInfo>(simulation, nElem, condition) {
                @Override
                public Element createEventSource(int ind, StandardElementGenerationInfo info) {
                    return new Element(simulation, info);
                }
            };
        ConditionDrivenGenerator<StandardElementGenerationInfo> gen2 =
            new ConditionDrivenGenerator<StandardElementGenerationInfo>(simulation, nElem, condition) {
                @Override
                public Element createEventSource(int ind, StandardElementGenerationInfo info) {
                    return new Element(simulation, info);
                }
            };

        // Then: they should have sequential IDs
        int id1 = Integer.parseInt(gen1.toString().replaceAll("[^0-9]", ""));
        int id2 = Integer.parseInt(gen2.toString().replaceAll("[^0-9]", ""));
        assertTrue(id2 > id1);
    }

    @Test
    public void shouldStoreCondition() {
        // Given: a specific condition
        AbstractCondition<ElementInstance> specificCondition = new TrueCondition<>();

        // When: creating a generator with this condition
        int nElem = 2;
        ConditionDrivenGenerator<StandardElementGenerationInfo> generator =
            new ConditionDrivenGenerator<StandardElementGenerationInfo>(simulation, nElem, specificCondition) {
                @Override
                public Element createEventSource(int ind, StandardElementGenerationInfo info) {
                    return new Element(simulation, info);
                }
            };

        // Then: the condition should be stored
        assertSame(specificCondition, generator.getCondition());
    }

    @Test
    public void shouldHandleZeroElements() {
        // Given: generator with zero elements
        int nElem = 0;

        // When: creating the generator
        ConditionDrivenGenerator<StandardElementGenerationInfo> generator =
            new ConditionDrivenGenerator<StandardElementGenerationInfo>(simulation, nElem, condition) {
                @Override
                public Element createEventSource(int ind, StandardElementGenerationInfo info) {
                    return new Element(simulation, info);
                }
            };

        // Then: it should be created successfully
        assertNotNull(generator);
    }

    @Test
    public void shouldHandleLargeNumberOfElements() {
        // Given: generator with large number of elements
        int nElem = 1000;

        // When: creating the generator
        ConditionDrivenGenerator<StandardElementGenerationInfo> generator =
            new ConditionDrivenGenerator<StandardElementGenerationInfo>(simulation, nElem, condition) {
                @Override
                public Element createEventSource(int ind, StandardElementGenerationInfo info) {
                    return new Element(simulation, info);
                }
            };

        // Then: it should be created successfully
        assertNotNull(generator);
    }

    @Test
    public void shouldAllowAddingGenerationInfo() {
        // Given: a generator
        int nElem = 3;
        ConditionDrivenGenerator<StandardElementGenerationInfo> generator =
            new ConditionDrivenGenerator<StandardElementGenerationInfo>(simulation, nElem, condition) {
                @Override
                public Element createEventSource(int ind, StandardElementGenerationInfo info) {
                    return new Element(simulation, info);
                }
            };

        // When: adding generation info
        StandardElementGenerationInfo info = new StandardElementGenerationInfo(elementType, flow, 1, null, 1.0);
        generator.add(info);

        // Then: generator should accept the info
        assertNotNull(generator);
    }
}
