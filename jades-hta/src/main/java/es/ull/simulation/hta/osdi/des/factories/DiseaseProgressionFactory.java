/**
 * 
 */
package es.ull.simulation.hta.osdi.des.factories;

import es.ull.simulation.hta.osdi.des.OSDiDESModel;
import es.ull.simulation.hta.osdi.des.OSDiDiseaseProgression;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.ClinicalProgressionWrapper;
import es.ull.simulation.hta.osdi.ontology.PopulationWrapper;
import es.ull.simulation.hta.progression.Disease;
import es.ull.simulation.hta.progression.DiseaseProgression;

/**
 * @author David Prieto González
 * @author Iván Castilla Rodríguez
 */
public interface DiseaseProgressionFactory extends DESModelComponentFactory {

	public static DiseaseProgression getDiseaseProgressionInstance(OSDiDESModel model, ClinicalProgressionWrapper progressionWrapper, Disease disease, PopulationWrapper populationWrapper) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
		return new OSDiDiseaseProgression(model, progressionWrapper, disease, populationWrapper);
	}
}
