package de.yard.threed.graph;

/**
 * Created by thomass on 13.09.16.
 */
public interface GraphValidator {
    boolean nodesValidForEdge(GraphNode n1, GraphNode n2);
}
