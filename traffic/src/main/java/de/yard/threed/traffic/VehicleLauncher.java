package de.yard.threed.traffic;

import de.yard.threed.core.*;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.apps.ModelSamples;
import de.yard.threed.engine.*;

import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.TeleportComponent;
import de.yard.threed.engine.ecs.VelocityComponent;
import de.yard.threed.graph.DefaultEdgeBasedRotationProvider;
import de.yard.threed.graph.GraphMovingComponent;
import de.yard.threed.graph.GraphPath;
import de.yard.threed.graph.GraphPosition;
import de.yard.threed.graph.GraphProjection;
import de.yard.threed.core.platform.Log;
import de.yard.threed.graph.ProjectedGraph;

import de.yard.threed.traffic.config.VehicleDefinition;

import de.yard.threed.engine.util.NearView;

import de.yard.threed.trafficcore.model.Vehicle;

import java.util.List;

/**
 * Abgrenzung zu TrafficHelper ist...:
 * hier ist nur etwas zu konkret einzelnen Vehicles.
 * <p>
 * <p>
 * 9.11.21: Weiter decoupled per FgVehicleLoader. Der hier nicht mehr.
 */
public class VehicleLauncher {
    private static Log logger = Platform.getInstance().getLog(VehicleLauncher.class);

    /**
     * This is THE main method for loading and building a vehicle, setup ECS and position it on a graph.
     * For
     * 1) Avatars aircraft
     * 2) GroundService
     * 3) AI aircraft
     * 4) Railing
     * Das Bundle muss noch nicht geladen sein. Hier wird die Entity angelegt.
     * Typically triggered via TRAFFIC_REQUEST_LOADVEHICLE and TRAFFIC_REQUEST_LOADVEHICLES
     * <p>
     * * Nur die AI Aircrafts aus FG_Root(fgdata) rotieren um main gears.
     * Das Vehicle kommt aus den Beginn der Edge (from) in Richtung "to".
     * JEdes Vehicle bekommt einen Viewpont seitlich hinten, um seine Bewegung verfolgen zu können.
     * <p>
     * // Das FG Model in z0 Ebene nur von x-Richtung drehen mit Nase Richtung +y (Norden)
     * // Die FG Aircraft Model haben die Spitze Richtung -x und z nach oben.
     * // Das ist im Prinzip schon mal passend, weil wir in der z0 Ebene sind.
     * <p>
     * Ein arrived aircraft kann irgendwo stehen, es kommt noch nicht auf den Graph. Obwohl das fuer eine Wegbewegung ja doof ist.
     * 1.3.18: Man koennte auch direkt eine Helperedge bauen. Aber betrachten wir das hier mal als einen statischen Flieger.
     * Ist auch ganz gut zum Test der Rotation.
     * 3.3.18: Ein nicht statisches Aircraft faellt ja nicht vom Himmel und müsste eigentlich immer eine Graphposition in irgendeinem
     * Graph sein.
     * <p>
     * Die destinationnode wird z.B. bei FlightScene gebraucht, wo es auch noch eine gekapselte "world" gibt.
     * 17.4.18: Diese Methode muesste eigentlich woanders untergebracht werden.
     * <p>
     * 9.1.19: Liefert bewusst nicht die Entity zurück, damit nicht eine Multiple Referenzierung neben SystemManager entsteht.
     * Wer die Entity nach dem Launch braucht, kann den Delegate verwenden.
     * 17.1.19: Wenn graph null ist (z.B. bei einem Vehicle dass sich entlang Viewpoints bewegt), wird keine GraphMovingComponent angelegt.
     * 24.11.20: avatarpc als Parameter ist deprecated. Das sollte ueber Events gehen. Gilt das nicht auch fuer den BuiltDelegate?
     * 24.11.20: Triggered via TRAFFIC_REQUEST_LOADVEHICLE.
     * MA31: Wegen VehicleHelperDecoupler nicht mehr static.
     * 27.12.21: Now with a list of delegates (for decoupling doormarker)
     * 24.6.24: Have optional path for immediate movement. However this doesn't work good with long loading vehicles
     * because they might move before they are visible. Maybe better disable automove?
     * <p>
     *
     * @param config
     * @param position
     * @param vehiclebasetransform Das was ein Vehicle braucht um richtig rum auf dem Graph zu stehen, unabhaengig von der
     *                             Rotation die sich durch den Graph selber ergibt. Das ist graph- nicht vehiclespezifisch. Sowas kann es zusätzlich noch
     *                             vehiclespezifisch geben.
     * @return
     */
    public static void launchVehicle(Vehicle vehicle, VehicleDefinition config, TrafficGraph graph, GraphPosition position, @Deprecated TeleportComponent avatarpc, SceneNode destinationnode, GraphProjection projectionforbackprojection,
                                     LocalTransform vehiclebasetransform, NearView nearView, List<VehicleBuiltDelegate> genericVehicleBuiltDelegates, VehicleLoader vehicleLoader,
                                     GraphPath optionalPath) {
        vehicleLoader.loadVehicle(vehicle, config, (SceneNode offsetNode, VehicleLoaderResult loaderResult/*9.11.21List<SGAnimation> animationList, SGPropertyNode rootpropertyNode*/, SceneNode lowresNode) -> {
            SceneNode modelNode = getModelNodeFromVehicleNode(offsetNode);
            SceneNode teleportParentNode = modelNode;
            SceneNode teleportSlaveNode = null;
            if (nearView != null) {
                if (lowresNode == null) {
                    //3->0.3 wegen Navigator. Der Cube ist bei Navigator wegen Rundung(?) zu weit links.
                    lowresNode = ModelSamples.buildCube(0.3, Color.BLUE);
                }
                //die modelNode hier muss fuer Telport erhalten bleiben. Aber sie wird beim parent ausgehangen und durch lowres ersetzt. Damit hat die hidden node dann keinen z-offset.
                // Das duerfte verzichtbar sein.
                //Der Teleport geschieht immer mit main camera und die soll zur "richtigen" Location, darum bekommt ja die modelNode (genau genommen nearView) eine eigene deferred camera.
                lowresNode.getTransform().setParent(modelNode.getTransform().getParent());
                nearView.hide(modelNode);
                teleportParentNode = lowresNode;
                teleportSlaveNode = modelNode;
            }
            //currentaircraft = rotateFgModelForGraph(currentaircraft);
            //13.3.18: Das MovingSystem überschreibt doch die Rotation. Wieso hat das einen Effekt?
            //Weils sofort gekapselt wird. FlighScene uebergibt da was anderes, weils nicht passt.
            if (vehiclebasetransform != null) {
                offsetNode.getTransform().setPosition(vehiclebasetransform.position);
                offsetNode.getTransform().setRotation(vehiclebasetransform.rotation);
            }
            //24.10.19: Das Konzept der ProxyNode muss sich noch bewähren (wegen Dependency zwischen Nodes), läuft erstmal aber ganz gut.
            //31.03.20: Aber nicht mit AI Aircraft, da geht dann die zoffsetnode verloren. Darum nur noch Proxy, wenn es wirklich einen Slave gibt.
            // Kapeseln muss aber sein, sonst stehen Vehicles in FlatTravel falsch! Das ist also wirklich noch nicht rund, obwohl das hier wohl schluessig ist.
            if (teleportSlaveNode != null) {
                offsetNode = new ProxyNode(offsetNode, teleportSlaveNode);
            } else {
                offsetNode = new SceneNode(offsetNode);

            }
            //offsetNode.setSlaveNode(teleportSlaveNode);
            offsetNode.setName("vehiclecontainer-" + config.getName());
            //Scene.getCurrent().addToWorld(currentaircraft);
            destinationnode.attach(offsetNode);
            EntityBuilder entityBuilder = TrafficHelper.getVehicleEntityBuilderByService(config);
            //TODO pruefen 24.10.19: Ist die zoffset node auf dem Graph nicht "falsch" wenn dann um eine andere Achse rotiert wird? Dann fällt das Vehicle doch vom Graph.
            //TODO Ich glaube mittlerweile, das z offset Problem sollte nicht hierhin mitgeschleppt werden.
            //Ist dafuer nicht die basenode in der Entity? JungeJunge, das ist aber auch'n Driss.
            //31.3.20: In EDDK stehen die AI aircraft aber doch wohl richtig, scheinbar auch in der Höhe.
            EcsEntity vehicleEntity = buildVehicleOnGraph(offsetNode, graph, position, config, projectionforbackprojection, entityBuilder, teleportParentNode);
            vehicleEntity.setName(config.getName());
            // 24.6.24: Have optional path
            if (optionalPath != null) {
                // 25.6.24: automove might be too early for some long loading vehicle.
                GraphMovingComponent.getGraphMovingComponent(vehicleEntity).setPath(optionalPath, vehicle.hasAutomove());
            } else {
                GraphMovingComponent.getGraphMovingComponent(vehicleEntity).setAutomove(vehicle.hasAutomove());
            }
            //26.10.19vehicleEntity.setBasenode(modelNode);

            //27.12.21 doormarker extracted to DoormarkerDelegate
            //9.1.19:TODO viel zu GroundServices/Flight spezifisch
            /*if (((Platform) Platform.getInstance()).isDevmode() && VehicleComponent.VEHICLE_AIRCRAFT.equals(config.getType())) {
                // ein kleiner doormarker im local space.
                SceneNode marker = ModelSamples.buildAxisHelper(8, 0.3f);
                marker.setName("localdoormarker");
                Util.notyet();
                Vector3 dp = null;//227.12.21 TODO TrafficWorldConfig.getInstance().getAircraftConfiguration(config.getModelType()).getCateringDoorPosition();
                marker.getTransform().setPosition(dp);
                modelNode.attach(marker);
            }*/

            loaderResult.applyResultsToEntity(vehicleEntity);

            GraphMovingComponent.getGraphMovingComponent(vehicleEntity).setUnscheduledmoving(config.getUnscheduledmoving());

            // 26.10.18: Erst jetzt zum Schluss die Teleportlocations für z.B. "Captain" eintragen.Scheint einfach sauberer.
            // 24.11.20: Ist jetzt besser in AvatarSystem untergebracht.
            // 26.01.22: Meanwhile the logic for attaching the vehicles view points to the vatar(s) was
            // exactly (first copied) moved to {@link TrafficSystem.attachAllAvatarsToNewVehicle()} for event TRAFFIC_EVENT_VEHICLELOADED.

            // Vehicle ist schon in den Scene Space (oder graph space?) rotiert. Etwas ueberraschend: x=seitlich,y=hoehe,z vor zurueck. 28.10.17: Wirklich?
            // ist noch in FG space. Dann muss ich aber rotieren. Aber warum so? 22.10.10: Ausserdem muss das in die Config
            /*jetzt in config if (avatarpc != null) {
                avatarpc.addPosition("BackSide", modelNode, new LocalTransform(new Vector3(90, 20, 15), Quaternion.buildFromAngles(new Degree(0), new Degree(90), new Degree(90))));
            }*/

            for (VehicleBuiltDelegate d : genericVehicleBuiltDelegates) {
                d.vehicleBuilt(vehicleEntity, config);
            }
            // 24.11.20: ein paar Parameter mehr fuer TRAFFIC_EVENT_VEHICLELOADED, um da teleportcomponent zu befuellen.
            SystemManager.sendEvent(new Event(TrafficEventRegistry.TRAFFIC_EVENT_VEHICLELOADED, new Payload(vehicleEntity, avatarpc, config, teleportParentNode, nearView)));
        });
    }

    /**
     * Ein graphgebundenes Vehicle mit den Vehicle typischen Components als ECS Entity bauen.
     * Moving- und Velocity Components sind mandatory. Vehicle optional.
     * type ist der
     * <p>
     * TODO: Die projection ist die totale Kruecke. Die koennte vielleicht mit dem Graph zusammen in einen "GraphContext".
     * 29.8.23: teleportParentNode added
     */
    public static EcsEntity buildVehicleOnGraph(SceneNode node, TrafficGraph graph, GraphPosition position, VehicleDefinition config,
            /*Map*/GraphProjection projection, EntityBuilder entityBuilder, SceneNode teleportParentNode) {
        GraphMovingComponent gmc = new GraphMovingComponent(node.getTransform());
        //MA31: navigator hat keinen graph
        if (projection != null && !(graph.getBaseGraph() instanceof ProjectedGraph)) {
            throw new RuntimeException("should use ProjectedGraph");
        }
        gmc.setGraph((graph == null) ? null : graph.getBaseGraph(), position, null/*projection*/);

        EcsEntity e = new EcsEntity(node, gmc);
        VelocityComponent vc = new VelocityComponent();
        vc.setMaximumSpeed(config.getMaximumSpeed());
        vc.setAcceleration(config.getAcceleration());
        // addComponent also triggers entity init and GraphMovingSystem.adjustVisual()
        e.addComponent(vc);
        if (config != null) {
            // its important to set the vehicle rotation before the final component is added
            // and an entity init is triggered.
            gmc.customModelRotation = getModelBaseRotation(config);

            // 29.8.23: Why VehicleComponent only if config exists? Conatins eg. also the teleportParentNode
            VehicleComponent vhc = new VehicleComponent(config/*type,modeltype*/);
            vhc.teleportParentNode = teleportParentNode;
            e.addComponent(vhc);
            // 27.12.21: Extracted to VehicleEntityBuilder
            if (entityBuilder != null) {
                entityBuilder.configure(e, config);
            }
            /*if (config.getType().equals(VehicleComponent.VEHICLE_CAR)) {
                // passt z.Z. zwar, aber nicht jedes Car muss unbedingt ein GS vehicle sein.
                GroundServiceComponent gsc = new GroundServiceComponent(config);
                e.addComponent(gsc);
            }*/
        }
        return e;
    }

    /**
     * The rotation of a standard oriented vehicle to
     * the graph standard orientation (see README).
     */
    public static Quaternion locToGraphRotation() {
        return Quaternion.buildFromAngles(new Degree(0), new Degree(-90), new Degree(0));
    }

    private static Quaternion getModelBaseRotation(VehicleDefinition config) {
        String name = config.getName();
        if (name.toLowerCase().equals("loc") || name.toLowerCase().equals("locomotive")) {
            // Was 'BaseTransformForVehicleOnGraph' in Demo.xml once
            return locToGraphRotation();
        }
        Quaternion fgrot = DefaultEdgeBasedRotationProvider.getFgVehicleForwardRotation();
        if (name.toLowerCase().contains("bluebird")) {
            return fgrot;
        }
        if (name.toLowerCase().contains("c172p")) {
            return fgrot;
        }
        if (name.toLowerCase().contains("777")) {
            return fgrot;
        }
        if (config.getBundlename().contains("fgdatabasicmodel")) {
            return fgrot;
        }
        logger.debug("no model rotation detected for " + config.getName());
        return new Quaternion();
    }

    /**
     * TODO generell Pfadsuche in SceneNode.
     * Skizze 37
     *
     * @param vehicleNode
     * @return
     */
    public static SceneNode getModelNodeFromVehicleNode(SceneNode vehicleNode) {
        //31.3.20 Needs to be recursive, weil durch proxynode/nearview und der ganze Huddel evtl. noch eine Ebene dazugekommen ist. Hoffentlich findet er nicht zu viele.
        List<SceneNode> result = vehicleNode.findNodeByName("zoffsetnode");
        if (result.size() != 1) {
            throw new RuntimeException("not exactly one zoffsetnode");
        }
        SceneNode zoffsetNode = result.get(0);
        result = zoffsetNode.findNodeByName("basenode");
        SceneNode baseNode = result.get(0);
        return baseNode;
    }
}
