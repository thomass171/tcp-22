package de.yard.threed.graph;

import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by thomass on 13.09.16.
 */
public class Graph {
    private Log logger = Platform.getInstance().getLog(Graph.class);
    private GraphValidator graphValidator = null;
    // Eine globale Liste der Knoten und Kanten ist genauso praktisch wie die verlinkten Kanten in Knoten und Knoten in Kanten.
    private List<GraphNode> nodes = new ArrayList<GraphNode>();
    private List<GraphEdge> edges = new ArrayList<GraphEdge>();
    //29.3.17: bloeder hack. Aber praktsich fuer den Upvector.
    //15.3.18 public boolean iszEbene;
    private GraphSmoothing smoothing = null;
    //y0 Ebene als Default weils damit abwaets komptibel ist.
    //163.18 public Vector3 upVector = new Vector3(0,1,0);
    //Man koennte denken, das gehört hier nicht hin. Aber das wird beim Anlegen eines Graph ja mit festgelegt. 
    // Koennte vielleicht hier in eine Art static Registry, damit es nicht Attribut der Klasse ist?
    public GraphOrientation orientation;
    //1.4.20 Ohne funktionale Bedeutung, aber ganz praktisch
    private String name;

    public Graph(GraphOrientation orientation) {
        this.orientation = orientation;
    }

    public Graph() {
        this(GraphOrientation.buildDefault());
    }

    public Graph(GraphValidator graphValidator, GraphOrientation orientation) {
        this(orientation);
        this.graphValidator = graphValidator;
    }

    public GraphNode addNode(String name, Vector3 location) {
        return new GraphNode(nodes, name, location);
    }

    public GraphEdge connectNodes(GraphNode from, GraphNode to) {
        return connectNodes(from, to, "");
    }

    /*30.8.17public GraphEdge connectNodes(GraphNode from, GraphNode to, int layer) {
        return connectNodes(from, to, "", layer);
    }*/

    public GraphEdge connectNodes(GraphNode from, GraphNode to, String edgename) {
        return connectNodes(from, to, edgename, 0);
    }

    public GraphEdge connectNodes(GraphNode from, GraphNode to, String edgename, int layer) {
        if (graphValidator != null) {
            if (!graphValidator.nodesValidForEdge(from, to)) {
                logger.warn("nodes not valid for building edge. Ignoring");
                return null;
            }
        }
        GraphEdge edge = new GraphEdge(from, to, edgename, layer);
        from.addEdge(edge);
        to.addEdge(edge);
        edges.add(edge);
        return edge;
    }

    public int getNodeCount() {
        return nodes.size();
    }

    public GraphNode getNode(int index) {
        return nodes.get(index);
    }

    public int getEdgeCount() {
        return edges.size();
    }

    public GraphEdge getEdge(int index) {
        return edges.get(index);
    }


    /**
     * Liefert die erste mir dem Namen; es koennte aber mehrere geben.
     *
     * @param name
     * @return
     */
    public GraphNode findNodeByName(String name) {
        for (GraphNode n : nodes) {
            if (name.equals(n.name)) {
                return n;
            }
        }
        return null;
    }

    public GraphEdge findConnection(GraphNode n1, GraphNode n2) {
        for (GraphEdge e : n1.edges) {
            if (e.from == n2 || e.to == n2) {
                return e;
            }
        }
        return null;
    }

    public double getWeight(GraphNode next, GraphNode v) {
        GraphEdge e = findConnection(next, v);
        return e.getLength();

    }

    /**
     * Liefert die erste mir dem Namen; es koennte aber mehrere geben.
     *
     * @param name
     * @return
     */
    public GraphEdge findEdgeByName(String name) {
        for (GraphEdge n : edges) {
            if (name.equals(n.getName())) {
                return n;
            }
        }
        return null;
    }

    /**
     * Return path from from node to to node by avoiding edges "voidedges".
     * Return null if no path isType found. Return an empty path if from equals to.
     * 18.5.17: Die MEthode als public ist eigentlich witzlos, allenfalls für Tests.
     *
     * @return
     */
    public GraphPath findPath(GraphNode from, GraphNode to, GraphWeightProvider graphWeightProvider) {
        long currentmillis = Platform.getInstance().currentTimeMillis();

        PathFinder pf = new PathFinder(this, from, nodes, graphWeightProvider);

        GraphPath result = pf.dijkstra(to);
        // pf.printPath(graph, result, graph.nodes.indexOf(from), graph.nodes.indexOf(to));
        //logger.debug("findPath took " + (Platform.getInstance().currentTimeMillis() - currentmillis) + " ms");
        return result;
    }


    public GraphSmoothing getSmoothing() {
        if (smoothing == null) {
            smoothing = new GraphSmoothing(this);
        }
        return smoothing;
    }

    public GraphEdge findNearestEdge(Vector3 p) {
        GraphEdge best = null;
        double bestdistance = java.lang.Double.MAX_VALUE;
        for (GraphEdge n : edges) {
            Vector3 nearest = Vector3.getNearestPointOnVector(p, n.getFrom().getLocation(), n.getDirection());
            double distance = Vector3.getDistance(p, nearest);
            if (distance < bestdistance) {
                bestdistance = distance;
                best = n;
            }
        }
        return best;
    }

    public GraphNode findNearestNode(Vector3 p, GraphNodeFilter filter) {
        GraphNode best = null;
        double bestdistance = java.lang.Double.MAX_VALUE;
        for (GraphNode n : nodes) {
            double distance = Vector3.getDistance(p, n.getLocation());
            if (distance < bestdistance && (filter == null || filter.acceptNode(n))) {
                bestdistance = distance;
                best = n;
            }
        }
        return best;
    }

    public void removeLayer(int layer) {
        //C# kann keine Iterator wie Java
        /*for (Iterator<GraphEdge> iter = edges.iterator(); iter.hasNext(); ) {
            GraphEdge e = iter.next();
            if (e.getLayer() == layer) {
                e.removeFromNodes();
                iter.remove();
            }
        }*/
        for (int i = getEdgeCount() - 1; i >= 0; i--) {
            GraphEdge e = edges.get(i);
            if (e.getLayer() == layer) {
                e.removeFromNodes();
                edges.remove(i);
            }
        }
    }

    public String getStatistic() {
        Map<Integer, Integer> layercount = new HashMap<Integer, Integer>();
        for (GraphEdge n : edges) {
            int layer = n.getLayer();
            if (layercount.get(layer) == null) {
                layercount.put(layer, 0);
            }
            layercount.put(layer, layercount.get(layer) + 1);
        }
        String s = "nodes:";
        for (int l : layercount.keySet()) {
            s += "" + l + ":" + layercount.get(l) + ";";
        }
        return s;
    }

    /**
     * TODO:besser in eine util/Builder class.
     *
     * @param vector3List
     */
    public void extendFromVectorList(List<Vector3> vector3List) {
        GraphNode last = null;
        for (Vector3 v : vector3List) {
            GraphNode n = addNode("", v);
            if (last != null) {
                connectNodes(last, n);
            }
            last = n;
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

/**
 * Local class due to access to internal lists.
 * <p>
 * Dijkstra's algorithm to find shortest path from s to all other nodes
 * <p>
 * eg.: http://cs.fit.edu/~ryan/java/programs/graph/Dijkstra-java.html
 * <p>
 * Created by thomass on 31.03.17.
 */
class PathFinder {
    Map<GraphNode, Double> dist = new HashMap<GraphNode, Double>();
    Map<GraphNode, GraphNode> pred = new HashMap<GraphNode, GraphNode>();
    Graph graph;
    List<GraphNode> unvisited = new ArrayList<GraphNode>();
    List<GraphNode> visited = new ArrayList<GraphNode>();
    private GraphWeightProvider graphWeightProvider;
    GraphNode startnode;

    PathFinder(Graph graph, GraphNode startnode, List<GraphNode> nodelist, GraphWeightProvider graphWeightProvider) {
        this.graph = graph;
        this.startnode = startnode;
        this.graphWeightProvider = graphWeightProvider;
        int size = graph.getNodeCount();
        for (int i = 0; i < size; i++) {
            GraphNode n = nodelist.get(i);
            //if (n!=s) {
            unvisited.add(n);
            //}
            dist.put(n, java.lang.Double.MAX_VALUE);
        }
        //C# heads-up
        dist.put(startnode, (double) 0);
    }

    public GraphPath dijkstra(GraphNode destination) {
        while (!unvisited.isEmpty()) {
            GraphNode/*int*/ next = closestUnvisitedNode();
            if (next == null) {
                //ob das ok ist?
                break;
            }
            visited.add(next);
            unvisited.remove(next);
            evaluatedNeighbors(next);
        }
        List<GraphNode> path = new ArrayList<GraphNode>();
        GraphNode x = destination;
        while (x != startnode) {
            path.add(0, x);
            x = pred.get(x);
            if (x == null) {
                //no predecssor->no path
                return null;
            }
        }
        GraphPath p = new GraphPath(-1);
        GraphNode current = startnode;
        for (GraphNode n : path) {
            p.addSegment(new GraphPathSegment(graph.findConnection(current, n), current));
            current = n;
        }
        return p;
    }

    void evaluatedNeighbors(GraphNode n) {
        List<GraphNode> neighbors = n.getNeighbors();
        for (GraphNode neighbor : neighbors) {
            if (!visited.contains(neighbor)) {
                final double totaldistance = (double) dist.get(n) + getWeight(n, neighbor);
                if (dist.get(neighbor) > totaldistance) {
                    dist.put(neighbor, totaldistance);
                    pred.put(neighbor, n);
                }
            }
        }
    }

    private double getWeight(GraphNode n1, GraphNode n2) {
        if (graphWeightProvider != null) {
            return graphWeightProvider.getWeight(n1, n2);
        }
        return graph.getWeight(n1, n2);
    }

    /**
     * find the node with the lowest distance in unvisited
     */
    private /*int*/GraphNode closestUnvisitedNode() {
        double x = java.lang.Double.MAX_VALUE;
        GraphNode fn = null;
        for (GraphNode n : unvisited) {
            if (dist.get(n) < x) {
                fn = n;
                x = (double) dist.get(n);
            }
        }

        return fn;
    }


}

