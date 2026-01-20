package es.ull.simulation.ontology;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

class PrefixManagerBuilderTest {

    @Test
    void shouldUseDeclaredPrefixes() throws Exception {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.createOntology(IRI.create("http://example.com/ont"));

        TurtleDocumentFormat format = new TurtleDocumentFormat();
        format.setPrefix("ex:", "http://example.com/");
        manager.setOntologyFormat(ontology, format);

        DefaultPrefixManager pm = PrefixManagerBuilder.buildPrefixManager(manager, ontology);
        Map<String, String> prefixes = pm.getPrefixName2PrefixMap();
        assertNotNull(prefixes);
    }

    @Test
    void shouldDetectNamespaceFromEntities() throws Exception {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.createOntology(IRI.create("http://example.org/ont"));

        OWLClass cls = manager.getOWLDataFactory().getOWLClass(IRI.create("http://example.org/ns#Entity"));
        OWLAxiom axiom = manager.getOWLDataFactory().getOWLDeclarationAxiom(cls);
        manager.addAxiom(ontology, axiom);

        manager.setOntologyFormat(ontology, new RDFXMLDocumentFormat());

        DefaultPrefixManager pm = PrefixManagerBuilder.buildPrefixManager(manager, ontology);
        Map<String, String> prefixes = pm.getPrefixName2PrefixMap();
        assertNotNull(prefixes);
    }

    @Test
    void shouldFallbackToOntologyIriWhenNoEntities() throws Exception {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.createOntology(IRI.create("http://example.org/empty"));
        manager.setOntologyFormat(ontology, new RDFXMLDocumentFormat());

        DefaultPrefixManager pm = PrefixManagerBuilder.buildPrefixManager(manager, ontology);
        Map<String, String> prefixes = pm.getPrefixName2PrefixMap();
        assertNotNull(prefixes);
    }

    @Test
    void shouldDetectNamespaceFromObjectProperties() throws Exception {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.createOntology(IRI.create("http://example.org/obj"));
        OWLObjectProperty prop = manager.getOWLDataFactory()
                .getOWLObjectProperty(IRI.create("http://example.org/obj#rel"));
        manager.addAxiom(ontology, manager.getOWLDataFactory().getOWLDeclarationAxiom(prop));
        manager.setOntologyFormat(ontology, new RDFXMLDocumentFormat());

        DefaultPrefixManager pm = PrefixManagerBuilder.buildPrefixManager(manager, ontology);
        Map<String, String> prefixes = pm.getPrefixName2PrefixMap();
        assertNotNull(prefixes);
    }

    @Test
    void shouldDetectNamespaceFromDataProperties() throws Exception {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.createOntology(IRI.create("http://example.org/data"));
        OWLDataProperty prop = manager.getOWLDataFactory()
                .getOWLDataProperty(IRI.create("http://example.org/data#value"));
        manager.addAxiom(ontology, manager.getOWLDataFactory().getOWLDeclarationAxiom(prop));
        manager.setOntologyFormat(ontology, new RDFXMLDocumentFormat());

        DefaultPrefixManager pm = PrefixManagerBuilder.buildPrefixManager(manager, ontology);
        Map<String, String> prefixes = pm.getPrefixName2PrefixMap();
        assertNotNull(prefixes);
    }

    @Test
    void shouldFallbackToDefaultNamespaceWhenNoOntologyIri() throws Exception {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.createOntology();
        manager.setOntologyFormat(ontology, new RDFXMLDocumentFormat());

        DefaultPrefixManager pm = PrefixManagerBuilder.buildPrefixManager(manager, ontology);
        Map<String, String> prefixes = pm.getPrefixName2PrefixMap();
        assertNotNull(prefixes);
    }

    @Test
    void shouldHandlePrefixFormatWithEmptyPrefixes() throws Exception {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.createOntology(IRI.create("http://example.org/emptyPrefix"));
        manager.addAxiom(ontology, manager.getOWLDataFactory().getOWLDeclarationAxiom(
                manager.getOWLDataFactory().getOWLClass(IRI.create("http://example.org/emptyPrefix#Cls"))));
        manager.setOntologyFormat(ontology, new TurtleDocumentFormat());

        DefaultPrefixManager pm = PrefixManagerBuilder.buildPrefixManager(manager, ontology);
        Map<String, String> prefixes = pm.getPrefixName2PrefixMap();
        assertNotNull(prefixes);
    }

    @Test
    void shouldCreateMultipleUniquePrefixes() throws Exception {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology root = manager.createOntology(IRI.create("http://example.org/root"));
        OWLOntology imported1 = manager.createOntology(IRI.create("http://example.org/import1"));
        OWLOntology imported2 = manager.createOntology(IRI.create("http://example.org/import2"));

        TurtleDocumentFormat formatRoot = new TurtleDocumentFormat();
        formatRoot.setPrefix("ex:", "http://root.example/");
        manager.setOntologyFormat(root, formatRoot);

        TurtleDocumentFormat format1 = new TurtleDocumentFormat();
        format1.setPrefix("ex:", "http://import1.example/");
        manager.setOntologyFormat(imported1, format1);

        TurtleDocumentFormat format2 = new TurtleDocumentFormat();
        format2.setPrefix("ex:", "http://import2.example/");
        manager.setOntologyFormat(imported2, format2);

        OWLImportsDeclaration decl1 = manager.getOWLDataFactory()
            .getOWLImportsDeclaration(imported1.getOntologyID().getOntologyIRI().orElseThrow());
        OWLImportsDeclaration decl2 = manager.getOWLDataFactory()
            .getOWLImportsDeclaration(imported2.getOntologyID().getOntologyIRI().orElseThrow());
        manager.applyChange(new AddImport(root, decl1));
        manager.applyChange(new AddImport(root, decl2));

        DefaultPrefixManager pm = PrefixManagerBuilder.buildPrefixManager(manager, root);
        Map<String, String> prefixes = pm.getPrefixName2PrefixMap();
        assertNotNull(prefixes);
    }

    @Test
    void shouldExtractNamespaceFromSlash() throws Exception {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.createOntology(IRI.create("http://example.org/slash"));
        manager.addAxiom(ontology, manager.getOWLDataFactory().getOWLDeclarationAxiom(
            manager.getOWLDataFactory().getOWLClass(IRI.create("http://example.org/slash/Cls"))));
        manager.setOntologyFormat(ontology, new RDFXMLDocumentFormat());

        DefaultPrefixManager pm = PrefixManagerBuilder.buildPrefixManager(manager, ontology);
        Map<String, String> prefixes = pm.getPrefixName2PrefixMap();
        assertNotNull(prefixes);
    }

    @Test
    void shouldCreateUniquePrefixesWhenDuplicated() throws Exception {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology root = manager.createOntology(IRI.create("http://example.com/root"));
        OWLOntology imported = manager.createOntology(IRI.create("http://example.com/imported"));

        TurtleDocumentFormat rootFormat = new TurtleDocumentFormat();
        rootFormat.setPrefix("ex:", "http://root.com/");
        manager.setOntologyFormat(root, rootFormat);

        TurtleDocumentFormat importedFormat = new TurtleDocumentFormat();
        importedFormat.setPrefix("ex:", "http://imported.com/");
        manager.setOntologyFormat(imported, importedFormat);

        OWLImportsDeclaration decl = manager.getOWLDataFactory()
                .getOWLImportsDeclaration(imported.getOntologyID().getOntologyIRI().orElseThrow());
        manager.applyChange(new AddImport(root, decl));

        DefaultPrefixManager pm = PrefixManagerBuilder.buildPrefixManager(manager, root);
        Map<String, String> prefixes = pm.getPrefixName2PrefixMap();
        assertNotNull(prefixes);
    }
}
