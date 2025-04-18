package de.yard.threed.engine;

/**
 * A 'WASD' FPS controller for a transform in general. It is not grounded which means freely moves in 3D.
 * Uses 'R' for rolling.
 * Moves in 'OpenGL' space, ie. z-axis points forward (orientation toggable), x-axis points right (pitch), y-axis points up (yaw/heading)
 *
 * <p>
 * Similar to THREE.FirstPersonControls, but without lookat.
 * <p>
 * 28.11.16: Available also outside ECS. In ECS it is handles by {@link FirstPersonMovingSystem}
 * <p>
 * 16.4.19: Occupies many keys, but well. Maybe some could be optional.
 */

import de.yard.threed.core.*;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.ecs.FirstPersonMovingSystem;

public class FirstPersonController {
    static Log logger = Platform.getInstance().getLog(FirstPersonController.class);
    float mouseSensitivity = 0.05f;
    private Point startdrag;
    private Point possibleMoveByMouse = null;
    public boolean moveByMouseEnabled = true;
    public FirstPersonTransformer transformer;
    private boolean forCamera;

    public FirstPersonController(Transform target, boolean forCamera) {
        // logger.debug("Building FirstPersonController ");
        this.transformer = new FirstPersonTransformer(target);
        this.forCamera = forCamera;
    }

    /**
     * Outside ECS update(). In ECS it is handled by {@link FirstPersonMovingSystem}
     *
     * @param tpf
     */
    public void update(double tpf) {
        //logger.debug("update: tpf="+tpf);
        if (Input.getKey(KeyCode.UpArrow)) {
            transformer.incPitchByDelta(tpf);
        }
        if (Input.getKey(KeyCode.DownArrow)) {
            transformer.incPitchByDelta(-tpf);
        }
        if (Input.getKey(KeyCode.LeftArrow)) {
            transformer.incHeadingByDelta(tpf);
        }
        if (Input.getKey(KeyCode.RightArrow)) {
            transformer.incHeadingByDelta(-tpf);
        }
        if (Input.getKey(KeyCode.W)) {
            if (forCamera) {
                transformer.moveForwardAsCamera(tpf);
            } else {
                transformer.moveForward(tpf);
            }
        }
        if (Input.getKey(KeyCode.S)) {
            if (forCamera) {
                transformer.moveForwardAsCamera(-tpf);
            } else {
                transformer.moveForward(-tpf);
            }
        }
        if (Input.getKey(KeyCode.A)) {
            transformer.moveSidew(-tpf);
        }
        if (Input.getKey(KeyCode.D)) {
            transformer.moveSidew(tpf);
        }
        // Is 'R' a common standard for rolling?
        if (Input.getKey(KeyCode.R)) {
            //roll
            if (Input.getKey(KeyCode.Shift)) {
                transformer.incRollByDelta(tpf);
            } else {
                transformer.incRollByDelta(-tpf);
            }
        }
        if (moveByMouseEnabled) {
            Point point = Input.getMouseDown();
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
                transformer.incHeadingByDelta(dragtpfheading);
                double dragtpfpitch = -(double) offset.getY() * dragfactor;
                transformer.incPitchByDelta(dragtpfpitch);
                startdrag = point;
                possibleMoveByMouse = null;
            }
            point = Input.getMouseUp();
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
                    //TODO as camera?
                    case 2:
                        transformer.moveForward(-tpf);
                        break;
                    case 22:
                        transformer.moveForward(tpf);
                        break;
                }
            }
        }
    }
}