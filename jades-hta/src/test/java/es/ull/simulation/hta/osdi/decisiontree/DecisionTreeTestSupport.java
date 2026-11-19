package es.ull.simulation.hta.osdi.decisiontree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.StreamDocumentSource;

import es.ull.simulation.hta.osdi.decisiontree.excelport.HTAExcelModelFactory;
import es.ull.simulation.hta.osdi.decisiontree.excelport.ProcessSheet;
import es.ull.simulation.hta.osdi.decisiontree.factories.AbstractModelFactory;
import es.ull.simulation.hta.osdi.decisiontree.factories.CentralModelFactory;
import es.ull.simulation.hta.osdi.decisiontree.factories.SimpleModel;
import es.ull.simulation.hta.osdi.decisiontree.nbsdecisiontree.NBSModel;
import es.ull.simulation.hta.osdi.ontology.ExperimentWrapper;
import es.ull.simulation.hta.osdi.ontology.OSDiWrapper;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper.DeterministicParameterData;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper.SecondOrderUncertaintyParameterData;
import es.ull.simulation.utils.ExcelTools;

/**
 * Common code of the tests of the decision trees generated from OSDi: loading a model, reading the generated tree as a set of leaves
 * and reading the outcomes of the generated Excel file.
 */
final class DecisionTreeTestSupport {
    /** Index, among the successors of the root of the tree, of the assessed intervention */
    static final int ASSESSED = 0;
    /** Index, among the successors of the root of the tree, of the comparator intervention */
    static final int COMPARATOR = 1;
    /** Prefixes of the names of the nodes that represent a development, a manifestation (see DiseaseSubTreeGenerator.generateNode) or an outcome of a strategy (see StrategyOutcomeExpander) */
    private static final String[] PROGRESSION_PREFIXES = {"DEVELOPMENT: ", "CHRONIC_MANIFESTATION: ", "ACUTE_MANIFESTATION: ", "STAGE: ", "OUTCOME: ", "ADVERSE EVENT: "};
    /** Prefix of the name of the node that represents the death at the onset of the disease (see DiseaseSubTreeGenerator.generate) */
    private static final String DEATH_PREFIX = "Death from ";
    /** Row of the "Process" sheet with the deterministic results */
    private static final int RESULTS_ROW = 10;
    /** First column of the "Process" sheet with results: cost, life years and QALYs of the first intervention, and then those of the second one */
    private static final int FIRST_RESULTS_COLUMN = 6;
    /** Names of the results, for the error messages */
    static final String[] OUTCOME_NAMES = {"cost (assessed)", "life years (assessed)", "QALYs (assessed)",
        "cost (comparator)", "life years (comparator)", "QALYs (comparator)"};
    private static boolean factoriesRegistered = false;

    private DecisionTreeTestSupport() {
    }

    /**
     * Registers the factories of decision tree models, just once
     */
    static synchronized void registerFactories() {
        if (!factoriesRegistered) {
            CentralModelFactory.register(NBSModel.getFactory());
            CentralModelFactory.register(SimpleModel.getFactory());
            factoriesRegistered = true;
        }
    }

    /**
     * The ontologies already loaded, by resource. Loading an ontology is the most expensive step of a test (more than one second for the largest
     * one), and building an experiment does not modify the ontology: each experiment creates its own model wrapper and model item wrappers.
     */
    private static final Map<String, OSDiWrapper> LOADED_ONTOLOGIES = new ConcurrentHashMap<>();

    /**
     * Returns the wrapper of an ontology, which is loaded only once per resource
     * @param ontology The resource with the ontology
     * @return The wrapper of the ontology
     */
    private static OSDiWrapper getOntology(String ontology) {
        return LOADED_ONTOLOGIES.computeIfAbsent(ontology, resource -> {
            final OWLOntologyDocumentSource source = new StreamDocumentSource(Objects.requireNonNull(
                DecisionTreeTestSupport.class.getResourceAsStream(resource), "Ontology file not found in resources: " + resource));
            try {
                return new OSDiWrapper.Builder().build(source);
            } catch (Exception e) {
                throw new IllegalStateException("The ontology " + resource + " cannot be loaded", e);
            }
        });
    }

    /**
     * Creates the decision tree model for an experiment of an ontology (loaded only once per resource, see {@link #getOntology(String)})
     * @param ontology The resource with the ontology
     * @param experiment The name of the experiment
     * @return The decision tree model
     * @throws Exception If the ontology cannot be loaded or the model cannot be created
     */
    static SingleDiseaseAndPopulationModel loadModel(String ontology, String experiment) throws Exception {
        registerFactories();
        final OSDiWrapper wrapper = getOntology(ontology);
        final ExperimentWrapper experimentWrapper = wrapper.buildExperiment(wrapper.toIRI(experiment));
        final Model model = CentralModelFactory.create(experimentWrapper);
        assertTrue(model instanceof SingleDiseaseAndPopulationModel,
            "The model created is not a SingleDiseaseAndPopulationModel. Found: " + model.getClass().getSimpleName());
        return (SingleDiseaseAndPopulationModel) model;
    }

    /**
     * Reads the sub-tree of an intervention as a set of leaves. Each leaf is identified by the developments and manifestations that are
     * active in the path from the intervention to the leaf, whatever the order in which the tree branches (see {@link DecisionTreeCatalog#leafKey(List)}).
     * The probability of a leaf is the product of the probabilities of the branches of its path, computed from the deterministic
     * (or expected) value of their parameters. The effects of the intervention on those parameters are NOT applied.
     * @param intervention The node of the intervention
     * @return A map from the key of each leaf to its probability
     */
    static Map<String, Double> readLeaves(BranchDestinationNode intervention) {
        final Map<String, Double> leaves = new TreeMap<>();
        collectLeaves(intervention, new ArrayList<>(), 1.0, leaves);
        return leaves;
    }

    private static void collectLeaves(BranchDestinationNode node, List<String> path, double probability, Map<String, Double> leaves) {
        if (node.isPayoff()) {
            final String key = leafKey(path);
            assertFalse(leaves.containsKey(key), "The tree contains two leaves for the same combination of progressions (" + key
                + "). Second one found in the path: " + path);
            leaves.put(key, probability);
            return;
        }
        double sum = 0.0;
        for (BranchDestinationNode successor : node.getSuccessors()) {
            final double branchProbability = expectedValue(successor.getProbability());
            sum += branchProbability;
            path.add(successor.name());
            collectLeaves(successor, path, probability * branchProbability, leaves);
            path.remove(path.size() - 1);
        }
        final BranchDestinationNode defaultSuccessor = node.getDefaultSuccessor();
        if (defaultSuccessor != null) {
            path.add(defaultSuccessor.name());
            collectLeaves(defaultSuccessor, path, probability * (1.0 - sum), leaves);
            path.remove(path.size() - 1);
        }
    }

    /**
     * Returns the key of a leaf from the names of the nodes in its path
     * @param path Names of the nodes from the intervention (excluded) to the leaf
     * @return The key of the leaf
     */
    private static String leafKey(List<String> path) {
        final List<String> progressions = new ArrayList<>();
        for (String nodeName : path) {
            if (nodeName.startsWith(DEATH_PREFIX)) {
                return DecisionTreeCatalog.DEATH;
            }
            for (String prefix : PROGRESSION_PREFIXES) {
                if (nodeName.startsWith(prefix)) {
                    progressions.add(nodeName.substring(prefix.length()));
                }
            }
        }
        return DecisionTreeCatalog.leafKey(progressions);
    }

    /**
     * Returns the deterministic (or expected) value of a parameter
     * @param parameter A parameter
     * @return the deterministic (or expected) value of the parameter
     */
    static double expectedValue(ParameterWrapper parameter) {
        assertNotNull(parameter, "A branch that is not the default one must define a probability");
        // Probability derived from a time to event T within a period: 1 - e^(-period/T)
        final Optional<AbstractModelFactory.TimeToEventProbability> tte = AbstractModelFactory.getTimeToEventProbability(parameter);
        if (tte.isPresent()) {
            final double period = tte.get().periodParameter().isPresent() ? expectedValue(tte.get().periodParameter().get()) : tte.get().periodValue();
            return 1.0 - Math.exp(-period / expectedValue(tte.get().time()));
        }
        if (parameter.getParameterNatureData() instanceof DeterministicParameterData data) {
            return data.value();
        }
        if (parameter.getParameterNatureData() instanceof SecondOrderUncertaintyParameterData data) {
            return data.expectedValue();
        }
        return fail("The value of the parameter " + parameter.getShortName() + " cannot be computed in the tests: " + parameter.getNature());
    }

    /**
     * System property that makes the tests write the Excel files to disk and read them back, instead of evaluating the workbooks in memory
     * (e.g., -Djades.tests.excel=true). Writing the file is the most expensive step of the tests of decision trees, and it does not change the
     * formulas that are evaluated.
     */
    static final String WRITE_EXCEL_PROPERTY = "jades.tests.excel";

    /**
     * Returns true if the tests must write the Excel files to disk (see {@link #WRITE_EXCEL_PROPERTY})
     * @return true if the tests must write the Excel files to disk
     */
    static boolean writeExcelFiles() {
        return Boolean.getBoolean(WRITE_EXCEL_PROPERTY);
    }

    /**
     * Sheets of the template of the generator that do not take part in the deterministic results: the generator does not write in them, and no
     * formula of the rest of sheets refers to them. They are removed from the template used in memory (see {@link #getReducedTemplate()}).
     */
    private static final List<String> SHEETS_NOT_IN_RESULTS = List.of("EVPI", "Results", "Cost-effectiveness", "Acceptability curves", "Impact of uncertainty",
        "One-way sensitivity", "Two-way sensitivity", "Budget impact", "Weights Spain", "Heights Spain", "Mortality Spain");
    /** The reduced template, created once from the template of the generator (see {@link #getReducedTemplate()}) */
    private static byte[] reducedTemplate = null;

    /**
     * Returns a copy of the template of the generator without the sheets that do not take part in the deterministic results
     * ({@link #SHEETS_NOT_IN_RESULTS}). Reading the template is the most expensive step of generating an Excel model in memory, and the reduced
     * template takes about a third of the time. It is created once, from the template of the generator, so it follows its changes.
     * @return The reduced template
     * @throws Exception If the template cannot be read or written
     */
    private static synchronized byte[] getReducedTemplate() throws Exception {
        if (reducedTemplate == null) {
            try (InputStream in = HTAExcelModelFactory.getTemplate(); XSSFWorkbook template = new XSSFWorkbook(in)) {
                for (String name : SHEETS_NOT_IN_RESULTS) {
                    final int index = template.getSheetIndex(name);
                    assertTrue(index >= 0, "The template of the generator has no sheet '" + name + "': update SHEETS_NOT_IN_RESULTS");
                    template.removeSheetAt(index);
                }
                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                template.write(out);
                reducedTemplate = out.toByteArray();
            }
        }
        return reducedTemplate;
    }

    /**
     * Generates the Excel model of a decision tree. By default, it is generated in memory from the reduced template (see {@link #getReducedTemplate()});
     * if {@link #WRITE_EXCEL_PROPERTY} is set, it is generated from the template of the generator, written to a file and read back.
     * @param model A decision tree model
     * @param excelFile The file to generate, if the Excel files are written to disk
     * @return The workbook with the Excel model; the caller must close it
     * @throws Exception If the Excel model cannot be generated or read
     */
    static Workbook generateExcel(SingleDiseaseAndPopulationModel model, Path excelFile) throws Exception {
        if (!writeExcelFiles()) {
            return HTAExcelModelFactory.createWorkbook(model, new ByteArrayInputStream(getReducedTemplate()));
        }
        HTAExcelModelFactory.build(excelFile.toString(), model);
        assertTrue(Files.exists(excelFile) && Files.size(excelFile) > 0, "Excel was not generated: " + excelFile);
        try (InputStream in = Files.newInputStream(excelFile)) {
            return new XSSFWorkbook(in);
        }
    }

    /**
     * Generates the Excel model of a decision tree (see {@link #generateExcel(SingleDiseaseAndPopulationModel, Path)}) and returns its deterministic results
     * @param model A decision tree model
     * @param excelFile The file to generate, if the Excel files are written to disk
     * @return Cost, life years and QALYs of the assessed intervention, followed by those of the comparator intervention
     * @throws Exception If the Excel model cannot be generated or read
     */
    static double[] readExcelOutcomes(SingleDiseaseAndPopulationModel model, Path excelFile) throws Exception {
        try (Workbook workbook = generateExcel(model, excelFile)) {
            final FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            final Sheet sheet = workbook.getSheet(ProcessSheet.SHEET_NAME);
            assertNotNull(sheet, "The generated Excel has no sheet " + ProcessSheet.SHEET_NAME);
            final double[] outcomes = new double[OUTCOME_NAMES.length];
            for (int i = 0; i < outcomes.length; i++) {
                final Cell cell = ExcelTools.getOrCreateCell(sheet, RESULTS_ROW, FIRST_RESULTS_COLUMN + i);
                final CellValue value = evaluator.evaluate(cell);
                assertNotNull(value, "The cell with the " + OUTCOME_NAMES[i] + " is empty");
                assertEquals(CellType.NUMERIC, value.getCellType(), "The cell with the " + OUTCOME_NAMES[i] + " does not contain a number: " + value.formatAsString());
                outcomes[i] = value.getNumberValue();
            }
            return outcomes;
        }
    }

    /**
     * Checks whether two values are equal, with a relative tolerance
     * @param expected The expected value
     * @param actual The actual value
     * @return true if both values are equal
     */
    static boolean close(double expected, double actual) {
        return Math.abs(expected - actual) <= 1e-8 * Math.max(1.0, Math.abs(expected));
    }
}
