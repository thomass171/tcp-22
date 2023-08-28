package de.yard.threed.javacommon.commondesktop;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.CoreTestFactory;
import de.yard.threed.javacommon.BufferedImageUtils;
import de.yard.threed.javacommon.ConfigurationByEnv;
import de.yard.threed.javacommon.ImageUtil;
import de.yard.threed.javacommon.LoadedImage;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import de.yard.threed.outofbrowser.FileSystemResource;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Created by thomass on 28.08.23.
 */
public class BufferedImageUtilsTest {
    static Platform platform = CoreTestFactory.initPlatformForTest(new SimpleHeadlessPlatformFactory(), null, new ConfigurationByEnv());

    @Test
    public void testLossFreeConversion() {

        BufferedImage bi = ImageUtil.loadImageFromFile(FileSystemResource.buildFromFullString("../data/textures/SokobanTarget.png"));
        assertNotNull(bi);
        LoadedImage li = BufferedImageUtils.toLoadedImage(bi);
        BufferedImage biNew = BufferedImageUtils.toBufferedImage(li);

        // Even though the internal model might change, should still provide the same image.

        for (int x = 0; x < bi.getWidth(); x++) {
            for (int y = 0; y < bi.getHeight(); y++) {
                assertEquals(bi.getRGB(x, y), biNew.getRGB(x, y), "x=" + x + ",y=" + y);
            }
        }
    }

    @Test
    public void testLossFreeConversionSimple() throws IOException {

        BufferedImage bi = new BufferedImage(1, 2, BufferedImage.TYPE_INT_ARGB);
        bi.setRGB(0, 0, Color.red.getRGB());
        bi.setRGB(0, 1, Color.blue.getRGB());
        assertEquals(Color.red.getRGB(), bi.getRGB(0, 0));
        assertEquals(Color.blue.getRGB(), bi.getRGB(0, 1));

        LoadedImage li = BufferedImageUtils.toLoadedImage(bi);
        BufferedImage biNew = BufferedImageUtils.toBufferedImage(li);
        assertEquals(Color.red.getRGB(), biNew.getRGB(0, 0));
        assertEquals(Color.blue.getRGB(), biNew.getRGB(0, 1));
    }
}
