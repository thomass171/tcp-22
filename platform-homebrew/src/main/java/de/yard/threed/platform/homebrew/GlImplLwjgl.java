package de.yard.threed.platform.homebrew;


import de.yard.threed.core.platform.Log;
import de.yard.threed.core.resource.NativeResource;
import de.yard.threed.core.ImageData;

import de.yard.threed.javacommon.BufferedImageUtils;
import de.yard.threed.javacommon.ImageUtil;
import de.yard.threed.javacommon.PNGDecoder;
import de.yard.threed.javacommon.LoadedImage;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.Util;
//OGL import org.lwjgl.util.glu.GLU;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

/**
 * Date: 14.02.14
 * Time: 17:45
 * <p/>
 * Ein GlImpl.
 */
public class GlImplLwjgl implements GlInterface {
    HashMap<Integer, Integer> boundbuffer = new HashMap<Integer, Integer>();
    int boundvertexarray;

    public GlImplLwjgl() {
    }

    @Override
    public void glBindBuffer(int target, int id) {
        GL15.glBindBuffer(target, id);
        boundbuffer.put(target, id);
    }

    @Override
    public void glBufferData(int glArrayBuffer, FloatBuffer verticesBuffer, int glStaticDraw) {
        GL15.glBufferData(glArrayBuffer, verticesBuffer, glStaticDraw);
    }

    @Override
    public void glBufferData(int glArrayBuffer, IntBuffer intBuffer, int glStaticDraw) {
        GL15.glBufferData(glArrayBuffer, intBuffer, glStaticDraw);
    }

    @Override
    public void VertexAttribPointer(int i, int positionElementCount, int glFloat, boolean b, int stride, int positionByteOffset) {
        GL20.glVertexAttribPointer(i, positionElementCount, glFloat, b, stride, positionByteOffset);
    }

    @Override
    public void glEnableVertexAttribArray(int id) {
        GL20.glEnableVertexAttribArray(id);
    }

    @Override
    public void glDisableVertexAttribArray(int id) {
        GL20.glDisableVertexAttribArray(id);
    }

    @Override
    public int GenBuffers() {
        return GL15.glGenBuffers();
    }

    @Override
    public int GenVertexArrays() {
        return GL30.glGenVertexArrays();
    }

    @Override
    public void glBindVertexArray(int vaoId) {
        boundvertexarray = vaoId;
        GL30.glBindVertexArray(vaoId);
    }

    @Override
    public void glDrawElements(int mode, int count, int type, int indices) {
        GL11.glDrawElements(mode, count, type, indices);
    }

    @Override
    public void glEnable(int cap) {
        GL11.glEnable(cap);
    }

    @Override
    public void glDisable(int cap) {
        GL11.glDisable(cap);
    }

    @Override
    public void glUseProgram(int id) {
        GL20.glUseProgram(id);
    }

    @Override
    public int glCreateProgram() {
        return GL20.glCreateProgram();
    }

    @Override
    public int glCreateShader(ShaderType type) {
        switch (type) {
            case VERTEX:
                return GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
            case FRAGMENT:
                return GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        }
        return (-2233);
    }

    @Override
    public void glShaderSource(int shader, String source) {
        GL20.glShaderSource(shader, source);
    }

    @Override
    public String glCompileShader(int shader) {
        GL20.glCompileShader(shader);
        if (GL20.glGetShader(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            String log = GL20.glGetShaderInfoLog(shader, 1000);
            return log;
        }
        // Alternative Pruefung Check for errors
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            String log = GL20.glGetShaderInfoLog(shader, GL20.glGetShaderi(shader, GL20.GL_INFO_LOG_LENGTH));
            return log;
        }
        return null;
    }

    @Override
    public boolean glLinkProgram(int id) {
        GL20.glLinkProgram(id);
        // Check for linking errors
        if (GL20.glGetProgrami(id, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            return false;
        }
        return true;
    }

    @Override
    public void glValidateProgram(int id) {
        GL20.glValidateProgram(id);
         /*2.2.16 scheitert der immer? if(glGetProgrami(shaderProgramid, GL_VALIDATE_STATUS) == GL_FALSE)
        {
            //TODO
            System.err.println("glValidateProgram failed:"+glGetShaderInfoLog(shaderProgramid, 1024));
            System.exit(1);
        }*/

    }

    @Override
    public void glAttachShader(int program, int shader) {
        GL20.glAttachShader(program, shader);
    }

    @Override
    public void glDetachShader(int program, int shader) {
        GL20.glDetachShader(program, shader);
    }

    @Override
    public void glDeleteShader(int shader) {
        GL20.glDeleteShader(shader);
    }

    @Override
    public void glDeleteProgram(int program) {
        GL20.glDeleteProgram(program);
    }

    @Override
    public void glBindAttribLocation(int programid, int index, String name) {
        GL20.glBindAttribLocation(programid, index, name);
    }

    /*@Override
    public void glVertexPointerFloat(int i, int i0, int i1) {
        GL11.glVertexPointer(i, GL11.GL_FLOAT, i0, i1);
    }*/

    @Override
    public int glGetError() {
        return GL11.glGetError();
    }

    @Override
    public int glGenTextures() {
        return GL11.glGenTextures();
    }

    /**
     * 5.9.16: Formatwechsel RGBA->BGRA, was wohl mehr Standard für GPUs ist. erstmal doch nicht.
     * @param width
     * @param height
     * @param buffer
     * @param repeat
     */
    @Override
    public void glUploadTexture(int width, int height, ByteBuffer buffer, boolean repeat) {
        // LWJGL Wiki sample LwjglQuadExampleTextured
        // All RGB bytes are aligned to each other and each component isType 1 byte
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);

        // Upload the texture data and generate mip maps (for scaling)
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, /*GL_BGRA*/GL11.GL_RGB, width, height, 0,
                /*GL_BGRA*/GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
        GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);

        // Setup the ST coordinate system (wrap mode)
        if (repeat) {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        } else {
            //ob clamp als Gegenstueck zu repeat gut ist, ist unklar
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        }

        //Setup texture scaling filtering
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER,
                GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER,
                GL11.GL_LINEAR_MIPMAP_LINEAR);
        //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

    }

    @Override
    public void glActiveTexture(int unitoffset) {
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + unitoffset);
    }

    @Override
    public void glBindTexture(int textureid) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureid);
    }

    @Override
    public int glGetUniformLocation(int shaderProgramid, String name) {
        return GL20.glGetUniformLocation(shaderProgramid, name);
    }

    @Override
    public void glUniformMatrix3(int uniloc, FloatBuffer value) {
        GL20.glUniformMatrix3(uniloc, false, value);
    }

    @Override
    public void glUniformMatrix4(int uniloc, FloatBuffer value) {
        GL20.glUniformMatrix4(uniloc, false, value);
    }

    @Override
    public void glUniform3f(int uniloc, float f0, float f1, float f2) {
        GL20.glUniform3f(uniloc, f0, f1, f2);
    }

    @Override
    public void glUniform4f(int uniloc, float f0, float f1, float f2, float f3) {
        GL20.glUniform4f(uniloc, f0, f1, f2, f3);
    }

    @Override
    public void glUniform1i(int uniloc, int value) {
        GL20.glUniform1i(uniloc, value);
    }

    @Override
    public String dump(String separator) {
        String s = "boundvertexarray=" + boundvertexarray;
        for (int key : boundbuffer.keySet()) {
            int id = boundbuffer.get(key);
            s += ";target=" + decode(key) + ",id=" + id;
        }
        return s;
    }

    private String decode(int key) {
        switch (key) {
            case 34962:
                return "GL_ARRAY_BUFFER";
            case 34963:
                return "GL_ELEMENT_ARRAY_BUFFER";
        }
        return "" + key;
    }

    /**
     * Ab jetzt die Konstanten.
     *
     * @return
     */
    @Override
    public int GL_TRIANGLES() {
        return GL11.GL_TRIANGLES;
    }

    @Override
    public int GL_ELEMENT_ARRAY_BUFFER() {
        return GL15.GL_ELEMENT_ARRAY_BUFFER;
    }

    @Override
    public int GL_LINE_LOOP() {
        return GL11.GL_LINE_LOOP;
    }

    @Override
    public int GL_LINES() {
        return GL11.GL_LINES;
    }

    @Override
    public int GL_STATIC_DRAW() {
        return GL15.GL_STATIC_DRAW;
    }

    @Override
    public int GL_FLOAT() {
        return GL11.GL_FLOAT;
    }

    @Override
    public int GL_ARRAY_BUFFER() {
        return GL15.GL_ARRAY_BUFFER;
    }

    @Override
    public int GL_UNSIGNED_INT() {
        return GL11.GL_UNSIGNED_INT;
    }

    @Override
    public int GL_NO_ERROR() {
        return GL11.GL_NO_ERROR;
    }

    @Override
    public void enableAlphaBlending() {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void disableAlphaBlending() {
        GL11.glDisable(GL11.GL_BLEND);
    }

    @Override
    public String translateGLErrorString(int errorValue) {
        return Util.translateGLErrorString(errorValue);
    }

    /**
     * TODO: das mit dem Exit ist aber noch nicht der wahre Jakob
     *
     * @param errorMessage
     */
    @Override
    public void exitOnGLError(GlInterface gl, String errorMessage) {
        int errorValue = gl.glGetError();

        if (errorValue != gl.GL_NO_ERROR()) {
            //Throws OpenGLException if glGetError() returns anything else than GL_NO_ERROR
            //scheint aber nicht zu gehen.
            //Util.checkGLError();
            String errorString = gl.translateGLErrorString(errorValue);
            System.err.println("ERROR - " + errorMessage + ": " + errorString);
            new NullPointerException().printStackTrace();
            //19.3.16 if (Display.isCreated()) Display.destroy();
            System.exit(1);
        }
    }

    /**
     * Nur Loggen
     */
    @Override
    public boolean hadGLError(GlInterface gl, String errorMessage, Log logger) {
        int errorValue = gl.glGetError();

        if (errorValue != gl.GL_NO_ERROR()) {
            //Throws OpenGLException if glGetError() returns anything else than GL_NO_ERROR
            //scheint aber nicht zu gehen.
            //Util.checkGLError();
            String errorString = gl.translateGLErrorString(errorValue);
            logger.error("ERROR - " + errorMessage + ": " + errorString);
            return true;
        }
        return false;
    }

    @Override
    public String getGlslVersion() {
        return "150";
    }

    /*@Override
    public ImageData drawString(ImageData image, String text, int x, int y, Color textcolor,  String font, int fontsize) {
        return ImageUtil.drawString(image, text, x, y, textcolor,   font, fontsize);
    }*/

    @Override
    public ImageData loadImageFromFile(NativeResource filename) {
        BufferedImage bi = ImageUtil.loadImageFromFile((filename));
        if (bi == null){
            return null;
        }
        //logger.debug(String.format("loadFromFile took %d ms", System.currentTimeMillis() - starttime));
        int[] pxl = bi.getRGB(0, 0, bi.getWidth(), bi.getHeight(), null, 0, bi.getWidth());
        ImageData image = new ImageData(bi.getWidth(), bi.getHeight(), pxl);
        return image;
    }

    @Override
    public int loadPngTextureFromFile(NativeResource filename, boolean repeat) {
        int gltextureid = glGenTextures();
        glActiveTexture(0);
        glBindTexture(gltextureid);
        InputStream in = null;
        try {
            // 25.8.16: Der PNG Decoder kann keine grey... laden. Darum per Cache
            boolean usepngdecoder = false;
            if (usepngdecoder) {
                in = new FileInputStream(filename.getName());
                PNGDecoder decoder = new PNGDecoder(in);

                System.out.println("width=" + decoder.getWidth());
                System.out.println("height=" + decoder.getHeight());

                ByteBuffer buf = ByteBuffer.allocateDirect(4 * decoder.getWidth() * decoder.getHeight());
                decoder.decode(buf, decoder.getWidth() * 4, PNGDecoder.Format.RGBA);
                buf.flip();
                //aus Beispiel:
                // glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, decoder.getWidth(), decoder.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
                glUploadTexture(decoder.getWidth(), decoder.getHeight(), buf, repeat);
                buf.clear();
            } else {
                LoadedImage li = BufferedImageUtils.toLoadedImage(ImageUtil.loadCachableImage(filename));
                glUploadTexture(li.width, li.height, li.buffer, repeat);
                li.buffer.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
            //TODO
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return gltextureid;
    }

    /*28.4.20 @Override
    public NativeEventBus getEventBus() {
        return JAEventBus.getInstance();
    }*/

}
