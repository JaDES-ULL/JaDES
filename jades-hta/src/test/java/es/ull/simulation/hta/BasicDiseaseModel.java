package es.ull.simulation.hta;

import es.ull.simulation.hta.interventions.DoNothingIntervention;
import es.ull.simulation.hta.progression.Disease;

public class BasicDiseaseModel extends HTAModel {
    private final BasicDiseaseExperiment.TESTS example;

    public BasicDiseaseModel(BasicDiseaseExperiment experiment, BasicDiseaseExperiment.TESTS example) throws MalformedSimulationModelException {
        super(experiment);
        this.example = example;
        final Disease disease = new BasicDisease(this);
        new BasicPopulation(this, disease);
		new DoNothingIntervention(this);
    }

    /**
     * @return the example
     */
    public BasicDiseaseExperiment.TESTS getExample() {
        return example;
    }
}