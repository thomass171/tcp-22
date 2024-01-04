package de.yard.threed.traffic.osm;


import de.yard.threed.core.Vector3;
import de.yard.threed.core.geometry.CustomGeometry;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.graph.Graph;
import de.yard.threed.graph.GraphEdge;

import de.yard.threed.graph.GraphTest;
import de.yard.threed.javacommon.JavaBundleResolverFactory;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import de.yard.threed.traffic.TrafficGraphFactory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


/**
 * Created by thomass on 07.09.16.
 */
public class TerrainBuilderTest {

    Platform  platform = EngineTestFactory.initPlatformForTest(new String[]{"engine"}, new SimpleHeadlessPlatformFactory());
    // Alles in der z-Ebene, zumindest zunächst

    int z = 0;

    @Test
    public void testRoadNachRechts() {
        float width = 1.8f;
        Vector3 from = new Vector3(-2, 1, z);
        Vector3 to = new Vector3(+3, 1, z);
        CustomGeometry roadgeo = TerrainBuilder.buildRoadGeometry(from, to, width);
        Vector3 linksoben = new Vector3(-2, 1.9f, 0);
        Vector3 linksunten = new Vector3(-2, 0.1f, 0);
        Vector3 rechtsunten = new Vector3(3, 0.1f, 0);
        Vector3 rechtsoben = new Vector3(3, 1.9f, 0);

        TestUtils.assertVector3( linksoben, roadgeo.getVertices().get(0),"linksoben");
        TestUtils.assertVector3( linksunten, roadgeo.getVertices().get(1),"linksunten");
        TestUtils.assertVector3( rechtsunten, roadgeo.getVertices().get(2),"rechtsunten");
        TestUtils.assertVector3( rechtsoben, roadgeo.getVertices().get(3),"rechtsoben");
    }

    @Test
    public void testRoadNachOben() {
        float width = 1.8f;
        Vector3 from = new Vector3(-2, 1, z);
        Vector3 to = new Vector3(-2, 4, z);
        CustomGeometry roadgeo = TerrainBuilder.buildRoadGeometry(from, to, width);
        // Die Bezeichnung linksoben etc, sind jetzt nicht mehr passend bzw. verwirrent
        Vector3 v0 = new Vector3(-2.9f, 1, 0);
        Vector3 v1 = new Vector3(-1.1f, 1f, 0);
        Vector3 v2 = new Vector3(-1.1f, 4, 0);
        Vector3 v3 = new Vector3(-2.9f, 4, 0);

        TestUtils.assertVector3( v0, roadgeo.getVertices().get(0),"linksoben");
        TestUtils.assertVector3( v1, roadgeo.getVertices().get(1),"linksunten");
        TestUtils.assertVector3( v2, roadgeo.getVertices().get(2),"rechtsunten");
        TestUtils.assertVector3( v3, roadgeo.getVertices().get(3),"rechtsoben");
    }

    /**
     * Erzeugung von (Out)Lines wie in OsmScene.
     * <p>
     * Skizze 11
     */
    @Test
    public void testGraphOutlineZ0() {
        Graph osm = TrafficGraphFactory.buildOsmSample().getBaseGraph();
        GraphEdge startedge = osm.getEdge(0);
        float width = 0.4f;
        float offset = width / 2;

        if (TerrainBuilder.useoutline) {
            CustomGeometry geo = TerrainBuilder.buildRoadGeometry(startedge, width,osm.orientation);
            Assertions.assertEquals( 4, geo.vertices.size(),"vertices");
            TestUtils.assertVector3(new Vector3(offset, 0, 0), geo.vertices.get(0),"v0");
            TestUtils.assertVector3( new Vector3(offset, 10, 0), geo.vertices.get(1),"v1");
            TestUtils.assertVector3( new Vector3(-offset, 10, 0), geo.vertices.get(2),"v2");
            TestUtils.assertVector3(new Vector3(-offset, 0, 0), geo.vertices.get(3),"v3");
        }
    }

    /**
     * Erzeugung von (Out)Lines.
     * <p>
     * Skizze 12
     */
    @Test
    public void testReturnkreisOutline() {
        float radius = 8;
        //Der Graph entsteht in y0.
        Graph graph = GraphTest.buildReturnKreis(radius);
        GraphEdge edgelinks = graph.getEdge(0);
        GraphEdge halbkreis = graph.getEdge(1);
        float width = 0.8f;
        float offset = width / 2;
        if (TerrainBuilder.useoutline) {
            CustomGeometry geo = TerrainBuilder.buildRoadGeometry(edgelinks, width,graph.orientation);
            Assertions.assertEquals(4, geo.vertices.size(),"vertices");
            TestUtils.assertVector3( new Vector3(31, 0, -offset), geo.vertices.get(0),"v0");
            TestUtils.assertVector3( new Vector3(15, 0, -offset), geo.vertices.get(1),"v1");
            TestUtils.assertVector3( new Vector3(15, 0, offset), geo.vertices.get(2),"v2");
            TestUtils.assertVector3( new Vector3(31, 0, offset), geo.vertices.get(3),"v3");
            geo = TerrainBuilder.buildRoadGeometry(halbkreis, width,graph.orientation);
            Assertions.assertEquals( 34, geo.vertices.size(),"vertices");
            TestUtils.assertVector3( new Vector3(15, 0, -offset), geo.vertices.get(0),"v0");
            TestUtils.assertVector3( new Vector3(15, 0, -16 + offset), geo.vertices.get(16),"v1");
            TestUtils.assertVector3( new Vector3(15, 0, -16.4f), geo.vertices.get(17),"v2");
            TestUtils.assertVector3( new Vector3(15, 0, offset), geo.vertices.get(33),"v3");
        }
    }
}