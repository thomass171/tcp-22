package de.yard.threed.platform.jme;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetLocator;
import com.jme3.asset.AssetManager;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.Uniform;
import de.yard.threed.core.platform.UniformType;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.AbstractShaderProgram;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Effects are dynamically built j3md files. So they have no rootpath.
 * <p>
 * Created by thomass on 30.10.15.
 */
public class JmeEffectLocator implements AssetLocator {
    Log logger = Platform.getInstance().getLog(JmeEffectLocator.class);

    public JmeEffectLocator() {

    }

    @Override
    public void setRootPath(String s) {

    }

    /**
     * Dynamically build temporary JME j3me file on the fly.
     *
     * @param assetManager
     * @param assetKey
     * @return
     */
    @Override
    public AssetInfo locate(AssetManager assetManager, AssetKey assetKey) {
        String assetkey = assetKey.getName();
        logger.debug("locate: " + assetkey);
        if (!assetkey.endsWith(".j3md")) {
            // cannot be located by this locator
            return null;
        }

        String effectKey = assetkey.replace(".j3md", "");
        AbstractShaderProgram effect = JmeProgram.effects.get(effectKey);
        String jdmestring =
                "MaterialDef Unimportant Name  {\n" +
                        "    // user defined uniforms\n" +
                        "    MaterialParameters {\n";

        for (Uniform uniform : effect.uniforms) {

            // 8.1.25 Remove prefix "u_" because JME uses "m_" implicitly. And workaround for unknown type 'mat3'.
            if (uniform.type == UniformType.MATRIX3){
                jdmestring += "Vector3 " + uniform.name.substring(2) + "_col0\n";
                jdmestring += "Vector3 " + uniform.name.substring(2) + "_col1\n";
                jdmestring += "Vector3 " + uniform.name.substring(2) + "_col2\n";
            }else {
                jdmestring += getJ3mdType(uniform.type) + " " + uniform.name.substring(2) + "\n";
            }
        }
        // 23.12.15: GLSL version 1.20 can be hard coded, because newer are not available due to the JME binding to
        // old OpenGL.
        // Setting LWJGL_OPENGL3 provides the option to use GLSL >= 1.5, but causes JME internal shader (using GLSL100) to fail
        jdmestring += " }\n" +
                "    Technique {\n" +
                "        // Shader\n" +
                // FG Original shader have version 120
                // use symbolic shader names for now. Will be replaced in JmeShaderLocator later
                "        VertexShader GLSL120:   " + JmeResourceManager.RESOURCEPREFIX + "/" + effectKey + "/vertexshader.vert\n" +
                "        FragmentShader GLSL120: " + JmeResourceManager.RESOURCEPREFIX + "/" + effectKey + "/fragmentshader.frag\n" +
                "        // global uniforms\n" +
                "        WorldParameters {\n" +
                "            WorldViewProjectionMatrix\n" +
                "            ProjectionMatrix\n" +
                "            WorldViewMatrix\n" +
                "            NormalMatrix\n" +
                "            ViewMatrix\n" +
                "        }\n" +
                "    }\n" +
                "}";

        final String sjdmestring = jdmestring;
        logger.debug("jdme=" + sjdmestring);

        AssetInfo ai = new AssetInfo(assetManager, assetKey) {
            @Override
            public InputStream openStream() {
                return new ByteArrayInputStream(sjdmestring.getBytes());
            }
        };
        return ai;
    }

    private String getJ3mdType(UniformType type) {
        switch (type) {
            case FLOAT_VEC4:
                return "Vector4";
            case SAMPLER_2D:
                return "Texture2D";
            case BOOL:
                return "Boolean";
            //not known in JME, will be handled later by workaround
            case MATRIX3:
                throw new RuntimeException("Should not be reached but handles by workaround");
            case FLOAT:
                return "Float";
            case FLOAT_VEC3:
                return "Vector3";
            case INT:
                return "Int";
            default:
                // no option to just continue
                throw new RuntimeException("unknown uniform type " + type);
        }
    }
}
