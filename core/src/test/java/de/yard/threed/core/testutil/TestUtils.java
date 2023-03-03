package de.yard.threed.core.testutil;

import de.yard.threed.core.Degree;
import de.yard.threed.core.DimensionF;
import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.core.LatLon;
import de.yard.threed.core.Matrix3;
import de.yard.threed.core.Matrix4;
import de.yard.threed.core.Pair;
import de.yard.threed.core.Payload;
import de.yard.threed.core.Point;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import org.junit.jupiter.api.Assertions;

import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Copied here from "main" to be a successor based on junit.
 * <p>
 * Date: 21.02.23
 */
public class TestUtils {
    //26.8.15: Sometimes there are great tolerances, but these are probably bugs.
    // public static float floattesttolerance = 0.001f;
    //28.8.16: 0.001->0.005 for picking ray maze
    public static double doubletesttolerance = 0.005f;

    public static void assertST(Vector2 expected, Vector2 actual) {
        Assertions.assertEquals(expected.getX(), actual.getX(), doubletesttolerance, "st.s");
        Assertions.assertEquals(expected.getY(), actual.getY(), doubletesttolerance, "st.t");
    }

    public static void assertST(String label, Vector2 expected, Vector2 actual) {
        Assertions.assertEquals(expected.getX(), actual.getX(), doubletesttolerance, label + ": st.s");
        Assertions.assertEquals(expected.getY(), actual.getY(), doubletesttolerance, label + ": st.t");
    }

    public static void assertVector3(Vector3 expected, Vector3 actual) {
        assertVector3(expected, actual, "");
    }

    public static void assertVector3(Vector3 expected, Vector3 actual, double tolerance) {
        assertVector3(expected, actual, tolerance, "");
    }

    public static void assertVector3(Vector3 expected, Vector3 actual, String label) {
        assertVector3(expected, actual, doubletesttolerance, label);
    }

    public static void assertVector3(Vector3 expected, Vector3 actual, double tolerance, String label) {
        Assertions.assertEquals(expected.getX(), actual.getX(), tolerance, label + ": v.x");
        Assertions.assertEquals(expected.getY(), actual.getY(), tolerance, label + ": v.y");
        Assertions.assertEquals(expected.getZ(), actual.getZ(), tolerance, label + ": v.z");
    }

    public static void assertQuaternion(Quaternion expected, Quaternion actual) {
        assertQuaternion(expected, actual, "");
    }

    public static void assertQuaternion(Quaternion expected, Quaternion actual, String label) {
        Assertions.assertEquals(expected.getX(), actual.getX(), doubletesttolerance, label + ": v.x");
        Assertions.assertEquals(expected.getY(), actual.getY(), doubletesttolerance, label + ": v.y");
        Assertions.assertEquals(expected.getZ(), actual.getZ(), doubletesttolerance, label + ": v.z");
        Assertions.assertEquals(expected.getW(), actual.getW(), doubletesttolerance, label + ": v.w");
    }

    /**
     *
     */
    public static void assertQuaternion(double degree0, double degree1, double degree2, Quaternion actual, String label) {
        double[] angles = new double[3];
        actual.toAngles(angles);
        Assertions.assertEquals(degree0, Degree.buildFromRadians(angles[0]).getDegree(), label + ".degree0");
        Assertions.assertEquals(degree1, Degree.buildFromRadians(angles[1]).getDegree(), label + ".degree1");
        Assertions.assertEquals(degree2, Degree.buildFromRadians(angles[2]).getDegree(), label + ".degree2");
    }

    public static void assertQuaternion(Degree degreex, Degree degreey, Degree degreez, Quaternion actual, String label) {
        double[] angles = new double[3];
        actual.toAngles(angles);
        Assertions.assertEquals(degreex.getDegree(), Degree.buildFromRadians(angles[0]).getDegree(), 0.0001, label + ".degree.x");
        Assertions.assertEquals(degreey.getDegree(), Degree.buildFromRadians(angles[1]).getDegree(), 0.0001, label + ".degree.y");
        Assertions.assertEquals(degreez.getDegree(), Degree.buildFromRadians(angles[2]).getDegree(), 0.0001, label + ".degree.z");
    }

    public static void assertMatrix4(String label, Matrix4 expected, Matrix4 actual) {
        assertMatrix4(label, expected, actual, doubletesttolerance);
    }

    public static void assertMatrix4(Matrix4 expected, Matrix4 actual) {
        assertMatrix4("", expected, actual, doubletesttolerance);
    }

    public static void assertMatrix4(String label, Matrix4 expected, Matrix4 actual, double delta) {
        Assertions.assertEquals(expected.getElement(0, 0), actual.getElement(0, 0), delta, label + "e11");
        Assertions.assertEquals(expected.getElement(0, 1), actual.getElement(0, 1), delta, label + "e12");
        Assertions.assertEquals(expected.getElement(0, 2), actual.getElement(0, 2), delta, label + "e13");
        Assertions.assertEquals(expected.getElement(0, 3), actual.getElement(0, 3), delta, label + "e14");
        Assertions.assertEquals(expected.getElement(1, 0), actual.getElement(1, 0), delta, label + "e21");
        Assertions.assertEquals(expected.getElement(1, 1), actual.getElement(1, 1), delta, label + "e22");
        Assertions.assertEquals(expected.getElement(1, 2), actual.getElement(1, 2), delta, label + "e23");
        Assertions.assertEquals(expected.getElement(1, 3), actual.getElement(1, 3), delta, label + "e24");
        Assertions.assertEquals(expected.getElement(2, 0), actual.getElement(2, 0), delta, label + "e31");
        Assertions.assertEquals(expected.getElement(2, 1), actual.getElement(2, 1), delta, label + "e32");
        Assertions.assertEquals(expected.getElement(2, 2), actual.getElement(2, 2), delta, label + "e33");
        Assertions.assertEquals(expected.getElement(2, 3), actual.getElement(2, 3), delta, label + "e34");
        Assertions.assertEquals(expected.getElement(3, 0), actual.getElement(3, 0), delta, label + "e41");
        Assertions.assertEquals(expected.getElement(3, 1), actual.getElement(3, 1), delta, label + "e42");
        Assertions.assertEquals(expected.getElement(3, 2), actual.getElement(3, 2), delta, label + "e43");
        Assertions.assertEquals(expected.getElement(3, 3), actual.getElement(3, 3), delta, label + "e44");
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

    public static void assertVector2(Vector2 expected, Vector2 actual) {
        assertVector2(expected, actual, "");
    }

    public static void assertVector2(Vector2 expected, Vector2 actual, String msg) {
        Assertions.assertEquals(expected.getX(), actual.getX(), doubletesttolerance, msg + ".x");
        Assertions.assertEquals(expected.getY(), actual.getY(), doubletesttolerance, msg + ".y");
    }

    public static void assertIndices(String msg, int[] expected, int[] actual) {
        for (int i = 0; i < expected.length; i++) {
            Assertions.assertEquals(expected[i], actual[i], msg + "" + i);
        }
    }

    public static void assertPoint(Point expected, Point actual) {
        assertPoint(expected, actual, "");
    }

    public static void assertPoint(Point expected, Point actual, String label) {
        Assertions.assertEquals(expected.getX(), actual.getX(), label + "p.x");
        Assertions.assertEquals(expected.getY(), actual.getY(), label + "p.y");
    }

    public static void assertMatrix3(String label, Matrix3 expected, Matrix3 actual) {
        double delta = doubletesttolerance;
        Assertions.assertEquals(expected.e11, actual.e11, delta, label + ":e11");
        Assertions.assertEquals(expected.e12, actual.e12, delta, label + ":e12");
        Assertions.assertEquals(expected.e13, actual.e13, delta, label + ":e13");
        Assertions.assertEquals(expected.e21, actual.e21, delta, label + ":e21");
        Assertions.assertEquals(expected.e22, actual.e22, delta, label + ":e22");
        Assertions.assertEquals(expected.e23, actual.e23, delta, label + ":e23");
        Assertions.assertEquals(expected.e31, actual.e31, delta, label + ":e31");
        Assertions.assertEquals(expected.e32, actual.e32, delta, label + ":e32");
        Assertions.assertEquals(expected.e33, actual.e33, delta, label + ":e33");
    }

    public static void assertBetween(double[] expected, double actual, String label) {
        Assertions.assertTrue(expected[0] <= actual && expected[1] >= actual, label);
    }

    public static void assertLatLon(LatLon expected, LatLon actual, double tolerance, String label) {
        Assertions.assertEquals(expected.getLatDeg().getDegree(), actual.getLatDeg().getDegree(), tolerance, label);
        Assertions.assertEquals(expected.getLonDeg().getDegree(), actual.getLonDeg().getDegree(), tolerance, label);
    }

    public static void assertDimensionF(DimensionF expected, DimensionF actual, double tolerance, String label) {
        Assertions.assertEquals(expected.getWidth(), actual.getWidth(), tolerance, label);
        Assertions.assertEquals(expected.getHeight(), actual.getHeight(), tolerance, label);
    }

    public static void assertPayload(Pair<String, String>[] expectedProperties, Payload actual, String label) {
        for (Pair<String, String> p : expectedProperties) {
            Object value = actual.get(p.getFirst());

            if (value == null) {
                fail("property not found:" + p.getFirst());
            }
            if (p.getSecond() != null) {
                if (p.getSecond().equals("*")) {
                    assertTrue(value != null, p.getFirst() + ": value is null");
                } else {
                    assertEquals(p.getSecond(), value.toString(), label + p.getFirst() + ":" + value + "!=" + p.getSecond());
                }
            }
        }
    }

    public static void assertEvent(EventType expectedType, Pair<String, String>[] expectedProperties, Event actual, String label) {
        Assertions.assertEquals(expectedType.getType(), actual.getType().getType(), "eventType");
        assertPayload(expectedProperties, actual.getPayload(), label);
    }

    public static void waitUntil(BooleanSupplier condition, int timeoutMillis) throws Exception {
        long start = System.currentTimeMillis();

        while (!condition.getAsBoolean()) {
            if (System.currentTimeMillis() - start > timeoutMillis) {
                fail(String.format("condition not met within %d millis", timeoutMillis));
            }
            Thread.sleep(100);
        }
    }
}
