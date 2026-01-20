package es.ull.simulation.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

class ExcelToolsTest {

    @Test
    void shouldGetStringValuesAndValidateCells() throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Sheet1");
            Row row = sheet.createRow(0);
            row.createCell(0, CellType.STRING).setCellValue("hello");
            row.createCell(1, CellType.NUMERIC).setCellValue(42);
            Row row2 = sheet.createRow(1);
            row2.createCell(0, CellType.STRING).setCellValue("world");

            assertEquals("hello", ExcelTools.getString(sheet, 0, 0));
            assertEquals("42", ExcelTools.getString(row, 1));
            assertEquals("world", ExcelTools.getString(row2, 0));
            assertTrue(ExcelTools.validCell(row, 0));
            assertFalse(ExcelTools.validCell(row, 1));
            assertFalse(ExcelTools.validCell(row, 2));
        }
    }

    @Test
    void shouldCreateRowsCellsAndApplyStyles() throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Sheet1");
            CellStyle style = wb.createCellStyle();

            Row createdRow = ExcelTools.getOrCreateRow(sheet, 2);
            Cell createdCell = ExcelTools.getOrCreateCell(createdRow, 3);
            assertSame(createdRow, sheet.getRow(2));
            assertSame(createdCell, createdRow.getCell(3));

            Cell createdCellInSheet = ExcelTools.getOrCreateCell(sheet, 2, 4);
            assertSame(createdCellInSheet, sheet.getRow(2).getCell(4));

            ExcelTools.applyStyleToRange(sheet, style, 0, 1, 0, 1);

            for (int r = 0; r <= 1; r++) {
                for (int c = 0; c <= 1; c++) {
                    Cell cell = sheet.getRow(r).getCell(c);
                    assertNotNull(cell);
                    assertEquals(style.getIndex(), cell.getCellStyle().getIndex());
                }
            }
        }
    }

    @Test
    void shouldGetStyleFromNamedRange() throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Sheet1");
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0, CellType.STRING);
            CellStyle style = wb.createCellStyle();
            cell.setCellStyle(style);

            Name name = wb.createName();
            name.setNameName("MyRange");
            name.setRefersToFormula("Sheet1!$A$1");

            assertEquals(style.getIndex(), ExcelTools.getStyleFromNamedRange(wb, "MyRange").getIndex());
            assertNull(ExcelTools.getStyleFromNamedRange(wb, "MissingRange"));

            Name empty = wb.createName();
            empty.setNameName("EmptyRange");
            assertNull(ExcelTools.getStyleFromNamedRange(wb, "EmptyRange"));

            Name badSheet = wb.createName();
            badSheet.setNameName("BadSheet");
            badSheet.setRefersToFormula("Missing!$A$1");
            assertNull(ExcelTools.getStyleFromNamedRange(wb, "BadSheet"));

            Name missingRow = wb.createName();
            missingRow.setNameName("MissingRow");
            missingRow.setRefersToFormula("Sheet1!$B$2");
            assertNull(ExcelTools.getStyleFromNamedRange(wb, "MissingRow"));
        }
    }

    @Test
    void shouldCopyRangesAndNamedRanges() throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet source = wb.createSheet("Source");
            Sheet target = wb.createSheet("Target");
            Row row = source.createRow(0);
            row.createCell(0, CellType.STRING).setCellValue("text");
            row.createCell(1, CellType.NUMERIC).setCellValue(5.0);
            row.createCell(2, CellType.FORMULA).setCellFormula("SUM(B1:B1)");
            row.createCell(3, CellType.BOOLEAN).setCellValue(true);
            row.createCell(4, CellType.ERROR).setCellErrorValue((byte) 7);
            row.createCell(5, CellType.BLANK);

            ExcelTools.copyCellRange(source, 0, 0, 0, 5, target, 1, 0);

            assertEquals("text", target.getRow(1).getCell(0).getStringCellValue());
            assertEquals(5.0, target.getRow(1).getCell(1).getNumericCellValue(), 1e-9);
            assertEquals("SUM(B1:B1)", target.getRow(1).getCell(2).getCellFormula());
            assertTrue(target.getRow(1).getCell(3).getBooleanCellValue());
            assertEquals((byte) 7, target.getRow(1).getCell(4).getErrorCellValue());
            assertEquals(CellType.BLANK, target.getRow(1).getCell(5).getCellType());

            Name name = wb.createName();
            name.setNameName("NamedBlock");
            name.setRefersToFormula("Source!$A$1:$B$1");

            ExcelTools.copyCellRange("NamedBlock", target, 2, 0);
            assertEquals("text", target.getRow(2).getCell(0).getStringCellValue());
            assertEquals(5.0, target.getRow(2).getCell(1).getNumericCellValue(), 1e-9);

            assertThrows(IllegalArgumentException.class,
                    () -> ExcelTools.copyCellRange("MissingBlock", target, 0, 0));
        }
    }

    @Test
    void shouldSetValuesInNamedRanges() throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Sheet1");
            sheet.createRow(0).createCell(0, CellType.STRING).setCellValue("init");

            Name name = wb.createName();
            name.setNameName("CellA1");
            name.setRefersToFormula("Sheet1!$A$1");

            ExcelTools.setValue(wb, "CellA1", "updated");
            assertEquals("updated", sheet.getRow(0).getCell(0).getStringCellValue());

            ExcelTools.setValue(wb, "CellA1", 7.5);
            assertEquals(7.5, sheet.getRow(0).getCell(0).getNumericCellValue(), 1e-9);

            assertThrows(IllegalArgumentException.class, () -> ExcelTools.setValue(wb, "Missing", "x"));
        }
    }

    @Test
    void shouldCreateNamedRanges() throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Sheet1");

            ExcelTools.nameRange(sheet, 0, 0, "SingleCell", true);
            assertNotNull(wb.getName("SingleCell"));
            assertTrue(wb.getName("SingleCell").getRefersToFormula().contains("$A$1"));

            ExcelTools.nameRange(sheet, 0, 0, 1, 1, "RangeBlock", false);
            assertNotNull(wb.getName("RangeBlock"));
            assertTrue(wb.getName("RangeBlock").getRefersToFormula().contains("A1"));
        }
    }
}
