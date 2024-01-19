package de.yard.threed.traffic;

import de.yard.threed.core.CharsetException;
import de.yard.threed.core.Degree;
import de.yard.threed.core.EventType;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.Payload;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeNode;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.engine.AmbientLight;
import de.yard.threed.engine.BaseEventRegistry;
import de.yard.threed.engine.DirectionalLight;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ViewPoint;
import de.yard.threed.engine.ecs.DataProvider;
import de.yard.threed.engine.ecs.DefaultEcsSystem;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsGroup;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.platform.common.RequestType;
import de.yard.threed.engine.util.XmlHelper;
import de.yard.threed.graph.GraphProjection;

import de.yard.threed.traffic.config.ConfigAttributeFilter;
import de.yard.threed.traffic.config.ConfigHelper;
import de.yard.threed.traffic.config.SceneConfig;

import de.yard.threed.traffic.config.VehicleDefinition;
import de.yard.threed.traffic.config.ViewpointConfig;
import de.yard.threed.traffic.config.XmlVehicleDefinition;
import de.yard.threed.traffic.geodesy.GeoCoordinate;

import de.yard.threed.traffic.geodesy.SimpleMapProjection;
import de.yard.threed.trafficcore.model.Airport;
import de.yard.threed.trafficcore.model.Vehicle;

import java.util.ArrayList;
import java.util.List;

/**
 * A draft for now. Features are (possibly just an idea):
 * - load/init of a sphere
 * - handling 2D/3D, so its a good location for projection.
 * - manage multiple concurrent spheres (server mode)
 * <p>
 * Optionally triggers loading of a (static?) tile. There might also be a TerrainSystem.
 * Provider for a projection fitting to the Sphere and a world node.
 * 16.11.23: Might also provide a provider for the full configuration, so other systems do not
 * need to read it again. See README.md#DataFlow.
 * <p>
 * 07.10.21
 */
public class SphereSystem extends DefaultEcsSystem implements DataProvider {

    Log logger = Platform.getInstance().getLog(SphereSystem.class);

    // contains optional full qualified tilename and optional vehiclelist
    public static RequestType USER_REQUEST_SPHERE = RequestType.register(4000, "USER_REQUEST_SPHERE");

    public static String TAG = "SphereSystem";

    //projection fuer 2D, bei 3D gibts das nicht.
    //Die Projection ist null bei 3D Scene, obwohl groundnet dann trotzdem projected (Groundnet hat ja seine eigene Projection, die haelt Groundservicessystem vor).
    //7.5.19 Projection ist in 2D erforderlich, weil sowas wie Routen immer erst in 3D erstellt werden.
    //14.10.21 projection aus DefaultTrafficWorld hierhin.
    public SimpleMapProjection projection;

    private boolean needsBackProjection = false;
    EllipsoidCalculations/*Flight3D*/ ellipsoidCalculations;
    GraphBackProjectionProvider backProjection;

    // Das was mal world in TravelScenes war. Eine destination node an der alles(?) haengt, zumindest statischer Content(terrain), der
    // sich damit dann komplett verschieben läßt. Könnte/sollte evtl. per DataProvider zur Verfügung gestellt werden.
    // Kommentar von TravelScene.world: Wofuer ist die world Zwsichenebene. Zum adjusten?
    // 20.3.18: Ja, zum komplettverschieben von allem, um Artefakte wegen Rundungsproblemen zu
    // vermeiden. Das ist was anderes als die Scene.world.
    private static SceneNode sphereNode;

    // 24.10.21:Nur gesetzt bei Nutzung der DefaultTrafficWorld. Provisorium, bis es andere configs gibt. TODO
    public SceneConfig sceneConfig;
    GeoCoordinate center;
    //29.11.21 public Tile activeTile; Stattdessen andere Kruecke.
    public boolean wasOsm;
    private LightDefinition[] lightDefinitions;

    /**
     * backprojection needed for 3D, otherwise null.
     * <p>
     * 27.12.21: SceneConfig (das ist NUR der scene sub Part) und center mal reinstecken.
     */
    public SphereSystem(EllipsoidCalculations/*Flight3D*/ ellipsoidCalculations, GraphBackProjectionProvider backProjection, GeoCoordinate center, SceneConfig sceneConfig) {
        super(new String[]{}, new RequestType[]{USER_REQUEST_SPHERE}, new EventType[]{});
        //??updatepergroup = false;
        this.ellipsoidCalculations = ellipsoidCalculations;
        this.backProjection = backProjection;
        if (ellipsoidCalculations != null) {
            SystemManager.putDataProvider("ellipsoidconversionprovider", new EllipsoidConversionsProvider(ellipsoidCalculations));
        }
        this.sceneConfig = sceneConfig;
        this.center = center;
    }

    @Override
    public void init() {
        sphereNode = new SceneNode();
        sphereNode.setName("TravelSphere");
        Scene.getCurrent().addToWorld(sphereNode);
    }

    /**
     *
     */
    @Override
    public void update(EcsEntity entity, EcsGroup group, double tpf) {
    }

    @Override
    public boolean processRequest(Request request) {
        //if ( keytorequestsystemdebuglog) {
        logger.debug("got request " + request.getType());
        // }

        if (request.getType().equals(USER_REQUEST_SPHERE)) {

            // muss tile kennen/laden um die initialposition und projection festzulegen.
            String basename = (String) request.getPayloadByIndex(0);
            List<Vehicle> vehicleList = (List<Vehicle>) request.getPayloadByIndex(1);
            logger.debug("requested tile:" + basename);
            TrafficSystem.vehiclelist = vehicleList;

            // 7.5.19: Anhand der initialPosition kann Terrain, groundnet, vehicles geladen werden und Projection gesetzt werden.
            // Das ist eigentlich unabhängig vom Avatar, denn dessen Position ist nicht klar, weils Vehicle erst später gibt.
            // Aber anhand der Configuration oder Parameter muesste die Position festgelegt sein.
            // 20.6.20: Das ist aber noch nicht die initiale Teleportposition? 7.10.21 static bis es wegkommt
            /*SGGeod*/
            GeoCoordinate initialPosition = null;

            // das war aber nur in "Flat". Unterscheiden 2D/3D am tilename, den gibts in 3D nicht(?).
            /*Tile*/
            BundleResource initialTile = null;
            List<NativeNode> xmlVPs = null;
            LightDefinition[] lds = lightDefinitions;
            if (basename != null) {
                initialTile = BundleResource.buildFromFullQualifiedString(basename);
                if (initialTile == null) {
                    throw new RuntimeException("Failed to parse tile name (bundle missing?):" + basename);
                }
                /*initialTile = TileKram.findTile(basename);
                if (initialTile == null) {
                    logger.error("unknown tile " + basename);
                } else*/
                {
                    initialPosition = activateTile(initialTile);
                }
                if (initialTile.getExtension().equals("xml")) {
                    // XML only sync for now

                    TrafficConfig xmlConfig = Tile.loadConfigFile(BundleRegistry.getBundle(initialTile.bundlename), initialTile);
                    if (xmlConfig != null) {
                        xmlVPs = xmlConfig.getViewpoints();
                        lds = xmlConfig.getLights();

                        List<NativeNode> xmlVehicles = xmlConfig.getVehicles();
                        if (xmlVehicles.size() > 0) {
                            logger.info("Replacing vehiclelist with list from config file");
                            TrafficSystem.vehiclelist = new ArrayList<Vehicle>();
                            for (NativeNode nn : xmlVehicles) {
                                String name = XmlHelper.getStringAttribute(nn, "name");
                                boolean delayedload = XmlHelper.getBooleanAttribute(nn, XmlVehicleDefinition.DELAYEDLOAD, false);
                                boolean automove = XmlHelper.getBooleanAttribute(nn, XmlVehicleDefinition.AUTOMOVE, false);
                                TrafficSystem.vehiclelist.add(new Vehicle(name, delayedload, automove,
                                        XmlHelper.getStringAttribute(nn, XmlVehicleDefinition.LOCATION),
                                        XmlHelper.getIntAttribute(nn, XmlVehicleDefinition.INITIALCOUNT, 0)));
                            }
                        }
                        List<NativeNode> xmlTerrains = xmlConfig.getTerrains();
                        if (xmlTerrains.size() > 0) {
                            // We have terrain, so no graph visualization needed.
                            ((GraphTerrainSystem) SystemManager.findSystem(GraphTerrainSystem.TAG)).disable();
                        }
                    }
                    for (VehicleDefinition vd : XmlVehicleDefinition.convertVehicleDefinitions(
                            xmlConfig.getVehicleDefinitions())) {
                        TrafficSystem.knownVehicles.add(vd);
                    }
                    // 28.11.23: Was in BasicTravelScene.customInit() before
                    TrafficSystem.baseTransformForVehicleOnGraph = xmlConfig.getBaseTransformForVehicleOnGraph();
                }
            } else {
                //16.6.20 3D: request geht hier noch nicht wegen "not inited". Darum weiter vereinfacht initialposition.
                // Das ist nur erstmal so ungefähr für Terrain. Wird das noch erreicht? Wann?
                //27.12.21 wegen dependency kopiert initialPosition = WorldGlobal.eddkoverviewfar.location.coordinates;
                // SGGeod.fromLatLon(gsw.getAirport("EDDK").getCenter());
                initialPosition = new GeoCoordinate(new Degree(50.843675), new Degree(7.109709), 1150);

            }
            if (lds != null) {
                logger.debug("Adding " + lds.length + " lights");
                for (LightDefinition ld : lds) {
                    if (ld.position != null) {
                        Scene.getCurrent().addLightToWorld(new DirectionalLight(ld.color, ld.position));
                    } else {
                        Scene.getCurrent().addLightToWorld(new AmbientLight(ld.color));
                    }
                }
            }
            if (initialPosition == null) {
                //wait for async service response
                logger.debug("no initial position yet. Waiting for information or no tile found");
            } else {
                // 7.10.21 das war frueher als erstes im update() sowohl 2D wie 3D
                sendInitialEvents(initialPosition, initialTile);
            }

            // 24.10.21: Das kommt jetzt erstmal einfach hier hin.
            // Viewpoints koennte auch TeleporterSystem kennen, aber irgendwo muss ein neu dazugekommener Player sie ja herholen können.
            // 17.11.23: But TeleporterSystem can collect these. Additionally send traffic independent event
            SystemManager.putDataProvider("viewpoints", new SphereViewPointProvider(this, xmlVPs));
            if (xmlVPs != null) {
                // viewpoints from XML
                for (NativeNode nn : xmlVPs) {
                    LocalTransform transform = ConfigHelper.getTransform(nn);
                    SystemManager.sendEvent(BaseEventRegistry.buildViewpointEvent(
                            XmlHelper.getStringAttribute(nn, "name"), transform));
                }
            }
            return true;
        }

        return false;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    public static SceneNode getSphereNode() {
        return sphereNode;
    }

    /**
     * Hier kommt jetzt der Kram, der vorher in Basic/FlatTravelScene war.
     */


    /**
     * // 7.5.19: Anhand der initialPosition kann dann Terrain, groundnet, vehicles geladen werden und Projection gesetzt werden.
     * // Das ist eigentlich unabhängig vom Avatar, denn dessen Position ist nicht klar, weils Vehicle erst später gibt.
     * // Aber anhand der Configuration oder Parameter muesste die Position festgelegt sein.
     * 7.10.21: Nicht mehr durch expliziten Aufruf, sondern per Request. Aber der macht doch eigentlich gar nichts? ausser initialtile setzen.
     * Genau, der setzt nur globale Variablen. Dann kann das auch so bleiben, obwohl es irgendwie hintenrum ist.
     *
     * @return
     */
    private GeoCoordinate activateTile(/*Tile*/BundleResource tile) {
        logger.debug("activateTile " + tile);
        //27.12.21 AirportConfig nearestairport1 = null;
        //das ist ja voelliger Quatsch. Geht nur, weil alles 0 basiert ist, verzerrt nur etwas. TODO 27.12.21 aber mal wirklich
        GeoCoordinate center = /*WorldGlobal.elsdorf0*/new GeoCoordinate(new Degree(50.937770f), new Degree(6.580982f), 0);

        // 5.12.23: Now uses EDDK-flat.xml instead of just "EDDK" from "dummy:EDDK". Still this is a workaround
        if (tile.getName().equals("EDDK") || tile.getName().equals("EDDK-flat.xml")) {
            // korrektes center ist wichtig für die Projection
            center = new GeoCoordinate(new Degree(50.86538f), new Degree(7.139103f), 0);
            // Traditional groundservices scene. Use config if it exists, otherwise use service. 27.12.21 not for now; always assume EDDK
            if (false /*27.12.21 DefaultTrafficWorld.getInstance() == null*/) {
                logger.debug("tile via service");
                Platform.getInstance().httpGet("http://localhost/airport/icao=EDDK", null, null, (response) -> {
                    try {
                        logger.debug("HTTP returned airport. status=" + response.getStatus() + ", response=" + response.getContentAsString());
                        if (response.getStatus() == 0) {
                            Airport airport = JsonUtil.toAirport(response.getContentAsString());
                            //12.10.21 elevation?
                            GeoCoordinate ctr = GeoCoordinate.fromLatLon(airport.getCenter(), 0);
                            sendInitialEvents(ctr, tile);
                        }
                    } catch (CharsetException e) {
                        // TODO improved eror handling
                        throw new RuntimeException(e);
                    }
                });
                // response will be async
                return null;
            } else {
                logger.debug("tile from DefaultTrafficWorld");
                //27.12.21nearestairport1 = DefaultTrafficWorld.getInstance().getConfiguration().getAirportConfig("EDDK");
                //27.12.21 jetzt als Parameter center = GeoCoordinate.fromLatLon(nearestairport1.getCenter(),0);

                // Aus FlatTravel.
                //27.12.21 jetzt als Parameter sceneConfig = DefaultTrafficWorld.getInstance().getConfiguration().getScene("GroundServices");


            }
        }

        /*7.10.21 TODO if (FlatTravelScene.fpc != null) {
            Scene.getCurrent().getDefaultCamera().getCarrier().getTransform().setPosition(tile.location);

        }*/

        /*7.10.21 einfach mal weglassen BasicTravelScene.initialPosition = center;
        DefaultTrafficWorld.getInstance().nearestairport = nearestairport1;
        BasicTravelScene.initialTile = tile;*/

        //29.11.21 activeTile = tile;


        wasOsm = true;
        return center;
    }

    /**
     * 7.10.21: Das war frueher der erste Schritt im update(), sowohl Flat wie 3D.
     * Den initial tile nur noch zu backwar compatibility. TODO weg damit.
     *
     * @param initialPosition
     */
    private void sendInitialEvents(GeoCoordinate initialPosition, /*Tile*/BundleResource initialTile) {
        logger.debug("sendInitialEvents " + initialTile);

        //TrafficTile.loadTile(basename);??
        //Das GraphTerrainSystem wird das terrain laden oder ground visualisieren. GroundNetSystem wird groundnet laden.
        //Wer laedt andere Graphen? Sagen wir doch mal TrafficSystem.

        //16.10.21DefaultTrafficWorld.getInstance().setProjection(projection);
        //DefaultTrafficWorld.getInstance().currentairport = DefaultTrafficWorld.getInstance().nearestairport;
        //DefaultTrafficWorld.getInstance().nearestairport = null;
        //27.3.20: Groundnet jetzt eigenstaendig ueber request, damit es auf terrain warten kann. Das mit der projection ist aber doof.
        SystemManager.sendEvent(TrafficEventRegistry.buildLOCATIONCHANGED(initialPosition, /*27.3.20 projection,*/ /*14.10.21 DefaultTrafficWorld.getInstance().currentairport,*/ null/*initialTile*/,
                (initialTile)));
        SystemManager.sendEvent(TrafficEventRegistry.buildSPHERELOADED(initialTile));

        //24.5.20 Event fuer Ground wird nicht mehr gebraucht, weil Airport per Init da ist und der Rest ueber Pending groundnet geht.
        //wird doch gebraucht, um groundnetXML zu lesen.
        // 16.10.21:Aber initial doch nur, wenn ein icao geladen wird. Zumindest solange tiles so simple sind wie jetzt.Aber 3D immer. Irgendwie frickelig.
        boolean loadEDDK = false;
        if (initialTile == null) {
            // 3D, das ist ja immer erstr nur in EDDK
            loadEDDK = true;
            // only backprojection?
            needsBackProjection = true;
        } else {
            // Die Projection wird hier festgelegt. Ob an der Location ein Airport ist, muss auch hier schon ermittelt werden,
            //weil die Systems nachher das nicht mehr ermitteln können.

            /*SimpleMapProjection*/
            // no backprojection in 2D
            projection = new SimpleMapProjection(/*BasicTravelScene.*/initialPosition);

            if (TrafficHelper.isIcao(initialTile.getName())) {
                // traditional EDDK GroundServices
                loadEDDK = true;
            }
            // 5.12.23: Now uses EDDK-flat.xml instead of just "EDDK" from "dummy:EDDK". Still this is a workaround
            if (initialTile.getName().equals("EDDK-flat.xml")) {
                loadEDDK = true;
            }
        }
        SystemManager.putDataProvider("projection", this);

        if (loadEDDK) {
            SystemManager.putRequest(new Request(RequestRegistry.TRAFFIC_REQUEST_LOADGROUNDNET, new Payload("EDDK"/*trafficWorld.currentairport.icao*/, projection)));
        }

    }

    @Override
    public Object getData(Object[] parameter) {

        //SphereProjections
        return new SphereProjections(projection, (needsBackProjection) ? getBackProjection() : null);

    }

    /**
     * 29.10.21:Moved from TravelScene to here. But GraphProjectionFlight3D needs FgMath.
     *
     * @return
     */
    protected GraphProjection/*Flight3D*/ getBackProjection() {
        if (backProjection/*DefaultTrafficWorld.getInstance()*/ == null) {
            return null;
        }
        //GraphProjectionFlight3D graphprojection = new GraphProjectionFlight3D(DefaultTrafficWorld.getInstance().getGroundNet("EDDK").projection);
        GraphProjection/*Flight3D*/ graphprojection = backProjection.getGraphBackProjection();
        return graphprojection;

    }

    public void setDefaultLightDefinition(LightDefinition[] light) {
        this.lightDefinitions = light;
    }
}

/**
 * War mal in FlatTravelScene
 */
class TileKram {
    //Tiles sind alle aus dem Bundle "osmscenery".
    //TODO tilelist in config? Oder in inputtorequest als Auswahl?
    static Tile[] tilelist = new Tile[]{
            new Tile("EDDK"),
            new Tile("B55-B477-small"),
            new Tile("B55-B477"),
            new Tile("A4A3A1"),
            new Tile("Zieverich-Sued"),
            new Tile("TestData", new Vector3(15, -70, 15)),
            new Tile("EDDK-Small"),
            new Tile("Desdorf", new Vector3(0, 0, 85/*15*/)),
            new Tile("BIKF"),
            new Tile("3056443"),
            new Tile("Wayland"),
    };
    static int major = 0;

    /**
     * 9.5.19: Ich glaube, das lasse ich erstmal, oder? Ziemlich tricky, und man kann ja neu starten.
     * 7.10.21: Genau, und wenn, sollte es ueber Input2Request gehen.
     */
    /*private void cycleMajor(int inc, int cnt) {
        major += inc;
        if (major < 0) {
            major = cnt - 1;
        }
        if (major >= cnt) {
            major = 0;
        }
        logger.info("cycled to " + "." + major);
        Tile tile = tilelist[major];
        unloadCurrentTile();
        activateTile(tile);
    }*/
    public static Tile findTile(String tilename) {
        boolean found = false;
        for (int i = 0; i < tilelist.length; i++) {
            if (tilelist[i].file.equals(tilename)) {
                return tilelist[i];
            }
        }
        return null;
    }
}

/**
 * 18.11.23: Deprecated in favor of USER_EVENT_VIEWPOINT
 */
@Deprecated
class SphereViewPointProvider implements DataProvider {

    Log logger = Platform.getInstance().getLog(SphereViewPointProvider.class);
    SphereSystem sphereSystem;
    List<NativeNode> xmlVPs;

    SphereViewPointProvider(SphereSystem sphereSystem, List<NativeNode> xmlVPs) {
        this.sphereSystem = sphereSystem;
        this.xmlVPs = xmlVPs;
    }

    @Override
    public Object getData(Object[] parameter) {
        if (xmlVPs != null) {
            List<ViewPoint> vps = new ArrayList<ViewPoint>();
            for (NativeNode nn : xmlVPs) {
                vps.add(ConfigHelper.buildViewpoint(nn));
            }
            return vps;
        }

        // "viewpoints"
        logger.debug("getData viewpoints, sceneConfig=" + sphereSystem.sceneConfig);
        if (sphereSystem.sceneConfig == null) {
            logger.debug("no sceneConfig");
            if (sphereSystem.wasOsm) {
                logger.debug("Using outside view points from osmscenery");
                // Outside von oben
                List<ViewPoint> vps = new ArrayList<ViewPoint>();
                vps.add(new ViewPoint("oben1", new LocalTransform(new Vector3(0, 0, 137), Quaternion.buildRotationX(new Degree(/*-9*/0)))));
                vps.add(new ViewPoint("oben1", new LocalTransform(new Vector3(0, 0, 500), Quaternion.buildRotationX(new Degree(0)))));
                vps.add(new ViewPoint("oben2", new LocalTransform(new Vector3(0, 0, 1000), Quaternion.buildRotationX(new Degree(0)))));
                vps.add(new ViewPoint("oben2", new LocalTransform(new Vector3(0, 0, 2000), Quaternion.buildRotationX(new Degree(0)))));
                vps.add(new ViewPoint("oben2", new LocalTransform(new Vector3(0, 0, 4000), Quaternion.buildRotationX(new Degree(0)))));
                vps.add(new ViewPoint("oben2", new LocalTransform(new Vector3(0, 0, 8000), Quaternion.buildRotationX(new Degree(0)))));
                vps.add(new ViewPoint("oben2", new LocalTransform(new Vector3(0, 0, 16000), Quaternion.buildRotationX(new Degree(0)))));
                vps.add(new ViewPoint("oben2", new LocalTransform(new Vector3(0, 0, 32000), Quaternion.buildRotationX(new Degree(0)))));
                vps.add(new ViewPoint("oben2", new LocalTransform(new Vector3(0, 0, 64000), Quaternion.buildRotationX(new Degree(0)))));
                return vps;
            }
            return null;
        }
        // From FlatTravel EDDK. 5.12.23: Pseudo tile "dummy:EDDK" no longer used.
        if (true) throw new RuntimeException("needs XSD config");
        List<ViewpointConfig> viewPointConfigs = null;//27.11.23 sphereSystem.sceneConfig.getViewpoints(new ConfigAttributeFilter("icao", "EDDK", true));
        List<ViewPoint> viewPoints = new ArrayList<ViewPoint>();
        for (ViewpointConfig vcfg : viewPointConfigs) {
            viewPoints.add(new ViewPoint(vcfg.name, vcfg.transform));
        }
        return viewPoints;
    }
}

