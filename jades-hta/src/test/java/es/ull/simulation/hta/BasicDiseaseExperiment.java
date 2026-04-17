package es.ull.simulation.hta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import es.ull.simulation.hta.config.BaseHTAExperimentConfigProvider;
import es.ull.simulation.hta.config.IHTAOutputConfigProvider;
import es.ull.simulation.hta.output.OutputItem;
import es.ull.simulation.inforeceiver.BasicListener;

public class BasicDiseaseExperiment extends HTAExperiment {
    public enum TESTS {
        ONE_CHRONIC(8100.0, 57.6, 82.0),
        TWO_CHRONIC(88100.0, 17.6, 82.0),
        TWO_CHRONIC_EXCLUSIVE(80100.0, 33.6, 82.0),
        ACUTE(8000.0, 72.2, 82.0),
        ACUTE_DEATH(1000.0, 8.8, 10.0),
        DEATH_BY_CHRONIC_LER(7100.0, 50.6, 72.0),
        DEATH_BY_CHRONIC_IMR(4050.137, 29.251, 41.50137);

        private final double expectedQALY;
        private final double expectedCost;
        private final double expectedLY;

        private TESTS(double expectedCost, double expectedQALY, double expectedLY) {
            this.expectedCost = expectedCost;
            this.expectedQALY = expectedQALY;
            this.expectedLY = expectedLY;
        }

        public double getExpectedQALY() {
            return expectedQALY;
        }

        public double getExpectedCost() {
            return expectedCost;
        }

        public double getExpectedLY() {
            return expectedLY;
        }

        
    }

    public static class TestOutputProvider implements IHTAOutputConfigProvider {
        public TestOutputProvider() {
        }

        @Override
        public List<OutputItem<?>> getBaseCaseOutputItems(HTAExperiment exp) {
            return new ArrayList<>();
        }

        @Override
        public List<OutputItem<?>> getPSAOutputItems(HTAExperiment exp) {
            return new ArrayList<>();
        }
    }

   public static class TestArgumentsProvider extends BaseHTAExperimentConfigProvider {
        private final TESTS example;
        private final List<Integer> debugPatients;
        public TestArgumentsProvider(TESTS example) throws IOException {
            super(new HTAArguments());
            debugPatients = new ArrayList<>();
            debugPatients.add(0);
            this.example = example;
        }

        public TESTS getExample() {
            return example;
        }

        @Override
        public List<Integer> getDebugPatients() {
            return debugPatients;
        }
    }

    private final BasicDiseaseExperiment.TESTS example;

    public BasicDiseaseExperiment(BasicDiseaseExperiment.TestArgumentsProvider arguments) throws MalformedSimulationModelException, IOException {
        super(arguments, new TestOutputProvider());
        this.example = arguments.getExample();
    }

    @Override
    public ArrayList<BasicListener> createAdditionalListeners(int id) {
        ArrayList<BasicListener> listeners = new ArrayList<BasicListener>();
        ArrayList<ExactOrderPatientEventChecker.PatientEvent> expectedEvents = TestEventOrder.getExpectedEventsForExample(example);
        listeners.add(new ExactOrderPatientEventChecker(expectedEvents));
        listeners.add(new OutcomesChecker(example));
        return listeners;
    }
    
    @Override
    public HTAModel createModel() throws MalformedSimulationModelException {
        return new BasicDiseaseModel(this, example);
    }

    /**
     * @return the example
     */
    public BasicDiseaseExperiment.TESTS getExample() {
        return example;
    }
}