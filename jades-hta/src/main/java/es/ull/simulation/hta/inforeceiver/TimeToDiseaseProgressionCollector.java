package es.ull.simulation.hta.inforeceiver;

import java.util.TreeMap;

import es.ull.simulation.hta.HTAExperiment;
import es.ull.simulation.hta.HTAModel;
import es.ull.simulation.hta.inforeceiver.TimeToDiseaseProgressionListener.AggregatedResult;
import es.ull.simulation.hta.inforeceiver.TimeToDiseaseProgressionListener.ResultsPerAcuteEvent;
import es.ull.simulation.hta.inforeceiver.TimeToDiseaseProgressionListener.ResultsPerChronicManifestation;
import es.ull.simulation.hta.inforeceiver.TimeToDiseaseProgressionListener.ResultsPerDiseaseProgression;
import es.ull.simulation.hta.interventions.Intervention;
import es.ull.simulation.hta.progression.DiseaseProgression;

public class TimeToDiseaseProgressionCollector extends BasicListenerCollector implements CSVProducer {
	private final static String STR_AVG_TIME = CSVProducer.STR_AVG_PREFIX + "TIME_TO_";
	private final static String STR_LCI_TIME = CSVProducer.STR_L95CI_PREFIX + "TIME_TO_";
	private final static String STR_UCI_TIME = CSVProducer.STR_U95CI_PREFIX + "TIME_TO_";
	private final static String STR_INC = "INC_";
	private final static String STR_PREV = "PREV_";

 	/** Enables printing the confidence intervals for first order simulations */
     private final boolean printFirstOrderVariance;
	/** Interventions assessed */
	private final Intervention[] interventions;
	/** The aggregated results by simulation experiment and intervention */
	private final AggregatedResult[][] results; 

    public TimeToDiseaseProgressionCollector(HTAExperiment exp, boolean baseCase, boolean printFirstOrderVariance) {
        super(exp, baseCase);
        this.printFirstOrderVariance = printFirstOrderVariance;
		final HTAModel model = exp.getModel();
		this.interventions = model.getRegisteredInterventions();
		this.results = new AggregatedResult[getNExperiments()][interventions.length];
    }

	@Override
	public String getStrHeader() {
		final HTAModel model = getModel();
		final StringBuilder str = new StringBuilder();
		if (printFirstOrderVariance) {
			for (Intervention intervention : model.getRegisteredInterventions()) {
				for (DiseaseProgression progression : model.getRegisteredDiseaseProgressions()) {
					final String suf = progression.name() + "_" + intervention.name() + getSeparatorString();
					switch(progression.getType()) {
					case CHRONIC_MANIFESTATION:
						str.append(STR_INC).append(suf).append(STR_PREV).append(suf).append(STR_AVG_TIME).append(suf).append(STR_LCI_TIME).append(suf).append(STR_UCI_TIME).append(suf);
						break;
					case ACUTE_MANIFESTATION:
						str.append(CSVProducer.STR_AVG_PREFIX).append(suf).append(CSVProducer.STR_L95CI_PREFIX).append(suf).append(CSVProducer.STR_U95CI_PREFIX).append(suf);
						break;
					default:
						break;
					}
				}			
			}
		}
		else {
			for (Intervention inter : model.getRegisteredInterventions()) {
				for (DiseaseProgression progression : model.getRegisteredDiseaseProgressions()) {
					final String suf = progression.name() + "_" + inter.name() + getSeparatorString();
					switch(progression.getType()) {
					case CHRONIC_MANIFESTATION:
						str.append(STR_INC).append(suf).append(STR_PREV).append(suf).append(STR_AVG_TIME).append(suf);
						break;
					case ACUTE_MANIFESTATION:
						str.append(CSVProducer.STR_AVG_PREFIX).append(suf);
						break;
					default:
						break;
					}
				}
			}
		}
		return str.toString();
	}
	
	@Override
	public String getPartialFormattedResult(int simulationId) {
		final StringBuilder str = new StringBuilder();
		final AggregatedResult[] aggregatedResults = results[isBaseCase() ? 0 : (simulationId - 1)];
		for (int i = 0; i < interventions.length; i++) {
			final TreeMap<DiseaseProgression, ResultsPerDiseaseProgression> resPerProgression = aggregatedResults[i].getResultsPerDiseaseProgression();
			for (DiseaseProgression progression : resPerProgression.keySet()) {
				switch(progression.getType()) {
					case CHRONIC_MANIFESTATION:
						final ResultsPerChronicManifestation resChronic = (ResultsPerChronicManifestation) resPerProgression.get(progression);
						str.append(resChronic.getIncidence()).append(getSeparatorString());
						str.append(resChronic.getPrevalence()).append(getSeparatorString());
						str.append(resChronic.getAvgTimeToEvent()).append(getSeparatorString());
						if (printFirstOrderVariance) {
							str.append(resChronic.getCiTimeToEvent()[0]).append(getSeparatorString());
							str.append(resChronic.getCiTimeToEvent()[1]).append(getSeparatorString());
						}
						break;
					case ACUTE_MANIFESTATION:
						final ResultsPerAcuteEvent resAcute = (ResultsPerAcuteEvent) resPerProgression.get(progression);
						str.append(resAcute.getAvgNEvents()).append(getSeparatorString());
						if (printFirstOrderVariance) {
							str.append(resAcute.getCiEvents()[0]).append(getSeparatorString());
							str.append(resAcute.getCiEvents()[1]).append(getSeparatorString());
						}
						break;
					default:
						break;
					}

			}
		}
		return str.toString();
	}

    @Override
    public String getFormattedResult() {
		final StringBuilder str = new StringBuilder();
        for (int i = 0; i < results.length; i++) {
            str.append(getPartialFormattedResult(i)).append(System.lineSeparator());
        }
        return str.toString();
    }

    @Override
    public void collectFromListener(int simulationId, AttachableToCollector listener) {
		final TimeToDiseaseProgressionListener timeToListener = (TimeToDiseaseProgressionListener) listener;
		results[isBaseCase() ? 0 : (simulationId - 1)] = timeToListener.getResults();        
    }

	@Override
	public BasicHTAListener createListener(HTAExperiment exp) {
		return new TimeToDiseaseProgressionListener(exp, this);
	}
}
