package es.ull.simulation.hta;

import static org.junit.jupiter.api.Assertions.assertTrue;

import es.ull.simulation.hta.BasicDiseaseExperiment.TESTS;
import es.ull.simulation.hta.info.PatientInfo;
import es.ull.simulation.hta.inforeceiver.CostListener;
import es.ull.simulation.hta.inforeceiver.LYListener;
import es.ull.simulation.hta.inforeceiver.OutcomeListener.AggregatedResult;
import es.ull.simulation.hta.inforeceiver.QALYListener;
import es.ull.simulation.hta.params.StandardParameter;
import es.ull.simulation.info.IPieceOfInformation;
import es.ull.simulation.info.SimulationStartStopInfo;
import es.ull.simulation.inforeceiver.BasicListener;
import es.ull.simulation.inforeceiver.IListener;

/**
 * A listener that checks the outcomes of the simulation against the expected values.
 */
public class OutcomesChecker extends BasicListener {
    final private static double DELTA = 0.0001;
    final private double expectedFinalLY;
    final private double expectedFinalQALY;
    final private double expectedFinalCost;
    private LYListener lyListener;
    private QALYListener qalyListener;
    private CostListener costListener;

    public OutcomesChecker(TESTS example) {
        super("Disease 0 listener");
        this.expectedFinalLY = example.getExpectedLY();
        this.expectedFinalQALY = example.getExpectedQALY();
        this.expectedFinalCost = example.getExpectedCost();
		addTargetInformation(PatientInfo.class);
        addTargetInformation(SimulationStartStopInfo.class);
        lyListener = null;
        qalyListener = null;
        costListener = null;
    }

    @Override
    public void infoEmited(IPieceOfInformation info) {
        if (info instanceof PatientInfo) {
            if (((PatientInfo) info).getType().equals(PatientInfo.Type.START)) {
                final PatientInfo pInfo = (PatientInfo) info;
                final Patient pat = pInfo.getPatient();
                assertTrue(Math.abs(pat.getDisease().getUsedParameterValue(StandardParameter.ANNUAL_DISUTILITY, pat) - BasicDisease.DISUTILITY) < DELTA, "Disutility not computed correctly");
                assertTrue(Math.abs(pat.getDisease().getUsedParameterValue(StandardParameter.ANNUAL_UTILITY, pat) - pat.getSimulation().getModel().getPopulation().getBaseUtility(pat) + BasicDisease.DISUTILITY) < DELTA, "Utility not computed correctly");
            }
        }
        else if (info instanceof SimulationStartStopInfo) {
            final SimulationStartStopInfo sInfo = (SimulationStartStopInfo) info;
            final SimulationStartStopInfo.Type type = sInfo.getType();
            final DiseaseProgressionSimulation simul = (DiseaseProgressionSimulation) sInfo.getSimul();
            switch (type) {
                case END:
                    AggregatedResult resultsQALY = qalyListener.getResults()[0];
                    assertTrue(Math.abs(expectedFinalQALY - resultsQALY.getAverage()) < DELTA, "The expected QALYs for the tested patient (" + resultsQALY.getAverage() + " do not fit the obtained value (" + expectedFinalQALY + " )");
                    AggregatedResult resultsCost = costListener.getResults()[0];
                    assertTrue(Math.abs(expectedFinalCost - resultsCost.getAverage()) < DELTA, "The expected cost for the tested patient (" + resultsCost.getAverage() + " do not fit the obtained value (" + expectedFinalCost + " )");
                    AggregatedResult resultsLY = lyListener.getResults()[0];
                    assertTrue(Math.abs(expectedFinalLY - resultsLY.getAverage()) < DELTA, "The expected LYs for the tested patient (" + resultsLY.getAverage() + " do not fit the obtained value (" + expectedFinalLY + " )");
                    break;
                case START:
                    for (IListener listener : simul.getListeners()) {
                        if (listener instanceof QALYListener) {
                            qalyListener = (QALYListener) listener;
                        }
                        else if (listener instanceof CostListener) {
                            costListener = (CostListener) listener;
                        }
                        else if (listener instanceof LYListener) {
                            lyListener = (LYListener) listener;
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }
}