package es.ull.simulation.hta.osdi.decisiontree;

import java.io.FileInputStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.util.CellReference;

/**
 * Utility to inspect the generated testPBD.xlsm Excel file.
 * Run manually as: mvn exec:java -Dexec.mainClass=es.ull.simulation.hta.osdi.decisiontree.InspectPBDExcel -pl jades-hta
 */
public class InspectPBDExcel {
    public static void main(String[] args) throws Exception {
        String path = args.length > 0 ? args[0] : "target/excel-artifacts/testPBD.xlsm";
        try (FileInputStream fis = new FileInputStream(path);
             XSSFWorkbook wb = new XSSFWorkbook(fis)) {
            
            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
            
            // Print named ranges we care about
            System.out.println("=== NAMED RANGES ===");
            for (Name n : wb.getAllNames()) {
                System.out.println(n.getNameName() + " -> " + n.getRefersToFormula());
            }
            
            // Print Process sheet rows 0, 9, 10 (only cols F-L for brevity)
            Sheet process = wb.getSheet("Process");
            System.out.println("\n=== PROCESS SHEET (cols F-L) ===");
            for (int r = 0; r <= 11; r++) {
                Row row = process.getRow(r);
                if (row == null) continue;
                System.out.print("Row " + r + ": ");
                for (int c = 5; c <= 12; c++) {
                    Cell cell = row.getCell(c);
                    if (cell == null) { System.out.print("[null] "); continue; }
                    String colName = CellReference.convertNumToColString(c);
                    if (cell.getCellType() == CellType.FORMULA) {
                        try {
                            CellValue cv = evaluator.evaluate(cell);
                            System.out.print(colName + "=" + cell.getCellFormula() + "=" + 
                                (cv.getCellType() == CellType.NUMERIC ? cv.getNumberValue() : cv.getStringValue()) + " ");
                        } catch (Throwable e) {
                            System.out.print(colName + "=[SKIP:" + e.getClass().getSimpleName() + "] ");
                            // Reset evaluator to avoid poisoned cache
                            evaluator.clearAllCachedResultValues();
                        }
                    } else if (cell.getCellType() == CellType.NUMERIC) {
                        System.out.print(colName + "=" + cell.getNumericCellValue() + " ");
                    } else if (cell.getCellType() == CellType.STRING) {
                        System.out.print(colName + "=\"" + cell.getStringCellValue() + "\" ");
                    }
                }
                System.out.println();
            }
            
            // Print Model sheet all rows
            Sheet model = wb.getSheet("Model");
            System.out.println("\n=== MODEL SHEET (all rows) ===");
            int lastModelRow = model.getLastRowNum();
            for (int r = 0; r <= lastModelRow; r++) {
                Row row = model.getRow(r);
                if (row == null) continue;
                System.out.print("Row " + r + ": ");
                for (int c = 0; c <= 10; c++) {
                    Cell cell = row.getCell(c);
                    if (cell == null) continue;
                    String colName = CellReference.convertNumToColString(c);
                    if (cell.getCellType() == CellType.FORMULA) {
                        try {
                            CellValue cv = evaluator.evaluate(cell);
                            System.out.print(colName + "=" + cell.getCellFormula() + "=" + 
                                (cv.getCellType() == CellType.NUMERIC ? cv.getNumberValue() : cv.getStringValue()) + " ");
                        } catch (Exception e) {
                            System.out.print(colName + "=" + cell.getCellFormula() + "=[ERR] ");
                        }
                    } else if (cell.getCellType() == CellType.NUMERIC) {
                        System.out.print(colName + "=" + cell.getNumericCellValue() + " ");
                    } else if (cell.getCellType() == CellType.STRING) {
                        System.out.print(colName + "=\"" + cell.getStringCellValue() + "\" ");
                    }
                }
                System.out.println();
            }
        }
    }
}
