package es.ull.simulation.hta.osdi.ontology;

/**
 * This interface can be used to mark classes that wrap OSDi classes.
 * It provides a method to retrieve the associated class in the ontology.
 */
public interface WrapsOSDiClass {
	/**
	 * Returns the associated class in the ontology
	 * @return The associated class in the ontology
	 */
    public OSDiClass getClazz();
}
