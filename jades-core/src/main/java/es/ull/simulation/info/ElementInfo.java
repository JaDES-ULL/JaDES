package es.ull.simulation.info;

import es.ull.simulation.model.Element;
import es.ull.simulation.model.ElementType;
import es.ull.simulation.model.Simulation;

public class ElementInfo extends SimulationInfo {

	/** Possible types of element information */
	public enum Type implements IPieceOfInformation.IInfoType {
			START ("ELEMENT START"), 
			FINISH ("ELEMENT FINISH");
			
			private final String description;
			
			Type(String description) {
				this.description = description;
			}

			public String getDescription() {
				return description;
			}

		};
	
	final private Element elem;
	final private Type type;
	final private ElementType et;
	
	public ElementInfo(final Simulation model, final Element elem,
					   final ElementType et, final Type type, final long ts) {
		super(model, ts);
		this.elem = elem;
		this.type = type;
		this.et = et;
	}
	
	public Element getElement() {
		return elem;
	}

	public Type getType() {
		return type;
	}
	
	public String toString() {
		return "" + simul.long2SimulationTime(getTs()) + "\t" + elem.toString() + "\t" +
				type.getDescription() + "\tET: " + et.getDescription();
	}
}
