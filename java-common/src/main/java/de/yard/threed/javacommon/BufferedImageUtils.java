package de.yard.threed.javacommon;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class BufferedImageUtils {

    /**
     * not public because quite useless without width/height.
     */
    static private ByteBuffer toByteBuffer(BufferedImage bi) {
        if (bi == null) {
            return null;
        }
        //logger.debug(String.format("loadFromFile took %d ms", System.currentTimeMillis() - starttime));
        int[] pxl = bi.getRGB(0, 0, bi.getWidth(), bi.getHeight(), null, 0, bi.getWidth());
        ByteBuffer bb = BufferHelper/*OpenGlTexture*/.buildTextureBuffer(bi.getWidth(), bi.getHeight(), pxl, 4);
        return bb;
    }

    static public LoadedImage toLoadedImage(BufferedImage bi) {
        if (bi == null) {
            return null;
        }
        ByteBuffer bb = toByteBuffer(bi);
        return new LoadedImage(bi.getWidth(), bi.getHeight(), bb);
    }

    static public BufferedImage toBufferedImage(LoadedImage li) {
        if (li == null) {
            return null;
        }
        return fromBuffer(li.width ,li.height, li.buffer);
    }

    static public BufferedImage fromBuffer(int width, int height, ByteBuffer buffer) {

        BufferedImage bufferedimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int[] argb = ImageUtils.toARGB(width * height, buffer);
        bufferedimage.setRGB(0, 0, width, height, argb, 0, width);
        return bufferedimage;
    }
}
