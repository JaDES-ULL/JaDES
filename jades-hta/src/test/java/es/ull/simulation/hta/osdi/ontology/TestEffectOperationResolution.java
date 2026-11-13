package es.ull.simulation.hta.osdi.ontology;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.params.modifiers.ModifierOperation;

/**
 * Checks how the data item type of the magnitude of an effect, and that of the affected parameter, determine the operation
 * (see decision D3 in docs/osdi-1.3-diseno.md).
 */
@DisplayName("Operation of an effect according to the data item types")
public class TestEffectOperationResolution {

    private static ModifierOperation resolve(OSDiDataItemType magnitude, OSDiDataItemType target) throws Exception {
        return EffectWrapper.resolveOperation(magnitude, target, "effect", "target");
    }

    @Test
    @DisplayName("Factors and ratios multiply")
    void factors() throws Exception {
        assertEquals(ModifierOperation.MULTIPLY, resolve(OSDiDataItemType.DI_FACTOR, OSDiDataItemType.DI_PROBABILITY));
        assertEquals(ModifierOperation.MULTIPLY, resolve(OSDiDataItemType.DI_FACTOR, OSDiDataItemType.CURRENCY_EURO));
        assertEquals(ModifierOperation.MULTIPLY, resolve(OSDiDataItemType.DI_RATIO, OSDiDataItemType.DI_CONTINUOUS_VARIABLE));
        assertEquals(ModifierOperation.MULTIPLY, resolve(OSDiDataItemType.DI_FACTOR, OSDiDataItemType.DI_FACTOR));
    }

    @Test
    @DisplayName("Relative risks and hazard ratios on probabilities are applied to the equivalent rate")
    void rateRatios() throws Exception {
        for (OSDiDataItemType target : new OSDiDataItemType[] { OSDiDataItemType.DI_PROBABILITY, OSDiDataItemType.DI_INCIDENCE,
                OSDiDataItemType.DI_PROPORTION, OSDiDataItemType.DI_PREVALENCE }) {
            assertEquals(ModifierOperation.RATE_RATIO, resolve(OSDiDataItemType.DI_RELATIVE_RISK, target), target.name());
            assertEquals(ModifierOperation.RATE_RATIO, resolve(OSDiDataItemType.DI_HAZARD_RATIO, target), target.name());
        }
    }

    @Test
    @DisplayName("A relative risk on something that is not a risk multiplies")
    void relativeRiskOnOtherTargets() throws Exception {
        assertEquals(ModifierOperation.MULTIPLY, resolve(OSDiDataItemType.DI_RELATIVE_RISK, OSDiDataItemType.CURRENCY_EURO));
        assertEquals(ModifierOperation.MULTIPLY, resolve(OSDiDataItemType.DI_RELATIVE_RISK, OSDiDataItemType.DI_CONTINUOUS_VARIABLE));
        // A relative magnitude always operates, even if the affected parameter has the same type (e.g., a mortality rate ratio)
        assertEquals(ModifierOperation.MULTIPLY, resolve(OSDiDataItemType.DI_RELATIVE_RISK, OSDiDataItemType.DI_RELATIVE_RISK));
    }

    @Test
    @DisplayName("Hazard ratios on times and odds ratios on probabilities")
    void otherRatios() throws Exception {
        assertEquals(ModifierOperation.TIME_HAZARD_RATIO, resolve(OSDiDataItemType.DI_HAZARD_RATIO, OSDiDataItemType.DI_TIME_TO_EVENT));
        assertEquals(ModifierOperation.ODDS_RATIO, resolve(OSDiDataItemType.DI_ODDS_RATIO, OSDiDataItemType.DI_PROBABILITY));
        assertEquals(ModifierOperation.ODDS_RATIO, resolve(OSDiDataItemType.DI_ODDS_RATIO, OSDiDataItemType.DI_INCIDENCE));
    }

    @Test
    @DisplayName("Mean differences subtract")
    void differences() throws Exception {
        assertEquals(ModifierOperation.SUBTRACT, resolve(OSDiDataItemType.DI_MEAN_DIFFERENCE, OSDiDataItemType.DI_INCIDENCE));
        assertEquals(ModifierOperation.SUBTRACT, resolve(OSDiDataItemType.DI_MEAN_DIFFERENCE, OSDiDataItemType.CURRENCY_EURO));
        assertEquals(ModifierOperation.SUBTRACT, resolve(OSDiDataItemType.DI_MEAN_DIFFERENCE, OSDiDataItemType.DI_UNDEFINED));
    }

    @Test
    @DisplayName("Absolute values replace the parameter")
    void replacements() throws Exception {
        assertEquals(ModifierOperation.REPLACE, resolve(OSDiDataItemType.DI_CONTINUOUS_VARIABLE, OSDiDataItemType.DI_INCIDENCE));
        assertEquals(ModifierOperation.REPLACE, resolve(OSDiDataItemType.DI_PROBABILITY, OSDiDataItemType.DI_PROBABILITY));
        assertEquals(ModifierOperation.REPLACE, resolve(OSDiDataItemType.CURRENCY_EURO, OSDiDataItemType.CURRENCY_EURO));
        // Different absolute types, or an undefined type, also replace (with a warning)
        assertEquals(ModifierOperation.REPLACE, resolve(OSDiDataItemType.DI_PROBABILITY, OSDiDataItemType.CURRENCY_EURO));
        assertEquals(ModifierOperation.REPLACE, resolve(OSDiDataItemType.DI_UNDEFINED, OSDiDataItemType.DI_PROBABILITY));
        assertEquals(ModifierOperation.REPLACE, resolve(OSDiDataItemType.DI_OTHER, OSDiDataItemType.DI_PROBABILITY));
    }

    @Test
    @DisplayName("Combinations without meaning are rejected")
    void rejections() {
        assertThrows(MalformedOSDiModelException.class, () -> resolve(OSDiDataItemType.DI_ODDS_RATIO, OSDiDataItemType.CURRENCY_EURO));
        assertThrows(MalformedOSDiModelException.class, () -> resolve(OSDiDataItemType.DI_ODDS_RATIO, OSDiDataItemType.DI_TIME_TO_EVENT));
        assertThrows(MalformedOSDiModelException.class, () -> resolve(OSDiDataItemType.DI_HAZARD_RATIO, OSDiDataItemType.CURRENCY_EURO));
        assertThrows(MalformedOSDiModelException.class, () -> resolve(OSDiDataItemType.DI_HAZARD_RATIO, OSDiDataItemType.DI_UNDEFINED));
        assertThrows(MalformedOSDiModelException.class, () -> resolve(OSDiDataItemType.DI_STANDARD_DEVIATION, OSDiDataItemType.DI_PROBABILITY));
        assertThrows(MalformedOSDiModelException.class, () -> resolve(OSDiDataItemType.DI_LOWER_95_CONFIDENCE_LIMIT, OSDiDataItemType.DI_PROBABILITY));
        assertThrows(MalformedOSDiModelException.class, () -> resolve(OSDiDataItemType.DI_UPPER_95_CONFIDENCE_LIMIT, OSDiDataItemType.DI_PROBABILITY));
        assertThrows(UnsupportedOSDiFeatureException.class, () -> resolve(OSDiDataItemType.DI_RELATIVE_RISK, OSDiDataItemType.DI_TIME_TO_EVENT));
    }
}
