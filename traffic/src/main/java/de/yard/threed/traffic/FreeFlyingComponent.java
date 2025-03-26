package de.yard.threed.traffic;


import de.yard.threed.core.MathUtil2;
import de.yard.threed.core.Matrix3;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.FirstPersonTransformer;
import de.yard.threed.engine.Transform;
import de.yard.threed.engine.ecs.EcsComponent;
import de.yard.threed.engine.ecs.EcsEntity;

/**
 * For flying a plane that is not graph bound.
 * For easier handling vehicle moves in 'vehicle move space' and is transformed by a parent sphere node to sphere space.
 * #### vehicle move space
 * The standard OpenGL space with forward to +x, ..., where vehicle
 * movement should happen (however currently not used for graph movement). If a vehicle model
 * doesn't comply that space, it should be transformed 'below' the vehicle ECS node.
 * Transform to sphere space should happen above.
 *
 * 25.3.25 Not sure that is really easier. Try merged and with rotateOnAxis. Control is not so bad now.
 * <p>
 * auto-roll is really hard. See https://gamedev.stackexchange.com/questions/123535/unwanted-roll-when-rotating-camera-with-pitch-and-yaw
 * Created by thomass on 24.08.23.
 */
public class FreeFlyingComponent extends EcsComponent {
    static Log logger = Platform.getInstance().getLog(FreeFlyingComponent.class);
    public static String TAG = "FreeFlyingComponent";
    public boolean freeflyingcomponentdebuglog = true;
    // For now have nothing like FirstPersonTransformer but everything here to keep it simple
    public boolean initialLocated = false;
    private boolean autoTurnleft, autoTurnright, autoTurnup, autoTurndown, autoRollleft, autoRollright, autoSpeedUp, autoSpeedDown;
    // speed must fit to the scene. The default might be too high/low. No movement without a user request for speed.
    private double movementSpeed = 0.0; //move 2 units per getSecond
    private double rotationSpeed = 0.1f; //move 10 units per getSecond
    // In rad for more efficient calcs. default straight to -z
    double yaw = 0, pitch = 0, roll = 0;
    private Transform transform;
    //private Quaternion baseRotation;
    // optionally try to roll without user controls. See below, it is really hard. So keep user controls for now
    //private boolean autoRoll = true;

    // optional movement by more simple FirstPersonTransformer
    FirstPersonTransformer firstPersonTransformer;
    boolean autoStabalize = true;

    /**
     *
     */
    public FreeFlyingComponent(Transform transform/*,SceneNode sphereTransform*/, Quaternion baseRotation) {
        //firstPersonTransformer = new FirstPersonTransformer(transform, FirstPersonTransformer.ROTATE_MODE_PERAXIS);
        this.transform = transform;
        //this.baseRotation = baseRotation;
        if (firstPersonTransformer != null) {
        }
        transform.setRotation(baseRotation);
    }

    @Override
    public String getTag() {
        return TAG;
    }

    public static FreeFlyingComponent getFreeFlyingComponent(EcsEntity e) {
        return (FreeFlyingComponent) e.getComponent(FreeFlyingComponent.TAG);
    }

    public void updateByDelta(double delta) {
        if (autoTurnleft) {
            incYawByDelta(delta);
        }
        if (autoTurnright) {
            incYawByDelta(-delta);
        }
        if (autoTurnup) {
            incPitchByDelta(-delta);
        }
        if (autoTurndown) {
            incPitchByDelta(delta);
        }
        if (autoRollleft) {
            incRollByDelta(delta);
        }
        if (autoRollright) {
            incRollByDelta(-delta);
        }
        if (autoSpeedUp) {
            incSpeedByDelta(delta);
        }
        if (autoSpeedDown) {
            incSpeedByDelta(-delta);
        }

        if (firstPersonTransformer == null) {

            double sina = MathUtil2.sin(pitch);
            double sinb = MathUtil2.sin(yaw);
            double sing = MathUtil2.sin(roll);
            double cosa = MathUtil2.cos(pitch);
            double cosb = MathUtil2.cos(yaw);
            double cosg = MathUtil2.cos(roll);

            int rotateMode = 1;
            switch (rotateMode) {
                case 0: //matrix
                    // From https://msl.cs.uiuc.edu/planning/node102.html and others.
                    // But this matrix apparently doesn't solve the 'unwanted-roll' problem. Maybe our math is not corect.
                    Matrix3 rotation = new Matrix3(
                            cosa * cosb, cosa * sinb * sing - sina * cosg, cosa * sinb * cosg + sina * sing,
                            sina * cosb, sina * sinb * sing + cosa * cosg, sina * sinb * cosg - cosa * sing,
                            -sinb, cosb * sing, cosb * cosg
                    );
                    transform.setRotation(rotation.extractQuaternion());
                    break;
                case 1:
                    transform.rotateOnAxis(new Vector3(0, 1, 0), yaw / 100.0);
                    transform.rotateOnAxis(new Vector3(0, 0, 1), pitch / 200.0);
                    transform.rotateOnAxis(new Vector3(1, 0, 0), roll / 100.0);
                    break;
            }
            // vehicle looks to -x, so movement also is to negative
            transform.translateOnAxis(new Vector3(-1, 0, 0), movementSpeed * delta);
        } else {
            firstPersonTransformer.moveForward(movementSpeed * delta);
        }

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

    public void toggleAutoSpeedUp() {
        autoSpeedUp = !autoSpeedUp;
    }

    public void toggleAutoSpeedDown() {
        autoSpeedDown = !autoSpeedDown;
    }

    public void incPitchByDelta(double delta) {
        pitch += rotationSpeed * delta;
        logger.debug("pitch=" + pitch);
        if (firstPersonTransformer != null) {
            firstPersonTransformer.incPitchByDelta(delta);
        }
    }

    public void incYawByDelta(double delta) {
        yaw += rotationSpeed * delta;
        logger.debug("yaw=" + yaw);
        if (firstPersonTransformer != null) {
            firstPersonTransformer.incHeadingByDelta(delta);
        }
    }

    public void incRollByDelta(double delta) {
        roll += rotationSpeed * delta;
        if (firstPersonTransformer != null) {
            firstPersonTransformer.incRollByDelta(delta);
        }
    }

    public void incSpeedByDelta(double delta) {
        // probaly need a accelaration factor. 5 only for testing
        movementSpeed += 5.0 * delta;
        logger.debug("new movementSpeed:" + movementSpeed);
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

    private double snap0(double d) {
        if (Math.abs(d) < 0.00001) {
            return 0.0;
        }
        return d;
    }

}
