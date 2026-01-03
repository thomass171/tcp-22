package de.yard.threed.trafficcore;

import de.yard.threed.core.Degree;
import de.yard.threed.core.ParseException;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.GeoCoordinate;
import de.yard.threed.core.testutil.CoreTestFactory;
import de.yard.threed.core.testutil.PlatformFactoryTestingCore;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class GeoRouteTest {

    static Platform platform = CoreTestFactory.initPlatformForTest(new PlatformFactoryTestingCore(), null);

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

    @Test
    public void testWithNegative() throws Exception {
        GeoRoute route = GeoRoute.parse("wp:50.768,-7.1672000->wp:50.8662999,7.1443999");
        assertNotNull(route);
        assertEquals(2, route.waypointsBeforeTakeoff.size());
        assertNull(route.takeoff);
    }

    @Test
    public void testBrokenRoute() throws Exception {
        // From NumberFormatException
        assertThrows(ParseException.class,()->GeoRoute.parse("wp:55.9442996,-3.3892534->takeoff:55.9467891,-3.3819122->wp:55.9758200,-3.3507778->wp:55.8461119,-4.4814731->touchdown:55.8647208,...->wp:55.8799744,-4.4182359"));

        // From failed split
        assertThrows(ParseException.class,()->GeoRoute.parse("wp:55.9442996,-3.3892534->takeoff:55.9467891,-3.3819122->wp:55.97->wp:55.8461119,-4.4814731->touchdown:55.8647208,...->wp:55.8799744,-4.4182359"));

        // From failed waypoint type
        assertThrows(ParseException.class,()->GeoRoute.parse("wp:55.9442996,-3.3892534->xxx:55.9467891,-3.3819122->wp:55.97->wp:55.8461119,-4.4814731->touchdown:55.8647208,...->wp:55.8799744,-4.4182359"));

    }
}

