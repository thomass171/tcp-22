package de.yard.threed.core;


import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.PlatformFactoryTestingCore;
import de.yard.threed.core.testutil.CoreTestFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>
 * Created by thomass on 07.3.17.
 */
public class DegreeTest {
    static Platform platform = CoreTestFactory.initPlatformForTest(new PlatformFactoryTestingCore(), null);

    @Test
    public void test1() {
        Degree d = new Degree(6.580982);
        assertEquals("6.580982", d.toString());

        d = Degree.parseDegree("N50 52.011847");
        assertEquals(50.866864f, d.getDegree(), 0.00001, "");

        d = Degree.parseDegree("N50 51.219783");
        assertEquals(50.853663f, d.getDegree(), 0.00001, "");

        d = Degree.parseDegree("E7 9.861116");
        assertEquals(7.164352f, d.getDegree(), 0.00001, "");

        d = Degree.parseDegree("E7 9.861116");
        assertEquals(7.164352f, d.getDegree(), 0.00001, "");

    }

    @Test
    public void testNormalize() {

        assertEquals("0.0", new Degree(0.0).normalize().toString());
        assertEquals("45.0", new Degree(45.0).normalize().toString());
        assertEquals("315.0", new Degree(-45.0).normalize().toString());
        assertEquals("315.0", new Degree(315.0).normalize().toString());
        assertEquals("0.0", new Degree(360.0).normalize().toString());

    }

    @Test
    public void testDiff() {
        Degree d1 = new Degree(348);
        Degree d2 = new Degree(64);
        assertEquals(12.0 + 64.0, Degree.diff(d1, d2).getDegree());

        d1 = new Degree(289);
        d2 = new Degree(348);
        assertEquals(11.0 + 48.0, Degree.diff(d1, d2).getDegree());

        d1 = new Degree(351);
        d2 = new Degree(244);
        assertEquals(-(51.0 + 56), Degree.diff(d1, d2).getDegree());

        d1 = new Degree(21);
        d2 = new Degree(244);
        assertEquals(-(21.0 + 116.0), Degree.diff(d1, d2).getDegree());

        d1 = new Degree(109);
        d2 = new Degree(322);
        assertEquals(-(109.0 + 38.0), Degree.diff(d1, d2).getDegree());
    }


}
