package de.yard.threed.graph;

/**
 * Created by thomass on 27.05.17.
 */
public interface GraphWeightProvider {
    double getWeight(GraphNode n1, GraphNode n2);
}
