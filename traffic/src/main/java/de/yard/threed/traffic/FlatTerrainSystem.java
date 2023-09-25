package de.yard.threed.traffic;

import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.Util;
import de.yard.threed.core.platform.NativeDocument;
import de.yard.threed.core.platform.NativeNode;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.engine.*;
import de.yard.threed.engine.ecs.TeleporterSystem;
import de.yard.threed.engine.ecs.DefaultEcsSystem;
import de.yard.threed.engine.ecs.EcsGroup;
import de.yard.threed.engine.util.XmlHelper;
import de.yard.threed.traffic.config.ConfigHelper;
import de.yard.threed.traffic.geodesy.GeoCoordinate;
import de.yard.threed.traffic.geodesy.MapProjection;
import de.yard.threed.traffic.geodesy.SimpleMapProjection;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;

import java.util.List;


/**
 * 2D Terrain mit einer Projektion aus 3D oder einfach aus einem 2D SceneryModelFile. Wirklich nur zur Terraindarstellung,
 * evtl. generiert aus einem Graph. Hat sonst aber nichts mit Graphen zu tun. Und damit auch nicht mit dem Laden eines TrafficTile,
 * das ja auch Graphen enthalten kann. Ohne dieses System fehlt nur das Terrain, es funktioniert aber alles.
 * <p>
 * Das ist das 2D Gegenstück zum {link TerrainSystem}.
 * <p>
 * <p>
 * 20.7.18: Auch zur Nutzung in OsmSceneryScene. Dafuer muss es aber mehr von Groundnet entkoppelt werden.
 * Die Projection ergibt sich erst, wenn die Location bekannt ist (per Event).
 * <p>
 * 17.11.20: Auch mal fuer Railing versuchen, bzw. alle solche halt, die keine echte Scenery haben.
 * Darum jetzt eher per TRAFFIC_EVENT_GRAPHLOADED.
 * Und umbenannt FlatTerrainSystem->GraphTerrainSystem. Das trifft es besser, weil es ja nicht zwingend "flat" sein muss.
 * Aber es ist ja auch für 2D scenery. Hmm, das ist aber unguenstig. Dann braeuchte man doch ein FlatTerrainSystem
 * Gesplittet.
 * 7.10.21: For displaying single (2D) tiles (Tile2.0) in general.
 * SphereSystem meanwhile knows the projection. And there is the idea of AbstractTerrainBuilder (but that in not for flat).
 * 20.9.23: Idea of merge with Terrain/ScenerySystem discarded (MA49).
 *
 * <p>
 * Created by thomass on 23.02.18.
 */
public class FlatTerrainSystem extends DefaultEcsSystem {
    Log logger = Platform.getInstance().getLog(FlatTerrainSystem.class);
    //20.10.21SceneNode world, earth;
    // 29.11.21: Projection only if there is an initialPosition that indicates a projection is needed.
    public MapProjection projection;
    //String basename;
    //20.10.21Scene scene;
    //AirportConfig airport = null;
    SceneNode terrain = null;

    public FlatTerrainSystem() {
        super(new EventType[]{TeleporterSystem.EVENT_POSITIONCHANGED, TrafficEventRegistry.GROUNDNET_EVENT_LOADED, TrafficEventRegistry.EVENT_LOCATIONCHANGED, TrafficEventRegistry.TRAFFIC_EVENT_GRAPHLOADED});
        //20.10.21 this.scene = scene;
        //20.10.21 this.world = world;
        // this.projection = projection;
        // this.airport = airport;
        //projection = new SimpleMapProjection(origin);
    }

    /**
     * 21.7.18: Constructor fuer Scenery file
     * 9.5.19: Wird wegen basename nicht mehr gehen.
     *
     */
    /*20.10.21public FlatTerrainSystem(Scene scene, SceneNode world, String basename) {
        // EVENT_POSITIONCHANGED event ist bestimmt mal ganz gut.
        // Und wenn GROUNDNET_EVENT_LOADED mal fuer Graph ist, das auch.
        super(new EventType[]{TeleporterSystem.EVENT_POSITIONCHANGED, TrafficEventRegistry.GROUNDNET_EVENT_LOADED});
        Util.nomore();
        this.scene = scene;
        this.world = world;
        //   this.basename = basename;
        //projection = new SimpleMapProjection(origin);
    }*/

    /**
     * Hier jetzt den aktuellen Airport darstellen. TODO: Abhäaenig von icao.
     * 7.5.19: Das ist noch etwas zu statisch, nur im init() das Terrain darzustellen. Sollte ueber sowas wie load-Event gehen.
     * Andererseits gibt es ja noch keinen 2D Tilewechsel.
     *
     * @param gr
     */
    @Override
    public void init(EcsGroup gr) {
        /*7.5.19 jetzt im process if (basename != null) {
            Bundle bundle = BundleRegistry.getBundle("osmscenery");

            BundleResource resource = new BundleResource(bundle, "tiles/" + basename + ".gltf");
            SceneNode node = ModelFactory.asyncModelLoad(resource);
            //addToWorld(node);
            world.attach(node);
        }*/
    }

    /**
     * @param evt
     */
    @Override
    public void process(Event evt) {
        if (true) {
            logger.debug("got event " + evt.getType());
        }

        if (evt.getType().equals(TrafficEventRegistry.EVENT_LOCATIONCHANGED)) {
            GeoCoordinate initialPosition = (GeoCoordinate) evt.getPayloadByIndex(0);
            // 28.10.21 index 1 is deprecated  nearestairport and thus always null.
            //projection = (SimpleMapProjection) evt.getPayloadByIndex(1);
            Tile initialTile = (Tile) evt.getPayloadByIndex(/*16.10.21 2 mal 1 doch wider 2*/1);
            BundleResource tileResource = (BundleResource) evt.getPayloadByIndex(2);

            SceneNode destinationNode = SphereSystem.getSphereNode();
            if (tileResource != null) {
                logger.debug("Loading tile 2.0 " + tileResource);
                // Tile 2.0
                if (tileResource.getExtension().equals("xml")) {
                    loadTileByConfigFile(tileResource);
                } else {
                    // 29.11.21 Das mit dem Bundle ist doch noch Murks wegen preload needed.
                    // 16.12.21 Ueberhaupt ist byConvetion Murks. So viel convention kann es doch gar nicht geben, z.B. vehicleList, light
                    Util.nomore();
                    loadTileTerrainByConvention(tileResource, destinationNode);
                }
            } else {
                // 30.11.21: Still needed for EDDK?
                projection = new SimpleMapProjection(initialPosition);

                if (initialTile == null) {
                    logger.debug("Ignoring null tile");
                    return;
                }
                if (TrafficHelper.isIcao(initialTile.file)) {
                    // handled by GroundServicesSystem? Sollte wahrscheinlich besser auch ein Tile sein.
                    logger.debug("Ignoring icao tile");
                    return;
                }
                logger.debug("Loading initial tile " + initialTile.file);
                String basename = initialTile.file;

                Bundle bundle = BundleRegistry.getBundle("osmscenery");
                if (bundle == null) {
                    logger.error("no bundle 'osmscenery'");
                    return;
                }
//        BundleData bundleData = bundle.getResource(new BundleResource(gnet));
                //      Graph osm = GraphFactory.buildfromXML(bundleData.getContentAsString());

                BundleResource resource = new BundleResource(bundle, "tiles/" + basename + ".gltf");

                loadTileTerrainByConvention(resource, destinationNode);


            }
            // 28.10.21: TrafficGraph aus einem Tile wird im TrafficSystem geladen. 29.11.21: Jetzt nicht mehr

        }
    }

    private void loadTileByConfigFile(BundleResource tileResource) {
        NativeDocument xmlConfig = Tile.loadConfigFile(tileResource);
        loadObjects(XmlHelper.getChildren(xmlConfig, "object"));
        List<NativeNode> xmlTerrains = XmlHelper.getChildren(xmlConfig, "terrain");
        for (NativeNode nn : xmlTerrains) {
            loadObjects(XmlHelper.getChildren(nn, "object"));
        }
    }

    private void loadObjects(List<NativeNode> xmlObjects) {

        for (NativeNode nn : xmlObjects) {
            String modelfile = XmlHelper.getStringAttribute(nn,"modelfile",null);
            if (modelfile!=null){

                logger.debug("Loading modelfile "+modelfile);
                BundleResource br = BundleResource.buildFromFullQualifiedString(modelfile);
                br = new BundleResource(BundleRegistry.getBundle(br.bundlename),br.getFullName());
                SceneNode destinationNode=ModelFactory.asyncModelLoad(br);
                //TODO set name, but on which node?
                LocalTransform transform = ConfigHelper.getTransform(nn);
                destinationNode.getTransform().setPosition(transform.position);
                destinationNode.getTransform().setRotation(transform.rotation);
                destinationNode.getTransform().setScale(transform.scale);
                Scene.getCurrent().addToWorld(destinationNode);
            }
        }
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

    /**
     * Here instead of in class Tile to make clear where it is used and to have it private instead of public static.
     * Also make event sending more closer to this system.
     * <p>
     * Loading graphs is still separately in TrafficSystem (see header). Even without a FlatTerrainSystem traffic works.
     * 16.12.21 Ueberhaupt ist byConvention Murks. So viel convention kann es doch gar nicht geben, z.B. vehicleList. Darum deprecated
     * <p>
     * 29.11.21
     */
    @Deprecated
    private void loadTileTerrainByConvention(BundleResource tileResource, SceneNode destinationNode) {
        Bundle bundle = BundleRegistry.getBundle(/*"osmscenery"*/tileResource.bundlename);
        if (bundle == null) {
            logger.error("no bundle '" + tileResource.bundlename + "'");
            return;
        }
        String fullBasename = tileResource.getFullName();
        BundleResource baseResource = new BundleResource(bundle, fullBasename + ".gltf");
        // TODO why async?
        terrain = ModelFactory.asyncModelLoad(baseResource);
        //addToWorld(node);
        if (destinationNode == null) {
            logger.warn("no destination node");
        } else {
            destinationNode.attach(terrain);
        }
    }
}
