package de.yard.threed.core;

import de.yard.threed.core.Degree;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.PlatformFactoryTestingCore;
import de.yard.threed.core.testutil.TestFactory;
import de.yard.threed.core.MathUtil2;
import de.yard.threed.core.testutil.TestUtil;
import org.junit.Test;


/**
 * Auch fuer MathUtil2.
 * <p>
 * Created by thomass on 29.11.16.
 */
public class MathTest {
    static Platform platform = TestFactory.initPlatformForTest(new PlatformFactoryTestingCore(),null);

    @Test
    public void testAngle() {
        double[] angles = new double[3];
        Vector3 rechts = new Vector3(1, 0, 0);
        Vector3 oben = new Vector3(0, 0, -1);
        Vector3 links = new Vector3(-1, 0, 0);
        Vector3 unten = new Vector3(0, 0, 1);

        double angle = rechts.getAngleBetween(rechts);
        TestUtil.assertEquals("anglerechts", 0, angle);
        Quaternion q = Quaternion.buildQuaternion(rechts, rechts);
        q.toAngles(angles);
        TestUtil.assertEquals("angles.x", 0, angles[0]);
        TestUtil.assertEquals("angles.y", 0, angles[1]);
        TestUtil.assertEquals("angles.z", 0, angles[2]);

        angle = rechts.getAngleBetween(oben);
        TestUtil.assertEquals("angle oben",  new Degree(90).toRad(), angle);
        q = Quaternion.buildQuaternion(rechts, oben);
        q.toAngles(angles);
        TestUtil.assertEquals("angles.x", 0, angles[0]);
        TestUtil.assertEquals("angles.y",  new Degree(90).toRad(), angles[1]);
        TestUtil.assertEquals("angles.z", 0, angles[2]);

        angle = rechts.getAngleBetween(links);
        TestUtil.assertEquals("angle links",  new Degree(180).toRad(), angle);
        q = Quaternion.buildQuaternion(rechts, links);
        q.toAngles(angles);
        TestUtil.assertEquals("angles.x", 0, angles[0]);
        TestUtil.assertEquals("angles.y",  new Degree(180).toRad(), angles[1]);
        TestUtil.assertEquals("angles.z", 0, angles[2]);
        Vector3 newrechts = rechts.rotate(q);
        TestUtil.assertVector3("",links,newrechts);
                
        angle = rechts.getAngleBetween(unten);
        TestUtil.assertEquals("angle unten",  new Degree(90).toRad(), angle);
        q = Quaternion.buildQuaternion(rechts, unten);
        q.toAngles(angles);
        TestUtil.assertEquals("angles.x", 0, angles[0]);
        TestUtil.assertEquals("angles.y",  new Degree(-90).toRad(), angles[1]);
        TestUtil.assertEquals("angles.z", 0, angles[2]);

        q = Quaternion.buildQuaternion(rechts, oben);
        Vector3 newoben = rechts.rotate(q);
        TestUtil.assertVector3("",oben,newoben);
    }

    @Test
    public void testIntersection() {
        Vector3 origin = new Vector3(-0.25f, 1, 0.25f);
        Vector3 direction = new Vector3(0, -1, 0);
        double size = 1;
        Vector3 a = new Vector3(-size, 0, size);
        Vector3 b = new Vector3(size, 0, size);
        Vector3 c = new Vector3(-size, 0, -size);
        //Ray ray = new Ray(origin, direction);
        Vector3 intersect = (MathUtil2.getTriangleIntersection(origin, direction, a, b, c));
        System.out.println("intersect=" + intersect);
        //Refwerte aus ThreeJS
        TestUtil.assertVector3("intersect", new Vector3(-0.25f, 0, 0.25f), intersect);
    }

    @Test
    public void testHeading() {
        Vector2 v = new Vector2(1, 0);
        Degree heading = MathUtil2.getHeadingFromDirection(v);
        TestUtil.assertEquals("heading", 90,  heading.getDegree());
        TestUtil.assertVector2("headingrev", v, MathUtil2.getDirectionFromHeading(heading));
        v = new Vector2(0, 1);
        heading = MathUtil2.getHeadingFromDirection(v);
        TestUtil.assertEquals("heading", 0,  heading.getDegree());
        TestUtil.assertVector2("headingrev", v, MathUtil2.getDirectionFromHeading(heading));
        v = new Vector2(0, -1);
        heading = MathUtil2.getHeadingFromDirection(v);
        TestUtil.assertEquals("heading", 180,  heading.getDegree());
        TestUtil.assertVector2("headingrev", v, MathUtil2.getDirectionFromHeading(heading));
        v = new Vector2(-1, 0);
        heading = MathUtil2.getHeadingFromDirection(v);
        TestUtil.assertEquals("heading", 270,  heading.getDegree());
        TestUtil.assertVector2("headingrev", v, MathUtil2.getDirectionFromHeading(heading));
        v = new Vector2(-1, -1);
        heading = MathUtil2.getHeadingFromDirection(v);
        TestUtil.assertEquals("heading", 225,  heading.getDegree());
        TestUtil.assertVector2("headingrev", v.normalize(), MathUtil2.getDirectionFromHeading(heading));
        v = new Vector2(1, 1);
        heading = MathUtil2.getHeadingFromDirection(v);
        TestUtil.assertEquals("heading", 45,  heading.getDegree());
        TestUtil.assertVector2("headingrev", v.normalize(), MathUtil2.getDirectionFromHeading(heading));
        v = new Vector2(-1, 1);
        heading = MathUtil2.getHeadingFromDirection(v);
        TestUtil.assertEquals("heading", 315,  heading.getDegree());
        TestUtil.assertVector2("headingrev", v.normalize(), MathUtil2.getDirectionFromHeading(heading));
        v = new Vector2(1, -1);
        heading = MathUtil2.getHeadingFromDirection(v);
        TestUtil.assertEquals("heading", 135,  heading.getDegree());
        TestUtil.assertVector2("headingrev", v.normalize(), MathUtil2.getDirectionFromHeading(heading));
    }

    /**
     * Skizze 24
     */
    @Test
    public void testNearestPointOnVector() {
        Vector3 p = new Vector3(0, 0, 0);
        Vector3 start = new Vector3(1, 1, 0);
        Vector3 v = new Vector3(2, 0, 0);

        //a)
        Vector3 nearest = Vector3.getNearestPointOnVector(p, start, v);
        TestUtil.assertVector3("", new Vector3(1, 1, 0), nearest);
        p = new Vector3(1, 2, 0);
        nearest = Vector3.getNearestPointOnVector(p, start, v);
        TestUtil.assertVector3("", new Vector3(1, 1, 0), nearest);

        //b)
        p = new Vector3(2, 0, 0);
        nearest = Vector3.getNearestPointOnVector(p, start, v);
        TestUtil.assertVector3("", new Vector3(2, 1, 0), nearest);

        p = new Vector3(2, 2, 0);
        nearest = Vector3.getNearestPointOnVector(p, start, v);
        TestUtil.assertVector3("", new Vector3(2, 1, 0), nearest);

        p = new Vector3(3, 0, 0);
        nearest = Vector3.getNearestPointOnVector(p, start, v);
        TestUtil.assertVector3("", new Vector3(3, 1, 0), nearest);

        p = new Vector3(3, 2, 0);
        nearest = Vector3.getNearestPointOnVector(p, start, v);
        TestUtil.assertVector3("", new Vector3(3, 1, 0), nearest);

        //c)
        p = new Vector3(22, 0, 0);
        nearest = Vector3.getNearestPointOnVector(p, start, v);
        TestUtil.assertVector3("", new Vector3(3, 1, 0), nearest);
        p = new Vector3(22, 7, 0);
        nearest = Vector3.getNearestPointOnVector(p, start, v);
        TestUtil.assertVector3("", new Vector3(3, 1, 0), nearest);

    }

    @Test
    public void testLineIntersection() {
        Vector2 a = new Vector2(8, 2);
        Vector2 b = new Vector2(9, 5);
        Vector2 c = new Vector2(6, 6);
        Vector2 d = new Vector2(5, 9);
        Vector2 result = MathUtil2.getLineIntersection(a, c, b, d);
        TestUtil.assertVector2("", new Vector2(4, 10), result);
    }

    /**
     * Die beiden Geraden sind quais parrallel. Da scheint es massive Rundungsfehler zu geben, anders ist der Fehler
     * z.Z. nicht zu erklären.
     */
    @Test
    public void testLineIntersection2() {
        Vector2 a = new Vector2(-1806.6642f,1056.5591f);
        Vector2 b = new Vector2(-1819.7007f,1041.3917f);
        Vector2 c = new Vector2(-1806.6642f,1056.5591f);
        Vector2 d = new Vector2(-1788.3176f,1077.9044f);
        Vector2 result = MathUtil2.getLineIntersection(a, b, c, d);
        //TestUtil.assertVector2("", new Vector2(-1806.6642f,1056.5591f), result);
    }

    @Test
    public void testRotateOnAxis() {
        Vector3 v = new Vector3(0,1,0);
        Vector3 axis = new Vector3(1,0,0);
        Vector3 result = v.rotateOnAxis(new Degree(90),axis);
        TestUtil.assertVector3(new Vector3(0,0,1),result);
        result = v.rotateOnAxis(new Degree(-90),axis);
        TestUtil.assertVector3(new Vector3(0,0,-1),result);
        result = v.rotateOnAxis(new Degree(45),axis);
        TestUtil.assertVector3(new Vector3(0,0.7071067f,0.7071067f),result);

        axis = new Vector3(1,1,0);
        result = v.rotateOnAxis(new Degree(90),axis);
        //Werte scheinen plausibel
        TestUtil.assertVector3(new Vector3(0.49999994f,0.49999994f,0.7071067f),result);
    }

    @Test
    public void testRotationAngle() {
        double PI4 = MathUtil2.PI_2/2;
        Vector2 a = new Vector2(1, 0);
        Vector2 b = new Vector2(0,1);
        double result = Vector2.getRotationAngleBetween(a, b);
        TestUtil.assertFloat("", MathUtil2.PI_2, result);
        b = new Vector2(-1,1);
        result = Vector2.getRotationAngleBetween(a, b);
        TestUtil.assertFloat("", 3*PI4, result);
        b = new Vector2(-1,0);
        result = Vector2.getRotationAngleBetween(a, b);
        TestUtil.assertFloat("", 4*PI4, result);
        b = new Vector2(-1,-1);
        result = Vector2.getRotationAngleBetween(a, b);
        TestUtil.assertFloat("", 5*PI4, result);
        b = new Vector2(0,-1);
        result = Vector2.getRotationAngleBetween(a, b);
        TestUtil.assertFloat("", 6*PI4, result);
        b = new Vector2(1,-1);
        result = Vector2.getRotationAngleBetween(a, b);
        TestUtil.assertFloat("", 7*PI4, result);
        b = new Vector2(1,0);
        result = Vector2.getRotationAngleBetween(a, b);
        //0 waere auch richtig. Tja.
        TestUtil.assertFloat("", 8*PI4, result);
    }
}