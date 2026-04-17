package es.ull.simulation.hta;

import es.ull.simulation.condition.AbstractCondition;
import es.ull.simulation.hta.BasicDisease.TestChronicManifestation.TestType;
import es.ull.simulation.hta.params.StandardParameter;
import es.ull.simulation.hta.progression.Disease;
import es.ull.simulation.hta.progression.DiseaseProgression;
import es.ull.simulation.hta.progression.DiseaseProgressionPathway;
import es.ull.simulation.hta.progression.calculator.ConstantTimeToEventCalculator;
import es.ull.simulation.hta.progression.condition.PreviousDiseaseProgressionCondition;

public class BasicDisease extends Disease {
    public static final int YEARS_AMONG_ACUTE_MANIFESTATIONS = 10;
    public static final int YEARS_TO_MANIFESTATION1 = 1;
    public static final int YEARS_TO_MANIFESTATION2 = 1;
    public static final double DEF_IMR = 2;
    public static final double DEF_LER = 10;
    public static final double DISUTILITY = 0.1;

    public static class TestChronicManifestation extends DiseaseProgression {
        public enum TestType {
            IMR,
            LER,
            NONE
        }

        private final double annualCost;
        private final double disutility;
        private final TestType testType;
    
        /**
         * @param model
         * @param disease
         */
        public TestChronicManifestation(BasicDiseaseModel model, Disease disease, String name, double annualCost, double disutility, TestType testType) {
            super(model, name, "Chronic manifestation of test disease with " + testType, disease, Type.CHRONIC_MANIFESTATION);
            this.annualCost = annualCost;
            this.disutility = disutility;
            this.testType = testType;
        }
    
        @Override
        public void createParameters() {
            addUsedParameter(StandardParameter.ANNUAL_COST, "", "Test", model.getStudyYear(), annualCost);
            addUsedParameter(StandardParameter.ANNUAL_DISUTILITY, "", "Test", disutility);
            switch (testType) {
                case IMR:
                    addUsedParameter(StandardParameter.INCREASED_MORTALITY_RATE, "", "Test", DEF_IMR);
                    break;
                case LER:
                    addUsedParameter(StandardParameter.LIFE_EXPECTANCY_REDUCTION, "", "Test", DEF_LER);
                    break;
                case NONE:
                    break;
            }
        }
    
    }

    public static class TestAcuteManifestation extends DiseaseProgression {
        private final double onsetCost;
        private final double onsetDisutility;
        private final boolean leadsToDeath;
    
        /**
         * @param model
         * @param disease
         */
        public TestAcuteManifestation(BasicDiseaseModel model, Disease disease, String name, double onsetCost, double onsetDisutility, boolean leadsToDeath) {
            super(model, name, "Acute manifestation of test disease", disease, Type.ACUTE_MANIFESTATION);
            this.onsetCost = onsetCost;
            this.onsetDisutility = onsetDisutility;
            this.leadsToDeath = leadsToDeath;
        }
    
        @Override
        public void createParameters() {
            addUsedParameter(StandardParameter.ONSET_COST, "", "Test", model.getStudyYear(), onsetCost);
            addUsedParameter(StandardParameter.ONSET_DISUTILITY, "", "Test", onsetDisutility);
            if (leadsToDeath) {
                addUsedParameter(StandardParameter.DISEASE_PROGRESSION_RISK_OF_DEATH, "Death by acute manifestation", 
                    "Test", 1.0);
            }
        }
    
    }
    private DiseaseProgression acuteManif1 = null;
    private DiseaseProgression manif1 = null;
    private DiseaseProgression manif2 = null;

    /**
     * @param model Repository with common information about the disease 
     */
    public BasicDisease(BasicDiseaseModel model) {
        super(model, "D0", "Test disease 0");
        switch (model.getExample()) {
        case DEATH_BY_CHRONIC_IMR:
            manif1 = new TestChronicManifestation(model, this, "MANIF1", 100, 0.2, TestType.IMR);
            new DiseaseProgressionPathway(model, "PATHWAY1",  "Pathway to chronic manifestation 1", manif1,
            new ConstantTimeToEventCalculator(YEARS_TO_MANIFESTATION1));
            break;
        case DEATH_BY_CHRONIC_LER:
            manif1 = new TestChronicManifestation(model, this, "MANIF1", 100, 0.2, TestType.LER);
            new DiseaseProgressionPathway(model, "PATHWAY1",  "Pathway to chronic manifestation 1", manif1,
            new ConstantTimeToEventCalculator(YEARS_TO_MANIFESTATION1));
            break;
        case ACUTE:
        case ACUTE_DEATH:
            acuteManif1 = new BasicDisease.TestAcuteManifestation(model, this, "ACUTE_MANIF1", 1000, 0.2, BasicDiseaseExperiment.TESTS.ACUTE_DEATH.equals(model.getExample()));
            new DiseaseProgressionPathway(model, "PATH_ACUTE1", "Pathway to acute manifestation 1", acuteManif1,
                new ConstantTimeToEventCalculator(YEARS_AMONG_ACUTE_MANIFESTATIONS));
            break;
        case ONE_CHRONIC:
        case TWO_CHRONIC:
        case TWO_CHRONIC_EXCLUSIVE:
            manif1 = new BasicDisease.TestChronicManifestation(model, this, "MANIF1", 100, 0.2, TestType.NONE);
            new DiseaseProgressionPathway(model, "PATHWAY1",  "Pathway to chronic manifestation 1", manif1,
                new ConstantTimeToEventCalculator(YEARS_TO_MANIFESTATION1));
            if (!BasicDiseaseExperiment.TESTS.ONE_CHRONIC.equals(model.getExample())) {
                manif2 = new BasicDisease.TestChronicManifestation(model, this, "MANIF2", 1000, 0.5, TestType.NONE);
                final AbstractCondition<DiseaseProgressionPathway.ConditionInformation> cond = new PreviousDiseaseProgressionCondition(manif1);
                new DiseaseProgressionPathway(model, "PATHWAY1_2", "Pathway from chronic manifestaion 1 to chronic manifestation 2", manif2, 
                    new ConstantTimeToEventCalculator(YEARS_TO_MANIFESTATION2), cond); 
                if (BasicDiseaseExperiment.TESTS.TWO_CHRONIC_EXCLUSIVE.equals(model.getExample())) {
                    addExclusion(manif2, manif1);
                }
            }                
            break;
        default:
            break;
        }
    }

    @Override
    public void createParameters() {
        addUsedParameter(StandardParameter.ANNUAL_UTILITY, "", "Test", 1.0 - DISUTILITY);
    }
}