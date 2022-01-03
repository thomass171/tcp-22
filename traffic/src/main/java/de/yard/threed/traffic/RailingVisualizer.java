package de.yard.threed.traffic;

import de.yard.threed.core.Degree;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.Transform;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector3;
import de.yard.threed.graph.DefaultGraphVisualizer;
import de.yard.threed.graph.Graph;
import de.yard.threed.graph.GraphEdge;
import de.yard.threed.graph.GraphNode;
import de.yard.threed.graph.GraphPath;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.MathUtil2;
import de.yard.threed.graph.RailingFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by thomass on 04.05.17.
 */
public class RailingVisualizer extends DefaultGraphVisualizer {
    private Map<Integer, List<SceneNode>> visuals = new HashMap<Integer, List<SceneNode>>();
    private Log logger = Platform.getInstance().getLog(RailingVisualizer.class);
    static Vector3 refVectorRechtsForRotation = new Vector3(1, 0, 0);
    // Wir nehmen eine y=0 Ebene an. Eine Rotation ist dann nicht erforderlich, wenn ein Vector exakt nach hinten zeigt.
    static Vector3 refVectorForRotation = new Vector3(0, 0, -1);


    public RailingVisualizer() {

    }

    @Override
    public void visualize(Graph graph, SceneNode destinationnode) {
        //13.11.18: Eine Zwischennode einziehen
        SceneNode tracks = new SceneNode("tracks");
        //scene.addToWorld(tracks);
        destinationnode.attach(tracks);

        for (int i = 0; i < graph.getNodeCount(); i++) {
            GraphNode n = graph.getNode(i);
            SceneNode node = buildNode(graph, n);
            if (node != null) {
                tracks.attach(node);
            }
        }
        for (int i = 0; i < graph.getEdgeCount(); i++) {
            GraphEdge n = graph.getEdge(i);
            SceneNode e = buildEdge(graph, n);
            if (e != null) {
                tracks.attach(e);
            }
        }

    }

    /**
     * Nodes erstmal nicht darstellen.
     *
     * @param n
     * @return
     */
    @Override
    public SceneNode buildNode(Graph graph, GraphNode n) {
        return null;
    }

    @Override
    public SceneNode buildEdge(Graph graph, GraphEdge edge) {
        float width = 1;
        float w2 = width / 2;
        GraphNode from = edge.getFrom();
        GraphNode to = edge.getTo();
        int segments = 32;
        double len = edge.getLength();
        double len2 = len / 2;
        SceneNode model;
        if (edge.getCenter() != null) {
            Vector3 c = edge.getCenter();
            Degree angle = new Degree(90);
            // angle berechnen dürfte erstmal nur in der einen Ebene gehen, aber immerhin
            // angle ist ungeeignet für die spätere Rotation, weil absolut. Dafuer besser Rotation
            angle = Degree.buildFromRadians(to.getLocation().subtract(c).getAngleBetween(from.getLocation().subtract(c)));
            //19.4.17: der angle aus edge ist evtl. negativ, das führt ducrh den CircleExtruder aber wohl zu falschen Faces?
            //angle = edge.getAngle();
            model = RailingFactory.buildRails(angle, edge.getArc().getRadius());

            // der Shape wurde "radius" nach rechts verschoben. Dadurch passt das
            // Rotationszentrum zu dem Bogencenter
            //model.getTransform().rotateOnAxis(new Vector3(0,1,0),angle);
            //19.4.17: from statt to??
            Quaternion rotation = Quaternion.buildQuaternion(refVectorRechtsForRotation, ((edge.getAngle().getDegree() < 0) ? to.getLocation() : from.getLocation()).subtract(c));
            model.getTransform().setRotation(rotation);
            model.getTransform().setPosition(c);

            double[] angles = new double[3];
            rotation.toAngles(angles);

            logger.debug("angles.y " + angles[1] + " for center " + c + " to to " + to.getLocation());
            
            /*Vector3 destination = from.getLocation().subtract(new Vector3(edge.radius, 0, 0));
            model.getTransform().setPosition(destination);
            SceneNode rotnode = new SceneNode();
            rotnode.getTransform().rotateOnAxis(new Vector3(0,1,0),angle);
                    
            rotnode.add(model);
            model = rotnode;*/
        } else {
            model = RailingFactory.buildRails(len);
            // In der richtigen Orientierung an die richtige Stelle legen.
            adjustTransform(model.getTransform(), from.getLocation(), to.getLocation(), true);
        }
        model.setName("node");
        return model;

    }

    private void adjustTransform(Transform tf, Vector3 from, Vector3 to, boolean iszoriented) {
        // float w2=width/2;
        double len = Vector3.getDistance(from, to);
        Vector3 diff = to.subtract(from);
        //TODO richtiges up
        Quaternion rotation = (MathUtil2.buildQuaternion(refVectorForRotation, diff));
        tf.setPosition(from.add(diff.multiply(0.5f)));
        tf.setRotation(rotation);
    }

    @Override
    public Vector3 getPositionOffset() {
        // Die Loc muss auf den Schienenkopf
        return new Vector3(0, RailingFactory.headheight, 0);
    }


    @Override
    public void visualizePath(Graph graph, GraphPath path, SceneNode destinationnode) {
        Util.notyet();


    }

    @Override
    public SceneNode visualizeEdge(Graph graph, GraphEdge e, SceneNode destinationnode) {
        Util.notyet();
        return null;
    }

    @Override
    public void addLayer(Graph g, int layer, SceneNode destinationnode) {
        visuals.put(layer, new ArrayList<SceneNode>());
        for (int i = 0; i < g.getEdgeCount(); i++) {
            GraphEdge e = g.getEdge(i);
            if (e.getLayer() == layer) {
                visuals.get(layer).add(visualizeEdge(g, e, destinationnode));
            }
        }
    }

    @Override
    public void removeLayer(Graph g, int layer) {
        List<SceneNode> nodes = visuals.get(layer);
        for (SceneNode n : nodes) {
            SceneNode.removeSceneNode(n);
        }
    }

    @Override
    public void removeVisualizedPath(Graph graph, GraphPath path) {
        Util.notyet();
    }
}
