package es.ull.simulation.model;

import es.ull.simulation.functions.AbstractTimeFunction;
import es.ull.simulation.functions.ConstantFunction;
import es.ull.simulation.model.flow.IInitializerFlow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link TimeDrivenElementGenerator}
 */
class TimeDrivenElementGeneratorTest {

    private Simulation simulation;
    private ISimulationCycle cycle;
    private ElementType elementType;
    private IInitializerFlow initFlow;

    @BeforeEach
    void setUp() {
        simulation = new Simulation(0, "Test Simulation");
        cycle = SimulationPeriodicCycle.newHourlyCycle(TimeUnit.MINUTE);
        elementType = new ElementType(simulation, "TestElementType");
        initFlow = null; // No se requiere flujo real para constructores
    }

    @Test
    void shouldCreateGenerator_whenUsingIntNElemConstructor() {
        // Given: número fijo de elementos
        int nElem = 5;

        // When: creando generador con int
        TimeDrivenElementGenerator generator = new TimeDrivenElementGenerator(simulation, nElem, cycle);

        // Then: debe ser creado correctamente
        assertNotNull(generator);
        assertEquals(simulation, generator.getSimulation());
    }

    @Test
    void shouldCreateGenerator_whenUsingAbstractTimeFunctionConstructor() {
        // Given: función de tiempo para número de elementos
        AbstractTimeFunction nElemFunction = new ConstantFunction(5.0);

        // When: creando generador con función
        TimeDrivenElementGenerator generator = new TimeDrivenElementGenerator(simulation, nElemFunction, cycle);

        // Then: debe ser creado correctamente
        assertNotNull(generator);
        assertEquals(simulation, generator.getSimulation());
    }

    @Test
    void shouldCreateGenerator_whenUsingSingleTypeWithIntNElem() {
        // Given: tipo de elemento específico con int
        int nElem = 3;

        // When: creando generador con tipo específico
        TimeDrivenElementGenerator generator = new TimeDrivenElementGenerator(simulation, nElem, elementType, initFlow, cycle);

        // Then: debe ser creado correctamente
        assertNotNull(generator);
        assertEquals(simulation, generator.getSimulation());
    }

    @Test
    void shouldCreateGenerator_whenUsingSingleTypeWithTimeFunction() {
        // Given: tipo de elemento específico con función
        AbstractTimeFunction nElemFunction = new ConstantFunction(3.0);

        // When: creando generador con tipo específico y función
        TimeDrivenElementGenerator generator = new TimeDrivenElementGenerator(simulation, nElemFunction, elementType, initFlow, cycle);

        // Then: debe ser creado correctamente
        assertNotNull(generator);
        assertEquals(simulation, generator.getSimulation());
    }

    @Test
    void shouldCreateEventSource_whenInvoked() {
        // Given: generador con tipo de elemento
        TimeDrivenElementGenerator generator = new TimeDrivenElementGenerator(
                simulation, 1, elementType, initFlow, cycle);
        StandardElementGenerationInfo info = new StandardElementGenerationInfo(elementType, initFlow, 0, null, 1.0);

        // When: creando fuente de eventos
        IEventSource eventSource = generator.createEventSource(0, info);

        // Then: debe crear un Element
        assertNotNull(eventSource);
        assertTrue(eventSource instanceof Element);
    }

    @Test
    void shouldHandleDifferentCycleTypes_whenCreatingGenerator() {
        // Given: diferentes tipos de ciclos
        ISimulationCycle hourlyCycle = SimulationPeriodicCycle.newHourlyCycle(TimeUnit.MINUTE);
        ISimulationCycle dailyCycle = SimulationPeriodicCycle.newDailyCycle(TimeUnit.MINUTE);

        // When: creando generadores con diferentes ciclos
        TimeDrivenElementGenerator generator1 = new TimeDrivenElementGenerator(simulation, 1, hourlyCycle);
        TimeDrivenElementGenerator generator2 = new TimeDrivenElementGenerator(simulation, 1, dailyCycle);

        // Then: ambos deben ser creados correctamente
        assertNotNull(generator1);
        assertNotNull(generator2);
        assertEquals(simulation, generator1.getSimulation());
        assertEquals(simulation, generator2.getSimulation());
    }

    @Test
    void shouldHandleZeroElements_whenCreatingGenerator() {
        // Given: generador que crea 0 elementos
        int nElem = 0;

        // When: creando generador con 0 elementos
        TimeDrivenElementGenerator generator = new TimeDrivenElementGenerator(simulation, nElem, cycle);

        // Then: debe ser creado sin errores
        assertNotNull(generator);
    }

    @Test
    void shouldHandleLargeNumberOfElements_whenCreatingGenerator() {
        // Given: generador que crea muchos elementos
        int nElem = 1000;

        // When: creando generador con muchos elementos
        TimeDrivenElementGenerator generator = new TimeDrivenElementGenerator(simulation, nElem, cycle);

        // Then: debe ser creado sin errores
        assertNotNull(generator);
    }

    @Test
    void shouldCreateGenerator_withMovableElementsUsingTimeFunction() {
        // Given: generador con elementos movibles y función de tiempo
        AbstractTimeFunction nElemFunction = new ConstantFunction(2.0);
        AbstractTimeFunction sizeFunction = new ConstantFunction(1.0);
        // Location puede ser null para este test básico de constructor

        // When: creando generador con location y size como función
        TimeDrivenElementGenerator generator = new TimeDrivenElementGenerator(
                simulation, nElemFunction, elementType, initFlow, sizeFunction, null, cycle);

        // Then: debe ser creado correctamente
        assertNotNull(generator);
        assertEquals(simulation, generator.getSimulation());
    }

    @Test
    void shouldCreateGenerator_withMovableElementsUsingIntAndSizeFunction() {
        // Given: generador con elementos movibles usando int para nElem y función para size
        int nElem = 3;
        AbstractTimeFunction sizeFunction = new ConstantFunction(2.0);

        // When: creando generador con int nElem y función de size
        TimeDrivenElementGenerator generator = new TimeDrivenElementGenerator(
                simulation, nElem, elementType, initFlow, sizeFunction, null, cycle);

        // Then: debe ser creado correctamente
        assertNotNull(generator);
        assertEquals(simulation, generator.getSimulation());
    }

    @Test
    void shouldCreateGenerator_withMovableElementsUsingIntSize() {
        // Given: generador con elementos movibles usando int para size
        int nElem = 4;
        int size = 5;

        // When: creando generador con int para nElem y size
        TimeDrivenElementGenerator generator = new TimeDrivenElementGenerator(
                simulation, nElem, elementType, initFlow, size, null, cycle);

        // Then: debe ser creado correctamente
        assertNotNull(generator);
        assertEquals(simulation, generator.getSimulation());
    }
}
