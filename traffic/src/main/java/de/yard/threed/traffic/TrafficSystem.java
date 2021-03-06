package de.yard.threed.traffic;


import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.Payload;
import de.yard.threed.core.platform.NativeDocument;
import de.yard.threed.core.platform.NativeNode;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleData;
import de.yard.threed.core.resource.BundleHelper;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.engine.*;
import de.yard.threed.engine.avatar.AvatarComponent;
import de.yard.threed.engine.avatar.AvatarSystem;
import de.yard.threed.engine.ecs.*;
import de.yard.threed.engine.util.XmlHelper;
import de.yard.threed.graph.*;
import de.yard.threed.engine.platform.common.*;
import de.yard.threed.traffic.config.SceneConfig;
import de.yard.threed.traffic.config.VehicleConfig;

import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.util.NearView;


import de.yard.threed.trafficcore.model.Vehicle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Logically handles all traffic. Movement of vehicles isType controlled by the GraphMovingSystem.
 * Singleton?. EcsSystem. Warum? Damit es jemanden gibt, der zum Abschluss von Movements neue auslösen kann.
 * 9.5.17: Einer muss ja die Trafficsteuerung übernehmen. Und der muss updaten() und auch auf Events reagieren (z.B. Vehicle hat Ziel erreicht).
 * Warum dann kein System? Es könnte auch GroundTraficSystem heissen, aber sagen wir erstmal ist ja auch Traffic.
 * Kann man immer noch aufdröseln.
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
 * 24.11.20: Nicht obselet? Gerade erst ist TRAFFIC_REQUEST_LOADVEHICLE2 dazugekommen, um den RequestHandler in TrafficCommon zu ersetzen.
 * Nee, besser in TrafficWorldSystem.
 * <p>
 * <p>
 * <p>
 * Created by thomass on 31.03.17.
 */
public class TrafficSystem extends DefaultEcsSystem implements DataProvider {

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
    //27.10.21 die vehiclelist aus DefaultTrafficWorld hierhin. Erstmal static, spaeter ueber event?
    public static /*ConfigNodeList*/ List<Vehicle> vehiclelist;
    public static VehicleBuiltDelegate genericVehicleBuiltDelegate = null;

    @Deprecated
    public static SceneConfig sceneConfig;
    @Deprecated
    public static LocalTransform baseTransformForVehicleOnGraph;

    VehicleLoader vehicleLoader;

    Map<String, TrafficGraph> trafficgraphs = new HashMap<String, TrafficGraph>();

    // 27.12.21 Not possible as parameter aslong as groundnet and airport are unknown
    public static TrafficContext trafficContext;

    /**
     * weil er neue Pfade im Graph hinzufuegt, lauscht er auch auf GRAPH_EVENT_PATHCOMPLETED, um diese wieder aus dem
     * Graph zu entfernen.(20.3.18:immer noch, wirklich ??)
     * 8.5.19: Die Layerentfernung macht jetzt der EventSender
     */
    public TrafficSystem( /*GraphVisualizer visualizer*/VehicleLoader vehicleLoader) {
        super(new String[]{VehicleComponent.TAG},
                new RequestType[]{
                        RequestRegistry.TRAFFIC_REQUEST_VEHICLE_MOVE,
                        RequestRegistry.TRAFFIC_REQUEST_LOADVEHICLES},
                new EventType[]{
                        GraphEventRegistry.GRAPH_EVENT_PATHCOMPLETED,
                        TrafficEventRegistry.TRAFFIC_EVENT_VEHICLELOADED,
                        TrafficEventRegistry.EVENT_LOCATIONCHANGED});
        //this.visualizer = visualizer;

        instance = this;
        this.name = "TrafficSystem";
        this.vehicleLoader = vehicleLoader;
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
            logger.debug("got event " + request.getType());
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
            TrafficGraph trafficGraph = (TrafficGraph) request.getPayloadByIndex(0);
            // groundnet only for backward caompatibility. Should be replaces by graph layer
            //27.12.21 GroundNet groundNet = (GroundNet) request.getPayloadByIndex(1);

            // terrain is needed (in 3D) for calculating elevation.
            boolean terrainavailable = false;
            if (AbstractSceneRunner.getInstance().getFrameCount() > 50) {
                terrainavailable = true;
            }

            if (terrainavailable) {
                SphereProjections sphereProjections = TrafficHelper.getProjectionByDataprovider();
                /*27.12.21AirportConfig airportConfig = null;
                if (DefaultTrafficWorld.getInstance() != null) {
                    airportConfig = DefaultTrafficWorld.getInstance().getAirport("EDDK");
                }*/
                if (trafficContext == null) {
                    //29.12.21 trafficContext is not necessarily needed
                    //throw new RuntimeException("no trafficContext");
                    logger.warn("no trafficContext");
                }
                TrafficHelper.launchVehicles(TrafficSystem.vehiclelist,
                        trafficContext/*27.12.21groundNet*/, trafficGraph/*DefaultTrafficWorld.getInstance().getGroundNetGraph("EDDK")*/,
                        /*4.3.22 (AvatarSystem.getAvatar() == null) ? null :*/ TeleportComponent.getTeleportComponent(UserSystem.getInitialUser()),
                        SphereSystem.getSphereNode()/*getWorld()*/, sphereProjections.backProjection,
                        /*27.12.21airportConfig,*/ baseTransformForVehicleOnGraph, vehicleLoader, genericVehicleBuiltDelegate);
                return true;
            }
        }

        return false;
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
                int captainpos = teleportComponent.findPoint("Captain");
                if (captainpos != -1) {
                    teleportComponent.stepTo(captainpos);
                }
            }

            // 26.11.20: Das gehört doch eher nach TrafficWorldSystem, oder? Was weiss ein Avatar von Traffic? Umgekehrt eher.
            // 9.3.21: Darum verschoben aus AvatarSystem nach hier.
            //if (evt.getType().equals(EventRegistry.TRAFFIC_EVENT_VEHICLELOADED)) {

            EcsEntity vehicleEntity = (EcsEntity) evt.getPayloadByIndex(0);
            VehicleConfig config = (VehicleConfig) evt.getPayloadByIndex(2);
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
        if (evt.getType().equals(TrafficEventRegistry.EVENT_LOCATIONCHANGED)) {
            //7.5.19: Das Groundnet wird woanders geladen. 30.11.21 Graphen zu einem Tile jetzt auch.
            //7.10.21: Der tile/basename kommt jetzt als payload mit.TODO
            //projection = (SimpleMapProjection) evt.getPayloadByIndex(1);
            /*27.12.21AirportConfig* /Object nearestairport = /*(AirportConfig) * /evt.getPayloadByIndex(1);*/
            Tile initialTile = (Tile) evt.getPayloadByIndex(/*16.10.21 2 mal 1 doch wider 2*/1);
            BundleResource tileResource = (BundleResource) evt.getPayloadByIndex(2);

            // 28.10.21 nearestairport is deprecated and thus always null.
            //27.12.21 if (nearestairport == null) {
            //ansonsten kümmert sich GroundServicesSystem darum. Oder hier immer zusätzlich? Nee, erstmal lieber nicht.

            TrafficGraph graph = null;
            String basename = (initialTile == null) ? null : initialTile.file;//TrafficWorld2D.basename;
                /*29.11.21 jetzt beim tile laden(loadTileByConvention()) und nicht mehr die Graphen hier.
                war nur fuer osmscenery, nicht fuer groundnet
                */
            //}
            if (tileResource != null) {
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
        }

    }

    @Override
    public Object getData(Object[] parameter) {
        return trafficgraphs.get((String) parameter[0]);
    }


    private static void attachAllAvatarsToNewVehicle(EcsEntity vehicleEntity, VehicleConfig config, SceneNode teleportParentNode, NearView nearView) {

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
    public static void attachAvatarToVehicle(EcsEntity avatar, EcsEntity vehicleEntity, VehicleConfig config, SceneNode teleportParentNode, NearView nearView) {
        // 24.11.20: aus VehicleHelper.launchVehicle()

        TeleportComponent avatarpc = TeleportComponent.getTeleportComponent(avatar);
        logger.debug("Adding teleport locations to avatar for vehicle " + vehicleEntity.getName());

        // die Viewpoints zum Avatar teleport ergaenzen. Obacht, dass das nicht schon im launchVehicle gemacht wurde!
        // die alten müssen nicht hier gelöscht werden, sondern erst bei Löschen des Vehicle. Die Locations für dieses Vehicle kommen
        // beim Avatar einfach mit dran.
        //pc.addPosition("Captain", new PosRot(new Vector3(-22.60f, -0.5f, 0.8f), new Quaternion(new Degree(90), new Degree(0), new Degree(90))));
        //21.2.18: Das mit der aircraft roration fuer Captain viewer ist doch fragwürdig. Das muss doch in den Avatar.
        //23.2.18: das ist aber nicht so trivial, weil z oben ist. Und eigentlich damit ja auch gar nicht passend,
        //denn Avatra hat hier ja nun mal eine andere Defaultorientierung.
        //pc.addPosition("Captain", currentaircraft, new PosRot(aircraft.getPilotPosition(), aircraft.orientation));
        Map<String, LocalTransform> viewpoints = config.getViewpoints();
        for (String key : viewpoints.keySet()) {
            //24.10.19: Der Parameter hier war immer schon die base node, also die ohne offset.
            avatarpc.addPosition(key, teleportParentNode.getTransform(), new LocalTransform(viewpoints.get(key).position, viewpoints.get(key).rotation), vehicleEntity.getName(), nearView);
        }

        // start in locomotive. Weil dann der TeleporterSystem.init schon gelaufen ist, muss auch "needsupdate" gesetzt werden, darum stepTo().
        // 16.2.22 Better start at some external overview point? The user can then teleport to a vehicle. So don't change teleport position?
        // 18.2.22 But if the user requests a vehicle load, its more convenient to teleport it to the cockpit after load. So for now keep auto teleport and add a flag later.
        avatarpc.stepTo(avatarpc.getPointCount() - 1);

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
                if (loadGraphFromBundleData(bundleData, cluster)) {
                    logger.debug(" graph found for tile " + fullBasename);
                } else {
                    logger.debug("no graph found for tile " + fullBasename);
                    logger.debug("no graph loaded for cluster " + cluster);
                }

            }


        }
    }

    private boolean loadGraphFromBundleData(BundleData bundleData, String cluster) {
        TrafficGraph graph = null;
        if (bundleData == null) {
            logger.debug("no graph found ");
            return false;
        } else {
            logger.debug(" graph found ");
            //Graph osm = TrafficGraphFactory.buildfromXML(bundleData.getContentAsString()).getBaseGraph();
            graph = TrafficGraphFactory.buildfromXML(bundleData.getContentAsString());
            //GraphEdge startedge = osm.getEdge(0);
            //GraphPosition startposition = new GraphPosition(startedge);
        }

        if (graph != null) {
            SystemManager.sendEvent(new Event(TrafficEventRegistry.TRAFFIC_EVENT_GRAPHLOADED, new Payload(graph, cluster)));
            //Event reicht nicht fuer alle Zwecke??
            //12.5.20: Das ist ja ein ganz allgemeiner Trafficgraph. Der kommt einfach mal in world dazu, unabhaengig von
            //Groundnet. Das ist doch bestimmt fuer eine OSM Scene.
            //DefaultTrafficWorld.setLoadedGraph(null, graph);
            //29.10.21 nur noch wenns das gibt, muss eh raus.
                /*30.11.21if (DefaultTrafficWorld.getInstance() != null) {
                    DefaultTrafficWorld.getInstance().addTrafficGraph(graph);
                }*/
            trafficgraphs.put(cluster, graph);

            // 30.10.21: Vehicles cannot be loaded immediately, because they need to wait for example for elevation.
            TrafficGraph trafficGraph = graph;
            //no groundnet here
            //GroundNet groundNet = null;
            SystemManager.putRequest(new Request(RequestRegistry.TRAFFIC_REQUEST_LOADVEHICLES, new Payload(trafficGraph, null)));
            return true;
        }
        return false;
    }

    public void loadTileGraphByConfigFile(BundleResource tile) {

        NativeDocument xmlConfig = Tile.loadConfigFile(tile);
        if (xmlConfig == null) {
            //already logged
            return;
        }
        List<NativeNode> xmlGraphs = XmlHelper.getChildren(xmlConfig, "trafficgraph");
        for (NativeNode nn : xmlGraphs) {
            String graphfile = XmlHelper.getStringAttribute(nn, "graphfile", null);
            if (graphfile == null) {
                logger.error("graphfile not set");
            } else {
                BundleResource br = BundleResource.buildFromFullQualifiedString(graphfile);
                BundleData bd = BundleHelper.loadDataFromBundle(br);
                loadGraphFromBundleData(bd, TrafficGraph.RAILWAY);
            }
        }

    }

}

