package de.yard.threed.traffic.testutils;

import de.yard.threed.core.Packet;
import de.yard.threed.core.Pair;
import de.yard.threed.core.Util;
import de.yard.threed.core.testutil.SimpleEventBusForTesting;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import de.yard.threed.engine.testutil.TestFactory;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import de.yard.threed.traffic.geodesy.GeoCoordinate;


import java.util.HashMap;
import java.util.List;

import static de.yard.threed.javanative.JavaUtil.sleepMs;


public class TrafficTestUtils {



    public static void runAdditionalFrames(SceneRunnerForTesting sceneRunner, int frames) {
        Util.notyet();
        /*sceneRunner.frameLimit = frames;
        sceneRunner.startRenderloop();*/
    }

    public static void assertGeoCoordinate(String label, GeoCoordinate expected, GeoCoordinate actual) {
        de.yard.threed.core.testutil.TestUtil.assertEquals("LatitudeDeg", (float) expected.getLatDeg().getDegree(), (float) actual.getLatDeg().getDegree());
        de.yard.threed.core.testutil.TestUtil.assertEquals("LongitudeDeg", (float) expected.getLonDeg().getDegree(), (float) actual.getLonDeg().getDegree());
    }
}
