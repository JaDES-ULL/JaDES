package es.ull.simulation.hta.params;

import es.ull.simulation.hta.HTAModel;
import es.ull.simulation.hta.Patient;

public class ConstantNatureParameter extends Parameter {
	private final double value;

    public ConstantNatureParameter(HTAModel model, String name, String description, String source, int year, ParameterGroup group, double value) {
        super(model, name, description, source, year, group);
        this.value = value;
    }

    public ConstantNatureParameter(HTAModel model, String name, String description, String source, ParameterGroup group, double value) {
        super(model, name, description, source, group);
        this.value = value;
    }

	@Override
	public double getValue(Patient pat) {
		return value;
	}
 
    public double getValue() {
        return value;
    }
}
