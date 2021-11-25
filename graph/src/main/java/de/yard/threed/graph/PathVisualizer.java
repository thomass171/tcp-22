package de.yard.threed.graph;

import de.yard.threed.engine.Material;
import de.yard.threed.engine.Mesh;
import de.yard.threed.core.Quaternion;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.core.Vector3;
import de.yard.threed.engine.geometry.ShapeGeometry;
import de.yard.threed.core.Color;
import de.yard.threed.core.MathUtil2;

/**
 * Visualisierung eines Graph mit Darstellung des up-Vektors.
 */
public class PathVisualizer extends DefaultGraphVisualizer {
   public PathVisualizer(Scene scene) {
       //18.11.20 super(scene);

    }

    @Override
    public SceneNode buildNode(Graph graph, GraphNode n) {
        return new SceneNode();
    }

    /**
     *
     *
     * @param edge
     * @return
     */
    @Override
    public SceneNode buildEdge(Graph graph, GraphEdge edge) {
        GraphNode from = edge.getFrom();
        GraphNode to = edge.getTo();
        int segments = 32;
        double len = edge.getLength();
        // Die tube tuts noch nicht richtig
        ShapeGeometry geoTube ;
        geoTube = ShapeGeometry.buildBox(0.1f, 0.1f, len, null);
        Material mat = Material.buildLambertMaterial(new Color(0, 0, 250));
        Mesh mesh = new Mesh(geoTube, mat);
        SceneNode model = new SceneNode();
        model.setMesh(mesh);
        model.setName("node");
        Vector3 half = to.getLocation().subtract(from.getLocation()).multiply(0.5f);
        model.getTransform().setPosition(from.getLocation().add(half));
        Quaternion rotation = (MathUtil2.buildLookRotation(half, new Vector3(0, 1, 0)));
        model.getTransform().setRotation(rotation);
        return model;


    }
}
