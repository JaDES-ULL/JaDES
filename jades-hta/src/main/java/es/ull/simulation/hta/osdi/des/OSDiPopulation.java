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
import es.ull.simulation.hta.osdi.ontology.ParameterNatureType;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper.DeterministicParameterData;
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
	/** The parameter that defines the sex of the patients: the proportion of women, or a first-order discrete distribution */
	private final ParameterWrapper sexWrapper;
	/** The distribution of the sex when the sex parameter is first-order; null otherwise (see {@link #getSexVariate(Patient)}) */
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
		this.sexWrapper = populationWrapper.getSexParameter().orElseThrow(() -> new MalformedOSDiModelException("Population " + populationWrapper.getShortName() + " does not have a valid sex parameter."));
		this.sexVariate = initializeSexVariate(populationWrapper.getShortName());
		
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

	/**
	 * Checks the sex parameter of the population and, if it is a first-order parameter, returns its distribution.
	 * The sex parameter represents the heterogeneity of the sex in the population (first-order uncertainty), with 0 for men and 1 for women:
	 * <ul>
	 * <li>A first-order parameter must be a discrete distribution (e.g., Bernoulli), which is sampled for each patient.</li>
	 * <li>Any other parameter (deterministic, second-order or calculated) is the proportion of women, p, which is the parameter of a
	 * Bernoulli distribution sampled for each patient. A second-order parameter defines the uncertainty on the true proportion: it is
	 * sampled once per simulation (its expected value in the base case), and the value is then used as the parameter of the Bernoulli.</li>
	 * </ul>
	 * @param populationName The name of the population, for the messages
	 * @return The distribution of the sex if the sex parameter is first-order; null otherwise
	 * @throws MalformedOSDiModelException If a first-order parameter is not a discrete distribution, or a deterministic proportion of women
	 * is not between 0 and 1
	 */
	private DiscreteRandomVariate initializeSexVariate(String populationName) throws MalformedOSDiModelException {
		if (ParameterNatureType.FIRST_ORDER.equals(sexWrapper.getNature())) {
			if (ParameterFactory.getRandomVariateInstance(sexWrapper) instanceof DiscreteRandomVariate discrete) {
				return discrete;
			}
			throw new MalformedOSDiModelException("The sex parameter " + sexWrapper.getShortName() + " of the population " + populationName
				+ " is a first-order parameter, so it must be a discrete distribution (e.g., Bernoulli). Use a deterministic or second-order parameter to define the proportion of women.");
		}
		if (sexWrapper.getParameterNatureData() instanceof DeterministicParameterData data && (data.value() < 0.0 || data.value() > 1.0)) {
			throw new MalformedOSDiModelException("The sex parameter " + sexWrapper.getShortName() + " of the population " + populationName
				+ " must be a proportion of women between 0 and 1. Found " + data.value());
		}
		return null;
	}

	@Override
	public void createParameters() {
		for (ParameterWrapper attrWrapper : attributeValues) {
			model.addParameter(ParameterFactory.getParameterInstance(getModel(), attrWrapper, ParameterGroup.ATTRIBUTE));
		}
		// The proportion of women is a parameter of the model (see initializeSexVariate)
		if (sexVariate == null) {
			model.addParameter(ParameterFactory.getParameterInstance(getModel(), sexWrapper, ParameterGroup.ATTRIBUTE));
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

	/**
	 * Returns the distribution of the sex of the patients (0 for men, 1 for women). It is called once per simulation (see
	 * {@link StdPopulation#getSex(Patient)}), so a second-order proportion of women is sampled once per simulation and then used as the
	 * parameter of a Bernoulli distribution, which is sampled for each patient (see {@link #initializeSexVariate(String)}).
	 */
	@Override
	protected DiscreteRandomVariate getSexVariate(Patient pat) {
		if (sexVariate != null) {
			return sexVariate;
		}
		return RandomVariateFactory.getDiscreteRandomVariateInstance("BernoulliVariate", getCommonRandomNumber(), getModel().getParameterValue(sexWrapper.getShortName(), pat));
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