package es.ull.simulation.hta.osdi.des;

import java.util.ArrayList;
import es.ull.simulation.condition.AbstractCondition;
import es.ull.simulation.condition.TrueCondition;
import es.ull.simulation.hta.osdi.des.factories.ParameterFactory;
import es.ull.simulation.hta.osdi.des.factories.TimeToEventCalculatorFactory;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.ontology.OSDiDataItemType;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper;
import es.ull.simulation.hta.osdi.ontology.PathwayWrapper;
import es.ull.simulation.hta.params.ParameterGroup;
import es.ull.simulation.hta.params.StandardParameter;
import es.ull.simulation.hta.progression.DiseaseProgressionPathway;
import es.ull.simulation.hta.progression.calculator.TimeToEventCalculator;

public class OSDiManifestationPathway extends DiseaseProgressionPathway {
	private final ArrayList<ParameterWrapper> riskWrappers;

	public OSDiManifestationPathway(OSDiDESModel model, PathwayWrapper pathway, OSDiDiseaseProgression destManifestation,
			TimeToEventCalculator timeToEvent, AbstractCondition<DiseaseProgressionPathway.ConditionInformation> condition) throws MalformedOSDiModelException {
		super(model, pathway.getShortName(), pathway.getDescription().orElse(""), destManifestation, timeToEvent, condition);
		this.riskWrappers = new ArrayList<>(pathway.getRiskParameters());
	}
	
	public OSDiManifestationPathway(OSDiDESModel model, OSDiDiseaseProgression destManifestation, ArrayList<ParameterWrapper> riskWrappers) throws MalformedOSDiModelException {
		super(model, "PATH_" + destManifestation.name(), "Progression to " + destManifestation.getDescription(),  destManifestation, 
			TimeToEventCalculatorFactory.getTimeToEventCalculator(model, destManifestation, riskWrappers), 
			new TrueCondition<DiseaseProgressionPathway.ConditionInformation>());
		this.riskWrappers = riskWrappers;
	}

	@Override
	public void createParameters() {
		for (ParameterWrapper riskWrapper : riskWrappers) {
			final OSDiDataItemType dataItems = riskWrapper.getDataItemType();
			if (dataItems.equals(OSDiDataItemType.DI_PROBABILITY)) {
				StandardParameter.PROBABILITY.addToModel(model, ParameterFactory.getParameterInstance((OSDiDESModel)model, riskWrapper, ParameterGroup.RISK));
			}
			else if (dataItems.equals(OSDiDataItemType.DI_PROPORTION)) {
				StandardParameter.PROPORTION.addToModel(model, ParameterFactory.getParameterInstance((OSDiDESModel)model, riskWrapper, ParameterGroup.RISK));
			}
			else if (dataItems.equals(OSDiDataItemType.DI_RELATIVE_RISK)) {
				StandardParameter.RELATIVE_RISK.addToModel(model, ParameterFactory.getParameterInstance((OSDiDESModel)model, riskWrapper, ParameterGroup.RISK));
			}

		}
	}
	
}