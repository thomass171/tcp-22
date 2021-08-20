package de.yard.threed.core;

import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.PlatformFactoryTestingCore;
import de.yard.threed.core.testutil.TestFactory;
import de.yard.threed.core.testutil.TestUtil;
import org.junit.Test;

/**
 * Das sind die Tests fuer plattformunabhängige Methoden (z.B. in Mathutil2).
 * Es gibt noch einen weiteren QuaternionTest!
 * <p>
 * Created by thomass on 07.09.16.
 */
public class QuaternionTest {
    static Platform platform = TestFactory.initPlatformForTest(new PlatformFactoryTestingCore(),null);

    @Test
    public void testLookat() {
        Vector3 direction = new Vector3(0, 0, -1);
        Vector3 up = new Vector3(0, 1, 0);
        Quaternion q = (MathUtil2.buildLookRotation(direction, up));
        // Referenzwerte einfach uebernommen
        TestUtil.assertEquals("x", 0f, q.getX());
        TestUtil.assertEquals("y", 1f, q.getY());
        TestUtil.assertEquals("z", 0f, q.getZ());
        TestUtil.assertEquals("w", 0f, q.getW());
        TestUtil.assertQuaternion("", 0, 180, 0, q);
    }

    @Test
    public void testVectorRotation() {
        // keine Rotation
        Vector3 v1 = new Vector3(0, 0, -1);
        Quaternion q = (MathUtil2.buildQuaternion(v1, v1));
        TestUtil.assertQuaternion("no rotation", new Quaternion(), q);
        TestUtil.assertQuaternion("no rotation", 0, 0, 0, q);

        // entgegengesetzte Rotation.
        Vector3 v2 = new Vector3(0, 0, 1);
        q = (MathUtil2.buildQuaternion(v1, v2));
        // Referenzwerte einfach uebernommen. Das mit den Euler Angles ist doch frasgwuerdig wegen order
        TestUtil.assertQuaternion("no rotation", new Quaternion(0, 1, 0, 0)/*new Degree(180),new Degree(0),new Degree(0))*/, q);
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
        TestUtil.assertVector3("gegenprobe",vf,vt.rotate(rot));
       /* float[] angles = new float[3];
        float angle;
        rot.toAngles(angles);
        angle = angles[2];
        TestUtil.assertFloat("angle", 2.56f, angle);*/

         rot = Vector3.getRotation(vf, vt);
        TestUtil.assertVector3("gegenprobe",vt,vf.rotate(rot));
        /*angles = new float[3];         
        rot.toAngles(angles);
        angle = angles[2];
        TestUtil.assertFloat("angle", 2.56f, angle);*/
    }
}
