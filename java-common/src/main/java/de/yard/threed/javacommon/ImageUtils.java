package de.yard.threed.javacommon;

import de.yard.threed.core.Util;
import de.yard.threed.core.platform.Log;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Diuplicate to desktop ImageUtil.
 * Created on 10.12.18.
 */
public class ImageUtils {
    /**
     * BufferedImage and ImageIO are the only ways in pure Java to load an image.
     * Returns null when the image couldn't be loaded, eg. *.rgb images (error already been logged).
     */
    public static BufferedImage loadImageFromFile(Log logger, java.io.InputStream is, String nameForLogging) {
        try {
            long starttime = System.currentTimeMillis();
            BufferedImage img = ImageIO.read(is);
            if (img != null) {
                logger.debug(String.format("Image loaded with size %dx%d from %s. Took %d ms", img.getHeight(), img.getWidth(), nameForLogging, System.currentTimeMillis() - starttime));
            } else {
                logger.warn("loadImageFromFile: ImageIO.read returned null for " + nameForLogging);
            }
            return img;
            // TODO close auf stream
        } catch (IOException e) {
            logger.error("loadImageFromFile: ImageIO.read failed for " + nameForLogging);
            return null;
        }
    }

    public static BufferedImage loadImageFromFile(Log logger, String filename) {
        try {
            return loadImageFromFile(logger, new FileInputStream(new File(filename)), filename);
            // TODO close auf stream
        } catch (IOException e) {
            logger.error("loadImageFromFile: ImageIO.read failed for " + filename);
            return null;
        }
    }

    /**
     * Buffer must be RGBA.
     *
     * @param size
     * @param buffer
     * @return
     */
    public static int[] toARGB(int size, ByteBuffer buffer) {
        int[] pixel = new int[size];
        for (int i = 0; i < size; i++) {
            int p = (de.yard.threed.core.Util.byte2int(buffer.get()) << 16) + (de.yard.threed.core.Util.byte2int(buffer.get()) << 8) + (de.yard.threed.core.Util.byte2int(buffer.get()) << 0) + (Util.byte2int(buffer.get()) << 24);

            pixel[i] = p;
        }
        return pixel;
    }

    /**
     * Extracted from JmeTexture to here.
     * 23.7.21:for future use to merge code in JmeTexture and OpenglTexture.
     *
     * @return
     */
    public int loadAndCacheImage() {
        return 0;
    }
}
