package es.ull.simulation.hta.config;

import java.util.List;

import es.ull.simulation.hta.HTAExperiment;
import es.ull.simulation.hta.output.OutputItem;

public interface IHTAOutputConfigProvider {
    List<OutputItem<?>> getBaseCaseOutputItems(HTAExperiment exp);
    List<OutputItem<?>> getPSAOutputItems(HTAExperiment exp);
}
