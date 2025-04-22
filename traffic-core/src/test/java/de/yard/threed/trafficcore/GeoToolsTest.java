package de.yard.threed.trafficcore;

import de.yard.threed.core.Degree;
import de.yard.threed.core.LatLon;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.CoreTestFactory;
import de.yard.threed.core.testutil.PlatformFactoryTestingCore;
import de.yard.threed.trafficcore.geodesy.GeoTools;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class GeoToolsTest {
    static Platform platform = CoreTestFactory.initPlatformForTest(new PlatformFactoryTestingCore(), null);

    LatLon runway14LFrom =  LatLon.fromDegrees(50.880469, 7.129075);
    LatLon runway14LTo =  LatLon.fromDegrees(50.855194, 7.165692);

    /**
     * Non obvious expected values from https://www.movable-type.co.uk/scripts/latlong.html
     */
    @Test
    public void testHeading() {

        Degree heading = GeoTools.heading(runway14LFrom, runway14LTo);
        assertEquals(137.54853588, heading.getDegree(),0.000001);

        heading = GeoTools.heading(runway14LFrom, runway14LFrom);
        assertNull(heading);

        heading = GeoTools.heading(LatLon.fromDegrees(50.0, 7.0), LatLon.fromDegrees(50.0, 7.002));
        assertEquals(90.0, heading.getDegree(),0.001);

        heading = GeoTools.heading(LatLon.fromDegrees(50.0, 7.0), LatLon.fromDegrees(50.0, 6.999));
        assertEquals(270.0, heading.getDegree(),0.001);

        heading = GeoTools.heading(LatLon.fromDegrees(50.0, 7.0), LatLon.fromDegrees(50.0001, 7.0));
        assertEquals(0.0, heading.getDegree(),0.001);

        heading = GeoTools.heading(LatLon.fromDegrees(50.0, 7.0), LatLon.fromDegrees(49.9999, 7.0));
        assertEquals(180.0, heading.getDegree(),0.001);

        heading = GeoTools.heading(LatLon.fromDegrees(50.0, -70.0), LatLon.fromDegrees(50.0, -70.0001));
        assertEquals(270.0, heading.getDegree(),0.001);

    }
}
