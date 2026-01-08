/**
 * 
 */
package es.ull.performance;

import java.util.ArrayList;
import java.util.Arrays;

import es.ull.simulation.model.ElementType;
import es.ull.simulation.model.ResourceType;
import es.ull.simulation.model.Simulation;
import es.ull.simulation.model.WorkGroup;
import es.ull.simulation.model.flow.ActivityFlow;
import es.ull.simulation.model.flow.ForLoopFlow;
import es.ull.simulation.model.flow.InterleavedRoutingFlow;
import es.ull.performance.BenchmarkTest.BenchmarkArguments;
import es.ull.simulation.factory.SimulationFactory;
import es.ull.simulation.factory.SimulationUserCode;
import es.ull.simulation.factory.UserMethod;
import es.ull.simulation.model.SimulationPeriodicCycle;
import es.ull.simulation.model.SimulationTimeFunction;
import es.ull.simulation.model.TimeStamp;
import es.ull.simulation.model.Resource;
import es.ull.simulation.functions.TimeFunctionFactory;

/**
 * @author Iván Castilla Rodríguez
 *
 */
public class BenchmarkModel {
	/**
	 * Defines different test models:
	 * - NORESOURCES: A model with E elements. Each one requests N times an activity. There are A
	 *   activities (A <= E), which are equally distributed among the elements. The activities have 
	 *   a fixed duration and use no resources.
	 * - RESOURCES: Exactly the same than NORESOURCES, but there are as many resources as elements. 
	 *   Resources are distributed in a way that all the activities can be performed.
	 * - CONFLICT: Again the same model, but this time, the resources have multiple timetable entries
	 *   and can be used by any activity. REVIEW!!
	 * - TOTALCONFLICT: Again the same model, but this time, the resources have multiple timetable 
	 *   entries and can be used by any activity.
	 * - PARALLEL: Each resource simultaneously requests ALL the activities. 
	 * @author Iván Castilla Rodríguez
	 */
	public enum ModelType {NORESOURCES, RESOURCES, CONFLICT, MIXCONFLICT, TOTALCONFLICT, PARALLEL}
	/**
	 * Defines the temporal behaviour of the elements when requesting the activities:
	 * - SAMETIME: All the elements request the activities at the same time
	 * - CONSECUTIVE: Only one element requests an activity at a time
	 * - MIXED: Something in the middle of the former ones.
	 * @author Iván Castilla Rodríguez
	 */
	enum OverlappingType {SAMETIME, CONSECUTIVE, MIXED};
	final private String head;
	final static private SimulationTimeFunction oneFunction = new SimulationTimeFunction(Simulation.DEF_TIME_UNIT,
			"ConstantVariate", 1);
	final int id;
	final ModelType modType;
	final OverlappingType ovType;
	final int nThread;
	final int nIter;
	/** Amount of elements. By default it's taken as the activity duration. */
	final int nElem;
	final int nAct;
	final int mixFactor;
	final long workLoad;
	final TimeStamp endTs;
	final SimulationPeriodicCycle allCycle;
	final int rtXact;
	final int rtXres;
	final double resAvailabilityFactor;

	
	/**
	 * @param id
	 */
	public BenchmarkModel(int id, BenchmarkArguments arguments) {
		this.id = id;
		this.modType = ModelType.values()[arguments.modType];
		this.ovType = OverlappingType.values()[arguments.ovType];
		this.nThread = arguments.nThreads;
		this.nIter = arguments.nIter;
		this.nElem = arguments.nElem;
		this.nAct = arguments.nAct;
		this.mixFactor = arguments.mixFactor;
		this.workLoad = arguments.workLoad;
		this.rtXact = arguments.rtXAct;
		this.rtXres = arguments.rtXRes;
		this.resAvailabilityFactor = arguments.resAvailabilityFactor;
		this.endTs = new TimeStamp(Simulation.DEF_TIME_UNIT, arguments.timeHorizon);
		String auxHead = "ParallelSimulation Type\tModel Type\tOverlapping Type\tThreads\tIterations";
		if (modType == ModelType.CONFLICT)
			auxHead += "\tRTxACT\tRTxRES";
		if (workLoad > 0)
			auxHead += "\tWork load";
		if (ovType == OverlappingType.MIXED)
			auxHead += "\tMix";
		auxHead += "\tActivities\tElements";
		head = auxHead;
		this.allCycle = new SimulationPeriodicCycle(Simulation.DEF_TIME_UNIT, TimeStamp.getZero(), new SimulationTimeFunction(Simulation.DEF_TIME_UNIT,
				"ConstantVariate", endTs.getValue()), 0);
	}
	
	@Override
	public String toString() {
		String text = "";
		if (modType == ModelType.CONFLICT)
			text += modType + "\t" + ovType + "\t" + nThread + "\t" + nIter + "\t" + rtXact + "\t"
					+ rtXres;
		else
			text += modType + "\t" + ovType + "\t" + nThread + "\t" + nIter;
		if (workLoad > 0)
			text += "\t" + workLoad;
		if (ovType == OverlappingType.MIXED)
			text += "\t" + mixFactor;
		return text + "\t" + nAct + "\t" + nElem;
	}

	/**
	 * Returns the header of a table containing results from these experiments.
	 * @return The list of fields as shown when using toString
	 */
	public String getHeader() {
		return head;
	}
	
	public Simulation getTestModel() {
		Simulation sim = null;
		switch(modType) {
			case NORESOURCES: sim = getTestSimpleNoResources(); break;
			case RESOURCES: sim = getTestSimpleResources(); break;
			case CONFLICT: sim = getTestConflict(); break;
			case MIXCONFLICT: sim = getTestMixConflict(); break;
			case TOTALCONFLICT: sim = getTestTotalConflict(); break;
			case PARALLEL: sim = getTestParallelSimResources(); break;
		}
		return sim;
	}
	
	private void stdBuildElementGenerators(SimulationFactory factory, ForLoopFlow[] smfs, ActivityFlow[] acts,
										   WorkGroup[] wgs) {
		ElementType et = factory.getElementTypeInstance("E_TEST");
		switch(ovType) {
			case SAMETIME:
				for (int i = 0; i < acts.length; i++)
			    	acts[i].newWorkGroupAdder(wgs[i]).withDelay(nElem).add();
				for (ForLoopFlow smf : smfs) {
					factory.getTimeDrivenElementGeneratorInstance(TimeFunctionFactory.getInstance(
							"ConstantVariate", nElem / smfs.length), et, smf, allCycle);
				}
				break;
			case CONSECUTIVE:
				for (int i = 0; i < acts.length; i++)
			    	acts[i].newWorkGroupAdder(wgs[i]).withDelay(nElem).add();
				for (ForLoopFlow smf : smfs) {
					factory.getTimeDrivenElementGeneratorInstance(TimeFunctionFactory.getInstance(
							"ConstantVariate", 1), et, smf, new SimulationPeriodicCycle(Simulation.DEF_TIME_UNIT,
							TimeStamp.getZero(), oneFunction, nElem));
				}
				break;
			case MIXED:
				for (int i = 0; i < acts.length; i++)
			    	acts[i].newWorkGroupAdder(wgs[i]).withDelay(nElem  / mixFactor).add();
				for (ForLoopFlow smf : smfs) {
					factory.getTimeDrivenElementGeneratorInstance(TimeFunctionFactory.getInstance(
							"ConstantVariate", 1), et, smf, new SimulationPeriodicCycle(Simulation.DEF_TIME_UNIT,
							TimeStamp.getZero(), oneFunction, nElem));
				}
				break;
		}
	}

	private SimulationUserCode addWorkLoad(SimulationFactory factory) {
		SimulationUserCode code = null;
		if (workLoad > 0) {
			code = new SimulationUserCode();
			factory.getSimulation().putVar("AA", 0);
			code.add(UserMethod.BEFORE_REQUEST, "for (int i = 1; i < " + workLoad + "; i++)" +
					"<%SET(S.AA, <%GET(S.AA)%> + Math.log(i))%>;" + 
					"return super.beforeRequest(e);");
		}
		return code;
	}
	
	private Simulation getTestParallelSimResources() {
		ResourceType[] rts = new ResourceType[nAct];
		WorkGroup[] wgs = new WorkGroup[nAct];
		Resource[] res = new Resource[nElem * nAct];
		ActivityFlow[] acts = new ActivityFlow[nAct];
		
		SimulationFactory factory = new SimulationFactory(id, "TEST");
		
		SimulationUserCode code = addWorkLoad(factory);
		
		for (int i = 0; i < acts.length; i++) {
			rts[i] = factory.getResourceTypeInstance("RT" + i);
			wgs[i] = factory.getWorkGroupInstance(new ResourceType[] {rts[i]}, new int[] {1});
			if (code != null)
				acts[i] = (ActivityFlow)factory.getFlowInstance("ActivityFlow", code, "A_TEST" + i);
			else
				acts[i] = (ActivityFlow)factory.getFlowInstance("ActivityFlow", "A_TEST" + i);
		}
		InterleavedRoutingFlow iFlow = (InterleavedRoutingFlow)factory.getFlowInstance("InterleavedRoutingFlow");
		for (ActivityFlow sf : acts)
			iFlow.addBranch(sf);
		ForLoopFlow rootFlow = (ForLoopFlow)factory.getFlowInstance("ForLoopFlow", iFlow,
				TimeFunctionFactory.getInstance("ConstantVariate", nIter));

		for (int i = 0; i < nElem * nAct; i++) {
			res[i] = factory.getResourceInstance("RES_TEST" + i);
			res[i].newTimeTableOrCancelEntriesAdder(rts[i % nAct]).withDuration(allCycle, endTs).addTimeTableEntry();
		}

		stdBuildElementGenerators(factory, new ForLoopFlow[] {rootFlow}, acts, wgs);
		return factory.getSimulation();

	}

	private Simulation getTestSimpleResources() {
		ResourceType[] rts = new ResourceType[nAct];
		WorkGroup[] wgs = new WorkGroup[nAct];
		Resource[] res = new Resource[nElem];
		
		ActivityFlow[] acts = new ActivityFlow[nAct];
		ForLoopFlow[] smfs = new ForLoopFlow[nAct];
		
		SimulationFactory factory = new SimulationFactory(id, "TEST");
		
		SimulationUserCode code = addWorkLoad(factory);
		
		for (int i = 0; i < acts.length; i++) {
			rts[i] = factory.getResourceTypeInstance("RT" + i);
			wgs[i] = factory.getWorkGroupInstance(new ResourceType[] {rts[i]}, new int[] {1});
			if (code != null)
				acts[i] = (ActivityFlow)factory.getFlowInstance("ActivityFlow", code, "A_TEST" + i);
			else
				acts[i] = (ActivityFlow)factory.getFlowInstance("ActivityFlow", "A_TEST" + i);
			smfs[i] = (ForLoopFlow)factory.getFlowInstance("ForLoopFlow", acts[i],
					TimeFunctionFactory.getInstance("ConstantVariate", nIter));
		}

		for (int i = 0; i < nElem; i++) {
			res[i] = factory.getResourceInstance("RES_TEST" + i);
			res[i].newTimeTableOrCancelEntriesAdder(rts[i % nAct]).withDuration(allCycle, endTs).addTimeTableEntry();
		}
		
		stdBuildElementGenerators(factory, smfs, acts, wgs);
		return factory.getSimulation();

	}

	private Simulation getTestSimpleNoResources() {
		ActivityFlow[] acts = new ActivityFlow[nAct];
		ForLoopFlow[] smfs = new ForLoopFlow[nAct];
		
		SimulationFactory factory = new SimulationFactory(id, "TEST");
		
		SimulationUserCode code = addWorkLoad(factory);
		
		for (int i = 0; i < acts.length; i++) {
			if (code != null)
				acts[i] = (ActivityFlow)factory.getFlowInstance("ActivityFlow", code, "A_TEST" + i);
			else
				acts[i] = (ActivityFlow)factory.getFlowInstance("ActivityFlow", "A_TEST" + i);
			smfs[i] = (ForLoopFlow)factory.getFlowInstance("ForLoopFlow", acts[i],
					TimeFunctionFactory.getInstance("ConstantVariate", nIter));
		}
		
		WorkGroup wg = factory.getWorkGroupInstance(new ResourceType[0], new int[0]);
		WorkGroup []wgs = new WorkGroup[nAct];
		// Assigns the same WG to each activity
		Arrays.fill(wgs, wg);
		
		stdBuildElementGenerators(factory, smfs, acts, wgs);
		return factory.getSimulation();

	}
	
	private Simulation getTestTotalConflict() {
		ActivityFlow[] acts = new ActivityFlow[nAct];
		ForLoopFlow[] smfs = new ForLoopFlow[nAct];
		ResourceType[] rts = new ResourceType[nAct];
		WorkGroup[] wgs = new WorkGroup[nAct];
		Resource[] res = new Resource[nElem];

		SimulationFactory factory = new SimulationFactory(id, "TEST");
		
		SimulationUserCode code = addWorkLoad(factory);
		
		for (int i = 0; i < nElem; i++)
			res[i] = factory.getResourceInstance("RES_TEST" + i);
		for (int i = 0; i < acts.length; i++) {
			if (code != null)
				acts[i] = (ActivityFlow)factory.getFlowInstance("ActivityFlow", code, "A_TEST" + i);
			else
				acts[i] = (ActivityFlow)factory.getFlowInstance("ActivityFlow", "A_TEST" + i);
			smfs[i] = (ForLoopFlow)factory.getFlowInstance("ForLoopFlow", acts[i],
					TimeFunctionFactory.getInstance("ConstantVariate", nIter));
			rts[i] = factory.getResourceTypeInstance("RT_TEST" + i);
			wgs[i] = factory.getWorkGroupInstance(new ResourceType[] {rts[i]}, new int[] {1});
		}
		ArrayList<ResourceType> list = new ArrayList<ResourceType>(Arrays.asList(rts));
		for (int j = 0; j < nElem; j++)
			res[j].newTimeTableOrCancelEntriesAdder(list).withDuration(allCycle, endTs).addTimeTableEntry();

//		for (int j = 0; j < nElem; j++)
//			res[j].addTimeTableEntry(allCycle, endTs, rts[j % acts.length]);
		
		stdBuildElementGenerators(factory, smfs, acts, wgs);
		return factory.getSimulation();
	}
	
	private Simulation getTestMixConflict() {
		ActivityFlow[] acts = new ActivityFlow[nAct];
		ForLoopFlow[] smfs = new ForLoopFlow[nAct];
		ResourceType[] rts = new ResourceType[nAct];
		WorkGroup[] wgs = new WorkGroup[nAct];
		Resource[] res = new Resource[nElem];

		SimulationFactory factory = new SimulationFactory(id, "TEST");
		
		SimulationUserCode code = addWorkLoad(factory);
		
		for (int i = 0; i < nElem / 2; i++)
			res[i] = factory.getResourceInstance("RES_TEST" + i);
		for (int i = 0; i < acts.length; i++) {
			if (code != null)
				acts[i] = (ActivityFlow)factory.getFlowInstance("ActivityFlow", code, "A_TEST" + i);
			else
				acts[i] = (ActivityFlow)factory.getFlowInstance("ActivityFlow", "A_TEST" + i);
			smfs[i] = (ForLoopFlow)factory.getFlowInstance("ForLoopFlow", acts[i],
					TimeFunctionFactory.getInstance("ConstantVariate", nIter));
			rts[i] = factory.getResourceTypeInstance("RT_TEST" + i);
			wgs[i] = factory.getWorkGroupInstance(new ResourceType[] {rts[i]}, new int[] {1});
		}
		ArrayList<ResourceType> list = new ArrayList<ResourceType>(Arrays.asList(rts));
		for (int j = 0; j < nElem / 2; j++)
			res[j].newTimeTableOrCancelEntriesAdder(list).withDuration(allCycle, endTs).addTimeTableEntry();
		
		stdBuildElementGenerators(factory, smfs, acts, wgs);

		return factory.getSimulation();		
	}

	private Simulation getTestConflict() {
		
		ResourceType[] rts = new ResourceType[nAct * rtXact];
		WorkGroup[] wgs = new WorkGroup[nAct];
		Resource[] res = new Resource[(int) (nElem * rtXact * resAvailabilityFactor)];
		
		ActivityFlow[] acts = new ActivityFlow[nAct];
		ForLoopFlow[] smfs = new ForLoopFlow[nAct];
		
		SimulationFactory factory = new SimulationFactory(id, "TEST");
		
		SimulationUserCode code = addWorkLoad(factory);
	
		for (int i = 0; i < rts.length; i++)
			rts[i] = factory.getResourceTypeInstance("RT" + i);
		
		for (int i = 0; i < acts.length; i++) {
			ResourceType[] rtGroup = new ResourceType[rtXact];
			int[] needGroup = new int[rtXact];
			for (int j = 0; j < rtXact; j++) {
				rtGroup[j] = rts[i * rtXact + j];
				needGroup[j] = 1;
			}
			wgs[i] = factory.getWorkGroupInstance(rtGroup, needGroup);
			if (code != null)
				acts[i] = (ActivityFlow)factory.getFlowInstance("ActivityFlow", code, "A_TEST" + i);
			else
				acts[i] = (ActivityFlow)factory.getFlowInstance("ActivityFlow", "A_TEST" + i);
			smfs[i] = (ForLoopFlow)factory.getFlowInstance("ForLoopFlow", acts[i],
					TimeFunctionFactory.getInstance("ConstantVariate", nIter));
		}

		for (int i = 0; i < res.length; i++) {
			res[i] = factory.getResourceInstance("RES_TEST" + i);
			ArrayList<ResourceType> roles = new ArrayList<ResourceType>();
			for (int j = 0; j < rtXres; j++)
				roles.add(rts[(i + (int) (j * (rts.length / rtXres) * resAvailabilityFactor)) % rts.length]);
			res[i].newTimeTableOrCancelEntriesAdder(roles).withDuration(allCycle, endTs).addTimeTableEntry();
		}
		
		stdBuildElementGenerators(factory, smfs, acts, wgs);
		return factory.getSimulation();

	}
}
