package de.yard.threed.engine.imaging;

import de.yard.threed.core.ImageData;


/**
 * Created by thomass on 18.05.16.
 */
@FunctionalInterface
public interface PixelHandler {
    void handlePixel(ImageData image, int x, int y);
}
