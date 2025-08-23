package de.yard.threed.traffic;

import de.yard.threed.core.*;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeNode;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.engine.*;
import de.yard.threed.engine.ecs.*;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.platform.common.RequestType;
import de.yard.threed.engine.util.XmlHelper;
import de.yard.threed.graph.GraphProjection;

import de.yard.threed.traffic.config.ConfigHelper;

import de.yard.threed.traffic.config.VehicleDefinition;
import de.yard.threed.traffic.config.XmlVehicleDefinition;

import de.yard.threed.trafficcore.EllipsoidCalculations;
import de.yard.threed.trafficcore.geodesy.MapProjection;
import de.yard.threed.trafficcore.geodesy.SimpleMapProjection;
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
 * Provider for a projection (if not 3D) fitting to the Sphere and a world node.
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

    //projection for 2D only. Needed because routes etc are always created in 3D.
    //It is null in 3D Scenes, even though groundnet is projected (Groundnet has its own projection in Groundservicessystem).
    public SimpleMapProjection projection;

    //25.5.24 private boolean needsBackProjection = false;
    // 10.5.24: Moved to AbstractSceneryBuilder EllipsoidCalculations/*Flight3D*/ ellipsoidCalculations;
    GraphBackProjectionProvider backProjectionProvider;

    // Das was mal world in TravelScenes war. Eine destination node an der alles(?) haengt, zumindest statischer Content(terrain), der
    // sich damit dann komplett verschieben läßt. Könnte/sollte evtl. per DataProvider zur Verfügung gestellt werden.
    // Kommentar von TravelScene.world: Wofuer ist die world Zwsichenebene.
    private static SceneNode sphereNode;
    //29.11.21 public Tile activeTile; Stattdessen andere Kruecke.
    public boolean wasOsm;
    // Have a 'world' for easy adjusting/moving/scaling and to avoid rounding artifacts
    // Different to Scene.world, which is only a technical layer.
    // 11.5.24: Moved here from 3D scenes. Might also be useful for shrinking in AR/VR
    public SceneNode world;

    /**
     * backProjectionProvider needed for 3D (only for groundnet?), otherwise null.
     * <p>
     */
    public SphereSystem(GraphBackProjectionProvider backProjectionProvider) {
        super(new String[]{}, new RequestType[]{USER_REQUEST_SPHERE}, new EventType[]{});
        this.backProjectionProvider = backProjectionProvider;

        // need to be set in contructor because other will use it before init()
        world = new SceneNode();
        world.setName("FlightWorld");
        // 16.5.24 addToWorld was in Scenes before, but now seems better here
        Scene.getCurrent().addToWorld(world);
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

        logger.debug("got request " + request.getType());

        if (request.getType().equals(USER_REQUEST_SPHERE)) {

            // muss tile kennen/laden um die initialposition und projection festzulegen.
            String basename = (String) request.getPayloadByIndex(0);
            List<Vehicle> vehicleList = (List<Vehicle>) request.getPayloadByIndex(1);
            if (vehicleList == null) {
                // just to be sure to avoid NPE
                vehicleList = new ArrayList<Vehicle>();
            }
            logger.debug("requested tile '" + basename + "' with " + vehicleList.size() + " vehicles");

            // 7.5.19: By initialPosition terrain, groundnet, vehicles can be loaded and projection gesetzt werden.
            // Independent from avatar.
            // 20.6.20: This is also not the initial teleport position
            GeoCoordinate initialPosition = null;

            BundleResource initialTile = null;
            List<NativeNode> xmlVPs = null;
            //15.3.25 lightDefinitions always from config
            LightDefinition[] lds = null;
            String groundnetToLoad = null;
            // 18.3.24: null no longer leads hard coded to 3D in EDDK but is accepted but ignored
            if (basename != null) {
                initialTile = BundleResource.buildFromFullQualifiedString(basename);
                if (initialTile != null) {
                    // found regular tilename

                    initialPosition = activateTile(initialTile);

                    if (initialTile.getExtension().equals("xml")) {
                        // XML only sync for now

                        TrafficConfig xmlConfig = Tile.loadConfigFile(BundleRegistry.getBundle(initialTile.bundlename), initialTile);
                        if (xmlConfig != null) {
                            xmlVPs = xmlConfig.getViewpoints();
                            lds = xmlConfig.getLights();

                            List<NativeNode> xmlVehicles = xmlConfig.getVehicles();
                            if (xmlVehicles.size() > 0) {
                                logger.info("Replacing vehiclelist with list from config file");
                                vehicleList = new ArrayList<Vehicle>();
                                for (NativeNode nn : xmlVehicles) {
                                    String name = XmlHelper.getStringAttribute(nn, "name");
                                    boolean delayedload = XmlHelper.getBooleanAttribute(nn, XmlVehicleDefinition.DELAYEDLOAD, false);
                                    boolean automove = XmlHelper.getBooleanAttribute(nn, XmlVehicleDefinition.AUTOMOVE, false);
                                    vehicleList.add(new Vehicle(name, delayedload, automove,
                                            XmlHelper.getStringAttribute(nn, XmlVehicleDefinition.LOCATION),
                                            XmlHelper.getIntAttribute(nn, XmlVehicleDefinition.INITIALCOUNT, 0)));
                                }
                            }
                            List<NativeNode> xmlTerrains = xmlConfig.getTerrains();
                            if (xmlTerrains.size() > 0) {
                                // We have terrain, so no graph visualization needed.
                                GraphTerrainSystem graphTerrainSystem = ((GraphTerrainSystem) SystemManager.findSystem(GraphTerrainSystem.TAG));
                                if (graphTerrainSystem != null) {
                                    graphTerrainSystem.disable();
                                }
                            }
                            // 17.5.24: Look for groundnets for which a LOAD request should be sent
                            List<NativeNode> xmlTrafficgraphs = xmlConfig.getTrafficgraphs();
                            if (xmlTrafficgraphs.size() > 0) {
                                for (NativeNode nn : xmlTrafficgraphs) {
                                    groundnetToLoad = XmlHelper.getStringAttribute(nn, "groundnet");
                                }
                            }
                            List<NativeNode> xmlProjections = xmlConfig.getProjections();
                            if (xmlProjections.size() > 0) {

                                String xmlCenter = XmlHelper.getStringAttribute(xmlProjections.get(0), "center");
                                GeoCoordinate center = GeoCoordinate.parse(xmlCenter);
                                projection = new SimpleMapProjection(center);

                            }
                        } // end of XML config
                        for (VehicleDefinition vd : XmlVehicleDefinition.convertVehicleDefinitions(
                                xmlConfig.getVehicleDefinitions())) {
                            //TrafficSystem.knownVehicles.add(vd);
                            // 7.3.25 no longer static, but should use event. Especially during tests TrafficSystem might not be available.
                            if (SystemManager.findSystem(TrafficSystem.TAG) != null) {
                                ((TrafficSystem) SystemManager.findSystem(TrafficSystem.TAG)).addKnownVehicle(vd);
                            }
                        }
                        // 28.11.23: Was in BasicTravelScene.customInit() before
                        //11.7.24 TrafficSystem.baseTransformForVehicleOnGraph = xmlConfig.getBaseTransformForVehicleOnGraph();
                        // 16.5.24: scenerybuilder now in config which also provides ellipsoidconversionprovider
                        String scenerybuilder = xmlConfig.findSceneryBuilder();
                        if (scenerybuilder != null) {
                            AbstractSceneryBuilder sceneryBuilder = BuilderRegistry.buildSceneryBuilder(scenerybuilder);
                            EllipsoidCalculations ellipsoidCalculations = sceneryBuilder.getEllipsoidCalculations();
                            if (ellipsoidCalculations != null) {
                                SystemManager.putDataProvider("ellipsoidconversionprovider", new EllipsoidConversionsProvider(ellipsoidCalculations));
                            }
                            ((ScenerySystem) SystemManager.findSystem(ScenerySystem.TAG)).setTerrainBuilder(sceneryBuilder);
                        }
                    }
                } else {
                    // basename is no bundle resource (tilename). Assume geo coordinate.
                    //18.3.24: Now we get initialPosition from basename.
                    //15.5.24: basename as initialPosition might be deprecated now as also 3D scenes use (tile) configs
                    initialPosition = GeoCoordinate.parse(basename);
                    logger.debug("Retrieved initialPosition from basename: " + initialPosition);
                    // 24.5.24: Try again to exit here
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
                // 26.2.24: vehicleList was public static in TrafficSystem before. Provider might not be perfect, but better at least.
                List<Vehicle> providerVehicleList = vehicleList;
                SystemManager.putDataProvider("vehiclelistprovider", parameter -> providerVehicleList);

                if (initialPosition == null) {
                    //wait for async service response. 18.3.24: Do we really have async here? If yes, request processing should be aborted and retried.
                    //23.5.24: we don't have it now. So exit with exception
                    throw new RuntimeException("no initial position yet. Waiting for information or no tile found");
                }
                // 7.10.21 das war frueher als erstes im update() sowohl 2D wie 3D
                sendInitialEvents(initialPosition, initialTile, groundnetToLoad);


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
            } else {
                logger.warn("ignoring null tilename");
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
     * <p>
     * 23.5.24: Returns projection center or(!!) initialPosition
     *
     * @return
     */
    private GeoCoordinate activateTile(/*Tile*/BundleResource tile) {
        logger.debug("activateTile " + tile);

        //Complete nonsense??. TODO 27.12.21 really fix it
        GeoCoordinate center = /*WorldGlobal.elsdorf0*/new GeoCoordinate(new Degree(50.937770f), new Degree(6.580982f), 0);

        // 5.12.23: Now uses EDDK-flat.xml instead of just "EDDK" from "dummy:EDDK". Still this is a workaround
        if (tile.getName().equals("EDDK") || tile.getName().equals("EDDK-flat.xml")) {
            // korrektes center ist wichtig für die Projection
            center = new GeoCoordinate(new Degree(50.86538f), new Degree(7.139103f), 0);
            // Traditional groundservices scene. Use config if it exists, otherwise use service. 27.12.21 not for now; always assume EDDK
        }

        // 15.5.24: Now also 3D scenes use (tile) configs instead of initialPosition via tilename. But for now keep it hard coded here.
        if (StringUtils.endsWith(tile.getName(), "EDDK-sphere.xml")) {
            // 18.3.24 From former hard coded EDDK setup.
            GeoCoordinate formerInitialPositionEDDK = new GeoCoordinate(new Degree(50.843675), new Degree(7.109709), 1150);
            return GeoCoordinate.parse(formerInitialPositionEDDK.toString());
        }

        wasOsm = true;
        return center;
    }

    /**
     * 7.10.21: Was once first step in update(), flat and 3D. 'groundnetToLoad' comes from config.
     * Shouldn't need initialTile?? TODO remove.
     *
     * @param initialPosition
     */
    private void sendInitialEvents(GeoCoordinate initialPosition, /*Tile*/BundleResource initialTile, String groundnetToLoad) {
        logger.debug("sendInitialEvents initialPosition=" + initialPosition + ", initialTile=" + initialTile);

        // Send event to trigger eg. graph loading in other systems. Groundnet however is ICAO specific and comes from config via 'groundnetToLoad'.
        // 10.5.24: Isn't this too early. Should be at least at end of method.
        SystemManager.sendEvent(TrafficEventRegistry.buildSPHERELOADED(initialTile, initialPosition));

        // 14.5.24: Now 3D EDDK might also use config files
        boolean loadEDDK = false;
        // 23.5.24: Now decide by defined projection. But theres nothing to decide? loadEDDK and needsBackProjection are no longer used?
        //if (initialTile == null || StringUtils.endsWith(initialTile.getName(), "EDDK-sphere.xml") || StringUtils.endsWith(initialTile.getName(), "Travel-sphere.xml")) {
        if (projection == null) {
            // only backprojection?
            //25.5.24 needsBackProjection = true;
        } else {

        }
        // 23.5.24 set provider independent from having a projection.
        SystemManager.putDataProvider("projection", this);

        if (groundnetToLoad != null) {
            // 23.2.24: Use builder with historic default values. No longer projection.
            //SystemManager.putRequest(new Request(RequestRegistry.TRAFFIC_REQUEST_LOADGROUNDNET, new Payload("EDDK"/*trafficWorld.currentairport.icao*/, projection)));
            SystemManager.putRequest(RequestRegistry.buildLoadGroundnet(groundnetToLoad == null ? "EDDK" : groundnetToLoad));
        }

    }

    /**
     * 22.3.24: Parameter 'forwardProjection' added for providing a fitting backprojection (3D only).
     */
    @Override
    public Object getData(Object[] parameter) {

        //SphereProjections
        // 18.5.24: Not up to us to decide whether backprojection is needed. Let the scene
        // and consumer decide
        return new SphereProjections(projection, (true/*needsBackProjection*/) ? getBackProjectionByProvider((MapProjection) parameter[0]) : null);

    }

    /**
     * 29.10.21:Moved from TravelScene to here. But GraphProjectionFlight3D needs FgMath.
     *
     * @return
     */
    protected GraphProjection/*Flight3D*/ getBackProjectionByProvider(MapProjection forwardProjection) {
        if (backProjectionProvider == null) {
            return null;
        }
        //GraphProjectionFlight3D graphprojection = new GraphProjectionFlight3D(DefaultTrafficWorld.getInstance().getGroundNet("EDDK").projection);
        GraphProjection/*Flight3D*/ graphprojection = backProjectionProvider.getGraphBackProjection(forwardProjection);
        return graphprojection;

    }

    /**
     * 23.5.24 Extracted code from activateTile() for future use. Was never used yet
     */
    private void getSphereViaHttp(BundleResource tile) {
        logger.debug("tile via service");
        Platform.getInstance().httpGet("http://localhost/airport/icao=EDDK", null, null, (response) -> {
            try {
                logger.debug("HTTP returned airport. status=" + response.getStatus() + ", response=" + response.getContentAsString());
                if (response.getStatus() == 0) {
                    Airport airport = JsonUtil.toAirport(response.getContentAsString());
                    //12.10.21 elevation?
                    GeoCoordinate ctr = GeoCoordinate.fromLatLon(airport.getCenter(), 0);
                    sendInitialEvents(ctr, tile, null);
                }
            } catch (CharsetException e) {
                // TODO improved eror handling
                throw new RuntimeException(e);
            }
        });
        // response will be async
    }
}

/**
 * Was in FlatTravelScene once. Only kept to remember what spheres we once had.
 */
class TileKram {
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
        logger.debug("no viewpoints");
        /* 16.3.24: Assume hard coded viewpoints are no longer needed
        logger.debug("getData hard coded viewpoints");
                Util.nomore();
        if (sphereSystem.wasOsm) {
            logger.debug("Using outside view points from osmscenery");

            List<ViewPoint> vps = new ArrayList<ViewPoint>();
            vps.add(new ViewPoint("oben1", new LocalTransform(new Vector3(0, 0, 137), Quaternion.buildRotationX(new Degree(/*-9* /0)))));
            vps.add(new ViewPoint("oben1", new LocalTransform(new Vector3(0, 0, 500), Quaternion.buildRotationX(new Degree(0)))));
            vps.add(new ViewPoint("oben2", new LocalTransform(new Vector3(0, 0, 1000), Quaternion.buildRotationX(new Degree(0)))));
            vps.add(new ViewPoint("oben2", new LocalTransform(new Vector3(0, 0, 2000), Quaternion.buildRotationX(new Degree(0)))));
            vps.add(new ViewPoint("oben2", new LocalTransform(new Vector3(0, 0, 4000), Quaternion.buildRotationX(new Degree(0)))));
            vps.add(new ViewPoint("oben2", new LocalTransform(new Vector3(0, 0, 8000), Quaternion.buildRotationX(new Degree(0)))));
            vps.add(new ViewPoint("oben2", new LocalTransform(new Vector3(0, 0, 16000), Quaternion.buildRotationX(new Degree(0)))));
            vps.add(new ViewPoint("oben2", new LocalTransform(new Vector3(0, 0, 32000), Quaternion.buildRotationX(new Degree(0)))));
            vps.add(new ViewPoint("oben2", new LocalTransform(new Vector3(0, 0, 64000), Quaternion.buildRotationX(new Degree(0)))));
            return vps;
        }*/
        return null;
    }
}

