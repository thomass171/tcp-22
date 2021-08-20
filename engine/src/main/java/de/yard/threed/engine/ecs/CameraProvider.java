package de.yard.threed.engine.ecs;

import de.yard.threed.engine.Camera;

/**
 * PickAnimation z.B. braucht die Camera
 */
@FunctionalInterface
public interface CameraProvider {
    Camera getCamera();
}