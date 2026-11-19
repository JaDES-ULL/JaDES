package es.ull.simulation.hta.osdi.decisiontree.excelport;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Set;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import es.ull.simulation.hta.MalformedSimulationModelException;
import es.ull.simulation.hta.osdi.decisiontree.SingleDiseaseAndPopulationModel;
import es.ull.simulation.hta.osdi.decisiontree.factories.AbstractModelFactory;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.EffectWrapper;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper;
import es.ull.simulation.utils.ExcelTools;

/**
 * A wrapper class for the HTA decision tree model to be implemented in an Excel file.
 * This class encapsulates the necessary methods and properties to interact with the HTA model.
 */
public class HTAExcelModelFactory {
    private static final String PATH_TO_TEMPLATE = "/model_template.xlsm";
    private static final String SHEET_TEMPLATES = "Style templates";
    public static enum CommonNamedRanges {
        CPI("CPI"), // The table with Consumer Price Index, used for cost adjustments
        N_SIM("nSim"), // The number of simulations to be performed
        STUDY_YEAR("studyYear"), // The year of the study
        INTERVENTION1("intervention1"),
        INTERVENTION2("intervention2"),
        COL_DIST1("colDist1"),
        COL_DISTN("colDistN"),
        UNIT_COST("unitCost"),
        UNIT_LE("unitLE"),
        UNIT_QALE("unitQALE"),
        IS_PROBABILISTIC("isProbabilistic"),
        DISCOUNT_COSTS("discountCosts"),
        DISCOUNT_LE("discountLE"),
        DISCOUNT_QALE("discountQALE"),
        TIME_HORIZON("timeHorizon");    // Limits the life years of each leaf (hasTimeHorizon of the experiment); lifetime if not declared
        private final String internalName;
        private CommonNamedRanges(String internalName) {
            this.internalName = internalName;
        }
        
        public String getName() {
            return internalName;
        }
        public void setValue(Workbook workbook, String value) {
            ExcelTools.setValue(workbook, this.getName(), value);
        }
        public void setValue(Workbook workbook, double value) {
            ExcelTools.setValue(workbook, this.getName(), value);
        }
    }
    /**
     * The Excel file handler for an xlsm workbook.
     */
    private final Workbook workbook;
    /**
     * A wrapper for the parameters sheet in the Excel file.
     */
    private final ParamSheet paramsSheet;
    /**
     * A wrapper for the model sheet in the Excel file.
     */
    private final ModelSheet modelSheet;
    /**
     * A wrapper for the control sheet in the Excel file.
     */
    private final ControlSheet controlSheet;
    /**
     * A wrapper for the process sheet in the Excel file.
     */
    private final ProcessSheet processSheet;
    /**
     * The HTA decision tree model. It must be a model with only one population and one disease.
     */
    private final SingleDiseaseAndPopulationModel model;
    /**
     * The depth of the decision tree.
     * This is used to determine how many levels of decisions are present in the tree.
     */
    private final int treeDepth;

    /**
     * Creates the workbook of the HTA model from the template of the generator, and fills its sheets. The workbook is kept in memory: it is
     * written to a file by {@link #build(String, SingleDiseaseAndPopulationModel)}.
     * @param model The OSDiTreeModel to be used.
     * @throws IOException If there is an error reading the template.
     * @throws UnsupportedOSDiFeatureException
     * @throws MalformedOSDiModelException
     */
    private HTAExcelModelFactory(SingleDiseaseAndPopulationModel model) throws IOException, MalformedSimulationModelException, MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        this(model, openTemplate());
    }

    /**
     * Opens the template of the generator.
     * @return The stream of the template; the caller must close it
     */
    private static InputStream openTemplate() {
        final InputStream in = HTAExcelModelFactory.class.getResourceAsStream(PATH_TO_TEMPLATE);
        if (in == null) {
            throw new IllegalStateException("Resource not found: " + PATH_TO_TEMPLATE);
        }
        return in;
    }

    /**
     * Creates the workbook of the HTA model from a template, and fills its sheets. The workbook is kept in memory.
     * @param model The OSDiTreeModel to be used.
     * @param template The template; it is closed after reading it.
     * @throws IOException If there is an error reading the template.
     * @throws UnsupportedOSDiFeatureException
     * @throws MalformedOSDiModelException
     */
    private HTAExcelModelFactory(SingleDiseaseAndPopulationModel model, InputStream template) throws IOException, MalformedSimulationModelException, MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        try (InputStream in = template) {
            this.workbook = new XSSFWorkbook(in);
        }
        try {
            this.model = model;
            this.treeDepth = model.getDepth();
            this.paramsSheet = new ParamSheet(this);
            this.modelSheet = new ModelSheet(this);
            this.controlSheet = new ControlSheet(this);
            this.processSheet = new ProcessSheet(this);
            this.paramsSheet.updateSheet();
            this.modelSheet.updateSheet();
            this.controlSheet.updateSheet();
            this.processSheet.updateSheet();
            // Remove the template sheet after copying styles
            this.workbook.removeSheetAt(this.workbook.getSheetIndex(SHEET_TEMPLATES));
        } catch (MalformedOSDiModelException | MalformedSimulationModelException | UnsupportedOSDiFeatureException | RuntimeException e) {
            workbook.close();
            throw e;
        }
    }

    /**
     * Writes the workbook to the specified Excel file path and closes the workbook.
     * @param excelFilePath The path to the Excel file.
     * @throws IOException If there is an error writing to the Excel file.
     */
    private void writeAndClose(String excelFilePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(excelFilePath)) {
            workbook.setForceFormulaRecalculation(true);
            workbook.write(fos);
        }
        finally {
            workbook.close();
        }
    }

    /**
     * Returns the workbook that contains the HTA model.
     * @return The workbook that contains the HTA model.
     */
    public Workbook getWorkbook() {
        return workbook;
    }

    /**
     * Returns the wrapper for the parameters sheet in the Excel file.
     * @return The wrapper for the parameters sheet in the Excel file.
     */
    public ParamSheet getParamsSheet() {
        return paramsSheet;
    }

    /**
     * Returns the DecisionTreeModel that contains the HTA decision tree model.
     * @return The DecisionTreeModel that contains the HTA decision tree model.
     */
    public SingleDiseaseAndPopulationModel getModel() {
        return model;
    }

    /**
     * Returns the depth of the decision tree.
     * @return The depth of the decision tree.
     */
    public int getTreeDepth() {
        return treeDepth;
    }
    
    /**
     * Returns the wrapper for the model sheet in the Excel file.
     * @return The wrapper for the model sheet in the Excel file.
     */
    public ModelSheet getModelSheet() {
        return modelSheet;
    }

    /**
     * Builds the Excel formula of a parameter, modified by all the effects of a path of the decision tree that affect it. The effects are
     * applied in the order defined by {@link EffectWrapper#sortForParameter(es.ull.simulation.hta.osdi.ontology.IModelItemWrapper, java.util.Collection)}
     * (replace, multiply, subtract), each one with the operation defined by {@link EffectWrapper#getOperation(es.ull.simulation.hta.osdi.ontology.IModelItemWrapper)}:
     * <ul>
     * <li>replace: m;</li>
     * <li>multiply: (x * m);</li>
     * <li>rate ratio (relative risk or hazard ratio on a probability): (1 - (1 - x) ^ m);</li>
     * <li>hazard ratio on an exponential time to event: (x / m);</li>
     * <li>odds ratio: (x * m / (1 - x + x * m));</li>
     * <li>subtract: (x - m).</li>
     * </ul>
     * A synthetic probability derived from a time to event T within a period (see
     * {@link AbstractModelFactory#getProbabilityParameterFromTimeToEvent(ParameterWrapper, Optional, double)}) is not written in the Excel file: its
     * formula is (1 - EXP(-period / T')), where T' is the formula of T modified by the effects of the path, and the period is the time horizon,
     * the evaluation time of an outcome (also modified by the effects of the path) or 1 year. For instance, a hazard ratio m on T gives
     * (1 - EXP(-H / (T / m))).
     * @param param The parameter
     * @param effects The effects of the path of the decision tree (only those that affect the parameter are used)
     * @return The Excel formula for the parameter
     * @throws MalformedOSDiModelException If more than one effect replaces the parameter
     */
    public static String getParameterTextFormula(ParameterWrapper param, Set<EffectWrapper> effects) throws MalformedOSDiModelException {
        final Optional<AbstractModelFactory.TimeToEventProbability> tte = AbstractModelFactory.getTimeToEventProbability(param);
        String formula = param.getShortName();
        if (tte.isPresent()) {
            final String period = tte.get().periodParameter().isPresent() ? getParameterTextFormula(tte.get().periodParameter().get(), effects) : Double.toString(tte.get().periodValue());
            formula = "(1 - EXP(-" + period + " / " + getParameterTextFormula(tte.get().time(), effects) + "))";
        }
        for (EffectWrapper effect : EffectWrapper.sortForParameter(param, effects)) {
            final String magnitude = effect.getEffectMagnitude().getShortName();
            switch (effect.getOperation(param)) {
                case REPLACE:
                    formula = magnitude;
                    break;
                case MULTIPLY:
                    formula = "(" + formula + " * " + magnitude + ")";
                    break;
                case RATE_RATIO:
                    formula = "(1 - (1 - " + formula + ") ^ " + magnitude + ")";
                    break;
                case TIME_HAZARD_RATIO:
                    formula = "(" + formula + " / " + magnitude + ")";
                    break;
                case ODDS_RATIO:
                    formula = "(" + formula + " * " + magnitude + " / (1 - " + formula + " + " + formula + " * " + magnitude + "))";
                    break;
                case SUBTRACT:
                    formula = "(" + formula + " - " + magnitude + ")";
                    break;
                default:
                    throw new IllegalStateException("Unknown parameter modification type: " + effect.getOperation(param));
            }
        }
        return formula;
    }

    /**
     * Generates the Excel model for the HTA decision tree.
     * @throws MalformedOSDiModelException 
     * @throws UnsupportedOSDiFeatureException 
     */
    public static void build(String excelFilePath, SingleDiseaseAndPopulationModel model) throws IOException, MalformedSimulationModelException, MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        new HTAExcelModelFactory(model).writeAndClose(excelFilePath);
    }

    /**
     * Generates the Excel model for the HTA decision tree in memory, without writing it to a file (e.g., to evaluate its formulas). Writing the file
     * is the most expensive step of {@link #build(String, SingleDiseaseAndPopulationModel)}. The caller must close the workbook.
     * @param model The decision tree model
     * @return The workbook with the Excel model, ready to evaluate its formulas
     * @throws IOException If there is an error reading the template
     * @throws MalformedOSDiModelException If the OSDi model is malformed
     * @throws UnsupportedOSDiFeatureException If the OSDi model uses features not supported by decision trees
     */
    public static Workbook createWorkbook(SingleDiseaseAndPopulationModel model) throws IOException, MalformedSimulationModelException, MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        return new HTAExcelModelFactory(model).getWorkbook();
    }

    /**
     * Generates the Excel model for the HTA decision tree in memory from another template, without writing it to a file. The template must contain
     * the sheets and named ranges that the generator fills (Control, Style templates, Parameters, Model, Process and the CPI table); e.g., the tests
     * use a copy of the template of the generator without the sheets that do not take part in the deterministic results, since reading the
     * template is the most expensive step of {@link #createWorkbook(SingleDiseaseAndPopulationModel)}. The caller must close the workbook.
     * @param model The decision tree model
     * @param template The template; it is closed after reading it
     * @return The workbook with the Excel model, ready to evaluate its formulas
     * @throws IOException If there is an error reading the template
     * @throws MalformedOSDiModelException If the OSDi model is malformed
     * @throws UnsupportedOSDiFeatureException If the OSDi model uses features not supported by decision trees
     */
    public static Workbook createWorkbook(SingleDiseaseAndPopulationModel model, InputStream template) throws IOException, MalformedSimulationModelException, MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        return new HTAExcelModelFactory(model, template).getWorkbook();
    }

    /**
     * Returns the template of the generator, to be used, e.g., as the base of another template (see {@link #createWorkbook(SingleDiseaseAndPopulationModel, InputStream)}).
     * @return The stream of the template; the caller must close it
     */
    public static InputStream getTemplate() {
        return openTemplate();
    }
}
