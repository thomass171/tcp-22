package de.yard.threed.maze;

import de.yard.threed.core.*;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.*;
import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.ecs.EcsComponent;
import de.yard.threed.engine.ecs.EcsEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Something moving in the maze, eg. Player, boxes, Bots.
 * <p>
 * Created by thomass on 11.01.16.
 */
public class MoverComponent extends EcsComponent implements GridMover {
    private WalkStatus walkstatus = null;
    private RotateStatus rotatestatus = null;
    Log logger = Platform.getInstance().getLog(MoverComponent.class);
    //the rotation around the Y axis of the camera
    // Der Speed muss zur Skalierung der Szene passen. Abhaengig davon kann 10 zu
    // schnell oder zu langsam sein.
    private float movementSpeed = 10.0f; //move 10 units per getSecond
    private float rotationSpeed = 5.0f; //1 ist in webgl zu langsam. Ziel ist eine Vierteldrehung in 0.5 Sekunden(?). 7.3.17: 5 statt 3 wegen Unity. Ist aber trotzdem gefühlt nicht schneller.

    private Degree yaw = new Degree(0);
    public SimpleTransform movable;
    public static boolean debugmovement = false;
    static String TAG = "MazeMovingComponent";
    private GridMover gridMover;
    public static final int MOVER_PLAYER = 1;
    public static final int MOVER_BOX = 2;
    // a box isn't a player, but a bot/monster is
    //public static final int MOVER_BOT = 3;
    private boolean player;
    //private int type = -1;

    public MoverComponent(SimpleTransform movable, boolean player, StartPosition startPosition, int team) {
        this.movable = movable;
        this.player = player;
        gridMover = new SimpleGridMover(startPosition, this, team);
    }

    /**
     *
     */
    public GridMovement rotate(boolean left) {
        if (!isRotating() && !isWalking()) {
            GridMovement gridMovement = gridMover.rotate(left);
            setRotateStatus(null, left);
            return gridMovement;
        }
        return null;
    }

    /**
     * Start rotating by setting a rotate state.
     * <p>
     * Liefert das GridMovement, wenn die Drehung ausgefuehrt wurde, sonst null.
     */
    private void setRotateStatus(GridState state, boolean left) {

        //turnpending += 90;
        if (rotatestatus != null) {
            // Drehung dazupacken, aber es gibt ja noch keine gequeute Bewegung
            // rotatestatus.add(90);
        } else {
            rotatestatus = new RotateStatus((left) ? 1 : -1);
        }
        //return state.setRotateStatus(left);
    }

    public boolean isWalking() {
        return walkstatus != null;
    }

    public boolean isRotating() {
        if (rotatestatus == null) {
            return false;
        }
        return true;
    }

    public boolean isMoving() {
        return isWalking() || isRotating();
    }

    /**
     * Moving in direction of orientation.
     */
    public MoveResult walk(GridMovement movement, GridState gridState, MazeLayout mazeLayout) {
        return move(movement, gridMover.getOrientation(), gridState, mazeLayout);
    }

    /**
     * Moving in *some* direction which not needs to be the orientation.
     * Relocate is allowed while moving because its not self triggered.
     */
    public MoveResult move(GridMovement movement, GridOrientation orientation, GridState gridState, MazeLayout mazeLayout) {

        if ((!isRotating() && !isWalking()) || movement.isRelocate()) {
            MoveResult moveResult = gridMover.move(movement, orientation, gridState, mazeLayout);
            if (moveResult != null) {
                // don't walk myself when kick or pull!
                GridMovement gridMovement = moveResult.movement;
                if (!gridMovement.isKick() && !gridMovement.isPull()) {
                    return new MoveResult(setWalkStatus(movement, null, orientation));
                }
            }
        }
        return null;
    }

    @Override
    public MoverComponent getParent() {
        return null;
    }

    public void setMovable(SimpleTransform movable){
        this.movable=movable;
    }
    /**
     * Liefert das Movement, wenn der Schritt ausgefuehrt wurde, sonst null.
     * 12.4.21: rotation nicht mehr reinstecken bzw. igmorieren. orientation isType needed for being moved.
     */
    private GridMovement setWalkStatus(GridMovement movement/*boolean forward*/, Quaternion
            rotation, GridOrientation orientation) {
        walkstatus = new WalkStatus(movement, false, /*rotation,*/ orientation);
        if (debugmovement) {
            logger.debug("start walk, player=" + player);
        }
        return movement;
    }

    private GridMovement setWalkStatus(GridMovement movement, GridOrientation orientation) {
        return setWalkStatus(movement, movable.getRotation(), orientation);
    }


    public static MoverComponent getMoverComponent(EcsEntity e) {
        MoverComponent m = (MoverComponent) e.getComponent(MoverComponent.TAG);
        return m;
    }

    public SimpleTransform getMovable() {
        return movable;
    }

    @Override
    public int getId() {
        return getEntityId();
    }

    @Override
    public int getTeam() {
        return gridMover.getTeam();
    }

    @Override
    public StartPosition getStartPosition() {
        return gridMover.getStartPosition();
    }

    @Override
    public String toString() {
        return "mover (" + getLocation() + ")";
    }

    /**
     * Liefert die auf Gridkoordinaten umgerechnete Richtung.
     * Das Vorzeichen von "dir" wird dabei gedreht, weil die x-Ache entgegen der 3D z-Achse läuft!
     *
     * @return
     */
    private Point getDirection(boolean forward) {
        //float[] rotation = maze.getRotation().toAngles(new float[3]);
        //float rot = rotation[1];
        //logger.debug("rotation="+Degree.buildFromRadians(rot));
        int dir = -1;
        if (!forward) {
            dir = 1;
        }
        float yawangle = (float) (yaw.getDegree() % 360);
        if (MathUtil.floatEquals(yawangle, 0)) {
            return new Point(0, -dir);
        }
        if (MathUtil.floatEquals(yawangle, 90) || MathUtil.floatEquals(yawangle, -270)) {
            return new Point(dir, 0);
        }
        if (MathUtil.floatEquals(yawangle, -90) || MathUtil.floatEquals(yawangle, 270)) {
            return new Point(-dir, 0);
        }
        return new Point(0, dir);
    }

    /**
     * 4.4.22: Redefined. Adjust visual location to final grid location. Is only used when movement completed.
     *
     */
    private void catchPosition() {
        Vector3 loc = movable.getPosition();
        // y weglassen, weil der noch bloeder springt
        loc = new Vector3(round05(loc.getX()), loc.getY()/*round05(loc.getY())*/, round05(loc.getZ()));
        Vector3 finalLoc = MazeUtils.point2Vector3(getLocation());
        loc = new Vector3(round05(finalLoc.getX()), loc.getY()/*round05(loc.getY())*/, round05(finalLoc.getZ()));
        movable.setPosition(loc);
    }

    /**
     * auf 0,5 genau runden.
     *
     * @param f
     * @return
     */
    private float round05(double f) {
        return (float) (Math.round(f * 2) / 2.0f);
    }

    /**
     * Die Rotation duch runden "fangen".
     */
    private void catchRotation() {
        yaw = new Degree((float) Math.round(yaw.getDegree()));
        Quaternion q = Quaternion.buildFromAngles(new Degree(0), yaw, new Degree(0));
        movable.setRotation(q);
    }

    private void logPosition(String label) {
        String pos = movable.getPosition().dump(" ");
        String rot = movable.getRotation().dump(" ");
        logger.debug(label + ":pos=" + pos + ",rot=" + rot);
    }

    /**
     * @param tpf
     * @return
     */
    public void update(double tpf) {
        boolean completed = false;
        // wenn tpf 0 ist, bleiben mover im maze einfach haengen
        /*7.3.22 Nonsense if (tpf < 0.001f) {
            logger.warn("Adjusting tpf " + tpf);
            tpf = 0.001f;
        }*/
        //totaltpf += tpf;
        //logger.debug("tpf="+tpf+", walkstatus="/*+totaltpf*/+walkstatus);
        // es geht nur entweder gehen oder drehen
        if (isWalking()) {
            //logPosition("vor  walk");
            Vector3 loc = movable.getPosition();
            //TODO der column(2) "Trick" in allgemeine Klasse
            //12.4.21: Kein Ahnung mehr, was es hier mit der Rotation so genau auf sich hat.
            Quaternion currentRotation = movable.getRotation(); //12.4.21 = walkstatus.currentrotation;
            Vector3 rot = Matrix4.buildRotationMatrix(/*movable.getRotation()*/currentRotation).getColumn(2);
            rot = new Vector3(rot.getX(), 0, rot.getZ()).normalize();
            // sicherstellen, dass ich nicht ueber das Ziel hinausschiesse
            double singlestep = Math.min(movementSpeed * tpf, walkstatus.walklength);
            boolean forward = walkstatus.movement.isForward() || walkstatus.movement.equals(GridMovement.ForwardMove);
            //9.3.17: Berechnung ueber orientation statt Rotation.
            //loc = loc.add(rot.multiply((forward ? -1 : 1) * singlestep));
            Point dir = walkstatus.orientation.getDirectionForMovement(walkstatus.movement).getPoint();
            loc = loc.add(new Vector3(dir.getX(), 0, -dir.getY()).multiply(singlestep));
            movable.setPosition(loc);
            if (walkstatus.walked(singlestep)) {
                // Destination reached
                catchPosition();
                walkstatus = null;
                if (debugmovement) {
                    logger.debug("walk completed. dir=" + dir + ",player=" + player);
                }
                completed = true;
            }
            //logPosition("nach walk");

        } else {
            if (isRotating()) {
                // auch hier sicherstellen, dass ich nicht ueber das Ziel hinausschiesse
                float singlestep = Math.min(rotationSpeed * 01.1f, rotatestatus.rotatelength);

                yaw = yaw.add(new Degree(rotatestatus.direction * singlestep));//rotatedirection * 01.1f * rotationSpeed;
                //Matrix4 rotmatrix = Matrix4.buildRotationYMatrix(new Degree(angle));
                Quaternion q = Quaternion.buildFromAngles(new Degree(0), yaw, new Degree(0));
                movable.setRotation(q);
                if (rotatestatus.rotated(singlestep)) {
                    // Destination reached
                    catchRotation();
                    if (debugmovement) {
                        logger.debug("catched rotation yaw=" + yaw);
                    }
                    rotatestatus = null;
                    completed = true;
                }
                if (debugmovement) {
                    logPosition("nach rot ");
                }
            }
        }
    }

    //@Override
    public void doinit() {

    }

    @Override
    public String getTag() {
        return TAG;
    }

    public void setYaw(Degree yaw) {
        this.yaw = yaw;
    }

    /**
     * not yet for multiplayer
     * <p>
     * 2.11.20
     *
     * @return
     */
    public boolean isPlayer() {
        return player;
    }

    public Point getLocation() {
        return gridMover.getLocation();
    }

    public void setLocation(Point point) {
        gridMover.setLocation(point);
        updateMovable();
    }

    @Override
    public List<GridMovement> getMoveOptions(GridState gridState, MazeLayout mazeLayout) {
        if (isMoving()) {
            return new ArrayList<GridMovement>();
        }
        return gridMover.getMoveOptions(gridState, mazeLayout);
    }

    public GridOrientation getOrientation() {
        return gridMover.getOrientation();
    }

    public GridOrientation getGridOrientation() {
        //10.4.21 TODO stimmt das? Nee.
        if (walkstatus != null) {
            return walkstatus.orientation;
        }
        return gridMover.getOrientation();
    }

    public void setOrientation(GridOrientation orientation) {
        gridMover.setOrientation(orientation);
        updateMovable();
    }

    public GridMover getGridMover() {
        return gridMover;
    }

    /**
     * Keep 'movable' in sync with 'gridMover'.
     */
    public void updateMovable() {
        Vector3 v = MazeUtils.point2Vector3(gridMover.getLocation());
        // y stays 0. The mover model should have a aligned container to fit its height.
        movable.setPosition(new Vector3(v.getX(), 0, v.getZ()));
        yaw = gridMover.getOrientation().getYaw();
        catchRotation();
    }

    public boolean isOnHomeField(MazeLayout layout) {
        return MazeUtils.getHomesOfTeam(layout, getTeam()).contains(getLocation());
    }
}

/**
 * Also used for relocate.
 */
class WalkStatus {
    public double walklength = 0;
    //public boolean forward;
    public GridMovement movement;
    Log logger = Platform.getInstance().getLog(WalkStatus.class);
    private boolean movingblock;
    //12.4.21 public Quaternion currentrotation;
    GridOrientation orientation;

    /**
     * currentrotation ist erforderlich, weil die Bewegung ja in Blickrichtung erfolgt und damit
     * beruecksichtigt werden muss. 12.4.21: Wirklich? mal ohne versuchen, wird offenbar eh nicht verwendet.
     *
     * @param movingblock
     */
    public WalkStatus(GridMovement movement/*boolean forward*/, boolean movingblock, /*Quaternion currentrotation,*/ GridOrientation orientation) {
        //17.11.15: Doch mal mit Einer Schritten probieren.
        walklength = MazeDimensions.GRIDSEGMENTSIZE /* *2*/;
        this.movement = movement;
        this.movingblock = movingblock;
        //12.4.21 this.currentrotation = currentrotation;
        this.orientation = orientation;
    }

    /**
     * Liefert true, wenn das Ziel erreicht ist
     *
     * @param singlestep
     */
    public boolean walked(double singlestep) {
        // relocate is no move 'with movement'. Immediately complete.
        if (movement.isRelocate()) {
            walklength = 0;
            return true;
        }

        walklength -= singlestep;
        if (MoverComponent.debugmovement) {
            logger.debug("walked " + singlestep + ", remaining " + walklength);
        }
        if (walklength > 0.00001) {
            return false;
        }
        walklength = 0;
        return true;

    }

}

/**
 * Also used for relocate.
 */
class RotateStatus {
    public int direction = 0;
    Degree yawdestination = null;
    // i Grad mit (oder ohne?) Vorzeichen fuer Links/Rechts
    public float rotatelength = 0;
    Log logger = Platform.getInstance().getLog(RotateStatus.class);

    public RotateStatus(int direction) {
        rotatelength = 90;
        //yawdestination = new Degree(((yawdestination == null) ? 0 : yawdestination.degree) + yaw.degree + 90);
        this.direction = direction;
    }

    /**
     * Liefert true, wenn das Ziel erreicht ist
     *
     * @param singlestep
     */
    public boolean rotated(float singlestep) {
        rotatelength -= singlestep;
        if (MoverComponent.debugmovement) {
            logger.debug("rotated " + singlestep + ", remaining " + rotatelength);
        }
        if (rotatelength > 0.00001) {
            return false;
        }
        rotatelength = 0;
        return true;

    }
}
