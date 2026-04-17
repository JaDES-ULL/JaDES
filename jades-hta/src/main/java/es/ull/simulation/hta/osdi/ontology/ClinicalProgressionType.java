package es.ull.simulation.hta.osdi.ontology;

import java.util.TreeMap;

import org.semanticweb.owlapi.model.IRI;

/**
 * A list of the different types of clinical progression that can be defined in OSDi. 
 */
public enum ClinicalProgressionType implements WrapsOSDiClass {
	ACUTE_MANIFESTATION(OSDiClass.ACUTE_MANIFESTATION),
	CHRONIC_MANIFESTATION(OSDiClass.CHRONIC_MANIFESTATION),
	DEVELOPMENT(OSDiClass.DEVELOPMENT),
	STAGE(OSDiClass.STAGE);

	private final static TreeMap<OSDiClass, ClinicalProgressionType> reverseClinicalProgression = new TreeMap<>();
	static {
		for (ClinicalProgressionType type : ClinicalProgressionType.values()) {
			reverseClinicalProgression.put(type.clazz, type);
		}
	}
	private final OSDiClass clazz;
	private ClinicalProgressionType(OSDiClass clazz) {
		this.clazz = clazz;
	}

	@Override
	public OSDiClass getClazz() {
		return clazz;
	}
	
	/**
	 * Returns the ClinicalProgressionWrapper corresponding to the given OSDi class.
	 * @param clazz The OSDiClasses to get the ClinicalProgressionWrapper for.
	 * @return The ClinicalProgressionWrapper associated with the given OSDiClasses.
	 */
	public static ClinicalProgressionType fromOSDiClass(OSDiClass clazz) {
		return reverseClinicalProgression.get(clazz);
	}

	/**
	 * Returns the clinical progression corresponding to the given IRI in the provided OSDi wrapper.
	 * @param iri The IRI to get the ClinicalProgressionWrapper for.
	 * @param wrap The OSDiWrapper containing the ontology information.
	 * @return The ClinicalProgressionWrapper associated with the given IRI, or null if none matches.
	 */
	public static ClinicalProgressionType fromIRI(IRI iri, OSDiWrapper wrap) {
		for (ClinicalProgressionType type : ClinicalProgressionType.values()) {
			if (wrap.isInstanceOf(iri, type.getClazz())) {
				return type;
			}
		}
		return null;
	}

	public String getFormattedString() {
		return this.name().toLowerCase().replace('_', ' ');
	}
}