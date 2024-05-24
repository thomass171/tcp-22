package de.yard.threed.traffic;


import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.traffic.geodesy.MapProjection;
import de.yard.threed.graph.DefaultGraphVisualizer;
import de.yard.threed.graph.Graph;
import de.yard.threed.graph.GraphEdge;
import de.yard.threed.graph.GraphNode;
import de.yard.threed.graph.GraphOrientation;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.Color;
import de.yard.threed.traffic.osm.TerrainBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 25.2.18: Aus GroundServiceVisualizer extrahiert.
 * <p>
 * Wird auch von osmScene verwendet, aber etwas andere Optionen.
 * Kann bestimmt zu einem TrafficGraphVisalizer o.ä. ausgebaut werden.
 * 20.3.18: Offen ist, wie zusaetzliche Layer visualisiert werden.
 * <p>
 * Created by thomass on 04.05.17.
 */
public class GraphTerrainVisualizer extends DefaultGraphVisualizer {
    private Map<Integer, List<SceneNode>> visuals = new HashMap<Integer, List<SceneNode>>();
    private Map<Integer, List<SceneNode>> visualPaths = new HashMap<Integer, List<SceneNode>>();
    private double taxiwaywidth = 20f;
    private Log logger = Platform.getInstance().getLog(GraphTerrainVisualizer.class);
    public boolean showtaxiways = true;
    public MapProjection projection;
    //18.11.20 Graph graph;
    //27.12.21: Das mit dem terrainbuilder ist etwas krude hinundher.oder?
    AbstractSceneryBuilder terrainBuilder;

    public GraphTerrainVisualizer(/*Graph graph/*,Scene scene*/) {
        this(20f);
    }

    public GraphTerrainVisualizer(AbstractSceneryBuilder terrainBuilder) {
        this(20);
        this.terrainBuilder = terrainBuilder;
    }

    public GraphTerrainVisualizer(/*Graph graph/*,Scene scene*/ double taxiwaywidth) {
        //super(scene);
        this.taxiwaywidth = taxiwaywidth;
        //18.11.20  this.graph=graph;
    }

    @Override
    public void visualize/*Graph*/(Graph graph, SceneNode destinationnode) {
        for (int i = 0; i < graph.getNodeCount(); i++) {
            GraphNode n = graph.getNode(i);
            SceneNode node = buildNode(graph, n);
            if (node != null) {
                //scene.addToWorld(node);
                destinationnode.attach(node);
            }
        }
        for (int i = 0; i < graph.getEdgeCount(); i++) {
            GraphEdge n = graph.getEdge(i);
            SceneNode e = buildEdge(graph, n);
            if (e != null) {
                //scene.addToWorld(e);
                destinationnode.attach(e);
            }
        }
    }

    /**
     * Nodes evtl., je nach dem, nicht darstellen.
     *
     * @param n
     * @return
     */
    @Override
    public SceneNode buildNode(Graph graph, GraphNode n) {
        return terrainBuilder.buildParkingNode(n);

    }

    public SceneNode buildEdge(GraphEdge edge, Color color, double width, double elevation, GraphOrientation graphOrientation) {
        SceneNode model = TerrainBuilder.buildEdgeArea(edge, width, color, elevation, graphOrientation);
        model.setName(edge.getName());
        return model;
    }

    /**
     * 12.7.17: Ein simples Taxiwaysegment.
     */
    @Override
    public SceneNode buildEdge(Graph graph, GraphEdge edge) {
        SceneNode segment = new SceneNode();
        double taxiwayMarkerwidth = 0.1f;
        // fuer groundnet etwas groesser.
        taxiwayMarkerwidth = 1;
        if (showtaxiways) {
            // Der z Wert für die Taxiwaymarkierung muss relativ hoch sein, warum auch immer.
            segment.attach(buildEdge(edge, Color.YELLOW, taxiwayMarkerwidth, 0.3f, graph.orientation));
        }
        // Asphalt ueber Textur
        segment.attach(buildEdge(edge, null, taxiwaywidth, 0, graph.orientation));
        return segment;
    }


}
