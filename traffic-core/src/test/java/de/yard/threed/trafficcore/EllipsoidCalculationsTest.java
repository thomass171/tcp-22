package de.yard.threed.trafficcore;

import de.yard.threed.core.Degree;
import de.yard.threed.core.GeoCoordinate;
import de.yard.threed.core.LatLon;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.CoreTestFactory;
import de.yard.threed.core.testutil.PlatformFactoryTestingCore;
import de.yard.threed.core.testutil.TestUtils;
import org.junit.jupiter.api.Test;

import static de.yard.threed.core.testutil.TestUtils.assertLatLon;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 */
public class EllipsoidCalculationsTest {
    Platform platform = CoreTestFactory.initPlatformForTest(new PlatformFactoryTestingCore(), null);

    GeoCoordinate southpole = new GeoCoordinate(new Degree(-90), new Degree(0), 0);
    GeoCoordinate northpole = new GeoCoordinate(new Degree(90), new Degree(0), 0);
    GeoCoordinate greenwich = new GeoCoordinate(new Degree(51.477524), new Degree(0), 0);
    GeoCoordinate nullequator = new GeoCoordinate(new Degree(0), new Degree(0), 0);


    /**
     * 30.3.25 Ref values taken from tcp-flightgear
     */
    @Test
    public void testBuildRotation() {
        // radius shouldn't matter here
        EllipsoidCalculations ec = new SimpleEllipsoidCalculations(10);
        // der Nordpol ist quasi Default und hat keine Rotation. 
        Quaternion refsuedpol = Quaternion.buildFromAngles(new Degree(0), new Degree(180), new Degree(0));
        Quaternion uprotationsuedpol = ec.buildZUpRotation(southpole);
        TestUtils.assertQuaternion(refsuedpol, uprotationsuedpol, "suedpol");

        Quaternion refnordpol = Quaternion.buildFromAngles(new Degree(0), new Degree(0), new Degree(0));
        Quaternion uprotationnordpol = ec.buildZUpRotation(northpole);
        TestUtils.assertQuaternion(refnordpol, uprotationnordpol, "nordpol");

        Quaternion refgreenwich = Quaternion.buildFromAngles(new Degree(0), new Degree((90 - greenwich.getLatDeg().getDegree())), new Degree(0));
        Quaternion uprotationgreenwich = ec.buildZUpRotation(greenwich);
        TestUtils.assertQuaternion(refgreenwich, uprotationgreenwich, "greenwich");

        Quaternion refnullequator = Quaternion.buildFromAngles(new Degree(0), new Degree(90), new Degree(0));
        Quaternion uprotationnullequator = ec.buildZUpRotation(nullequator);
        TestUtils.assertQuaternion(refnullequator, uprotationnullequator, "nullequator");
    }



    @Test
    public void testDistance() {
        // radius does matter here
        EllipsoidCalculations ec = new SimpleEllipsoidCalculations(SimpleEllipsoidCalculations.eQuatorialEarthRadius);
        LatLon cologne = LatLon.fromDegrees(50.941402, 6.957739);
        LatLon paris = LatLon.fromDegrees(48.853363, 2.349042);
        double distance = ec.distanceTo(cologne, paris);
        // 403.46130 is reference value from an online calculator. Accept difference as result of different algorithms
        assertEquals(403.91327, distance, 0.0001);
    }

    @Test
    public void testApplyCourseDistance() {
        // radius does matter here
        EllipsoidCalculations ec = new SimpleEllipsoidCalculations(SimpleEllipsoidCalculations.eQuatorialEarthRadius);
        LatLon cologne = LatLon.fromDegrees(50.941402, 6.957739);
        // expected values were validated by map.
        LatLon destination = ec.applyCourseDistance(cologne, new Degree(30), 0.3);
        assertLatLon(LatLon.fromDegrees(50.9437358, 6.95987), destination, 0.0001, "");
        destination = ec.applyCourseDistance(cologne, new Degree(270), 30);
        assertLatLon(LatLon.fromDegrees(50.94062097, 6.53005258), destination, 0.0001, "");
    }
}
