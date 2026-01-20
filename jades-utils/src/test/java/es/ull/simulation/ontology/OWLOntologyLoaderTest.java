package es.ull.simulation.ontology;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

class OWLOntologyLoaderTest {

    @Test
    void shouldLoadFromPathStreamAndIri() throws Exception {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.createOntology(IRI.create("http://example.org/load"));

        Path tempFile = Files.createTempFile("ontology_loader", ".owl");
        manager.saveOntology(ontology, IRI.create(tempFile.toUri()));

        try {
            OWLOntologyLoader loaderFromPath = OWLOntologyLoader.fromPath(tempFile.toString());
            assertNotNull(loaderFromPath.getManager());
            assertNotNull(loaderFromPath.getOntology());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            manager.saveOntology(ontology, baos);
            try (InputStream in = new ByteArrayInputStream(baos.toByteArray())) {
                OWLOntologyLoader loaderFromStream = OWLOntologyLoader.fromStream(in);
                assertNotNull(loaderFromStream.getOntology());
            }

            OWLOntologyLoader loaderFromIri = OWLOntologyLoader.fromIRI(IRI.create(tempFile.toUri()));
            assertNotNull(loaderFromIri.getOntology());
        } finally {
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException ignored) {
            }
        }
    }

    @Test
    void shouldRejectInvalidLocalMappings() {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        assertThrows(IllegalArgumentException.class,
                () -> OWLOntologyLoader.addLocalIRIMappers(manager, "http://example.org/ont=missing.owl"));
    }
}
