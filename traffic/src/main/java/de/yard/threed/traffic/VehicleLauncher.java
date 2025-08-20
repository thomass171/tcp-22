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
     * This is THE main method for loading and building a vehicle, setup ECS and position it via VehiclePositioner (might be on a graph).
     * For
     * 1) Avatars aircraft
     * 2) GroundService
     * 3) AI aircraft
     * 4) Railing
     * The bundle may not have been loaded yet. The ECS entity is created here.
     * Typically triggered via TRAFFIC_REQUEST_LOADVEHICLE and TRAFFIC_REQUEST_LOADVEHICLES
     * <p>
     * Only AI Aircrafts from FG_Root(fgdata) rotate around main gears.
     * Every vehicle gets viewpoints from its configuration.
     * <p>
     * Ein arrived aircraft kann irgendwo stehen, es kommt noch nicht auf den Graph. Obwohl das fuer eine Wegbewegung ja doof ist.
     * 1.3.18: Man koennte auch direkt eine Helperedge bauen. Aber betrachten wir das hier mal als einen statischen Flieger.
     * Ist auch ganz gut zum Test der Rotation.
     * 3.3.18: Ein nicht statisches Aircraft faellt ja nicht vom Himmel und müsste eigentlich immer eine Graphposition in irgendeinem
     * Graph sein.
     * <p>
     * <p>
     * 9.1.19: Liefert bewusst nicht die Entity zurück, damit nicht eine Multiple Referenzierung neben SystemManager entsteht.
     * Wer die Entity nach dem Launch braucht, kann den Delegate verwenden.
     * 17.1.19: Wenn graph null ist (z.B. bei einem Vehicle dass sich entlang Viewpoints bewegt), wird keine GraphMovingComponent angelegt.
     * 24.11.20: avatarpc Parameter is deprecated. Should use Events. Also BuiltDelegate?
     * 24.11.20: Triggered via TRAFFIC_REQUEST_LOADVEHICLE.
     * 27.12.21: Now with a list of delegates (for decoupling doormarker)
     * 24.6.24: Have optional path for immediate movement. However this doesn't work good with long loading vehicles
     * because they might move before they are visible. Maybe better disable automove?
     */
    public static void launchVehicle(Vehicle vehicle, VehicleDefinition config, VehiclePositioner vehiclePositioner /*TrafficGraph graph, GraphPosition position*/, @Deprecated TeleportComponent avatarpc/*, GraphProjection projectionforbackprojection*/,
                                     LocalTransform vehiclebasetransform, NearView nearView, List<VehicleBuiltDelegate> genericVehicleBuiltDelegates, VehicleLoader vehicleLoader,
                                     GraphPath optionalPath) {
        vehicleLoader.loadVehicle(vehicle, config, (SceneNode offsetNode, VehicleLoaderResult loaderResult, SceneNode lowresNode) -> {
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
            //13.3.18: MovingSystem will set rotation later on a different (sub?) node (which one?). So this is important here as the name says ("basetransform").
            // FlighScene uebergibt da was anderes, weils nicht passt.
            //02.04.25: After defining FG space a standard, it is needed for 'loc' eg. But it is vehicle dependent. Unfortunatly graph conversions
            // hided the need previously.'vehiclebasetransform' however is really not needed.
            LocalTransform vehicletransform = config.getTransform();
            if (vehicletransform != null) {
                offsetNode.getTransform().setPosition(vehicletransform.position);
                offsetNode.getTransform().setRotation(vehicletransform.rotation);
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
            //22.3.25 now in VehiclePositioner destinationnode.attach(offsetNode);
            vehiclePositioner.getDestinationNode().attach(offsetNode);
            EntityBuilder entityBuilder = TrafficHelper.getVehicleEntityBuilderByService(config);
            //TODO pruefen 24.10.19: Ist die zoffset node auf dem Graph nicht "falsch" wenn dann um eine andere Achse rotiert wird? Dann fällt das Vehicle doch vom Graph.
            //TODO Ich glaube mittlerweile, das z offset Problem sollte nicht hierhin mitgeschleppt werden.
            //Ist dafuer nicht die basenode in der Entity? JungeJunge, das ist aber auch'n Driss.
            //31.3.20: In EDDK stehen die AI aircraft aber doch wohl richtig, scheinbar auch in der Höhe.
            EcsEntity vehicleEntity = buildVehicleOnGraph(offsetNode, vehiclePositioner, config, /*projectionforbackprojection,*/ entityBuilder, teleportParentNode);
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

            loaderResult.applyResultsToEntity(vehicleEntity);

            GraphMovingComponent.getGraphMovingComponent(vehicleEntity).setUnscheduledmoving(config.getUnscheduledmoving());

            // 26.01.22: Meanwhile the logic for attaching the vehicles view points to the vatar(s) was
            // exactly (first copied) moved to {@link TrafficSystem.attachAllAvatarsToNewVehicle()} for event TRAFFIC_EVENT_VEHICLELOADED.

            for (VehicleBuiltDelegate d : genericVehicleBuiltDelegates) {
                d.vehicleBuilt(vehicleEntity, config);
            }
            // 24.11.20: more parameter for TRAFFIC_EVENT_VEHICLELOADED, for filling teleportcomponent there.
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
    public static EcsEntity buildVehicleOnGraph(SceneNode node, VehiclePositioner vehiclePositioner /*TrafficGraph graph, GraphPosition position,*/, VehicleDefinition config,
            /*Map*//*GraphProjection projection,*/ EntityBuilder entityBuilder, SceneNode teleportParentNode) {
        GraphMovingComponent gmc = new GraphMovingComponent(node.getTransform());

        EcsEntity e = new EcsEntity(node, gmc);

        vehiclePositioner.positionVehicle(e);

        VelocityComponent vc = new VelocityComponent();
        vc.setMaximumSpeed(config.getMaximumSpeed());
        vc.setAcceleration(config.getAcceleration());
        // addComponent also triggers entity init and GraphMovingSystem.adjustVisual()
        e.addComponent(vc);
        if (config != null) {
            // its important to set the vehicle rotation before the final component is added
            // and an entity init is triggered.
            // 1.4.25: Now that 'traffic' has 'FG space' as default, no special treatment is needed any more. We always set 'FG space'
            gmc.customModelRotation = FgVehicleSpace.getFgVehicleForwardRotation();

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
    /* 'loc' 'mobi' now is also rotated to FG, so handle it like an FG model.
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
    }*/

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
