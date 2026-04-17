package es.ull.simulation.hta.osdi.decisiontree.nbsdecisiontree;

import es.ull.simulation.hta.MalformedSimulationModelException;
import es.ull.simulation.hta.osdi.decisiontree.Model;
import es.ull.simulation.hta.osdi.decisiontree.factories.AbstractModelFactory;
import es.ull.simulation.hta.osdi.decisiontree.factories.FactorySuitabilityScore;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.DetectionInterventionWrapper;
import es.ull.simulation.hta.osdi.ontology.ExperimentWrapper;
import es.ull.simulation.hta.osdi.ontology.InterventionWrapper;
import es.ull.simulation.hta.osdi.ontology.ModelWrapper;
import es.ull.simulation.hta.osdi.ontology.OSDiClass;
import es.ull.simulation.hta.osdi.ontology.ParameterNatureType;
import es.ull.simulation.hta.osdi.ontology.PopulationWrapper;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper.DeterministicParameterData;

public class NBSModelFactory implements AbstractModelFactory {

    public NBSModelFactory() {
        // Default constructor
    }

    @Override
    public FactorySuitabilityScore getSuitability(ExperimentWrapper experiment) throws MalformedSimulationModelException {
        FactorySuitabilityScore score = new FactorySuitabilityScore();
        // Initially assumed to be perfectly suited
        if (experiment.getModelWrapper().getDiseaseIndividuals().size() > 1) {
            score.addMildPenalty("Multiple disease instances found");
        }
        if (experiment.getModelWrapper().getPopulationIndividuals().size() > 1) {
            score.addMildPenalty("Multiple population instances found");
        }
        checkAge(experiment.getModelWrapper(), score);
        checkInterventions(experiment.getModelWrapper(), score);
        return score;
    }

    private void checkAge(ModelWrapper modelWrapper, FactorySuitabilityScore score) {
        PopulationWrapper population = modelWrapper.getPopulationIndividuals().iterator().next();
        if (population.getAgeParameter().isEmpty()) {
            score.addMildPenalty("Population instance is missing age information");
        }
        else {
            if (!ParameterNatureType.DETERMINISTIC.equals(population.getAgeParameter().get().getNature())) {
                score.addModeratePenalty("Population instance should define a deterministic age parameter with value 0");
            }
            else {
                double age = ((DeterministicParameterData) population.getAgeParameter().get().getParameterNatureData()).value();
                if (age != 0) {
                    score.addModeratePenalty("Population instance should define a deterministic age parameter with value 0");
                }
            }
        }
    }

    private void checkInterventions(ModelWrapper modelWrapper, FactorySuitabilityScore score) throws MalformedSimulationModelException {
        if (modelWrapper.getInterventionIndividuals().size() > 2) {
            score.addMildPenalty("More than two intervention instances found");
        }
        boolean foundScreeningIntervention = false;
        boolean foundNoScreeningIntervention = false;
        boolean foundNoDetectionInterventions = false;
        for (InterventionWrapper intervention : modelWrapper.getInterventionIndividuals()) {
            if (!(intervention instanceof DetectionInterventionWrapper)) {
                foundNoDetectionInterventions = true;
                break;
            }
            DetectionInterventionWrapper detectionIntervention = (DetectionInterventionWrapper) intervention;
            OSDiClass interventionClass = NBSModel.getNBSModelInterventionClass(modelWrapper.getOSDiWrapper(), detectionIntervention);
            switch (interventionClass) {
                case SCREENING_INTERVENTION:
                    foundScreeningIntervention = true;
                    break;
                // Assume that any detection subclass is a no screening intervention
                case DIAGNOSIS_INTERVENTION:
                case DETECTION_INTERVENTION:
                    foundNoScreeningIntervention = true;
                    break;                    
                default:
                    foundNoDetectionInterventions = true;
            }
        }
        if (foundNoDetectionInterventions) {
            score.addModeratePenalty("Found interventions that are not of type detection");
        }
        if (!foundScreeningIntervention || !foundNoScreeningIntervention) {
            score.addSeverePenalty("Could not find both screening and no screening interventions");
        }
    }
    @Override
    public Model createModelGenerator(ExperimentWrapper experiment) throws MalformedSimulationModelException, MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        return new NBSModel(experiment);
    }

}
