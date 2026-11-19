package es.ull.simulation.hta.osdi.decisiontree;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.ull.simulation.hta.osdi.ontology.OSDiTest;
import es.ull.simulation.hta.CamelCaseDisplayNameGenerator;
import es.ull.simulation.hta.osdi.decisiontree.excelport.HTAExcelModelFactory;
import es.ull.simulation.hta.osdi.decisiontree.factories.SimpleModel;
import es.ull.simulation.hta.osdi.decisiontree.nbsdecisiontree.NBSModel;
import es.ull.simulation.utils.ExcelTools;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayNameGeneration(CamelCaseDisplayNameGenerator.class)
public class TestHTAExcelGeneration {
    private static final Logger log = LoggerFactory.getLogger(TestHTAExcelGeneration.class);

    /**
     * Expected outcomes for both arms of the OSDi_test models. The values were computed independently, by enumerating every combination of 
     * active developments and applying the combination methods of each experiment (costs, disutilities, life expectancy reductions and 
     * increased mortality rates). Discount rates are 0, so cost = annual cost x life expectancy and QALY = utility x life expectancy.
     * The effective intervention removes every development, so its outcomes are the same in every model: 65 years x (1000 + 100) and 65 x 0.85.
     * @param ineffectiveCost Expected cost for the ineffective intervention
     * @param ineffectiveLE Expected life expectancy for the ineffective intervention
     * @param ineffectiveQALY Expected QALYs for the ineffective intervention
     * @return the cells to check
     */
    private static ToCheckExcel[] testModelArms(double ineffectiveCost, double ineffectiveLE, double ineffectiveQALY) {
        return new ToCheckExcel[] {
            new ToCheckExcel("Process", 10, 6, 71500.0), // Cost intervention Effective
            new ToCheckExcel("Process", 10, 9, ineffectiveCost), // Cost intervention Ineffective
            new ToCheckExcel("Process", 10, 7, 65.0), // LE intervention Effective
            new ToCheckExcel("Process", 10, 10, ineffectiveLE), // LE intervention Ineffective
            new ToCheckExcel("Process", 10, 8, 55.25), // QALY intervention Effective
            new ToCheckExcel("Process", 10, 11, ineffectiveQALY) // QALY intervention Ineffective
        };
    }

    /**
     * Expected outcomes of the newborn screening cases: the costs of both arms, with 80 life years and 68 QALYs in both.
     * @param screeningCost The cost of the screening arm.
     * @param noScreeningCost The cost of the arm without screening.
     * @return The values to check in the Excel file.
     */
    private static ToCheckExcel[] nbsArms(double screeningCost, double noScreeningCost) {
        return nbsArms(screeningCost, 68.0, noScreeningCost);
    }

    /**
     * Expected outcomes of the newborn screening cases: the costs of both arms and the QALYs of the screening arm, with 80 life years in both and
     * 68 QALYs in the arm without screening.
     * @param screeningCost The cost of the screening arm.
     * @param screeningQALY The QALYs of the screening arm.
     * @param noScreeningCost The cost of the arm without screening.
     * @return The values to check in the Excel file.
     */
    private static ToCheckExcel[] nbsArms(double screeningCost, double screeningQALY, double noScreeningCost) {
        return new ToCheckExcel[] {
            new ToCheckExcel("Process", 10, 6, screeningCost),
            new ToCheckExcel("Process", 10, 7, 80.0),
            new ToCheckExcel("Process", 10, 8, screeningQALY),
            new ToCheckExcel("Process", 10, 9, noScreeningCost),
            new ToCheckExcel("Process", 10, 10, 80.0),
            new ToCheckExcel("Process", 10, 11, 68.0)
        };
    }

    static Stream<DecisionTreeExcelCase> cases() {
        return Stream.of(
            // Alternative developments
            new DecisionTreeExcelCase("/OSDi_test.ttl", "TEST_Experiment1", "test1.xlsm", SimpleModel.class, testModelArms(85000.0, 49.0, 27.25)),
            // Coexistent developments, with default combination methods
            new DecisionTreeExcelCase("/OSDi_test.ttl", "TEST_Experiment2", "test2.xlsm", SimpleModel.class, testModelArms(124200.0, 49.0, 19.41)),
            // Coexistent + alternative developments sharing cost, disutility and life expectancy reduction parameters
            // Costs: ADD; disutilities: ADD_DISTINCT; life expectancy reductions: MAX (default)
            new DecisionTreeExcelCase("/OSDi_test.ttl", "TEST_Experiment3", "test3.xlsm", SimpleModel.class, testModelArms(154440.0, 45.8, 14.322)),
            // Same model, adding everything. Some leaves have negative utility (0.85 - 0.4 - 0.2 - 0.4), which is not truncated
            new DecisionTreeExcelCase("/OSDi_test.ttl", "TEST_Experiment3AddAll", "test3AddAll.xlsm", SimpleModel.class, testModelArms(105800.0, 33.0, 4.21)),
            // Same model, counting only once every shared parameter
            new DecisionTreeExcelCase("/OSDi_test.ttl", "TEST_Experiment3Distinct", "test3Distinct.xlsm", SimpleModel.class, testModelArms(91080.0, 45.8, 14.322)),
            // Same model, taking the maximum disutility and life expectancy reduction
            new DecisionTreeExcelCase("/OSDi_test.ttl", "TEST_Experiment3Max", "test3Max.xlsm", SimpleModel.class, testModelArms(91080.0, 45.8, 21.234)),
            // Coexistent developments increasing mortality rates (x2 and x1.25 + 20 years reduction). LE = 65 / IMR - LER
            new DecisionTreeExcelCase("/OSDi_test.ttl", "TEST_Experiment4Max", "test4Max.xlsm", SimpleModel.class, testModelArms(20920.0, 20.92, 17.782)),
            new DecisionTreeExcelCase("/OSDi_test.ttl", "TEST_Experiment4Mult", "test4Mult.xlsm", SimpleModel.class, testModelArms(16760.0, 16.76, 14.246)),
            // Regression values for the PBD model
            new DecisionTreeExcelCase("/PBD.ttl", "PBD_ExperimentBase", "testPBD.xlsm", NBSModel.class, new ToCheckExcel[] {
                new ToCheckExcel("Process", 10, 6, 1.5512269701985133),
                new ToCheckExcel("Process", 10, 9, 0.7636187637821464)
            }),
            // Newborn screening with detection strategies (OSDi_test_tree_nbs.ttl). Birth prevalence 0.01; screening: sensitivity 0.9 and specificity 0.95;
            // diagnosis: 500 for true and false positives (screening) or for the patients with the disease (no screening); disease: 1000 per year during 80 years.
            // False negatives do not pay a diagnosis (see docs/osdi-1.3-diseno.md). No discount; life years = 80 and QALYs = 0.85 * 80 = 68 in both arms.
            // Screening: 10 + 0.01 * 0.9 * 500 + 0.01 * 80000 + 0.99 * 0.05 * 500 = 839.25. No screening: 0.01 * (500 + 80000) = 805
            new DecisionTreeExcelCase("/OSDi_test_tree_nbs.ttl", "TT_B01_Experiment", "testB01.xlsm", NBSModel.class, nbsArms(839.25, 805.0)),
            // The screening test costs 4 per application, applied twice according to its guideline: 8 instead of 10
            new DecisionTreeExcelCase("/OSDi_test_tree_nbs.ttl", "TT_B02_Experiment", "testB02.xlsm", NBSModel.class, nbsArms(837.25, 805.0)),
            // The disease has a follow-up of 200 per year, replaced by 50 per year in the screening arm: + 0.01 * 80 * 50 = 40 (screening); + 0.01 * 80 * 200 = 160 (no screening)
            new DecisionTreeExcelCase("/OSDi_test_tree_nbs.ttl", "TT_B03_Experiment", "testB03.xlsm", NBSModel.class, nbsArms(879.25, 965.0)),
            // Explicit detection outcomes of the screening test (positive: 0.9 if diseased, 0.05 if not; negative by default), equivalent to B01
            new DecisionTreeExcelCase("/OSDi_test_tree_nbs.ttl", "TT_B04_Experiment", "testB04.xlsm", NBSModel.class, nbsArms(839.25, 805.0)),
            // S1 (10): positive 0.85 / 0.04; inconclusive 0.10 / 0.05, with a QALY loss of 0.01, followed by S2 (15): positive 0.95 / 0.02; negative by default.
            // Inconclusive: 0.01 * 0.10 + 0.99 * 0.05 = 0.0505. Screening: 10 + 0.0505 * 15 + 0.01 * (0.85 + 0.10 * 0.95) * 500 + 0.99 * (0.04 + 0.05 * 0.02) * 500
            // + 800 = 10 + 0.7575 + 4.725 + 20.295 + 800 = 835.7775. QALYs: 68 - 0.0505 * 0.01 = 67.999495
            new DecisionTreeExcelCase("/OSDi_test_tree_nbs.ttl", "TT_B05_Experiment", "testB05.xlsm", NBSModel.class, nbsArms(835.7775, 67.999495, 805.0)),
            // S1 as in B05, but its inconclusive results go directly to the (perfect) diagnosis.
            // Screening: 10 + 0.01 * (0.85 + 0.10) * 500 + 0.99 * (0.04 + 0.05) * 500 + 800 = 10 + 4.75 + 44.55 + 800 = 859.3. QALYs: 68 - 0.0505 * 0.01 = 67.999495
            new DecisionTreeExcelCase("/OSDi_test_tree_nbs.ttl", "TT_B06_Experiment", "testB06.xlsm", NBSModel.class, nbsArms(859.3, 67.999495, 805.0))
        );
    }

    /**
     * Experiments whose Excel file is always written to disk and read back, to check the writing of the file. The rest of the Excel models are
     * generated in memory from the template of the generator, unless the property jades.tests.excel (or keepExcel) is set.
     */
    private static final Set<String> WRITTEN_TO_DISK = Set.of("TEST_Experiment1", "PBD_ExperimentBase");

    @TempDir
    Path tempDir;

    @ParameterizedTest(name = "[{index}] generate {0}")
    @MethodSource("cases")
    @OSDiTest
    @DisplayName("HTA Excel creation test")
    void generateExcels(DecisionTreeExcelCase tc) throws Exception {
        // The ontologies are loaded once (see DecisionTreeTestSupport.loadModel)
        final SingleDiseaseAndPopulationModel model = DecisionTreeTestSupport.loadModel(tc.ontologyResource(), tc.modelName());
        assertTrue(tc.expectedClass().isInstance(model), "The model is expected to be an instance of " + tc.expectedClass().getSimpleName() + ", but got " + model.getClass().getSimpleName());
        try (XSSFWorkbook workbook = generateWorkbook(tc, model)) {
            final FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            for (ToCheckExcel check : tc.checks()) {
                check.checkValue(workbook, evaluator);
            }
        }
    }

    /**
     * Generates the Excel model of a case from the template of the generator: written to disk and read back for the experiments of
     * {@link #WRITTEN_TO_DISK}, or for all of them if the property jades.tests.excel or keepExcel is set; in memory otherwise.
     */
    private XSSFWorkbook generateWorkbook(DecisionTreeExcelCase tc, SingleDiseaseAndPopulationModel model) throws Exception {
        if (!WRITTEN_TO_DISK.contains(tc.modelName()) && !DecisionTreeTestSupport.writeExcelFiles() && !Boolean.getBoolean("keepExcel")) {
            return (XSSFWorkbook) HTAExcelModelFactory.createWorkbook(model);
        }
        final Path out = tempDir.resolve(tc.excelFileName());
        assertDoesNotThrow(() -> HTAExcelModelFactory.build(out.toString(), model));
        assertTrue(Files.exists(out), "Excel was not generated: " + out);
        assertTrue(Files.size(out) > 0, "Excel is empty: " + out);
        maybeKeepForManualReview(out, tc.excelFileName());
        try (InputStream fis = Files.newInputStream(out)) {
            return new XSSFWorkbook(fis);
        }
    }

    private static void maybeKeepForManualReview(Path generatedFile, String fileName) throws Exception {
        if (Boolean.getBoolean("keepExcel")) {
            Path dir = Paths.get("target", "excel-artifacts");
            Files.createDirectories(dir);

            Path dest = dir.resolve(fileName);
            Files.copy(generatedFile, dest, StandardCopyOption.REPLACE_EXISTING);

            log.debug("Saved Excel for manual review: " + dest.toAbsolutePath());
        }
    }

    private static record DecisionTreeExcelCase(
        String ontologyResource,
        String modelName,
        String excelFileName,
        Class<?> expectedClass,
        ToCheckExcel[] checks
    ) {}
    private static record ToCheckExcel(String sheetName, int row, int column, double expectedValue) {
        private final static double ERROR = 0.00000001;
        public void checkValue(XSSFWorkbook workbook, FormulaEvaluator evaluator) {
            final Sheet sheet = workbook.getSheet(sheetName);
            final Cell cell = ExcelTools.getOrCreateCell(sheet, row, column);
            final double cellValue = evaluator.evaluate(cell).getNumberValue();
            assertTrue(Math.abs(cellValue - expectedValue) < ERROR, "Value check failed with " + this + ". Found " + cellValue);
        }
        @Override
        public final String toString() {
            return "ToCheckExcel{" +
                    "sheetName='" + sheetName + '\'' +
                    ", row=" + row +
                    ", column=" + column +
                    ", expectedValue=" + expectedValue +
                    '}';
        }
    }

}
