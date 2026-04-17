package es.ull.simulation.hta.osdi.decisiontree.excelport;

import es.ull.simulation.hta.osdi.decisiontree.excelport.HTAExcelModelFactory.CommonNamedRanges;
import es.ull.simulation.utils.ExcelFormula;
import es.ull.simulation.utils.ExcelFormulaLibrary;

public enum FormulaLibrary implements ExcelFormula {
    // Update of costs according to CPI. Expects two parameters: the cost value and the year to adjust the cost
    UPDATE_COST("%s*"+ ExcelFormulaLibrary.ROUND0.getFormula(
                "1000*"+
                ExcelFormulaLibrary.VLOOKUP_EXACT.getFormula(CommonNamedRanges.STUDY_YEAR.getName(), CommonNamedRanges.CPI.getName(),2) + "/" +
                ExcelFormulaLibrary.VLOOKUP_EXACT.getFormula("%s", CommonNamedRanges.CPI.getName(), 2)) +
                "/ 1000"), 
    // Continuous discount. Expects three parameters: the discount rate, the time period (in years), and the (positive) value to discount
    CONT_DISCOUNT("PV(%s,%s,-%s)"), 
    // Future value discount, expects the same parameters as CONT_DISCOUNT plus the year where the future value starts its application.
    FUTURE_CONT_DISCOUNT(CONT_DISCOUNT.getTemplate() + " * (1 + %s) ^ %s") {
        @Override
        public String getFormula(Object... operands) {
            // The template expects five operands, but the future value discount only requires four.
            Object[] newOperands = new Object[operands.length + 1];
            System.arraycopy(operands, 0, newOperands, 0, operands.length);
            // The last operand must be the year where the future value applies.
            newOperands[operands.length] = operands[operands.length - 1];
            // The second to last operand must be the discount rate.
            newOperands[operands.length - 1] = operands[0];
            return super.getFormula(newOperands);
        }
    },
    // Punctual discount, expects the value to discount, the discount rate, the exact time (year) when the value is discounted
    PUNCTUAL_DISCOUNT("%s / (1 + %s) ^ %s");
    private final String template;
    private FormulaLibrary(String template) {
        this.template = template;
    }
    @Override
    public String getTemplate() {
        return template;
    }
}
