package es.ull.simulation.hta.osdi.ontology;

import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Objects;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests to check whether the local version of the OSDi ontology complies with the expected structure defined in this project.
 */
public class TestOSDiCompliance {
    private final static String INDIVIDUALS_ONTOLOGY = "/OSDi_Test.ttl";
    private static OSDiWrapper wrapper;
    private static OWLOntology ontology;

    @Test
    @OSDiTest
    @DisplayName("Test ontology consistency")
    public void testOntologyConsistency() throws Exception {
        assertNotNull(ontology, "The local version of OSDi ontology must be loaded");
        OWLReasoner reasoner = wrapper.getReasoner();
        boolean consistent = reasoner.isConsistent();
        reasoner.dispose();
        assertTrue(consistent, "Ontology must be consistent");
    }

    @Test
    @OSDiTest
    @DisplayName("Test classes")
    public void testClasses() throws Exception {
        // Test all expected classes are present
        for (OSDiClass cls : OSDiClass.values()) {
            if (cls.isCore()) {
                final IRI clsIRI = entityIRI(cls.getShortName());
                assertNotNull(clsIRI, "Class IRI must not be null for: " + cls.getShortName());
                assertTrue(ontology.containsClassInSignature(clsIRI, Imports.INCLUDED),
                        "Ontology must contain class: " + cls.getShortName());
                assertTrue(wrapper.findOWLClass(clsIRI).isPresent(), "Class must be retrievable: " + cls.getShortName());
            }
        }
        // Complementary test: check that a non-existing class is indeed not present
        final String wrongClass = "NonExistingClass";
        final IRI wrongClassIRI = entityIRI(wrongClass);
        assertNotNull(wrongClassIRI, "Class IRI must not be null for: " + wrongClass);
        assertTrue(!ontology.containsClassInSignature(wrongClassIRI, Imports.INCLUDED),
                "Ontology must not contain class: " + wrongClass);
        assertTrue(wrapper.findOWLClass(wrongClassIRI).isEmpty(), "Class must not be retrievable: " + wrongClass);
    }

    @Test
    @OSDiTest
    @DisplayName("Test properties")
    public void testProperties() throws Exception {
        // Test all expected data properties are present
        for (OSDiDataProperty prop : OSDiDataProperty.values()) {
            final IRI propIRI = entityIRI(prop.getShortName());
            assertNotNull(propIRI, "Data property IRI must not be null for: " + prop.getShortName());
            assertTrue(ontology.containsDataPropertyInSignature(propIRI, Imports.INCLUDED),
                    "Ontology must contain data property: " + prop.getShortName());
            assertTrue(wrapper.findOWLDataProperty(propIRI).isPresent(), "Data property must be retrievable: " + prop.getShortName());
        }
        // Complementary test: check that a non-existing property is indeed not present
        final String wrongProperty = "NonExistingProperty";
        final IRI wrongPropertyIRI = entityIRI(wrongProperty);
        assertNotNull(wrongPropertyIRI, "Data property IRI must not be null for: " + wrongProperty);
        assertTrue(!ontology.containsDataPropertyInSignature(wrongPropertyIRI, Imports.INCLUDED),
                "Ontology must not contain data property: " + wrongProperty);
        assertTrue(wrapper.findOWLDataProperty(wrongPropertyIRI).isEmpty(), "Data property must not be retrievable: " + wrongProperty);
        // Test all expected object properties are present
        for (OSDiObjectProperty prop : OSDiObjectProperty.values()) {
            if (prop.isCore()) {
                final IRI propIRI = entityIRI(prop.getShortName());
                assertNotNull(propIRI, "Object property IRI must not be null for: " + prop.getShortName());
                assertTrue(ontology.containsObjectPropertyInSignature(propIRI, Imports.INCLUDED),
                        "Ontology must contain object property: " + prop.getShortName());
                assertTrue(wrapper.findOWLObjectProperty(propIRI).isPresent(), "Object property must be retrievable: " + prop.getShortName());
            }
        }
        // Complementary test: check that a non-existing property is indeed not present
        final String wrongObjProperty = "NonExistingObjectProperty";
        final IRI wrongObjPropertyIRI = entityIRI(wrongObjProperty);
        assertNotNull(wrongObjPropertyIRI, "Object property IRI must not be null for: " + wrongObjProperty);
        assertTrue(!ontology.containsObjectPropertyInSignature(wrongObjPropertyIRI, Imports.INCLUDED),
                "Ontology must not contain object property: " + wrongObjProperty);
        assertTrue(wrapper.findOWLObjectProperty(wrongObjPropertyIRI).isEmpty(), "Object property must not be retrievable: " + wrongObjProperty);
    }

    @Test
    @OSDiTest
    @DisplayName("Test individuals")
    public void testIndividuals() throws Exception {
        for (OSDiDataItemType type : OSDiDataItemType.values()) {
            final IRI individualIRI = entityIRI(type.getShortName());
            assertNotNull(individualIRI, "Individual IRI must not be null for: " + type.getShortName());
            assertTrue(ontology.containsIndividualInSignature(individualIRI, Imports.INCLUDED),
                    "Ontology must contain individual: " + type.getShortName());
            assertTrue(wrapper.findOWLIndividual(individualIRI).isPresent(), "Individual must be retrievable: " + type.getShortName());
        }
        // Complementary test: check that a non-existing individual is indeed not present
        final String wrongIndividual = "NonExistingIndividual";
        final IRI wrongIndividualIRI = entityIRI(wrongIndividual);
        assertNotNull(wrongIndividualIRI, "Individual IRI must not be null for: " + wrongIndividual);
        assertTrue(!ontology.containsIndividualInSignature(wrongIndividualIRI, Imports.INCLUDED),
                "Ontology must not contain individual: " + wrongIndividual);
        assertTrue(wrapper.findOWLIndividual(wrongIndividualIRI).isEmpty(), "Individual must not be retrievable: " + wrongIndividual);
    }

    @BeforeAll
    public static void setUp() throws Exception {
        OWLOntologyDocumentSource source = new StreamDocumentSource(Objects.requireNonNull(
                TestOntology.class.getResourceAsStream(INDIVIDUALS_ONTOLOGY), "Ontology file not found in resources: " + INDIVIDUALS_ONTOLOGY));
        wrapper = new OSDiWrapper.Builder().build(source);
        ontology = wrapper.getOntology();
    }

    /**
     * Creates the IRI for an entity in the OSDi ontology.
     * @param localName The local name of the entity
     * @return The IRI of the entity
     */
    private IRI entityIRI(String localName) {
        return IRI.create(OSDiWrapper.OSDI_IRI.toString() + "#" + localName);
    }
}

