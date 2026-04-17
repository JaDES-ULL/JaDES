package es.ull.simulation.hta.osdi.decisiontree.excelport;

import java.util.ArrayList;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import es.ull.simulation.hta.MalformedSimulationModelException;
import es.ull.simulation.hta.osdi.decisiontree.ChoiceNode;
import es.ull.simulation.hta.osdi.decisiontree.BranchDestinationNode;
import es.ull.simulation.hta.osdi.decisiontree.Model;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.EffectWrapper;
import es.ull.simulation.utils.ExcelFormulaLibrary;
import es.ull.simulation.utils.ExcelTools;

/**
 * A wrapper class for the Model Sheet in the HTA decision tree model to be implemented in an Excel file.
 * This class encapsulates the necessary methods and properties to interact with the model sheet.
 */
public class ModelSheet implements SheetWrapper {
    public static final String SHEET_NAME = "Model";
    private static final String PAYOFF_TITLE_RANGE = "payoffTitles";
    private static final String SUMMARY_TITLE_RANGE = "summaryTitles";
    private static final String SUMMARY_RANGE = "summaryRow";
    private enum Styles {
        TREE_NODE("stTreeNode"), 
        TREE_BRANCH("stTreeBranch"), 
        TREE_CORNER("stTreeCorner"), 
        PROBABILITY("stProbability"), 
        COST("stCost"), 
        LIFE_YEARS("stEffect1"), 
        QALY("stEffect2");
        private final String styleName;
        private Styles(String styleName) {
            this.styleName = styleName;
        }
        public CellStyle getStyle(Workbook workbook) {
            return ExcelTools.getStyleFromNamedRange(workbook, styleName);
        }
    }
    /**
     * The row and column where the summary starts. More specifically, the row where the first cell of the title of the summary will be placed.
     */
    private final int[] SUMMARY_CELL = {1, 1}; 
    /**
     * Column where choices should be placed.
     */
    private final int CHOICE_COLUMN = 3; 
    /**
     * Columns between nodes in the decision tree.
     */
    private final int COLS_BETWEEN_NODES = 3; 
    /**
     * Rows between nodes in the decision tree.
     */
    private final int ROWS_BETWEEN_NODES = 3;  
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
     * The columnn index where the payoff will be placed in the Excel sheet.
     */
    private final int payoffColumn;
    /**
     * The first row where the tree starts drawing
     */
    private final int firstTreeRow;

    /**
     * Constructor to initialize the ModelSheet with a given HTAExcelModelBuilder.
     * @param modelBuilder The HTAExcelModelBuilder that contains the workbook and styles.
     * @throws MalformedSimulationModelException 
     * @throws UnsupportedOSDiFeatureException 
     * @throws MalformedOSDiModelException 
     */
    public ModelSheet(HTAExcelModelFactory modelBuilder) throws MalformedSimulationModelException, MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        this.modelBuilder = modelBuilder;
        this.workbook = modelBuilder.getWorkbook();
        // Initialize the model sheet with the provided workbook
        this.sheet = workbook.getSheet(SHEET_NAME);
        if (this.sheet == null) {
            throw new MalformedSimulationModelException("The workbook must contain a sheet named '" + SHEET_NAME + "'.");
        }
        // Set the first row where the tree starts drawing
        final int choices = modelBuilder.getModel().getTree().getSuccessors().size();
        // Set the first row after the summary and the choice nodes
        this.firstTreeRow = SUMMARY_CELL[0] + choices + 2; 
        // Prepare the summary area in the Excel sheet
        ExcelTools.copyCellRange(SUMMARY_TITLE_RANGE, sheet, SUMMARY_CELL[0], SUMMARY_CELL[1]);
        for (int i = 0; i < choices; i++) {
            ExcelTools.copyCellRange(SUMMARY_RANGE, sheet, SUMMARY_CELL[0] + i + 1, SUMMARY_CELL[1]);
        }
        // Prepares the payoff area in the Excel sheet
        this.payoffColumn = modelBuilder.getTreeDepth() * 3 + CHOICE_COLUMN; // Calculate the column index for the payoff based on the tree depth
        ExcelTools.copyCellRange(PAYOFF_TITLE_RANGE, sheet, firstTreeRow - 3, payoffColumn);
    }
    
    /**
     * Draws the decision tree in the model sheet starting from the root node.
     * It iterates through the successors of the root node and draws each node in the Excel sheet.
     * @throws MalformedSimulationModelException 
     * @throws MalformedOSDiModelException 
     * @throws UnsupportedOSDiFeatureException
     */
    @Override
    public void updateSheet() throws MalformedSimulationModelException, MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        final ChoiceNode root = modelBuilder.getModel().getTree();
        if (root == null) {
            throw new MalformedSimulationModelException("The model must have a root node.");
        }

        int row = firstTreeRow;
        int column = CHOICE_COLUMN;
        Row nodesRow = ExcelTools.getOrCreateRow(sheet, row);
        final ArrayList<BranchDestinationNode> successors = root.getSuccessors();
        if (successors.isEmpty()) {
            throw new MalformedSimulationModelException("Choice nodes must have at least one branch.");
        }
        // Draw the choice node in the specified row and column
        Cell choiceCell = nodesRow.createCell(column);
        BranchDestinationNode branch = successors.get(0);
        drawNodeText(choiceCell, branch.name());
        // Apply the style also to the cell previous to the first choice node
        ExcelTools.applyStyleToRange(sheet, Styles.TREE_NODE.getStyle(workbook), row, row, column - 2, column - 2);
        int oldRow = row;
        PayoffWrapper payoffWrapper = new PayoffWrapper(modelBuilder.getModel().getOSDiWrapper());
        row = continueTreeDrawing(branch, row, column, payoffWrapper);
        drawSummary(0, branch.name(), oldRow, row - 3);
        for (int i = 1; i < successors.size(); i++) {
            nodesRow = sheet.getRow(row);
            branch = successors.get(i);
            // Draw the choice node in the specified row and column
            choiceCell = nodesRow.createCell(column);
            drawNodeText(choiceCell, branch.name());
            drawTreeConnection(oldRow + 1, row, column - 1);
            oldRow = row;
            payoffWrapper = new PayoffWrapper(modelBuilder.getModel().getOSDiWrapper());
            row = continueTreeDrawing(branch, row, column, payoffWrapper);
            drawSummary(i, branch.name(), oldRow, row - 3);
        }
    }

    /**
     * Continues the drawing of the decision tree by processing the next branch destination component.
     * It checks the type of the branch and calls the appropriate method to draw the node.
     * @param branch The branch destination component to be processed.
     * @param row The current row index where the node will be drawn.
     * @param column The column index where the node will be drawn.
     * @param payoffWrapper The wrapper containing payoff information for the node.
     * @return The row index available to draw the next branch of the tree.
     * @throws MalformedOSDiModelException
     * @throws UnsupportedOSDiFeatureException
     * @throws MalformedSimulationModelException
     */
    private int continueTreeDrawing(BranchDestinationNode branch, int row, int column, PayoffWrapper payoffWrapper) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException, MalformedSimulationModelException {
        if (!branch.isPayoff()) {
            row = drawProbabilityNode(branch, row, column + COLS_BETWEEN_NODES, payoffWrapper);
        } else {
            row = drawPayoffNode(branch, row, column, payoffWrapper);
        }
        return row;
    }

    /**
     * Draws the summary section in the Excel sheet for a specific intervention.
     * @param interventionIndex The index of the intervention.
     * @param interventionName The name of the intervention.
     * @param firstRow The first row that contains payoffs related to the intervention.
     * @param lastRow The last row that contains payoffs related to the intervention.
     * @throws UnsupportedOSDiFeatureException 
     * @throws MalformedSimulationModelException 
     * @throws MalformedOSDiModelException 
     */
    private void drawSummary(int interventionIndex, String interventionName, int firstRow, int lastRow) throws MalformedOSDiModelException, MalformedSimulationModelException, UnsupportedOSDiFeatureException {
        ExcelTools.getOrCreateCell(sheet, SUMMARY_CELL[0] + 1 + interventionIndex, SUMMARY_CELL[1]).setCellValue(interventionName);
        ExcelTools.getOrCreateCell(sheet, SUMMARY_CELL[0] + 1 + interventionIndex, SUMMARY_CELL[1] + 1).setCellFormula("SUM(" + new CellReference(firstRow, payoffColumn, true, true).formatAsString() + ":" + new CellReference(lastRow, payoffColumn, true, true).formatAsString() + ")=1");
        for (Model.PayoffType payoffType : Model.PayoffType.values()) {
            int payoffIndex = payoffType.ordinal();
            ExcelTools.getOrCreateCell(sheet, SUMMARY_CELL[0] + 1 + interventionIndex, SUMMARY_CELL[1] + 2 + payoffIndex).setCellFormula("SUM(" + new CellReference(firstRow, payoffColumn + 1 + payoffIndex, true, true).formatAsString() + ":" + new CellReference(lastRow, payoffColumn + 1 + payoffIndex, true, true).formatAsString() + ")");
            ExcelTools.nameRange(sheet, SUMMARY_CELL[0] + 1 + interventionIndex, SUMMARY_CELL[1] + 2 + payoffIndex, payoffType.getPrefix() + modelBuilder.getModel().getTree().getSuccessors().get(interventionIndex).name(), true);
        }
    }
   
    /**
     * Draws a probability node in the model sheet.
     * It creates a row for the node and its successors, draws the node text, and sets the probability formula for each successor.
     * @param node The probability node to be drawn.
     * @param row The row index where the node will be drawn.
     * @param column The column index where the node will be drawn.
     * @param payoffWrapper The wrapper containing payoff information for the node.
     * @return The row index available to draw the next branch of the tree.
     * @throws MalformedOSDiModelException
     * @throws UnsupportedOSDiFeatureException
     * @throws MalformedSimulationModelException 
     */
    private int drawProbabilityNode(BranchDestinationNode node, int row, int column, PayoffWrapper payoffWrapper) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException, MalformedSimulationModelException {
        payoffWrapper.updateWithTreeNode(node);
        final ArrayList<BranchDestinationNode> successors = node.getSuccessors();
        Row nodesRow = ExcelTools.getOrCreateRow(sheet, row);
        Row valuesRow = ExcelTools.getOrCreateRow(sheet, row + 1);
        ArrayList<Integer> rowsUsed = new ArrayList<>();
        for (BranchDestinationNode successor : successors) {
            drawNodeText(nodesRow.createCell(column), successor.name());
            drawProbabilityFormula(valuesRow, column, successor, payoffWrapper.getEffects());
            if (!rowsUsed.isEmpty()) {
                drawTreeConnection(rowsUsed.get(rowsUsed.size() - 1), row, column - 1);
            }
            rowsUsed.add(row + 1);
            PayoffWrapper newPayoffWrapper = new PayoffWrapper(payoffWrapper);
            newPayoffWrapper.addProbability(new CellReference(row + 1, column, true, true));
            row = continueTreeDrawing(successor, row, column, newPayoffWrapper);
            nodesRow = ExcelTools.getOrCreateRow(sheet, row);
            valuesRow = ExcelTools.getOrCreateRow(sheet, row + 1);
        }
        if (node.getDefaultSuccessor() != null) {
            drawNodeText(nodesRow.createCell(column), node.getDefaultSuccessor().name());
            drawProbabilityFormula(valuesRow, column, rowsUsed);
            if (!rowsUsed.isEmpty()) {
                drawTreeConnection(rowsUsed.get(rowsUsed.size() - 1), row, column - 1);
            }
            PayoffWrapper newPayoffWrapper = new PayoffWrapper(payoffWrapper);
            newPayoffWrapper.addProbability(new CellReference(row + 1, column, true, true));
            row = continueTreeDrawing(node.getDefaultSuccessor(), row, column, newPayoffWrapper);
            nodesRow = ExcelTools.getOrCreateRow(sheet, row);
            valuesRow = ExcelTools.getOrCreateRow(sheet, row + 1);
        }
        return row;
    }

    /**
     * Draws all the elements related to a payoff node in the Excel sheet.
     * It creates the necessary cells for the payoff node and sets the formulas for the probability, cost, life years, and QALY.
     * @param payoff The payoff node to be drawn.
     * @param row The row index where the payoff node will be drawn.
     * @param column The column index where the payoff node will be drawn.
     * @param payoffWrapper The wrapper containing payoff information for the node.
     * @return The row index available to draw the next branch of the tree.
     * @throws MalformedSimulationModelException 
     * @throws MalformedOSDiModelException
     * @throws UnsupportedOSDiFeatureException
     */
    private int drawPayoffNode(BranchDestinationNode payoff, int row, int column, PayoffWrapper payoffWrapper) throws MalformedSimulationModelException, MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        payoffWrapper.updateWithTreeNode(payoff);
        if (column < payoffColumn) {
            ExcelTools.applyStyleToRange(sheet, Styles.TREE_NODE.getStyle(workbook), row, row, column + 2, payoffColumn - 2);
            column = payoffColumn;
        }
        final Row payoffRow = ExcelTools.getOrCreateRow(sheet, row);
        final Cell probCell = ExcelTools.getOrCreateCell(payoffRow, column);
        final Cell costCell = ExcelTools.getOrCreateCell(payoffRow, column + 1);
        final Cell lyCell = ExcelTools.getOrCreateCell(payoffRow, column + 2);
        final Cell qalyCell = ExcelTools.getOrCreateCell(payoffRow, column + 3);
        final Cell costDetailCell = ExcelTools.getOrCreateCell(payoffRow, column + 5);
        final Cell lyDetailCell = ExcelTools.getOrCreateCell(payoffRow, column + 6);
        final Cell qalyDetailCell = ExcelTools.getOrCreateCell(payoffRow, column + 7);
        final Cell undiscountedlyDetail = ExcelTools.getOrCreateCell(payoffRow, column + 9);
        // Apply styles to the cells
        probCell.setCellStyle(Styles.PROBABILITY.getStyle(workbook));
        costCell.setCellStyle(Styles.COST.getStyle(workbook));
        lyCell.setCellStyle(Styles.LIFE_YEARS.getStyle(workbook));
        qalyCell.setCellStyle(Styles.QALY.getStyle(workbook));
        costDetailCell.setCellStyle(Styles.COST.getStyle(workbook));
        lyDetailCell.setCellStyle(Styles.LIFE_YEARS.getStyle(workbook));
        undiscountedlyDetail.setCellStyle(Styles.LIFE_YEARS.getStyle(workbook));
        qalyDetailCell.setCellStyle(Styles.QALY.getStyle(workbook));
        
        // Set the formulas for the cells
        final String probFormula = payoffWrapper.getProbabilityFormula();
        if (probFormula.isEmpty()) {
            probCell.setCellValue(0.0);
        }
        else {
            probCell.setCellFormula(probFormula);
        }

        final String costFormula = payoffWrapper.getCostFormula(new CellReference(undiscountedlyDetail));
        if (!costFormula.isEmpty()) {
            costDetailCell.setCellFormula(costFormula);
        } else {
            costDetailCell.setCellValue(0.0);
        }
        costCell.setCellFormula(new CellReference(probCell).formatAsString() + " * " + new CellReference(costDetailCell).formatAsString());

        final String qaleFormula = payoffWrapper.getQALEFormula(new CellReference(undiscountedlyDetail), modelBuilder.getModel().getBaseUtility(), modelBuilder.getModel().getDisutilityCombinationMethod());
        if (!qaleFormula.isEmpty()) {
            qalyDetailCell.setCellFormula(qaleFormula);
        } else {
            qalyDetailCell.setCellValue(0.0);
        }
        qalyCell.setCellFormula(new CellReference(probCell).formatAsString() + " * " + new CellReference(qalyDetailCell).formatAsString());

        String lyFormula = payoffWrapper.getUndiscountedLEFormula(modelBuilder.getModel().getLifeExpectancy());
        // Tune the formula to use the time horizon (useful for budget impact analysis)
        lyFormula = ExcelFormulaLibrary.MIN.getFormula(lyFormula + ", " + HTAExcelModelFactory.CommonNamedRanges.TIME_HORIZON.getName());
        undiscountedlyDetail.setCellFormula(lyFormula);
        lyFormula = FormulaLibrary.CONT_DISCOUNT.getFormula(HTAExcelModelFactory.CommonNamedRanges.DISCOUNT_LE.getName(), new CellReference(undiscountedlyDetail), 1);
        lyDetailCell.setCellFormula(lyFormula);
        lyCell.setCellFormula(new CellReference(probCell).formatAsString() + " * " + new CellReference(lyDetailCell).formatAsString());

        return row + ROWS_BETWEEN_NODES;
    }

    /**
     * Draws the probability formula in the specified cell.
     * @param cell The cell where the probability formula will be drawn.
     * @param successor The successor node for which the probability formula will be drawn.
     * @throws UnsupportedOSDiFeatureException 
     */
    private void drawProbabilityFormula(Row valuesRow, int column, BranchDestinationNode successor, Set<EffectWrapper> effects) throws UnsupportedOSDiFeatureException {
        final Cell cell = ExcelTools.getOrCreateCell(valuesRow, column);
        final String probFormula = HTAExcelModelFactory.getParameterTextFormula(successor.getProbability(), effects);
        cell.setCellFormula(probFormula);
        cell.setCellStyle(Styles.PROBABILITY.getStyle(workbook));
    }

    /**
     * Draws the default probability formula in the specified cell based on a list of rows used.
     * @param cell The cell where the probability formula will be drawn.
     * @param column The column index where the formula will be placed.
     * @param rowsUsed The list of rows used to calculate the probability.
     */
    private void drawProbabilityFormula(Row valuesRow, int column, ArrayList<Integer> rowsUsed) {
        String defaultBranchFormula = "1 - SUM(" + new CellReference(rowsUsed.get(0), column, true, true).formatAsString();
        for (int i = 1; i < rowsUsed.size(); i++) {
            defaultBranchFormula += "," + new CellReference(rowsUsed.get(i), column, true, true).formatAsString();
        }
        defaultBranchFormula += ")";
        final Cell cell = ExcelTools.getOrCreateCell(valuesRow, column);
        cell.setCellFormula(defaultBranchFormula);
        cell.setCellStyle(Styles.PROBABILITY.getStyle(workbook));
    }

    /**
     * Draws the text for a node in the Excel sheet.
     * @param cell The cell where the text will be drawn.
     * @param text The text to be drawn in the cell.
     */
    private void drawNodeText(Cell cell, String text) {
        cell.setCellValue(text);
        int row = cell.getRowIndex();
        int column = cell.getColumnIndex();
        ExcelTools.applyStyleToRange(sheet, Styles.TREE_NODE.getStyle(workbook), row, row,column-1, column+1);
        sheet.setColumnWidth(column - 1, 5*256); // Set column width to 20 characters
        sheet.autoSizeColumn(column);        
    }

    /**
     * Draws a connection between two nodes in the decision tree.
     * It fills the cells in the specified range with a specific style to represent the connection.
     * @param iniRow The starting row of the connection.
     * @param endRow The ending row of the connection.
     * @param column The column where the connection will be drawn.
     */
    private void drawTreeConnection(int iniRow, int endRow, int column) {
        for (int i = iniRow; i < endRow; i++) {
            final Cell cell = ExcelTools.getOrCreateCell(ExcelTools.getOrCreateRow(sheet, i), column);
            cell.setCellStyle(Styles.TREE_BRANCH.getStyle(workbook));
        }
        final Cell cell = ExcelTools.getOrCreateCell(ExcelTools.getOrCreateRow(sheet, endRow), column);
        cell.setCellStyle(Styles.TREE_CORNER.getStyle(workbook));
    }
}
