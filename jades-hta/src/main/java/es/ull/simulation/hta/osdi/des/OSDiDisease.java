package es.ull.simulation.hta.osdi.des;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import es.ull.simulation.hta.osdi.des.factories.CostParametersHandler;
import es.ull.simulation.hta.osdi.des.factories.ParameterFactory;
import es.ull.simulation.hta.osdi.des.factories.UtilityParametersHandler;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.DiseaseWrapper;
import es.ull.simulation.hta.osdi.ontology.OSDiWrapper;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper;
import es.ull.simulation.hta.osdi.ontology.ResourceUsageSKOSCategory;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper.SpecificInformationForUtility;
import es.ull.simulation.hta.params.ParameterTemplate;
import es.ull.simulation.hta.params.StandardParameter;
import es.ull.simulation.hta.progression.Disease;

public class OSDiDisease extends Disease {
	final private Map<ParameterTemplate, ParameterWrapper> paramMapping;
	final OSDiWrapper wrap;
	final private DiseaseWrapper diseaseWrapper;
	final private CostParametersHandler	costParametersHandler;
	final private UtilityParametersHandler utilityHandler;

	public OSDiDisease(OSDiDESModel model, DiseaseWrapper diseaseWrapper) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
		super(model, diseaseWrapper.getShortName(), diseaseWrapper.getDescription().orElse(""));
		paramMapping = new TreeMap<>();
		wrap = model.getOwlWrapper();
		this.diseaseWrapper = diseaseWrapper;
		this.costParametersHandler = new CostParametersHandler(diseaseWrapper.getCosts());
		this.utilityHandler = new UtilityParametersHandler(diseaseWrapper.getUtilities());

		// Create parameters
        // FIXME: Currently only using the first annual cost found. We should consider how to include multiple costs (e.g. annual cost, diagnosis cost, etc.) in the model
        Set<ParameterWrapper> costParams = costParametersHandler.getCostParametersByApplication(false);
		if (!costParams.isEmpty()) {
            paramMapping.put(StandardParameter.ANNUAL_COST, costParams.iterator().next());
        }
		costParams = costParametersHandler.getCostParametersByCategoryAndApplication(ResourceUsageSKOSCategory.DIAGNOSIS, true);
		if (!costParams.isEmpty()) {
			paramMapping.put(StandardParameter.DISEASE_DIAGNOSIS_COST, costParams.iterator().next());
		}

		Set<ParameterWrapper> utilityParams = utilityHandler.getUtilityParametersByApplication(false);
		if (!utilityParams.isEmpty()) {
			ParameterWrapper utilityParam = utilityParams.iterator().next();
			boolean isDisutility = ((SpecificInformationForUtility) utilityParam.getSpecificInformation()).isDisutility();
			paramMapping.put(isDisutility ? StandardParameter.ANNUAL_DISUTILITY : StandardParameter.ANNUAL_UTILITY, utilityParam);
		}
	}

	public DiseaseWrapper getDiseaseWrapper() {
		return diseaseWrapper;
	}

	public OSDiDESModel getModel() {
		return (OSDiDESModel) super.getModel();
	}
	
	@Override
	public void createParameters() {
		for (ParameterTemplate paramDesc : paramMapping.keySet()) {
			final ParameterWrapper param = paramMapping.get(paramDesc);
			addUsedParameter(paramDesc, ParameterFactory.getParameterInstance(getModel(), param, paramDesc.getGroup()));
		}
	}
	
}