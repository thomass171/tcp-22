package de.yard.threed.engine.testutil;

import de.yard.threed.core.LocalTransform;
import de.yard.threed.engine.ViewPoint;

import static de.yard.threed.core.testutil.TestUtils.assertTransform;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestUtils {

    public static void assertViewPoint(String expectedName, LocalTransform expected, ViewPoint actual){
        assertEquals(expectedName, actual.name);
        assertTransform(expected, actual.transform);
    }
}
