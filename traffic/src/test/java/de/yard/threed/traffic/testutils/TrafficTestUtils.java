package de.yard.threed.traffic.testutils;

import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.Util;
import de.yard.threed.core.platform.NativeNode;
import de.yard.threed.engine.ViewPoint;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import de.yard.threed.engine.util.XmlHelper;
import de.yard.threed.traffic.config.ConfigHelper;
import de.yard.threed.traffic.geodesy.GeoCoordinate;
import org.junit.jupiter.api.Assertions;

import static de.yard.threed.core.testutil.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class TrafficTestUtils {


    public static void runAdditionalFrames(SceneRunnerForTesting sceneRunner, int frames) {
        Util.notyet();
        /*sceneRunner.frameLimit = frames;
        sceneRunner.startRenderloop();*/
    }

    public static void assertGeoCoordinate(GeoCoordinate expected, GeoCoordinate actual, String label) {
        assertEquals(expected.getLatDeg().getDegree(), actual.getLatDeg().getDegree(),0.000001, "LatitudeDeg");
        assertEquals(expected.getLonDeg().getDegree(), actual.getLonDeg().getDegree(),0.000001, "LongitudeDeg");
    }


}
