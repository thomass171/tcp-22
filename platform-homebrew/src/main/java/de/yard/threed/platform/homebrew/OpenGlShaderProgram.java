package de.yard.threed.platform.homebrew;

import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.Color;
import de.yard.threed.engine.platform.common.Settings;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.List;


/**
 * 01.08.2016: Shaderprogramme zwischen Materialien sharen.
 * 
 * Date: 24.03.14
 */
public class OpenGlShaderProgram {
    int shaderProgramid = -1;
    OpenGlVertexShader vertexShader;
    OpenGlFragmentShader fragmentShader;
    List<String> varstobind;
    Log logger = Platform.getInstance().getLog(OpenGlShaderProgram.class);
    static HashMap<String, OpenGlShaderProgram> shader = new HashMap<String, OpenGlShaderProgram>();
    
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
    public OpenGlShaderProgram(GlInterface glcontext,OpenGlVertexShader vertexShader, OpenGlFragmentShader fragmentShader, List<String> varstobind) {
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
        setup(glcontext);
    }

    private void setup(GlInterface glcontext) {


        //TODO wo genau kommt der errorcheck hin
        int errorCheckValue = glcontext.glGetError();

        // Load the vertex shader
        vertexShader.setup(glcontext);// = new VertexShader(vertexshadersource);
        // Load the fragment shader
        fragmentShader.setup(glcontext);// = new FragmentShader(fragmentshadersource);

        // Create a new shader program that links both shaders
        shaderProgramid = glcontext.glCreateProgram();
        glcontext.glAttachShader(shaderProgramid, vertexShader.getId());
        GlInterface gl = glcontext;
        glcontext.exitOnGLError(gl, "after attach");
        glcontext.glAttachShader(shaderProgramid, fragmentShader.getId());

        glcontext.exitOnGLError(gl, "after attach");

        for (int index = 0; index < varstobind.size(); index++) {
            if (Settings.usevertexarrays) {
                glcontext.glBindAttribLocation(shaderProgramid, index, varstobind.get(index));
            } else {
                //TODO andere ausser gl_vertex
                //18.3.16 zu alt glcontext.glVertexPointerFloat(3, 0, 0);
            }
            glcontext.exitOnGLError(gl, "bind");

            logger.info("vertex attribute index " + index + " bound to attribute variable " + varstobind.get(index));
        }
        link(gl);
//??        GlImpl.exitOnGLError("after link");
    }

    /**
     * Muss nach glBindAttribLocation gemacht werden
     */
    public void link(GlInterface glcontext) {
        if (!glcontext.glLinkProgram(shaderProgramid)) {
            System.err.println("Unable to link shader program:");
            System.exit(1);//TODO
        }
        glcontext.glValidateProgram(shaderProgramid);
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

    public void setUniformMatrix3(GlInterface glcontext,String name, FloatBuffer value) {
        glcontext.glUniformMatrix3(glcontext.glGetUniformLocation(shaderProgramid, name), value);
        glcontext.exitOnGLError(glcontext, "setUniformMatrix3 (glUniform3f) name=" + name + ", value=" + value + ", shaderprogram=" + shaderProgramid);
    }

    public void setUniformMatrix4(GlInterface glcontext,String name, FloatBuffer value) {
        glcontext.glUniformMatrix4(glcontext.glGetUniformLocation(shaderProgramid, name), value);
        glcontext.exitOnGLError(glcontext, "setUniformMatrix4 (glUniform3f) name=" + name + ", value=" + value + ", shaderprogram=" + shaderProgramid);
    }

    public void setUniform(GlInterface glcontext,String name, Color value) {
        int ul = glcontext.glGetUniformLocation(shaderProgramid, name);
        glcontext.glUniform4f(ul, value.getR(), value.getG(), value.getB(), value.getAlpha());
        glcontext.exitOnGLError(glcontext, "setUniformColor name=" + name + ", value=" + value + ", shaderprogram=" + shaderProgramid);
    }

    public void setUniform(GlInterface glcontext,String name, int value) {
        int ul = glcontext.glGetUniformLocation(shaderProgramid, name);
        glcontext.glUniform1i(ul, value);
        glcontext.exitOnGLError(glcontext, "setUniformColor name=" + name + ", value=" + value + ", shaderprogram=" + shaderProgramid);
    }

    public void setUniform(GlInterface glcontext,String name, boolean value) {
        glcontext.glUniform1i(glcontext.glGetUniformLocation(shaderProgramid, name), value ? 1 : 0);
        glcontext.exitOnGLError(glcontext, "setUniformBoolean name=" + name + ", value=" + value + ", shaderprogram=" + shaderProgramid);
    }

    public boolean hasUniform(GlInterface glcontext, String name) {
        return glcontext.glGetUniformLocation(shaderProgramid, name) != -1;
    }

    public void setUniform(GlInterface glcontext,String name, Vector3 v) {
        setUniform(glcontext, name, (float)v.getX(),(float) v.getY(), (float)v.getZ());
    }

    public void setUniform(GlInterface glcontext,String name, float v0, float v1, float v2) {
        int location = glcontext.glGetUniformLocation(shaderProgramid, name);
        //logger.debug("location="+location);
        if (location == -1)
            throw new RuntimeException("uniform " + name + " not found in shader program " + getName());
        glcontext.glUniform3f(location, v0, v1, v2);
        glcontext.exitOnGLError(glcontext, "setUniform2 (glUniform3f) name=" + name + ", shaderprogram=" + this);
    }

    public void use(GlInterface glcontext) {
        glcontext.glUseProgram(shaderProgramid);
    }

   /*25.1.23 public void bind() {
        GlInterface glcontext = PlatformHomeBrew.getGlContext();
        glcontext.glUseProgram(shaderProgramid);
    }

    public void unbind() {
        GlInterface glcontext = PlatformHomeBrew.getGlContext();
        glcontext.glUseProgram(0);
    }

    public void destroyOpenGL() {
        GlInterface glcontext = PlatformHomeBrew.getGlContext();
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
    public void glEnableVertexAttribArray(GlInterface glcontext) {
        for (int index = 0; index < varstobind.size(); index++) {
            glcontext.glEnableVertexAttribArray(index);
            glcontext.exitOnGLError(glcontext, "glEnableVertexAttribArray");
        }
    }

    public void DisableVertexAttribArray(GlInterface glcontext) {
        for (int index = 0; index < varstobind.size(); index++) {
            glcontext.glDisableVertexAttribArray(index);
            glcontext.exitOnGLError(glcontext, "DisableVertexAttribArray");
        }
    }

    public static OpenGlShaderProgram getOrBuildShader(GlInterface glcontext,String vertexshader, String fragmentshader, List<String> varstobind) {
        String id = vertexshader + fragmentshader;
        for (String s:varstobind){
            id+=s;
        }
        OpenGlShaderProgram prg = shader.get(id);
        if (prg == null) {
            // Dann anlegen    

            prg = new OpenGlShaderProgram(glcontext,new OpenGlVertexShader(OpenGlShader.loadFromFile(glcontext,vertexshader), "?"),
                    new OpenGlFragmentShader(OpenGlShader.loadFromFile(glcontext,fragmentshader), "?"), varstobind);
            shader.put(id,prg);
        }
        return prg;
    }
}
