package de.yard.threed.traffic;

import de.yard.threed.core.Degree;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import de.yard.threed.traffic.geodesy.GeoCoordinate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 *
 */
public class GeoRouteTest {

    static Platform platform = EngineTestFactory.initPlatformForTest(new String[]{"engine", "traffic"}, new SimpleHeadlessPlatformFactory());

    @Test
    public void testEDKB_EDDK() throws Exception {
        GeoRoute geoRoute = new GeoRoute(
                new GeoCoordinate(new Degree(50.7680), new Degree(7.1672)),
                new GeoCoordinate(new Degree(50.7692), new Degree(7.1617)),
                new GeoCoordinate(new Degree(50.7704), new Degree(7.1557)));

        geoRoute.addWaypoint(new GeoCoordinate(new Degree(50.8176), new Degree(7.0999)));

        geoRoute.addLanding(
                // might not be exact on heading of runway
                new GeoCoordinate(new Degree(50.8519), new Degree(7.0921)),
                new GeoCoordinate(new Degree(50.8625), new Degree(7.1317)),
                new GeoCoordinate(new Degree(50.8663), new Degree(7.1444)));

        assertEquals(GeoRoute.SAMPLE_EDKB_EDDK, geoRoute.toString());
    }
}

