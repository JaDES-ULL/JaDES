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
     * @param assessedLeaves The expected leaves of the arm of the assessed intervention, when they differ from those of the comparator (e.g., when the
     * intervention replaces a strategy with outcomes or adverse events); null if both arms have the same leaves
     */
    public record TreeCase(String ontology, String experiment, String title, Map<String, Double> leaves, double[] assessed, double[] comparator, 
            KnownIssue structureIssue, KnownIssue outcomesIssue, Map<String, Double> assessedLeaves) {

        /** Marks the case: neither its leaves nor its outcomes are expected to be right with the current generator */
        public TreeCase knownIssue(KnownIssue issue) {
            return new TreeCase(ontology, experiment, title, leaves, assessed, comparator, issue, issue, assessedLeaves);
        }

        /** Marks the case: its leaves are expected to be right, but not its outcomes */
        public TreeCase knownOutcomesIssue(KnownIssue issue) {
            return new TreeCase(ontology, experiment, title, leaves, assessed, comparator, structureIssue, issue, assessedLeaves);
        }

        /** Declares the leaves of the arm of the assessed intervention, when they differ from those of the comparator */
        public TreeCase withAssessedLeaves(Map<String, Double> assessedLeaves) {
            return new TreeCase(ontology, experiment, title, leaves, assessed, comparator, structureIssue, outcomesIssue, assessedLeaves);
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
    /**
     * A model that must be rejected
     * @param ontology The resource with the ontology
     * @param experiment The name of the experiment
     * @param title What the case tests
     * @param expectedException The type of the exception that must be thrown
     * @param expectedMessage A fragment of the message of the exception, which checks that the model is rejected for the right reason
     */
    public record MalformedCase(String ontology, String experiment, String title, Class<? extends Exception> expectedException, String expectedMessage) {
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
        return new TreeCase(ontology, experiment, title, leaves, assessed, comparator, null, null, null);
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
        // The risk is a time to event T = 2, whose probability is 1 - e^(-1/T). The hazard ratio divides the time: T' = 2 / 0.5 = 4.
        // p = 1 - e^(-1/2) = 0.393469340287; p' = 1 - e^(-1/4) = 0.221199216929
        cases.add(tc(PAYOFFS, "TT_P26_Experiment", "Intervention effect on a time to event: hazard ratio (0.5), which divides the time",
                leaves("TT_P26_", leaf(0.3934693402873666, "Severe"), leaf(0.6065306597126334, "Normal")),
                outcomes(76587.581989358, 60.576015661, 47.508027407), outcomes(74836.733507184, 57.130613194, 41.478573090)));
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
        // P28 with a frequency (0.1 episodes per year, phase 11): the risk is the probability of having episodes, and the one-time payoffs are multiplied
        // by the expected number of episodes in the lifetime, E = 0.1 * 65 = 6.5. Comparator: 65000 + 0.2 * 6.5 * 2000 = 67600; QALYs 55.25 - 0.2 * 6.5 * 0.5 = 54.6
        cases.add(tc(PAYOFFS, "TT_P30_Experiment", "Acute manifestation with a risk and a frequency (0.1 episodes per year, no horizon): the one-time payoffs are multiplied by the expected number of episodes in the lifetime",
                leaves("TT_P30_", leaf(0.2, "ManA"), leaf(0.8)),
                outcomes(74100.0, 65.0, 54.6), outcomes(67600.0, 65.0, 54.6)));
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
        // Strategy outcomes (P42-P46). No discount. L1: 300 per year; success 0.6 (continues L1) or failure 0.4 (followed by L2, 500 per year), evaluated at 2 years.
        // Success: L1 during 65 years = 19500. Failure: L1 during 2 years (600) + L2 during 63 years (31500) = 32100.
        // Comparator: 65000 + 0.6 * 19500 + 0.4 * 32100 = 89540. Assessed: + 6500
        cases.add(tc(PAYOFFS, "TT_P42_Experiment", "Strategy with a success that continues the treatment and a failure followed by another strategy, evaluated at 2 years",
                leaves("TT_P42_", leaf(0.6, "L1_Success"), leaf(0.4, "L1_Failure")),
                outcomes(96040.0, 65.0, 55.25), outcomes(89540.0, 65.0, 55.25)));
        // The failure also costs 1000 and loses 0.5 QALYs, once: + 0.4 * 1000 = 400; QALYs = 55.25 - 0.4 * 0.5 = 55.05
        cases.add(tc(PAYOFFS, "TT_P43_Experiment", "Strategy outcome with a one-time cost and a one-time QALY loss",
                leaves("TT_P43_", leaf(0.6, "L1_Success"), leaf(0.4, "L1_Failure")),
                outcomes(96440.0, 65.0, 55.05), outcomes(89940.0, 65.0, 55.05)));
        // The success cures the disease at 2 years: its annual cost and L1 stop. Success: 2 * 1000 + 2 * 300 = 2600. Failure: 65000 + 32100 = 97100.
        // Comparator: 0.6 * 2600 + 0.4 * 97100 = 40400. Assessed: + 6500 (the intervention applies during the whole life)
        cases.add(tc(PAYOFFS, "TT_P44_Experiment", "Strategy outcome that resolves (cures) a chronic disease at the evaluation time",
                leaves("TT_P44_", leaf(0.6, "L1_Success"), leaf(0.4, "L1_Failure")),
                outcomes(46900.0, 65.0, 55.25), outcomes(40400.0, 65.0, 55.25)));
        // L1 fails if DevA (0.3) appears, at time 0 (no evaluation time), and is followed by L2. DevA: LE 55; costs 55 * (1000 + 500 + 500) = 110000;
        // QALYs 55 * 0.75 = 41.25. No DevA: L1 during the whole life: 65000 + 19500 = 84500; QALYs 55.25.
        // Comparator: 92150; LE 0.3 * 55 + 0.7 * 65 = 62; QALYs 51.05. Assessed: + 0.3 * 5500 + 0.7 * 6500 = 6200
        cases.add(tc(PAYOFFS, "TT_P45_Experiment", "Short form of the outcomes: the strategy fails when a development appears (failsIf a health condition)",
                leaves("TT_P45_", leaf(0.3, "DevA", "L1_Failure"), leaf(0.7, "L1_Success")),
                outcomes(98350.0, 62.0, 51.05), outcomes(92150.0, 62.0, 51.05)));
        // Acute episode (2000, 0.5 QALYs), always present, treated with T (20): success 0.7 (resolves) or failure 0.3, followed by a rescue R (50) and losing 0.2 QALYs.
        // Comparator: 65000 + 2000 + 20 + 0.3 * 50 = 67035; QALYs 55.25 - 0.5 - 0.3 * 0.2 = 54.69. Assessed: + 6500
        cases.add(tc(PAYOFFS, "TT_P46_Experiment", "Treatment of an acute episode: the success resolves it and the failure is followed by a rescue treatment",
                leaves("TT_P46_", leaf(0.7, "ManA", "T_Success"), leaf(0.3, "ManA", "T_Failure")),
                outcomes(73535.0, 65.0, 54.69), outcomes(67035.0, 65.0, 54.69)));
        // Adverse events (P47-P49, D8). No discount. They happen each time a strategy is applied, independently of its outcome, with one-time payoffs.
        // P47 (as in Coco 2007): acute episode (2000, 0.5 QALYs), always present. Antibiotic A (20) with two independent adverse events:
        // diarrhoea (0.1; 30; 0.01 QALYs) and rash (0.05; 60; 0.02 QALYs). Expected per application of A: 20 + 0.1 * 30 + 0.05 * 60 = 26; 0.1 * 0.01 + 0.05 * 0.02 = 0.002 QALYs.
        // Assessed (immediate antibiotic, replaces W): 65000 + 2000 + 26 = 67026; QALYs 55.25 - 0.5 - 0.002 = 54.748.
        // Comparator: watchful waiting W (10): success 0.7 (resolves) or failure 0.3 (0.05 QALYs), followed by A.
        // 65000 + 2000 + 10 + 0.3 * 26 = 67017.8; QALYs 55.25 - 0.5 - 0.3 * 0.05 - 0.3 * 0.002 = 54.7344
        cases.add(tc(PAYOFFS, "TT_P47_Experiment", "Adverse events of an antibiotic, given at once or after the failure of watchful waiting (otitis media, as in Coco 2007)",
                // Comparator (watchful waiting): success 0.7; failure 0.3 * (0.1 * 0.05, 0.1 * 0.95, 0.9 * 0.05, 0.9 * 0.95)
                leaves("TT_P47_", leaf(0.7, "ManA", "W_Success"), leaf(0.0015, "ManA", "W_Failure", "Diarrhoea", "Rash"), leaf(0.0285, "ManA", "W_Failure", "Diarrhoea"),
                    leaf(0.0135, "ManA", "W_Failure", "Rash"), leaf(0.2565, "ManA", "W_Failure")),
                outcomes(67026.0, 65.0, 54.748), outcomes(67017.8, 65.0, 54.7344))
                // Immediate antibiotic: 0.1 * 0.05; 0.1 * 0.95; 0.9 * 0.05; 0.9 * 0.95
                .withAssessedLeaves(leaves("TT_P47_", leaf(0.005, "ManA", "Diarrhoea", "Rash"), leaf(0.095, "ManA", "Diarrhoea"), leaf(0.045, "ManA", "Rash"), leaf(0.855, "ManA"))));
        // P48: the episode (2000, 0.5 QALYs) is treated with A (20), which is abandoned if diarrhoea (0.1; 30; 0.01 QALYs) appears (failsIf), and then followed by B (40).
        // Comparator: 65000 + 2000 + 20 + 0.1 * (30 + 40) = 67027; QALYs 55.25 - 0.5 - 0.1 * 0.01 = 54.749. Assessed: + 6500
        cases.add(tc(PAYOFFS, "TT_P48_Experiment", "Abandonment of a treatment because of an adverse event (failsIf the adverse event), followed by another treatment",
                leaves("TT_P48_", leaf(0.1, "Diarrhoea", "ManA", "A_Failure"), leaf(0.9, "ManA", "A_Success")),
                outcomes(73527.0, 65.0, 54.749), outcomes(67027.0, 65.0, 54.749)));
        // P49: the disease is treated with A (20), whose anaphylaxis (0.01; 500) is fatal with probability 0.1 (at the start: life expectancy 0).
        // No adverse event (0.99): 65000 + 20 = 65020. Anaphylaxis, alive (0.009): 65520. Anaphylaxis, death (0.001): 20 + 500 = 520 (one-time costs at time 0).
        // Comparator: 64369.8 + 589.68 + 0.52 = 64960; life years 0.999 * 65 = 64.935; QALYs 0.999 * 55.25 = 55.19475. Assessed: + 100 * 64.935 = 71453.5
        cases.add(tc(PAYOFFS, "TT_P49_Experiment", "Fatal adverse event of a treatment of the disease (InstantDeathEffect of the adverse event)",
                leaves("TT_P49_", death(0.001), leaf(0.009, "Anaphylaxis"), leaf(0.99)),
                outcomes(71453.5, 64.935, 55.19475), outcomes(64960.0, 64.935, 55.19475)));
        // Relapse after the response (P50, D9). No discount. Acute episode (2000, 0.5 QALYs), always present, treated with A (100): failure 0.3 (failsIf),
        // followed by a rescue R (300) without follow-up; success (resolves), followed by the follow-up F (50), whose failure is the relapse (0.4 among
        // those who responded; 0.1 QALYs, once), followed by a second dose D (80).
        // Comparator: 65000 + 2000 + 100 + 0.3 * 300 + 0.7 * (50 + 0.4 * 80) = 67247.4; QALYs 55.25 - 0.5 - 0.7 * 0.4 * 0.1 = 54.722. Assessed: + 6500
        cases.add(tc(PAYOFFS, "TT_P50_Experiment", "Relapse after the response: the success is followed by a follow-up whose failure is the relapse, treated with a second dose; no relapse after the rescue",
                leaves("TT_P50_", leaf(0.3, "ManA", "A_Failure"), leaf(0.28, "ManA", "A_Success", "F_Relapse"), leaf(0.42, "ManA", "A_Success", "F_NoRelapse")),
                outcomes(73747.4, 65.0, 54.722), outcomes(67247.4, 65.0, 54.722)));
        // Time horizon (P51-P52). No discount. The life years of each leaf are limited to the horizon, and so are the payoffs that depend on them.
        // P51: horizon of 10 years. ManX (0.2): 500 and 0.1 of disutility per year, and 10 years of life lost (55 > 10, so it does not matter).
        // ManY (0.5) starts at 35 years, 15 years after the start, beyond the horizon: neither its one-time cost (2000) nor its annual cost (250) are accounted.
        // Comparator: 10 * 1000 + 0.2 * 10 * 500 = 11000; life years 10; QALYs 10 * 0.85 - 0.2 * 10 * 0.1 = 8.3. Assessed: + 10 * 100
        cases.add(tc(PAYOFFS, "TT_P51_Experiment", "Time horizon (10 years) shorter than the life expectancy: annual payoffs and QALYs limited to the horizon, and a manifestation that starts after it",
                leaves("TT_P51_", leaf(0.4), leaf(0.1, "ManX"), leaf(0.4, "ManY"), leaf(0.1, "ManX", "ManY")),
                outcomes(12000.0, 10.0, 8.3), outcomes(11000.0, 10.0, 8.3)));
        // P52: horizon of 24 hours (h = 1/365 years), as in Evans. Comparator: 1000 h; life years h; QALYs 0.85 h. Assessed: 1100 h
        cases.add(tc(PAYOFFS, "TT_P52_Experiment", "Time horizon of 24 hours (1/365 years), as in Evans: the annual payoffs and QALYs of the disease are limited to the horizon",
                leaves("TT_P52_", leaf(1.0)),
                outcomes(3.0136986301369864, 0.0027397260273972603, 0.002328767123287671), outcomes(2.73972602739726, 0.0027397260273972603, 0.002328767123287671)));
        // Times to event with a horizon (P53-P54, D6). A time to event T becomes the probability of the event within a period: 1 - exp(-period / T).
        // P53: P26 with a horizon of 10 years. p = 1 - exp(-10 / 2) = 0.993262053; with the hazard ratio (0.5), T' = 4: p' = 1 - exp(-10 / 4) = 0.917915001.
        // The life years of both leaves are 10 (Severe: 45; Normal: 65). Severe adds 1000 per year and 0.4 of disutility per year.
        // Comparator: 10000 + 10000 p = 19932.62053; QALYs 8.5 - 4 p = 4.526951788. Assessed: 11000 + 10000 p' = 20179.150014; QALYs 8.5 - 4 p' = 4.828339994
        cases.add(tc(PAYOFFS, "TT_P53_Experiment", "Time to event with a time horizon (10 years): the probability of the event within the horizon, 1 - exp(-H / T), with T divided by a hazard ratio (0.5)",
                leaves("TT_P53_", leaf(0.9932620530009145, "Severe"), leaf(0.006737946999085476, "Normal")),
                outcomes(20179.150013761013, 10.0, 4.828339994495595), outcomes(19932.620530009146, 10.0, 4.526951787996342)));
        // P54: P42 whose failure has a time to event T = 5, evaluated at 2 years (no horizon): p = 1 - exp(-2 / 5) = 0.329679954; the success is the default outcome.
        // Comparator: 65000 + (1 - p) * 19500 + p * 32100 = 84500 + 12600 p = 88653.96742. Assessed: + 6500
        cases.add(tc(PAYOFFS, "TT_P54_Experiment", "Strategy outcome whose risk is a time to event (5 years), evaluated at 2 years: probability 1 - exp(-2 / 5)",
                leaves("TT_P54_", leaf(0.6703200460356393, "L1_Success"), leaf(0.3296799539643607, "L1_Failure")),
                outcomes(95153.96741995095, 65.0, 55.25), outcomes(88653.96741995095, 65.0, 55.25)));
        // Frequency of an acute manifestation (P55-P56, phase 11): it does not change the tree; the one-time payoffs of the episode, of the strategies of the
        // acute manifestation and of their outcomes and adverse events are multiplied by the expected number of episodes, E = f * duration of the window of the leaf.
        // P55: horizon of 10 years; ManA (0.2) with 3 episodes per year (E = 30), 200 and 0.01 QALYs per episode; T (20 per episode) with nausea (0.1; 10).
        // ManA with nausea: 10000 + 30 * (200 + 20 + 10) = 16900; ManA without nausea: 10000 + 30 * 220 = 16600; no ManA: 10000. QALYs: 8.5 - 30 * 0.01 = 8.2 with ManA.
        // Comparator: 0.02 * 16900 + 0.18 * 16600 + 0.8 * 10000 = 11326; life years 10; QALYs 0.2 * 8.2 + 0.8 * 8.5 = 8.44. Assessed: + 10 * 100
        cases.add(tc(PAYOFFS, "TT_P55_Experiment", "Acute manifestation with a risk and a frequency (3 episodes per year, horizon of 10 years): the one-time payoffs of the episode, of its strategy and of the adverse event of the strategy are multiplied by the expected number of episodes",
                leaves("TT_P55_", leaf(0.02, "ManA", "Nausea"), leaf(0.18, "ManA"), leaf(0.8)),
                outcomes(12326.0, 10.0, 8.44), outcomes(11326.0, 10.0, 8.44)));
        // P56 (formerly N06): only a frequency (2 per year), so ManA is always present; horizon of 5 years: E = 10, 300 per episode.
        // Comparator: 5 * 1000 + 10 * 300 = 8000; life years 5; QALYs 4.25. Assessed: + 5 * 100
        cases.add(tc(PAYOFFS, "TT_P56_Experiment", "Acute manifestation characterized only by a frequency (2 episodes per year, horizon of 5 years): always present, with the expected number of episodes",
                leaves("TT_P56_", leaf(1.0, "ManA")),
                outcomes(8500.0, 5.0, 4.25), outcomes(8000.0, 5.0, 4.25)));
        // P57: costs in Canadian dollars of 1997, the year of the study (phase 12): they are not updated (the CPI table of the template starts in 2002).
        // Comparator: 65 * 1000 = 65000; assessed: 65 * (1000 + 100) = 71500. No discount.
        cases.add(tc(PAYOFFS, "TT_P57_Experiment", "Costs in Canadian dollars of 1997, the year of the study: they are not updated with the CPI",
                leaves("TT_P57_", leaf(1.0)),
                outcomes(71500.0, 65.0, 55.25), outcomes(65000.0, 65.0, 55.25)));
        // P58 (phase 12): a group of alternative strategies as the next strategy of an outcome. Acute episode (2000), always present, treated with T (20):
        // success 0.6 (resolves) or failure 0.4 (0.1 QALYs), followed by the group {R1 (0.7; 50), R2 (the rest; 300)}; R2 fails with 0.1, followed by H (1000).
        // Comparator: 65000 + 2000 + 20 + 0.4 * (0.7 * 50 + 0.3 * (300 + 0.1 * 1000)) = 67082; QALYs 55.25 - 0.4 * 0.1 = 55.21. Assessed: + 6500
        cases.add(tc(PAYOFFS, "TT_P58_Experiment", "Group of alternative rescue strategies after the failure of a treatment: R1 (0.7) or R2 (the rest), which has its own outcomes",
                leaves("TT_P58_", leaf(0.6, "ManA", "T_Success"), leaf(0.28, "ManA", "T_Failure", "R1"), leaf(0.108, "ManA", "T_Failure", "R2", "R2_Success"),
                    leaf(0.012, "ManA", "T_Failure", "R2", "R2_Failure")),
                outcomes(73582.0, 65.0, 55.21), outcomes(67082.0, 65.0, 55.21)));
        // P59 (phase 13, D10): the population declares neither age nor sex, so an initial age of 18 years is assumed (sex is not used by decision trees).
        // ManY (0.5) starts at 30 years, i.e., 12 years after the start; it lasts 53 years (life expectancy 65): 0.5 * (2000 + 53 * 250) = 7625. No discount.
        // Comparator: 65000 + 7625 = 72625 (70375 with the former initial age of 0: 0.5 * (2000 + 35 * 250)). Assessed: + 6500
        cases.add(tc(PAYOFFS, "TT_P59_Experiment", "Population without age or sex (D10): an initial age of 18 years is assumed to interpret the onset age of a manifestation",
                leaves("TT_P59_", leaf(0.5, "ManY"), leaf(0.5)),
                outcomes(79125.0, 65.0, 55.25), outcomes(72625.0, 65.0, 55.25)));
        // P60: the population does not declare a life expectancy, so a synthetic one of 100 years is assumed, with a warning. It must be written in the Excel
        // file, like any other parameter. Comparator: 100 * 1000 = 100000; life years 100; QALYs 100 * 0.85 = 85. Assessed: + 100 * 100
        cases.add(tc(PAYOFFS, "TT_P60_Experiment", "Population without life expectancy: a default life expectancy of 100 years is assumed, with a warning",
                leaves("TT_P60_", leaf(1.0)),
                outcomes(110000.0, 100.0, 85.0), outcomes(100000.0, 100.0, 85.0)));
        // P61 (phase 13, D5): P45 with DevA from 40 years (20 years after the start). L1 fails when DevA appears: at T = MAX(start of L1, 40 - 20) = 20.
        // DevA (0.3): LE 55; 55 * 1000 + 35 * 500 (DevA from 20 to 55) + 20 * 300 (L1 until 20) + 35 * 500 (L2 from 20) = 96000; QALYs 55 * 0.85 - 35 * 0.1 = 43.25.
        // No DevA (0.7): 84500; 65; 55.25. Comparator: 87950; life years 62; QALYs 51.65. Assessed: + 0.3 * 5500 + 0.7 * 6500 = 6200
        cases.add(tc(PAYOFFS, "TT_P61_Experiment", "The strategy fails when a development appears at 40 years (failsIf a health condition with an onset age, D5)",
                leaves("TT_P61_", leaf(0.3, "DevA", "L1_Failure"), leaf(0.7, "L1_Success")),
                outcomes(94150.0, 62.0, 51.65), outcomes(87950.0, 62.0, 51.65)));
        // P62 (phase 13): the assessed intervention does not declare its target, so it targets the disease, with a warning (see TestDecisionTreeWarnings).
        // Comparator: 65000; assessed: 65 * (1000 + 100) = 71500
        cases.add(tc(PAYOFFS, "TT_P62_Experiment", "Intervention without target in the model: it is assumed to target the disease, with a warning",
                leaves("TT_P62_", leaf(1.0)),
                outcomes(71500.0, 65.0, 55.25), outcomes(65000.0, 65.0, 55.25)));
        // P63: the population does not declare a base utility, so a base utility of 1 is assumed, with a warning. Comparator: 65000; QALYs 65. Assessed: 71500
        cases.add(tc(PAYOFFS, "TT_P63_Experiment", "Population without base utility: a base utility of 1 is assumed, with a warning",
                leaves("TT_P63_", leaf(1.0)),
                outcomes(71500.0, 65.0, 65.0), outcomes(65000.0, 65.0, 65.0)));
        // P64 (phase 15): P56 with ManA as the target of the interventions. The tree evaluates a single episode, the one at time 0, and ignores the
        // frequency with a warning (see TestDecisionTreeWarnings); the DES also simulates the next episodes (accepted divergence).
        // Horizon of 5 years: comparator 5 * 1000 + 300 = 5300; life years 5; QALYs 4.25. Assessed: + 50 (one-time cost of the intervention).
        // With the frequency (10 episodes), the comparator would be 8000, as in P56.
        cases.add(tc(PAYOFFS, "TT_P64_Experiment", "Acute manifestation with a frequency that is the target of the interventions: the tree evaluates a single episode",
                leaves("TT_P64_", leaf(1.0, "ManA")),
                outcomes(5350.0, 5.0, 4.25), outcomes(5300.0, 5.0, 4.25)));
        // P65 (phase 16): fatal adverse event of a strategy that starts later. No discount. L1 (20, once): failure 0.4, evaluated at 10 years and followed
        // by R (200, once), whose anaphylaxis (0.05; 500, once) is fatal with probability 0.2; success, the default. The death happens at the start of R (T = 10).
        // Success (0.6): 65020; 65; 55.25. Failure without anaphylaxis (0.38): 65220. Anaphylaxis, alive (0.016): 65720.
        // Anaphylaxis, death (0.004): 10 * 1000 + 20 + 200 + 500 = 10720; life years 10; QALYs 8.5.
        // Comparator: 39012 + 24783.6 + 1051.52 + 42.88 = 64890; life years 0.996 * 65 + 0.004 * 10 = 64.78; QALYs 0.996 * 55.25 + 0.004 * 8.5 = 55.063.
        // Assessed: + 100 * 64.78 = 71368
        cases.add(tc(PAYOFFS, "TT_P65_Experiment", "Fatal adverse event of a strategy that starts at 10 years: the death happens at the start of the strategy",
                leaves("TT_P65_", death(0.004), leaf(0.016, "L1_Failure", "Anaphylaxis"), leaf(0.38, "L1_Failure"), leaf(0.6, "L1_Success")),
                outcomes(71368.0, 64.78, 55.063), outcomes(64890.0, 64.78, 55.063)));
        // P66 (phase 16): ManX (0.2), from 40 years (T = 40 - 20 = 20), 150 per year, with an instant death of probability 0.1 at its onset. No discount.
        // No ManX (0.8): 65000; 65; 55.25. ManX, alive (0.18): 65000 + 150 * 45 = 71750; 65; 55.25. ManX, death (0.02): life years MIN(20, 65) = 20; 20000; QALYs 17.
        // Comparator: 52000 + 12915 + 400 = 65315; life years 0.98 * 65 + 0.02 * 20 = 64.1; QALYs 54.145 + 0.34 = 54.485. Assessed: + 100 * 64.1 = 71725
        cases.add(tc(PAYOFFS, "TT_P66_Experiment", "Instant death at the onset of a chronic manifestation from 40 years: the life years are cut at its onset",
                leaves("TT_P66_", death(0.02), leaf(0.18, "ManX"), leaf(0.8)),
                outcomes(71725.0, 64.1, 54.485), outcomes(65315.0, 64.1, 54.485)));
        // P67 (phase 16): ManX, chronic, with an instant death (0.1), is the target of the interventions. The patients present it at time 0, so they survived its
        // onset: the death is ignored with a warning, as that of the disease (see TestDecisionTreeWarnings). Comparator: 65 * (1000 + 150) = 74750; 65; 55.25.
        // Assessed: + 65 * 100 = 81250
        cases.add(tc(PAYOFFS, "TT_P67_Experiment", "Chronic target with an instant death: the patients survived its onset, so the death is ignored",
                leaves("TT_P67_", leaf(1.0, "ManX")),
                outcomes(81250.0, 65.0, 55.25), outcomes(74750.0, 65.0, 55.25)));
        // P68 (phase 16): ManA, acute, with an instant death (0.1), is the target of the interventions. Every patient suffers the episode at time 0, within
        // the model, so the death applies at T = 0. Alive (0.9): 65 * 1000 + 300 = 65300; 65; 55.25. Death (0.1): life years 0; 300 (the one-time cost of the
        // episode, at time 0); QALYs 0. Comparator: 58770 + 30 = 58800; life years 58.5; QALYs 49.725. Assessed: + 50 (one-time cost of the intervention) = 58850
        cases.add(tc(PAYOFFS, "TT_P68_Experiment", "Acute target with an instant death: the episode happens at time 0, so the death applies at time 0",
                leaves("TT_P68_", death(0.1), leaf(0.9, "ManA")),
                outcomes(58850.0, 58.5, 49.725), outcomes(58800.0, 58.5, 49.725)));

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
        // Evans et al. (1997), sumatriptan versus caffeine/ergotamine for a migraine attack (phase 12). Horizon of 24 hours (1/365 years), no discount, base utility 1,
        // costs in CAD of 1997. Each arm replaces the treatment of the attack (the target) by its drug: dose c, no relief f (1.3/365 QALYs), followed by a rescue at
        // home (0.92) or in the emergency department (0.08; 63.16), whose relief has a QALY loss of -0.4/365 and whose failure (0.002) leads to a hospitalization (1093).
        // The relief of the drug is followed by a follow-up whose failure is the relapse r (0.1/365 QALYs), followed by another dose, which always relieves.
        // Cost = c + (1 - f) r c + 0.08 f (63.16 + 0.002 * 1093); QALY loss = 1.3 f - 0.4 * 0.08 * 0.998 f + 0.1 (1 - f) r (x 1/365).
        // Caffeine/ergotamine (c = 1.32, f = 0.621, r = 0.297): 4.71497244; QALYs (1 - 0.798724044) / 365 = 0.201275956 / 365.
        // Sumatriptan (c = 16.10, f = 0.442, r = 0.406): 22.05805736; QALYs 0.416860912 / 365. ICER 29363 CAD/QALY (29366 in Evans et al.)
        cases.add(tc(SCENARIOS, "TT_I05_Experiment", "Evans et al. (1997): sumatriptan versus caffeine/ergotamine for a migraine attack, with rescue and relapse after the relief",
                leaves("TT_I05_", leaf(0.26643700000000003, "Attack", "Cafergot_Relief", "CafergotFollowUp_NoRelapse"), leaf(0.112563, "Attack", "Cafergot_Relief", "CafergotFollowUp_Relapse"),
                    leaf(0.57132, "Attack", "Cafergot_NoRelief", "Home"), leaf(0.04958064, "Attack", "Cafergot_NoRelief", "EmergencyDepartment", "EmergencyDepartment_Relief"),
                    leaf(9.936000000000001e-05, "Attack", "Cafergot_NoRelief", "EmergencyDepartment", "EmergencyDepartment_NoRelief")),
                outcomes(22.058057360000003, 0.0027397260273972603, 0.001142084690410959), outcomes(4.71497244, 0.0027397260273972603, 0.0005514409753424658))
                .withAssessedLeaves(leaves("TT_I05_", leaf(0.331452, "Attack", "Sumatriptan_Relief", "SumatriptanFollowUp_NoRelapse"),
                    leaf(0.22654800000000003, "Attack", "Sumatriptan_Relief", "SumatriptanFollowUp_Relapse"), leaf(0.40664, "Attack", "Sumatriptan_NoRelief", "Home"),
                    leaf(0.03528928, "Attack", "Sumatriptan_NoRelief", "EmergencyDepartment", "EmergencyDepartment_Relief"),
                    leaf(7.072e-05, "Attack", "Sumatriptan_NoRelief", "EmergencyDepartment", "EmergencyDepartment_NoRelief"))));

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
            new MalformedCase(MALFORMED, "TT_N01_Experiment", "Alternative developments: two of them without risk", MalformedOSDiModelException.class,
                "Multiple developments without risk parameter are not allowed in an alternative combination rule"),
            // The remaining probability cannot be assigned both to a default development and to the null progression.
            new MalformedCase(MALFORMED, "TT_N02_Experiment", "Alternative developments: null progression and a development without risk", MalformedOSDiModelException.class,
                "is affected by an alternative combination rule that is supposed to include a null progression"),
            // A combination rule must affect progressions of the same type.
            new MalformedCase(MALFORMED, "TT_N03_Experiment", "Combination rule that mixes a development and a manifestation", MalformedOSDiModelException.class,
                "TT_N03_MixedRule affects progressions of different types"),
            // Only probabilities, proportions, incidences... can characterize a risk in a decision tree.
            new MalformedCase(MALFORMED, "TT_N04_Experiment", "Risk of a development characterized by a parameter that is not a probability", UnsupportedOSDiFeatureException.class,
                "for the parameter TT_N04_DevA_Risk cannot be used as a probability in the decision tree"),
            // A shared cost cannot be counted once if it applies in different periods.
            new MalformedCase(MALFORMED, "TT_N05_Experiment", "ADD_DISTINCT with a cost shared by manifestations with different temporal constraints", UnsupportedOSDiFeatureException.class,
                "The cost parameter TT_N05_SharedCost is shared by several items with different temporal constraints"),
            // N06 (acute manifestation with only a frequency) became P56 when the frequency was supported in decision trees (phase 11).
            // Decision trees assume that every patient starts without progressions.
            new MalformedCase(MALFORMED, "TT_N07_Experiment", "Initial proportion of a manifestation (not supported yet in decision trees)", UnsupportedOSDiFeatureException.class,
                "defines an initial proportion (hasInitialProportion) for the progression TT_N07_ManX"),
            // Two replacements of the same parameter are ambiguous.
            new MalformedCase(MALFORMED, "TT_N08_Experiment", "Two intervention effects replace the same risk", MalformedOSDiModelException.class,
                "modify TT_N08_Severe_Risk, and 2 of them replace its value"),
            // Odds ratios and hazard ratios only apply to probabilities (and hazard ratios, also to times to event).
            new MalformedCase(MALFORMED, "TT_N09_Experiment", "Odds ratio applied to a cost", MalformedOSDiModelException.class,
                "on TT_N09_Severe_Cost (of type Currency_Euro): an odds ratio can only modify a probability"),
            new MalformedCase(MALFORMED, "TT_N10_Experiment", "Hazard ratio applied to a cost", MalformedOSDiModelException.class,
                "on TT_N10_Severe_Cost (of type Currency_Euro): a hazard ratio can only modify a probability or a time to event"),
            // Statistics of the uncertainty are not effect magnitudes.
            new MalformedCase(MALFORMED, "TT_N11_Experiment", "Effect magnitude defined as a standard deviation", MalformedOSDiModelException.class,
                "a statistic of the uncertainty cannot be the magnitude of an effect"),
            // A relative risk is not defined on a time to event (a hazard ratio must be used instead).
            new MalformedCase(MALFORMED, "TT_N12_Experiment", "Relative risk applied to a time to event", UnsupportedOSDiFeatureException.class,
                "a relative risk cannot modify a time to event"),
            // N13 (hazard ratio on a time to event) became P26 again when times to event were supported in decision trees.
            // The compared interventions must have the same target (D1).
            new MalformedCase(MALFORMED, "TT_N14_Experiment", "Compared interventions with different targets", MalformedOSDiModelException.class,
                "The compared interventions must have the same target"),
            // Every intervention has exactly one target.
            new MalformedCase(MALFORMED, "TT_N15_Experiment", "Intervention with two targets", MalformedOSDiModelException.class,
                "The intervention TT_N15_InterventionTreat defines more than one target in the model"),
            // A patient that presents a manifestation of a development also presents the development, but the manifestation may depend on several developments.
            new MalformedCase(MALFORMED, "TT_N16_Experiment", "Interventions whose target is a manifestation that depends on a development (not supported in decision trees)", UnsupportedOSDiFeatureException.class,
                "The interventions of the model target the manifestation TT_N16_ManY, which depends on the development TT_N16_DevA"),
            // At most one strategy replacement per health condition and arm (D2).
            new MalformedCase(MALFORMED, "TT_N17_Experiment", "Two strategy replacements of the same health condition in the same arm", MalformedOSDiModelException.class,
                "defines two strategy replacements (TT_N17_Replacement1 and TT_N17_Replacement2) of the same health condition"),
            // Only a strategy replacement effect can affect a health condition.
            new MalformedCase(MALFORMED, "TT_N18_Experiment", "Generic effect that affects a health condition", MalformedOSDiModelException.class,
                "Only a strategy replacement effect (StrategyReplacementEffect) can affect a health condition"),
            // The decision tree does not support yet sub-guidelines, condition expressions or application ages in a guideline.
            new MalformedCase(MALFORMED, "TT_N19_Experiment", "Guideline with sub-guidelines (not supported yet)", UnsupportedOSDiFeatureException.class,
                "The guideline TT_N19_G1 defines hasSubGuideline"),
            // Detection properties are declared by the detection strategies, and the disease detected by an intervention is its target.
            new MalformedCase(MALFORMED, "TT_N20_Experiment", "Screening intervention with a sensitivity", MalformedOSDiModelException.class,
                "The detection intervention TT_N20_Screening declares hasSensitivity"),
            new MalformedCase(MALFORMED, "TT_N21_Experiment", "Screening strategy in the arm without screening", MalformedOSDiModelException.class,
                "uses the screening strategy TT_N21_ScreeningStrategy, but it is not a screening intervention"),
            new MalformedCase(MALFORMED, "TT_N22_Experiment", "Detection intervention that declares the disease it detects", MalformedOSDiModelException.class,
                "The detection intervention TT_N22_Screening declares detects"),
            // Strategy outcomes (phase 7a): decision trees cannot expand cycles nor times to event, and outcomes must be well defined.
            new MalformedCase(MALFORMED, "TT_N23_Experiment", "Cycle in the chain of strategies", UnsupportedOSDiFeatureException.class,
                "goes back to the strategy TT_N23_L1. Cycles are not supported in decision trees"),
            new MalformedCase(MALFORMED, "TT_N24_Experiment", "Strategy outcome whose risk is a time to event, without evaluation time or time horizon", UnsupportedOSDiFeatureException.class,
                "is a time to event, but neither the outcome defines an evaluation time (hasEvaluationTime) nor the experiment a time horizon"),
            new MalformedCase(MALFORMED, "TT_N25_Experiment", "Two strategy outcomes without risk", MalformedOSDiModelException.class,
                "The strategy TT_N25_L1 has 2 outcomes without risk"),
            new MalformedCase(MALFORMED, "TT_N26_Experiment", "Short form (failsIf) combined with explicit strategy outcomes", MalformedOSDiModelException.class,
                "The strategy TT_N26_L1 combines the short form (failsIf) with explicit outcomes"),
            new MalformedCase(MALFORMED, "TT_N27_Experiment", "Two detection outcomes without risk for the patients with the disease", MalformedOSDiModelException.class,
                "The detection strategy TT_N27_S1 has 2 outcomes without risk for the individuals with the disease"),
            new MalformedCase(MALFORMED, "TT_N28_Experiment", "Inconclusive detection outcome followed by a strategy that is not a detection strategy", MalformedOSDiModelException.class,
                "is followed by the strategy TT_N28_FollowUp, which is not a detection strategy"),
            new MalformedCase(MALFORMED, "TT_N29_Experiment", "Cycle in the chain of detection strategies", UnsupportedOSDiFeatureException.class,
                "goes back to the strategy TT_N29_S1. Cycles are not supported in decision trees"),
            new MalformedCase(MALFORMED, "TT_N30_Experiment", "Confirmation of the positive results with a diagnosis strategy with explicit outcomes (not supported yet)", UnsupportedOSDiFeatureException.class,
                "The diagnosis strategy TT_N30_Diagnosis of the arm of the intervention TT_N30_NoScreening defines explicit outcomes"),
            // The risk of an adverse event is mandatory (D8).
            new MalformedCase(MALFORMED, "TT_N31_Experiment", "Adverse event without risk", MalformedOSDiModelException.class,
                "The adverse event TT_N31_Diarrhoea does not define a risk"),
            // The risk of an adverse event is a probability per application of the strategy in decision trees (D6, D8).
            new MalformedCase(MALFORMED, "TT_N32_Experiment", "Adverse event whose risk is a time to event (not supported in decision trees)", UnsupportedOSDiFeatureException.class,
                "The risk of the adverse event TT_N32_Diarrhoea of the strategy TT_N32_A is a time to event"),
            // A group of strategies as the next strategy is a choice among alternatives: its members must declare their proportions (phase 12).
            new MalformedCase(MALFORMED, "TT_N33_Experiment", "Group of strategies without proportions as the next strategy of an outcome", UnsupportedOSDiFeatureException.class,
                "but its strategies do not declare their proportions (hasProportionWithinGroup)"),
            new MalformedCase(MALFORMED, "TT_N34_Experiment", "Group of alternative strategies whose proportions do not add up to 1, without a member without proportion", MalformedOSDiModelException.class,
                "and none of them takes the rest"),
            // Every patient presents the target of the interventions at time 0 (D1, phase 15), so the target cannot define an onset or end age (as in the DES).
            new MalformedCase(MALFORMED, "TT_N35_Experiment", "Interventions whose target defines a temporal constraint (onset age)", UnsupportedOSDiFeatureException.class,
                "The interventions of the model target TT_N35_ManX, which defines a temporal constraint (onset or end age)"),
            // The effects of an acute manifestation on the life expectancy are not multiplied by its expected number of episodes (phase 16).
            new MalformedCase(MALFORMED, "TT_N36_Experiment", "Acute manifestation with a frequency and a life expectancy reduction", UnsupportedOSDiFeatureException.class,
                "The acute manifestation TT_N36_ManA defines a frequency (TT_N36_ManA_Frequency) and an effect on the life expectancy (TT_N36_ManA_LER)")
        );
    }
}
