package es.ull.simulation.model.location;

import es.ull.simulation.functions.AbstractTimeFunction;
import es.ull.simulation.functions.ConstantFunction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link Node}
 */
class NodeTest {

    @Test
    void shouldCreateNode_whenUsingAbstractTimeFunctionAndCapacity() {
        // Given: description, time function, and capacity
        String description = "Test Node";
        AbstractTimeFunction delayAtExit = new ConstantFunction(5.0);
        int capacity = 10;

        // When: creating node
        Node node = new Node(description, delayAtExit, capacity);

        // Then: node should be created
        assertNotNull(node);
        assertEquals(capacity, node.getCapacity());
    }

    @Test
    void shouldCreateNode_whenUsingLongAndCapacity() {
        // Given: description, long delay, and capacity
        String description = "Long Delay Node";
        long delayAtExit = 10L;
        int capacity = 20;

        // When: creating node
        Node node = new Node(description, delayAtExit, capacity);

        // Then: node should be created
        assertNotNull(node);
        assertEquals(capacity, node.getCapacity());
    }

    @Test
    void shouldCreateNode_whenUsingAbstractTimeFunctionWithoutCapacity() {
        // Given: description and time function (no capacity)
        String description = "Unlimited Node";
        AbstractTimeFunction delayAtExit = new ConstantFunction(3.0);

        // When: creating node
        Node node = new Node(description, delayAtExit);

        // Then: node should have maximum capacity
        assertNotNull(node);
        assertEquals(Integer.MAX_VALUE, node.getCapacity());
    }

    @Test
    void shouldCreateNode_whenUsingLongWithoutCapacity() {
        // Given: description and long delay (no capacity)
        String description = "Long Unlimited Node";
        long delayAtExit = 5L;

        // When: creating node
        Node node = new Node(description, delayAtExit);

        // Then: node should have maximum capacity
        assertNotNull(node);
        assertEquals(Integer.MAX_VALUE, node.getCapacity());
    }

    @Test
    void shouldCreateNode_whenUsingOnlyCapacity() {
        // Given: description and capacity (no delay)
        String description = "No Delay Node";
        int capacity = 15;

        // When: creating node
        Node node = new Node(description, capacity);

        // Then: node should be created
        assertNotNull(node);
        assertEquals(capacity, node.getCapacity());
    }

    @Test
    void shouldCreateNode_whenUsingOnlyDescription() {
        // Given: only description (no delay, no capacity)
        String description = "Minimal Node";

        // When: creating node
        Node node = new Node(description);

        // Then: node should have maximum capacity and no delay
        assertNotNull(node);
        assertEquals(Integer.MAX_VALUE, node.getCapacity());
    }

    @Test
    void shouldReturnSelfAsLocation_whenGetLocationCalled() {
        // Given: a node
        Node node = new Node("Self Reference Node", 10);

        // When: getting location
        Location location = node.getLocation();

        // Then: should return itself
        assertNotNull(location);
        assertEquals(node, location);
        assertSame(node, location);
    }

    @Test
    void shouldHaveUniqueIdentifiers_whenMultipleNodesCreated() {
        // Given: multiple nodes
        Node node1 = new Node("Node 1");
        Node node2 = new Node("Node 2");
        Node node3 = new Node("Node 3");

        // When: getting identifiers
        int id1 = node1.getIdentifier();
        int id2 = node2.getIdentifier();
        int id3 = node3.getIdentifier();

        // Then: all should have unique IDs
        assertNotEquals(id1, id2);
        assertNotEquals(id2, id3);
        assertNotEquals(id1, id3);
    }

    @Test
    void shouldHandleZeroCapacity_whenSpecified() {
        // Given: node with zero capacity
        String description = "Zero Capacity Node";
        int capacity = 0;

        // When: creating node
        Node node = new Node(description, capacity);

        // Then: should accept zero capacity
        assertNotNull(node);
        assertEquals(0, node.getCapacity());
    }

    @Test
    void shouldHandleNegativeDelay_whenSpecified() {
        // Given: negative delay
        String description = "Negative Delay Node";
        long delay = -5L;

        // When: creating node
        Node node = new Node(description, delay, 10);

        // Then: node should be created (behavior depends on implementation)
        assertNotNull(node);
    }

    @Test
    void shouldHandleLargeCapacity_whenSpecified() {
        // Given: very large capacity
        String description = "Large Capacity Node";
        int capacity = 1_000_000;

        // When: creating node
        Node node = new Node(description, capacity);

        // Then: should handle large capacity
        assertNotNull(node);
        assertEquals(capacity, node.getCapacity());
    }

    @Test
    void shouldGetAvailableCapacity_whenQueried() {
        // Given: node with specific capacity
        Node node = new Node("Available Capacity Node", 100);

        // When: getting available capacity
        int available = node.getAvailableCapacity();

        // Then: should return full capacity initially
        assertEquals(100, available);
    }

    @Test
    void shouldProvideLinkedLocations_whenQueried() {
        // Given: nodes
        Node node1 = new Node("Node 1", 10);
        Node node2 = new Node("Node 2", 10);

        // When: linking nodes
        node1.linkTo(node2);

        // Then: linkedTo should contain node2
        assertTrue(node1.getLinkedTo().contains(node2));
        assertTrue(node2.getLinkedFrom().contains(node1));
    }
}

