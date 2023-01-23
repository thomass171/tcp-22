package de.yard.threed.platform.homebrew;


import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.NativeResource;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.ImageData;

import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;


/**
 * Zur Nutzung in Unittests mit OpenGL.
 */
public class GlImplDummyForTests implements GlInterface {
    static Log logger = Platform.getInstance().getLog(GlImplDummyForTests.class);

    public GlImplDummyForTests() {
    }

    @Override
    public void glBindBuffer(int target, int id) {
    }

    @Override
    public void glBufferData(int glArrayBuffer, FloatBuffer verticesBuffer, int glStaticDraw) {
    }

    @Override
    public void glBufferData(int glArrayBuffer, IntBuffer intBuffer, int glStaticDraw) {
    }

    @Override
    public void VertexAttribPointer(int i, int positionElementCount, int glFloat, boolean b, int stride, int positionByteOffset) {
    }

    @Override
    public void glEnableVertexAttribArray(int id) {
    }

    @Override
    public void glDisableVertexAttribArray(int id) {
    }

    @Override
    public int GenBuffers() {
        return 2;
    }

    @Override
    public int GenVertexArrays() {
        return 22;
    }

    @Override
    public void glBindVertexArray(int vaoId) {
    }

    @Override
    public void glDrawElements(int mode, int count, int type, int indices) {
    }

    @Override
    public void glEnable(int cap) {
    }

    @Override
    public void glDisable(int cap) {
    }

    @Override
    public void glUseProgram(int id) {
    }

    @Override
    public int glCreateProgram() {
        return 23;
    }

    @Override
    public int glCreateShader(ShaderType type) {
        switch (type) {
            case VERTEX:
                return 5;
            case FRAGMENT:
                return 6;
        }
        return (-2233);
    }

    @Override
    public void glShaderSource(int shader, String source) {
    }

    @Override
    public String glCompileShader(int shader) {
        return null;
    }

    @Override
    public boolean glLinkProgram(int id) {
        return true;
    }

    @Override
    public void glValidateProgram(int id) {


    }

    @Override
    public void glAttachShader(int program, int shader) {
    }

    @Override
    public void glDetachShader(int program, int shader) {
    }

    @Override
    public void glDeleteShader(int shader) {
    }

    @Override
    public void glDeleteProgram(int program) {
    }

    @Override
    public void glBindAttribLocation(int programid, int index, String name) {
    }

    /*@Override
    public void glVertexPointerFloat(int i, int i0, int i1) {
        GL11.glVertexPointer(i, GL11.GL_FLOAT, i0, i1);
    }*/

    @Override
    public int glGetError() {
        return 0;
    }

    @Override
    public int glGenTextures() {
        return 7;
    }

    @Override
    public void glUploadTexture(int width, int height, ByteBuffer buffer, boolean repeat) {

    }

    @Override
    public void glActiveTexture(int unitoffset) {
    }

    @Override
    public void glBindTexture(int textureid) {
    }

    @Override
    public int glGetUniformLocation(int shaderProgramid, String name) {
        return 3;
    }

    @Override
    public void glUniformMatrix3(int uniloc, FloatBuffer value) {
    }

    @Override
    public void glUniformMatrix4(int uniloc, FloatBuffer value) {
    }

    @Override
    public void glUniform3f(int uniloc, float f0, float f1, float f2) {
    }

    @Override
    public void glUniform4f(int uniloc, float f0, float f1, float f2, float f3) {
    }

    @Override
    public void glUniform1i(int uniloc, int value) {
    }

    @Override
    public String dump(String separator) {
        String s = "boundvertexarray=";

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
        return 5;//GL11.GL_TRIANGLES;
    }

    @Override
    public int GL_ELEMENT_ARRAY_BUFFER() {
        return 6;//GL15.GL_ELEMENT_ARRAY_BUFFER;
    }

    @Override
    public int GL_LINE_LOOP() {
        return 7;//GL_LINE_LOOP;
    }

    @Override
    public int GL_LINES() {
        return 8;//GL_LINES;
    }

    @Override
    public int GL_STATIC_DRAW() {
        return 9;//GL15.GL_STATIC_DRAW;
    }

    @Override
    public int GL_FLOAT() {
        return 10;//GL_FLOAT;
    }

    @Override
    public int GL_ARRAY_BUFFER() {
        return 11;//GL15.GL_ARRAY_BUFFER;
    }

    @Override
    public int GL_UNSIGNED_INT() {
        return 12;//GL_UNSIGNED_INT;
    }

    @Override
    public int GL_NO_ERROR() {
        return 0;//GL_NO_ERROR;
    }

    @Override
    public void enableAlphaBlending() {
    }

    @Override
    public void disableAlphaBlending() {
    }

    @Override
    public String translateGLErrorString(int errorValue) {
        return "" + errorValue;
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
    public ImageData drawString( String text, Color textcolor,  String font, int fontsize) {
        return buildDummyImageData(6, 4);
    }*/

    private ImageData buildDummyImageData(int width, int height) {
        int[] pixel = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixel[y * width + x] = 188;
            }
        }
        ImageData id = new ImageData(width, height, pixel);
        return id;
    }

    /**
     * TODO:  02.10.19: Das ist aber eine doofe Kaschierung von z.B. Bundle Resolve Problemen.
     *
     * @param filename
     * @return
     */
    @Override
    public ImageData loadImageFromFile(NativeResource filename) {
        return buildDummyImageData(6, 4);
        /*BufferedImage bi = ImageUtil.loadImageFromFile(new File(filename));
        //logger.debug(String.format("loadFromFile took %d ms", System.currentTimeMillis() - starttime));
        int[] pxl = bi.getRGB(0, 0, bi.getWidth(), bi.getHeight(), null, 0, bi.getWidth());
        ImageData image = new ImageData(bi.getWidth(), bi.getHeight(), pxl);
        return image;*/
    }

    /**
     * Liefert -1 bei Fehler.
     *
     * @param filename
     * @param repeat
     * @return
     */
    @Override
    public int loadPngTextureFromFile(NativeResource filename, boolean repeat) {
        //28.12.16 zumindest den Dateizugriff durchfuehren. return glGenTextures();
        InputStream in = null;
        /*16.10.18 wird ja eh nicht wirklich gemacht.
        02.10.19: Das ist aber eine doofe Kaschierung von z.B. Bundle Resolve Problemen. Nutzung von nativem IO duerfte hier im Test kein Problem sein*/
        if (!new File(filename.getFullName()).exists()) {
            logger.error("file for resource does not exist:" + filename.getFullName());
        }
        /*try {
            de.yard.threed.platform.common.InputStream isType = FileReader.getFileStream(filename);
            
        } catch (IOException e) {
            //10.6.17: Es koennte zwar Tests geben, bei denen Images fehlen, das soll aber eigentlich nicht so sein.
            //Ein Test, der Wert auf ein nicht gefundenes Image legt, soll das selber pruefen. In Produktion gehts ja auch
            //mit einem Defaultimage weiter.
            //throw new RuntimeException(e);
            logger.error("coulnd't read "+filename.getFullName());
            return -1;
        }
        */
        return glGenTextures();
    }

    /*28.4.20 @Override
    public NativeEventBus getEventBus() {
        return SimpleEventBus.getInstance();
    }*/

}

