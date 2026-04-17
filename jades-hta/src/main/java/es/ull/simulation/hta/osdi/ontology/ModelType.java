package es.ull.simulation.hta.osdi.ontology;

import java.util.TreeMap;

import org.semanticweb.owlapi.model.IRI;

import es.ull.simulation.hta.osdi.OSDiLogger;

/**
 * A list of the different types of models that can be defined in OSDi
 * @author Iván Castilla
 *
 */
public enum ModelType implements WrapsOSDiClass {
	DES(OSDiClass.DISCRETE_EVENT_SIMULATION_MODEL),
	AGENT(OSDiClass.AGENT_BASED_MODEL),
	MARKOV(OSDiClass.MARKOV_MODEL),
	DECISION_TREE(OSDiClass.DECISION_TREE_MODEL);

	private final static OSDiLogger	log = OSDiLogger.getLogger(ModelType.class);
	private final static TreeMap<OSDiClass, ModelType> reverseModelType = new TreeMap<>(); 
	static {
		for (ModelType type : ModelType.values()) {
			reverseModelType.put(type.clazz, type);
		}
	}

	private final OSDiClass clazz;
	private ModelType(OSDiClass clazz) {
		this.clazz = clazz;
	}

	@Override
	public OSDiClass getClazz() {
		return clazz;
	}

	/**
 	 * Returns the ModelTypeWrapper associated with the given OSDiClasses.
	 * @param clazz The OSDiClasses to get the ModelTypeWrapper for.
	 * @return The ModelTypeWrapper associated with the given OSDiClasses.
	 */
	public static ModelType fromOSDiClass(OSDiClass clazz) {
		return reverseModelType.get(clazz);
	}

	/**
	 * Returns the model type corresponding to the given IRI in the provided OSDi wrapper.
	 * @param iri The IRI to get the ModelTypeWrapper for.
	 * @param wrap The OSDiWrapper containing the ontology information.
	 * @return The ModelTypeWrapper associated with the given IRI, or null if none matches.
	 */
	public static ModelType fromIRI(IRI iri, OSDiWrapper wrap) {
		for (ModelType type : ModelType.values()) {
			if (wrap.isInstanceOf(iri, type.getClazz())) {
				return type;
			}
		}
		if (wrap.isInstanceOf(iri, OSDiClass.MODEL)) {
			log.warn("Model " + iri + " is not a subclass of any known model type. Defaulting to DECISION_TREE.");
			return DECISION_TREE;
		}
		return null;
	}
}