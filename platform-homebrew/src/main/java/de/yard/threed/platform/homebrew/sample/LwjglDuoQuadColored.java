package de.yard.threed.platform.homebrew.sample;

import de.yard.threed.platform.homebrew.OpenGlMatrix4;
import de.yard.threed.platform.homebrew.PlatformHomeBrew;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.PixelFormat;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * A very simple native 3D OpenGL app without using a scene, platform and engine.
 * Top view on two quadrants. Bottom left a small red one and top right a large green one.
 * Little size animated to see something is going on.
 * <p/>
 * Prototype for VAO "Vertex Array Object" mechanismus.
 * <p/>
 * <p/>
 * 02.03.16: Also works with wireframe.
 * 20.02.23: Still working.
 * <p/>
 * Date: 29.01.16
 */
public class LwjglDuoQuadColored {
    public static boolean usevertexarray = true;
    public static boolean wireframe = false;
    // 29.8.16: Wahrscheinlich ist "ohne Shader" völliger Quatsch.
    public static boolean useshader = true;

    public static void main(String[] args) {
        PlatformHomeBrew.getInstance();
        new LwjglDuoQuadColored();
    }

    float[] colorsred = {
            1f, 0f, 0f, 1f,
            0.5f, 0, 0f, 1f,
            0.7f, 0f, 0f, 1f,
            1f, 1f, 1f, 1f,
    };
    float[] colorsgreen = {
            0f, 0.2f, 0f, 1f,
            0f, 1f, 0f, 1f,
            0f, 0.6f, 0f, 1f,
            1f, 1f, 1f, 1f,
    };

    // Setup variables
    private final String WINDOW_TITLE = "The Duo Quad: colored";
    private final int WIDTH = 800;
    private final int HEIGHT = 600;
    private Quad linksunten, rechtsoben;
    // Shader variables
    private VertexShader vsId = null;
    private FragmentShader fsId = null;
    private ShaderProgram pId = null;
    private int cycle = 0;

    public LwjglDuoQuadColored() {
        // Initialize OpenGL (Display)
        this.setupOpenGL();

        //camera = new Camera(45, (float) Display.getWidth() / (float) Display.getHeight(), 0.1f, 100f);
        //camera.setPosition(new Vector3f(0, 0, -3));

        linksunten = new Quad(0.5f, -0.5f, colorsred);
        rechtsoben = new Quad(1f, +0.5f, colorsgreen);
        if (useshader) {
            this.setupShaders();
        }

        while (!Display.isCloseRequested()) {
            this.loopCycle();
            // Force a maximum FPS of about 60
            Display.sync(60);
            // Let the CPU synchronize with the GPU if GPU isType tagging behind
            Display.update();
        }
        // Destroy OpenGL (Display)
        this.destroyOpenGL();
    }

    public void setupOpenGL() {
        // Setup an OpenGL context with API version 3.2
        try {
            PixelFormat pixelFormat = new PixelFormat();
            ContextAttribs contextAtrributes = new ContextAttribs(3, 2)
                    .withForwardCompatible(true)
                    .withProfileCore(true);

            Display.setDisplayMode(new DisplayMode(WIDTH, HEIGHT));
            Display.setTitle(WINDOW_TITLE);
            Display.create(pixelFormat, contextAtrributes);

            GL11.glViewport(0, 0, WIDTH, HEIGHT);
        } catch (LWJGLException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        // Setup an XNA like background color
        GL11.glClearColor(0.4f, 0.6f, 0.9f, 0f);

        // Map the internal OpenGL coordinate system to the entire screen
        GL11.glViewport(0, 0, WIDTH, HEIGHT);
    }


    private void setupShaders() {
        int errorCheckValue = GL11.glGetError();

        // Load the vertex shader
        vsId = new VertexShader("#version 150 core\n" +
                "uniform mat4 projection;\n" +
                "uniform mat4 viewer;\n" +
                "uniform mat4 model;\n" +
                "\n" +
                "in vec4 in_Position;\n" +
                "in vec4 in_Color;\n" +
                "in vec4 in_ColorXXX;\n" +
                "\n" +
                "out vec4 pass_Color;\n" +
                "\n" +
                "void main(void) {\n" +
                "\tgl_Position = projection * viewer * model * in_Position;\n" +
                "\tpass_Color = in_ColorXXX;\n" +
                "\tpass_Color = in_Color;\n" +
                "}", "buildin");
        // Load the fragment shader
        fsId = new FragmentShader("#version 150 core\n" +
                "\n" +
                "in vec4 pass_Color;\n" +
                "\n" +
                "out vec4 out_Color;\n" +
                "\n" +
                "void main(void) {\n" +
                "\tout_Color = pass_Color;\n" +
                "}", "buildin");

        // Create a new shader program that links both shaders
        List<String> varstobind = new ArrayList<String>();
        varstobind.add("in_Position");
        varstobind.add("in_Color");
        pId = new ShaderProgram(vsId, fsId, varstobind);
        //pId.setup();
        /*// Position information will be attribute 0
        GL20.glBindAttribLocation(pId.getId(), 0, "in_Position");
        // Color information will be attribute 1
        GL20.glBindAttribLocation(pId.getId(), 1, "in_Color");
        pId.link();*/
    }

    public void loopCycle() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

        if (useshader) {
            pId.use();
            pId.setUniformMatrix4("projection", new OpenGlMatrix4().toFloatBuffer());//camera.getProjectionMatrix()));
            pId.setUniformMatrix4("viewer", new OpenGlMatrix4().toFloatBuffer());//camera.getViewMatrix()));

            pId.setUniformMatrix4("model", linksunten.getModelMatrix(cycle).toFloatBuffer());
        }        
        linksunten.draw();
        
        if (useshader) {
            pId.setUniformMatrix4("model", rechtsoben.getModelMatrix(cycle).toFloatBuffer());
        }
        rechtsoben.draw();
        
        if (useshader) {
            GL20.glUseProgram(0);
        }
        cycle++;
    }

    void destroyOpenGL() {
        linksunten.destroyOpenGL();
        Display.destroy();
    }
}

/**
 * Ein einzelnes Quadrat mit 2 Triangles.
 * <p/>
 * Eine Einheit fuer ein Vertex Array
 */
class Quad {
    // Quad variables
    private int vaoId = 0;
    private int vboId = 0;
    private int vbocId = 0;
    private int vboiId = 0;
    private int indicesCount = 0;
    private float offset;

    public Quad(float size, float offset, float[] colors) {
        this.offset = offset;

        // Vertices, the order isType not important. XYZW instead of XYZ
        float[] vertices = {
                -size, size, 0f, 1f,
                -size, -size, 0f, 1f,
                size, -size, 0f, 1f,
                size, size, 0f, 1f
        };
        FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(vertices.length);
        verticesBuffer.put(vertices);
        verticesBuffer.flip();

        FloatBuffer colorsBuffer = BufferUtils.createFloatBuffer(colors.length);
        colorsBuffer.put(colors);
        colorsBuffer.flip();

        // OpenGL expects to draw vertices in counter clockwise order by default
        byte[] indices = {
                0, 1, 2,
                2, 3, 0
        };

        indicesCount = indices.length;
        ByteBuffer indicesBuffer = BufferUtils.createByteBuffer(indicesCount);
        indicesBuffer.put(indices);
        indicesBuffer.flip();

        // Create a new Vertex Array Object in memory and select it (bind)
        vaoId = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoId);

        // Create a new Vertex Buffer Object in memory and select it (bind) - VERTICES
        vboId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, verticesBuffer, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(0, 4, GL11.GL_FLOAT, false, 0, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        // Create a new VBO for the indices and select it (bind) - COLORS
        vbocId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbocId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, colorsBuffer, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 0, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        // Deselect (bind to 0) the VAO
        GL30.glBindVertexArray(0);

        // Create a new VBO for the indices and select it (bind) - INDICES
        vboiId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboiId);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public void draw() {
        // Bind to the VAO that has all the information about the vertices. Die ArrayBuffer und Attribpointer
        // haengen da mit drin. Der Indexbuffer aber nicht! Der muss separat wieder gebinded werden.
        GL30.glBindVertexArray(vaoId);
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        // Bind to the index VBO that has all the information about the order of the vertices
        // Der liegt auuser des VertexArray
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboiId);

        // Draw the vertices
        if (LwjglDuoQuadColored.wireframe) {
            GL11.glDrawElements(GL11.GL_LINE_LOOP, 3, GL11.GL_UNSIGNED_BYTE, 0);
            GL11.glDrawElements(GL11.GL_LINE_LOOP, 3, GL11.GL_UNSIGNED_BYTE, 3);

        } else {
            GL11.glDrawElements(GL11.GL_TRIANGLES, indicesCount, GL11.GL_UNSIGNED_BYTE, 0);
        }
        //Util.exitOnGLError(gl, "loopCycle glDrawElements");

        // Put everything back to default (deselect)
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL30.glBindVertexArray(0);

    }

    OpenGlMatrix4 getModelMatrix(int cycle) {
        float scale = (float) (0.5f * Math.sin((float) (cycle % 100) / 100));
        OpenGlMatrix4 modelmatrix = new OpenGlMatrix4(
                scale, 0, 0, offset,
                0, scale, 0, offset,
                0, 0, scale, 0,
                0, 0, 0, 1);
        return modelmatrix;
    }

    public void destroyOpenGL() {
        // Delete the shaders
        /*GL20.glUseProgram(0);
        GL20.glDetachShader(pId, vsId);
        GL20.glDetachShader(pId, fsId);

        GL20.glDeleteShader(vsId);
        GL20.glDeleteShader(fsId);
        GL20.glDeleteProgram(pId);
         */

        // Select the VAO
        GL30.glBindVertexArray(vaoId);

        // Disable the VBO index from the VAO attributes list
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);

        // Delete the vertex VBO
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glDeleteBuffers(vboId);

        // Delete the color VBO
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glDeleteBuffers(vbocId);

        // Delete the index VBO
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL15.glDeleteBuffers(vboiId);

        // Delete the VAO
        GL30.glBindVertexArray(0);
        GL30.glDeleteVertexArrays(vaoId);
    }
}

class ShaderProgram {
    int shaderProgramid = -1;
    VertexShader vertexShader;
    FragmentShader fragmentShader;
    List<String> varstobind;
    // GlInterface glcontext = OpenGlContext.getGlContext();
    //static HashMap<String, OpenGlShaderProgram> shader = new HashMap<String, OpenGlShaderProgram>();

    //String vertexshadersource;
    //String fragmentshadersource;

    /*public ShaderProgram(String vertexshadersource, String fragmentshadersource) {
        this.vertexshadersource = vertexshadersource;
        this.fragmentshadersource = fragmentshadersource;
    } */


    /**
     * 14.3.16: Den setup direkt hier im Constructor machen
     *
     * @param vertexShader
     * @param fragmentShader
     * @param varstobind
     */
    public ShaderProgram(VertexShader vertexShader, FragmentShader fragmentShader, List<String> varstobind) {
        this.vertexShader = vertexShader;
        this.fragmentShader = fragmentShader;
        this.varstobind = varstobind;


//not supported        GL30.glBindFragDataLocation(shaderProgramid, 0, "outColor");

        // Wofuer sind link und validate? glBindAttribLocation() koennte man auch schon vorher machen
        // 11.4.14: Laut https://www.opengl.org/wiki/Generic_Vertex_Attribute_-_examples muss man es sogar vorher machen
        // Aus Doku:
        // Attribute variable name-to-generic attribute index bindings for a program object can be explicitly assigned at any time by calling glBindAttribLocation.
        // Attribute bindings do not go into effect until glLinkProgram isType called.
        // After a program object has been linked successfully, the index values for generic attributes
        // remain fixed (and their values can be queried) until the next link command occurs.

        //TODO this.exitOnGLError("setupShaders");
        setup();
    }

    private void setup() {


        //TODO wo genau kommt der errorcheck hin
        // int errorCheckValue = glcontext.glGetError();

        // Load the vertex shader
        vertexShader.setup();// = new VertexShader(vertexshadersource);
        // Load the fragment shader
        fragmentShader.setup();// = new FragmentShader(fragmentshadersource);

        // Create a new shader program that links both shaders
        shaderProgramid = GL20.glCreateProgram();
        GL20.glAttachShader(shaderProgramid, vertexShader.getId());
        //GlInterface gl = OpenGlContext.getGlContext();
        //OpenGlContext.getGlContext().exitOnGLError(gl, "after attach");
        GL20.glAttachShader(shaderProgramid, fragmentShader.getId());

        //OpenGlContext.getGlContext().exitOnGLError(gl, "after attach");

        for (int index = 0; index < varstobind.size(); index++) {
            if (LwjglDuoQuadColored.usevertexarray) {
                GL20.glBindAttribLocation(shaderProgramid, index, varstobind.get(index));
            } else {
                //TODO andere ausser gl_vertex
                //18.3.16 zu alt glcontext.glVertexPointerFloat(3, 0, 0);
            }
            //OpenGlContext.getGlContext().exitOnGLError(gl, "bind");

            //logger.info("vertex attribute index " + index + " bound to attribute variable " + varstobind.get(index));
        }
        link();
//??        GlImpl.exitOnGLError("after link");
    }

    /**
     * Muss nach glBindAttribLocation gemacht werden
     */
    public void link() {
        //if (!GL20.glLinkProgram(shaderProgramid)) {
        GL20.glLinkProgram(shaderProgramid);
          /*  System.err.println("Unable to link shader program:");
            System.exit(1);//TODO
        }*/
        GL20.glValidateProgram(shaderProgramid);
        // GlImpl.exitOnGLError("link");
    }

    public int getId() {
        return shaderProgramid;
    }

    public String getName() {
        return vertexShader.getName() + "-" + fragmentShader.getName();
    }

    /*19.3.16 public int getAttribLocation(String attr) {
        return GL20.glGetAttribLocation(shaderProgramid, attr);
    }*/

    /*public void setUniformMatrix3(String name, FloatBuffer value) {
        glcontext.glUniformMatrix3(glcontext.glGetUniformLocation(shaderProgramid, name), value);
        OpenGlContext.getGlContext().exitOnGLError(glcontext, "setUniformMatrix3 (glUniform3f) name=" + name + ", value=" + value + ", shaderprogram=" + shaderProgramid);
    }*/

    public void setUniformMatrix4(String name, FloatBuffer value) {
        GL20.glUniformMatrix4(GL20.glGetUniformLocation(shaderProgramid, name), false, value);
        // OpenGlContext.getGlContext().exitOnGLError(glcontext, "setUniformMatrix4 (glUniform3f) name=" + name + ", value=" + value + ", shaderprogram=" + shaderProgramid);
    }

    /*
    public void setUniform(String name, Color value) {
        int ul = glcontext.glGetUniformLocation(shaderProgramid, name);
        glcontext.glUniform4f(ul, value.getR(), value.getG(), value.getB(), value.getAlpha());
        OpenGlContext.getGlContext().exitOnGLError(glcontext, "setUniformColor name=" + name + ", value=" + value + ", shaderprogram=" + shaderProgramid);
    }

    public void setUniform(String name, int value) {
        int ul = glcontext.glGetUniformLocation(shaderProgramid, name);
        glcontext.glUniform1i(ul, value);
        OpenGlContext.getGlContext().exitOnGLError(glcontext, "setUniformColor name=" + name + ", value=" + value + ", shaderprogram=" + shaderProgramid);
    }

    public void setUniform(String name, boolean value) {
        glcontext.glUniform1i(glcontext.glGetUniformLocation(shaderProgramid, name), value ? 1 : 0);
        OpenGlContext.getGlContext().exitOnGLError(glcontext, "setUniformBoolean name=" + name + ", value=" + value + ", shaderprogram=" + shaderProgramid);
    }*/

   /* public boolean hasUniform(String name) {
        return glcontext.glGetUniformLocation(shaderProgramid, name) != -1;
    }*/

    /*public void setUniform(String name, OpenGlVector3 v) {
        setUniform(name, v.getX(), v.getY(), v.getZ());
    }*/

    /*public void setUniform(String name, float v0, float v1, float v2) {
        int location = glcontext.glGetUniformLocation(shaderProgramid, name);
        //logger.debug("location="+location);
        if (location == -1)
            throw new RuntimeException("uniform " + name + " not found in shader program " + getName());
        glcontext.glUniform3f(location, v0, v1, v2);
        OpenGlContext.getGlContext().exitOnGLError(glcontext, "setUniform2 (glUniform3f) name=" + name + ", shaderprogram=" + this);
    }*/

    public void use() {
        GL20.glUseProgram(shaderProgramid);
    }

    public void bind() {
        GL20.glUseProgram(shaderProgramid);
    }

    public void unbind() {
        GL20.glUseProgram(0);
    }

    /*public void destroyOpenGL() {
        glcontext.glUseProgram(0);
        glcontext.glDetachShader(getId(), vertexShader.getId());
        glcontext.glDetachShader(getId(), fragmentShader.getId());

        glcontext.glDeleteShader(vertexShader.getId());
        glcontext.glDeleteShader(fragmentShader.getId());
        glcontext.glDeleteProgram(getId());
    }*/

    /**
     * Vorher muss das VAO gebunden worden sein.
     */
   /* public void glEnableVertexAttribArray() {
        for (int index = 0; index < varstobind.size(); index++) {
            glcontext.glEnableVertexAttribArray(index);
            OpenGlContext.getGlContext().exitOnGLError(glcontext, "glEnableVertexAttribArray");
        }
    }

    public void DisableVertexAttribArray() {
        for (int index = 0; index < varstobind.size(); index++) {
            glcontext.glDisableVertexAttribArray(index);
            OpenGlContext.getGlContext().exitOnGLError(glcontext, "DisableVertexAttribArray");
        }
    }*/

  /*  public static OpenGlShaderProgram getOrBuildShader(String vertexshader, String fragmentshader, List<String> varstobind) {
        String id = vertexshader + fragmentshader;
        for (String s:varstobind){
            id+=s;
        }
        OpenGlShaderProgram prg = shader.get(id);
        if (prg == null) {
            // Dann anlegen    

            prg = new OpenGlShaderProgram(new OpenGlVertexShader(OpenGlShader.loadFromFile(vertexshader), "?"),
                    new OpenGlFragmentShader(OpenGlShader.loadFromFile(fragmentshader), "?"), varstobind);
            shader.put(id,prg);
        }
        return prg;
    }*/
}

class VertexShader extends Shader {
    public VertexShader(String source, String name) {
        super(GL20.GL_VERTEX_SHADER, source, name);
    }
}

class FragmentShader extends Shader {
    public FragmentShader(String source, String name) {
        super(GL20.GL_FRAGMENT_SHADER, source, name);
    }
}

class Shader {
    int shader;
    int type;
    String source;
    // Der name ist nur zur Dokumentation. Niemnd prueft auf doppelte Namen und die stoeren auch nicht
    String name;
    public static boolean use15 = true;

    protected Shader(int type, String source, String name) {
        this.type = type;
        this.source = source;
        this.name = name;
    }

    public void setup() {
        shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, /*1,*/ source/*, null*/);
        String log;
        GL20.glCompileShader(shader);
        /*
        if ((log = GL20.glCompileShader(shader)) != null) {
            //TODO
            System.err.println("Could not compile shader: " + log + "source:" + source);
            System.exit(-1);
        }*/
        //TODO this.exitOnGLError("loadShader");
    }

    public int getId() {
        return shader;
    }

    public String getName() {
        return name;
    }


}

