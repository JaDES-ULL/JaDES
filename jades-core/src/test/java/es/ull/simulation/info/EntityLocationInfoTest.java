package es.ull.simulation.info;

import es.ull.simulation.model.Simulation;
import es.ull.simulation.model.TimeUnit;
import es.ull.simulation.model.location.Location;
import es.ull.simulation.model.location.IMovable;
import es.ull.simulation.model.location.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link EntityLocationInfo}
 */
class EntityLocationInfoTest {

    private Simulation simulation;
    private Location location;

    @BeforeEach
    void setUp() {
        simulation = new Simulation(1, "Test Simulation", TimeUnit.MINUTE);
        location = new Node("Test Location");
    }

    @Test
    void shouldCreateEntityLocationInfo_whenAllParametersProvided() {
        // Given: entity, location, type, and timestamp
        IMovable entity = new TestMovableEntity("Entity 1");
        EntityLocationInfo.Type type = EntityLocationInfo.Type.ARRIVE;
        long ts = 10L;

        // When: creating EntityLocationInfo
        EntityLocationInfo info = new EntityLocationInfo(simulation, entity, location, type, ts);

        // Then: info should be created with correct values
        assertNotNull(info);
        assertEquals(entity, info.getEntity());
        assertEquals(location, info.getLocation());
        assertEquals(type, info.getType());
        assertEquals(ts, info.getTs());
        assertEquals(simulation, info.getSimul());
    }

    @Test
    void shouldGetEntity_whenQueried() {
        // Given: EntityLocationInfo with entity
        IMovable entity = new TestMovableEntity("Test Entity");
        EntityLocationInfo info = new EntityLocationInfo(simulation, entity, location, EntityLocationInfo.Type.ARRIVE, 0L);

        // When: getting entity
        IMovable result = info.getEntity();

        // Then: should return correct entity
        assertEquals(entity, result);
    }

    @Test
    void shouldGetLocation_whenQueried() {
        // Given: EntityLocationInfo with location
        IMovable entity = new TestMovableEntity("Entity");
        EntityLocationInfo info = new EntityLocationInfo(simulation, entity, location, EntityLocationInfo.Type.ARRIVE, 0L);

        // When: getting location
        Location result = info.getLocation();

        // Then: should return correct location
        assertEquals(location, result);
    }

    @Test
    void shouldGetType_whenQueried() {
        // Given: EntityLocationInfo with type
        IMovable entity = new TestMovableEntity("Entity");
        EntityLocationInfo.Type type = EntityLocationInfo.Type.LEAVE;
        EntityLocationInfo info = new EntityLocationInfo(simulation, entity, location, type, 0L);

        // When: getting type
        EntityLocationInfo.Type result = info.getType();

        // Then: should return correct type
        assertEquals(type, result);
    }

    @Test
    void shouldCreateInfoWithArriveType() {
        // Given: ARRIVE type
        IMovable entity = new TestMovableEntity("Entity");
        EntityLocationInfo info = new EntityLocationInfo(simulation, entity, location, EntityLocationInfo.Type.ARRIVE, 5L);

        // When/Then: type should be ARRIVE
        assertEquals(EntityLocationInfo.Type.ARRIVE, info.getType());
        assertEquals("ARRIVE AT LOCATION", info.getType().getDescription());
    }

    @Test
    void shouldCreateInfoWithLeaveType() {
        // Given: LEAVE type
        IMovable entity = new TestMovableEntity("Entity");
        EntityLocationInfo info = new EntityLocationInfo(simulation, entity, location, EntityLocationInfo.Type.LEAVE, 10L);

        // When/Then: type should be LEAVE
        assertEquals(EntityLocationInfo.Type.LEAVE, info.getType());
        assertEquals("LEAVE FROM LOCATION", info.getType().getDescription());
    }

    @Test
    void shouldCreateInfoWithStartType() {
        // Given: START type
        IMovable entity = new TestMovableEntity("Entity");
        EntityLocationInfo info = new EntityLocationInfo(simulation, entity, location, EntityLocationInfo.Type.START, 15L);

        // When/Then: type should be START
        assertEquals(EntityLocationInfo.Type.START, info.getType());
        assertEquals("START AT LOCATION", info.getType().getDescription());
    }

    @Test
    void shouldCreateInfoWithWaitForType() {
        // Given: WAIT_FOR type
        IMovable entity = new TestMovableEntity("Entity");
        EntityLocationInfo info = new EntityLocationInfo(simulation, entity, location, EntityLocationInfo.Type.WAIT_FOR, 20L);

        // When/Then: type should be WAIT_FOR
        assertEquals(EntityLocationInfo.Type.WAIT_FOR, info.getType());
        assertEquals("WAIT FOR LOCATION", info.getType().getDescription());
    }

    @Test
    void shouldCreateInfoWithCondWaitType() {
        // Given: COND_WAIT type
        IMovable entity = new TestMovableEntity("Entity");
        EntityLocationInfo info = new EntityLocationInfo(simulation, entity, location, EntityLocationInfo.Type.COND_WAIT, 25L);

        // When/Then: type should be COND_WAIT
        assertEquals(EntityLocationInfo.Type.COND_WAIT, info.getType());
        assertEquals("CONDITIONAL WAIT", info.getType().getDescription());
    }

    @Test
    void shouldGenerateToString_whenCalled() {
        // Given: EntityLocationInfo
        IMovable entity = new TestMovableEntity("Entity");
        EntityLocationInfo info = new EntityLocationInfo(simulation, entity, location, EntityLocationInfo.Type.ARRIVE, 30L);

        // When: calling toString
        String result = info.toString();

        // Then: should contain information
        assertNotNull(result);
        assertTrue(result.contains("ARRIVE AT LOCATION"));
    }

    @Test
    void shouldCreateInfoWithDifferentTimestamps() {
        // Given: different timestamps
        IMovable entity = new TestMovableEntity("Entity");
        EntityLocationInfo info1 = new EntityLocationInfo(simulation, entity, location, EntityLocationInfo.Type.ARRIVE, 0L);
        EntityLocationInfo info2 = new EntityLocationInfo(simulation, entity, location, EntityLocationInfo.Type.LEAVE, 100L);
        EntityLocationInfo info3 = new EntityLocationInfo(simulation, entity, location, EntityLocationInfo.Type.START, 500L);

        // When/Then: timestamps should be different
        assertEquals(0L, info1.getTs());
        assertEquals(100L, info2.getTs());
        assertEquals(500L, info3.getTs());
    }

    @Test
    void shouldCreateInfoWithDifferentLocations() {
        // Given: different locations
        IMovable entity = new TestMovableEntity("Entity");
        Location location1 = new Node("Location 1");
        Location location2 = new Node("Location 2");
        EntityLocationInfo info1 = new EntityLocationInfo(simulation, entity, location1, EntityLocationInfo.Type.ARRIVE, 0L);
        EntityLocationInfo info2 = new EntityLocationInfo(simulation, entity, location2, EntityLocationInfo.Type.LEAVE, 0L);

        // When/Then: locations should be different
        assertEquals(location1, info1.getLocation());
        assertEquals(location2, info2.getLocation());
    }

    @Test
    void shouldHaveAllTypeEnumValues() {
        // Given: Type enum
        EntityLocationInfo.Type[] types = EntityLocationInfo.Type.values();

        // When/Then: should have 5 types
        assertEquals(5, types.length);
        assertEquals(EntityLocationInfo.Type.ARRIVE, types[0]);
        assertEquals(EntityLocationInfo.Type.LEAVE, types[1]);
        assertEquals(EntityLocationInfo.Type.START, types[2]);
        assertEquals(EntityLocationInfo.Type.WAIT_FOR, types[3]);
        assertEquals(EntityLocationInfo.Type.COND_WAIT, types[4]);
    }

    @Test
    void shouldGetTypeByName() {
        // Given: type name
        String typeName = "ARRIVE";

        // When: getting type by name
        EntityLocationInfo.Type type = EntityLocationInfo.Type.valueOf(typeName);

        // Then: should return correct type
        assertEquals(EntityLocationInfo.Type.ARRIVE, type);
    }

    // Helper class for testing
    private static class TestMovableEntity implements IMovable {
        private final String name;
        private Location location;

        public TestMovableEntity(String name) {
            this.name = name;
        }

        @Override
        public Location getLocation() {
            return location;
        }

        @Override
        public void setLocation(Location location) {
            this.location = location;
        }

        @Override
        public void notifyLocationAvailable(Location location) {
            // Implementation not needed for tests
        }

        @Override
        public int getCapacity() {
            return 1;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
