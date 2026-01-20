package es.ull.simulation.functions;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RoundFunctionTest {

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
            this.value = ((Number) params[0]).doubleValue();
        }
    }

    @Test
    void shouldRoundCeilAndFloor() {
        AbstractTimeFunction base = new ConstantTimeFunction(12.2);
        RoundFunction round = new RoundFunction(RoundFunction.Type.ROUND, base, 5.0, 1.0);
        RoundFunction ceil = new RoundFunction(RoundFunction.Type.CEIL, base, 5.0, 1.0);
        RoundFunction floor = new RoundFunction(RoundFunction.Type.FLOOR, base, 5.0, 1.0);

        assertEquals(11.0, round.getValue(() -> 0.0), 1e-9);
        assertEquals(16.0, ceil.getValue(() -> 0.0), 1e-9);
        assertEquals(11.0, floor.getValue(() -> 0.0), 1e-9);
    }

    @Test
    void shouldIgnoreShiftWhenScaleIsZero() {
        RoundFunction func = new RoundFunction();
        func.setType(RoundFunction.Type.ROUND);
        func.setFunc(new ConstantTimeFunction(7.0));
        func.setScale(0.0);
        func.setShift(5.0);

        assertEquals(7.0, func.getValue(() -> 0.0), 1e-9);
    }

    @Test
    void shouldValidateParameters() {
        RoundFunction func = new RoundFunction();
        AbstractTimeFunction base = new ConstantTimeFunction(3.3);

        func.setParameters(RoundFunction.Type.ROUND, base, 2.0, 0.5);
        assertEquals(RoundFunction.Type.ROUND, func.getType());
        assertEquals(base, func.getFunc());

        assertThrows(IllegalArgumentException.class, () -> func.setParameters(RoundFunction.Type.ROUND, base, 1.0));
        assertThrows(IllegalArgumentException.class, () -> func.setParameters("x", base, 1.0, 0.0));
    }
}
