package de.yard.threed.graph;

import de.yard.threed.engine.apps.ModelSamples;
import de.yard.threed.core.Util;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.core.Vector3;

import de.yard.threed.core.Color;

/**
 * Visualizer, der als lines darstellt.
 * <p>
 * Created by thomass on 12.02.18.
 */
public class SimpleGraphVisualizer extends DefaultGraphVisualizer {

    public SimpleGraphVisualizer() {
    }

    public SimpleGraphVisualizer(SceneNode destinationnode) {
        super(destinationnode);
    }

    @Override
    public SceneNode buildNode(Graph graph, GraphNode n) {
        return new SceneNode();
    }

    @Override
    public SceneNode buildEdge(Graph graph, GraphEdge edge) {
        return buildEdge(edge, Color.ORANGE);
    }

    private SceneNode buildEdge(GraphEdge edge, Color color) {
        SceneNode sn = buildEdge(edge, color, null);
        return sn;
    }

    public static SceneNode buildEdge(GraphEdge edge, Color color, /*MA31 ist eh null MapProjection*/Double projection) {
        if (edge.getArc() == null) {
            return ModelSamples.buildLine(project(edge.getFrom().getLocation(), projection), project(edge.getTo().getLocation(), projection), color);
        }
        int segments = 16;
        SceneNode container = new SceneNode();
        double segsize = edge.getLength() / segments;
        for (int i = 0; i < segments; i++) {
            Vector3 from = edge.get3DPosition(i * segsize);
            Vector3 to = edge.get3DPosition((i + 1) * segsize);
            /*scene.addToWorld*/
            container.attach(ModelSamples.buildLine(project(from, projection), project(to, projection), color));
        }
        return container;
    }

    private static Vector3 project(Vector3 p, /*MA31 ist eh null MapProjection*/Double projection) {
        if (projection == null) {
            return p;
        }
        //TODO muesste Vector3 sein.
        //9.3.21 MA31 hier gibts keine Projection mehr
        Util.notyet();
        //MA31 Vector2 v = projection.project(SGGeod.fromCart(p));
        //MA31 return new Vector3(v.getX(), v.getY(), 0);
        return null;
    }

    /**
     * Declarations for C#
     */

    /*@Override
    public Vector3 getPositionOffset() {
        return new Vector3();
    }*/

    @Override
    public void visualizePath(Graph graph,  GraphPath path, SceneNode destinationnode) {
        SceneNode container = new SceneNode();

        for (int i = 0; i < path.getSegmentCount(); i++) {
            GraphPathSegment s = path.getSegment(i);
            GraphEdge edge = s.edge;
            SceneNode sn = buildEdge(edge, Color.BLUE);
            container.attach(sn);

        }
        //if (destinationnode != null) {
            destinationnode.attach(container);
        //} else {
         //18.11.20   scene.addToWorld(container);
        //}
    }

    @Override
    public SceneNode visualizeEdge(Graph graph,  GraphEdge edge, SceneNode destinationnode) {
        return null;
    }

    @Override
    public void addLayer(Graph g, int layer, SceneNode destinationnode) {

    }

    @Override
    public void removeLayer(Graph graph, int layer) {

    }

    @Override
    public void removeVisualizedPath(Graph graph,   GraphPath path) {

    }
}
