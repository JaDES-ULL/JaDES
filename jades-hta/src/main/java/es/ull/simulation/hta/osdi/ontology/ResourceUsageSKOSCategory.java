package es.ull.simulation.hta.osdi.ontology;

import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.parameters.Imports;

/**
 * Enumeration representing the categories of resource usage defined in the SKOS hierarchy of the OSDi ontology. Each category has a short name that corresponds
 * to the suffix of its IRI in the SKOS hierarchy. 
 * The enumeration provides methods to get the IRI of each category, check if an individual belongs to a category, and check if a category is a subcategory of another category.
 */
public enum ResourceUsageSKOSCategory implements IOSDiComponentWrapper {
    DETECTION("DetectionCat"),
    DIAGNOSIS("DiagnosisCat"),
    DRUG("DrugCat"),
    FOLLOW_UP("FollowUpCat"),
    HEALTH_CARE("HealthCareCat"),
    SCREENING("ScreeningCat"),
    TREATMENT("TreatmentCat");

    private final String shortName;
    private final IRI iri;
    private ResourceUsageSKOSCategory(String shortName) {
        this.shortName = shortName;
        this.iri = IRI.create(OSDiWrapper.OSDI_IRI.getIRIString() + "#" + shortName);
    }
    
    @Override
    public String getShortName() {
        return shortName;
    }

    @Override
    public boolean isCore() {
        return true;
    }

    /**
     * Returns the IRI of this category in the SKOS hierarchy.
     * @return The IRI of this category in the SKOS hierarchy.
     */
    public IRI getIRI() {
        return iri;
    }
    
    public static ResourceUsageSKOSCategory fromIRI(IRI iri) {
        for (ResourceUsageSKOSCategory category : values()) {
            if (category.getIRI().equals(iri)) {
                return category;
            }
        }
        return null;
    }
    /**
     * Checks if a given individual belongs to this category, directly or through broader categories. It checks all the categories of the individual and their 
     * ancestors in the SKOS hierarchy.
     * @param individualIRI The IRI of the individual to check.
     * @param wrap The OSDi wrapper to use for querying the model.
     * @return True if the individual belongs to this category, false otherwise.
     */
    public boolean belongsToCategory(IRI individualIRI, OSDiWrapper wrap) {
        Set<IRI> categories = wrap.getValues(individualIRI, OSDiObjectProperty.HAS_CATEGORY);
        for (IRI category : categories) {
            if (wrap.getSKOSQuery().belongsToCategory(category, getIRI(), Imports.INCLUDED)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if this category is the same as or a subcategory of the other category, false otherwise.
     * @param other The other category to compare with.
     * @param wrap The OSDi wrapper to use for querying the model.
     * @return True if this category is the same as or a subcategory of the other category, false otherwise.
     */
    public boolean isSubcategoryOf(ResourceUsageSKOSCategory other, OSDiWrapper wrap) {
        return wrap.getSKOSQuery().belongsToCategory(getIRI(), other.getIRI(), Imports.INCLUDED);
    }
}
