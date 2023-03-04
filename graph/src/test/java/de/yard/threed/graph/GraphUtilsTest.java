package de.yard.threed.graph;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.CustomGeometry;
import de.yard.threed.core.Vector3;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.core.MathUtil2;
import de.yard.threed.core.testutil.RuntimeTestUtil;
import de.yard.threed.engine.util.Bezier;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static de.yard.threed.core.testutil.Assert.assertEquals;

/**
 * <p>
 * <p>
 * Created by thomass on 13.03.17.
 */
public class GraphUtilsTest {
    //static Platform platform = TestFactory.initPlatformForTest(false, new String[]{},false);
    static Platform platform = EngineTestFactory.initPlatformForTest( new String[] {"engine","data"/*,"data-old","railing"*/}, new SimpleHeadlessPlatformFactory());

    /**
     * Skizze 29
     */
    @Test
    public void testAlternateRouteTrivial() {
        Graph graph = new Graph();
        //graph.iszEbene = true;
        GraphNode start = graph.addNode("n1", new Vector3(0, 0, 0));
        GraphNode mid = graph.addNode("n2", new Vector3(0, 6, 0));
        GraphEdge e1 = graph.connectNodes(start, mid);
        GraphNode end = graph.addNode("n3", new Vector3(9, 6, 0));
        GraphEdge e2 = graph.connectNodes(mid, end);
        float radius = 3;
        GraphEdge arc = GraphUtils.addAlternateRouteByArc(graph, start, e1, mid, e2, end, radius, 0);
        RuntimeTestUtil.assertFloat("arc.angle", 90f, (float) arc.getAngle().getDegree());
        RuntimeTestUtil.assertVector3("arc.center", new Vector3(3, 3, 0), arc.getCenter());
        // Zwar falsches Testthema, da ich aber gerade dabei bin.
        CustomGeometry geo = GraphFactory.buildGraphGeometry(arc, 0.5f, 2);
        RuntimeTestUtil.assertVector3("erster vertex", new Vector3(0.25f, 6 - radius, 0), geo.getVertices().get(0));
        RuntimeTestUtil.assertVector3("zweiter vertex", new Vector3(-0.25f, 6 - radius, 0), geo.getVertices().get(1));
        RuntimeTestUtil.assertVector3("5.vertex", new Vector3(radius, 6 - 0.25f, 0), geo.getVertices().get(4));
        RuntimeTestUtil.assertVector3("6.vertex", new Vector3(radius, 6 + 0.25f, 0), geo.getVertices().get(5));

        // Und jetzt mit y nach links
        graph = new Graph();
        //graph.iszEbene = true;
        start = graph.addNode("n1", new Vector3(0, 0, 0));
        mid = graph.addNode("n2", new Vector3(0, 6, 0));
        e1 = graph.connectNodes(start, mid);
        end = graph.addNode("n3", new Vector3(-9, 6, 0));
        e2 = graph.connectNodes(mid, end);

        arc = GraphUtils.addAlternateRouteByArc(graph, start, e1, mid, e2, end, 3, 0);
        RuntimeTestUtil.assertVector3("arc.center", new Vector3(-3, 3, 0), arc.getCenter());
        RuntimeTestUtil.assertFloat("arc.angle", 90f, (float) arc.getAngle().getDegree());

        // Zwar falsches Testthema, da ich aber gerade dabei bin.
        geo = GraphFactory.buildGraphGeometry(arc, 0.5f, 2);
        RuntimeTestUtil.assertVector3("erster vertex", new Vector3(-0.25f, 6 - radius, 0), geo.getVertices().get(0));
        RuntimeTestUtil.assertVector3("zweiter vertex", new Vector3(0.25f, 6 - radius, 0), geo.getVertices().get(1));
        RuntimeTestUtil.assertVector3("5.vertex", new Vector3(-radius, 6 - 0.25f, 0), geo.getVertices().get(4));
        RuntimeTestUtil.assertVector3("6.vertex", new Vector3(-radius, 6 + 0.25f, 0), geo.getVertices().get(5));

    }

    /**
     * Skizze 29
     */
    @Test
    public void testAlternateRoute() {
        Graph graph = new Graph();
        //graph.iszEbene = true;
        GraphNode start = graph.addNode("n1", new Vector3(2, 3, 0));
        GraphNode mid = graph.addNode("n2", new Vector3(4, 6, 0));
        GraphEdge e1 = graph.connectNodes(start, mid);
        GraphNode end = graph.addNode("n3", new Vector3(9, 6, 0));
        GraphEdge e2 = graph.connectNodes(mid, end);

        GraphEdge arc = GraphUtils.addAlternateRouteByArc(graph, start, e1, mid, e2, end, 3, 0);
        //y zufaellig genau 3?
        RuntimeTestUtil.assertVector3("arc.center", new Vector3(5.6055512f, 3, 0), arc.getCenter());
        RuntimeTestUtil.assertFloat("arc.angle", 56.30994f, (float) arc.getAngle().getDegree());
// Zwar falsches Testthema, da ich aber gerade dabei bin.
        CustomGeometry geo = GraphFactory.buildGraphGeometry(arc, 0.5f, 2);
        //Refwerte durch ausprobieren
        RuntimeTestUtil.assertVector3("erster vertex", new Vector3(3.3174129f, 4.5254254f, 0), geo.getVertices().get(0));
        RuntimeTestUtil.assertVector3("zweiter vertex", new Vector3(2.9013877f, 4.8027754f, 0), geo.getVertices().get(1));

        // gleich Anordnung, aber andere Richtung. Muss dasselbe Ergebnis bringen, aber Winkel positiv

        arc = GraphUtils.addAlternateRouteByArc(graph, end, e2, mid, e1, start, 3, 0);
        //y zufaellig genau 3?
        RuntimeTestUtil.assertVector3("arc.center", new Vector3(5.6055512f, 3, 0), arc.getCenter());
        RuntimeTestUtil.assertFloat("arc.angle", 56.30994f, (float) arc.getAngle().getDegree());
        geo = GraphFactory.buildGraphGeometry(arc, 0.5f, 2);
        //TestUtil.assertVector3("erster vertex", new Vector3(-0.25f, 6 - radius, 0), geo.getVertices().getElement(0));
        //TestUtil.assertVector3("zweiter vertex", new Vector3(0.25f, 6 - radius, 0), geo.getVertices().getElement(1));
        RuntimeTestUtil.assertVector3("5.vertex", new Vector3(3.3174129f, 4.5254254f, 0), geo.getVertices().get(4));
        RuntimeTestUtil.assertVector3("6.vertex", new Vector3(2.9013877f, 4.8027754f, 0), geo.getVertices().get(5));

    }

    /**
     * Skizze 29b
     */
    @Test
    public void testTransitionRoute29b() {
        Graph graph = new Graph();
        //graph.iszEbene = true;
        GraphNode start = graph.addNode("n1", new Vector3(0, 0, 0));
        GraphNode mid = graph.addNode("n2", new Vector3(10, 3, 0));
        GraphEdge e1 = graph.connectNodes(start, mid);
        GraphNode end = graph.addNode("n3", new Vector3(-10, 9, 0));
        GraphEdge e2 = graph.connectNodes(mid, end);

        GraphEdge arc = GraphUtils.addAlternateRouteByArc(graph, start, e1, mid, e2, end, 2f, 0);
        RuntimeTestUtil.assertVector3("arc.center", new Vector3(3.039798f, 3, 0), arc.getCenter());
        RuntimeTestUtil.assertFloat("arc.angle", 146.6015f, (float) arc.getAngle().getDegree());
        // Zwar falsches Testthema, da ich aber gerade dabei bin.
        CustomGeometry geo = GraphFactory.buildGraphGeometry(arc, 0.5f, 2);
    }

    /**
     * Skizze 29b
     */
    @Test
    public void testTransisitonRoute29bOuter() {
        Graph graph = new Graph();
        //graph.iszEbene = true;
        GraphNode start = graph.addNode("n1", new Vector3(0, 0, 0));
        GraphNode mid = graph.addNode("n2", new Vector3(10, 3, 0));
        GraphEdge e1 = graph.connectNodes(start, mid);
        GraphNode end = graph.addNode("n3", new Vector3(-10, 9, 0));
        GraphEdge e2 = graph.connectNodes(mid, end);

        GraphEdge arc = GraphUtils.addArcToAngleSimple(graph, start, e1, mid.getLocation(), e2, end, 2f, false, false, 0,false);
        RuntimeTestUtil.assertVector3("arc.center", new Vector3(3.039798f, 3, 0), arc.getCenter());
        RuntimeTestUtil.assertFloat("arc.angle", 146.6015f - 360, (float) arc.getAngle().getDegree());

    }

    /**
     * ähnlich Skizze 29b, aber ohne zusätzliche Nodes.
     */
    @Test
    public void testConnectOuter() {
        testConnectOuter(false, false, false);
        //Mit anderer edge Orientierung
        testConnectOuter(true, false, false);
        testConnectOuter(false, true, false);
        testConnectOuter(true, true, false);
        testConnectOuter(false, false, true);
        testConnectOuter(true, false, true);
        testConnectOuter(false, true, true);
        testConnectOuter(true, true, true);

    }

    /**
     * Ein gerades Stueck und dann ein "Zurückkreis". Skizze 12. Mit Extension.
     */
    @Test
    public void testfindPath() {
        float radius = 8;
        Graph graph = GraphFactory.buildReturnKreis(radius, true);
        GraphEdge first = graph.findEdgeByName("getFirst");
        GraphEdge extension = graph.findEdgeByName("extension");
        GraphPosition pos = new GraphPosition(first, 0);
        // zum turnen kommt ein teardrop dazu. 24.5.17: aber jetzt ohne eine smooted node im graph. Nur im path.
        GraphPathConstraintProvider graphPathConstraintProvider = new DefaultGraphPathConstraintProvider(0, 0.5);

        GraphPath path = GraphUtils.createPathFromGraphPosition(graph, pos, extension.to, null, graphPathConstraintProvider, 1, true, false, null);
        RuntimeTestUtil.assertEquals("path.segments", 4, path.getSegmentCount());
        // Warum smooth begin/end in genau dieser Richtung sind ist unklar, aber eigentlich egal.24.5.17:Mit transition ist es wie erwartet. aber es fehlen noch namen
        RuntimeTestUtil.assertEquals("path.segments.name0", "teardrop.smootharc", path.getSegment(0).edge.getName());
        RuntimeTestUtil.assertEquals("path.segments.name1", "smoothbegin.start", path.getSegment(1).edge.getName());
        RuntimeTestUtil.assertEquals("path.segments.name2", "smootharc", path.getSegment(2).edge.getName());
        RuntimeTestUtil.assertEquals("path.segments.name3", "smoothend.start", path.getSegment(3).edge.getName());
        RuntimeTestUtil.assertEquals("path.destinationnode", extension.to.getName(), path.getSegment(3).edge.getTo().getName());

    }

    /**
     * Ein gerades Stueck und dann ein "Zurückkreis". Skizze 12. Mit Extension.
     */
    @Test
    public void testBackwards() {
        float radius = 8;
        Graph graph = GraphFactory.buildReturnKreis(radius, true);
        GraphEdge first = graph.findEdgeByName("getFirst");
        GraphEdge extension = graph.findEdgeByName("extension");
        GraphPosition start = new GraphPosition(extension, extension.getLength(), true);

        // Back ist hier entartet, weil es exakt gerade weitergeht.
        TurnExtension turn = GraphUtils.createBack(graph, extension.to, extension, first, 1);
        RuntimeTestUtil.assertVector3("", extension.to.getLocation(), turn.edge.to.getLocation());
        RuntimeTestUtil.assertVector3("", extension.to.getLocation(), turn.arc.getCenter());
    }


    /**
     * Skizze 29d/29e
     */
    @Test
    public void testTransisiton29d() {
        Graph graph = new Graph();
        //graph.iszEbene = true;
        GraphNode n1 = graph.addNode("n1", new Vector3(0, 0, 0));
        GraphNode n2 = graph.addNode("n2", new Vector3(0, 5, 0));
        GraphEdge e = graph.connectNodes(n1, n2);
        e.setName("e");
        GraphPosition position = new GraphPosition(e, 3);
        testTransisiton29d(graph, n1, n2, e, position);
        e = graph.connectNodes(n2, n1);
        e.setName("e");
        position = new GraphPosition(e, 1, true);
        testTransisiton29d(graph, n1, n2, e, position);
    }

    @Test
    public void testfindWithOnlyVoid() {
        Graph graph = new Graph();
        //graph.iszEbene = true;
        GraphNode n1 = graph.addNode("n1", new Vector3(0, 0, 0));
        GraphNode n2 = graph.addNode("n2", new Vector3(0, 5, 0));
        GraphEdge e = graph.connectNodes(n1, n2, "e", 1);
        GraphWeightProvider gwp = new DefaultGraphWeightProvider(graph, 0);
        GraphPath path = graph.findPath(n1, n2, gwp);
        RuntimeTestUtil.assertNull("path", path);
    }

    /**
     * Skizze 29d/29e mit verschiedenen Orientierungen der startedge.
     */
    private void testTransisiton29d(Graph graph, GraphNode n1, GraphNode n2, GraphEdge e, GraphPosition position) {
        float radius = 2.2f;//5 Cent muenze auf Rechenraster

        // 1a
        GraphNode n3 = graph.addNode("n3", new Vector3(2, 6, 0));
        GraphNode n4 = graph.addNode("n4", new Vector3(8, 12, 0));
        GraphEdge e1a = graph.connectNodes(n3, n4);

        Vector3 intersection = GraphUtils.getIntersection(e, e1a);
        RuntimeTestUtil.assertNotNull("intersection", intersection);
        RuntimeTestUtil.assertVector3("intersection", new Vector3(0, 4, 0), intersection);

        GraphArcParameter arcpara = GraphUtils.calcArcParameter(n1, e, intersection, e1a, n4, radius, true, false);
        RuntimeTestUtil.assertNotNull("arcpara", arcpara);
        RuntimeTestUtil.assertVector3("arcpara.arccenter", new Vector3(radius, 3.08873f, 0), arcpara.arccenter);
        RuntimeTestUtil.assertEquals("distancefromintersection", 0.91126996f, arcpara.distancefromintersection);
        GraphPathConstraintProvider graphPathConstraintProvider = new DefaultGraphPathConstraintProvider(0, radius);
        GraphTransition transition = GraphUtils.createTransition(graph, position, e1a, n4, graphPathConstraintProvider, 233);
        RuntimeTestUtil.assertVector3("transition.arc.center", new Vector3(radius, 3.08873f, 0), transition.seg.get(1).edge.getCenter());

        // 4d
        n3 = graph.addNode("n3", new Vector3(3, 1, 0));
        n4 = graph.addNode("n4", new Vector3(7, -1, 0));
        GraphEdge e4d = graph.connectNodes(n3, n4);
        intersection = GraphUtils.getIntersection(e, e4d);
        RuntimeTestUtil.assertNotNull("intersection", intersection);
        RuntimeTestUtil.assertVector3("intersection", new Vector3(0, 2.5f, 0), intersection);

        arcpara = GraphUtils.calcArcParameter(n1, e, intersection, e4d, n4, radius, false, false);
        RuntimeTestUtil.assertNotNull("arcpara", arcpara);
        RuntimeTestUtil.assertVector3("arcpara.arccenter", new Vector3(radius, -1.0596745f, 0), arcpara.arccenter);
        RuntimeTestUtil.assertEquals("distancefromintersection", 3.5596745f, arcpara.distancefromintersection);
        
        transition = GraphUtils.createTransition(graph, position, e4d, n4, graphPathConstraintProvider, 233);
        //??TestUtil.assertVector3("transition.arc.center", new Vector3(radius, 3.08873f, 0), transition.arc.getCenter());

        // 5e, wie eine A20 turnloop
        n3 = graph.addNode("n3", new Vector3(0, 5, 0));
        n4 = graph.addNode("n4", new Vector3(-2, -2, 0));
        GraphEdge e5e = graph.connectNodes(n3, n4);
        intersection = GraphUtils.getIntersection(e, e5e);
        RuntimeTestUtil.assertNotNull("intersection", intersection);
        RuntimeTestUtil.assertVector3("intersection", new Vector3(0, 5f, 0), intersection);

        transition = GraphUtils.createTransition(graph, position, e4d, n4, graphPathConstraintProvider, 233);
        //Werte wegen turnloop extend 20
        RuntimeTestUtil.assertVector3("transition.arc.center", new Vector3(32.360676f, 25, 0), transition.seg.get(1).edge.getCenter());

        // 6f, (mit Ziel weit genug weg.)
        n3 = graph.addNode("n3", new Vector3(-1, 6, 0));
        n4 = graph.addNode("n4", new Vector3(3, 12, 0));
        GraphEdge e6f = graph.connectNodes(n3, n4);
        intersection = GraphUtils.getIntersection(e, e6f);
        RuntimeTestUtil.assertNotNull("intersection", intersection);
        RuntimeTestUtil.assertVector3("intersection", new Vector3(0, 7.5f, 0), intersection);

        transition = GraphUtils.createTransition(graph, position, e6f, n4, graphPathConstraintProvider, 233);
        RuntimeTestUtil.assertVector3("transition.arc.center", new Vector3(2.2f, 6.833894f, 0), transition.seg.get(1).edge.getCenter());

    }


    private void testConnectOuter(boolean reversee1, boolean reversee2, boolean startendreverse) {
        Graph graph = new Graph();
        //graph.iszEbene = true;
        GraphNode start = graph.addNode("n1", new Vector3(0, 0, 0));
        GraphNode mid = graph.addNode("n2", new Vector3(10, 3, 0));
        GraphNode end = graph.addNode("n3", new Vector3(0, 6, 0));
        if (startendreverse) {
            GraphNode t = end;
            end = start;
            start = t;
        }
        GraphEdge e1;
        if (reversee1) {
            e1 = graph.connectNodes(mid, start);
        } else {
            e1 = graph.connectNodes(start, mid);
        }
        GraphEdge e2;
        if (reversee2) {
            e2 = graph.connectNodes(end, mid);
        } else {
            e2 = graph.connectNodes(mid, end);
        }
        GraphEdge arc = GraphUtils.addArcToAngleSimple(graph, start, e1, mid.getLocation(), e2, end, e1.getLength(), false, true, 0,false);

        RuntimeTestUtil.assertVector3("arc.center", new Vector3(-0.9f, 3, 0), arc.getCenter());
        RuntimeTestUtil.assertVector3("arc.n", new Vector3(0, 0, ((startendreverse) ? -1 : 1)), arc.arcParameter.n);
        RuntimeTestUtil.assertVector3("arc.ex", new Vector3(0.28734788f, ((startendreverse) ? -1 : 1) * -0.9578263f, 0), arc.arcParameter.ex);
        RuntimeTestUtil.assertFloat("arc.angle", ((startendreverse) ? /*-*/1 : 1) * (146.6015f - 360), (float) arc.getAngle().getDegree());
        RuntimeTestUtil.assertFloat("arc.radius", 3.132f, arc.getArc().getRadius());
        RuntimeTestUtil.assertEquals("nodes", 4, graph.getNodeCount());
        RuntimeTestUtil.assertEquals("arc.start", (startendreverse) ? "n3" : "n1", arc.getFrom().getName());
        RuntimeTestUtil.assertEquals("arc.end", (startendreverse) ? "smootharcto" : "smootharcto", arc.getTo().getName());
        RuntimeTestUtil.assertFloat("arc.len", 11.665504f, arc.getLength());

    }

    @Test
    public void testArcCalcParameter3D() {
        //Vector3 start = new Vector3(1, 0, 0);
        Vector3 intersection = new Vector3(1, 2, 0);
        float radius = 0.5f;
        boolean radiusisdistance = false;
        Graph gr = new Graph();
        GraphNode s = gr.addNode("", new Vector3(1, 0, 0));
        GraphNode i = gr.addNode("", intersection);
        GraphEdge start = gr.connectNodes(s, i);

        //
        // -
        // |
        //Vector3 end = new Vector3(3, 2, 0);
        GraphNode n1 = gr.addNode("", new Vector3(3, 2, 0));
        GraphEdge edge1 = gr.connectNodes(i, n1);
        GraphArcParameter p = GraphUtils.calcArcParameterAtConnectedEdges(start, edge1, radius, true, radiusisdistance);
        System.out.println("center=" + p.arccenter);
        RuntimeTestUtil.assertVector3("center", new Vector3(1.5f, 1.5f, 0), p.arccenter);
        RuntimeTestUtil.assertEquals("distancefromintersection", 0.5f,
                p.distancefromintersection);
        RuntimeTestUtil.assertVector3("ex", new Vector3(-1f, 0, 0), p.arc.ex);
        RuntimeTestUtil.assertFloat("beta", MathUtil2.PI_2, p.arc.getBeta());
        RuntimeTestUtil.assertVector3("n", new Vector3(0, 0, -1), p.arc.n);
        //TestUtil.assertVector3("ey", new Vector3(0, 1, 0), p.arc.ey);
        Vector3 er = p.arc.getRotatedEx(0.5f, 0);
        RuntimeTestUtil.assertVector3("er", new Vector3(-0.35355335f, 0.3535534f, 0), er);

        // /
        // |
        //end = new Vector3(3, 4, 0);
        GraphNode n2 = gr.addNode("", new Vector3(3, 4, 0));
        GraphEdge edge2 = gr.connectNodes(i, n2);
        p = GraphUtils.calcArcParameterAtConnectedEdges(start, edge2, radius, true,
                radiusisdistance);
        System.out.println("center=" + p.arccenter);
        RuntimeTestUtil.assertVector3("center", new Vector3(1.5f, 1.7928932f, 0), p.arccenter);
        RuntimeTestUtil.assertEquals("distancefromintersection", 0.2071068f,
                p.distancefromintersection);
        RuntimeTestUtil.assertVector3("ex", new Vector3(-1f, 0, 0), p.arc.ex);
        //TestUtil.assertVector3("ey", new Vector3(-0.70710677f, 0.70710677f, 0), p.arc.ey);
        er = p.arc.getRotatedEx(0.5f, 0);
        RuntimeTestUtil.assertVector3("er0.5", new Vector3(-0.46193975f, 0.19134171f, 0), er);
        RuntimeTestUtil.assertVector3("er0", p.arc.ex, p.arc.getRotatedEx(0.0f, 0).normalize());
        //TestUtil.assertVector3("er1", p.arc.ey, p.arc.getRotatedEx(1.0f, 0).normalize());

        //
        // nach links
        // -
        // |
        //Vector3 end = new Vector3(3, 2, 0);
        GraphNode n1a = gr.addNode("", new Vector3(-1, 2, 0));
        GraphEdge edge1a = gr.connectNodes(i, n1a);
        p = GraphUtils.calcArcParameterAtConnectedEdges(start, edge1a, radius, true, radiusisdistance);
        System.out.println("center=" + p.arccenter);
        RuntimeTestUtil.assertVector3("center", new Vector3(0.5f, 1.5f, 0), p.arccenter);
        RuntimeTestUtil.assertEquals("distancefromintersection", 0.5f,
                p.distancefromintersection);
        RuntimeTestUtil.assertFloat("beta", MathUtil2.PI_2, p.arc.getBeta());
        RuntimeTestUtil.assertVector3("n", new Vector3(0, 0, 1), p.arc.n);
        RuntimeTestUtil.assertVector3("ex", new Vector3(1f, 0, 0), p.arc.ex);
        //TestUtil.assertVector3("ey", new Vector3(0, 1, 0), p.arc.ey);
        er = p.arc.getRotatedEx(0.5f, 0);
        RuntimeTestUtil.assertVector3("er", new Vector3(0.35355335f, 0.3535534f, 0), er);

        //
        // || fast parallel, fast schon eine Singularitaet
        //end = new Vector3(1.001f, 4, 0);
        GraphNode n3 = gr.addNode("", new Vector3(1.001f, 4, 0));
        GraphEdge edge3 = gr.connectNodes(i, n3);
        p = GraphUtils.calcArcParameterAtConnectedEdges(start, edge3, radius, true,
                radiusisdistance);
        System.out.println("center=" + p.arccenter);
        RuntimeTestUtil.assertVector3("center", new Vector3(1.5f, 1.9998779f, 0), p.arccenter);
        er = p.arc.getRotatedEx(0.5f, 0);
        //hier muss ex nach link und ey quasi auch gehen
        RuntimeTestUtil.assertVector3("ex", new Vector3(-1f, 0f, 0), p.arc.ex);
        //TestUtil.assertVector3("ey", new Vector3(-1f, 0, 0), p.arc.ey);
        RuntimeTestUtil.assertVector3("er", new Vector3(-0.5f, 0.000122f, 0), er);

        // | - 45 Grad rechts hoch
        //end = new Vector3(3, 2, 2);
        GraphNode n4 = gr.addNode("", new Vector3(3, 2, 2));
        GraphEdge edge4 = gr.connectNodes(i, n4);
        p = GraphUtils.calcArcParameterAtConnectedEdges(start, edge4, radius, true, radiusisdistance);
        System.out.println("center=" + p.arccenter);
        RuntimeTestUtil.assertVector3("center", new Vector3(1.3535534f, 1.5f, 0.35355338f), p.arccenter);
        RuntimeTestUtil.assertEquals("distancefromintersection", 0.5f,
                p.distancefromintersection);
        RuntimeTestUtil.assertVector3("ex", new Vector3(-0.70710677f, 0, -0.70710677f), p.arc.ex);
        //TestUtil.assertVector3("ey", new Vector3(0, 1, 0), p.arc.ey);
        er = p.arc.getRotatedEx(0.5f, 0);
        RuntimeTestUtil.assertVector3("er", new Vector3(-0.25f, 0.35355335f, -0.25f), er);
        RuntimeTestUtil.assertVector3("er0", p.arc.ex, p.arc.getRotatedEx(0.0f, 0).normalize());
        //TestUtil.assertVector3("er1", p.arc.ey, p.arc.getRotatedEx(1.0f, 0).normalize());

        // -| 45 Grad links hoch
        //end = new Vector3(-1, 2, 2);
        GraphNode n5 = gr.addNode("", new Vector3(-1, 2, 2));
        GraphEdge edge5 = gr.connectNodes(i, n5);
        p = GraphUtils.calcArcParameterAtConnectedEdges(start, edge5, radius, true, radiusisdistance);
        System.out.println("center=" + p.arccenter);
        RuntimeTestUtil.assertVector3("center", new Vector3(0.6464466f, 1.5f, 0.35355338f), p.arccenter);
        RuntimeTestUtil.assertEquals("distancefromintersection", 0.5f,
                p.distancefromintersection);
        RuntimeTestUtil.assertVector3("ex", new Vector3(0.70710677f, 0, -0.70710677f), p.arc.ex);
        //TestUtil.assertVector3("ey", new Vector3(0, 1, 0), p.arc.ey);
        er = p.arc.getRotatedEx(0.5f, 0);
        RuntimeTestUtil.assertVector3("er", new Vector3(0.25f, 0.35355335f, -0.25f), er);
        RuntimeTestUtil.assertVector3("er0", p.arc.ex, p.arc.getRotatedEx(0.0f, 0).normalize());
        //TestUtil.assertVector3("er1", p.arc.ey, p.arc.getRotatedEx(1.0f, 0).normalize());
    }


    @Test
    public void TestBezier() {
        Vector3 p0 = new Vector3(1, 0, 0);
        Vector3 p1 = new Vector3(1, 2, 0);

        //
        // -
        // |
        Vector3 p2 = new Vector3(2, 3, 0);
        Vector3 p3 = new Vector3(3, 3, 0);

        Vector3 start = Bezier.CalculateBezierPoint(0.01f, p0, p1, p2, p3);
        RuntimeTestUtil.assertVector3("start", new Vector3(1.000299f, 0.0597f, 0), start);
        Vector3 mid = Bezier.CalculateBezierPoint(0.5f, p0, p1, p2, p3);
        RuntimeTestUtil.assertVector3("mid", new Vector3(1.625f, 2.25f, 0), mid);
        Vector3 end = Bezier.CalculateBezierPoint(0.99f, p0, p1, p2, p3);
        RuntimeTestUtil.assertVector3("end", new Vector3(2.97f, 2.9997f, 0), end);

        //
        // raise to z1
        // -
        // |
        p2 = new Vector3(2, 3, 1);
        p3 = new Vector3(3, 3, 1);

        start = Bezier.CalculateBezierPoint(0.01f, p0, p1, p2, p3);
        RuntimeTestUtil.assertVector3("start", new Vector3(1.000299f, 0.0597f, 0.00029f), start);
        mid = Bezier.CalculateBezierPoint(0.5f, p0, p1, p2, p3);
        RuntimeTestUtil.assertVector3("mid", new Vector3(1.625f, 2.25f, 0.5f), mid);
        end = Bezier.CalculateBezierPoint(0.99f, p0, p1, p2, p3);
        RuntimeTestUtil.assertVector3("end", new Vector3(2.97f, 2.9997f, 0.9997f), end);

    }

   

    /**
     * inner arc am takeoff auf 14L.
     */
    @Test
    public void testTakeoffSmoothing() {
        Graph graph = new Graph();
        GraphNode start = graph.addNode("holding", new Vector3(-790.92615f, 1454.5745f, 0));
        GraphNode mid = graph.addNode("takeoff", new Vector3(-471.13983f, 1233.7941f, 0));
        GraphEdge e1 = graph.connectNodes(start, mid);
        GraphNode end = graph.addNode("sid", new Vector3(2726.7913f, -975.14325f, 0));
        GraphEdge e2 = graph.connectNodes(mid, end);

        GraphArcParameter arcpara = GraphUtils.calcArcParameterAtConnectedEdges(e1, e2, 100, true, false);
        GraphPosition from = new GraphPosition(e1);
        double relpos = GraphUtils.compareEdgePosition(from, arcpara.arcbeginloc);
        RuntimeTestUtil.assertEquals("e1.length", 388.59653f, e1.getLength());
        RuntimeTestUtil.assertEquals("relpos", /*seit float->double 388.5793f*/388.5850, relpos);
        //TestUtil.assertVector3("arc.center", new Vector3(3.039798f, 3, 0), arc.getCenter());
        //TestUtil.assertFloat("arc.angle", 146.6015f - 360, arc.getAngle().getDegree());

    }

    /**
     * <p>
     * Skizze 28
     */
    @Test
    public void testUTurn() {
        float distance = 2, baselen = 9;

        Graph graph = new Graph();
        GraphNode n0 = graph.addNode("n0", new Vector3(0, 0, 0));
        GraphNode n1 = graph.addNode("n1", new Vector3(baselen, 0, 0));
        GraphEdge e0 = graph.connectNodes(n0, n1);
        GraphNode n2 = graph.addNode("n2", new Vector3(baselen, distance, 0));
        GraphNode n3 = graph.addNode("n3", new Vector3(0, distance, 0));
        GraphEdge e1 = graph.connectNodes(n2, n3);
        float radius = 2;
        TurnExtension uturn = GraphUtils.addUTurn(graph, n1, e0, n2, e1, distance, radius, 22);
        RuntimeTestUtil.assertVector3("arc0.center", new Vector3(9, -2, 0), uturn.edge.getArc().getCenter());
        RuntimeTestUtil.assertVector3("n0", new Vector3(10.322876f, -0.5f, 0), uturn.edge.getTo().getLocation());

        RuntimeTestUtil.assertVector3("arc1.center", new Vector3(11.645751f, 1, 0), uturn.arc.getArc().getCenter());

        RuntimeTestUtil.assertVector3("arc2.center", new Vector3(9, 4, 0), uturn.branch.getArc().getCenter());
        RuntimeTestUtil.assertVector3("n1", new Vector3(10.322876f, 2.5f, 0), uturn.branch.getFrom().getLocation());

        GraphPosition start = new GraphPosition(e0);
        GraphPathConstraintProvider  graphPathConstraintProvider = new DefaultGraphPathConstraintProvider(0, 0);
        GraphPath path = GraphUtils.createPathFromGraphPosition(graph, start, n3, new DefaultGraphWeightProvider(graph, new GraphEdge[]{}), graphPathConstraintProvider, 0, false, false, null);
        GraphMovingComponent gmc = new GraphMovingComponent(null);
        gmc.setGraph(graph, start, null);
        gmc.setPath(path);
        gmc.moveForward(baselen + 0.2f);
        //position auf turn0
        //arclen ist plausibel
        float arclen0und2 = 1.4454685f;
        RuntimeTestUtil.assertEquals("uturn0.len", arclen0und2, uturn.edge.getLength());
        RuntimeTestUtil.assertEquals("currentposition.position", 0.2f, gmc.getCurrentposition().edgeposition);
        RuntimeTestUtil.assertEquals("currentposition.edge", uturn.edge.getId(), gmc.getCurrentposition().currentedge.getId());
        RuntimeTestUtil.assertFalse("currentposition.reverse", gmc.getCurrentposition().isReverseOrientation());
        RuntimeTestUtil.assertVector3("uturn0.arc.ex", new Vector3(0, 1, 0), uturn.edge.getArc().ex);
        RuntimeTestUtil.assertVector3("uturn2.arc.exrotated0", new Vector3(0, 2, 0), uturn.edge.getArc().getRotatedEx(0));
        RuntimeTestUtil.assertVector3("uturn2.arc.exrotated1", new Vector3(1.32f, 1.5f, 0), uturn.edge.getArc().getRotatedEx(1));
        RuntimeTestUtil.assertVector3("uturn0.arc.n", new Vector3(0, 0, 1), uturn.edge.getArc().n);
        RuntimeTestUtil.assertEquals("uturn0.arc.beta", -0.7227342f, uturn.edge.getArc().getBeta());

        gmc.moveForward(5);
        //jetzt steht er auf dem uturn1
        float arclen1 = 9.174122f;
        RuntimeTestUtil.assertEquals("uturn.len", arclen1, uturn.arc.getLength());
        RuntimeTestUtil.assertEquals("currentposition.position", 5 - arclen0und2 + 0.2f, gmc.getCurrentposition().edgeposition);
        RuntimeTestUtil.assertEquals("currentposition.edge", uturn.arc.getId(), gmc.getCurrentposition().currentedge.getId());
        RuntimeTestUtil.assertFalse("currentposition.reverse", gmc.getCurrentposition().isReverseOrientation());

        gmc.moveForward(6);
        //jetzt steht er gerade so auf dem uturn2
        RuntimeTestUtil.assertEquals("uturn.len", arclen0und2, uturn.branch.getLength());
        RuntimeTestUtil.assertEquals("currentposition.position", 6 - (arclen1 - (5 - arclen0und2 + 0.2f)), gmc.getCurrentposition().edgeposition);
        RuntimeTestUtil.assertEquals("currentposition.edge", uturn.branch.getId(), gmc.getCurrentposition().currentedge.getId());
        RuntimeTestUtil.assertFalse("currentposition.reverse", gmc.getCurrentposition().isReverseOrientation());
        RuntimeTestUtil.assertVector3("uturn2.arc.ex", new Vector3(0.6614378f, -0.75f, 0), uturn.branch.getArc().ex);
        RuntimeTestUtil.assertVector3("uturn2.arc.exrotated", new Vector3(1.32f, -1.5f, 0), uturn.branch.getArc().getRotatedEx(0));
        RuntimeTestUtil.assertVector3("uturn2.arc.exrotated1", new Vector3(0, -2, 0), uturn.branch.getArc().getRotatedEx(1));
        RuntimeTestUtil.assertVector3("uturn2.arc.n", new Vector3(0, 0, 1), uturn.branch.getArc().n);
        RuntimeTestUtil.assertEquals("uturn2.arc.beta", -0.7227342f, uturn.branch.getArc().getBeta());


    }

    /**
     * Erzeugung von Multilanes über OutLines.
     * <p>
     * Skizze 11
     */
    @Test
    public void testMultilane() {

        Graph graph = GraphTest.buildSkizze11();
        GraphNode zero = graph.findNodeByName("zero");
        GraphNode eins = graph.findNodeByName("eins");
        GraphNode drei = graph.findNodeByName("drei");
        GraphNode acht = graph.findNodeByName("acht");
        GraphEdge zeroeins = graph.findEdgeByName("zeroeins");
        float offset = -0.3f;
        List<Vector3> outline = graph.orientation.getOutlineFromNode(zero, offset);
        RuntimeTestUtil.assertVector3("", new Vector3(0.21213204f, 0, -0.21213204f), outline.get(0));

        // start ist auf zero Richtung links oben. Dann passt ein U-Turn
        GraphPosition from = new GraphPosition(zeroeins, zeroeins.getLength(), true);
        float smoothingradius = 0.3f;
        int layer = 34;
        // lane offset positiv -> rechts
        GraphLane lane = new GraphLane(0.2f);
        GraphPathConstraintProvider  graphPathConstraintProvider = new DefaultGraphPathConstraintProvider(0, smoothingradius);
        GraphPath path = GraphUtils.createPathFromGraphPosition(graph, from, drei, null, graphPathConstraintProvider, layer, true, false, lane);
        //    TestUtil.assertEquals("path", "eins:e1->turnloop.smootharc(94)->smoothbegin.eins(20)->smootharc(0)->smoothbegin.outline0(0)->smootharc(0)->smoothbegin.outline1(1)->smootharc(0)->smoothend.outline1(2)", path.toString());

        RuntimeTestUtil.assertEquals("path", "zero:uturn0->uturn1(1)->uturn2(0)->smoothbegin.outline1(1)->smootharc(0)->smoothbegin.zwei(1)->smootharc(0)->smoothend.zwei(2)", path.toString());
        RuntimeTestUtil.assertEquals("path", "zero:uturn0--n0-->uturn1(1)--n1-->uturn2(0)--outline0-->smoothbegin.outline1(1)--smootharcfrom-->smootharc@outline1(0)--smootharcto-->smoothbegin.zwei(1)--smootharcfrom-->smootharc@zwei(0)--smootharcto-->smoothend.zwei(2)", path.getDetailedString());
        RuntimeTestUtil.assertVector3("", new Vector3(-0.14142135f, 0, 0.14142135f), path.getSegment(3).getEnterNode().getLocation());


    }

    /**
     * "arc low distance" problem
     * <p>
     * Skizze 11d
     */
    @Test
    public void testArcLowDistance() {
        Graph graph = new Graph();
        //graph.upVector = new Vector3(0, 1, 0);
        GraphNode zero = graph.addNode("zero", new Vector3(1, 0, 0));
        GraphNode eins = graph.addNode("eins", new Vector3(1, 0, 3));
        GraphNode zwei = graph.addNode("zwei", new Vector3(2, 0, 3));
        GraphNode drei = graph.addNode("drei", new Vector3(5, 0, 3));

        GraphEdge zeroeins = graph.connectNodes(zero, eins, "zeroeins");
        graph.connectNodes(eins, zwei, "einszwei");
        graph.connectNodes(zwei, drei);

        GraphPosition from = new GraphPosition(zeroeins);
        float smoothingradius = 1.3f;
        GraphPathConstraintProvider  graphPathConstraintProvider = new DefaultGraphPathConstraintProvider(0, smoothingradius);
        GraphPath path = GraphUtils.createPathFromGraphPosition(graph, from, drei, null, graphPathConstraintProvider, 2, true, true, null);
        RuntimeTestUtil.assertEquals("path", "zero:smoothbegin.eins->smootharc(2)->smoothend.eins(0)->(3)", path.toString());
        GraphTestUtil.assertGraphPathSegment("seg1",new String[]{"smootharcfrom","smootharcto"},path.getSegment(1));
        GraphTestUtil.assertGraphPathSegment("seg2",new String[]{"smootharcto","zwei"},path.getSegment(2));
        GraphTestUtil.assertGraphPathSegment("seg3",new String[]{"zwei","drei"},path.getSegment(3));
    }

}
