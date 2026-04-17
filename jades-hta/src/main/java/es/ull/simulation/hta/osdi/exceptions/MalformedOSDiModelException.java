package es.ull.simulation.hta.osdi.exceptions;

import es.ull.simulation.hta.osdi.ontology.OSDiDataProperty;
import es.ull.simulation.hta.osdi.ontology.OSDiObjectProperty;

import org.semanticweb.owlapi.model.IRI;

import es.ull.simulation.hta.osdi.ontology.OSDiClass;

/**
 * An exception related to a malformed OSDi model.
 * This exception is thrown when the OSDi model does not conform to the expected structure or content.
 */
public class MalformedOSDiModelException extends Exception implements OSDiException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1683838127328936001L;

	public MalformedOSDiModelException(String message, Throwable cause) {
		super("The OSDi model was incomplete or malformed: " + message, cause);
	}

	public MalformedOSDiModelException(String message) {
		super("The OSDi model was incomplete or malformed: " + message);
	}

	public MalformedOSDiModelException(OSDiClass involvedClass, String instanceName, OSDiDataProperty involvedProperty, String content) {
		this("(" + involvedClass.getShortName() + ") " + instanceName + ":" + involvedProperty.getShortName() + "\tError parsing\"" + content + "\"");
	}

	public MalformedOSDiModelException(OSDiClass involvedClass, String instanceName, OSDiDataProperty involvedProperty, String content, Throwable cause) {
		this("(" + involvedClass.getShortName() + ") " + instanceName + ":" + involvedProperty.getShortName() + "\tError parsing\"" + content + "\"", cause);
	}

	public MalformedOSDiModelException(OSDiClass involvedClass, String instanceName, OSDiObjectProperty involvedProperty, String content) {
		this("(" + involvedClass.getShortName() + ") " + instanceName + ":" + involvedProperty.getShortName() + "\tError parsing\"" + content + "\"");
	}

	public MalformedOSDiModelException(OSDiClass involvedClass, String instanceName, OSDiObjectProperty involvedProperty, String content, Throwable cause) {
		this("(" + involvedClass.getShortName() + ") " + instanceName + ":" + involvedProperty.getShortName() + "\tError parsing\"" + content + "\"", cause);
	}

	public MalformedOSDiModelException(OSDiClass involvedClass, IRI individualIRI, OSDiDataProperty involvedProperty, String content) {
		this("(" + involvedClass.getShortName() + ") " + individualIRI + ":" + involvedProperty.getShortName() + "\tError parsing\"" + content + "\"");
	}

	public MalformedOSDiModelException(OSDiClass involvedClass, IRI individualIRI, OSDiDataProperty involvedProperty, String content, Throwable cause) {
		this("(" + involvedClass.getShortName() + ") " + individualIRI + ":" + involvedProperty.getShortName() + "\tError parsing\"" + content + "\"", cause);
	}

	public MalformedOSDiModelException(OSDiClass involvedClass, IRI individualIRI, OSDiObjectProperty involvedProperty, String content) {
		this("(" + involvedClass.getShortName() + ") " + individualIRI + ":" + involvedProperty.getShortName() + "\tError parsing\"" + content + "\"");
	}

	public MalformedOSDiModelException(OSDiClass involvedClass, IRI individualIRI, OSDiObjectProperty involvedProperty, String content, Throwable cause) {
		this("(" + involvedClass.getShortName() + ") " + individualIRI + ":" + involvedProperty.getShortName() + "\tError parsing\"" + content + "\"", cause);
	}
}