package es.ull.simulation.hta.osdi.des;

import java.io.IOException;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import es.ull.simulation.hta.HTAExperiment;
import es.ull.simulation.hta.HTAModel;
import es.ull.simulation.hta.MalformedSimulationModelException;
import es.ull.simulation.hta.config.IHTAOutputConfigProvider;
import es.ull.simulation.hta.osdi.OSDiExperimentConfigurationProvider;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;

public class OSDiExperiment extends HTAExperiment {
    public OSDiExperiment(OSDiExperimentConfigurationProvider configProvider, IHTAOutputConfigProvider htaConfigProvider) throws MalformedSimulationModelException, IOException, OWLOntologyCreationException, MalformedOSDiModelException {
        super(configProvider, htaConfigProvider);
    }

    @Override
    public HTAModel createModel() throws MalformedSimulationModelException {
            OSDiDESModel model = null;
            try {
                model = new OSDiDESModel(this);
            } catch (MalformedOSDiModelException | UnsupportedOSDiFeatureException e) {
                throw new MalformedSimulationModelException("Error creating the OSDi model", e);
            }
            return model;
    }

}
