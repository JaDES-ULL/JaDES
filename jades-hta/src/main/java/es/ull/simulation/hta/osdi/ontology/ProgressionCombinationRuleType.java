package es.ull.simulation.hta.osdi.ontology;

import java.util.TreeMap;

import org.semanticweb.owlapi.model.IRI;

import es.ull.simulation.hta.osdi.OSDiLogger;

/**
 * A list of the different types of progression combination rules that can be defined in OSDi. 
 */
public enum ProgressionCombinationRuleType implements WrapsOSDiClass {
	ALTERNATIVE(OSDiClass.ALTERNATIVE_COMBINATION_RULE),
	SEQUENTIAL(OSDiClass.SEQUENTIAL_COMBINATION_RULE),
	COEXISTENT(OSDiClass.COEXISTENT_COMBINATION_RULE);

	private final static OSDiLogger log = OSDiLogger.getLogger(ProgressionCombinationRuleType.class);
	private final static TreeMap<OSDiClass, ProgressionCombinationRuleType> reverseDiseaseProgressionSet = new TreeMap<>();
	static {
		for (ProgressionCombinationRuleType type : ProgressionCombinationRuleType.values()) {
			reverseDiseaseProgressionSet.put(type.clazz, type);
		}
	}
	private final OSDiClass clazz;
	private ProgressionCombinationRuleType(OSDiClass clazz) {
		this.clazz = clazz;
	}

	@Override
	public OSDiClass getClazz() {
		return clazz;
	}
	
	/**
	 * Returns the ProgressionCombinationRuleWrapper corresponding to the given OSDi class.
	 * @param clazz The OSDiClasses to get the ProgressionCombinationRuleWrapper for.
	 * @return The ProgressionCombinationRuleWrapper associated with the given OSDiClasses.
	 */
	public static ProgressionCombinationRuleType fromOSDiClass(OSDiClass clazz) {
		return reverseDiseaseProgressionSet.get(clazz);
	}

	/**
	 * Returns the type of progression combination rule corresponding to the given IRI in the provided OSDi wrapper.
	 * @param iri The IRI to get the ProgressionCombinationRuleWrapper for.
	 * @param wrap The OSDiWrapper containing the ontology information.
	 * @return The ProgressionCombinationRuleWrapper associated with the given IRI, or null if none matches.
	 */
	@Deprecated
	public static ProgressionCombinationRuleType fromIRI(IRI iri, OSDiWrapper wrap) {
		for (ProgressionCombinationRuleType type : ProgressionCombinationRuleType.values()) {
			if (wrap.isInstanceOf(iri, type.getClazz())) {
				return type;
			}
		}
		if (wrap.isInstanceOf(iri, OSDiClass.PROGRESSION_COMBINATION_RULE)) {
			log.warn("Progression combination rule " + iri + " is not a subclass of any known combination rule type. Defaulting to COEXISTENT.");
			return COEXISTENT;
		}
		return null;
	}
}