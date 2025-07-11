package de.yard.threed.trafficcore;

import de.yard.threed.core.GeoCoordinate;
import de.yard.threed.core.LatLon;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.CoreTestFactory;
import de.yard.threed.core.testutil.PlatformFactoryTestingCore;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 */
@Slf4j
public class GeoRouteBuilderTest {

    static Platform platform = CoreTestFactory.initPlatformForTest(new PlatformFactoryTestingCore(), null);

    @Test
    public void testEDKB29_EDDK06() {
        GeoCoordinate edkb29From = GeoCoordinate.fromLatLon(LatLon.fromDegrees(50.7680, 7.1672));
        GeoCoordinate edkb29To = GeoCoordinate.fromLatLon(LatLon.fromDegrees(50.7692, 7.1617));
        GeoCoordinate eddk06From = GeoCoordinate.fromLatLon(LatLon.fromDegrees(50.8625, 7.1317));
        GeoCoordinate eddk06To = GeoCoordinate.fromLatLon(LatLon.fromDegrees(50.8663, 7.1444));

        GeoRouteBuilder geoRouteBuilder = new GeoRouteBuilder(new SimpleEllipsoidCalculations(SimpleEllipsoidCalculations.eQuatorialEarthRadius));
        GeoRoute geoRoute = geoRouteBuilder.buildAirportToAirportRoute(edkb29From, edkb29To, eddk06From, eddk06To);

        log.debug("geoRoute={}", geoRoute);
        assertEquals("wp:50.7681904,7.1663272->takeoff:50.7697576,7.1591436->wp:50.7785403,7.1188541->wp:50.7841839,7.1078005->wp:50.7918687,7.1004420->wp:50.8006778,7.0976580->wp:50.8272422,7.0860276->wp:50.8360513,7.0832420->wp:50.8447672,7.0866863->wp:50.8517169,7.0957016->touchdown:50.8632697,7.1342719->wp:50.8662999,7.1443999", geoRoute.toString());
    }

    @Test
    public void testEDKB11_EDDK06() {
        GeoCoordinate edkb11From = GeoCoordinate.fromLatLon(LatLon.fromDegrees(50.7692, 7.1617));
        GeoCoordinate edkb11To = GeoCoordinate.fromLatLon(LatLon.fromDegrees(50.7680, 7.1672));
        GeoCoordinate eddk06From = GeoCoordinate.fromLatLon(LatLon.fromDegrees(50.8625, 7.1317));
        GeoCoordinate eddk06To = GeoCoordinate.fromLatLon(LatLon.fromDegrees(50.8663, 7.1444));

        GeoRouteBuilder geoRouteBuilder = new GeoRouteBuilder(new SimpleEllipsoidCalculations(SimpleEllipsoidCalculations.eQuatorialEarthRadius));
        GeoRoute geoRoute = geoRouteBuilder.buildAirportToAirportRoute(edkb11From, edkb11To, eddk06From, eddk06To);

        log.debug("geoRoute={}", geoRoute);
        assertEquals("wp:50.7690095,7.1625727->takeoff:50.7674421,7.1697561->wp:50.7586475,7.2100295->wp:50.7631255,7.2223405->wp:50.7719389,7.2250893->wp:50.7790387,7.2163862->wp:50.8279106,7.0980980->wp:50.8350111,7.0893857->wp:50.8439761,7.0884822->wp:50.8517169,7.0957016->touchdown:50.8632697,7.1342719->wp:50.8662999,7.1443999", geoRoute.toString());
    }

    @Test
    public void testEDKB29_EDDK24() {
        GeoCoordinate edkb29From = GeoCoordinate.fromLatLon(LatLon.fromDegrees(50.7680, 7.1672));
        GeoCoordinate edkb29To = GeoCoordinate.fromLatLon(LatLon.fromDegrees(50.7692, 7.1617));
        GeoCoordinate eddk24To = GeoCoordinate.fromLatLon(LatLon.fromDegrees(50.8625, 7.1317));
        GeoCoordinate eddk24From = GeoCoordinate.fromLatLon(LatLon.fromDegrees(50.8663, 7.1444));

        GeoRouteBuilder geoRouteBuilder = new GeoRouteBuilder(new SimpleEllipsoidCalculations(SimpleEllipsoidCalculations.eQuatorialEarthRadius));
        GeoRoute geoRoute = geoRouteBuilder.buildAirportToAirportRoute(edkb29From, edkb29To, eddk24From, eddk24To);

        log.debug("geoRoute={}", geoRoute);
        assertEquals("wp:50.7681904,7.1663272->takeoff:50.7697576,7.1591436->wp:50.7785403,7.1188541->wp:50.7854075,7.1096948->wp:50.7942723,7.1073955->wp:50.8026293,7.1126086->wp:50.8573971,7.1943713->wp:50.8657543,7.1995905->wp:50.8739540,7.1937766->wp:50.8770663,7.1804221->touchdown:50.8655304,7.1418276->wp:50.8625,7.1317000", geoRoute.toString());
    }

    @Test
    public void testEDKB11_EDDK24() {
        GeoCoordinate edkb11From = GeoCoordinate.fromLatLon(LatLon.fromDegrees(50.7692, 7.1617));
        GeoCoordinate edkb11To = GeoCoordinate.fromLatLon(LatLon.fromDegrees(50.7680, 7.1672));
        GeoCoordinate eddk24To = GeoCoordinate.fromLatLon(LatLon.fromDegrees(50.8625, 7.1317));
        GeoCoordinate eddk24From = GeoCoordinate.fromLatLon(LatLon.fromDegrees(50.8663, 7.1444));

        GeoRouteBuilder geoRouteBuilder = new GeoRouteBuilder(new SimpleEllipsoidCalculations(SimpleEllipsoidCalculations.eQuatorialEarthRadius));
        GeoRoute geoRoute = geoRouteBuilder.buildAirportToAirportRoute(edkb11From, edkb11To, eddk24From, eddk24To);

        log.debug("geoRoute={}", geoRoute);
        assertEquals("wp:50.7690095,7.1625727->takeoff:50.7674421,7.1697561->wp:50.7586475,7.2100295->wp:50.7617634,7.2233489->wp:50.7695139,7.2305295->wp:50.7783873,7.2283157->wp:50.8602005,7.2066196->wp:50.8690739,7.2044019->wp:50.8754893,7.1944375->wp:50.8770663,7.1804221->touchdown:50.8655304,7.1418276->wp:50.8625,7.1317000", geoRoute.toString());
    }
}

