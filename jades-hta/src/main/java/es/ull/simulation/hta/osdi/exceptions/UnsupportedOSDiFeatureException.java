package es.ull.simulation.hta.osdi.exceptions;

import es.ull.simulation.hta.osdi.ontology.OSDiClass;
import es.ull.simulation.hta.osdi.ontology.OSDiDataProperty;
import es.ull.simulation.hta.osdi.ontology.OSDiObjectProperty;

public class UnsupportedOSDiFeatureException extends Exception implements OSDiException {
    private static final long serialVersionUID = 1L;

    public UnsupportedOSDiFeatureException(String message) {
        super("Unsupported OSDi feature: " + message);
    }

    public UnsupportedOSDiFeatureException(String message, Throwable cause) {
        super("Unsupported OSDi feature: " + message, cause);
    }

	public UnsupportedOSDiFeatureException(OSDiClass involvedClass, String instanceName, OSDiDataProperty involvedProperty, String content) {
		this("(" + involvedClass.getShortName() + ") " + instanceName + ":" + involvedProperty.getShortName() + "\tError parsing\"" + content + "\"");
	}

	public UnsupportedOSDiFeatureException(OSDiClass involvedClass, String instanceName, OSDiDataProperty involvedProperty, String content, Throwable cause) {
		this("(" + involvedClass.getShortName() + ") " + instanceName + ":" + involvedProperty.getShortName() + "\tError parsing\"" + content + "\"", cause);
	}

	public UnsupportedOSDiFeatureException(OSDiClass involvedClass, String instanceName, OSDiObjectProperty involvedProperty, String content) {
		this("(" + involvedClass.getShortName() + ") " + instanceName + ":" + involvedProperty.getShortName() + "\tError parsing\"" + content + "\"");
	}

	public UnsupportedOSDiFeatureException(OSDiClass involvedClass, String instanceName, OSDiObjectProperty involvedProperty, String content, Throwable cause) {
		this("(" + involvedClass.getShortName() + ") " + instanceName + ":" + involvedProperty.getShortName() + "\tError parsing\"" + content + "\"", cause);
    }
}
