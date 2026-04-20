/**
 * 
 */
package es.ull.simulation.hta.osdi.des;

import java.util.Collection;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;

import es.ull.simulation.hta.HTAModel;
import es.ull.simulation.hta.MalformedSimulationModelException;
import es.ull.simulation.hta.osdi.des.factories.InterventionFactory;
import es.ull.simulation.hta.osdi.des.factories.ParameterFactory;
import es.ull.simulation.hta.osdi.OSDiExperimentConfigurationProvider;
import es.ull.simulation.hta.osdi.des.factories.DiseaseFactory;
import es.ull.simulation.hta.osdi.des.factories.PopulationFactory;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.DiseaseWrapper;
import es.ull.simulation.hta.osdi.ontology.ExperimentWrapper;
import es.ull.simulation.hta.osdi.ontology.InterventionWrapper;
import es.ull.simulation.hta.osdi.ontology.ModelWrapper;
import es.ull.simulation.hta.osdi.ontology.OSDiWrapper;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper;
import es.ull.simulation.hta.osdi.ontology.PopulationWrapper;
import es.ull.simulation.hta.params.Parameter;
import es.ull.simulation.hta.params.ParameterGroup;

/**
 * A DES model that is created from an OSDi model. It uses the OSDiWrapper to access the information of the ontology and create the necessary components of the model.
 * The model only uses one disease and one population.
 * @author Iván Castilla Rodríguez
 *
 */
public class OSDiDESModel extends HTAModel {
	private static final Logger log = org.slf4j.LoggerFactory.getLogger(OSDiDESModel.class);
	private final OSDiWrapper wrap; 
	private final ModelWrapper modelWrapper;
	
	/**
	 * Constructor to initialize the OSDiDESModel with the specified HTA experiment, path to the ontology, model ID, and instance prefix.
	 * This constructor will parse the ontology and create the necessary model components such as diseases, populations, and interventions.
	 * @param experiment The HTA experiment associated with this model.
	 * @throws OWLOntologyCreationException 
	 * @throws MalformedSimulationModelException 
	 * TODO: Convert all the bernouilli parameters into BernouilliParam (e.g. Population parameters such as sex)
	 * @throws UnsupportedOSDiFeatureException 
	 */
	public OSDiDESModel(OSDiExperiment experiment) throws MalformedSimulationModelException, MalformedOSDiModelException, UnsupportedOSDiFeatureException {
		super(experiment);
		final ExperimentWrapper experimentWrapper = ((OSDiExperimentConfigurationProvider)experiment.getConfigProvider()).getExperimentWrapper();
		this.wrap = experimentWrapper.getOSDiWrapper();
		this.modelWrapper = experimentWrapper.getModelWrapper();
		PopulationWrapper populationWrapper = modelWrapper.getPopulationIndividuals().iterator().next();
		DiseaseWrapper diseaseWrapper = modelWrapper.getDiseaseIndividuals().iterator().next();
		final OSDiDisease disease = DiseaseFactory.getDiseaseInstance(this, diseaseWrapper, populationWrapper);
		PopulationFactory.getPopulationInstance(this, populationWrapper, disease);
		
		// Build interventions that belong to the model
		final Collection<InterventionWrapper> interventions = modelWrapper.getInterventionIndividuals();
		for (InterventionWrapper interventionWrapper : interventions) {
			InterventionFactory.getInterventionInstance(this, interventionWrapper);
		}
		// TODO: Adapt the rest to use the wrapper

		
		
	}
	
	public OSDiWrapper getOwlWrapper() {
		return wrap;
	}

	public ModelWrapper getModelWrapper() {
		return modelWrapper;
	}
	
	@Override
	public void createParameters() {
		log.info("Creating parameters");
		for (ParameterWrapper param : modelWrapper.getParameters()) {
			log.debug("Parameter: " + param.getShortName());
		}

		super.createParameters();
		// Create all of those parameters not directly created from a disease, disease progression, etc.
		for (ParameterWrapper param : modelWrapper.getParameters()) {
			if (!getParameters().containsKey(param.getShortName())) {
				ParameterGroup group = ParameterGroup.UNKNOWN;
				try {
					group = param.getParameterGroup();
				} catch (MalformedOSDiModelException e) {
					log.warn("Parameter " + param.getShortName() + " has an unknown parameter group. It will be added to the model with the UNKNOWN group.");
				}
				this.addParameter(ParameterFactory.getParameterInstance(this, param, group));
			}
		}
		log.info("Parameters created");
		for (Parameter param : getParameters().values()) {
			log.debug("Parameter: " + param.name());
		}
	}
}
