package de.yard.threed.platform.jme;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import de.yard.threed.core.Util;
import de.yard.threed.core.platform.NativeProgram;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.engine.Uniform;
import de.yard.threed.engine.UniformType;
import de.yard.threed.engine.platform.AbstractShaderProgram;

import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A material and uniform setter do not belong here because a program might be shared.
 */
public class JmeProgram extends AbstractShaderProgram implements NativeProgram {

    // Das mit den j3md Dteien ist total verbaut. Dem kann man nur hinten rum
    // etwas unterschieben. Die Map ist über den Wrapper gesynced.
    static SortedMap<String, AbstractShaderProgram> effects = Collections.synchronizedSortedMap(new TreeMap<String, AbstractShaderProgram>());

    //ShaderInfo myShaderInfo;

    public JmeProgram(String name, BundleResource vertexShader, BundleResource fragmentShader) {
        super(name, loadShader(vertexShader), loadShader(fragmentShader));
        effects.put(name, this);
    }

    @Override
    public void addSampler2DUniform(String name) {
        validate(name);
        uniforms.add(new Uniform(name, UniformType.SAMPLER_2D));
    }

    @Override
    public void addMatrix3Uniform(String name) {
        validate(name);
        uniforms.add(new Uniform(name, UniformType.MATRIX3));
    }

    @Override
    public void addFloatVec3Uniform(String name) {
        validate(name);
        uniforms.add(new Uniform(name, UniformType.FLOAT_VEC3));
    }

    @Override
    public void addFloatVec4Uniform(String name) {
        validate(name);
        uniforms.add(new Uniform(name, UniformType.FLOAT_VEC4));
    }

    @Override
    public void addBooleanUniform(String name) {
        validate(name);
        Util.notyet();
    }

    @Override
    public void addFloatUniform(String name) {
        validate(name);
        uniforms.add(new Uniform(name, UniformType.FLOAT));
    }

    @Override
    public void compile() {
        // needs uniforms
        //mat = buildProgram();
        //program is build during material building
    }

    /**
     * No idea how jme shares shader.
     * So for now create a new material for each using of a program.
     *
     * @return
     */
    public static Material buildProgram(JmeProgram myShaderInfo) {

        Material mat;

        boolean hasnormalmap = false;

        AssetManager am = ((PlatformJme) Platform.getInstance()).jmeResourceManager.am;

        // Suffix j3md is needed for triggering the fitting loader (JmeEffectLocator?). The j3md file is created temporarily on the fly.
        mat = new Material(am/**/, /*effect.*/myShaderInfo.name + ".j3md");
        if (mat.getName() == null) {
            mat.setName(myShaderInfo.name);
        }
        // Even when using dedicated shader which handle transparency its imported to tell the engine
        // to put these objects at the end of rendering.
            /*done later in JmeMaterial if (transparency != null/*effect.transparent* /) {
                //ist fuer Shader, aber eigene haben das nicht.
                if (mat.getParam("UseAlpha") != null) {
                    mat.setBoolean("UseAlpha", true);
                }
                mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
            }*/

           /* for (int i = 0; i < textures.length; i++) {
                //mat.setTexture("texture" + i, ((JmeTexture) textures[i]).texture); // with Lighting.j3md

            }*/


        // 3.11.15: Backface Culling ausschalten. Wird z.Z. nur für Leaf gebraucht (als vorübergehende Krücke) und sollte mittelfristig
        //generell eingeschaltet sein.
        //mat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);

        return mat;
    }
}
