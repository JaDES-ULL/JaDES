/**
 * 
 */
package es.ull.simulation.hta.inforeceiver;

import java.util.TreeMap;

import es.ull.simulation.hta.DiseaseProgressionSimulation;
import es.ull.simulation.hta.HTAExperiment;
import es.ull.simulation.hta.info.PatientInfo;
import es.ull.simulation.hta.interventions.DetectionTestResult;
import es.ull.simulation.hta.interventions.Intervention;
import es.ull.simulation.hta.interventions.ScreeningIntervention;
import es.ull.simulation.info.IPieceOfInformation;
import es.ull.simulation.info.SimulationStartStopInfo;

/**
 * Listener for the results of the screening tests.
 * It collects the results of the screening tests and stores them in a collector.
 * 
 * @author Iván Castilla Rodríguez
 *
 */
public class ScreeningTestPerformanceListener extends BasicHTAListener {
	/** The results per intervention. Each position of the array is a potential result of the screening test */
	private final TreeMap<Intervention, int[]> results;

	/**
	 * Creates a new listener for the results of the screening tests
	 * @param exp The experiment to be analyzed
	 * @param collector The collector of the results
	 */
	public ScreeningTestPerformanceListener(HTAExperiment exp, ScreeningTestPerformanceCollector collector) {
		super("Screening test performance", exp, collector);
		results = new TreeMap<>();
		for (Intervention intervention : interventions) {
			if (intervention instanceof ScreeningIntervention) {
				results.put(intervention, new int[DetectionTestResult.values().length]);
			}
		}
		addTargetInformation(PatientInfo.class);
		addTargetInformation(SimulationStartStopInfo.class);
	}

	@Override
	public void aggregateResultsAfterIntervention(int simulationId, Intervention intervention) {
		// Do nothing
	}

	/**
	 * Returns the results of the screening tests
	 * @return the results
	 */
	public TreeMap<Intervention, int[]> getResults() {
		return results;
	}

	@Override
	public void infoEmited(IPieceOfInformation info) {
		if (info instanceof SimulationStartStopInfo) {
			final SimulationStartStopInfo tInfo = (SimulationStartStopInfo) info;
			if (SimulationStartStopInfo.Type.END.equals(tInfo.getType())) {
				final DiseaseProgressionSimulation simul = (DiseaseProgressionSimulation)tInfo.getSimul();
				incrementInterventionsSimulated(simul);
			}
		}
		else if (info instanceof PatientInfo) {
			final PatientInfo pInfo = (PatientInfo) info;
			if (PatientInfo.Type.SCREEN.equals(pInfo.getType())) {
				results.get(pInfo.getPatient().getIntervention())[((DetectionTestResult)pInfo.getCause()).ordinal()]++;
			}
		}
	}

}
