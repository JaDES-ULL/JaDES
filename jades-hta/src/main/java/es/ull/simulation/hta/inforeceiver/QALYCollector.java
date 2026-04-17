package es.ull.simulation.hta.inforeceiver;

import es.ull.simulation.hta.HTAExperiment;
import es.ull.simulation.hta.params.Discount;

public class QALYCollector extends OutcomeCollector {
	private final static String PREFIX = "QALY_";
    
    public QALYCollector(HTAExperiment exp, boolean baseCase, String description, Discount discountRate) {
        super(exp, baseCase, description, discountRate);
    }

    @Override
    public BasicHTAListener createListener(HTAExperiment exp) {
        return new QALYListener(exp, this, getDescription(), getDiscountRate(), exp.getDisutilityCombinationMethod());
    }

	@Override
	public String getPrefix() {
		return PREFIX;
	}
    
}
