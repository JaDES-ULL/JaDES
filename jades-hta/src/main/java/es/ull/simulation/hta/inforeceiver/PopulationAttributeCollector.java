package es.ull.simulation.hta.inforeceiver;

import java.util.Set;
import java.util.TreeMap;

import es.ull.simulation.hta.HTAExperiment;
import es.ull.simulation.hta.HTAModel;
import es.ull.simulation.hta.inforeceiver.PopulationAttributeListener.AttributeAggregatedResult;
import es.ull.simulation.hta.interventions.Intervention;
import es.ull.simulation.hta.params.Parameter;
import es.ull.simulation.hta.params.ParameterGroup;

public class PopulationAttributeCollector extends BasicListenerCollector implements CSVProducer {
	/** Interventions assessed */
	protected final Intervention[] interventions;
	/** The names of the attributes used in the simulation */
	private final String [] attributeNames;
    private final AggregatedResult[] results;

    public PopulationAttributeCollector(HTAExperiment exp, boolean baseCase) {
        super(exp, baseCase);
        final HTAModel model = exp.getModel();
        this.interventions = model.getRegisteredInterventions();
		final Set<String> attributeList = Parameter.getParametersByType(ParameterGroup.ATTRIBUTE).keySet();
		this.attributeNames = attributeList.toArray(new String[attributeList.size()]);
        this.results = new AggregatedResult[getNExperiments()];
    }
    
	@Override
	public String getStrHeader() {
		final StringBuilder str = new StringBuilder();
		for (int nIntervention = 0; nIntervention < interventions.length; nIntervention++) {
			str.append(STR_AVG_PREFIX + "INITAGE_" + interventions[nIntervention].name() + getSeparatorString());
			str.append(STR_L95CI_PREFIX + "INITAGE_" + interventions[nIntervention].name() + getSeparatorString());
			str.append(STR_U95CI_PREFIX + "INITAGE_" + interventions[nIntervention].name() + getSeparatorString());
			for (String attribName : attributeNames) {
				str.append(STR_AVG_PREFIX + attribName + "_" + interventions[nIntervention].name() + getSeparatorString());
				str.append(STR_L95CI_PREFIX + attribName + "_" + interventions[nIntervention].name() + getSeparatorString());
				str.append(STR_U95CI_PREFIX + attribName + "_" + interventions[nIntervention].name() + getSeparatorString());
			}
		}
		return str.toString();
	}
	
	@Override
	public String getPartialFormattedResult(int simulationId) {
		final StringBuilder str = new StringBuilder();
        final TreeMap<String, AttributeAggregatedResult> aggregated = results[isBaseCase() ? 0 : (simulationId - 1)].getResults();
		for (int nIntervention = 0; nIntervention < interventions.length; nIntervention++) {
            final AttributeAggregatedResult resInitAge = aggregated.get(PopulationAttributeListener.STR_INIT_AGE);
            str.append(resInitAge.getAverage(nIntervention)).append(getSeparatorString());
            str.append(resInitAge.get95Percentiles(nIntervention)[0]).append(getSeparatorString());
            str.append(resInitAge.get95Percentiles(nIntervention)[1]).append(getSeparatorString());
			for (String attribName : attributeNames) {
                final AttributeAggregatedResult resAttrib = aggregated.get(attribName);
                str.append(resAttrib.getAverage(nIntervention)).append(getSeparatorString());
                str.append(resAttrib.get95Percentiles(nIntervention)[0]).append(getSeparatorString());
                str.append(resAttrib.get95Percentiles(nIntervention)[1]).append(getSeparatorString());
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
        final PopulationAttributeListener populationAttributeListener = (PopulationAttributeListener) listener;
        results[isBaseCase() ? 0 : (simulationId - 1)] = new AggregatedResult(populationAttributeListener.getResults());        
    }

    @Override
    public BasicHTAListener createListener(HTAExperiment exp) {
        return new PopulationAttributeListener(exp, this);
    }

    private static class AggregatedResult {
        private final TreeMap<String, AttributeAggregatedResult> results;

        public AggregatedResult(TreeMap<String, AttributeAggregatedResult> results) {
            this.results = results;
        }

        public TreeMap<String, AttributeAggregatedResult> getResults() {
            return results;
        }
    }
}
