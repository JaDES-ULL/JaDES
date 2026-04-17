package es.ull.simulation.hta.osdi.des.factories;

import es.ull.simulation.hta.osdi.des.OSDiDESModel;
import es.ull.simulation.hta.osdi.ontology.ClinicalProgressionWrapper;
import es.ull.simulation.hta.progression.Development;
import es.ull.simulation.hta.progression.Disease;

/**
 * Allows the creation of a {@link Development} based on the information stored in the ontology
 * @author Iván Castilla Rodríguez
 * @author David Prieto González
 */
public interface DevelopmentFactory extends DESModelComponentFactory {

	/**
	 * Creates a {@link Development} based on the information stored in the ontology
	 * @param developmentName Name of the Development as defined in the ontology
	 * @param disease {@link Disease} this {@link Development} is related to
	 * @return a {@link Development} based on the information stored in the ontology
	 */
	public static Development getDevelopmentInstance(OSDiDESModel model, ClinicalProgressionWrapper developmentWrapper, Disease disease) {
		final Development develop = new Development(model, developmentWrapper.getShortName(), developmentWrapper.getDescription().orElse(""), disease);
		return develop;
	}
}
