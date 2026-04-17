/**
 * 
 */
package es.ull.simulation.hta.inforeceiver;

import java.util.Locale;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import es.ull.simulation.hta.HTAExperiment;
import es.ull.simulation.hta.HTAModel;
import es.ull.simulation.hta.interventions.Intervention;
import es.ull.simulation.hta.params.Discount;
import es.ull.simulation.hta.progression.Disease;

/**
 * A collector of {@link BreakdownCostListener listeners} for the breakdown of annual costs.
 * 
 * @author Iván Castilla Rodríguez
 *
 */
public class BreakdownCostCollector extends BasicListenerCollector implements JSONProducer, TxtProducer {
	/** The interventions defined in the model */
	private final Intervention[] interventions;
	/** The minimum age of the patients in the simulation */
	private final int minAge;
	/** The maximum age of the patients in the simulation */
	private final int maxAge;
	/** The breakdown of costs by intervention, disease and age */
	private final double[][][] diseaseCost;
	/** The breakdown of costs directly related to the intervention, by intervention and age */
	private final double[][] interventionCost;
	/** The breakdown of costs directly related to the management of the disease, by intervention and age */
	private final double[][] managementCost;
	/** The discount to be applied to the costs */
	private final Discount discount;

	/**
	 * Creates a collector for the breakdown of annual costs
	 * @param exp The experiment to be analyzed
	 * @param baseCase If true, collects data from a single experiment; otherwise, collects data from multiple experiments
	 */
	public BreakdownCostCollector(HTAExperiment exp, boolean baseCase, Discount discount) {
		super(exp, baseCase);
		final HTAModel model = exp.getModel();
		this.interventions = model.getRegisteredInterventions();
		final int nInterventions = interventions.length;
		this.minAge = (int)model.getPopulation().getMinAge();
		this.maxAge = (int)Math.ceil(model.getPopulation().getMaxAge());
		this.discount = discount;
		diseaseCost = new double[nInterventions][model.getRegisteredDiseases().length][maxAge-minAge+1];
		interventionCost = new double[nInterventions][maxAge-minAge+1];
		managementCost = new double[nInterventions][maxAge-minAge+1];
	}

	@Override
	public BasicHTAListener createListener(HTAExperiment exp) {
		return new BreakdownCostListener(exp, this, discount);
	}

	@Override
	public synchronized void collectFromListener(int simulationId, AttachableToCollector listener) {
		final BreakdownCostListener annualCostListener = (BreakdownCostListener) listener;
		for (int interventionId = 0; interventionId < interventions.length; interventionId++) {
			for (int i = 0; i < maxAge-minAge+1; i++) {
				managementCost[interventionId][i] += annualCostListener.getManagementCost()[interventionId][i] / getNPatients();
				interventionCost[interventionId][i] += annualCostListener.getInterventionCost()[interventionId][i] / getNPatients();
				for (int j = 0; j < getModel().getRegisteredDiseases().length; j++) {
					diseaseCost[interventionId][j][i] += annualCostListener.getDiseaseCost()[interventionId][j][i] / getNPatients();
				}
			}			
		}
		incrementExperimentsPerformed();
	}

	@Override
	public void incrementExperimentsPerformed() {
		super.incrementExperimentsPerformed();
		if (getExperimentsPerformed() == getNExperiments()) {
			for (int year = 0; year < maxAge-minAge+1; year++) {
				for (int i = 0; i < interventions.length; i++) {
					interventionCost[i][year] /= getNExperiments();
					managementCost[i][year] /= getNExperiments();
					for (int k = 0; k < getModel().getRegisteredDiseases().length; k++) {
						diseaseCost[i][k][year] /= getNExperiments();
					}
				}
			}
		}
	}

	@Override
	public ObjectNode produceJSON() {
		final ObjectMapper mapper = new ObjectMapper();
		final ObjectNode json = mapper.createObjectNode();
		json.put("name", "breakdown costs");
		json.put("base case", isBaseCase());
		json.put("discount", discount.getDiscountRate());
		final ArrayNode interventionsArray = mapper.createArrayNode();
		final HTAModel model = getModel();
 		for (int i = 0; i < interventions.length; i++) {
			final ObjectNode jsonIntervention = mapper.createObjectNode();
			jsonIntervention.put("name", interventions[i].name());
			final ArrayNode jsonInterventionCosts =mapper.createArrayNode();
			final ArrayNode jsonManagementCosts = mapper.createArrayNode();
			final ArrayNode[] jsonDiseaseCosts = new ArrayNode[model.getRegisteredDiseases().length];
			for (Disease disease : model.getRegisteredDiseases()) {
				jsonDiseaseCosts[disease.ordinal()] = mapper.createArrayNode();
				jsonIntervention.set(disease.name(), jsonDiseaseCosts[disease.ordinal()]);
			}
			for (int year = 0; year < maxAge-minAge+1; year++) {
				jsonInterventionCosts.add(interventionCost[i][year]);
				jsonManagementCosts.add(managementCost[i][year]);
				for (Disease disease : model.getRegisteredDiseases()) {
					jsonDiseaseCosts[disease.ordinal()].add(diseaseCost[i][disease.ordinal()][year]);
				}
			}
			jsonIntervention.set("intervention", jsonInterventionCosts);
			jsonIntervention.set("management", jsonManagementCosts);
			interventionsArray.add(jsonIntervention);
		}
		json.set("results", interventionsArray);
		return json;
	}

	@Override
	public String getStrHeader() {
		final StringBuilder str = new StringBuilder("Breakdown of costs");
		str.append(System.lineSeparator()).append("Year");
		for (int i = 0; i < interventions.length; i++) {
			final String name = interventions[i].name();
			str.append("\t").append(name).append("-I\t" + name + "-M");
			for (Disease disease : getModel().getRegisteredDiseases()) {
				str.append("\t" + name + "-").append(disease);
			}
		}
		return str.toString();
	}

	@Override
	public String getFormattedResult() {
		final StringBuilder str = new StringBuilder("Breakdown of costs");
		for (int year = 0; year < maxAge-minAge+1; year++) {
			str.append(System.lineSeparator()).append(year);
			for (int i = 0; i < interventions.length; i++) {
				str.append("\t").append(String.format(Locale.US, "%.2f", interventionCost[i][year]));
				str.append("\t").append(String.format(Locale.US, "%.2f", managementCost[i][year]));
				for (int k = 0; k < getModel().getRegisteredDiseases().length; k++) {
					str.append("\t").append(String.format(Locale.US, "%.2f", diseaseCost[i][k][year]));
				}
			}
		}
		return str.toString();
	}
}
