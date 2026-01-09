package es.ull.simulation.model;

import es.ull.simulation.functions.TimeFunctionFactory;
import es.ull.simulation.model.flow.ActivityFlow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class GeneratorTest {
    private Simulation simulation;
    private ElementType elementType;
    private ActivityFlow flow;
    
    @BeforeEach
    public void setUp() {
        simulation = new Simulation(0, "Test Simulation", TimeUnit.MINUTE);
        elementType = new ElementType(simulation, "Test Element Type");
        flow = new ActivityFlow(simulation, "Test Activity");
    }

    private Generator<StandardElementGenerationInfo> createTestGenerator(int nElem) {
        return new Generator<StandardElementGenerationInfo>(simulation, 1, nElem) {
            @Override
            public IEventSource createEventSource(int ind, StandardElementGenerationInfo info) {
                return new Element(simulation, info);
            }
        };
    }

    @Test
    public void shouldCallGetNElem() {
        // Given: a generator with nElem = 5
        Generator<StandardElementGenerationInfo> generator = createTestGenerator(5);

        // When: calling getNElem
        var nElem = generator.getNElem();

        // Then: should return the time function
        assertNotNull(nElem);
        assertEquals(5.0, nElem.getValue(generator));
    }

    @Test
    public void shouldCallGetGenerationInfos() {
        // Given: a generator with generation infos added
        Generator<StandardElementGenerationInfo> generator = createTestGenerator(3);
        StandardElementGenerationInfo info = new StandardElementGenerationInfo(elementType, flow, 1, null, 1.0);
        generator.add(info);

        // When: calling getGenerationInfos
        ArrayList<StandardElementGenerationInfo> infos = generator.getGenerationInfos();

        // Then: should return the list with the added info
        assertNotNull(infos);
        assertEquals(1, infos.size());
        assertEquals(info, infos.get(0));
    }

    @Test
    public void shouldCallGetObjectTypeIdentifier() {
        // Given: a generator
        Generator<StandardElementGenerationInfo> generator = createTestGenerator(1);

        // When: calling getObjectTypeIdentifier
        String identifier = generator.getObjectTypeIdentifier();

        // Then: should return "GEN"
        assertEquals("GEN", identifier);
    }

    @Test
    public void shouldCallGetGenerationInfos_withEmptyList() {
        // Given: a generator with no generation infos
        Generator<StandardElementGenerationInfo> generator = createTestGenerator(2);

        // When: calling getGenerationInfos
        ArrayList<StandardElementGenerationInfo> infos = generator.getGenerationInfos();

        // Then: should return an empty list
        assertNotNull(infos);
        assertTrue(infos.isEmpty());
    }

    @Test
    public void shouldCallGetNElem_withTimeFunction() {
        // Given: a generator created with a time function
        var timeFunction = TimeFunctionFactory.getInstance("ConstantVariate", 10);
        Generator<StandardElementGenerationInfo> generator = new Generator<StandardElementGenerationInfo>(simulation, 1, timeFunction) {
            @Override
            public IEventSource createEventSource(int ind, StandardElementGenerationInfo info) {
                return new Element(simulation, info);
            }
        };

        // When: calling getNElem
        var nElem = generator.getNElem();

        // Then: should return the same time function
        assertSame(timeFunction, nElem);
        assertEquals(10.0, nElem.getValue(generator));
    }

    @Test
    public void shouldCallGetGenerationInfos_withMultipleInfos() {
        // Given: a generator with multiple generation infos
        Generator<StandardElementGenerationInfo> generator = createTestGenerator(3);
        StandardElementGenerationInfo info1 = new StandardElementGenerationInfo(elementType, flow, 1, null, 0.5);
        StandardElementGenerationInfo info2 = new StandardElementGenerationInfo(elementType, flow, 2, null, 0.5);
        generator.add(info1);
        generator.add(info2);

        // When: calling getGenerationInfos
        ArrayList<StandardElementGenerationInfo> infos = generator.getGenerationInfos();

        // Then: should return the list with both infos
        assertNotNull(infos);
        assertEquals(2, infos.size());
        assertEquals(info1, infos.get(0));
        assertEquals(info2, infos.get(1));
    }
}
