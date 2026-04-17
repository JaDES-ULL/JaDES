/**
 * 
 */
package es.ull.simulation.hta.inforeceiver;

import java.util.Locale;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import es.ull.simulation.hta.HTAExperiment;
import es.ull.simulation.hta.interventions.Intervention;

/**
 * @author Iván Castilla Rodríguez
 *
 */
public class BudgetImpactCollector extends BasicListenerCollector implements JSONProducer {
	final int timeHorizon;
	final double[][] cost;
	private final Intervention[] interventions;
	private final double coefExperiments;

	/**
	 * A collector for the budget impact analysis
	 * @param exp The experiment to be analyzed
	 * @param baseCase Whether the analysis is for the base case or not
	 * @param timeHorizon The time horizon for the budget impact analysis (in years)
	 */
	public BudgetImpactCollector(HTAExperiment exp, boolean baseCase, int timeHorizon) {
		super(exp, baseCase);
		this.coefExperiments = 1.0 / (double)getNExperiments();
		this.interventions = exp.getModel().getRegisteredInterventions();
		this.timeHorizon = timeHorizon;
		this.cost = new double[interventions.length][timeHorizon+1];
	}

	/**
	 * Returns the time horizon for the budget impact analysis (in years)
	 * @return The time horizon for the budget impact analysis (in years)
	 */
	public int getTimeHorizon() {
		return timeHorizon;
	}

	@Override
	public BasicHTAListener createListener(HTAExperiment exp) {
		return new BudgetImpactListener(exp, this);
	}

	@Override
	public synchronized void collectFromListener(int simulationId, AttachableToCollector listener) {
		final BudgetImpactListener budgetImpactListener = (BudgetImpactListener) listener;
		for (int interventionId = 0; interventionId < interventions.length; interventionId++) {
			for (int year = 0; year < budgetImpactListener.getCost()[interventionId].length; year++) {
				cost[interventionId][year] += (budgetImpactListener.getCost()[interventionId][year] / getNPatients());
			}
		}
	}


	@Override
	public ObjectNode produceJSON() {
		final ObjectMapper mapper = new ObjectMapper();
		final ObjectNode json = mapper.createObjectNode();
		json.put("name", "budget impact");
		json.put("patients", getNPatients());
		json.put("years", timeHorizon);
		for (int interventionId = 0; interventionId < interventions.length; interventionId++) {
			final ArrayNode interventionJSON = mapper.createArrayNode();
			for (int year = 0; year <= timeHorizon; year++) {
				interventionJSON.add(cost[interventionId][year]);
			}
			json.set(interventions[interventionId].name(), interventionJSON);
		}
		return json;
	}

	@Override
	public String toString() {
		final StringBuilder str = new StringBuilder("Annual costs (for computing budget impact)");
		str.append(System.lineSeparator()).append("Year");
		for (int i = 0; i < interventions.length; i++) {
			str.append("\t").append(interventions[i].name());
		}
		str.append(System.lineSeparator());
		for (int year = 0; year < timeHorizon; year++) {
			str.append(year);
			for (int i = 0; i < interventions.length; i++) {
				str.append("\t").append(String.format(Locale.US, "%.2f", cost[i][year]));			
			}
			str.append(System.lineSeparator());
		}
		return str.toString();
	}
	
	@Override
	public void incrementExperimentsPerformed() {
		super.incrementExperimentsPerformed();
		if (getExperimentsPerformed() == getNExperiments()) {
			for (int interventionId = 0; interventionId < interventions.length; interventionId++) {
				for (int year = 0; year < cost.length; year++) {
					cost[interventionId][year] *= coefExperiments;
				}
			}
		}
	}
}
