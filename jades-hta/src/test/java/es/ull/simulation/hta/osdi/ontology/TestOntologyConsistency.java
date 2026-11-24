package es.ull.simulation.hta.osdi.ontology;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

/**
 * Checks with a complete reasoner (HermiT) that the OSDi ontology and the test ontologies are consistent (phase 17 of docs/osdi-1.3-diseno.md).
 * JaDES uses the StructuralReasoner, which does not check consistency, so a modeling error such as an individual that violates a cardinality
 * restriction would go unnoticed. The ontologies are loaded as they are written, without the changes that {@link OSDiWrapper} makes after loading.
 * <p>
 * Besides, the reasoner must not infer that two different diseases are the same individual: it would happen, for instance, if an intervention,
 * which has exactly one target, were shared by the models of several diseases.
 * </p>
 * <p>
 * The ontology of malformed models is checked case by case: some of its cases are inconsistent on purpose, since the reasoner detects the same
 * error that JaDES rejects (see {@link #INCONSISTENT_MALFORMED_CASES}).
 * </p>
 */
@OSDiTest
@DisplayName("Consistency of the OSDi ontology and the test ontologies with a complete reasoner")
public class TestOntologyConsistency {
    /** The local copies of the ontologies imported by OSDi (see {@link OSDiWrapper}) */
    private static final Map<String, String> LOCAL_IMPORTS = Map.of(
        "http://www.w3.org/2004/02/skos/core", "/skos.rdf",
        "http://www.w3.org/2006/time", "/time.owl",
        "http://www.w3.org/2006/time#2016", "/time.owl");
    /** The ontology of malformed models, checked case by case */
    private static final String MALFORMED = "OSDi_test_tree_malformed.ttl";
    /**
     * The malformed cases that are inconsistent on purpose: N15 (an intervention with two targets, the disease and a manifestation, which the
     * cardinality makes equal although they are disjoint); N20 and N22 (a screening intervention that declares hasSensitivity or detects, whose
     * domain is DetectionStrategy, disjoint with Intervention).
     */
    private static final Set<String> INCONSISTENT_MALFORMED_CASES = Set.of("N15", "N20", "N22");
    /** The case of an individual of the tree test ontologies, e.g., N15 in TT_N15_Disease */
    private static final Pattern CASE = Pattern.compile("^TT_([A-Z]\\d\\d)_");

    private int count = 0;

    /**
     * Creates a manager that solves the imports of OSDi with the local copies, and loads the local copy of OSDi.
     */
    private static OWLOntologyManager createManager() throws Exception {
        final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        for (Map.Entry<String, String> entry : LOCAL_IMPORTS.entrySet()) {
            final URL resource = Objects.requireNonNull(TestOntologyConsistency.class.getResource(entry.getValue()), entry.getValue());
            manager.getIRIMappers().add(new SimpleIRIMapper(IRI.create(entry.getKey()), IRI.create(resource)));
        }
        final URL osdi = Objects.requireNonNull(TestOntologyConsistency.class.getResource("/OSDi.ttl"));
        final OWLOntology core = manager.loadOntologyFromOntologyDocument(IRI.create(osdi));
        // The test ontologies import the version IRI of OSDi, which is solved with the local copy
        final IRI versionIRI = core.getOntologyID().getVersionIRI().orElseThrow();
        manager.getIRIMappers().add(new SimpleIRIMapper(versionIRI, IRI.create(osdi)));
        return manager;
    }

    private static OWLOntology loadTestOntology(OWLOntologyManager manager, String file) throws Exception {
        try (InputStream stream = TestOntologyConsistency.class.getResourceAsStream("/" + file)) {
            assertNotNull(stream, "Test ontology not found: " + file);
            return manager.loadOntologyFromOntologyDocument(stream);
        }
    }

    private static OWLReasoner createReasoner(OWLOntology ontology) {
        final Configuration configuration = new Configuration();
        // OWL-Time uses datatypes that HermiT does not support (e.g., xsd:gYear); they are irrelevant for OSDi
        configuration.ignoreUnsupportedDatatypes = true;
        return new ReasonerFactory().createReasoner(ontology, configuration);
    }

    /**
     * Checks that an ontology is consistent and that no two different diseases are inferred to be the same individual.
     */
    private static void assertConsistent(OWLOntology ontology, String name) {
        final OWLReasoner reasoner = createReasoner(ontology);
        try {
            assertTrue(reasoner.isConsistent(), name + " is not consistent with OSDi");
            final OWLClass disease = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IRI.create(OSDiWrapper.OSDI_IRI + "#Disease"));
            final Set<String> merged = new TreeSet<>();
            for (OWLNamedIndividual individual : reasoner.getInstances(disease, false).getFlattened()) {
                final Set<OWLNamedIndividual> same = reasoner.getSameIndividuals(individual).getEntities();
                if (same.size() > 1) {
                    merged.add(same.stream().map(i -> i.getIRI().getShortForm()).sorted().collect(Collectors.joining(" = ")));
                }
            }
            assertTrue(merged.isEmpty(), name + ": the reasoner infers that different diseases are the same individual: " + merged);
        }
        finally {
            reasoner.dispose();
        }
    }

    private static boolean isConsistent(OWLOntology ontology) {
        final OWLReasoner reasoner = createReasoner(ontology);
        try {
            return reasoner.isConsistent();
        }
        finally {
            reasoner.dispose();
        }
    }

    private static Set<String> getCases(OWLAxiom axiom) {
        final Set<String> cases = new TreeSet<>();
        for (OWLNamedIndividual individual : axiom.getIndividualsInSignature()) {
            final Matcher matcher = CASE.matcher(individual.getIRI().getShortForm());
            if (matcher.find()) {
                cases.add(matcher.group(1));
            }
        }
        return cases;
    }

    @Test
    @DisplayName("OSDi.ttl is consistent")
    void osdiIsConsistent() throws Exception {
        final OWLOntologyManager manager = createManager();
        final OWLOntology core = manager.getOntologies().stream()
            .filter(o -> o.getOntologyID().getOntologyIRI().map(OSDiWrapper.OSDI_IRI::equals).orElse(false)).findFirst().orElseThrow();
        assertConsistent(core, "OSDi.ttl");
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = { "OSDi_test.ttl", "OSDi_test_des.ttl", "OSDi_test_tree_developments.ttl", "OSDi_test_tree_manifestations.ttl",
        "OSDi_test_tree_nbs.ttl", "OSDi_test_tree_payoffs.ttl", "OSDi_test_tree_scenarios.ttl", "PBD.ttl", "BD.ttl", "T1DM.ttl" })
    @DisplayName("Each test ontology is consistent with OSDi, and its diseases are different individuals")
    void testOntologyIsConsistent(String file) throws Exception {
        final OWLOntologyManager manager = createManager();
        assertConsistent(loadTestOntology(manager, file), file);
    }

    @Test
    @DisplayName("Each case of the malformed models is consistent with OSDi, except those that are inconsistent on purpose (N15, N20, N22)")
    void malformedCasesAreConsistent() throws Exception {
        final OWLOntologyManager manager = createManager();
        final OWLOntology ontology = loadTestOntology(manager, MALFORMED);
        final IRI version = ontology.getImportsDeclarations().iterator().next().getIRI();
        final Set<String> cases = new TreeSet<>();
        ontology.getAxioms().forEach(axiom -> cases.addAll(getCases(axiom)));
        assertTrue(cases.containsAll(INCONSISTENT_MALFORMED_CASES), "Missing malformed cases: " + INCONSISTENT_MALFORMED_CASES);
        // Each case with the items shared by all the cases, without the axioms of the other cases
        final Set<String> inconsistent = new TreeSet<>();
        for (String c : cases) {
            final List<OWLAxiom> axioms = new ArrayList<>();
            for (OWLAxiom axiom : ontology.getAxioms()) {
                final Set<String> axiomCases = getCases(axiom);
                if (axiomCases.isEmpty() || axiomCases.equals(Set.of(c))) {
                    axioms.add(axiom);
                }
            }
            if (!isConsistent(createOntology(manager, version, axioms))) {
                inconsistent.add(c);
            }
        }
        assertEquals(new TreeSet<>(INCONSISTENT_MALFORMED_CASES), inconsistent, "Inconsistent malformed cases");
        // The rest of cases together, to check that their diseases are different individuals
        final List<OWLAxiom> rest = ontology.getAxioms().stream()
            .filter(axiom -> getCases(axiom).stream().noneMatch(INCONSISTENT_MALFORMED_CASES::contains)).collect(Collectors.toList());
        assertConsistent(createOntology(manager, version, rest), MALFORMED + " without " + INCONSISTENT_MALFORMED_CASES);
    }

    private OWLOntology createOntology(OWLOntologyManager manager, IRI imported, List<OWLAxiom> axioms) throws Exception {
        final OWLOntology ontology = manager.createOntology(IRI.create("urn:test:consistency:" + (count++)));
        manager.applyChange(new AddImport(ontology, manager.getOWLDataFactory().getOWLImportsDeclaration(imported)));
        manager.addAxioms(ontology, axioms.stream());
        return ontology;
    }
}
