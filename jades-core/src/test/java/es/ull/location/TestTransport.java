/**
 * 
 */
package es.ull.location;

import java.util.ArrayList;

import com.beust.jcommander.JCommander;

import es.ull.simulation.experiment.BaseExperiment;
import es.ull.simulation.experiment.CommonArguments;
import es.ull.simulation.functions.TimeFunctionFactory;
import es.ull.simulation.info.ElementActionInfo;
import es.ull.simulation.info.EntityLocationInfo;
import es.ull.simulation.info.IPieceOfInformation;
import es.ull.simulation.inforeceiver.BasicListener;
import es.ull.simulation.model.ElementType;
import es.ull.simulation.model.ResourceType;
import es.ull.simulation.model.Simulation;
import es.ull.simulation.model.SimulationPeriodicCycle;
import es.ull.simulation.model.SimulationTimeFunction;
import es.ull.simulation.model.TimeDrivenElementGenerator;
import es.ull.simulation.model.WorkGroup;
import es.ull.simulation.model.flow.ReleaseResourcesFlow;
import es.ull.simulation.model.flow.RequestResourcesFlow;
import es.ull.simulation.model.location.Location;
import es.ull.simulation.model.location.IMovable;
import es.ull.simulation.model.location.MoveResourcesFlow;
import es.ull.simulation.model.location.Node;
import es.ull.simulation.model.location.Path;
import es.ull.simulation.model.location.IRouter;
import es.ull.simulation.model.location.TransportFlow;

/**
 * @author Iván Castilla Rodríguez
 *
 */
public class TestTransport extends BaseExperiment {
	private static final long ENDTS = 200;
	private static final int NELEM = 3;
	private static final int NMOTOS = 2;
	private static final int MOTOSIZE = 1;
	private static final int NPATHS = 3;
	private static final long DELAY_HOME = 5;
	private static final long DELAY_PATH = 10;
	private static final boolean NOSIZE = false;
	private static final boolean UNREACHABLE = false;

	public TestTransport(CommonArguments arguments) {
		super("Experiment with locations", arguments);
	}

	class MyRouter implements IRouter {
		final private Node home;
		final private Node destination;
		final private Path[] paths;
		
		public MyRouter() {
			home = NOSIZE ? new Node("Pizzeria", TimeFunctionFactory.getInstance(
					"ConstantVariate", DELAY_HOME)) :
				new Node("Pizzeria", TimeFunctionFactory.getInstance(
						"ConstantVariate", DELAY_HOME), NELEM * MOTOSIZE);
			paths = new Path[NPATHS];
			for (int i = 0; i < NPATHS; i++) {
				paths[i] = NOSIZE ? new Path("Path " + i, TimeFunctionFactory.getInstance(
						"ConstantVariate", DELAY_PATH)) :
					new Path("Path " + i, TimeFunctionFactory.getInstance(
							"ConstantVariate", DELAY_PATH), 1, 1);
			}
			destination = NOSIZE ? new Node("Client", TimeFunctionFactory.getInstance(
					"ConstantVariate", 0)) :
				new Node("Client", NELEM * MOTOSIZE);

			home.linkTo(paths[0]);
			for (int i = 0; i < NPATHS - 1; i++) {
				paths[i].linkTo(paths[i + 1]);
			}
			if (!UNREACHABLE)
				paths[NPATHS - 1].linkTo(destination);
		}
		
		
		/**
		 * @return the home
		 */
		public Node getHome() {
			return home;
		}

		/**
		 * @return the destination
		 */
		public Node getDestination() {
			return destination;
		}

		@Override
		public Location getNextLocationTo(IMovable entity, Location finalLocation) {
			if (destination.equals(finalLocation)) {
				final ArrayList<Location> links = entity.getLocation().getLinkedTo();
				if (links.size() > 0)
					return links.get(0);
			}
			else if (home.equals(finalLocation)) {
				final ArrayList<Location> links = entity.getLocation().getLinkedFrom();
				if (links.size() > 0)
					return links.get(0);
			}
			return IRouter.UNREACHABLE_LOCATION;
		}
		
	}

	class SimulLocation extends Simulation {
		public SimulLocation(int id) {
			super(id, "Simulating locations " + id);
			final MyRouter IRouter = new MyRouter(); 
			final ElementType et = new ElementType(this, "Pizza request from client");
			final ResourceType rtMoto = new ResourceType(this, "Delivery moto");
			rtMoto.addGenericResources(NMOTOS, NOSIZE ? 0 : MOTOSIZE, IRouter.getHome());
			final WorkGroup wgMoto = new WorkGroup(this, rtMoto, 1);
			final RequestResourcesFlow reqFlow = new RequestResourcesFlow(this, "Request moto");
			reqFlow.newWorkGroupAdder(wgMoto).add();
			final MoveResourcesFlow moveFlow1 = new MoveResourcesFlow(this, "Move moto to pizzeria",
					IRouter.getHome(), IRouter, wgMoto);
			final TransportFlow moveFlow2 = new TransportFlow(this, "Take pizza to destination",
					IRouter.getDestination(), IRouter, rtMoto);
			final ReleaseResourcesFlow relFlow = new ReleaseResourcesFlow(this, "Release pizza",
					wgMoto);
			reqFlow.link(moveFlow1).link(moveFlow2).link(relFlow);
			
			new TimeDrivenElementGenerator(this, NELEM, et, reqFlow, 0, IRouter.getHome(),
					new SimulationPeriodicCycle(getTimeUnit(), 0L, new SimulationTimeFunction(getTimeUnit(),
							"ConstantVariate", getEndTs()), 1));
		}
		
	}

	class LocationListener extends BasicListener {

		public LocationListener() {
			super("Location listener");
			addTargetInformation(EntityLocationInfo.class);
			addTargetInformation(ElementActionInfo.class);
		}

		@Override
		public void infoEmited(IPieceOfInformation info) {
			System.out.println(info);
		}
		
	}

	@Override
	public void runExperiment(int ind) {
		final SimulLocation sim =  new SimulLocation(ind);
		sim.registerListener(new LocationListener());
		sim.run(ENDTS);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final CommonArguments arguments = new CommonArguments();
		final JCommander jc = JCommander.newBuilder().addObject(arguments).build();
		jc.parse(args);

		new TestTransport(arguments).run();;

	}

}
