package es.ull.simulation.hta.osdi.ontology;

import java.util.Optional;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLLiteral;
import es.ull.simulation.ontology.OWLOntologyWrapper.InstanceCheckMode;

public interface IIndividualWrapper extends Comparable<IIndividualWrapper> {
    /**
     * Returns the IRI of the individual in the ontology
     * @return the IRI of the individual in the ontology
     */
    public IRI getIndividualIRI();
    
    /**
     * Returns the short name of the individual, i.e., the fragment of its IRI.
     * @return the short name of the individual, i.e., the fragment of its IRI.
     */
    public default String getShortName() {
        return getIndividualIRI().getShortForm();
    }

    /**
     * Returns the OSDiWrapper that contains this individual.
     * @return The OSDiWrapper that contains this individual.
     */
    OSDiWrapper getOSDiWrapper();

    /**
     * Returns the description of the individual, if present.
     * @return the description of the individual.
     */
    public default Optional<String> getDescription() {
        Optional<OWLLiteral> descLiteral = getOSDiWrapper().getValue(getIndividualIRI(), OSDiDataProperty.HAS_DESCRIPTION);
        if (descLiteral.isPresent()) {
            return Optional.of(descLiteral.get().getLiteral());
        }
        else {
            return Optional.empty();
        }
    }

    /**
     * Returns the set of types of the individual, i.e., the classes it belongs to. 
     * @param mode The mode of instance checking to use when retrieving the types. 
     * @return the set of types of the individual, i.e., the classes it belongs to. 
     */
    public default Set<OSDiClass> getTypes(InstanceCheckMode mode) {
        return getOSDiWrapper().getTypes(getIndividualIRI(), mode);
    }

    @Override
    public default int compareTo(IIndividualWrapper o) {
        return this.getIndividualIRI().compareTo(o.getIndividualIRI());
    }
}
