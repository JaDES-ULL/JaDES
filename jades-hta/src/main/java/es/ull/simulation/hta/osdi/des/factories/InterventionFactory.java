/**
 * 
 */
package es.ull.simulation.hta.osdi.des.factories;

import es.ull.simulation.hta.interventions.DoNothingIntervention;
import es.ull.simulation.hta.interventions.Intervention;
import es.ull.simulation.hta.osdi.des.OSDiDESModel;
import es.ull.simulation.hta.osdi.des.OSDiIntervention;
import es.ull.simulation.hta.osdi.des.OSDiDetectionIntervention;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.DetectionInterventionWrapper;
import es.ull.simulation.hta.osdi.ontology.InterventionWrapper;

/**
 * @author Iván Castilla
 */
public interface InterventionFactory extends DESModelComponentFactory {
	public static String DO_NOTHING = "DO_NOTHING";

	public static Intervention getInterventionInstance(OSDiDESModel model, InterventionWrapper interventionWrapper) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
		String interventionName = interventionWrapper.getShortName();
		if (DO_NOTHING.equals(interventionName))
			return new DoNothingIntervention(model);
		// TODO: Populate different methods for different interventions
		if (interventionWrapper instanceof DetectionInterventionWrapper)
			return new OSDiDetectionIntervention(model, (DetectionInterventionWrapper) interventionWrapper);
		return new OSDiIntervention(model, interventionWrapper);
	}
}
