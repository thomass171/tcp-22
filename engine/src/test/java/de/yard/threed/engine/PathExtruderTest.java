package de.yard.threed.engine;

import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;
import de.yard.threed.engine.testutil.TestFactory;
import de.yard.threed.core.testutil.TestUtil;
import org.junit.Test;


/**
 * Created by thomass on 07.04.15.
 */
public class PathExtruderTest {
    static Platform platform = TestFactory.initPlatformForTest( new String[] {"engine"}, new PlatformFactoryHeadless());

    /**
     * Der Radius des Extrusionkreises dürfte für die Extrusion doch keine Rolle spielen. Oder doch,
     * wenn er nicht 1 ist, erfolgt die Extrusion auch nach aussen.
     * 2.12.16: DEn Kommentar gibts woanders auch. Es spielt eine Rolle.
     */
    @Test
    public void testPathForDisc() {
        testPathForDisc(1);
        testPathForDisc(2);
        testPathForDisc(77.7f);
    }

    private void testPathForDisc(float radius) {
        int segments = 64;
        SegmentedPath path = SegmentedPath.buildHorizontalArc(radius,segments);
        PathExtruder pathextruder = new PathExtruder(path);
        // Der ist ja nicht ganz geschlossen
        // 02.12.16: closed geo gibts nicht mehr. Darum doch geschlossen
        TestUtil.assertEquals("length", (float) ((float) (segments/*-1*/)/segments * 2 *radius * Math.PI), path.getLength());
        Vector2 p = new Vector2(3,0);
        Vector3 v;
        //v = pathextruder.transformPoint(p,0);
        //TestUtil.assertVector3(new Vector3(radius*3, 0, 0), v);

        p = new Vector2(0.5f,0);
        // Nach hinten
        v = pathextruder.transformPoint(p, (float) (radius*Math.PI/2));
        System.out.println("v="+v.dump(""));
        TestUtil.assertVector3(new Vector3(0, 0, -0.5f), v);
        v = pathextruder.transformPoint(p,(float) (radius*Math.PI));
        TestUtil.assertVector3(new Vector3(-0.5f, 0, 0), v);

        //TODO noch weiter testen

    }

}
