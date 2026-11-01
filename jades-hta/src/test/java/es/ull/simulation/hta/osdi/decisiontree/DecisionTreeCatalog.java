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
     * Reasons why the current generator is not expected to produce the right tree for a case 
     */
    public enum KnownIssue {
        /** ModelSheet.drawPayoffNode sets a probability of 0 (instead of 1) to a payoff that hangs directly from an intervention, so every outcome of the arm is 0 */
        ROOT_PAYOFF("ModelSheet.drawPayoffNode sets a probability of 0 (instead of 1) to a payoff that hangs directly from an intervention, so every outcome of the arm is 0"),
        /** Synthetic parameters (e.g. the default risk of a development without risk) are not returned by ModelWrapper.getParameters(), so they are not written in the Parameters sheet and the formulas that use them are not valid */
        SYNTHETIC("Synthetic parameters (e.g. the default risk of a development without risk) are not returned by ModelWrapper.getParameters(), so they are not written in the Parameters sheet and the formulas that use them are not valid");

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
                outcomes(71500.0, 65.0, 55.25), outcomes(65000.0, 65.0, 55.25))
                .knownOutcomesIssue(KnownIssue.ROOT_PAYOFF));
        // One development not affected by any rule: it either appears (0.3) or not.
        cases.add(tc(DEVELOPMENTS, "TT_S02_Experiment", "Single development with risk",
                leaves("TT_S02_", leaf(0.3, "DevA"), leaf(0.7)),
                outcomes(76450.0, 62.0, 51.05), outcomes(70250.0, 62.0, 51.05)));
        // A development with no risk and no rule is assumed to always appear.
        cases.add(tc(DEVELOPMENTS, "TT_S03_Experiment", "Single development without risk (default risk of 1)",
                leaves("TT_S03_", leaf(1.0, "DevA"), leaf(0.0)),
                outcomes(88000.0, 55.0, 41.25), outcomes(82500.0, 55.0, 41.25))
                .knownOutcomesIssue(KnownIssue.SYNTHETIC));
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

        // ---- Costs, utilities, life expectancy and effects: one feature per case ----
        cases.add(tc(PAYOFFS, "TT_P01_Experiment", "Intervention effect on a risk: relative risk (x0.5)",
                leaves("TT_P01_", leaf(0.8, "Severe"), leaf(0.2, "Normal")),
                outcomes(80700.0, 57.0, 41.25), outcomes(85000.0, 49.0, 27.25)));
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

        // ---- Scenarios: several features together ----
        // DevA (0.6) or DevB (default). DevA may lead to ManY; ManX may always appear. DevA and ManY share the cost of the follow-up visits (counted once). Disutilities take the maximum.
        cases.add(tc(SCENARIOS, "TT_I01_Experiment", "Chronic disease: alternative developments, linked and independent manifestations, shared costs, effects and discount",
                leaves("TT_I01_", leaf(0.06, "DevA", "ManX", "ManY"), leaf(0.06, "DevA", "ManX"), leaf(0.24, "DevA", "ManY"), leaf(0.24, "DevA"),
                    leaf(0.08, "DevB", "ManX"), leaf(0.32, "DevB")),
                outcomes(46537.225884784, 35.650025718, 25.895043969), outcomes(56068.863600865, 35.371309585, 23.047094457)));
        // Sudden death (0.05). Survivors may suffer ManX (from 0 to 10 years) and ManY (from 5 years until death, with a one-time cost at onset and 10 years of life lost). No discount.
        cases.add(tc(SCENARIOS, "TT_I02_Experiment", "Newborns: sudden death, coexistent manifestations with temporal constraints and intervention effects",
                leaves("TT_I02_", death(0.05), leaf(0.057, "ManX", "ManY"), leaf(0.228, "ManX"), leaf(0.133, "ManY"), leaf(0.532)),
                outcomes(92665.0, 77.22, 62.9145), outcomes(84765.0, 74.1, 60.23)));

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
            new MalformedCase(MALFORMED, "TT_N05_Experiment", "ADD_DISTINCT with a cost shared by manifestations with different temporal constraints", UnsupportedOSDiFeatureException.class)
        );
    }
}
