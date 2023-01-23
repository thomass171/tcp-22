package de.yard.threed.platform.homebrew;

import de.yard.threed.core.Matrix3;
import de.yard.threed.core.Matrix4;
import de.yard.threed.javacommon.BufferHelper;

import java.nio.FloatBuffer;

/**
 * Created by thomass on 26.01.16.
 */
public class OpenGlBufferUtils {
    public static FloatBuffer toFloatBuffer(Matrix3 m) {
        FloatBuffer buffer = BufferHelper.createFloatBuffer(9);
        buffer.put((float)m.e11);
        buffer.put((float)m.e21);
        buffer.put((float)m.e31);
        buffer.put((float)m.e12);
        buffer.put((float)m.e22);
        buffer.put((float)m.e32);
        buffer.put((float)m.e13);
        buffer.put((float)m.e23);
        buffer.put((float)m.e33);
        buffer.flip();
        return buffer;
    }

    public static FloatBuffer toFloatBuffer(Matrix4 m) {
        FloatBuffer buffer = BufferHelper.createFloatBuffer(16);
        buffer.put((float)m.a11);
        buffer.put((float)m.a21);
        buffer.put((float)m.a31);
        buffer.put((float)m.a41);
        buffer.put((float)m.a12);
        buffer.put((float)m.a22);
        buffer.put((float)m.a32);
        buffer.put((float)m.a42);
        buffer.put((float)m.a13);
        buffer.put((float)m.a23);
        buffer.put((float)m.a33);
        buffer.put((float)m.a43);
        buffer.put((float)m.a14);
        buffer.put((float)m.a24);
        buffer.put((float)m.a34);
        buffer.put((float)m.a44);
        buffer.flip();
        return buffer;
    }
}
