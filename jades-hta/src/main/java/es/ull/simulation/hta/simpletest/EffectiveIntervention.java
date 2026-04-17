package es.ull.simulation.hta.simpletest;

import java.util.ArrayList;

import es.ull.simulation.hta.HTAModel;
import es.ull.simulation.hta.Patient;
import es.ull.simulation.hta.interventions.Intervention;
import es.ull.simulation.hta.params.Discount;
import es.ull.simulation.hta.params.StandardParameter;
import es.ull.simulation.hta.params.modifiers.FactorParameterModifier;
import simkit.random.RandomVariateFactory;

/**
 * An intervention that delays all the transitions
 * @author Iván Castilla Rodríguez
 *
 */
public class EffectiveIntervention extends Intervention {
	private final static double ANNUAL_COST = 200.0; 
	private final static double RR = 0.5;
	final ArrayList<String> modifiedParams;

	public EffectiveIntervention(HTAModel model, ArrayList<String> modifiedParams) {
		super(model, "INT", "Effective intervention");
		this.modifiedParams = modifiedParams;
	}

	@Override
	public void createParameters() {
		for (String paramName : modifiedParams) {
			String modName = StandardParameter.RELATIVE_RISK.createName(this + "_" + paramName);
			StandardParameter.RELATIVE_RISK.addToModel(model, modName, "Modification of parameter " + paramName, "", model.getStudyYear(),
					RR, RandomVariateFactory.getInstance("UniformVariate", RR * 0.8, RR * 1.2));
			model.addParameterModifier(paramName, this, new FactorParameterModifier(modName));
		}
	}

	@Override
	public double getCostWithinPeriod(Patient pat, double initT, double endT, Discount discountRate) {
		return discountRate.applyDiscount(ANNUAL_COST, initT, endT);
	}

	@Override
	public double getStartingCost(Patient pat, double time, Discount discountRate) {
		return 0;
	}

	@Override
	public double[] getAnnualizedCostWithinPeriod(Patient pat, double initT, double endT, Discount discountRate) {
		return discountRate.applyAnnualDiscount(ANNUAL_COST, initT, endT);
	}

	@Override
	public double getTreatmentAndFollowUpCosts(Patient pat, double initT, double endT, Discount discountRate) {
		return 0;
	}

	@Override
	public double[] getAnnualizedTreatmentAndFollowUpCosts(Patient pat, double initT, double endT,
			Discount discountRate) {
		return discountRate.applyAnnualDiscount(0.0, initT, endT);
	}
	
}