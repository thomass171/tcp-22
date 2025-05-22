package de.yard.threed.engine.ecs;

import de.yard.threed.core.*;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.platform.common.*;

import java.util.List;


/**
 * Rapid movement of an object (EcsEntity).
 * Anders als ein MovingSystem, das auf einer Geschwindigkeitskomponente basiert.Irgendwie ist das alles zwar ähnlich,
 * aber so sehr ist es doch nicht dasselbe. Bei der animated teleport sieht das wohl so ähnlich aus, aber trotzdem ist es doch deutlich anders.
 * <p>
 * From StepController. See also TeleportComponent.
 * Aehnlich zum deprecated ViewpointSystem. Um sich umzusehen, brauchts noch ein Observersystem?
 * Die TeleportComponent befindet sich typischerweise in einem Avatar Entity. NeeNee, eher Vehicle?
 * <p>
 * 12.10.19: Heads up! TeleportComponents are not always attached to the avatar, and never multiple, but eg. to vehicles.
 * Avatar and vehicle have a TC to position itself somewhere.
 * The TC of a avatar is extended by viewpoints of a vehicle, eg. for sitting in a cockpit or for a sideview on the vehicle.
 * Navigator eg. has a TC with all world POIs.
 * <p>
 * Here only the current TeleportComponent is known (toggled by ctrl-t). Should use id for better decoupling.
 * Created by thomass on 09.01.17.
 */
public class TeleporterSystem extends DefaultEcsSystem {
    Log logger = Platform.getInstance().getLog(TeleporterSystem.class);
    private boolean animated = true;
    public static String TAG = "TeleporterSystem";
    boolean cycleActive = false;
    //ein Request muss im update() gemacht werden, weils nur das die TCs gibt.
    IntHolder pendingRequest = null;

    // See comment in TrafficEventRegistry
    public static EventType EVENT_POSITIONCHANGED = EventType.register(1009, "EVENT_POSITIONCHANGED");

    public TeleporterSystem() {
        super(new String[]{"TeleportComponent"},
                new RequestType[]{
                        UserSystem.USER_REQUEST_TELEPORT,},
                new EventType[]{});
    }

    //Die gerade aktive Component (bzw. Avatar), cyclen mit ctrl-t
    TeleportComponent activetc = null;
    //17.1.2020 long switchframe = 0;

    /**
     * Key 't' gilt nur fuer eine bestimme TeleportComponent. Das koennen z.B. mehrere Avatare sein.
     * TODO: oder auch mehrere Cycle in einer Entity sein (z.B. an verschiedenen Vehicle oder in verschiedenen Regionen).20.2.18 Immer noch??
     *
     * @param group
     * @param tpf
     */
    @Override
    public void update(EcsEntity entity, EcsGroup group, double tpf) {
        TeleportComponent curenttc = (TeleportComponent) group.cl.get(0);

        if (activetc == null) {
            // beim ersten Mal und beim cyclen.
            activetc = curenttc;
            logger.debug("switching to tc oberver " + curenttc.getObserverName());
        }

        // Nicht im selben Frame nochmal die Keys abfragen und nochmal cyclen, sonst bin ich wieder am Anfang.
        // 17.1.2020: Seit Request nicht mehr erforderlich.Vielmehr muss der needsupdate check immer für alle gemacht
        // werden, sonst geht was verloren.
        /*if (switchframe == AbstractSceneRunner.getInstance().getFrameCount()) {
            return;
        }
        // und auch nicht fuer nicht aktive
        if (activetc != curenttc) {
            return;
        }*/
        TeleportComponent tc = activetc;

        //13.10.19: jetzt über Request. Der stepTo muss hier bleiben wegen init().
        /*if (Input.GetKeyDown(KeyCode.T)) {
            // Cyclen der Position des aktuellen Model (das Pilot hat)
            if (Input.GetKey(KeyCode.Shift)) {
                cyclePosition(tc, (false));
            } else {
                if (Input.GetKey(KeyCode.Ctrl)) {
                    //dann wird der naechste beim naechsten update() gesetzt.
                    activetc = null;
                    switchframe = AbstractSceneRunner.getInstance().getFrameCount();
                    return;
                } else {
                    cyclePosition(tc, (true));
                }
            }
        }*/
        if (pendingRequest != null) {
            int option = pendingRequest.v;
            logger.debug("pendingRequest.option=" + option);
            if (option == 4) {
                //wird woanders gemacht.
            } else {
                if (option == 1) {
                    cyclePosition(tc, (false));

                } else {
                    if (option == 2) {
                        //dann wird der naechste beim naechsten update() gesetzt.
                        activetc = null;
                        logger.debug("activetc=null");
                    } else {
                        cyclePosition(tc, (true));
                    }
                }
                pendingRequest = null;
            }
        }
        //17.2.20: der needsupdate muss für alle gemacht werden, nicht nur der aktive.
        if (tc.needsupdate) {
            // 26.10.18: Das kann man doch ueber stepTo() machen, oder nicht?
            // Ich weiss nicht mehr woher das kam, fuer usermode ist es aber genau das richtige. Also stepTo().
            // 17.1.20: Es muss ja auch ein Event verschickt werden.
            doTeleport(tc);
            tc.needsupdate = false;
        }
        if (curenttc.needsupdate) {
            doTeleport(curenttc);
            curenttc.needsupdate = false;
        }
    }

    public void cycle() {
        cyclePosition(activetc, (true));
    }

    @Override
    public void init(EcsGroup group) {
        if (group != null) {
            TeleportComponent pc = (TeleportComponent) group.cl.get(0);
            doTeleport(/*pc.getI,*/pc);
        }
    }

    @Override
    public void frameinit() {

    }

    @Override
    public boolean processRequest(Request request) {
        logger.debug("got request " + request.getType());
        if (request.getType().equals(UserSystem.USER_REQUEST_TELEPORT)) {
            pendingRequest = (IntHolder) request.getPayloadByIndex(0);
            if (pendingRequest.v == 4) {
                String destination = (String) request.getPayloadByIndex(1);
                SystemManager.processEntityGroups(this.getGroupId(), (entity, group) -> {
                    TeleportComponent tc = (TeleportComponent) group.cl.get(0);
                    int index = tc.findPoint(destination);
                    logger.debug("teleport request 4 for destination label '" + destination + "' in entity " + entity.getName() + ",index=" + index);
                    if (index != -1) {
                        tc.stepTo(index);
                    }
                });
                pendingRequest = null;
            }
            return true;
        }
        return false;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    /**
     * Tatsaechlich der Teleport an die Position des CURRENT index.
     * Inkl. Event des Position Change.
     *
     * @param vc
     * @return
     */
    private LocalTransform doTeleport(TeleportComponent vc) {
        //logger.debug("stepping to position " + vc.getIndex() + " in observer " + vc.observer.getName());
        /*LocalTransform posrot = vc.getPosRot();
        if (posrot == null) {
            //no point to step to yet.
            return null;
        }*/
        /*SceneNode parent = null;
        if (vc.getParent() != null) {
            parent = vc.getParent();
        }*/
        //SceneNode node = vc.observer;
        // 28.10.17: Der setNewStep setzt nicht den parent (bzw sein parent ist ein anderer).
        // 24.10.19: Jetzt doch in eigenem setNewStep
        LocalTransform posrot =/*TeleportComponent*/vc.setNewStep(/*node, posrot.position, posrot.rotation,*/ animated);
        if (posrot == null) {
            //no point to step to yet.
            return null;
        }
        //logger.debug("distance to (0,0,0) isType " + Vector3.getDistance(posrot.position, new Vector3()));
        // Neue Position publishen (fuer Scenery)
        // 3.5.25: 'teleport' is one of the use cases for EVENT_POSITIONCHANGED.
        // 4.5.25: Parameter change from "posrot" to position only.
        SystemManager.sendEvent(buildPositionChanged(posrot.position));

        return posrot;
    }

    public LocalTransform step(TeleportComponent vc, double tpf, boolean forward) {
        int newindex = vc.step(forward);
        /*if (vc.point.size() > 0) {
            if (forward) {
                if (++vc.index >= vc.point.size())
                    vc.index = 0;
            } else {
                if (--vc.index < 0)
                    vc.index = vc.point.size() - 1;
            }
            return stepTo(vc.index, vc);
        }*/
        if (newindex != -1) {

            return doTeleport(/*newindex,*/ vc);
        }
        return null;
    }

    /**
     * Might lead to stuttering, especially in large 3D universes,
     */
    public void setAnimated(boolean animated) {
        this.animated = animated;
    }

    private void cyclePosition(TeleportComponent tc, boolean forward) {
        //positioncontroller.step(true, 0, forward);

        LocalTransform newpos = step(tc, 0, forward);
        logger.debug("cyclePosition to" + newpos);
        if (newpos != null) {
            /*FlightLocation fl = FlightLocation.fromPosRot(newpos);
            PositionInit.initPositionFromGeod(fl);
            //18.4.17: Pos event macht jetzt TeleporterSystem
            //SystemManager.sendEvent(EcsSystemEvent.buildPositionChangedEvent(newpos));
            //hud.setText(0, "pos: " + entity.scenenode.object3d.getPosition().dump(""));
            hud.setText(0, "pos: " + tc.label.get(tc.index));*/
        }
    }

    /**
     * 7.7.20: Useful?
     * 25.5.24: At least in (external) tests its useful.
     *
     * @param activetc
     */
    public void setActivetc(TeleportComponent activetc) {
        this.activetc = activetc;
    }

    public static Event buildPositionChanged(Vector3 position) {
        return new Event(TeleporterSystem.EVENT_POSITIONCHANGED, new Payload().addPosition(position));
    }

    /**
     * 9.10.19: Return the entity where teleport currently is attached.
     * In travelling probably the vehicle where avatar is located.
     * 12.03.25: The implementation seems OK so far.
     * 17.05.25: Moved here from getAvatarVehicle() in BasicTravelScene.
     * <p>
     * Returns null when teleport isn't attached.
     */
    public static EcsEntity getTeleportEntity() {
        return getTeleportEntity(TeleportComponent.getTeleportComponent(UserSystem.getInitialUser()));
    }

    public static EcsEntity getTeleportEntity(TeleportComponent tc) {

        String name = tc.getTargetEntity();
        if (name == null) {
            //21.3.25 no need for warning. Might just happen.
            return null;
        }
        // 19.5.25 No longer limit to vehicles but be more generic
        //return TrafficHelper.findVehicleByName(name);
        List<EcsEntity> es = EcsHelper.findEntitiesByName(name);
        if (es.size() == 0) {
            return null;
        }
        // warn about multiple? But why? Just could happen. Mabye we need to find by id? Hmmm
        return es.get(0);
    }
}
