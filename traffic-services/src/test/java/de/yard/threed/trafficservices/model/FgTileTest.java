package de.yard.threed.trafficservices.model;

import de.yard.threed.core.LatLon;
import de.yard.threed.core.geometry.Polygon;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.CoreTestFactory;
import de.yard.threed.core.testutil.PlatformFactoryTestingCore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static de.yard.threed.core.testutil.TestUtils.assertLatLon;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


public class FgTileTest {

    @BeforeAll
    static void setup() {
        Platform platform = CoreTestFactory.initPlatformForTest(new PlatformFactoryTestingCore(),null);
    }

    @Test
    void testDahlem(){
        Tile fgTile= new FgTile(3056410);
        Polygon<LatLon> outline = fgTile.getOutline();
        assertEquals(4, outline.getPointCount());
        assertLatLon(LatLon.fromDegrees(50.375,6.5), outline.getPoint(0),0.00001,"0");
        assertLatLon(LatLon.fromDegrees(50.375,6.75), outline.getPoint(1),0.00001,"0");
        assertLatLon(LatLon.fromDegrees(50.5,6.75), outline.getPoint(2),0.00001,"0");
        assertLatLon(LatLon.fromDegrees(50.5,6.5), outline.getPoint(3),0.00001,"0");
        assertFalse(outline.closed);
    }
}
