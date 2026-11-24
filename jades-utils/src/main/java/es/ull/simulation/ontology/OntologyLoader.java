package es.ull.simulation.ontology;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFJsonLDDocumentFormat;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.io.IRIDocumentSource;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

/**
 * A class to load OWL ontologies from files or IRIs with optional local IRI mappings
 */
public class OntologyLoader {

    /**
     * Loads an ontology from the specified file or IRI with default options
     * @param ontologyFileOrIRI The file path or IRI of the ontology
     * @return an instance of LoadedOntology containing the loaded ontology, its manager, and data factory
     * @throws OWLOntologyCreationException if there is an error loading the ontology
     */
    public LoadedOntology load(String ontologyFileOrIRI) throws OWLOntologyCreationException {
        return load(getOntologyDocumentSourceFromString(ontologyFileOrIRI), OWLManager.createOWLOntologyManager(), getDefaultLoadOptions());
    }

    /**
     * Loads an ontology from the specified file or IRI with specified options
     * @param ontologyFileOrIRI The file path or IRI of the ontology
     * @param options The ontology load options
     * @return an instance of LoadedOntology containing the loaded ontology, its manager, and data factory
     * @throws OWLOntologyCreationException if there is an error loading the ontology
     */
    public LoadedOntology load(String ontologyFileOrIRI, OntologyLoadOptions options) throws OWLOntologyCreationException {
        return load(getOntologyDocumentSourceFromString(ontologyFileOrIRI), OWLManager.createOWLOntologyManager(), options);
    }

    /**
     * Loads an ontology from the specified file or IRI with default options and using the provided manager
     * @param ontologyFileOrIRI The file path or IRI of the ontology
     * @param manager A previously created OWL ontology manager
     * @return an instance of LoadedOntology containing the loaded ontology, its manager, and data factory
     * @throws OWLOntologyCreationException if there is an error loading the ontology
     */
    public LoadedOntology load(String ontologyFileOrIRI, OWLOntologyManager manager, OntologyLoadOptions options) throws OWLOntologyCreationException {
        return load(getOntologyDocumentSourceFromString(ontologyFileOrIRI), manager, options);
    }

    /**
     * Loads an ontology from the specified source with default options
     * @param source The source of the ontology
     * @return an instance of LoadedOntology containing the loaded ontology, its manager, and data factory
     * @throws OWLOntologyCreationException if there is an error loading the ontology
     */
    public LoadedOntology load(OWLOntologyDocumentSource source) throws OWLOntologyCreationException {
        return load(source, OWLManager.createOWLOntologyManager(), getDefaultLoadOptions());
    }

    /**
     * Loads an ontology from the specified source with specified options
     * @param source The source of the ontology
     * @param options The ontology load options
     * @return an instance of LoadedOntology containing the loaded ontology, its manager, and data factory
     * @throws OWLOntologyCreationException if there is an error loading the ontology
     */
    public LoadedOntology load(OWLOntologyDocumentSource source, OntologyLoadOptions options) throws OWLOntologyCreationException {
        return load(source, OWLManager.createOWLOntologyManager(), options);
    }

    /**
     * Loads an ontology from the specified source with default options and using the provided manager
     * @param source The source of the ontology
     * @param manager A previously created OWL ontology manager
     * @return an instance of LoadedOntology containing the loaded ontology, its manager, and data factory
     * @throws OWLOntologyCreationException if there is an error loading the ontology
     */
    public LoadedOntology load(OWLOntologyDocumentSource source, OWLOntologyManager manager) throws OWLOntologyCreationException {
        return load(source, manager, getDefaultLoadOptions());
    }

    /**
     * Loads an ontology from the specified source with specified options and using the provided manager
     * @param source The source of the ontology
     * @param manager A previously created OWL ontology manager
     * @param options The ontology load options
     * @return an instance of LoadedOntology containing the loaded ontology, its manager, and data factory
     * @throws OWLOntologyCreationException if there is an error loading the ontology
     */
    public LoadedOntology load(OWLOntologyDocumentSource source, OWLOntologyManager manager, OntologyLoadOptions options) throws OWLOntologyCreationException {
        Objects.requireNonNull(source, "Ontology source should never be null");
        Objects.requireNonNull(manager, "Ontology manager should never be null");
        if (options == null) {
            options = getDefaultLoadOptions();
        }
        addLocalIRIMappers(manager, options.getLocalMappings());
        final OWLDataFactory df = manager.getOWLDataFactory();

        if (source.getDocumentIRI().toString().endsWith(".json") ||
            source.getDocumentIRI().toString().endsWith(".jsonld") ||
            source.getDocumentIRI().toString().endsWith(".json-ld")) {
            final OWLOntology ontology = manager.loadOntologyFromOntologyDocument(
                new FileDocumentSource(Objects.requireNonNull(new File(source.getDocumentIRI().toURI()), "Ontology file must not be null"), 
                new RDFJsonLDDocumentFormat()), 
				Objects.requireNonNull(options.getOwlConfig())
            );
            return new LoadedOntology(ontology, manager, df);
        }
        final OWLOntology ontology = manager.loadOntologyFromOntologyDocument(source, Objects.requireNonNull(options.getOwlConfig()));
        return new LoadedOntology(ontology, manager, df);
    }

    /**
     * Gets the default ontology load options
     * @return the default OntologyLoadOptions
     */
    private OntologyLoadOptions getDefaultLoadOptions() {
        return OntologyLoadOptions.defaults();
    }
    
	/**
	 * Adds a set of local path mappings for IRIs to the ontology manager. It is static so it can be used before loading ontologies.
	 * @param manager The OWL ontology manager
     * @param localMappings A list of local mappings in the form IRI=localPath
	 */
	public static void addLocalIRIMappers(OWLOntologyManager manager, List<String> localMappings) {
		if (localMappings.size() > 0) {
			for (String mapping : localMappings) {
				String[] parts = mapping.split("=");
				if (parts.length == 2) {
                    IRI schemaIRI = IRI.create(Objects.requireNonNull(parts[0], "IRI in local mapping should never be null"));
                    File schemaFile = new File(Objects.requireNonNull(parts[1], "Local path in local mapping should never be null"));
                    if (schemaFile.exists() == false) {
                        throw new IllegalArgumentException("The local path in the mapping does not exist: " + schemaFile.getAbsolutePath());
                    }
                    manager.getIRIMappers().add(
                        new SimpleIRIMapper(
                            Objects.requireNonNull(schemaIRI, "Schema IRI should never be null"), 
                            Objects.requireNonNull(IRI.create(schemaFile), "Schema file IRI should never be null")));		
				}
			}
		}
	}

    /**
     * Creates an OWLOntologyDocumentSource from a string that can be either a file path or an IRI. A path to an existing file (absolute or relative,
     * with or without the file:/// prefix) is loaded as a file. Otherwise, the string must be an absolute IRI with a scheme of more than one letter
     * (e.g., https://...): a one-letter scheme is a Windows drive (C:/...), and a relative path is not an IRI.
     * @param ontologyFileOrIRI The string representing either the file path or the IRI of the ontology
     * @return an OWLOntologyDocumentSource corresponding to the given string
     * @throws OWLOntologyCreationException if the string is neither an existing file nor an absolute IRI
     */
	public static OWLOntologyDocumentSource getOntologyDocumentSourceFromString(String ontologyFileOrIRI) throws OWLOntologyCreationException {
        Objects.requireNonNull(ontologyFileOrIRI, "Ontology file/IRI must not be null");
        try {
            final Path path = Paths.get(ontologyFileOrIRI);
            if (Files.isRegularFile(path)) {
                return new FileDocumentSource(Objects.requireNonNull(path.toFile()));
            }
        } catch (java.nio.file.InvalidPathException e) {
            // Not a file path (e.g., an IRI with characters that are not valid in a path)
        }
        java.net.URI uri = null;
        try {
            uri = new java.net.URI(ontologyFileOrIRI);
        } catch (java.net.URISyntaxException e) {
            // Not an IRI either
        }
        if (uri != null && uri.isAbsolute() && uri.getScheme().length() > 1) {
            return new IRIDocumentSource(Objects.requireNonNull(IRI.create(ontologyFileOrIRI)));
        }
        throw new OWLOntologyCreationException("The specified ontology is neither an existing file nor an absolute IRI: " + ontologyFileOrIRI);
    }
}
