package de.yard.threed.traffic;


import de.yard.threed.core.BooleanHolder;
import de.yard.threed.core.CharsetException;
import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.core.LatLon;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.Payload;
import de.yard.threed.core.platform.NativeNode;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleData;
import de.yard.threed.core.resource.BundleHelper;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.engine.*;
import de.yard.threed.engine.avatar.AvatarComponent;
import de.yard.threed.engine.ecs.*;
import de.yard.threed.engine.util.XmlHelper;
import de.yard.threed.graph.*;
import de.yard.threed.engine.platform.common.*;
import de.yard.threed.traffic.config.VehicleConfigDataProvider;
import de.yard.threed.traffic.config.VehicleDefinition;

import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.util.NearView;


import de.yard.threed.traffic.flight.FlightRouteGraph;
import de.yard.threed.trafficcore.GeoRoute;
import de.yard.threed.trafficcore.model.SmartLocation;
import de.yard.threed.trafficcore.model.Vehicle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Logically handles all generic traffic (but no advanced like ground services). Movement of vehicles is controlled by the GraphMovingSystem.
 * <p>
 * Provides:
 * - loading of graphs, eg. from Tiles (Tile20)
 * - pooling all known traffic graphs and serving these as DataProvider
 * - load vehicles
 * <p>
 * Warum? Damit es jemanden gibt, der zum Abschluss von Movements neue auslösen kann.
 * 9.5.17: Einer muss ja die Trafficsteuerung übernehmen. Und der muss updaten() und auch auf Events reagieren (z.B. Vehicle hat Ziel erreicht).
 *
 * <p>
 * Die Requests (z.B. Followme Anforderung) können hier als Event reinkommen.
 * Hier werden auch die Entities angelegt (über buildVehicle(), aber nicht die Model) und vorgehalten. Wobei vorgehalten werden muessen sie ja doch nicht wirklich.
 * Ob das so ganz sauber ist, muss sich noch zeigen.
 * <p>
 * Requests beginnen mit "request".
 * 15.7.17: Wenn dies ein ECS System per Vehicle ist, sollte es nicht alle Vehicles kennen. Dafür muesste es ein weiteres ECSSystem ohne ... geben. --> TrafficController
 * Der ist aber hier aufgegangen. Der SystemManager kennt alle Entities. Das duerfte reichen.
 * 12.02.2018: Sollte in 2D(GroundServices) wie in 3D(FlighScene) nutzbar sein. Das der immer ein groundnet kennt, ist eigentlich zu speziell. Hier kann es
 * auch mehrere Graphen geben; oder Rundfluege etc. Das sollte
 * zB. mit TrafficWorld entkoppelt werden. Oder ist das hier eigentlich ein GroundServiceSystem? Das könnte auch gut passen. Z.B. wegen der ganzen GroundService
 * spezifischen Use Cases. Dann kann er ein startendes Vehicle an ein anderes System übergeben oder übergeben bekommen?
 * 20.2.18: Und er könnte erkennen, wann ein Groundnet zu laden ist. Jetzt umbenannt: TrafficSystem->GroundServicesSystem.
 * 23.2.18: Nicht mehr Master der Projection. Die wird reingegeben (statt origin, das muesste hier irrelevant sein).
 * 27.2.18. Aber wenn das hier für GroundServices ist, gehoert Schedule und sowas nicht hier hin. Also brauchts doch zusaetzlich noch ein TrafficSystem, in dem
 * schedules liegen. Das hatte ich oben auch schon mal erwähnt. Ich starte mal eine Aufsplittung. Also gibts jetzt ein TrafficSystem und ein GroundServicesSystem.
 * Requests muessen auch gesplittet werden? Ich lass erstmal nur die schedules hier.
 * 27.2.18: TODO Dies System brauchts doch nur per group?
 * 14.3.19: Neues eigenes AutomoveSystem. Evtl. ist das TrafficSystem obselet, weil zu allgemein?
 * 24.11.20: Nicht obselet? Replaces RequestHandler in TrafficCommon zu ersetzen.
 * 07.03.25: VehicleConfigDataProvider integrated here
 * <p>
 * Created by thomass on 31.03.17.
 */
public class TrafficSystem extends DefaultEcsSystem implements DataProvider {
    public static String TAG = "TrafficSystem";

    //27.2.18:Kruecke
    private static TrafficSystem instance = null;
    private static Log logger = Platform.getInstance().getLog(TrafficSystem.class);
    // Der Visualizer kann auch null sein. Er ist hier bekannt, weil ja je nach Traffic etwas neues anzuzeigen sein kann.
    // 12.2.18: Dafuer gibt es aber doch das GraphVisualizationSystem, das über Event informiert wird. Dann sollte nur
    // der den Visualizer kennen. Wenn das Terrain stimmig ist, ist vielleicht ohnehin nur was zu Debugzwecken anzuzeigen.
    //20.3.18 ich glaub, der braucht keinen    GraphVisualizer visualizer;
    public static boolean trafficsystemdebuglog = true;
    //public Map<Integer, Schedule> schedules = new HashMap<Integer, Schedule>();
    //long lastscheduling = 0;
    //static int schedulinginterval = 2;
    public List<VehicleBuiltDelegate> genericVehicleBuiltDelegates = new ArrayList<VehicleBuiltDelegate>();
    // 19.11.23 ugly workaround for testing until we have requests in eventqueue
    public int vehiclesLoaded = 0;

    VehicleLoader vehicleLoader = new SimpleVehicleLoader();

    Map<String, TrafficGraph> trafficgraphs = new HashMap<String, TrafficGraph>();

    // 27.12.21 Not possible as parameter aslong as groundnet and airport are unknown
    public static TrafficContext trafficContext;

    private Map<String, AbstractTrafficGraphFactory> graphFactories = new HashMap<String, AbstractTrafficGraphFactory>();
    int nextlocationindex = 0;

    /**
     * 31.10.23: After moving processing of TRAFFIC_REQUEST_LOADVEHICLE from BasicTravelScene to here,
     * data is missing. These are the fields formerly in BasicTravelScene with null values.
     */
    public /*4.12.23ConfigNodeList*/ List<SmartLocation> locationList;
    public NearView nearView;

    // 27.11.23: Knows vehicle already, should also know vehicle definitions and provide these. static for now, but
    // should be per event.
    private List<VehicleDefinition> knownVehicles = new ArrayList<VehicleDefinition>();
    private boolean sphereloaded = false;
    private boolean userAssembled = false;

    /**
     * weil er neue Pfade im Graph hinzufuegt, lauscht er auch auf GRAPH_EVENT_PATHCOMPLETED, um diese wieder aus dem
     * Graph zu entfernen.(20.3.18:immer noch, wirklich ??)
     * 8.5.19: Die Layerentfernung macht jetzt der EventSender
     */
    public TrafficSystem() {
        super(new String[]{VehicleComponent.TAG},
                new RequestType[]{
                        RequestRegistry.TRAFFIC_REQUEST_VEHICLE_MOVE,
                        RequestRegistry.TRAFFIC_REQUEST_LOADVEHICLE,
                        RequestRegistry.TRAFFIC_REQUEST_LOADVEHICLES},
                new EventType[]{
                        GraphEventRegistry.GRAPH_EVENT_PATHCOMPLETED,
                        TrafficEventRegistry.TRAFFIC_EVENT_VEHICLELOADED,
                        TrafficEventRegistry.TRAFFIC_EVENT_SPHERE_LOADED,
                        // 20.3.24 TRAFFIC_EVENT_GRAPHLOADED added
                        TrafficEventRegistry.TRAFFIC_EVENT_GRAPHLOADED,
                        BaseEventRegistry.EVENT_USER_ASSEMBLED});
        //this.visualizer = visualizer;

        instance = this;
        this.name = "TrafficSystem";
    }

    /**
     * 27.2.18: Vorlauefige Kruecke, um schedule von aussen verfuegbar zu haben.
     *
     * @return
     */
    public static TrafficSystem getInstance() {
        /*if (instance==null){
            instance=new TrafficSystem();
        }*/
        return instance;
    }

    @Override
    public void init() {
        SystemManager.putDataProvider("trafficgraph", this);

        //27.11.23: Knows defs and should provide these
        //7.3.25  SystemManager.putDataProvider("vehicleconfig", new VehicleConfigDataProvider(knownVehicles));
    }

    @Override
    public void init(EcsGroup group) {

    }

    @Override
    final public void update(EcsEntity entity, EcsGroup group, double tpf) {
        if (group == null) {
            return;
        }
        VehicleComponent vhc = VehicleComponent.getVehicleComponent(entity);
        GraphMovingComponent gmc = GraphMovingComponent.getGraphMovingComponent(entity);

        // check for completed movment
        GraphPath p;
        // 13.2.18: Einen Request hier im Entity update kann aber auch etwas unpassend sein.
        /*if (request != null) {
            switch (request.type) {
                case 'f':
                    /*if (vhc.type == VehicleComponent.VEHICLE_FOLLOME) {
                        createFollowMe(request.aircraft.entity, group.entity, request.from, request.destination);
                        request = null;
                    }* /
                    break;
                case 'm':
                    if (vhc.type.equals(request.vehicletype)) {
                        spawnMoving(group.entity, request.destination.node);
                        request = null;
                    }
                    break;
                /*ueber event case 'c':
                    if (vhc.type.equals(request.vehicletype)) {
                        spawnMoving(group.entity, request.destination.node);
                        request = null;
                    }
                    break;* /
                case 'd':
                    //depart
                    if (group.entity.equals(request.aircraft.entity)) {
                        spawnMoving(group.entity, request.holding);
                        request = null;
                    }
                    break;
            }
        }*/

        /*if (lastscheduling < ((Platform)Platform.getInstance()).currentTimeMillis() - schedulinginterval * 1000) {
            lastscheduling = ((Platform)Platform.getInstance()).currentTimeMillis();

            // check for completed actions and schedules.
            //C# kann keine Iterator wie Java

            List<Integer> skeys = new ArrayList<Integer>(schedules.keySet());
            for (int i : skeys) {
                Schedule s = schedules.get(i);
                for (int j = s.actions.size() - 1; j >= 0; j--) {
                    ScheduledAction a = s.actions.get(j);
                    if (a.isActive() && a.checkCompletion()) {
                        //actionsactive.remove(i);
                    }
                }
                ScheduledAction action = s.next();
                if (action != null) {
                    action.trigger();
                    //actionsactive.add(action);
                }
                if (s.checkCompletion()) {
                    logger.info("schedule " + s + " completed. " + (schedules.size() - 1) + " schedules remaining");
                    s.delete();
                }
            }

            // check for completed SPs
            skeys = new ArrayList<Integer>(GroundServicesSystem.servicepoint.keySet());
            for (int i : skeys) {
                ServicePoint sp = GroundServicesSystem.servicepoint.get(i);
                if (sp.cateringschedule.isCompleted() && sp.fuelschedule.isCompleted()) {
                    sp.delete();
                }
            }
        }*/


    }

    @Override
    public boolean processRequest(Request request) {
        if (trafficsystemdebuglog) {
            logger.debug("got request " + request.getType());
        }
        if (request.getType().equals(RequestRegistry.TRAFFIC_REQUEST_VEHICLE_MOVE)) {
            EcsEntity vehicle = (EcsEntity) request.getPayloadByIndex(0);
            TrafficGraph trafficGraph = (TrafficGraph) request.getPayloadByIndex(1);
            GraphNode destination = (GraphNode) request.getPayloadByIndex(2);

            if (TrafficHelper.spawnMoving(vehicle, destination, trafficGraph)) {
                if (!vehicle.lockEntity(this)) {
                    logger.error("Lock vehicle failed");
                }
            }
            // Auch bei failure erledigt setzen.
            return true;
        }
        if (request.getType().equals(RequestRegistry.TRAFFIC_REQUEST_LOADVEHICLES)) {
            // For initial and 'other' vehicles.
            // Second parameter was groundnet once up to 2021 (replaced by graph layer?).
            TrafficGraph trafficGraph = (TrafficGraph) request.getPayloadByIndex(0);

            // terrain is needed (in 3D) for calculating elevation.
            boolean terrainavailable = false;
            if (AbstractSceneRunner.getInstance().getFrameCount() > 50) {
                terrainavailable = true;
            }

            if (terrainavailable) {
                //22.3.24 SphereProjections sphereProjections = TrafficHelper.getProjectionByDataprovider();
                /*27.12.21AirportConfig airportConfig = null;
                if (DefaultTrafficWorld.getInstance() != null) {
                    airportConfig = DefaultTrafficWorld.getInstance().getAirport("EDDK");
                }*/
                if (trafficContext == null) {
                    //29.12.21 trafficContext is not necessarily needed
                    //throw new RuntimeException("no trafficContext");
                    logger.warn("no trafficContext");
                }
                // 27.2.24: Currently we only have a single global vehicle list to load. Difficult to decouple via request/event due to additional vehicle properties.
                // Provider seems a good compromise.
                List<Vehicle> vehiclelist = TrafficHelper.getVehicleListByDataprovider();

                // In server mode no user might be logged in yet, so maybe there is no TeleportComponent
                // 19.11.23: TeleportComponent should be removed here in favor of EVENT_VIEWPOINT
                TrafficHelper.launchVehicles(vehiclelist,
                        trafficContext/*27.12.21groundNet*/, trafficGraph/*DefaultTrafficWorld.getInstance().getGroundNetGraph("EDDK")*/,
                        (UserSystem.getInitialUser() == null) ? null : TeleportComponent.getTeleportComponent(UserSystem.getInitialUser()),
                        /*getWorld()*//*9.3.25, null/*22.3.24sphereProjections.backProjection*/
                        /*27.12.21airportConfig,*/ null/*baseTransformForVehicleOnGraph*/, vehicleLoader, genericVehicleBuiltDelegates);
                vehiclesLoaded++;
                return true;
            }
        }
        if (request.getType().equals(RequestRegistry.TRAFFIC_REQUEST_LOADVEHICLE)) {
            // 31.10.23:moved here from BasicTravelScene
            // User request to load a vehicle.
            // Loads a specific vehicle by name or the next not yet loaded from a/the vehicle list
            logger.debug("Processing TRAFFIC_REQUEST_LOADVEHICLE request " + request);

            String vehiclename = (String) request.getPayload().get("name");
            SmartLocation smartLocation = request.getPayload().get("location", s -> SmartLocation.fromString(s));
            GeoRoute initialRoute = request.getPayload().get("initialRoute", s -> GeoRoute.parse(s));
            String initialHeading = request.getPayload().get("heading", s -> s);

            // 10.5.24 also wait for user join? Hmm, maybe not because joining user can enter the
            // vehicle loaded here. But TRAFFIC_REQUEST_LOADVEHICLE typically is triggered by a joined
            // user, so it seems more consistent to wait here. Use ASSEMBLED as we already have this event.
            if (!sphereloaded) {
                logger.debug("Aborting TRAFFIC_REQUEST_LOADVEHICLE due to missing sphere load");
                informUserAboutDelay("Waiting for sphere", request.getUserEntityId());
                return false;
            }
            if (!userAssembled) {
                logger.debug("Aborting TRAFFIC_REQUEST_LOADVEHICLE due to missing userAssembled");
                informUserAboutDelay("Waiting for user to be ready", request.getUserEntityId());
                return false;
            }

            GraphPath optionalPath = null;
            VehiclePositioner vehiclePositioner;
            if (initialRoute == null) {

                // 20.3.24: Would be better to move graph detection below for knowing the vehicle and location we can find the graph. But this breaks nextlocationindex.
                // Traditionally always groundneteddk was used here. Seems neither Wayland nor Demo use it up to now.
                // We need a graph for placing the vehicle. Wait until its available. We can assume that also terrain will be available
                // when a graph is available. Not sure this is really true always and not sure terrain is needed at all.
                // graph appears sufficient. But is it always a groundnet??
                // 20.2.24: For now use always the first
                TrafficGraph graphToUse = null;
                for (String cluster : trafficgraphs.keySet()) {
                    if (trafficgraphs.get(cluster) != null) {
                        graphToUse = trafficgraphs.get(cluster);
                    }
                }
                // 27.3.25 Some locations can be used without a graph
                if (graphToUse == null && (smartLocation == null || smartLocation.needsGraph())) {
                    logger.debug("Aborting TRAFFIC_REQUEST_LOADVEHICLE due to missing traffic graph. groundnet not loaded?");
                    informUserAboutDelay("Waiting for traffic graph(groundnet)", request.getUserEntityId());
                    return false;
                }

                if (smartLocation == null) {
                    // 26.3.20 Was ware denn die naechste Location? Das ist ja jetzt alles EDDK lastig. TODO
                    String icao = "EDDK";
                    //27.12.21  DefaultTrafficWorld.getInstance().getConfiguration().getLocationListByName(icao).get(nextlocationindex);
                /*4.12.23 ConfigNode location = locationList/*getLocationList()* /.get(nextlocationindex);
                smartLocation = new SmartLocation(icao, XmlHelper.getStringValue(location.nativeNode));*/
                    if (locationList == null || nextlocationindex >= locationList.size()) {
                        logger.warn("Aborting TRAFFIC_REQUEST_LOADVEHICLE due to missing next location. Will not retry.");
                        return true;
                    }
                    smartLocation = locationList.get(nextlocationindex);

                    // 27.21.21 das ist jetzt schwierig zu pruefen. Es ist auch unklar, ob es wirklich noch noetig ist. Mal weglassen.
                /*if (DefaultTrafficWorld.getInstance().getGroundNetGraph(icao) == null) {
                    SystemManager.putRequest(new Request(RequestRegistry.TRAFFIC_REQUEST_LOADGROUNDNET, new Payload(icao)));
                    // warten und nochmal versuchen
                    return false;
                }*/
                    nextlocationindex++;
                    logger.debug("No location in request. Now using " + smartLocation);
                    vehiclePositioner = new GraphVehiclePositioner(graphToUse, getGraphStartPosition(smartLocation, graphToUse));
                } else {
                    // we have a smartlocation
                    //vehiclePositioner = buildVehiclePositioner(smartLocation);
                    PositionerFactory.PositionerFactoryResult result = PositionerFactory.buildFromLocation(smartLocation, initialHeading);
                    if (result.status != PositionerFactory.SUCCESS) {
                        logger.debug("Aborting TRAFFIC_REQUEST_LOADVEHICLE:" + result.msg);
                        return result.status == PositionerFactory.NOTYET ? false : true;
                    }
                    vehiclePositioner = result.positioner;
                }
            } else {
                // process 'initialRoute'
                // groundnet has a (back)projection. On a initialRoute we need something different. Geo graph will have a FG graph orientation.
                BooleanHolder shouldAbort = new BooleanHolder(false);
                FlightRouteGraph flightRoute = BasicRouteBuilder
                        .fromGeoRoute(TrafficHelper.getEllipsoidConversionsProviderByDataprovider(), initialRoute, geoCoordinate -> {
                            logger.warn("No elevation for " + geoCoordinate + " of initialRoute");
                            if (!shouldAbort.getValue()) {
                                // trigger terrain loading (but only once, so set to abort. Anyway it might be triggered again later)
                                SystemManager.putRequest(RequestRegistry.buildLoadScenery(new LatLon(geoCoordinate.getLatDeg(), geoCoordinate.getLonDeg())));
                                shouldAbort.setValue(true);
                            }
                        });
                if (shouldAbort.getValue()) {
                    logger.debug("Aborting TRAFFIC_REQUEST_LOADVEHICLE due to missing elevation");
                    informUserAboutDelay("Waiting for scenery terrain", request.getUserEntityId());
                    return false;
                }

                flightRoute.smooth();
                Graph graph = flightRoute.getGraph();
                // using first edge as start should be ok
                vehiclePositioner = new GraphVehiclePositioner(new TrafficGraph(graph), new GraphPosition(graph.getEdge(0)));
                // get smoothed flightpath
                optionalPath = flightRoute.getPath();
                // like FlightSystem does. Needed for visualization.
                SystemManager.sendEvent(new Event(GraphEventRegistry.GRAPH_EVENT_PATHCREATED, new Payload(graph, optionalPath)));
            }
            String name = vehiclename;
            if (name == null) {
                List<Vehicle> vehiclelist = TrafficHelper.getVehicleListByDataprovider();

                // no vehicle name in request, so get the next unloaded one.
                for (int i = 0; i < vehiclelist.size(); i++) {
                    //SceneVehicle sv = sceneConfig.getVehicle(i);
                    if (TrafficHelper.findVehicleByName(vehiclelist.get(i).getName()) == null) {
                        name = vehiclelist.get(i).getName();
                        logger.debug("found not yet loaded vehicle " + name);
                        break;
                    }
                }
            }
            if (name == null) {
                logger.error("no vehicle found to load. vehicle list reached end?");
                //set request to done
                return true;
            }

            //22.3.24 SphereProjections sphereProjections = TrafficHelper.getProjectionByDataprovider();

            //VehicleDefinition config = TrafficHelper.getVehicleConfigByDataprovider(name, null);// tw.getVehicleConfig(name);
            VehicleDefinition config = ((TrafficSystem) SystemManager.findSystem(TrafficSystem.TAG)).getVehicleConfig(name, null);// tw.getVehicleConfig(name);
            if (config == null) {
                logger.warn("Aborting TRAFFIC_REQUEST_LOADVEHICLE due to missing config for vehicle " + name + ". Will not retry.");
                return true;
            }
            EcsEntity avatar = UserSystem.getInitialUser();
            /*21.3.25 if (destinationNode == null) {
                // the most ugly hack ever. Who sets destinationNode?
                destinationNode = Scene.getCurrent().getWorld();
            }*/

            // 21.3.24 Without login there's no avatar yet
            VehicleLauncher.launchVehicle(new Vehicle(name), config, vehiclePositioner /*graphToUse, graphStartPosition*/,
                    avatar == null ? null : TeleportComponent.getTeleportComponent(avatar),
                    /*destinationNode/*9.3.25 , null/*22.3.24 sphereProjections.backProjection*/ null/*baseTransformForVehicleOnGraphToUse*/, nearView, genericVehicleBuiltDelegates,
                    vehicleLoader, optionalPath);
            return true;
        }
        return false;
    }

    private void informUserAboutDelay(String msg, Integer receiverUserEntityId) {
        SystemManager.putRequest(BaseRequestRegistry.buildUserMessageRequest(null, msg, 3000, receiverUserEntityId));
    }

    @Override
    public void process(Event evt) {
        if (trafficsystemdebuglog) {
            logger.debug("got event " + evt);
        }
        if (evt.getType().equals(GraphEventRegistry.GRAPH_EVENT_PATHCOMPLETED)) {
            //Graph graph = (Graph) ((Object[]) evt.getPayload())[0];
            GraphPath path = (GraphPath) evt.getPayloadByIndex(1);
            EcsEntity vehicle = (EcsEntity) evt.getPayloadByIndex(2);
            VehicleComponent vc = VehicleComponent.getVehicleComponent(vehicle);
            GraphMovingComponent gmc = GraphMovingComponent.getGraphMovingComponent(vehicle);
            //GroundServiceComponent gsc = GroundServiceComponent.getGroundServiceComponent(vehicle);
            //z.Z. nothing to do

        }
        if (evt.getType().equals(TrafficEventRegistry.TRAFFIC_EVENT_VEHICLELOADED)) {
            //22.3.19: wenn das neue Vehicle eine Captain position hat, dahin wechseln.
            EcsEntity vehicle = (EcsEntity) evt.getPayloadByIndex(0);
            TeleportComponent teleportComponent = (TeleportComponent) evt.getPayloadByIndex(1);
            // Es muss nicht unbedingt eine TeleportComponent geben.
            if (teleportComponent != null) {
                // 13.3.24: This setting here might be too early. Its again set in attachAvatarToVehicle()
                int captainpos = teleportComponent.findPoint("Captain");
                logger.debug("captainpos=" + captainpos);
                if (captainpos != -1) {
                    teleportComponent.stepTo(captainpos);
                }
            }

            // 26.11.20: Das gehört doch eher nach TrafficWorldSystem, oder? Was weiss ein Avatar von Traffic? Umgekehrt eher.
            // 9.3.21: Darum verschoben aus AvatarSystem nach hier.
            //if (evt.getType().equals(EventRegistry.TRAFFIC_EVENT_VEHICLELOADED)) {

            EcsEntity vehicleEntity = (EcsEntity) evt.getPayloadByIndex(0);
            VehicleDefinition config = (VehicleDefinition) evt.getPayloadByIndex(2);
            SceneNode teleportParentNode = (SceneNode) evt.getPayloadByIndex(3);
            NearView nearView = (NearView) evt.getPayloadByIndex(4);

            attachAllAvatarsToNewVehicle(vehicleEntity, config, teleportParentNode, nearView);

            //}

            // 14.12.21: Aus dem TrafficWorldSystem hierhin kopiert. "keycontrolled" muss aber mit automove abgestimmt sein. Und geht doch nur mit einem
            // Vehicle, oder? Noch etwas unklar. Der "automove" ist fuer railing noch nicht geeignet.
            if (TrafficHelper.isRailer(config)) {
                GraphMovingComponent.getGraphMovingComponent(vehicleEntity).keycontrolled = true;
                // Der RailingBranchSelector erfoder gemoothte Graphen without kink.
                GraphMovingComponent.getGraphMovingComponent(vehicleEntity).setSelector(new RailingBranchSelector());
            }
        }
        if (evt.getType().equals(TrafficEventRegistry.TRAFFIC_EVENT_SPHERE_LOADED)) {
            //7.5.19: Das Groundnet wird woanders geladen. 30.11.21 Graphen zu einem Tile jetzt auch.
            //7.10.21: Der tile/basename kommt jetzt als payload mit.TODO
            //projection = (SimpleMapProjection) evt.getPayloadByIndex(1);
            /*27.12.21AirportConfig* /Object nearestairport = /*(AirportConfig) * /evt.getPayloadByIndex(1);*/
            //BundleResource tileResource = (BundleResource) evt.getPayloadByIndex(2);
            BundleResource tileResource = evt.getPayload().get("tilename", s -> BundleResource.buildFromFullQualifiedString(s));

            // 28.10.21 nearestairport is deprecated and thus always null.
            //27.12.21 if (nearestairport == null) {
            //ansonsten kümmert sich GroundServicesSystem darum. Oder hier immer zusätzlich? Nee, erstmal lieber nicht.

            TrafficGraph graph = null;

            if (tileResource != null) {
                logger.debug("tileResource=" + tileResource);
                // Tile 2.0
                if (tileResource.getExtension().equals("xml")) {
                    loadTileGraphByConfigFile(tileResource);
                } else {
                    // 29.11.21 Das mit dem Bundle ist doch noch Murks wegen preload needed.
                    // 16.12.21 Ueberhaupt ist byConvetion Murks. So viel convention kann es doch gar nicht geben, z.B. vehicleList,light
                    //wird noch fuer EDDK verwendet, obwohl er da direkt wieder rausgeht.
                    // Util.nomore();
                    loadTileGraphByConvention(tileResource);
                }

            }
            sphereloaded = true;
        }
        if (evt.getType().equals(BaseEventRegistry.EVENT_USER_ASSEMBLED)) {
            // 31.10.23 From TrafficWorldSystem
            //nearView soll nur die Lok abdecken.
            //21.10.19 optional
            NearView nearView = null;
            //das stammt aus railing
            /*31.10.23: nearview probably needs some refactoring if (isRailing) {

                if (enableNearView) {
                    nearView = new NearView(Scene.getCurrent().getDefaultCamera(), 0.01, 20, Scene.getCurrent());
                    //damit man erkennt, ob alles an home attached ist weg von (0,0,0) und etwas höher
                    nearView.setPosition(new Vector3(-30, 10, -10));
                }

                //Camera bekommt auch ein ProxyTransform
                Transform slave = null;
                if (nearView != null) {
                    //wirklich??slave = nearView.getCamera().getCarrier().getTransform();
                }
            }*/

            TrafficSystem.attachNewAvatarToAllVehicles(UserSystem.getInitialUser(), nearView);
            userAssembled = true;
        }
        // 20.3.24: Event handler added and handling moved here from below loader.
        if (evt.getType().equals(TrafficEventRegistry.TRAFFIC_EVENT_GRAPHLOADED)) {
            TrafficGraph graph = (TrafficGraph) evt.getPayloadByIndex(0);
            String cluster = (String) evt.getPayloadByIndex(1);

            trafficgraphs.put(cluster, graph);

            // 30.10.21: Vehicles cannot be loaded immediately, because they need to wait for example for elevation.
            TrafficGraph trafficGraph = graph;
            SystemManager.putRequest(RequestRegistry.buildLoadVehicles(trafficGraph));
        }
    }

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    public Object getData(Object[] parameter) {
        return trafficgraphs.get((String) parameter[0]);
    }

    public void setVehicleLoader(VehicleLoader vehicleLoader) {
        this.vehicleLoader = vehicleLoader;
    }

    private static void attachAllAvatarsToNewVehicle(EcsEntity vehicleEntity, VehicleDefinition config, SceneNode teleportParentNode, NearView nearView) {

        logger.debug("attaching all existing avatars to new vehicle");

        //9.3.21: Navigator hat auch TeleportComponent!
        List<EcsEntity> avatars = SystemManager.findEntities((e) -> e.getComponent(TeleportComponent.TAG) != null);
        for (EcsEntity avatar : avatars) {
            if (AvatarComponent.getAvatarComponent(avatar) != null) {
                attachAvatarToVehicle(avatar, vehicleEntity, config, teleportParentNode, nearView);
            }
        }
    }


    /**
     * MA31: Moved here from AvatarSystem
     */
    public static void attachAvatarToVehicle(EcsEntity avatar, EcsEntity vehicleEntity, VehicleDefinition config, SceneNode teleportParentNode, NearView nearView) {
        // 24.11.20: aus VehicleHelper.launchVehicle()

        TeleportComponent avatarpc = TeleportComponent.getTeleportComponent(avatar);

        // die Viewpoints zum Avatar teleport ergaenzen. Obacht, dass das nicht schon im launchVehicle gemacht wurde!
        // die alten müssen nicht hier gelöscht werden, sondern erst bei Löschen des Vehicle. Die Locations für dieses Vehicle kommen
        // beim Avatar einfach mit dran.
        //pc.addPosition("Captain", new PosRot(new Vector3(-22.60f, -0.5f, 0.8f), new Quaternion(new Degree(90), new Degree(0), new Degree(90))));
        //21.2.18: Das mit der aircraft roration fuer Captain viewer ist doch fragwürdig. Das muss doch in den Avatar.
        //23.2.18: das ist aber nicht so trivial, weil z oben ist. Und eigentlich damit ja auch gar nicht passend,
        //denn Avatra hat hier ja nun mal eine andere Defaultorientierung.
        //pc.addPosition("Captain", currentaircraft, new PosRot(aircraft.getPilotPosition(), aircraft.orientation));
        Map<String, LocalTransform> viewpoints = config.getViewpoints();
        logger.debug("Adding " + viewpoints.size() + " teleport locations to avatar for vehicle " + vehicleEntity.getName());
        for (String key : viewpoints.keySet()) {
            //24.10.19: Der Parameter hier war immer schon die base node, also die ohne offset.
            avatarpc.addPosition(key, teleportParentNode.getTransform(), new LocalTransform(viewpoints.get(key).position, viewpoints.get(key).rotation), vehicleEntity.getName(), nearView);
        }

        // start in locomotive. Weil dann der TeleporterSystem.init schon gelaufen ist, muss auch "needsupdate" gesetzt werden, darum stepTo().
        // 16.2.22 Better start at some external overview point? The user can then teleport to a vehicle. So don't change teleport position?
        // 18.2.22 But if the user requests a vehicle load, its more convenient to teleport it to the cockpit after load. So for now keep auto teleport and add a flag later.
        // 13.3.24: Agreed, especially for a time to time user its more convenient. But make sure to really use "Captain" position.
        int captainpos = avatarpc.findPoint("Captain");
        logger.debug("attachAvatarToVehicle:captainpos=" + captainpos);
        if (captainpos != -1) {
            avatarpc.stepTo(captainpos);
        } else {
            avatarpc.stepTo(avatarpc.getPointCount() - 1);
        }
    }

    public void addGraphFactory(String name, AbstractTrafficGraphFactory graphFactory) {
        graphFactories.put(name, graphFactory);
    }

    /**
     * 31.10.23: Moved here from TrafficWorldSystem.
     */
    public static void attachNewAvatarToAllVehicles(EcsEntity avatar, NearView nearView) {

        logger.debug("attaching new avatar to all existing vehicles");

        // Ob das der beste Weg ist, vehicles zu finden, muss sich noch zeigen. Aber der schlechteste wird es nicht sein
        List<EcsEntity> vehicles = EcsHelper.findEntitiesByComponent(GraphMovingComponent.TAG);
        for (EcsEntity vehicle : vehicles) {

            VehicleComponent vc = VehicleComponent.getVehicleComponent(vehicle);
            if (vc == null) {
                logger.warn("vehicle without VehicleComponent?");
            } else {
                VehicleDefinition config = vc.config;

                TrafficSystem.attachAvatarToVehicle(avatar, vehicle, config, vc.teleportParentNode, nearView);
            }
        }
    }

    /**
     * 30.11.21: Still independent from FlatTerrainSystem to have graph working even without terrain.
     * 16.12.21 Ueberhaupt ist byConvention Murks. So viel convention kann es doch gar nicht geben, z.B. vehicleList. Darum deprecated
     */
    @Deprecated
    private void loadTileGraphByConvention(BundleResource tileResource) {
        Bundle bundle = BundleRegistry.getBundle(/*"osmscenery"*/tileResource.bundlename);
        if (bundle == null) {
            logger.error("no bundle '" + tileResource.bundlename + "'");
            return;
        }
        String fullBasename = tileResource.getFullName();

        for (String cluster : new String[]{TrafficGraph.RAILWAY, TrafficGraph.ROAD}) {
            String gnet = fullBasename + "-" + cluster + ".xml";
            logger.debug("loading graph " + gnet);

            //Bundle bundle = BundleRegistry.getBundle("osmscenery");
        /*if (bundle == null) {
            logger.debug("bundle osmscenery not found");
        } else*/
            {
                BundleData bundleData = bundle.getResource(new BundleResource(gnet));
                if (loadGraph(bundleData, cluster, null)) {
                    logger.debug(" graph found for tile " + fullBasename);
                } else {
                    logger.debug("no graph found for tile " + fullBasename);
                    logger.debug("no graph loaded for cluster " + cluster);
                }

            }


        }
    }

    /**
     * Load a TrafficGraph.
     */
    private boolean loadGraph(BundleData bundleData, String cluster, AbstractTrafficGraphFactory factory) {
        TrafficGraph graph = null;
        if (bundleData == null) {
            if (factory == null) {
                logger.debug("neither graph nor factory found ");
                return false;
            }
            graph = factory.buildGraph();
        } else {
            logger.debug(" graph found ");
            //Graph osm = TrafficGraphFactory.buildfromXML(bundleData.getContentAsString()).getBaseGraph();
            try {
                graph = TrafficGraphFactory.buildfromXML(bundleData.getContentAsString());
            } catch (CharsetException e) {
                // TODO improved eror handling
                throw new RuntimeException(e);
            }
            //GraphEdge startedge = osm.getEdge(0);
            //GraphPosition startposition = new GraphPosition(startedge);
        }

        if (graph != null) {
            SystemManager.sendEvent(new Event(TrafficEventRegistry.TRAFFIC_EVENT_GRAPHLOADED, new Payload(graph, cluster)));
            // 20.3.24 handling of new graph moved to event handler
            return true;
        }
        return false;
    }

    public void loadTileGraphByConfigFile(BundleResource tile) {

        logger.debug("loadTileGraphByConfigFile");

        TrafficConfig xmlConfig = Tile.loadConfigFile(BundleRegistry.getBundle(tile.bundlename), tile);
        if (xmlConfig == null) {
            //already logged
            return;
        }
        List<NativeNode> xmlGraphs = xmlConfig.getTrafficgraphs();
        logger.debug("" + xmlGraphs.size() + " xml graphs found");
        for (NativeNode nn : xmlGraphs) {
            String graphfile = XmlHelper.getStringAttribute(nn, "graphfile", null);
            if (graphfile == null) {
                String factory = XmlHelper.getStringAttribute(nn, "graphfactory", null);
                if (factory == null) {
                    logger.error("neither graphfile nor graphfactory set");
                } else {
                    if (graphFactories.containsKey(factory)) {
                        loadGraph(null, TrafficGraph.RAILWAY, graphFactories.get(factory));
                    } else {
                        logger.error("unknown graphfactory " + factory);
                    }
                }
            } else {
                BundleResource br = BundleResource.buildFromFullQualifiedString(graphfile);
                BundleData bd = BundleHelper.loadDataFromBundle(br);
                loadGraph(bd, TrafficGraph.RAILWAY, null);
            }
        }

    }

    public void addVehicleBuiltDelegate(VehicleBuiltDelegate vehicleBuiltDelegate) {
        genericVehicleBuiltDelegates.add(vehicleBuiltDelegate);
    }

    /**
     * 8.5.24: Extracted from a custom VehicleLauncher.lauchVehicleByName().
     * Currently fails for non "groundnet" smartlocations (neither used in Demo nor in Wayland because TRAFFIC_REQUEST_LOADVEHICLE
     * isn't used there).
     * Returns some arbitrary position from graph if no start position can be derived from SmartLocation
     */
    private GraphPosition getGraphStartPosition(SmartLocation location, TrafficGraph trafficGraph) {
        GraphEdge ed = null;
        String edge = location.getGroundnetLocation();
        if (edge != null) {
            ed = trafficGraph.getBaseGraph().findEdgeByName(edge);
        }
        if (ed == null) {
            long millis = Platform.getInstance().currentTimeMillis();
            logger.warn("edge not found for smart location: " + location + ".Using random from millis " + millis);
            ed = trafficGraph.getBaseGraph().getEdge((int) (millis % trafficGraph.getBaseGraph().getNodeCount()));
        }
        //27.12.21VehicleConfig config = tw.getVehicleConfig(name);
        GraphPosition start = new GraphPosition(ed/*, ed.getLength() , true*/);
        return start;
    }

    /**
     * 7.3.25 Following methods integrated from class VehicleConfigDataProvider and replacing DataProvder
     * Some systems (GroundServiceSystem) need to know both the available service vehicles and the dimensions of aircrafts.
     * Should be set during startup (events might also be an option)
     */
    /*public class VehicleConfigDataProvider implements DataProvider {
        //27.12.21TrafficWorldConfig tw;
        // TrafficConfig tw;
        private List<VehicleDefinition> vehicleDefinitions;

        public VehicleConfigDataProvider(List<VehicleDefinition> /*TrafficConfig* / tw) {
            if (tw == null) {
                Platform.getInstance().getLog(VehicleConfigDataProvider.class).error("Setting null tw!");
            }
            // Heads up: The list might be extended later from outside(TrafficSystem)
            this.vehicleDefinitions = tw;
        }*/

    /**
     * Finds by name basically.
     * 24.11.23: From AircraftConfigProvider find by type additionally.
     * 7.3.25: Might return a list?? This is not consistent. Renamed from getVehicleConfigByDataprovider().
     *
     * @return VehicleDefinition
     */
    /*@Override
    public Object getData(Object[] parameter) {
        String vehicleName = (String) parameter[0];*/
    public VehicleDefinition getVehicleConfig(String vehicleName, String type) {

        if (knownVehicles == null) {
            //25.11.23 return new LocConfig();
            logger.error("no known vehicles yet");
            return null;
        }
        if (vehicleName == null) {
            return VehicleConfigDataProvider.findVehicleDefinitionsByModelType(knownVehicles, type).get(0);
        }
        //27.12.21 VehicleConfig vconfig = tw.getVehicleConfig(vehicleName);

        //30.10.23: xsd layout getter
        List<VehicleDefinition> vconfig = VehicleConfigDataProvider.findVehicleDefinitionsByName(knownVehicles, vehicleName);
        if (vconfig.size() > 0) {
            return vconfig.get(0);
        }
        logger.warn("vehicle " + vehicleName + " not found in " + knownVehicles.size() + " known.");

        //30.10.23: Legacy getter
        //26.11.23 no longer
        vconfig = null;//ConfigHelper.getVehicleConfig(tw, vehicleName);
        return null;//vconfig;
    }

    /**
     * Was in TafficWorldConfig with name 'getAircraftConfiguration' once.
     */
    /*public VehicleDefinition getByType(String type) {
        List<VehicleDefinition> vconfig = tw.findVehicleDefinitionsByModelType(type);
        if (vconfig.size() > 0) {
            return vconfig.get(0);
        }
        return null;

    }*/
    public List<VehicleDefinition> findVehicleDefinitionsByName(String name) {
        return VehicleConfigDataProvider.findVehicleDefinitionsByName(knownVehicles, name);
    }

    public List<VehicleDefinition> findVehicleDefinitionsByModelType(String modeltype) {
        return VehicleConfigDataProvider.findVehicleDefinitionsByModelType(knownVehicles, modeltype);
    }

    public void addKnownVehicle(VehicleDefinition vd) {
        knownVehicles.add(vd);
    }

    public List<VehicleDefinition> getKnownVehicles() {
        return knownVehicles;
    }
}

