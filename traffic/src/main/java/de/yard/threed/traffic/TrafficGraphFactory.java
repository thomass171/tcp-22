package de.yard.threed.traffic;


import de.yard.threed.core.*;
import de.yard.threed.core.platform.*;
import de.yard.threed.engine.XmlDocument;
import de.yard.threed.graph.*;

import de.yard.threed.core.XmlException;
import de.yard.threed.engine.util.XmlHelper;

import java.util.ArrayList;
import java.util.List;


/**
 * MA31: Klasse duplicated. and renamed from GraphFactory
 * Created by thomass on 14.09.16.
 */
public class TrafficGraphFactory {
    static private Log logger = Platform.getInstance().getLog(TrafficGraphFactory.class);


    /**
     * Bild 3
     * <p>
     * Eine einfache Karte in der z=0 Ebene, damit es nicht zu trivial ist, was die Rotationen angeht.
     * 17.7.17: Den Kreis nach oben verlagert, damit Returnkreis besser passt.
     *
     * @return
     */
    public static TrafficGraph buildOsmSample() {
        TrafficGraph graph = new TrafficGraph(GraphOrientation.buildForZ0());
        //graph.iszEbene = true;
        //graph.upVector = new Vector3(0, 0, 1);
        GraphNode n0 = graph.getBaseGraph().addNode("n0", new Vector3(0, 0, 0));
        GraphNode n1 = graph.getBaseGraph().addNode("n1", new Vector3(0, 10, 0));
        GraphNode n2 = graph.getBaseGraph().addNode("n2", new Vector3(5, 15, 0));
        GraphNode n3 = graph.getBaseGraph().addNode("n3", new Vector3(5, 25, 0));
        GraphNode n4 = graph.getBaseGraph().addNode("n4", new Vector3(15, 25, 0));
        //Node n5 = graph.addNode("n1",new Vector3(0,10,0));
        //Node n6 = graph.addNode("n1",new Vector3(0,15,0));
        //Node oben = graph.addNode("n1",new Vector3(0,20,0));

        graph.getBaseGraph().connectNodes(n0, n1, "0-1");
        graph.getBaseGraph().connectNodes(n0, n2);

        graph.getBaseGraph().connectNodes(n1, n2, "1-2");
        graph.getBaseGraph().connectNodes(n2, n3, "2-3");
        graph.getBaseGraph().connectNodes(n3, n4, "3-4");
        
        /*graph.connectNodes(vorne,n5);
        graph.connectNodes(vorne,n6);
        graph.connectNodes(vorne,oben);

        graph.connectNodes(n3,n5);
        
        graph.connectNodes(n5,oben);
        graph.connectNodes(n6,oben);*/

        double radius = 7;
        Vector2 center = new Vector2(15 + radius, 25);
        Vector2 p = new Vector2(15 + 2 * radius, 25);

        //links beginnen. Da liegt der erste Kreispunkt exakt auf n4
        List<GraphNode> circle = new ArrayList<GraphNode>();
        int segments = 16;
        for (int i = 0; i < segments; i++) {
            Degree angle = new Degree((double) (180 + 360.0 * (double) i / segments));
            Vector2 rotated = p.rotate(center, angle);
            GraphNode n = graph.getBaseGraph().addNode("c" + i, new Vector3(rotated.getX(), rotated.getY(), 0));
            circle.add(n);
        }

        graph.getBaseGraph().connectNodes(n4, circle.get(0));
        for (int i = 0; i < segments - 1; i++) {
            graph.getBaseGraph().connectNodes(circle.get(i), circle.get(i + 1));
        }
        graph.getBaseGraph().connectNodes(circle.get(segments - 1), circle.get(0));
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
     * Wertet ein nur die "n" und "e" nodes aus. Das koennte auch ein bischen spezifischer sein.
     *
     * @param s
     * @return
     */
    public static TrafficGraph buildfromXML(String s) {


        //TODO orientation aus XML. Ich brauc evtl. eine mit up nach -y, geht aber nicht weil left handed
        //23.8.18: Warum ist hier denn noch Default? Jetzt z0

        List<Long> tripNodes = new ArrayList<Long>();
        TrafficGraph graph = new TrafficGraph(GraphFactory.buildfromXML(s, tripNodes));
        graph.tripnodes = tripNodes;
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