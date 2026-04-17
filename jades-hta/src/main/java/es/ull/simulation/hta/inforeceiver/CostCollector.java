package es.ull.simulation.hta.inforeceiver;

import es.ull.simulation.hta.HTAExperiment;
import es.ull.simulation.hta.params.Discount;

public class CostCollector extends OutcomeCollector {
	private final static String PREFIX = "C_";
    
    public CostCollector(HTAExperiment exp, boolean baseCase, String description, Discount discountRate) {
        super(exp, baseCase, description, discountRate);
    }

    @Override
    public BasicHTAListener createListener(HTAExperiment exp) {
        return new CostListener(exp, this, getDescription(), getDiscountRate());
    }

	@Override
	public String getPrefix() {
		return PREFIX;
	}
}
