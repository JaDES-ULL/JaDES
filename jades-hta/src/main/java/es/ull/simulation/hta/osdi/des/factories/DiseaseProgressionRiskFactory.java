/**
 * 
 */
package es.ull.simulation.hta.osdi.des.factories;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import es.ull.simulation.condition.AndCondition;
import es.ull.simulation.condition.OrCondition;
import es.ull.simulation.condition.AbstractCondition;
import es.ull.simulation.condition.TrueCondition;
import es.ull.simulation.hta.osdi.des.OSDiDESModel;
import es.ull.simulation.hta.osdi.des.OSDiDiseaseProgression;
import es.ull.simulation.hta.osdi.des.OSDiManifestationPathway;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.ontology.ClinicalProgressionWrapper;
import es.ull.simulation.hta.osdi.ontology.IModelItemWrapper;
import es.ull.simulation.hta.osdi.ontology.InterventionWrapper;
import es.ull.simulation.hta.osdi.ontology.OSDiClass;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper;
import es.ull.simulation.hta.osdi.ontology.PathwayWrapper;
import es.ull.simulation.hta.osdi.ontology.OSDiObjectProperty;
import es.ull.simulation.hta.progression.Disease;
import es.ull.simulation.hta.progression.DiseaseProgression;
import es.ull.simulation.hta.progression.DiseaseProgressionPathway;
import es.ull.simulation.hta.progression.calculator.TimeToEventCalculator;
import es.ull.simulation.hta.progression.condition.InterventionCondition;
import es.ull.simulation.hta.progression.condition.PreviousDiseaseProgressionCondition;

/**
 * @author Iván Castilla Rodríguez
 * @author David Prieto González
 * TODO: Process different types of combination of parameters (P + RR, TTE, ...) to create the time to event 
 */
public interface DiseaseProgressionRiskFactory extends DESModelComponentFactory {

	/**
	 * Creates a {@link DiseaseProgressionPathway manifestation pathway}. If, for any reason, a manifestation pathway was already created for the specified name, returns the 
	 * previously created pathway.
	 * @param model
	 * @param progression
	 * @return
	 * @throws MalformedOSDiModelException 
	 */
	public static ArrayList<DiseaseProgressionPathway> getPathwayInstances(OSDiDESModel model, OSDiDiseaseProgression progression) throws MalformedOSDiModelException {
		final ArrayList<DiseaseProgressionPathway> pathwayInstances = new ArrayList<>();
		final ArrayList<ParameterWrapper> riskWrappers = new ArrayList<>();
		final Set<IModelItemWrapper> riskParameters = progression.getProgressionWrapper().getProgressionRisks();
		for (IModelItemWrapper riskParameter : riskParameters) {
			if (riskParameter instanceof PathwayWrapper) {
				pathwayInstances.add(getPathwayInstance(model, progression, (PathwayWrapper)riskParameter));
			}
			else if (riskParameter instanceof ParameterWrapper) {
				riskWrappers.add((ParameterWrapper)riskParameter);
			}
			else {
				throw new MalformedOSDiModelException(OSDiClass.CLINICAL_PROGRESSION_ELEMENT, progression.name(), OSDiObjectProperty.HAS_RISK_CHARACTERIZATION, "Unknown risk characterizations for a disease progression: " + riskParameter.getIndividualIRI());
			}
		}
		
		if (riskWrappers.size() > 0) {
			pathwayInstances.add(new OSDiManifestationPathway(model, progression, riskWrappers));
		}
		return pathwayInstances;
	}
	
	public static DiseaseProgressionPathway getPathwayInstance(OSDiDESModel model, OSDiDiseaseProgression progression, PathwayWrapper pathway) throws MalformedOSDiModelException {
		final AbstractCondition<DiseaseProgressionPathway.ConditionInformation> cond = createCondition(model, progression.getDisease(), pathway);

		final ArrayList<ParameterWrapper> riskWrappers = new ArrayList<>(pathway.getRiskParameters());

		if (riskWrappers.size() == 0)
			throw new MalformedOSDiModelException(OSDiClass.CLINICAL_PROGRESSION_ELEMENT, pathway.getShortName(), OSDiObjectProperty.HAS_PARAMETER, "At least one valid risk characterizations for a disease progression pathway is required.");

		final TimeToEventCalculator tte = TimeToEventCalculatorFactory.getTimeToEventCalculator(model, progression, riskWrappers);
		return new OSDiManifestationPathway(model, pathway, progression, tte, cond);
	}

	/**
	 * Creates a condition for the pathway. Conditions may be expressed by one or more strings in a "hasCondition" data property, or as object properties by means of "requiresPreviousManifestation".
	 * @param model Repository
	 * @param disease The disease
	 * @param pathwayIRI The IRI of the pathway instance in the ontology for which the condition is being created
	 * @return A condition for the pathway
	 */
	private static AbstractCondition<DiseaseProgressionPathway.ConditionInformation> createCondition(OSDiDESModel model, Disease disease, PathwayWrapper pathway) {		
		final ArrayList<AbstractCondition<DiseaseProgressionPathway.ConditionInformation>> condList = new ArrayList<>();
		// Required clinical progressions are combined as a logical AND
		if (pathway.getRequiredClinicalProgressions().size() > 0) {
			final List<DiseaseProgression> manifList = new ArrayList<>();
			for (ClinicalProgressionWrapper requiredProgression : pathway.getRequiredClinicalProgressions()) {
				manifList.add(disease.getDiseaseProgression(requiredProgression.getShortName()));
			}
			condList.add(new PreviousDiseaseProgressionCondition(manifList));
		}
		// Required interventions are combined as a logical OR, and then combined with the previous conditions as a logical AND
		if (pathway.getRequiredInterventions().size() > 0) {
			final ArrayList<AbstractCondition<DiseaseProgressionPathway.ConditionInformation>> interventionConditions = new ArrayList<>();
			for (InterventionWrapper requiredIntervention : pathway.getRequiredInterventions()) {
				interventionConditions.add(new InterventionCondition(model.getIntervention(requiredIntervention.getShortName())));
			}
			condList.add(new OrCondition<DiseaseProgressionPathway.ConditionInformation>(interventionConditions));
		}
		// After going through for previous manifestations and other conditions, checks how many conditions were created
		if (condList.size() == 0)
			return new TrueCondition<DiseaseProgressionPathway.ConditionInformation>();
		if (condList.size() == 1)
			return condList.get(0);
		return new AndCondition<DiseaseProgressionPathway.ConditionInformation>(condList);
	}
	
	/**
	 * Creates a proper name for the second-order parameter that represents the probability associated to this pathway. To ensure unique name, and as a rule of thumb, 
	 * if the name of the pathway instance already includes the name of the destination manifestation, uses the name of the pathway instance. Otherwise, suffixes the 
	 * name of the destination manifestation to the name of the pathway instance. In any case, includes a prefix to indicate that the parameter is a probability. 
	 * @param manifestation The destination manifestation for this pathway
	 * @param pathwayName The name of the pathway instance in the ontology
	 * @return a proper name for the second-order parameter that represents the probability associated to this pathway
	 */
	public static String getProbString(DiseaseProgression manifestation, String pathwayName) {
		if (pathwayName.contains(manifestation.name())) {
			return pathwayName; 
		}
		else {
			return pathwayName + "_" + manifestation.name(); 			
		}
	}

	/**
	 * Creates a proper description for the second-order parameter that represents the probability associated to this pathway.  
	 * @param manifestation The destination manifestation for this pathway
	 * @param pathwayName The name of the pathway instance in the ontology
	 * @return a proper description for the second-order parameter that represents the probability associated to this pathway
	 */
	public static String getDescriptionString(DiseaseProgression manifestation, String pathwayName) {
		return "Probability of developing " + manifestation + " due to " + pathwayName; 
	}
}
