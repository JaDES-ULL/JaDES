package es.ull.simulation.hta.osdi.des;

import java.util.Set;

import es.ull.simulation.hta.MalformedSimulationModelException;
import es.ull.simulation.hta.Patient;
import es.ull.simulation.hta.osdi.OSDiLogger;
import es.ull.simulation.hta.osdi.des.factories.ParameterFactory;
import es.ull.simulation.hta.osdi.des.factories.UtilityParametersHandler;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.EpidemiologicCharacterizationType;
import es.ull.simulation.hta.osdi.ontology.OSDiDataItemType;
import es.ull.simulation.hta.osdi.ontology.OSDiDataProperty;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper;
import es.ull.simulation.hta.osdi.ontology.PopulationWrapper;
import es.ull.simulation.hta.params.ParameterGroup;
import es.ull.simulation.hta.params.StandardParameter;
import es.ull.simulation.hta.populations.StdPopulation;
import es.ull.simulation.hta.progression.calculator.EmpiricalSpainDeathSubmodel;
import es.ull.simulation.hta.progression.calculator.TimeToEventCalculator;
import simkit.random.DiscreteRandomVariate;
import simkit.random.RandomVariate;
import simkit.random.RandomVariateFactory;

public class OSDiPopulation extends StdPopulation {
	private final static OSDiLogger log = OSDiLogger.getLogger(OSDiPopulation.class);
	private final RandomVariate ageVariate;
	private final DiscreteRandomVariate sexVariate;
	private final Set<ParameterWrapper> epidemParams;
	private final ParameterWrapper utilityParam;
	private final Set<ParameterWrapper> attributeValues;
	private final OSDiDisease disease;
	
	public OSDiPopulation(OSDiDESModel model, PopulationWrapper populationWrapper, OSDiDisease disease) throws MalformedOSDiModelException, MalformedSimulationModelException, UnsupportedOSDiFeatureException {
		super(model, populationWrapper.getShortName(), populationWrapper.getDescription().orElse(""), disease);
		this.disease = disease;
		if (populationWrapper.getMinAge().isPresent()) {
			setMinAge(populationWrapper.getMinAge().getAsDouble());
		} else {
			log.warn(populationWrapper.getShortName(), OSDiDataProperty.HAS_MIN_AGE, "Invalid or not defined minimum age format for population " + populationWrapper.getShortName() + ". Using default value " + super.getMinAge());
			setMinAge(super.getMinAge());
		}
		if (populationWrapper.getMaxAge().isPresent()) {
			setMaxAge(populationWrapper.getMaxAge().getAsDouble());
		} else {
			log.warn(populationWrapper.getShortName(), OSDiDataProperty.HAS_MAX_AGE, "Invalid or not defined maximum age format for population " + populationWrapper.getShortName() + ". Using default value " + super.getMaxAge());
			setMaxAge(super.getMaxAge());
		}
		 // TODO: Currently we are only defining initially assigned attributes, i.e., attributes whose value does not change during the simulation 
		// Process population age
		final ParameterWrapper ageWrapper = populationWrapper.getAgeParameter().orElseThrow(() -> new MalformedOSDiModelException("Population " + populationWrapper.getShortName() + " does not have a valid age parameter."));
		ageVariate = ParameterFactory.getRandomVariateInstance(ageWrapper);
		// Process population sex
		final ParameterWrapper sexWrapper = populationWrapper.getSexParameter().orElseThrow(() -> new MalformedOSDiModelException("Population " + populationWrapper.getShortName() + " does not have a valid sex parameter."));
		sexVariate = (DiscreteRandomVariate) ParameterFactory.getRandomVariateInstance(sexWrapper);
		
		this.utilityParam = getAndCheckUtilityParam(populationWrapper);
		if (utilityParam != null)
			setUsedParameterName(StandardParameter.POPULATION_BASE_UTILITY, utilityParam.getShortName());
		
		this.epidemParams = populationWrapper.getEpidemiologicalCharacterizationOfDisease(disease.getDiseaseWrapper());
		
		// Process other attribute values
		attributeValues = populationWrapper.getAttributeValues();
	}
	
	public OSDiDESModel getModel() {
		return (OSDiDESModel) super.getModel();
	}

	@Override
	public void createParameters() {
		for (ParameterWrapper attrWrapper : attributeValues) {
			model.addParameter(ParameterFactory.getParameterInstance(getModel(), attrWrapper, ParameterGroup.ATTRIBUTE));
		}
		if (epidemParams.isEmpty()) {
			log.warn("No valid epidemiological parameter found for population " + name() + " and disease " + disease.name() + ". We will assume a full prevalence for the population and disease.");
		}
		else {
			// FIXME: Look for a more sophisticated way of doing this. Currently just using the first one found. We should consider how to use the best suited epidemiological parameter in case of finding more than one (e.g. true vs apparent epidemiological parameters, or parameters with different levels of evidence)
			ParameterWrapper epidemParam = epidemParams.iterator().next();
			OSDiDataItemType epidemType = epidemParam.getDataItemType();
			EpidemiologicCharacterizationType epidemiologicalCharacterization = EpidemiologicCharacterizationType.fromDataItemType(epidemType);
			switch (epidemiologicalCharacterization) {
				case PREVALENCE:
					StandardParameter.PREVALENCE.addToModel(model, ParameterFactory.getParameterInstance(getModel(), epidemParam, ParameterGroup.RISK));
					break;
				case BIRTH_PREVALENCE:
					StandardParameter.BIRTH_PREVALENCE.addToModel(model, ParameterFactory.getParameterInstance(getModel(), epidemParam, ParameterGroup.RISK));
					break;
				case INCIDENCE:
					StandardParameter.INCIDENCE.addToModel(model, ParameterFactory.getParameterInstance(getModel(), epidemParam, ParameterGroup.RISK));
					break;
				default:
					break;
			}
		}

		if (utilityParam != null)
			addUsedParameter(StandardParameter.POPULATION_BASE_UTILITY, ParameterFactory.getParameterInstance(getModel(), utilityParam, StandardParameter.POPULATION_BASE_UTILITY.getGroup())); 
	}
	
	/**
	 * Registers the base utility associated to the population by extracting the information from the ontology. 
	 * @throws MalformedOSDiModelException When there was a problem parsing the ontology
	 * @throws UnsupportedOSDiFeatureException 
	 */
	private ParameterWrapper getAndCheckUtilityParam(PopulationWrapper populationWrapper) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
		if (populationWrapper.getUtilities().isEmpty()) { 
			return null;
		}
		// We expect just one utility parameter
		UtilityParametersHandler utilityHandler = new UtilityParametersHandler(populationWrapper.getUtilities());
		Set<ParameterWrapper> utilityParams = utilityHandler.getUtilityParametersByTypeAndApplication(false, false);
		if (utilityParams.isEmpty()) {
			if (!utilityHandler.getUtilityParametersByApplication(true).isEmpty())
				log.warn("Population " + populationWrapper.getShortName() + " defines at least one-time utility but no annual utility. Only annual utilities should be associated to a population. Ignoring the one-time utility defined and not using any base utility for the population.");
			if (!utilityHandler.getUtilityParametersByType(false).isEmpty())
				log.warn("Population " + populationWrapper.getShortName() + " defines at least one disutility but no regular utility. Only regular utilities should be associated to a population. Ignoring the disutility defined and not using any base utility for the population.");
			return null;
		}
		if (utilityParams.size() > 1) {
			log.warn("Population " + populationWrapper.getShortName() + " defines more than one regular annual utility. Only the first one found will be used. The following parameters were found: " + utilityParams);
		}
		return utilityParams.iterator().next();
	}

	@Override
	protected DiscreteRandomVariate getSexVariate(Patient pat) {
		return sexVariate;
	}
	
	@Override
	protected DiscreteRandomVariate getDiseaseVariate(Patient pat) {
		if (!epidemParams.isEmpty())
			return RandomVariateFactory.getDiscreteRandomVariateInstance("BernoulliVariate", getCommonRandomNumber(), model.getParameterValue(epidemParams.iterator().next().getShortName(), pat));
		return RandomVariateFactory.getDiscreteRandomVariateInstance("BernoulliVariate", getCommonRandomNumber(), 1.0);
	}
	
	@Override
	protected DiscreteRandomVariate getDiagnosedVariate(Patient pat) {
		// TODO Do something with true and apparent epidemiologic parameters
		return RandomVariateFactory.getDiscreteRandomVariateInstance("BernoulliVariate", getCommonRandomNumber(), 1.0);
	}
	
	@Override
	protected RandomVariate getBaselineAgeVariate(Patient pat) {
		return ageVariate;
	}

	@Override
	public TimeToEventCalculator initializeDeathCharacterization() {
		// TODO: Death submodel should be context specific, depending on the population
		return new EmpiricalSpainDeathSubmodel(getModel());
	}

}