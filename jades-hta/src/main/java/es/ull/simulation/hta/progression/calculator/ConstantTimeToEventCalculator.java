package es.ull.simulation.hta.progression.calculator;

import es.ull.simulation.hta.Patient;
import es.ull.simulation.model.TimeUnit;

public class ConstantTimeToEventCalculator implements TimeToEventCalculator {
    private final double value;
    private final TimeUnit timeUnit;

    public ConstantTimeToEventCalculator(double value, TimeUnit timeUnit) {
        this.value = value;
        this.timeUnit = timeUnit;
    }

    public ConstantTimeToEventCalculator(double value) {
        this(value, TimeUnit.YEAR);
    }

    @Override
    public double getTimeToEvent(Patient pat) {
        return value;
    }

    @Override
    public TimeUnit getTimeUnit() {
        return timeUnit;
    }
}
