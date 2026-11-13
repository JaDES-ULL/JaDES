package es.ull.simulation.hta.params.modifiers;

import java.util.function.ToDoubleFunction;

import es.ull.simulation.hta.HTAModel;
import es.ull.simulation.hta.Patient;

/**
 * A modifier that applies a {@link ModifierOperation} with a magnitude that may depend on the patient.
 */
public class OperationParameterModifier implements ParameterModifier {
    /** The operation to apply */
    private final ModifierOperation operation;
    /** Returns the magnitude of the modification for a patient */
    private final ToDoubleFunction<Patient> magnitude;

    /**
     * Creates a modifier whose magnitude is computed by a function of the patient
     * @param operation The operation to apply
     * @param magnitude A function that returns the magnitude of the modification for a patient
     */
    public OperationParameterModifier(ModifierOperation operation, ToDoubleFunction<Patient> magnitude) {
        this.operation = operation;
        this.magnitude = magnitude;
    }

    /**
     * Creates a modifier whose magnitude is the value of a parameter of the model
     * @param operation The operation to apply
     * @param model The model that contains the parameter
     * @param magnitudeParamName The name of the parameter with the magnitude of the modification
     */
    public OperationParameterModifier(ModifierOperation operation, HTAModel model, String magnitudeParamName) {
        this(operation, pat -> model.getParameterValue(magnitudeParamName, pat));
    }

    /**
     * Returns the operation applied by this modifier
     * @return the operation applied by this modifier
     */
    public ModifierOperation getOperation() {
        return operation;
    }

    @Override
    public double getModifiedValue(Patient pat, double originalValue) {
        return operation.apply(originalValue, magnitude.applyAsDouble(pat));
    }

    @Override
    public String toString() {
        return operation.name();
    }
}
