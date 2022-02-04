package de.yard.threed.engine;

/**
 * A 'WASD' FPS controller for a transform in general. It is not grounded which means freely moves in 3D.
 * Uses 'R' for rolling.
 *
 * <p>
 * Similar to THREE.FirstPersonControls, but without lookat.
 * <p>
 * 28.11.16: Funktional was ähnliches für ECS ist MovingComponent. Das hier ist aber speziell zur Nutzung ausserhalb ECS.
 * <p>
 * 16.4.19: Irgendwie doof, dass hier so viele KEys belegt werden. Aber Naja.
 * 14.5.19: Statt rotateOnAxis() mal rotationvector/matrix mit forwat/lookat/up versuchen. TODO das hat aber andere auswirkungen??
 */

import de.yard.threed.core.*;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

public class FirstPersonController {
    static Log logger = Platform.getInstance().getLog(FirstPersonController.class);
    //the rotation around the Y axis of the camera
    // Der Speed muss zur Skalierung der Szene passen. Abhaengig davon kann 10 zu
    // schnell oder zu langsam sein.
    private double movementSpeed = 10.0f; //move 10 units per getSecond
    private double rotationSpeed = 20.0f; //move 10 units per getSecond
    float mouseSensitivity = 0.05f;
    private Transform target;
    private Point startdrag;
    private Point possibleMoveByMouse = null;
    public boolean moveByMouseEnabled = true;

    public FirstPersonController(Transform target) {
        // logger.debug("Building FirstPersonController ");
        this.target = target;
    }

    public void update(double tpf) {
        //logger.debug("update: tpf="+tpf);
        if (Input.GetKey(KeyCode.UpArrow)) {
            incPitch(target, new Degree(rotationSpeed * tpf));
        }
        if (Input.GetKey(KeyCode.DownArrow)) {
            incPitch(target, new Degree(-rotationSpeed * tpf));
        }
        if (Input.GetKey(KeyCode.LeftArrow)) {
            incHeading(target, new Degree(rotationSpeed * tpf));
        }
        if (Input.GetKey(KeyCode.RightArrow)) {
            incHeading(target, new Degree(-rotationSpeed * tpf));
        }
        if (Input.GetKey(KeyCode.W)) {
            moveForward(target, +movementSpeed * tpf);
        }
        if (Input.GetKey(KeyCode.S)) {
            moveForward(target, -movementSpeed * tpf);
        }
        if (Input.GetKey(KeyCode.A)) {
            moveSidew(-movementSpeed * tpf);
        }
        if (Input.GetKey(KeyCode.D)) {
            moveSidew(movementSpeed * tpf);
        }
        // Is 'R' a common standard for rolling?
        if (Input.GetKey(KeyCode.R)) {
            //roll
            if (Input.GetKey(KeyCode.Shift)) {
                incRoll(target, new Degree(rotationSpeed * tpf));
            } else {
                incRoll(target, new Degree(-rotationSpeed * tpf));
            }
        }
        if (moveByMouseEnabled) {
            Point point = Input.getMousePress();
            if (point != null) {
                startdrag = point;
                possibleMoveByMouse = point;
                //logger.debug("possible drag from " + startdrag);
            }
            point = Input.getMouseMove();
            if (point != null && startdrag != null) {
                Point offset = point.subtract(startdrag);
                //logger.debug("dragging offset " + offset);
                double dragfactor = 0.003;
                double dragtpfheading = (double) offset.getX() * dragfactor;
                incHeading(target, new Degree(rotationSpeed * dragtpfheading));
                double dragtpfpitch = -(double) offset.getY() * dragfactor;
                incPitch(target, new Degree(rotationSpeed * dragtpfpitch));
                startdrag = point;
                possibleMoveByMouse = null;
            }
            point = Input.getMouseClick();
            if (point != null) {
                // mouse released
                if (startdrag != null) {
                    //was drag
                    startdrag = null;
                }
                possibleMoveByMouse = null;
            }
            if (possibleMoveByMouse != null) {
                // only in top or bottom segment
                int segment = Input.getClickSegment(possibleMoveByMouse, Scene.getCurrent().getDimension(), 5);
                logger.debug("clicked segment:" + segment);
                switch (segment) {
                    case 2:
                        moveForward(target, -movementSpeed * tpf);
                        break;
                    case 22:
                        moveForward(target, +movementSpeed * tpf);
                        break;
                }
            }
        }
    }

    /**
     * static to be used from outside
     *
     * @param inc
     */
    public static void incHeading(Transform target, Degree inc) {
        target.rotateOnAxis(new Vector3(0, 1, 0), inc);
    }

    /**
     * static to be used from outside
     *
     * @param inc
     */
    public static void incPitch(Transform target, Degree inc) {
        target.rotateOnAxis(new Vector3(1, 0, 0), inc);
    }

    public static void incRoll(Transform target, Degree inc) {
        target.rotateOnAxis(new Vector3(0, 0, 1), inc);
    }

    public void setMovementSpeed(double movementSpeed) {
        this.movementSpeed = movementSpeed;
    }

    public void setRotationSpeed(double rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
    }

    /**
     * Die Bewegung geht immer ueber die z-Achse. Die effektive Richtung
     * wird durch die aktuele Rotation berücksichtigt.
     * 15.11.16: Letzlich ist sowohl die Achse wie auch die Richtung rein willkürlich gewählt. Die Wahl entsnad aus der
     * Verwendung des FPS für eine Camera.
     * <p>
     * static to be used from outside
     */
    public static void moveForward(Transform target, double amount) {
        target.translateOnAxis(new Vector3(0, 0, -1), amount);
    }

    public void moveSidew(double amount) {
        target.translateOnAxis(new Vector3(1, 0, 0), amount);
    }
}