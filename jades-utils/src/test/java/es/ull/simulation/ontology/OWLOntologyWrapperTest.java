package es.ull.simulation.ontology;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

class OWLOntologyWrapperTest {

    private static final String BASE = "http://example.org/ont#";
    private static final String PREFIX = "ex:";

    private static void registerPrefix(OWLOntologyManager manager, OWLOntology ontology) {
        TurtleDocumentFormat format = new TurtleDocumentFormat();
        format.setPrefix(PREFIX, BASE);
        manager.setOntologyFormat(ontology, format);
    }

    private static void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }

    @Test
    void shouldAddIndividualsAndQueryProperties() throws Exception {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.createOntology(IRI.create(BASE));
        OWLDataFactory factory = manager.getOWLDataFactory();
        registerPrefix(manager, ontology);

        OWLClass person = factory.getOWLClass(IRI.create(BASE + "Person"));
        OWLClass city = factory.getOWLClass(IRI.create(BASE + "City"));
        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(person));
        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(city));

        OWLObjectProperty livesIn = factory.getOWLObjectProperty(IRI.create(BASE + "livesIn"));
        OWLDataProperty age = factory.getOWLDataProperty(IRI.create(BASE + "age"));
        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(livesIn));
        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(age));

        OWLOntologyWrapper wrapper = new OWLOntologyWrapper(ontology);

        wrapper.addIndividual(PREFIX + "Person", PREFIX + "Alice");
        wrapper.addIndividual(PREFIX + "City", PREFIX + "Paris");
        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(
            factory.getOWLNamedIndividual(IRI.create(BASE + "Alice"))));
        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(
            factory.getOWLNamedIndividual(IRI.create(BASE + "Paris"))));
        manager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(
            person, factory.getOWLNamedIndividual(IRI.create(BASE + "Alice"))));
        manager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(
            city, factory.getOWLNamedIndividual(IRI.create(BASE + "Paris"))));
        wrapper.addObjectPropertyValue(PREFIX + "Alice", PREFIX + "livesIn", PREFIX + "Paris");
        wrapper.addDataPropertyValue(PREFIX + "Alice", PREFIX + "age", "30");
        manager.addAxiom(ontology, factory.getOWLObjectPropertyAssertionAxiom(
            factory.getOWLObjectProperty(IRI.create(BASE + "livesIn")),
            factory.getOWLNamedIndividual(IRI.create(BASE + "Alice")),
            factory.getOWLNamedIndividual(IRI.create(BASE + "Paris"))));
        manager.addAxiom(ontology, factory.getOWLDataPropertyAssertionAxiom(
            factory.getOWLDataProperty(IRI.create(BASE + "age")),
            factory.getOWLNamedIndividual(IRI.create(BASE + "Alice")),
            factory.getOWLLiteral("30")));

        assertTrue(wrapper.individualsToString().contains("Alice"));
        assertTrue(wrapper.classesToString().contains("Person"));
        assertTrue(wrapper.dataPropertiesToString().contains("age"));
        assertTrue(wrapper.objectPropertiesToString().contains("livesIn"));
    }

    @Test
    void shouldHandleLabelsAndClassConversions() throws Exception {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.createOntology(IRI.create(BASE));
        OWLDataFactory factory = manager.getOWLDataFactory();
        registerPrefix(manager, ontology);

        OWLClass person = factory.getOWLClass(IRI.create(BASE + "Person"));
        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(person));

        OWLAnnotation label = factory.getOWLAnnotation(factory.getRDFSLabel(), factory.getOWLLiteral("Persona", "es"));
        OWLAxiom labelAxiom = factory.getOWLAnnotationAssertionAxiom(person.getIRI(), label);
        manager.addAxiom(ontology, labelAxiom);

        OWLOntologyWrapper wrapper = new OWLOntologyWrapper(ontology);
        assertEquals("Persona", wrapper.getLabelForIRI(PREFIX + "Person", "es"));
        assertEquals("PERSON_CLASS", OWLOntologyWrapper.camel2SNAKE("PersonClass"));
    }

    @Test
    void shouldRemoveIndividualsAndChangeInstancesToSubclasses() throws Exception {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.createOntology(IRI.create(BASE));
        OWLDataFactory factory = manager.getOWLDataFactory();
        registerPrefix(manager, ontology);

        OWLClass person = factory.getOWLClass(IRI.create(BASE + "Person"));
        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(person));

        OWLOntologyWrapper wrapper = new OWLOntologyWrapper(ontology);
        wrapper.addIndividual(PREFIX + "Person", PREFIX + "Alice");

        wrapper.changeInstanceToSubclass(PREFIX + "Person", PREFIX + "Sub_");
        assertFalse(wrapper.containsIndividual(PREFIX + "Alice"));
        assertTrue(wrapper.classesToString().contains("Sub_Alice"));

        wrapper.addIndividual(PREFIX + "Person", PREFIX + "Bob");
        wrapper.removeIndividualsOfClass(PREFIX + "Person");
        assertFalse(wrapper.containsIndividual(PREFIX + "Bob"));
    }

    @Test
    void shouldAddOntologiesFromStreamPathAndIri() throws Exception {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();

        OWLOntology base = manager.createOntology(IRI.create(BASE + "base"));
        registerPrefix(manager, base);
        manager.addAxiom(base, factory.getOWLDeclarationAxiom(
                factory.getOWLClass(IRI.create(BASE + "BaseClass"))));

        OWLOntologyWrapper wrapper = new OWLOntologyWrapper(base);

        OWLOntology other = manager.createOntology(IRI.create(BASE + "other"));
        registerPrefix(manager, other);
        manager.addAxiom(other, factory.getOWLDeclarationAxiom(
                factory.getOWLClass(IRI.create(BASE + "OtherClass"))));

        Path otherFile = Files.createTempFile("ontology_other_source", ".owl");
        manager.saveOntology(other, IRI.create(otherFile.toUri()));

        try (InputStream in = Files.newInputStream(otherFile)) {
            wrapper.addOntology(in);
        }
        assertTrue(wrapper.classesToString().contains("OtherClass"));

        wrapper.addOntology(otherFile.toString());
        assertTrue(wrapper.classesToString().contains("OtherClass"));

        wrapper.addOntology(IRI.create(otherFile.toUri()));
        assertTrue(wrapper.classesToString().contains("OtherClass"));

        deleteQuietly(otherFile);
    }

    @Test
    void shouldMergeFromInputStreamAndPrintHelpers() throws Exception {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();

        OWLOntology ontology = manager.createOntology(IRI.create(BASE + "main"));
        registerPrefix(manager, ontology);
        OWLClass cls = factory.getOWLClass(IRI.create(BASE + "ClassA"));
        OWLObjectProperty prop = factory.getOWLObjectProperty(IRI.create(BASE + "prop"));
        OWLDataProperty data = factory.getOWLDataProperty(IRI.create(BASE + "data"));
        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(cls));
        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(prop));
        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(data));

        OWLOntologyWrapper wrapper = new OWLOntologyWrapper(ontology);

        OWLOntology other = manager.createOntology(IRI.create(BASE + "merge"));
        registerPrefix(manager, other);
        OWLClass clsB = factory.getOWLClass(IRI.create(BASE + "ClassB"));
        manager.addAxiom(other, factory.getOWLDeclarationAxiom(clsB));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        manager.saveOntology(other, baos);

        try (InputStream in = new ByteArrayInputStream(baos.toByteArray())) {
            wrapper.mergeOtherOntology(in);
        }

        wrapper.printClasses();
        wrapper.printClassesAsEnum();
        wrapper.printDataProperties();
        wrapper.printDataPropertiesAsEnum();
        wrapper.printObjectProperties();
        wrapper.printObjectPropertiesAsEnum();
        wrapper.printIndividuals(false);
        wrapper.printIndividuals(PREFIX + "ClassA", false);

        assertTrue(wrapper.classesToString().contains("ClassB"));
    }

    @Test
    void shouldSaveAndMergeOntologies() throws Exception {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.createOntology(IRI.create(BASE));
        OWLDataFactory factory = manager.getOWLDataFactory();
        registerPrefix(manager, ontology);

        OWLClass person = factory.getOWLClass(IRI.create(BASE + "Person"));
        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(person));

        OWLOntologyWrapper wrapper = new OWLOntologyWrapper(ontology);

        Path tempFile = Files.createTempFile("ontology", ".owl");
        Path otherFile = null;
        try {
            wrapper.saveAs(tempFile);
            wrapper.save();

            OWLOntology other = manager.createOntology(IRI.create(BASE + "other"));
            OWLClass city = factory.getOWLClass(IRI.create(BASE + "City"));
            manager.addAxiom(other, factory.getOWLDeclarationAxiom(city));
            otherFile = Files.createTempFile("ontology_other", ".owl");
            manager.saveOntology(other, IRI.create(otherFile.toUri()));

            try (InputStream in = Files.newInputStream(otherFile)) {
                wrapper.mergeOtherOntology(in);
            }
            assertTrue(wrapper.classesToString().contains("City"));

            int before = wrapper.manager.getIRIMappers().size();
            wrapper.addLocalIRIMapper(BASE, otherFile.toString());
            assertTrue(wrapper.manager.getIRIMappers().size() > before);
        } finally {
            deleteQuietly(tempFile);
            deleteQuietly(otherFile);
        }
    }

    @Test
    void shouldReplaceMinCardinalityWithAllValuesFrom() throws Exception {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.createOntology(IRI.create(BASE));
        OWLDataFactory factory = manager.getOWLDataFactory();
        registerPrefix(manager, ontology);

        OWLClass person = factory.getOWLClass(IRI.create(BASE + "Person"));
        OWLObjectProperty knows = factory.getOWLObjectProperty(IRI.create(BASE + "knows"));
        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(person));
        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(knows));

        OWLAxiom axiom = factory.getOWLSubClassOfAxiom(
                person, factory.getOWLObjectMinCardinality(0, knows, person));
        manager.addAxiom(ontology, axiom);

        OWLOntologyWrapper wrapper = new OWLOntologyWrapper(ontology);
        wrapper.replaceMin0ByOnlyRecursive();

        assertTrue(ontology.getAxioms().stream()
                .filter(a -> a.isOfType(org.semanticweb.owlapi.model.AxiomType.SUBCLASS_OF))
                .map(a -> ((org.semanticweb.owlapi.model.OWLSubClassOfAxiom) a).getSuperClass())
                .noneMatch(expr -> expr instanceof OWLObjectMinCardinality));
    }
}
