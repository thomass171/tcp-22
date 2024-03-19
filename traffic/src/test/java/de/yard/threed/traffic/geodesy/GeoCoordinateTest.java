package de.yard.threed.traffic.geodesy;

import de.yard.threed.core.Degree;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import de.yard.threed.traffic.testutils.TrafficTestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 */
public class GeoCoordinateTest {

    static Platform platform = EngineTestFactory.initPlatformForTest(new String[]{"engine"}, new SimpleHeadlessPlatformFactory());

    @Test
    public void testWithElevation() throws Exception {
        GeoCoordinate geoCoordinate = new GeoCoordinate(new Degree(50.843675), new Degree(7.109709), 1150);
        String s = geoCoordinate.toString();
        assertEquals("50.843675,7.109709,1150.0", s);
        TrafficTestUtils.assertGeoCoordinate(new GeoCoordinate(new Degree(50.843675), new Degree(7.109709), 1150), GeoCoordinate.parse(s), "");
    }

    @Test
    public void testWithoutElevation() throws Exception {
        GeoCoordinate geoCoordinate = new GeoCoordinate(new Degree(50.843675), new Degree(7.109709));
        String s = geoCoordinate.toString();
        assertEquals("50.843675,7.109709", s);
        TrafficTestUtils.assertGeoCoordinate(new GeoCoordinate(new Degree(50.843675), new Degree(7.109709)), GeoCoordinate.parse(s), "");
    }
}

