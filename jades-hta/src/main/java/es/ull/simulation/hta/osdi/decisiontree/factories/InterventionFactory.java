package es.ull.simulation.hta.osdi.decisiontree.factories;

import es.ull.simulation.hta.MalformedSimulationModelException;
import es.ull.simulation.hta.osdi.decisiontree.Model;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.InterventionWrapper;

public class InterventionFactory {
    private InterventionFactory() {
        // Prevent instantiation
    }

    /**
     * Creates an InterventionBuilder for the given intervention IRI.
     * TODO: Extend this method to support different types of InterventionBuilders based on the intervention type.
     * @param parentTreeModel The parent tree model associated with this factory.
     * @param interventionWrapper The wrapper for the intervention.
     * @return An InterventionBuilder instance.
     * @throws MalformedOSDiModelException
     * @throws MalformedSimulationModelException
     * @throws UnsupportedOSDiFeatureException
     */
    public static InterventionGenerator getInterventionBuilder(Model parentTreeModel, InterventionWrapper interventionWrapper) throws MalformedOSDiModelException, MalformedSimulationModelException, UnsupportedOSDiFeatureException {
        return new SimpleInterventionGenerator(parentTreeModel, interventionWrapper);
    }
}
