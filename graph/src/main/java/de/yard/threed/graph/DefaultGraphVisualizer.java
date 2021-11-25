package de.yard.threed.graph;

import de.yard.threed.engine.Scene;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.core.Vector3;

/**
 * Default Visualizer, der nichts sichtbares darstellt.
 * <p>
 * Created by thomass on 12.05.17.
 */
public class DefaultGraphVisualizer implements GraphVisualizer {
    //18.11.20 protected Scene scene;
    GraphPathVisualizer graphPathVisualizer = null;
    //just optional See super interface.
    SceneNode destinationnode = null;

    /*18.11.20 public DefaultGraphVisualizer(Scene scene) {
        this.scene = scene;
        this.destinationnode = null;
    }

    public DefaultGraphVisualizer(Scene scene, SceneNode destinationnode) {
        this.scene = scene;
        this.destinationnode = destinationnode;
    }*/

    public DefaultGraphVisualizer() {
    }

    public DefaultGraphVisualizer(SceneNode destinationnode) {
        this.destinationnode = destinationnode;
    }

    @Override
    public void visualize/*Graph*/(Graph graph, SceneNode destinationnode) {
        for (int i = 0; i < graph.getNodeCount(); i++) {
            GraphNode n = graph.getNode(i);
            SceneNode node = buildNode(graph, n);
            if (node != null) {
                //18.11.20 scene.addToWorld(node);
                destinationnode.attach(node);
            }
        }
        for (int i = 0; i < graph.getEdgeCount(); i++) {
            GraphEdge n = graph.getEdge(i);
            SceneNode e = buildEdge(graph, n);
            if (e != null) {
                //18.11.20 scene.addToWorld(e);
                destinationnode.attach(e);
            }
        }
    }

    @Override
    public SceneNode getDestinationNode() {
        return destinationnode;
    }

    public SceneNode buildNode(Graph graph, GraphNode n) {
        return new SceneNode();
    }

    public SceneNode buildEdge(Graph graph, GraphEdge edge) {
        return new SceneNode();
    }

    /**
     * Declarations for C#
     */

    @Override
    public Vector3 getPositionOffset() {
        return new Vector3();
    }

    @Override
    public void visualizePath(Graph graph, GraphPath path, SceneNode destinationnode) {
        if (graphPathVisualizer != null) {
            graphPathVisualizer.visualizePath(graph, path, destinationnode);
        }
    }

    @Override
    public SceneNode visualizeEdge(Graph graph, GraphEdge edge, SceneNode destinationnode) {
        return null;
    }

    @Override
    public void addLayer(Graph g, int layer, SceneNode destinationnode) {
    }

    @Override
    public void removeLayer(Graph graph, int layer) {
    }

    @Override
    public void removeVisualizedPath(Graph graph, GraphPath path) {
    }
}
