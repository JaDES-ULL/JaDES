package es.ull.simulation.hta.osdi.des.factories;

import java.util.ArrayList;
import java.util.Set;

import es.ull.simulation.hta.osdi.OSDiLogger;
import es.ull.simulation.hta.osdi.des.OSDiDESModel;
import es.ull.simulation.hta.osdi.des.OSDiDisease;
import es.ull.simulation.hta.osdi.des.OSDiDiseaseProgression;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.ClinicalProgressionType;
import es.ull.simulation.hta.osdi.ontology.ClinicalProgressionWrapper;
import es.ull.simulation.hta.osdi.ontology.DiseaseWrapper;
import es.ull.simulation.hta.osdi.ontology.PopulationWrapper;
import es.ull.simulation.hta.osdi.ontology.ProgressionCombinationRuleWrapper;
import es.ull.simulation.hta.progression.DiseaseProgression;
import es.ull.simulation.hta.progression.DiseaseProgressionPathway;

/**
 * Allows the creation of a {@link StandardDisease} based on the information stored in the ontology
 * @author Iván Castilla Rodríguez
 * @author David Prieto González
 */
public interface DiseaseFactory extends DESModelComponentFactory {
	final static OSDiLogger log = OSDiLogger.getLogger(DiseaseFactory.class);

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
		// TODO: Combination rules of progressions are not implemented yet in the DES
		for (ProgressionCombinationRuleWrapper rule : diseaseWrapper.getCombinationRules()) {
			log.warn("The combination rule " + rule.getShortName() + " of the disease " + diseaseWrapper.getShortName() + " is ignored by the DES.");
		}
		// Build developments
		final Set<ClinicalProgressionWrapper> developments = diseaseWrapper.getDevelopments();
		for (ClinicalProgressionWrapper developmentWrapper : developments) {
			DevelopmentFactory.getDevelopmentInstance(model, developmentWrapper, disease);
			// TODO: Developments are not simulated yet in the DES, so their payoffs and effects are not applied
			if (!developmentWrapper.getCosts().isEmpty() || !developmentWrapper.getStrategies().isEmpty()
				|| !developmentWrapper.getUtilities().isEmpty() || !developmentWrapper.getEffects().isEmpty()) {
				log.warn("The costs, utilities and effects of the development " + developmentWrapper.getShortName() + " are ignored by the DES.");
			}
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
			final ArrayList<DiseaseProgressionPathway> pathways = DiseaseProgressionRiskFactory.getPathwayInstances(model, (OSDiDiseaseProgression) progression);
			// Acute manifestations never get an initial proportion in the DES (see OSDiDiseaseProgression)
			final boolean hasInitialProportion = !ClinicalProgressionType.ACUTE_MANIFESTATION.equals(progressionWrapper.getClinicalProgressionType())
				&& populationWrapper.getInitialProportions().containsKey(progressionWrapper);
			if (pathways.isEmpty() && !hasInitialProportion) {
				log.warn("The manifestation " + progressionWrapper.getShortName() + " has neither risk characterization nor initial proportion, so it will never occur in the DES.");
			}
			// Also include exclusions among progressions
			final Set<ClinicalProgressionWrapper> exclusions = progressionWrapper.getSupersededProgressions();
			for (ClinicalProgressionWrapper excludedManif : exclusions) {
				disease.addExclusion(progression, disease.getDiseaseProgression(excludedManif.getShortName()));
			}
		}
		return disease;
	}
}
