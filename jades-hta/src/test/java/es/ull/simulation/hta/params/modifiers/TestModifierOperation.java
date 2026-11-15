package es.ull.simulation.hta.params.modifiers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Checks the operations that apply the magnitude of an effect to a parameter, and the order in which several of them are combined.
 * Expected values are computed by hand from the definition of each operation.
 */
@DisplayName("Operations that apply the magnitude of an effect")
public class TestModifierOperation {
    private static final double TOLERANCE = 1e-12;

    @Test
    @DisplayName("Each operation transforms the value as defined")
    void applyOperations() {
        assertEquals(0.6, ModifierOperation.REPLACE.apply(0.8, 0.6), TOLERANCE);
        assertEquals(0.4, ModifierOperation.MULTIPLY.apply(0.8, 0.5), TOLERANCE);
        assertEquals(0.7, ModifierOperation.SUBTRACT.apply(0.8, 0.1), TOLERANCE);
        // Rate ratio on a probability: 1 - (1 - 0.8)^0.5 = 1 - sqrt(0.2)
        assertEquals(1 - Math.sqrt(0.2), ModifierOperation.RATE_RATIO.apply(0.8, 0.5), TOLERANCE);
        // Odds ratio on a probability: 0.8 * 0.5 / (1 - 0.8 + 0.8 * 0.5) = 2/3
        assertEquals(2.0 / 3.0, ModifierOperation.ODDS_RATIO.apply(0.8, 0.5), TOLERANCE);
        // Hazard ratio on an exponential mean time: 2 / 0.5 = 4
        assertEquals(4.0, ModifierOperation.TIME_HAZARD_RATIO.apply(2.0, 0.5), TOLERANCE);
    }

    @Test
    @DisplayName("Rate and odds ratios keep a probability within [0, 1]")
    void ratiosKeepProbabilities() {
        // A relative risk of 3 on a probability of 0.8 would give 2.4 if applied linearly
        assertEquals(1 - Math.pow(0.2, 3), ModifierOperation.RATE_RATIO.apply(0.8, 3.0), TOLERANCE);
        assertEquals(0.8 * 3 / (1 - 0.8 + 0.8 * 3), ModifierOperation.ODDS_RATIO.apply(0.8, 3.0), TOLERANCE);
    }

    @Test
    @DisplayName("Several operations are applied in the order: replace, multiply, subtract")
    void compositeOrder() {
        // Same values as the decision tree case P27: 0.6 * 0.5 - 0.1 = 0.2, whatever the order in which the effects are added
        final List<List<OperationParameterModifier>> insertionOrders = List.of(
            List.of(constant(ModifierOperation.REPLACE, 0.6), constant(ModifierOperation.MULTIPLY, 0.5), constant(ModifierOperation.SUBTRACT, 0.1)),
            List.of(constant(ModifierOperation.SUBTRACT, 0.1), constant(ModifierOperation.MULTIPLY, 0.5), constant(ModifierOperation.REPLACE, 0.6)),
            List.of(constant(ModifierOperation.MULTIPLY, 0.5), constant(ModifierOperation.SUBTRACT, 0.1), constant(ModifierOperation.REPLACE, 0.6)));
        for (List<OperationParameterModifier> modifiers : insertionOrders) {
            final CompositeParameterModifier composite = new CompositeParameterModifier();
            modifiers.forEach(composite::add);
            assertEquals(0.2, composite.getModifiedValue(null, 0.8), TOLERANCE, "Insertion order: " + modifiers);
        }
    }

    @Test
    @DisplayName("Ratios on different scales are applied after the linear factors")
    void compositeScales() {
        // Linear factor first (0.8 * 0.5 = 0.4), then the rate ratio: 1 - (1 - 0.4)^0.5
        final CompositeParameterModifier composite = new CompositeParameterModifier();
        composite.add(constant(ModifierOperation.RATE_RATIO, 0.5));
        composite.add(constant(ModifierOperation.MULTIPLY, 0.5));
        assertEquals(1 - Math.sqrt(0.6), composite.getModifiedValue(null, 0.8), TOLERANCE);
    }

    private static OperationParameterModifier constant(ModifierOperation operation, double magnitude) {
        return new OperationParameterModifier(operation, pat -> magnitude);
    }
}
