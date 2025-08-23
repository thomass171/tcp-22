package de.yard.threed.graph;


import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.Input;
import de.yard.threed.engine.KeyCode;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.ecs.*;
import de.yard.threed.core.Event;
import de.yard.threed.core.Payload;

/**
 * Movement of an entity with a GraphMovingComponent along a graph(path).
 * This is only for simple/standard movement. More advanced graph related features
 * need dedicated Systems, eg. GroundServicesSystem.
 * <p>
 * Created by thomass on 16.09.16.
 */
public class GraphMovingSystem extends DefaultEcsSystem {
    private static Log logger = Platform.getInstance().getLog(GraphMovingSystem.class);
    //decouple of SGGeod.
    public static GraphAltitudeProvider graphAltitudeProvider;
    public static String TAG = "GraphMovingSystem";

    /**
     *
     */
    public GraphMovingSystem() {
        super(new String[]{GraphMovingComponent.TAG, VelocityComponent.TAG});
    }

    /**
     * Also shows initial position.
     * init() also called for new Entities/Components.
     *
     * @param group
     */
    @Override
    public void init(EcsGroup group) {
        if (group != null) {
            GraphMovingComponent gmc = (GraphMovingComponent) group.cl.get(0);
            adjustVisual(gmc);
        }
    }

    /**
     * 9.10.19: Hier auf Keys zu reagieren ist doch unguenstig, weil es auf ALLE Entities wirkt.
     * Und ueberhaupt sollte es eher ueber Request gehen,oder?
     * Naja, wirkt nur auf die, die keycontrolled gesetzt haben, aber trotzdem nicht schön.
     *
     * @param entity
     * @param group
     * @param tpf
     */
    @Override
    final public void update(EcsEntity entity, EcsGroup group, double tpf) {
        boolean changed = false;
        GraphMovingComponent gmc = (GraphMovingComponent) group.cl.get(0);

        VelocityComponent vc = (VelocityComponent) group.cl.get(1);

        if (gmc.hasAutomove()) {
            adjustSpeed(gmc, vc, tpf);
            moveForward(entity, gmc, vc, tpf * vc.getMovementSpeed());
            //logger.debug("new position of "+entity.getName()+entity.getId()+" isType "+gmc.getPosition());
        }

        // Intentionally no "else", because theoretically we could have both (but shouldn't, Hmm, don't know because of automove toggle).
        // TODO 3.4.25 change to requests (keys via InputtorequestSystem)
        if (gmc.keycontrolled) {
            if (Input.getKey(KeyCode.W)) {
                moveForward(entity, gmc, vc, vc.maximumSpeed * tpf);
            }
            if ((Input.getKey(KeyCode.W) && Input.getKey(KeyCode.Shift)) || Input.getKey(KeyCode.S)) {
                moveForward(entity, gmc, vc, vc.maximumSpeed * (-tpf));
            }
            // speed control interferes with adjustSpeed?. Velocity also needs an auto...TODO
            // 3.4.25 changed from +/- to PGUP/DOWN
            if (Input.getKey(KeyCode.PageUp)) {
                vc.incMovementSpeed(1);
            }
            if (Input.getKey(KeyCode.PageDown)) {
                vc.incMovementSpeed(-1);
            }
            if (Input.getKeyDown(KeyCode.A)) {
                gmc.setAutomove(!gmc.hasAutomove());
                if (!gmc.hasAutomove()) {
                    // reset
                    vc.setMovementSpeed(0);
                }
            }
        }


        /* 9.3.21: MA31: Tja, was soll man machen? Ob das jemals verwendet wurde?
        6.4.21: Wer weiss, asi Needle war ja auch suddenly weg.moved to railingScene.update()
        das war fuer asi needle! und auch in Travel c172p!
        PropertyComponent pc = PropertyComponent.getPropertyComponent(entity);
        if (pc != null) {
            pc.setSpeed(vc.getMovementSpeed());
        }*/
        //return changed;

        gmc.checkForPositionUpdate();
    }

    @Override
    public String getTag() {
        return TAG;
    }

    public void moveForward(EcsEntity entity, GraphMovingComponent gmc, VelocityComponent vc, double amount) {
        GraphPath completedpath = gmc.moveForward(amount);
        //und auch visuell die Position anpassen
        adjustVisual(gmc);
        if (completedpath != null) {
            vc.setMovementSpeed(0);
            //8.5.19 hier schon den Layer löschen, weil sonst unklar ist, welches System das macht.
            gmc.graph.removeLayer(completedpath.layer);
            SystemManager.sendEvent(new Event(GraphEventRegistry.GRAPH_EVENT_PATHCOMPLETED, new Payload(gmc.getGraph(), completedpath, entity)));
        }
    }


    /**
     * Eine "echte" Beschleunigung nachbilden.
     */
    public void adjustSpeed(GraphMovingComponent gmc, VelocityComponent vc, double deltatime) {
        double left = gmc.getRemainingPathLen();
        boolean needsbraking = false;
        boolean needsspeedup = false;
        double speedlimit = vc.getMaximumSpeed();

        if (vc.getHyperSpeedAltitude() != null) {
            //physikalisch absurd
            Vector3 position = gmc.getCurrentposition().get3DPosition();

            double altitude = graphAltitudeProvider.getAltitude(position);

            if (altitude > vc.getHyperSpeedAltitude()) {
                //-500 ->500
                //>70000 -> 10000 (36000kmh)
                double maxspeed = 1000000;
                if (gmc.getRemainingPathLen() < 100000) {
                    maxspeed /= 10;
                }
                altitude = Math.min(altitude, 70000);
                double speed = 500 + (Math.pow(altitude / 70000, 3)) * maxspeed;
                vc.setMovementSpeed(speed);
                //logger.debug("altitude=" + altitude + ",speed=" + speed);
                return;
            }
        }
        //physikalisch halbwegs korrekt
        if (!vc.hasAcceleration()) {
            return;
        }
        //Ob in einem Bogen der speed reduziert wird, haengt natürlich auch vom Radius ab. Erstmal simpel.
        if (gmc.getCurrentposition() != null) {
            if (gmc.getCurrentposition().currentedge.isArc() && gmc.getCurrentposition().currentedge.getArc().getRadius() < 15) {
                speedlimit = vc.getMaximumSpeed() / 2;
            }
        }

        // erstmal checken, ob bremsen erfoderlich ist
        // avoid ping/pong (by +1)
        if (vc.getMovementSpeed() > speedlimit + 1) {
            needsbraking = true;
        }
        double breakingdistance = vc.getBrakingDistance();//20;//10;
        if (left < breakingdistance) {
            //logger.debug("brake due to left " + left+", deltatime="+deltatime);
            needsbraking = true;
        }
        // und beschleunigen
        if (left > breakingdistance) {
            // sonst schon mal nicht
            if (vc.getMovementSpeed() < speedlimit - 1) {
                needsspeedup = true;
            }
        }

        /* Logging in vc.accelerate() should be sufficient
        if (needsbraking || needsspeedup) {
            logger.debug("needsbraking=" + needsbraking + ",needsspeedup=" + needsspeedup);
        }*/
        if (needsbraking) {
            vc.accelerate(-deltatime);
        }
        if (needsspeedup) {
            vc.accelerate(deltatime);
        }

    }

    /**
     * visuell die Position anpassen
     */
    private void adjustVisual(GraphMovingComponent gmc) {
        Vector3 offset = new Vector3();
        /*1.3.18 das war eh nur Kruecke fuer Railing. Die macht das jetzt anders. if (gmc.visualizer != null) {
            offset = gmc.visualizer.getPositionOffset();
        }*/
        LocalTransform posrot;
        if (gmc.graph instanceof ProjectedGraph) {
            posrot = getPosRot(gmc/*24.5.24, ((ProjectedGraph) gmc.graph).backProjection*/);
        } else {
            posrot = getPosRot(gmc/*24.5.24, gmc.getProjection()*/);
        }
        if (posrot != null) {
            gmc.setPosRot(posrot);
            //MA31 der Rest war schon kommentiert. SGGeod geod = SGGeod.fromCart(posrot.position);
            //logger.debug("adjustVisual: setting to pos "+ posrot.position+", "+geod.toWGS84decimalString()+ "with elevation "+geod.getElevationM());
            //TODO validateflag wegen Ressourcen. Das ist eh nur für erath 3D geeignet. Vielleicht auslagern in einen Validator
            /*if (SGGeod.fromCart(posrot.position).getElevationM() < 0){
                logger.warn("elevation < 0: "+SGGeod.fromCart(posrot.position).getElevationM() );
            }*/
        }
    }

    /**
     * Greift das auch bei Groundnet?
     * <p>
     * 29.3.20
     *
     * @param gmc
     * @return
     */
    public static LocalTransform getTransform(GraphMovingComponent gmc) {
        if (gmc.graph instanceof ProjectedGraph) {
            return getPosRot(gmc/*24.5.24), ((ProjectedGraph) gmc.graph).backProjection*/);
        }
        // keep old way for now
        return getPosRot(gmc/*24.5.24, gmc.getProjection()*/);
    }

    /**
     * Position/Rotation unter Beruecksichtigung einer evtl. Projection ermitteln.
     * 9.1.19: Etwas "richtiger". Erstmal immer von posrot ohne Projection ausgehen, und das dann unprojecten. Un eigene GraphProjection statt
     * Dependency zu Map.
     * Die Projection ist zur Darstellung, nicht die des Groundnet! Die Methode muss auch woanders hin.
     * <p>
     * Ausgelagert auch zum Testen.
     * 22.3.24: Implementation split to (Projected)Graph.
     * 24.5.24: Projection apparently not used.
     */
    public static LocalTransform getPosRot(GraphMovingComponent gmc/*, /*Map* /GraphProjection projection*/) {
        GraphPosition cp = gmc.getCurrentposition();
        if (cp != null) {
            return gmc.getGraph().getPosRot(cp, gmc.customModelRotation);
        }
        return null;
    }


}
