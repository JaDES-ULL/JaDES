package es.ull.simulation.hta.osdi.des;

import es.ull.simulation.hta.osdi.des.factories.ParameterFactory;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.DetectionInterventionWrapper;
import es.ull.simulation.hta.params.StandardParameter;

public class OSDiDetectionIntervention extends OSDiIntervention {
	public OSDiDetectionIntervention(OSDiDESModel model, DetectionInterventionWrapper interventionWrapper) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
		super(model, interventionWrapper);
	}

	
	public OSDiDESModel getModel() {
		return (OSDiDESModel) super.getModel();
	}

	@Override
	public void createParameters() {
		super.createParameters();
		DetectionInterventionWrapper interventionWrapper = (DetectionInterventionWrapper) getInterventionWrapper();
		if (interventionWrapper.getSensitivityParameter().isEmpty())
			addUsedParameter(StandardParameter.SENSITIVITY, "Assumed sensitivity", "Assumption", 1.0);
		else
			addUsedParameter(StandardParameter.SENSITIVITY, ParameterFactory.getParameterInstance(getModel(), interventionWrapper.getSensitivityParameter().get(), StandardParameter.SENSITIVITY.getGroup()));
		if (interventionWrapper.getSpecificityParameter().isEmpty())
			addUsedParameter(StandardParameter.SPECIFICITY, "Assumed specificity", "Assumption", 1.0);
		else
			addUsedParameter(StandardParameter.SPECIFICITY, ParameterFactory.getParameterInstance(getModel(), interventionWrapper.getSpecificityParameter().get(), StandardParameter.SPECIFICITY.getGroup()));
	}
	
}