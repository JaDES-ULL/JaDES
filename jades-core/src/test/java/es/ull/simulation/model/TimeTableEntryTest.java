package es.ull.simulation.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.ull.simulation.functions.ConstantFunction;

class TimeTableEntryTest {
    private Simulation simulation;
    private ResourceType resourceType;
    private ISimulationCycle cycle;
    private TimeStamp duration;

    @BeforeEach
    void setUp() {
        simulation = new Simulation(1, "Test Simulation");
        resourceType = new ResourceType(simulation, "Test Resource Type");
        cycle = new SimulationPeriodicCycle(
            TimeUnit.MINUTE,
            new TimeStamp(TimeUnit.MINUTE, 0),
            new ConstantFunction(5),
            new TimeStamp(TimeUnit.MINUTE, 100)
        );
        duration = new TimeStamp(TimeUnit.MINUTE, 10);
    }

    @Test
    void shouldCreateEntryWithCycleAndDuration() {
        TimeTableEntry entry = new TimeTableEntry(cycle, duration, resourceType);

        assertNotNull(entry);
        assertEquals(cycle, entry.getCycle());
        assertEquals(duration, entry.getDuration());
        assertEquals(resourceType, entry.getRole());
    }

    @Test
    void shouldCreatePermanentEntry() {
        TimeTableEntry entry = new TimeTableEntry(resourceType);

        assertNotNull(entry);
        assertNull(entry.getCycle());
        assertNull(entry.getDuration());
        assertEquals(resourceType, entry.getRole());
    }

    @Test
    void shouldReturnTrueForIsPermanent_whenCreatedAsPermanent() {
        TimeTableEntry entry = new TimeTableEntry(resourceType);

        assertTrue(entry.isPermanent());
    }

    @Test
    void shouldReturnFalseForIsPermanent_whenCreatedWithCycle() {
        TimeTableEntry entry = new TimeTableEntry(cycle, duration, resourceType);

        assertFalse(entry.isPermanent());
    }

    @Test
    void shouldReturnCorrectDuration_whenCreatedWithDuration() {
        TimeStamp customDuration = new TimeStamp(TimeUnit.HOUR, 2);
        TimeTableEntry entry = new TimeTableEntry(cycle, customDuration, resourceType);

        assertEquals(customDuration, entry.getDuration());
        assertEquals(2, entry.getDuration().getValue());
    }

    @Test
    void shouldReturnCorrectCycle_whenCreatedWithCycle() {
        TimeTableEntry entry = new TimeTableEntry(cycle, duration, resourceType);

        assertEquals(cycle, entry.getCycle());
    }

    @Test
    void shouldReturnCorrectRole_whenCreatedWithRole() {
        TimeTableEntry entry = new TimeTableEntry(cycle, duration, resourceType);

        assertEquals(resourceType, entry.getRole());
        assertEquals("Test Resource Type", entry.getRole().getDescription());
    }

    @Test
    void shouldReturnCorrectRoleForPermanentEntry() {
        TimeTableEntry entry = new TimeTableEntry(resourceType);

        assertEquals(resourceType, entry.getRole());
    }

    @Test
    void shouldGenerateToString_whenCreatedWithCycle() {
        TimeTableEntry entry = new TimeTableEntry(cycle, duration, resourceType);

        String result = entry.toString();

        assertNotNull(result);
        assertTrue(result.contains("Test Resource Type"));
        assertTrue(result.contains("10"));
    }

    @Test
    void shouldHandleZeroDuration() {
        TimeStamp zeroDuration = new TimeStamp(TimeUnit.MINUTE, 0);
        TimeTableEntry entry = new TimeTableEntry(cycle, zeroDuration, resourceType);

        assertEquals(zeroDuration, entry.getDuration());
        assertEquals(0, entry.getDuration().getValue());
    }

    @Test
    void shouldHandleLargeDuration() {
        TimeStamp largeDuration = new TimeStamp(TimeUnit.DAY, 365);
        TimeTableEntry entry = new TimeTableEntry(cycle, largeDuration, resourceType);

        assertEquals(largeDuration, entry.getDuration());
        assertEquals(365, entry.getDuration().getValue());
    }

    @Test
    void shouldCreateMultipleEntriesWithSameRole() {
        ISimulationCycle cycle2 = new SimulationPeriodicCycle(
            TimeUnit.HOUR,
            new TimeStamp(TimeUnit.HOUR, 0),
            new ConstantFunction(2),
            new TimeStamp(TimeUnit.HOUR, 50)
        );
        TimeStamp duration2 = new TimeStamp(TimeUnit.HOUR, 1);

        TimeTableEntry entry1 = new TimeTableEntry(cycle, duration, resourceType);
        TimeTableEntry entry2 = new TimeTableEntry(cycle2, duration2, resourceType);

        assertEquals(resourceType, entry1.getRole());
        assertEquals(resourceType, entry2.getRole());
        assertFalse(entry1.isPermanent());
        assertFalse(entry2.isPermanent());
    }

    @Test
    void shouldCreateMultipleEntriesWithDifferentRoles() {
        ResourceType resourceType2 = new ResourceType(simulation, "Second Type");
        
        TimeTableEntry entry1 = new TimeTableEntry(cycle, duration, resourceType);
        TimeTableEntry entry2 = new TimeTableEntry(cycle, duration, resourceType2);

        assertEquals(resourceType, entry1.getRole());
        assertEquals(resourceType2, entry2.getRole());
        assertEquals("Test Resource Type", entry1.getRole().getDescription());
        assertEquals("Second Type", entry2.getRole().getDescription());
    }

    @Test
    void shouldMaintainStateAfterCreation() {
        TimeTableEntry entry = new TimeTableEntry(cycle, duration, resourceType);

        // Verificar que los valores no cambian
        assertEquals(cycle, entry.getCycle());
        assertEquals(duration, entry.getDuration());
        assertEquals(resourceType, entry.getRole());
        assertFalse(entry.isPermanent());
        
        // Llamar múltiples veces no debe cambiar el estado
        entry.getCycle();
        entry.getDuration();
        entry.getRole();
        
        assertEquals(cycle, entry.getCycle());
        assertEquals(duration, entry.getDuration());
        assertEquals(resourceType, entry.getRole());
    }
}
