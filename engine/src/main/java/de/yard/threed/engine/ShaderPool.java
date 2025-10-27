package de.yard.threed.engine;


import de.yard.threed.core.GeneralParameterHandler;
import de.yard.threed.core.Matrix3;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.NativeLight;
import de.yard.threed.core.platform.NativeMaterial;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Uniform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.ResourceLoader;
import de.yard.threed.engine.platform.ResourceLoaderFromBundle;
import de.yard.threed.engine.platform.common.ShaderProgram;

/**
 * Was class "Effect" once, inspired from FG effects (*eff files) and JME material definition (*.j3md).
 * <p>
 * After many refactorings this finally is a set of custom shader that are used
 * together with some material. These are used when special effects are needed that are not covered by the platform.
 * But there is no close relation to a material, at least not here.
 * <p>
 * Transparency for example could both be defined through the platform or by effects. Depends on the use case whether a custom shader is used.
 * <p>
 * 25.2.22: platform independent Shader still not work in ThreeJs and Unity. Maybe its a bad approach.
 * 16.10.2024: Now they work.
 * 3.2.25: Renamed from "Effect" to "ShaderPool".
 * <p>
 * Created by thomass on 30.10.15.
 */
public class ShaderPool {
    static Log logger = Platform.getInstance().getLog(ShaderPool.class);

    public static ShaderProgram buildPhotoalbumEffect() {
        ShaderProgram program = buildShaderProgram("PhotoAlbum", "engine",
                "shader/PhotoAlbum.vert",
                "shader/PhotoAlbum.frag");
        program.program.addSampler2DUniform("u_texture0");
        program.program.addSampler2DUniform("u_texture1");
        program.program.compile();
        return program;
    }

    /*16.10.24 public static Effect buildModelCombinedEffect() {
        Effect effect = new Effect("model-combined");
        effect.shader.uniforms.add(/*"BaseTex",* / new Uniform("BaseTex", UniformType.SAMPLER_2D));
        effect.shader.vertexshader = "shader/ubershader.vert";
        effect.shader.fragmentshader = "shader/model-ALS-ultra.frag";

        return effect;
    }*/

    /**
     * 8.10.17: deprecated, because available in platform.
     * 16.10.24: Used as default shader in platform Homebrew and thus in sceneserver. Should be moved there.
     * 24.2.25: Now the merged SolidColorEffect and SimpleTextureEffect (platform shader have no textureMatrix(?)).
     *
     * @return
     */
    public static ShaderProgram buildUniversalEffect() {
        ShaderProgram program = buildShaderProgram("Universal", "engine",
                "shader/Universal.vert",
                "shader/Universal.frag");
        program.program.addFloatVec4Uniform(Uniform.COLOR);
        program.program.addSampler2DUniform(Uniform.TEXTURE);
        program.program.addMatrix3Uniform(Uniform.TEXTUREMATRIX);
        program.program.addFloatUniform(Uniform.TRANSPARENCY);
        program.program.addBooleanUniform(Uniform.SHADED);
        program.program.addBooleanUniform(Uniform.TEXTURED);
        program.program.addFloatVec3Uniform(Uniform.AMBIENT_LIGHT_COLOR);
        program.program.addFloatVec3Uniform(Uniform.DIRECTIONAL_LIGHT_COLOR);
        program.program.addFloatVec3Uniform(Uniform.DIRECTIONAL_LIGHT_DIRECTION);
        program.program.addIntUniform(Uniform.DEBUG_MODE);
        program.program.compile();

        program.defaultSetter = mat -> {
            // Some platforms like ThreeJs reject unset uniforms, especially vector3, so want a value for all.
            // However texture seems to be no problem, so no default for texture
            //mat.getUniform(Uniform.TEXTURE).setValue(new Texture());
            mat.getUniform(Uniform.COLOR).setValue(new Quaternion());

            //  Default is diagonal matrix with 1.0 on the diagonal.
            mat.getUniform(Uniform.TEXTUREMATRIX).setValue(new Matrix3());
            mat.getUniform(Uniform.TRANSPARENCY).setValue(Float.valueOf(0.0f));
            // textured is the most common use case
            mat.getUniform(Uniform.TEXTURED).setValue(true);
            mat.getUniform(Uniform.SHADED).setValue(true);

            // lights will be updated later in RegisteredShaderMaterial
            mat.getUniform(Uniform.AMBIENT_LIGHT_COLOR).setValue(new Vector3());
            mat.getUniform(Uniform.DIRECTIONAL_LIGHT_COLOR).setValue(new Vector3(22, 22, 22));
            mat.getUniform(Uniform.DIRECTIONAL_LIGHT_DIRECTION).setValue(new Vector3(33, 33, 33));
        };
        return program;
    }

    private static ShaderProgram buildShaderProgram(String name, String bundleName, String vertexShader, String fragmentShader) {
        Bundle bundle = BundleRegistry.getBundle(bundleName);
        ResourceLoader vertexShaderResourceLoader = new ResourceLoaderFromBundle(new BundleResource(bundle, null, vertexShader));
        ResourceLoader fragmentShaderResourceLoader = new ResourceLoaderFromBundle(new BundleResource(bundle, null, fragmentShader));
        return new ShaderProgram(name, new BundleResource(bundle, null, vertexShader), new BundleResource(bundle, null, fragmentShader));

    }
}

