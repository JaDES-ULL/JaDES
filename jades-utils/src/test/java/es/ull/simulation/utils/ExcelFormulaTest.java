package es.ull.simulation.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.poi.ss.util.CellReference;
import org.junit.jupiter.api.Test;

class ExcelFormulaTest {

    private static final class SumFormula implements ExcelFormula {
        @Override
        public String getTemplate() {
            return "SUM(%s,%s)";
        }
    }

    @Test
    void shouldFormatCellReferences() {
        ExcelFormula formula = new SumFormula();
        String result = formula.getFormula(new CellReference("A1"), new CellReference("B2"));
        assertEquals("SUM(A1,B2)", result);
    }
}
