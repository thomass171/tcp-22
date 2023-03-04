package de.yard.threed.graph;


import de.yard.threed.core.Degree;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.core.MathUtil2;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import de.yard.threed.engine.testutil.DeterministicIntProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static de.yard.threed.core.testutil.Assert.assertEquals;

/**
 * Es wird zwar RailingFactory zum Spiegeln verwendet, Trotzdem ein abstrakter Graphtest.
 * 19.12.16: Teilweise aber doch auch fuer railing, weil die Tests vielfach gut wiederverwendbar sind.
 * Auch fuer GraphNode,GraphPosition und GraphEdge. Aber nicht fuer GraphUtil.
 * <p>
 * <p>
 * Created by thomass on 13.09.16.
 */
public class GraphTest {
    //static Platform platform = TestFactory.initPlatformForTest(false, null, false);
    static Platform platform = EngineTestFactory.initPlatformForTest(new String[]{"engine", "data"/*,"data-old","railing"*/}, new SimpleHeadlessPlatformFactory());

    @Test
    public void testSplit() {
        Graph graph = new Graph();
        GraphNode n1 = graph.addNode("n1", new Vector3(2, 3, 4));
        GraphNode n2 = graph.addNode("n2", new Vector3(5, 7, 9));
        GraphEdge edge = graph.connectNodes(n1, n2);
        TestUtils.assertVector3(new Vector3(3, 4, 5), edge.getDirection(), "dir");
        Assertions.assertEquals(7.071068f, edge.getLength(), 0.00001, "len");
        Assertions.assertEquals(1, n1.edges.size(), "edges");
        Assertions.assertEquals(1, n2.edges.size(), "edges");

        GraphNode split = edge.split(graph, 3);
        Assertions.assertEquals(2, graph.getEdgeCount(), "edges");
        Assertions.assertEquals(3f, graph.getEdge(0).getLength(), 0.0001, "len0");
        TestUtils.assertVector3(split.getLocation().subtract(n1.getLocation()), graph.getEdge(0).getDirection(), "dir0");
        Assertions.assertEquals(4.071068f, graph.getEdge(1).getLength(), 0.00001, "len split");
        TestUtils.assertVector3(n2.getLocation().subtract(split.getLocation()), graph.getEdge(1).getDirection(), "dir1");
        Assertions.assertEquals(1, n1.edges.size(), "edges");
        Assertions.assertEquals(1, n2.edges.size(), "edges");

    }

    /**
     * split mit Nachfolger
     */
    @Test
    public void testSplit2() {
        Graph graph = new Graph();
        GraphNode n1 = graph.addNode("n1", new Vector3(2, 3, 4));
        GraphNode n2 = graph.addNode("n2", new Vector3(5, 7, 9));
        GraphNode n3 = graph.addNode("n3", new Vector3(11, 14, 17));
        GraphEdge edge = graph.connectNodes(n1, n2);
        graph.connectNodes(n2, n3);
        TestUtils.assertVector3(new Vector3(3, 4, 5), edge.getDirection(), "dir");
        Assertions.assertEquals(7.071068f, edge.getLength(), 0.0001, "len");
        Assertions.assertEquals(1, n3.edges.size(), "edges");
        GraphNode split = edge.split(graph, 3);
        Assertions.assertEquals(3, graph.getEdgeCount(), "edges");
        Assertions.assertEquals(3f, graph.getEdge(0).getLength(), 0.00001, "len0");
        TestUtils.assertVector3(split.getLocation().subtract(n1.getLocation()), graph.getEdge(0).getDirection(), "dir0");
        Assertions.assertEquals(4.071068f, graph.getEdge(2).getLength(), 0.00001, "len split");
        TestUtils.assertVector3(n2.getLocation().subtract(split.getLocation()), graph.getEdge(2).getDirection(), "dir1");
        Assertions.assertEquals(1, n3.edges.size(), "edges");

    }

    /**
     * Graph mit zwei geraden Stücken parallel zur x-Achse nach links.
     * <p>
     * <-#<-
     */
    @Test
    public void testGraphPosition1() {
        Graph graph = new Graph();
        // Graph ist in xz Ebene!
        GraphNode start = GraphFactory.addNode(graph, 20, 0, "start");
        GraphNode links = GraphFactory.addNode(graph, 15, 0, "links");
        GraphEdge edgelinks = graph.connectNodes(start, links, "links");
        GraphNode rechts = GraphFactory.addNode(graph, 33, 0, "rechts");
        GraphEdge edgerechts = graph.connectNodes(rechts, start, "rechts");
        GraphPosition pos = new GraphPosition(edgelinks);
        GraphMovingComponent gmc = new GraphMovingComponent();
        gmc.setGraph(graph, pos, null);
        gmc.setSelector(new RandomGraphSelector(new DeterministicIntProvider(new int[]{1, 0})));
        move(gmc);
        LocalTransform posrot = GraphMovingSystem.getPosRot(gmc, null);
        //Die Rotation muss eine y 90 Grad sein
        TestUtils.assertVector3(new Vector3(-1, 0, 0), new Vector3(0, 0, -1).rotate(posrot.rotation), "rotated reference");

        // auf ganz links positionieren und beide Kanten reverse gehen.
        pos = new GraphPosition(edgelinks, 0, true);
        gmc = new GraphMovingComponent();
        gmc.setGraph(graph, pos, null);
        gmc.setSelector(new RandomGraphSelector(new DeterministicIntProvider(new int[]{0, 1, 0})));
        Assertions.assertEquals(0, gmc.getCurrentposition().edgeposition, "position");
        gmc.moveForward(1);
        Assertions.assertEquals(1, gmc.getCurrentposition().edgeposition, "position");
        gmc.moveForward(3);
        Assertions.assertEquals(4, gmc.getCurrentposition().edgeposition, "position");
        gmc.moveForward(-3);
        Assertions.assertEquals(1, gmc.getCurrentposition().edgeposition, "position");
        // auf die rechts Kante gehen
        gmc.moveForward(7);
        Assertions.assertEquals(3, gmc.getCurrentposition().edgeposition, "position");
        Assertions.assertEquals("rechts", gmc.getCurrentposition().currentedge.getName(), "kante");
        // Stueck weiter
        gmc.moveForward(2);
        Assertions.assertEquals(5, gmc.getCurrentposition().edgeposition, "position");
        // wieder zurueck auf die linke Kante gehen
        gmc.moveForward(-8);
        Assertions.assertEquals(2, gmc.getCurrentposition().edgeposition, "position");
        Assertions.assertEquals("links", gmc.getCurrentposition().currentedge.getName(), "kante");
    }

    /**
     * Wie oben, aber rechts Stueck mit anderer Ausrichtung
     * <p>
     * <-#->
     */
    @Test
    public void testGraphPosition2() {
        int xl = 15, x = 20, xr = 33;
        Graph graph = new Graph();
        GraphNode start = GraphFactory.addNode(graph, x, 0, "start");
        GraphNode links = GraphFactory.addNode(graph, xl, 0, "links");
        GraphEdge edgelinks = graph.connectNodes(start, links, "links");
        GraphNode rechts = GraphFactory.addNode(graph, xr, 0, "rechts");
        GraphEdge edgerechts = graph.connectNodes(start, rechts, "rechts");
        GraphPosition pos = new GraphPosition(edgelinks);
        GraphMovingComponent gmc = new GraphMovingComponent();
        gmc.setGraph(null, pos, null);
        gmc.setSelector(new RandomGraphSelector(new DeterministicIntProvider(new int[]{1, 0})));
        move(gmc);

        RailingBranchSelector railingselector = new RailingBranchSelector();
        GraphPosition railingpos = new GraphPosition(edgelinks);
        gmc = new GraphMovingComponent();
        gmc.setGraph(null, railingpos, null);
        gmc.setSelector(railingselector);
        TestUtils.assertVector3(new Vector3(x, 0, 0), railingpos.get3DPosition());
        gmc.moveForward(777);
        TestUtils.assertVector3(new Vector3(xl, 0, 0), railingpos.get3DPosition());

        // auf ganz links positionieren und nach rechts gehen.
        pos = new GraphPosition(edgelinks, 0, true);
        gmc = new GraphMovingComponent();
        gmc.setGraph(null, pos, null);
        gmc.setSelector(new RandomGraphSelector(new DeterministicIntProvider(new int[]{0, 1, 0})));
        Assertions.assertEquals(0, gmc.getCurrentposition().edgeposition, "position");
        gmc.moveForward(1);
        Assertions.assertEquals(1, gmc.getCurrentposition().edgeposition, "position");
        Assertions.assertTrue(gmc.getCurrentposition().isReverseOrientation(), "reverse");
        // auf die rechts Kante gehen
        gmc.moveForward(7);
        Assertions.assertEquals(3, gmc.getCurrentposition().edgeposition, "position");
        Assertions.assertEquals("rechts", gmc.getCurrentposition().currentedge.getName(), "kante");
        Assertions.assertFalse(gmc.getCurrentposition().isReverseOrientation(), "reverse");
        // Stueck weiter
        gmc.moveForward(2);
        Assertions.assertEquals(5, gmc.getCurrentposition().edgeposition, "position");
        // wieder zurueck auf die linke Kante gehen
        gmc.moveForward(-8);
        Assertions.assertEquals(2, gmc.getCurrentposition().edgeposition, "position");
        Assertions.assertEquals("links", gmc.getCurrentposition().currentedge.getName(), "kante");
        Assertions.assertTrue(gmc.getCurrentposition().isReverseOrientation(), "reverse");


        // ganz links positionieren.
        pos = new GraphPosition(edgelinks, 0/*edgelinks.getLength()*/, true);
        gmc = new GraphMovingComponent();
        gmc.setGraph(graph, pos, null);
        gmc.setSelector(new RandomGraphSelector(new DeterministicIntProvider(new int[]{0, 1, 0})));
        Assertions.assertEquals(0, gmc.getCurrentposition().edgeposition, "position");
        gmc.moveForward(1);
        Assertions.assertEquals(1, gmc.getCurrentposition().edgeposition, "position");
        gmc.moveForward(3);
        Assertions.assertEquals(4, gmc.getCurrentposition().edgeposition, "position");
        gmc.moveForward(-3);
        Assertions.assertEquals(1, gmc.getCurrentposition().edgeposition, "position");
        // auf die rechts Kante gehen
        gmc.moveForward(7);
        Assertions.assertEquals(3, gmc.getCurrentposition().edgeposition, "position");
        Assertions.assertEquals("rechts", gmc.getCurrentposition().currentedge.getName(), "kante");
        // Stueck weiter
        gmc.moveForward(2);
        Assertions.assertEquals(5, gmc.getCurrentposition().edgeposition, "position");
        // wieder zurueck auf die linke Kante gehen
        gmc.moveForward(-8);
        Assertions.assertEquals(2, gmc.getCurrentposition().edgeposition, "position");
        Assertions.assertEquals("links", gmc.getCurrentposition().currentedge.getName(), "kante");
    }

    /**
     * Wie oben, aber wieder andere Ausrichtung
     * <p>
     * ->#<-
     */
    @Test
    public void testGraphPosition3() {
        int xl = 15, x = 20, xr = 33;
        Graph graph = new Graph();
        GraphNode start = GraphFactory.addNode(graph, x, 0, "start");
        GraphNode links = GraphFactory.addNode(graph, xl, 0, "links");
        GraphEdge edgelinks = graph.connectNodes(links, start, "links");
        GraphNode rechts = GraphFactory.addNode(graph, xr, 0, "rechts");
        GraphEdge edgerechts = graph.connectNodes(rechts, start, "rechts");
        // auf ganz links positionieren und nach rechts gehen.
        GraphPosition pos = new GraphPosition(edgelinks, 0, false);
        GraphMovingComponent gmc = new GraphMovingComponent();
        gmc.setGraph(null, pos, null);
        gmc.setSelector(new RandomGraphSelector(new DeterministicIntProvider(new int[]{0, 1, 0})));
        Assertions.assertEquals(0, gmc.getCurrentposition().edgeposition, "position");
        gmc.moveForward(1);
        Assertions.assertEquals(1, gmc.getCurrentposition().edgeposition, "position");
        Assertions.assertEquals(1, gmc.getCurrentposition().getAbsolutePosition(), "absoluteposition");
        Assertions.assertFalse(gmc.getCurrentposition().isReverseOrientation(), "reverse");
        // auf die rechts Kante gehen
        gmc.moveForward(7);
        Assertions.assertEquals(3, gmc.getCurrentposition().edgeposition, "position");
        Assertions.assertEquals(10, gmc.getCurrentposition().getAbsolutePosition(), "absoluteposition");
        Assertions.assertEquals("rechts", gmc.getCurrentposition().currentedge.getName(), "kante");
        Assertions.assertTrue(gmc.getCurrentposition().isReverseOrientation(), "reverse");
        // Stueck weiter
        gmc.moveForward(2);
        Assertions.assertEquals(5, gmc.getCurrentposition().edgeposition, "position");
        // wieder zurueck auf die linke Kante gehen
        gmc.moveForward(-8);
        Assertions.assertEquals(2, gmc.getCurrentposition().edgeposition, "position");
        Assertions.assertEquals(2, gmc.getCurrentposition().getAbsolutePosition(), "absoluteposition");
        Assertions.assertEquals("links", gmc.getCurrentposition().currentedge.getName(), "kante");
        Assertions.assertFalse(gmc.getCurrentposition().isReverseOrientation(), "reverse");

        //outline von links nach rechts (unten). upVector ist y. Dann geht rechts nach +z
        float offset = 7;
        List<Vector3> outline = graph.orientation.getOutline(buildListFromEdge(edgelinks.from, edgelinks, edgerechts), offset, -1);
        Assertions.assertEquals(3, outline.size());
        TestUtils.assertVector3(new Vector3(15, 0, offset), outline.get(0));
        TestUtils.assertVector3(new Vector3(20, 0, offset), outline.get(1));
        TestUtils.assertVector3(new Vector3(33, 0, offset), outline.get(2));

    }

    @Test
    public void testSkizze12Abstrakt() {
        testSkizze12(false);
    }

    @Test
    public void testSkizze12Railing() {
        testSkizze12(true);
    }

    /**
     * Ein gerades Stueck und dann ein "Zurückkreis". Skizze 12.
     */
    private void testSkizze12(boolean forrailing) {
        float radius = 8;
        float umfang = 2 * (float) (Math.PI * radius);
        float halbumfang = (float) (Math.PI * radius);
        float umfang4 = umfang / 4;
        float umfang8 = umfang / 8;

        Graph graph = buildReturnKreis(radius);
        GraphEdge edgelinks = graph.getEdge(0);
        GraphEdge halbkreis = graph.getEdge(1);

        GraphSelector selector;
        if (forrailing) {
            selector = new RailingBranchSelector();
        } else {
            selector = new RandomGraphSelector(new DeterministicIntProvider(new int[]{1, 0, 1, 0, 1, 0, 1}));
        }
        GraphPosition pos = new GraphPosition(edgelinks);
        GraphMovingComponent gmc = new GraphMovingComponent();
        gmc.setGraph(null, pos, null);
        gmc.setSelector(selector);
        TestUtils.assertVector3(new Vector3(15 + radius + radius, 0, 0), pos.get3DPosition());
        TestUtils.assertQuaternion(new Degree(0), new Degree(90), new Degree(0), get3DRotation(graph, pos), "");

        // nach ganz links
        gmc.moveForward(2 * radius + umfang4);
        Assertions.assertEquals(umfang4, pos.edgeposition, 0.00001, "position");
        TestUtils.assertVector3(new Vector3(15 - radius, 0, -radius), pos.get3DPosition());
        TestUtils.assertQuaternion(new Degree(0), new Degree(0), new Degree(0), get3DRotation(graph, pos), "");

        // halb rechts oben
        gmc.moveForward(umfang4 + umfang8);
        Assertions.assertEquals(umfang8, pos.edgeposition, 0.00001, "position");
        TestUtils.assertQuaternion(new Degree(0), new Degree(-135), new Degree(0), get3DRotation(graph, pos), "");
        // halb rechts unten 
        gmc.moveForward(umfang4);
        Assertions.assertEquals(umfang8, pos.edgeposition, 0.00001, "position");
        Assertions.assertEquals("closing", pos.currentedge.getName(), "edgename");

        // start wird jetzt ueberschossen. Er darf nicht wieder in die Gegenrichtung gehen, sondern muss auf start bei pos 0 stehenbleiben, aber
        // mit anderer Orientierung als am Anfang. 20.12.16: Aber warum eigentlich? Das gilt fuer Railing, aber nicht einen abstrakten Graph. Da
        // ist das weitergehen auf dem ersten Edge doch genau das richtige.
        // 19.4.17:Na, ob das wirklich immer so ist? Oder config abhaengig?
        gmc.moveForward(umfang4);
        if (forrailing) {
            // Am Ende des closing stehenbleiben
            Assertions.assertEquals(pos.currentedge.getLength(), pos.edgeposition, "position");
            Assertions.assertEquals("closing", pos.currentedge.getName(), "edgename");
        } else {
            Assertions.assertEquals(umfang8, pos.edgeposition, 0.00001, "position");
            Assertions.assertEquals("getFirst", pos.currentedge.getName(), "edgename");
            // wieder zurueck
            gmc.moveForward(-umfang4);
            Assertions.assertEquals(umfang8, pos.edgeposition, 0.00001, "position");
            Assertions.assertEquals("closing", pos.currentedge.getName(), "edgename");
        }
    }

    /**
     * Ein gerades Stueck und dann ein "Zurückkreis". Skizze 12. Mit Extension.
     */
    @Test
    public void testSkizze12Ext() {
        float radius = 8;
        float umfang = 2 * (float) (Math.PI * radius);
        float halbumfang = (float) (Math.PI * radius);
        float umfang4 = umfang / 4;
        float umfang8 = umfang / 8;

        Graph graph = GraphFactory.buildReturnKreis(radius, true);
        GraphEdge edgelinks = graph.getEdge(0);
        GraphEdge halbkreis = graph.getEdge(1);

        GraphSelector selector;
        selector = new RailingBranchSelector();
        GraphPosition pos = new GraphPosition(edgelinks);
        GraphMovingComponent gmc = new GraphMovingComponent();
        gmc.setGraph(null, pos, null);
        gmc.setSelector(selector);

        // ganz rum auf das Ende der Extension
        gmc.moveForward(200000);
        Assertions.assertEquals("extension", pos.currentedge.getName(), "edgename");
        Assertions.assertEquals(9, pos.edgeposition, "position");
        Assertions.assertFalse(pos.isReverseOrientation(), "reverse");
        TestUtils.assertVector3(new Vector3(15 + radius + radius + 9, 0, 0), pos.get3DPosition());
        //rotation bezieht sich auf -z
        TestUtils.assertQuaternion(new Degree(0), new Degree(-90), new Degree(0), get3DRotation(graph, pos), "");

        //jetzt zurueck auf den Beginn des Halbkreis
        gmc.moveForward(-2 * radius - 9 - 0.00001f);
        Assertions.assertEquals("firsthalbkreis", pos.currentedge.getName(), "edgename");
        Assertions.assertEquals(halbumfang, pos.edgeposition, 0.0001, "position");
        Assertions.assertTrue(pos.isReverseOrientation(), "reverse");
        //rotation bezieht sich auf -z
        TestUtils.assertQuaternion(new Degree(0), new Degree(-90), new Degree(0), get3DRotation(graph, pos), "");

    }

    /**
     * Orientierung im Bogen.
     * Ein gerades Stueck und dann ein "Zurückkreis". Skizze 12.
     */
    @Test
    public void testGraphPosition4() {
        float radius = 8;
        float umfang = 2 * (float) (Math.PI * radius);
        float halbumfang = (float) (Math.PI * radius);
        float umfang4 = umfang / 4;
        float umfang8 = umfang / 8;

        Graph graph = buildReturnKreis(radius);
        GraphEdge edgelinks = graph.getEdge(0);

        GraphPosition pos = new GraphPosition(edgelinks);
        GraphMovingComponent gmc = new GraphMovingComponent();
        gmc.setGraph(null, pos, null);
        gmc.setSelector(new RandomGraphSelector(new DeterministicIntProvider(new int[]{1, 0, 1, 0, 1, 0, 1})));
        // der up Vector ist der default. Trotzdem mal setzen zum Testen.
        //pos.setUpVector(new Vector3(0, 1, 0));
        TestUtils.assertVector3(new Vector3(15 + radius + radius, 0, 0), pos.get3DPosition());
        TestUtils.assertQuaternion(new Degree(0), new Degree(90), new Degree(0), get3DRotation(graph, pos), "");

        // Ein kleines Stück in den Halbkreis einfahren
        gmc.moveForward(2 * radius + umfang / 20);
        Assertions.assertEquals(umfang / 20, pos.edgeposition, 0.0001, "position");
        // Die Refwerte sind experimentell ermittelt
        TestUtils.assertVector3(new Vector3(15 - 2.47214f, 0, -0.39154816f), pos.get3DPosition());
        TestUtils.assertQuaternion(new Degree(0), new Degree(71.99999f), new Degree(0), get3DRotation(graph, pos), "");

        // Ein Stück bis vor ganz links im Halbkreis 
        gmc.moveForward(umfang4 - 2 * (umfang / 20));
        Assertions.assertEquals(halbumfang - umfang4 - umfang / 20, pos.edgeposition, 0.00001, "position");
        // Die Refwerte sind experimentell ermittelt
        TestUtils.assertVector3(new Vector3(7.3915477f, 0, -5.527864f), pos.get3DPosition());
        TestUtils.assertQuaternion(new Degree(0), new Degree(18.000002f), new Degree(0), get3DRotation(graph, pos), "");

        // Noch en Stück bis ganz links im Halbkreis 
        gmc.moveForward(umfang / 20);
        Assertions.assertEquals(umfang4, pos.edgeposition, 0.00001, "position");
        // Die Refwerte sind experimentell ermittelt
        TestUtils.assertVector3(new Vector3(15 - radius, 0, -8f), pos.get3DPosition());
        TestUtils.assertQuaternion(new Degree(0), new Degree(0), new Degree(0), get3DRotation(graph, pos), "");
    }

    /**
     * Erzeugung von (Out)Lines.
     * <p>
     * Skizze 11
     */
    @Test
    public void testGraphOutline() {

        Graph graph = buildSkizze11();
        GraphNode zero = graph.findNodeByName("zero");
        GraphNode eins = graph.findNodeByName("eins");
        GraphEdge zeroeins = graph.findEdgeByName("zeroeins");
        float offset = -0.3f;
        List<Vector3> outline = graph.orientation.getOutlineFromNode(zero, offset);
        Assertions.assertEquals(9, outline.size());
        //TestUtil.assertVector3( zero.getLocation().add(new Vector3(1, 0, -1).normalize().multiply(offset)), outline.get(0));
        //TestUtil.assertVector3( eins.getLocation().add(new Vector3(1, 0, -1).normalize().multiply(offset)), outline.get(1));
        TestUtils.assertVector3(new Vector3(0.21213204f, 0, -0.21213204f), outline.get(0));
        TestUtils.assertVector3(new Vector3(2.1120955f, 0, 1.7878313f), outline.get(2), 0.1f);
        TestUtils.assertVector3(new Vector3(3.93f, 0, 1.7228361f), outline.get(3), 0.1f);
        TestUtils.assertVector3(new Vector3(5.9f, 0, 0.7696745f), outline.get(4), 0.1f);
        TestUtils.assertVector3(new Vector3(7.7f, 0, 0), outline.get(7), 0.1f);
        TestUtils.assertVector3(new Vector3(7.7f, 0, -1), outline.get(8), 0.1f);
        outline = graph.orientation.getOutlineFromNode(zero, -offset);
        Assertions.assertEquals(9, outline.size());
        // TestUtil.assertVector3( zero.getLocation().add(new Vector3(-1, 0, 1).normalize().multiply(offset)), outline.get(0));
        // TestUtil.assertVector3( eins.getLocation().add(new Vector3(-1, 0, 1).normalize().multiply(offset)), outline.get(1));
        TestUtils.assertVector3(new Vector3(-0.21213204f, 0, 0.21213204f), outline.get(0));
        TestUtils.assertVector3(new Vector3(1.9f, 0, 2.2121687f), outline.get(2), 0.1f);
        TestUtils.assertVector3(new Vector3(4.114805f, 0, 2.277164f), outline.get(3), 0.1f);
        TestUtils.assertVector3(new Vector3(6.1f, 0, 1.2303256f), outline.get(4), 0.1f);
        TestUtils.assertVector3(new Vector3(8.3f, 0, 0), outline.get(7));
        TestUtils.assertVector3(new Vector3(8.3f, 0, -1), outline.get(8));

        /*MA31 TODO woanders hin if (TerrainBuilder.useoutline) {
            CustomGeometry geo = TerrainBuilder.buildRoadGeometry(zeroeins, 0.2f, graph.orientation);
            TestUtil.assertEquals("vertices", 4, geo.vertices.size());
            TestUtil.assertVector3("v0", new Vector3(-0.07071067f, 0, 0.07071068f), geo.vertices.get(0));
            TestUtil.assertVector3("v1", new Vector3(0.9293015f, 0, 1.0707228f), geo.vertices.get(1));
            TestUtil.assertVector3("v2", new Vector3(1.0707228f, 0, 0.9293015f), geo.vertices.get(2));
            TestUtil.assertVector3("v3", new Vector3(0.07071067f, 0, -0.07071068f), geo.vertices.get(3));
        }*/
    }


    /**
     * Erzeugung von (Out)Line nur fuer einen einzelnen Bogen.
     * Skizze 11
     */
    @Test
    public void testGraphOutlineSimpleArc() {

        Graph graph = buildSkizze11();
        GraphNode zero = graph.findNodeByName("zero");
        GraphNode a = graph.findNodeByName("a");
        GraphNode b = graph.findNodeByName("b");
        GraphEdge zeroa = graph.findEdgeByName("zeroa");
        GraphEdge ab = graph.findEdgeByName("ab");

        // offset negativ, outline geht nach aussen.
        float offset = -0.2f;

        //erstmal ganz simpel einen Halbreis
        GraphEdge zerob = graph.connectNodes(zero, b, "");
        zerob.setArcAtFrom(new Vector3(0, 0, -2), 2, MathUtil2.PI, new Vector3(0, 1, 0));
        //zerob.arcParameter = new GraphArcParameter(zerob.getCenter(), zerob.radius,  0, zerob.from.getLocation(),  0, null, true, new Vector3(0,1,0),new Vector3(0,0,1),new Vector3 (0,0,-1),new Vector3(0,1,0));
        zerob.arcParameter = new GraphArc(zerob.getCenter(), zerob.getArc().getRadius(), new Vector3(0, 0, 1), new Vector3(0, 1, 0), -MathUtil2.PI);

        List<GraphPathSegment> path;
        List<Vector3> outline = graph.orientation.getOutline(buildListFromEdge(zerob.from, zerob), offset, -1);
        Assertions.assertEquals(17, outline.size());
        Assertions.assertEquals(-offset, Vector3.getDistance(outline.get(0), zerob.from.getLocation()), "distance.arcbeginloc");
        TestUtils.assertVector3(new Vector3(0f, 0, -offset), outline.get(0));
        TestUtils.assertVector3(new Vector3(0, 0, -4 + offset), outline.get(16));
        TestUtils.assertVector3(new Vector3(-2.2f, 0, -2), outline.get(8));

        float radius = 0.3f;

        // erstmal ein gerades Edge
        path = new ArrayList<GraphPathSegment>();
        path.add(new GraphPathSegment(zeroa, zeroa.from));
        outline = graph.orientation.getOutline(path, offset, -1);
        Assertions.assertEquals(2, outline.size());
        TestUtils.assertVector3(new Vector3(-0.14142136f, 0, 0.14142136f), outline.get(0));

        //jetzt der innenbogen an "a".
        GraphEdge arc = GraphUtils.addArcToAngleSimple(graph, zero, zeroa, a.getLocation(), ab, b, radius, true, false, 0, false);
        TestUtils.assertVector3(new Vector3(-1.5757361f, 0, -2), arc.arcParameter.arccenter, "center");
        TestUtils.assertVector3(new Vector3(-radius, 0, 0), arc.arcParameter.getRotatedEx(0.5f), "mid.er");
        Vector3 letztergeraderoutline = outline.get(1);
        // Verwendet immer 16 Segmente
        path = new ArrayList<GraphPathSegment>();
        path.add(new GraphPathSegment(arc, arc.from));
        //outline = graph.orientation.getOutline(arc.getFrom(),offset, arc.from, arc,null);
        outline = graph.orientation.getOutline(path, offset, -1);
        Assertions.assertEquals(17, outline.size());
        Assertions.assertEquals(-offset, Vector3.getDistance(outline.get(0), arc.from.getLocation()), 0.0001, "distance.arcbeginloc");
        //knifflig zu testen. 
        //TestUtil.assertVector3( letztergeraderoutline, outline.get(0));
        Assertions.assertTrue(outline.get(0).getX() < arc.from.getLocation().getX());
        TestUtils.assertVector3(new Vector3(-2.075736f, 0, -2), outline.get(8));
    }

    /**
     * Ein GraphSelector darf nicht wieder vorner beginnen, wenn er einmal abgefahren wurde.
     * Skizze 11
     */
    @Test
    public void testGraphSelectorAtEnd() {

        Graph graph = buildSkizze11();
        GraphNode zero = graph.findNodeByName("zero");
        GraphNode a = graph.findNodeByName("a");
        GraphNode b = graph.findNodeByName("b");
        GraphEdge zeroa = graph.findEdgeByName("zeroa");
        GraphEdge ab = graph.findEdgeByName("ab");
        int layer = 2;
        GraphEdge zeroeins = graph.findEdgeByName("zeroeins");
        GraphPosition start = new GraphPosition(zeroeins);
        GraphPathConstraintProvider graphPathConstraintProvider = new DefaultGraphPathConstraintProvider(0, 1.5);
        GraphPath path = GraphUtils.createPathFromGraphPosition(graph, start, b, null, graphPathConstraintProvider, layer, false, false, null);
        Assertions.assertEquals("eins:teardrop.smootharc->teardrop.branch(1)->zeroa(3)->ab(3)", path.toString(), "path");
        path = GraphUtils.createPathFromGraphPosition(graph, start, b, null, graphPathConstraintProvider, layer, true, false, null);
        Assertions.assertEquals("eins:teardrop.smootharc--ex-->smoothbegin.zero(1)--smootharcfrom-->smootharc@zero(1)--smootharcto-->smoothbegin.a(1)--smootharcfrom-->smootharc@a(2)--smootharcto-->smoothend.a(1)", path.getDetailedString(), "path");
        GraphMovingComponent gmc = new GraphMovingComponent(null);
        gmc.setGraph(graph, start, null);
        gmc.setPath(path);
        gmc.moveForward(18);
        //muss jetzt am Ende stehen, und zwar umgesetzt wieder auf layer 0.
        Assertions.assertEquals( "ab", gmc.getCurrentposition().currentedge.getName(),"currentposition.edge");
        Assertions.assertFalse(gmc.getCurrentposition().isReverseOrientation(), "currentposition.reverseorientation");
        Assertions.assertEquals(2.828427f, gmc.getCurrentposition().edgeposition, 0.00001,"currentposition.position");
        gmc.moveForward(15);
        //und da muss er jetzt immer noch stehen
        Assertions.assertEquals("ab", gmc.getCurrentposition().currentedge.getName(),"currentposition.edge");
        Assertions.assertFalse(gmc.getCurrentposition().isReverseOrientation(), "currentposition.reverseorientation");
        Assertions.assertEquals(2.828427f, gmc.getCurrentposition().edgeposition, 0.00001,"currentposition.position");
    }

    /**
     * Ein GraphSelector muss bei backward eine predecessor edge liefern.
     * Skizze 11
     */
    @Test
    public void testGraphSelectorBackward() {
        Graph graph = buildSkizze11();
        GraphNode zero = graph.findNodeByName("zero");
        GraphNode a = graph.findNodeByName("a");
        GraphNode b = graph.findNodeByName("b");
        GraphEdge zeroa = graph.findEdgeByName("zeroa");
        GraphEdge ab = graph.findEdgeByName("ab");
        int layer = 2;
        GraphEdge zeroeins = graph.findEdgeByName("zeroeins");
        GraphPosition start = new GraphPosition(zeroeins);
        GraphPathConstraintProvider graphPathConstraintProvider = new DefaultGraphPathConstraintProvider(0, 1.5);
        GraphPath path = GraphUtils.createPathFromGraphPosition(graph, start, b, null, graphPathConstraintProvider, layer, false, false, null);
        Assertions.assertEquals("eins:teardrop.smootharc->teardrop.branch(1)->zeroa(3)->ab(3)", path.toString(), "path");
        path = GraphUtils.createPathFromGraphPosition(graph, start, b, null, graphPathConstraintProvider, layer, true, false, null);
        Assertions.assertEquals("eins:teardrop.smootharc--ex-->smoothbegin.zero(1)--smootharcfrom-->smootharc@zero(1)--smootharcto-->smoothbegin.a(1)--smootharcfrom-->smootharc@a(2)--smootharcto-->smoothend.a(1)", path.getDetailedString(), "path");
        Assertions.assertEquals("smootharcfrom", path.getSegment(4).getEnterNode().getName(), "enternode darf nicht a sein");
        GraphMovingComponent gmc = new GraphMovingComponent(null);
        gmc.setGraph(graph, start, null);
        gmc.setPath(path);
        gmc.moveForward(7);
        //steht jetzt irgendwo auf dem Path, aber nicht am Ende
        gmc.moveForward(-8);
        //jetzt muss er wieder auf dem start des path stehen. Wobei das evtl. nicht sauber ist, er koennte doch auch weiter back. Aber nee, er hat ja den GraphSelector,
        //und der geht nicht weiter zurueck.
        Assertions.assertEquals("teardrop.smootharc", gmc.getCurrentposition().currentedge.getName(), "currentposition.edge");
        Assertions.assertTrue(gmc.getCurrentposition().isReverseOrientation(), "currentposition.reverseorientation");
        Assertions.assertEquals(1.3462735f, gmc.getCurrentposition().edgeposition,0.00001, "currentposition.position");
        gmc.moveForward(-8);
        //immer noch
        Assertions.assertEquals("teardrop.smootharc", gmc.getCurrentposition().currentedge.getName(), "currentposition.edge");
        Assertions.assertTrue(gmc.getCurrentposition().isReverseOrientation(), "currentposition.reverseorientation");
        Assertions.assertEquals(1.3462735f, gmc.getCurrentposition().edgeposition,0.00001, "currentposition.position");


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
        Graph graph = buildReturnKreis(radius);
        GraphEdge edgelinks = graph.getEdge(0);
        GraphEdge halbkreis = graph.getEdge(1);
        //positiver offset, outline innen (negatives z!)
        float offset = 0.3f;

        List<GraphPathSegment> path = new ArrayList<GraphPathSegment>();
        path.add(new GraphPathSegment(edgelinks, edgelinks.from));
        List<Vector3> outline = graph.orientation.getOutline(path, offset, -1);
        Assertions.assertEquals(2, outline.size());
        TestUtils.assertVector3(new Vector3(31, 0, -offset), outline.get(0));
        TestUtils.assertVector3(new Vector3(15, 0, -offset), outline.get(1));
        path = new ArrayList<GraphPathSegment>();
        path.add(new GraphPathSegment(halbkreis, halbkreis.from));
        //outline = graph.orientation.getOutline(halbkreis.getFrom(),offset, halbkreis.to, halbkreis,null);
        outline = graph.orientation.getOutline(path, offset, -1);
        Assertions.assertEquals(17, outline.size());
        TestUtils.assertVector3(new Vector3(15, 0, -offset), outline.get(0));
        TestUtils.assertVector3(new Vector3(15, 0, -15.7f), outline.get(16));
        //wieder zurueck. outline jetzt aussen.
        path = new ArrayList<GraphPathSegment>();
        path.add(new GraphPathSegment(halbkreis, halbkreis.to));
        //outline = graph.orientation.getOutline(halbkreis.getTo(),offset, halbkreis.from, halbkreis,null);
        outline = graph.orientation.getOutline(path, offset, -1);
        Assertions.assertEquals(17, outline.size());
        TestUtils.assertVector3(new Vector3(15, 0, -16.3f), outline.get(0));
        TestUtils.assertVector3(new Vector3(15, 0, offset), outline.get(16));

    }


    public static Graph buildSkizze11() {
        Graph graph = new Graph();
        //graph.upVector = new Vector3(0, 1, 0);
        GraphNode zero = graph.addNode("zero", new Vector3(0, 0, 0));
        GraphNode eins = graph.addNode("eins", new Vector3(1, 0, 1));
        GraphNode zwei = graph.addNode("zwei", new Vector3(2, 0, 2));
        GraphNode drei = graph.addNode("drei", new Vector3(4, 0, 2));
        GraphNode vier = graph.addNode("vier", new Vector3(6, 0, 1));
        GraphNode fuenf = graph.addNode("fuenf", new Vector3(7, 0, 1));
        GraphNode sechs = graph.addNode("sechs", new Vector3(8, 0, 1));
        GraphNode sieben = graph.addNode("sieben", new Vector3(8, 0, 0));
        GraphNode acht = graph.addNode("acht", new Vector3(8, 0, -1));
        GraphEdge zeroeins = graph.connectNodes(zero, eins, "zeroeins");
        graph.connectNodes(eins, zwei, "einszwei");
        graph.connectNodes(zwei, drei);
        graph.connectNodes(drei, vier, "dreivier");
        graph.connectNodes(vier, fuenf);
        graph.connectNodes(fuenf, sechs);
        graph.connectNodes(sechs, sieben);
        graph.connectNodes(sieben, acht);
        GraphNode a = graph.addNode("a", new Vector3(-2, 0, -2));
        GraphNode b = graph.addNode("b", new Vector3(0, 0, -4));
        graph.connectNodes(zero, a, "zeroa");
        graph.connectNodes(a, b, "ab");
        return graph;
    }

    /**
     * Graph mit zwei geraden Stücken an der y-Achse nach oben und dann nach links. (z0 Ebene wie OsmScene)
     */
    @Test
    public void testGraphPosition5() {
        Graph graph = new Graph();
        graph.orientation = GraphOrientation.buildForZ0();
        GraphNode unten = graph.addNode("", new Vector3(0, 0, 0));
        GraphNode mitte = graph.addNode("", new Vector3(0, 2, 0));
        GraphNode oben = graph.addNode("", new Vector3(0, 5, 0));
        GraphNode links = graph.addNode("", new Vector3(-3, 5, 0));
        GraphEdge edgeunten = graph.connectNodes(unten, mitte);
        GraphEdge edgeoben = graph.connectNodes(mitte, oben);
        GraphEdge edgelinks = graph.connectNodes(oben, links);
        GraphPosition pos = new GraphPosition(edgeunten);
        GraphMovingComponent gmc = new GraphMovingComponent();
        gmc.setGraph(graph, pos, null);

        LocalTransform posrot = GraphMovingSystem.getPosRot(gmc, null);
        //Die Rotation muss eine x 90 Grad sein
        TestUtils.assertVector3(new Vector3(0, 1, 0), new Vector3(0, 0, -1).rotate(posrot.rotation), "rotated reference");


        gmc.setSelector(new RandomGraphSelector(new DeterministicIntProvider(new int[]{1, 0, 0, 1, 0})));
        //pos.setUpVector(new Vector3(0, 0, 1));
        TestUtils.assertVector3(new Vector3(0, 0, 0), pos.get3DPosition());
        TestUtils.assertQuaternion(new Degree(90), new Degree(0), new Degree(0), get3DRotation(graph, pos), "");
        // auf das linke Stuck
        // in einem Schritt 
        gmc.moveForward(6.5f);
        TestUtils.assertVector3(new Vector3(-1.5f, 5, 0), pos.get3DPosition());
        //15.3.18: Durch die andere Berechnung kommt jetzt was anderes raus. Ist das wohl kompatibel? Scheint so.16.3.18: jetzt ist es wieder anders
        TestUtils.assertQuaternion(new Degree(0), new Degree(90), new Degree(90), get3DRotation(graph, pos), "");
        //TestUtil.assertQuaternion( new Degree(90), new Degree(-90), new Degree(0), pos.get3DRotation(graph,null/*new Vector3(0, 0, 1)*/));

    }

    /**
     * In der XZ Ebene mit upVector +y ist Rotation Identity
     */
    @Test
    public void testRotationXZ() {
        Graph graph = new Graph();
        //19.2.20 graph.orientation =  GraphOrientation.buildForZ0();
        graph.orientation = GraphOrientation.buildForY0();
        GraphNode n1 = graph.addNode("n1", new Vector3(0, 0, 3));
        GraphNode n2 = graph.addNode("n2", new Vector3(0, 0, -3));
        GraphEdge edge = graph.connectNodes(n1, n2);

        GraphPosition position = new GraphPosition(edge);
        //position.setUpVector(new Vector3(0, 1, 0));
        Quaternion rot = get3DRotation(graph, position);
        TestUtils.assertQuaternion(new Quaternion(), rot, "zup rotation");
    }

    /**
     * In der XY Ebene mit upVector +z ist der referencevector 90 Grad um x rotiert.
     */
    @Test
    public void testRotationXY() {
        Graph graph = new Graph();
        graph.orientation = GraphOrientation.buildForZ0();
        //graph.iszEbene = true;
        GraphNode n1 = graph.addNode("n1", new Vector3(0, 3, 0));
        GraphNode n2 = graph.addNode("n2", new Vector3(0, 7, 0));
        GraphEdge edge = graph.connectNodes(n1, n2);

        GraphPosition position = new GraphPosition(edge);
        //position.setUpVector(new Vector3(0, 0, 1));
        Quaternion rot = get3DRotation(graph, position);
        //29.3.18 TestUtil.assertQuaternion("zup rotation", Quaternion.buildRotationX(new Degree(90)), rot);
        TestUtils.assertQuaternion(Quaternion.buildRotationX(new Degree(90)), rot, "zup rotation");

        GraphNode n3 = graph.addNode("n3", new Vector3(4, 11, 0));
        GraphEdge arc = graph.connectNodes(n2, n3);
        arc.setArcAtFrom(new Vector3(4, 7, 0), 4, -MathUtil2.PI_2, new Vector3(0, 0, 1));

        // Direction im Bogen
        TestUtils.assertVector3(new Vector3(0, 1, 0), arc.getEffectiveDirection(0), "arc.dir.0");
        Assertions.assertEquals(-90f, (float) arc.getAngle().getDegree(), "arc.angle");

        // Ein Vehicle mit graphgleicher Orientierung braucht keine Rotation
        /*
        // Ein x-Achse Vehicle auf "Start"
         pitchvector = new Vector3(0,0,-1);
         rot = GraphMovingComponent.get3DRotation(0,position.upVector,false,
                 position.currentedge.getEffectiveDirection( 0),pitchvector);
        TestUtil.assertQuaternion("vehicle rotation", new Quaternion(new Degree(90),new Degree(0),new Degree(90)), rot);
*/
    }

    /**
     * Die quasi Defaultrotation im Graph.
     */
    @Test
    public void testIdentity() {


        Graph graph = new Graph();
        graph.orientation = GraphOrientation.buildDefault();
        GraphNode n1 = graph.addNode("n1", new Vector3(0, 0, 0));
        GraphNode n2 = graph.addNode("n2", new Vector3(0, 0, -1));
        //Die Referenzedge, bei der nicht rotiert wird.
        GraphEdge edge = graph.connectNodes(n1, n2);

        Quaternion rot = graph.orientation.get3DRotation(false, edge.getEffectiveBeginDirection(), edge/*,graph.orientation.orientation*/);
        TestUtils.assertQuaternion(new Quaternion(), rot, "identity rotation");

        Vector3 outpoint = graph.orientation.getEndOutlinePoint(edge.from, edge, edge.getEffectiveOutboundDirection(edge.from), 456);
        TestUtils.assertVector3(new Vector3(456, 0, 0), outpoint, "outline");
        outpoint = graph.orientation.getEndOutlinePoint(edge.from, edge, edge.getEffectiveOutboundDirection(edge.from), -456);
        TestUtils.assertVector3(new Vector3(-456, 0, 0), outpoint, "outline");
        outpoint = graph.orientation.getEndOutlinePoint(edge.to, edge, edge.getEffectiveEndDirection(), -456);
        TestUtils.assertVector3(new Vector3(-456, 0, -1), outpoint, "outline");
        outpoint = graph.orientation.getEndOutlinePoint(edge.to, edge, edge.getEffectiveEndDirection(), 456);
        TestUtils.assertVector3(new Vector3(456, 0, -1), outpoint, "outline");
    }

    /**
     * Ein gerades Stueck und dann ein "Zurückkreis". Skizze 12. Mit Extension.
     */
    @Test
    public void testPathSkizze12Ext() {
        float radius = 8;
        float umfang = 2 * (float) (Math.PI * radius);
        float halbumfang = (float) (Math.PI * radius);
        float umfang4 = umfang / 4;
        float umfang8 = umfang / 8;

        Graph graph = GraphFactory.buildReturnKreis(radius, true);
        GraphEdge edgelinks = graph.getEdge(0);
        GraphEdge halbkreis = graph.getEdge(1);
        GraphNode start = graph.getNode(0);
        GraphNode oben = graph.getNode(2);
        GraphEdge closing = graph.findEdgeByName("closing");

        GraphMovingComponent gmc = new GraphMovingComponent();
        gmc.setGraph(null, new GraphPosition(edgelinks), null);
        // nicht den kurzen Pfad nehmen
        DefaultGraphWeightProvider gwp = new DefaultGraphWeightProvider(graph, new GraphEdge[]{closing});
        GraphPath path = graph.findPath(start, oben, gwp);
        Assertions.assertEquals(16 + halbumfang, path.getLength(null), 0.00001);
        Assertions.assertEquals(16 + halbumfang, path.getLength(gmc.getCurrentposition()), 0.00001);
        gmc.setPath(path);
        gmc.moveForward(5);
        Assertions.assertEquals(16 + halbumfang - 5, path.getLength(gmc.getCurrentposition()), 0.00001);
        gmc.moveForward(12);
        Assertions.assertEquals(halbumfang - 1, path.getLength(gmc.getCurrentposition()), 0.00001);
        gmc.moveForward(2000);
        Assertions.assertEquals(0, path.getLength(gmc.getCurrentposition()));
        gmc.moveForward(2);
        Assertions.assertEquals(0, path.getLength(gmc.getCurrentposition()));

    }

    @Test
    public void testAngle() {
        Graph graph = new Graph();
        GraphNode n1 = graph.addNode("n1", new Vector3(0, 0, 0));
        GraphNode n2 = graph.addNode("n2", new Vector3(0, 3, 0));
        GraphNode n3 = graph.addNode("n3", new Vector3(3, 3, 0));
        GraphNode n4 = graph.addNode("n4", new Vector3(3, 0, 0));
        GraphNode n5 = graph.addNode("n5", new Vector3(0, 6, 0));
        GraphEdge e1 = graph.connectNodes(n1, n2);
        GraphEdge e2 = graph.connectNodes(n2, n3);
        GraphEdge e3 = graph.connectNodes(n4, n2);
        GraphEdge e4 = graph.connectNodes(n5, n1);

        Assertions.assertEquals(MathUtil2.PI_2, GraphEdge.getAngleBetweenEdges(e1, n2, e2), "angle 90");
        Assertions.assertEquals(MathUtil2.PI * 0.75f, GraphEdge.getAngleBetweenEdges(e1, n2, e3), "angle 135");
        Assertions.assertEquals(0, GraphEdge.getAngleBetweenEdges(e1, n2, e4), "angle 0");
    }

    /**
     * Mit den Werten ging es mal nicht (wegen fehlender Normalisierung).
     */
    @Test
    public void testGraphArc() {
        Vector3 arccenter = new Vector3(15, 0, -8);
        float radius = 8;
        Vector3 ex = new Vector3(0, 0, 8);
        Vector3 ey = new Vector3(0, 0, -8);
        Vector3 n = new Vector3(0, 1, 0);
        // das wird ein Halbkreis, der von unten rechts rum CCW nach oben geht.
        GraphArc arc = new GraphArc(arccenter, radius, ex, n, MathUtil2.PI);
        Vector3 rotated = arc.getRotatedEx(0.1f);
        //15.3.18: von -2.4 auf positiv
        TestUtils.assertVector3(new Vector3(2.47214f, 0, 7.608451f), rotated, "rotated");
    }

    /**
     * Wie in GroundServicesScene.
     * <p>
     * Skizze 11b
     */
    @Test
    public void testOutlineInZ0() {
        float radius = 8;
        Graph graph = new Graph(GraphOrientation.buildForZ0());
        GraphNode n1 = graph.addNode("n1", new Vector3(-2000, 3000, 0));
        GraphNode n2 = graph.addNode("n2", new Vector3(-2100, 3100, 0));
        GraphEdge edge = graph.connectNodes(n1, n2);
        float offset = 30f;

        List<Vector3> outline = graph.orientation.getOutline(buildListFromEdge(edge.from, edge), offset, -1);
        Assertions.assertEquals(2, outline.size());
        TestUtils.assertVector3(new Vector3(-1978.7867f, 3021.2131f, 0), outline.get(0));
        TestUtils.assertVector3(new Vector3(-2078.7869f, 3121.2131f, 0), outline.get(1));

        GraphNode n3 = graph.addNode("n3", new Vector3(-2200, 3100, 0));
        GraphEdge edgel = graph.connectNodes(n2, n3);

        outline = graph.orientation.getOutline(buildListFromEdge(edge.from, edge, edgel), offset, -1);
        Assertions.assertEquals(3, outline.size());
        TestUtils.assertVector3(new Vector3(-1978, 3021, 0), outline.get(0), 1);
        TestUtils.assertVector3(new Vector3(-2088, 3127, 0), outline.get(1), 1);
        TestUtils.assertVector3(new Vector3(-2200, 3130, 0), outline.get(2), 1);

        offset = -30;
        outline = graph.orientation.getOutline(buildListFromEdge(edge.from, edge, edgel), offset, -1);
        Assertions.assertEquals(3, outline.size());
        TestUtils.assertVector3(new Vector3(-2021, 2978, 0), outline.get(0), 1);
        TestUtils.assertVector3(new Vector3(-2111, 3072, 0), outline.get(1), 1);
        TestUtils.assertVector3(new Vector3(-2200, 3070, 0), outline.get(2), 1);

    }

    /**
     * Wie in GroundServicesScene.
     * <p>
     * Skizze 11c
     */
    @Test
    public void testOutlineInZ0_Smaller() {
        float radius = 8;
        Graph graph = new Graph(GraphOrientation.buildForZ0());
        GraphNode n1 = graph.addNode("n1", new Vector3(20, 30, 0));
        GraphNode n2 = graph.addNode("n2", new Vector3(25, 25, 0));
        GraphEdge edge = graph.connectNodes(n1, n2);
        float offset = 2f;
        GraphNode n3 = graph.addNode("n3", new Vector3(25, 30, 0));
        GraphEdge edgel = graph.connectNodes(n3, n2);
        GraphNode n4 = graph.addNode("n4", new Vector3(25, 35, 0));
        GraphEdge edge4 = graph.connectNodes(n3, n4);
        GraphNode n5 = graph.addNode("n5", new Vector3(30, 40, 0));
        GraphEdge edge5 = graph.connectNodes(n4, n5);
        GraphNode n6 = graph.addNode("n6", new Vector3(35, 45, 0));
        GraphEdge edge6 = graph.connectNodes(n5, n6);
        GraphNode n7 = graph.addNode("n7", new Vector3(40, 40, 0));
        GraphEdge edge7 = graph.connectNodes(n6, n7);
        GraphNode n8 = graph.addNode("n8", new Vector3(45, 35, 0));
        GraphEdge edge8 = graph.connectNodes(n7, n8);

        GraphPath path = new GraphPath(0);
        path.addSegment(new GraphPathSegment(edge, n1));
        path.addSegment(new GraphPathSegment(edgel, n2));
        path.addSegment(new GraphPathSegment(edge4, n3));
        path.addSegment(new GraphPathSegment(edge5, n4));
        path.addSegment(new GraphPathSegment(edge6, n5));
        path.addSegment(new GraphPathSegment(edge7, n6));
        path.addSegment(new GraphPathSegment(edge8, n7));
        List<Vector3> outline = graph.orientation.getOutline(path.path, offset, -1);
        Assertions.assertEquals(8, outline.size());
        TestUtils.assertVector3(new Vector3(18, 28, 0), outline.get(0), 1);
        TestUtils.assertVector3(new Vector3(25.7f, 23.5f, 0), outline.get(1), 1);
        TestUtils.assertVector3(new Vector3(27, 30, 0), outline.get(2));
        TestUtils.assertVector3(new Vector3(26.8f, 34.2f, 0), outline.get(3), 0.1f);

        offset = -2;
        outline = graph.orientation.getOutline(path.path, offset, -1);
        Assertions.assertEquals(8, outline.size());
        TestUtils.assertVector3(new Vector3(21.4f, 31, 0), outline.get(0), 1);
        TestUtils.assertVector3(new Vector3(24f, 26, 0), outline.get(1), 1);
        TestUtils.assertVector3(new Vector3(23, 30, 0), outline.get(2));
        TestUtils.assertVector3(new Vector3(23.1f, 35.8f, 0), outline.get(3), 0.1f);

    }

    /**
     * Skizze 25
     */
    @Test
    public void testCircleWithStart() {
        Graph graph = new Graph(GraphOrientation.buildForZ0());
        graph.addNode("", new Vector3(0, 0, 5));
        doTestCircle(graph, 1);
    }

    /**
     * Skizze 25
     */
    @Test
    public void testCircleWithoutStart() {
        Graph graph = new Graph(GraphOrientation.buildForZ0());
        doTestCircle(graph, 0);
    }

    /**
     * Skizze 25
     */
    void doTestCircle(Graph graph, int offset) {
        int x = -3;
        int y = 1;
        Vector3 startPoint = new Vector3(x, y, 0);
        GraphFactory.addZ0Circle(graph, startPoint, "e");
        Assertions.assertEquals(offset + 4, graph.getNodeCount(), "circle.nodes");
        Assertions.assertEquals(offset + 4, graph.getEdgeCount(), "circle.edges");
        TestUtils.assertVector3(new Vector3(x, y, 0), graph.getNode(offset + 0).getLocation(), "circle.0");
        TestUtils.assertVector3(new Vector3(-y, x, 0), graph.getNode(offset + 1).getLocation(), "circle.1");
        TestUtils.assertVector3(new Vector3(-x, -y, 0), graph.getNode(offset + 2).getLocation(), "circle.2");
        TestUtils.assertVector3(new Vector3(y, -x, 0), graph.getNode(offset + 3).getLocation(), "circle.3");

        GraphEdge edge0_1 = graph.getEdge(offset);
        TestUtils.assertVector3(new Vector3(x, y, 0), edge0_1.from.getLocation(), "edge0.from");
        TestUtils.assertVector3(new Vector3(-y, x, 0), edge0_1.to.getLocation(), "edge0.to");
        Vector3 position = edge0_1.get3DPosition(0.5);
        TestUtils.assertVector3(new Vector3(-3.12003395, 0.515158333, 0), position, "edge0.position");

        GraphEdge edge1_2 = graph.getEdge(offset + 1);
        TestUtils.assertVector3(new Vector3(-y, x, 0), edge1_2.from.getLocation(), "edge1_2.from");
        TestUtils.assertVector3(new Vector3(-x, -y, 0), edge1_2.to.getLocation(), "edge1_2.to");
        position = edge1_2.get3DPosition(4.5);
        //Werte sind plausibel
        TestUtils.assertVector3(new Vector3(2.82007078, -1.430804234, 0), position, "edge1_2.position");
    }

    /**
     * Eins vor, drei zurück und dann wieder 7 vor.
     * isReverseOrientation kann hier nicht fix geprueft werden, weil es in verschiedenene Tests unterschiedlich ist.
     *
     * @param gmc
     */
    private void move(GraphMovingComponent gmc) {
        Assertions.assertEquals(0, gmc.getCurrentposition().edgeposition, "position");
        gmc.moveForward(1);
        Assertions.assertEquals(1, gmc.getCurrentposition().edgeposition, "position");
        gmc.moveForward(-3);
        Assertions.assertEquals(33 - 20 - 2, gmc.getCurrentposition().edgeposition, "position");
        gmc.moveForward(7);
        Assertions.assertEquals(5, gmc.getCurrentposition().edgeposition, "position");
    }

    /**
     * Skizze 12
     *
     * @param radius
     * @return
     */
    public static Graph buildReturnKreis(float radius) {

        float halbumfang = (float) (Math.PI * radius);
        Graph graph = GraphFactory.buildReturnKreis(radius, false);

        GraphEdge halbkreis = graph.findEdgeByName("firsthalbkreis");
        Assertions.assertEquals(-180, (float) halbkreis.getAngle().getDegree(), "halbkreis.angle");
        GraphEdge closing = graph.findEdgeByName("closing");
        Assertions.assertEquals(90, (float) closing.getAngle().getDegree(), "closing.angle");
        // Vor einer Bewegung erstmal die erste Kante testen
        Assertions.assertEquals(halbumfang, halbkreis.getLength(), "edge.len");
        Assertions.assertEquals(-180, (float) halbkreis.getAngle().getDegree(), "edge.angle");
        TestUtils.assertVector3(new Vector3(-1, 0, 0), halbkreis.getEffectiveDirection(0), "halbkreis.dir.0");
        TestUtils.assertVector3(new Vector3(0, 0, -1), halbkreis.getEffectiveDirection(halbkreis.getLength() / 2), "halbkreis.dir.90");
        TestUtils.assertVector3(new Vector3(1, 0, 0), halbkreis.getEffectiveDirection(halbkreis.getLength()), "halbkreis.dir.180");
        TestUtils.assertVector3(new Vector3(0, 0, 1), closing.getEffectiveDirection(0), "closing.dir.0");

        // und bei der Gelegenheit auch den Selector testen
        RailingBranchSelector selector = new RailingBranchSelector();
        GraphEdge edgelinks = graph.findEdgeByName("getFirst");
        Assertions.assertEquals("firsthalbkreis", selector.findNextEdgeAtNode(edgelinks, edgelinks.to).edge.getName(), "selector");
        Assertions.assertEquals("first4", selector.findNextEdgeAtNode(halbkreis, halbkreis.to).edge.getName(), "selector");
        Assertions.assertNull(selector.findNextEdgeAtNode(closing, closing.to), "selector");
        return graph;
    }


    /**
     * Aus GraphPosition hierhin verschoben, weil es nur in Tests verwendet wird und auch fragwürdigen Kontext dort hat.
     *
     * @return
     */
    /*19.4.17 eigentlich keine Sache des Graphen?? Und auf was beziht sich diese Rotation? Bei Railing wohl -z*/
    public Quaternion get3DRotation(Graph graph, GraphPosition position) {
        return graph.orientation.get3DRotation(position.reverseorientation, position.currentedge.getEffectiveDirection(position.getAbsolutePosition()), position.currentedge/*,graph.orientation.orientation*/);
        //return currentedge.get3DRotation(edgeposition, upVector,reverseorientation);
    }

    /**
     * driss
     *
     * @param from
     * @param edge
     * @param edge1
     * @return
     */
    @Deprecated
    public static List<GraphPathSegment> buildListFromEdge(GraphNode from, GraphEdge edge, GraphEdge edge1) {
        List<GraphPathSegment> path = buildListFromEdge(from, edge);
        path.add(new GraphPathSegment(edge1, edge.getOppositeNode(from)));
        return path;
    }

    public static List<GraphPathSegment> buildListFromEdge(GraphNode from, GraphEdge edge) {
        List<GraphPathSegment> path = new ArrayList<GraphPathSegment>();
        path.add(new GraphPathSegment(edge, from));
        return path;
    }
}
