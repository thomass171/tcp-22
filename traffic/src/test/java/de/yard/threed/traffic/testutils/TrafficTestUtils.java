package de.yard.threed.traffic.testutils;

import de.yard.threed.core.Util;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import de.yard.threed.traffic.geodesy.GeoCoordinate;
import org.junit.jupiter.api.Assertions;


public class TrafficTestUtils {



    public static void runAdditionalFrames(SceneRunnerForTesting sceneRunner, int frames) {
        Util.notyet();
        /*sceneRunner.frameLimit = frames;
        sceneRunner.startRenderloop();*/
    }

    public static void assertGeoCoordinate(String label, GeoCoordinate expected, GeoCoordinate actual) {
        Assertions.assertEquals( (float) expected.getLatDeg().getDegree(), (float) actual.getLatDeg().getDegree(),"LatitudeDeg");
        Assertions.assertEquals( (float) expected.getLonDeg().getDegree(), (float) actual.getLonDeg().getDegree(),"LongitudeDeg");
    }
}
