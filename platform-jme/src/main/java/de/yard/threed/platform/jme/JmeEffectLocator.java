package de.yard.threed.platform.jme;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetLocator;
import com.jme3.asset.AssetManager;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.Effect;
import de.yard.threed.engine.Uniform;
import de.yard.threed.engine.UniformType;

import de.yard.threed.core.platform.Log;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Effects are dynamically built j3md files. So they have no rootpath.
 *
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

        Effect effect = JmeMaterial.effects.get(assetkey.replace(".j3md", ""));
        String jdmestring =
                "MaterialDef Unimportant Name  {\n" +
                        "    // user defined uniforms\n" +
                        "    MaterialParameters {\n";
        //TODO unwanted dependny, NativeUniform reuqired
        for (Uniform uniform : effect.shader.uniforms) {
            //  Uniform uniform = effect.uniforms.get(key);
            jdmestring += getJ3mdType(uniform.type) + " " + uniform.name + "\n";
        }
        // 23.12.15: GLSL version 1.20 can be hard coded, because newer are not available due to the JME binding to
        // old OpenGL.
        // Setting LWJGL_OPENGL3 provides the option to use GLSL >= 1.5, but causes JME internal shader (using GLSL100) to fail
        jdmestring += " }\n" +
                "    Technique {\n" +
                "        // Shader\n" +
                // FG Original shader have version 120

                "        VertexShader GLSL120:   " + JmeResourceManager.RESOURCEPREFIX + "/" + effect.shader.vertexshader + "\n" +
                "        FragmentShader GLSL120: " + JmeResourceManager.RESOURCEPREFIX + "/" + effect.shader.fragmentshader + "\n" +
                "        // global uniforms\n" +
                "        WorldParameters {\n" +
                "            WorldViewProjectionMatrix\n" +
                "            ProjectionMatrix\n" +
                "            WorldViewMatrix\n" +
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
            default:
                logger.error("unknown uniform type " + type);
                return "unknown";
        }
    }
}
