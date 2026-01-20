package es.ull.simulation.functions;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.Test;

class TimeFunctionFactoryTest {

    @Test
        void shouldCreateLinearFunctionFromFqcn() {
        AbstractTimeFunction fn = TimeFunctionFactory.getInstance(
            "es.ull.simulation.functions.LinearFunction",
            new ConstantFunction(2.0), new ConstantFunction(3.0));

        assertTrue(fn instanceof LinearFunction);
        assertEquals(7.0, fn.getValue(() -> 2.0), 1e-9);
        assertTrue(TimeFunctionFactory.getCache()
            .containsKey("es.ull.simulation.functions.LinearFunction"));
    }

    @Test
    void shouldCreateFunctionUsingSearchPackage() {
        Set<String> originalPackages = TimeFunctionFactory.getSearchPackages();
        try {
            TimeFunctionFactory.addSearchPackage("es.ull.simulation.functions");
            AbstractTimeFunction fn = TimeFunctionFactory.getInstance(
                    "LinearFunction",
                    new ConstantFunction(1.0), new ConstantFunction(0.5));
            assertEquals(1.5, fn.getValue(() -> 1.0), 1e-9);
        } finally {
            TimeFunctionFactory.setSearchPackages(originalPackages);
        }
    }

    @Test
    void shouldFallbackToRandomVariate() {
        AbstractTimeFunction fn = TimeFunctionFactory.getInstance("DiscreteConstantVariate", 3);
        assertTrue(fn instanceof RandomFunction);
        assertEquals(3.0, fn.getValue(null), 1e-9);
    }

    @Test
    void shouldThrowOnNullClassName() {
        assertThrows(IllegalArgumentException.class, () -> TimeFunctionFactory.getInstance(null));
    }

    @Test
    void shouldFindFullyQualifiedNameOrReturnNull() {
        assertNotNull(TimeFunctionFactory.findFullyQualifiedNameFor(
            "es.ull.simulation.functions.LinearFunction"));
        assertNull(TimeFunctionFactory.findFullyQualifiedNameFor("NonExistingFunction"));
    }
}
