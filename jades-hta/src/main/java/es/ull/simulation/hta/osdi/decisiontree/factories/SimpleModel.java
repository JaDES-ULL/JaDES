package es.ull.simulation.hta.osdi.decisiontree.factories;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import org.semanticweb.owlapi.model.IRI;

import es.ull.simulation.hta.MalformedSimulationModelException;
import es.ull.simulation.hta.osdi.OSDiLogger;
import es.ull.simulation.hta.osdi.decisiontree.BaseModel;
import es.ull.simulation.hta.osdi.decisiontree.BranchDestinationNode;
import es.ull.simulation.hta.osdi.decisiontree.ChoiceNode;
import es.ull.simulation.hta.osdi.decisiontree.SingleDiseaseAndPopulationModel;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.DiseaseWrapper;
import es.ull.simulation.hta.osdi.ontology.EpidemiologicCharacterizationType;
import es.ull.simulation.hta.osdi.ontology.ExperimentWrapper;
import es.ull.simulation.hta.osdi.ontology.InterventionWrapper;
import es.ull.simulation.hta.osdi.ontology.ModelWrapper;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper.DeterministicParameterData;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper.SpecificInformationForUtility;
import es.ull.simulation.hta.osdi.ontology.PopulationWrapper;

/**
 * A decision tree model for a scenario that must fulfill the following criteria:
 * <ul>
 * <li> Two therapeutic interventions, i.e., interventions that do not involve the detection of a disease. One of them should be the 
 * assessed intervention and the other one, the comparator. </li>
 * <li> A population with a 100% prevalence of a specific disease. If a different prevalence or incidence is defined, it will be ignored.</li>
 * <li> One disease. </li>
 * </ul>
 * The tree comprises a decision branch per intervention. The payoffs belonging to each branch incorporate the effects of the corresponding intervention.
 * The disease is represented in every branch, but using the effects of the intervention to modify its impact.
 * 
 */
public class SimpleModel extends BaseModel implements SingleDiseaseAndPopulationModel {
    private final static OSDiLogger log = OSDiLogger.getLogger(SimpleModel.class);
    /**
     * The wrapper of the disease individual in this model.
     */
    private final DiseaseSubTreeGenerator diseaseGenerator;
    /**
     * The wrapper of the population individual in this model. Tree models are supposed to use one population individual.
     */
    private final PopulationWrapper populationWrapper;
    /**
     * The IRI of the assessed intervention individual in this model.
     */
    private final InterventionGenerator assessedIntervention;
    /**
     * The IRI of the comparator intervention individual in this model.
     */
    private final InterventionGenerator comparatorIntervention;
    /**
     * The life expectancy parameter if the population of the model. If not defined, a synthetic parameter will be created for it.
     */
    private final ParameterWrapper lifeExpectancyParameter;

    /**
     * Constructor to initialize the SimpleDecisionTreeModel with the specified ontology wrapper.
     * @param wrap The ontology wrapper to use for initialization.
     * @throws MalformedOSDiModelException 
     * @throws MalformedSimulationModelException
     * @throws UnsupportedOSDiFeatureException
     */
    SimpleModel(ExperimentWrapper experiment) throws MalformedOSDiModelException, MalformedSimulationModelException, UnsupportedOSDiFeatureException {
        super(experiment);
        final ModelWrapper modelWrapper = experiment.getModelWrapper();
        
        populationWrapper = modelWrapper.getPopulationIndividuals().iterator().next();
        if (modelWrapper.getPopulationIndividuals().size() > 1)
            log.warn("This type of model requires exactly one population. Found " + modelWrapper.getPopulationIndividuals().size() + ". Using " + populationWrapper.getShortName() + " as the population for the model and ignoring the rest.");
        Optional<ParameterWrapper> lifeExpectancyParamOpt = populationWrapper.getLifeExpectancy();
        if (lifeExpectancyParamOpt.isEmpty()) {
            log.warn("No life expectancy parameter defined for the population of the model. A synthetic parameter will be created for it.");
            this.lifeExpectancyParameter = new ParameterWrapper.SyntheticBuilder(modelWrapper, "FakePopulationAge", new DeterministicParameterData(100)).build();
            modelWrapper.registerSyntheticModelItem(lifeExpectancyParameter);
        }
        else {
            this.lifeExpectancyParameter = lifeExpectancyParamOpt.get();
        }

        final DiseaseWrapper diseaseWrapper = modelWrapper.getDiseaseIndividuals().iterator().next();
        if (modelWrapper.getDiseaseIndividuals().size() > 1)
            log.warn("This type of model requires exactly one disease. Found " + modelWrapper.getDiseaseIndividuals().size() + ". Using " + diseaseWrapper.getShortName() + " as the disease for the model and ignoring the rest.");
        diseaseGenerator = new DiseaseSubTreeGenerator(this, diseaseWrapper);

        final Set<InterventionWrapper> interventions = new TreeSet<>(modelWrapper.getInterventionIndividuals());
        final Set<InterventionWrapper> assessedInterventions = new TreeSet<>(modelWrapper.getAssessedInterventionIndividuals());
        if (interventions.size() != 2) {
            log.warn("This type of model requires exactly two interventions: one assessed intervention and one comparator intervention. Found " + interventions.size() + ".");
        }
        // Make the intersection of the two sets to find the assessed intervention. If there is no intersection, or more than one, we will just use the first intervention as the assessed intervention.
        assessedInterventions.retainAll(interventions);
        if (assessedInterventions.size() > 1) {
            log.warn("This type of model requires exactly one assessed intervention. Found " + assessedInterventions.size() + ".");
        }
        assessedIntervention = InterventionFactory.getInterventionBuilder(this, assessedInterventions.iterator().next());
        // The comparator intervention is the one that is not the assessed intervention. If there is no assessed intervention, we will just use the first intervention as the comparator.
        interventions.removeAll(assessedInterventions);
        if (interventions.size() > 1) {
            log.warn("This type of model requires exactly one comparator intervention. Found " + interventions.size() + ".");
        }
        comparatorIntervention = InterventionFactory.getInterventionBuilder(this, interventions.iterator().next());
    }

    public static AbstractModelFactory getFactory() {
        return new SimpleModelFactory();
    }

    @Override
    public ChoiceNode createTree()
            throws MalformedOSDiModelException, MalformedSimulationModelException, UnsupportedOSDiFeatureException {
        ChoiceNode rootNode = new ChoiceNode(this);
        BranchDestinationNode assessedInterventionPart = assessedIntervention.generate(assessedIntervention.getInterventionWrapper().getShortName());
        BranchDestinationNode comparatorInterventionPart = comparatorIntervention.generate(comparatorIntervention.getInterventionWrapper().getShortName());
        rootNode.link(assessedInterventionPart);
        rootNode.link(comparatorInterventionPart);
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
        interventions.add(assessedIntervention.getInterventionWrapper());
        interventions.add(comparatorIntervention.getInterventionWrapper());
        return interventions;
    }

    @Override
    public InterventionWrapper getIntervention(IRI interventionIRI) {
        if (assessedIntervention.getInterventionWrapper().getIndividualIRI().equals(interventionIRI)) {
            return assessedIntervention.getInterventionWrapper();
        }
        else if (comparatorIntervention.getInterventionWrapper().getIndividualIRI().equals(interventionIRI)) {
            return comparatorIntervention.getInterventionWrapper();
        }
        return null;
    }   

    public InterventionWrapper getComparatorIntervention() {
        return comparatorIntervention.getInterventionWrapper();
    }

    @Override
    public ParameterWrapper getLifeExpectancy() throws MalformedSimulationModelException, MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        return this.lifeExpectancyParameter;
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
