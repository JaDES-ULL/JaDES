package es.ull.simulation.hta.osdi.ontology;

public interface IOSDiComponentWrapper {
	/**
	 * Returns the short name of this component in the ontology.
	 * @return the short name of this component in the ontology.
	 */
	public String getShortName();

    /**
     * Returns whether this component is a core component of the OSDi model.
     * Core components are those that are essential for the structure of the OSDi model and are expected to be present in any valid OSDi model.
     * @return true if this component is a core component of the OSDi model, false otherwise.
     */
	public boolean isCore();
}
