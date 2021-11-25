package de.yard.threed.graph;

/**
 * Created on 21.11.18.
 */
public interface GraphPathConstraintProvider {
    double getMinimumLength();
    double getSmoothingRadius(GraphNode graphNode);
}
