package es.ull.simulation.hta.expressionEvaluators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.ull.simulation.hta.BasicPopulation;
import es.ull.simulation.hta.HTAArguments;
import es.ull.simulation.hta.HTAExperiment;
import es.ull.simulation.hta.HTAModel;
import es.ull.simulation.hta.MalformedSimulationModelException;
import es.ull.simulation.hta.Patient;
import es.ull.simulation.hta.config.BaseHTAExperimentConfigProvider;
import es.ull.simulation.hta.config.IHTAOutputConfigProvider;
import es.ull.simulation.hta.info.PatientInfo;
import es.ull.simulation.hta.interventions.DoNothingIntervention;
import es.ull.simulation.hta.output.OutputItem;
import es.ull.simulation.hta.params.ParameterGroup;
import es.ull.simulation.hta.params.StandardParameter;
import es.ull.simulation.hta.progression.Disease;
import es.ull.simulation.hta.progression.DiseaseProgression;
import es.ull.simulation.hta.progression.DiseaseProgressionPathway;
import es.ull.simulation.hta.progression.calculator.ParameterBasedTimeToEventCalculator;
import es.ull.simulation.info.IPieceOfInformation;
import es.ull.simulation.info.SimulationStartStopInfo;
import es.ull.simulation.inforeceiver.BasicListener;
import es.ull.simulation.model.TimeUnit;

public class TestCalculatedParameters {  
    public enum TESTS {
        JAVALUATOR,
        JEXL
    }

    @BeforeEach
    public void setUp() {
        es.ull.simulation.hta.params.Parameter.resetAll();
    }

    @Test
    @DisplayName("Test if utilities stored as disutilities are computed fine in a disease")
    public void testJavaluator() throws MalformedSimulationModelException, IOException {
        final HTAExperiment exp = new BasicDiseaseExperiment(new TestArgumentsProvider(TESTS.JAVALUATOR));
        exp.initializeModel();
        exp.run();
    }

    @Test
    @DisplayName("Test if utilities stored as utilities are computed fine in a disease")
    public void testJEXL() throws MalformedSimulationModelException, IOException {
        final HTAExperiment exp = new BasicDiseaseExperiment(new TestArgumentsProvider(TESTS.JEXL));
        exp.initializeModel();
        exp.run();
    }


    public static class BasicDiseaseExperiment extends HTAExperiment {
        private final TESTS example;
        public BasicDiseaseExperiment(TestArgumentsProvider arguments) throws MalformedSimulationModelException, IOException {
            super(arguments, new TestOutputProvider());
            this.example = arguments.example;
        }

        @Override
        public ArrayList<BasicListener> createAdditionalListeners(int id) {
            ArrayList<BasicListener> listeners = new ArrayList<BasicListener>();
            listeners.add(new ExpressionChecker(this));
            return listeners;
        }
        
        @Override
        public HTAModel createModel() throws MalformedSimulationModelException {
            return new BasicDiseaseModel(this, example);
        }

        /**
         * @return the example
         */
        public TESTS getExample() {
            return example;
        }
    }
    public static class BasicDiseaseModel extends HTAModel {
        private final TESTS example;

        public BasicDiseaseModel(BasicDiseaseExperiment experiment, TESTS example) throws MalformedSimulationModelException {
            super(experiment);
            this.example = example;
            final Disease disease = new BasicDisease(this);
            new BasicPopulation(this, disease);
			new DoNothingIntervention(this);
        }

        /**
         * @return the example
         */
        public TESTS getExample() {
            return example;
        }
    }

    public static class BasicDisease extends Disease {
        public static final int YEARS_TO_MANIFESTATION1 = 50;
        public static final double DISUTILITY = 0.1;
        /** An ad hoc expression used to reduce the time to suffer certain problem according to some attributes of the patient. 
         * The expression divides the years to manifestation by a formula: (LDL * 2 + HDL - ¿female? * 10) */
        private final String expression = "" + YEARS_TO_MANIFESTATION1 + " / (" + BasicPopulation.ATTRIBUTE_LDL + " * 2 + " + BasicPopulation.ATTRIBUTE_HDL + " - SEX * 10)";

        /**
         * @param model Repository with common information about the disease 
         */
        public BasicDisease(BasicDiseaseModel model) {
            super(model, "D0", "Test disease 0");
            final DiseaseProgression manif1 = new TestChronicManifestation(model, this, "Cholesterol", 100, 0.2);
            new DiseaseProgressionPathway(model, "PATHWAY1",  "Pathway to cholesterol", manif1,
                new ParameterBasedTimeToEventCalculator("tteChol", TimeUnit.YEAR));
        }

        @Override
        public void createParameters() {
            addUsedParameter(StandardParameter.ANNUAL_DISUTILITY, "", "Test", DISUTILITY);
            if (TESTS.JAVALUATOR.equals(((BasicDiseaseModel) model).getExample()))
                model.addParameter(new JavaluatorParameter(model, "tteChol", "Time to cholesterol", 
                    "Assumption", 2024, ParameterGroup.RISK, expression));
            else
                model.addParameter(new ExpressionLanguageParameter(model, "tteChol", "Time to cholesterol", 
                "Assumption", 2024, ParameterGroup.RISK, expression));
        }
    }

    public static class TestChronicManifestation extends DiseaseProgression {
        private final double annualCost;
        private final double disutility;

        /**
         * @param model
         * @param disease
         */
        public TestChronicManifestation(BasicDiseaseModel model, Disease disease, String name, double annualCost, double disutility) {
            super(model, name, "Chronic manifestation of test disease", disease, Type.CHRONIC_MANIFESTATION);
            this.annualCost = annualCost;
            this.disutility = disutility;
        }

        @Override
        public void createParameters() {
            addUsedParameter(StandardParameter.ANNUAL_COST, "", "Test", model.getStudyYear(), annualCost);
            addUsedParameter(StandardParameter.ANNUAL_DISUTILITY, "", "Test", disutility);
        }

    }

    public static class ExpressionChecker extends BasicListener {
        final private BasicDiseaseExperiment exp;
        final private long [] expectedTimesToEvent;
        public ExpressionChecker(BasicDiseaseExperiment exp) {
            super("Disease 0 listener");
            this.exp = exp;
            this.expectedTimesToEvent = new long[exp.getNPatients()];
    		addTargetInformation(PatientInfo.class);
            addTargetInformation(SimulationStartStopInfo.class);
        }

        @Override
        public void infoEmited(IPieceOfInformation info) {
            if (info instanceof PatientInfo) {
                final PatientInfo pInfo = (PatientInfo) info;
                final Patient pat = pInfo.getPatient();
                if (pInfo.getType().equals(PatientInfo.Type.START)) {
                    final int sex = pat.getSex();
                    final double ldl = exp.getModel().getParameterValue(BasicPopulation.ATTRIBUTE_LDL, pat);
                    final double hdl = exp.getModel().getParameterValue(BasicPopulation.ATTRIBUTE_HDL, pat);
                    double timeTo = BasicDisease.YEARS_TO_MANIFESTATION1 / (ldl * 2 + hdl - sex * 10);
                    expectedTimesToEvent[pat.getIdentifier()] = pat.getTs() + pat.getSimulation().getTimeUnit().convert(timeTo, TimeUnit.YEAR);
                }
                else if (pInfo.getType().equals(PatientInfo.Type.START_MANIF)) {
                    assertEquals(expectedTimesToEvent[pat.getIdentifier()], pInfo.getTs(), "Event time does not match");
                }
                else if (pInfo.getType().equals(PatientInfo.Type.DEATH)) {
                    assertTrue(pat.getState().contains(exp.getModel().getDiseaseProgression("Cholesterol")), "The event never happened. Probably the time to event was not computed properly.");
                }
            }
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

    
}