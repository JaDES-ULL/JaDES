package es.ull.simulation.info;

import es.ull.simulation.model.Element;
import es.ull.simulation.model.ElementInstance;
import es.ull.simulation.model.Simulation;
import es.ull.simulation.model.Resource;
import es.ull.simulation.model.ResourceType;
import es.ull.simulation.model.flow.IResourceHandlerFlow;

public class ResourceUsageInfo extends SimulationInfo {

	/** Possible types of resource information */
	public enum Type implements IPieceOfInformation.IInfoType {
			CAUGHT	("CAUGHT RESOURCE"), 
			RELEASED	("RELEASED RESOURCE");
			
			private final String description;
			
			Type (String description) {
				this.description = description;
			}

			public String getDescription() {
				return description;
			}		
			
		};
	
	final private Resource res;
	final private ResourceType rt;
	final private ElementInstance instance;
	final private IResourceHandlerFlow act;
	final private Type type;
	final private Element elem;
	
	public ResourceUsageInfo(final Simulation model, final Resource res, final ResourceType rt,
							 final ElementInstance instance, final Element elem, final IResourceHandlerFlow act,
							 final Type type, final long ts) {
		super(model, ts);
		this.res = res;
		this.rt = rt;
		this.instance = instance;
		this.elem = elem;
		this.act = act;
		this.type = type;
	}
	
	public Resource getResource() {
		return res;
	}
	
	public ResourceType getResourceType() {
		return rt;
	}
	
	public ElementInstance getElementInstance() {
		return instance;
	}
	
	public Type getType() {
		return type;
	}
	
	public String toString() {
		String message = "" + simul.long2SimulationTime(getTs()) + "\t" + elem.toString() + "\t";
		message += type.getDescription() + "\t" + res.toString() + "\t" + res.getDescription() + "\t";
		message += "ROLE: " + rt.getDescription() + "\t";	
		message += "ACT: " + act.getDescription() + "\t";
		return message;
	}

	public IResourceHandlerFlow getActivity() {
		return act;
	}
}
