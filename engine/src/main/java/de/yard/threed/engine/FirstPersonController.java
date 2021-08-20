package de.yard.threed.engine;

/**
 * Ein FPS Controller fuer eine Camera bzw. allgemein für ein Transform.
 * <p>
 * In vager Anlehnung an den THREE.FirstPersonControls, aber ohne dessen lookat.
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
    private float movementSpeed = 10.0f; //move 10 units per getSecond
    private float rotationSpeed = 20.0f; //move 10 units per getSecond
    float mouseSensitivity = 0.05f;
    private Degree heading = new Degree(0);
    private Degree pitch = new Degree(0);
    private Transform target;
    // Durch fortgesetztes Rotieren (Quaterion mults) scheinen sich Rundungsartefakten zu haeufen.
    // Darum optional die Rotation absolut nachhalten. Das ist aber nicht die einzige Ursache,
    // und es verhindert das Setzen der Camerarotation von aussen (das springt dann). Das ist auch doof.
    // 15.3.16: Darum erstmal wieder aus.
    private boolean useabsrotation = false;
    //Das mit dem z0 Flag ist doch irgendwie ungar. TODO upVector. Obwohl das für einen generischen FPC auch nicht passt. z0 ist ja auch sowas wie upVector.
    private boolean z0 = false;
    private Vector3 upVector = null;
    private Point startdrag;
    private Point possibleMoveByMouse = null;
    private boolean fixupvector = false;

    public FirstPersonController(Transform target) {
        // logger.debug("Building FirstPersonController ");
        this.target = target;
    }

    public FirstPersonController(Transform target, Vector3 up) {
        // logger.debug("Building FirstPersonController ");
        this.target = target;
        this.upVector = up;
        //22.5.19 Kokolores. this.fixupvector = true;
    }

    public void update(double tpf) {
        //logger.debug("update: tpf="+tpf);
        if (Input.GetKey(KeyCode.UpArrow)) {
            incPitch(target, new Degree(rotationSpeed * tpf));
            fixUpVector();
        }
        if (Input.GetKey(KeyCode.DownArrow)) {
            incPitch(target, new Degree(-rotationSpeed * tpf));
            fixUpVector();
        }
        if (Input.GetKey(KeyCode.LeftArrow)) {
            incHeading(target, new Degree(rotationSpeed * tpf), z0);
            fixUpVector();
        }
        if (Input.GetKey(KeyCode.RightArrow)) {
            incHeading(target, new Degree(-rotationSpeed * tpf), z0);
            fixUpVector();
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
        if (Input.GetKey(KeyCode.R)) {
            //roll
            if (Input.GetKey(KeyCode.Shift)) {
                incRoll(target, new Degree(rotationSpeed * tpf), z0);
            } else {
                incRoll(target, new Degree(-rotationSpeed * tpf), z0);
            }
        }
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
            incHeading(target, new Degree(rotationSpeed * dragtpfheading), z0);
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

    /**
     * static, um auch von woanders verwendet werden zu koennen.
     * 17.7.17:Optional fuer z0 Ebene.
     *
     * @param inc
     */
    public static void incHeading(Transform target, Degree inc, boolean z0) {
        /*if (useabsrotation){
            heading = new Degree(heading.degree+inc.degree);
            Quaternion q = new Quaternion(pitch,heading,new Degree(0));
            target.setRotation(q);
        }else {*/
        //target.incHeading( inc);
        if (z0) {
            target.rotateOnAxis(new Vector3(0, 0, 1), inc);
        }
        target.rotateOnAxis(new Vector3(0, 1, 0), inc);

        //}
    }

    /**
     * static, um auch von woanders verwendet werden zu koennen.
     *
     * @param inc
     */
    public static void incPitch(Transform target, Degree inc) {
       /* if (useabsrotation){
            pitch = new Degree(pitch.degree+inc.degree);
            Quaternion q = new Quaternion(pitch,heading,new Degree(0));
            target.setRotation(q);
        }else {*/
        //target.incPitch( inc);
        target.rotateOnAxis(new Vector3(1, 0, 0), inc);

        //}
    }

    /**
     * 14.5.19: fix up Vector after a heading/pitch rotation for avoiding the need to roll.
     * Das scheint zu funktionieren, verhindert ab zu starken pitch (sowohl oben wie unten).  Puuh, aber im Endeffekt ist das ganz ok.
     * Aber das sollte ein spezielles Feature sein. Und tuts auf Pads nicht??
     * 22.5.19: Das ist irgendwie Kokelores, weil man nicht mehr nach unten/oben sehen kann.
     */
    private void fixUpVector() {
        if (upVector == null || !fixupvector) {
            return;
        }
        Vector3 left = target.getWorldModelMatrix().getLeft();
        Vector3 forward = target.getWorldModelMatrix().getForward();
        //logger.debug("up=" + up);
        Quaternion rotation = MathUtil2.extractQuaternion(left, upVector, forward);
        target.setRotation(rotation);
        Vector3 up = target.getWorldModelMatrix().getUp();
        //logger.debug("up="+up);
    }

    public static void incRoll(Transform target, Degree inc, boolean z0) {
       /* if (useabsrotation){
            pitch = new Degree(pitch.degree+inc.degree);
            Quaternion q = new Quaternion(pitch,heading,new Degree(0));
            target.setRotation(q);
        }else {*/
        //target.incPitch( inc);
        if (z0) {
            target.rotateOnAxis(new Vector3(0, 0, 1), inc);
        } else {
            target.rotateOnAxis(new Vector3(0, 0, 1), inc);
        }
        //}
    }

    public void setMovementSpeed(float movementSpeed) {
        this.movementSpeed = movementSpeed;
    }

    public void setRotationSpeed(float rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
    }

    /**
     * Die Bewegung geht immer ueber die z-Achse. Die effektive Richtung
     * wird durch die aktuele Rotation berücksichtigt.
     * 15.11.16: Letzlich ist sowohl die Achse wie auch die Richtung rein willkürlich gewählt. Die Wahl entsnad aus der
     * Verwendung des FPS für eine Camera.
     * <p>
     * static, um auch von woanders verwendet werden zu koennen.
     */
    public static void moveForward(Transform target, double amount) {
        target.translateOnAxis(new Vector3(0, 0, -1), amount);
    }

    public void moveSidew(double amount) {
        target.translateOnAxis(new Vector3(1, 0, 0), amount);
    }


}