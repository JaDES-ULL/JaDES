package es.ull.simulation.hta.osdi.des.factories;

import java.util.ArrayList;

import es.ull.simulation.hta.osdi.des.OSDiDESModel;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.ontology.OSDiDataItemType;
import es.ull.simulation.hta.osdi.ontology.OSDiClass;
import es.ull.simulation.hta.osdi.ontology.OSDiObjectProperty;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper;
import es.ull.simulation.hta.params.StandardParameter;
import es.ull.simulation.hta.progression.DiseaseProgression;
import es.ull.simulation.hta.progression.calculator.AnnualRiskBasedTimeToEventCalculator;
import es.ull.simulation.hta.progression.calculator.ProportionBasedTimeToEventCalculator;
import es.ull.simulation.hta.progression.calculator.TimeToEventCalculator;

public interface TimeToEventCalculatorFactory extends DESModelComponentFactory {
    public enum SupportedCombinations {
        PROBABILITY(new OSDiDataItemType[] { OSDiDataItemType.DI_PROBABILITY}),
        PROPORTION(new OSDiDataItemType[] { OSDiDataItemType.DI_PROPORTION}),
        // TIME_TO_EVENT(new DataItemType[] { DataItemType.DI_TIME_TO_EVENT}),
        PROBABILITY_RR(new OSDiDataItemType[] { OSDiDataItemType.DI_PROBABILITY, OSDiDataItemType.DI_RELATIVE_RISK});
        private final int n;
        private final OSDiDataItemType[] dataItems;

        private SupportedCombinations(OSDiDataItemType[] dataItems) {
            this.dataItems = dataItems;
            this.n = dataItems.length;
        }
    }

	/**
	 * Creates the calculator for the time to event associated to this pathway. Currently only allows the time to be expressed as an annual risk and, consequently, uses 
	 * a {@link AnnualRiskBasedTimeToEventCalculator}. 
	 * @param model Repository
	 * @param progression The destination progression for this pathway
	 * @return
	 * @throws MalformedOSDiModelException 
	 */
	public static TimeToEventCalculator getTimeToEventCalculator(OSDiDESModel model, DiseaseProgression progression, ArrayList<ParameterWrapper> riskWrappers) throws MalformedOSDiModelException {
        final SupportedCombinations comb = foundValidCombination(riskWrappers);
        if (comb == null) {
			throw new MalformedOSDiModelException(OSDiClass.PATHWAY, progression.name(), OSDiObjectProperty.HAS_RISK_CHARACTERIZATION, "Unsupported combination of parameters for risk characterization.");
        }
        switch(comb) {
            case PROPORTION:
                return new ProportionBasedTimeToEventCalculator(progression, StandardParameter.PROPORTION.createName(riskWrappers.get(0).getShortName()));
            case PROBABILITY_RR:
                if (riskWrappers.get(0).getDataItemType().equals(OSDiDataItemType.DI_RELATIVE_RISK)) {
                    return new AnnualRiskBasedTimeToEventCalculator(progression, StandardParameter.PROBABILITY.createName(riskWrappers.get(1).getShortName()), StandardParameter.RELATIVE_RISK.createName(riskWrappers.get(0).getShortName()));
                } else {
                    return new AnnualRiskBasedTimeToEventCalculator(progression, StandardParameter.PROBABILITY.createName(riskWrappers.get(0).getShortName()));
                }
            case PROBABILITY:
            default:
                return new AnnualRiskBasedTimeToEventCalculator(progression, StandardParameter.PROBABILITY.createName(riskWrappers.get(0).getShortName()));
        }
	}
 
    public static SupportedCombinations foundValidCombination(ArrayList<ParameterWrapper> riskWrappers) {
        for (SupportedCombinations comb : SupportedCombinations.values()) {
            if (comb.n == riskWrappers.size()) {
                final ArrayList<ParameterWrapper> temp = new ArrayList<>(riskWrappers);

                // Starts the search
                boolean valid = true;
                for (int i = 0; i < comb.n && valid; i++) {
                    // Resets the condition until a valid combination is found
                    valid = false;
                    // One of the remaining wrappers must contain the data item
                    for (int j = 0; j < temp.size() && !valid; j++) {
                        if (temp.get(j).getDataItemType().equals(comb.dataItems[i])) {
                            valid = true;
                            temp.remove(j);
                        }
                    }
                }
                if (valid) {
                    return comb;
                }
            }
        }
        return null;
    }
}
