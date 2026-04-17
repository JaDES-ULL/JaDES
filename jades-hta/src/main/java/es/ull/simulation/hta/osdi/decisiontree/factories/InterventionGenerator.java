package es.ull.simulation.hta.osdi.decisiontree.factories;

import es.ull.simulation.hta.osdi.decisiontree.Model;
import es.ull.simulation.hta.osdi.ontology.InterventionWrapper;

public abstract class InterventionGenerator implements SubTreeGenerator {
    private final InterventionWrapper interventionWrapper;
    /**
     * The parent tree model associated with this factory.
     */
    private final Model parentTreeModel;

    public InterventionGenerator(Model parentTreeModel, InterventionWrapper interventionWrapper) {
        this.interventionWrapper = interventionWrapper;
        this.parentTreeModel = parentTreeModel;
    }

    @Override
    public Model getParentTreeModel() {
        return parentTreeModel;
    }

    public InterventionWrapper getInterventionWrapper() {
        return interventionWrapper;
    }
}
