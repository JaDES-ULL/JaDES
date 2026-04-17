package es.ull.simulation.hta.osdi.decisiontree.excelport;

import java.util.Optional;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFRow;

import es.ull.simulation.hta.MalformedSimulationModelException;
import es.ull.simulation.hta.osdi.OSDiLogger;
import es.ull.simulation.hta.osdi.decisiontree.SingleDiseaseAndPopulationModel;
import es.ull.simulation.hta.osdi.decisiontree.excelport.HTAExcelModelFactory.CommonNamedRanges;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.ExpressionLanguageType;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper;
import es.ull.simulation.hta.osdi.ontology.ProbabilisticExpressionWrapper;
import es.ull.simulation.hta.osdi.ontology.ProbabilityDistributionExpressionType;
import es.ull.simulation.hta.osdi.ontology.UncertaintyCharacterization;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper.CalculatedParameterData;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper.DeterministicParameterData;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper.SecondOrderUncertaintyParameterData;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper.SpecificInformationForCost;
import es.ull.simulation.hta.params.ParameterGroup;
import es.ull.simulation.utils.ExcelFormula;
import es.ull.simulation.utils.ExcelFormulaLibrary;
import es.ull.simulation.utils.ExcelTools;

/**
 * A wrapper class for the parameters sheet in an Excel file for HTA decision tree models.
 * This class encapsulates the necessary methods and properties to interact with the parameters sheet.
 */
public class ParamSheet implements SheetWrapper {
    /**
	 * The logger for this class
	 */
	private final static OSDiLogger log = OSDiLogger.getLogger(ParamSheet.class);

    private enum ProbabilisticFormulas {
        BETA(ProbabilityDistributionExpressionType.BETA, 2, ExcelFormulaLibrary.BETA_INV),
        GAMMA(ProbabilityDistributionExpressionType.GAMMA, 2, ExcelFormulaLibrary.GAMMA_INV),
        NORMAL(ProbabilityDistributionExpressionType.NORMAL, 2, ExcelFormulaLibrary.NORMAL_INV),
        UNIFORM(ProbabilityDistributionExpressionType.UNIFORM, 2, ExcelFormulaLibrary.UNIFORM_INV),
        EXPONENTIAL(ProbabilityDistributionExpressionType.EXPONENTIAL, 1, ExcelFormulaLibrary.EXPONENTIAL_INV),
        POISSON(ProbabilityDistributionExpressionType.POISSON, 1, ExcelFormulaLibrary.POISSON_INV),
        BERNOULLI(ProbabilityDistributionExpressionType.BERNOULLI, 1, ExcelFormulaLibrary.BERNOULLI_INV);
        
        private final static TreeMap<ProbabilityDistributionExpressionType, ProbabilisticFormulas> reverseFormula = new TreeMap<>();
        static {
            for (ProbabilisticFormulas formula : ProbabilisticFormulas.values()) {
                reverseFormula.put(formula.oSDiDistribution, formula);
            }
        }
        private int nParameters;
        private ExcelFormula template;
        private ProbabilityDistributionExpressionType oSDiDistribution;
        private ProbabilisticFormulas(ProbabilityDistributionExpressionType oSDiDistribution, int nParameters, ExcelFormula template) {
            this.nParameters = nParameters;
            this.template = template;
            this.oSDiDistribution = oSDiDistribution;
        }

        public String getFormula(int nRow) {
            Object[] parameters = new String[nParameters + 1];
            parameters[0] = ExcelFormulaLibrary.RAND.getFormula();
            for (int i = 0; i < nParameters; i++) {
                parameters[i + 1] = new CellReference(nRow, ColumnNames.DIST_PARAM1.getColIndex() + i).formatAsString();
            }
            return template.getFormula(parameters);
        }

        /**
         * Returns the ProbabilisticFormulas associated with the given OSDi distribution.
         * @param oSDiDistribution The OSDi distribution to get the ProbabilisticFormulas for.
         * @return The ProbabilisticFormulas associated with the given OSDi distribution.
         */
        public static ProbabilisticFormulas fromOSDiDistribution(ProbabilityDistributionExpressionType oSDiDistribution) {
            return reverseFormula.get(oSDiDistribution);
        }
    }

    public static final String SHEET_NAME = "Parameters";
    private static final String PARAMETER_ROW_RANGE = "parameterRow";
    enum ColumnNames {
        NAME(1), // Column B in Excel
        GROUP(2), // Column C in Excel
        NATURE(3), // Column D in Excel
        LIVE_VALUE(5), // Column F in Excel
        PROBABILISTIC_VALUE(6), // Column G in Excel
        DETERMINISTIC_VALUE(7), // Column H in Excel
        STD_ERROR(8), // Column I in Excel
        PROBABILISTIC_DISTRIBUTION(9), // Column J in Excel
        DIST_PARAM1(10), // Column K in Excel
        DIST_PARAM2(11), // Column L in Excel
        YEAR(13), // Column N in Excel
        DESCRIPTION(14), // Column O in Excel
        SOURCE(15); // Column P in Excel
        private final int colIndex;
        private ColumnNames(int colIndex) {
            this.colIndex = colIndex;
        }
        public int getColIndex() {
            return colIndex;
        }
        public Cell getOrCreateCell(XSSFRow row) {
            return ExcelTools.getOrCreateCell(row, colIndex);
        }
    }
    /** The first row of the parameters sheet where it can be added a new parameter name */
    private static final int[] PARAMS_SHEET_FIRST_CELL = {6,1}; // Row 8, Column 1 (B9) in Excel
    /**
     * The sheet that contains the parameters for the HTA model.
     */
    private final Sheet sheet;
    /**
     * The workbook that contains the parameters sheet.
     */
    private final Workbook workbook;
    /**
     * The first available row in the parameters sheet where a new parameter can be added.
     */
    private int firstAvailableParamRow;
    /**
     * The HTAExcelModelBuilder that contains the workbook and styles.
     */
    private final HTAExcelModelFactory modelBuilder;

    /**
     * Constructor to initialize the ExcelParamSheet with a given workbook.
     * This constructor checks if the workbook contains a sheet named "Parameters" and initializes the parameters sheet.
     * @param workbook The workbook containing the parameters sheet.
     * @throws MalformedSimulationModelException If the workbook does not contain a sheet named "Parameters".
     */
    public ParamSheet(HTAExcelModelFactory modelBuilder) throws MalformedSimulationModelException {
        this.modelBuilder = modelBuilder;
        this.workbook = modelBuilder.getWorkbook();
        // Initialize the parameters sheet with the provided workbook
        this.sheet = workbook.getSheet(SHEET_NAME);
        if (this.sheet == null) {
            throw new MalformedSimulationModelException("The workbook must contain a sheet named '" + SHEET_NAME + "'.");
        }
    }

    /**
     * Fills the parameters sheet with the parameters from the specified OSDiTreeModel.
     * This method iterates through all the parameters in the model and inserts them into the parameters sheet.
     * @throws MalformedSimulationModelException If the model is null or if there is an issue with the model's parameters.
     * @throws MalformedOSDiModelException If there is an issue with the OSDi model, such as missing parameters or incorrect data types.
     * @throws UnsupportedOSDiFeatureException If the OSDi model contains features that are not supported by the decision tree.
     */
    @Override
    public void updateSheet() throws MalformedSimulationModelException, MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        SingleDiseaseAndPopulationModel model = modelBuilder.getModel();
        // Reset the first available row to the first cell of the parameters sheet
        this.firstAvailableParamRow = PARAMS_SHEET_FIRST_CELL[0];
        // Iterate through all parameters in the model and insert them into the parameters sheet
        for (ParameterGroup type : ParameterGroup.values()) {
            for (ParameterWrapper param : model.getParameters(type)) {
                insertParameter(param, type);
            }
        }
    }

    /**
     * Inserts a parameter into the parameters sheet of the Excel workbook.
     * This method creates a new row in the parameters sheet and populates it with the details of the given parameter.
     * 
     * @param param The parameter to be inserted into the parameters sheet.
     * @param group The group to which the parameter belongs, used for categorization in the decision tree.
     * @throws MalformedSimulationModelException If the parameter is null or if there is an issue with the parameter's nature.
     */
    public void insertParameter(ParameterWrapper param, ParameterGroup type) throws MalformedSimulationModelException {
        if (param == null) {
            throw new MalformedSimulationModelException("param cannot be null.");
        }
        ExcelTools.copyCellRange(PARAMETER_ROW_RANGE, sheet, firstAvailableParamRow, PARAMS_SHEET_FIRST_CELL[1]);

        final XSSFRow newRow = (XSSFRow)ExcelTools.getOrCreateRow(sheet, firstAvailableParamRow);
        ColumnNames.NAME.getOrCreateCell(newRow).setCellValue(param.getShortName());
        ColumnNames.GROUP.getOrCreateCell(newRow).setCellValue(type.name());
        ColumnNames.NATURE.getOrCreateCell(newRow).setCellValue(param.getNature().name());
        if (ParameterGroup.COST.equals(type)) {
            SpecificInformationForCost specificInfo = (SpecificInformationForCost) param.getSpecificInformation();
            ColumnNames.YEAR.getOrCreateCell(newRow).setCellValue(specificInfo.year());
        }
        ColumnNames.DESCRIPTION.getOrCreateCell(newRow).setCellValue(param.getDescription().orElse(""));
        ColumnNames.SOURCE.getOrCreateCell(newRow).setCellValue(param.getSource());
        switch (param.getNature()) {
            case SECOND_ORDER:
                SecondOrderUncertaintyParameterData secondOrderData = (SecondOrderUncertaintyParameterData)param.getParameterNatureData();
                UncertaintyCharacterization characterization = secondOrderData.uncertaintyCharacterization();
                ColumnNames.DETERMINISTIC_VALUE.getOrCreateCell(newRow).setCellValue(secondOrderData.expectedValue());
                Optional<ProbabilisticExpressionWrapper> probabilisticExpressionOpt = characterization.getProbabilisticExpression(); 
                String liveValueFormula;
                if (probabilisticExpressionOpt.isPresent()) {
                    ProbabilisticExpressionWrapper probabilisticExpression = probabilisticExpressionOpt.get();
                    ColumnNames.PROBABILISTIC_DISTRIBUTION.getOrCreateCell(newRow).setCellValue(probabilisticExpression.getProbabilityDistribution().name());
                    ColumnNames.DIST_PARAM1.getOrCreateCell(newRow).setCellValue(probabilisticExpression.getParameterValues()[0]);
                    ColumnNames.DIST_PARAM2.getOrCreateCell(newRow).setCellValue(probabilisticExpression.getParameterValues()[1]);
                    ProbabilisticFormulas refFormula = ProbabilisticFormulas.fromOSDiDistribution(probabilisticExpression.getProbabilityDistribution());
                    String formula = refFormula.getFormula(newRow.getRowNum());
                    ColumnNames.PROBABILISTIC_VALUE.getOrCreateCell(newRow).setCellFormula(formula);
                    liveValueFormula = ExcelFormulaLibrary.IF.getFormula(
                            CommonNamedRanges.IS_PROBABILISTIC.getName() + "=0",
                            newRow.getCell(ColumnNames.DETERMINISTIC_VALUE.getColIndex()).getReference(),
                            newRow.getCell(ColumnNames.PROBABILISTIC_VALUE.getColIndex()).getReference());
                }
                else {
                    // TODO: Add comment to state that no proper handling of second order uncertainty was possible
                    liveValueFormula = newRow.getCell(ColumnNames.DETERMINISTIC_VALUE.getColIndex()).getReference();
                }
                if (ParameterGroup.COST.equals(type)) {
                    liveValueFormula = FormulaLibrary.UPDATE_COST.getFormula(liveValueFormula, newRow.getCell(ColumnNames.YEAR.getColIndex()).getReference());
                }
                ColumnNames.LIVE_VALUE.getOrCreateCell(newRow).setCellFormula(liveValueFormula);
                break;
            case DETERMINISTIC:
                DeterministicParameterData deterministicData = (DeterministicParameterData)param.getParameterNatureData();
                ColumnNames.DETERMINISTIC_VALUE.getOrCreateCell(newRow).setCellValue(deterministicData.value());
                ColumnNames.STD_ERROR.getOrCreateCell(newRow).setBlank(); // Standard error is not applicable for deterministic parameters
                ColumnNames.PROBABILISTIC_DISTRIBUTION.getOrCreateCell(newRow).setBlank();
                ColumnNames.PROBABILISTIC_VALUE.getOrCreateCell(newRow).setBlank();
                ColumnNames.DIST_PARAM1.getOrCreateCell(newRow).setBlank();
                ColumnNames.DIST_PARAM2.getOrCreateCell(newRow).setBlank();
                liveValueFormula = newRow.getCell(ColumnNames.DETERMINISTIC_VALUE.getColIndex()).getReference();
                if (ParameterGroup.COST.equals(type)) {
                    liveValueFormula = FormulaLibrary.UPDATE_COST.getFormula(liveValueFormula, newRow.getCell(ColumnNames.YEAR.getColIndex()).getReference());
                }
                ColumnNames.LIVE_VALUE.getOrCreateCell(newRow).setCellFormula(liveValueFormula);
                break;
            case CALCULATED:
                ColumnNames.PROBABILISTIC_DISTRIBUTION.getOrCreateCell(newRow).setBlank();
                ColumnNames.DIST_PARAM1.getOrCreateCell(newRow).setBlank();
                ColumnNames.DIST_PARAM2.getOrCreateCell(newRow).setBlank();
                ColumnNames.DETERMINISTIC_VALUE.getOrCreateCell(newRow).setBlank();
                CalculatedParameterData calculatedData = (CalculatedParameterData)param.getParameterNatureData();
                if (ExpressionLanguageType.EXCEL.equals(calculatedData.expressionLanguage())) {
                    // If the expression language is Excel, we can directly set the formula
                    liveValueFormula = calculatedData.expression();
                } else {
                    ColumnNames.LIVE_VALUE.getOrCreateCell(newRow).setBlank();
                    liveValueFormula = "0.0";
                    log.warn("Calculated parameter " + param.getShortName() + " has an expression language of " + calculatedData.expressionLanguage() + ", which is not supported. The live value cell will be set to 0.0");
                }
                ColumnNames.LIVE_VALUE.getOrCreateCell(newRow).setCellFormula(liveValueFormula);
                break;
            case FIRST_ORDER:
                throw new MalformedSimulationModelException("First order uncertainty cannot be handled from a decision tree: " + param.getNature());
            default:
                throw new MalformedSimulationModelException("Unknown parameter nature: " + param.getNature());
        }
        nameLiveValueCell(newRow);
        firstAvailableParamRow++;   
    }

    /**
     * Names the live value cell in the parameters sheet.
     * This method creates a name for the live value cell based on the parameter name.
     * @param newRow The new row in the parameters sheet where the live value cell is located.
     */
    private void nameLiveValueCell(XSSFRow newRow) {
        ExcelTools.nameRange(sheet, newRow.getRowNum(), ColumnNames.LIVE_VALUE.getColIndex(), newRow.getCell(ColumnNames.NAME.getColIndex()).getStringCellValue(), true);
/*        final Name cellName = workbook.createName();
        cellName.setNameName(ColumnNames.NAME.getOrCreateCell(newRow).getStringCellValue());
        cellName.setRefersToFormula("'" + SHEET_NAME + "'!" + new CellReference(newRow.getRowNum(), ColumnNames.LIVE_VALUE.getColIndex(), true, true).formatAsString());*/
    }

    public int getNParameters() {
        return firstAvailableParamRow - PARAMS_SHEET_FIRST_CELL[0];
    }
}