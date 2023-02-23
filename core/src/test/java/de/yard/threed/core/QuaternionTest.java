package de.yard.threed.core;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.PlatformFactoryTestingCore;
import de.yard.threed.core.testutil.TestFactory;
import de.yard.threed.core.testutil.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Das sind die Tests fuer plattformunabhängige Methoden (z.B. in Mathutil2).
 * Es gibt noch einen weiteren QuaternionTest!
 * <p>
 * Created by thomass on 07.09.16.
 */
public class QuaternionTest {
    static Platform platform = TestFactory.initPlatformForTest(new PlatformFactoryTestingCore(), null);

    @Test
    public void testLookat() {
        Vector3 direction = new Vector3(0, 0, -1);
        Vector3 up = new Vector3(0, 1, 0);
        Quaternion q = (MathUtil2.buildLookRotation(direction, up));
        // Referenzwerte einfach uebernommen
        Assertions.assertEquals(0f, q.getX(), "x");
        Assertions.assertEquals(1f, q.getY(), "y");
        Assertions.assertEquals(0f, q.getZ(), "z");
        Assertions.assertEquals(0f, q.getW(), "w");
        TestUtils.assertQuaternion(0, 180, 0, q, "");
    }

    @Test
    public void testVectorRotation() {
        // keine Rotation
        Vector3 v1 = new Vector3(0, 0, -1);
        Quaternion q = (MathUtil2.buildQuaternion(v1, v1));
        TestUtils.assertQuaternion(new Quaternion(), q, "no rotation");
        TestUtils.assertQuaternion(0, 0, 0, q, "no rotation");

        // entgegengesetzte Rotation.
        Vector3 v2 = new Vector3(0, 0, 1);
        q = (MathUtil2.buildQuaternion(v1, v2));
        // Referenzwerte einfach uebernommen. Das mit den Euler Angles ist doch frasgwuerdig wegen order
        TestUtils.assertQuaternion(new Quaternion(0, 1, 0, 0)/*new Degree(180),new Degree(0),new Degree(0))*/, q, "no rotation");
    }

    /**
     * Skizze 29b
     * 31.3.17: toangles ist wegen Euler Abhängigkeiten und leichter Falschnutzung (Einzelwinkelverwendung) eigentlich unbrauchbar.
     */
    @Test
    public void testVectorRotation2() {
        Vector3 vf = new Vector3(0.5746958f, -1.9156525f, 0);
        Vector3 vt = new Vector3(0.5746958f, 1.9156525f, 0);

        Quaternion rot = Vector3.getRotation(vt, vf);
        TestUtils.assertVector3(vf, vt.rotate(rot), "gegenprobe");
       /* float[] angles = new float[3];
        float angle;
        rot.toAngles(angles);
        angle = angles[2];
        TestUtil.assertFloat("angle", 2.56f, angle);*/

        rot = Vector3.getRotation(vf, vt);
        TestUtils.assertVector3(vt, vf.rotate(rot), "gegenprobe");
        /*angles = new float[3];         
        rot.toAngles(angles);
        angle = angles[2];
        TestUtil.assertFloat("angle", 2.56f, angle);*/
    }
}
