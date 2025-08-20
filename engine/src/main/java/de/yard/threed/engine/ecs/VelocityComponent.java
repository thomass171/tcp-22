package de.yard.threed.engine.ecs;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

/**
 * Fuer alles was sich bewegt.
 * <p>
 * Created by thomass on 13.01.17.
 */
public class VelocityComponent extends DefaultEcsComponent {
    Log logger = Platform.getInstance().getLog(VelocityComponent.class);
    // Einheit ist m/s 
    //17.8.17: Speed beginnt jetzt bei 0 und wird beschleunigt.
    private double movementSpeed = 0.0f;
    public double maximumSpeed = 10.0f;
    // bei null springt er sofort auf max speed
    private Double acceleration = 1.0;
    private Double deceleration = 2.5;
    public static String TAG = "VelocityComponent";
    //Mit HyperSpeed kann ein Vehicle unrealistisch hohe Geschwindkeiten erreichen, z.B. fürn Weltraum.
    private Double hyperSpeedAltitude = null;
    private boolean autoSpeedUp, autoSpeedDown;
    // avoid to much logging
    private long lastLog = 0;

    public VelocityComponent() {

    }

    @Override
    public String getTag() {
        return TAG;
    }

    public void setMovementSpeed(double movementSpeed) {
        this.movementSpeed = movementSpeed;
    }

    public void incMovementSpeed(int offset) {
        this.movementSpeed += offset;

    }

    public double getMovementSpeed() {
        return movementSpeed;
    }

    public void setMaximumSpeed(double maximumSpeed) {
        this.maximumSpeed = maximumSpeed;
    }

    public void setAcceleration(Double acceleration) {
        this.acceleration = acceleration;
        // decelerate is 2.5 times stronger than accelerate
        // Sounds plausible: https://de.wikipedia.org/wiki/Größenordnung_(Beschleunigung)
        if (acceleration == null) {
            deceleration = null;
        } else {
            this.deceleration = acceleration * 2.5f;
        }
    }

    public double getAcceleration() {
        return acceleration;
    }

    public void accelerate(double deltatime) {
        // deltatime can be < 0 when it is negated for slowing down.
        if (deltatime < 0) {
            // Don't finally stop, otherwise we might never reach our target.
            double diff = (double) deceleration * deltatime;
            movementSpeed += diff;
            //logger.debug("accelerate:diff=" + diff + ",movementSpeed=" + movementSpeed + ",acceleration=" + acceleration);
            if (movementSpeed < 1) {
                movementSpeed = 1;
            }
        } else {
            movementSpeed += (double) acceleration * deltatime;
        }
        if (Platform.getInstance().currentTimeMillis() > lastLog + 1000) {
            if (deltatime < 0) {
                logger.debug("decelerated for delta " + deltatime + " and deceleration " + deceleration + " to " + movementSpeed);
            } else {
                logger.debug("accelerated for delta " + deltatime + " and acceleration " + acceleration + " to " + movementSpeed);
            }
            lastLog = Platform.getInstance().currentTimeMillis();
        }
    }

    public double getMaximumSpeed() {
        return maximumSpeed;
    }

    public static VelocityComponent getVelocityComponent(EcsEntity e) {
        VelocityComponent vc = (VelocityComponent) e.getComponent(VelocityComponent.TAG);
        return vc;
    }

    public double getBrakingDistance() {

        return 0.5f * (movementSpeed * movementSpeed / (double) deceleration);
    }

    public boolean hasAcceleration() {
        return acceleration != null;
    }

    public void enableHyperSpeed(double atAltitude) {
        hyperSpeedAltitude = atAltitude;
    }

    public Double getHyperSpeedAltitude() {
        return hyperSpeedAltitude;
    }

    public void toggleAutoSpeedUp() {
        autoSpeedUp = !autoSpeedUp;
    }

    public void toggleAutoSpeedDown() {
        autoSpeedDown = !autoSpeedDown;
    }

    public void updateByDelta(double delta) {
        if (autoSpeedUp) {
            accelerate(delta);
        }
        if (autoSpeedDown) {
            accelerate(-delta);
        }
    }
}
