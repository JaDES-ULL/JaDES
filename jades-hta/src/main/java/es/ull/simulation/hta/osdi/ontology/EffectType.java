package es.ull.simulation.hta.osdi.ontology;

import java.util.TreeMap;

import org.semanticweb.owlapi.model.IRI;

public enum EffectType implements WrapsOSDiClass {
    INSTANT_DEATH_EFFECT(OSDiClass.INSTANT_DEATH_EFFECT),
    MORTALITY_RATE_EFFECT(OSDiClass.INCREASED_MORTALITY_RATE_EFFECT),
    LIFE_EXPECTANCY_REDUCTION_EFFECT(OSDiClass.LIFE_EXPECTANCY_REDUCTION_EFFECT),
    GENERAL_EFFECT(OSDiClass.EFFECT);

	private final static TreeMap<OSDiClass, EffectType> reverseEffect = new TreeMap<>();
	static {
		for (EffectType type : EffectType.values()) {
			reverseEffect.put(type.clazz, type);
		}
	}

    private final OSDiClass clazz;
    private EffectType(OSDiClass clazz) {
        this.clazz = clazz;
    }
    @Override
    public OSDiClass getClazz() {
        return clazz;
    }

    /**
     * Returns the EffecType corresponding to the given OSDi class.
     * @param clazz The OSDiClasses to get the EffecType for.
     * @return The EffecType associated with the given OSDiClasses.
     */
    public static EffectType fromOSDiClass(OSDiClass clazz) {
        return reverseEffect.get(clazz);
    }

    /**
     * Returns the effect type corresponding to the given IRI in the provided OSDi wrapper.
     * @param iri The IRI to get the EffecType for.
     * @param wrap The OSDiWrapper containing the ontology information.
     * @return The EffecType associated with the given IRI, or null if none matches.
     */
    public static EffectType fromIRI(IRI iri, OSDiWrapper wrap) {
        for (EffectType type : EffectType.values()) {
            if (wrap.isInstanceOf(iri, type.getClazz())) {
                return type;
            }
        }
        return null;
    }
}