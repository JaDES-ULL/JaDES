package es.ull.simulation.hta.osdi.ontology;

import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.search.EntitySearcher;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests to check whether the local version of the OSDi ontology complies with the expected structure defined in this project.
 * The checks are done in both directions: every element mapped by the Java enums must exist in the ontology, and every element
 * defined by the OSDi ontology must be mapped by the corresponding enum. The second direction only uses the axioms asserted in
 * the OSDi core ontology, so it does not depend on the reasoner.
 */
public class TestOSDiCompliance {
    private final static String INDIVIDUALS_ONTOLOGY = "/OSDi_test.ttl";
    /** Namespace of the elements defined by the OSDi ontology */
    private final static String OSDI_NAMESPACE = OSDiWrapper.OSDI_IRI.toString() + "#";
    /** Namespace of the SKOS elements, which are mapped by the enums as non-core elements */
    private final static String SKOS_NAMESPACE = "http://www.w3.org/2004/02/skos/core#";
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

    @Test
    @OSDiTest
    @DisplayName("Test that every class and property of OSDi is mapped")
    public void testOntologyElementsAreMapped() throws Exception {
        final OWLOntology core = wrapper.getOsdiCoreOntology();
        assertAllMapped("classes", core.classesInSignature(Imports.EXCLUDED), OSDiClass.values());
        assertAllMapped("object properties", core.objectPropertiesInSignature(Imports.EXCLUDED), OSDiObjectProperty.values());
        assertAllMapped("data properties", core.dataPropertiesInSignature(Imports.EXCLUDED), OSDiDataProperty.values());
    }

    @Test
    @OSDiTest
    @DisplayName("Test that every individual of OSDi is mapped")
    public void testOntologyIndividualsAreMapped() throws Exception {
        final OWLOntology core = wrapper.getOsdiCoreOntology();
        // Data item types, including the individuals of its subclasses (e.g. Currency or QoLDataItemType)
        final Set<OWLClass> dataItemTypeClasses = withSubClasses(core, owlClass(OSDiClass.DATA_ITEM_TYPE));
        assertAllMapped("data item types", instancesOf(core, dataItemTypeClasses), OSDiDataItemType.values());
        // Expression languages
        assertAllMapped("expression languages", instancesOf(core, Set.of(owlClass(OSDiClass.EXPRESSION_LANGUAGE))), ExpressionLanguageType.values());
        // Categories of the resource usage scheme
        final OWLObjectProperty inScheme = wrapper.getDataFactory().getOWLObjectProperty(IRI.create(SKOS_NAMESPACE + "inScheme"));
        final OWLNamedIndividual scheme = wrapper.getDataFactory().getOWLNamedIndividual(entityIRI("ResourceUsageCategoryScheme"));
        final Stream<OWLNamedIndividual> categories = core.individualsInSignature(Imports.EXCLUDED)
            .filter(ind -> EntitySearcher.getObjectPropertyValues(ind, inScheme, core).anyMatch(scheme::equals));
        assertAllMapped("resource usage categories", categories, ResourceUsageSKOSCategory.values());
    }

    @Test
    @OSDiTest
    @DisplayName("Test that the individuals mapped in Java are defined in OSDi")
    public void testMappedIndividualsExist() throws Exception {
        for (IOSDiComponentWrapper item : concat(ExpressionLanguageType.values(), ResourceUsageSKOSCategory.values())) {
            final IRI individualIRI = entityIRI(item.getShortName());
            assertTrue(ontology.containsIndividualInSignature(individualIRI, Imports.INCLUDED), "Ontology must contain individual: " + item.getShortName());
        }
    }

    @Test
    @OSDiTest
    @DisplayName("Test that the non-core elements mapped in Java are defined in SKOS")
    public void testNonCoreElementsAreSKOS() throws Exception {
        for (OSDiClass cls : OSDiClass.values()) {
            if (!cls.isCore()) {
                assertTrue(ontology.containsClassInSignature(IRI.create(SKOS_NAMESPACE + cls.getShortName()), Imports.INCLUDED),
                        "SKOS must contain class: " + cls.getShortName());
            }
        }
        for (OSDiObjectProperty prop : OSDiObjectProperty.values()) {
            if (!prop.isCore()) {
                assertTrue(ontology.containsObjectPropertyInSignature(IRI.create(SKOS_NAMESPACE + prop.getShortName()), Imports.INCLUDED),
                        "SKOS must contain object property: " + prop.getShortName());
            }
        }
    }

    /**
     * Checks that every element of the OSDi namespace is mapped by the specified enum values, and reports all the elements that are not mapped.
     * @param kind The kind of elements, used in the message
     * @param elements The elements defined in the ontology (elements from other namespaces are ignored)
     * @param mapped The values of the enum that maps such elements
     */
    private static void assertAllMapped(String kind, Stream<? extends OWLEntity> elements, IOSDiComponentWrapper[] mapped) {
        final Set<String> mappedNames = Arrays.stream(mapped).map(IOSDiComponentWrapper::getShortName).collect(Collectors.toSet());
        final List<String> notMapped = elements.map(OWLEntity::getIRI)
            .filter(iri -> OSDI_NAMESPACE.equals(iri.getNamespace()))
            .map(IRI::getShortForm)
            .filter(name -> !mappedNames.contains(name))
            .sorted()
            .toList();
        assertTrue(notMapped.isEmpty(), "The OSDi ontology defines " + kind + " that are not mapped in Java: " + notMapped);
    }

    /**
     * Returns a class together with all its subclasses, according to the subclass axioms asserted in the ontology.
     * @param ontology The ontology
     * @param cls The class
     * @return The class and all its named subclasses
     */
    private static Set<OWLClass> withSubClasses(OWLOntology ontology, OWLClass cls) {
        final Set<OWLClass> result = new HashSet<>();
        final Deque<OWLClass> pending = new ArrayDeque<>(List.of(cls));
        while (!pending.isEmpty()) {
            final OWLClass current = pending.pop();
            if (result.add(current)) {
                ontology.subClassAxiomsForSuperClass(current)
                    .map(OWLSubClassOfAxiom::getSubClass)
                    .filter(OWLClassExpression::isNamed)
                    .map(OWLClassExpression::asOWLClass)
                    .forEach(pending::push);
            }
        }
        return result;
    }

    /**
     * Returns the individuals that are asserted to belong to any of the specified classes.
     * @param ontology The ontology
     * @param classes The classes
     * @return The individuals asserted to belong to any of the classes
     */
    private static Stream<OWLNamedIndividual> instancesOf(OWLOntology ontology, Set<OWLClass> classes) {
        return ontology.individualsInSignature(Imports.EXCLUDED)
            .filter(ind -> EntitySearcher.getTypes(ind, ontology).anyMatch(classes::contains));
    }

    private OWLClass owlClass(OSDiClass cls) {
        return wrapper.getDataFactory().getOWLClass(entityIRI(cls.getShortName()));
    }

    private static IOSDiComponentWrapper[] concat(IOSDiComponentWrapper[] first, IOSDiComponentWrapper[] second) {
        return Stream.concat(Arrays.stream(first), Arrays.stream(second)).toArray(IOSDiComponentWrapper[]::new);
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

