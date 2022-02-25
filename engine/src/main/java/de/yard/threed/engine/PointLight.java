package de.yard.threed.engine;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.Color;

/**
 * A point light.
 * Has no own transform but will be attached to a node.
 *
 * Date: 09.07.14
 */
public class PointLight extends Light {

    public PointLight(Color color) {
        nativelight = Platform.getInstance().buildPointLight(color,1000000000.0);
    }

    public PointLight(Color color, double range) {
        nativelight = Platform.getInstance().buildPointLight(color, range);
    }
}
