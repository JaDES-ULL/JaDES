package es.ull.simulation.hta.osdi.ontology;

import java.util.Optional;

public record TemporalConstraint(Optional<ParameterWrapper> startTime, Optional<ParameterWrapper> endTime) {

}
