package de.yard.threed.engine;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.Color;

/**
 * Date: 30.08.14
 */
public class AmbientLight extends Light {
    public AmbientLight(Color color) {
        nativelight = Platform.getInstance().buildAmbientLight(color);
    }
}
