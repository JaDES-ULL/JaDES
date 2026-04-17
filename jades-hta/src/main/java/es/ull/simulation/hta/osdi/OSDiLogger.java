package es.ull.simulation.hta.osdi;

import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.ull.simulation.hta.osdi.ontology.OSDiDataProperty;
import es.ull.simulation.hta.osdi.ontology.OSDiObjectProperty;

public class OSDiLogger {
    private final Logger logger;
    
    private OSDiLogger(Class<?> clazz) {
        this.logger = LoggerFactory.getLogger(clazz);
    }    
    
    public static OSDiLogger getLogger(Class<?> clazz) {
        return new OSDiLogger(clazz);
    }

	public void warn(String individualIRI, OSDiDataProperty prop, String msg) {
		warn(individualIRI + "\t" + prop.getShortName() + "\tWARNING\t\"" + msg + "\"");
	}
	
	public void warn(String individualIRI, OSDiObjectProperty prop, String msg) {
		warn(individualIRI + "\t" + prop.getShortName() + "\tWARNING\t\"" + msg + "\"");
	}
	
	public void warn(boolean condition, String individualIRI, OSDiDataProperty prop, String msg) {
		if (condition)
			warn(individualIRI, prop, msg);
	}
	
	public void warn(boolean condition, String individualIRI, OSDiObjectProperty prop, String msg) {
		if (condition)
			warn(individualIRI, prop, msg);
	}

	public void warn(IRI individualIRI, OSDiDataProperty prop, String msg) {
		warn(individualIRI.getShortForm() + "\t" + prop.getShortName() + "\tWARNING\t\"" + msg + "\"");
	}
	
	public void warn(IRI individualIRI, OSDiObjectProperty prop, String msg) {
		warn(individualIRI.getShortForm() + "\t" + prop.getShortName() + "\tWARNING\t\"" + msg + "\"");
	}
	
	public void warn(boolean condition, IRI individualIRI, OSDiDataProperty prop, String msg) {
		if (condition)
			warn(individualIRI, prop, msg);
	}
	
	public void warn(boolean condition, IRI individualIRI, OSDiObjectProperty prop, String msg) {
		if (condition)
			warn(individualIRI, prop, msg);
	}
	
	public void warn(boolean condition, String msg) {
		if (condition)
			warn(msg);
	}

    public void info(String msg) {
        logger.info(msg);
    }

    public void warn(String msg) {
        logger.warn(msg);
    }

    public void error(String msg, Throwable t) {
        logger.error(msg, t);
    }

	public void debug(String msg) {
		logger.debug(msg);
	}
}
