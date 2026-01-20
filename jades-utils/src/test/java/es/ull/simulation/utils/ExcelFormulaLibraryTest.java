package es.ull.simulation.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.poi.ss.util.CellReference;
import org.junit.jupiter.api.Test;

class ExcelFormulaLibraryTest {

    @Test
    void shouldFormatFormulas() {
        assertEquals("SUM(A1:A3)", ExcelFormulaLibrary.SUM.getFormula("A1:A3"));
        assertEquals("ROUND(A1,0)", ExcelFormulaLibrary.ROUND0.getFormula("A1"));
    }

    @Test
    void shouldFormatWithCellReferences() {
        String formula = ExcelFormulaLibrary.COUNT.getFormula(new CellReference(0, 0));
        assertEquals("COUNT(A1)", formula);
    }

    @Test
    void shouldHandleUniformInverse() {
        String formula = ExcelFormulaLibrary.UNIFORM_INV.getFormula("RAND()", "A1", "B1");
        assertEquals("RAND() * (A1 - B1) + B1", formula);
    }
}
