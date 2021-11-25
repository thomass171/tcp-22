package de.yard.threed.graph;


import de.yard.threed.core.Vector3;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thomass on 13.09.16.
 */
public class GraphNode {
    String name;
    private Vector3 location;
    public List<GraphEdge> edges = new ArrayList<GraphEdge>();
    Log logger = Platform.getInstance().getLog(GraphNode.class);
    public GraphComponent customdata;
    static private int uniqueid = 1;
    private int id = uniqueid++;
    //optional, eg. for outlined nodes
    public GraphNode parent;

    /**
     * Constructor nur fuer Aufruf aus Graph
     *
     * @param nodes
     */
    GraphNode(List<GraphNode> nodes, String name, Vector3 location) {
        this.name = name;
        this.location = location;
        nodes.add(this);
    }

    public Vector3 getLocation() {
        return location;
    }

    /**
     * 2.8.18: Eigentlich nicht zuässig, weil damit die edges (z.B. len,arc) kaputt gehen können.
     * Wer das akzeptiert, kann es aber trotzdem aufrufen.
     *
     * @param l
     */
    @Deprecated
    public void setLocationOnlyForSpecialPurposes(Vector3 l) {
        this.location = l;
    }

    public void addEdge(GraphEdge edge) {
        edges.add(edge);
    }

    public void removeEdge(GraphEdge edge) {
        //GraphEdge overrides equals
        edges.remove(edge);
    }

    public GraphEdge getFirstFromEdge() {
        for (GraphEdge e : edges) {
            if (e.from == this) {
                return e;
            }
        }
        return null;
    }

    public int getEdgeCount() {
        return edges.size();
    }

    public GraphEdge getEdge(int index) {
        return edges.get(index);
    }

    public List<GraphEdge> getEdgesExcept(GraphEdge edge) {
        List<GraphEdge> l = new ArrayList<GraphEdge>();
        for (GraphEdge e : edges) {
            if (e != edge) {
                l.add(e);
            }
        }
        return l;
    }

    public String getName() {
        return name;
    }

    public List<GraphNode> getNeighbors() {
        List<GraphNode> neighbors = new ArrayList<GraphNode>();
        for (GraphEdge e : edges) {
            neighbors.add(e.getOppositeNode(this));
        }
        return neighbors;
    }

    /**
     * Wichtig fuer remove.
     *
     * @param e
     * @return
     */
    @Override
    public boolean equals(Object e) {
        return ((GraphNode) e).id == this.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return getName();
    }

    public GraphEdge findEdge(GraphEdge edge) {
        for (GraphEdge e : edges) {
            if (e.equals(edge)) {
                return e;
            }
        }
        return null;
    }


}
