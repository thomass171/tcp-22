package de.yard.threed.core.testutil;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

/**
 * Nachbildung der Klasse aus org.junit für (Platform) Tests at runtime, die wegen ThreeJS auch im Browser laufen muessen.
 * Created by thomass on 29.05.15.
 */
public class Assert {
    static Log logger = /*Engine*/Platform.getInstance().getLog(Assert.class);

    public static void assertEquals(String label, int expected, int actual) {
        if (expected != actual) {
            fail("failed: " + label + ", expected:" + expected + ",actual:" + actual);
        }
    }

    public static void assertEquals(String label, String expected, String actual) {
        if (!expected.equals(actual)) {
            fail("failed: " + label + ", expected:" + expected + ",actual:" + actual);
        }
    }

    public static void assertEquals(String label, double expected, double actual, double tolerance) {
        if (java.lang.Double.isNaN(actual)){
            fail("failed: " + label + ": expected:" + expected + ",actual: NaN");
        }
        if (Math.abs(expected - actual) > tolerance) {
            fail("failed: " + label + ": expected:" + expected + ",actual:" + actual);
        }
    }

    public static void assertEquals(String label, boolean expected, boolean actual) {
        if (expected != actual) {
            fail("failed: " + label + ", expected:" + expected + ",actual:" + actual);
        }
    }
    
    /**
     * Ob die Behandlung des fail so besonders gut ist, muss sich nocht zeigen
     * @param s
     */
    public static void fail(String s) {
        logger.error(s);
        throw new RuntimeException(s);
    }
}
