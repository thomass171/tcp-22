package de.yard.threed.graph;


import de.yard.threed.core.*;
import de.yard.threed.core.platform.NativeAttributeList;
import de.yard.threed.core.platform.NativeDocument;
import de.yard.threed.core.platform.NativeNode;
import de.yard.threed.core.platform.NativeNodeList;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.BasicGeometry;
import de.yard.threed.core.geometry.CustomGeometry;
import de.yard.threed.engine.XmlDocument;
import de.yard.threed.core.geometry.IndexList;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.geometry.Face3;
import de.yard.threed.core.geometry.FaceList;
import de.yard.threed.engine.util.XmlHelper;


import java.util.ArrayList;
import java.util.List;


/**
 * MA31: Klasse duplicated und z.Z. nach traffic verschoben.
 * Created by thomass on 14.09.16.
 */
public class GraphFactory {
    static private Log logger = Platform.getInstance().getLog(GraphFactory.class);

    /**
     * Eine Art Atomium.
     *
     * @return
     */
    public static Graph buildTubeSample() {
        Graph graph = new Graph(GraphOrientation.buildDefault());
        GraphNode unten = graph.addNode("n1", new Vector3(0, 0, 0));
        GraphNode vorne = graph.addNode("n2", new Vector3(-1, 0, 0));
        GraphNode n3 = graph.addNode("n1", new Vector3(1, 0.5f, 0.5f));
        GraphNode n4 = graph.addNode("n1", new Vector3(1, 0.5f, -0.5f));
        GraphNode n5 = graph.addNode("n1", new Vector3(0, 1.5f, 0.5f));
        GraphNode n6 = graph.addNode("n1", new Vector3(0, 1.5f, -0.5f));
        GraphNode oben = graph.addNode("n1", new Vector3(0, 2, 0));

        graph.connectNodes(unten, vorne);
        graph.connectNodes(unten, n3);
        graph.connectNodes(unten, n4);

        graph.connectNodes(vorne, n3);
        graph.connectNodes(vorne, n4);
        graph.connectNodes(vorne, n5);
        graph.connectNodes(vorne, n6);
        graph.connectNodes(vorne, oben);

        graph.connectNodes(n3, n5);

        graph.connectNodes(n4, n6);

        graph.connectNodes(n5, oben);
        graph.connectNodes(n6, oben);

        return graph;
    }


    /**
     * Rotiert CCW von z-oben gesehen.
     * Skizze 25
     * In z0 layer.
     * 30.1.2020
     */
    public static void addZ0Circle(Graph graph, Vector3 startE1, String edgeLabel) {
        Vector3 center = new Vector3();
        double angle = MathUtil2.PI_2;
        boolean z0 = true;
        double radius = startE1.length();
        Vector3 up = new Vector3(0, 0, 1);
        Vector3 e1 = startE1;
        GraphNode node = graph.addNode("start", e1);
        GraphNode startNode = node;
        GraphNode lastNode;
        if (graph.getNodeCount() > 1) {
            lastNode = graph.getNode(graph.getNodeCount() - 2);
            graph.connectNodes(lastNode, node);
        }
        for (int i = 0; i < 3; i++) {
            e1 = e1.rotateOnAxis(angle, up);
            lastNode = node;
            node = graph.addNode("n" + i, e1);
            //Der ex des arc muss zum from zeigen
            graph.connectNodes(lastNode, node, edgeLabel + "." + i).setArc(new GraphArc(center, radius, lastNode.getLocation().normalize(), up, angle));

        }
        graph.connectNodes(node, startNode, edgeLabel + ".3").setArc(new GraphArc(center, radius, node.getLocation().normalize(), up, angle));

        /*GraphNode links = addNode(graph, -radius, 0, "links", z0);
        Vector3 up = new Vector3(0, 0, 1);
        graph.connectNodes(start, links).setArc(new GraphArc(center, radius, new Vector3(0, -1, 0), up, -MathUtil2.PI_2));
        GraphNode oben = addNode(graph, 0, radius, "", z0);
        graph.connectNodes(links, oben).setArc(new GraphArc(center, radius, new Vector3(-1, 0, 0), up, -MathUtil2.PI_2));
        GraphNode right = addNode(graph, radius, 0, "", z0);
        graph.connectNodes(oben, right).setArc(new GraphArc(center, radius, new Vector3(0, 1, 0), up, -MathUtil2.PI_2));
        graph.connectNodes(right, start).setArc(new GraphArc(center, radius, new Vector3(1, 0, 0), up, -MathUtil2.PI_2));
        return graph;*/
    }


    /**
     * Ein gerades Stueck und dann ein "Zurückkreis". Skizze 12.
     * 18.5.17: Extension fuer bessere Sichtbarkeit vegrößert.
     */
    public static /*MA31 Traffic*/Graph buildReturnKreis(double radius, boolean extended) {
        /*Traffic*/
        Graph graph = new /*Traffic*/Graph(GraphOrientation.buildDefault());
        //15.3.18 graph.upVector = new Vector3(0, 1, 0);
        //Railing liegt in y0 und hat Defaultausrichtung
        return addReturnKreis(graph, radius, extended, false);
    }

    public static /*MA31 Traffic*/Graph addReturnKreis(/*Traffic*/Graph graph, double radius, boolean extended, boolean z0) {
        double umfang = 2 * (double) (Math.PI * radius);
        double halbumfang = (double) (Math.PI * radius);
        GraphNode start = addNode(graph, 15 + radius + radius, 0, "start", z0);
        GraphNode links = addNode(graph, 15, 0, "links", z0);
        GraphEdge edgelinks = graph.connectNodes(start, links, "getFirst");
        GraphNode linksoben = addNode(graph, 15, 2 * radius, "", z0);
        GraphEdge halbkreis = graph.connectNodes(links, linksoben, "firsthalbkreis");
        halbkreis.setArcAtFrom(buildVector3(new Vector2(15, radius), z0), radius, -MathUtil2.PI, new Vector3(0, 1, 0));
        GraphNode wendepunkt = addNode(graph, 15 + radius, radius, "", z0);
        graph.connectNodes(linksoben, wendepunkt, "first4").setArcAtFrom(buildVector3(new Vector2(15, radius), z0), radius, -MathUtil2.PI_2, new Vector3(0, 1, 0));
        GraphEdge closing = graph.connectNodes(wendepunkt, start, "closing");
        closing.setArcAtFrom(buildVector3(new Vector2(15 + radius + radius, radius), z0), radius, MathUtil2.PI_2, new Vector3(0, 1, 0));
        if (extended) {
            // Noch ein gerades Stück vorm ersten
            GraphNode ext = addNode(graph, 15 + radius + radius + 9/*2*/, 0, "", z0);
            graph.connectNodes(start, ext, "extension");
        }

        return graph;
    }


    public static void addBridge(Graph graph) {
        GraphNode n1 = graph.findNodeByName("n1");
        GraphNode n2 = graph.findNodeByName("n2");
        GraphNode nu1 = graph.addNode("nu1", new Vector3(2, 15, 1));
        GraphNode nu2 = graph.addNode("nu2", new Vector3(-2, 15, 1));
        GraphNode n12 = graph.addNode("n3", new Vector3(-5, 15, 0));
        GraphNode n11 = graph.addNode("n4", new Vector3(0, 25, 0));
        GraphEdge hoch = graph.connectNodes(n1, n11, "hoch");
        GraphEdge up = graph.connectNodes(n2, nu1, "up");
        GraphEdge bridge = graph.connectNodes(nu1, nu2, "bridge");
        GraphEdge down = graph.connectNodes(nu2, n12, "down");

    }

    /**
     * Zurücksetzen. Skizze 12
     *
     * @param graph
     */
    public static GraphEdge buildBack(Graph graph) {
        GraphEdge extension = graph.findEdgeByName("extension");
        GraphNode backpoint = addNode(graph, 32, -1, "backedge");
        Vector3 turncenter = new Vector3(33, -1, 0);
        GraphEdge backedge = graph.connectNodes(extension.to, backpoint, "backedge0");
        graph.connectNodes(backpoint, extension.from, "backedge1");
        return backedge;
    }

    /**
     * hier wird von /y nach x/z uebertragen. Auch z spiegeln, um eine intuitive Entsprechung der Darstellung
     * mit Default OpenGL Camera zur Skizze zu haben.
     * 17.7.17: Optional in z0 Ebene statt y0 Ebene; dann ohne spiegeln.
     *
     * @param graph
     * @param x
     * @param y
     * @return
     */
    public static GraphNode addNode(Graph graph, double x, double y, String name, boolean z0) {
        if (z0) {
            return graph.addNode(name, new Vector3(x, y, 0));
        }
        GraphNode n = graph.addNode(name, buildVector3(x, y));
        return n;
    }

    public static GraphNode addNode(Graph graph, double x, double y, String name) {
        return addNode(graph, x, y, name, false);
    }

    /**
     * hier wird von /y nach x/z uebertragen. Auch z spiegeln, um eine intuitive Entsprechung der Darstellung
     * mit Default OpenGL Camera zur Skizze zu haben. Diese Spiegelung macht auch die erforderlichen
     * Rotationen übersichtlicher. D.h., eine Änderung der Spiegelung erfordert eine Anpassung
     * der Rotationen.
     */
    private static Vector3 buildVector3(double x, double y, boolean z0) {
        if (z0) {
            return new Vector3(x, y, 0);
        }
        return new Vector3(x, 0, -y);
    }

    private static Vector3 buildVector3(double x, double y) {
        return buildVector3(x, y, false);
    }

    public static Vector3 buildVector3(Vector2 v) {
        return buildVector3(v.getX(), v.getY());
    }

    public static Vector3 buildVector3(Vector2 v, boolean z0) {
        return buildVector3(v.getX(), v.getY(), z0);
    }

    /**
     * TODO auch fuer Geraden.
     * <p>
     * TODO Das ist mir auch zuviel Handarbeit, und uvs sind auch noch nicht drin. Gibts da nichts anderes?
     * 8.2.18: Warum kann man nicht die ShapeGeometry wie bei Railing nehmen? Weil die nicht verschiedene Ups an from/to handelt?
     * Bei Kreisbögen muss es das vielleicht nicht geben, bei Bezier aber schon.
     * <p>
     * Naja, so verkehrt ist villeicht doch nicht. Allerdings nur Kreise. Viuelleicht doch ShapeGeometry.
     * Das wird vielleicht doch deprecated, weil fuer Terrain outline Favorit ist.
     * in z=0 Ebene
     * 15.3.18: jetzt auch ausserhalb
     *
     * @param edge
     * @param width
     * @return
     */
    public static CustomGeometry buildGraphGeometry(GraphEdge edge, double width, int segments) {
        //Platform platform = ((Platform)Platform.getInstance());
        List</*Native*/Vector3> vertices = new ArrayList<Vector3>();
        List<Vector2> uvs = new ArrayList<Vector2>();
        List<Vector3> normals;
        IndexList indexes = new IndexList();
        FaceList facelist = new FaceList(true);

        normals = new ArrayList<Vector3>();

        double w2 = width / 2;
        Degree angle = edge.getAngle();
        Vector3 center = edge.getCenter();
        Vector3 v = edge.from.getLocation().subtract(center);
        double radius = v.length();
        Vector3 vi = v.normalize().multiply(radius - w2);
        Vector3 vo = v.normalize().multiply(radius + w2);

        float anglestep = (float) (angle.toRad() / segments);
        //Shape shape = new Shape(true);
        //shape.addPoint(edge.getFrom().getLocation().getX(), edge.getFrom().getLocation().getY());
        //shape.addArc(new Vector2(c.getX(),c.getY()),,segments);
        Vector3 normal = new Vector3(0, 0, 1);

        for (int i = 0; i <= segments; i++) {
            Quaternion rot = Quaternion.buildRotationZ(Degree.buildFromRadians(i * anglestep));
            Vector3 er = edge.arcParameter.getRotatedEx((float) i / segments);
            vertices.add(center.add(er.normalize().multiply(radius - w2)/*vi.rotate(rot))*/));
            vertices.add(center.add(er.normalize().multiply(radius + w2)/*vo.rotate(rot)*/));
            uvs.add(new Vector2(0, 0));
            uvs.add(new Vector2(0, 0));
            normals.add(normal);
            normals.add(normal);
        }

        for (int i = 0; i < segments; i++) {
            if (angle.getDegree() > 0) {
                int a = i * 2;
                int b = i * 2 + 1;
                int c = i * 2 + 2;
                int d = i * 2 + 3;
                indexes.add(a, b, d);
                indexes.add(d, c, a);
                facelist.add(new Face3(a, b, d));
                facelist.add(new Face3(d, c, a));
            } else {
                int a = i * 2;
                int b = i * 2 + 2;
                int c = i * 2 + 3;
                int d = i * 2 + 1;
                indexes.add(a, b, d);
                indexes.add(b, c, d);
                facelist.add(new Face3(a, b, d));
                facelist.add(new Face3(b, c, d));
            }
        }
        // return new SimpleGeometry(vertices, indexes.getIndices(), uvs, normals);

        return new BasicGeometry(vertices, facelist);
    }

    /**
     * Wertet ein nur die "n" und "e" nodes aus. Das koennte auch ein bischen spezifischer sein.
     *
     * @param s
     * @return
     */
    public static Graph buildfromXML(String s, List<Long> tripnodes) {

        XmlDocument xmlDocument;
        try {
            xmlDocument = XmlDocument.buildXmlDocument(s);
        } catch (XmlException e) {
            throw new RuntimeException(e);
        }

        Graph graph;
        NativeDocument xmlGraph = xmlDocument.nativedocument;
        String orientation = xmlGraph.getAttribute("orientation");
        if (orientation == null) {
            logger.warn("No orientation defined in graph. Using default.");
            // Ich brauc evtl. eine mit up nach -y, geht aber nicht weil left handed
            //23.8.18: Warum ist hier denn noch Default? Jetzt z0
            graph = new Graph(GraphOrientation.buildForZ0()/*new GraphOrientationYnegative()*/);
        } else {
            graph = new Graph(GraphOrientation.buildByName(orientation));
        }
        List<NativeNode> nodelist = XmlHelper.getChildren(XmlHelper.getChild(xmlGraph, "nodes", 0).nativeNode, "n");
        //NativeNodeList nodelist = .getElementsByTagName("n");
        for (int i = 0; i < nodelist.size(); i++) {
            NativeNode node = nodelist.get(i);
            NativeAttributeList attrs = node.getAttributes();
            // name muss der index sein, denn darueber wird gesucht.
            String name = attrs.getNamedItem("name").getValue();
            double x = Util.parseDouble(attrs.getNamedItem("x").getValue());
            double y = Util.parseDouble(attrs.getNamedItem("y").getValue());
            double z = Util.parseDouble(attrs.getNamedItem("z").getValue());
            Vector3 location = new Vector3(x, y, z);
            GraphNode n = graph.addNode(name, location);

        }
        NativeNodeList edgelist = xmlDocument.nativedocument.getElementsByTagName("e");
        for (int i = 0; i < edgelist.getLength(); i++) {
            NativeNode node = edgelist.getItem(i);
            NativeAttributeList attrs = node.getAttributes();
            String from = XmlHelper.getStringAttribute(node, "from");
            String to = XmlHelper.getStringAttribute(node, "to");
            String name = XmlHelper.getStringAttribute(node, "name");
            String center = XmlHelper.getStringAttribute(node, "center");
            String radius = XmlHelper.getStringAttribute(node, "radius");
            String angle = XmlHelper.getStringAttribute(node, "angle");

            GraphNode bn = graph.findNodeByName(from);
            GraphNode en = graph.findNodeByName(to);
            if (bn == null) {
                logger.warn("from node not found: " + from);
            } else {
                if (en == null) {
                    logger.warn("to node not found: " + to);
                } else {
                    GraphEdge c = graph.connectNodes(bn, en, name);
                    if (center != null) {
                        RailingFactory.setArc(c, Util.parseVector3(center), Util.parseDouble(radius), Util.parseDouble(angle));
                    }
                }
            }
        }
        NativeNodeList tlist = xmlDocument.nativedocument.getElementsByTagName("tripnode");
        for (int i = 0; i < tlist.getLength(); i++) {
            NativeNode node = tlist.getItem(i);
            NativeAttributeList attrs = node.getAttributes();
            String osmid = XmlHelper.getStringAttribute(node, "osmid");
            tripnodes.add(Util.parseLong(osmid));
        }
        return graph;
    }

}

/**
 * upVector ist dann (0,-1,0)
 */
/*7.6.18 unnuetz fuer OSM (left handed)class GraphOrientationYnegative extends GraphOrientation {

    @Override
    public Quaternion getForwardRotation() {
        Quaternion rotation;
        rotation = Quaternion.buildRotationX(new Degree(180));
        return rotation;
    }

    @Override
    public Vector3 getUpVector(GraphEdge edge) {
        return new Vector3(0, -1, 0);
    }
}
*/