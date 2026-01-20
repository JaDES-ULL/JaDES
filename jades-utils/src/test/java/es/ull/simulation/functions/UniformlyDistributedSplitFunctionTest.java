package es.ull.simulation.functions;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UniformlyDistributedSplitFunctionTest {

    private static final class ConstantTimeFunction extends AbstractTimeFunction {
        private double value;

        ConstantTimeFunction(double value) {
            this.value = value;
        }

        @Override
        public double getValue(TimeFunctionParams params) {
            return value;
        }

        @Override
        public void setParameters(Object... params) {
            this.value = (double) params[0];
        }
    }

    private static final class FixedTimeParams implements TimeFunctionParams {
        private final double time;

        FixedTimeParams(double time) {
            this.time = time;
        }

        @Override
        public double getTime() {
            return time;
        }
    }

    @Test
    void shouldSelectPartByTimeUnit() {
        AbstractTimeFunction[] parts = new AbstractTimeFunction[] {
                new ConstantTimeFunction(10.0),
                new ConstantTimeFunction(20.0)
        };
        UniformlyDistributedSplitFunction function = new UniformlyDistributedSplitFunction(parts, 5.0);

        assertEquals(10.0, function.getValue(new FixedTimeParams(0.0)), 1e-9);
        assertEquals(10.0, function.getValue(new FixedTimeParams(4.9)), 1e-9);
        assertEquals(20.0, function.getValue(new FixedTimeParams(5.0)), 1e-9);
        assertEquals(10.0, function.getValue(new FixedTimeParams(10.0)), 1e-9);
    }

    @Test
    void shouldSetParametersAndExposeFields() {
        UniformlyDistributedSplitFunction function = new UniformlyDistributedSplitFunction();
        AbstractTimeFunction[] parts = new AbstractTimeFunction[] { new ConstantTimeFunction(3.0) };
        function.setParameters(parts, Double.valueOf(2.5));

        assertSame(parts, function.getPart());
        assertEquals(2.5, function.getTimeUnit(), 1e-9);
    }

    @Test
    void shouldRejectInvalidParameters() {
        UniformlyDistributedSplitFunction function = new UniformlyDistributedSplitFunction();
        assertThrows(IllegalArgumentException.class, () -> function.setParameters());
        assertThrows(IllegalArgumentException.class,
                () -> function.setParameters(new Object(), Double.valueOf(1.0)));
        assertThrows(IllegalArgumentException.class,
            () -> function.setParameters(new AbstractTimeFunction[0], "x"));
    }
}
