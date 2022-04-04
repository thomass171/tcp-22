package de.yard.threed.core.testutil;

import de.yard.threed.core.*;
import de.yard.threed.core.Point;




/**
 * Date: 04.06.14
 */
public class TestUtil {
    //26.8.15: Es gibt wohl schon mal so grosse Toleranzen. NeeNee, das dürften echte Fehler sein.
    // public static float floattesttolerance = 0.001f;
    //28.8.16: 0.001->0.005 wegen picking maze
    public static double doubletesttolerance = 0.005f;

    public static void assertST(Vector2 expected, Vector2 actual) {
        Assert.assertEquals("st.s", expected.getX(), actual.getX(), doubletesttolerance);
        Assert.assertEquals("st.t", expected.getY(), actual.getY(), doubletesttolerance);
    }

    public static void assertST(String label, Vector2 expected, Vector2 actual) {
        Assert.assertEquals(label + ": st.s", expected.getX(), actual.getX(), doubletesttolerance);
        Assert.assertEquals(label + ": st.t", expected.getY(), actual.getY(), doubletesttolerance);
    }

    public static void assertVector3(Vector3 expected, Vector3 actual) {
        assertVector3("", expected, actual);
    }

    public static void assertVector3(String label, Vector3 expected, Vector3 actual) {
        assertVector3(label, expected, actual, doubletesttolerance);
    }

    public static void assertVector3(String label, Vector3 expected, Vector3 actual, double tolerance) {
        Assert.assertEquals(label + ": v.x", expected.getX(), actual.getX(), tolerance);
        Assert.assertEquals(label + ": v.y", expected.getY(), actual.getY(), tolerance);
        Assert.assertEquals(label + ": v.z", expected.getZ(), actual.getZ(), tolerance);
    }

    public static void assertQuaternion(String label, Quaternion expected, Quaternion actual) {
        Assert.assertEquals(label + ": v.x", expected.getX(), actual.getX(), doubletesttolerance);
        Assert.assertEquals(label + ": v.y", expected.getY(), actual.getY(), doubletesttolerance);
        Assert.assertEquals(label + ": v.z", expected.getZ(), actual.getZ(), doubletesttolerance);
        Assert.assertEquals(label + ": v.w", expected.getW(), actual.getW(), doubletesttolerance);
    }

    /**
     *
     */
    public static void assertQuaternion(String label, double degree0, double degree1, double degree2, Quaternion actual) {
        double[] angles = new double[3];
        actual.toAngles(angles);
        assertEquals(label + ".degree0", degree0, Degree.buildFromRadians(angles[0]).getDegree());
        assertEquals(label + ".degree1", degree1, Degree.buildFromRadians(angles[1]).getDegree());
        assertEquals(label + ".degree2", degree2, Degree.buildFromRadians(angles[2]).getDegree());
    }

    public static void assertQuaternion(String label, Degree degreex, Degree degreey, Degree degreez, Quaternion actual) {
        double[] angles = new double[3];
        actual.toAngles(angles);
        assertEquals(label + ".degree.x", degreex.getDegree(), Degree.buildFromRadians(angles[0]).getDegree());
        assertEquals(label + ".degree.y", degreey.getDegree(), Degree.buildFromRadians(angles[1]).getDegree());
        assertEquals(label + ".degree.z", degreez.getDegree(), Degree.buildFromRadians(angles[2]).getDegree());
    }

    public static void assertMatrix4(String label, Matrix4 expected, Matrix4 actual) {
        assertMatrix4(label, expected, actual, doubletesttolerance);
    }

    public static void assertMatrix4(Matrix4 expected, Matrix4 actual) {
        assertMatrix4("", expected, actual, doubletesttolerance);
    }

    public static void assertMatrix4(String label, Matrix4 expected, Matrix4 actual, double delta) {
        Assert.assertEquals(label + "e11", expected.getElement(0, 0), actual.getElement(0, 0), delta);
        Assert.assertEquals(label + "e12", expected.getElement(0, 1), actual.getElement(0, 1), delta);
        Assert.assertEquals(label + "e13", expected.getElement(0, 2), actual.getElement(0, 2), delta);
        Assert.assertEquals(label + "e14", expected.getElement(0, 3), actual.getElement(0, 3), delta);
        Assert.assertEquals(label + "e21", expected.getElement(1, 0), actual.getElement(1, 0), delta);
        Assert.assertEquals(label + "e22", expected.getElement(1, 1), actual.getElement(1, 1), delta);
        Assert.assertEquals(label + "e23", expected.getElement(1, 2), actual.getElement(1, 2), delta);
        Assert.assertEquals(label + "e24", expected.getElement(1, 3), actual.getElement(1, 3), delta);
        Assert.assertEquals(label + "e31", expected.getElement(2, 0), actual.getElement(2, 0), delta);
        Assert.assertEquals(label + "e32", expected.getElement(2, 1), actual.getElement(2, 1), delta);
        Assert.assertEquals(label + "e33", expected.getElement(2, 2), actual.getElement(2, 2), delta);
        Assert.assertEquals(label + "e34", expected.getElement(2, 3), actual.getElement(2, 3), delta);
        Assert.assertEquals(label + "e41", expected.getElement(3, 0), actual.getElement(3, 0), delta);
        Assert.assertEquals(label + "e42", expected.getElement(3, 1), actual.getElement(3, 1), delta);
        Assert.assertEquals(label + "e43", expected.getElement(3, 2), actual.getElement(3, 2), delta);
        Assert.assertEquals(label + "e44", expected.getElement(3, 3), actual.getElement(3, 3), delta);
    }

    public static boolean equals(Vector3 v1, Vector3 v2) {
        if (Math.abs(v1.getX() - v2.getX()) > doubletesttolerance)
            return false;
        if (Math.abs(v1.getY() - v2.getY()) > doubletesttolerance)
            return false;
        if (Math.abs(v1.getZ() - v2.getZ()) > doubletesttolerance)
            return false;
        return true;
    }

    public static void assertVector2(String msg, Vector2 expected, Vector2 actual) {
        Assert.assertEquals(msg + ".x", expected.getX(), actual.getX(), doubletesttolerance);
        Assert.assertEquals(msg + ".y", expected.getY(), actual.getY(), doubletesttolerance);
    }

    public static void assertEquals(String label, double expected, double actual) {
        Assert.assertEquals(label, expected, actual, doubletesttolerance);
    }

    public static void assertInt(String label, int expected, int actual) {
        Assert.assertEquals(label, expected, actual);
    }

    public static void assertIndices(String msg, int[] expected, int[] actual) {
        for (int i = 0; i < expected.length; i++) {
            assertEquals(msg + ""+i, expected[i], actual[i]);
        }
    }

    public static void assertPoint(Point expected, Point actual) {
        assertPoint("", expected, actual);
    }

    @Deprecated
    public static void assertPoint(String label, Point expected, Point actual) {
        Assert.assertEquals(label + "p.x", expected.getX(), actual.getX());
        Assert.assertEquals(label + "p.y", expected.getY(), actual.getY());
    }

    public static void assertPoint(Point expected, Point actual, String label) {
        Assert.assertEquals(label + "p.x", expected.getX(), actual.getX());
        Assert.assertEquals(label + "p.y", expected.getY(), actual.getY());
    }

    public static void assertMatrix3(String label, Matrix3 expected, Matrix3 actual) {
        double delta = doubletesttolerance;
        Assert.assertEquals(label + ":e11", expected.e11, actual.e11, delta);
        Assert.assertEquals(label + ":e12", expected.e12, actual.e12, delta);
        Assert.assertEquals(label + ":e13", expected.e13, actual.e13, delta);
        Assert.assertEquals(label + ":e21", expected.e21, actual.e21, delta);
        Assert.assertEquals(label + ":e22", expected.e22, actual.e22, delta);
        Assert.assertEquals(label + ":e23", expected.e23, actual.e23, delta);
        Assert.assertEquals(label + ":e31", expected.e31, actual.e31, delta);
        Assert.assertEquals(label + ":e32", expected.e32, actual.e32, delta);
        Assert.assertEquals(label + ":e33", expected.e33, actual.e33, delta);
    }

    public static void assertEquals(String label, String expected, String actual) {
        Assert.assertEquals(label, expected, actual);
    }

    public static void assertTrue(String label, boolean actual) {
        Assert.assertEquals(label, true, actual);
    }

    public static void assertFalse(String label, boolean actual) {
        Assert.assertEquals(label, false, actual);
    }

    public static void assertNull(String label, Object obj) {
        if (obj != null) {
            Assert.fail("failed: " + label + ", expected: null,actual:" + obj);
        }
    }

    public static void assertNotNull(String label, Object obj) {
        if (obj == null) {
            Assert.fail("failed: " + label + ", expected instance ,actual: null");
        }
    }



    public static void assertFloat(String label, double expected, double actual) {
        Assert.assertEquals(label, expected, actual, doubletesttolerance);
    }

    public static void assertFloat(String label, double expected, double actual, double tolerance) {
        Assert.assertEquals("", expected, actual, tolerance);
    }

    public static void assertBetween(String label, double[] expected, double actual) {
        assertTrue(label, expected[0] <= actual && expected[1] >= actual);
    }

    public static void assertLatLon(String label, LatLon expected, LatLon actual, double tolerance) {
        assertFloat(label,expected.getLatDeg().getDegree(),actual.getLatDeg().getDegree(),tolerance);
        assertFloat(label,expected.getLonDeg().getDegree(),actual.getLonDeg().getDegree(),tolerance);
    }
}
