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
    private static final Logger log = LoggerFactory.getLogger(TestSingleDiseaseAndPopulationDecisionTreeModelGeneration.class);

    private final static ToCheckExcel[][] TO_CHECK_EXCEL = {
        {
            new ToCheckExcel("Process", 10, 6, 71500.0), // Cost intervention Effective
            new ToCheckExcel("Process", 10, 9, 85000.0), // Cost intervention Ineffective
            new ToCheckExcel("Process", 10, 7, 65.0), // LE intervention Effective
            new ToCheckExcel("Process", 10, 10, 49.0), // LE intervention Ineffective
            new ToCheckExcel("Process", 10, 8, 55.25), // QALY intervention Effective
            new ToCheckExcel("Process", 10, 11, 27.25) // QALY intervention Ineffective
        },
        {
            new ToCheckExcel("Process", 10, 6, 71500.0), // Cost intervention Effective
            new ToCheckExcel("Process", 10, 9, 124200.0), // Cost intervention Ineffective
            new ToCheckExcel("Process", 10, 7, 65.0), // LE intervention Effective
            new ToCheckExcel("Process", 10, 10, 49.0), // LE intervention Ineffective
            new ToCheckExcel("Process", 10, 8, 55.25), // QALY intervention Effective
            new ToCheckExcel("Process", 10, 11, 19.41) // QALY intervention Ineffective
        },
        {
            new ToCheckExcel("Process", 10, 6, 71500.0), // Cost intervention Effective
            new ToCheckExcel("Process", 10, 9, 154440.0), // Cost intervention Ineffective
            new ToCheckExcel("Process", 10, 7, 65.0), // LE intervention Effective
            new ToCheckExcel("Process", 10, 10, 45.8), // LE intervention Ineffective
            new ToCheckExcel("Process", 10, 8, 55.25), // QALY intervention Effective
            new ToCheckExcel("Process", 10, 11, 14.322) // QALY intervention Ineffective

        },
        {
            new ToCheckExcel("Process", 10, 6, 1.5512269701985133),
            new ToCheckExcel("Process", 10, 9, 0.7638404417187111)
        }
    };

    static Stream<DecisionTreeExcelCase> cases() {
        return Stream.of(
            new DecisionTreeExcelCase("/OSDi_test.ttl", "TEST_Experiment1", "test1.xlsm", SimpleModel.class, TO_CHECK_EXCEL[0]),
            new DecisionTreeExcelCase("/OSDi_test.ttl", "TEST_Experiment2", "test2.xlsm", SimpleModel.class, TO_CHECK_EXCEL[1]),
            new DecisionTreeExcelCase("/OSDi_test.ttl", "TEST_Experiment3", "test3.xlsm", SimpleModel.class, TO_CHECK_EXCEL[2]),
            new DecisionTreeExcelCase("/PBD.ttl", "PBD_ExperimentBase", "testPBD.xlsm", NBSModel.class, TO_CHECK_EXCEL[3])
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
        SingleDiseaseAndPopulationModel model = (SingleDiseaseAndPopulationModel) tempModel;
        Path out = tempDir.resolve(tc.excelFileName());
        assertDoesNotThrow(() -> HTAExcelModelFactory.build(out.toString(), model));
        assertTrue(Files.exists(out), "Excel was not generated: " + out);
        assertTrue(Files.size(out) > 0, "Excel is empty: " + out);
        maybeKeepForManualReview(out, tc.excelFileName());
        final InputStream fis = Files.newInputStream(out);
        final XSSFWorkbook workbook = new XSSFWorkbook(fis);
        final FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        for (ToCheckExcel check : tc.checks()) {
            check.checkValue(workbook, evaluator);
        }
        fis.close();
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
