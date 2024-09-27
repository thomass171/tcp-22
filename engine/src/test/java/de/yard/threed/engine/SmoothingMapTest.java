package de.yard.threed.engine;


import de.yard.threed.core.geometry.Face3List;
import de.yard.threed.core.geometry.GeometryHelper;
import de.yard.threed.core.geometry.VertexMap;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.geometry.ShapeGeometry;
import de.yard.threed.engine.test.testutil.TestUtil;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 *
 */
public class SmoothingMapTest {
    static Platform platform = EngineTestFactory.initPlatformForTest(new String[]{"engine", "data"}, new PlatformFactoryHeadless());

    @Test
    public void testPrepareSmoothingMaps() {

        ShapeGeometry cg = ShapeGeometry.buildBox(1, 1, 1, null);

        // Faces are:
        //
        List<Face3List> facelists = GeometryHelper.triangulate(cg.vertices, cg.getFaceLists());

        List<VertexMap> smoothingMaps = VertexMap.prepareVertexMaps(cg.vertices, facelists);

        TestUtil.assertEquals("smoothingMaps", 6, smoothingMaps.size());
        // Reference values from debugging
        VertexMap vertexMap = smoothingMaps.get(0);
        // original indices only
        assertEquals(4, vertexMap.map.size());
        assertEquals(2, vertexMap.map.get(0).size());
        assertEquals(1, vertexMap.map.get(1).size());
        assertEquals(1, vertexMap.map.get(4).size());
        assertEquals(2, vertexMap.map.get(5).size());

        vertexMap = smoothingMaps.get(1);
        // two original indices, two new
        assertEquals(4, vertexMap.map.size());
        assertEquals(1, vertexMap.map.get(2).size());
        assertEquals(2, vertexMap.map.get(6).size());
        assertEquals(2, vertexMap.map.get(8).size());
        assertEquals(1, vertexMap.map.get(9).size());

        vertexMap = smoothingMaps.get(2);
        // also two original indices(??)
        assertEquals(4, vertexMap.map.size());
        assertEquals(1, vertexMap.map.get(3).size());
        assertEquals(2, vertexMap.map.get(7).size());
        assertEquals(2, vertexMap.map.get(10).size());
        assertEquals(1, vertexMap.map.get(11).size());

        // TODO check remaining three
    }

}