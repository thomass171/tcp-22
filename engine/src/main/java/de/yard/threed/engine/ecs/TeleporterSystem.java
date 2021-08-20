package de.yard.threed.engine.ecs;

import de.yard.threed.core.*;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.EventRegistry;
import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.platform.common.*;


/**
 * "Sehr schnelles" Versetzen eines Objektes (EcsEntity). Anders als ein MovingSystem, das auf einer Geschwindigkeitskomponente basiert.Irgendwie ist das alles zwar ähnlich,
 * aber so sehr ist es doch nicht dasselbe. Bei der animated teleport sieht das wohl so ähnlich aus, aber trotzdem ist es doch deutlich anders.
 * <p>
 * Stammt aus dem StepController. Siehe auch TeleportComponent.
 * Aehnlich zum deprecated ViewpointSystem. Um sich umzusehen, brauchts noch ein Observersystem?
 * Die TeleportComponent befindet sich typischerweise in einem Avatar Entity. NeeNee, eher Vehicle?
 * <p>
 * 12.10.19: Obacht Denkfehler!  Die TCs haengen nicht - unbedingt - am Avatar, schon gar nicht mehrere, sondern an Z.B Vehicles. Der Avatar wie auch Vehicle haben ihre TC um sich
 * irgendwohin zu positionieren. Die TC des Avatar wird erweitert um Viewpoints eines Vehicle, um z.B. im Cockpit zu sitzen oder einen Seitenblick aufs Vehicle zu haben.
 * Navigator hat z.B. eine TC mit den ganzen POIs.
 * <p>
 * Hier kennt er nur eine aktuelle, was zur sauberen Trennung ueber eine Id gehen sollte.
 * Created by thomass on 09.01.17.
 */
public class TeleporterSystem extends DefaultEcsSystem {
    Log logger = Platform.getInstance().getLog(TeleporterSystem.class);
    private boolean animated = true;
    boolean cycleActive = false;
    //ein Request muss im update() gemacht werden, weils nur das die TCs gibt.
    IntHolder pendingRequest = null;

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
                    logger.debug("teleport request 4 for entity " + entity.getName()+",index="+index);
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

    /**
     *
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
        SystemManager.sendEvent(new Event(EventRegistry.EVENT_POSITIONCHANGED, new Payload(posrot)));

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

    public void setAnimated(boolean animated) {
        this.animated = animated;
    }

    private void cyclePosition(TeleportComponent tc, boolean forward) {
        //positioncontroller.step(true, 0, forward);
        LocalTransform newpos = step(tc, 0, forward);
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
     * 7.7.20: Ist das wirklich sinnvoll? Oder eine Kruecke?
     * @param activetc
     */
    public void setActivetc(TeleportComponent activetc) {
        this.activetc = activetc;
    }
}
