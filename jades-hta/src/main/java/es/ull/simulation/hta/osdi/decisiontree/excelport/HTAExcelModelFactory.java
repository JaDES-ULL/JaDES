package es.ull.simulation.hta.osdi.decisiontree.excelport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import es.ull.simulation.hta.MalformedSimulationModelException;
import es.ull.simulation.hta.osdi.decisiontree.SingleDiseaseAndPopulationModel;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.EffectWrapper;
import es.ull.simulation.hta.osdi.ontology.IModelItemWrapper;
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
        TIME_HORIZON("timeHorizon");    // In case of trees, this is useful only to prepare the model for budget impact analysis
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
     * Constructor to initialize the ExcelModelWrapper with a new workbook.
     * IMPORTANT: This constructor ALWAYS creates a new workbook that overrides the existing one.
     * @param excelFilePath The path to the Excel file.
     * @param model The OSDiTreeModel to be used.
     * @throws IOException If there is an error reading the Excel file.
     * @throws UnsupportedOSDiFeatureException 
     * @throws MalformedOSDiModelException 
     */
    private HTAExcelModelFactory(String excelFilePath, SingleDiseaseAndPopulationModel model) throws IOException, MalformedSimulationModelException, MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        final Path target = Paths.get(excelFilePath);

        try (InputStream in = getClass().getResourceAsStream(PATH_TO_TEMPLATE)) {
            if (in == null) {
                throw new IllegalStateException("Resource not found: " + PATH_TO_TEMPLATE);
            }
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }        
        final FileInputStream fis = new FileInputStream(new File(excelFilePath));
        try {
            this.workbook = new XSSFWorkbook(fis);
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
        } catch (IOException | MalformedOSDiModelException | MalformedSimulationModelException | UnsupportedOSDiFeatureException e) {
            throw e;
        } finally {
            fis.close();
        }
    }

    /**
     * Writes the workbook to the specified Excel file path and closes the workbook.
     * @param excelFilePath The path to the Excel file.
     * @throws IOException If there is an error writing to the Excel file.
     */
    private void writeAndClose(String excelFilePath) throws IOException {
        FileOutputStream fos = new FileOutputStream(excelFilePath);
        workbook.setForceFormulaRecalculation(true);
        workbook.write(fos);
        fos.close();
        workbook.close();
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
     * Builds an string formula for a parameter in the decision tree. It takes into account the modifications made to the parameter in the decision tree.
     * @param paramIRI The IRI of the parameter to be modified.
     * @param node The decision tree node where the parameter is located.
     * @return The string formula for the parameter.
     */
    public static String getParameterTextFormula(ParameterWrapper param, Set<EffectWrapper> effects) {
        EffectWrapper modifParam = null;
        for (EffectWrapper effect : effects) {
            for (IModelItemWrapper modifiedParam : effect.getModifiedItems()) {
                if (modifiedParam.compareTo(param) == 0) {
                    modifParam = effect;
                    break;
                }
            }
        }
        // If the successor is a parameterized component, we need to get the modified probability
        if (modifParam == null) {
            return param.getShortName(); // No modification, return the original IRI
        }
        switch (modifParam.getMagnitudeType()) {
            case DIFF:
                return param.getShortName() + " - " + modifParam.getEffectMagnitude().getShortName();
            case FACTOR:
                return param.getShortName() + " * " + modifParam.getEffectMagnitude().getShortName();
            case SET:
                return modifParam.getEffectMagnitude().getShortName();
            default:
                throw new IllegalStateException("Unknown parameter modification type: " + modifParam.getMagnitudeType());
        }
    }

    /**
     * Generates the Excel model for the HTA decision tree.
     * @throws MalformedOSDiModelException 
     * @throws UnsupportedOSDiFeatureException 
     */
    public static void build(String excelFilePath, SingleDiseaseAndPopulationModel model) throws IOException, MalformedSimulationModelException, MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        HTAExcelModelFactory excelWrapper = null;
        try {
            excelWrapper = new HTAExcelModelFactory(excelFilePath, model);
        } catch (IOException | MalformedOSDiModelException | MalformedSimulationModelException | UnsupportedOSDiFeatureException e) {
            throw e;
        } finally {
            // Ensure the workbook is written and closed properly
            if (excelWrapper != null) {
                excelWrapper.writeAndClose(excelFilePath);
            }
        }
    }
}
