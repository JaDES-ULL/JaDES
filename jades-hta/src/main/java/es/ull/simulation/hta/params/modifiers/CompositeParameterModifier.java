package es.ull.simulation.hta.params.modifiers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import es.ull.simulation.hta.Patient;

/**
 * A modifier that combines several {@link OperationParameterModifier operation modifiers} of the same parameter. The modifiers are applied
 * in ascending {@link ModifierOperation#getOrder() order} of their operations (replace, multiply, subtract), regardless of the order in
 * which they were added. Modifiers with operations of the same order are applied in the order in which they were added.
 */
public class CompositeParameterModifier implements ParameterModifier {
    /** The modifiers, sorted by the order of their operations */
    private final List<OperationParameterModifier> modifiers = new ArrayList<>();

    /**
     * Creates an empty composite modifier
     */
    public CompositeParameterModifier() {
    }

    /**
     * Adds a modifier
     * @param modifier The modifier to add
     */
    public void add(OperationParameterModifier modifier) {
        modifiers.add(modifier);
        // The sort is stable: modifiers with operations of the same order keep the order in which they were added
        modifiers.sort(Comparator.comparingInt(mod -> mod.getOperation().getOrder()));
    }

    /**
     * Returns the modifiers, in the order in which they are applied
     * @return the modifiers, in the order in which they are applied
     */
    public List<OperationParameterModifier> getModifiers() {
        return List.copyOf(modifiers);
    }

    @Override
    public double getModifiedValue(Patient pat, double originalValue) {
        double value = originalValue;
        for (OperationParameterModifier modifier : modifiers) {
            value = modifier.getModifiedValue(pat, value);
        }
        return value;
    }

    @Override
    public String toString() {
        return modifiers.toString();
    }
}
