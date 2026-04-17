package es.ull.simulation.hta.osdi.ontology;

import org.semanticweb.owlapi.io.IRIDocumentSource;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import es.ull.simulation.ontology.OntologyLoader;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Objects;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class TestOntology {

    private static Stream<Arguments> provideLocalOntologyTestCases() {
        return Stream.of(
        Arguments.of("/T1DM.ttl", "T1DM_ExperimentBase"),
        Arguments.of("/PBD.ttl", "PBD_ExperimentBase"),
        Arguments.of("/OSDi_Test.ttl", "TEST_Experiment1")
        );
    }    

    @ParameterizedTest
    @MethodSource("provideLocalOntologyTestCases")
    @OSDiTest
    public void testLocalOntologyConsistency(String ontologyFile, String experimentName) throws Exception {
        OWLOntologyDocumentSource source = new StreamDocumentSource(Objects.requireNonNull(
                TestOntology.class.getResourceAsStream(ontologyFile), "Ontology file not found in resources: " + ontologyFile));
        final OSDiWrapper wrapper = new OSDiWrapper.Builder().build(source);
        final OWLOntology ontology = wrapper.getOntology();
        assertNotNull(ontology, "Ontology must not be null for file: " + ontologyFile);
        ExperimentWrapper experimentWrapper = wrapper.buildExperiment(wrapper.toIRI(experimentName));
        assertNotNull(experimentWrapper, "Experiment wrapper must not be null for file: " + ontologyFile);

        OWLReasoner reasoner = wrapper.getReasoner();
        boolean consistent = reasoner.isConsistent();
        reasoner.dispose();
        assertTrue(consistent, "Ontology for " + ontologyFile + " must be consistent");
    }


    private static Stream<Arguments> provideRemoteOntologyTestCases() {
        return Stream.of(
        Arguments.of(OSDiWrapper.OSDI_IRI + "/individuals/T1DM.ttl", "T1DM_StdModelDES"),
        Arguments.of(OSDiWrapper.OSDI_IRI + "/individuals/PBD.ttl", "PBD_ModelTree")
        );
    }    

    @ParameterizedTest
    @MethodSource("provideRemoteOntologyTestCases")
    @OSDiTest
    public void testRemoteOntologyConsistency(String individualsIRI, String experimentName) throws Exception {
        OWLOntologyDocumentSource source = OntologyLoader.getOntologyDocumentSourceFromString(individualsIRI);
        final OSDiWrapper wrapper = new OSDiWrapper.Builder(new IRIDocumentSource(Objects.requireNonNull(OSDiWrapper.OSDI_IRI))).build(source);
        final OWLOntology ontology = wrapper.getOntology();
        assertNotNull(ontology, "Ontology must not be null for IRI: " + individualsIRI);
        ExperimentWrapper experimentWrapper = wrapper.buildExperiment(wrapper.toIRI(experimentName));
        assertNotNull(experimentWrapper, "Experiment wrapper must not be null for remote individuals in: " + individualsIRI);

        OWLReasoner reasoner = wrapper.getReasoner();
        boolean consistent = reasoner.isConsistent();
        reasoner.dispose();
        assertTrue(consistent, "Ontology for " + individualsIRI + " must be consistent");
    }
}

