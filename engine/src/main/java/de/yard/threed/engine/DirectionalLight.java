package de.yard.threed.engine;

import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.Color;

/**
 *
 * Date: 16.08.15
 */
public class DirectionalLight extends Light {
    /**
     * direction is the position from where the light is coming, related to (0,0,0). So
     * it is the vector from 0,0,0 to the light origin.
     *
     * @param color
     * @param direction
     */
    public DirectionalLight(Color color, Vector3 direction) {
        nativelight = Platform.getInstance().buildDirectionalLight(color,direction.normalize());
    }
}
