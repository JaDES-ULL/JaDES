package es.ull.simulation.hta.osdi.decisiontree.factories;

import java.util.HashSet;
import java.util.Set;

import es.ull.simulation.hta.MalformedSimulationModelException;
import es.ull.simulation.hta.osdi.decisiontree.Model;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.ExperimentWrapper;
import es.ull.simulation.hta.osdi.ontology.InterventionWrapper;
import es.ull.simulation.hta.osdi.ontology.ModelWrapper;

public class SimpleModelFactory implements AbstractModelFactory {
    public SimpleModelFactory() {
        // Default constructor
    }
    
    @Override
    public FactorySuitabilityScore getSuitability(ExperimentWrapper experiment) throws MalformedSimulationModelException {
        ModelWrapper modelWrapper = experiment.getModelWrapper();
        FactorySuitabilityScore score = new FactorySuitabilityScore();
        // Initially assumed to be perfectly suited
        if (modelWrapper.getDiseaseIndividuals().size() > 1) {
            score.addSeverePenalty("Multiple disease instances found");
        }
        if (modelWrapper.getPopulationIndividuals().size() > 1) {
            score.addSeverePenalty("Multiple population instances found");
        }
        checkInterventions(modelWrapper, score);
        return score;
    }

    private void checkInterventions(ModelWrapper modelWrapper, FactorySuitabilityScore score) throws MalformedSimulationModelException {
        if (modelWrapper.getInterventionIndividuals().size() != 2) {
            score.addSeverePenalty("Found more or less than two intervention instances");
        }
        final Set<InterventionWrapper> assessedInterventions = new HashSet<>(modelWrapper.getAssessedInterventionIndividuals());
        if (assessedInterventions.isEmpty()) {
            score.addSeverePenalty("No intervention is marked as assessed intervention");
        } else if (assessedInterventions.size() > 1) {
            score.addSeverePenalty("More than one intervention is marked as assessed intervention. Only one expected.");
        }
    }

    @Override
    public Model createModelGenerator(ExperimentWrapper experiment) throws MalformedSimulationModelException, MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        return new SimpleModel(experiment);
    }

}
