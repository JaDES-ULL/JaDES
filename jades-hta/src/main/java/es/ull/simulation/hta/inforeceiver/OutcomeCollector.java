package es.ull.simulation.hta.inforeceiver;

import es.ull.simulation.hta.HTAExperiment;
import es.ull.simulation.hta.HTAModel;
import es.ull.simulation.hta.inforeceiver.OutcomeListener.AggregatedResult;
import es.ull.simulation.hta.interventions.Intervention;
import es.ull.simulation.hta.params.Discount;

/**
 * A collector of information from outcome listeners
 */
public abstract class OutcomeCollector extends BasicListenerCollector implements CSVProducer {
	/** Interventions assessed */
	protected final Intervention[] interventions;
    /** The discount rate to be applied to future values of the outcomes */
    private final Discount discountRate;
    /** A description of the outcome */
    private final String description;
    /** The aggregated results by simulation experiment and intervention */
    private final AggregatedResult[][] results;

    /**
     * Creates a new collector of outcomes
     * @param exp The experiment that defines the simulations
     * @param baseCase Whether the collector is for the base case or not
     * @param description A description of the outcome
     * @param discountRate The discount rate to be applied to future values of the outcomes
     */
    public OutcomeCollector(HTAExperiment exp, boolean baseCase, String description, Discount discountRate) {
        super(exp, baseCase);
        this.discountRate = discountRate;
        this.description = description;
        results = new AggregatedResult[getNExperiments()][exp.getModel().getRegisteredInterventions().length];
		HTAModel model = exp.getModel();
		this.interventions = model.getRegisteredInterventions();
    }

    @Override
    public void collectFromListener(int simulationId, AttachableToCollector listener) {
        final OutcomeListener outcomeListener = (OutcomeListener) listener;
        results[isBaseCase() ? 0 : (simulationId - 1)] = outcomeListener.getResults();
    }

	@Override
	public String getStrHeader() {
		final StringBuilder str = new StringBuilder();
		for (int nIntervention = 0; nIntervention < interventions.length; nIntervention++) {
			str.append(STR_AVG_PREFIX + getPrefix() + interventions[nIntervention].name() + getSeparatorString());
			str.append(STR_L95CI_PREFIX + getPrefix() + interventions[nIntervention].name() + getSeparatorString());
			str.append(STR_U95CI_PREFIX + getPrefix() + interventions[nIntervention].name() + getSeparatorString());
		}
		return str.toString();
	}
	
	@Override
	public String getPartialFormattedResult(int simulationId) {
		final StringBuilder str = new StringBuilder();
        final AggregatedResult[] aggregatedResults = results[isBaseCase() ? 0 : (simulationId - 1)];
		for (int nIntervention = 0; nIntervention < interventions.length; nIntervention++) {
			str.append(aggregatedResults[nIntervention].getAverage()).append(getSeparatorString());
            str.append(aggregatedResults[nIntervention].get95ConfidenceIntervals()[0]).append(getSeparatorString());
            str.append(aggregatedResults[nIntervention].get95ConfidenceIntervals()[1]).append(getSeparatorString());
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

    /**
     * Returns a description of the outcome
     * @return A description of the outcome
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the aggregated results by simulation experiment and intervention
     * @return The aggregated results by simulation experiment and intervention
     */
    public AggregatedResult[][] getResults() {
        return results;
    }

    /**
     * Returns the discount rate to be applied to future values of the outcomes
     * @return The discount rate to be applied to future values of the outcomes
     */
    public Discount getDiscountRate() {
        return discountRate;
    }

	/**
	 * Returns the prefix to be used when formatting the header of the outcome
	 * @return The prefix to be used when formatting the header of the outcome
	 */
	public abstract String getPrefix();

}
