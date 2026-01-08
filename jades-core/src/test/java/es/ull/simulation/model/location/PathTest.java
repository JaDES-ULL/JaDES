package es.ull.simulation.model.location;

import es.ull.simulation.functions.AbstractTimeFunction;
import es.ull.simulation.functions.ConstantFunction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link Path}
 */
class PathTest {

    @Test
    void shouldCreatePath_whenUsingAllParameters() {
        // Given: description, time function, capacity per lane, and number of lanes
        String description = "Highway Path";
        AbstractTimeFunction delayAtExit = new ConstantFunction(10.0);
        int capacityPerLane = 50;
        int nLanes = 4;

        // When: creating path
        Path path = new Path(description, delayAtExit, capacityPerLane, nLanes);

        // Then: path should be created with correct capacity and lanes
        assertNotNull(path);
        assertEquals(200, path.getCapacity()); // 50 * 4
        assertEquals(capacityPerLane, path.getCapacityPerLane());
        assertEquals(nLanes, path.getnLanes());
    }

    @Test
    void shouldCreatePath_whenUsingTimeFunctionOnly() {
        // Given: description and time function (no capacity)
        String description = "Unlimited Highway";
        AbstractTimeFunction delayAtExit = new ConstantFunction(5.0);

        // When: creating path
        Path path = new Path(description, delayAtExit);

        // Then: path should have unlimited capacity and lanes
        assertNotNull(path);
        assertEquals(Integer.MAX_VALUE, path.getCapacity());
        assertEquals(Integer.MAX_VALUE, path.getnLanes());
        assertEquals(Integer.MAX_VALUE, path.getCapacityPerLane());
    }

    @Test
    void shouldCreatePath_whenUsingCapacityOnly() {
        // Given: description, capacity per lane, and number of lanes (no delay)
        String description = "City Path";
        int capacityPerLane = 30;
        int nLanes = 2;

        // When: creating path
        Path path = new Path(description, capacityPerLane, nLanes);

        // Then: path should be created with correct capacity
        assertNotNull(path);
        assertEquals(60, path.getCapacity()); // 30 * 2
        assertEquals(capacityPerLane, path.getCapacityPerLane());
        assertEquals(nLanes, path.getnLanes());
    }

    @Test
    void shouldCreatePath_whenUsingDescriptionOnly() {
        // Given: only description
        String description = "Simple Path";

        // When: creating path
        Path path = new Path(description);

        // Then: path should have unlimited capacity and lanes
        assertNotNull(path);
        assertEquals(Integer.MAX_VALUE, path.getCapacity());
        assertEquals(Integer.MAX_VALUE, path.getnLanes());
        assertEquals(Integer.MAX_VALUE, path.getCapacityPerLane());
    }

    @Test
    void shouldReturnSelfAsLocation_whenGetLocationCalled() {
        // Given: a path
        Path path = new Path("Self Reference Path", 10, 2);

        // When: getting location
        Location location = path.getLocation();

        // Then: should return itself
        assertNotNull(location);
        assertEquals(path, location);
        assertSame(path, location);
    }

    @Test
    void shouldCalculateCorrectCapacity_whenMultipleLanes() {
        // Given: multiple lanes
        Path path1 = new Path("Path 1", 25, 3);
        Path path2 = new Path("Path 2", 100, 5);
        Path path3 = new Path("Path 3", 10, 10);

        // When: getting capacity
        // Then: capacity should be product of lanes and capacity per lane
        assertEquals(75, path1.getCapacity());
        assertEquals(500, path2.getCapacity());
        assertEquals(100, path3.getCapacity());
    }

    @Test
    void shouldHandleSingleLane_whenSpecified() {
        // Given: single lane path
        Path path = new Path("Single Lane", 50, 1);

        // When: checking properties
        // Then: capacity per lane should equal total capacity
        assertEquals(50, path.getCapacity());
        assertEquals(50, path.getCapacityPerLane());
        assertEquals(1, path.getnLanes());
    }

    @Test
    void shouldHaveUniqueIdentifiers_whenMultiplePathsCreated() {
        // Given: multiple paths
        Path path1 = new Path("Path 1");
        Path path2 = new Path("Path 2");
        Path path3 = new Path("Path 3");

        // When: getting identifiers
        int id1 = path1.getIdentifier();
        int id2 = path2.getIdentifier();
        int id3 = path3.getIdentifier();

        // Then: all should have unique IDs
        assertNotEquals(id1, id2);
        assertNotEquals(id2, id3);
        assertNotEquals(id1, id3);
    }

    @Test
    void shouldGetAvailableCapacity_whenQueried() {
        // Given: path with specific capacity
        Path path = new Path("Available Path", 25, 4);

        // When: getting available capacity
        int available = path.getAvailableCapacity();

        // Then: should return full capacity initially (25 * 4 = 100)
        assertEquals(100, available);
    }

    @Test
    void shouldProvideLinkedLocations_whenQueried() {
        // Given: paths and nodes
        Path path = new Path("Main Path", 50, 2);
        Node node1 = new Node("Node 1", 10);
        Node node2 = new Node("Node 2", 10);

        // When: linking locations
        path.linkTo(node1);
        path.linkTo(node2);

        // Then: linkedTo should contain both nodes
        assertTrue(path.getLinkedTo().contains(node1));
        assertTrue(path.getLinkedTo().contains(node2));
        assertTrue(node1.getLinkedFrom().contains(path));
        assertTrue(node2.getLinkedFrom().contains(path));
    }

    @Test
    void shouldHandleLargeNumberOfLanes_whenSpecified() {
        // Given: path with many lanes
        Path path = new Path("Massive Highway", 100, 50);

        // When: checking capacity
        // Then: should handle large capacity
        assertEquals(5000, path.getCapacity());
        assertEquals(50, path.getnLanes());
        assertEquals(100, path.getCapacityPerLane());
    }
}
