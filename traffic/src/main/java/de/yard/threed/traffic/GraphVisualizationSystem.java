package de.yard.threed.traffic;


import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ecs.DefaultEcsSystem;
import de.yard.threed.engine.ecs.EcsGroup;
import de.yard.threed.graph.Graph;
import de.yard.threed.graph.GraphEventRegistry;
import de.yard.threed.graph.GraphPath;
import de.yard.threed.graph.GraphVisualizer;


/**
 * Ein Event ECS System für Graphdarstellung, to be more precise "tracking" visualization. Ist ein System, weil sich im Graph ja immer mal was aendern kann, was dargestellt werden soll (z.B. smoothing path).
 * 1.3.18: Nicht fix an einen graph gebunden.
 * <p>
 * 17.11.20: Der Name passt nicht ganz, weil es gar nicht um Terrain Darstellung geht, sondern um moving paths/tracks. TrackVisualizationSystem waere besser.
 * Es gibt noch den {@link GraphTerrainVisualizer} bzw. {@link GraphTerrainSystem}.
 *
 * <p>
 * Created by thomass on 25.07.17.
 */
public class GraphVisualizationSystem extends DefaultEcsSystem {
    private Log logger = Platform.getInstance().getLog(GraphVisualizationSystem.class);
    GraphVisualizer visualizer;
    public static String TAG = "GraphVisualizationSystem";

    /**
     *
     */
    public GraphVisualizationSystem(GraphVisualizer visualizer) {
        super(new EventType[]{GraphEventRegistry.GRAPH_EVENT_PATHCREATED, GraphEventRegistry.GRAPH_EVENT_PATHCOMPLETED, GraphEventRegistry.GRAPH_EVENT_LAYERCREATED, GraphEventRegistry.GRAPH_EVENT_LAYERREMOVED
                /*EventRegistry.GROUNDNET_EVENT_LOADED*/});
        this.visualizer = visualizer;
    }

    @Override
    public void process(Event evt) {
        logger.debug("got event " + evt.getType());
        if (evt.getType().equals(GraphEventRegistry.GRAPH_EVENT_PATHCREATED)) {
            Graph graph = castGraph(evt.getPayloadByIndex(0));
            GraphPath path = (GraphPath) evt.getPayloadByIndex(1);
            //path.getSegment(1).edge.from.location = new Vector3();
            visualizer.visualizePath(graph, path, getDestinationNode());
        }
        if (evt.getType().equals(GraphEventRegistry.GRAPH_EVENT_PATHCOMPLETED)) {
            Graph graph = castGraph(evt.getPayloadByIndex(0));
            GraphPath path = (GraphPath) evt.getPayloadByIndex(1);
            visualizer.removeVisualizedPath(graph, path);
        }
        if (evt.getType().equals(GraphEventRegistry.GRAPH_EVENT_LAYERCREATED)) {
            Graph graph = castGraph(evt.getPayloadByIndex(0));
            int layer = (int) (Integer) evt.getPayloadByIndex(1);
            visualizer.addLayer(graph, layer, getDestinationNode());
        }
        if (evt.getType().equals(GraphEventRegistry.GRAPH_EVENT_LAYERREMOVED)) {
            Graph graph = castGraph(evt.getPayloadByIndex(0));
            int layer = (int) (Integer) evt.getPayloadByIndex(1);
            visualizer.removeLayer(graph, layer);
        }
       /*12.11.18  if (evt.getType().equals(EventRegistry.GROUNDNET_EVENT_LOADED)) {
            Object[] objs = (Object[]) evt.getPayload();
            GroundNet gn = (GroundNet) objs[0];
            //graph = gn.groundnetgraph;
        }*/
    }

    private Graph castGraph(Object payloadByIndex) {
        if (payloadByIndex instanceof Graph){
            return (Graph) payloadByIndex;
        }
        return ((TrafficGraph)payloadByIndex).getBaseGraph();
    }

    @Override
    public void init(EcsGroup group) {

    }

    @Override
    public String getTag() {
        return TAG;
    }

    private SceneNode getDestinationNode() {
        if (visualizer.getDestinationNode() != null) {
            return visualizer.getDestinationNode();
        }
        return Scene./*21.10.21 getCurrent().*/getCurrent().getWorld();
    }
}
