package de.yard.threed.platform.homebrew;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.resource.NativeResource;
import de.yard.threed.core.ImageData;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Abstraction of GL? Useful for having a headless implementation for testing.
 *
 * Spec is from http://www.opengl.org/sdk/docs/man/html/...
 * <p/>
 * Date: 31.05.14
 */
public interface GlInterface {
    int GL_TRIANGLES = 0;

    public enum ShaderType {
        VERTEX, FRAGMENT
    }

    ;


    void glBindBuffer(int glArrayBuffer, int vboId);

    void glBufferData(int glArrayBuffer, FloatBuffer verticesBuffer, int glStaticDraw);

    void glBufferData(int glElementArrayBuffer, IntBuffer intbuffer, int glStaticDraw);

    /**
     * glVertexAttribPointer, glVertexAttribIPointer and glVertexAttribLPointer specify the location and data format of the array of generic vertex attributes at index index
     * to use when rendering. size specifies the number of components per attribute and must be 1, 2, 3, 4, or GL_BGRA. type specifies the data type of each component, and
     * stride specifies the byte stride from one attribute to the next, allowing vertices and attributes to be packed into a single array or stored in separate arrays.
     * <p/>
     * For glVertexAttribPointer, if normalized isType set to GL_TRUE, it indicates that values stored in an integer format are to be mapped to the
     * range [-1,1] (for signed values) or [0,1] (for unsigned values) when they are accessed and converted to floating point. Otherwise, values will be converted to floats directly without normalization.
     * <p/>
     * For glVertexAttribIPointer, only the integer types GL_BYTE, GL_UNSIGNED_BYTE, GL_SHORT, GL_UNSIGNED_SHORT, GL_INT, GL_UNSIGNED_INT are accepted.
     * Values are always left as integer values.
     * <p/>
     * glVertexAttribLPointer specifies state for a generic vertex attribute array associated with a shader attribute variable
     * declared with 64-bit double precision components. type must be GL_DOUBLE. index, size, and stride behave as described for glVertexAttribPointer and glVertexAttribIPointer.
     * <p/>
     * If pointer isType not NULL, a non-zero named buffer object must be bound to the GL_ARRAY_BUFFER target (see glBindBuffer), otherwise an error isType generated.
     * pointer isType treated as a byte offset into the buffer object's data store. The buffer object binding (GL_ARRAY_BUFFER_BINDING) isType saved as generic vertex
     * attribute array state (GL_VERTEX_ATTRIB_ARRAY_BUFFER_BINDING) for index index.
     * <p/>
     * When a generic vertex attribute array isType specified, size, type, normalized, stride, and pointer are saved as vertex array state,
     * in addition to the current vertex array buffer object binding.
     * <p/>
     * To enable and disable a generic vertex attribute array, call glEnableVertexAttribArray and glDisableVertexAttribArray with index. If enabled,
     * the generic vertex attribute array isType used when glDrawArrays, glMultiDrawArrays, glDrawElements, glMultiDrawElements, or glDrawRangeElements isType called.
     */
    void VertexAttribPointer(int i, int positionElementCount, int glFloat, boolean b, int stride, int positionByteOffset);

    void glEnableVertexAttribArray(int id);

    void glDisableVertexAttribArray(int id);

    int GenBuffers();

    int GenVertexArrays();

    void glBindVertexArray(int vaoId);

    /**
     * glDrawElements specifies multiple geometric primitives with very few subroutine calls. Instead of calling a GL function to pass each vertex attribute,
     * you can use glVertexAttribPointer to prespecify separate arrays of vertex attributes and use them to construct a sequence of primitives with a single call to glDrawElements.
     * When glDrawElements isType called, it uses count sequential elements from an enabled array, starting at indices to construct a sequence of geometric primitives.
     * mode specifies what kind of primitives are constructed and how the array elements construct these primitives. If more than one array isType enabled, each isType used.
     * To enable and disable a generic vertex attribute array, call glEnableVertexAttribArray and glDisableVertexAttribArray.
     */
    void glDrawElements(int mode, int count, int type, int indices);

    void glEnable(int cap);

    void glDisable(int cap);

    void glUseProgram(int id);

    int glCreateProgram();

    int glCreateShader(ShaderType type);

    void glShaderSource(int shader, String source);

    /**
     * Liefert null bei positivem Compilestatus, sonst einen Fehlertext
     */
    String glCompileShader(int shader);

    /**
     * Liefert true bei positivem Linkstatus
     */
    boolean glLinkProgram(int id);

    void glValidateProgram(int id);

    void glAttachShader(int program, int shader);

    void glDetachShader(int program, int shader);

    void glDeleteShader(int shader);

    void glDeleteProgram(int program);

    void glBindAttribLocation(int id, int index, String name);

    //18.3.16 zu alt void glVertexPointerFloat(int i, int i0, int i1);

    int glGetError();

    int glGenTextures();

    void glUploadTexture(int width, int height, ByteBuffer buffer, boolean repeat);

    void glActiveTexture(int unitoffset);

    void glBindTexture(int id);

    int glGetUniformLocation(int shaderProgramid, String name);

    void glUniformMatrix3(int uniloc, FloatBuffer value);

    void glUniformMatrix4(int uniloc, FloatBuffer value);

    void glUniform3f(int uniloc, float f0, float f1, float f2);

    void glUniform4f(int uniloc, float f0, float f1, float f2, float f3);

    void glUniform1i(int uniloc, int value);


    String dump(String separator);

    /**
     * Ab jetzt die Konstanten.
     */
    public int GL_TRIANGLES();

    public int GL_ELEMENT_ARRAY_BUFFER();

    public int GL_LINE_LOOP();

    public int GL_LINES();

    public int GL_STATIC_DRAW();

    public int GL_FLOAT();

    public int GL_ARRAY_BUFFER();

    public int GL_UNSIGNED_INT();

    public int GL_NO_ERROR();

    void enableAlphaBlending();

    void disableAlphaBlending();

    String translateGLErrorString(int errorValue);

    void exitOnGLError(GlInterface gl, String errorMessage);

    boolean hadGLError(GlInterface gl, String errorMessage, Log logger);

    String getGlslVersion();

    /**
     * Sonstige Unterschiede Platformunterschiede in OpenGL, die eigentlich nicht direkt etwas mit OpenGL zu haben. Ist aber ganz praktisch
     * hier unter zubringen.
     * <p>
     * Android hat kein java.awt.* und kein ImageIO. z.B. BufferedImage.
     * Und in desktop können Java Dinge verwendet werden, die in core (unittests) nicht zur Verfuegung stehen.
     */
    //24.5.16 public ImageData drawString(ImageData image, String text, int x, int y, Color textcolor,  String font, int fontsize);
    public ImageData loadImageFromFile(NativeResource filename);

    public int loadPngTextureFromFile(NativeResource filename, boolean repeat);

    //28.4.20 public NativeEventBus getEventBus();
}