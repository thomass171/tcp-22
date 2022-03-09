package de.yard.threed.platform.commondesktop;

import de.yard.threed.core.Color;
import de.yard.threed.core.ImageData;
import de.yard.threed.core.ImageFactory;
import de.yard.threed.core.platform.Platform;

import de.yard.threed.core.testutil.TestFactory;
import de.yard.threed.core.testutil.TestUtil;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import org.junit.jupiter.api.Test;

/**
 * Created by thomass on 24.05.16.
 */
public class ImageDataTest {
    static Platform platform = TestFactory.initPlatformForTest( new SimpleHeadlessPlatformFactory(),null);


    @Test
    public void testBasic() {

        ImageData blueimage = ImageFactory.buildSingleColor(64, 32, Color.BLUE);
        int bluecnt = countColor(blueimage, Color.BLUE);
        TestUtil.assertEquals("", 64 * 32, bluecnt);
        ImageData greenimage = ImageFactory.buildSingleColor(20, 10, Color.GREEN);
        int greencnt = countColor(greenimage, Color.GREEN);
        TestUtil.assertEquals("", 20 * 10, greencnt);

        blueimage.overlayImage(greenimage, 5, 15);
        bluecnt = countColor(blueimage, Color.BLUE);
        TestUtil.assertEquals("", 64 * 32 - 20 * 10, bluecnt);
        greencnt = countColor(blueimage, Color.GREEN);
        TestUtil.assertEquals("", 20 * 10, greencnt);

        ImageData subimage = blueimage.getSubimage(5, 15, 20, 10);
        greencnt = countColor(subimage, Color.GREEN);
        TestUtil.assertEquals("", 20 * 10, greencnt);
    }

    private int countColor(ImageData image, Color color) {
        int cnt = 0;
        for (int y = 0; y < image.height; y++) {
            for (int x = 0; x < image.width; x++) {
                if (image.pixel[y * image.width + x] == color.getARGB()) {
                    cnt++;
                }
            }

        }
        return cnt;

    }
}
