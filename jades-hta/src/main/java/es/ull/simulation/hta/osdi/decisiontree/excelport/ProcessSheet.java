package es.ull.simulation.hta.osdi.decisiontree.excelport;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import es.ull.simulation.hta.MalformedSimulationModelException;
import es.ull.simulation.hta.osdi.decisiontree.Model;
import es.ull.simulation.hta.osdi.decisiontree.SingleDiseaseAndPopulationModel;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.ParameterNatureType;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper;
import es.ull.simulation.hta.params.ParameterGroup;
import es.ull.simulation.utils.ExcelFormula;
import es.ull.simulation.utils.ExcelFormulaLibrary;
import es.ull.simulation.utils.ExcelTools;

/**
 * A wrapper class for the Process sheet of the Excel file that defines a HTA decision tree model.
 * The Process sheet summarizes the results of the decision tree model together with the values of the parameters used in the model.
 */
public class ProcessSheet implements SheetWrapper {
    public static final String SHEET_NAME = "Process";
    /**
     * Column index for the first payoff column (G)
     */
    private final static int FIRST_PAYOFF_COL_INDEX = 6;
    /**
     * The first row where the simulation (PSA) data starts.
     */
    static final int FIRST_SIMULATION_ROW_INDEX = 11;
    /**
     * A collection of formulas used in the Process sheet.
     */
    enum UsefulFormulas implements ExcelFormula {
        AVERAGE(ExcelFormulaLibrary.AVERAGE.getFormula(ExcelFormulaLibrary.OFFSET.getFormula("%s$"+ (FIRST_SIMULATION_ROW_INDEX + 1), 0,0, "nSim")), 1), // Used to calculate the average of a range
        STD_DEV(ExcelFormulaLibrary.STD_DEV.getFormula(ExcelFormulaLibrary.OFFSET.getFormula("%s$"+ (FIRST_SIMULATION_ROW_INDEX + 1), 0,0, "nSim")), 2), // Used to calculate the standard deviation of a range
        PERCENTILE05(ExcelFormulaLibrary.PERCENTILE05.getFormula(ExcelFormulaLibrary.OFFSET.getFormula("%s$"+ (FIRST_SIMULATION_ROW_INDEX + 1), 0,0, "nSim")), 3), // Used to calculate the 0.5 percentile (median) of a range
        PERCENTILE0025(ExcelFormulaLibrary.PERCENTILE0025.getFormula(ExcelFormulaLibrary.OFFSET.getFormula("%s$"+ (FIRST_SIMULATION_ROW_INDEX + 1), 0,0, "nSim")), 4), // Used to calculate the 0.025 percentile of a range
        PERCENTILE0975(ExcelFormulaLibrary.PERCENTILE0975.getFormula(ExcelFormulaLibrary.OFFSET.getFormula("%s$"+ (FIRST_SIMULATION_ROW_INDEX + 1), 0,0, "nSim")), 5), // Used to calculate the 0.975 percentile of a range
        R2COST(ExcelFormulaLibrary.R2.getFormula(ExcelFormulaLibrary.OFFSET.getFormula("$B$"+ (FIRST_SIMULATION_ROW_INDEX + 1), 0,0, "nSim"), ExcelFormulaLibrary.OFFSET.getFormula("%s$"+ (FIRST_SIMULATION_ROW_INDEX + 1), 0,0, "nSim")), 6), // Used to calculate the R2 index regarding the cost of a range
        R2LE(ExcelFormulaLibrary.R2.getFormula(ExcelFormulaLibrary.OFFSET.getFormula("$C$"+ (FIRST_SIMULATION_ROW_INDEX + 1), 0,0, "nSim"), ExcelFormulaLibrary.OFFSET.getFormula("%s$"+ (FIRST_SIMULATION_ROW_INDEX + 1), 0,0, "nSim")), 7), // Used to calculate the R2 index regarding the life expectancy of a range
        R2QALY(ExcelFormulaLibrary.R2.getFormula(ExcelFormulaLibrary.OFFSET.getFormula("$E$"+ (FIRST_SIMULATION_ROW_INDEX + 1), 0,0, "nSim"), ExcelFormulaLibrary.OFFSET.getFormula("%s$"+ (FIRST_SIMULATION_ROW_INDEX + 1), 0,0, "nSim")), 8); // Used to calculate the R2 index regarding the QALY of a range

        /**
         * The row offset where the formula will be placed in the Process sheet.
         */
        private final int rowOffset;
        /**
         * The template formula that will be used to create the formula in the Process sheet.
         */
        private final String template;
        /**
         * Constructor for the UsefulFormulas enum.
         * @param template The template formula that will be used to create the formula in the Process sheet.
         * @param rowOffset The row offset where the formula will be placed in the Process sheet.
         */
        private UsefulFormulas(String template, int rowOffset) {
            this.template = template;
            this.rowOffset = rowOffset;
        }

        /**
         * Returns the formula for the given column index.
         * @param colIndex The column index for which the formula is requested.
         * @return The formula as a string.
         */
        public String getFormula(int colIndex) {
            return getFormula(CellReference.convertNumToColString(colIndex));
        } 

        @Override
        public String getTemplate() {
            return template;
        }
        
        /**
         * Returns the row offset where the formula will be placed in the Process sheet.
         * @return The row offset as an integer.
         */
        public int getRowOffset() {
            return rowOffset;
        }
    }
    /**
     * The sheet that contains the model for the HTA model.
     */
    private final Sheet sheet;
    /**
     * The workbook that contains the model sheet.
     */
    private final Workbook workbook;
    /**
     * The HTAExcelModelBuilder that contains the workbook and styles.
     */
    private final HTAExcelModelFactory modelBuilder;

    /**
     * Constructor to initialize the HTAExcelProcessSheet with the HTAExcelModelBuilder.
     * @param modelBuilder The HTAExcelModelBuilder that contains the workbook and styles.
     * @throws MalformedSimulationModelException If the workbook does not contain a sheet named "Process".
     */
    public ProcessSheet(HTAExcelModelFactory modelBuilder) throws MalformedSimulationModelException {
        this.modelBuilder = modelBuilder;
        this.workbook = modelBuilder.getWorkbook();
        this.sheet = workbook.getSheet(SHEET_NAME);
        if (sheet == null) {
            throw new MalformedSimulationModelException("The workbook must contain a sheet named '" + SHEET_NAME + "'.");
        }
    }

    @Override
    public void updateSheet() throws MalformedSimulationModelException, UnsupportedOSDiFeatureException, MalformedOSDiModelException {
        final int nInterventions = modelBuilder.getModel().getTree().getSuccessors().size();
        for (int i = 0; i < nInterventions; i++) {
            final String interventionIRI = modelBuilder.getModel().getTree().getSuccessors().get(i).name();
            for (Model.PayoffType payoffType : Model.PayoffType.values()) {
                final int payoffColumn = FIRST_PAYOFF_COL_INDEX + i * Model.PayoffType.values().length + payoffType.ordinal();
                ExcelTools.getOrCreateCell(sheet, 0, payoffColumn).setCellValue(payoffType.getPrefix() + interventionIRI);
            }
        }
        final int firstParamIndex = FIRST_PAYOFF_COL_INDEX + Model.PayoffType.values().length * nInterventions;
        HTAExcelModelFactory.CommonNamedRanges.COL_DIST1.setValue(workbook, CellReference.convertNumToColString(firstParamIndex));
        int paramIndex = firstParamIndex;
        final SingleDiseaseAndPopulationModel model = modelBuilder.getModel();
        // Iterate through all parameters in the model and insert them into the parameters sheet
        for (ParameterGroup type : ParameterGroup.values()) {
            for (ParameterWrapper param : model.getParameters(type)) {
                if (ParameterNatureType.SECOND_ORDER.equals(param.getNature())) {
                    ExcelTools.getOrCreateCell(sheet, 0, paramIndex).setCellValue(param.getShortName());
                    setParamFormulas(paramIndex++);
                }
            }
        }
        HTAExcelModelFactory.CommonNamedRanges.COL_DISTN.setValue(workbook, CellReference.convertNumToColString(paramIndex - 1));
    }

    /**
     * Sets the formulas for the parameters in the Process sheet.
     * @param column The column index where the formulas will be set.
     */
    private void setParamFormulas(int column) {
        for (UsefulFormulas formula : UsefulFormulas.values()) {
            Cell cell = ExcelTools.getOrCreateCell(sheet, formula.getRowOffset(), column);
            cell.setCellFormula(formula.getFormula(column));
        }
        ExcelTools.getOrCreateCell(sheet, FIRST_SIMULATION_ROW_INDEX - 1, column).setCellFormula(ExcelFormulaLibrary.INDIRECT.getFormula(CellReference.convertNumToColString(column) + "1"));
    }
}
