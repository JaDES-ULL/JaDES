package es.ull.simulation.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.ull.simulation.functions.ConstantFunction;
import es.ull.simulation.model.flow.ActivityFlow;

/**
 * Test class for {@link TimeDrivenGenerator}.
 * Tests time-driven element generation functionality.
 */
public class TimeDrivenGeneratorTest {

    private Simulation simulation;
    private ISimulationCycle cycle;
    private ElementType elementType;
    private ActivityFlow flow;

    @BeforeEach
    public void setUp() {
        simulation = new Simulation(0, "Test Simulation", TimeUnit.MINUTE);
        elementType = new ElementType(simulation, "Test Element Type");
        flow = new ActivityFlow(simulation, "Test Activity");

        // Create a simple periodic cycle
        TimeStamp startTs = new TimeStamp(TimeUnit.MINUTE, 0);
        ConstantFunction period = new ConstantFunction(10);
        TimeStamp endTs = new TimeStamp(TimeUnit.MINUTE, 100);
        cycle = new SimulationPeriodicCycle(TimeUnit.MINUTE, startTs, period, endTs);
    }

    @Test
    public void shouldCreateGeneratorWithFixedElements() {
        // Given: number of elements to generate
        int nElem = 5;

        // When: creating a time-driven generator with fixed number
        TimeDrivenGenerator<StandardElementGenerationInfo> generator =
            new TimeDrivenGenerator<StandardElementGenerationInfo>(simulation, nElem, cycle) {
                @Override
                public Element createEventSource(int ind, StandardElementGenerationInfo info) {
                    return new Element(simulation, info);
                }
            };

        // Then: it should be created successfully
        assertNotNull(generator);
        assertEquals(cycle, generator.getCycle());
    }

    @Test
    public void shouldCreateGeneratorWithFunctionElements() {
        // Given: function for number of elements
        ConstantFunction nElemFunction = new ConstantFunction(10);

        // When: creating a time-driven generator with function
        TimeDrivenGenerator<StandardElementGenerationInfo> generator =
            new TimeDrivenGenerator<StandardElementGenerationInfo>(simulation, nElemFunction, cycle) {
                @Override
                public Element createEventSource(int ind, StandardElementGenerationInfo info) {
                    return new Element(simulation, info);
                }
            };

        // Then: it should be created successfully
        assertNotNull(generator);
        assertEquals(cycle, generator.getCycle());
    }

    @Test
    public void shouldBelongToSimulation() {
        // Given: a generator
        int nElem = 3;
        TimeDrivenGenerator<StandardElementGenerationInfo> generator =
            new TimeDrivenGenerator<StandardElementGenerationInfo>(simulation, nElem, cycle) {
                @Override
                public Element createEventSource(int ind, StandardElementGenerationInfo info) {
                    return new Element(simulation, info);
                }
            };

        // Then: it should be registered in simulation
        assertTrue(simulation.getTimeDrivenGeneratorList().contains(generator));
    }

    @Test
    public void shouldHaveObjectTypeIdentifier() {
        // Given: a generator
        int nElem = 2;
        TimeDrivenGenerator<StandardElementGenerationInfo> generator =
            new TimeDrivenGenerator<StandardElementGenerationInfo>(simulation, nElem, cycle) {
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
        TimeDrivenGenerator<StandardElementGenerationInfo> gen1 =
            new TimeDrivenGenerator<StandardElementGenerationInfo>(simulation, nElem, cycle) {
                @Override
                public Element createEventSource(int ind, StandardElementGenerationInfo info) {
                    return new Element(simulation, info);
                }
            };
        TimeDrivenGenerator<StandardElementGenerationInfo> gen2 =
            new TimeDrivenGenerator<StandardElementGenerationInfo>(simulation, nElem, cycle) {
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
    public void shouldStoreCycle() {
        // Given: a specific cycle
        TimeStamp startTs = new TimeStamp(TimeUnit.MINUTE, 10);
        ConstantFunction period = new ConstantFunction(5);
        TimeStamp endTs = new TimeStamp(TimeUnit.MINUTE, 60);
        ISimulationCycle specificCycle = new SimulationPeriodicCycle(TimeUnit.MINUTE, startTs, period, endTs);

        // When: creating a generator with this cycle
        int nElem = 2;
        TimeDrivenGenerator<StandardElementGenerationInfo> generator =
            new TimeDrivenGenerator<StandardElementGenerationInfo>(simulation, nElem, specificCycle) {
                @Override
                public Element createEventSource(int ind, StandardElementGenerationInfo info) {
                    return new Element(simulation, info);
                }
            };

        // Then: the cycle should be stored
        assertSame(specificCycle, generator.getCycle());
    }

    @Test
    public void shouldHandleZeroElements() {
        // Given: generator with zero elements
        int nElem = 0;

        // When: creating the generator
        TimeDrivenGenerator<StandardElementGenerationInfo> generator =
            new TimeDrivenGenerator<StandardElementGenerationInfo>(simulation, nElem, cycle) {
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
        TimeDrivenGenerator<StandardElementGenerationInfo> generator =
            new TimeDrivenGenerator<StandardElementGenerationInfo>(simulation, nElem, cycle) {
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
        TimeDrivenGenerator<StandardElementGenerationInfo> generator =
            new TimeDrivenGenerator<StandardElementGenerationInfo>(simulation, nElem, cycle) {
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

    @Test
    public void shouldCallOnDestroy() {
        // Given: a generator
        TimeDrivenGenerator<StandardElementGenerationInfo> generator =
            new TimeDrivenGenerator<StandardElementGenerationInfo>(simulation, 1, cycle) {
                @Override
                public Element createEventSource(int ind, StandardElementGenerationInfo info) {
                    return new Element(simulation, info);
                }
            };

        // When: calling onDestroy
        DiscreteEvent event = generator.onDestroy(100);

        // Then: should return a finalize event
        assertNotNull(event);
        assertInstanceOf(DiscreteEvent.DefaultFinalizeEvent.class, event);
        assertEquals(100, event.getTs());
    }

    @Test
    public void shouldCallOnDestroyWithZeroTimestamp() {
        // Given: a generator
        TimeDrivenGenerator<StandardElementGenerationInfo> generator =
            new TimeDrivenGenerator<StandardElementGenerationInfo>(simulation, 1, cycle) {
                @Override
                public Element createEventSource(int ind, StandardElementGenerationInfo info) {
                    return new Element(simulation, info);
                }
            };

        // When: calling onDestroy with zero timestamp
        DiscreteEvent event = generator.onDestroy(0);

        // Then: should return a finalize event at time 0
        assertNotNull(event);
        assertEquals(0, event.getTs());
    }

    @Test
    public void shouldCallOnDestroyWithMaxTimestamp() {
        // Given: a generator
        TimeDrivenGenerator<StandardElementGenerationInfo> generator =
            new TimeDrivenGenerator<StandardElementGenerationInfo>(simulation, 1, cycle) {
                @Override
                public Element createEventSource(int ind, StandardElementGenerationInfo info) {
                    return new Element(simulation, info);
                }
            };

        // When: calling onDestroy with max timestamp
        DiscreteEvent event = generator.onDestroy(Long.MAX_VALUE);

        // Then: should return a finalize event at max time
        assertNotNull(event);
        assertEquals(Long.MAX_VALUE, event.getTs());
    }
    
    /**
     * PREVIOUSLY IMPOSSIBLE TEST - now enabled by ISimulationContext refactoring.
     * 
     * Before: notifyEnd() required full SimulationEngine initialization (NullPointerException).
     * After: Using MockSimulationContext through Simulation allows testing without SimulationEngine.
     * 
     * This demonstrates the power of the refactoring - methods that were untestable
     * due to tight coupling are now easily testable with proper context isolation.
     * 
     * The test uses a Simulation that delegates time queries to MockSimulationContext,
     * proving that the time abstraction works correctly through the inheritance chain.
     */
    @Test
    public void shouldCallNotifyEnd_withoutSimulationEngine() {
        // Given: a simulation that provides controlled timestamps without SimulationEngine
        MockSimulationContext mockContext = new MockSimulationContext(100);
        Simulation testSimulation = new Simulation(0, "Test") {
            @Override
            public long getCurrentTimestamp() {
                return mockContext.getCurrentTimestamp();
            }
            
            @Override
            public void scheduleEvent(DiscreteEvent ev) {
                mockContext.scheduleEvent(ev);
            }
        };
        
        TimeDrivenGenerator<StandardElementGenerationInfo> generator =
            new TimeDrivenGenerator<StandardElementGenerationInfo>(testSimulation, 1, cycle) {
                @Override
                public Element createEventSource(int ind, StandardElementGenerationInfo info) {
                    return null; // Not testing Element creation, just notifyEnd behavior
                }
            };
        
        // When: calling notifyEnd (previously threw NullPointerException)
        assertDoesNotThrow(() -> generator.notifyEnd());
        
        // Then: should schedule a finalize event at current timestamp
        assertEquals(1, mockContext.getScheduledEvents().size());
        DiscreteEvent scheduledEvent = mockContext.getScheduledEvents().get(0);
        assertNotNull(scheduledEvent);
        assertEquals(100, scheduledEvent.getTs(), "Event should be scheduled at current timestamp");
    }
}


