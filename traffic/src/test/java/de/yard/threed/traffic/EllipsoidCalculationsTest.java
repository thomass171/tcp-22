package de.yard.threed.traffic;

import de.yard.threed.core.Degree;
import de.yard.threed.core.GeoCoordinate;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import org.junit.jupiter.api.Test;

/**
 *
 */
public class EllipsoidCalculationsTest {
    Platform platform = EngineTestFactory.initPlatformForTest(new String[]{"engine"}, new SimpleHeadlessPlatformFactory());

    GeoCoordinate southpole = new GeoCoordinate(new Degree(-90), new Degree(0), 0);
    GeoCoordinate northpole = new GeoCoordinate(new Degree(90), new Degree(0), 0);
    GeoCoordinate greenwich = new GeoCoordinate(new Degree(51.477524), new Degree(0), 0);
    GeoCoordinate nullequator = new GeoCoordinate(new Degree(0), new Degree(0), 0);
    // radius shouldn't matter here
    EllipsoidCalculations ec = new SimpleEllipsoidCalculations(10);

    /**
     * 30.3.25 Ref values taken from tcp-flightgear
     */
    @Test
    public void testBuildRotation() {
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
    public void testLocSpaceToFgSpace(){
        Vector3 v = new Vector3(1,2,-3);

        TestUtils.assertVector3(new Vector3(1,3,2),v.multiply(FgVehicleSpace.getLocSpaceToFgSpace()));
    }
}
