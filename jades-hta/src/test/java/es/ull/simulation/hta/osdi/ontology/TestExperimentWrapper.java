package es.ull.simulation.hta.osdi.ontology;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Objects;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import es.ull.simulation.hta.outcomes.DisutilityCombinationMethod;

/**
 * Tests how the time horizon of an experiment (hasTimeHorizon) is written and read. OSDi defines it as a double, whereas the models
 * work with whole years.
 */
public class TestExperimentWrapper {
    private final static String INDIVIDUALS_ONTOLOGY = "/OSDi_test.ttl";
    private final static String MODEL = "TEST_DecisionTreeModel1";
    private OSDiWrapper wrap;
    private IRI experimentIRI;

    @BeforeEach
    public void setUp() throws Exception {
        wrap = new OSDiWrapper.Builder().build(new StreamDocumentSource(Objects.requireNonNull(
                getClass().getResourceAsStream(INDIVIDUALS_ONTOLOGY), "Ontology file not found in resources: " + INDIVIDUALS_ONTOLOGY)));
        experimentIRI = wrap.toIRI("TEST_ExperimentTimeHorizon");
        ExperimentWrapper.create(wrap, experimentIRI, wrap.toIRI(MODEL), DisutilityCombinationMethod.ADD, 2026, 40, 0.0, 0.0, 0, 100);
    }

    @Test
    @OSDiTest
    @DisplayName("The time horizon is written as a double and read back")
    public void testCreatedTimeHorizon() throws Exception {
        final Set<OWLLiteral> values = wrap.getValues(experimentIRI, OSDiDataProperty.HAS_TIME_HORIZON);
        assertEquals(1, values.size(), "The experiment must define a single time horizon");
        assertTrue(values.iterator().next().getDatatype().getIRI().equals(OWL2Datatype.XSD_DOUBLE.getIRI()), "The time horizon must be written as a double, as defined in OSDi");
        assertEquals(40, wrap.buildExperiment(experimentIRI).getTimeHorizon());
    }

    @Test
    @OSDiTest
    @DisplayName("An integer time horizon is still read")
    public void testIntegerTimeHorizon() throws Exception {
        replaceTimeHorizon("35", OWL2Datatype.XSD_INTEGER);
        assertEquals(35, wrap.buildExperiment(experimentIRI).getTimeHorizon());
    }

    @Test
    @OSDiTest
    @DisplayName("A time horizon that is not a whole number of years is rounded")
    public void testRoundedTimeHorizon() throws Exception {
        replaceTimeHorizon("40.6", OWL2Datatype.XSD_DOUBLE);
        assertEquals(41, wrap.buildExperiment(experimentIRI).getTimeHorizon());
        replaceTimeHorizon("40.4", OWL2Datatype.XSD_DOUBLE);
        assertEquals(40, wrap.buildExperiment(experimentIRI).getTimeHorizon());
    }

    @Test
    @OSDiTest
    @DisplayName("The default time horizon is used if it is not defined or not valid")
    public void testDefaultTimeHorizon() throws Exception {
        assertEquals(100, wrap.buildExperiment(wrap.toIRI("TEST_Experiment1")).getTimeHorizon(), "TEST_Experiment1 does not define a time horizon");
        replaceTimeHorizon("abc", OWL2Datatype.XSD_STRING);
        assertEquals(100, wrap.buildExperiment(experimentIRI).getTimeHorizon());
    }

    /**
     * Replaces the time horizon of the experiment created for the tests
     * @param value The new value
     * @param type The datatype of the new value
     */
    private void replaceTimeHorizon(String value, OWL2Datatype type) {
        final OWLOntology ontology = wrap.getOntology();
        final IRI timeHorizonIRI = wrap.toIRI(OSDiDataProperty.HAS_TIME_HORIZON);
        ontology.getOWLOntologyManager().removeAxioms(ontology,
            ontology.dataPropertyAssertionAxioms(wrap.getDataFactory().getOWLNamedIndividual(experimentIRI))
                .filter(axiom -> axiom.getProperty().asOWLDataProperty().getIRI().equals(timeHorizonIRI)));
        wrap.assertDataProperty(experimentIRI, OSDiDataProperty.HAS_TIME_HORIZON, value, type);
    }
}
