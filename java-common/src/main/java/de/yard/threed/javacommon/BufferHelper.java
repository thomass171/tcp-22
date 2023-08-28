package de.yard.threed.javacommon;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class BufferHelper {

    static int BYTES_PER_FLOAT = 4;
    static int BYTES_PER_INT = 4;

    public static ByteBuffer createByteBuffer(int size) {
        return ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
    }

    public static FloatBuffer createFloatBuffer(int size) {
        return ByteBuffer.allocateDirect(size * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
    }

    public static IntBuffer createIntBuffer(int size) {
        return ByteBuffer.allocateDirect(size * BYTES_PER_INT).order(ByteOrder.nativeOrder()).asIntBuffer();
    }

    /**
     * OpenGL hat (0,0) links unten, ImageData aber links oben. So rows are flipped here.
     * Liefert RGBA Format (wenn die Pixel in ARGB sind;wie in BufferedImage).
     *
     * 4.7.21: Was OpenGlTexture.buildBuffer() before.
     * 28.8.23: No longer flip rows. This spoils forward/backward conversion (via BufferedImageUtils) and isn't consistent.
     * If split is needed, it must be done somewhere else, not here.
     *
     */
    public static ByteBuffer buildTextureBuffer(int width, int height, int[] pixels, int BYTES_PER_PIXEL) {
       /* this.width = width;
        this.height = height;
        this.pixels = pixels;  */


        ByteBuffer buffer = BufferHelper/*OpenGlContext*/.createByteBuffer(width * height * BYTES_PER_PIXEL); //4 for RGBA, 3 for RGB
        // logger.debug(String.format("Erstes Pixel=0x%x", pixels[0]));
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pix = pixels[y * width + x];
                buffer.put((byte) ((pix >> 16) & 0xFF));     // Red component
                buffer.put((byte) ((pix >> 8) & 0xFF));      // Green component
                buffer.put((byte) (pix & 0xFF));               // Blue component
                buffer.put((byte) ((pix >> 24) & 0xFF));    // Alpha component. Only for RGBA
            }
        }

        buffer.flip(); //FOR THE LOVE OF GOD DO NOT FORGET THIS
        return buffer;
    }

}
