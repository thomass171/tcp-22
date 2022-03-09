package de.yard.threed.engine;

import de.yard.threed.core.Degree;
import de.yard.threed.core.MathUtil;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.TestUtil;
import de.yard.threed.core.Vector3;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;
import de.yard.threed.engine.testutil.TestFactory;

import de.yard.threed.engine.testutil.TestHelper;
import org.junit.jupiter.api.Test;


/**
 * Created by thomass on 07.04.15.
 */
public class PathTest {
    static Platform platform = TestFactory.initPlatformForTest( new String[] {"engine"}, new PlatformFactoryHeadless());

    @Test
    public void testSegmentedPath() {
        SegmentedPath path = TestHelper.buildTestPath();
        TestUtil.assertEquals("length", (float) (2 + Math.PI + 3), path.getLength());
        TestUtil.assertVector3(path.getOrigin(), new Vector3(0, 0, 0));
        TestUtil.assertVector3(path.getDestination(), new Vector3(-5, 0, -4));

        TestUtil.assertVector3(new Vector3(0, 0, -2), path.getPosition((float) (2)));
        TestUtil.assertVector3(new Vector3(-2, 0, -4), path.getPosition((float) (2 + Math.PI)));

        // Die Tangenten sind normalisiert
        TestUtil.assertVector3(new Vector3(0, 0, -1), path.getTangent(0));
        TestUtil.assertVector3(new Vector3(0, 0, -1), path.getTangent(1.9f));
        // bei PI halbe sind 45 Grad, d.h. z = x
        TestUtil.assertVector3(new Vector3(-0.70710975f, 0, -0.70710975f), path.getTangent(2 + MathUtil.PI_2));
        TestUtil.assertVector3(new Vector3(-1, 0, 0), path.getTangent(2 + MathUtil.PI + 0.01f));

        // Die Extrusionsteps
        double[] steps = path.getExtrusionSteps(0);
        TestUtil.assertEquals("Anzahl Steps Segment 0", 1, steps.length);
        TestUtil.assertEquals("", 2, steps[0]);
        steps = path.getExtrusionSteps(1);
        TestUtil.assertEquals("Anzahl Steps Segment 1", 4, steps.length);
        for (int i=0;i<4;i++) {
            TestUtil.assertEquals("step " + i, 2 + (i + 1) * MathUtil.PI_4, steps[i]);
        }
    }

    /**
     * Der Pfad, ähnlich wie er fuer die Extrusion eines Kreisbogen zur Kugel verwendet wird.
     */
    @Test
    public void testSphereExtrusion(){
        SegmentedPath path = new SegmentedPath(new Vector3(1, 0, 0));
        Degree r = new Degree(360);
        path.addArc(new Vector3(0, 0, 0), r, 16);
    }

    @Test
    public void testCirclePath(){
        float radius = 2;
        int segments = 8;
        SegmentedPath path = new SegmentedPath(new Vector3(radius, 0, 0));
        Degree r = new Degree(360 * (segments - 1) / segments);
        path.addArc(new Vector3(0, 0, 0), r, segments - 1);
        float umfang = (float) (2 * Math.PI * radius);
        TestUtil.assertEquals("length", umfang * (segments - 1) / segments, path.getLength());
    }
}
