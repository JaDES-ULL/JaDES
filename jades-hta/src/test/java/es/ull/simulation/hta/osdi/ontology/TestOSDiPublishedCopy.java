package es.ull.simulation.hta.osdi.ontology;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

/**
 * Checks that the copy of OSDi in jades-hta is a published version of OSDi (option A of section 5 of docs/osdi-1.3-traslado.md): the
 * repository of OSDi is the reference, and jades-hta uses an identical copy of a published version.
 * <ul>
 * <li>The header is coherent: the version IRI is the IRI of the ontology followed by its version (owl:versionInfo, without the suffix of a
 * draft), and it declares the version it replaces (owl:priorVersion).</li>
 * <li>The copy of a published version is identical to the published file: its SHA-256 fingerprint, with the line endings normalized to LF
 * (Git may convert them on checkout), matches the one recorded in {@link #PUBLISHED_SHA256}. Any local edit makes this check fail: a change
 * of the ontology must be published in the repository of OSDi, or the copy must become a draft with its own version (e.g., versionInfo
 * "1.4-dev" and version IRI .../OSDi/1.4). A draft skips this check, so it is reported as skipped.</li>
 * <li>Every test ontology and every example imports the version of the copy, which is the only one that JaDES solves locally.</li>
 * </ul>
 */
@OSDiTest
@DisplayName("The copy of OSDi is a published version")
public class TestOSDiPublishedCopy {
    /** The IRI of the OSDi ontology */
    private static final String OSDI_IRI = "https://w3id.org/ontologies-ULL/OSDi";
    /**
     * The SHA-256 fingerprint (line endings normalized to LF) of the OSDi.ttl published for each version in the repository of OSDi. When a
     * new version is published, its OSDi.ttl is copied unchanged into jades-hta/src/main/resources and its fingerprint is added here.
     */
    private static final Map<String, String> PUBLISHED_SHA256 = Map.of(
        "1.3", "dbae59c7ef1c2e08e38ae641bffec9f1e6be2572ccfe843509f852952dd8c765");
    /** The suffix of the versionInfo of a draft */
    private static final String DRAFT_SUFFIX = "-dev";
    /** The local copies of the ontologies imported by OSDi (see {@link OSDiWrapper}) */
    private static final Map<String, String> LOCAL_IMPORTS = Map.of(
        "http://www.w3.org/2004/02/skos/core", "/skos.rdf",
        "http://www.w3.org/2006/time", "/time.owl",
        "http://www.w3.org/2006/time#2016", "/time.owl");
    /** An import of some version of OSDi in a Turtle file */
    private static final Pattern OSDI_IMPORT = Pattern.compile("owl:imports\\s+<(" + Pattern.quote(OSDI_IRI) + "/[^>]*)>");
    /** A mention of a version of OSDi in a comment, e.g., "Individuals for OSDi ontology, version 1.3" */
    private static final Pattern OSDI_VERSION_MENTION = Pattern.compile("OSDi ontology, version (\\d+\\.\\d+(?:\\.\\d+)?)");
    /** The folders (relative to the module) with the ontologies that import OSDi */
    private static final List<String> IMPORTING_FOLDERS = List.of("src/test/resources", "examples");

    private static OWLOntology osdi;
    private static String versionInfo;

    @BeforeAll
    static void loadCopy() throws Exception {
        final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        for (Map.Entry<String, String> entry : LOCAL_IMPORTS.entrySet()) {
            final URL resource = Objects.requireNonNull(TestOSDiPublishedCopy.class.getResource(entry.getValue()), entry.getValue());
            manager.getIRIMappers().add(new SimpleIRIMapper(IRI.create(entry.getKey()), IRI.create(resource)));
        }
        final URL copy = Objects.requireNonNull(TestOSDiPublishedCopy.class.getResource("/OSDi.ttl"));
        osdi = manager.loadOntologyFromOntologyDocument(IRI.create(copy));
        versionInfo = getHeaderLiteral(OWLRDFVocabulary.OWL_VERSION_INFO.getIRI())
            .orElseThrow(() -> new AssertionError("The copy of OSDi declares no owl:versionInfo"));
    }

    private static Stream<OWLAnnotation> getHeaderAnnotations(IRI property) {
        return osdi.annotations().filter(a -> a.getProperty().getIRI().equals(property));
    }

    private static Optional<String> getHeaderLiteral(IRI property) {
        return getHeaderAnnotations(property).map(a -> a.getValue().asLiteral()).flatMap(Optional::stream).map(l -> l.getLiteral()).findFirst();
    }

    /** The version of the copy without the suffix of a draft, e.g., 1.4 for 1.4-dev */
    private static String getBaseVersion() {
        return versionInfo.endsWith(DRAFT_SUFFIX) ? versionInfo.substring(0, versionInfo.length() - DRAFT_SUFFIX.length()) : versionInfo;
    }

    private static String getVersionIRI() {
        return osdi.getOntologyID().getVersionIRI().map(IRI::toString).orElseThrow(() -> new AssertionError("The copy of OSDi declares no owl:versionIRI"));
    }

    /** The SHA-256 fingerprint of a text, with its line endings normalized to LF */
    static String fingerprint(byte[] content) throws Exception {
        final String text = new String(content, StandardCharsets.UTF_8).replace("\r\n", "\n");
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    @DisplayName("The header declares a coherent version and the version it replaces")
    void headerIsCoherent() {
        assertEquals(OSDI_IRI, osdi.getOntologyID().getOntologyIRI().map(IRI::toString).orElse(null), "Unexpected ontology IRI");
        assertTrue(getBaseVersion().matches("\\d+\\.\\d+(\\.\\d+)?"), "Unexpected owl:versionInfo: " + versionInfo);
        assertEquals(OSDI_IRI + "/" + getBaseVersion(), getVersionIRI(), "The version IRI does not match owl:versionInfo " + versionInfo);
        final List<String> prior = getHeaderAnnotations(OWLRDFVocabulary.OWL_PRIOR_VERSION.getIRI())
            .map(a -> a.getValue().asIRI().map(IRI::toString).orElse("")).toList();
        assertEquals(1, prior.size(), "The copy of OSDi must declare exactly one owl:priorVersion");
        assertTrue(prior.get(0).startsWith(OSDI_IRI + "/"), "Unexpected owl:priorVersion: " + prior.get(0));
        assertFalse(prior.get(0).equals(getVersionIRI()), "owl:priorVersion cannot be the version itself");
    }

    @Test
    @DisplayName("The copy of a published version is identical to the published file")
    void copyIsPublished() throws Exception {
        Assumptions.assumeFalse(versionInfo.endsWith(DRAFT_SUFFIX),
            "The copy of OSDi is a draft (" + versionInfo + "): it is not compared with a published version");
        final String expected = PUBLISHED_SHA256.get(versionInfo);
        assertNotNull(expected, "No fingerprint recorded for OSDi " + versionInfo + ": if it is published, add the SHA-256 of its OSDi.ttl to "
            + "PUBLISHED_SHA256; otherwise, mark the copy as a draft (owl:versionInfo \"" + versionInfo + DRAFT_SUFFIX + "\")");
        final byte[] content;
        try (InputStream stream = Objects.requireNonNull(TestOSDiPublishedCopy.class.getResourceAsStream("/OSDi.ttl"))) {
            content = stream.readAllBytes();
        }
        assertEquals(expected, fingerprint(content), "The copy of OSDi " + versionInfo + " differs from the published file. Publish the change in "
            + "the repository of OSDi with a new version, or mark the copy as a draft (owl:versionInfo \"<next version>" + DRAFT_SUFFIX + "\")");
    }

    @Test
    @DisplayName("Every test ontology and every example imports, and mentions, the version of the copy")
    void importsMatchTheCopy() throws IOException {
        final String versionIRI = getVersionIRI();
        final String version = getBaseVersion();
        final List<String> wrong = new ArrayList<>();
        int count = 0;
        for (String folder : IMPORTING_FOLDERS) {
            final Path dir = Paths.get(folder);
            assertTrue(Files.isDirectory(dir), "Folder not found: " + dir.toAbsolutePath());
            try (Stream<Path> files = Files.walk(dir)) {
                for (Path file : files.filter(p -> p.toString().endsWith(".ttl")).sorted().toList()) {
                    final String content = Files.readString(file, StandardCharsets.UTF_8);
                    final Matcher matcher = OSDI_IMPORT.matcher(content);
                    while (matcher.find()) {
                        count++;
                        if (!matcher.group(1).equals(versionIRI))
                            wrong.add(file + " imports " + matcher.group(1));
                    }
                    final Matcher mention = OSDI_VERSION_MENTION.matcher(content);
                    while (mention.find()) {
                        if (!mention.group(1).equals(version))
                            wrong.add(file + " mentions OSDi version " + mention.group(1));
                    }
                }
            }
        }
        assertTrue(count > 0, "No ontology imports OSDi");
        assertTrue(wrong.isEmpty(), "Imports or mentions that do not match the version of the copy (" + versionIRI + "):\n" + String.join("\n", wrong));
    }
}
