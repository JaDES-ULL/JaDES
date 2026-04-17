package es.ull.simulation.hta.params;

import es.ull.simulation.hta.Patient;

public interface ParameterExpression {
    public double getValue(Patient pat);
}
