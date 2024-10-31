package de.yard.threed.engine;


import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.loader.PortableMaterial;
import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.platform.common.EffectShader;

/**
 * Inspired from FG effects (*eff files) and JME material definition (*.j3md).
 * <p>
 * After many refactorings this finally is a set of custom shader that are used
 * together with some material. These are used when special effects are needed that are not covered by the platform.
 * But there is no close relation to a material, at least not here.
 * <p>
 * Transparency for example could both be defined through the platform or by effects. Depends on the use case whether a custom shader is used.
 * <p/>
 * <p>
 * 9.3.21: MA31: Split to engine and FG(FGEffect).
 * <p>
 * 25.2.22: platform independent Shader still not work in ThreeJs and Unity. Maybe its a bad approach.
 * 16.10.2024: Now they work.
 * <p>
 * Created by thomass on 30.10.15.
 */
public class Effect {
    Log logger = Platform.getInstance().getLog(Effect.class);

    public String name;
    //9.3.21 public SGPropertyNode root, parametersProp;

    // 16.10.24: No longer optional. An effect now is just a collection of shader that are used in every platform.
    public EffectShader shader = new EffectShader();

    public Effect() {
    }

    /**
     * Erstmal zum Einstieg
     */
    private Effect(String name) {
        this.name = name;
        shader = new EffectShader();

    }

    @Deprecated
    public static Effect buildSolidColorEffect() {
        Effect effect = new Effect("SolidColor");
        effect.shader.vertexshader = "shader/SolidColor.vert";
        effect.shader.fragmentshader = "shader/SolidColor.frag";
        effect.shader.uniforms.add(/*"Color",*/ new Uniform("Color", UniformType.FLOAT_VEC4));
        return effect;
    }

    /**
     * 21.9.19 noch in verwendung
     * 16.10.24: Really? If so, buildUniversalEffect() can be used directly.
     */
    public static Effect buildSimpleTextureEffect() {
        // 16.10.24 return buildUniversalEffect();
        Effect effect = new Effect("SimpleTexture");
        effect.shader.vertexshader = "shader/SimpleTexture.vert";
        effect.shader.fragmentshader = "shader/SimpleTexture.frag";
        //effect.shader.uniforms.add("Color", new Uniform("Color",UniformType.FLOAT_VEC4));
        effect.shader.uniforms.add(new Uniform("texture",UniformType.SAMPLER_2D));
        return effect;
    }

    public static Effect buildPhotoalbumEffect() {
        Effect effect = new Effect("PhotoAlbum");
        effect.shader.vertexshader = "shader/PhotoAlbum.vert";
        effect.shader.fragmentshader = "shader/PhotoAlbum.frag";
        effect.shader.uniforms.add(/*"texture0", */new Uniform("texture0", UniformType.SAMPLER_2D));
        effect.shader.uniforms.add(/*"texture1",*/ new Uniform("texture1", UniformType.SAMPLER_2D));
        return effect;
    }

    /*16.10.24 public static Effect buildModelCombinedEffect() {
        Effect effect = new Effect("model-combined");
        effect.shader.uniforms.add(/*"BaseTex",* / new Uniform("BaseTex", UniformType.SAMPLER_2D));
        effect.shader.vertexshader = "shader/ubershader.vert";
        effect.shader.fragmentshader = "shader/model-ALS-ultra.frag";

        return effect;
    }*/

    /**
     * 8.10.17: deprecated, weil so was wie universal die Platform schon können könnte und ich dafuer keine custom shader brauche.
     * 21.9.19 Aufruf wirklich verhindern. Der UniversalShader wandert in die Platform OpenGL.
     * 16.10.24: Used as default shader in platform Homebrew. Should be moved there.
     * @return
     */
    @Deprecated
    public static Effect buildUniversalEffect(/*boolean transparent*/) {
        Effect effect = new Effect("universal");
        effect.shader.uniforms.add(/*"BaseTex",*/ new Uniform("basetex", UniformType.SAMPLER_2D));
        effect.shader.uniforms.add(/*"BaseTex",*/ new Uniform("isunshaded", UniformType.BOOL));
        effect.shader.vertexshader = "shader/Universal.vert";
        effect.shader.fragmentshader = "shader/Universal.frag";
        //20.7.16 effect.transparent = transparent;
        return effect;
    }

    public boolean valid() {
        return true;//TODO Util.notyet();
    }

    public void setName(String name) {
        this.name = name;
    }
}

