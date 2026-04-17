package es.ull.simulation.hta.osdi.ontology;

import java.util.TreeMap;

import org.semanticweb.owlapi.model.IRI;

import es.ull.simulation.hta.osdi.OSDiLogger;

/**
 * A list of the different types of strategy that can be defined in OSDi. 
 */
public enum StrategyType implements WrapsOSDiClass {
    DIAGNOSIS_STRATEGY(OSDiClass.DIAGNOSIS_STRATEGY, ResourceUsageSKOSCategory.DIAGNOSIS),
    FOLLOW_UP_STRATEGY(OSDiClass.FOLLOW_UP_STRATEGY, ResourceUsageSKOSCategory.FOLLOW_UP),
    LINE_OF_THERAPY(OSDiClass.LINE_OF_THERAPY, ResourceUsageSKOSCategory.TREATMENT),
    SCREENING_STRATEGY(OSDiClass.SCREENING_STRATEGY, ResourceUsageSKOSCategory.SCREENING);

    private final static OSDiLogger log = OSDiLogger.getLogger(StrategyType.class);
    private final static TreeMap<OSDiClass, StrategyType> reverseStrategy = new TreeMap<>();
	static {
		for (StrategyType type : StrategyType.values()) {
			reverseStrategy.put(type.clazz, type);
		}
	}

    private final OSDiClass clazz;
    private final ResourceUsageSKOSCategory category;

    private StrategyType(OSDiClass clazz, ResourceUsageSKOSCategory category) {
        this.clazz = clazz;
        this.category = category;
    }

    @Override
    public OSDiClass getClazz() {
        return clazz;
    }

    /**
     * Returns the ResourceUsageSKOSCategory associated with this strategy type.
     * @return The ResourceUsageSKOSCategory associated with this strategy type.
     */
    public ResourceUsageSKOSCategory getCategory() {
        return category;
    }

     /**
     * Returns the strategy type corresponding to the given IRI in the provided OSDi wrapper.
     * @param iri The IRI to get the StrategyType for.
     * @param wrap The OSDiWrapper containing the ontology information.
     * @return The StrategyType associated with the given IRI, or null if none matches.
     */
    /**
     * Returns the StrategyType corresponding to the given OSDi class.
     * @param clazz The OSDiClasses to get the StrategyType for.
     * @return The StrategyType associated with the given OSDiClasses.
     */
    public static StrategyType fromOSDiClass(OSDiClass clazz) {
        return reverseStrategy.get(clazz); 
    }

    /**
     * Returns the strategy type corresponding to the given IRI in the provided OSDi wrapper.
     * @param iri The IRI to get the StrategyType for.
     * @param wrap The OSDiWrapper containing the ontology information.
     * @return The StrategyType associated with the given IRI, or null if none matches.
     */
    public static StrategyType fromIRI(IRI iri, OSDiWrapper wrap) {
        for (StrategyType type : StrategyType.values()) {
            if (wrap.isInstanceOf(iri, type.getClazz())) {
                return type;
            }
        }
        if (wrap.isInstanceOf(iri, OSDiClass.DETECTION_STRATEGY)) {
            log.warn("Detection strategy " + iri + " is not a subclass of " + OSDiClass.SCREENING_STRATEGY.getShortName() + " or " + OSDiClass.DIAGNOSIS_STRATEGY.getShortName() + ". Defaulting to " + OSDiClass.DIAGNOSIS_STRATEGY.getShortName() + ".");
            return DIAGNOSIS_STRATEGY;
        }
        return null;
    }

	/**
	 * Returns the strategy type corresponding to the given SKOS category.
	 * @param category The SKOS category to get the strategy type for.
	 * @return The strategy type corresponding to the given SKOS category, or null if none matches.
	 */
	public static StrategyType fromSKOSCategory(ResourceUsageSKOSCategory category) {
		for (StrategyType type : StrategyType.values()) {
			if (type.getCategory().equals(category)) {
				return type;
			}
		}
		return null;
	}
}