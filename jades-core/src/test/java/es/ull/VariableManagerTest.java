package es.ull;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.beust.jcommander.JCommander;

import es.ull.simulation.experiment.BaseExperiment;
import es.ull.simulation.experiment.CommonArguments;
import es.ull.simulation.factory.SimulationFactory;
import es.ull.simulation.model.Simulation;
import es.ull.simulation.model.TimeStamp;
import es.ull.simulation.model.TimeUnit;
import es.ull.simulation.variable.EnumType;
import es.ull.simulation.variable.EnumVariable;
import es.ull.simulation.variable.IntVariable;



public class VariableManagerTest {	
	@Test
	public void test1() {
		final CommonArguments arguments = new CommonArguments();
		final JCommander jc = JCommander.newBuilder().addObject(arguments).build();
		jc.parse("-q");

		new ExperimentManagerTest1(arguments).run();
	}

	static class ExperimentManagerTest1 extends BaseExperiment {
		final static TimeUnit unit = Simulation.DEF_TIME_UNIT;
		static final int NDAYS = 1;
		static final double PERIOD = 1040.0;
		
		public ExperimentManagerTest1(CommonArguments arguments) {
			super("Banco", arguments);
		}
	
		public void runExperiment(int ind) {
			boolean quiet = getArguments().quiet;
			SimulationFactory factory = new SimulationFactory(ind, "Ej");
			Simulation sim = factory.getSimulation();
	
			factory.getFlowInstance("ActivityFlow", "Verificar cuenta", 0, false, false);
			
			sim.putVar("Coste total", new IntVariable(0));
			sim.putVar("Coste", new IntVariable(200));
			
			IntVariable temp = (IntVariable) sim.getVar("Coste");
			temp.setValue(temp.getValue().intValue() * 10); 
			assertEquals(sim.getVar("Coste").getValue(), 2000);
			if (!quiet)
				System.out.println("Coste de la actividad = " + sim.getVar("Coste").toString());
			
			((IntVariable)sim.getVar("Coste total")).setValue(sim.getVar("Coste").getValue());
			assertEquals(sim.getVar("Coste total").getValue(), 2000);
			if (!quiet)
				System.out.println("Coste total= " + sim.getVar("Coste total").toString());
			
			EnumType type = new EnumType("Deportivo", "Familiar", "Gasoil");
			sim.putVar("tipoCoche", new EnumVariable(type, Integer.valueOf(0)));
			((EnumVariable)sim.getVar("tipoCoche")).setValue(2);
			assertEquals(sim.getVar("tipoCoche").toString(), "Gasoil");
			if (!quiet)
				System.out.println("Valor del enumerado: " + sim.getVar("tipoCoche").toString());
			((EnumVariable)sim.getVar("tipoCoche")).setValue("Deportivo");
			assertEquals(sim.getVar("tipoCoche").toString(), "Deportivo");
			if (!quiet)
				System.out.println("Valor del enumerado: " + sim.getVar("tipoCoche").toString());
	
			sim.run(new TimeStamp(TimeUnit.DAY, NDAYS));
		}
	}
	}
