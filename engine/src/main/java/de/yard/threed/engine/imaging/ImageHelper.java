package de.yard.threed.engine.imaging;

import de.yard.threed.core.ImageData;


/**
 * Created by thomass on 20.03.17. 
 */
public class ImageHelper {
    public static void loop(/*BufferedImage*/ImageData image, int width, int height, PixelHandler handler) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // handlePixel muss im C' Delegate manuell entfernt werden.
                handler.handlePixel(image, x, y);
            }
        }
    }
}
