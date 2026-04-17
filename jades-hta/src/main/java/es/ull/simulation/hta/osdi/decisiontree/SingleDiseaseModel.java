package es.ull.simulation.hta.osdi.decisiontree;

import es.ull.simulation.hta.osdi.decisiontree.factories.DiseaseSubTreeGenerator;
import es.ull.simulation.hta.osdi.ontology.DiseaseWrapper;

public interface SingleDiseaseModel extends Model {
    /**
     * Returns the part of the decision tree that represents the disease component of the model.
     * @return The DiseaseTreePart instance representing the disease component.
     */
    public DiseaseSubTreeGenerator getDiseaseGenerator();
    /**
     * Returns the wrapper of the disease individual in this model. 
     * @return The wrapper of the disease individual in this model.
     */
    public default DiseaseWrapper getDiseaseWrapper() {
        return getDiseaseGenerator().getDiseaseWrapper();
    }
}
