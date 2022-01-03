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
import static org.junit.Assert.fail;

public class TrafficTestUtils {



    /**
     * Setup scene like it isType done in main and render "initialFrames" frames.
     *
     */
    public static SceneRunnerForTesting setupForScene(int initialFrames, HashMap<String, String> properties) throws Exception {

        TestFactory.initPlatformForTest( new String[] {"engine","data","traffic"}, new SimpleHeadlessPlatformFactory(new SimpleEventBusForTesting()),properties);

        SceneRunnerForTesting sceneRunner =  (SceneRunnerForTesting) SceneRunnerForTesting.getInstance();
        sceneRunner.runLimitedFrames(initialFrames);
        return sceneRunner;
    }

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
