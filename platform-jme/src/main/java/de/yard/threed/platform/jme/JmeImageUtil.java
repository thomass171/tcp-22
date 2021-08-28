package de.yard.threed.platform.jme;

import com.jme3.texture.plugins.AWTLoader;
import de.yard.threed.core.ImageData;
import de.yard.threed.core.platform.Log;
import de.yard.threed.javacommon.ImageUtil;
import de.yard.threed.javacommon.JALog;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Created by thomass on 03.11.15.
 */
public class JmeImageUtil {
    static Log logger = new JALog(/*LogFactory.getLog(*/JmeImageUtil.class);

    public static com.jme3.texture.Image buildJmeImage(ImageData imagedata) {
        AWTLoader loader = new AWTLoader();
        com.jme3.texture.Image image = loader.load(ImageUtil.buildBufferedImage(imagedata), true);
        return image;
    }

    public static com.jme3.texture.Image buildJmeImage(BufferedImage img) {
        AWTLoader loader = new AWTLoader();
        com.jme3.texture.Image image = loader.load(img, true);
        return image;
    }

    public static com.jme3.texture.Image buildJmeImage(java.io.InputStream ins) throws IOException {
        long starttime = System.currentTimeMillis();
        AWTLoader loader = new AWTLoader();
        com.jme3.texture.Image image = null;
        image = loader.load(ins, true);
        logger.debug("AWTLoader took " + (System.currentTimeMillis() - starttime) + " ms");
        return image;
    }
}
