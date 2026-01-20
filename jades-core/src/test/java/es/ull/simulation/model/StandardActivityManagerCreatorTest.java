package es.ull.simulation.model;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.ull.simulation.model.engine.SimulationEngine;
import es.ull.simulation.model.flow.RequestResourcesFlow;

class StandardActivityManagerCreatorTest {
    private Simulation simulation;

    @BeforeEach
    void setUp() {
        simulation = new Simulation(1, "Test Simulation");
        SimulationEngine engine = new SimulationEngine(1, simulation);
        simulation.setSimulationEngine(engine);
    }

    @Test
    void shouldAssignSameManagerForConnectedResourceTypes() {
        ResourceType rt1 = new ResourceType(simulation, "RT1");
        ResourceType rt2 = new ResourceType(simulation, "RT2");

        WorkGroup wg = new WorkGroup(simulation, new ResourceType[] { rt1, rt2 }, new int[] { 1, 1 });
        RequestResourcesFlow flow = new RequestResourcesFlow(simulation, "Flow1");
        flow.newWorkGroupAdder(wg).add();

        StandardActivityManagerCreator creator = new StandardActivityManagerCreator(simulation);
        creator.createActivityManagers();

        assertNotNull(rt1.getManager());
        assertNotNull(rt2.getManager());
        assertSame(rt1.getManager(), rt2.getManager());
        assertSame(rt1.getManager(), flow.getManager());
    }

    @Test
    void shouldAssignDifferentManagersForDisconnectedResourceTypes() {
        ResourceType rt1 = new ResourceType(simulation, "RT1");
        ResourceType rt2 = new ResourceType(simulation, "RT2");

        StandardActivityManagerCreator creator = new StandardActivityManagerCreator(simulation);
        creator.createActivityManagers();

        assertNotNull(rt1.getManager());
        assertNotNull(rt2.getManager());
        assertNotSame(rt1.getManager(), rt2.getManager());
    }

    @Test
    void shouldCreateManagerForFlowWithoutWorkGroup() {
        ResourceType rt1 = new ResourceType(simulation, "RT1");
        RequestResourcesFlow flow = new RequestResourcesFlow(simulation, "Flow1");

        StandardActivityManagerCreator creator = new StandardActivityManagerCreator(simulation);
        creator.createActivityManagers();

        assertNotNull(rt1.getManager());
        assertNotNull(flow.getManager());
        assertNotSame(rt1.getManager(), flow.getManager());
    }
}
