package es.ull.simulation.hta.osdi.decisiontree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;

/**
 * Catalog of the test cases for the decision trees generated from OSDi. Every case points to an experiment of one of the test 
 * ontologies (OSDi_test_tree_*.ttl) and states what the generated tree must represent:
 * <ul>
 * <li>its leaves: every feasible combination of developments and manifestations, with its probability (see {@link #leaf(double, String...)});</li> 
 * <li>its outcomes: expected cost, life years and QALYs for the assessed and the comparator interventions.</li>
 * </ul>
 * The expected values do not come from the generator. They were computed independently, by enumerating the combinations of 
 * progressions of each model and computing the payoffs of each combination from the values of the ontology:
 * life years = max(0, LE / increased mortality rate - life expectancy reduction); annual costs and disutilities apply while the patient 
 * is alive and within the onset and end ages of the progression; one-time costs and disutilities apply once. With discount rate r, 
 * an annual payoff x that lasts n years and starts at year s is worth x * (1 - (1 + r)^-n) / r / (1 + r)^s (Excel's PV).
 * <p>
 * Common values (unless the model states otherwise): life expectancy 65 (80 for newborns), base utility 0.85, annual cost of the 
 * disease 1000, annual cost of the assessed intervention 100, no discount.
 * </p>
 * <p>
 * Some cases are marked with a {@link KnownIssue}: according to a review of the code, the generator does not yet produce the expected 
 * result for them. They are run by separate test methods, tagged as "known-issue", so that they can be excluded 
 * (<code>mvn test -DexcludedGroups=known-issue</code>). When an issue is fixed, simply remove the mark from the case.
 * </p>
 */
public final class DecisionTreeCatalog {
    /** Test ontology with different combinations of developments */
    public static final String DEVELOPMENTS = "/OSDi_test_tree_developments.ttl";
    /** Test ontology with different combinations of manifestations and developments */
    public static final String MANIFESTATIONS = "/OSDi_test_tree_manifestations.ttl";
    /** Test ontology with models that test one feature of costs, utilities, life expectancy or effects */
    public static final String PAYOFFS = "/OSDi_test_tree_payoffs.ttl";
    /** Test ontology with models that combine several features */
    public static final String SCENARIOS = "/OSDi_test_tree_scenarios.ttl";
    /** Test ontology with models that must be rejected */
    public static final String MALFORMED = "/OSDi_test_tree_malformed.ttl";
    /** The basic test ontology, also used by other tests */
    public static final String BASIC = "/OSDi_test.ttl";
    /** Key of the leaf where no development or manifestation appears */
    public static final String NO_PROGRESSION = "";
    /** Key of the leaf that represents the death at the onset of the disease */
    public static final String DEATH = "DEATH";

    /**
     * Reasons why the current generator is not expected to produce the right tree for a case.
     * <p>
     * A case marked with a known issue (see {@link TreeCase#knownIssue} and {@link TreeCase#knownOutcomesIssue}) is not checked by the regular
     * tests, but by the "...KnownIssues" tests, which are tagged as "known-issue" and are expected to fail until the defect is fixed. This way,
     * a defect can be documented with a test case before fixing it. Once fixed, the mark is removed from the case and its value from this enum.
     * </p>
     * There are no known issues at the moment, so the enum is empty and the "...KnownIssues" tests have no cases to run.
     */
    public enum KnownIssue {
        ;

        private final String description;

        private KnownIssue(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * A probability and the developments and manifestations that are active in a leaf of the tree 
     */
    public record Leaf(double probability, boolean death, String... progressions) {
    }

    /**
     * A decision tree model that must be generated correctly
     * @param ontology The resource with the ontology
     * @param experiment The name of the experiment
     * @param title What the case tests
     * @param leaves The expected leaves of each arm of the tree: the key identifies the developments and manifestations that are active 
     * in the leaf (names sorted and joined with '+'); the value is the probability of the leaf for the comparator intervention
     * @param assessed Expected cost, life years and QALYs for the assessed intervention (null if they are not checked)
     * @param comparator Expected cost, life years and QALYs for the comparator intervention (null if they are not checked)
     * @param structureIssue The reason why the leaves are not expected to be right with the current generator (null if they are)
     * @param outcomesIssue The reason why the outcomes are not expected to be right with the current generator (null if they are)
     */
    public record TreeCase(String ontology, String experiment, String title, Map<String, Double> leaves, double[] assessed, double[] comparator, 
            KnownIssue structureIssue, KnownIssue outcomesIssue) {

        /** Marks the case: neither its leaves nor its outcomes are expected to be right with the current generator */
        public TreeCase knownIssue(KnownIssue issue) {
            return new TreeCase(ontology, experiment, title, leaves, assessed, comparator, issue, issue);
        }

        /** Marks the case: its leaves are expected to be right, but not its outcomes */
        public TreeCase knownOutcomesIssue(KnownIssue issue) {
            return new TreeCase(ontology, experiment, title, leaves, assessed, comparator, structureIssue, issue);
        }

        @Override
        public final String toString() {
            return experiment + ": " + title;
        }
    }

    /**
     * A model that must be rejected when generating the decision tree
     * @param ontology The resource with the ontology
     * @param experiment The name of the experiment
     * @param title What the case tests
     * @param expectedException The exception that must be thrown
     */
    public record MalformedCase(String ontology, String experiment, String title, Class<? extends Exception> expectedException) {
        @Override
        public final String toString() {
            return experiment + ": " + title;
        }
    }

    private DecisionTreeCatalog() {
    }

    /**
     * Creates a leaf where the specified developments and manifestations are active
     * @param probability Probability of the leaf for the comparator intervention
     * @param progressions Names of the active developments and manifestations (without the prefix of the case). None if no progression appears
     * @return a leaf
     */
    static Leaf leaf(double probability, String... progressions) {
        return new Leaf(probability, false, progressions);
    }

    /**
     * Creates the leaf that represents the death at the onset of the disease
     * @param probability Probability of the leaf for the comparator intervention
     * @return a leaf
     */
    static Leaf death(double probability) {
        return new Leaf(probability, true);
    }

    /**
     * Creates the expected leaves of a tree
     * @param prefix Common prefix of the names of the individuals of the case
     * @param leaves The leaves
     * @return A map from the key of each leaf to its probability
     */
    static Map<String, Double> leaves(String prefix, Leaf... leaves) {
        final Map<String, Double> map = new TreeMap<>();
        for (Leaf leaf : leaves) {
            final String key = leaf.death() ? DEATH : leafKey(Arrays.stream(leaf.progressions()).map(name -> prefix + name).collect(Collectors.toList()));
            if (map.put(key, leaf.probability()) != null) {
                throw new IllegalArgumentException("Duplicated leaf: " + key);
            }
        }
        return map;
    }

    /**
     * Creates the key that identifies a leaf from the names of the developments and manifestations that are active in it
     * @param progressions Names of the active developments and manifestations
     * @return The names, sorted and joined with '+'
     */
    public static String leafKey(List<String> progressions) {
        return progressions.stream().sorted().collect(Collectors.joining("+"));
    }

    /** Expected cost, life years and QALYs of an intervention */
    private static double[] outcomes(double cost, double lifeYears, double qalys) {
        return new double[] {cost, lifeYears, qalys};
    }

    private static TreeCase tc(String ontology, String experiment, String title, Map<String, Double> leaves, double[] assessed, double[] comparator) {
        return new TreeCase(ontology, experiment, title, leaves, assessed, comparator, null, null);
    }

    /**
     * Returns all the cases of the catalog
     * @return all the cases of the catalog
     */
    public static List<TreeCase> allCases() {
        final List<TreeCase> cases = new ArrayList<>();

        // ---- Structure: developments ----
        // The disease defines neither developments nor manifestations.
        cases.add(tc(DEVELOPMENTS, "TT_S01_Experiment", "No progression: each arm is a single payoff",
                leaves("TT_S01_", leaf(1.0)),
                outcomes(71500.0, 65.0, 55.25), outcomes(65000.0, 65.0, 55.25)));
        // One development not affected by any rule: it either appears (0.3) or not.
        cases.add(tc(DEVELOPMENTS, "TT_S02_Experiment", "Single development with risk",
                leaves("TT_S02_", leaf(0.3, "DevA"), leaf(0.7)),
                outcomes(76450.0, 62.0, 51.05), outcomes(70250.0, 62.0, 51.05)));
        // A development without risk that does not belong to a group of alternatives is always active: there is no chance node.
        cases.add(tc(DEVELOPMENTS, "TT_S03_Experiment", "Single development without risk (always active, no chance node)",
                leaves("TT_S03_", leaf(1.0, "DevA")),
                outcomes(88000.0, 55.0, 41.25), outcomes(82500.0, 55.0, 41.25)));
        // Developments not affected by any rule are independent: 4 combinations.
        cases.add(tc(DEVELOPMENTS, "TT_S04_Experiment", "Two coexistent developments (no rule)",
                leaves("TT_S04_", leaf(0.15, "DevA", "DevB"), leaf(0.15, "DevA"), leaf(0.35, "DevB"), leaf(0.35)),
                outcomes(83790.0, 60.6, 48.38), outcomes(77730.0, 60.6, 48.38)));
        // Same as S04, but the developments are grouped by an explicit CoexistentCombinationRule.
        cases.add(tc(DEVELOPMENTS, "TT_S05_Experiment", "Two coexistent developments (explicit coexistent rule)",
                leaves("TT_S05_", leaf(0.15, "DevA", "DevB"), leaf(0.15, "DevA"), leaf(0.35, "DevB"), leaf(0.35)),
                outcomes(83790.0, 60.6, 48.38), outcomes(77730.0, 60.6, 48.38)));
        // Three mutually exclusive developments whose probabilities sum 1.
        cases.add(tc(DEVELOPMENTS, "TT_S06_Experiment", "Three alternative developments",
                leaves("TT_S06_", leaf(0.5, "DevA"), leaf(0.3, "DevB"), leaf(0.2, "DevC")),
                outcomes(86520.0, 58.8, 46.055), outcomes(80640.0, 58.8, 46.055)));
        // Two mutually exclusive developments plus a 'none of them' option (0.2).
        cases.add(tc(DEVELOPMENTS, "TT_S07_Experiment", "Alternative developments with null progression",
                leaves("TT_S07_", leaf(0.5, "DevA"), leaf(0.3, "DevB"), leaf(0.2)),
                outcomes(83920.0, 58.8, 46.315), outcomes(78040.0, 58.8, 46.315)));
        // Two independent sets of alternatives: {DevA, DevB (default)} x {DevC, DevD}.
        cases.add(tc(DEVELOPMENTS, "TT_S08_Experiment", "Two sets of alternative developments",
                leaves("TT_S08_", leaf(0.18, "DevA", "DevC"), leaf(0.42, "DevA", "DevD"), leaf(0.12, "DevB", "DevC"), leaf(0.28, "DevB", "DevD")),
                outcomes(94422.0, 57.4, 43.5238), outcomes(88682.0, 57.4, 43.5238)));
        // DevE is independent of the set of alternatives {DevA, DevB, none}.
        cases.add(tc(DEVELOPMENTS, "TT_S09_Experiment", "Coexistent development plus alternative developments with null progression",
                leaves("TT_S09_", leaf(0.2, "DevA", "DevE"), leaf(0.3, "DevA"), leaf(0.12, "DevB", "DevE"), leaf(0.18, "DevB"), leaf(0.08, "DevE"),
                    leaf(0.12)),
                outcomes(92176.0, 58.08, 43.891), outcomes(86368.0, 58.08, 43.891)));
        // The disease may cause sudden death (0.1); survivors may suffer DevA.
        cases.add(tc(DEVELOPMENTS, "TT_S10_Experiment", "Instant death at the onset of the disease plus one development",
                leaves("TT_S10_", death(0.1), leaf(0.27, "DevA"), leaf(0.63)),
                outcomes(69305.0, 55.8, 45.945), outcomes(63725.0, 55.8, 45.945)));
        // DevA has no risk and is not affected by any rule, so it is always active; DevB (0.5) either appears or not.
        cases.add(tc(DEVELOPMENTS, "TT_S20_Experiment", "Always active development (no risk) plus a coexistent development with risk",
                leaves("TT_S20_", leaf(0.5, "DevA", "DevB"), leaf(0.5, "DevA")),
                outcomes(96250.0, 55.0, 39.875), outcomes(90750.0, 55.0, 39.875)));
        // Same as S20, but with an explicit CoexistentCombinationRule: DevA is still always active.
        cases.add(tc(DEVELOPMENTS, "TT_S21_Experiment", "Always active development (no risk) plus a development with risk (explicit coexistent rule)",
                leaves("TT_S21_", leaf(0.5, "DevA", "DevB"), leaf(0.5, "DevA")),
                outcomes(96250.0, 55.0, 39.875), outcomes(90750.0, 55.0, 39.875)));
        // DevA is always active; DevC has no risk but belongs to a group of alternatives, so it takes the remaining probability.
        cases.add(tc(DEVELOPMENTS, "TT_S24_Experiment", "Always active development (no risk) plus alternative developments, one of them without risk",
                leaves("TT_S24_", leaf(0.5, "DevA", "DevB"), leaf(0.5, "DevA", "DevC")),
                outcomes(101750.0, 55.0, 39.325), outcomes(96250.0, 55.0, 39.325)));

        // ---- Structure: manifestations and their relationship with developments ----
        // Manifestations not linked to any development and not affected by any rule are independent.
        cases.add(tc(MANIFESTATIONS, "TT_S11_Experiment", "Two coexistent manifestations without developments",
                leaves("TT_S11_", leaf(0.1, "ManX", "ManY"), leaf(0.1, "ManX"), leaf(0.4, "ManY"), leaf(0.4)),
                outcomes(80195.0, 64.0, 52.126), outcomes(73795.0, 64.0, 52.126)));
        // Two mutually exclusive manifestations plus 'none of them' (0.2).
        cases.add(tc(MANIFESTATIONS, "TT_S12_Experiment", "Alternative manifestations with null progression, without developments",
                leaves("TT_S12_", leaf(0.5, "ManY"), leaf(0.3, "ManZ"), leaf(0.2)),
                outcomes(85100.0, 64.0, 51.73), outcomes(78700.0, 64.0, 51.73)));
        // ManX is independent of the set of alternatives {ManY, ManZ, none}: 6 combinations.
        cases.add(tc(MANIFESTATIONS, "TT_S13_Experiment", "Coexistent manifestation plus alternative manifestations",
                leaves("TT_S13_", leaf(0.2, "ManX", "ManY"), leaf(0.3, "ManY"), leaf(0.12, "ManX", "ManZ"), leaf(0.18, "ManZ"), leaf(0.08, "ManX"),
                    leaf(0.12)),
                outcomes(88940.0, 64.0, 50.962), outcomes(82540.0, 64.0, 50.962)));
        // ManY is a sub-progression of DevA: it can only appear together with DevA.
        cases.add(tc(MANIFESTATIONS, "TT_S14_Experiment", "Development with a linked manifestation",
                leaves("TT_S14_", leaf(0.15, "DevA", "ManY"), leaf(0.15, "DevA"), leaf(0.7)),
                outcomes(78512.5, 62.0, 50.555), outcomes(72312.5, 62.0, 50.555)));
        // ManY only appears with DevA and ManZ only with DevB: 9 combinations.
        cases.add(tc(MANIFESTATIONS, "TT_S15_Experiment", "Two coexistent developments, each one with its own manifestation",
                leaves("TT_S15_", leaf(0.03, "DevA", "DevB", "ManY", "ManZ"), leaf(0.045, "DevA", "DevB", "ManY"), leaf(0.03, "DevA", "DevB", "ManZ"),
                    leaf(0.045, "DevA", "DevB"), leaf(0.075, "DevA", "ManY"), leaf(0.075, "DevA"), leaf(0.14, "DevB", "ManZ"),
                    leaf(0.21, "DevB"), leaf(0.35)),
                outcomes(89996.5, 60.6, 47.4114), outcomes(83936.5, 60.6, 47.4114)));
        // DevA (0.6) may lead to ManY; DevB (default, 0.4) may lead to ManZ.
        cases.add(tc(MANIFESTATIONS, "TT_S16_Experiment", "Alternative developments, each one with its own manifestation",
                leaves("TT_S16_", leaf(0.3, "DevA", "ManY"), leaf(0.3, "DevA"), leaf(0.16, "DevB", "ManZ"), leaf(0.24, "DevB")),
                outcomes(94501.0, 57.4, 42.8896), outcomes(88761.0, 57.4, 42.8896)));
        // ManX appears whatever the development; ManY only with DevA: 6 combinations.
        cases.add(tc(MANIFESTATIONS, "TT_S17_Experiment", "Development with a linked manifestation plus an independent manifestation",
                leaves("TT_S17_", leaf(0.03, "DevA", "ManX", "ManY"), leaf(0.03, "DevA", "ManX"), leaf(0.12, "DevA", "ManY"), leaf(0.12, "DevA"),
                    leaf(0.14, "ManX"), leaf(0.56)),
                outcomes(80372.5, 62.0, 50.183), outcomes(74172.5, 62.0, 50.183)));
        // ManZ may appear if DevA, DevB or both are present (once): 7 combinations.
        cases.add(tc(MANIFESTATIONS, "TT_S18_Experiment", "Manifestation shared by two coexistent developments",
                leaves("TT_S18_", leaf(0.06, "DevA", "DevB", "ManZ"), leaf(0.09, "DevA", "DevB"), leaf(0.06, "DevA", "ManZ"), leaf(0.09, "DevA"),
                    leaf(0.14, "DevB", "ManZ"), leaf(0.21, "DevB"), leaf(0.35)),
                outcomes(89089.0, 60.6, 47.7744), outcomes(83029.0, 60.6, 47.7744)));
        // DevA (0.6) leads to ManY, ManZ or none of them; DevB (default) has no manifestations.
        cases.add(tc(MANIFESTATIONS, "TT_S19_Experiment", "Alternative developments, one of them with alternative manifestations",
                leaves("TT_S19_", leaf(0.3, "DevA", "ManY"), leaf(0.18, "DevA", "ManZ"), leaf(0.12, "DevA"), leaf(0.4, "DevB")),
                outcomes(94550.0, 57.4, 42.884), outcomes(88810.0, 57.4, 42.884)));
        // DevA has no risk, so it is always active; ManY (0.5) is a sub-progression of DevA.
        cases.add(tc(MANIFESTATIONS, "TT_S22_Experiment", "Always active development (no risk) with a linked manifestation",
                leaves("TT_S22_", leaf(0.5, "DevA", "ManY"), leaf(0.5, "DevA")),
                outcomes(94875.0, 55.0, 39.6), outcomes(89375.0, 55.0, 39.6)));
        // ManX is a sub-progression of DevB (0.5) without risk: it is always active when DevB is active.
        cases.add(tc(MANIFESTATIONS, "TT_S23_Experiment", "Development with a linked manifestation without risk",
                leaves("TT_S23_", leaf(0.5, "DevB", "ManX"), leaf(0.5)),
                outcomes(83025.0, 63.0, 51.11), outcomes(76725.0, 63.0, 51.11)));

        // ---- Costs, utilities, life expectancy and effects: one feature per case ----
        // A relative risk on a probability is applied to the equivalent rate: p' = 1 - (1 - 0.8)^0.5 = 0.552786404500042.
        // Assessed arm: cost = 71500 + 23000 p'; life years = 65 - 20 p'; QALYs = 55.25 - 35 p'
        cases.add(tc(PAYOFFS, "TT_P01_Experiment", "Intervention effect on a risk: relative risk (0.5), applied to the equivalent rate",
                leaves("TT_P01_", leaf(0.8, "Severe"), leaf(0.2, "Normal")),
                outcomes(84214.087303501, 53.944271910, 35.902475842), outcomes(85000.0, 49.0, 27.25)));
        cases.add(tc(PAYOFFS, "TT_P02_Experiment", "Intervention effect on a risk: difference (-0.3)",
                leaves("TT_P02_", leaf(0.8, "Severe"), leaf(0.2, "Normal")),
                outcomes(83000.0, 55.0, 37.75), outcomes(85000.0, 49.0, 27.25)));
        cases.add(tc(PAYOFFS, "TT_P03_Experiment", "Intervention effect on a risk: new value (0.25)",
                leaves("TT_P03_", leaf(0.8, "Severe"), leaf(0.2, "Normal")),
                outcomes(77250.0, 60.0, 46.5), outcomes(85000.0, 49.0, 27.25)));
        cases.add(tc(PAYOFFS, "TT_P04_Experiment", "Intervention effect on a cost: factor (x0.5)",
                leaves("TT_P04_", leaf(0.8, "Severe"), leaf(0.2, "Normal")),
                outcomes(71900.0, 49.0, 27.25), outcomes(85000.0, 49.0, 27.25)));
        cases.add(tc(PAYOFFS, "TT_P05_Experiment", "Intervention effect on a cost: difference (-300)",
                leaves("TT_P05_", leaf(0.8, "Severe"), leaf(0.2, "Normal")),
                outcomes(79100.0, 49.0, 27.25), outcomes(85000.0, 49.0, 27.25)));
        cases.add(tc(PAYOFFS, "TT_P06_Experiment", "Intervention effect on a disutility: difference (-0.1)",
                leaves("TT_P06_", leaf(0.8, "Severe"), leaf(0.2, "Normal")),
                outcomes(89900.0, 49.0, 30.85), outcomes(85000.0, 49.0, 27.25)));
        cases.add(tc(PAYOFFS, "TT_P07_Experiment", "Intervention effect on a life expectancy reduction: factor (x0.5)",
                leaves("TT_P07_", leaf(0.8, "Severe"), leaf(0.2, "Normal")),
                outcomes(106700.0, 57.0, 30.85), outcomes(85000.0, 49.0, 27.25)));
        cases.add(tc(PAYOFFS, "TT_P08_Experiment", "Intervention effect on a life expectancy reduction: difference (-5)",
                leaves("TT_P08_", leaf(0.8, "Severe"), leaf(0.2, "Normal")),
                outcomes(98300.0, 53.0, 29.05), outcomes(85000.0, 49.0, 27.25)));
        cases.add(tc(PAYOFFS, "TT_P09_Experiment", "Intervention effect on an increased mortality rate: factor (x0.75)",
                leaves("TT_P09_", leaf(0.8, "Severe"), leaf(0.2, "Normal")),
                outcomes(87100.0, 47.666666667, 26.65), outcomes(65000.0, 39.0, 22.75)));
        cases.add(tc(PAYOFFS, "TT_P10_Experiment", "One-time cost of a development",
                leaves("TT_P10_", leaf(0.8, "Severe"), leaf(0.2, "Normal")),
                outcomes(93900.0, 49.0, 27.25), outcomes(89000.0, 49.0, 27.25)));
        // Disutilities are added
        cases.add(tc(PAYOFFS, "TT_P11_Experiment", "One-time disutility of a development",
                leaves("TT_P11_", leaf(0.8, "Severe"), leaf(0.2, "Normal")),
                outcomes(89900.0, 49.0, 26.85), outcomes(85000.0, 49.0, 26.85)));
        // Annual disutilities take the maximum; one-time disutilities are always added
        cases.add(tc(PAYOFFS, "TT_P11_ExperimentMax", "One-time disutility of a development, with MAX combination of annual disutilities",
                leaves("TT_P11_", leaf(0.8, "Severe"), leaf(0.2, "Normal")),
                outcomes(89900.0, 49.0, 26.85), outcomes(85000.0, 49.0, 26.85)));
        cases.add(tc(PAYOFFS, "TT_P12_Experiment", "Utility (instead of disutility) of a development",
                leaves("TT_P12_", leaf(0.8, "Severe"), leaf(0.2, "Normal")),
                outcomes(89900.0, 49.0, 32.65), outcomes(85000.0, 49.0, 32.65)));
        cases.add(tc(PAYOFFS, "TT_P13_Experiment", "Costs and disutility of the intervention",
                leaves("TT_P13_", leaf(0.8, "Severe"), leaf(0.2, "Normal")),
                outcomes(91900.0, 49.0, 24.8), outcomes(85000.0, 49.0, 27.25)));
        cases.add(tc(PAYOFFS, "TT_P14_Experiment", "Cost from a different year (CPI update from 2013 to 2026)",
                leaves("TT_P14_", leaf(0.8, "Severe"), leaf(0.2, "Normal")),
                outcomes(100088.0, 49.0, 27.25), outcomes(95188.0, 49.0, 27.25)));
        cases.add(tc(PAYOFFS, "TT_P15_Experiment", "Discount rates (3% costs, 1.5% effects)",
                leaves("TT_P15_", leaf(0.8, "Severe"), leaf(0.2, "Normal")),
                outcomes(47451.073203228, 34.309426978, 18.746265035), outcomes(44920.518369566, 34.309426978, 18.746265035)));
        cases.add(tc(PAYOFFS, "TT_P16_ExperimentAdd", "Shared cost and disutility are added once per item (ADD)",
                leaves("TT_P16_", leaf(0.3, "DevA", "ManY"), leaf(0.3, "DevA"), leaf(0.4, "DevB")),
                outcomes(100750.0, 65.0, 49.4), outcomes(94250.0, 65.0, 49.4)));
        cases.add(tc(PAYOFFS, "TT_P16_ExperimentDistinct", "Shared cost and disutility are counted only once (ADD_DISTINCT)",
                leaves("TT_P16_", leaf(0.3, "DevA", "ManY"), leaf(0.3, "DevA"), leaf(0.4, "DevB")),
                outcomes(91000.0, 65.0, 51.35), outcomes(84500.0, 65.0, 51.35)));
        // No discount
        cases.add(tc(PAYOFFS, "TT_P17_Experiment", "Manifestation between two ages (newborns, 10 to 30 years)",
                leaves("TT_P17_", leaf(0.5, "ManY"), leaf(0.5)),
                outcomes(98000.0, 80.0, 67.0), outcomes(90000.0, 80.0, 67.0)));
        // Costs and disutilities that start in the future must be discounted to the present
        cases.add(tc(PAYOFFS, "TT_P17_ExperimentDiscount", "Manifestation between two ages, with discount (3%)",
                leaves("TT_P17_", leaf(0.5, "ManY"), leaf(0.5)),
                outcomes(38755.959046756, 30.200763446, 25.117137003), outcomes(35735.882702173, 30.200763446, 25.117137003)));
        cases.add(tc(PAYOFFS, "TT_P18_Experiment", "Manifestation from an onset age until death (newborns, from 10 years)",
                leaves("TT_P18_", leaf(0.5, "ManY"), leaf(0.5)),
                outcomes(123000.0, 80.0, 64.5), outcomes(115000.0, 80.0, 64.5)));
        cases.add(tc(PAYOFFS, "TT_P19_Experiment", "One-time cost at the onset age of a manifestation, with discount (newborns)",
                leaves("TT_P19_", leaf(0.5, "ManY"), leaf(0.5)),
                outcomes(35081.074577651, 30.200763446, 25.670648929), outcomes(32060.998233068, 30.200763446, 25.670648929)));
        // The onset is an age (30): for a population that starts at 20, it happens 10 years after the start of the model
        cases.add(tc(PAYOFFS, "TT_P20_Experiment", "One-time cost at the onset age of a manifestation, with discount (population starting at 20 years)",
                leaves("TT_P20_", leaf(0.5, "ManY"), leaf(0.5)),
                outcomes(33158.415457451, 28.452891518, 24.184957791), outcomes(30313.126305614, 28.452891518, 24.184957791)));
        // Life expectancy is 80: costs and disutilities stop at death
        cases.add(tc(PAYOFFS, "TT_P21_Experiment", "Manifestation whose end age exceeds the life expectancy (newborns, 70 to 90 years)",
                leaves("TT_P21_", leaf(0.5, "ManY"), leaf(0.5)),
                outcomes(93000.0, 80.0, 67.5), outcomes(85000.0, 80.0, 67.5)));
        // Life expectancy is 80: neither the annual payoffs nor the one-time cost of a manifestation that starts at 85 are accounted
        cases.add(tc(PAYOFFS, "TT_P22_Experiment", "Manifestation whose onset age exceeds the life expectancy (newborns, from 85 years)",
                leaves("TT_P22_", leaf(0.5, "ManY"), leaf(0.5)),
                outcomes(88000.0, 80.0, 68.0), outcomes(80000.0, 80.0, 68.0)));
        // The population starts at 20: the annual payoffs are accounted from the start, and the one-time cost (at 10 years) is not accounted
        cases.add(tc(PAYOFFS, "TT_P23_Experiment", "Manifestation whose onset age is lower than the initial age of the population (population starting at 20 years, from 10 years)",
                leaves("TT_P23_", leaf(0.5, "ManY"), leaf(0.5)),
                outcomes(104000.0, 65.0, 52.0), outcomes(97500.0, 65.0, 52.0)));
        // Assessed arm of P24-P27 (same payoffs as P01): cost = 71500 + 23000 p'; life years = 65 - 20 p'; QALYs = 55.25 - 35 p'.
        // Comparator arm: cost = 65000 + 25000 p; life years = 65 - 20 p; QALYs = 55.25 - 35 p
        // p' = 1 - (1 - 0.8)^0.25 = 0.331259695023
        cases.add(tc(PAYOFFS, "TT_P24_Experiment", "Intervention effect on a risk: hazard ratio (0.25), applied to the equivalent rate",
                leaves("TT_P24_", leaf(0.8, "Severe"), leaf(0.2, "Normal")),
                outcomes(79118.972985542, 58.374806100, 43.655910674), outcomes(85000.0, 49.0, 27.25)));
        // p' = 0.8 * 0.5 / (1 - 0.8 + 0.8 * 0.5) = 2/3
        cases.add(tc(PAYOFFS, "TT_P25_Experiment", "Intervention effect on a risk: odds ratio (0.5), applied to the odds",
                leaves("TT_P25_", leaf(0.8, "Severe"), leaf(0.2, "Normal")),
                outcomes(86833.333333333, 51.666666667, 31.916666667), outcomes(85000.0, 49.0, 27.25)));
        // P26 (hazard ratio on a time to event) is the malformed case N13 until times to event are supported in decision trees.
        // Effects are applied in the order: replace, multiply, subtract. p' = 0.6 * 0.5 - 0.1 = 0.2
        cases.add(tc(PAYOFFS, "TT_P27_Experiment", "Several intervention effects on the same risk: new value (0.6), factor (x0.5) and difference (-0.1)",
                leaves("TT_P27_", leaf(0.8, "Severe"), leaf(0.2, "Normal")),
                outcomes(76100.0, 61.0, 48.25), outcomes(85000.0, 49.0, 27.25)));
        // Acute manifestations (P28-P32): ManA is a single episode within the lifetime, with a one-time cost (2000) and a one-time QALY loss (0.5). No discount.
        // Comparator arm with a risk p: cost = 65 * 1000 + 2000 p; life years = 65; QALYs = 0.85 * 65 - 0.5 p. Assessed arm: + 65 * 100 = 6500 of cost.
        // p = 0.2: cost = 65400 (71900); QALYs = 55.15
        cases.add(tc(PAYOFFS, "TT_P28_Experiment", "Acute manifestation with a risk, a one-time cost and a one-time QALY loss: one episode within the lifetime",
                leaves("TT_P28_", leaf(0.2, "ManA"), leaf(0.8)),
                outcomes(71900.0, 65.0, 55.15), outcomes(65400.0, 65.0, 55.15)));
        // Same as P28: the annual cost (500) and the annual disutility (0.1) of an acute manifestation are ignored
        cases.add(tc(PAYOFFS, "TT_P29_Experiment", "Acute manifestation with annual payoffs, which are ignored with a warning",
                leaves("TT_P29_", leaf(0.2, "ManA"), leaf(0.8)),
                outcomes(71900.0, 65.0, 55.15), outcomes(65400.0, 65.0, 55.15)));
        // Same as P28: the frequency (12 episodes per year) is ignored, since the acute manifestation also defines a risk
        cases.add(tc(PAYOFFS, "TT_P30_Experiment", "Acute manifestation with a risk and a frequency: the frequency is ignored with a warning",
                leaves("TT_P30_", leaf(0.2, "ManA"), leaf(0.8)),
                outcomes(71900.0, 65.0, 55.15), outcomes(65400.0, 65.0, 55.15)));
        // p = 1: cost = 65000 + 2000 = 67000 (73500); QALYs = 55.25 - 0.5 = 54.75
        cases.add(tc(PAYOFFS, "TT_P31_Experiment", "Acute manifestation without risk: the episode always happens",
                leaves("TT_P31_", leaf(1.0, "ManA")),
                outcomes(73500.0, 65.0, 54.75), outcomes(67000.0, 65.0, 54.75)));
        // p' = 0.2 * 0.5 = 0.1: cost = 65000 + 6500 + 200 = 71700; QALYs = 55.25 - 0.05 = 55.2
        cases.add(tc(PAYOFFS, "TT_P32_Experiment", "Intervention effect on the risk of an acute manifestation: factor (x0.5)",
                leaves("TT_P32_", leaf(0.2, "ManA"), leaf(0.8)),
                outcomes(71700.0, 65.0, 55.2), outcomes(65400.0, 65.0, 55.15)));
        // Strategies (P33-P41). No discount; life years = 65 and QALYs = 0.85 * 65 = 55.25 in both arms, since the strategies have no utilities.
        // The costs of the strategies are added to the own costs of the disease (1000 per year). Assessed arm: + 100 per year.
        // Disease with S1 (follow-up, 200) and L1 (treatment, 300): comparator = 65 * 1500 = 97500
        cases.add(tc(PAYOFFS, "TT_P33_Experiment", "Strategy of the disease: its costs are added to the own costs of the disease",
                leaves("TT_P33_", leaf(1.0)),
                outcomes(104000.0, 65.0, 55.25), outcomes(97500.0, 65.0, 55.25)));
        // The new follow-up strategy S2 (50) replaces S1 and keeps L1: 65 * (1000 + 50 + 300 + 100) = 94250
        cases.add(tc(PAYOFFS, "TT_P34_Experiment", "Strategy replacement by type: a new follow-up strategy replaces the follow-up strategy of the disease and keeps its treatment strategy",
                leaves("TT_P34_", leaf(1.0)),
                outcomes(94250.0, 65.0, 55.25), outcomes(97500.0, 65.0, 55.25)));
        // S2 is assigned to a disease without strategies: 65 * (1000 + 50 + 100) = 74750
        cases.add(tc(PAYOFFS, "TT_P35_Experiment", "Strategy replacement on a health condition without strategies: the new strategy is assigned to it",
                leaves("TT_P35_", leaf(1.0)),
                outcomes(74750.0, 65.0, 55.25), outcomes(65000.0, 65.0, 55.25)));
        // T1: 10 per application, 1 every 730 hours = 12 per year, during 2 years = 240. T2 without guideline: 5 per year = 325.
        // Assessed: 65000 + 6500 + 240 + 325 = 72065
        cases.add(tc(PAYOFFS, "TT_P36_Experiment", "Costs of the health technologies of a strategy: with a guideline (frequency, period and duration) and without guideline",
                leaves("TT_P36_", leaf(1.0)),
                outcomes(72065.0, 65.0, 55.25), outcomes(65000.0, 65.0, 55.25)));
        // Only hasCount (3): 3 * 10 = 30 at the start. Assessed: 65000 + 6500 + 30 = 71530
        cases.add(tc(PAYOFFS, "TT_P37_Experiment", "Guideline with only a number of applications (hasCount): one-time cost at the start",
                leaves("TT_P37_", leaf(1.0)),
                outcomes(71530.0, 65.0, 55.25), outcomes(65000.0, 65.0, 55.25)));
        // 12 per year; L = MIN(2, 6 / 12) = 0.5 years: 6 * 10 = 60. Assessed: 71560
        cases.add(tc(PAYOFFS, "TT_P38_Experiment", "Guideline with frequency, duration and number of applications: the guideline ends with whatever comes first",
                leaves("TT_P38_", leaf(1.0)),
                outcomes(71560.0, 65.0, 55.25), outcomes(65000.0, 65.0, 55.25)));
        // 12 per year during [64, 66], limited by death at 65: 1 year * 12 * 10 = 120. Assessed: 71620
        cases.add(tc(PAYOFFS, "TT_P39_Experiment", "Guideline with a start offset: the applications after death are not accounted",
                leaves("TT_P39_", leaf(1.0)),
                outcomes(71620.0, 65.0, 55.25), outcomes(65000.0, 65.0, 55.25)));
        // T1 without its own guideline uses the guideline of the strategy (hasCount 3): 30. Assessed: 71530
        cases.add(tc(PAYOFFS, "TT_P40_Experiment", "Guideline of the strategy, used by a health technology without its own guideline",
                leaves("TT_P40_", leaf(1.0)),
                outcomes(71530.0, 65.0, 55.25), outcomes(65000.0, 65.0, 55.25)));
        // The group replaces S1 by S2 (50) and L1 by L2 (20): 65 * (1000 + 50 + 20 + 100) = 76050
        cases.add(tc(PAYOFFS, "TT_P41_Experiment", "Strategy replacement by a group: each member replaces the strategies of its type",
                leaves("TT_P41_", leaf(1.0)),
                outcomes(76050.0, 65.0, 55.25), outcomes(97500.0, 65.0, 55.25)));

        // ---- Scenarios: several features together ----
        // DevA (0.6) or DevB (default). DevA may lead to ManY; ManX may always appear. DevA and ManY share the cost of the follow-up visits (counted once). Disutilities take the maximum.
        cases.add(tc(SCENARIOS, "TT_I01_Experiment", "Chronic disease: alternative developments, linked and independent manifestations, shared costs, effects and discount",
                leaves("TT_I01_", leaf(0.06, "DevA", "ManX", "ManY"), leaf(0.06, "DevA", "ManX"), leaf(0.24, "DevA", "ManY"), leaf(0.24, "DevA"),
                    leaf(0.08, "DevB", "ManX"), leaf(0.32, "DevB")),
                outcomes(49179.593997019, 35.587273275, 25.253833187), outcomes(56068.863600865, 35.371309585, 23.047094457)));
        // Sudden death (0.05). Survivors may suffer ManX (from 0 to 10 years) and ManY (from 5 years until death, with a one-time cost at onset and 10 years of life lost). No discount.
        cases.add(tc(SCENARIOS, "TT_I02_Experiment", "Newborns: sudden death, coexistent manifestations with temporal constraints and intervention effects",
                leaves("TT_I02_", death(0.05), leaf(0.057, "ManX", "ManY"), leaf(0.228, "ManX"), leaf(0.133, "ManY"), leaf(0.532)),
                outcomes(92929.131474625, 77.22, 62.901293426), outcomes(84765.0, 74.1, 60.23)));
        // Target: the acute manifestation ManC. The tree starts with an episode (probability 1, its risk is ignored), and DevA and ManX are excluded. No discount.
        // Comparator: cost = 65 * 1000 + 2000 = 67000; QALYs = 0.85 * 65 - 0.5 = 54.75. Assessed: + 300 (one-time); the QALY loss is halved: 55.25 - 0.25 = 55
        cases.add(tc(SCENARIOS, "TT_I03_Experiment", "Interventions whose target is an acute manifestation: the tree starts with an episode and excludes the rest of the progressions",
                leaves("TT_I03_", leaf(1.0, "ManC")),
                outcomes(67300.0, 65.0, 55.0), outcomes(67000.0, 65.0, 54.75)));
        // Target: the development DevA (its risk is ignored). Its own manifestation ManY (0.5) is included; the independent ManX is excluded. No discount.
        // Life years = 65 - MAX(10, 2) = 55 in both leaves. DevA + ManY: cost = 55 * 1750 = 96250; QALYs = 55 * 0.69 = 37.95. DevA: cost = 55 * 1500 = 82500; QALYs = 55 * 0.75 = 41.25.
        // Comparator: cost = 89375; QALYs = 39.6. Assessed: + 55 * 100 = 5500 of cost
        cases.add(tc(SCENARIOS, "TT_I04_Experiment", "Interventions whose target is a development: the tree starts with the patients that present it and only includes its own manifestations",
                leaves("TT_I04_", leaf(0.5, "DevA", "ManY"), leaf(0.5, "DevA")),
                outcomes(94875.0, 55.0, 39.6), outcomes(89375.0, 55.0, 39.6)));

        // ---- Models of OSDi_test.ttl (their outcomes are checked in TestHTAExcelGeneration) ----
        cases.add(tc(BASIC, "TEST_Experiment1", "Test model 1: alternative developments",
                leaves("TEST_", leaf(0.8, "SevereDevelopment"), leaf(0.2, "NormalDevelopment")),
                null, null));
        cases.add(tc(BASIC, "TEST_Experiment2", "Test model 2: coexistent developments",
                leaves("TEST_", leaf(0.64, "CoexistentDevelopment1", "CoexistentDevelopment2"), leaf(0.16, "CoexistentDevelopment1"),
                    leaf(0.16, "CoexistentDevelopment2"), leaf(0.04)),
                null, null));
        cases.add(tc(BASIC, "TEST_Experiment3", "Test model 3: coexistent and alternative developments",
                leaves("TEST_", leaf(0.512, "CoexistentDevelopment1", "CoexistentDevelopment2", "SevereDevelopment"),
                    leaf(0.128, "CoexistentDevelopment1", "SevereDevelopment"), leaf(0.128, "CoexistentDevelopment2", "SevereDevelopment"),
                    leaf(0.032, "SevereDevelopment"), leaf(0.128, "CoexistentDevelopment1", "CoexistentDevelopment2", "NormalDevelopment"),
                    leaf(0.032, "CoexistentDevelopment1", "NormalDevelopment"), leaf(0.032, "CoexistentDevelopment2", "NormalDevelopment"),
                    leaf(0.008, "NormalDevelopment")),
                null, null));
        cases.add(tc(BASIC, "TEST_Experiment4Max", "Test model 4: coexistent developments that increase mortality",
                leaves("TEST_", leaf(0.64, "MortalityDevelopment1", "MortalityDevelopment2"), leaf(0.16, "MortalityDevelopment1"),
                    leaf(0.16, "MortalityDevelopment2"), leaf(0.04)),
                null, null));
        return cases;
    }

    /**
     * Returns the cases whose leaves must be right
     * @return the cases whose leaves must be right
     */
    public static Stream<TreeCase> structureCases() {
        return allCases().stream().filter(tc -> tc.structureIssue() == null);
    }

    /**
     * Returns the cases whose leaves are not expected to be right with the current generator
     * @return the cases whose leaves are not expected to be right with the current generator
     */
    public static Stream<TreeCase> structureKnownIssues() {
        return allCases().stream().filter(tc -> tc.structureIssue() != null);
    }

    /**
     * Returns the cases whose outcomes must be right
     * @return the cases whose outcomes must be right
     */
    public static Stream<TreeCase> outcomeCases() {
        return allCases().stream().filter(tc -> tc.assessed() != null && tc.outcomesIssue() == null);
    }

    /**
     * Returns the cases whose outcomes are not expected to be right with the current generator
     * @return the cases whose outcomes are not expected to be right with the current generator
     */
    public static Stream<TreeCase> outcomeKnownIssues() {
        return allCases().stream().filter(tc -> tc.assessed() != null && tc.outcomesIssue() != null);
    }

    /**
     * Returns the models that must be rejected
     * @return the models that must be rejected
     */
    public static Stream<MalformedCase> malformedCases() {
        return Stream.of(
            // Only one development of an alternative set can lack a risk (it takes the remaining probability).
            new MalformedCase(MALFORMED, "TT_N01_Experiment", "Alternative developments: two of them without risk", MalformedOSDiModelException.class),
            // The remaining probability cannot be assigned both to a default development and to the null progression.
            new MalformedCase(MALFORMED, "TT_N02_Experiment", "Alternative developments: null progression and a development without risk", MalformedOSDiModelException.class),
            // A combination rule must affect progressions of the same type.
            new MalformedCase(MALFORMED, "TT_N03_Experiment", "Combination rule that mixes a development and a manifestation", MalformedOSDiModelException.class),
            // Only probabilities, proportions, incidences... can characterize a risk in a decision tree.
            new MalformedCase(MALFORMED, "TT_N04_Experiment", "Risk of a development characterized by a parameter that is not a probability", UnsupportedOSDiFeatureException.class),
            // A shared cost cannot be counted once if it applies in different periods.
            new MalformedCase(MALFORMED, "TT_N05_Experiment", "ADD_DISTINCT with a cost shared by manifestations with different temporal constraints", UnsupportedOSDiFeatureException.class),
            // A decision tree represents a single episode of an acute manifestation, whose probability must be declared as a risk: a frequency alone is not enough.
            new MalformedCase(MALFORMED, "TT_N06_Experiment", "Acute manifestation characterized only by a frequency (not supported in decision trees)", UnsupportedOSDiFeatureException.class),
            // Decision trees assume that every patient starts without progressions.
            new MalformedCase(MALFORMED, "TT_N07_Experiment", "Initial proportion of a manifestation (not supported yet in decision trees)", UnsupportedOSDiFeatureException.class),
            // Two replacements of the same parameter are ambiguous.
            new MalformedCase(MALFORMED, "TT_N08_Experiment", "Two intervention effects replace the same risk", MalformedOSDiModelException.class),
            // Odds ratios and hazard ratios only apply to probabilities (and hazard ratios, also to times to event).
            new MalformedCase(MALFORMED, "TT_N09_Experiment", "Odds ratio applied to a cost", MalformedOSDiModelException.class),
            new MalformedCase(MALFORMED, "TT_N10_Experiment", "Hazard ratio applied to a cost", MalformedOSDiModelException.class),
            // Statistics of the uncertainty are not effect magnitudes.
            new MalformedCase(MALFORMED, "TT_N11_Experiment", "Effect magnitude defined as a standard deviation", MalformedOSDiModelException.class),
            // A relative risk is not defined on a time to event (a hazard ratio must be used instead).
            new MalformedCase(MALFORMED, "TT_N12_Experiment", "Relative risk applied to a time to event", UnsupportedOSDiFeatureException.class),
            // Decision trees do not support effects on a time to event yet (the probability derived from the time is not written in the Excel file).
            new MalformedCase(MALFORMED, "TT_N13_Experiment", "Hazard ratio applied to a time to event (not supported yet in decision trees)", UnsupportedOSDiFeatureException.class),
            // The compared interventions must have the same target (D1).
            new MalformedCase(MALFORMED, "TT_N14_Experiment", "Compared interventions with different targets", MalformedOSDiModelException.class),
            // Every intervention has exactly one target.
            new MalformedCase(MALFORMED, "TT_N15_Experiment", "Intervention with two targets", MalformedOSDiModelException.class),
            // A patient that presents a manifestation of a development also presents the development, but the manifestation may depend on several developments.
            new MalformedCase(MALFORMED, "TT_N16_Experiment", "Interventions whose target is a manifestation that depends on a development (not supported in decision trees)", UnsupportedOSDiFeatureException.class),
            // At most one strategy replacement per health condition and arm (D2).
            new MalformedCase(MALFORMED, "TT_N17_Experiment", "Two strategy replacements of the same health condition in the same arm", MalformedOSDiModelException.class),
            // Only a strategy replacement effect can affect a health condition.
            new MalformedCase(MALFORMED, "TT_N18_Experiment", "Generic effect that affects a health condition", MalformedOSDiModelException.class),
            // The decision tree does not support yet sub-guidelines, condition expressions or application ages in a guideline.
            new MalformedCase(MALFORMED, "TT_N19_Experiment", "Guideline with sub-guidelines (not supported yet)", UnsupportedOSDiFeatureException.class),
            // Detection properties are declared by the detection strategies, and the disease detected by an intervention is its target.
            new MalformedCase(MALFORMED, "TT_N20_Experiment", "Screening intervention with a sensitivity", MalformedOSDiModelException.class),
            new MalformedCase(MALFORMED, "TT_N21_Experiment", "Screening strategy in the arm without screening", MalformedOSDiModelException.class),
            new MalformedCase(MALFORMED, "TT_N22_Experiment", "Detection intervention that declares the disease it detects", MalformedOSDiModelException.class)
        );
    }
}
