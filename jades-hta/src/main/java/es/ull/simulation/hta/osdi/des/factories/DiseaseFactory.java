package es.ull.simulation.hta.osdi.des.factories;

import java.util.Set;

import es.ull.simulation.hta.osdi.des.OSDiDESModel;
import es.ull.simulation.hta.osdi.des.OSDiDisease;
import es.ull.simulation.hta.osdi.des.OSDiDiseaseProgression;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.ClinicalProgressionWrapper;
import es.ull.simulation.hta.osdi.ontology.DiseaseWrapper;
import es.ull.simulation.hta.osdi.ontology.PopulationWrapper;
import es.ull.simulation.hta.progression.DiseaseProgression;

/**
 * Allows the creation of a {@link StandardDisease} based on the information stored in the ontology
 * @author Iván Castilla Rodríguez
 * @author David Prieto González
 */
public interface DiseaseFactory extends DESModelComponentFactory {
	/**
	 * Creates an instance of a disease from the information stored in the ontology. The population is required because some parameters are population-dependant; 
	 * e.g. initial proportion of manifestations should be related both to a manifestation and a population.
	 * @param model Common parameters repository
	 * @param diseaseIRI Name of the disease, used as IRI in the ontology
	 * @param populationIRI Name of the population, used as IRI in the ontology. 
	 * @return An instance of a disease 
	 * @throws MalformedOSDiModelException 
	 * @throws UnsupportedOSDiFeatureException 
	 */
	public static OSDiDisease getDiseaseInstance(OSDiDESModel model, DiseaseWrapper diseaseWrapper, PopulationWrapper populationWrapper) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
		final OSDiDisease disease = new OSDiDisease(model, diseaseWrapper);
		// Build developments
		final Set<ClinicalProgressionWrapper> developments = diseaseWrapper.getDevelopments();
		for (ClinicalProgressionWrapper developmentWrapper : developments) {
			DevelopmentFactory.getDevelopmentInstance(model, developmentWrapper, disease);
		}		
		// Build manifestations
		final Set<ClinicalProgressionWrapper> progressions = diseaseWrapper.getManifestations();
		for (ClinicalProgressionWrapper progressionWrapper : progressions) {
			DiseaseProgressionFactory.getDiseaseProgressionInstance(model, progressionWrapper, disease, populationWrapper);
		}
		// Build stages
		final Set<ClinicalProgressionWrapper> stages = diseaseWrapper.getStages();
		for (ClinicalProgressionWrapper stageWrapper : stages) {
			DiseaseProgressionFactory.getDiseaseProgressionInstance(model, stageWrapper, disease, populationWrapper);
		}

		// Build progression pathways after creating all the manifestations
		for (ClinicalProgressionWrapper progressionWrapper : progressions) {
			final DiseaseProgression progression = disease.getDiseaseProgression(progressionWrapper.getShortName());
			DiseaseProgressionRiskFactory.getPathwayInstances(model, (OSDiDiseaseProgression) progression);
			// Also include exclusions among progressions
			final Set<ClinicalProgressionWrapper> exclusions = progressionWrapper.getSupersededProgressions();
			for (ClinicalProgressionWrapper excludedManif : exclusions) {
				disease.addExclusion(progression, disease.getDiseaseProgression(excludedManif.getShortName()));
			}
		}
		return disease;
	}
}
