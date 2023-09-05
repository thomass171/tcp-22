package de.yard.threed.engine;

/**
 * A 'WASD' FPS controller for a transform in general. It is not grounded which means freely moves in 3D.
 * Uses 'R' for rolling.
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
        if (Input.GetKey(KeyCode.UpArrow)) {
            transformer.incPitch(tpf);
        }
        if (Input.GetKey(KeyCode.DownArrow)) {
            transformer.incPitch(-tpf);
        }
        if (Input.GetKey(KeyCode.LeftArrow)) {
            transformer.incHeading(tpf);
        }
        if (Input.GetKey(KeyCode.RightArrow)) {
            transformer.incHeading(-tpf);
        }
        if (Input.GetKey(KeyCode.W)) {
            if (forCamera) {
                transformer.moveForwardAsCamera(tpf);
            } else {
                transformer.moveForward(tpf);
            }
        }
        if (Input.GetKey(KeyCode.S)) {
            if (forCamera) {
                transformer.moveForwardAsCamera(-tpf);
            } else {
                transformer.moveForward(-tpf);
            }
        }
        if (Input.GetKey(KeyCode.A)) {
            transformer.moveSidew(-tpf);
        }
        if (Input.GetKey(KeyCode.D)) {
            transformer.moveSidew(tpf);
        }
        // Is 'R' a common standard for rolling?
        if (Input.GetKey(KeyCode.R)) {
            //roll
            if (Input.GetKey(KeyCode.Shift)) {
                transformer.incRoll(tpf);
            } else {
                transformer.incRoll(-tpf);
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
                transformer.incHeading(dragtpfheading);
                double dragtpfpitch = -(double) offset.getY() * dragfactor;
                transformer.incPitch(dragtpfpitch);
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