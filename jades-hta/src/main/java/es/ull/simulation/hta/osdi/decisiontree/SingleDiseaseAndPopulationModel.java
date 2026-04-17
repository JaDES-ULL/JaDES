package es.ull.simulation.hta.osdi.decisiontree;

import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.EpidemiologicCharacterizationType;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper;

public interface SingleDiseaseAndPopulationModel extends SingleDiseaseModel, SinglePopulationModel {
    ParameterWrapper getEpidemiologicalCharacterizationOfDisease(EpidemiologicCharacterizationType characterization) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException;
}
