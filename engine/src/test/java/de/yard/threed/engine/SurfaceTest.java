package de.yard.threed.engine;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.Vector2;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;
import de.yard.threed.engine.testutil.TestFactory;

import de.yard.threed.core.testutil.TestUtil;
import org.junit.jupiter.api.Test;


import java.util.ArrayList;
import java.util.List;


/**
 * Tests fuer alle Implementierungen von Surface.
 * <p/>
 * Created by thomass on 13.11.14.
 */
public class SurfaceTest {
    static Platform platform = TestFactory.initPlatformForTest( new String[] {"engine"}, new PlatformFactoryHeadless());

    @Test
    public void testMostSimpleGridSurface() {
       List<Double> segments = new ArrayList<Double>();
        // die Segmentlänge ist in diesem Test voellig belanglos
        segments.add(456d);
        GridSurface surface = new GridSurface(1,segments);
        Vector2 st = surface.calcVertexLocation(0,0);
        TestUtil.assertST(new Vector2(0, 1), st);
         st = surface.calcVertexLocation(0,1);
        TestUtil.assertST(new Vector2(0, 0), st);
         st = surface.calcVertexLocation(1,1);
        TestUtil.assertST(new Vector2(1, 0), st);
         st = surface.calcVertexLocation(1,0);
        TestUtil.assertST(new Vector2(1, 1), st);

    }

    /*21.8.15 TODO @Test
    public void testULikeGridSurface() {
        float arcbaselen = MathUtil.getBaseLen(0.25f, new Degree(30));
        // die Laenge haengt natuerlich davon ab, wie genau U gebaut wird. Darum pruefen, dass
        // sich das nicht einfach aendert.
        TestUtil.assertEquals(0.1294, arcbaselen, TestUtil.floattesttolerance);
        float totallength = 3 + 6 * arcbaselen + 3;

        ShapeGeometry sg = ShapedGeometryTestHelper.buildULike();
        TestUtil.assertEquals("Anzahl Surfaces", 1, sg.getSurfaces().size());

        GridSurface surface = (GridSurface) sg.getSurfaces().get(0);
        Vector2 st = surface.calcVertexLocation(0,0);
        TestUtil.assertST(new Vector2(0,0),st);
        st = surface.calcVertexLocation(0,2);
        TestUtil.assertST(new Vector2(0,(3 + arcbaselen) / totallength),st);
        //ganz rechts
        st = surface.calcVertexLocation(0,4);
        TestUtil.assertST(new Vector2(0,(3 + 3 * arcbaselen) / totallength),st);
    }*/
}