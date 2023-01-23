package de.yard.threed.platform.homebrew;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Created by thomass on 18.03.16.
 */
public class OpenGlContext {
    private static GlInterface glcontext;


    /**
     * 26.4.20: Deprecated, weil das ueber den Render in die Plaform soll
     * @param gl
     */
    @Deprecated
    public static void init(GlInterface gl) {
        glcontext = gl;
    }

    public static GlInterface getGlContext() {
        return glcontext;
    }


}
