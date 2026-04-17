package es.ull.simulation.hta.osdi.ontology;

import java.util.TreeMap;

import org.semanticweb.owlapi.model.IRI;

import es.ull.simulation.hta.osdi.OSDiLogger;

/**
 * A list of the different types of interventions that can be defined in OSDi. The intervention type is one of the key indicators on 
 * how to generate a model.
 */
public enum InterventionType implements WrapsOSDiClass {
	DIAGNOSIS(OSDiClass.DIAGNOSIS_INTERVENTION, ResourceUsageSKOSCategory.DIAGNOSIS),
	SCREENING(OSDiClass.SCREENING_INTERVENTION, ResourceUsageSKOSCategory.SCREENING),
	THERAPEUTIC(OSDiClass.THERAPEUTIC_INTERVENTION, ResourceUsageSKOSCategory.TREATMENT);

	private final static OSDiLogger	log = OSDiLogger.getLogger(InterventionType.class);
	private final static TreeMap<OSDiClass, InterventionType> reverseInterventionType = new TreeMap<>();
	static {
		for (InterventionType type : InterventionType.values()) {
			reverseInterventionType.put(type.clazz, type);
		}
	}
	private final OSDiClass clazz;
	private final ResourceUsageSKOSCategory category;
	private InterventionType(OSDiClass clazz, ResourceUsageSKOSCategory category) {
		this.clazz = clazz;
		this.category = category;
	}

	@Override
	public OSDiClass getClazz() {
		return clazz;
	}
	
	/**
	 * Returns the ResourceUsageSKOSCategory associated with this intervention type.
	 * @return The ResourceUsageSKOSCategory associated with this intervention type.
	 */
	public ResourceUsageSKOSCategory getCategory() {
		return category;
	}

	/**
	 * Returns the InterventionTypeWrapper corresponding to the given OSDi class.
	 * @param clazz The OSDiClasses to get the InterventionTypeWrapper for.
	 * @return The InterventionTypeWrapper associated with the given OSDiClasses.
	 */
	public static InterventionType fromOSDiClass(OSDiClass clazz) {
		return reverseInterventionType.get(clazz);
	}

	/**
	 * Returns the intervention type corresponding to the given IRI in the provided OSDi wrapper.
	 * @param iri The IRI to get the InterventionTypeWrapper for.
	 * @param wrap The OSDiWrapper containing the ontology information.
	 * @return The InterventionTypeWrapper associated with the given IRI, or null if none matches.
	 */
	public static InterventionType fromIRI(IRI iri, OSDiWrapper wrap) {
		for (InterventionType type : InterventionType.values()) {
			if (wrap.isInstanceOf(iri, type.getClazz())) {
				return type;
			}
		}
		if (wrap.isInstanceOf(iri, OSDiClass.DETECTION_INTERVENTION)) {
			log.warn("Intervention " + iri + " is not a subclass of " + OSDiClass.DIAGNOSIS_INTERVENTION.getShortName() + " nor " + OSDiClass.SCREENING_INTERVENTION.getShortName() + ". Defaulting to " + OSDiClass.DIAGNOSIS_INTERVENTION.getShortName() + ".");
			return DIAGNOSIS;
		}
		if (wrap.isInstanceOf(iri, OSDiClass.INTERVENTION)) {
			log.warn("Intervention " + iri + " is not a subclass of any known intervention type. Defaulting to " + OSDiClass.THERAPEUTIC_INTERVENTION.getShortName() + ".");
			return THERAPEUTIC;
		}
		return null;
	}

	/**
	 * Returns the intervention type corresponding to the given SKOS category.
	 * @param category The SKOS category to get the intervention type for.
	 * @return The intervention type corresponding to the given SKOS category, or null if none matches.
	 */
	public static InterventionType fromSKOSCategory(ResourceUsageSKOSCategory category) {
		for (InterventionType type : InterventionType.values()) {
			if (type.getCategory().equals(category)) {
				return type;
			}
		}
		return null;
	}
}