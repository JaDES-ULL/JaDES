package es.ull.simulation.examples;

import java.util.ArrayList;

import com.beust.jcommander.JCommander;

import es.ull.simulation.functions.TimeFunctionFactory;
import es.ull.simulation.condition.AbstractCondition;
import es.ull.simulation.condition.NotCondition;
import es.ull.simulation.experiment.BaseExperiment;
import es.ull.simulation.experiment.CommonArguments;
import es.ull.simulation.factory.SimulationFactory;
import es.ull.simulation.factory.SimulationUserCode;
import es.ull.simulation.factory.UserMethod;
import es.ull.simulation.inforeceiver.StdInfoListener;
import es.ull.simulation.model.ElementInstance;
import es.ull.simulation.model.ElementType;
import es.ull.simulation.model.Resource;
import es.ull.simulation.model.ResourceType;
import es.ull.simulation.model.Simulation;
import es.ull.simulation.model.SimulationWeeklyPeriodicCycle;
import es.ull.simulation.model.WorkGroup;
import es.ull.simulation.model.flow.ActivityFlow;
import es.ull.simulation.model.flow.IFlow;
import es.ull.simulation.model.flow.MultiChoiceFlow;
import es.ull.simulation.utils.cycle.WeeklyPeriodicCycle;

class BarrelShippingExperiment extends BaseExperiment {

	static final int NDAYS = 1;
	static final int NTHREADS = 2;
	
	public BarrelShippingExperiment(CommonArguments arguments) {
		super("Barrel Shipping Experiment", arguments);
	}
	
	@Override
	public void runExperiment(int ind) {
		SimulationFactory factory = new SimulationFactory(
				ind, "Barrel shipping");
		Simulation simul = factory.getSimulation();
		
		// Declares global model variables
		simul.putVar("totalLiters", 0.0);
		simul.putVar("shipments", 0);

		ElementType etShipping = factory.getElementTypeInstance("etShipping");
		
		ResourceType rtOperator = factory.getResourceTypeInstance("rtOperator");
    	
		// Defines the resource timetables: Operators work only the weekdays, starting at 8 am 
		SimulationWeeklyPeriodicCycle resCycle = new SimulationWeeklyPeriodicCycle(
				simul.getTimeUnit(), WeeklyPeriodicCycle.WEEKDAYS, 480, 0);

		// Declares two operators who work 8 hours a day
		Resource operator1 = factory.getResourceInstance("Operator1");
		operator1.newTimeTableOrCancelEntriesAdder(rtOperator).withDuration(resCycle, 480).addTimeTableEntry();
		Resource operator2 = factory.getResourceInstance("Operator2");
		operator2.newTimeTableOrCancelEntriesAdder(rtOperator).withDuration(resCycle, 480).addTimeTableEntry();

		
		// Defines the needs of the activities in terms of resources
		WorkGroup wgOperator = factory.getWorkGroupInstance(new ResourceType [] {rtOperator}, new int[] {1});	

		// Defines the way the variables are updated when filling the barrels	
		SimulationUserCode userMethods = new SimulationUserCode();
		userMethods.add(UserMethod.AFTER_FINALIZE, 
				"if (<%GET(S.totalLiters)%> < <%GET(S.barrelCapacity)%>) {" +
					"double random = Math.random() * 50; " +
					"<%SET(S.totalLiters, <%GET(S.totalLiters)%> + random)%>;" +	
				"}");
			
		// Declares activities (Tasks)
		ActivityFlow actFilling = (ActivityFlow)factory.getFlowInstance(
				"ActivityFlow", userMethods, "Barrel Filling", 0, false, false);
		// Defines the way the variables are updated when shipping the barrels
		userMethods.clear();
		userMethods.add(UserMethod.BEFORE_REQUEST, "<%SET(S.totalLiters, 0)%>;" +
					"<%SET(S.shipments, <%GET(S.shipments)%> + 1)%>;" +
					"return true;");

		ActivityFlow actShipping = (ActivityFlow)factory.getFlowInstance(
				"ActivityFlow", userMethods, "Barrel Shipping", 0, false, false);

		// Declares variables for the Barrel Filling activity
		simul.putVar("barrelCapacity", 100);

		// Defines duration of activities
		actFilling.newWorkGroupAdder(wgOperator).withDelay(15).add();
		actShipping.newWorkGroupAdder(wgOperator).withDelay(20).add();

		// Defines loop conditions	
		AbstractCondition<ElementInstance> cond = factory.getCustomizedConditionInstance(
				"", "<%GET(S.totalLiters)%> < <%GET(S.barrelCapacity)%>");
		NotCondition<ElementInstance> notCond = new NotCondition<ElementInstance>(cond);

		// Declares a MultiChoice node	
		MultiChoiceFlow mul1 = (MultiChoiceFlow) factory.getFlowInstance("MultiChoiceFlow");


		// Defines the workflow
		actFilling.link(mul1);
		ArrayList<IFlow> succList = new ArrayList<IFlow>();
		succList.add(actFilling);
		succList.add(actShipping);
		ArrayList<AbstractCondition<ElementInstance>> condList = new ArrayList<AbstractCondition<ElementInstance>>();
		condList.add(cond);
		condList.add(notCond);
		mul1.link(succList, condList);

		// Defines the way the processes are created
		SimulationWeeklyPeriodicCycle cGen = new SimulationWeeklyPeriodicCycle(
				simul.getTimeUnit(), WeeklyPeriodicCycle.WEEKDAYS, 0, NDAYS);
		factory.getTimeDrivenElementGeneratorInstance(TimeFunctionFactory.getInstance(
				"ConstantVariate", 1.0), etShipping, actFilling, cGen);

		simul.registerListener(new StdInfoListener());
		simul.run(0, NDAYS * 24 * 60);
	}

}

public class BarrelShipping {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final CommonArguments arguments = new CommonArguments();
		final JCommander jc = JCommander.newBuilder().addObject(arguments).build();
		jc.parse(args);

		new BarrelShippingExperiment(arguments).run();
	}

}
