package de.yard.threed.traffic;

import de.yard.threed.core.*;

import de.yard.threed.core.platform.Platform;

import de.yard.threed.engine.Scene;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ecs.DefaultEcsSystem;
import de.yard.threed.engine.ecs.EcsGroup;
import de.yard.threed.engine.ecs.TeleporterSystem;
import de.yard.threed.traffic.geodesy.GeoCoordinate;
import de.yard.threed.traffic.geodesy.MapProjection;

import de.yard.threed.traffic.geodesy.SimpleMapProjection;
import de.yard.threed.graph.*;
import de.yard.threed.core.platform.Log;
import de.yard.threed.trafficcore.model.Runway;


/**
 * 2D Terrain mit einer Projektion aus 3D oder einfach aus einem 2D SceneryModelFile. Wirklich nur zur Terraindarstellung,
 * evtl. generiert aus einem Graph. Hat sonst aber nichts mit Graphen zu tun. Und damit auch nicht mit dem Laden eines TrafficTile,
 * das ja auch Graphen enthalten kann. Ohne dieses System fehlt nur das Terrain, es funktioniert aber alles.
 * <p>
 * Das ist das 2D Gegenstück zum {link TerrainSystem}. 9.3.21: Wirklich?
 * <p>
 * <p>
 * 20.7.18: Auch zur Nutzung in OsmSceneryScene. Dafuer muss es aber mehr von Groundnet entkoppelt werden.
 * Die Projection ergibt sich erst, wenn die Location bekannt ist (per Event).
 * <p>
 * 17.11.20: Auch mal fuer Railing versuchen, bzw. alle solche halt, die keine echte Scenery haben.
 * Darum jetzt eher per TRAFFIC_EVENT_GRAPHLOADED.
 * Und umbenannt FlatTerrainSystem->GraphTerrainSystem. Das trifft es besser, weil es ja nicht zwingend "flat" sein muss.
 * Aber es ist ja auch für 2D scenery. Hmm, das ist aber unguenstig. Dann braeuchte man doch ein FlatTerrainSystem
 * * Gesplittet. Das ist aber wohl auch weil hier ein grounnet graph per projection geflattet wird. Das muesste aber doch nicht sein.
 * <p>
 * 03.10.21: Nur Behelf wegen dürftiger Visualisierung und fehlender Elevation. Weiterhin als einfache Visualisierung eines (Traffic)Graphen (per GROUNDNET_EVENT_LOADED).
 * Groundnet ist eigentlich nur ein Spezialfall davon. Das Event muesste da wohl etwas geaendert werden. Oder TRAFFIC_EVENT_GRAPHLOADED?
 * 09.12.21: Ob das wirklich ein eigenes System sein sollte? Vielleicht schon, mal sehen.
 * <p>
 * Created by thomass on 23.02.18.
 */
public class GraphTerrainSystem extends DefaultEcsSystem {
    Log logger = Platform.getInstance().getLog(GraphTerrainSystem.class);
    //10.12.21 SceneNode world, earth;
    public MapProjection projection;
    //der ist jetzt evtl. mehrfach vorhanden.
    //13.12.21 public /*20.11.20 GraphTerrain*/ GraphVisualizer visualizer;
    //String basename;
    //10.12.21 Scene scene;
    //AirportConfig airport = null;
    SceneNode terrain = null;

    public static String TAG = "GraphTerrainSystem";

    public boolean enabled = true;

    private AbstractTerrainBuilder terrainBuilder;

    public GraphTerrainSystem(/*10.12.21 Scene scene, SceneNode world/*, SGGeod origin* /, MapProjection projection, AirportConfig airport*/AbstractTerrainBuilder terrainBuilder) {
        super(new EventType[]{TeleporterSystem.EVENT_POSITIONCHANGED, TrafficEventRegistry.GROUNDNET_EVENT_LOADED, TrafficEventRegistry.EVENT_LOCATIONCHANGED, TrafficEventRegistry.TRAFFIC_EVENT_GRAPHLOADED});
        //10.12.21 this.scene = scene;
        //10.12.21 this.world = world;
        // this.projection = projection;
        // this.airport = airport;
        //projection = new SimpleMapProjection(origin);
        this.terrainBuilder=terrainBuilder;
    }


    /**
     * Hier jetzt den aktuellen Airport darstellen. TODO: Abhäaenig von icao.
     * 7.5.19: Das ist noch etwas zu statisch, nur im init() das Terrain darzustellen. Sollte ueber sowas wie load-Event gehen.
     * Andererseits gibt es ja noch keinen 2D Tilewechsel.
     *
     * @param gr
     */
    @Override
    public void init(EcsGroup gr) {

    }

    /**
     * @param evt
     */
    @Override
    public void process(Event evt) {
        if (true) {
            logger.debug("got event " + evt.getType());
        }
        if (evt.getType().equals(TrafficEventRegistry.GROUNDNET_EVENT_LOADED) && enabled) {
            // Groundnet wurde geladen. Als 2D Terrain darstellen.
            // 7.5.19: Aber nur, wenn kein anderes Terrain vorliegt TODO
            //18.11.20: Das koennte evtl. in einen GroundnetVisualizer? Nee, besser zweiteilig 20.11.20 ob das wirklich besser wäre?
            // Naja, ein Groundnetgraphdecorator vielleicht, sowas koennte auch fuer Railing gehen

            terrainBuilder.buildTerrain(evt.getPayloadByIndex(0),evt.getPayloadByIndex(1),projection);

        }
        if (evt.getType().equals(TrafficEventRegistry.EVENT_LOCATIONCHANGED) && enabled) {
            GeoCoordinate initialPosition = (GeoCoordinate) evt.getPayloadByIndex(0);

            //projection = (SimpleMapProjection) evt.getPayloadByIndex(1);
            //7.10.21 Tile initialTile = (Tile) evt.getPayloadByIndex(2);
            projection = new SimpleMapProjection(initialPosition);
        }

        if (evt.getType().equals(TrafficEventRegistry.TRAFFIC_EVENT_GRAPHLOADED) && enabled) {

            TrafficGraph graph = (TrafficGraph) evt.getPayloadByIndex(0);
            String cluster = (String) evt.getPayloadByIndex(1);

            GraphVisualizer visualizer =null;
            if (cluster.equals(TrafficGraph.RAILWAY)) {
                visualizer = new RailingVisualizer();
            }
            //13.12.21: TODO: osmScene needs road?
            buildTerrainFromGraph(visualizer, graph.getBaseGraph());
        }
    }

    /**
     * The base function.
     * Erstmal fuer Railing
     * 20.11.20
     */
    private void buildTerrainFromGraph(GraphVisualizer visualizer, Graph graph) {
        logger.debug("Building terrain from graph "+graph.getName());
        if (visualizer == null) {
            logger.warn("no visualizer");
            return;
        }
        SceneNode destinationNode=new SceneNode();
        Scene.getCurrent().addToWorld(destinationNode);
        visualizer.visualize/*Graph*/(graph /*13.12.21 visualizer.getDestinationNode()*/,destinationNode);
    }

    /*public void setBasename(String basename) {
        this.basename = basename;
    }*/

    public void removeAll() {
        if (terrain != null) {
            SceneNode.removeSceneNode(terrain);
            terrain = null;
        }
    }

    public void disable() {
        logger.debug("Disabling GraphTerrainSystem");
        enabled = false;
    }

    @Override
    public String getTag() {
        return TAG;
    }
}
