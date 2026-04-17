package es.ull.simulation.hta.osdi.decisiontree.excelport;

import java.util.ArrayList;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import es.ull.simulation.hta.MalformedSimulationModelException;
import es.ull.simulation.hta.osdi.decisiontree.BranchDestinationNode;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;

/**
 * A wrapper class for the Control sheet of the Excel file that defines a HTA decision tree model.
 * The Control sheet contains the necessary controls and settings for the HTA model.
 */
public class ControlSheet implements SheetWrapper {
    public static final String SHEET_NAME = "Control";
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
     * Constructor to initialize the HTAExcelControlSheet with a workbook and model builder.
     * @param workbook The workbook that contains the model sheet.
     * @param modelBuilder The HTAExcelModelBuilder that contains the workbook and styles.
     */
    public ControlSheet(HTAExcelModelFactory modelBuilder) throws MalformedSimulationModelException {
        this.modelBuilder = modelBuilder;
        this.workbook = modelBuilder.getWorkbook();
        this.sheet = workbook.getSheet(SHEET_NAME);
        if (this.sheet == null) {
            throw new MalformedSimulationModelException("The workbook must contain a sheet named '" + SHEET_NAME + "'.");
        }
    }

    @Override
    public void updateSheet() throws MalformedSimulationModelException, UnsupportedOSDiFeatureException, MalformedOSDiModelException {
        // Update the control sheet with the necessary values
        final ArrayList<BranchDestinationNode> interventions = modelBuilder.getModel().getTree().getSuccessors();
        if (interventions.size() != 2) {
            throw new UnsupportedOSDiFeatureException("The model must contain two interventions. Found: " + interventions.size());
        }
        HTAExcelModelFactory.CommonNamedRanges.STUDY_YEAR.setValue(workbook, modelBuilder.getModel().getStudyYear());
        HTAExcelModelFactory.CommonNamedRanges.DISCOUNT_COSTS.setValue(workbook, modelBuilder.getModel().getDiscountRateForCosts());
        HTAExcelModelFactory.CommonNamedRanges.DISCOUNT_LE.setValue(workbook, modelBuilder.getModel().getDiscountRateForEffects());
        HTAExcelModelFactory.CommonNamedRanges.DISCOUNT_QALE.setValue(workbook, modelBuilder.getModel().getDiscountRateForEffects());
        HTAExcelModelFactory.CommonNamedRanges.INTERVENTION1.setValue(workbook, interventions.get(0).name());
        HTAExcelModelFactory.CommonNamedRanges.INTERVENTION2.setValue(workbook, interventions.get(1).name());
    }
}
