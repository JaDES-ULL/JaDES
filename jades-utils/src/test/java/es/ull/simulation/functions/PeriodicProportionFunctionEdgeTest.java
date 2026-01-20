package es.ull.simulation.functions;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

class PeriodicProportionFunctionEdgeTest {

    @Test
    void shouldComputeWithCollectionsAndTimeIndexing() {
        PeriodicProportionFunction function = new PeriodicProportionFunction(
                List.of(10, 20), List.of(0.25, 0.75), 5.0);

        assertEquals(2.5, function.getValue(() -> 0.0), 1e-9);
        assertEquals(7.5, function.getValue(() -> 5.0), 1e-9);
        assertEquals(5.0, function.getValue(() -> 10.0), 1e-9);
    }

    @Test
    void shouldRejectInvalidParameters() {
        PeriodicProportionFunction function = new PeriodicProportionFunction();
        assertThrows(IllegalArgumentException.class, () -> function.setParameters());
        assertThrows(IllegalArgumentException.class, () -> function.setParameters(new int[0], new double[0]));
        assertThrows(IllegalArgumentException.class, () -> function.setParameters("x", new double[0], 1.0));
        assertThrows(IllegalArgumentException.class, () -> function.setParameters(new int[0], "x", 1.0));
    }
}
