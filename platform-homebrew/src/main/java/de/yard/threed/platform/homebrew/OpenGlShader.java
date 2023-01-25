package de.yard.threed.platform.homebrew;

import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.platform.common.ShaderUtil;

/**
 * Date: 24.03.14
 */
public class OpenGlShader {
    static Log logger = PlatformHomeBrew.getInstance().getLog(OpenGlShader.class);
    int shader;
    GlInterface.ShaderType type;
    String source;
    // Der name ist nur zur Dokumentation. Niemnd prueft auf doppelte Namen und die stoeren auch nicht
    String name;
    public static boolean use15 = true;

    protected OpenGlShader(GlInterface.ShaderType type, String source, String name) {
        this.type = type;
        this.source = source;
        this.name = name;
    }

    public void setup(GlInterface glcontext) {
        shader = glcontext.glCreateShader(type);
        glcontext.glShaderSource(shader, /*1,*/ source/*, null*/);
        String log;
        if ((log = glcontext.glCompileShader(shader)) != null) {
            //TODO
            System.err.println("Could not compile shader: " + log + "source:" + source);
            System.exit(-1);
        }
        //TODO this.exitOnGLError("loadShader");
    }

    public int getId() {
        return shader;
    }

    public String getName() {
        return name;
    }

    /**
     * So wie der JmeShaderLocator auch einen Shader lädt.
     *
     * @param filename
     * @return
     */
    public static String loadFromFile(GlInterface glcontext, String filename) {
        StringBuilder shaderSource = new StringBuilder();
        String source;
        try {
            // 20.4.17: Jetzt aus Bundle
            String loc = filename;
            logger.debug("loading shader from " + loc);
            //InputStream isType = Platform.getInstance().loadResourceSync(new BundleResource(loc));
            //10.12.18: Wo genau die Shader liegen sollten? core oder engine? Wer weiss. Jetzt in core. Muessten mit PlatformOpenGL und JME
            //aber nach Desktop.
            String/*byte[]*/ bytebuf = new String(BundleRegistry.getBundle("engine").getResource(loc).getContentAsString());//isType.readFully();
            source = bytebuf;//new String(bytebuf, "UTF-8");
            //HashMap<String,String> translatemap = new HashMap<String, String>();
            source = ShaderUtil.preprocess(source/*,translatemap*/);
            if (filename.endsWith(".vert")) {
                if (use15) {
                    // In 1.2 gibt es keine separate VIEWMATRIX und MODELMATRIX. Darum hier auch nicht
                    // Die precision ist wohl nur für WebGl/ES erforderlich.
                    source = "#version " + glcontext.getGlslVersion() + "\n" +
                            //"precision highp float;\n" +
                            //"precision highp int;\n"+
                            "#define VERSION150\n" +
                            "uniform mat4 PROJECTIONMATRIX;\n" +
                            //    "uniform mat4 MODELVIEWMATRIX;\n" +
                            "uniform mat4 MODELVIEWMATRIX;\n" +
                            //schon implit, aber als vec4
                            "in vec3 VERTEX;\n" +
                            "in vec3 NORMAL;\n" +
                            "in vec2 MULTITEXCOORD0;\n" +
                            //schon implizit, aber als vec4  "attribute vec2 gl_MultiTexCoord0;\n" +
                            "uniform mat3 NORMALMATRIX;\n" +
                            source;
                    //source = source.replaceAll("MODELVIEWPROJECTIONMATRIX","g_WorldViewProjectionMatrix");
                    //source = source.replaceAll("PROJECTIONMATRIX", "gl_ProjectionMatrix");
                    //source = source.replaceAll("MODELVIEWMATRIX", "gl_ModelViewMatrix");
                    //source = source.replaceAll("VERTEX", "vec3(gl_Vertex)");
                    //source = source.replaceAll("MULTITEXCOORD0", "vec2(gl_MultiTexCoord0)");
                } else {
                    source = "#version 120\n" +
                            //schon implizit "uniform mat4 gl_ProjectionMatrix;\n" +
                            //schon implizit "uniform mat4 gl_ModelViewMatrix;\n" +
                            //schon implit, aber als vec4 attribute vec3 gl_Vertex;\n" +
                            //schon implizit, aber als vec4  "attribute vec2 gl_MultiTexCoord0;\n" +
                            source;
                    //source = source.replaceAll("MODELVIEWPROJECTIONMATRIX","g_WorldViewProjectionMatrix");
                    source = source.replaceAll("PROJECTIONMATRIX", "gl_ProjectionMatrix");
                    source = source.replaceAll("MODELVIEWMATRIX", "gl_ModelViewMatrix");
                    source = source.replaceAll("VERTEX", "vec3(gl_Vertex)");
                    source = source.replaceAll("MULTITEXCOORD0", "vec2(gl_MultiTexCoord0)");
                    source = source.replaceAll("NORMALMATRIX", "gl_NormalMatrix");
                    source = source.replaceAll("NORMAL", "gl_Normal");

                }
            }
            if (filename.endsWith(".frag")) {
                if (use15) {
                    source = "#version " + glcontext.getGlslVersion() + "\n" +
                            //"precision highp float;\n" +
                            //"precision highp int;\n"+
                            "#define VERSION150\n" +
                            "out vec4 my_fragcolor;\n" +
                            source;
                    source = source.replaceAll("FRAGCOLOR", "my_fragcolor");
                    source = source.replaceAll("TEXTURE2D", "texture");

                } else {
                    source = "#version 120\n" + source;
                    source = source.replaceAll("FRAGCOLOR", "gl_FragColor");
                    source = source.replaceAll("TEXTURE2D", "texture2d");

                }
                //logger.debug("shadersource:"+source);
            }
            return source;
            /*BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = reader.readLine()) != null) {
                shaderSource.append(line).append("\n");
            }
            reader.close();*/
        } catch (Exception e) {
            System.err.println("Could not read file " + filename);
            e.printStackTrace();
            System.exit(-1);  //TODO
        }
        return shaderSource.toString();
    }
}
