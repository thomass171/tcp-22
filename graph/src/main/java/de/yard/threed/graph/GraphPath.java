package de.yard.threed.graph;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Ein Weg durch einen Graph.
 * Die erste Edge ist die nächste(!) von der aktuellen Position. 16.2.18:Ist das immer so? Passt das dann zu start?
 * 11.4.18: brauch ich den start ueberhaupt noch? Das ist doch die enternode des ersten Segments.weg damit.
 * <p>
 * Created by thomass on 31.03.17.
 */
public class GraphPath {
    Log logger = Platform.getInstance().getLog(GraphPath.class);
    //11.4.18 GraphNode start;
    // layer of temporary path graph segments. Used later
    // for removing the temporary edges from the graph.
    public int layer;
    // path from index 0 to the end of list.
    public List<GraphPathSegment> path = new ArrayList<GraphPathSegment>();
    // Eine optionale Startposition, zu der gesprungen wird, statt von der aktuellen Position zum Pfad
    // beginn zu fahren. Ist hier um sie ohne weitere Datenstruktur unterbringen zu konnen.
    public GraphPosition startposition = null;
    // Fuer den Spezailfall, dass eine Node zur Spitzwende verwendet wird. Dann wird von startposition rückwärts gefahhen.
    public boolean backward = false;
    private static int uniid = 1;
    public int id = uniid++;
    //Wenn der Path gesmoothed oder anderweitig an temp edges endet, muss das Vehicle auf eine regulaere Edge umgesetzt werden können, die idealerweise
    // an selber Stelle liegt.
    public GraphPosition finalposition;
    //1.4.20 Ohne funktionale Bedeutung, aber ganz praktisch
    private String name;
    // 12.11.18: Es ist einfach manschmal praktisch (z.B. im Visualizer, wenn man den Graph kennt.
    //ja, trotzdem nochmal ohne versuchen. Graph graph

    public GraphPath(/*GraphNode start,*/ int layer) {
        //this.start = start;
        this.layer = layer;
    }

    /*public GraphPath(int layer) {
        //this.start = start;
        this.layer = layer;
    }*/

    public void addSegment(GraphPathSegment segment) {
        // validate?
        // 15.2.18: Zumindest mal pruefen, dass start die entrynode des ersten Segments ist.
        /*11.4.18if (path.size() == 0 && segment.enternode != start) {
            throw new RuntimeException("startnode != enternode");
        }*/
        path.add(segment);
    }

    public int getSegmentCount() {
        return path.size();
    }

    public GraphPathSegment getSegment(int index) {
        return path.get(index);
    }

    /**
     * start looking at "from" (excepting from).
     *
     * @param from
     * @return
     */
    public GraphEdge getNearestLineEdge(GraphEdge from) {
        GraphEdge nearestlineedge = null;
        boolean foundfrom = (from == null);
        for (int i = 0; i < getSegmentCount(); i++) {
            GraphEdge e = getSegment(i).edge;
            if (e.getCenter() == null) {
                if (foundfrom) {
                    return e;
                }
                if (from != null && e.equals(from)) {
                    foundfrom = true;
                }
            }
        }
        return nearestlineedge;
    }

    public GraphPathSegment getLast() {
        return getSegment(getSegmentCount() - 1);
    }

    public void insertSegment(int index, GraphPathSegment segment) {
        path.add(index, segment);
    }

    @Override
    public String toString() {
        return toString(false);
    }

    public String toString(boolean detailed) {

        String s = "";

        if (startposition != null && backward) {
            s += "[back on " + startposition.currentedge.getName() + "]";
        }
        //11.4.18: es gibt kein explizite start mehr
        if (path.size() == 0) {
            return s;
        }
        s += getStart().getName() + ":";
        s += path.get(0).edge.getName();
        for (int i = 1; i < path.size(); i++) {
            GraphEdge edge = path.get(i).edge;
            String edgename = edge.getName();
            GraphArc arcpara = edge.getArc();
            if (detailed && arcpara != null && arcpara.origin != null) {
                edgename += "@" + arcpara.origin.getName();
            }
            String nodetag = "->";
            if (detailed) {
                GraphNode enternode = path.get(i).getEnterNode();
                nodetag = "--" + enternode.getName();
                if (enternode.parent != null) {
                    nodetag += "@" + enternode.parent.getName();
                }
                nodetag += "-->";
            }
            // die Laenge auf ganze Stellen runden, wegen Lesbarkeit und um Fehler wegen Rundungen (z.B. FG) zu vermeiden.
            s += nodetag + edgename + "(" + Math.round(edge.getLength()) + ")";
        }
        return s;
    }

    /**
     * Geht auch wenn es keine last gibt. dann wird einfach angehagen.
     * 13.3.19: Check, dass ein valider path rauskommt. Ansonsten wird die Transition ignoriert.
     * Returns true if segment was replaced, false otherwise.
     * @param transition
     */
    public boolean replaceLast(GraphTransition transition) {
        GraphPathSegment currseg = null;
        //Validierung muss beim vorletzten ansetzen, denn das letzte wird ja entfernt.
        if (path.size() > 1) {
            currseg = path.get(path.size() - 2);
        }
        for (GraphPathSegment s : transition.seg) {
            if (currseg != null && !isConnectedToPredecessor(s, currseg)) {
                return false;
            }
            currseg = s;
        }
        if (path.size() > 0) {
            path.remove(path.size() - 1);
        }
        for (GraphPathSegment s : transition.seg) {
            path.add(s);
        }
        return true;
    }

    /**
     * Die Position ist optional.
     *
     * @param currentposition
     * @return
     */
    public double getLength(GraphPosition currentposition) {
        double len = 0;
        for (int i = getSegmentCount() - 1; i >= 0; i--) {
            GraphEdge e = getSegment(i).edge;
            if (currentposition != null && e == currentposition.currentedge) {
                len += currentposition.currentedge.getLength() - currentposition.edgeposition;
                return len;
            }
            len += e.getLength();
        }
        return len;
    }

    public static GraphPath buildFromEdgelist(GraphNode startnode, List<GraphEdge> edgelist, int layer) {
        GraphPath path = new GraphPath(layer);
        GraphNode lastnode = startnode;
        for (GraphEdge e : edgelist) {
            path.addSegment(new GraphPathSegment(e, lastnode));
            lastnode = e.getOppositeNode(lastnode);
        }
        return path;
    }

    /**
     * Von einer Node ausgehen so lange den Pfad bauen, wie die Verzweigung klar ist.
     *
     * @param node
     * @return
     */
    public static GraphPath buildFromNode(GraphNode node, int layer) {
        GraphEdge edge = node.getFirstFromEdge();
        GraphPath path = new GraphPath(layer);

        do {
            path.addSegment(new GraphPathSegment(edge, node));
            node = edge.getOppositeNode(node);
            List<GraphEdge> nextedges = node.getEdgesExcept(edge);
            edge = null;
            if (nextedges.size() == 1) {
                // nur wenns genau eine gibt
                edge = nextedges.get(0);
            }
        }
        while (edge != null);


        return path;
    }

    public GraphNode getStart() {
        return path.get(0).getEnterNode();
    }

    public String getDetailedString() {
        return toString(true);
    }

    /**
     * Prüfen, dass die Enternode in allen Segmenten eine Node des Vorsegments ist.
     *
     * @return
     */
    public String validate() {
        for (int i = 1; i < path.size(); i++) {
            GraphPathSegment seg = path.get(i);
            GraphPathSegment pre = path.get(i - 1);
            if (!isConnectedToPredecessor(seg, pre)) {
                logger.error("unconnected enter node");
                return "unconnected enter node";
            }
        }
        return null;
    }

    private boolean isConnectedToPredecessor(GraphPathSegment seg, GraphPathSegment predecessor) {
        return seg.enternode.equals(predecessor.edge.getFrom()) || seg.enternode.equals(predecessor.edge.getTo());
    }

    public void setName(String name) {
        this.name = name;
    }
}
