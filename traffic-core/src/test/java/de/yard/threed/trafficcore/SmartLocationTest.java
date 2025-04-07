package de.yard.threed.trafficcore;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.CoreTestFactory;
import de.yard.threed.core.testutil.PlatformFactoryTestingCore;
import de.yard.threed.trafficcore.model.SmartLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class SmartLocationTest {
    static Platform platform = CoreTestFactory.initPlatformForTest(new PlatformFactoryTestingCore(),null);

    @Test
    public void testBasic(){
        SmartLocation smartLocation;
        smartLocation = SmartLocation.fromString("parkpos:E4");
        assertEquals("E4", smartLocation.getParkPos());
        assertEquals("E4", smartLocation.getSubLocation());
        assertNull(smartLocation.getGroundnetLocation());

        smartLocation = SmartLocation.fromString("E4");
        assertNull(smartLocation.getParkPos());
        assertNull(smartLocation.getSubLocation());
        assertNull(smartLocation.getGroundnetLocation());

        smartLocation = SmartLocation.fromString("a:b");
        assertNull(smartLocation.getParkPos());
        assertEquals("b", smartLocation.getSubLocation());
        assertNull(smartLocation.getGroundnetLocation());

        smartLocation = SmartLocation.fromString("groundnet:b");
        assertNull(smartLocation.getParkPos());
        assertEquals("b", smartLocation.getSubLocation());
        assertEquals("b", smartLocation.getGroundnetLocation());

        smartLocation = SmartLocation.fromString("geo:52.2,7.3");
        assertNull(smartLocation.getParkPos());
        assertEquals("52.2,7.3", smartLocation.getSubLocation());
        assertNull(smartLocation.getGroundnetLocation());
        assertEquals(52.2, smartLocation.getGeoCoordinate().getLatDeg().getDegree());
        assertEquals(7.3, smartLocation.getGeoCoordinate().getLonDeg().getDegree());

        smartLocation = SmartLocation.fromString("coordinate:52.2,7.3,3.6");
        assertNull(smartLocation.getParkPos());
        assertEquals("52.2,7.3,3.6", smartLocation.getSubLocation());
        assertNull(smartLocation.getGroundnetLocation());
        assertEquals(52.2, smartLocation.getCoordinate().getX());
        assertEquals(7.3, smartLocation.getCoordinate().getY());
        assertEquals(3.6, smartLocation.getCoordinate().getZ());
    }
}
