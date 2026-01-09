package es.ull.simulation.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the ResourceAvailability class - extracted from Resource refactoring
 */
class ResourceAvailabilityTest {
    private Simulation simulation;
    private Resource resource;
    private ResourceType testRole;
    private static final int SIMULATION_ID = 1;
    private static final String SIMULATION_DESC = "Test Simulation";

    @BeforeEach
    void setUp() {
        simulation = new Simulation(SIMULATION_ID, SIMULATION_DESC);
        resource = new Resource(simulation, "Test Resource");
        testRole = new ResourceType(simulation, "Test Role");
    }

    @Test
    void shouldInitializeWithEmptyTimeTables() {
        // When: getting timetable entries from new resource
        int timeTableSize = resource.getTimeTableEntries().size();
        int cancelTableSize = resource.getCancellationPeriodEntries().size();

        // Then: both should be empty
        assertEquals(0, timeTableSize);
        assertEquals(0, cancelTableSize);
    }

    @Test
    void shouldStartWithNullCurrentResourceType() {
        // When: getting current resource type from new resource
        ResourceType currentType = resource.getCurrentResourceType();

        // Then: should be null
        assertNull(currentType);
    }

    @Test
    void shouldSetAndGetCurrentResourceType() {
        // When: setting resource type
        resource.setCurrentResourceType(testRole);

        // Then: should return the same resource type
        assertEquals(testRole, resource.getCurrentResourceType());
    }

    @Test
    void shouldStartWithTimeOutFalse() {
        // When: checking timeout on new resource
        boolean isTimeOut = resource.isTimeOut();

        // Then: should be false
        assertFalse(isTimeOut);
    }

    @Test
    void shouldSetAndGetTimeOut() {
        // When: setting timeout to true
        resource.setTimeOut(true);

        // Then: should return true
        assertTrue(resource.isTimeOut());

        // When: setting back to false
        resource.setTimeOut(false);

        // Then: should return false
        assertFalse(resource.isTimeOut());
    }

    @Test
    void shouldCreateTimeTableOrCancelEntriesAdderWithSingleRole() {
        // When: creating adder with single role
        ResourceAvailability.TimeTableOrCancelEntriesAdder adder =
                resource.newTimeTableOrCancelEntriesAdder(testRole);

        // Then: adder should not be null
        assertNotNull(adder);
    }

    @Test
    void shouldCreateTimeTableOrCancelEntriesAdderWithMultipleRoles() {
        // Given: multiple roles
        ResourceType role1 = new ResourceType(simulation, "Role 1");
        ResourceType role2 = new ResourceType(simulation, "Role 2");
        ArrayList<ResourceType> roles = new ArrayList<>();
        roles.add(role1);
        roles.add(role2);

        // When: creating adder with role list
        ResourceAvailability.TimeTableOrCancelEntriesAdder adder =
                resource.newTimeTableOrCancelEntriesAdder(roles);

        // Then: adder should not be null
        assertNotNull(adder);
    }

    @Test
    void shouldAddTimeTableEntry() {
        // Given: time table entry adder
        ResourceAvailability.TimeTableOrCancelEntriesAdder adder =
                resource.newTimeTableOrCancelEntriesAdder(testRole);

        // When: adding time table entry
        adder.addTimeTableEntry();

        // Then: time table should have one entry
        assertEquals(1, resource.getTimeTableEntries().size());
    }

    @Test
    void shouldAddCancelEntry() {
        // Given: time table entry adder
        ResourceAvailability.TimeTableOrCancelEntriesAdder adder =
                resource.newTimeTableOrCancelEntriesAdder(testRole);

        // When: adding cancel entry
        adder.addCancelEntry();

        // Then: cancel table should have one entry
        assertEquals(1, resource.getCancellationPeriodEntries().size());
    }

    @Test
    void shouldAddMultipleTimeTableEntries() {
        // Given: adder for multiple roles
        ResourceType role1 = new ResourceType(simulation, "Role 1");
        ResourceType role2 = new ResourceType(simulation, "Role 2");
        ArrayList<ResourceType> roles = new ArrayList<>();
        roles.add(role1);
        roles.add(role2);
        ResourceAvailability.TimeTableOrCancelEntriesAdder adder =
                resource.newTimeTableOrCancelEntriesAdder(roles);

        // When: adding time table entry
        adder.addTimeTableEntry();

        // Then: time table should have two entries (one per role)
        assertEquals(2, resource.getTimeTableEntries().size());
    }

    @Test
    void shouldClearCurrentResourceTypeBySettingNull() {
        // Given: resource with current type set
        resource.setCurrentResourceType(testRole);

        // When: setting to null
        resource.setCurrentResourceType(null);

        // Then: should be null
        assertNull(resource.getCurrentResourceType());
    }
}
