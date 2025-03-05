package de.yard.threed.platform.jme;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetLocator;
import com.jme3.asset.AssetManager;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Uniform;
import de.yard.threed.core.platform.UniformType;
import de.yard.threed.core.platform.AbstractShaderProgram;
import de.yard.threed.engine.platform.common.ShaderUtil;

import java.io.ByteArrayInputStream;
import java.util.List;


/**
 * 3.5.19: Allgemeine custom shader (z.B. Photoalbum) liegen in bundle core. Da ist das mit rootpath aeusserst fragwürdig?
 * Es koennte zufällig gehen, weil die Dateien auch im FS liegen. Und es gibt auch JME spezifische custom shader (MYLighting),
 * die liegen nur in JME im FS. Da ist etwas nicht rund?
 * <p>
 * Created by thomass on 23.12.15.
 */
public class JmeShaderLocator implements AssetLocator {
    Log logger = Platform.getInstance().getLog(JmeShaderLocator.class);
    private String rootpath;

    public JmeShaderLocator() {

    }

    @Override
    public void setRootPath(String s) {
        rootpath = s;
    }

    /**
     * @param assetManager
     * @param assetKey
     * @return
     */
    @Override
    public AssetInfo locate(AssetManager assetManager, final AssetKey assetKey) {
        final String assetkey = assetKey.getName();
        logger.debug("locate: " + assetkey + " with root path " + rootpath);
        if (!assetkey.endsWith(".vert") && !assetkey.endsWith(".frag")) {
            // cannot be located by this locator
            return null;
        }

        AssetInfo ai = new AssetInfo(assetManager, assetKey) {
            @Override
            public java.io.InputStream openStream() {
                try {
                    String source = null;
                    String loc = /*17.3.16 rootpath + "/" +*/ assetkey.substring(JmeResourceManager.RESOURCEPREFIX.length() + 1);
                    String effectKey = StringUtils.substringBefore(loc, "/");
                    loc = StringUtils.substringAfter(loc, "/");
                    logger.debug("loading " + loc + " from effect " + effectKey);
                    AbstractShaderProgram shaderInfo = JmeProgram.effects.get(effectKey);

                    if (loc.equals("vertexshader.vert")) {
                        source = shaderInfo.vertexshader;
                    }
                    if (loc.equals("fragmentshader.frag")) {
                        source = shaderInfo.fragmentshader;
                    }
                    if (source == null) {
                        throw new RuntimeException("no source for shader " + loc);
                    }
                    //HashMap<String,String> translatemap = new HashMap<String, String>();
                    source = ShaderUtil.preprocess(source/*,translatemap*/);
                    if (assetkey.endsWith(".vert")) {
                        source = "uniform mat4 g_ProjectionMatrix;\n" +
                                "uniform mat4 g_WorldViewMatrix;\n" +
                                "attribute vec3 inPosition;\n" +
                                "attribute vec3 inNormal;\n" +
                                "attribute vec2 inTexCoord;\n" +
                                // Multiple comment/blanks to avoid 'uniform mat3' replacement
                                "uniform  /* */    mat3 g_NormalMatrix;\n" +
                                "uniform mat4 g_ViewMatrix;\n" +
                                source;
                        //source = source.replaceAll("MODELVIEWPROJECTIONMATRIX","g_WorldViewProjectionMatrix");
                        source = source.replaceAll("PROJECTIONMATRIX", "g_ProjectionMatrix");
                        source = source.replaceAll("MODELVIEWMATRIX", "g_WorldViewMatrix");
                        source = source.replaceAll("VERTEX", "inPosition");
                        source = source.replaceAll("MULTITEXCOORD0", "inTexCoord");
                        // See README.md about normalMatrix Problem in JME
                        source = source.replaceAll("NORMALMATRIX", "g_NormalMatrix");
                        source = source.replaceAll("NORMAL", "inNormal");
                        source = source.replaceAll("OUT", "varying");
                        source = source.replaceAll("VIEWMATRIX", "g_ViewMatrix");
                    }
                    if (assetkey.endsWith(".frag")) {
                        source = source.replaceAll("FRAGCOLOR", "gl_FragColor");
                        source = source.replaceAll("TEXTURE2D", "texture2D");
                        source = source.replaceAll("IN", "varying");
                    }
                    source = workaroundMat3(source, shaderInfo.uniforms);

                    // Consider the unusual JME naming convention of material uniforms with prefix "m_".
                    // 27.4.16: That is really annoying and error prone.
                    // 8.1.25: And why doesn't "texture" need a replacement? Anyway, we use 'u_texture' now.
                    //22.2.25 source = source.replaceAll("u_isunshaded", "m_isunshaded");
                    source = source.replaceAll("u_texture", "m_texture");
                    source = source.replaceAll("u_color", "m_color");
                    source = source.replaceAll("u_texture0", "m_texture0");
                    source = source.replaceAll("u_texture1", "m_texture1");
                    source = source.replaceAll("u_transparency", "m_transparency");

                    source = source.replaceAll("u_shaded", "m_shaded");
                    source = source.replaceAll("u_textured", "m_textured");
                    source = source.replaceAll("u_ambient_light_color", "m_ambient_light_color");
                    source = source.replaceAll("u_directional_light_color", "m_directional_light_color");
                    source = source.replaceAll("u_directional_light_direction", "m_directional_light_direction");

                    if (source.contains("u_")){
                        throw new RuntimeException("unconverted 'u_...' uniform?");
                    }
                    logger.debug("final shader source:" + source);
                    return new ByteArrayInputStream(source.getBytes("UTF-8"));
                } catch (Exception e) {
                    //TODO Fehlerhandling
                    e.printStackTrace();
                    return null;
                }
            }
        };
        return ai;
    }

    /**
     * JME has no mat3 uniform type.
     *
     * @param source
     * @param uniforms
     * @return
     */
    private String workaroundMat3(String source, List<Uniform> uniforms) {

        String MAIN_LINE = "void main() {";
        String initPart = "";

        if (!source.contains(MAIN_LINE)) {
            throw new RuntimeException("no main line found");
        }
        for (Uniform uniform : uniforms) {
            if (uniform.type == UniformType.MATRIX3) {
                String expectedUniformDefinition = "uniform mat3 " + uniform.name + ";";
                // This workaround is called for both vert/frag shader, but uniforms might only exist in one of these
                if (source.contains(expectedUniformDefinition)) {

                    source = source.replaceAll(expectedUniformDefinition,
                            "uniform vec3 " + uniform.name + "_col0;\n" +
                                    "uniform vec3 " + uniform.name + "_col1;\n" +
                                    "uniform vec3 " + uniform.name + "_col2;");

                    initPart +=
                            //  GLSL uses column major!
                            // texture_matrix[0] = u_texture_matrix_col0; // first column
                            //    texture_matrix[1] = u_texture_matrix_col1; // second column
                            //    texture_matrix[2] = u_texture_matrix_col2; // third column
                            "\n" +
                                    "    mat3 " + uniform.name + ";\n" +
                                    "    " + uniform.name + "[0]=" + uniform.name + "_col0;" + "// first column\n" +
                                    "    " + uniform.name + "[1]=" + uniform.name + "_col1;" + "// second column\n" +
                                    "    " + uniform.name + "[2]=" + uniform.name + "_col2;" + "// third column\n";

                }
            }
        }

        source = source.replace(MAIN_LINE, MAIN_LINE + initPart);

        if (source.contains("uniform mat3")) {
            throw new RuntimeException("'uniform mat3' not replaced");
        }

        return source;
    }


}
