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
    public double movementSpeed = 0.0f;
    public double maximumSpeed = 10.0f;
    // bei null springt er sofort auf max speed
    private Double acceleration = 1.0;
    private Double deceleration = 2.5;
    public static String TAG = "VelocityComponent";
    //Mit HyperSpeed kann ein Vehicle unrealistisch hohe Geschwindkeiten erreichen, z.B. fürn Weltraum.
    private Double hyperSpeedAltitude = null;

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
        // bremsen geht drei mal staerker als beschleunigen. 
        // das ist hablbwegs plausibel: https://de.wikipedia.org/wiki/Größenordnung_(Beschleunigung)
        if (acceleration == null) {
            deceleration = null;
        } else {
            this.deceleration = acceleration * 2.5f;
        }
    }

    public void accelerate(double deltatime) {
        //if (movementSpeed < maximumSpeed){
        if (deltatime < 0) {
            // Nicht zum Stillstand bremsen, sonst komm ich vielleicht nie ans Ziel.
            double diff = (double)deceleration * deltatime;
            movementSpeed += diff;
            //logger.debug("accelerate:diff=" + diff + ",movementSpeed=" + movementSpeed + ",acceleration=" + acceleration);
            if (movementSpeed < 1) {
                movementSpeed = 1;
            }
        } else {
            movementSpeed += (double)acceleration * deltatime;
        }
        //}
    }

    public double getMaximumSpeed() {
        return maximumSpeed;
    }

    public static VelocityComponent getVelocityComponent(EcsEntity e) {
        VelocityComponent vc = (VelocityComponent) e.getComponent(VelocityComponent.TAG);
        return vc;
    }

    public double getBrakingDistance() {

        return 0.5f * (movementSpeed * movementSpeed / (double)deceleration);
    }

    public boolean hasAcceleration() {
        return acceleration!=null;
    }

    public void enableHyperSpeed(double atAltitude){
        hyperSpeedAltitude = atAltitude;
    }

    public Double getHyperSpeedAltitude(){
        return hyperSpeedAltitude;
    }
}
