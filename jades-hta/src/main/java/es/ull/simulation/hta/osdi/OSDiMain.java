package es.ull.simulation.hta.osdi;

import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.slf4j.Logger;

import com.beust.jcommander.JCommander;

import es.ull.simulation.hta.config.JsonOutputConfigProvider;
import es.ull.simulation.hta.osdi.cliargs.CheckOsdiArgs;
import es.ull.simulation.hta.osdi.cliargs.GenerateExcelOsdiArgs;
import es.ull.simulation.hta.osdi.cliargs.RootArgs;
import es.ull.simulation.hta.osdi.cliargs.RunDESOsdiArgs;
import es.ull.simulation.hta.osdi.decisiontree.Model;
import es.ull.simulation.hta.osdi.decisiontree.SingleDiseaseAndPopulationModel;
import es.ull.simulation.hta.osdi.decisiontree.excelport.HTAExcelModelFactory;
import es.ull.simulation.hta.osdi.decisiontree.factories.CentralModelFactory;
import es.ull.simulation.hta.osdi.des.OSDiExperiment;
import es.ull.simulation.hta.osdi.ontology.ExperimentWrapper;
import es.ull.simulation.hta.osdi.ontology.OSDiWrapper;
import es.ull.simulation.ontology.OntologyLoader;

public class OSDiMain {
	private static final Logger log = org.slf4j.LoggerFactory.getLogger(OSDiMain.class);

    private static void runCheck(CheckOsdiArgs args) {
        try {
            OWLOntologyDocumentSource source = OntologyLoader.getOntologyDocumentSourceFromString(args.osdiPath);
            OSDiWrapper osdi = new OSDiWrapper.Builder().build(source);
            // Just try to get the experiment
            osdi.buildExperiment(osdi.toIRI(args.osdiExperiment));
        } catch (Exception e) {
            log.error("Error checking OSDi experiment " + args.osdiExperiment + " at " + args.osdiPath, e);
        }
    }

    private static void runGenerateExcel(GenerateExcelOsdiArgs args) {
        try {
            OWLOntologyDocumentSource source = OntologyLoader.getOntologyDocumentSourceFromString(args.osdiPath);
            OSDiWrapper osdi = new OSDiWrapper.Builder().build(source);
            ExperimentWrapper experiment = osdi.buildExperiment(osdi.toIRI(args.osdiExperiment));
            Model tempModel = CentralModelFactory.create(experiment);
            if (!(tempModel instanceof SingleDiseaseAndPopulationModel)) {
                log.error("The OSDi experiment " + args.osdiExperiment + " at " + args.osdiPath + " does not represent a single disease and population decision tree model. Currently, only this type of model is supported for Excel generation.");
            }
            else {
                HTAExcelModelFactory.build(args.excelPath, (SingleDiseaseAndPopulationModel)tempModel);

            }
        } catch (Exception e) {
            log.error("Error generating simulation model from OSDi experiment " + args.osdiExperiment + " at " + args.osdiPath, e);
        }
    }

    private static void runDES(RunDESOsdiArgs args) {
        try {
            OWLOntologyDocumentSource source = OntologyLoader.getOntologyDocumentSourceFromString(args.osdiPath);
            OSDiWrapper osdi = new OSDiWrapper.Builder().build(source);

            OSDiExperimentConfigurationProvider configProvider = new OSDiExperimentConfigurationProvider(args, osdi);
            JsonOutputConfigProvider outputConfigProvider = null;
            if (args.htaArguments.outputsConfigFile != null) {
                outputConfigProvider = new JsonOutputConfigProvider(args.htaArguments.outputsConfigFile);
            }
            OSDiExperiment desExperiment = new OSDiExperiment(configProvider, outputConfigProvider);
            desExperiment.initializeModel();
            desExperiment.run();
        } catch (Exception e) {
            log.error("Error running DES simulation from OSDi experiment " + args.osdiExperiment + " at " + args.osdiPath, e);
        }
    }
    public static void main(String[] args) {
        RootArgs root = new RootArgs();
        CheckOsdiArgs check = new CheckOsdiArgs();
        GenerateExcelOsdiArgs genExcel = new GenerateExcelOsdiArgs();
        RunDESOsdiArgs runDES = new RunDESOsdiArgs();
        
        JCommander jc = JCommander.newBuilder()
            .addObject(root)
            .addCommand("check", check)
            .addCommand("genExcel", genExcel)
            .addCommand("runDES", runDES)
            .build();

        jc.parse(args);

        if (root.help || jc.getParsedCommand() == null) {
            jc.usage();
            return;
        }

        switch (jc.getParsedCommand()) {
            case "check" -> runCheck(check);
            case "genExcel" -> runGenerateExcel(genExcel);
            case "runDES" -> runDES(runDES);
            default -> throw new IllegalStateException("Unknown command");
        }
    }
}
