package es.ull.simulation.hta.osdi.decisiontree.factories;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import es.ull.simulation.hta.MalformedSimulationModelException;
import es.ull.simulation.hta.osdi.decisiontree.Model;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.ExperimentWrapper;
import es.ull.simulation.hta.osdi.ontology.ModelType;
import es.ull.simulation.hta.osdi.ontology.OSDiClass;

/**
 * A factory class for creating decision tree models. It analyzes the provided OSDiWrapper
 * and creates the appropriate decision tree model based on the interventions found.
 *
 * Currently, only supports one disease, one population and two interventions (one of them must be the assessed intervention and the other must be the comparator intervention).
 * If more than two interventions are found, only the first ones identified as assessed and comparator will be used. Prints warnings if additional interventions, populations, or diseases are found.
 */
public class CentralModelFactory {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(CentralModelFactory.class);
    private static final List<AbstractModelFactory> factories = new ArrayList<>();

    public static void register(AbstractModelFactory factory) {
        factories.add(factory);
    }

    public static void checkSuitability(ExperimentWrapper experiment) throws MalformedSimulationModelException {
        for (AbstractModelFactory factory : factories) {
            FactorySuitabilityScore score = factory.getSuitability(experiment);
            log.info("Suitability score for " + factory.getClass().getSimpleName() + ": " + score);
        }
    }

    public static Model create(ExperimentWrapper experiment) throws MalformedOSDiModelException, MalformedSimulationModelException, UnsupportedOSDiFeatureException {
        // Checks if the provided model is a valid decision tree model
        if (!ModelType.DECISION_TREE.equals(experiment.getModelType()))
            throw new MalformedOSDiModelException("According to the ontology, the provided model is not a valid decision tree model. Decision trees must be of type " + OSDiClass.DECISION_TREE_MODEL.getShortName());
        // Factories with score 0 are not considered
        int maxScore = 0;
        AbstractModelFactory chosenFactory = null;
        for (AbstractModelFactory factory : factories) {
            final FactorySuitabilityScore score = factory.getSuitability(experiment);
            if (score.getScore() > maxScore) {
                maxScore = score.getScore();
                chosenFactory = factory;
            }
        }
        if (chosenFactory == null) {
            throw new UnsupportedOSDiFeatureException("Could not find a suitable decision tree factory.");
        }
        return chosenFactory.createModelGenerator(experiment);
    }
}
