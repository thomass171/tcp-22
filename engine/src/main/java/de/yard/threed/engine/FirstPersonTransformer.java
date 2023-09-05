package de.yard.threed.engine;

import de.yard.threed.core.Degree;
import de.yard.threed.core.Vector3;

/**
 * Decoupled from FirstPersonController.
 */
public class FirstPersonTransformer {
    //the rotation around the Y axis of the camera
    // speed must fit to the scene. The default might be too high/low.
    private double movementSpeed = 10.0f; //move 10 units per getSecond
    private double rotationSpeed = 20.0f; //move 10 units per getSecond
    private Transform target;

    public FirstPersonTransformer(Transform target) {
        // logger.debug("Building FirstPersonController ");
        this.target = target;
    }

    public void incPitch(double tpf) {
        Transform.incPitch(target, new Degree(rotationSpeed * tpf));
    }

    public void incHeading(double tpf) {
        Transform.incHeading(target, new Degree(rotationSpeed * tpf));
    }

    public void incHeading(Degree inc) {
        target.rotateOnAxis(new Vector3(0, 1, 0), inc);
    }

    public void incRoll(double tpf) {
        Transform.incRoll(target, new Degree(rotationSpeed * tpf));
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

    public void moveForwardAsCamera(double amount) {
        Transform.moveForwardAsCamera(target, amount);
    }

    public void moveSidew(double amount) {
        target.translateOnAxis(new Vector3(1, 0, 0), amount);
    }
}
