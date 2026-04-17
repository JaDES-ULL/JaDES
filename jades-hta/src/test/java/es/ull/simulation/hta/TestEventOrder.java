package es.ull.simulation.hta;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import es.ull.simulation.hta.info.PatientInfo;
import es.ull.simulation.hta.populations.Population;
import es.ull.simulation.model.TimeUnit;

/**
 * A class to test event order for different combination of manifestations
 */
public class TestEventOrder {

    @BeforeEach
    public void setUp() {
        es.ull.simulation.hta.params.Parameter.resetAll();
    }

    @ParameterizedTest
    @EnumSource(BasicDiseaseExperiment.TESTS.class)
    @DisplayName("Test different examples of disease progression")
    public void testAll(BasicDiseaseExperiment.TESTS example) throws MalformedSimulationModelException, IOException {
        final BasicDiseaseExperiment.TestArgumentsProvider arguments = new BasicDiseaseExperiment.TestArgumentsProvider(example);
        final HTAExperiment exp = new BasicDiseaseExperiment(arguments);
        exp.initializeModel();
        exp.run();
    }

    static ArrayList<ExactOrderPatientEventChecker.PatientEvent> getExpectedEventsForExample(BasicDiseaseExperiment.TESTS example) {
        ArrayList<ExactOrderPatientEventChecker.PatientEvent> expectedEvents = new ArrayList<ExactOrderPatientEventChecker.PatientEvent>();
        expectedEvents.add(new ExactOrderPatientEventChecker.PatientEvent(0, 0, PatientInfo.Type.START));
        int year = BasicDisease.YEARS_TO_MANIFESTATION1;
        double lifeExpectancy = Population.DEF_MAX_AGE - Population.DEF_MIN_AGE;

        switch (example) {
        case ONE_CHRONIC:
            expectedEvents.add(new ExactOrderPatientEventChecker.PatientEvent(0, TimeUnit.DAY.convert(year, TimeUnit.YEAR), PatientInfo.Type.START_MANIF));
            break;
        case DEATH_BY_CHRONIC_IMR:
            expectedEvents.add(new ExactOrderPatientEventChecker.PatientEvent(0, TimeUnit.DAY.convert(year, TimeUnit.YEAR), PatientInfo.Type.START_MANIF));
            lifeExpectancy = BasicDisease.YEARS_TO_MANIFESTATION1 + (lifeExpectancy - BasicDisease.YEARS_TO_MANIFESTATION1) / BasicDisease.DEF_IMR;
            break;
        case DEATH_BY_CHRONIC_LER:
            expectedEvents.add(new ExactOrderPatientEventChecker.PatientEvent(0, TimeUnit.DAY.convert(year, TimeUnit.YEAR), PatientInfo.Type.START_MANIF));
            lifeExpectancy -= BasicDisease.DEF_LER;            
            break;
        case TWO_CHRONIC:
        case TWO_CHRONIC_EXCLUSIVE:
            expectedEvents.add(new ExactOrderPatientEventChecker.PatientEvent(0, TimeUnit.DAY.convert(year, TimeUnit.YEAR), PatientInfo.Type.START_MANIF));
            year += BasicDisease.YEARS_TO_MANIFESTATION2;
            expectedEvents.add(new ExactOrderPatientEventChecker.PatientEvent(0, TimeUnit.DAY.convert(year, TimeUnit.YEAR), PatientInfo.Type.START_MANIF));
            if (example == BasicDiseaseExperiment.TESTS.TWO_CHRONIC_EXCLUSIVE) {
                expectedEvents.add(new ExactOrderPatientEventChecker.PatientEvent(0, TimeUnit.DAY.convert(year, TimeUnit.YEAR), PatientInfo.Type.END_MANIF));
            }
            break;
        case ACUTE:
            year = BasicDisease.YEARS_AMONG_ACUTE_MANIFESTATIONS;
            while (year < Population.DEF_MAX_AGE - Population.DEF_MIN_AGE) {
                expectedEvents.add(new ExactOrderPatientEventChecker.PatientEvent(0, TimeUnit.DAY.convert(year, TimeUnit.YEAR), PatientInfo.Type.START_MANIF));
                year += BasicDisease.YEARS_AMONG_ACUTE_MANIFESTATIONS;
            }
            break;
        case ACUTE_DEATH:
            expectedEvents.add(new ExactOrderPatientEventChecker.PatientEvent(0, TimeUnit.DAY.convert(BasicDisease.YEARS_AMONG_ACUTE_MANIFESTATIONS, TimeUnit.YEAR), PatientInfo.Type.START_MANIF));
            lifeExpectancy = BasicDisease.YEARS_AMONG_ACUTE_MANIFESTATIONS;
            break;
        }
        expectedEvents.add(new ExactOrderPatientEventChecker.PatientEvent(0, TimeUnit.DAY.convert(lifeExpectancy, TimeUnit.YEAR), PatientInfo.Type.DEATH));
        return expectedEvents;
    }

}
