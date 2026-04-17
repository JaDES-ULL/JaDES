package es.ull.simulation.hta.inforeceiver;

import es.ull.simulation.hta.HTAExperiment;
import es.ull.simulation.hta.params.Discount;

public class LYCollector extends OutcomeCollector {
	private final static String PREFIX = "LY_";
    
    public LYCollector(HTAExperiment exp, boolean baseCase, String description, Discount discountRate) {
        super(exp, baseCase, description, discountRate);
    }

    @Override
    public BasicHTAListener createListener(HTAExperiment exp) {
        return new LYListener(exp, this, getDescription(), getDiscountRate());
    }

	@Override
	public String getPrefix() {
		return PREFIX;
	}
    
}
