package de.yard.threed.graph;

import de.yard.threed.engine.SceneNode;

public interface GraphPathVisualizer {
    public void visualizePath(Graph graph, GraphPath path, SceneNode destinationNode);
}
