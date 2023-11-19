package de.yard.threed.engine;

import de.yard.threed.core.Degree;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

/**
 * Decoupled from FirstPersonController.
 */
public class FirstPersonTransformer {
    static Log logger = Platform.getInstance().getLog(FirstPersonTransformer.class);
    //the rotation around the Y axis of the camera
    // speed must fit to the scene. The default might be too high/low.
    private double movementSpeed = 2.0f; //move 2 units per getSecond
    private double rotationSpeed = 20.0f; //move 10 units per getSecond
    private Transform target;
    // involves all three axes
    static public int ROTATE_MODE_ADDITIVE = 1;
    // single axis
    static public int ROTATE_MODE_PERAXIS = 2;
    int rotateMode;
    // default straight to -z
    double heading = 0, pitch = 0;

    public FirstPersonTransformer(Transform target) {
        // logger.debug("Building FirstPersonController ");
        this.target = target;
        this.rotateMode = ROTATE_MODE_ADDITIVE;
    }

    public FirstPersonTransformer(Transform target, int rotateMode) {
        // logger.debug("Building FirstPersonController ");
        this.target = target;
        this.rotateMode = rotateMode;
    }

    public void incPitchByDelta(double delta) {
        Transform.incPitch(target, new Degree(rotationSpeed * delta));
    }

    public void incPitch(Degree inc) {
        target.rotateOnAxis(new Vector3(1, 0, 0), inc);
    }

    public void incHeadingByDelta(double delta) {
        Transform.incHeading(target, new Degree(rotationSpeed * delta));
    }

    public void incHeading(Degree inc) {
        target.rotateOnAxis(new Vector3(0, 1, 0), inc);
    }

    public void incRollByDelta(double delta) {
        Transform.incRoll(target, new Degree(rotationSpeed * delta));
    }

    public void incRoll(Degree inc) {
        target.rotateOnAxis(new Vector3(0, 0, 1), inc);
    }

    public void setMovementSpeed(double movementSpeed) {
        this.movementSpeed = movementSpeed;
    }

    public double getRotationSpeed() {
        return rotationSpeed;
    }

    public double getMovementSpeed() {
        return movementSpeed;
    }

    public void setRotationSpeed(double rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
    }

    public void moveForward(double amount) {
        Transform.moveForward(target, amount);
    }

    public void moveForwardByDelta(double delta) {
        Transform.moveForward(target, movementSpeed * delta);
    }

    public void moveForwardAsCamera(double amount) {
        Transform.moveForwardAsCamera(target, amount);
    }

    public void moveForwardAsCameraByDelta(double delta) {
        Transform.moveForwardAsCamera(target, movementSpeed * delta);
        logger.debug("new position:" + getTransform().getPosition());
    }

    public void moveSidew(double amount) {
        target.translateOnAxis(new Vector3(1, 0, 0), amount);
    }

    public void mouseMove(int dx, int dy) {
        logger.debug("dx=" + dx + ",dy=" + dy);
        if (rotateMode == ROTATE_MODE_ADDITIVE) {
            // rotating involves all axes, accumulating diviations (requiring roll to fix)
            // TODO needs a kind of mouse/cursor lock
            incHeading(new Degree(((double) -dx / 5)));
            incPitch(new Degree(((double) dy / 5)));
        }
        if (rotateMode == ROTATE_MODE_PERAXIS) {
            heading += ((double) -dx / 100);
            pitch += ((double) dy / 100);
            Quaternion rotation = Quaternion.buildFromAngles(pitch, heading, 0);
            target.setRotation(rotation);
        }
    }

    public Transform getTransform() {
        return target;
    }
}
