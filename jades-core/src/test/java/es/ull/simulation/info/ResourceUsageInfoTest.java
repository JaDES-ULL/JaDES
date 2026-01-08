package es.ull.simulation.info;

import es.ull.simulation.info.ResourceUsageInfo.Type;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ResourceUsageInfo}
 */
class ResourceUsageInfoTest {

    @Test
    void shouldGetCaughtTypeDescription() {
        // Given: CAUGHT type
        ResourceUsageInfo.Type type = ResourceUsageInfo.Type.CAUGHT;

        // When: getting description
        String description = type.getDescription();

        // Then: should return correct description
        assertEquals("CAUGHT RESOURCE", description);
    }

    @Test
    void shouldGetReleasedTypeDescription() {
        // Given: RELEASED type
        ResourceUsageInfo.Type type = ResourceUsageInfo.Type.RELEASED;

        // When: getting description
        String description = type.getDescription();

        // Then: should return correct description
        assertEquals("RELEASED RESOURCE", description);
    }

    @Test
    void shouldHaveAllTypeEnumValues() {
        // Given: Type enum
        ResourceUsageInfo.Type[] types = ResourceUsageInfo.Type.values();

        // When/Then: should have 2 types
        assertEquals(2, types.length);
        assertEquals(ResourceUsageInfo.Type.CAUGHT, types[0]);
        assertEquals(ResourceUsageInfo.Type.RELEASED, types[1]);
    }

    @Test
    void shouldGetTypeByName() {
        // Given: type name
        String typeName = "CAUGHT";

        // When: getting type by name
        ResourceUsageInfo.Type type = ResourceUsageInfo.Type.valueOf(typeName);

        // Then: should return correct type
        assertEquals(ResourceUsageInfo.Type.CAUGHT, type);
    }

    @Test
    void shouldGetReleasedTypeByName() {
        // Given: type name
        String typeName = "RELEASED";

        // When: getting type by name
        ResourceUsageInfo.Type type = ResourceUsageInfo.Type.valueOf(typeName);

        // Then: should return correct type
        assertEquals(ResourceUsageInfo.Type.RELEASED, type);
    }

    @Test
    void shouldImplementIInfoType() {
        // Given: Type enum values
        Type caught = ResourceUsageInfo.Type.CAUGHT;
        Type released = ResourceUsageInfo.Type.RELEASED;

        // When/Then: should implement IPieceOfInformation.IInfoType
        assertTrue(caught instanceof IPieceOfInformation.IInfoType);
        assertTrue(released instanceof IPieceOfInformation.IInfoType);
    }

    @Test
    void shouldHaveDifferentDescriptions() {
        // Given: both types
        String caughtDesc = ResourceUsageInfo.Type.CAUGHT.getDescription();
        String releasedDesc = ResourceUsageInfo.Type.RELEASED.getDescription();

        // When/Then: descriptions should be different
        assertNotEquals(caughtDesc, releasedDesc);
    }

    @Test
    void shouldNotBeNull_whenGettingDescription() {
        // Given: types
        // When/Then: descriptions should not be null
        assertNotNull(ResourceUsageInfo.Type.CAUGHT.getDescription());
        assertNotNull(ResourceUsageInfo.Type.RELEASED.getDescription());
    }

    @Test
    void shouldBeEnum() {
        // Given: Type class
        // When/Then: should be an enum
        assertTrue(ResourceUsageInfo.Type.class.isEnum());
    }

    @Test
    void shouldHaveCorrectEnumName() {
        // Given: enum values
        // When: getting name
        String caughtName = ResourceUsageInfo.Type.CAUGHT.name();
        String releasedName = ResourceUsageInfo.Type.RELEASED.name();

        // Then: names should match
        assertEquals("CAUGHT", caughtName);
        assertEquals("RELEASED", releasedName);
    }
}
