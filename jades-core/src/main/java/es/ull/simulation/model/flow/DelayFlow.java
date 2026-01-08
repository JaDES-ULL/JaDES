/**
 * 
 */
package es.ull.simulation.model.flow;

import es.ull.simulation.info.ElementActionInfo;
import es.ull.simulation.model.Element;
import es.ull.simulation.model.ElementInstance;
import es.ull.simulation.model.Simulation;

/**
 * A IFlow that makes an element be delayed for a certain time
 * @author Iván Castilla Rodríguez
 *
 */
public abstract class DelayFlow extends AbstractSingleSuccessorFlow implements ITaskFlow, IActionFlow {
    /** A brief description of the delay */
    private final String description;

    /**
     * Creates a delay IFlow
     * @param model The simulation model this IFlow belongs to
     * @param description A short text describing this IFlow
     */
	public DelayFlow(final Simulation model, final String description) {
		super(model);
		this.description = description;
	}

	@Override
	public String getDescription() {
		return description;
	}

    /**
     * Returns the duration of the delay
     * @param elem The element delaying
     * @return The duration of the delay
     */
    public abstract long getDurationSample(final Element elem);

	@Override
	public void addPredecessor(final IFlow predecessor) {
	}

	@Override
	public void afterFinalize(final ElementInstance ei) {
	}

	@Override
	public void request(final ElementInstance ei) {
		if (!ei.wasVisited(this)) {
			if (ei.isExecutable()) {
				if (beforeRequest(ei)) {
					final Element elem = ei.getElement();
					simul.notifyInfo(new ElementActionInfo(simul, ei, elem, this, ei.getExecutionWG(), null, ElementActionInfo.Type.START, simul.getTs()));
					elem.trace("Start delay\t" + this + "\t" + getDescription());	
					ei.startDelay(getDurationSample(elem));
				}
				else {
					ei.cancel(this);
					next(ei);
				}
			}
			else {
				ei.updatePath(this);
				next(ei);
			}
		} else
			ei.notifyEnd();
	}

	@Override
	public void finish(final ElementInstance ei) {
		simul.notifyInfo(new ElementActionInfo(simul, ei, ei.getElement(), this, ei.getExecutionWG(), null, ElementActionInfo.Type.END, simul.getTs()));
		ei.getElement().trace("Finishes\t" + this + "\t" + getDescription());
		afterFinalize(ei);
		next(ei);
	}

}
