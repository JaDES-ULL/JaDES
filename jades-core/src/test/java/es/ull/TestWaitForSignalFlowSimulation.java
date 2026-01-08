/**
 * 
 */
package es.ull;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.TreeMap;

import es.ull.simulation.info.IPieceOfInformation;
import es.ull.simulation.info.SimulationStartStopInfo;
import es.ull.simulation.inforeceiver.BasicListener;
import es.ull.simulation.model.DiscreteEvent;
import es.ull.simulation.model.ElementInstance;
import es.ull.simulation.model.ElementType;
import es.ull.simulation.model.IEventSource;
import es.ull.simulation.model.SimulationObject;
import es.ull.simulation.model.engine.SimulationEngine;
import es.ull.simulation.model.flow.TimeFunctionDelayFlow;
import es.ull.simulation.model.flow.WaitForSignalFlow;

/**
 * A dummy example of using the WaitForSignalFlow class. We define a simple flow with a WaitForSignalFlow and a
 * DelayFlow. Elements get to the WaitForSignalFlow and waits until a special object ({@link SimListener}) tells them to
 * continue. The {@link SimListener} is a class that checks the waiting list every 5 minutes and let pass half of the waiting elements.
 * @author Iván Castilla Rodríguez
 *
 */
public class TestWaitForSignalFlowSimulation extends StandardTestSimulation {
	final static private long DELAY = 10;
	final static private long CHECK_DELAY = 5;
	private SimListener listener;
	private final TreeMap<Long, Integer> elementsPassedPerTime;

	/**
	 */
	public TestWaitForSignalFlowSimulation(TestArguments args) {
		super(0, "Test wait for signal simulation", args);
		elementsPassedPerTime = new TreeMap<>();
	}
	
	@Override
	protected void createModel() {
		final ElementType et = getDefElementType("Message");
		final TimeFunctionDelayFlow delayFlow = new TimeFunctionDelayFlow(this,
				"Little delay", DELAY) {
			@Override
			public void afterFinalize(ElementInstance ei) {
				super.afterFinalize(ei);
				ei.getElement().trace("Finished");
			}
			
			@Override
			public boolean beforeRequest(ElementInstance ei) {
				ei.getElement().trace("Passed");
				if (elementsPassedPerTime.get(getTs()) == null) {
					elementsPassedPerTime.put(getTs(), 1);
				} else {
					elementsPassedPerTime.put(getTs(), elementsPassedPerTime.get(getTs()) + 1);
				}
				return super.beforeRequest(ei);
			}
		};
		registerActivity(delayFlow, DELAY);
		listener = new SimListener(this);
		final WaitForSignalFlow waitFlow = new WaitForSignalFlow(this, "Wait", listener) {
			@Override
			public boolean beforeRequest(ElementInstance ei) {
				ei.getElement().trace("Trying to pass");
				return super.beforeRequest(ei);
			}
		};
		waitFlow.link(delayFlow);
		getDefGenerator(et, waitFlow);
	}

	@Override
	protected void addCheckers() {
		super.addCheckers();
		registerListener(new CheckerListener(getArguments().nElements));
	}
	
	@Override
	public void init() {
		super.init();
		addEvent(listener.onCreate(getTs()));
	}
	
	class SimListener extends SimulationObject implements IEventSource, WaitForSignalFlow.Listener {
		private WaitForSignalFlow IFlow = null;
		final private TestWaitForSignalFlowSimulation simul;
		final private ArrayList<ElementInstance> waiting;
		
		public SimListener(TestWaitForSignalFlowSimulation simul) {
			super(simul, 0, "LIS");
			this.simul = simul;
			this.waiting = new ArrayList<ElementInstance>();
		}

		@Override
		public DiscreteEvent onCreate(long ts) {
			return new CheckEvent(ts + CHECK_DELAY);
		}
		
		@Override
		public DiscreteEvent onDestroy(long ts) {
			return new DiscreteEvent.DefaultFinalizeEvent(this, ts);
		}

		@Override
		public void notifyEnd() {
	        simul.addEvent(onDestroy(simul.getTs()));		
		}

		@Override
		public void register(WaitForSignalFlow IFlow) {
			this.IFlow = IFlow;
			
		}

		@Override
		public void notifyArrival(WaitForSignalFlow IFlow, ElementInstance ei) {
			waiting.add(ei);
		}
		
		class CheckEvent extends DiscreteEvent {

			public CheckEvent(long ts) {
				super(ts);
			}

			@Override
			public void event() {
				final int n = (int)Math.ceil(waiting.size() / 2.0);
				if (n > 0)
					SimListener.this.trace("Allowing " + n + " elements to pass");
				for (int i = 0; i < n; i++) {
					IFlow.signal(waiting.remove(0));
				}
				simul.addEvent(new CheckEvent(ts + CHECK_DELAY));
			}
			
		}

		@Override
		protected void assignSimulation(SimulationEngine engine) {
		}
		
	}

	class CheckerListener extends BasicListener {
		final private int nElements;
		public CheckerListener(int nElements) {
			super("Checker");
			this.nElements = nElements;
			addTargetInformation(SimulationStartStopInfo.class);
		}

		@Override
		public void infoEmited(IPieceOfInformation info) {
			if (info instanceof SimulationStartStopInfo) {
				final SimulationStartStopInfo sInfo = (SimulationStartStopInfo) info;
				ArrayList<Long> times = new ArrayList<>(elementsPassedPerTime.keySet());
				int remaining = nElements;
				if (SimulationStartStopInfo.Type.END.equals(sInfo.getType())) {
					for (int i = 0; i < elementsPassedPerTime.size(); i++) {
						int n = (int)Math.ceil(remaining / 2.0);
						assertEquals(n, elementsPassedPerTime.get(times.get(i)).intValue(), "Wrong number of elements passed at time " + times.get(i));
						remaining -= n;
					}
				}
			}
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestArguments arguments = new TestArguments();
		arguments.simEnd = 35;
		arguments.nElements = 10;
		new TestWaitForSignalFlowSimulation(arguments).run();

	}

}
