package es.ull.simulation.hta.inforeceiver;

import java.util.TreeMap;

import es.ull.simulation.hta.HTAExperiment;
import es.ull.simulation.hta.HTAModel;
import es.ull.simulation.hta.interventions.DetectionTestResult;
import es.ull.simulation.hta.interventions.Intervention;
import es.ull.simulation.hta.interventions.ScreeningIntervention;

public class ScreeningTestPerformanceCollector extends BasicListenerCollector implements CSVProducer {
	/** Interventions assessed */
	protected final Intervention[] interventions;
    /** Results per simulation experiments, intervention and  */
    private final TreeMap<Intervention, int[][]> results;

    public ScreeningTestPerformanceCollector(HTAExperiment exp, boolean baseCase) {
        super(exp, baseCase);
        final HTAModel model = exp.getModel();
        this.interventions = model.getRegisteredInterventions();
        this.results = new TreeMap<>();
		for (Intervention intervention : interventions) {
			if (intervention instanceof ScreeningIntervention) {
				results.put(intervention, new int[getNExperiments()][DetectionTestResult.values().length]);
			}
		}
    }

    @Override
    public BasicHTAListener createListener(HTAExperiment exp) {
        return new ScreeningTestPerformanceListener(exp, this);
    }

    @Override
    public void collectFromListener(int simulationId, AttachableToCollector listener) {
        final ScreeningTestPerformanceListener screeningListener = (ScreeningTestPerformanceListener) listener;
        final TreeMap<Intervention, int[]> res = screeningListener.getResults();
        for (Intervention intervention : res.keySet()) {
            final int[] resIntervention = res.get(intervention);
            for (int i = 0; i < DetectionTestResult.values().length; i++) {
                results.get(intervention)[isBaseCase() ? 0 : (simulationId - 1)][i] = resIntervention[i];
            }
        }        
    }

	@Override
	public String getStrHeader() {
		final StringBuilder str = new StringBuilder();
        for (Intervention intervention : results.keySet()) {
            for (DetectionTestResult res : DetectionTestResult.values())
                str.append(res + "_" + intervention.name() + getSeparatorString());
        }
		return str.toString();
	}

	@Override
	public String getFormattedResult() {
		final StringBuilder str = new StringBuilder();
        for (int i = 0; i < getNExperiments(); i++) {
            str.append(getPartialFormattedResult(i));
        }
		return str.toString();
    }

    @Override
    public String getPartialFormattedResult(int simulationId) {
		final StringBuilder str = new StringBuilder();
        for (Intervention intervention : results.keySet()) {
            for (int res : results.get(intervention)[isBaseCase() ? 0 : (simulationId - 1)]) {
                str.append(res + getSeparatorString());
			}
		}
		return str.toString();
	}    
}
