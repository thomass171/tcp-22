package de.yard.threed.traffic;


import de.yard.threed.core.*;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.FirstPersonTransformer;
import de.yard.threed.engine.PositionUpdateTrigger;
import de.yard.threed.engine.Transform;
import de.yard.threed.engine.ecs.EcsComponent;
import de.yard.threed.engine.ecs.EcsEntity;

/**
 * For flying a plane that is not graph bound.
 * 02.02.25 Moves in 'FG vehicle' space, ie. x-axis points forward, y-axis points right (pitch), z-axis points up (yaw/heading)
 * <p>
 * auto-roll is tricky. See https://gamedev.stackexchange.com/questions/123535/unwanted-roll-when-rotating-camera-with-pitch-and-yaw
 * Created by thomass on 24.03.25.
 */
public class FreeFlyingComponent extends EcsComponent {
    static Log logger = Platform.getInstance().getLog(FreeFlyingComponent.class);
    public static String TAG = "FreeFlyingComponent";
    public boolean freeflyingcomponentdebuglog = true;
    // For now have nothing like FirstPersonTransformer but everything here to keep it simple
    public boolean initialLocated = false;
    // movementSpeed and autoSpeed is in VelocityComponent
    private boolean autoTurnleft, autoTurnright, autoTurnup, autoTurndown, autoRollleft, autoRollright;
    // speed must fit to the scene. The default might be too high/low. No movement without a user request for speed.
    private double rotationSpeed = 0.1f; //move 10 units per getSecond
    // In rad for more efficient calcs. default straight to -z
    double yaw = 0, pitch = 0, roll = 0;
    private Transform transform;
    // optionally try to roll without user controls. See below, it is really hard. So keep user controls for now
    //private boolean autoRoll = true;

    boolean autoStabalize = true;
    private PositionUpdateTrigger positionUpdateTrigger = new PositionUpdateTrigger();
    // Execute each 12th of code reaches
    private Threshold positionCheckThreshold = new Threshold(12);

    /**
     *
     */
    public FreeFlyingComponent(Transform transform) {
        this.transform = transform;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    public static FreeFlyingComponent getFreeFlyingComponent(EcsEntity e) {
        return (FreeFlyingComponent) e.getComponent(FreeFlyingComponent.TAG);
    }

    /**
     * movementSpeed comes from VelocityComponent
     */
    public void updateByDelta(double delta, double movementSpeed) {
        if (autoTurnleft) {
            incYawByDelta(delta);
        }
        if (autoTurnright) {
            incYawByDelta(-delta);
        }
        if (autoTurnup) {
            incPitchByDelta(delta);
        }
        if (autoTurndown) {
            incPitchByDelta(-delta);
        }
        if (autoRollleft) {
            incRollByDelta(delta);
        }
        if (autoRollright) {
            incRollByDelta(-delta);
        }

            /*
            solution for unwanted roll? probably not.
             double sina = MathUtil2.sin(pitch);
            double sinb = MathUtil2.sin(yaw);
            double sing = MathUtil2.sin(roll);
            double cosa = MathUtil2.cos(pitch);
            double cosb = MathUtil2.cos(yaw);
            double cosg = MathUtil2.cos(roll);
                    // From https://msl.cs.uiuc.edu/planning/node102.html and others.
                    // But this matrix apparently doesn't solve the 'unwanted-roll' problem. Maybe our math is not corect.
                    Matrix3 rotation = new Matrix3(
                            cosa * cosb, cosa * sinb * sing - sina * cosg, cosa * sinb * cosg + sina * sing,
                            sina * cosb, sina * sinb * sing + cosa * cosg, sina * sinb * cosg - cosa * sing,
                            -sinb, cosb * sing, cosb * cosg
                    );
                    transform.setRotation(rotation.extractQuaternion());
             */

        transform.rotateOnAxis(new Vector3(0, 0, 1), yaw / 100.0);
        transform.rotateOnAxis(new Vector3(0, 1, 0), pitch / 200.0);
        transform.rotateOnAxis(new Vector3(1, 0, 0), roll / 100.0);

        // vehicle looks to -x, so movement also is to negative
        transform.translateOnAxis(new Vector3(-1, 0, 0), movementSpeed * delta);

        if (autoStabalize) {
            /* no sense if (!autoTurnleft && !autoTurnright) {
                yaw *= 0.998;
            }*/
            if (!autoTurnup && !autoTurndown) {
                pitch *= 0.998;
            }
            if (!autoRollleft && !autoRollright) {
                roll *= 0.998;
            }
        }
        yaw = snap0(yaw);
        roll = snap0(roll);
        pitch = snap0(pitch);
    }

    // toggling between START and STOP
    public void toggleAutoTurnleft() {
        autoTurnleft = !autoTurnleft;
    }

    public void toggleAutoTurnright() {
        autoTurnright = !autoTurnright;
    }

    public void toggleAutoTurnup() {
        autoTurnup = !autoTurnup;
    }

    public void toggleAutoTurndown() {
        autoTurndown = !autoTurndown;
    }

    public void toggleAutoRollleft() {
        autoRollleft = !autoRollleft;
    }

    public void toggleAutoRollright() {
        autoRollright = !autoRollright;
    }

    public void incPitchByDelta(double delta) {
        pitch += rotationSpeed * delta;
        logger.debug("pitch=" + pitch);
    }

    public void incYawByDelta(double delta) {
        yaw += rotationSpeed * delta;
        logger.debug("yaw=" + yaw);
    }

    public void incRollByDelta(double delta) {
        roll += rotationSpeed * delta;
    }

    public double getRotationSpeed() {
        return rotationSpeed;
    }

    public void setRotationSpeed(double rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
    }

    private double snap0(double d) {
        if (Math.abs(d) < 0.00001) {
            return 0.0;
        }
        return d;
    }

    public void checkForPositionUpdate() {
        // don't execute each frame
        if (positionCheckThreshold.reached(1)) {
            positionUpdateTrigger.checkForPositionUpdate(transform);
        }
    }
}
