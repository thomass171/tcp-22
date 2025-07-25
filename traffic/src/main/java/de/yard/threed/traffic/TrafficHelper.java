package de.yard.threed.traffic;

import de.yard.threed.core.Degree;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.Util;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.ViewPoint;
import de.yard.threed.engine.ecs.DataProvider;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsHelper;
import de.yard.threed.engine.ecs.EcsService;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.TeleportComponent;
import de.yard.threed.traffic.config.*;

import de.yard.threed.graph.*;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.Event;
import de.yard.threed.core.Payload;
import de.yard.threed.traffic.flight.FlightLocation;
import de.yard.threed.core.GeoCoordinate;
import de.yard.threed.trafficcore.EllipsoidCalculations;
import de.yard.threed.trafficcore.geodesy.MapProjection;
import de.yard.threed.trafficcore.config.LocatedVehicle;
import de.yard.threed.trafficcore.model.SmartLocation;
import de.yard.threed.trafficcore.model.Vehicle;


import java.util.ArrayList;
import java.util.List;

/**
 * Container für statische MEthodesn, die sich so in den Systems rundruecken.
 * Aber nur wenn sie halbwegs Traffic Allgemeingültig sind. Die Travelspzifiscehn kommen nach TravelHelper.
 */
public class TrafficHelper {
    private static Log logger = Platform.getInstance().getLog(TrafficHelper.class);

    /**
     * The most common part of launching a moving vehicle.
     * 7.3.20: Ist aber begrenzt auf einen Graph. spawnTravel() ist generischer.
     * <p>
     * hier wird noch nicht der lockEntity gemacht.
     * Returns true when spawn was successful.
     */
    public static boolean spawnMoving(EcsEntity entity, GraphNode destinationnode, TrafficGraph groundnet) {

        GraphMovingComponent gmc = GraphMovingComponent.getGraphMovingComponent(entity);
        VehicleComponent vhc = VehicleComponent.getVehicleComponent(entity);
        GraphPath path = groundnet.createPathFromGraphPosition(gmc.getCurrentposition(), destinationnode, null, vhc.config);
        if (path == null) {
            return false;
        }
        SystemManager.sendEvent(new Event(GraphEventRegistry.GRAPH_EVENT_PATHCREATED, new Payload(gmc.getGraph(), path)));
        gmc.setPath(path, true);
        return true;

    }


    /**
     * jetzt noch die konfigurierten Vehicles, die einfach so rumfahren oder fuer GroundServices genutz werden.
     * Aircraft fliegen hier auch eine Runde, wenn der Airport bekannt ist.
     * <p>
     * War mal halbwegs analog zu FG (z.B. createVehicle).
     * <p>
     * <p>
     * 7.5.19: Wenn ein Groundndet vorliegt, die Infos draus verwenden. Sonst kommen die Vehicle einfach so auf den Graph.
     * Der Graph muss ja nicht unbedingt von einem groundnet sein. Ueberhaupt muss es mittlerweile nicht unbedingt ein groundnet geben.
     */
    public static void launchVehicles(/*ConfigNodeList*/List<Vehicle> vehiclelist, TrafficContext trafficContext/*27.12.21GroundNet groundnet*/, TrafficGraph graph, TeleportComponent avatarpc//, SceneNode destinationnode
                                                        /*9.3.25, GraphProjection projection /*27.12.21AirportConfig airport*/, LocalTransform baseTransformForVehicleOnGraph/* SceneConfig sceneConfig*/,
                                                        VehicleLoader vehicleLoader, List<VehicleBuiltDelegate> genericVehicleBuiltDelegates) {
        logger.debug("launchVehicles groundnet=" + ",graph=" + graph);
        //27.12.21 TrafficWorldConfig tw = TrafficWorldConfig.getInstance();
        for (int i = 0; i < vehiclelist.size()/*tw.getVehicleCount()*/; i++) {
            //for (GroundServiceVehicleConfig c : config.vehicles) {
            Vehicle/*ConfigNode*/ vehicle = vehiclelist.get(i);
            //29.10.21 VehicleConfig vconfig = tw.getVehicleConfig(vehicle.getName());
            //VehicleDefinition vconfig = getVehicleConfigByDataprovider(vehicle.getName(), null);
            VehicleDefinition vconfig = ((TrafficSystem)SystemManager.findSystem(TrafficSystem.TAG)).getVehicleConfig(vehicle.getName(), null);
            if (vconfig == null) {
                logger.warn("Vehicle not found:" + vehicle.getName());
                return;
            }
            if (!vehicle.hasDelayedLoad() && !vehicle.wasLoaded/*getBooleanAttribute(TrafficWorldConfig.DELAYEDLOAD, false)*/) {
                for (int j = 0; j < vconfig.getInitialCount(); j++) {
                    GraphPosition graphposition = null;

                    if (/*27.12.21groundnet*/trafficContext != null) {
                        graphposition = trafficContext.getStartPosition(vconfig);

                    } else {
                        if (graph == null || graph.getBaseGraph().getEdgeCount() == 0) {
                            logger.warn("empty graph");
                            return;
                        }
                        if (vehicle.getLocation() != null) {
                            GraphEdge startEdge = graph.getBaseGraph().findEdgeByName(vehicle.getLocation().getSubLocation());
                            if (startEdge == null) {
                                logger.warn("location not found in graph:" + vehicle.getLocation());
                            } else {
                                graphposition = new GraphPosition(startEdge);
                            }
                        }
                        if (graphposition == null) {
                            //einfach erstmal so.
                            long millis = Platform.getInstance().currentTimeMillis();
                            logger.warn("no location defined. Using random from millis " + millis);
                            graphposition = new GraphPosition(graph.getBaseGraph().getEdge((int) (millis % graph.getBaseGraph().getEdgeCount())));
                        }
                    }
                    VehicleLauncher.launchVehicle(vehicle, vconfig, new GraphVehiclePositioner(graph/*groundnet.groundnetgraph*/, graphposition), avatarpc, /*destinationnode,/*9.3.25 projection,*/
                            /*sceneConfig.getBaseTransformForVehicleOnGraph()*/baseTransformForVehicleOnGraph, null, genericVehicleBuiltDelegates, vehicleLoader, null);
                }
                vehicle.wasLoaded = true;
            } else {
                logger.debug("Skipping delayload vehicle " + vehicle.getName());
            }
        }

        // mit ein paar Fliegern bestücken. Sowas gibt es (noch) nicht in FG
        // das sind zusaetzlich zu den konfigurierten ground vehicles, die oben schon gestartet wurden, ein paar Aircraft.
        /*27.12.21groundnet if (groundnet != null) {*/
        if (trafficContext != null) {
            for (int i = 0; i < /*27.12.21airport*/trafficContext.getVehicleCount(); i++) {
                LocatedVehicle vconf = /*27.12.21airport*/trafficContext.getVehicle(i);
                VehicleDefinition config = null;// 27.12.21 tw.getVehicleConfig(vconf.getName());
                //config = getVehicleConfigByDataprovider(vconf.getName(), null);
                config = ((TrafficSystem)SystemManager.findSystem(TrafficSystem.TAG)).getVehicleConfig(vconf.getName(), null);
                SmartLocation location = vconf.getLocation();
                //buildArrivedAircraft(config, gsw.groundnet.getParkPos(location.getParkPos()));
                //VehicleLauncher.launchVehicle(new Vehicle(vconf.getName()),config, groundnet.groundnetgraph, groundnet.getParkingPosition(groundnet.getParkPos(location.getParkPos())), avatarpc, destinationnode, projection, /*sceneConfig.getBaseTransformForVehicleOnGraph()*/baseTransformForVehicleOnGraph, null, null, vehicleLoader);
                VehicleLauncher.launchVehicle(new Vehicle(vconf.getName()), config,
                        new GraphVehiclePositioner(trafficContext.getGraph(), trafficContext.getStartPositionFromLocation(location)), avatarpc,/*, destinationnode, /*9.3.25 projection, /*sceneConfig.getBaseTransformForVehicleOnGraph()*/baseTransformForVehicleOnGraph, null, new ArrayList<VehicleBuiltDelegate>(), vehicleLoader, null);
            }
        }
        //}
    }

    /**
     * 22.3.24: Parameter 'forwardProjection' added for providing a fitting backprojection (3D only).
     */
    public static SphereProjections getProjectionByDataprovider(MapProjection forwardProjection) {
        DataProvider projectionDataProvider = SystemManager.getDataProvider("projection");
        if (projectionDataProvider == null) {
            logger.error("no projection");
            return null;
        }

        SphereProjections projection = (SphereProjections) projectionDataProvider.getData(new Object[]{forwardProjection});
        return projection;
    }

    /**
     * Viewpoints koennte auch TeleporterSystem kennen, aber irgendwo muss ein neu dazugekommener Player sie ja herholen können.
     * Not a good location because its not necessarily related to traffic?
     *
     * @return
     */
    public static List<ViewPoint> getViewpointsByDataprovider() {
        DataProvider viewpointsDataProvider = SystemManager.getDataProvider("viewpoints");
        if (viewpointsDataProvider == null) {
            logger.warn("no viewpointsDataProvider");
            return null;
        }

        List<ViewPoint> viewPoints = (List<ViewPoint>) viewpointsDataProvider.getData(null);
        return viewPoints;
    }

    public static boolean isIcao(String tilename) {
        //TODO improves
        return StringUtils.startsWith(tilename, "E") && StringUtils.length(tilename) == 4;
    }

    /**
     * 24.11.23: Parameter 'type' added. 'name' is optional now.
     * 7.3.25 Integrated to TrafficSystem
     */
    /*public static VehicleDefinition getVehicleConfigByDataprovider(String vehicleName, String type) {
        DataProvider vehicleConfigDataProvider = SystemManager.getDataProvider("vehicleconfig");
        if (vehicleConfigDataProvider == null) {
            logger.warn("no vehicleConfigDataProvider");
            return null;
        }

        VehicleDefinition vehicleConfig = (VehicleDefinition) vehicleConfigDataProvider.getData(new String[]{vehicleName, type});
        return vehicleConfig;
    }*/

    /**
     * Really needed? Time will tell.
     * 29.11.21
     */
    public static TrafficGraph getTrafficGraphByDataprovider(String type) {
        DataProvider trafficGraphDataProvider = SystemManager.getDataProvider("trafficgraph");
        if (trafficGraphDataProvider == null) {
            logger.warn("no trafficGraphDataProvider");
            return null;
        }

        TrafficGraph trafficGraph = (TrafficGraph) trafficGraphDataProvider.getData(new Object[]{type});
        return trafficGraph;
    }

    public static EllipsoidCalculations getEllipsoidConversionsProviderByDataprovider() {
        DataProvider provider = SystemManager.getDataProvider("ellipsoidconversionprovider");
        if (provider == null) {
            logger.warn("no ellipsoidconversionprovider");
            return null;
        }

        EllipsoidCalculations rbcp = (EllipsoidCalculations) provider.getData(new Object[]{});
        return rbcp;
    }

    public static EntityBuilder getVehicleEntityBuilderByService(VehicleDefinition config) {
        EcsService p = SystemManager.getService("vehicleentitybuilder");
        if (p == null) {
            logger.warn("no vehicleentitybuilder");
            return null;
        }
        EntityBuilder entityBuilder = (EntityBuilder) p;
        return entityBuilder;
    }

    public static boolean isAircraft(VehicleDefinition config) {
        return config.getType().equals(VehicleComponent.VEHICLE_AIRCRAFT);
    }

    public static boolean isRailer(VehicleDefinition config) {
        return config.getType().equals(VehicleComponent.VEHICLE_RAILER);
    }

    public static EcsEntity findVehicleByName(String name) {
        List<EcsEntity> aircrafts = EcsHelper.findEntitiesByComponent(VehicleComponent.TAG);
        EcsEntity found = null;
        int i;
        for (i = 0; i < aircrafts.size(); i++) {
            if (aircrafts.get(i).getName().equals(name/**/)) {
                if (found != null) {
                    //besser erstmal abbrechen. Das gibt sonst schwer erkennbare Fehlfunktionen
                    throw new RuntimeException("duplicate " + name);
                }
                found = aircrafts.get(i);
            }
        }
        return found;
    }

    public static List<Vehicle> getVehicleListByDataprovider() {
        DataProvider dataProvider = SystemManager.getDataProvider("vehiclelistprovider");
        if (dataProvider == null) {
            logger.warn("no vehiclelistprovider");
            return null;
        }
        List<Vehicle> vehicleList = (List<Vehicle>) dataProvider.getData(null);
        return vehicleList;
    }

    /**
     * 22.7.24
     * 7.3.25 is it really a good idea to couple these properties this way? Eg. This breaks the smart location idea.
     * Set to deprecated.
     * Moved to SmartLocation/PositionerFactory. But still needed in standalone scenes.
     * Still used in FG. 15.7.25 no longer
     */
    /*@Deprecated
    public static FlightLocation getInitialFlightLocation() {
        // have it int two separate properties due to complex parsing
        String initialLocation = Platform.getInstance().getConfiguration().getString("initialLocation");
        if (initialLocation == null) {
            return null;
        }
        GeoCoordinate g = GeoCoordinate.parse(initialLocation);
        String initialHeading = Platform.getInstance().getConfiguration().getString("initialHeading");
        if (initialHeading == null) {
            return new FlightLocation(g, new Degree(0));
        }
        return new FlightLocation(g, new Degree(Util.parseDouble(initialHeading)));
    }*/
}
