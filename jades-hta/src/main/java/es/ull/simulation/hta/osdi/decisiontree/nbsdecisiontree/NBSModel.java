package es.ull.simulation.hta.osdi.decisiontree.nbsdecisiontree;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import org.semanticweb.owlapi.model.IRI;

import es.ull.simulation.hta.MalformedSimulationModelException;
import es.ull.simulation.hta.osdi.ontology.OSDiWrapper;
import es.ull.simulation.hta.osdi.decisiontree.ChoiceNode;
import es.ull.simulation.hta.osdi.OSDiLogger;
import es.ull.simulation.hta.osdi.decisiontree.BaseModel;
import es.ull.simulation.hta.osdi.decisiontree.SingleDiseaseAndPopulationModel;
import es.ull.simulation.hta.osdi.decisiontree.factories.AbstractModelFactory;
import es.ull.simulation.hta.osdi.decisiontree.factories.DiseaseSubTreeGenerator;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.DetectionInterventionWrapper;
import es.ull.simulation.hta.osdi.ontology.DiseaseWrapper;
import es.ull.simulation.hta.osdi.ontology.EpidemiologicCharacterizationType;
import es.ull.simulation.hta.osdi.ontology.ExperimentWrapper;
import es.ull.simulation.hta.osdi.ontology.InterventionType;
import es.ull.simulation.hta.osdi.ontology.InterventionWrapper;
import es.ull.simulation.hta.osdi.ontology.ModelWrapper;
import es.ull.simulation.hta.osdi.ontology.OSDiClass;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper.DeterministicParameterData;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper.SpecificInformationForUtility;
import es.ull.simulation.hta.osdi.ontology.PopulationWrapper;
import es.ull.simulation.hta.osdi.ontology.ResourceUsageSKOSCategory;
import es.ull.simulation.ontology.OWLOntologyWrapper.InstanceCheckMode;

/**
 * A wrapper class for an decision tree model intended to represent a newborn screening (NBS) intervention compared to a clinical diagnosis intervention.
 * Currently, it supports exactly two interventions: one for screening and one for no screening, i.e., clinical diagnosis.
 */
public class NBSModel extends BaseModel implements SingleDiseaseAndPopulationModel {
    private final static OSDiLogger log = OSDiLogger.getLogger(NBSModel.class);
    /**
     * The birth prevalence parameter defined in the population of this model.
     * It is used to calculate the number of newborns affected by the disease in the population.
     */
    private ParameterWrapper birthPrevalence;
    /**
     * The parts of the decision tree that represent the different components of the NBS intervention.
     */
    private final NBSScreeningInterventionBuilder screeningTreePart;
    /**
     * The part of the decision tree that represents the no screening intervention, i.e., clinical diagnosis.
     */
    private final NBSNoScreeningInterventionBuilder noScreeningTreePart;
    /**
     * The diagnosis costs associated with the disease.
     */
    private final Set<ParameterWrapper> diagnosisCosts;
    /**
     * The wrapper of the disease instance in this model.
     */
    private final DiseaseSubTreeGenerator diseaseGenerator;
    /**
     * The wrapper of the population instance in this model. Tree models are supposed to use one population instance.
     */
    private final PopulationWrapper populationWrapper;
    /**
     * The wrapper of the disease instance in this model.
     */
    private final DiseaseWrapper diseaseWrapper;
    /**
     * The life expectancy parameter if the population of the model. If not defined, a synthetic parameter will be created for it.
     */
    private final ParameterWrapper lifeExpectancyParameter;

    /**
     * Constructor for a decision tree that represents a NBS intervention. Should be invoked from the factory and only after checking the suitability.
     * @param experiment The experiment wrapper containing the ontology and experiment details.
     * @throws MalformedSimulationModelException If the model does not contain exactly two interventions or if they are not correctly classified.
     * @throws MalformedOSDiModelException If there are issues with the OSDi model, such as missing parameters or incorrect data types.
     */
    NBSModel(ExperimentWrapper experiment) throws MalformedSimulationModelException, MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        super(experiment);

        final ModelWrapper modelWrapper = getModelWrapper();
        populationWrapper = modelWrapper.getPopulationIndividuals().iterator().next();
		if (modelWrapper.getPopulationIndividuals().size() > 1)
            log.warn("This type of model requires exactly one population. Found " + modelWrapper.getPopulationIndividuals().size() + ". Using " + populationWrapper.getIndividualIRI() + " as the population for the model and ignoring the rest.");
        Optional<ParameterWrapper> lifeExpectancyParamOpt = populationWrapper.getLifeExpectancy();
        if (lifeExpectancyParamOpt.isEmpty()) {
            log.warn("No life expectancy parameter defined for the population of the model. A synthetic parameter will be created for it.");
            this.lifeExpectancyParameter = new ParameterWrapper.SyntheticBuilder(modelWrapper, "FakePopulationAge", new DeterministicParameterData(100)).build();
            modelWrapper.registerSyntheticModelItem(lifeExpectancyParameter);
        }
        else {
            this.lifeExpectancyParameter = lifeExpectancyParamOpt.get();
        }
        
        diseaseWrapper = modelWrapper.getDiseaseIndividuals().iterator().next();
        diseaseGenerator = new DiseaseSubTreeGenerator(this, diseaseWrapper);
        if (modelWrapper.getDiseaseIndividuals().size() > 1)
            log.warn("This type of model requires exactly one disease. Found " + modelWrapper.getDiseaseIndividuals().size() + ". Using " + diseaseWrapper.getShortName() + " as the disease for the model and ignoring the rest.");

        this.diagnosisCosts = findDiagnosisCosts();
        final ArrayList<InterventionWrapper> interventions = new ArrayList<>(modelWrapper.getInterventionIndividuals());
        NBSScreeningInterventionBuilder screeningTreePart = null;
        NBSNoScreeningInterventionBuilder noScreeningTreePart = null;
        for (InterventionWrapper interventionWrapper : interventions) {
            if (!(interventionWrapper instanceof DetectionInterventionWrapper)) {
                throw new MalformedOSDiModelException("The wrapper for the intervention " + interventionWrapper.getIndividualIRI() + " is not a DetectionInterventionWrapper, which is required for the NBS model.");
            }
            DetectionInterventionWrapper detectionInterventionWrapper = (DetectionInterventionWrapper) interventionWrapper;
            if (InterventionType.DIAGNOSIS.equals(detectionInterventionWrapper.getInterventionType())) {
                if (noScreeningTreePart == null) {
                    noScreeningTreePart = new NBSNoScreeningInterventionBuilder(this, detectionInterventionWrapper);
                } else {
                    log.warn("There are more than one no screening intervention in the model: " + noScreeningTreePart.getInterventionWrapper().getShortName() + " and " + detectionInterventionWrapper.getShortName());
                }
            }
            else if (InterventionType.SCREENING.equals(detectionInterventionWrapper.getInterventionType())) {
                if (screeningTreePart == null) {
                    screeningTreePart = new NBSScreeningInterventionBuilder(this, detectionInterventionWrapper);
                } else {
                    log.warn("There are more than one screening intervention in the model: " + screeningTreePart.getInterventionWrapper().getShortName() + " and " + detectionInterventionWrapper.getShortName());
                }
            }
            else {
                throw new MalformedOSDiModelException("The intervention " + detectionInterventionWrapper.getShortName() + " is not a valid NBS intervention. Must be a subclass of "
                        + OSDiClass.SCREENING_INTERVENTION.getShortName() + ", "
                        + OSDiClass.DIAGNOSIS_INTERVENTION.getShortName() + ", or "
                        + OSDiClass.DETECTION_INTERVENTION.getShortName() + ".");
            }            
        }
        if (screeningTreePart == null) {
            throw new MalformedSimulationModelException("There is no \"screening\" intervention in the model.");
        }
        if (noScreeningTreePart == null) {
            throw new MalformedSimulationModelException("There is no \"no screening\" intervention in the model.");
        }
        this.screeningTreePart = screeningTreePart;
        this.noScreeningTreePart = noScreeningTreePart;
    }

    /**
     * Find the diagnosis costs associated with the disease. This method looks for a diagnosis strategy defined in the disease and returns its costs. If there is no diagnosis strategy, an empty set will be returned.        
     * TODO: Currently we assume that the disease defines a diagnosis cost. Another way of getting this cost would be using the no screening intervention and
     * even the screening intervention (in case it includes diagnosis costs as well)
     * @return A set of ParameterWrapper instances representing the diagnosis costs associated with the disease.
     * @throws UnsupportedOSDiFeatureException 
     * @throws MalformedOSDiModelException If there are issues with the OSDi model, such as missing parameters or incorrect data types.
     */
    private Set<ParameterWrapper> findDiagnosisCosts() throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        Set<ParameterWrapper> diagnosisCosts = getOSDiWrapper().filterModelItemsWithSKOSCategory(diseaseWrapper.getCosts(), ResourceUsageSKOSCategory.DIAGNOSIS);
        // TODO: Explore strategies in the disease and even in the interventions to find diagnosis costs as well.
        return diagnosisCosts;
    }

    /**
     * Returns a new instance of the factory for this model.
     * @return A new instance of NBSDecisionTreeModelFactory.
     */
    public static AbstractModelFactory getFactory() {
        return new NBSModelFactory();
    }

    @Override
    protected ChoiceNode createTree() throws MalformedOSDiModelException, MalformedSimulationModelException, UnsupportedOSDiFeatureException {
        ChoiceNode rootNode = new ChoiceNode(this);
        rootNode.link(screeningTreePart.generate(screeningTreePart.getInterventionWrapper().getShortName()));
        rootNode.link(noScreeningTreePart.generate(noScreeningTreePart.getInterventionWrapper().getShortName()));
        return rootNode;
    }
    
    @Override
    public DiseaseSubTreeGenerator getDiseaseGenerator() {
        return diseaseGenerator;
    }

    @Override
    public PopulationWrapper getPopulationWrapper() {
        return populationWrapper;
    }

    @Override
    public ArrayList<InterventionWrapper> getInterventions() {
        ArrayList<InterventionWrapper> interventions = new ArrayList<>();
        interventions.add(screeningTreePart.getInterventionWrapper());
        interventions.add(noScreeningTreePart.getInterventionWrapper());
        return interventions;
    }

    @Override
    public InterventionWrapper getIntervention(IRI interventionIRI) {
        if (screeningTreePart.getInterventionWrapper().getIndividualIRI().equals(interventionIRI)) {
            return screeningTreePart.getInterventionWrapper();
        }
        else if (noScreeningTreePart.getInterventionWrapper().getIndividualIRI().equals(interventionIRI)) {
            return noScreeningTreePart.getInterventionWrapper();
        }
        else {
            return null;
        }
    }
    
    /**
     * Returns the diagnosis costs associated with the disease.
     * @return The diagnosis costs.
     */
    public Set<ParameterWrapper> getDiagnosisCosts() {
        return diagnosisCosts;
    }
    
    /**
     * Returns the part of the decision tree that represents the screening intervention.
     * @return The ScreeningTreePart instance representing the screening intervention.
     */
    public NBSScreeningInterventionBuilder getScreeningTreePart() {
        return screeningTreePart;
    }

    /**
     * Returns the part of the decision tree that represents the no screening intervention, i.e., clinical diagnosis.
     * @return The NoScreeningTreePart instance representing the no screening intervention.
     */
    public NBSNoScreeningInterventionBuilder getNoScreeningTreePart() {
        return noScreeningTreePart;
    }

    /**
     * Checks if the given intervention IRI is a valid NBS intervention and returns the corresponding class in the ontology.
     * @param interventionWrapper The wrapper of the intervention to check.
     * @return The OSDiClasses enumeration value corresponding to the intervention class in case it is valid.
     * @throws MalformedSimulationModelException If the intervention IRI is not a valid NBS intervention.
     */
    public static OSDiClass getNBSModelInterventionClass(OSDiWrapper wrap, DetectionInterventionWrapper interventionWrapper) throws MalformedSimulationModelException {
        final IRI interventionIRI = interventionWrapper.getIndividualIRI();
		final Set<OSDiClass> superclasses = wrap.getTypes(interventionIRI, InstanceCheckMode.ASSERTED_ALL);
        if (superclasses.contains(OSDiClass.SCREENING_INTERVENTION))
            return OSDiClass.SCREENING_INTERVENTION;
        if (superclasses.contains(OSDiClass.DIAGNOSIS_INTERVENTION))
            return OSDiClass.DIAGNOSIS_INTERVENTION;
        if (superclasses.contains(OSDiClass.DETECTION_INTERVENTION))
            return OSDiClass.DETECTION_INTERVENTION;
        throw new MalformedSimulationModelException("The intervention " + interventionIRI + " is not a valid NBS intervention. Must be a subclass of "
                + OSDiClass.SCREENING_INTERVENTION.getShortName() + ", "
                + OSDiClass.DIAGNOSIS_INTERVENTION.getShortName() + ", or "
                + OSDiClass.DETECTION_INTERVENTION.getShortName() + ".");
    }

    /**
     * Returns the birth prevalence parameter defined in the population of this model. 
     * In case it was not previously defined, it will be searched in the population parameters.
     * @return The birth prevalence parameter.
     * @throws MalformedSimulationModelException
     * @throws MalformedOSDiModelException 
     * @throws UnsupportedOSDiFeatureException 
     */
    public ParameterWrapper getBirthPrevalence() throws MalformedSimulationModelException, MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        if (birthPrevalence != null) {
            return birthPrevalence;
        }
        birthPrevalence = getEpidemiologicalCharacterizationOfDisease(EpidemiologicCharacterizationType.BIRTH_PREVALENCE);
        if (birthPrevalence != null) {
            return birthPrevalence;
        } else {
            throw new MalformedSimulationModelException("The population " + getPopulationWrapper() + " does not define a birth prevalence parameter. It is required for the NBS model."); 
        }
    }

    @Override
    public ParameterWrapper getLifeExpectancy() throws MalformedSimulationModelException, MalformedOSDiModelException {
        return lifeExpectancyParameter;
    }

    @Override
    public ParameterWrapper getBaseUtility() throws MalformedSimulationModelException, MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        final Set<ParameterWrapper> utilityParams = populationWrapper.getUtilities();
        ParameterWrapper utilityParam = null;
        if (utilityParams.isEmpty()) {
            log.warn("No utility parameter defined for the population of the model. A synthetic parameter will be created for it.");
            utilityParam = new ParameterWrapper.SyntheticBuilder(getModelWrapper(), "FakePopulationUtility", new DeterministicParameterData(1)).build();
            getModelWrapper().registerSyntheticModelItem(utilityParam);
        }
        else if (utilityParams.size() > 1) {
            log.warn("More than one utility parameter defined for the population of the model. The following parameters were found: " + utilityParams + ". Only the first will be used.");
        }
        utilityParam = utilityParams.iterator().next();
        final SpecificInformationForUtility paramInfo = (SpecificInformationForUtility) utilityParam.getSpecificInformation();
        if (paramInfo.appliesOneTime()) {
            log.warn("The utility parameter defined for the population of the model is a one-time utility, but a regular annual utility is expected. The following parameter was found: " + utilityParam + ". This parameter will be used as base utility for the population, but please check if this is what you intended when defining the model.");
        }
        if (paramInfo.isDisutility()) {
            log.warn("The utility parameter defined for the population of the model is a disutility, but a regular utility is expected. The following parameter was found: " + utilityParam + ". This parameter will be used as base utility for the population, but please check if this is what you intended when defining the model.");
        }
        return utilityParam;
    }

    @Override
    public ParameterWrapper getEpidemiologicalCharacterizationOfDisease(EpidemiologicCharacterizationType characterization) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        return populationWrapper.getEpidemiologicalCharacterizationOfDisease(characterization, getDiseaseWrapper());
    }
}
