package de.yard.threed.traffic;

import de.yard.threed.core.Degree;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import de.yard.threed.core.GeoCoordinate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


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

        GeoRoute r = GeoRoute.parse(GeoRoute.SAMPLE_EDKB_EDDK);
        assertEquals(1, r.waypointsBeforeTakeoff.size());
        assertNotNull(r.takeoff);
        assertEquals(3, r.waypointsInFlight.size());
        assertNotNull(r.touchdown);
        assertEquals(1, r.waypointsAfterTouchdown.size());
    }

    @Test
    public void testTrivial() throws Exception {
        GeoRoute route = GeoRoute.parse("wp:50.768,7.1672000->wp:50.8662999,7.1443999");
        assertNotNull(route);
        assertEquals(2, route.waypointsBeforeTakeoff.size());
        assertNull(route.takeoff);
    }
}

