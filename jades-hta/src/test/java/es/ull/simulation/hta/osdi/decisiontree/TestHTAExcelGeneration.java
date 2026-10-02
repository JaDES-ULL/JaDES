package es.ull.simulation.hta.osdi.decisiontree;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.ull.simulation.hta.osdi.ontology.ExperimentWrapper;
import es.ull.simulation.hta.osdi.ontology.OSDiTest;
import es.ull.simulation.hta.osdi.ontology.OSDiWrapper;
import es.ull.simulation.hta.CamelCaseDisplayNameGenerator;
import es.ull.simulation.hta.osdi.decisiontree.excelport.HTAExcelModelFactory;
import es.ull.simulation.hta.osdi.decisiontree.factories.CentralModelFactory;
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
                new ToCheckExcel("Process", 10, 9, 0.7638404417187111)
            })
        );
    }

    @TempDir
    Path tempDir;

    @BeforeAll
    void setup() {
        CentralModelFactory.register(NBSModel.getFactory());
        CentralModelFactory.register(SimpleModel.getFactory());
    }

    @ParameterizedTest(name = "[{index}] generate {0}")
    @MethodSource("cases")
    @OSDiTest
    @DisplayName("HTA Excel creation test")
    void generateExcels(DecisionTreeExcelCase tc) throws Exception {
        OWLOntologyDocumentSource source = new StreamDocumentSource(Objects.requireNonNull(
                getClass().getResourceAsStream(tc.ontologyResource()), "Ontology file not found in resources: " + tc.ontologyResource()));
        final OSDiWrapper wrapper = new OSDiWrapper.Builder().build(source);
        final ExperimentWrapper experimentWrapper = wrapper.buildExperiment(wrapper.toIRI(tc.modelName()));
        Model tempModel = CentralModelFactory.create(experimentWrapper);
        assertTrue(tempModel instanceof SingleDiseaseAndPopulationModel, "The model created is not of the expected type SingleDiseaseAndPopulationModel. Found: " + tempModel.getClass().getSimpleName());
        assertTrue(tc.expectedClass().isInstance(tempModel), "The model is expected to be an instance of " + tc.expectedClass().getSimpleName() + ", but got " + tempModel.getClass().getSimpleName());
        SingleDiseaseAndPopulationModel model = (SingleDiseaseAndPopulationModel) tempModel;
        Path out = tempDir.resolve(tc.excelFileName());
        assertDoesNotThrow(() -> HTAExcelModelFactory.build(out.toString(), model));
        assertTrue(Files.exists(out), "Excel was not generated: " + out);
        assertTrue(Files.size(out) > 0, "Excel is empty: " + out);
        maybeKeepForManualReview(out, tc.excelFileName());
        try (InputStream fis = Files.newInputStream(out); XSSFWorkbook workbook = new XSSFWorkbook(fis)) {
            final FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            for (ToCheckExcel check : tc.checks()) {
                check.checkValue(workbook, evaluator);
            }
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
