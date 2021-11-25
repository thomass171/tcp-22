package de.yard.threed.graph;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thomass on 24.05.17.
 */
public class GraphPathSegment {
    public GraphEdge edge;
    GraphNode enternode;
    boolean changeorientation = false;

    public GraphPathSegment(GraphEdge edge, GraphNode enternode) {
        this.edge = edge;
        this.enternode = enternode;
    }

    public GraphNode getLeaveNode() {
        return edge.getOppositeNode(enternode);
    }

    public GraphNode getEnterNode() {
        return enternode;
    }

    @Override
    public String toString() {
        return edge.toString();
    }
}
