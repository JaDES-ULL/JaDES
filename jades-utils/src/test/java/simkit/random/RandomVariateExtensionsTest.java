package simkit.random;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RandomVariateExtensionsTest {

    @Test
    void logisticVariateShouldAcceptParameters() {
        LogisticVariate variate = new LogisticVariate();

        variate.setParameters(1.5, 0.5);

        Object[] params = variate.getParameters();
        assertEquals(1.5, (double) params[0], 1e-9);
        assertEquals(0.5, (double) params[1], 1e-9);
        assertTrue(Double.isFinite(variate.generate()));
    }

    @Test
    void logLogisticVariateShouldTransform() {
        LogLogisticVariate variate = new LogLogisticVariate();
        variate.setParameters(0.0, 1.0);

        double value = variate.generate();
        assertTrue(Double.isFinite(value));
        assertTrue(value > 0.0);
    }

    @Test
    void expTransformVariateShouldWrapRandomVariate() {
        DiscreteConstantVariate inner = new DiscreteConstantVariate();
        inner.setParameters(0);

        ExpTransformVariate variate = new ExpTransformVariate();
        variate.setParameters(inner);

        assertEquals(1.0, variate.generate(), 1e-9);
        assertSame(inner, variate.getInnerRnd());
    }

    @Test
    void discreteConstantVariateShouldGenerateFixedValue() {
        DiscreteConstantVariate variate = new DiscreteConstantVariate();
        variate.setParameters(4);

        assertEquals(4.0, variate.generate(), 1e-9);
        assertEquals(4, variate.generateInt());
        assertEquals(4, ((Number) variate.getParameters()[0]).intValue());
    }

    @Test
    void shouldRejectInvalidParameters() {
        LogisticVariate logistic = new LogisticVariate();
        assertThrows(IllegalArgumentException.class, () -> logistic.setParameters(1.0));
        assertThrows(IllegalArgumentException.class, () -> logistic.setParameters("a", "b"));

        ExpTransformVariate exp = new ExpTransformVariate();
        assertThrows(IllegalArgumentException.class, () -> exp.setParameters());
        assertThrows(IllegalArgumentException.class, () -> exp.setParameters("x"));

        DiscreteConstantVariate discrete = new DiscreteConstantVariate();
        assertThrows(IllegalArgumentException.class, () -> discrete.setParameters());
        assertThrows(IllegalArgumentException.class, () -> discrete.setParameters("x"));
    }
}
